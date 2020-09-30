/*
 * Copyright (c) 2020 Couchbase, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.couchbase.devguide;

import com.couchbase.client.core.error.ParsingFailureException;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.query.QueryMetaData;
import com.couchbase.client.java.query.QueryOptions;
import com.couchbase.client.java.query.QueryScanConsistency;

import java.util.List;

/**
 * Example of Querying with N1QL in Java for the Couchbase Developer Guide.
 */
public class QueryCriteria extends ConnectionBase {

    @Override
    protected void doWork() {

        JsonObject airport = JsonObject.create()
            .put( "type", "airport")
            .put("airportname", "Reno International Airport")
            .put("city", "Reno")
            .put("country", "United States");

        bucket.defaultCollection().upsert("1", airport);


        String statement = "SELECT airportname, city, country FROM `default` WHERE type=\"airport\" AND city=\"Reno\"";

        LOGGER.info("Results from a simple statement:");
        LOGGER.info(statement);
        List<JsonObject> result = cluster.query(statement, QueryOptions.queryOptions().scanConsistency(QueryScanConsistency.REQUEST_PLUS)).rowsAsObject();
        for (JsonObject row : result) {
            LOGGER.info("\t" + row);
        }

        //when there is a server-side error, the server will feed errors in the result.error() collection
        //you can find that out by checking finalSuccess() == false
        //alternatively, syntax errors are also detected early and for them you can check parseSuccess()

        try {
            QueryMetaData errorResult = cluster.query("SELECTE * FROM `travel-sample` LIMIT 3")
                .metaData();
            LOGGER.info("With bad syntax, finalSuccess = " + errorResult.status() + ", errors: " + errorResult.warnings());
        }catch (ParsingFailureException pf){
            System.out.println(pf);
        }
/*
        //there is also a fluent API to construct N1QL statements, generally import static the Select.select method
        Statement fluentStatement =
            Select.select("airportname", "city", "country")
                //Expression.i escapes an expression with backticks
                .from(i("travel-sample"))
                //Expression.x creates an expression token that can be manipulated
                //Expression.s creates a string literal
                .where(x("type").eq(s("airport"))
                                .and(x("city").eq(s("Reno")))
                );

        LOGGER.info("Results from a fluent-API statement:");
        LOGGER.info(fluentStatement.toString());
        N1qlQueryResult fluentResult = bucket.query(N1qlQuery.simple(fluentStatement));
        for (N1qlQueryRow row : fluentResult) {
            LOGGER.info("\t" + row.value());
        }

        //the result also contains metrics sent by the server
        LOGGER.info("Metrics: " + fluentResult.info());
*/
    }

    public static void main(String[] args) {
        new QueryCriteria().execute();
    }
}
