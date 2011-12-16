/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 * You may elect to redistribute this code under either of these licenses.
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/

/*
 * Sample TCF service implementation.
 */

#include <config.h>
#include <time.h>
#include <framework/json.h>
#include <framework/errors.h>
#include <framework/exceptions.h>
#include <daytime.h>

static const char * DAYTIME = "Daytime";

static void command_get_time_of_day(char * token, Channel * c) {
    char str[0x100];
    char res[0x100];
    time_t t;

    // Read command argumnet: string TZ - time zone name
    json_read_string(&c->inp, str, sizeof(str));
    // Each JSON encoded argument should end with zero byte
    if (c->inp.read(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    // Done reading arguments.
    // The command message should end with MARKER_EOM (End Of Message)
    if (c->inp.read(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    // Execute the command: retrieve current time as a string.
    // Note: we ignore command argument for simplicity,
    // a real command handler should do something better then that.
    time(&t);
    strcpy(res, ctime(&t));

    // Start reply message with zero terminated string "R"
    write_stringz(&c->out, "R");
    // Send back the command token
    write_stringz(&c->out, token);
    // Send error report, for now always "no error"
    write_errno(&c->out, 0);
    // Send reply data
    json_write_string(&c->out, res);
    // JSON encoded data should end with zero byte
    c->out.write(&c->out, 0);
    // Done sending reply data.
    // The reply message should end with MARKER_EOM (End Of Message)
    c->out.write(&c->out, MARKER_EOM);
    // Done sending reply message.
    // Command handling is complete.
}

void ini_daytime_service(Protocol * proto) {
        // Install command handler
    add_command_handler(proto, DAYTIME, "getTimeOfDay", command_get_time_of_day);
}
