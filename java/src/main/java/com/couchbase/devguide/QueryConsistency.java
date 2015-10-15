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

import static com.couchbase.client.java.query.Select.select;
import static com.couchbase.client.java.query.dsl.Expression.x;

import java.util.Random;

import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.JsonLongDocument;
import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.error.DocumentDoesNotExistException;
import com.couchbase.client.java.query.N1qlParams;
import com.couchbase.client.java.query.N1qlQuery;
import com.couchbase.client.java.query.N1qlQueryResult;
import com.couchbase.client.java.query.N1qlQueryRow;
import com.couchbase.client.java.query.Statement;
import com.couchbase.client.java.query.consistency.ScanConsistency;
import com.couchbase.client.java.query.dsl.functions.MetaFunctions;

/**
 * Example of N1QL Query Consistency in Java for the Couchbase Developer Guide.
 */
public class QueryConsistency extends ConnectionBase {

    @Override
    protected void doWork() {
        String key = "javaDevguideExampleQueryConsistency";

        LOGGER.info("Please ensure there is a primary index on the default bucket!");
        Random random = new Random();
        int randomNumber = random.nextInt(10000000);

        //prepare the random user
        JsonObject user = JsonObject.create()
                .put("name", JsonArray.from("Brass", "Doorknob"))
                .put("email", "brass.doorknob@juno.com")
                .put("random", randomNumber);
        //upsert it with the corresponding random key
        JsonDocument doc = JsonDocument.create(key, user); //the same document will be updated with a random internal value
        bucket.upsert(doc);

        //immediately query N1QL (note we imported relevant static methods)
        //prepare the statement
        Statement statement = select("name", "email", "random", "META(default).id")
                .from("default")
                .where(x("$1").in("name"));

        //configure the query
        N1qlParams params = N1qlParams.build()
                //If this line is removed, the latest 'random' field might not be present
                .consistency(ScanConsistency.REQUEST_PLUS);

        N1qlQuery query = N1qlQuery.parameterized(statement, JsonArray.from("Brass"), params);

        LOGGER.info("Expecting random: " + randomNumber);
        N1qlQueryResult result = bucket.query(query);
        if (!result.finalSuccess() || result.allRows().isEmpty()) {
            LOGGER.warn("No result/errors: " + result.errors().toString());
        }

        for (N1qlQueryRow queryRow : result) {
            JsonObject row = queryRow.value();
            int rowRandom = row.getInt("random");
            String rowId = row.getString("id");

            LOGGER.info("Doc Id: " + rowId  + ", Name: " + row.getArray("name") + ", Email: " + row.getString("email")
                + ", Random: " + rowRandom);

            if (rowRandom == randomNumber) {
                LOGGER.info("!!! Found our newly inserted document !!!");
            } else {
                LOGGER.warn("Found a different random value : " + rowRandom);
            }

            if (System.getProperty("REMOVE_DOORKNOBS") != null || System.getenv("REMOVE_DOORKNOBS") != null) {
                bucket.remove(rowId);
            }
        }
    }

    public static void main(String[] args) {
        new QueryConsistency().execute();
    }
}
