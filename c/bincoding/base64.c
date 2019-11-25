#include <string.h>

#include <openssl/bio.h>
#include <openssl/evp.h>

char *base64(const char *in, int in_len, int *out_len)
{
    BIO *buf, *b64;
    char *ptr, *out;
    long len;

    buf = BIO_new(BIO_s_mem());
    BIO_set_close(buf, BIO_CLOSE);

    b64 = BIO_new(BIO_f_base64());
    BIO_set_flags(b64, BIO_FLAGS_BASE64_NO_NL);
    BIO_push(b64, buf);

    BIO_write(b64, in, in_len);
    BIO_flush(b64);

    len = BIO_get_mem_data(buf, &ptr);
    out = malloc((len + 1) * sizeof(char));
    memcpy(out, ptr, len);
    out[len] = '\0';
    *out_len = len + 1;

    BIO_free_all(b64);
    return out;
}

char *unbase64(const char *in, int in_len, int *out_len)
{
    BIO *buf, *b64;
    char *out;
    long len;

    buf = BIO_new_mem_buf(in, in_len);
    BIO_set_close(buf, BIO_CLOSE);

    b64 = BIO_new(BIO_f_base64());
    BIO_set_flags(b64, BIO_FLAGS_BASE64_NO_NL);
    BIO_push(b64, buf);

    out = malloc((in_len + 1) * sizeof(char));
    len = BIO_read(b64, out, in_len);
    out[len] = '\0';
    *out_len = len;

    BIO_free_all(b64);
    return out;
}
