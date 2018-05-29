#include <libcouchbase/couchbase.h>
#include <libcouchbase/api3.h>
#include <vector>
#include <map>
#include <string>

struct Result {
    lcb_error_t rc;
    std::string key;
    std::string value;
    lcb_CAS cas;

    explicit Result(const lcb_RESPBASE *rb)
        : rc(rb->rc), key(reinterpret_cast< const char * >(rb->key), rb->nkey), cas(rb->cas)
    {
    }
};

typedef std::vector< Result > ResultList;

static void op_callback(lcb_t, int cbtype, const lcb_RESPBASE *rb)
{
    ResultList *results = reinterpret_cast< ResultList * >(rb->cookie);
    Result res(rb);

    if (cbtype == LCB_CALLBACK_GET && rb->rc == LCB_SUCCESS) {
        const lcb_RESPGET *rg = reinterpret_cast< const lcb_RESPGET * >(rb);
        res.value.assign(reinterpret_cast< const char * >(rg->value), rg->nvalue);
    }
    results->push_back(res);
}

int main(int argc, char **argv)
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

    lcb_install_callback3(instance, LCB_CALLBACK_STORE, op_callback);

    // Make a list of keys to store initially
    std::map< std::string, std::string > toStore;
    toStore["foo"] = "{\"value\":\"fooValue\"}";
    toStore["bar"] = "{\"value\":\"barValue\"}";
    toStore["baz"] = "{\"value\":\"bazValue\"}";

    ResultList results;

    lcb_sched_enter(instance);
    std::map< std::string, std::string >::const_iterator its = toStore.begin();
    for (; its != toStore.end(); ++its) {
        lcb_CMDSTORE scmd = {};
        LCB_CMD_SET_KEY(&scmd, its->first.c_str(), its->first.size());
        LCB_CMD_SET_VALUE(&scmd, its->second.c_str(), its->second.size());
        scmd.operation = LCB_SET;
        rc = lcb_store3(instance, &results, &scmd);
        if (rc != LCB_SUCCESS) {
            fprintf(stderr, "Couldn't schedule item %s: %s\n", its->first.c_str(), lcb_strerror(NULL, rc));

            // Unschedules all operations since the last scheduling context
            // (created by lcb_sched_enter)
            lcb_sched_fail(instance);
            break;
        }
    }
    lcb_sched_leave(instance);
    lcb_wait(instance);

    ResultList::iterator itr;
    for (itr = results.begin(); itr != results.end(); ++itr) {
        printf("%s: ", itr->key.c_str());
        if (itr->rc != LCB_SUCCESS) {
            printf("Failed (%s)\n", lcb_strerror(NULL, itr->rc));
        } else {
            printf("Stored. CAS=%llu\n", (unsigned long long)itr->cas);
        }
    }

    lcb_destroy(instance);
    return 0;
}
