package fr.larousso.graphql.repository;

import fr.larousso.graphql.model.TitleType;
import graphql.schema.DataFetchingFieldSelectionSet;
import graphql.schema.SelectedField;
import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.JSONB;
import org.jooq.impl.DSL;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.val;

public sealed interface QueryNode {

    Field<JSONB> select(Field<String> selectAlias);


    static QueryNode create(DataFetchingFieldSelectionSet dataFetchingFieldSelectionSet) {
        return createTitle(1, dataFetchingFieldSelectionSet.getFields());
    }

    private static PersonNode createPerson(Integer level, List<SelectedField> selectedFields) {
        Optional<SelectedField> titles = selectedFields.stream().filter(f -> f.getName().equals("titles")).findFirst();
        Optional<SelectedField> knownFor = selectedFields.stream().filter(f -> f.getName().equals("knownFor")).findFirst();
        return new PersonNode(
                level,
                knownFor.map(c -> createTitle(c.getLevel(), c.getSelectionSet().getFields())).orElse(null),
                titles.map(c -> createTitle(c.getLevel(), c.getSelectionSet().getFields())).orElse(null),
                titles.map(t -> (TitleType) t.getArguments().get("type")).orElse(null)
        );
    }

    private static RoleNode createCast(Integer level, List<SelectedField> selectedFields) {
        Optional<SelectedField> person = selectedFields.stream().filter(f -> f.getName().equals("person")).findFirst();
        return new RoleNode(level, person.map(c -> createPerson(c.getLevel(), c.getSelectionSet().getFields())).orElse(null));
    }

    private static EpisodeNode createEpisodes(Integer level, List<SelectedField> selectedFields) {
        Optional<SelectedField> show = selectedFields.stream().filter(f -> f.getName().equals("show")).findFirst();
        return new EpisodeNode(level, show.map(c -> createTitle(c.getLevel(), c.getSelectionSet().getFields())).orElse(null));
    }

    private static TitleNode createTitle(Integer level, List<SelectedField> selectedFields) {
        Optional<SelectedField> cast = selectedFields.stream().filter(f -> f.getName().equals("cast")).findFirst();
        Optional<SelectedField> episodes = selectedFields.stream().filter(f -> f.getName().equals("episodes")).findFirst();
        return new TitleNode(level,
                cast.map(c -> createCast(c.getLevel(), c.getSelectionSet().getFields())).orElse(null),
                episodes.map(c -> createEpisodes(c.getLevel(), c.getSelectionSet().getFields())).orElse(null)
        );
    }


    record TitleNode(Integer level, RoleNode castNode, EpisodeNode episodeNode) implements QueryNode {
        @Override
        public Field<JSONB> select(Field<String> selectAlias) {

            Field<JSONB> castField = Optional.ofNullable(castNode).map(n -> {
                Field<String> castAlias = field("cast"+level, String.class);
                return field("""
                        jsonb_build_object('cast', array_to_json(array(
                            select {0}
                            from title_principals {1}
                            where {1}.tconst = {2}.tconst
                        )))
                        """, JSONB.class, n.select(castAlias), castAlias, selectAlias);
            }).orElse(field("""
                    jsonb_build_object('cast', json_build_array())""", JSONB.class));

            Field<JSONB> episodeField = Optional.ofNullable(episodeNode).map(episode -> {
                Field<String> epAlias = field("ep"+level, String.class);
                return field("""
                    jsonb_build_object('episodes', array_to_json(array(
                        select {0} 
                        from title_episode {1}                       
                        where {1}."parentTconst" = {2}.tconst
                    )))
                    """, JSONB.class, episode.select(epAlias), epAlias, selectAlias);
            }).orElse(field("""
                    jsonb_build_object('episodes', json_build_array())""", JSONB.class));

            return field("""
                    row_to_json({0})::jsonb || 
                    {1} || 
                    {2}
                    """, JSONB.class, selectAlias, castField, episodeField);
        }
    }

    record EpisodeNode(Integer level, TitleNode titleNode) implements QueryNode {

        @Override
        public Field<JSONB> select(Field<String> selectAlias) {

            Field<JSONB> titleField = Optional.ofNullable(titleNode).map(n -> {
                Field<String> castAlias = field("te"+level, String.class);
                return field("""
                        jsonb_build_object('show', (
                                select {0}
                                from title_basics {1}
                                where {1}.tconst = {2}.const
                        ))
                        """, JSONB.class, n.select(castAlias), castAlias, selectAlias);
            }).orElse(field("""
                    jsonb_build_object('show', jsonb_build_object())""", JSONB.class));

            return field("""
                    row_to_json({0})::jsonb || 
                    {1}                   
                    """, JSONB.class, selectAlias, titleField);
        }
    }

    record PersonNode(Integer level, TitleNode personKnownFor, TitleNode personTitles, TitleType type) implements QueryNode {

        @Override
        public Field<JSONB> select(Field<String> selectAlias) {

            Field<JSONB> personKnownForField = Optional.ofNullable(personKnownFor).map(n -> {
                Field<String> titleAlias = field("tb"+level, String.class);
                Field<String> nameAlias = field("nb"+level, String.class);
                Field<String> knownAlias = field("known"+level, String.class);
                return field("""
                        jsonb_build_object('knownFor', array(
                                select {0}
                                from name_basics {1}
                                cross join LATERAL UNNEST({1}."knownForTitles") {2}(tconst)
                                join title_basics {3} on {3}.tconst = {2}.tconst
                                where {1}.nconst = {4}.nconst 
                        ))
                        """, JSONB.class, n.select(titleAlias), nameAlias, knownAlias, titleAlias, selectAlias);
            }).orElse(field("""
                    jsonb_build_object('knownFor', jsonb_build_array())""", JSONB.class));


            Field<JSONB> personTitle = Optional.ofNullable(personTitles).map(n -> {
                Field<String> titleAlias = field("tb"+level, String.class);
                Field<String> titlePrincipal = field("tp"+level, String.class);
                Condition condition = Objects.nonNull(type) ?
                        field("{0}.\"titleType\"", String.class, titleAlias).eq(type.getName()) :
                        val(1).eq(val(1));

                return field("""
                        jsonb_build_object('titles', array(
                                select {0}
                                from title_principals {1}
                                join title_basics {2} on {1}.tconst = {2}.tconst
                                where {1}.nconst = {3}.nconst and {4}
                        ))
                        """, JSONB.class, n.select(titleAlias), titlePrincipal, titleAlias, selectAlias, condition);
            }).orElse(field("""
                    jsonb_build_object('titles', jsonb_build_array())""", JSONB.class));


            return field("""
                    row_to_json({0})::jsonb || 
                    {1} ||
                    {2}
                    """, JSONB.class, selectAlias, personKnownForField, personTitle);
        }
    }

    record RoleNode(Integer level, PersonNode personNode) implements QueryNode {

        @Override
        public Field<JSONB> select(Field<String> selectAlias) {

            Field<JSONB> personField = Optional.ofNullable(personNode).map(p -> {
                Field<String> personAlias = field("p"+level, String.class);
                return field("""
                        jsonb_build_object('person', (
                            select {0} 
                            from name_basics {1} 
                            where {1}.nconst = {2}.nconst 
                        ))""", JSONB.class, p.select(personAlias), personAlias, selectAlias);
            }).orElse(field("jsonb_build_object('person', jsonb_build_object())", JSONB.class));

            return field("""
                    row_to_json({0})::jsonb || 
                    {1}
                    """, JSONB.class, selectAlias, personField);
        }
    }

}
