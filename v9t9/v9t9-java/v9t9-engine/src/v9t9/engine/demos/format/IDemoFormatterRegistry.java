/*
  IDemoFormatterRegistry.java

  (c) 2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.demos.format;

import v9t9.common.demos.IDemoEventFormatter;

/**
 * @author ejs
 *
 */
public interface IDemoFormatterRegistry {

	void registerDemoEventFormatter(IDemoEventFormatter formatter);

	IDemoEventFormatter findFormatterByBuffer(String bufferId);
	IDemoEventFormatter findFormatterByEvent(String eventIdentifier);

}