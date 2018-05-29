#include <libcouchbase/couchbase.h>
#if LCB_VERSION < 0x020508
#error "Example requires libcouchbase 2.5.8 or greater!"
#endif
#include <libcouchbase/subdoc.h>
#include <string>
#include <vector>

struct Result {
    std::string value;
    lcb_error_t status;

    Result() : status(LCB_SUCCESS) {}
};

struct SubdocResults {
    lcb_error_t status;
    std::vector< Result > results;
};

extern "C" {
static void sdget_callback(lcb_t, int, const lcb_RESPSUBDOC *resp)
{
    // "cast" to specific callback type
    SubdocResults *results = reinterpret_cast< SubdocResults * >(resp->cookie);
    results->status = resp->rc;

    if (resp->rc != LCB_SUCCESS && resp->rc != LCB_SUBDOC_MULTI_FAILURE) {
        // If the error code is neither SUCCESS nor SUBDOC_MULTI_FAILURE then
        // it means that there are no results and an error occurred during
        // document access.
        return;
    }

    lcb_SDENTRY ent = {};
    size_t ii = 0;
    while (lcb_sdresult_next(resp, &ent, &ii)) {
        Result r;
        r.status = ent.status;
        if (ent.nvalue) {
            r.value.assign(reinterpret_cast< const char * >(ent.value), ent.nvalue);
        }
        results->results.push_back(r);
    }
}
}

int main(int, char **)
{
    lcb_create_st crst = {};
    lcb_t instance;
    lcb_error_t rc;

    crst.version = 3;
    crst.v.v3.connstr = "couchbase://127.0.0.1/default";
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
    const char *value = "{\"name\":\"mark\", \"array\":[1,2,3], \"email\":\"m@n.com\"}";
    LCB_CMD_SET_KEY(&scmd, key, strlen(key));
    LCB_CMD_SET_VALUE(&scmd, value, strlen(value));
    scmd.operation = LCB_SET; // Upsert

    lcb_store3(instance, NULL, &scmd);
    lcb_wait(instance);

    // Install the callback for GET operations. Note this can be done at any
    // time before the operation is scheduled
    lcb_install_callback3(instance, LCB_CALLBACK_SDLOOKUP, reinterpret_cast< lcb_RESPCALLBACK >(sdget_callback));

    SubdocResults my_results;
    lcb_SDSPEC specs[3] = {};
    lcb_CMDSUBDOC sdcmd = {};

    LCB_CMD_SET_KEY(&sdcmd, key, strlen(key));

    specs[0].sdcmd = LCB_SDCMD_GET;
    LCB_SDSPEC_SET_PATH(&specs[0], "email", 5);

    specs[1].sdcmd = LCB_SDCMD_GET;
    LCB_SDSPEC_SET_PATH(&specs[1], "array[1]", strlen("array[1]"));

    specs[2].sdcmd = LCB_SDCMD_EXISTS;
    LCB_SDSPEC_SET_PATH(&specs[2], "non-exist", strlen("non-exist"));
    sdcmd.specs = specs;
    sdcmd.nspecs = 3;

    lcb_subdoc3(instance, &my_results, &sdcmd);
    lcb_wait(instance);

    // Should have three results
    assert(my_results.status == LCB_SUBDOC_MULTI_FAILURE);
    for (size_t ii = 0; ii < my_results.results.size(); ++ii) {
        const Result &r = my_results.results[ii];
        printf("Path [%lu]: Status=%s. Value=%s\n", ii, lcb_strerror_short(r.status), r.value.c_str());
    }

    lcb_destroy(instance);
}
