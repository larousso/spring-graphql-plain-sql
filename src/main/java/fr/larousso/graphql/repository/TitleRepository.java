package fr.larousso.graphql.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.larousso.graphql.model.Title;
import fr.larousso.graphql.model.TitleType;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.DataFetchingFieldSelectionSet;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.JSONB;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.*;
import static org.jooq.impl.DSL.val;

@Component
public class TitleRepository {

    private final ObjectMapper mapper;
    private final DSLContext dsl;

    public TitleRepository(ObjectMapper mapper, DSLContext dsl) {
        this.mapper = mapper;
        this.dsl = dsl;
    }

    public Map<String, List<Title>> titlesByPersons(List<String> ids) {
        record PersonIdAndTitle(String id, Title title) {}
        return this.dsl
                .resultQuery("""
                    select nb.nconst, row_to_json(tb)::jsonb
                    from name_basics nb
                    join title_principals tp on tp.nconst = nb.nconst
                    join title_basics tb on tb.tconst = tp.tconst
                    where nb.nconst in ({0})                      
                    """,
                    list(ids.stream().map(DSL::val).toList())
                )
                .stream().map(record -> {
                    String id = record.get(0, String.class);
                    Title title = fromJson(record.get(1, JSONB.class));
                    return new PersonIdAndTitle(id, title);
                })
                .collect(Collectors.groupingBy((PersonIdAndTitle k) -> k.id))
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().stream().map(personIdAndTitle -> personIdAndTitle.title()).toList()));
    }

    public Map<String, List<Title>> titlesKnowForByPersons(List<String> ids) {
        record PersonIdAndTitle(String id, Title title) {}
        return this.dsl
                .resultQuery("""
                        select nb.nconst, row_to_json(tb)::jsonb
                        from name_basics nb
                        cross join LATERAL UNNEST(nb."knownForTitles") known(tconst)
                        join title_basics tb on tb.tconst = known.tconst
                        where nb.nconst in ({0})                    
                        """,
                        list(ids.stream().map(DSL::val).toList())
                )
                .stream().map(record -> {
                    String id = record.get(0, String.class);
                    Title title = fromJson(record.get(1, JSONB.class));
                    return new PersonIdAndTitle(id, title);
                })
                .collect(Collectors.groupingBy((PersonIdAndTitle k) -> k.id))
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().stream().map(personIdAndTitle -> personIdAndTitle.title()).toList()));
    }



    public List<Title> titles(String name, TitleType type, Integer page, Integer size, DataFetchingFieldSelectionSet environment) {
        QueryNode queryNode = QueryNode.create(environment);
        return this.dsl
                .resultQuery("""
                    select {0}
                    from title_basics t 
                    where t."originalTitle" = {1} and 
                            t."titleType" = {2}
                    offset {3} limit {4}
                    """,
                        queryNode.select(field("t", String.class)),
                        name,
                        type.name(),
                        (Objects.requireNonNullElse(page, 1) - 1) * Objects.requireNonNullElse(size, 20),
                        size
                )
                .fetch(0, JSONB.class)
                .stream().map(this::fromJson)
                .toList();
    }
    public List<Title> titles(String name, TitleType type, Integer page, Integer size, boolean queryCast, boolean queryEpisode) {
        return this.dsl
                .resultQuery("""
                    select 
                        row_to_json(t)::jsonb ||
                            jsonb_build_object(
                                'cast', {0},
                                'episodes', {1}
                            )
                    from title_basics t 
                    where t."originalTitle" = {2} and 
                            t."titleType" = {3}
                    offset {4} limit {5}
                    """,
                        distributionQuery(queryCast),
                        episodesQuery(queryEpisode),
                        name,
                        type.name(),
                        (Objects.requireNonNullElse(page, 1) - 1) * Objects.requireNonNullElse(size, 20),
                        size
                )
                .fetch(0, JSONB.class)
                .stream().map(this::fromJson)
                .toList();
    }

    Field<JSONB> distributionQuery(boolean queryDistribution) {
        if (queryDistribution) {
            //language=postgresql
            return field("""
                    array_to_json(array(
                        select row_to_json(tp)::jsonb ||
                            jsonb_build_object('person', row_to_json(nb))
                        from title_principals tp
                         join name_basics nb on nb.nconst = tp.nconst
                        where tp.tconst = t.tconst
                    ))
                    """, JSONB.class);
        } else {
            //language=postgresql
            return field("""
                    json_build_array()
                    """, JSONB.class);
        }
    }

    Field<JSONB> episodesQuery(boolean queryEpisodes) {
        if (queryEpisodes) {
            return field("""
                    array_to_json(array(
                        select row_to_json(te)::jsonb ||
                            jsonb_build_object('show', row_to_json(tbe))
                        from title_episode te
                        join title_basics tbe on tbe.tconst = te.const
                        where te."parentTconst" = t.tconst
                    ))
                    """, JSONB.class);
        } else {
            return field("""
                    json_build_array()
                    """, JSONB.class);
        }
    }


    Title fromJson(JSONB json) {
        try {
            return mapper.readValue(json.data(), Title.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Title> titlesByPersonsAndType(String nconst, TitleType type) {
        Condition condition = Objects.nonNull(type) ?
                field("tb.\"titleType\"", String.class).eq(type.getName()) :
                val(1).eq(val(1));

        return this.dsl
                .resultQuery("""
                    select row_to_json(tb)::jsonb
                    from name_basics nb
                    join title_principals tp on tp.nconst = nb.nconst
                    join title_basics tb on tb.tconst = tp.tconst
                    where nb.nconst = {0} and {1}                      
                    """, nconst, condition
                )
                .stream().map(record -> {
                    Title title = fromJson(record.get(0, JSONB.class));
                    return title;
                })
                .toList();
    }

    public List<Title> titlesKnownForByPersons(String nconst) {
        return this.dsl
                .resultQuery("""
                        select row_to_json(tb)::jsonb
                        from name_basics nb
                        cross join LATERAL UNNEST(nb."knownForTitles") known(tconst)
                        join title_basics tb on tb.tconst = known.tconst
                        where nb.nconst = {0}                    
                        """,
                        nconst
                )
                .stream().map(record -> fromJson(record.get(0, JSONB.class)))
                .toList();
    }
}
