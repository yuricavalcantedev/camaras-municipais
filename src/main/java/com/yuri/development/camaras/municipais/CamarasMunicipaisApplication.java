package com.yuri.development.camaras.municipais;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class CamarasMunicipaisApplication {

	public static void main(String[] args) {
		SpringApplication.run(CamarasMunicipaisApplication.class, args);
	}

}
