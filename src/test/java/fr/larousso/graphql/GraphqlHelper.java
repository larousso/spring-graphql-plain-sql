package fr.larousso.graphql;

import graphql.ExecutionInput;
import graphql.GraphQL;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLSchema;
import graphql.schema.StaticDataFetcher;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;

import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static graphql.schema.idl.RuntimeWiring.newRuntimeWiring;

public class GraphqlHelper {
    static class CaptureDataFetcher<T> implements DataFetcher {

        private final AtomicReference<T> result = new AtomicReference<>();
        private final Function<DataFetchingEnvironment, T> function;

        CaptureDataFetcher(Function<DataFetchingEnvironment, T> function) {
            this.function = function;
        }

        @Override
        public Object get(DataFetchingEnvironment environment) throws Exception {
            result.set(function.apply(environment));
            return null;
        }

        T getResult() {
            return result.get();
        }
    }


    private static RuntimeWiring buildRuntimeWiring(DataFetcher captureDataFetcher) {
        GraphqlApplication application = new GraphqlApplication();
        RuntimeWiring.Builder builder = newRuntimeWiring();
        application.runtimeWiringConfigurer().configure(builder);
        return builder
                .type("Query", typeWiring -> typeWiring
                        .dataFetcher("titles", captureDataFetcher)
                )
                .build();
    }


    public static <T> T captureDataFetchingEnvironmentForQuery(String query, Function<DataFetchingEnvironment, T> function) {

        SchemaParser schemaParser = new SchemaParser();
        SchemaGenerator schemaGenerator = new SchemaGenerator();

        var files = List.of("schema.graphqls");

        TypeDefinitionRegistry typeRegistry = new TypeDefinitionRegistry();
        files.stream().map(f -> {
                    String filePath = GraphqlHelper.class.getResource("/graphql/" + f).getFile();
                    File file = new File(filePath);
                    return file;
                })
                .forEach(f -> {
                    typeRegistry.merge(schemaParser.parse(f));
                });

        CaptureDataFetcher<T> captureDataFetcher = new CaptureDataFetcher<T>(function);
        GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeRegistry, buildRuntimeWiring(captureDataFetcher));

        GraphQL graphQL = GraphQL.newGraphQL(graphQLSchema).build();

        ExecutionInput executionInput = new ExecutionInput.Builder()
                .query(query)
                .build();

        graphQL.execute(executionInput);
        return captureDataFetcher.getResult();
    }
}
