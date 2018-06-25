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

#include "common_provider.h"

/**
 *
 *
 * ¡DO NOT REUSE THESE KEYS. THEY HERE TO SIMPLIFY EXAMPLES!
 *
 * ¡REAL APPLICATION SHOULD IMPLEMENT SECURE KEY PROVIDERS!
 *
 */

char *common_aes256_key_id = "mykeyid";

uint8_t *common_hmac_sha256_key = "myauthpassword";

uint8_t common_aes256_key[AES256_KEY_SIZE] = "!mysecretkey#9^5usdk39d&dlf)03sL";
uint8_t common_aes256_iv[AES256_IV_SIZE] = {0x65, 0xe7, 0x66, 0xbe, 0x35, 0xb2, 0xd2, 0x52,
                                            0x2b, 0x2e, 0x7e, 0x8e, 0x99, 0x9,  0x8d, 0xa9};

char *common_rsa_private_key_id = "MyPrivateKeyName";
char *common_rsa_public_key_id = "MyPublicKeyName";

char *common_rsa_private_key = "-----BEGIN RSA PRIVATE KEY-----\n"
                               "MIIEpAIBAAKCAQEAwP6s/siq+geZAcN858as1U6VIFeNDjvepl88jyd748idDt1a\n"
                               "hDqw7pGw5WMygq04anWQG3kKUUhElxwG9BJ/z4rxJXO0Vbflv0whgBlTVVxXuXSP\n"
                               "wtyA200CENLO6aTaVN/aettSvA3cEuTit6eg4Ayi0iSO97SI/9Jp4XeI4bA5Ls55\n"
                               "1Y9XR+PVbnaNgDWxGvebpw9GvjeK/hUdMHwP8QhLdyLLjbQ6i3YxOWFYWqjtSQav\n"
                               "CdkpHNui7U1rULxYYFSAhR64dOwoTs2yB8lLMQsjTdIQR6oQZgaKRlVzPzHlJgp0\n"
                               "tISJxvJYXrct7ZEjEFtTLnOMx4E7MbmcN3bsDwIDAQABAoIBAGiiq5CHo4tjyyUV\n"
                               "pAbVxKbxsBCU5zksZI63W9IRii35eo2wnX7Lg1oVS19S5PPMjqXJj5QVj+55zBZR\n"
                               "b8Oss/cGUbAIh2FiDwIkeJVHJdNF+ZnnBHqVqpc7rT8JzH0IkAcsRvwNJVIoAYWM\n"
                               "6w6/p41RzIU6pPjPvOdWYWmIsYIKZAhVnTf8QXDBpBdjzrrlTnocChNtEdkqyCFm\n"
                               "FILOWUiFbzWsHJe5/1o+v+Kw4qQGHNZVpFi2vQCJxTLdEbcUHCmVqgQOs+1hs+Ax\n"
                               "37pkXfVBRh97E5RV0Os8JtH3smw9uCcQveJanmuPVhsa+8zjOK2j1AHjdsaPZgMP\n"
                               "wuleVoECgYEA5mJ72lPRcFjNTTDQfLUHxCq4rWekkS+QsgPyBuE8z5mi7SsHuScV\n"
                               "i+PcLehRY8e6Z464Kl9Ni6c17HcM+Bm8ay70hxPeTlqrVBjxKiTixF1BccbBHPjd\n"
                               "Jl0WCEODxKMp5TAasJsfM7Pg18cYNakmOqK/agc7LJtsyo99jKfFYR8CgYEA1nPz\n"
                               "mGfhZZ2JXsNBNlqyvitV7Uzwa62DMGJUuosODnaz5v4gTPZhMF4gaOwh3wofP882\n"
                               "JZM1YEDF64Nn3tMDXImidoE9tKDMPyT1+obaBEPe8AfhAJGfMrWHgU3Yicd16bxK\n"
                               "vbU3kODpFgBtnE50JcceEyFYTWzZeNRWlsW4ZxECgYEAqTxDGthji5HQDhoDrPgW\n"
                               "omV3j/oIi5ZTRlFbou4mC6IiavInFD2/uClD/n0f/JolNhlC8+1aO3IzTGcPodjV\n"
                               "7i5p9igEL66vGHHSBlFeOzz97CRCi5PMcHgEzUE7NGFfTzqNAJqSyxoh2qAoCpMc\n"
                               "wAn5blutflEWE55gbch4V6UCgYEArFiBU2FgzmZd6O9ocENR1O1E4DHuIctPXEoa\n"
                               "J9TrFgqlqCVhVhjHoLR0vX3P9szOslxX+riks9c6eHyhtHzG/c6K50wUiB6WJsUQ\n"
                               "fidz/OuCtkrOs8NUOs+SuAMU3B2VkKPHOVDy+BcYm5r6fBy80UOF0wAAVDD/UVDs\n"
                               "ybza5tECgYB+ksZiUbZ+8WTXVIB3HJJT8U8bZ8676lrRUJ5YxB+avHh4g/TI+e53\n"
                               "jZKBVvB3Mhp6QFMZITuUTRgiGuAjBap4SZ32Pmyu3TxiWDxKktmvFMPLUVFntDJ0\n"
                               "th2u9Xpw8+T01AOCFc0PKtC8g0Covxu+qWLfqnJnTCx+Q03+dQj9rQ==\n"
                               "-----END RSA PRIVATE KEY-----\n";

char *common_rsa_public_key = "-----BEGIN PUBLIC KEY-----\n"
                              "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAwP6s/siq+geZAcN858as\n"
                              "1U6VIFeNDjvepl88jyd748idDt1ahDqw7pGw5WMygq04anWQG3kKUUhElxwG9BJ/\n"
                              "z4rxJXO0Vbflv0whgBlTVVxXuXSPwtyA200CENLO6aTaVN/aettSvA3cEuTit6eg\n"
                              "4Ayi0iSO97SI/9Jp4XeI4bA5Ls551Y9XR+PVbnaNgDWxGvebpw9GvjeK/hUdMHwP\n"
                              "8QhLdyLLjbQ6i3YxOWFYWqjtSQavCdkpHNui7U1rULxYYFSAhR64dOwoTs2yB8lL\n"
                              "MQsjTdIQR6oQZgaKRlVzPzHlJgp0tISJxvJYXrct7ZEjEFtTLnOMx4E7MbmcN3bs\n"
                              "DwIDAQAB\n"
                              "-----END PUBLIC KEY-----\n";
