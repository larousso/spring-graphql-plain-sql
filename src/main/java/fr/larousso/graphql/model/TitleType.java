package fr.larousso.graphql.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.stream.Stream;

public enum TitleType {

    movie("movie"),
tvEpisode("tvEpisode"),
tvMiniSeries("tvMiniSeries"),
tvMovie("tvMovie"),
tvPilot("tvPilot"),
tvSeries("tvSeries"),
tvShort("tvShort"),
tvSpecial("tvSpecial"),
video("video"),
videoGame("videoGame"),
Short("short") ;

    public final String name;

    TitleType(String name) {
        this.name = name;
    }

    @JsonCreator
    public static TitleType fromName(String name) {
        return Stream.of(TitleType.values()).filter(t -> t.name.equals(name)).findFirst().orElse(null);
    }

    @JsonValue
    public String getName() {
        return name;
    }
}
