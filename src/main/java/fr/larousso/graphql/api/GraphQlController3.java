package fr.larousso.graphql.api;


import fr.larousso.graphql.model.Title;
import fr.larousso.graphql.model.TitleType;
import fr.larousso.graphql.repository.TitleRepository;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class GraphQlController3 {

    private final TitleRepository titleRepository;

    public GraphQlController3(TitleRepository titleRepository) {
        this.titleRepository = titleRepository;
    }

    @QueryMapping
    public List<Title> titles(@Argument String name,
                              @Argument TitleType type,
                              @Argument Integer page,
                              @Argument Integer size,
                              DataFetchingEnvironment dataFetchingEnvironment) {
        return titleRepository.titles(name, type, page, size, dataFetchingEnvironment.getSelectionSet());
    }
}
