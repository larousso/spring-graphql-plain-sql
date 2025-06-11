package fr.larousso.graphql.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.larousso.graphql.model.Title;
import fr.larousso.graphql.model.TitleType;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.JSONB;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.jooq.impl.DSL.field;

@Component
public class TitleRepository {

    private final ObjectMapper mapper;
    private final DSLContext dsl;

    public TitleRepository(ObjectMapper mapper, DSLContext dsl) {
        this.mapper = mapper;
        this.dsl = dsl;
    }

    public List<Title> movies(String name, TitleType type, Integer page, Integer size) {
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
                .stream().map(j ->
                        {
                            try {
                                return mapper.readValue(j.data(), Title.class);
                            } catch (JsonProcessingException e) {
                                throw new RuntimeException(e);
                            }
                        }
                )
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

}
