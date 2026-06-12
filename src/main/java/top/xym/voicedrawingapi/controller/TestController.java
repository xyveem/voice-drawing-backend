package top.xym.voicedrawingapi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "系统测试接⼝")
@RestController
public class TestController {
    @GetMapping("/test")
    @Operation(summary = "测试接⼝")
    public String test() {
        return "Hello ShareApp !";
    }
}