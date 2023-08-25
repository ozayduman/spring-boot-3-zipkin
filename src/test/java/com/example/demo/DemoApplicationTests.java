package com.example.demo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.devtools.restart.RestartScope;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.stream.IntStream;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class DemoApplicationTests {

	@LocalServerPort
	int port;


	@Container
	//@RestartScope
	static GenericContainer<?> zipkinConatiner = new GenericContainer<>("openzipkin/zipkin:2.24-arm64")
			.withReuse(false)
			.withExposedPorts(9411)
			.waitingFor(Wait.forHttp("/api/v2/spans?serviceName=anything").withStartupTimeout(Duration.of(2, ChronoUnit.MINUTES )))
			.withNetwork(Network.SHARED)
			.withNetworkAliases("zipkin");


	@Test
	void testRestEndpoint() {
		System.err.println("http://localhost:%s/api/v2/spans?serviceName=spring-micrometer-tracing-demo".formatted(getPort()));
		RestTemplate restTemplate = new RestTemplate();
		IntStream.rangeClosed(0,99).forEach(i ->{
			restTemplate.getForObject("http://localhost:%s/status".formatted(port), String.class);
		});
		String status = restTemplate.getForObject("http://localhost:%s/status".formatted(port), String.class);
		Assertions.assertEquals("OK", status);
		String zipkinResult = restTemplate.getForObject("http://localhost:%s/api/v2/spans?serviceName=spring-micrometer-tracing-demo".formatted(getPort()), String.class);
		Assertions.assertFalse("[]".equals(zipkinResult));
		sleep(90);
	}

	private static void sleep(int sn) {
		try {
			Thread.sleep(sn * 1000);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	@DynamicPropertySource
	static void zipkinProperties(DynamicPropertyRegistry registry) {
		Integer p = getPort();
		registry.add("spring.zipkin.baseUrl",()-> "http://localhost:%s".formatted(p));
		registry.add("management.zipkin.tracing.endpoint", () -> "http://localhost:%s/api/v2/spans".formatted(p));
	}

	private static Integer getPort() {
		return zipkinConatiner.getMappedPort(9411);
	}

}
