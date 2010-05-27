/*******************************************************************************
 * Copyright (c) 2009, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.services.remote;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import org.eclipse.tm.tcf.core.Command;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.protocol.JSON;
import org.eclipse.tm.tcf.services.IPathMap;

public class PathMapProxy implements IPathMap {

    private final IChannel channel;

    private static class MapRule implements PathMapRule {

        final Map<String,Object> props;

        MapRule(Map<String,Object> props) {
            this.props = props;
        }

        public Map<String,Object> getProperties() {
            return props;
        }

        public String getID() {
            return (String)props.get(PROP_ID);
        }

        public String getSource() {
            return (String)props.get(PROP_SOURCE);
        }

        public String getDestination() {
            return (String)props.get(PROP_DESTINATION);
        }

        public String getHost() {
            return (String)props.get(PROP_HOST);
        }

        public String getProtocol() {
            return (String)props.get(PROP_PROTOCOL);
        }

        public String toString() {
            return props.toString();
        }
    }

    public PathMapProxy(IChannel channel) {
        this.channel = channel;
    }

    public String getName() {
        return NAME;
    }

    public IToken get(final DoneGet done) {
        return new Command(channel, this, "get", null) {
            @Override
            public void done(Exception error, Object[] args) {
                PathMapRule[] map = null;
                if (error == null) {
                    assert args.length == 2;
                    error = toError(args[0]);
                    map = toPathMap(args[1]);
                }
                done.doneGet(token, error, map);
            }
        }.token;
    }

    public IToken set(PathMapRule[] map, final DoneSet done) {
        return new Command(channel, this, "set", new Object[]{ map }) {
            @Override
            public void done(Exception error, Object[] args) {
                if (error == null) {
                    assert args.length == 1;
                    error = toError(args[0]);
                }
                done.doneSet(token, error);
            }
        }.token;
    }

    @SuppressWarnings("unchecked")
    private PathMapRule[] toPathMap(Object o) {
        if (o == null) return null;
        int i = 0;
        Collection<Object> c = (Collection<Object>)o;
        PathMapRule[] map = new PathMapRule[c.size()];
        for (Object x : c) map[i++] = toPathMapRule(x);
        return map;
    }

    @SuppressWarnings("unchecked")
    private PathMapRule toPathMapRule(Object o) {
        if (o == null) return null;
        return new MapRule((Map<String,Object>)o);
    }

    static {
        JSON.addObjectWriter(PathMapRule.class, new JSON.ObjectWriter<PathMapRule>() {
            public void write(PathMapRule r) throws IOException {
                JSON.writeObject(r.getProperties());
            }
        });
    }
}
