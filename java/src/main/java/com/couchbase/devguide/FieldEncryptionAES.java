package com.couchbase.devguide;

import java.nio.charset.Charset;

import com.couchbase.client.encryption.AES128CryptoProvider;
import com.couchbase.client.encryption.AES256CryptoProvider;
import com.couchbase.client.encryption.CryptoManager;
import com.couchbase.client.encryption.JceksKeyStoreProvider;
import com.couchbase.client.java.document.EntityDocument;
import com.couchbase.client.java.env.DefaultCouchbaseEnvironment;
import com.couchbase.client.java.repository.annotation.EncryptedField;
import com.couchbase.client.java.repository.annotation.Id;

public class FieldEncryptionAES extends ConnectionBase {

    static {
        try {
            JceksKeyStoreProvider kp = new JceksKeyStoreProvider("secret");
            kp.publicKeyName("mypublickey");
            kp.storeKey("mypublickey", "!mysecretkey#9^5usdk39d&dlf)03sL".getBytes(Charset.forName("UTF-8")));
            kp.signingKeyName("HMACsecret");
            kp.storeKey("HMACsecret", "myauthpassword".getBytes(Charset.forName("UTF-8")));
            AES256CryptoProvider aes256CryptoProvider = new AES256CryptoProvider(kp);
            CryptoManager cryptoManager = new CryptoManager();
            cryptoManager.registerProvider("MyAESProvider", aes256CryptoProvider);
            environment = DefaultCouchbaseEnvironment.builder().cryptoManager(cryptoManager).build();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    protected void doWork() {
        Person person = new Person();
        person.id = "johnd";
        person.password = "secret";
        person.firstName = "John";
        person.lastName = "Doe";
        person.userName = "jdoe";
        person.age = 20;

        EntityDocument<Person> document = EntityDocument.create(person);
        bucket.repository().upsert(document);
        EntityDocument<Person> stored = bucket.repository().get(person.id, Person.class);
        System.out.println("Password: " + stored.content().password);
    }

    public static class Person {

        @Id
        public String id;

        @EncryptedField(provider = "MyAESProvider")
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