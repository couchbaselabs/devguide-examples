/* -*- Mode: C; tab-width: 4; c-basic-offset: 4; indent-tabs-mode: nil -*- */
/*
 *     Copyright 2018 Couchbase, Inc.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

/**
 * This is an example of using crypto API of libcouchbase. The implementation should not be considered as production
 * ready, because it uses hardcoded keys, insecure memory allocation, copying and comparison. Consult documentation of
 * your crypto library on how to properly work with keys and buffers.
 */

#include <stdlib.h>
#include <string.h>
#include "openssl_asymmetric_provider.h"

#include <openssl/ssl.h>
#include <openssl/conf.h>
#include <openssl/evp.h>
#include <openssl/err.h>

static void oap_free(lcbcrypto_PROVIDER *provider)
{
    free(provider);
}

static void oap_release_bytes(lcbcrypto_PROVIDER *provider, void *bytes)
{
    free(bytes);
    (void)provider;
}

static const char *oap_get_key_id(lcbcrypto_PROVIDER *provider)
{
    return common_rsa_public_key_id;
}

static lcb_error_t oap_encrypt(struct lcbcrypto_PROVIDER *provider, const uint8_t *input, size_t input_len,
                               const uint8_t *iv, size_t iv_len, uint8_t **output, size_t *output_len)
{
    BIO *bio = BIO_new_mem_buf((void *)common_rsa_public_key, -1);
    RSA *rsa_pub_key = PEM_read_bio_RSA_PUBKEY(bio, NULL, NULL, NULL);
    BIO_free(bio);
    if (!rsa_pub_key) {
        fprintf(stderr, "Failed to read public key: %s\n", ERR_error_string(ERR_get_error(), NULL));
        return LCB_EINVAL;
    }
    /**
     * For simplicity this providers operates with data which is no more than RSA_size().
     * In production application, the data have to be processed in blocks
     */
    *output = malloc(RSA_size(rsa_pub_key));
    *output_len = RSA_public_encrypt(input_len, input, *output, rsa_pub_key, RSA_PKCS1_OAEP_PADDING);
    return LCB_SUCCESS;
}

static lcb_error_t oap_decrypt(struct lcbcrypto_PROVIDER *provider, const uint8_t *input, size_t input_len,
                               const uint8_t *iv, size_t iv_len, uint8_t **output, size_t *output_len)
{
    BIO *bio = BIO_new_mem_buf((void *)common_rsa_private_key, -1);
    RSA *rsa_priv_key = PEM_read_bio_RSAPrivateKey(bio, NULL, NULL, NULL);
    BIO_free(bio);
    if (!rsa_priv_key) {
        fprintf(stderr, "Failed to read private key: %s\n", ERR_error_string(ERR_get_error(), NULL));
        return LCB_EINVAL;
    }
    /**
     * For simplicity this providers operates with data which is no more than RSA_size().
     * In production application, the data have to be processed in blocks
     */
    *output = malloc(RSA_size(rsa_priv_key));
    *output_len = RSA_private_decrypt(input_len, input, *output, rsa_priv_key, RSA_PKCS1_OAEP_PADDING);
    return LCB_SUCCESS;
}

lcbcrypto_PROVIDER *oap_create()
{
    lcbcrypto_PROVIDER *provider = calloc(1, sizeof(lcbcrypto_PROVIDER));
    provider->version = 1;
    provider->destructor = oap_free;
    provider->v.v1.release_bytes = oap_release_bytes;
    provider->v.v1.encrypt = oap_encrypt;
    provider->v.v1.decrypt = oap_decrypt;
    provider->v.v1.get_key_id = oap_get_key_id;
    return provider;
}

void oap_initialize()
{
    SSL_library_init();
    SSL_load_error_strings();
    EVP_add_cipher(EVP_aes_256_cbc());
}
