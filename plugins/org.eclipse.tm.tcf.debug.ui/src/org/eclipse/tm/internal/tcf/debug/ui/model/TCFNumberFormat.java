/*******************************************************************************
 * Copyright (c) 2008, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.debug.ui.model;

import java.math.BigInteger;

public class TCFNumberFormat {

    public static String isValidHexNumber(String s) {
        int l = s.length();
        if (l == 0) return "Need at least one digit";
        for (int i = 0; i < l; i++) {
            char ch = s.charAt(i);
            if (ch >= '0' && ch <= '9') continue;
            if (ch >= 'A' && ch <= 'F') continue;
            if (ch >= 'a' && ch <= 'f') continue;
            return "Hex digit expected";
        }
        return null;
    }

    public static String isValidDecNumber(boolean fp, String s) {
        int i = 0;
        int l = s.length();
        if (l == 0) return "Need at least one digit";
        char ch = s.charAt(i++);
        if (ch == '-' || ch == '+') {
            if (i >= l) return "Need at least one digit";
            ch = s.charAt(i++);
        }
        if (fp) {
            String n = s.substring(i - 1);
            if (n.equals("NaN")) return null;
            if (n.equals("Infinity")) return null;
        }
        while (ch >= '0' && ch <= '9') {
            if (i >= l) return null;
            ch = s.charAt(i++);
        }
        if (fp) {
            if (ch == '.') {
                if (i >= l) return null;
                ch = s.charAt(i++);
                while (ch >= '0' && ch <= '9') {
                    if (i >= l) return null;
                    ch = s.charAt(i++);
                }
            }
            if (ch == 'e' || ch == 'E') {
                if (i >= l) return "Invalid exponent: need at least one digit";
                ch = s.charAt(i++);
                if (ch == '-' || ch == '+') {
                    if (i >= l) return "Invalid exponent: need at least one digit";
                    ch = s.charAt(i++);
                }
                while (ch >= '0' && ch <= '9') {
                    if (i >= l) return null;
                    ch = s.charAt(i++);
                }
                return "Invalid exponent: decimal digit expected";
            }
        }
        return "Decimal digit expected";
    }

    public static byte[] toByteArray(String s, int radix, boolean fp, int size, boolean signed, boolean big_endian) throws Exception {
        byte[] bf = null;
        if (!fp) {
            bf = new BigInteger(s, radix).toByteArray();
        }
        else if (size == 4) {
            int n = Float.floatToIntBits(Float.parseFloat(s));
            bf = new byte[size];
            for (int i = 0; i < size; i++) {
                bf[i] = (byte)((n >> ((size - 1 - i) * 8)) & 0xff);
            }
        }
        else if (size == 8) {
            long n = Double.doubleToLongBits(Double.parseDouble(s));
            bf = new byte[size];
            for (int i = 0; i < size; i++) {
                bf[i] = (byte)((n >> ((size - 1 - i) * 8)) & 0xff);
            }
        }
        else {
            throw new Exception("Unsupported floating point format");
        }
        byte[] rs = new byte[size];
        if (signed && rs.length > bf.length && (bf[0] & 0x80) != 0) {
            // Sign extension
            for (int i = 0; i < rs.length; i++) rs[i] = (byte)0xff;
        }
        for (int i = 0; i < bf.length; i++) {
            // i == 0 -> least significant byte
            byte b = bf[bf.length - i - 1];
            int j = big_endian ? rs.length - i - 1 : i;
            if (j < rs.length) rs[j] = b;
        }
        return rs;
    }
}
