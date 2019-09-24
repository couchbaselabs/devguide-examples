package main

import (
	"fmt"
	"strconv"

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

	// Create a JSON document
	type Doc struct {
		Item string `json:"item"`
	}
	key := "goDevguideExampleBulkInsert"
	val := Doc{"A bulk insert test value"}

	// Create an Array of BulkOps for Insert
	var items []gocb.BulkOp

	// Add 10 items to the array that will be performed as a bulk operation
	for i := 0; i < 10; i++ {
		items = append(items, &gocb.InsertOp{Key: key + "_" + strconv.Itoa(i), Value: &val})
	}

	// Perform the bulk operation
	err = bucket.Do(items)
	if err != nil {
		fmt.Println("ERROR PERFORMING BULK INSERT:", err)
	}

	// Exiting
	fmt.Println("Example Successful  - Exiting")
}
