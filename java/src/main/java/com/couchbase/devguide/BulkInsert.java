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

import com.couchbase.client.java.ReactiveCollection;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.kv.MutationResult;
import reactor.core.publisher.Flux;

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

      ReactiveCollection reactiveCollection = collection.reactive();
      Flux<MutationResult> resultFlux = Flux.range(0, 10)
          .map(index ->  {return key + "_" + index; }  )
          .flatMap( k -> reactiveCollection.upsert(k, content));

      resultFlux.subscribe(System.out::println);

    }

    public static void main(String[] args) {
        new BulkInsert().execute();
    }
}
