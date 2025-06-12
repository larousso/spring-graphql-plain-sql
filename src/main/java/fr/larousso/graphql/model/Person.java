package fr.larousso.graphql.model;

import java.time.LocalDate;
import java.util.List;

public record Person(
        String nconst,
        String primaryName,
        LocalDate birthYear,
        LocalDate deathYear,
        List<String> primaryProfession,
        List<Title> titles,
        List<Title> titlesKnowFor
) {
}
