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

import com.couchbase.client.core.deps.io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import com.couchbase.client.core.env.IoConfig;
import com.couchbase.client.core.env.SecurityConfig;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.ClusterOptions;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.Scope;
import com.couchbase.client.java.env.ClusterEnvironment;
import com.couchbase.client.java.json.JsonArray;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.manager.query.CreatePrimaryQueryIndexOptions;
import com.couchbase.client.java.query.QueryResult;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.couchbase.client.java.query.QueryOptions.queryOptions;

/**
 * Example of Cas (Check and Set) handling in Java for the Couchbase Developer Guide.
 * TODO: not tested | ported from 2.x, but not tested.  See CloudConnect.java for 3.x
 */

public class Cloud {
    // Update this to your certificate.
    private static final String CERT = "-----BEGIN CERTIFICATE-----\n" +
            "MIIDFTCCAf2gAwIBAgIRANLVkgOvtaXiQJi0V6qeNtswDQYJKoZIhvcNAQELBQAw\n" +
            "JDESMBAGA1UECgwJQ291Y2hiYXNlMQ4wDAYDVQQLDAVDbG91ZDAeFw0xOTEyMDYy\n" +
            "MjEyNTlaFw0yOTEyMDYyMzEyNTlaMCQxEjAQBgNVBAoMCUNvdWNoYmFzZTEOMAwG\n" +
            "A1UECwwFQ2xvdWQwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCfvOIi\n" +
            "enG4Dp+hJu9asdxEMRmH70hDyMXv5ZjBhbo39a42QwR59y/rC/sahLLQuNwqif85\n" +
            "Fod1DkqgO6Ng3vecSAwyYVkj5NKdycQu5tzsZkghlpSDAyI0xlIPSQjoORA/pCOU\n" +
            "WOpymA9dOjC1bo6rDyw0yWP2nFAI/KA4Z806XeqLREuB7292UnSsgFs4/5lqeil6\n" +
            "rL3ooAw/i0uxr/TQSaxi1l8t4iMt4/gU+W52+8Yol0JbXBTFX6itg62ppb/Eugmn\n" +
            "mQRMgL67ccZs7cJ9/A0wlXencX2ohZQOR3mtknfol3FH4+glQFn27Q4xBCzVkY9j\n" +
            "KQ20T1LgmGSngBInAgMBAAGjQjBAMA8GA1UdEwEB/wQFMAMBAf8wHQYDVR0OBBYE\n" +
            "FJQOBPvrkU2In1Sjoxt97Xy8+cKNMA4GA1UdDwEB/wQEAwIBhjANBgkqhkiG9w0B\n" +
            "AQsFAAOCAQEARgM6XwcXPLSpFdSf0w8PtpNGehmdWijPM3wHb7WZiS47iNen3oq8\n" +
            "m2mm6V3Z57wbboPpfI+VEzbhiDcFfVnK1CXMC0tkF3fnOG1BDDvwt4jU95vBiNjY\n" +
            "xdzlTP/Z+qr0cnVbGBSZ+fbXstSiRaaAVcqQyv3BRvBadKBkCyPwo+7svQnScQ5P\n" +
            "Js7HEHKVms5tZTgKIw1fbmgR2XHleah1AcANB+MAPBCcTgqurqr5G7W2aPSBLLGA\n" +
            "fRIiVzm7VFLc7kWbp7ENH39HVG6TZzKnfl9zJYeiklo5vQQhGSMhzBsO70z4RRzi\n" +
            "DPFAN/4qZAgD5q3AFNIq2WWADFQGSwVJhg==\n" +
            "-----END CERTIFICATE-----";

    public static void main(String... args) throws Exception {
        // Update this to your cluster
        String endpoint = "cb.<your endpoint address>.dp.cloud.couchbase.com";
        String bucketName = "couchbasecloudbucket";
        String username = "user";
        String password = "password";
        // User Input ends here.

        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        trustStore.load(null, null);
        trustStore.setCertificateEntry("server", decodeCertificates(Collections.singletonList(CERT)).get(0));

        ClusterEnvironment env = ClusterEnvironment.builder()
            .securityConfig(SecurityConfig.enableTls(true)
                .trustManagerFactory(InsecureTrustManagerFactory.INSTANCE))
            .ioConfig(IoConfig.enableDnsSrv(true))
            .build();

        // Initialize the Connection
        Cluster cluster = Cluster.connect(endpoint,
            ClusterOptions.clusterOptions(username, password).environment(env));
        Bucket bucket = cluster.bucket(bucketName);
        Scope scope = bucket.defaultScope();
        Collection collection = bucket.defaultCollection();

        // Create a N1QL Primary Index (but ignore if it exists)
        cluster.queryIndexes().createPrimaryIndex(bucketName, CreatePrimaryQueryIndexOptions.createPrimaryQueryIndexOptions().ignoreIfExists(true));

        // Create a JSON Document
        JsonObject arthur = JsonObject.create()
            .put("name", "Arthur")
            .put("email", "kingarthur@couchbase.com")
            .put("interests", JsonArray.from("Holy Grail", "African Swallows"));

        // Store the Document
        collection.upsert( "u:king_arthur", arthur);

        // Load the Document and print it
        // Prints Content and Metadata of the stored Document
        System.out.println(collection.get("u:king_arthur"));

        // Perform a N1QL Query
        // Perform a N1QL Query
        QueryResult result = cluster.query(
            String.format("SELECT name FROM `%s` WHERE $1 IN interests", bucketName),
            queryOptions().parameters(JsonArray.from("African Swallows"))
        );

        // Print each found Row
        for (JsonObject row : result.rowsAsObject()) {
            System.out.println(row);
        }
    }

    public static List<X509Certificate> decodeCertificates(final List<String> certificates) {
        final CertificateFactory cf;
        try {
            cf = CertificateFactory.getInstance("X.509");
        } catch (CertificateException e) {
            throw new RuntimeException(e);
        }
        return certificates.stream().map(c -> {
            try {
                return (X509Certificate) cf.generateCertificate(
                        new ByteArrayInputStream(c.getBytes(StandardCharsets.UTF_8))
                );
            } catch (CertificateException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());
    }
}
