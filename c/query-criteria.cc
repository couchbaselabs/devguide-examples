#include <libcouchbase/couchbase.h>
#include <libcouchbase/n1ql.h>
#include <vector>
#include <string>
#include <iostream>

struct Rows {
    std::vector< std::string > rows;
    std::string metadata;
    lcb_error_t rc;
    short htcode;
    Rows() : rc(LCB_ERROR), htcode(0) {}
};

static void query_callback(lcb_t, int, const lcb_RESPN1QL *resp)
{
    Rows *rows = reinterpret_cast< Rows * >(resp->cookie);

    // Check if this is the last invocation
    if (resp->rflags & LCB_RESP_F_FINAL) {
        rows->rc = resp->rc;

        // Assign the metadata (usually not needed)
        rows->metadata.assign(resp->row, resp->nrow);

    } else {
        rows->rows.push_back(std::string(resp->row, resp->nrow));
    }
}

int main(int, char **)
{
    lcb_t instance;
    lcb_create_st crst;
    lcb_error_t rc;
    lcb_N1QLPARAMS *params;
    lcb_CMDN1QL cmd = {};
    Rows rows;

    crst.version = 3;
    crst.v.v3.connstr = "couchbase://127.0.0.1/travel-sample";
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

    params = lcb_n1p_new();
    rc = lcb_n1p_setstmtz(params, "SELECT airportname, city, country FROM `travel-sample` "
                                  "WHERE type=\"airport\" AND city=\"Reno\"");
    cmd.callback = query_callback;
    rc = lcb_n1p_mkcmd(params, &cmd);
    rc = lcb_n1ql_query(instance, &rows, &cmd);
    lcb_wait(instance);

    if (rows.rc == LCB_SUCCESS) {
        std::cout << "Query successful!" << std::endl;
        std::vector< std::string >::iterator ii;
        for (ii = rows.rows.begin(); ii != rows.rows.end(); ++ii) {
            std::cout << *ii << std::endl;
        }
    } else {
        std::cerr << "Query failed!";
        std::cerr << "(" << int(rows.rc) << "). ";
        std::cerr << lcb_strerror(NULL, rows.rc) << std::endl;
    }
    lcb_n1p_free(params);
    lcb_destroy(instance);
}
