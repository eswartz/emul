/*
  ListPropertyFactory.java

  (c) 2010-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package ejs.base.properties;

import java.util.ArrayList;
import java.util.LinkedList;


/**
 * @author ejs
 *
 */
public class ListPropertyFactory implements IClassPropertyFactory {

	private static ListPropertyFactory instance = new ListPropertyFactory();
	
	private static final String ARRAY= "arrayList";
	private static final String LINKED  = "linkedList";

	/* (non-Javadoc)
	 * 
	 */
	public Object create(String id) {
		if (LINKED.equals(id) || id.length() == 0)
			return new LinkedList<Object>();
		if (ARRAY.equals(id))
			return new ArrayList<Object>();
		return null;
	}

	/* (non-Javadoc)
	 * 
	 */
	public String getId(Object value) {
		if (value instanceof ArrayList)
			return ARRAY;
		if (value instanceof LinkedList)
			return LINKED;
		return null;
	}

	/* (non-Javadoc)
	 * 
	 */
	public String[] getIds() {
		return new String[] { ARRAY, LINKED };
	}

	/**
	 * @return
	 */
	public static IClassPropertyFactory getInstance() {
		return instance;
	}

}
