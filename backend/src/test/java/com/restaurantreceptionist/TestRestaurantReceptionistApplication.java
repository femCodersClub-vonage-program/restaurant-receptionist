package com.restaurantreceptionist;

import org.springframework.boot.SpringApplication;

public class TestRestaurantReceptionistApplication {

	public static void main(String[] args) {
		SpringApplication.from(RestaurantReceptionistApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
