package com.de.gmproxy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@EnableConfigurationProperties(UriConfiguration.class)
@RestController
public class GmProxyApplication {
	public static void main(String[] args) {
		SpringApplication.run(GmProxyApplication.class, args);
	}

	@Bean
	public RouteLocator routes(RouteLocatorBuilder builder, ModifyReqHeaderGatewayFilterFactory modifyReqHeaderFactory,
			UriConfiguration uriConfiguration) {
		// System.out.println("uriConfiguration.getHttpbin(): " +
		// uriConfiguration.getHttpbin());
		return builder.routes()
				.route(r -> r.alwaysTrue().filters(
						f -> f.filter(modifyReqHeaderFactory.apply(new ModifyReqHeaderGatewayFilterFactory.Config())))
						.uri(uriConfiguration.getHttpbin()))
				.build();
	}
}

@ConfigurationProperties(prefix = "forward")
class UriConfiguration {
	private String httpbin = "http://httpbin.org:80";

	public String getHttpbin() {
		return httpbin;
	}

	public void setHttpbin(String httpbin) {
		this.httpbin = httpbin;
	}
}
