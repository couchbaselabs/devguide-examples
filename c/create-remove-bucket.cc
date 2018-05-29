#include <libcouchbase/couchbase.h>
#include <cassert>
#include <cstdlib>
#include <string>

static void http_callback(lcb_t, int, const lcb_RESPHTTP *resp)
{
    printf("Operation completed with HTTP code: %d\n", resp->htstatus);
    printf("Payload: %.*s\n", (int)resp->nbody, (char *)resp->body);
}

int main(int, char **)
{
    lcb_create_st crst = {};
    lcb_t instance;

    crst.version = 3;
    crst.v.v3.connstr = "couchbase://127.0.0.1/default";
    crst.v.v3.username = "testuser";
    crst.v.v3.passwd = "password";

    lcb_create(&instance, &crst);
    lcb_connect(instance);
    lcb_wait(instance);
    assert(lcb_get_bootstrap_status(instance) == LCB_SUCCESS);
    lcb_install_callback3(instance, LCB_CALLBACK_HTTP, (lcb_RESPCALLBACK)http_callback);

    // Create the required parameters according to the Couchbase REST API
    std::string path("/pools/default/buckets");

    std::string params;
    params += "name=newBucket&";
    params += "bucketType=couchbase&";

    // authType should always be SASL. You can leave the saslPassword field
    // empty if you don't want to protect this bucket.
    params += "authType=sasl&saslPassword=&";
    params += "ramQuotaMB=100";
    printf("Using %s\n", params.c_str());

    lcb_CMDHTTP htcmd = {};
    LCB_CMD_SET_KEY(&htcmd, path.c_str(), path.size());
    htcmd.body = params.c_str();
    htcmd.nbody = params.size();
    htcmd.content_type = "application/x-www-form-urlencoded";
    htcmd.method = LCB_HTTP_METHOD_POST;
    htcmd.type = LCB_HTTP_TYPE_MANAGEMENT;
    htcmd.username = "Administrator";
    htcmd.password = "password";
    lcb_http3(instance, NULL, &htcmd);
    lcb_wait(instance);

    // now remove the bucket
    memset(&htcmd, 0, sizeof htcmd);
    path = "/pools/default/buckets/newBucket";
    LCB_CMD_SET_KEY(&htcmd, path.c_str(), path.size());
    htcmd.method = LCB_HTTP_METHOD_DELETE;
    htcmd.type = LCB_HTTP_TYPE_MANAGEMENT;
    htcmd.username = "Administrator";
    htcmd.password = "password";
    lcb_http3(instance, NULL, &htcmd);
    lcb_wait(instance);

    lcb_destroy(instance);
}
