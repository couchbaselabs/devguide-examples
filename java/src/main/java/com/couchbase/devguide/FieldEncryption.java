package com.couchbase.devguide;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.SecureRandom;

import com.couchbase.client.crypto.AES128CryptoProvider;
import com.couchbase.client.crypto.EncryptionConfig;
import com.couchbase.client.crypto.JceksKeyStoreProvider;
import com.couchbase.client.java.document.EntityDocument;
import com.couchbase.client.java.encryption.EncryptionProvider;
import com.couchbase.client.java.env.DefaultCouchbaseEnvironment;
import com.couchbase.client.java.repository.annotation.EncryptedField;
import com.couchbase.client.java.repository.annotation.Id;

public class FieldEncryption extends ConnectionBase {

	static {
		try {
			String secretKeyName = "secretKey";
			KeyGenerator keyGen = KeyGenerator.getInstance("AES");
			SecureRandom random = new SecureRandom();
			keyGen.init(128, random);
			SecretKey secretKey = keyGen.generateKey();
			JceksKeyStoreProvider kp = new JceksKeyStoreProvider("secret");
			kp.storeKey(secretKeyName, secretKey.getEncoded());
			AES128CryptoProvider aes128CryptoProvider = new AES128CryptoProvider(kp, secretKeyName);
			EncryptionConfig encryptionConfig = new EncryptionConfig();
			encryptionConfig.addCryptoProvider(aes128CryptoProvider);
			environment = DefaultCouchbaseEnvironment.builder()
					.encryptionConfig(encryptionConfig)
					.build();
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
		person.age = 20;

		EntityDocument<Person> document = EntityDocument.create(person);
		bucket.repository().upsert(document);
		EntityDocument<Person> stored = bucket.repository().get(person.id, Person.class);
		System.out.println("Password: " + stored.content().password);
	}

	public static class Person {

		@Id
		public String id;

		@EncryptedField(provider = EncryptionProvider.AES128)
		public String password;

		//The rest will be transported and stored unencrypted
		public String firstName;
		public String lastName;
		public String userName;
		public int age;
	}

	public static void main(String[] args) {
		new FieldEncryption().execute();
	}
}

