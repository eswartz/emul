# *******************************************************************************
# * Copyright (c) 2011 Wind River Systems, Inc. and others.
# * All rights reserved. This program and the accompanying materials
# * are made available under the terms of the Eclipse Public License v1.0
# * which accompanies this distribution, and is available at
# * http:#www.eclipse.org/legal/epl-v10.html
# *
# * Contributors:
# *     Wind River Systems - initial API and implementation
# *******************************************************************************

import cStringIO, time, types

# Error report attribute names
ERROR_CODE = "Code"           # integer
ERROR_TIME = "Time"           # integer
ERROR_SERVICE = "Service"     # string
ERROR_FORMAT = "Format"       # string
ERROR_PARAMS = "Params"       # array
ERROR_SEVERITY = "Severity"   # integer
ERROR_ALT_CODE = "AltCode"    # integer
ERROR_ALT_ORG = "AltOrg"      # string
ERROR_CAUSED_BY = "CausedBy"  # object

# Error severity codes
SEVERITY_ERROR = 0
SEVERITY_WARNING = 1
SEVERITY_FATAL = 2

# Error code ranges
# Standard TCF code range */
CODE_STD_MIN = 0
CODE_STD_MAX = 0xffff

# Service specific codes. Decoding requires service ID. */
CODE_SERVICE_SPECIFIC_MIN = 0x10000
CODE_SERVICE_SPECIFIC_MAX = 0x1ffff

# Reserved codes - will never be used by the TCF standard */
CODE_RESERVED_MIN = 0x20000
CODE_RESERVED_MAX = 0x2ffff

# Standard TCF error codes
TCF_ERROR_OTHER               = 1
TCF_ERROR_JSON_SYNTAX         = 2
TCF_ERROR_PROTOCOL            = 3
TCF_ERROR_BUFFER_OVERFLOW     = 4
TCF_ERROR_CHANNEL_CLOSED      = 5
TCF_ERROR_COMMAND_CANCELLED   = 6
TCF_ERROR_UNKNOWN_PEER        = 7
TCF_ERROR_BASE64              = 8
TCF_ERROR_EOF                 = 9
TCF_ERROR_ALREADY_STOPPED     = 10
TCF_ERROR_ALREADY_EXITED      = 11
TCF_ERROR_ALREADY_RUNNING     = 12
TCF_ERROR_ALREADY_ATTACHED    = 13
TCF_ERROR_IS_RUNNING          = 14
TCF_ERROR_INV_DATA_SIZE       = 15
TCF_ERROR_INV_CONTEXT         = 16
TCF_ERROR_INV_ADDRESS         = 17
TCF_ERROR_INV_EXPRESSION      = 18
TCF_ERROR_INV_FORMAT          = 19
TCF_ERROR_INV_NUMBER          = 20
TCF_ERROR_INV_DWARF           = 21
TCF_ERROR_SYM_NOT_FOUND       = 22
TCF_ERROR_UNSUPPORTED         = 23
TCF_ERROR_INV_DATA_TYPE       = 24
TCF_ERROR_INV_COMMAND         = 25
TCF_ERROR_INV_TRANSPORT       = 26
TCF_ERROR_CACHE_MISS          = 27
TCF_ERROR_NOT_ACTIVE          = 28

_timestamp_format = "%Y-%m-%d %H:%M:%S"

class ErrorReport(Exception):
    def __init__(self, msg, attrs):
        super(ErrorReport, self).__init__(msg)
        if type(attrs) is types.IntType:
            attrs = {
                ERROR_CODE : attrs,
                ERROR_TIME : int(time.time()),
                ERROR_FORMAT : msg,
                ERROR_SEVERITY : SEVERITY_ERROR
            }
        self.attrs = attrs
        caused_by = attrs.get(ERROR_CAUSED_BY)
        if caused_by:
            map = caused_by
            bf = cStringIO.StringIO()
            bf.write("TCF error report:")
            bf.write('\n')
            appendErrorProps(bf, map)
            self.caused_by = ErrorReport(bf.getvalue(), map)

    def getErrorCode(self):
        return self.attrs.get(ERROR_CODE) or 0

    def getAltCode(self):
        return self.attrs.get(ERROR_ALT_CODE) or 0

    def getAltOrg(self):
        return self.attrs.get(ERROR_ALT_ORG)

    def getAttributes(self):
        return self.attrs


def toErrorString(data):
    if not data: return None
    map = data
    fmt = map.get(ERROR_FORMAT)
    if fmt:
        c = map.get(ERROR_PARAMS)
        if c: return fmt.format(c)
        return fmt
    code = map.get(ERROR_CODE)
    if code is not None:
        if code == TCF_ERROR_OTHER:
            alt_org = map.get(ERROR_ALT_ORG)
            alt_code = map.get(ERROR_ALT_CODE)
            if alt_org and alt_code:
                return "%s Error %d" % (alt_org, alt_code)
        return "TCF Error %d" % code
    return "Invalid error report format"

def appendErrorProps(bf, map):
    timeVal = map.get(ERROR_TIME)
    code = map.get(ERROR_CODE)
    service = map.get(ERROR_SERVICE)
    severity = map.get(ERROR_SEVERITY)
    alt_code = map.get(ERROR_ALT_CODE)
    alt_org = map.get(ERROR_ALT_ORG)
    if timeVal:
        bf.write('\n')
        bf.write("Time: ")
        bf.write(time.strftime(_timestamp_format, time.localtime(timeVal/1000.)))
    if severity:
        bf.write('\n')
        bf.write("Severity: ")
        if severity == SEVERITY_ERROR: bf.write("Error")
        elif severity == SEVERITY_FATAL: bf.write("Fatal")
        elif severity == SEVERITY_WARNING: bf.write("Warning")
        else: bf.write("Unknown")
    bf.write('\n')
    bf.write("Error text: ")
    bf.write(toErrorString(map))
    bf.write('\n')
    bf.write("Error code: ")
    bf.write(str(code))
    if service:
        bf.write('\n')
        bf.write("Service: ")
        bf.write(service)
    if alt_code:
        bf.write('\n')
        bf.write("Alt code: ")
        bf.write(str(alt_code))
        if alt_org:
            bf.write('\n')
            bf.write("Alt org: ")
            bf.write(alt_org)
