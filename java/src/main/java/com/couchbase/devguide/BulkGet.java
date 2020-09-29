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

import java.util.ArrayList;
import java.util.List;

import com.couchbase.client.java.ReactiveCollection;
import com.couchbase.client.java.json.JsonObject;
import reactor.core.publisher.Flux;

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
            collection.upsert(id, content);
        }

        JsonObject jo = collection.get(key+"_1").contentAsObject();
        System.out.println(jo);
        // Describe what we want to do asynchronously using RxJava Observables:


        ReactiveCollection reactiveCollection = collection.reactive();
        Flux<Object> resultFlux = Flux.range(0, 10)
            .map(index ->  {return key + "_" + index; }  )
            .flatMap( k -> reactiveCollection.get(k));

        resultFlux.subscribe(System.out::println);

    }

    public static void main(String[] args) {
        new BulkGet().execute();
    }
}
