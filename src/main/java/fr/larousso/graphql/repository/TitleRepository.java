package fr.larousso.graphql.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.larousso.graphql.model.Title;
import fr.larousso.graphql.model.TitleType;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.JSONB;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.list;

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

    public List<Title> titles(String name, TitleType type, Integer page, Integer size) {
        return this.dsl
                .resultQuery("""
                    select 
                        row_to_json(t)::jsonb ||
                            jsonb_build_object(
                                'distribution', {0},
                                'episodes', {1}
                            )
                    from title_basics t 
                    where t."originalTitle" = {2} and 
                            t."titleType" = {3}
                    offset {4} limit {5}
                    """,
                        distributionQuery(true),
                        episodesQuery(true),
                        name,
                        type.name(),
                        (page - 1) * size,
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

}
