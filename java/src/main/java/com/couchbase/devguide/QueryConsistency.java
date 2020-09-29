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
import com.couchbase.client.java.manager.query.CreatePrimaryQueryIndexOptions;
import com.couchbase.client.java.query.QueryOptions;
import com.couchbase.client.java.query.QueryResult;
import com.couchbase.client.java.query.QueryScanConsistency;
import com.couchbase.client.java.query.QueryStatus;

import java.util.Random;

/**
 * Example of N1QL Query Consistency in Java for the Couchbase Developer Guide.
 */
public class QueryConsistency extends ConnectionBase {

    @Override
    protected void doWork() {
        String key = "javaDevguideExampleQueryConsistency";

        cluster.queryIndexes().createPrimaryIndex(bucketName, CreatePrimaryQueryIndexOptions.createPrimaryQueryIndexOptions().ignoreIfExists(true));
        Random random = new Random();
        int randomNumber = random.nextInt(10000000);

        //prepare the random user
        JsonObject user = JsonObject.create()
                .put("name", JsonArray.from("Brass", "Doorknob"))
                .put("email", "brass.doorknob@juno.com")
                .put("random", randomNumber);
        //upsert it with the corresponding random key
        bucket.defaultCollection().upsert(key, user);

        LOGGER.info("Expecting random: " + randomNumber);
        QueryResult result = cluster.query("select name, email, random, META(default).id " +
            " from default where $1 in name",
            QueryOptions.queryOptions().scanConsistency(QueryScanConsistency.REQUEST_PLUS).parameters(JsonArray.from("Brass")));
        if (!result.metaData().status().equals(QueryStatus.SUCCESS) ) {
            LOGGER.warn("No result/errors: " + result.metaData().warnings());
        }

        for (JsonObject row : result.rowsAsObject()) {
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
                bucket.defaultCollection().remove(rowId);
            }
        }
    }

    public static void main(String[] args) {
        new QueryConsistency().execute();
    }
}
