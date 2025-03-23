package me.emmajiugo.spond;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class SpondApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpondApplication.class, args);
	}

}
