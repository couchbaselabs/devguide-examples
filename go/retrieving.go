package main

import (
	"fmt"

	"github.com/couchbase/gocb"
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

	// Create a document
	key := "goDevguideExampleRetrieve"
	val := "Retrieve Test Value"
	_, err = bucket.Upsert(key, &val, 0)
	if err != nil {
		fmt.Println("ERROR CREATING DOCUMENT:", err)
	}

	// Retrieve Document
	var retValue interface{}
	_, err = bucket.Get(key, &retValue)
	if err != nil {
		fmt.Println("ERROR RETURNING DOCUMENT:", err)
	}
	fmt.Println("Document Retrieved:", retValue)

	// Exiting
	fmt.Println("Example Successful - Exiting")
}
