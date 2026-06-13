package top.xym.voicedrawingapi.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(description = "⽤户信息")
public class UserInfoVO implements Serializable {
    @Serial
    private static final long serialVersionUID = -45095106764580159L;
    @Schema(description = "主键")
    private Integer id;
    @Schema(description = "头像")
    private String avatar;
    @Schema(description = "昵称")
    private String nickname;
}