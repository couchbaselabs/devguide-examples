package main

import (
	"fmt"
	"github.com/couchbase/gocb"
	"strconv"
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

	// Create Array of BulkOps for Insert, and one for Get
	var items []gocb.BulkOp
	var itemsGet []gocb.BulkOp

	// Add 10 items to the array that will be performed as a bulk operation
	for i := 0; i < 10; i++ {
		items = append(items, &gocb.InsertOp{Key: key + "_" + strconv.Itoa(i), Value: &val})
	}

	// Perform the bulk operation to Insert
	err = bucket.Do(items)
	if err != nil {
		fmt.Println("ERROR PERFORMING BULK INSERT:", err)
	}

	// Retrieve 10 items to the array that will be performed as a bulk operation
	for i := 0; i < 10; i++ {
		itemsGet = append(itemsGet, &gocb.GetOp{Key: key + "_" + strconv.Itoa(i), Value: &Doc{}})
	}

	// Perform the bulk operation to Get
	err = bucket.Do(itemsGet)
	if err != nil {
		fmt.Println("ERROR PERFORMING BULK GET:", err)
	}

	// Print the output
	for i := 0; i < len(itemsGet); i++ {
		fmt.Println(itemsGet[i].(*gocb.GetOp).Key, itemsGet[i].(*gocb.GetOp).Value.(*Doc).Item)
	}

	// Exiting
	fmt.Println("Example Successful  - Exiting")
}
