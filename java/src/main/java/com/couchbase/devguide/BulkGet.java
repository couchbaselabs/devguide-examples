package com.couchbase.devguide;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.sound.midi.Soundbank;

import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.JsonLongDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.error.DocumentDoesNotExistException;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Example of Bulk Get in Java for the Couchbase Developer Guide.
 */
public class BulkGet extends ConnectionBase {

    @Override
    protected void doWork() {
        final String key = "javaDevguideExampleBulkGet";

        // Create a JSON document content
        final JsonObject content = JsonObject.create().put("item", "A bulk get test value");

        // Prepare 10 keys
        List<String> keys = new ArrayList<String>(10);
        for (int i = 0; i < 10; i++) {
            keys.add(key + "_" + i);
        }

        // Insert 10 documents, the old way
        for (String id : keys) {
            JsonDocument doc = JsonDocument.create(id, content);
            bucket.upsert(doc);
        }

        // Describe what we want to do asynchronously using RxJava Observables:

        Observable<JsonDocument> asyncBulkGet = Observable
                // Use RxJava from to start from the keys we know in advance
                .from(keys)
                //now use flatMap to asynchronously retrieve (get) each corresponding document using the SDK
                .flatMap(new Func1<String, Observable<JsonDocument>>() {
                    public Observable<JsonDocument> call(String key) {
                        if (key.endsWith("3"))
                            return bucket.async().get(key).delay(3, TimeUnit.SECONDS); //artificial delay for item 3
                        return bucket.async().get(key);
                    }
                });

        // So far we've described and not triggered the processing, let's subscribe
        /*
         *  Note: since our app is not fully asynchronous, we want to revert back to blocking at the end,
         *  so we subscribe using toBlocking().
         *
         *  toBlocking will throw any exception that was propagated through the Observer's onError method.
         *
         *  The SDK is doing its own parallelisation so the blocking is just waiting for the last item,
         *  notice how our artificial delay doesn't impact printout of the other values, that come in the order
         *  in which the server answered...
         */
        try {
            asyncBulkGet.toBlocking()
                    // we'll still printout each inserted document (with CAS gotten from the server)
                    // toBlocking() also offers several ways of getting one of the emitted values (first(), single(), last())
                    .forEach(new Action1<JsonDocument>() {
                        public void call(JsonDocument jsonDocument) {
                            LOGGER.info("Found " + jsonDocument);
                        }
                    });
        } catch (Exception e) {
            LOGGER.error("Error during bulk get", e);
        }
    }

    public static void main(String[] args) {
        new BulkGet().execute();
    }
}
