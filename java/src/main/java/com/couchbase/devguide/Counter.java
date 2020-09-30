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
import com.couchbase.client.java.kv.CounterResult;
import com.couchbase.client.java.kv.DecrementOptions;
import com.couchbase.client.java.kv.IncrementOptions;

/**
 * Example of Counters in Java for the Couchbase Developer Guide.
 */
public class Counter extends ConnectionBase {

    @Override
    protected void doWork() {
        String key = "javaDevguideExampleCounter";
        // Remove document so we have predictable behavior in this example
        try {
            bucket.defaultCollection().remove(key);
        } catch (DocumentNotFoundException e) {
            //do nothing, the document is already not here
        }

        try {
            bucket.defaultCollection().binary().increment(key, IncrementOptions.incrementOptions().delta(20).initial(100));
        } catch (DocumentNotFoundException e) {
            LOGGER.info("The counter method failed because the counter doesn't exist yet and no initial value was provided");
        }

        CounterResult rv = bucket.defaultCollection().binary().increment(key, IncrementOptions.incrementOptions().initial(20));
        LOGGER.info("increment Delta=20, Initial=100. Current value is: " + rv.content());


        rv = bucket.defaultCollection().binary().increment(key, IncrementOptions.incrementOptions().delta(1));
        LOGGER.info("increment Delta=1. Current value is: " + rv.content());

        rv = bucket.defaultCollection().binary().decrement(key, DecrementOptions.decrementOptions().delta(50));
        LOGGER.info("decrement Delta=50. Current value is: " + rv.content());
    }

    public static void main(String[] args) {
        new Counter().execute();
    }
}
