package fr.larousso.graphql;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;

import static fr.larousso.graphql.utils.DateTimeScalar.*;

@SpringBootApplication
public class GraphqlApplication {

	public static void main(String[] args) {
		SpringApplication.run(GraphqlApplication.class, args);
	}

	@Bean
	public RuntimeWiringConfigurer runtimeWiringConfigurer() {
		return wiringBuilder -> {
			wiringBuilder
					.scalar(localDateTimeScalarType())
					.scalar(localDateScalarType())
					.scalar(yearScalarType());

		};
	}
}
