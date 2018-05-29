#include <libcouchbase/couchbase.h>
#include <string.h>
#include <stdlib.h>

static void die(lcb_error_t rc, const char *msg)
{
    fprintf(stderr, "%s failed. (0x%x, %s)\n", msg, rc, lcb_strerror(NULL, rc));
    exit(EXIT_FAILURE);
}

int main(int argc, char **argv)
{
    lcb_t instance;
    struct lcb_create_st cropts;
    lcb_error_t rc;

    memset(&cropts, 0, sizeof cropts);
    cropts.version = 3;
    cropts.v.v3.connstr = "couchbases://127.0.0.1/default"
                          "?truststorepath=../etc/x509-cert/SSLCA/clientdir/trust.pem"
                          "&certpath=../etc/x509-cert/SSLCA/clientdir/client.pem"
                          "&keypath=../etc/x509-cert/SSLCA/clientdir/client.key";

    rc = lcb_create(&instance, &cropts);
    if (rc != LCB_SUCCESS) {
        die(rc, "Creating instance");
    }

    rc = lcb_connect(instance);
    if (rc != LCB_SUCCESS) {
        die(rc, "Connection scheduling");
    }

    /* This function required to actually schedule the operations on the network */
    lcb_wait(instance);

    /* Determines if the bootstrap/connection succeeded */
    rc = lcb_get_bootstrap_status(instance);
    if (rc != LCB_SUCCESS) {
        die(rc, "Connection bootstraping");
    } else {
        printf("Connection succeeded. Cluster has %d nodes\n", lcb_get_num_nodes(instance));
    }

    /* SSL connections use different ports. For example, the REST API
     * connection will use port 18091 rather than 8091 when using SSL */
    const char *node = lcb_get_node(instance, LCB_NODE_HTCONFIG, 0);
    printf("First node address for REST API: %s\n", node);

    lcb_destroy(instance);
    return 0;
}
