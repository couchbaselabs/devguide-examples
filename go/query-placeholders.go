package main

import (
	"github.com/couchbase/gocb"
	"fmt"
)

// bucket reference - reuse as bucket reference in the application
var bucket *gocb.Bucket

func main() {
	// Connect to Cluster
	cluster, err := gocb.Connect("couchbase://127.0.0.1")
  if err != nil{
  		fmt.Println("ERRROR CONNECTING TO CLUSTER:",err)
  	}
  // Open Bucket
	bucket, err = cluster.OpenBucket("travel-sample","")
  if err != nil{
      fmt.Println("ERRROR OPENING BUCKET:",err)
    }

  // Setup a new query with a placeholder
  myQuery := gocb.NewN1qlQuery("SELECT airportname, city, country FROM `travel-sample` " +
    "WHERE type='airport' AND city=$1 ")

  // Setup an array for parameters
  var myParams []interface{}
  myParams = append(myParams, "Reno")

  // Execute Query
  rows,err := bucket.ExecuteN1qlQuery(myQuery,myParams)
  if err!=nil{
      fmt.Println("ERROR EXECUTING N1QL QUERY:",err)
    }

  // Iterate through rows and print output
  var row interface{}
  for rows.Next(&row) {
      fmt.Printf("Results: %+v\n", row)
    }

  // Exiting
  fmt.Println("Example Succesful - Exiting")
}
