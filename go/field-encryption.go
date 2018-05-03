package main

import (
	"github.com/couchbaselabs/gocbfieldcrypt"
	"gopkg.in/couchbase/gocb.v1"
	"log"
)

type Person struct {
	Password  string `json:"password" cbcrypt:"aes256,publickey,mysecret"`
	FirstName string
	LastName  string
	UserName  string
	Age       int
}

var bucket *gocb.Bucket

func main() {
	// Connect to the cluster
	cluster, err := gocb.Connect("couchbase://127.0.0.1")
	if err != nil {
		panic(err)
	}

	cluster.Authenticate(gocb.PasswordAuthenticator{
		Username: "Administrator",
		Password: "password",
	})

	bucket, err := cluster.OpenBucket("default", "")
	if err != nil {
		panic(err)
	}

	// Set up our key store
	publicKey := "!mysecretkey#9^5usdk39d&dlf)03sL"
	signingKey := "myauthpassword"
	keyStore := &gocbfieldcrypt.InsecureKeystore{
		Keys: map[string][]byte{
			"publickey": []byte(publicKey),
			"mysecret":  []byte(signingKey),
		},
	}

	// Set up our bucket to transcode via the field level encryption library
	bucket.SetTranscoder(&gocbfieldcrypt.Transcoder{
		KeyStore: keyStore,
	})

	// The Password field will be encrypted - see the definition of the
	// People struct above for reference of how to annotate the property
	teddy := Person{
		Age:       33,
		FirstName: "Ted",
		LastName:  "DeBloss",
		Password:  "ssloBeD12345",
	}

	//Password field will be encrypted in transport and at rest in the database
	_, err = bucket.Upsert("person::1", teddy, 0)
	if err != nil {
		panic(err)
	}

	// If the document is fetched without using the struct it will by-pass decryption so we
	// can see how the document is stored within Couchbase without triggering decryption
	var encryptedDoc interface{}
	_, err = bucket.Get("person::1", &encryptedDoc)
	if err != nil {
		panic(err)
	}

	log.Printf("Encrypted: %+v", encryptedDoc)

	// Fetching the document will reverse the encryption process so Password at the
	// application only will be in plaintext. In transport and in storage it will encrypted.
	var decryptedDoc Person
	_, err = bucket.Get("person::1", &decryptedDoc)
	if err != nil {
		panic(err)
	}

	log.Printf("Decrypted: %+v", encryptedDoc)
}
