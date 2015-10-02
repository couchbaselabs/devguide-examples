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

  // Create a document
   key := "goDevguideExampleUpdate"
   val := "Retrieve Test Value"
   _,err = bucket.Upsert(key,&val, 0)
  if err != nil{
      fmt.Println("ERRROR CREATING DOCUMENT:",err)
    }

  // Retrieve Document
  var retValue interface{}
  _,err = bucket.Get(key,&retValue)
  if err != nil{
      fmt.Println("ERRROR RETURNING DOCUMENT:",err)
    }
  fmt.Println("Document Retrieved:",retValue)

  // Add something to the value
  retValue = retValue.(string) + " with Additional Test Value"

  // Replace the existing document
  _,err = bucket.Replace(key,&retValue, 0, 0)
 if err != nil{
     fmt.Println("ERRROR REPLACING DOCUMENT:",err)
   }

 // Retrieve updated document
 _,err = bucket.Get(key,&retValue)
 if err != nil{
     fmt.Println("ERRROR RETURNING DOCUMENT:",err)
   }
 fmt.Println("Document Retrieved:",retValue)

  // Exiting
  fmt.Println("Example Successful  - Exiting")
}
