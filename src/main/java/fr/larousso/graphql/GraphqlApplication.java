package fr.larousso.graphql;

import fr.larousso.graphql.model.TitleType;
import graphql.schema.idl.EnumValuesProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;

import static fr.larousso.graphql.utils.DateTimeScalar.*;
import static graphql.schema.idl.TypeRuntimeWiring.newTypeWiring;

@SpringBootApplication
public class GraphqlApplication {

	public static void main(String[] args) {
		SpringApplication.run(GraphqlApplication.class, args);
	}

	@Bean
	public RuntimeWiringConfigurer runtimeWiringConfigurer() {
		return wiringBuilder -> {
			wiringBuilder
					.type(newTypeWiring("TitleType", typeBuilder ->
							typeBuilder.enumValues(name -> TitleType.fromName(name))
					))
					.scalar(localDateTimeScalarType())
					.scalar(localDateScalarType())
					.scalar(yearScalarType());

		};
	}
}
