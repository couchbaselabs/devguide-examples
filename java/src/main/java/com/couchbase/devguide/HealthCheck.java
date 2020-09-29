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

import com.couchbase.client.core.diagnostics.DiagnosticsResult;
import com.couchbase.client.core.diagnostics.EndpointDiagnostics;
import com.couchbase.client.core.diagnostics.EndpointPingReport;
import com.couchbase.client.core.diagnostics.PingResult;
import com.couchbase.client.core.diagnostics.PingState;
import com.couchbase.client.core.endpoint.EndpointState;
import com.couchbase.client.core.service.ServiceType;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.kv.MutationResult;

import java.time.Duration;
import java.util.List;
import java.util.Map;


/**
 * Example Health Check with the Couchbase Java SDKa for the Couchbase Developer Guide.
 */
public class HealthCheck extends ConnectionBase {


//    public static void main(String... args) {

//        Cluster cluster = Cluster.connect("127.0.0.1", "Administrator", "password");
//
//        Bucket bucket = cluster.bucket("bucket-name");
//        Scope scope = bucket.scope("scope-name");
//        Collection collection = scope.collection("collection-name");
//
//        JsonObject json = JsonObject.create()
//                .put("foo", "bar")
//                .put("baz", "qux");
//
//
//        AsyncCollection asyncCollection = collection.async();
//        ReactiveCollection reactiveCollection = collection.reactive();
//
//        JsonObject content = JsonObject.create().put("foo", "bar");
////        MutationResult result = collection.upsert("document-key", content);
//
//        PingResult ping = bucket.ping();
    @Override
    protected void doWork() {

        bucket.waitUntilReady(Duration.ofSeconds(5));

        JsonObject content = JsonObject.create()
        .put("foo", "bar")
        .put("baz", "qux");

        MutationResult result = collection.upsert("document-key", content);

        // Ping a specified bucket to look at the state of all associated endpoints
        PingResult pingResult = bucket.ping();
        // Look at the KV endpoints and warn if their state is not OK
        Map<ServiceType, List<EndpointPingReport>> pingEndpoints = pingResult.endpoints();
        List<EndpointPingReport> kvPingReports = pingEndpoints.get(ServiceType.KV);

        for (EndpointPingReport pingEndpoint : kvPingReports) {
            if (pingEndpoint.state() != PingState.OK) {
                LOGGER.warn(String.format("Node %s at remote %s is %s.", pingEndpoint.id(), pingEndpoint.remote(), pingEndpoint.state()));
            } else {
                LOGGER.info(String.format("Node %s at remote %s is OK.", pingEndpoint.id(), pingEndpoint.remote()));
            }
        }

        // Get all diagnostics associated with a given cluster, passively
        DiagnosticsResult diagnosticsResult = cluster.diagnostics();
        Map<ServiceType, List<EndpointDiagnostics>> diagEndpoints = diagnosticsResult.endpoints();
        // Look at the KV connections, warn if not connected
        List<EndpointDiagnostics> kvDiagReports = diagEndpoints.get(ServiceType.KV);

        for (EndpointDiagnostics diagEndpoint : kvDiagReports) {
            // Identify the KV connection associated with the bucket we are using from the namespace
            if (diagEndpoint.namespace().isPresent() && diagEndpoint.namespace().get().contentEquals(bucketName)) {
                if (diagEndpoint.state() != EndpointState.CONNECTED) {
                    LOGGER.warn(String.format("Endpoint %s at remote %s is in state %s.", diagEndpoint.id(), diagEndpoint.remote(), diagEndpoint.state()));
                } else {
                    LOGGER.info(String.format("Endpoint %s at remote %s connected.", diagEndpoint.id().orElse("NO_ID"), diagEndpoint.remote()));
                }
            }
        }
    }

    public static void main(String[] args) {
        new HealthCheck().execute();
    }


}
