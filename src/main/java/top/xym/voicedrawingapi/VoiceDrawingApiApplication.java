package top.xym.voicedrawingapi;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan(basePackages = {"top.xym.voicedrawingapi.mapper"})
public class VoiceDrawingApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(VoiceDrawingApiApplication.class, args);
    }

}
