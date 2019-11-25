#ifndef BASE64_H
#define BASE64_H

char *base64(const char *in, int in_len, int *out_len);
char *unbase64(const char *in, int in_len, int *out_len);

#endif
