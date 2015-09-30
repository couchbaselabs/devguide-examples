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

/**
 * Example of Retrieving in Java for the Couchbase Developer Guide.
 */
public class Retrieving extends ConnectionBase {

    @Override
    protected void doWork() {
        String key = "javaDevguideExampleRetrieving";
        LOGGER.info("Getting non-existent key. Should fail..");
        JsonDocument nonExistentDocument = bucket.get("non-exist-document");
        if (nonExistentDocument == null) {
            LOGGER.info("Got null for missing document, it doesn't exist!");
        }

        LOGGER.info("Upserting...");
        JsonDocument document = JsonDocument.create(key, JsonObject.create().put("foo", "bar"));
        bucket.upsert(document);
        LOGGER.info("Getting...");
        LOGGER.info(bucket.get(key));
    }

    public static void main(String[] args) {
        new Retrieving().execute();
    }
}
