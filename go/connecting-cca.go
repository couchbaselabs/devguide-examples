package main

import (
	"github.com/couchbase/gocb"
	"fmt"
)

func main() {
	// Connect to the cluster using certificates and node key, note: couchbases
	cluster, err := gocb.Connect("couchbases://10.111.175.101?" +
		"cacertpath=../x509/ca.pem&" +
		"certpath=../x509/chain.pem&" +
		"keypath=../x509/pkey.key")
	if err != nil {
		fmt.Println("ERROR CONNECTING TO CLUSTER:", err)
	}

	// Use the CertificateAuthenticator to authenticate
	cluster.Authenticate(gocb.CertificateAuthenticator{})

	// Open the bucket
	_, err = cluster.OpenBucket("travel-sample", "")
	if err != nil {
		fmt.Println("ERROR OPENING BUCKET:", err)
	}

	fmt.Println("Example Successful - Exiting")

}

