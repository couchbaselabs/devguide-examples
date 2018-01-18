/* -*- Mode: C; tab-width: 4; c-basic-offset: 4; indent-tabs-mode: nil -*- */
/*
 *     Copyright 2012-2013 Couchbase, Inc.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

/**
 * @file
 *
 * This is a minimal example file showing how to encode custom binary formats to be embedded into document.
 *
 * It stores user avatar along with profile information.
 */

#include <stdio.h>
#include <libcouchbase/couchbase.h>
#include <libcouchbase/api3.h>
#include <stdlib.h>
#include <string.h> /* strlen */
#ifdef _WIN32
#define PRIx64 "I64x"
#else
#include <inttypes.h>
#endif

#include "base64.h"
#include "avatar.h"
#include "cJSON.h"

static void die(lcb_t instance, const char *msg, lcb_error_t err)
{
    fprintf(stderr, "%s. Received code 0x%X (%s)\n", msg, err, lcb_strerror(instance, err));
    exit(EXIT_FAILURE);
}

typedef struct {
    char *name;
    int age;
    void *avatar;
    int avatar_len;
} Profile;

static Profile *new_profile()
{
    return calloc(1, sizeof(Profile));
}

static void free_profile(Profile *profile)
{
    if (profile) {
        free(profile->name);
        free(profile->avatar);
    }
    free(profile);
}

static Profile *decode_profile(const char *data)
{
    Profile *profile = NULL;
    cJSON *json = cJSON_Parse(data);

    if (json) {
        cJSON *val;

        profile = new_profile();
        val = cJSON_GetObjectItem(json, "name");
        if (val && val->type == cJSON_String) {
            profile->name = strdup(val->valuestring);
        }
        val = cJSON_GetObjectItem(json, "age");
        if (val && val->type == cJSON_Number) {
            profile->age = val->valueint;
        }
        val = cJSON_GetObjectItem(json, "avatar");
        if (val && val->type == cJSON_String) {
            profile->avatar = unbase64(val->valuestring, strlen(val->valuestring), &profile->avatar_len);
        }
        cJSON_Delete(json);
    }
    return profile;
}

static char *encode_profile(Profile *profile)
{
    cJSON *json = cJSON_CreateObject();
    if (profile->name) {
        cJSON *val = cJSON_CreateString(profile->name);
        cJSON_AddItemToObject(json, "name", val);
    }
    if (profile->age) {
        cJSON *val = cJSON_CreateNumber(profile->age);
        cJSON_AddItemToObject(json, "age", val);
    }
    if (profile->avatar_len > 0 && profile->avatar) {
        cJSON *val;
        char *b64_val;
        int b64_len = 0;
        b64_val = base64(profile->avatar, profile->avatar_len, &b64_len);
        val = cJSON_CreateString(b64_val);
        cJSON_AddItemToObject(json, "avatar", val);
    }
    return cJSON_PrintUnformatted(json);
}

static void store_callback(lcb_t instance, int cbtype, const lcb_RESPBASE *rb)
{
    if (rb->rc == LCB_SUCCESS) {
        fprintf(stderr, "The profile has been persisted in Couchbase successfully (CAS: 0x%" PRIx64 ")\n", rb->cas);
    } else {
        die(instance, lcb_strcbtype(cbtype), rb->rc);
    }
}

static void get_callback(lcb_t instance, int cbtype, const lcb_RESPBASE *rb)
{
    char *filename = rb->cookie;
    if (rb->rc == LCB_SUCCESS) {
        /* generate HTML profile with embedded avatar */
        FILE *html = fopen(filename, "w+");
        if (html) {
            const lcb_RESPGET *rg = (const lcb_RESPGET *)rb;
            Profile *profile = decode_profile(rg->value);
            char *b64_val;
            int b64_len = 0;
            b64_val = base64(profile->avatar, profile->avatar_len, &b64_len);
            fprintf(html, "<img src=\"data:image/jpeg;base64,%.*s\"><br>", b64_len, b64_val);
            fprintf(html, "<b>NAME:</b> %s<br>", profile->name);
            fprintf(html, "<b>AGE:</b> %d<br>", profile->age);
            fclose(html);
            fprintf(stderr, "The profile has been retrieved from Couchbase and stored as HTML in \"%s\"\n", filename);
            free_profile(profile);
        } else {
            perror("failed to open output file for writing");
        }
    } else {
        die(instance, lcb_strcbtype(cbtype), rb->rc);
    }
}

int main(int argc, char *argv[])
{
    lcb_error_t err;
    lcb_t instance;
    struct lcb_create_st create_options = {0};
    lcb_CMDSTORE scmd = {0};
    lcb_CMDGET gcmd = {0};
    char *html_filename = "profile.html";

    create_options.version = 3;

    if (argc < 2) {
        fprintf(stderr, "Usage: %s couchbase://host/bucket [ password [ username [ filename ] ] ]\n", argv[0]);
        exit(EXIT_FAILURE);
    }

    create_options.v.v3.connstr = argv[1];
    if (argc > 2) {
        create_options.v.v3.passwd = argv[2];
    }
    if (argc > 3) {
        create_options.v.v3.username = argv[3];
    }
    if (argc > 4) {
        html_filename = argv[4];
    }

    err = lcb_create(&instance, &create_options);
    if (err != LCB_SUCCESS) {
        die(NULL, "Couldn't create couchbase handle", err);
    }

    err = lcb_connect(instance);
    if (err != LCB_SUCCESS) {
        die(instance, "Couldn't schedule connection", err);
    }

    lcb_wait(instance);

    err = lcb_get_bootstrap_status(instance);
    if (err != LCB_SUCCESS) {
        die(instance, "Couldn't bootstrap from cluster", err);
    }

    /* Assign the handlers to be called for the operation types */
    lcb_install_callback3(instance, LCB_CALLBACK_GET, get_callback);
    lcb_install_callback3(instance, LCB_CALLBACK_STORE, store_callback);

    char *id = "profile-0001";
    Profile profile = {"Griet", 16,
                       /* the avatar could have been received via network, or loaded from file system */
                       avatar_jpg, avatar_jpg_len};

    /* encoding user data to be stored in Couchbase as JSON */
    char *json = encode_profile(&profile);

    LCB_CMD_SET_KEY(&scmd, id, strlen(id));
    LCB_CMD_SET_VALUE(&scmd, json, strlen(json));
    scmd.operation = LCB_SET;

    err = lcb_store3(instance, NULL, &scmd);
    free(json); /* now we can release memory occupied by JSON representation of the profile */
    if (err != LCB_SUCCESS) {
        die(instance, "Couldn't schedule storage operation", err);
    }

    /* The store_callback is invoked from lcb_wait() */
    fprintf(stderr, "Will wait for storage operation to complete..\n");
    lcb_wait(instance);

    /* Now fetch the item back */
    LCB_CMD_SET_KEY(&gcmd, id, strlen(id));
    err = lcb_get3(instance, html_filename, &gcmd);
    if (err != LCB_SUCCESS) {
        die(instance, "Couldn't schedule retrieval operation", err);
    }

    /* Likewise, the get_callback is invoked from here */
    fprintf(stderr, "Will wait to retrieve item..\n");
    lcb_wait(instance);

    /* Now that we're all done, close down the connection handle */
    lcb_destroy(instance);
    return 0;
}
