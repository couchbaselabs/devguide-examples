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
