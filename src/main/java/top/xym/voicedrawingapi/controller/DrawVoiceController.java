package top.xym.voicedrawingapi.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.xym.voicedrawingapi.common.result.Result;
import top.xym.voicedrawingapi.service.DrawVoiceService;

import java.util.Map;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class DrawVoiceController {

    private final DrawVoiceService drawVoiceService;

    @PostMapping("/voice")
    public Result<Map<String, Object>> voiceDraw(
            @RequestBody DrawVoiceService.DrawVoiceReq req,
            @RequestParam(defaultValue = "800") int canvasWidth,
            @RequestParam(defaultValue = "600") int canvasHeight
    ) {
        System.out.println("========== 收到语音绘图请求 ==========");
        System.out.println("req.getContent(): " + req.getContent());
        System.out.println("req.getAudio(): " + (req.getAudio() != null ? "有音频数据" : "null"));
        System.out.println("canvasWidth: " + canvasWidth);
        System.out.println("canvasHeight: " + canvasHeight);

        Map<String, Object> result = drawVoiceService.drawVoiceChat(req, canvasWidth, canvasHeight);
        System.out.println("返回结果: " + result);
        return Result.ok(result);
    }
}