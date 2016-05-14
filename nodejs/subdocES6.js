// This is a Couchbase subdoc api example written using ES6 features.  Please
//  ensure the version of nodejs installed in your environment is using at
//  at least version
'use strict';

// Require Couchbase Module
var couchbase = require('couchbase');

// Setup Cluster Connection Object
var cluster = new couchbase.Cluster('couchbase://192.168.99.100');

// Setup Bucket object to be reused within the code
var bucket = cluster.openBucket('travel-sample');

// Key for example
var key = "nodeDevguideExampleSubdoc";

// Run the example
verifyNodejsVersion()
    .then(storeInitial)
    .then(lookupEntireDocument)
    .then(subdocItemLookupTwoFields)
    .then(subdocArrayAdd)
    .then(lookupEntireDocument)
    .then(subdocArrayManipulation)
    .then(subDocumentItemLookup)
    .then(subdocArrayRemoveItem)
    .then(subDocumentItemLookup)
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

function storeInitial() {
    return new Promise((resolve, reject) => {
        bucket.upsert(key, {
            firstItem: "Some Test Field Data for firstItem",
            secondItem: "Some Test Field Data for secondItem",
            thirdItem: "Some Test Field Data for thirdItem"
        }, (err, res) => {
            if (err) reject(err);
            else
            // Print results
                console.log('Initialized document, stored to bucket...');
            resolve();
        });
    });
}

function lookupEntireDocument() {
    return new Promise((resolve, reject) => {
        // Get Document
        bucket.get(key, (err, resReadFullDoc) => {
            if (err) reject(err);
            else
            // Print Document Value
                console.log("Retrieve full document:", resReadFullDoc.value, "\n", "\n");
            resolve();
        });
    });
}

function subDocumentItemLookup() {
    return new Promise((resolve, reject) => {
        // Get Document
        bucket.lookupIn(key).
        get("fourthItem").
        execute((err, resSubdocOp) => {
            if (err) reject(err);
            else
            // Print the values
                console.log("Retrieve modified fourth item:", resSubdocOp.contents, "\n", "\n");
            resolve();
        });
    });
}

function subdocItemLookupTwoFields() {
    return new Promise((resolve, reject) => {
        // Subdoc Operation: Retrieve Document Fragment for two fields, using LookupIn
        bucket.lookupIn(key).
        get("secondItem").get("thirdItem").
        execute((err, resSubdocOp1) => {
            if (err) reject(err);
            else
            // Print the values
                console.log("Retrieve just second and third items:\n", resSubdocOp1.contents, "\n", "\n");
            resolve();
        });
    });
}

function subdocArrayAdd() {
    console.log("Add array to the fourth item of the document...");
    return new Promise((resolve, reject) => {
        bucket.mutateIn(key, 0, 0).
        upsert("fourthItem", ["250 GT SWB", "250 GTO", "250 LM", "275 GTB"], true).
        execute((err, resSubdocOp2) => {
            if (err) reject(err);
            else
                resolve();
        });
    });
}

function subdocArrayManipulation() {
    console.log("Add a value to a specific position, to the 'front' \n" +
        "  of the fourthItem Array, to the 'back' of the \n" +
        "  fourthItem Array, and another unique value to \n" +
        "  the back of the Array in one operation...");
    return new Promise((resolve, reject) => {
        // Subdoc Operation: Add a value to a specific position, to the "front"
        //   of the fourthItem Array, to the "back" of the fourthItem Array, and
        //   another unique value to the back of the Array in one operation
        bucket.mutateIn(key, 0, 0).
        arrayInsert("fourthItem[2]", "250 GTO Series II").
        pushFront("fourthItem", "250 GT Lusso", false).
        pushBack("fourthItem", "275 GTB/4", false).
        addUnique("fourthItem", "288 GTO", false).
        execute((err, resSubdocOp3)=>{
            if (err) reject(err);
            else
                resolve();
        });
    });
}

function subdocArrayRemoveItem() {
    console.log("Remove item at position three in fourth item array...");
    return new Promise((resolve, reject) => {
        // Subdoc Operation: Remove a value from the fourthItem Array
        bucket.mutateIn(key, 0, 0).
        remove("fourthItem[3]").execute((err, resSubdocOp5)=> {
            if (err) reject(err);
            else
                resolve();
        });
    });
}
