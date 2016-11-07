// This is a Couchbase bulk api example written using ES6 features.  This
//  example creates a group of documents, and then retrives them using the
//  "getMulti" method to load them all into the node application memory.  It
//  also shows an asyncronous pattern for retrieving documents, and then shows
//  an example of how this might be used for bulk processing to update documents
//  in bulk.
//
// Please ensure the version of nodejs installed in your environment is using
//  at least version 4.0.0 prior to running this example.
//
//
// TO RUN:
//  [1] Include package.json file below
//  [2] npm install
//  [3] Change the Couchbase "Connection string" to match your cluster
//  [4] node app.js OR npm start
//  [5] Edit these fields to change the characteristics of what this example does:
//    -- connString: The connection string to your cluster
//    -- opsGroup: This is the "buffer" of operations to keep in the queue for
//        creating new documents.
//    -- totalDocs: Number of new documents to create/and then bulk retrieve
//    -- documentSize: Controls how big of a field (in chracters) the "fieldToProcess"
//        field is for each document created.
//
//  package.json File, save and include separately
/*
{
  "name": "nodejs",
  "version": "1.0.0",
  "description": "Test Application Codebook",
  "main": "bulk.js",
  "scripts": {
       "start": "node bulk.js"
  },
  "author": "todd@couchbase.com",
  "license": "ISC",
  "dependencies": {
    "couchbase": "^2.2.3"
  }
}
*/

'use strict';

// Key for example
var connString='couchbase://localhost';
var opsGroup=1000;
var totalDocs=10000;
var documentSize=2048;
var getMultiArray=[];

// Require Couchbase Module
var couchbase = require('couchbase');

// Setup Cluster Connection Object
var cluster = new couchbase.Cluster(connString);

// Setup Bucket object to be reused within the code
var bucket = cluster.openBucket('travel-sample');

// Run the example
verifyNodejsVersion()
    .then(preload)
    .then(bulkGetMulti)
    .then(bulkGetAsyncPattern)
    .then(bulkUpdateAsyncPattern)
    .then(() => {
        process.exit(0);
    })
    .catch((err) => {
        console.log("ERR:", err)
        process.exit(0);
    });

function verifyNodejsVersion() {
    return new Promise(
        (resolve, reject) => {
            if (parseInt(((process.version).split("v"))[1].substr(0, 1)) < 4) {
                console.log("\n  The nodejs version is too low.  This application requires\n" +
                    "  ES6 features, specifically: \n" +
                    "    --promises \n    --arrow functions \n" +
                    "  Please upgrade the nodejs version from:\n    --Current " +
                    process.version + "\n    --Minimum:4.0.0");
                reject();
            } else resolve();
        });
}

function preload() {
    return new Promise(
        (resolve, reject) => {

            var completed = 0;
            var runFlag = false;
            var startTime = process.hrtime();

            // Function for upserting one document, during preload.  Notice,
            // this is only in scope for preload
            function upsertOne() {

                // First Check if the preloading is done
                if (completed >= totalDocs && !runFlag) {
                    runFlag = true;
                    var time = process.hrtime(startTime);
                    console.log("====");
                    console.log("  Async Insert Loop Took: " + parseInt((time[0] * 1000) +
                            (time[1] / 1000000)) + " ms for: " + getMultiArray.length +
                        " items");
                    resolve();
                } else {
                    if (completed <= totalDocs) {

                        // Add key to array for later bulk retrieval
                        getMultiArray[completed] = 'test' + completed;

                        // Upsert one document
                        bucket.upsert('test' + completed, {
                            fieldToProcess: generateCharacters(documentSize),
                            fieldType: "url",
                            lastEdited: Date(),
                            rev: 0
                        }, function(err, res) {
                            if (err) reject(err);

                            // This will fire WHEN and only WHEN a callback is received.
                            if (res) {
                                // Increment completed upserts count
                                completed++;

                                // Recursive call to insert
                                upsertOne();
                            }
                        });
                    }
                }
            }
            // The loop that sets up a "buffer" of queued operations
            // This sets up a number of requests always in the buffer waiting to execute
            for (var i = 0; i < opsGroup; ++i) {
                upsertOne();
            }
        });
}

function bulkGetMulti() {
    return new Promise((resolve, reject) => {
        var startTime = process.hrtime();

        // This is the only bulk method exposed by the nodejs SDK.   This method
        // takes an array of keys, and returns a map of json documents for all
        // documents retrieved.   It will fire a callback when completed.
        bucket.getMulti(getMultiArray, function(err, res) {
            if (err) {
                console.console.log("  Error:", error);
            }
            // The callback "res" will only fire once the getMulti
            // operation has finished
            if (res) {
                var time = process.hrtime(startTime);
                console.log("====");
                console.log("  Bulk getMulti took: " + parseInt((time[0] * 1000) +
                        (time[1] / 1000000)) + " ms for: " + getMultiArray.length +
                    " items");
                resolve();
            }
        });
    });
}

function bulkGetAsyncPattern(){
  return new Promise(
      (resolve, reject) => {

          var completed = 0;
          var runFlag = false;
          var startTime = process.hrtime();

          // Function for modify one document, during bulk loop.  Notice,
          // this is only in scope for bulkGetAsyncPattern
          function getOne() {

              // First Check if the bulk pattern loop is done
              if (completed >= totalDocs && !runFlag) {
                  runFlag = true;
                  var time = process.hrtime(startTime);
                  console.log("====");
                  console.log("  Bulk Pattern Get Loop Took: " + parseInt((time[0] * 1000) +
                          (time[1] / 1000000)) + " ms for: " + getMultiArray.length +
                      " items");
                  resolve();
              } else {
                  if (completed <= totalDocs) {

                      // Modify One Document
                      bucket.get(getMultiArray[completed], function(err, res) {
                          if (err) console.log("  Error Retrieving:", err.message);

                          // This will fire WHEN and only WHEN a callback is received.
                          if (res) {
                              // Increment completed count
                              completed++;

                              // Recursive call to modify
                              getOne();
                          }
                      });
                  }
              }
          }
          // The loop that sets up a "buffer" of queued operations
          // This sets up a number of requests always in the buffer waiting to execute
          for (var i = 0; i < opsGroup; ++i) {
              getOne();
          }
      });
}

function bulkUpdateAsyncPattern(){
  return new Promise(
      (resolve, reject) => {

          var completed = 0;
          var runFlag = false;
          var startTime = process.hrtime();

          // Function for modify one document, during bulk loop.  Notice,
          // this is only in scope for bulkUpdateAsyncPattern
          function modifyOne() {

              // First Check if the bulk pattern loop is done
              if (completed >= totalDocs && !runFlag) {
                  runFlag = true;
                  var time = process.hrtime(startTime);
                  console.log("====");
                  console.log("  Bulk Pattern Processing Loop Took: " + parseInt((time[0] * 1000) +
                          (time[1] / 1000000)) + " ms for: " + getMultiArray.length +
                      " items");
                  resolve();
              } else {
                  if (completed <= totalDocs) {

                      // Modify One Document
                      bucket.mutateIn('test' + completed, 0, 0)
                      .counter('rev', 1, false)
                      .execute( function(err, res) {
                          if (err) console.log("  Error modifying:", err.message);

                          // This will fire WHEN and only WHEN a callback is received.
                          if (res) {
                              // Increment completed count
                              completed++;

                              // Recursive call to modify
                              modifyOne();
                          }
                      });
                  }
              }
          }
          // The loop that sets up a "buffer" of queued operations
          // This sets up a number of requests always in the buffer waiting to execute
          for (var i = 0; i < opsGroup; ++i) {
              modifyOne();
          }
      });
}

// Generate Random Characters to document field size
function generateCharacters(len) {
    var rdmString = "";
    for (; rdmString.length < len; rdmString += Math.random().toString(36).substr(2));
    return rdmString.substr(0, len);
}
