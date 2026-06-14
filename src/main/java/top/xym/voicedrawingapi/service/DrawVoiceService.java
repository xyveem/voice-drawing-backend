package top.xym.voicedrawingapi.service;

import cn.hutool.core.io.IoUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.http.ProtocolType;
import com.aliyuncs.profile.DefaultProfile;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DrawVoiceService {

    private final ChatModel chatModel;

    @Value("${spring.ai.dashscope.api-key}")
    private String apiKey;

    @Value("${aliyun.oss.accessKeyId}")
    private String accessKeyId;

    @Value("${aliyun.oss.accessKeySecret}")
    private String accessKeySecret;

    @Value("${aliyun.nls.app-key}")
    private String NLS_APP_KEY;

    private ChatClient chatClient;
    private String nlsToken;
    private long tokenExpireTime;

    @jakarta.annotation.PostConstruct
    public void init() {
        this.chatClient = ChatClient.builder(chatModel).build();
    }

    // ====================== 对外暴露绘图语音接口 ======================
    public Map<String, Object> drawVoiceChat(DrawVoiceReq request, int canvasWidth, int canvasHeight) {
        String recognizedText = null;
        System.out.println("=== 开始处理 ===");

        // 1、ASR语音识别
        if (StringUtils.isNotBlank(request.getAudio())) {
            System.out.println("开始语音识别...");
            recognizedText = qwenAsrByHttp(request.getAudio());
            request.setContent(recognizedText);
            System.out.println("识别结果: " + recognizedText);
        }
        if (StringUtils.isBlank(request.getContent())) {
            throw new RuntimeException("未识别到有效语音内容");
        }

        // 2、构造绘图专属Prompt，调用LLM
        System.out.println("开始构造Prompt...");
        Prompt prompt = buildDrawPrompt(request.getContent(), canvasWidth, canvasHeight);
        String llmRawResp = chatClient.prompt(prompt).call().content();

        // 清洗LLM返回内容
        llmRawResp = cleanLLMResponse(llmRawResp);

        // 解析JSON
        String replyTextFromLLM = "";
        String drawDesc = "";
        List<DrawStep> stepList = new ArrayList<>();

        try {
            // 截取有效的JSON部分
            String validJson = extractValidJson(llmRawResp);
            if (StringUtils.isNotBlank(validJson)) {
                JSONObject rootObj = JSON.parseObject(validJson);
                replyTextFromLLM = rootObj.getString("replyText");
                drawDesc = rootObj.getString("drawDesc");

                JSONArray stepsArray = rootObj.getJSONArray("drawSteps");
                if (stepsArray != null) {
                    stepList = stepsArray.toJavaList(DrawStep.class);
                }

                // 处理绘图步骤，转换坐标
                processDrawSteps(stepList, canvasWidth, canvasHeight);
            }
        } catch (Exception e) {
            stepList = new ArrayList<>();
            System.err.println("LLM返回JSON解析异常: " + e.getMessage());
            System.err.println("原始内容: " + llmRawResp);
        }

        // 3、TTS生成播报语音
        String replyText;
        String ttsTip;
        if (stepList.isEmpty()) {
            replyText = "未识别到有效的绘图指令，请重新口述绘图要求";
            ttsTip = replyText;
        } else {
            replyText = StringUtils.isNotBlank(replyTextFromLLM) ? replyTextFromLLM : "解析完成，开始绘制";
            ttsTip = replyText;
        }
        byte[] audioBytes = textToSpeech(ttsTip);
        String audioBase64 = Base64.getEncoder().encodeToString(audioBytes);

        // 4、组装返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("recognizedText", recognizedText);
        result.put("drawSteps", stepList);
        result.put("audio", audioBase64);
        result.put("replyText", replyText);
        result.put("drawDesc", drawDesc);

        System.out.println("返回步骤数: " + stepList.size());
        return result;
    }

    // 清洗LLM返回内容
    private String cleanLLMResponse(String raw) {
        if (raw == null) {
            return "";
        }
        // 移除markdown代码块标记
        String result = raw.replaceAll("```(json)?\\n?", "").replaceAll("```", "");
        // 剔除不可见控制字符
        result = result.replaceAll("[\\x00-\\x1F\\x7F]", "");
        // 压缩空白
        result = result.replaceAll("\\s+", " ").trim();
        return result;
    }

    // 提取有效JSON
    private String extractValidJson(String text) {
        int jsonStart = text.indexOf("{");
        int jsonEnd = text.lastIndexOf("}");
        if (jsonStart != -1 && jsonEnd > jsonStart) {
            return text.substring(jsonStart, jsonEnd + 1);
        }
        return "";
    }

    // 处理绘图步骤，转换坐标
    private void processDrawSteps(List<DrawStep> stepList, int canvasWidth, int canvasHeight) {
        for (DrawStep step : stepList) {
            // 兜底默认值
            if (step.getColor() == null) {
                step.setColor("#000000");
            }
            if (step.getLineWidth() == null) {
                step.setLineWidth(3);
            }

            String type = step.getType();
            if (type == null) {
                continue;
            }

            switch (type) {
                case "free":
                    if (step.getPoints() != null) {
                        for (PointItem pi : step.getPoints()) {
                            if (pi.getXPercent() != null) {
                                pi.setX(percent2Px(pi.getXPercent(), canvasWidth));
                                pi.setY(percent2Px(pi.getYPercent(), canvasHeight));
                            }
                        }
                    }
                    break;

                case "circle":
                    // 兼容圆心+半径格式
                    if (step.getCenterXPercent() != null && step.getRadiusPercent() != null) {
                        double r = step.getRadiusPercent();
                        double cx = step.getCenterXPercent();
                        double cy = step.getCenterYPercent();
                        step.setStartXPercent(cx - r);
                        step.setStartYPercent(cy - r);
                        step.setEndXPercent(cx + r);
                        step.setEndYPercent(cy + r);
                    }
                    // 转换为像素坐标
                    step.setStartX(percent2Px(step.getStartXPercent(), canvasWidth));
                    step.setStartY(percent2Px(step.getStartYPercent(), canvasHeight));
                    step.setEndX(percent2Px(step.getEndXPercent(), canvasWidth));
                    step.setEndY(percent2Px(step.getEndYPercent(), canvasHeight));
                    break;

                case "line":
                case "rectangle":
                    step.setStartX(percent2Px(step.getStartXPercent(), canvasWidth));
                    step.setStartY(percent2Px(step.getStartYPercent(), canvasHeight));
                    step.setEndX(percent2Px(step.getEndXPercent(), canvasWidth));
                    step.setEndY(percent2Px(step.getEndYPercent(), canvasHeight));
                    break;

                default:
                    break;
            }
        }
    }

    // ====================== 构造绘图LLM提示词 ======================
    private Prompt buildDrawPrompt(String userInput, int w, int h) {
        List<Message> messages = new ArrayList<>();
        String sysPrompt = """
                你是简笔画拆解绘图生成器，严格按照卡通小动物结构拆分绘图元素，只输出标准JSON，禁止额外文字、markdown、注释。
                画布宽%d、高%d，坐标0~100百分比，X强制20~80、Y强制15~85，全部图形居中排布，不许越界。
                可用type仅：free、line、rectangle、circle。

                ### JSON固定结构（字段不能增删改名）
                {
                  "replyText": "简短回复用户绘图指令",
                  "drawDesc": "完整描述简笔画结构：头部、耳朵、眼睛、鼻子、嘴巴、身体、四肢、尾巴各自位置与比例",
                  "drawSteps": [独立绘图元素数组]
                }

                ### 绘图元素强制拆分规则（核心！）
                1. 画小狗必须拆成**10个独立绘图对象**依次放入drawSteps数组：
                   ① free：外轮廓大圆脑袋（12~18个坐标点，闭合平滑椭圆）
                   ② free：左耳朵（三角闭合曲线）
                   ③ free：右耳朵（三角闭合曲线）
                   ④ circle：左眼（实心小圆）
                   ⑤ circle：右眼（实心小圆）
                   ⑥ circle：鼻头（更小实心圆）
                   ⑦ free：微笑嘴巴弧线
                   ⑧ rectangle：矩形身体（在脑袋正下方，垂直间隔3个百分比）
                   ⑨ line：四条腿（前腿2根、后腿2根垂直直线，底部对齐画布下方）
                   ⑩ free：卷曲尾巴（身体右侧后方向外弯曲弧线）

                2. 字段填写规则：
                - free：必填points数组，一组{xPercent,yPercent}，必须闭合首尾坐标一致；
                - circle：二选一写法：(centerXPercent+centerYPercent+radiusPercent) 或 start/end百分比；
                - line、rectangle：必须填startX/Y、endX/Y百分比；
                - color默认#000000，lineWidth固定3，不许空。

                ### 硬性强制约束
                1. drawSteps数组长度固定≥10，每个结构部件单独一条对象，绝不合并、绝不遗漏四肢、耳朵、尾巴；
                2. 完整闭合JSON，无缺失括号、数组末尾无多余逗号；
                3. 所有部件上下分层：脑袋在上、身体紧跟脑袋下方、四条腿在身体底部、尾巴在身体后侧，垂直排布不重叠混乱；
                4. 曲线点位充足，线条圆润可爱，不要零散孤立圆圈、短线。
                """.formatted(w, h);

        messages.add(new SystemMessage(sysPrompt));
        messages.add(new UserMessage(userInput));
        return new Prompt(messages);
    }

    // 百分比转像素
    private double percent2Px(Double percent, int total) {
        if (percent == null) {
            return 0d;
        }
        return percent / 100.0 * total;
    }

    // ====================== 阿里云ASR语音识别 ======================
    private String qwenAsrByHttp(String base64Audio) {
        try {
            String pureBase64 = base64Audio.contains(",") ? base64Audio.split(",")[1] : base64Audio;
            byte[] audioBytes = Base64.getDecoder().decode(pureBase64);
            String token = getNlsToken();

            String asrUrl = "https://nls-gateway.cn-shanghai.aliyuncs.com/stream/v1/asr"
                    + "?appkey=" + NLS_APP_KEY
                    + "&format=mp3"
                    + "&sample_rate=16000";

            HttpURLConnection conn = (HttpURLConnection) new URL(asrUrl).openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(30000);
            conn.setDoOutput(true);

            conn.setRequestProperty("Host", "nls-gateway-cn-shanghai.aliyuncs.com");
            conn.setRequestProperty("X-NLS-Token", token);
            conn.setRequestProperty("Content-Type", "application/octet-stream");
            conn.setRequestProperty("Content-Length", String.valueOf(audioBytes.length));

            try (OutputStream os = conn.getOutputStream()) {
                os.write(audioBytes);
                os.flush();
            }

            int code = conn.getResponseCode();
            InputStream in = code == 200 ? conn.getInputStream() : conn.getErrorStream();
            String result = IoUtil.read(in, StandardCharsets.UTF_8);

            JSONObject json = JSON.parseObject(result);
            if ("20000000".equals(json.getString("status"))) {
                return json.getString("result");
            } else {
                throw new RuntimeException("ASR识别失败：" + result);
            }
        } catch (Exception e) {
            throw new RuntimeException("语音识别异常", e);
        }
    }

    // ====================== 阿里云TTS语音合成 ======================
    public byte[] textToSpeech(String text) {
        try {
            String token = getNlsToken();
            URL url = new URL("https://nls-gateway.cn-shanghai.aliyuncs.com/stream/v1/tts");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json;charset=utf-8");
            conn.setRequestProperty("X-NLS-Token", token);
            conn.setDoOutput(true);

            JSONObject params = new JSONObject();
            params.put("appkey", NLS_APP_KEY);
            params.put("text", text);
            params.put("voice", "xiaoyun");
            params.put("format", "mp3");
            params.put("sample_rate", 16000);
            params.put("volume", 50);
            params.put("speed", 0);
            params.put("pitch", 0);

            conn.getOutputStream().write(params.toString().getBytes(StandardCharsets.UTF_8));

            if (conn.getResponseCode() != 200) {
                return new byte[0];
            }

            try (InputStream in = conn.getInputStream()) {
                return in.readAllBytes();
            }
        } catch (Exception e) {
            return new byte[0];
        }
    }

    // ====================== 获取NLS Token ======================
    public String getNlsToken() {
        if (nlsToken != null && System.currentTimeMillis() < tokenExpireTime) {
            return nlsToken;
        }
        try {
            DefaultProfile profile = DefaultProfile.getProfile("cn-shanghai", accessKeyId, accessKeySecret);
            IAcsClient client = new DefaultAcsClient(profile);

            CommonRequest request = new CommonRequest();
            request.setDomain("nls-meta.cn-shanghai.aliyuncs.com");
            request.setVersion("2019-02-28");
            request.setAction("CreateToken");
            request.setMethod(MethodType.GET);
            request.setProtocol(ProtocolType.HTTPS);

            CommonResponse response = client.getCommonResponse(request);
            JSONObject json = JSON.parseObject(response.getData());

            nlsToken = json.getJSONObject("Token").getString("Id");
            long expire = json.getJSONObject("Token").getLong("ExpireTime");
            tokenExpireTime = expire * 1000 - 120000;

            return nlsToken;
        } catch (Exception e) {
            throw new RuntimeException("获取NLS Token失败", e);
        }
    }

    // ====================== 内部实体类 ======================
    @Data
    public static class DrawVoiceReq {
        private String audio;
        private String content;
    }

    @Data
    public static class DrawStep {
        private String type;
        private String color;
        private Integer lineWidth;
        private List<PointItem> points;
        // 百分比坐标
        private Double startXPercent;
        private Double startYPercent;
        private Double endXPercent;
        private Double endYPercent;
        // 像素坐标
        private Double startX;
        private Double startY;
        private Double endX;
        private Double endY;
        // 圆心+半径格式（兼容）
        private Double centerXPercent;
        private Double centerYPercent;
        private Double radiusPercent;
    }

    @Data
    public static class PointItem {
        private Double xPercent;
        private Double yPercent;
        private Double x;
        private Double y;
    }
}