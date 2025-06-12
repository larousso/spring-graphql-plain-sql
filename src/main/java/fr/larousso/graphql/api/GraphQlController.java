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
    public List<Title> movies(@Argument String name,
                              @Argument TitleType type,
                              @Argument Integer page,
                              @Argument Integer size,
                              DataFetchingEnvironment dataFetchingEnvironment) {
        return titleRepository.titles(name, type, page, size);
    }


    @BatchMapping
    public Map<Person, List<Title>> titles(List<Person> personList) {
        Map<String, List<Person>> personById = personList.stream().collect(Collectors.groupingBy(p -> p.nconst()));
        return titleRepository.titlesByPersons(personList.stream().map(p -> p.nconst())
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

//    @SchemaMapping(typeName = "Person", field = "titles")
//    public List<Title> titlesWithFilter(@Argument("type") TitleType type, DataFetchingEnvironment dataFetchingEnvironment) {
//        Person person = dataFetchingEnvironment.getSource();
//        return person.titles()
//                .stream()
//                .filter(t -> t.titleType().equals(type))
//                .toList();
//    }
}
