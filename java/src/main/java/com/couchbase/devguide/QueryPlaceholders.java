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

import com.couchbase.client.java.json.JsonArray;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.query.QueryOptions;

import java.util.List;

/**
 * Example of Querying using placeholders with N1QL in Java for the Couchbase Developer Guide.
 */
public class QueryPlaceholders extends ConnectionBase {

    private static final String PLACEHOLDER_STATEMENT = "SELECT airportname FROM `travel-sample` WHERE city=$1 AND type=\"airport\"";

    private List<JsonObject> queryCity(String city) {


        //the placeholder values can be provided as a JSON array (if using $1 syntax)
        // or map-like JSON object (if using $name syntax)
        JsonArray placeholderValues = JsonArray.from(city);
        return cluster.query("SELECT airportname FROM `default` WHERE city=$1 AND type=\"airport\"",
            QueryOptions.queryOptions().parameters(placeholderValues)).rowsAsObject();
    }

    @Override
    protected void doWork() {

        JsonObject airport;

        airport = JsonObject.create()
            .put( "type", "airport")
            .put("airportname", "Reno International Airport")
            .put("city", "Reno")
            .put("country", "United States");

        bucket.defaultCollection().upsert("1", airport);

        airport = JsonObject.create()
            .put( "type", "airport")
            .put("airportname", "Los Angeles International Airport")
            .put("city", "Los Angeles")
            .put("country", "United States");

        bucket.defaultCollection().upsert("2", airport);

        airport = JsonObject.create()
            .put( "type", "airport")
            .put("airportname", "Culver City Airport")
            .put("city", "Los Angeles")
            .put("country", "United States");

        bucket.defaultCollection().upsert("3", airport);

        airport = JsonObject.create()
            .put( "type", "airport")
            .put("airportname", "Dallas International Airport")
            .put("city", "Dallas")
            .put("country", "United States");

        bucket.defaultCollection().upsert("4", airport);

        LOGGER.info("Airports in Reno: ");
        for (JsonObject row : queryCity("Reno")) {
            LOGGER.info("\t" + row);
        }

        LOGGER.info("Airports in Dallas: ");
        for (JsonObject row : queryCity("Dallas")) {
            LOGGER.info("\t" + row);
        }

        LOGGER.info("Airports in Los Angeles: ");
        for (JsonObject row : queryCity("Los Angeles")) {
            LOGGER.info("\t" + row);
        }
    }

    public static void main(String[] args) {
        new QueryPlaceholders().execute();
    }
}
