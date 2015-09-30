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

import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.error.DocumentAlreadyExistsException;

/**
 * Example of Updating/Storing in Java for the Couchbase Developper Guide.
 */
public class Updating extends ConnectionBase {

    @Override
    protected void doWork() {
        String key = "javaDevguideExampleUpdating";
        //create content
        JsonObject content = JsonObject.create().put("topic", "storing").put("mutation", true);

        //create document
        JsonDocument document = JsonDocument.create(key, content);
        LOGGER.info("Prepared document " + document);

        //store the document (upsert will always work whether or not a value is already associated to the key)
        document = bucket.upsert(document);
        LOGGER.info("Document after upsert: " + document); //notice the CAS changed (the returned document is updated with correct CAS)

        //prepare an update
        document.content().put("update", "something");
        //see that inserting it fails because it already exists
        try {
            bucket.insert(document);
        } catch (DocumentAlreadyExistsException e) {
            LOGGER.warn("Couldn't insert it, DocumentAlreadyExists... Let's try to replace it");
        }

        //on the other hand, updating works (it would have failed if the key was not in database)
        document = bucket.replace(document);
        LOGGER.info("Replaced the old document by the new one: " + document); //notice the document's CAS changed again...

        LOGGER.info("Got the following from database: " + bucket.get(key)); //... which is consistent with a get (RYOW)
    }

    public static void main(String[] args) {
        new Updating().execute();
    }
}
