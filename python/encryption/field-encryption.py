from cbencryption import AES256CryptoProvider
from couchbase.bucket import Bucket
from couchbase.crypto import InMemoryKeyStore
# create insecure key store and register both public and private keys
keystore = InMemoryKeyStore()
keystore.set_key('mypublickey', b'!mysecretkey#9^5usdk39d&dlf)03sL')
keystore.set_key('myprivatekey', b'myauthpassword')

# create and register provider
provider = AES256CryptoProvider.AES256CryptoProvider(keystore, 'mypublickey', 'myprivatekey')
bucket = Bucket("couchbase://10.143.180.101:8091/default",password='password')
bucket.register_crypto_provider('AES-256-HMAC-SHA256', provider)

# encrypt document, the alg name must match the provider name and the kid must match a key in the keystore
prefix = '__crypt_'
document = {'message': 'The old grey goose jumped over the wrickety gate.'}
fieldspec = [{'alg': 'AES-256-HMAC-SHA256', 'name': 'message'}]
encrypted_document = bucket.encrypt_fields(document,
                                           fieldspec,
                                           prefix)
expected = {
    "__crypt_message": {"alg": "AES-256-HMAC-SHA256",
                        "kid": "mypublickey",
                        "ciphertext": "sR6AFEIGWS5Fy9QObNOhbCgfg3vXH4NHVRK1qkhKLQqjkByg2n69lot89qFEJuBsVNTXR77PZR6RjN4h4M9evg=="
                        }
}

# retain only signature/iv-independent fields for comparison


def filter_encrypted(encrypted_dict):
    return {k:v for k,v in encrypted_dict.items() if k in {"alg","kid","ciphertext"}}

subset_expected = filter_encrypted(expected)
subset_actual = filter_encrypted(encrypted_document)
assert subset_expected == subset_actual
# decrypt document using registered provider
decrypted_document = bucket.decrypt_fields(encrypted_document, fieldspec, prefix)
assert decrypted_document==document