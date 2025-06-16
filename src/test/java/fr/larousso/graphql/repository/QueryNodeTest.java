package fr.larousso.graphql.repository;

import fr.larousso.graphql.model.TitleType;
import org.junit.jupiter.api.Test;

import static fr.larousso.graphql.GraphqlHelper.captureDataFetchingEnvironmentForQuery;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class QueryNodeTest {

    @Test
    public void testQueryNode() {
        QueryNode queryNode = captureDataFetchingEnvironmentForQuery("""
                query MyQuery {
                  titles(name: "Severance", page: 1, size: 10, type: tvSeries) {
                    primaryTitle
                    genres
                    titleType
                    cast {
                      category
                      job
                      person {
                        primaryName
                        birthYear
                        primaryProfession
                        knownFor {
                          titleType
                          primaryTitle
                        }
                        titles(type: movie) {
                          titleType
                          primaryTitle
                        }
                      }
                    }
                    episodes {
                      seasonNumber
                      episodeNumber
                      show {
                        primaryTitle
                        originalTitle
                      }
                    }
                  }
                }
                """, d -> QueryNode.create(d.getSelectionSet()));

        assertThat(queryNode).isEqualTo(
                new QueryNode.TitleNode(
                        1,
                        new QueryNode.RoleNode(
                                2,
                                new QueryNode.PersonNode(
                                    3,
                                    new QueryNode.TitleNode(4, null, null),
                                    new QueryNode.TitleNode(4, null, null),
                                    TitleType.movie
                                )
                        ),
                        new QueryNode.EpisodeNode(
                                2,
                                new QueryNode.TitleNode(3, null, null)
                        )
                ));
    }

}