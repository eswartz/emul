/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.debug.tests;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.services.IPathMap;
import org.eclipse.tm.tcf.services.IPathMap.PathMapRule;

class TestPathMap implements ITCFTest {

    private final TCFTestSuite test_suite;
    private final List<PathMapRule> map;
    private final IPathMap service;

    private final Random rnd = new Random();

    private static final String[] prop_names = {
        IPathMap.PROP_SOURCE,
        IPathMap.PROP_DESTINATION,
        IPathMap.PROP_HOST,
        IPathMap.PROP_PROTOCOL,
    };

    private int cnt = 0;

    private static class Rule implements IPathMap.PathMapRule {

        final Map<String,Object> props;

        public Rule(Map<String,Object> props) {
            this.props = props;
        }

        public Map<String,Object> getProperties() {
            return props;
        }

        public String getID() {
            return (String)props.get(IPathMap.PROP_ID);
        }

        public String getSource() {
            return (String)props.get(IPathMap.PROP_SOURCE);
        }

        public String getDestination() {
            return (String)props.get(IPathMap.PROP_DESTINATION);
        }

        public String getHost() {
            return (String)props.get(IPathMap.PROP_HOST);
        }

        public String getProtocol() {
            return (String)props.get(IPathMap.PROP_PROTOCOL);
        }

        public String toString() {
            return props.toString();
        }
    }

    TestPathMap(TCFTestSuite test_suite, IChannel channel, List<PathMapRule> map) {
        this.test_suite = test_suite;
        this.map = map;
        service = channel.getRemoteService(IPathMap.class);
    }

    public void start() {
        if (service == null) {
            exit(null);
        }
        else {
            test_map();
        }
    }

    private void test_map() {
        if (cnt >= 40) {
            if (map == null) {
                exit(null);
            }
            else {
                service.set(map.toArray(new PathMapRule[map.size()]), new IPathMap.DoneSet() {
                    public void doneSet(IToken token, Exception error) {
                        exit(error);
                    }
                });
            }
        }
        else {
            cnt++;
            final IPathMap.PathMapRule[] map_out = new IPathMap.PathMapRule[rnd.nextInt(12)];
            for (int i = 0; i < map_out.length; i++) {
                Map<String,Object> props = new HashMap<String,Object>();
                props.put(IPathMap.PROP_ID, "PM" + i);
                for (int l = 0; l < 2; l++) {
                    String nm = prop_names[rnd.nextInt(prop_names.length)];
                    StringBuffer bf = new StringBuffer();
                    int n = rnd.nextInt(1024);
                    for (int j = 0; j < n; j++) {
                        char ch = (char)(rnd.nextInt(0xfff0) + 1);
                        bf.append(ch);
                    }
                    String val = bf.toString();
                    props.put(nm, val);
                }
                map_out[i] = new Rule(props);
            }
            service.set(map_out, new IPathMap.DoneSet() {
                public void doneSet(IToken token, Exception error) {
                    if (error != null) {
                        exit(error);
                    }
                    else {
                        service.get(new IPathMap.DoneGet() {
                            public void doneGet(IToken token, Exception error, PathMapRule[] map_inp) {
                                if (error != null) {
                                    exit(error);
                                }
                                else if (map_inp == null) {
                                    exit(new Exception("PathMap.get returned null"));
                                }
                                else if (map_out.length != map_inp.length) {
                                    exit(new Exception("PathMap.get error: wrong map size"));
                                }
                                else {
                                    for (int i = 0; i < map_out.length; i++) {
                                        if (!map_equ(map_out[i].getProperties(), map_inp[i].getProperties())) {
                                            return;
                                        }
                                    }
                                    test_map();
                                }
                            }
                        });
                    }
                }
            });
        }
    }

    private boolean map_equ(Map<String,Object> x, Map<String,Object> y) {
        for (String key : x.keySet()) {
            if (!obj_equ(key, x.get(key), y.get(key))) return false;
        }
        for (String key : y.keySet()) {
            if (!obj_equ(key, x.get(key), y.get(key))) return false;
        }
        return true;
    }

    private boolean obj_equ(String nm, Object x, Object y) {
        if (x == y) return true;
        if (x != null && x.equals(y)) return true;
        exit(new Exception("PathMap.get: wrong map data, " + nm + ": " + x + " != " + y));
        return false;
    }

    private void exit(Throwable x) {
        if (!test_suite.isActive(this)) return;
        test_suite.done(this, x);
    }
}
