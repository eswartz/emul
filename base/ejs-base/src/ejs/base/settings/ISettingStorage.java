/*
  ISettingStorage.java

  (c) 2010-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package ejs.base.settings;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Interface for reading and writing settings to files
 * @author ejs
 *
 */
public interface ISettingStorage {
	ISettingSection load(InputStream inputStream) throws IOException;
	void save(OutputStream outputStream, ISettingSection section) throws IOException;
}
