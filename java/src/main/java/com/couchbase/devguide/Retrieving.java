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

import com.couchbase.client.core.error.DocumentNotFoundException;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.kv.GetResult;

/**
 * Example of Retrieving in Java for the Couchbase Developer Guide.
 */
public class Retrieving extends ConnectionBase {

    @Override
    protected void doWork() {
        String key = "javaDevguideExampleRetrieving";
        LOGGER.info("Getting non-existent key. Should fail..");
        try {
            GetResult nonExistentDocument = bucket.defaultCollection()
                .get("non-exist-document");
            if (nonExistentDocument == null) {
                LOGGER.info("Got null for missing document, it doesn't exist!");
            }
        } catch (DocumentNotFoundException dnf){
            System.out.println(dnf);
        }

        LOGGER.info("Upserting...");
        JsonObject document = JsonObject.create().put("foo", "bar");
        bucket.defaultCollection().upsert(key,document);
        LOGGER.info("Getting...");
        LOGGER.info(bucket.defaultCollection().get(key));
    }

    public static void main(String[] args) {
        new Retrieving().execute();
    }
}
