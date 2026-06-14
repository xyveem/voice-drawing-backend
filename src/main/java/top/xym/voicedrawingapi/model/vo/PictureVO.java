package top.xym.voicedrawingapi.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PictureVO {
    private Integer id;
    private String title;
    private String contentBase64;
    private String voiceCommand;
    private LocalDateTime createTime;
}
