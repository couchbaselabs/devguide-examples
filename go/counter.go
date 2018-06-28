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

	// Create a document and assign it to 10 - counter works atomically by
	//   first creating a document if it doesn't exist.   If it exists, the
	//   same method will increment/decrement per the "delta" parameter
	key := "goDevguideExampleCounter"
	curKeyValue, _, err := bucket.Counter(key, 2, 10, 0)
	if err != nil {
		fmt.Println("ERROR CREATING KEY:", err)
	}

	// Should Print 10
	fmt.Println("Initialized Counter:", curKeyValue)

	// Issue same operation, increment value by 2, to 12
	curKeyValue, _, err = bucket.Counter(key, 2, 10, 0)
	if err != nil {
		fmt.Println("ERROR CREATING KEY:", err)
	}

	// Should Print 12
	fmt.Println("Incremented Counter:", curKeyValue)

	// Exiting
	fmt.Println("Example Successful - Exiting")
}
