#include <libcouchbase/couchbase.h>
#include <libcouchbase/api3.h>
#include <stdlib.h>
#include <string.h>

typedef struct {
    lcb_error_t rc;
    lcb_cas_t cas;
} my_OPINFO;

static void update_callback(lcb_t instance, int cbtype, const lcb_RESPBASE *resp)
{
    my_OPINFO *info = resp->cookie;
    info->rc = resp->rc;
    info->cas = resp->cas;
}

int main(int argc, char **argv)
{
    lcb_t instance;
    struct lcb_create_st crst = {};
    lcb_error_t rc;
    lcb_CMDSTORE cmd = {0};
    const char *key, *value;
    my_OPINFO info;

    crst.version = 3;
    crst.v.v3.connstr = "couchbase://127.0.0.1/default";
    crst.v.v3.username = "testuser";
    crst.v.v3.passwd = "password";

    /* See connecting.c for error checking */
    lcb_create(&instance, &crst);
    lcb_connect(instance);
    lcb_wait(instance);
    rc = lcb_get_bootstrap_status(instance);
    if (rc != LCB_SUCCESS) {
        printf("Unable to bootstrap cluster: %s\n", lcb_strerror_short(rc));
        exit(1);
    }

    /* Set global storage callback */
    lcb_install_callback3(instance, LCB_CALLBACK_STORE, update_callback);

    key = "docid";
    value = "{\"property\":\"value\"}";

    LCB_CMD_SET_KEY(&cmd, key, strlen(key));
    LCB_CMD_SET_VALUE(&cmd, value, strlen(value));

    lcb_sched_enter(instance);
    /* Schedule unconditional upsert (LCB_SET). Should always succeed */
    cmd.operation = LCB_SET;
    rc = lcb_store3(instance, &info, &cmd);
    if (rc != LCB_SUCCESS) {
        printf("Couldn't schedule store operation: %s\n", lcb_strerror_short(rc));
        exit(EXIT_FAILURE);
    }

    lcb_sched_leave(instance);
    lcb_wait(instance);

    printf("Upsert for %s got code %s (0 is success)\n", key, lcb_strerror_short(info.rc));

    /* Preserve the operation structure, just changing the operation type */
    lcb_sched_enter(instance);
    /* Do an insert (LCB_ADD). This will fail since the item already exists */
    cmd.operation = LCB_ADD;
    lcb_store3(instance, &info, &cmd);
    lcb_sched_leave(instance);
    lcb_wait(instance);

    printf("Insert for %s got code %s (0 is success). Failure expected\n", key, lcb_strerror_short(info.rc));

    /* Do a replace (LCB_REPLACE) */
    cmd.operation = LCB_REPLACE;
    lcb_sched_enter(instance);
    lcb_store3(instance, &info, &cmd);
    lcb_sched_leave(instance);
    lcb_wait(instance);

    printf("Replace for %s got code %s\n", key, lcb_strerror_short(info.rc));

    lcb_destroy(instance);
    return 0;
}
