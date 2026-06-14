package top.xym.voicedrawingapi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.xym.voicedrawingapi.common.result.Result;
import top.xym.voicedrawingapi.model.vo.UserInfoVO;
import top.xym.voicedrawingapi.service.UserService;

@Slf4j
@RestController
@RequestMapping("/user")
@AllArgsConstructor
@Tag(name = "⽤户接⼝")
public class UserController {
    private final UserService userService;
    @GetMapping("info")
    @Operation(summary = "查询⽤户信息")
    public Result<UserInfoVO> userInfo() {
        return Result.ok(userService.userInfo());
    }
}