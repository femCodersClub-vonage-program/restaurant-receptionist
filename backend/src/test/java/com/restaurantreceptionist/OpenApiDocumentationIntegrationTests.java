package com.restaurantreceptionist;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Import({
		TestcontainersConfiguration.class,
		OpenApiDocumentationIntegrationTests.SmokeTestController.class
})
@SpringBootTest(properties = {
		"springdoc.api-docs.enabled=true",
		"springdoc.swagger-ui.enabled=true"
})
@AutoConfigureMockMvc
class OpenApiDocumentationIntegrationTests {

	private static final String API_DESCRIPTION =
			"Generated documentation for the fictional Restaurant Receptionist demo API. "
					+ "Operations reflect implemented backend controllers and will expand as endpoints are added.";

	@Autowired
	private MockMvc mockMvc;

	@Test
	void generatedOpenApiDocumentIncludesMetadataAndMvcOperations() throws Exception {
		mockMvc.perform(get("/v3/api-docs"))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.info.title").value("Restaurant Receptionist API"))
				.andExpect(jsonPath("$.info.version").value("0.0.1-SNAPSHOT"))
				.andExpect(jsonPath("$.info.description").value(API_DESCRIPTION))
				.andExpect(jsonPath("$.paths").isMap())
				.andExpect(jsonPath("$.paths['/openapi-smoke-test'].get").exists());
	}

	@Test
	void swaggerUiIsAvailable() throws Exception {
		mockMvc.perform(get("/swagger-ui/index.html"))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML));
	}

	@RestController
	static class SmokeTestController {

		@GetMapping("/openapi-smoke-test")
		String smokeTest() {
			return "ok";
		}
	}
}
