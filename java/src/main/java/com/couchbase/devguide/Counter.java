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

import com.couchbase.client.java.document.JsonLongDocument;
import com.couchbase.client.java.error.DocumentDoesNotExistException;

/**
 * Example of Counters in Java for the Couchbase Developer Guide.
 */
public class Counter extends ConnectionBase {

    @Override
    protected void doWork() {
        String key = "javaDevguideExampleCounter";
        // Remove document so we have predictable behavior in this example
        try {
            bucket.remove(key);
        } catch (DocumentDoesNotExistException e) {
            //do nothing, the document is already not here
        }

        try {
            bucket.counter(key, 20);
        } catch (DocumentDoesNotExistException e) {
            LOGGER.info("The counter method failed because the counter doesn't exist yet and no initial value was provided");
        }

        JsonLongDocument rv = bucket.counter(key, 20, 100);
        LOGGER.info("Delta=20, Initial=100. Current value is: " + rv.content());


        rv = bucket.counter(key, 1);
        LOGGER.info("Delta=1. Current value is: " + rv.content());

        rv = bucket.counter(key, -50);
        LOGGER.info("Delta=-50. Current value is: " + rv.content());
    }

    public static void main(String[] args) {
        new Counter().execute();
    }
}
