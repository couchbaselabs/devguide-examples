package com.couchbase.devguide;

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
 * Example of Bulk Insert in Java for the Couchbase Developer Guide.
 */
public class BulkInsert extends ConnectionBase {

    @Override
    protected void doWork() {
        final String key = "javaDevguideExampleBulkInsert";

        // Create a JSON document content
        final JsonObject content = JsonObject.create().put("item", "A bulk insert test value");

        // Describe what we want to do asynchronously using RxJava Observables:

        Observable<JsonDocument> asyncProcessing = Observable
                // Use RxJava range + map to generate 10 keys. One could also use "from" with a pre-existing collection of keys.
                .range(0, 10)
                .map(new Func1<Integer, String>() {
                    public String call(Integer i) {
                        return key + "_" + i;
                    }
                })
                //then create a JsonDocument out each one of these keys
                .map(new Func1<String, JsonDocument>() {
                    public JsonDocument call(String s) {
                        return JsonDocument.create(s, content);
                    }
                })
                //now use flatMap to asynchronously call the SDK upsert operation on each
                .flatMap(new Func1<JsonDocument, Observable<JsonDocument>>() {
                    public Observable<JsonDocument> call(JsonDocument doc) {
                        if (doc.id().endsWith("3"))
                            return bucket.async().upsert(doc).delay(3, TimeUnit.SECONDS); //artificial delay for item 3
                        return bucket.async().upsert(doc);
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
            asyncProcessing.toBlocking()
                // we'll still printout each inserted document (with CAS gotten from the server)
                // toBlocking() also offers several ways of getting one of the emitted values (first(), single(), last())
                .forEach(new Action1<JsonDocument>() {
                    public void call(JsonDocument jsonDocument) {
                        LOGGER.info("Inserted " + jsonDocument);
                    }
                });
        } catch (Exception e) {
            LOGGER.error("Error during bulk insert", e);
        }
    }

    public static void main(String[] args) {
        new BulkInsert().execute();
    }
}
