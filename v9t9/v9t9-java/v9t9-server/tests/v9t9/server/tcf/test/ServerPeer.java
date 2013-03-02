/*
  ServerPeer.java

  (c) 2011-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.server.tcf.test;

import java.util.Map;

import org.eclipse.tm.tcf.core.AbstractPeer;

class ServerPeer extends AbstractPeer {
	ServerPeer(Map<String, String> attrs) {
		super(attrs);
	}
}