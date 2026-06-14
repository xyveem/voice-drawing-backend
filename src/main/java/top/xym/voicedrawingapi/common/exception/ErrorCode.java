package top.xym.voicedrawingapi.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    UNAUTHORIZED(401, "登录失效，请重新登录"),
    INTERNAL_SERVER_ERROR(500, "服务器异常，请稍后再试"),
    CODE_SEND_FAIL(3002, "短信发送失败"),
    PARAMS_ERROR(3003, "参数异常"),
    USER_NOT_EXIST(3006, "⽤户不存在"),
    LOGIN_STATUS_EXPIRE(3001, "登录过期"),
    SMS_CODE_ERROR(3004, "短信验证码错误"),

    DATA_NOT_EXIST(3007, "画作数据不存在"),
    NO_PERMISSION(3008, "无权限操作他人画作"),
    FILE_EMPTY(3009, "上传图片不能为空"),
    FILE_PARSE_FAIL(3010, "图片二进制解析失败");

    private final int code;
    private final String msg;
}
