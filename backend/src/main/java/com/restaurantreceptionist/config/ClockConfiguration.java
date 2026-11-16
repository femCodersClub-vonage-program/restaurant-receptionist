package com.restaurantreceptionist.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration(proxyBeanMethods = false)
public class ClockConfiguration {

	public static final ZoneId RESTAURANT_ZONE = ZoneId.of("Europe/Madrid");

	@Bean
	Clock clock() {
		return Clock.system(RESTAURANT_ZONE);
	}
}
