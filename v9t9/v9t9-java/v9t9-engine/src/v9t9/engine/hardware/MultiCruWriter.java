/*
  MultiCruWriter.java

  (c) 2014 Ed Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.hardware;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ejs
 *
 */
public class MultiCruWriter implements ICruWriter {

	private List<ICruWriter> writers = new ArrayList<ICruWriter>(1);
	
	public void addWriter(ICruWriter writer) {
		writers.add(writer);
	}
	public void removeWriter(ICruWriter writer) {
		writers.remove(writer);
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.hardware.ICruWriter#write(int, int, int)
	 */
	@Override
	public int write(int addr, int data, int num) {
		for (ICruWriter writer : writers) {
			writer.write(addr, data, num);
		}
		return 0;
	}
	/**
	 * 
	 */
	public boolean removeLast() {
		if (writers.isEmpty())
			return false;
		writers.remove(writers.size() - 1);
		return true;
	}

}
