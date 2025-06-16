package fr.larousso.graphql.repository;

import org.junit.jupiter.api.Test;

import static fr.larousso.graphql.GraphqlHelper.captureDataFetchingEnvironmentForQuery;
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

        System.out.println(queryNode);
    }

}