#include <libcouchbase/couchbase.h>
#include <libcouchbase/api3.h>
#include <stdio.h>

static void counter_callback(lcb_t, int, const lcb_RESPBASE *rb)
{
    const lcb_RESPCOUNTER *resp = reinterpret_cast< const lcb_RESPCOUNTER * >(rb);
    if (resp->rc != LCB_SUCCESS) {
        fprintf(stderr, "Couldn't perform counter operation!\n");
        fprintf(stderr, "Error code 0x%x (%s)\n", resp->rc, lcb_strerror(NULL, resp->rc));
        return;
    }

    printf("Current counter value is %llu\n", (unsigned long long)resp->value);
}

// Removes the counter. This is optional, but is helpful for demonstrative
// purposes so that we always start off with a fresh counter
static void remove_counter(lcb_t instance, const char *docid)
{
    lcb_CMDREMOVE cmd = {};
    LCB_CMD_SET_KEY(&cmd, docid, strlen(docid));
    lcb_sched_enter(instance);
    lcb_remove3(instance, NULL, &cmd);
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

    lcb_install_callback3(instance, LCB_CALLBACK_COUNTER, counter_callback);

    lcb_sched_enter(instance);

    const char *docid = "docid";
    remove_counter(instance, docid);

    lcb_CMDCOUNTER cmd = {};
    LCB_CMD_SET_KEY(&cmd, docid, strlen(docid));
    cmd.initial = 100;
    cmd.delta = 20;
    cmd.create = 1;

    rc = lcb_counter3(instance, NULL, &cmd);
    lcb_sched_leave(instance);
    lcb_wait(instance);

    lcb_sched_enter(instance);
    cmd.delta = 1;
    rc = lcb_counter3(instance, NULL, &cmd);
    lcb_sched_leave(instance);
    lcb_wait(instance);

    lcb_sched_enter(instance);
    cmd.delta = -50;
    rc = lcb_counter3(instance, NULL, &cmd);
    lcb_sched_leave(instance);
    lcb_wait(instance);

    lcb_destroy(instance);
}
