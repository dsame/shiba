package org.codeforamerica.shiba;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ApplicationQueries {
    private final Map<String, Query> queries;

    public ApplicationQueries(Map<String, Query> queries) {
        this.queries = queries;
    };

    public Query getQuery(String query) {
        return this.queries.get(query);
    }
}
