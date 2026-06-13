package top.xym.voicedrawingapi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.xym.voicedrawingapi.common.result.Result;
import top.xym.voicedrawingapi.model.vo.UserLoginVO;
import top.xym.voicedrawingapi.service.AuthService;

@RestController
@RequestMapping("/auth")
@Tag(name = "认证接⼝")
@AllArgsConstructor
public class AuthController {
    private final AuthService authService;
    @PostMapping("/login")
    @Operation(summary = "⼿机号登录")
    public Result<UserLoginVO> loginByPhone(@RequestParam("phone") String
                                                    phone, @RequestParam("code") String code) {
        return Result.ok(authService.loginByPhone(phone, code));
    }
}