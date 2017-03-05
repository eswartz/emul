/*
  ControllerConfig.java

  (c) 2017 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.keyboard;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import ejs.base.utils.TextUtils;

/**
 * A configuration of how controller(s) map to the actions of a joystick.
 * 
 * This is serialized like:
 * 
 * <pre>
 *  "Fancy \"PS\" Controller!","b0",2=BUTTON
 * "Fancy \"PS\" Controller!","b1",3=BUTTON
 * "Fancy \"PS\" Controller!","b2",10=BUTTON
 * "Fancy \"PS\" Controller!","b3",11=BUTTON
 * "Fancy \"PS\" Controller!","x",8=X_AXIS
 * "Fancy \"PS\" Controller!","y",4=Y_AXIS
 * "Something Else","rx",9=X_AXIS
 * "Something Else","ry",5=Y_AXIS
 * </pre>
 * 
 * The first string is the net.java.games.input.Controller's name; the second is
 * the name of the .java.games.input.Component, and the third entry is an
 * integer telling where this item was discovered originally in its parent (in
 * case the name changes).
 * 
 * Each entry is mapped to a JoystickRole function, one of which is IGNORE for
 * cases like the Retro-Link USB joystick, which on OSX has a stuck X-axis.
 * 
 * @author ejs
 * 
 */
public class ControllerConfig {
	private static final Logger logger = Logger.getLogger(ControllerConfig.class);
	
	public static class ParseException extends Exception {
		private static final long serialVersionUID = 1L;
		
		int line;

		public ParseException(int line, String message) {
			super(message);
			this.line = line;
		}
	}

	private Map<ControllerIdentifier, JoystickRole> mapping = new TreeMap<ControllerIdentifier, JoystickRole>();


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((mapping == null) ? 0 : mapping.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ControllerConfig other = (ControllerConfig) obj;
		if (mapping == null) {
			if (other.mapping != null)
				return false;
		} else if (!mapping.equals(other.mapping))
			return false;
		return true;
	}

		
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		for (Map.Entry<ControllerIdentifier, JoystickRole> ent : mapping.entrySet()) {
			ControllerIdentifier id = ent.getKey();
			sb.append(TextUtils.quote(id.controllerName, '"'));
			sb.append(',');
			sb.append(TextUtils.quote(id.name, '"'));
			sb.append(',');
			sb.append(id.index);
			sb.append("=");
			sb.append(ent.getValue()).append('\n');
		}
		
		return sb.toString();
		
	}
	
	public void fromString(String text) throws ParseException {
		if (text == null)
			return;
		
		String[] lines = text.split(TextUtils.LINE_ENDING_PATTERN_STRING);
		int lineno = 0;
		for (String origLine : lines) {
			lineno++;
			String line = origLine.trim();
			if (line.isEmpty() || line.startsWith("#"))
				continue;

			int eq = line.lastIndexOf('=');
			if (eq < 0)
				throw new ParseException(lineno, "invalid line (no '=<role>'): " + origLine);
			
			String roleStr = line.substring(eq+1).trim();
			line = line.substring(0, eq);
			
			int comma = line.lastIndexOf(',');
			if (comma < 0)
				throw new ParseException(lineno, "invalid line (no ',<index>'): " + origLine);
			
			int index = -1;
			try {
				index = Integer.parseInt(line.substring(comma+1).trim());
			} catch (NumberFormatException e) {
				throw new ParseException(lineno, "invalid line (invalid index): " + origLine);
			}
			
			line = line.substring(0, comma);
			comma = line.lastIndexOf(',');
			if (comma < 0)
				throw new ParseException(lineno, "invalid line (no ',<name>'): " + origLine);
			
			String name = TextUtils.unescape(TextUtils.unquote(line.substring(comma+1).trim(), '"'), '"');
			
			line = line.substring(0, comma);
			String controllerName = TextUtils.unescape(TextUtils.unquote(line.trim(), '"'), '"');
			
			JoystickRole role = JoystickRole.IGNORE;
			try {
				role = JoystickRole.valueOf(roleStr);
			} catch (IllegalArgumentException e) {
				logger.error("unhandled JoystickRole: " + roleStr + ", ignoring");
			}
			
			mapping.put(new ControllerIdentifier(controllerName, index, name), role);
		}
	}

	public void clear() {
		mapping.clear();
	}
	
	public JoystickRole map(ControllerIdentifier id, JoystickRole role) {
		JoystickRole old = mapping.get(id);
		mapping.put(id, role);
		return old;
	}

	public JoystickRole find(ControllerIdentifier id) {
		JoystickRole role = mapping.get(id);
		if (role == null)
			role = JoystickRole.IGNORE;
		return role;
	}
	
	public Map<ControllerIdentifier, JoystickRole> getMap() {
		return Collections.unmodifiableMap(mapping);
	}

	public void mergeFrom(ControllerConfig other) {
		mapping.putAll(other.getMap());
	}

	/**
	 * @param id
	 */
	public void remove(ControllerIdentifier id) {
		mapping.remove(id);
	}

}
