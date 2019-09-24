package main

import (
	"fmt"
	"time"

	"gopkg.in/couchbase/gocb.v1"
)

// bucket reference - reuse as bucket reference in the application
var bucket *gocb.Bucket

func main() {
	// Connect to Cluster
	cluster, err := gocb.Connect("couchbase://127.0.0.1")
	if err != nil {
		fmt.Println("ERROR CONNECTING TO CLUSTER:", err)
	}
	// Open Bucket
	bucket, err = cluster.OpenBucket("travel-sample", "")
	if err != nil {
		fmt.Println("ERROR OPENING BUCKET:", err)
	}

	// Create a document and assign an expiration of 2 seconds
	key := "goDevguideExampleExpiration"
	val := "Expiration Test Value"
	_, err = bucket.Upsert(key, &val, 2)
	if err != nil {
		fmt.Println("ERROR CREATING DOCUMENT:", err)
	}

	// Retrieve Value Right Away
	var retValue interface{}
	_, err = bucket.Get(key, &retValue)
	if err != nil {
		fmt.Println("ERROR RETURNING DOCUMENT:", err)
	}
	fmt.Println("Document Not Yet Expired:", retValue)

	// Sleep for 4 seconds
	time.Sleep(4 * time.Second)

	// Try to retrieve document when Expired
	_, err = bucket.Get(key, &retValue)
	if err != nil {
		fmt.Println("Document Expired:", err)
	}

	// Create a document with no expiration, and add an expiry using
	//  touch after the document is created of 2 seconds
	_, err = bucket.Upsert(key, &val, 2)
	if err != nil {
		fmt.Println("ERROR CREATING DOCUMENT:", err)
	}

	// Add an expiry
	_, err = bucket.Touch(key, 0, 2)
	if err != nil {
		fmt.Println("ERROR TOUCHING DOCUMENT:", err)
	}

	// Retrieve Document
	_, err = bucket.Get(key, &retValue)
	if err != nil {
		fmt.Println("ERROR RETURNING DOCUMENT:", err)
	}
	fmt.Println("Document Not Yet Expired:", retValue)

	// Sleep for 4 seconds
	time.Sleep(4 * time.Second)

	// Try to retrieve document when Expired
	_, err = bucket.Get(key, &retValue)
	if err != nil {
		fmt.Println("Document Expired:", err)
	}

	// Exiting
	fmt.Println("Example Successful - Exiting")
}
