'use strict';

var couchbase = require('couchbase');
var cbfieldcrypt = require('couchbase-encryption');

var cluster = new couchbase.Cluster('couchbase://127.0.0.1');
cluster.authenticate('Administrator', 'password');
var bucket = cluster.openBucket('default');


var publicKey = '!mysecretkey#9^5usdk39d&dlf)03sL';
var signingKey = 'myauthpassword';

var keyStore = new cbfieldcrypt.InsecureKeyStore();
keyStore.addKey('publickey', publicKey);
keyStore.addKey('mysecret', signingKey);

var personCryptFields = {
  password: new cbfieldcrypt.AesCryptoProvider(keyStore, 'publickey', 'mysecret')
};

// The Password field will be encrypted - see the definition of the
// People object encrypted fields above.
var teddy = {
  age: 33,
  firstName: 'Ted',
  lastName: 'DeBloss',
  password: 'ssloBeD12345'
};

//Password field will be encrypted in transport and at rest in the database
var encryptedTeddy = cbfieldcrypt.encryptFields(teddy, personCryptFields);
bucket.upsert('person::1', encryptedTeddy, function(err, res) {
  if (err) {
    throw err;
  }

  bucket.get('person::1', function(err, res) {
    if (err) {
      throw err;
    }

    // Inspecting the data before performing decryption allows us to see
    // how the document is stored in Couchbase.
    var encryptedData = res.value;
    console.log('Encrypted:', encryptedData);

    // Performing decryption on the document will reverse the encryption
    // process applied above and allow you to view the password in plain-text.
    var decryptedData =
        cbfieldcrypt.decryptFields(encryptedData, personCryptFields);
    console.log('Decrypted:', decryptedData);

    process.exit(0);
  });
});

