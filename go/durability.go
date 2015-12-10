package main

import (
	"fmt"
	"github.com/couchbase/gocb"
	"time"
)

// bucket reference - reuse as bucket reference in the application
var bucket *gocb.Bucket

func main() {
	// Connect to Cluster
	cluster, err := gocb.Connect("couchbase://127.0.0.1")
	if err != nil {
		fmt.Println("ERRROR CONNECTING TO CLUSTER:", err)
	}
	// Open Bucket
	bucket, err = cluster.OpenBucket("travel-sample", "")
	if err != nil {
		fmt.Println("ERRROR OPENING BUCKET:", err)
	}
	// Set Durability Timeout to 3 seconds, default is 40 seconds
	bucket.SetDurabilityTimeout(3 * time.Second)

	// Create a document and assign a "Persist To" value of 1 node.
	// Should Always Succeed, even on single node cluster.
	//   NOTICE the function signature:
	//      Bucket.UpsertDura(key, value, expiry, replicateTo, _PERSIST_TO_)
	key := "goDevguideExamplePersistTo"
	val := "Durabilty PersistTo Test Value"
	_, err = bucket.UpsertDura(key, &val, 0, 0, 1)
	if err != nil {
		fmt.Println("DURABILITY Persist Example:", err)
		fmt.Println("ERRROR CREATING DOCUMENT:", err)
	}

	// Retrieve Value Persist To
	var retValue interface{}
	_, err = bucket.Get(key, &retValue)
	if err != nil {
		fmt.Println("ERRROR RETURNING DOCUMENT:", err)
	}
	fmt.Println("Document Retrieved:", retValue)

	// Create a document and assign a "Replicate To" value of 1 node.
	// Should Fail on a single node cluster, succeed on a multi node
	// cluster of 3 or more nodes with at least one replica enabled.
	//   NOTICE the function signature:
	//      Bucket.UpsertDura(key, value, expiry, _REPLICATE_TO_, persistTo)

	key = "goDevguideExampleReplicateTo"
	val = "Durabilty ReplicateTo Test Value"
	_, err = bucket.UpsertDura(key, &val, 0, 1, 0)
	if err != nil {
		fmt.Println("DURABILITY Replicate Example:", err)
		fmt.Println("ERRROR CREATING DOCUMENT:", err)
	}

	// Retrieve Value Replicate To
	// Should succeed even if durability fails, as the document was
	// still written.
	_, err = bucket.Get(key, &retValue)
	if err != nil {
		fmt.Println("ERRROR RETURNING DOCUMENT:", err)
	}
	fmt.Println("Document Retrieved:", retValue)

	// Create a document and assign a "Replicate To" and a "Persist TO"
	// value of 1 node. Should Fail on a single node cluster, succeed on
	// a multi node cluster of 3 or more nodes with at least one replica
	// enabled.
	//   NOTICE the function signature:
	//      Bucket.UpsertDura(key, value, expiry, _REPLICATE_TO_, _PERSIST_TO_)

	key = "goDevguideExamplePersistToAndReplicateTo"
	val = "Durabilty PersistTo and ReplicateTo Test Value"
	_, err = bucket.UpsertDura(key, &val, 0, 1, 1)
	if err != nil {
		fmt.Println("DURABILITY Persist and Replicate Example:", err)
		fmt.Println("ERRROR CREATING DOCUMENT:", err)
	}

	// Retrieve Value Replicate To
	// Should succeed even if durability fails, as the document was
	// still written.
	_, err = bucket.Get(key, &retValue)
	if err != nil {
		fmt.Println("ERRROR RETURNING DOCUMENT:", err)
	}
	fmt.Println("Document Retrieved:", retValue)

	// Exiting
	fmt.Println("Example Complete - Exiting")
}
