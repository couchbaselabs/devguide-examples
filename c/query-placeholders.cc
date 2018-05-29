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

// Copy/pasted from query-criteria.cc
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

void dump_results(const Rows &rows)
{
    if (rows.rc == LCB_SUCCESS) {
        std::cout << "Query successful!" << std::endl;
        std::vector< std::string >::const_iterator ii;
        for (ii = rows.rows.begin(); ii != rows.rows.end(); ++ii) {
            std::cout << *ii << std::endl;
        }
    } else {
        std::cerr << "Query failed!";
        std::cerr << "(" << int(rows.rc) << "). ";
        std::cerr << lcb_strerror(NULL, rows.rc) << std::endl;
    }
}

static void query_city(lcb_t instance, const char *city)
{
    lcb_N1QLPARAMS *params = lcb_n1p_new();
    lcb_error_t rc;
    lcb_CMDN1QL cmd = {};
    Rows rows;

    // Need to make this properly formatted JSON
    std::string city_str;
    city_str += '"';
    city_str += city;
    city_str += '"';

    rc = lcb_n1p_setstmtz(params, "SELECT airportname FROM `travel-sample` "
                                  "WHERE city=$1 AND type=\"airport\"");
    rc = lcb_n1p_posparam(params, city_str.c_str(), city_str.size());

    cmd.callback = query_callback;

    // To enable using prepared (optimized) statements, you can use
    // the LCB_CMDN1QL_F_PREPCACHE flag. This is equivalent to setting
    // 'adhoc=False' in other SDKs
    cmd.cmdflags |= LCB_CMDN1QL_F_PREPCACHE;

    rc = lcb_n1p_mkcmd(params, &cmd);
    rc = lcb_n1ql_query(instance, &rows, &cmd);
    if (rc != LCB_SUCCESS) {
        printf("Unable to schedule N1QL query: %s\n", lcb_strerror_short(rc));
        exit(1);
    }
    lcb_wait(instance);

    std::cout << "Results for " << city << std::endl;
    dump_results(rows);
    lcb_n1p_free(params);
}

int main(int, char **)
{
    lcb_t instance;
    lcb_create_st crst = {};
    lcb_error_t rc;

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

    query_city(instance, "Reno");
    query_city(instance, "Dallas");
    query_city(instance, "Los Angeles");

    lcb_destroy(instance);
}
