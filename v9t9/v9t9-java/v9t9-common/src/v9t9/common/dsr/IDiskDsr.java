/*
  IDiskDsr.java

  (c) 2011-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.dsr;

import java.io.IOException;

import ejs.base.properties.IProperty;

import v9t9.common.files.Catalog;

/**
 * @author ejs
 *
 */
public interface IDiskDsr {
	boolean isImageBased();
	Catalog getCatalog(IProperty diskSetting) throws IOException;
}
