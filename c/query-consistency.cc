#include <libcouchbase/couchbase.h>
#include <libcouchbase/api3.h>
#include <libcouchbase/n1ql.h>
#include <string>
#include <vector>

static int RandomNumber_g;
typedef std::vector< std::string > RowList;

static void query_callback(lcb_t, int, const lcb_RESPN1QL *resp)
{
    if (resp->rc != LCB_SUCCESS) {
        fprintf(stderr, "N1QL query failed (%s)\n", lcb_strerror(NULL, resp->rc));
    }

    if (resp->rflags & LCB_RESP_F_FINAL) {
        // We're simply notified here that the last row has already been returned.
        // no processing needed here.
        return;
    }

    // Add rows to the vector, we'll process the results in the calling
    // code.
    RowList *rowlist = reinterpret_cast< RowList * >(resp->cookie);
    rowlist->push_back(std::string(resp->row, resp->nrow));
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

    // Initialize random seed to get a "random" value in our documents
    srand(time(NULL));
    RandomNumber_g = rand() % 10000;

    char key[256];
    sprintf(key, "user:%d", RandomNumber_g);
    char value[4096];
    sprintf(value,
            "{"
            "  \"name\":[\"Brass\",\"Doorknob\"],"
            "  \"email\":\"brass.doorknob@juno.com\","
            "  \"random\":%d"
            "}",
            RandomNumber_g);

    printf("Will insert new document with random number %d\n", RandomNumber_g);

    lcb_CMDSTORE scmd = {};
    LCB_CMD_SET_KEY(&scmd, key, strlen(key));
    LCB_CMD_SET_VALUE(&scmd, value, strlen(value));
    scmd.operation = LCB_SET;

    lcb_sched_enter(instance);
    rc = lcb_store3(instance, NULL, &scmd);
    lcb_sched_leave(instance);
    lcb_wait(instance);
    // In real code, we'd also install a store callback so we can know if the
    // actual storage operation was a success.

    lcb_N1QLPARAMS *params = lcb_n1p_new();

    lcb_CMDN1QL cmd = {};
    RowList rows;
    cmd.callback = query_callback;

    rc = lcb_n1p_setstmtz(params, "SELECT name, email, random FROM default WHERE $1 IN name");
    // -1 for length indicates nul-terminated string
    rc = lcb_n1p_posparam(params, "\"Brass\"", -1);

    // This guarantess that the query will include the newly-inserted document.
    rc = lcb_n1p_setconsistency(params, LCB_N1P_CONSISTENCY_REQUEST);
    rc = lcb_n1p_mkcmd(params, &cmd);
    rc = lcb_n1ql_query(instance, &rows, &cmd);
    lcb_wait(instance);

    // To demonstrate the CONSISTENCY_REQUEST feature, we check each row for the
    // "random" value. Because the C standard library does not come with a JSON
    // parser, we are limited in how we can inspect the row (which is JSON).
    // For clarity, we print out only the row's "Random" field. When the
    // CONSISTENCY_REQUEST feature is enabled, one of the results should contain
    // the newest random number (the value of RandomNumber_g). When disabled, the
    // row may or may not appear.
    for (RowList::iterator ii = rows.begin(); ii != rows.end(); ++ii) {
        std::string &row = *ii;
        size_t begin_pos = row.find("\"random\"");
        size_t end_pos = row.find_first_of("},", begin_pos);
        std::string row_number = row.substr(begin_pos, end_pos - begin_pos);
        printf("Row has random number %s\n", row_number.c_str());
    }

    lcb_n1p_free(params);
    lcb_destroy(instance);
}
