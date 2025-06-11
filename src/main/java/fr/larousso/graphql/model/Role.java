package fr.larousso.graphql.model;

import java.util.List;

public record Role(
        String id,
        String category,
        String job,
        Person person,
        List<String> characters,
        List<Episode> episodes
) { }
