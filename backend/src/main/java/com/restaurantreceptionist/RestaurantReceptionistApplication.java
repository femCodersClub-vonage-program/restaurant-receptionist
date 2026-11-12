package com.restaurantreceptionist;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(
		title = "Restaurant Receptionist API",
		version = "0.0.1-SNAPSHOT",
		description = "Generated documentation for the fictional Restaurant Receptionist demo API. "
				+ "Operations reflect implemented backend controllers and will expand as endpoints are added."))
public class RestaurantReceptionistApplication {

	public static void main(String[] args) {
		SpringApplication.run(RestaurantReceptionistApplication.class, args);
	}

}
