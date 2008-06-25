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
package org.eclipse.tm.tcf.protocol;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.tm.internal.tcf.core.ReadOnlyCollection;
import org.eclipse.tm.internal.tcf.core.ReadOnlyMap;


/**
 * JSON is TCF preferred marshaling. This class implements generation and parsing of JSON strings.
 * The code is optimized for speed since it is a time-critical part of the framework.
 *
 * Reading of JSON produces data structure that consists of objects of these classes:
 * Boolean, Number, String, Collection, Map.
 * 
 * Writing of JSON is supported for:
 * Boolean, Number, String, char[], byte[], Object[], Collection, Map
 * 
 * Clients can enable writing support for objects of a other classes by
 * registering ObjectWriter interface implementation.
 */
public class JSON {
    
    /**
     * Clients implement ObjectWriter interface when they want to enable marshaling of
     * object classes that are not directly supported by JSON library.
     */
    public interface ObjectWriter {
        void write(Object o) throws IOException;
    }
    
    private static final Map<Class<?>,ObjectWriter> object_writers = new HashMap<Class<?>,ObjectWriter>(); 
    
    private static char[] tmp_buf = new char[0x1000];
    private static byte[] tmp_bbf = new byte[0x1000];
    private static int tmp_buf_pos;
    
    private static Reader reader;
    private static final char[] cur_buf = new char[0x1000];
    private static int cur_buf_pos;
    private static int cur_buf_len;
    private static int cur_ch;
    
    // This buffer is used to create nice error reports 
    private static final char[] err_buf = new char[100];
    private static int err_buf_pos;
    private static int err_buf_cnt;
    
    /**
     * Add a handler for converting objects of a particular class into JSON.
     * @param cls - a class
     * @param writer - ObjectWriter implementation that provides generation of JSON for a given class.
     */
    public static void addObjectWriter(Class<?> cls, ObjectWriter writer) {
        object_writers.put(cls, writer);
    }

    /**
     * Write a character into JSON output buffer.
     * Clients should not call this method directly, except from ObjectWriter implementation.
     * @param ch
     */
    public static void write(char ch) {
        if (tmp_buf_pos >= tmp_buf.length) {
            char[] tmp = new char[tmp_buf.length * 2];
            System.arraycopy(tmp_buf, 0, tmp, 0, tmp_buf_pos);
            tmp_buf = tmp;
        }
        tmp_buf[tmp_buf_pos++] = ch;
    }
    
    /**
     * Write a string into JSON output buffer.
     * The string is written "as-is". Call writeObject() to convert a String into JSON string.
     * Clients should not call this method directly, except from ObjectWriter implementation.
     * @param s - a string
     */
    public static void write(String s) {
        int l = s.length();
        for (int i = 0; i < l; i++) {
            char ch = s.charAt(i);
            if (tmp_buf_pos >= tmp_buf.length) write(ch);
            else tmp_buf[tmp_buf_pos++] = ch;
        }
    }
    
    /**
     * Write a non-negative integer number into JSON output buffer.
     * Clients should not call this method directly, except from ObjectWriter implementation.
     * @param n - a number
     */
    public static void writeUInt(int n) {
        assert n >= 0;
        if (n >= 10) writeUInt(n / 10);
        write((char)('0' + n % 10));
    }

    private static void read() throws IOException {
        if (cur_buf_pos >= cur_buf_len) {
            cur_buf_len = reader.read(cur_buf);
            cur_buf_pos = 0;
            if (cur_buf_len < 0) {
                cur_buf_len = 0;
                cur_ch = -1;
                return;
            }
        }
        cur_ch = cur_buf[cur_buf_pos++];
        err_buf[err_buf_pos++] = (char)cur_ch;
        if (err_buf_pos >= err_buf.length) {
            err_buf_pos = 0;
            err_buf_cnt++;
        }
    }
    
    private static void error() throws IOException {
        error("syntax error");
    }

    private static void error(String msg) throws IOException {
        StringBuffer bf = new StringBuffer();
        bf.append("JSON " + msg + ":");
        int cnt = 0;
        boolean nl = true;
        for (int i = 0;; i++) {
            char ch = 0;
            if (err_buf_cnt == 0 && i < err_buf_pos) {
                ch = err_buf[i];
            }
            else if (err_buf_cnt > 0 && i < err_buf.length) {
                ch = err_buf[(err_buf_pos + i) % err_buf.length];
            }
            else {
                int n = reader.read();
                if (n < 0) break;
                ch = (char)n;
            }
            if (nl) {
                bf.append("\n ");
                if (err_buf_cnt == 0) bf.append(cnt);
                else bf.append('*');
                bf.append(": ");
                if (cnt == 0 && err_buf_cnt > 0) bf.append("...");
                nl = false;
            }
            if (ch == 0) {
                cnt++;
                nl = true;
                continue;
            }
            bf.append(ch);
        }
        throw new IOException(bf.toString());
    }

    private static int readHexDigit() throws IOException {
        int n = 0;
        if (cur_ch >= '0' && cur_ch <= '9') n = cur_ch - '0';
        else if (cur_ch >= 'A' && cur_ch <= 'F') n = cur_ch - 'A' + 10;
        else if (cur_ch >= 'a' && cur_ch <= 'f') n = cur_ch - 'a' + 10;
        else error();
        read();
        return n;
    }
    
    private static Object readNestedObject() throws IOException {
        switch (cur_ch) {
        case '"':
            read();
            tmp_buf_pos = 0;
            for (;;) {
                if (cur_ch <= 0) error();
                if (cur_ch == '"') break;
                if (cur_ch == '\\') {
                    read();
                    if (cur_ch <= 0) error();
                    switch (cur_ch) {
                    case '"':
                    case '\\':
                    case '/':
                        break;
                    case 'b':
                        cur_ch = '\b';
                        break;
                    case 'f':
                        cur_ch = '\f';
                        break;
                    case 'n':
                        cur_ch = '\n';
                        break;
                    case 'r':
                        cur_ch = '\r';
                        break;
                    case 't':
                        cur_ch = '\t';
                        break;
                    case 'u':
                        read();
                        int n = 0;
                        n |= readHexDigit() << 12;
                        n |= readHexDigit() << 8;
                        n |= readHexDigit() << 4;
                        n |= readHexDigit();
                        write((char)n);
                        continue;
                    default:
                        error();
                        break;
                    }
                }
                if (tmp_buf_pos >= tmp_buf.length) {
                    write((char)cur_ch);
                }
                else {
                    tmp_buf[tmp_buf_pos++] = (char)cur_ch;
                }
                if (cur_buf_pos >= cur_buf_len) {
                    read();
                }
                else {
                    cur_ch = cur_buf[cur_buf_pos++];
                    err_buf[err_buf_pos++] = (char)cur_ch;
                    if (err_buf_pos >= err_buf.length) {
                        err_buf_pos = 0;
                        err_buf_cnt++;
                    }
                }
            }
            read();
            return new String(tmp_buf, 0, tmp_buf_pos);
        case '[':
            Collection<Object> l = new ArrayList<Object>();
            read();
            if (cur_ch <= 0) error();
            if (cur_ch != ']') {
                for (;;) {
                    l.add(readNestedObject());
                    if (cur_ch == ']') break;
                    if (cur_ch != ',') error();
                    read();
                }
            }
            read();
            return new ReadOnlyCollection<Object>(l);
        case '{':
            Map<String,Object> m = new HashMap<String,Object>();
            read();
            if (cur_ch <= 0) error();
            if (cur_ch != '}') {
                for (;;) {
                    String key = (String)readNestedObject();
                    if (cur_ch != ':') error();
                    read();
                    Object val = readNestedObject();
                    m.put(key, val);
                    if (cur_ch == '}') break;
                    if (cur_ch != ',') error();
                    read();
                }
            }
            read();
            return new ReadOnlyMap<String,Object>(m);
        case 'n':
            read();
            if (cur_ch != 'u') error();
            read();
            if (cur_ch != 'l') error();
            read();
            if (cur_ch != 'l') error();
            read();
            return null;
        case 'f':
            read();
            if (cur_ch != 'a') error();
            read();
            if (cur_ch != 'l') error();
            read();
            if (cur_ch != 's') error();
            read();
            if (cur_ch != 'e') error();
            read();
            return Boolean.FALSE;
        case 't':
            read();
            if (cur_ch != 'r') error();
            read();
            if (cur_ch != 'u') error();
            read();
            if (cur_ch != 'e') error();
            read();
            return Boolean.TRUE;
        default:
            boolean neg = cur_ch == '-';
            if (neg) read();
            if (cur_ch >= '0' && cur_ch <= '9') {
                // TODO: float number
                int v = 0;
                while (v <= 0x7fffffff / 10 - 1) {
                    v = v * 10 + (cur_ch - '0');
                    read();
                    if (cur_ch < '0' || cur_ch > '9') {
                        return new Integer(neg ? -v : v);
                    }
                }
                long vl = v;
                while (vl < 0x7fffffffffffffffl / 10 - 1) {
                    vl = vl * 10 + (cur_ch - '0');
                    read();
                    if (cur_ch < '0' || cur_ch > '9') {
                        return new Long(neg ? -vl : vl);
                    }
                }
                StringBuffer sb = new StringBuffer();
                if (neg) sb.append('-');
                sb.append(vl);
                while (true) {
                    sb.append(cur_ch);
                    read();
                    if (cur_ch < '0' || cur_ch > '9') {
                        return new BigInteger(sb.toString());
                    }
                }
            }
            error();
            return null;
        }
    }

    private static Object readObject() throws IOException {
        Object o = readNestedObject();
        if (cur_ch >= 0) error();
        return o;
    }

    private static Object[] readSequence() throws IOException {
        List<Object> l = new ArrayList<Object>();
        while (cur_ch >= 0) {
            if (cur_ch == 0) l.add(null);
            else l.add(readNestedObject());
            if (cur_ch != 0) error("missing \\0 terminator");
            read();
        }
        return l.toArray();
    }
    
    /**
     * Write an object into JSON output buffer.
     * Clients should not call this method directly, except from ObjectWriter implementation.
     * @param o - an object to write
     */
    @SuppressWarnings("unchecked")
    public static void writeObject(Object o) throws IOException {
        if (o == null) {
            write("null");
        }
        else if (o instanceof Boolean) {
            write(o.toString());
        }
        else if (o instanceof Number) {
            write(o.toString());
        }
        else if (o instanceof String) {
            String s = (String)o;
            char[] arr = new char[s.length()];
            s.getChars(0, arr.length, arr, 0);
            writeObject(arr);
        }
        else if (o instanceof char[]) {
            char[] s = (char[])o;
            write('"');
            int l = s.length;
            for (int i = 0; i < l; i++) {
                char ch = s[i];
                switch (ch) {
                case 0:
                    write("\\u0000");
                    break;
                case '\r':
                    write("\\r");
                    break;
                case '\n':
                    write("\\n");
                    break;
                case '\t':
                    write("\\t");
                    break;
                case '\b':
                    write("\\b");
                    break;
                case '\f':
                    write("\\f");
                    break;
                case '"':
                case '\\':
                    write('\\');
                default:
                    if (tmp_buf_pos >= tmp_buf.length) write(ch);
                    else tmp_buf[tmp_buf_pos++] = ch;
                }
            }
            write('"');
        }
        else if (o instanceof byte[]) {
            write('[');
            byte[] arr = (byte[])o;
            boolean comma = false;
            for (int i = 0; i < arr.length; i++) {
                if (comma) write(',');
                writeUInt(arr[i] & 0xff);
                comma = true;
            }
            write(']');
        }
        else if (o instanceof Object[]) {
            write('[');
            Object[] arr = (Object[])o;
            boolean comma = false;
            for (int i = 0; i < arr.length; i++) {
                if (comma) write(',');
                writeObject(arr[i]);
                comma = true;
            }
            write(']');
        }
        else if (o instanceof Collection) {
            write('[');
            boolean comma = false;
            for (Iterator<Object> i = ((Collection<Object>)o).iterator(); i.hasNext();) {
                if (comma) write(',');
                writeObject(i.next());
                comma = true;
            }
            write(']');
        }
        else if (o instanceof Map) {
            Map<String,Object> map = (Map<String,Object>)o;
            write('{');
            boolean comma = false;
            for (Iterator<Map.Entry<String,Object>> i = map.entrySet().iterator(); i.hasNext();) {
                if (comma) write(',');
                Map.Entry<String,Object> e = i.next();
                writeObject(e.getKey());
                write(':');
                writeObject(e.getValue());
                comma = true;
            }
            write('}');
        }
        else {
            ObjectWriter writer = object_writers.get(o.getClass());
            if (writer != null) {
                writer.write(o);
            }
            else {
                throw new IOException("JSON: unsupported object type");
            }
        }
    }

    private static byte[] toBytes() throws UnsupportedEncodingException {
        int inp_pos = 0;
        int out_pos = 0;
        while (inp_pos < tmp_buf_pos) {
            if (out_pos >= tmp_bbf.length - 4) {
                byte[] tmp = new byte[tmp_bbf.length * 2];
                System.arraycopy(tmp_bbf, 0, tmp, 0, out_pos);
                tmp_bbf = tmp;
            }
            int ch = tmp_buf[inp_pos++];
            if (ch < 0x80) {
                tmp_bbf[out_pos++] = (byte)ch; 
            }
            else if (ch < 0x800) {
                tmp_bbf[out_pos++] = (byte)((ch >> 6) | 0xc0); 
                tmp_bbf[out_pos++] = (byte)(ch & 0x3f | 0x80); 
            }
            else if (ch < 0x10000) {
                tmp_bbf[out_pos++] = (byte)((ch >> 12) | 0xe0); 
                tmp_bbf[out_pos++] = (byte)((ch >> 6) & 0x3f | 0x80); 
                tmp_bbf[out_pos++] = (byte)(ch & 0x3f | 0x80); 
            }
            else {
                tmp_bbf[out_pos++] = (byte)((ch >> 18) | 0xf0); 
                tmp_bbf[out_pos++] = (byte)((ch >> 12) & 0x3f | 0x80); 
                tmp_bbf[out_pos++] = (byte)((ch >> 6) & 0x3f | 0x80); 
                tmp_bbf[out_pos++] = (byte)(ch & 0x3f | 0x80); 
            }
        }
        byte[] res = new byte[out_pos];
        System.arraycopy(tmp_bbf, 0, res, 0, out_pos);
        return res;
    }

    public static String toJSON(Object o) throws IOException {
        assert Protocol.isDispatchThread();
        tmp_buf_pos = 0;
        writeObject(o);
        return new String(tmp_buf, 0, tmp_buf_pos);
    }

    public static byte[] toJASONBytes(Object o) throws IOException {
        assert Protocol.isDispatchThread();
        tmp_buf_pos = 0;
        writeObject(o);
        return toBytes();
    }

    public static byte[] toJSONSequence(Object[] o) throws IOException {
        assert Protocol.isDispatchThread();
        if (o == null || o.length == 0) return null;
        tmp_buf_pos = 0;
        for (int i = 0; i < o.length; i++) {
            writeObject(o[i]);
            write((char)0);
        }
        return toBytes();
    }

    public static Object parseOne(String s) throws IOException {
        assert Protocol.isDispatchThread();
        if (s.length() == 0) return null;
        reader = new StringReader(s);
        err_buf_pos = 0;
        err_buf_cnt = 0;
        cur_buf_pos = 0;
        cur_buf_len = 0;
        read();
        return readObject();
    }

    public static Object parseOne(byte[] b) throws IOException {
        assert Protocol.isDispatchThread();
        if (b.length == 0) return null;
        reader = new InputStreamReader(new ByteArrayInputStream(b), "UTF8");
        err_buf_pos = 0;
        err_buf_cnt = 0;
        cur_buf_pos = 0;
        cur_buf_len = 0;
        read();
        return readObject();
    }

    public static Object[] parseSequence(byte[] b) throws IOException {
        assert Protocol.isDispatchThread();
        reader = new InputStreamReader(new ByteArrayInputStream(b), "UTF8");
        err_buf_pos = 0;
        err_buf_cnt = 0;
        cur_buf_pos = 0;
        cur_buf_len = 0;
        read();
        return readSequence();
    }
}
