package top.xym.voicedrawingapi.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class RestClientConfig {

    @Value("${app.http.connect-timeout:30}")
    private int connectTimeoutSeconds;

    @Value("${app.http.read-timeout:300}")
    private int readTimeoutSeconds;

    @Bean
    public RestClientCustomizer restClientCustomizer() {
        return restClientBuilder -> {
            restClientBuilder.requestFactory(ClientHttpRequestFactories.get(
                    ClientHttpRequestFactorySettings.DEFAULTS
                            .withConnectTimeout(Duration.ofSeconds(connectTimeoutSeconds))
                            .withReadTimeout(Duration.ofSeconds(readTimeoutSeconds))
            ));
        };
    }
}