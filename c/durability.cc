#include <libcouchbase/couchbase.h>
#include <libcouchbase/api3.h>
#include <stdio.h>

// Low-level durability
static void durability_callback(lcb_t, int, const lcb_RESPBASE *rb)
{
    const lcb_RESPENDURE *resp = reinterpret_cast< const lcb_RESPENDURE * >(rb);
    if (resp->rc != LCB_SUCCESS) {
        printf("Durability failed! %s\n", lcb_strerror(NULL, rb->rc));
    }
    printf("Persisted to %d nodes. Replicated to %d nodes\n", resp->npersisted, resp->nreplicated);
}

static void store_callback(lcb_t instance, int, const lcb_RESPBASE *rb)
{
    const lcb_RESPSTORE *resp = reinterpret_cast< const lcb_RESPSTORE * >(rb);
    if (resp->rc != LCB_SUCCESS) {
        printf("Storage operation failed (%s)\n", lcb_strerror(NULL, rb->rc));
        return;
    }

    lcb_durability_opts_t options;
    memset(&options, 0, sizeof options);
    options.v.v0.cap_max = 1;
    options.v.v0.persist_to = -1;
    options.v.v0.replicate_to = -1;

    lcb_error_t rc;
    lcb_sched_enter(instance);
    lcb_MULTICMD_CTX *mctx = lcb_endure3_ctxnew(instance, &options, &rc);
    if (mctx == NULL) {
        printf("Couldn't create durability context! (%s)\n", lcb_strerror(NULL, rc));
        return;
    }

    printf("Store OK. Performing explicit lcb_endure3\n");

    lcb_CMDENDURE cmd = {};
    LCB_CMD_SET_KEY(&cmd, resp->key, resp->nkey);
    // Set the old CAS
    cmd.cas = resp->cas;
    rc = mctx->addcmd(mctx, (const lcb_CMDBASE *)&cmd);
    // Check RC
    rc = mctx->done(mctx, NULL);
    lcb_sched_leave(instance);
    // No need to call lcb_wait() here since we are already inside lcb_wait()
}

static void do_store_and_endure(lcb_t instance)
{
    lcb_CMDSTORE scmd = {};
    lcb_error_t rc;

    const char *key = "docid";
    const char *value = "[1,2,3]";

    scmd.operation = LCB_SET;
    LCB_CMD_SET_KEY(&scmd, key, strlen(key));
    LCB_CMD_SET_VALUE(&scmd, value, strlen(value));
    lcb_sched_enter(instance);
    rc = lcb_store3(instance, NULL, &scmd);
    if (rc != LCB_SUCCESS) {
        printf("Unable to schedule store operation: %s\n", lcb_strerror_short(rc));
    }
    lcb_sched_leave(instance);
    lcb_wait(instance);
}

// New-style durability
static void durstore_callback(lcb_t, int, const lcb_RESPBASE *rb)
{
    const lcb_RESPSTOREDUR *resp = reinterpret_cast< const lcb_RESPSTOREDUR * >(rb);
    if (resp->dur_resp) {
        const lcb_RESPENDURE *r2 = resp->dur_resp;
        printf("Persisted to %d nodes. Replicated to %d nodes\n", r2->npersisted, r2->nreplicated);
    }
    if (resp->rc != LCB_SUCCESS) {
        if (resp->store_ok) {
            printf("Store succeeded but durability failed:");
        } else {
            printf("Store failed:");
        }
        printf("%s\n", lcb_strerror(NULL, resp->rc));
    }
}

static void do_durstore(lcb_t instance)
{
    lcb_error_t rc;
    lcb_CMDSTOREDUR sdcmd = {};
    const char *key = "docid";
    const char *value = "[1,2,3]";
    LCB_CMD_SET_KEY(&sdcmd, key, strlen(key));
    LCB_CMD_SET_VALUE(&sdcmd, value, strlen(value));
    sdcmd.operation = LCB_SET;
    sdcmd.persist_to = -1;
    sdcmd.replicate_to = -1;

    lcb_sched_enter(instance);
    rc = lcb_storedur3(instance, NULL, &sdcmd);
    if (rc != LCB_SUCCESS) {
        printf("Unable to schedule store+durability operation: %s\n", lcb_strerror_short(rc));
    }
    lcb_sched_leave(instance);
    lcb_wait(instance);
}

int main(int, char **)
{
    lcb_create_st crst = {};
    lcb_error_t rc;
    lcb_t instance;

    crst.version = 3;
    crst.v.v3.connstr = "couchbase://127.0.0.1/default";
    crst.v.v3.username = "testuser";
    crst.v.v3.passwd = "password";

    rc = lcb_create(&instance, &crst);
    rc = lcb_connect(instance);
    lcb_wait(instance);
    rc = lcb_get_bootstrap_status(instance);
    if (rc != LCB_SUCCESS) {
        printf("Unable to bootstrap cluster: %s\n", lcb_strerror_short(rc));
        exit(1);
    }

    lcb_install_callback3(instance, LCB_CALLBACK_STOREDUR, durstore_callback);
    lcb_install_callback3(instance, LCB_CALLBACK_STORE, store_callback);
    lcb_install_callback3(instance, LCB_CALLBACK_ENDURE, durability_callback);

    // The C SDK provides a convenient way to perform storage operations and
    // ensure durability in one API call.
    printf("Performing DURSTORE!\n");
    do_durstore(instance);

    // For more control (or perhaps when doing multiple batched operations), it
    // may be more prudent to perform durability and storage as two discreet
    // operations. Note durability is not exposed as a distinct API call to
    // most SDKs:
    printf("Performing store + durability\n");
    do_store_and_endure(instance);

    lcb_destroy(instance);
    return 0;
}
