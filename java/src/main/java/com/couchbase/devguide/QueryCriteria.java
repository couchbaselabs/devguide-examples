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

import static com.couchbase.client.java.query.dsl.Expression.i;
import static com.couchbase.client.java.query.dsl.Expression.s;
import static com.couchbase.client.java.query.dsl.Expression.x;

import com.couchbase.client.java.query.N1qlQuery;
import com.couchbase.client.java.query.N1qlQueryResult;
import com.couchbase.client.java.query.N1qlQueryRow;
import com.couchbase.client.java.query.Select;
import com.couchbase.client.java.query.Statement;

/**
 * Example of Querying with N1QL in Java for the Couchbase Developer Guide.
 */
public class QueryCriteria extends ConnectionBase {

    @Override
    protected void doWork() {
        String statement = "SELECT airportname, city, country FROM `travel-sample` WHERE type=\"airport\" AND city=\"Reno\"";
        N1qlQuery query = N1qlQuery.simple(statement);

        LOGGER.info("Results from a simple statement:");
        LOGGER.info(statement);
        N1qlQueryResult result = bucket.query(query);
        for (N1qlQueryRow row : result) {
            LOGGER.info("\t" + row.value());
        }

        //when there is a server-side error, the server will feed errors in the result.error() collection
        //you can find that out by checking finalSuccess() == false
        //alternatively, syntax errors are also detected early and for them you can check parseSuccess()
        N1qlQueryResult errorResult = bucket.query(N1qlQuery.simple("SELECTE * FROM `travel-sample` LIMIT 3"));
        LOGGER.info("With bad syntax, parseSuccess = " + errorResult.parseSuccess()
            + ", finalSuccess = " + errorResult.finalSuccess() + ", errors: " + errorResult.errors());

        //there is also a fluent API to construct N1QL statements, generally import static the Select.select method
        Statement fluentStatement =
            Select.select("airportname", "city", "country")
                //Expression.i escapes an expression with backticks
                .from(i("travel-sample"))
                //Expression.x creates an expression token that can be manipulated
                //Expression.s creates a string literal
                .where(x("type").eq(s("airport"))
                                .and(x("city").eq(s("Reno")))
                );

        LOGGER.info("Results from a fluent-API statement:");
        LOGGER.info(fluentStatement.toString());
        N1qlQueryResult fluentResult = bucket.query(N1qlQuery.simple(fluentStatement));
        for (N1qlQueryRow row : fluentResult) {
            LOGGER.info("\t" + row.value());
        }

        //the result also contains metrics sent by the server
        LOGGER.info("Metrics: " + fluentResult.info());

    }

    public static void main(String[] args) {
        new QueryCriteria().execute();
    }
}
