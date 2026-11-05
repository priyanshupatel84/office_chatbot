package com.office.office_chatbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class OfficeChatbotApplication {

	public static void main(String[] args) {
		SpringApplication.run(OfficeChatbotApplication.class, args);
	}

}
