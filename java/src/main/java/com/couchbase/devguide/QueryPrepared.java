package com.couchbase.devguide;

import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.query.N1qlParams;
import com.couchbase.client.java.query.N1qlQuery;
import com.couchbase.client.java.query.N1qlQueryResult;
import com.couchbase.client.java.query.N1qlQueryRow;
import com.couchbase.client.java.query.ParameterizedN1qlQuery;

/**
 * Example of Querying with N1QL and its Prepared Statement feature, in Java for the Couchbase Developer Guide.
 */
public class QueryPrepared extends ConnectionBase {

    private static final String PLACEHOLDER_STATEMENT = "SELECT airportname FROM `travel-sample` WHERE city=$city AND type=\"airport\"";

    private N1qlQueryResult queryCity(String city, boolean optimize) {
        //the placeholder values can be provided as a JSON array (if using $1 syntax)
        // or map-like JSON object (if using $name syntax)
        JsonObject placeholderValues = JsonObject.create().put("city", city);

        //the N1qlParams.adhoc(false) is used to trigger server-side optimizations, namely preparing
        // the query and reusing the prepared data on subsequent calls
        N1qlParams params = N1qlParams.build().adhoc(!optimize);

        ParameterizedN1qlQuery query = N1qlQuery.parameterized(PLACEHOLDER_STATEMENT, placeholderValues, params);
        return bucket.query(query);
    }

    @Override
    protected void doWork() {
        LOGGER.info("Airports in Reno: ");
        N1qlQueryResult result = queryCity("Reno", true);
        for (N1qlQueryRow row : result) {
            LOGGER.info("\t" + row);
        }
        LOGGER.info("1st query took " + result.info().executionTime());

        result = queryCity("Dallas", true);
        LOGGER.info("\nAirports in Dallas: ");
        for (N1qlQueryRow row : result) {
            LOGGER.info("\t" + row);
        }
        LOGGER.info("2nd query took " + result.info().executionTime());

        LOGGER.info("\nAirports in Los Angeles: ");
        result = queryCity("Los Angeles", true);
        for (N1qlQueryRow row : result) {
            LOGGER.info("\t" + row);
        }
        LOGGER.info("3rd query took " + result.info().executionTime());

        LOGGER.info("\nCompare with unprepared (adhoc) query for Los Angeles: ");
        LOGGER.info(queryCity("Los Angeles", false).info().executionTime());

        //invalidateQueryCache empties the local cache of optimized statements, returning the previous count
        //of such statements.
        LOGGER.info("\nThe SDK prepared " + bucket.invalidateQueryCache() + " queries");
    }

    public static void main(String[] args) {
        new QueryPrepared().execute();
    }
}
