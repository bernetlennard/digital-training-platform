package ch.bernet.trainingplatform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.Clock;

@SpringBootApplication
public class TrainingplatformApplication {

	public static void main(String[] args) {
		SpringApplication.run(TrainingplatformApplication.class, args);
	}

	@Bean
	public Clock clock() {
		// TODO: Clock mit Zeitzone Europe/Zurich zurückgeben (wird in Services injiziert)
		return null;
	}

}
