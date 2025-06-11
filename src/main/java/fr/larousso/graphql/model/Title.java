package fr.larousso.graphql.model;

import java.time.Year;
import java.util.List;

public record Title(
        String id,
        TitleType titleType,
        String primaryTitle,
        String originalTitle,
        Year startYear,
        Year endYear,
        List<String> genres,
        List<Role> distribution,
        List<Episode> episodes
        ) {
}
