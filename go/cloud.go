package main

import (
	"fmt"
	"log"

	"gopkg.in/couchbase/gocb.v1"
)

func main() {
	// Uncomment following line to enable logging
	// gocb.SetLogger(gocb.VerboseStdioLogger())
	endpoint := "cb.e493356f-f395-4561-a6b5-a3a1ec0aaa29.dp.cloud.couchbase.com"
	bucketName := "couchbasecloudbucket"
	username := "user"
	password := "password"

	// Initialize the Connection
	cluster, err := gocb.Connect("couchbases://" + endpoint + "?ssl=no_verify")
	if err != nil {
		log.Fatal(err)
	}

	_ = cluster.Authenticate(gocb.PasswordAuthenticator{
		Username: username,
		Password: password,
	})

	bucket, err := cluster.OpenBucket(bucketName, "")
	if err != nil {
		log.Fatal(err)
	}
	fmt.Println("Connected..")

	// Create a N1QL Primary Index (but ignore if it exists)
	err = bucket.Manager("", "").CreatePrimaryIndex("", true, false)
	if err != nil {
		log.Fatal(err)
	}

	type User struct {
		Name      string   `json:"name"`
		Email     string   `json:"email"`
		Interests []string `json:"interests"`
	}

	// Create and store a Document
	_, err = bucket.Upsert("u:kingarthur",
		User{
			Name:      "Arthur",
			Email:     "kingarthur@couchbase.com",
			Interests: []string{"Holy Grail", "African Swallows"},
		}, 0)
	if err != nil {
		log.Fatal(err)
	}

	// Get the document back
	var inUser User
	_, err = bucket.Get("u:kingarthur", &inUser)
	if err != nil {
		log.Fatal(err)
	}
	fmt.Printf("User: %v\n", inUser)

	// Perform a N1QL Query
	query := gocb.NewN1qlQuery(fmt.Sprintf("SELECT name FROM `%s` WHERE $1 IN interests", bucketName))
	rows, err := bucket.ExecuteN1qlQuery(query, []interface{}{"African Swallows"})
	if err != nil {
		log.Fatal(err)
	}

	// Print each found Row
	var row interface{}
	for rows.Next(&row) {
		fmt.Printf("Row: %v", row)
	}
}
