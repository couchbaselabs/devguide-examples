package main

import (
	"fmt"
	"github.com/couchbase/gocb"
)

// bucket reference - reuse as bucket reference in the application
var bucket *gocb.Bucket

func main() {
	// Create a JSON document struct
	type Doc struct {
		FirstItem  string `json:"firstItem"`
		SecondItem string `json:"secondItem"`
		ThirdItem  string `json:"thirdItem"`
	}

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
	// Create a document
	key := "goDevguideExampleSubdoc"
	val := Doc{"Some Test Field Data for firstItem", "Some Test Field Data for secondItem", "Some Test Field Data for thirdItem"}
	_, err = bucket.Upsert(key, &val, 0)
	if err != nil {
		fmt.Println("ERRROR CREATING DOCUMENT:", err)
	}
	// Retrieve Full Document, Get operation
	var retValue interface{}
	_, err = bucket.Get(key, &retValue)
	if err != nil {
		fmt.Println("ERRROR RETURNING DOCUMENT:", err)
	}
	fmt.Println("Document Retrieved:", retValue)

	// Subdoc Operation: Retrieve Document Fragment for two fields, using LookupIn
	frag, err := bucket.LookupIn("goDevguideExampleSubdoc").Get("secondItem").Get("thirdItem").Execute()
	if err != nil {
		fmt.Println("ERRROR RETURNING DOCUMENT FRAGMENT:", err)
	}
	// Print results
	frag.Content("firstItem", &retValue)
	fmt.Println("Document Fragment Retrieved (secondItem):", retValue)
	frag.Content("thirdItem", &retValue)
	fmt.Println("Document Fragment Retrieved (thirdItem):", retValue)

	// Subdoc Operation: Add a fourth item that is an array
	frag, err = bucket.MutateIn("goDevguideExampleSubdoc", 0, 0).
		Upsert("fourthItem", []string{"250 GT SWB", "250 GTO", "250 LM", "275 GTB"}, true).
		Execute()
	if err != nil {
		fmt.Println("ERRROR ADDING DOCUMENT FRAGMENT:", err)
	}
	// LookupIn to retrieve the changes
	frag, err = bucket.LookupIn("goDevguideExampleSubdoc").Get("fourthItem").Execute()
	if err != nil {
		fmt.Println("ERRROR RETURNING DOCUMENT FRAGMENT:", err)
	}
	frag.Content("fourthItem", &retValue)
	fmt.Println("Document Fragment Retrieved (fourthItem):", retValue)

	// Subdoc Operation: Add a value to a specific position, to the "front" of the fourthItem Array,
	// to the "back" of the fourthItem Array, and another unique value to the back of the Array in one operation
	frag, err = bucket.MutateIn("goDevguideExampleSubdoc", 0, 0).
		ArrayInsert("fourthItem[2]", "250 GTO Series II").
		PushFront("fourthItem", "250 GT Lusso", false).
		PushBack("fourthItem", "275 GTB/4", false).
		AddUnique("fourthItem", "288 GTO", false).
		Execute()
	if err != nil {
		fmt.Println("ERRROR ADDING DOCUMENT FRAGMENT:", err)
	}
	// LookupIn to retrieve the changes
	frag, err = bucket.LookupIn("goDevguideExampleSubdoc").Get("fourthItem").Execute()
	if err != nil {
		fmt.Println("ERRROR RETURNING DOCUMENT FRAGMENT:", err)
	}
	frag.Content("fourthItem", &retValue)
	fmt.Println("Document Fragment Retrieved (fourthItem):", retValue)

	// Subdoc Operation: Remove a value from the fourthItem Array
	frag, err = bucket.MutateIn("goDevguideExampleSubdoc", 0, 0).Remove("fourthItem[3]").Execute()
	if err != nil {
		fmt.Println("ERRROR ADDING DOCUMENT FRAGMENT:", err)
	}
	// LookupIn to retrieve the changes
	frag, err = bucket.LookupIn("goDevguideExampleSubdoc").Get("fourthItem").Execute()
	if err != nil {
		fmt.Println("ERRROR RETURNING DOCUMENT FRAGMENT:", err)
	}
	frag.Content("fourthItem", &retValue)
	fmt.Println("Document Fragment Retrieved (fourthItem):", retValue)

	// Exiting
	fmt.Println("Example Successful - Exiting")
}
