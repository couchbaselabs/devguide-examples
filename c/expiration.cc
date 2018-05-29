#include <libcouchbase/couchbase.h>
#include <libcouchbase/api3.h>

#ifdef _WIN32
#include <windows.h>
#define sleep(s) Sleep(s / 1000)
#else
#include <unistd.h>
#endif

static void op_callback(lcb_t, int cbtype, const lcb_RESPBASE *resp)
{
    if (resp->rc == LCB_SUCCESS) {
        printf("Operation (type=%d) OK\n", cbtype);
    } else {
        printf("Operation (type=%d) Failed (%s)\n", cbtype, lcb_strerror(NULL, resp->rc));
    }

    if (cbtype == LCB_CALLBACK_GET) {
        const lcb_RESPGET *rg = reinterpret_cast< const lcb_RESPGET * >(resp);
        if (resp->rc == LCB_SUCCESS) {
            printf("Got value %.*s\n", (int)rg->nvalue, (char *)rg->value);
        }
    }
}

static void store_key(lcb_t instance, const char *key, const char *value, unsigned exp = 0)
{
    lcb_CMDSTORE scmd = {0};
    LCB_CMD_SET_KEY(&scmd, key, strlen(key));
    LCB_CMD_SET_VALUE(&scmd, value, strlen(value));
    scmd.exptime = exp; // Only live for 2 seconds!
    scmd.operation = LCB_SET;

    lcb_sched_enter(instance);
    lcb_store3(instance, NULL, &scmd);
    lcb_sched_leave(instance);
    lcb_wait(instance);
}

static void get_or_touch(lcb_t instance, const char *key, unsigned exp = 0, bool get = false)
{
    union {
        lcb_CMDBASE base;
        lcb_CMDGET get;
        lcb_CMDTOUCH touch;
    } u;

    memset(&u, 0, sizeof u);

    LCB_CMD_SET_KEY(&u.base, key, strlen(key));
    u.base.exptime = exp;

    lcb_sched_enter(instance);

    if (get) {
        lcb_get3(instance, NULL, &u.get);
    } else {
        lcb_touch3(instance, NULL, &u.touch);
    }

    lcb_sched_leave(instance);
    lcb_wait(instance);
}

int main(int, char **)
{
    lcb_t instance;
    lcb_create_st crst = {};
    lcb_error_t rc;

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

    // You can actually use the same callback for multiple types of operations.
    // The second parameter is the type of callback being invoked. You can
    // then cast the return type to the more specific struct.
    lcb_install_callback3(instance, LCB_CALLBACK_DEFAULT, op_callback);

    const char *key = "docid";
    const char *value = "{\"some\":\"value\"}";

    // First store with an expiry of 1 second
    printf("Storing with an expiration of 2\n");
    store_key(instance, key, value, 2);
    printf("Getting key immediately after store..\n");
    get_or_touch(instance, key, 0, true);

    printf("Sleeping for 4 seconds..\n");
    sleep(4);
    printf("Getting key again (should fail)\n");
    get_or_touch(instance, key);

    printf("Storing key again (without expiry)\n");
    store_key(instance, key, value);
    printf("Using get-and-touch to retrieve key and modify expiry\n");
    get_or_touch(instance, key, 1, true);

    printf("Sleeping for another 4 seconds\n");
    sleep(4);
    printf("Getting key again (should fail)\n");
    get_or_touch(instance, key, 0, true);

    printf("Storing key again (without expiry)\n");
    store_key(instance, key, value);

    printf("Touching key (without get). Setting expiry for 1 second\n");
    get_or_touch(instance, key, 1, false);

    printf("Sleeping for 4 seconds\n");
    sleep(4);

    printf("Getting again... (should fail)\n");
    get_or_touch(instance, key, 0, true);

    lcb_destroy(instance);
}
