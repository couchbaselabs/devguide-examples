<?php

require __DIR__ . '/vendor/autoload.php';

use Couchbase\Aes256HmacSha256Provider;
use Couchbase\KeyProvider;
use Couchbase\Cluster;
use Couchbase\Bucket;

final class InsecureKeyProvider implements KeyProvider
{
    public function getKey(string $id)
    {
        switch ($id) {
        case 'mypublickey':
            return "!mysecretkey#9^5usdk39d&dlf)03sL";
        case 'HMAC_KEY_ID':
            return 'myauthpassword';
        default:
            throw new InvalidArgumentException("Unknown key '$id");
        }
    }
}

$cluster = new Cluster('couchbase://localhost');
$cluster->authenticateAs('Administrator', 'password');
$bucket = $cluster->openBucket('default');
$bucket->registerCryptoProvider(
    'AES-256-HMAC-SHA256',
    new Aes256HmacSha256Provider(new InsecureKeyProvider(), "mypublickey", "HMAC_KEY_ID")
);


// source document, which contains some sensitive data
$document = [
    'message' => 'The old grey goose jumped over the wrickety gate.'
];

// lets encrypt everything stored in the 'message' property using key with ID
// 'mypyblickey' and our crypto provider.
$encrypted = $bucket->encryptFields(
    $document,
    [
        [
            'name' => 'message',
            'alg' => 'AES-256-HMAC-SHA256'
        ]
    ]
);

// now we are ready to persist document with encrypted field using regular APIs
$bucket->upsert('secret-1', $encrypted);

// the database does not have our encryption keys, and cannot see the plain contents
$document = $bucket->get('secret-1')->value;
var_dump($document);
// the output should be similar to following:
// => object(stdClass)#9 (1) {
//      ["__crypt_message"]=>
//      object(stdClass)#8 (5) {
//        ["alg"]=>
//        string(19) "AES-256-HMAC-SHA256"
//        ["ciphertext"]=>
//        string(88) "aK1RxvZkP4YWyMapQTpiRKvAG6V1MsFWUJwNfY7TXh3d5DdFO3jwmQu3rFMN6p98Y4ziM+pQNkrB/Cc7GP9/yw=="
//        ["iv"]=>
//        string(24) "1tzdgObtNJmNOrgSImzdKg=="
//        ["kid"]=>
//        string(11) "mypublickey"
//        ["sig"]=>
//        string(44) "qStQ7U28A05nz/ZP5SKDSMQuMofy1K9QHX8nYALLwOo="
//      }
//    }

$decrypted = $bucket->decryptFields(
    $document,
    [
        [
            'name' => 'message',
            'alg' => 'AES-256-HMAC-SHA256'
        ]
    ]

);
var_dump($decrypted);
// now we have our document readable
// => array(1) {
//      ["message"]=>
//      string(49) "The old grey goose jumped over the wrickety gate."
//    }
