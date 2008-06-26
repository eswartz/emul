/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.tcf.core;

/**
 * Methods for translating Base64 encoded strings to byte arrays and back.
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public final class Base64 {
    
    public static char[] toBase64(byte[] buf, int pos, int len) {
        char[] out_buf = new char[4 * ((len + 2) / 3)];
        int end = pos + len;
        int out_pos = 0;
        while (pos < end) {
            int byte0 = buf[pos++] & 0xff;
            out_buf[out_pos++] = int2char[byte0 >> 2];
            if (pos == end) {
                out_buf[out_pos++] = int2char[(byte0 << 4) & 0x3f];
                out_buf[out_pos++] = '=';
                out_buf[out_pos++] = '=';
            }
            else {
                int byte1 = buf[pos++] & 0xff;
                out_buf[out_pos++] = int2char[(byte0 << 4) & 0x3f | (byte1 >> 4)];
                if (pos == end) {
                    out_buf[out_pos++] = int2char[(byte1 << 2) & 0x3f];
                    out_buf[out_pos++] = '=';
                }
                else {
                    int byte2 = buf[pos++] & 0xff;
                    out_buf[out_pos++] = int2char[(byte1 << 2) & 0x3f | (byte2 >> 6)];
                    out_buf[out_pos++] = int2char[byte2 & 0x3f];
                }
            }
        }
        assert out_pos == out_buf.length;
        return out_buf;
    }
    
    public static void toByteArray(byte[] buf, int offs, int size, char[] inp) {
        int out_pos = offs;
        if (inp != null) {
            int inp_len = inp.length;
            if (inp_len % 4 != 0) {
                throw new IllegalArgumentException(
                        "BASE64 string length must be a multiple of four.");
            }
            int out_len = inp_len / 4 * 3;
            if (inp_len > 0 && inp[inp_len - 1] == '=') {
                out_len--;
                if (inp[inp_len - 2] == '=') {
                    out_len--;
                }
            }
            if (out_len > size) {
                throw new IllegalArgumentException(
                        "BASE64 data array is longer then destination buffer.");
            }
            int inp_pos = 0;
            while (inp_pos < inp_len) {
                int n0, n1, n2, n3;
                char ch0 = inp[inp_pos++];
                char ch1 = inp[inp_pos++];
                char ch2 = inp[inp_pos++];
                char ch3 = inp[inp_pos++];
                if (ch0 >= char2int.length || (n0 = char2int[ch0]) < 0) {
                    throw new IllegalArgumentException("Illegal character " + ch0);
                }
                if (ch1 >= char2int.length || (n1 = char2int[ch1]) < 0) {
                    throw new IllegalArgumentException("Illegal character " + ch1);
                }
                buf[out_pos++] = (byte)((n0 << 2) | (n1 >> 4));
                if (ch2 == '=') break;
                if (ch2 >= char2int.length || (n2 = char2int[ch2]) < 0) {
                    throw new IllegalArgumentException("Illegal character " + ch2);
                }
                buf[out_pos++] = (byte)((n1 << 4) | (n2 >> 2));
                if (ch3 == '=') break;
                if (ch3 >= char2int.length || (n3 = char2int[ch3]) < 0) {
                    throw new IllegalArgumentException("Illegal character " + ch3);
                }
                buf[out_pos++] = (byte)((n2 << 6) | n3);
            }
            assert out_pos == offs + out_len;
        }
        while (out_pos < offs + size) buf[out_pos++] = 0;
    }

    public static byte[] toByteArray(char[] inp) {
        int inp_len = inp.length;
        int out_len = inp_len / 4 * 3;
        if (inp_len > 0 && inp[inp_len - 1] == '=') {
            out_len--;
            if (inp[inp_len - 2] == '=') {
                out_len--;
            }
        }
        byte[] buf = new byte[out_len];
        toByteArray(buf, 0, buf.length, inp);
        return buf;
    }
    
    /*
     * See RFC 2045.
     */
    private static final char int2char[] = {
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H',
        'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
        'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X',
        'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f',
        'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
        'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
        'w', 'x', 'y', 'z', '0', '1', '2', '3',
        '4', '5', '6', '7', '8', '9', '+', '/'
    };

    /*
     * See RFC 2045
     */
    private static final byte char2int[] = {
        -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, 62, -1, -1, -1, 63,
        52, 53, 54, 55, 56, 57, 58, 59,
        60, 61, -1, -1, -1, -1, -1, -1,
        -1,  0,  1,  2,  3,  4,  5,  6,
         7,  8,  9, 10, 11, 12, 13, 14,
        15, 16, 17, 18, 19, 20, 21, 22,
        23, 24, 25, -1, -1, -1, -1, -1,
        -1, 26, 27, 28, 29, 30, 31, 32,
        33, 34, 35, 36, 37, 38, 39, 40,
        41, 42, 43, 44, 45, 46, 47, 48,
        49, 50, 51
    };
}
