# *******************************************************************************
# * Copyright (c) 2011 Wind River Systems, Inc. and others.
# * All rights reserved. This program and the accompanying materials
# * are made available under the terms of the Eclipse Public License v1.0
# * which accompanies this distribution, and is available at
# * http://www.eclipse.org/legal/epl-v10.html
# *
# * Contributors:
# *     Wind River Systems - initial API and implementation
# *******************************************************************************

"Internal utility methods used for logging/tracing."

from tcf import protocol
import locale, time, cStringIO

DECIMAL_DELIMITER = locale.localeconv().get('decimal_point', '.')

def getDebugTime():
    """
    Returns a relative timestamp in the form "seconds,milliseconds". Each
    segment is zero-padded to three digits, ensuring a consistent length of
    seven characters. The timestamp has no absolute meaning. It is merely the
    elapsed time since January 1, 1970 UT truncated at 999 seconds. Do not
    use this for production code, especially for mathematically determining
    the relative time between two events, since the counter will flip to zero
    roughly every 16 minutes.
    """
    traceBuilder = cStringIO.StringIO()

    # Record the time
    tm = int(time.time())
    seconds = (tm / 1000) % 1000
    if seconds < 100: traceBuilder.write('0')
    if seconds < 10: traceBuilder.write('0')
    traceBuilder.write(str(seconds))
    traceBuilder.write(DECIMAL_DELIMITER)
    millis = time % 1000
    if millis < 100: traceBuilder.write('0')
    if millis < 10: traceBuilder.write('0')
    traceBuilder.write(str(millis))
    return traceBuilder.getvalue()

def trace(msg):
    """
    Trace hooks should use this method to log a message. It prepends the
    message with a timestamp and sends it to the TCF logger facility. The
    logger implementation may or may not inject its own timestamp. For
    tracing, we definitely need one, so we introduce a minimal, relative-time
    stamp.
    
    @param msg  the trace message
    """
    protocol.log('%s msg' % (getDebugTime(), msg))
