package fr.larousso.graphql.api;


import fr.larousso.graphql.repository.TitleRepository;
import fr.larousso.graphql.model.Title;
import fr.larousso.graphql.model.TitleType;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

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
        return titleRepository.movies(name, type, page, size);
    }

//    @SchemaMapping
//    public JsonNode author(Book book) {
//        return Author.getById(book.authorId());
//    }
}
