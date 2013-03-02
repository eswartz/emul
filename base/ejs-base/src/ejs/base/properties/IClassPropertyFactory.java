/*
  IClassPropertyFactory.java

  (c) 2010-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package ejs.base.properties;

/**
 * This is a factory for properties based on classes, which provides
 * the ability to serialize and deserialize them. 
 * @author ejs
 *
 */
public interface IClassPropertyFactory {

	/** Get the identifier used to serialize the object */
	String getId(Object value);
	/** Create an instance of the object identified by the id when deserializing the object */
	Object create(String id);
	/**
	 * 
	 */
	String[] getIds();

}
