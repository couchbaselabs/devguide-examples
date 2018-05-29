#include <libcouchbase/couchbase.h>
#include <libcouchbase/api3.h>
#include <string>
#include <string.h>
#include <iostream>

struct Result {
    std::string value;
    lcb_error_t status;

    Result() : status(LCB_SUCCESS) {}
};

extern "C" {
static void get_callback(lcb_t, int, const lcb_RESPBASE *rb)
{
    // "cast" to specific callback type
    const lcb_RESPGET *resp = reinterpret_cast< const lcb_RESPGET * >(rb);
    Result *my_result = reinterpret_cast< Result * >(rb->cookie);

    my_result->status = resp->rc;
    my_result->value.clear(); // Remove any prior value
    if (resp->rc == LCB_SUCCESS) {
        my_result->value.assign(reinterpret_cast< const char * >(resp->value), resp->nvalue);
    }
}
}

int main(int, char **)
{
    lcb_create_st crst = {};
    lcb_t instance;
    lcb_error_t rc;

    crst.version = 3;
    crst.v.v3.connstr = "couchbase://127.0.0.1/travel-sample";
    crst.v.v3.username = "testuser";
    crst.v.v3.passwd = "password";

    lcb_create(&instance, &crst);
    lcb_connect(instance);
    lcb_wait(instance);
    rc = lcb_get_bootstrap_status(instance);
    if (rc != LCB_SUCCESS) {
        printf("Unable to bootstrap cluster: %s\n", lcb_strerror_short(rc));
        exit(1);
    }

    // Store a key first, so we know it will exist later on. In real production
    // environments, we'd also want to install a callback for storage operations
    // so we know if they succeeded
    lcb_CMDSTORE scmd = {};
    const char *key = "a_key";
    const char *value = "{\"some\":\"json\"}";
    LCB_CMD_SET_KEY(&scmd, key, strlen(key));
    LCB_CMD_SET_VALUE(&scmd, value, strlen(value));
    scmd.operation = LCB_SET; // Upsert

    lcb_sched_enter(instance);
    lcb_store3(instance, NULL, &scmd);
    lcb_sched_leave(instance);
    lcb_wait(instance);

    // Install the callback for GET operations. Note this can be done at any
    // time before the operation is scheduled
    lcb_install_callback3(instance, LCB_CALLBACK_GET, get_callback);

    Result my_result;
    lcb_CMDGET gcmd = {};
    LCB_CMD_SET_KEY(&gcmd, key, strlen(key));
    lcb_sched_enter(instance);
    lcb_get3(instance, &my_result, &gcmd);
    lcb_sched_leave(instance);
    lcb_wait(instance);

    std::cout << "Status for getting " << key << ": ";
    std::cout << lcb_strerror(NULL, my_result.status);
    std::cout << ". Value: " << my_result.value << std::endl;

    // Let's see what happens if we get a key that isn't yet stored:
    key = "non-exist-key";
    LCB_CMD_SET_KEY(&gcmd, key, strlen(key));

    lcb_sched_enter(instance);
    lcb_get3(instance, &my_result, &gcmd);
    lcb_sched_leave(instance);
    lcb_wait(instance);
    std::cout << "Status for getting " << key << ": ";
    std::cout << lcb_strerror(NULL, my_result.status) << std::endl;

    lcb_destroy(instance);
}
