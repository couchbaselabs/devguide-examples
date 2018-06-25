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
        $root = dirname(__FILE__);
        switch ($id) {
        case 'MyPublicKeyName':
            return file_get_contents("$root/../../etc/field-level-encryption/publickey.cer");
        case 'MyPrivateKeyName':
            return file_get_contents("$root/../../etc/field-level-encryption/private.key");
        default:
            throw new InvalidArgumentException("Unknown key '$id");
        }
    }
}

$cluster = new Cluster('couchbase://localhost');
$cluster->authenticateAs('Administrator', 'password');
$bucket = $cluster->openBucket('default');
$bucket->registerCryptoProvider(
    'RSA-2048-OAEP-SHA1',
    new Aes256HmacSha256Provider(new InsecureKeyProvider(), "MyPublicKeyName", "MyPrivateKeyName")
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
            'alg' => 'RSA-2048-OAEP-SHA1'
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
//        string(18) "RSA-2048-OAEP-SHA1"
//        ["ciphertext"]=>
//        string(88) "uY14lwNqKQSZCNPc23h8dXgLbkj6hrWG5wA+9swPJQmuqOXUUr4YI9IE6a5vplG8z7XUnrmrpeG2y/85hu2zDg=="
//        ["iv"]=>
//        string(24) "n+ijNQiRYaTyguGoh7plCQ=="
//        ["kid"]=>
//        string(15) "MyPublicKeyName"
//        ["sig"]=>
//        string(44) "PJGt++Yz74QcMclan0p97l1dkBlMuCidH2OlSpYrBeU="
//      }
//    }

$decrypted = $bucket->decryptFields(
    $document,
    [
        [
            'name' => 'message',
            'alg' => 'RSA-2048-OAEP-SHA1'
        ]
    ]
);
var_dump($decrypted);
// now we have our document readable
// => array(1) {
//      ["message"]=>
//      string(49) "The old grey goose jumped over the wrickety gate."
//    }
