package main

import (
	"encoding/json"
	"fmt"
	"net/http"

	"gopkg.in/couchbase/gocb.v1"
)

// bucket reference - reuse as bucket reference in the application
var bucket *gocb.Bucket

// Create a struct for strongly typed query results
type TypedJSONAirport struct {
	Inner struct {
		Airport string `json:"airportname"`
		City    string `json:"city"`
		Country string `json:"country"`
		FAA     string `json:"faa"`
		Geo     struct {
			Alt int     `json:"alt"`
			Lat float64 `json:"lat"`
			Lon float64 `json:"lon"`
		} `json:"geo"`
	} `json:"travel-sample"`
}

func queryOne(w http.ResponseWriter, r *http.Request) {

	// Grab the parameter for the city we're looking for
	search := r.URL.Query().Get("search")

	// New query, a really generic one with high selectivity
	myQuery := gocb.NewN1qlQuery("SELECT * FROM `travel-sample` " +
		"WHERE name like '" + search + "%' OR airportname like '" + search + "%' ")
	rows, err := bucket.ExecuteN1qlQuery(myQuery, nil)
	if err != nil {
		fmt.Println("ERROR EXECUTING N1QL QUERY:", err)
	}

	// Interface for handling streaming return values
	var row interface{}

	// Stream the first result only into the interface
	err = rows.One(&row)
	if err != nil {
		fmt.Println("ERROR ITERATING QUERY RESULTS:", err)
	}

	// Marshal single result in interface
	jsonOut, err := json.Marshal(row)
	if err != nil {
		fmt.Println("ERROR PROCESSING STREAMING OUTPUT:", err)
	}

	// Return the JSON
	w.Write(jsonOut)

}

func queryUntyped(w http.ResponseWriter, r *http.Request) {

	// Grab the parameter for the city we're looking for
	search := r.URL.Query().Get("search")

	// New query, a really generic one with high selectivity
	myQuery := gocb.NewN1qlQuery("SELECT * FROM `travel-sample` " +
		"WHERE name like '" + search + "%' OR airportname like '" + search + "%' ")
	rows, err := bucket.ExecuteN1qlQuery(myQuery, nil)
	if err != nil {
		fmt.Println("ERROR EXECUTING N1QL QUERY:", err)
	}

	// Interfaces for handling streaming return values
	var retValues []interface{}
	var row interface{}

	// Stream the values returned from the query into an untyped and unstructred
	// array of interfaces
	for rows.Next(&row) {
		retValues = append(retValues, row)
	}

	// Marshal array of interfaces to JSON
	jsonOut, err := json.Marshal(retValues)
	if err != nil {
		fmt.Println("ERROR PROCESSING STREAMING OUTPUT:", err)
	}

	// Return the JSON
	w.Write(jsonOut)

}

func queryTyped(w http.ResponseWriter, r *http.Request) {

	// Grab the parameter for the city we're looking for
	search := r.URL.Query().Get("search")

	// New query, a really generic one with high selectivity
	myQuery := gocb.NewN1qlQuery("SELECT * FROM `travel-sample` " +
		"WHERE name like '" + search + "%' OR airportname like '" + search + "%' ")
	rows, err := bucket.ExecuteN1qlQuery(myQuery, nil)
	if err != nil {
		fmt.Println("ERROR EXECUTING N1QL QUERY:", err)
	}

	// Interfaces for handling streaming return values
	var row TypedJSONAirport
	var retValues []TypedJSONAirport

	// Stream the values returned from the query into a typed array of structs
	for rows.Next(&row) {

		// Check if the current row has a value for FAA, if it does it's an airport
		//  and should be added to the return values
		if row.Inner.FAA != "" {
			retValues = append(retValues, row)
		}

		// Set the row to an empty struct, to prevent current values being added
		//  to the next row in the results collection returned by the query
		row = TypedJSONAirport{}
	}

	// Marshal array of structs to JSON
	bytes, err := json.Marshal(retValues)
	if err != nil {
		fmt.Println("ERROR PROCESSING STREAMING OUTPUT:", err)
	}

	// Return the JSON
	w.Write(bytes)

}

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

	// Inbound http handlers
	http.Handle("/", http.FileServer(http.Dir("./public")))
	http.HandleFunc("/api/query/untyped", queryUntyped)
	http.HandleFunc("/api/query/typed", queryTyped)
	http.HandleFunc("/api/query/one", queryOne)
	fmt.Printf("Starting server on :3000\n")
	http.ListenAndServe(":3000", nil)

}
