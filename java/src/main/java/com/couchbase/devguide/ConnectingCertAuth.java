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
import com.couchbase.client.core.error.DocumentNotFoundException;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.ClusterOptions;
import com.couchbase.client.java.env.ClusterEnvironment;

/**
 * This example shows how to connect to a couchbase server EE cluster using
 * client certificate authentication.
 *
 * Please make sure to follow the proper server side documentation on how to
 * import the certifactes into the server and set up the Java keystore.
 *
 * https://developer.couchbase.com/documentation/server/current/security/security-x509certsintro.html
 */
public class ConnectingCertAuth {
    static String connectstring = "127.0.0.1";
    static String username="Administrator";
    static String password="password";

    public static void main(String... args) {
        ClusterEnvironment env = ClusterEnvironment.builder()
            .securityConfig(SecurityConfig.enableTls(true)
            .trustManagerFactory(InsecureTrustManagerFactory.INSTANCE))
            .ioConfig(IoConfig.enableDnsSrv(true))
            /*
            .sslEnabled(true)
            .certAuthEnabled(true)
            .sslKeystoreFile("/path/to/keystore")
            .sslKeystorePassword("password")
            .sslTruststoreFile("/path/to/truststore") // you can also pack it all in just the keystore
            .sslTruststorePassword("password")
                */
            .build();

        Cluster cluster =  Cluster.connect(connectstring,
            ClusterOptions.clusterOptions(username, password).environment(env));
        // IMPORTANT: do NOT call cluster.authenticate() since this is part of the cert auth
        Bucket bucket = cluster.bucket("default");

        // perform operations here...
        try {
            bucket.defaultCollection()
                .get("mydoc");
        } catch (DocumentNotFoundException dnf){
            System.out.println(dnf);
        }
    }

}
