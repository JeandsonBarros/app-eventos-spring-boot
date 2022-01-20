package com.appEventos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan({"com.appEventos.controllers", "com.appEventos.security"})

@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
/*A parte importante é: @SpringBootApplication(exclude = { SecurityAutoConfiguration.class }). 
 * Isso desativa a configuração automática dos recursos de segurança do Spring.*/

public class AppEventosApplication {

	public static void main(String[] args) {
		SpringApplication.run(AppEventosApplication.class, args);
		
	}

}
