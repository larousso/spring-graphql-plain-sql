package fr.larousso.graphql.api;


import fr.larousso.graphql.model.Person;
import fr.larousso.graphql.model.Title;
import fr.larousso.graphql.model.TitleType;
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

//@Controller
public class GraphQlController2 {

    private final TitleRepository titleRepository;

    public GraphQlController2(TitleRepository titleRepository) {
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


    @BatchMapping
    public Map<Person, List<Title>> titles(List<Person> persons) {
        Map<String, List<Person>> personById = persons.stream().collect(Collectors.groupingBy(p -> p.nconst()));
        return titleRepository.titlesByPersons(persons.stream().map(p -> p.nconst())
                .collect(Collectors.toList()))
                .entrySet().stream().collect(Collectors.toMap(
                        entry -> personById.get(entry.getKey()).get(0),
                        entry -> entry.getValue()
                ));
    }

    @BatchMapping
    public Map<Person, List<Title>> knownFor(List<Person> personList) {
        Map<String, List<Person>> personById = personList.stream().collect(Collectors.groupingBy(p -> p.nconst()));
        return titleRepository.titlesKnowForByPersons(personList.stream().map(p -> p.nconst())
                .collect(Collectors.toList()))
                .entrySet().stream().collect(Collectors.toMap(
                        entry -> personById.get(entry.getKey()).get(0),
                        entry -> entry.getValue()
                ));
    }

}
