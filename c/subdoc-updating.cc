// This file is a slight variation from subdoc-retrieving.cc

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

    Result() : status(LCB_MAX_ERROR) {}
    bool valid() const
    {
        return status != LCB_MAX_ERROR;
    }
    void reset()
    {
        status = LCB_MAX_ERROR;
        value.clear();
    }
};

struct SubdocResults {
    lcb_error_t status;
    std::vector< Result > results;
    void reset(size_t max_results)
    {
        results.resize(max_results);
        for (size_t ii = 0; ii < max_results; ii++) {
            results[ii].reset();
        }
    }
};

extern "C" {
static void sdmutate_callback(lcb_t, int, const lcb_RESPSUBDOC *resp)
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
        // Not all results are returned. Those that are returned
        // can be correlated with the request path index by using
        // lcb_SDENTRY::index

        Result &r = results->results[ent.index];
        r.status = ent.status;
        if (ent.nvalue) {
            r.value.assign(reinterpret_cast< const char * >(ent.value), ent.nvalue);
        }
    }
}

static void fulldoc_get_callback(lcb_t, int, const lcb_RESPGET *resp)
{
    assert(resp->rc == LCB_SUCCESS);
    printf("Document is now: %.*s\n", (int)resp->nvalue, (char *)resp->value);
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
    const char *value = "{\"name\":\"mark\", \"array\":[1,2,3,4], \"email\":\"m@n.com\"}";
    LCB_CMD_SET_KEY(&scmd, key, strlen(key));
    LCB_CMD_SET_VALUE(&scmd, value, strlen(value));
    scmd.operation = LCB_SET; // Upsert

    lcb_store3(instance, NULL, &scmd);
    lcb_wait(instance);

    // Install the callback for GET operations. Note this can be done at any
    // time before the operation is scheduled
    lcb_install_callback3(instance, LCB_CALLBACK_SDMUTATE, reinterpret_cast< lcb_RESPCALLBACK >(sdmutate_callback));

    SubdocResults my_results;
    lcb_SDSPEC specs[3] = {};
    lcb_CMDSUBDOC sdcmd = {};

    LCB_CMD_SET_KEY(&sdcmd, key, strlen(key));

    specs[0].sdcmd = LCB_SDCMD_ARRAY_ADD_LAST;
    LCB_SDSPEC_SET_PATH(&specs[0], "array", strlen("array"));
    LCB_SDSPEC_SET_VALUE(&specs[0], "42", 2);

    specs[1].sdcmd = LCB_SDCMD_COUNTER;
    LCB_SDSPEC_SET_PATH(&specs[1], "array[0]", strlen("array[0]"));
    LCB_SDSPEC_SET_VALUE(&specs[1], "99", 2);

    specs[2].sdcmd = LCB_SDCMD_DICT_UPSERT;
    LCB_SDSPEC_SET_PATH(&specs[2], "description", strlen("description"));
    LCB_SDSPEC_SET_VALUE(&specs[2], "\"just a dev\"", strlen("\"just a dev\""));
    sdcmd.specs = specs;
    sdcmd.nspecs = 3;
    my_results.reset(3);

    lcb_subdoc3(instance, &my_results, &sdcmd);
    lcb_wait(instance);

    // Should have three results
    assert(my_results.status == LCB_SUCCESS);
    for (size_t ii = 0; ii < my_results.results.size(); ++ii) {
        const Result &r = my_results.results[ii];
        if (!r.valid()) {
            printf("[%lu]: No output\n", ii);
        } else {
            printf("[%lu]: %s\n", ii, r.value.c_str());
        }
    }

    lcb_install_callback3(instance, LCB_CALLBACK_GET, (lcb_RESPCALLBACK)fulldoc_get_callback);

    printf("=== Current doc ===\n");
    lcb_CMDGET gcmd = {};
    LCB_CMD_SET_KEY(&gcmd, key, strlen(key));
    lcb_get3(instance, NULL, &gcmd);
    lcb_wait(instance);

    // Show how to set command options!
    memset(&specs[0], 0, sizeof specs[0]);
    const char *deep_path = "some.deep.path";
    LCB_SDSPEC_SET_PATH(&specs[0], deep_path, strlen(deep_path));
    LCB_SDSPEC_SET_VALUE(&specs[0], "true", 4);
    specs[0].sdcmd = LCB_SDCMD_DICT_UPSERT;
    sdcmd.nspecs = 1;
    my_results.reset(1);
    lcb_subdoc3(instance, &my_results, &sdcmd);
    lcb_wait(instance);

    // Should fail with LCB_SUBDOC_PATH_ENOENT
    printf("upserting deep path fails: %s\n", lcb_strerror_short(my_results.results[0].status));

    // Use a flag
    specs[0].options = LCB_SDSPEC_F_MKINTERMEDIATES;
    lcb_subdoc3(instance, &my_results, &sdcmd);
    lcb_wait(instance);
    printf("Status with MKINTERMEDIATES: %s\n", lcb_strerror_short(my_results.status));

    lcb_destroy(instance);
}
