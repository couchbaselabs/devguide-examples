#include <libcouchbase/couchbase.h>
#include <libcouchbase/ixmgmt.h>

static void ixmgmt_callback(lcb_t, int, const lcb_RESPN1XMGMT *resp)
{
    if (resp->rc == LCB_SUCCESS) {
        printf("Operation successful!\n");
    } else if (resp->rc == LCB_KEY_EEXISTS) {
        printf("Index already exists!\n");
    } else {
        printf("Operation failed: %s\n", lcb_strerror(NULL, resp->rc));
    }
}

int main(int, char **)
{
    lcb_t instance;
    lcb_create_st crst = {};
    crst.version = 3;
    crst.v.v3.connstr = "couchbase://127.0.0.1/default";
    crst.v.v3.username = "testuser";
    crst.v.v3.passwd = "password";

    lcb_create(&instance, &crst);
    lcb_connect(instance);
    lcb_wait(instance);

    if (lcb_get_bootstrap_status(instance) != LCB_SUCCESS) {
        printf("Couldn't bootstrap: %s\n", lcb_strerror(NULL, lcb_get_bootstrap_status(instance)));
        exit(EXIT_FAILURE);
    }

    const char *bktname;
    lcb_cntl(instance, LCB_CNTL_GET, LCB_CNTL_BUCKETNAME, &bktname);

    lcb_CMDN1XMGMT cmd = {};
    cmd.spec.flags = LCB_N1XSPEC_F_PRIMARY;
    cmd.spec.keyspace = bktname;
    cmd.spec.nkeyspace = strlen(bktname);
    cmd.callback = ixmgmt_callback;
    lcb_n1x_create(instance, NULL, &cmd);
    lcb_wait(instance);
    lcb_destroy(instance);
}
