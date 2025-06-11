package fr.larousso.graphql.model;

public record Episode(
        Integer seasonNumber,
        Integer episodeNumber,
        Title show
) {
}
