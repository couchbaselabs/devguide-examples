#include <libcouchbase/couchbase.h>
#include <libcouchbase/n1ql.h>

extern "C" {
static void query_callback(lcb_t, int, const lcb_RESPN1QL *resp)
{
    // This might also fail if the index is already created!
    if (resp->rc != LCB_SUCCESS) {
        fprintf(stderr, "N1QL query failed (%s)\n", lcb_strerror(NULL, resp->rc));
    }
    printf("Result text: %.*s\n", (int)resp->nrow, resp->row);
}
}

int main(int, char **)
{
    lcb_t instance;
    struct lcb_create_st crst = {};
    lcb_error_t rc;
    lcb_CMDN1QL cmd = {};
    lcb_N1QLPARAMS *params;

    crst.version = 3;
    crst.v.v3.connstr = "couchbase://127.0.0.1/travel-sample";
    crst.v.v3.username = "testuser";
    crst.v.v3.passwd = "password";
    rc = lcb_create(&instance, &crst); // Check rc
    rc = lcb_connect(instance);        // Check rc
    lcb_wait(instance);
    rc = lcb_get_bootstrap_status(instance);
    if (rc != LCB_SUCCESS) {
        printf("Unable to bootstrap cluster: %s\n", lcb_strerror_short(rc));
        exit(1);
    }

    params = lcb_n1p_new();
    rc = lcb_n1p_setstmtz(params, "CREATE PRIMARY INDEX ON `travel-sample`");

    cmd.callback = query_callback;
    lcb_n1p_mkcmd(params, &cmd);
    rc = lcb_n1ql_query(instance, NULL, &cmd); // Check RC
    lcb_wait(instance);

    lcb_n1p_free(params);
    lcb_destroy(instance);
}
