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

import com.couchbase.client.core.encryption.CryptoManager;
import com.couchbase.client.encryption.AeadAes256CbcHmacSha512Provider;
import com.couchbase.client.encryption.DefaultCryptoManager;
import com.couchbase.client.encryption.KeyStoreKeyring;
import com.couchbase.client.encryption.Keyring;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.ClusterOptions;
import com.couchbase.client.java.encryption.annotation.Encrypted;
import com.couchbase.client.java.env.ClusterEnvironment;

import java.io.FileInputStream;
import java.security.KeyStore;


public class FieldEncryptionAES extends ConnectionBase {

    @Override
    protected void doWork() {
        Cluster cluster;
        try {
            KeyStore javaKeyStore = KeyStore.getInstance("MyKeyStoreType");
            FileInputStream fis = new java.io.FileInputStream("keyStoreName");
            char[] password = {'a', 'b', 'c'};
            javaKeyStore.load(fis,
                password);
            Keyring keyring = new KeyStoreKeyring(javaKeyStore,
                keyName -> "swordfish");

            // AES-256 authenticated with HMAC SHA-512. Requires a 64-byte key.
            AeadAes256CbcHmacSha512Provider provider = AeadAes256CbcHmacSha512Provider.builder()
                .keyring(keyring)
                .build();

            CryptoManager cryptoManager = DefaultCryptoManager.builder()
                .decrypter(provider.decrypter())
                .defaultEncrypter(provider.encrypterForKey("myKey"))
                .build();

            ClusterEnvironment env = ClusterEnvironment.builder()
                .cryptoManager(cryptoManager)
                .build();

            cluster = Cluster.connect("localhost",
                ClusterOptions.clusterOptions("username",
                    "password")
                    .environment(env));
        } catch (Exception e){
           throw new RuntimeException(e);
        }

        Bucket myBucket = cluster.bucket(bucketName);

        Person person = new Person();
        person.id = "johnd";
        person.password = "secret";
        person.firstName = "John";
        person.lastName = "Doe";
        person.userName = "jdoe";
        person.age = 20;

        myBucket.defaultCollection().upsert(person.id, person);
        Person stored = bucket.defaultCollection().get(person.id).contentAs(Person.class);
        System.out.println("Password: " + stored.password);
    }

    public static class Person {

        public String id;

        @Encrypted
        public String password;

        //The rest will be transported and stored unencrypted
        public String firstName;
        public String lastName;
        public String userName;
        public int age;
    }

    public static void main(String[] args) {
        new FieldEncryptionAES().execute();
    }
}
