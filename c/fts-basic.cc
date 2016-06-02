#include <libcouchbase/couchbase.h>
#include <libcouchbase/cbft.h>
#include <cassert>
#include <cstdio>
#include <cstdlib>
#include <string>

static void rowCallback(lcb_t, int, const lcb_RESPFTS *resp)
{
    if (resp->rflags & LCB_RESP_F_FINAL) {
        printf("Status: %d\n", resp->rc);
        printf("Meta: %.*s\n", (int)resp->nrow, resp->row);
        if (resp->htresp) {
            printf("HTTP Response: %.*s\n", (int)resp->htresp->nbody, resp->htresp->body);
        }
    } else {
        printf("Row: %.*s\n", (int)resp->nrow, resp->row);
    }
}

int main(int, char **)
{
    lcb_t instance;
    lcb_error_t rc = lcb_create(&instance, NULL);
    assert(rc == LCB_SUCCESS);
    lcb_cntl_string(instance, "detailed_errcodes", "true");
    lcb_connect(instance);
    lcb_wait(instance);
    assert(lcb_get_bootstrap_status(instance) == LCB_SUCCESS);

    // Be sure to include the indexName within the request payload
    std::string encodedQuery(
        "{\"query\":{\"match\":\"hoppy\"},\"indexName\":\"beer-search\",\"size\":10}");
    lcb_CMDFTS cmd = { 0 };
    cmd.callback = rowCallback;
    cmd.query = encodedQuery.c_str();
    cmd.nquery = encodedQuery.size();
    rc = lcb_fts_query(instance, NULL, &cmd);
    assert(rc == LCB_SUCCESS);

    lcb_wait(instance);
    lcb_destroy(instance);
}
