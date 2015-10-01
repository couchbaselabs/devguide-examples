/**
 * Copyright (C) 2015 Couchbase, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALING
 * IN THE SOFTWARE.
 */
package com.couchbase.devguide;

import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.query.N1qlQuery;
import com.couchbase.client.java.query.N1qlQueryResult;
import com.couchbase.client.java.query.N1qlQueryRow;
import com.couchbase.client.java.query.ParameterizedN1qlQuery;

/**
 * Example of Querying using placeholders with N1QL in Java for the Couchbase Developer Guide.
 */
public class QueryPlaceholders extends ConnectionBase {

    private static final String PLACEHOLDER_STATEMENT = "SELECT airportname FROM `travel-sample` WHERE city=$1 AND type=\"airport\"";

    private N1qlQueryResult queryCity(String city) {
        //the placeholder values can be provided as a JSON array (if using $1 syntax)
        // or map-like JSON object (if using $name syntax)
        JsonArray placeholderValues = JsonArray.from(city);

        ParameterizedN1qlQuery query = N1qlQuery.parameterized(PLACEHOLDER_STATEMENT, placeholderValues);
        return bucket.query(query);
    }

    @Override
    protected void doWork() {
        LOGGER.info("Airports in Reno: ");
        for (N1qlQueryRow row : queryCity("Reno")) {
            LOGGER.info("\t" + row);
        }

        LOGGER.info("Airports in Dallas: ");
        for (N1qlQueryRow row : queryCity("Dallas")) {
            LOGGER.info("\t" + row);
        }

        LOGGER.info("Airports in Los Angeles: ");
        for (N1qlQueryRow row : queryCity("Los Angeles")) {
            LOGGER.info("\t" + row);
        }
    }

    public static void main(String[] args) {
        new QueryPlaceholders().execute();
    }
}
