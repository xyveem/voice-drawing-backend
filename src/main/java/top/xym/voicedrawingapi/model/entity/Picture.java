package top.xym.voicedrawingapi.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("drawing")
public class Picture {
    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer userId;

    private String title;

    private byte[] content;

    private String voiceCommand;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleteFlag;
}