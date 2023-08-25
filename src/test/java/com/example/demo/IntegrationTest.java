package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestTemplate;

import java.util.stream.IntStream;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class IntegrationTest {


    @LocalServerPort
    int port;


    @Test
    void testZipkin() {
        RestTemplate restTemplate = new RestTemplate();
        IntStream.rangeClosed(0,99).forEach(i ->{
            String status = restTemplate.getForObject("http://localhost:%s/status".formatted(port), String.class);
            System.err.println(status);
        });
    }

    @DynamicPropertySource
    static void zipkinProperties(DynamicPropertyRegistry registry) {
        Integer p = 9411;
        registry.add("management.endpoints.web.exposure.include", ()-> "health,info,prometheus");
        registry.add("spring.zipkin.baseUrl",()-> "http://localhost:%s".formatted(p));
        registry.add("management.zipkin.tracing.endpoint", () -> "http://localhost:%s/api/v2/spans".formatted(p));
    }
}
