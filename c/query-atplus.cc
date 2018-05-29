#include <libcouchbase/couchbase.h>
#include <libcouchbase/n1ql.h>
#include <string>
#include <vector>
#include <sstream>

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

static void storage_callback(lcb_t, int cbtype, const lcb_RESPBASE *resp)
{
    if (resp->rc != LCB_SUCCESS) {
        fprintf(stderr, "Failed to store document: %s\n", lcb_strerror(NULL, resp->rc));
        exit(EXIT_FAILURE);
    }

    lcb_MUTATION_TOKEN *mt = reinterpret_cast< lcb_MUTATION_TOKEN * >(resp->cookie);
    *mt = *lcb_resp_get_mutation_token(cbtype, resp);
}

int main(int, char **)
{
    lcb_t instance;
    lcb_create_st crst = {};
    lcb_error_t rc;

    crst.version = 3;
    crst.v.v3.connstr = "couchbase://127.0.0.1/default?fetch_mutation_tokens=true";
    crst.v.v3.username = "testuser";
    crst.v.v3.passwd = "password";

    rc = lcb_create(&instance, &crst);
    rc = lcb_connect(instance);
    lcb_wait(instance);
    rc = lcb_get_bootstrap_status(instance);

    // Initialize random seed to get a "random" value in our documents
    srand(time(NULL));
    RandomNumber_g = rand() % 10000;

    // Install the storage callback which will be used to retrieve the
    // mutation token
    lcb_install_callback3(instance, LCB_CALLBACK_STORE, storage_callback);

    lcb_MUTATION_TOKEN mt;
    memset(&mt, 0, sizeof mt);

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

    rc = lcb_store3(instance, &mt, &scmd);
    lcb_wait(instance);

    lcb_CMDN1QL cmd = {};
    RowList rows;
    cmd.callback = query_callback;

    // At the time of writing, the lcb_N1QLPARAMS implementation has some
    // bugs in it with respect to adding mutation tokens. For this reason, we're
    // encoding the query manually. This would look a bit nicer if we were
    // using a real JSON library:

    const char *bucketname;
    lcb_cntl(instance, LCB_CNTL_GET, LCB_CNTL_BUCKETNAME, &bucketname);
    std::stringstream stmt;

    stmt << "{"
            "\"statement\":\"SELECT name, email, random FROM default WHERE $1 in name\","
            "\"args\":[\"Brass\"],"
            "\"scan_consistency\":\"at_plus\",";

    stmt << "\"scan_vectors\":{"
         << "\"" << bucketname << "\":{"
         << "\"" << LCB_MUTATION_TOKEN_VB(&mt) << "\":[" << LCB_MUTATION_TOKEN_SEQ(&mt) << ","
         << "\"" << LCB_MUTATION_TOKEN_ID(&mt) << "\""
         << "]"
         << "}"
         << "}"
         << "}";

    /*
    The above expands to something like this:

    {
        "statement": "SELECT name, email, random FROM default WHERE $1 in name",
        "args": ["Brass"],
        "scan_consistency": "at_plus",
        "scan_vectors": {
            "default": {
                "29": [3, "88598346863273"]
            }
        }
    }
    */

    std::string stmt_str = stmt.str();
    cmd.query = stmt_str.c_str();
    cmd.nquery = stmt_str.size();
    rc = lcb_n1ql_query(instance, &rows, &cmd);
    assert(rc == LCB_SUCCESS);
    lcb_wait(instance);

    // To demonstrate the at_plus feature,
    // we check each row for the "random" value.
    // Because the C standard library does not come with a JSON
    // parser, we are limited in how we can inspect the row (which is JSON).
    // For clarity, we print out only the row's "Random" field. When the
    // at_plus feature is enabled, one of the results should contain
    // the newest random number (the value of RandomNumber_g). When disabled, the
    // row may or may not appear.
    for (RowList::iterator ii = rows.begin(); ii != rows.end(); ++ii) {
        std::string &row = *ii;
        size_t begin_pos = row.find("\"random\"");
        size_t end_pos = row.find_first_of("},", begin_pos);
        std::string row_number = row.substr(begin_pos, end_pos - begin_pos);
        printf("Row has random number %s\n", row_number.c_str());
    }

    lcb_destroy(instance);
}
