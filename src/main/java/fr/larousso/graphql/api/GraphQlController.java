package fr.larousso.graphql.api;


import fr.larousso.graphql.model.*;
import fr.larousso.graphql.repository.TitleRepository;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class GraphQlController {

    private final TitleRepository titleRepository;

    public GraphQlController(TitleRepository titleRepository) {
        this.titleRepository = titleRepository;
    }

    @QueryMapping
    public List<Title> titles(@Argument String name,
                              @Argument TitleType type,
                              @Argument Integer page,
                              @Argument Integer size,
                              DataFetchingEnvironment dataFetchingEnvironment) {
        boolean queryCast = dataFetchingEnvironment.getSelectionSet().contains("cast");
        boolean queryEpisodes = dataFetchingEnvironment.getSelectionSet().contains("episodes");
        return titleRepository.titles(name, type, page, size, queryCast, queryEpisodes);
    }


    @SchemaMapping
    public List<Title> titles(Person person, @Argument TitleType type) {
        return titleRepository.titlesByPersonsAndType(person.nconst(), type);
    }


    @SchemaMapping
    public List<Title> knownFor(Person person) {
        return titleRepository.titlesKnownForByPersons(person.nconst());
    }

}
