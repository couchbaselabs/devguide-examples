#include <string.h>
#include <libcouchbase/couchbase.h>  //this code sample is for libcouchbase 2.10 & later
#include <stdlib.h>

static void
opCallback(lcb_t instance, int cbtype, const lcb_RESPBASE *rb) {
    fprintf(stderr, "%.*s: %s... ", (int)rb->nkey, rb->key, lcb_strcbtype(cbtype));
    if (rb->rc != LCB_SUCCESS) {
        fprintf(stderr, "%s\n", lcb_strerror(NULL, rb->rc));
    } else {
        fprintf(stderr, "OK");
        if (cbtype == LCB_CALLBACK_GET) {
            const lcb_RESPGET *rg = (const lcb_RESPGET *)rb;
            fprintf(stderr, "... Value: %.*s\n", (int)rg->nvalue, rg->value);
        } else {
            fprintf(stderr, "\n");
        }
    }
}

int main(int argc, char **argv) 
{
    lcb_t instance = NULL;
    struct lcb_create_st crst = {0};
    memset(&crst, 0, sizeof crst);
    // Note that version 3 here refers to the internal API/ABI, not the version of the library supporting
    // that API/ABI.  This allows extension within a libcouchbase version with forward compatibility
    crst.version = 3;

    /* User input starts here; see note on v3 above */
    crst.v.v3.connstr = "couchbases://cb.<your endpoint address>.dp.cloud.couchbase.com/couchbasecloudbucket?ssl=no_verify";
    crst.v.v3.username = "user";
    crst.v.v3.passwd = "password";
    /* User input ends here */

    lcb_create(&instance, &crst);
    lcb_connect(instance);


    /* This function is required to actually schedule the operations on the network */
    lcb_wait(instance);

    /* Determines if the bootstrap/connection succeeded */
    lcb_error_t rc;
    rc = lcb_get_bootstrap_status(instance);
    if (rc != LCB_SUCCESS) {
        fprintf(stderr, "%s failed. (0x%x, %s)\n", "bootstrap failure", rc, lcb_strerror(NULL, rc));
        exit(1);
    } else {
        printf("Connection succeeded. Cluster has %d nodes\n", lcb_get_num_nodes(instance));
    }

    lcb_install_callback3(instance, LCB_CALLBACK_GET, opCallback);
    lcb_install_callback3(instance, LCB_CALLBACK_STORE, opCallback);

    lcb_CMDSTORE scmd = { 0 };
    LCB_CMD_SET_KEY(&scmd, "key", 3);
    LCB_CMD_SET_VALUE(&scmd, "true", 4);
    scmd.operation = LCB_SET;
    lcb_store3(instance, NULL, &scmd);
    lcb_wait(instance);

    lcb_CMDGET gcmd = { 0 };
    LCB_CMD_SET_KEY(&gcmd, "key", 3);
    lcb_get3(instance, NULL, &gcmd);
    lcb_wait(instance);
    lcb_destroy(instance);

    return 0;
}
