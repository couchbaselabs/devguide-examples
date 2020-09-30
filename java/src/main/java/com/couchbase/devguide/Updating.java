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

import com.couchbase.client.core.error.DocumentExistsException;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.kv.MutationResult;

/**
 * Example of Updating/Storing in Java for the Couchbase Developer Guide.
 */
public class Updating extends ConnectionBase {

    @Override
    protected void doWork() {
        String key = "javaDevguideExampleUpdating";
        //create content
        JsonObject content = JsonObject.create().put("topic", "storing").put("mutation", true);

        LOGGER.info("Prepared document " + content);

        //store the document (upsert will always work whether or not a value is already associated to the key)
        MutationResult result = bucket.defaultCollection().upsert( key, content);
        LOGGER.info("Result after upsert: " + result); //notice the CAS changed (the returned document is updated with correct CAS)

        //prepare an update
        content.put("update", "something");
        //see that inserting it fails because it already exists
        try {
            bucket.defaultCollection().insert(key,content);
        } catch (DocumentExistsException e) {
            LOGGER.warn("Couldn't insert it, DocumentAlreadyExists... Let's try to replace it");
        }

        //on the other hand, updating works (it would have failed if the key was not in database)
        result = bucket.defaultCollection().replace(key,content);
        LOGGER.info("Replaced the old document by the new one: " + result); //notice the document's CAS changed again...

        LOGGER.info("Got the following from database: " + bucket.defaultCollection().get(key)); //... which is consistent with a get (RYOW)
    }

    public static void main(String[] args) {
        new Updating().execute();
    }
}
