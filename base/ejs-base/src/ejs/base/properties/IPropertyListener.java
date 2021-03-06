/*
  IPropertyListener.java

  (c) 2010-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package ejs.base.properties;

/**
 * @author ejs
 *
 */
public interface IPropertyListener {
	/** The property changed: either value, hidden state, ... */
	void propertyChanged(IProperty property);
}
