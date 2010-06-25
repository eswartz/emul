/*******************************************************************************
 * Copyright (c) 2010 Freescale Semiconductor, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Freescale Semiconductor - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.core;

import java.text.DecimalFormatSymbols;

import org.eclipse.tm.tcf.protocol.Protocol;

/**
 * Internal utility methods used for logging/tracing.
 */
public class LoggingUtil {

    private static final char DECIMAL_DELIMITER = new DecimalFormatSymbols().getDecimalSeparator();

    /**
     * Returns a relative timestamp in the form "seconds,milliseconds". Each
     * segment is zero-padded to three digits, ensuring a consistent length of
     * seven characters. The timestamp has no absolute meaning. It is merely the
     * elapsed time since January 1, 1970 UT truncated at 999 seconds. Do not
     * use this for production code, especially for mathematically determining
     * the relative time between two events, since the counter will flip to zero
     * roughly every 16 minutes.
     */
    public static String getDebugTime() {
        StringBuilder traceBuilder = new StringBuilder();

        // Record the time
        long time = System.currentTimeMillis();
        long seconds = (time / 1000) % 1000;
        if (seconds < 100) traceBuilder.append('0');
        if (seconds < 10) traceBuilder.append('0');
        traceBuilder.append(seconds);
        traceBuilder.append(DECIMAL_DELIMITER);
        long millis = time % 1000;
        if (millis < 100) traceBuilder.append('0');
        if (millis < 10) traceBuilder.append('0');
        traceBuilder.append(millis);
        return traceBuilder.toString();
    }

    /**
     * Trace hooks should use this method to log a message. It prepends the
     * message with a timestamp and sends it to the TCF logger facility. The
     * logger implementation may or may not inject its own timestamp. For
     * tracing, we definitely need one, so we introduce a minimal, relative-time
     * stamp.
     *
     * @param msg
     *            the trace message
     */
    public static void trace(String msg) {
        Protocol.log(LoggingUtil.getDebugTime() + ' ' + msg, null);
    }
}
