/*
  CycleCounts.java

  (c) 2013 Ed Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.cpu;

/**
 * @author ejs
 *
 */
public class CycleCounts {
	private int fetch;
	private int load;
	private int store;
	private int execute;
	private int overhead;
	
	// saved state
	private int savedFetches;
	private int savedExecutes;
	private int savedLoads;
	private int savedStores;
	private int savedOverheads;
	
	public CycleCounts() {
	}
	
	@Override
	public String toString() {
		return "CycleCounts [fetch=" + fetch + ", load=" + load  + ", store="
				+ store  + ", execute=" + execute  + ", overhead=" + overhead 
				+ "]";
	}

	public void addFetch(int cycles) {
		fetch += cycles;
	}
	
	public void addLoad(int cycles) {
		load += cycles;
	}
	
	public void addStore(int cycles) {
		store += cycles;
	}
	
	public void addExecute(int cycles) {
		execute += cycles;
	}
	
	public void addOverhead(int cycles) {
		overhead += cycles;
	}
	
	public int getAndResetTotal() {
		int total = getTotal();
		fetch = load = store = execute = overhead = 0;
		return total;
	}

	public int getTotal() {
		return fetch + load + store + execute + overhead;
	}
	
	public int getFetch() {
		return fetch;
	}
	public int getLoad() {
		return load;
	}
	public int getStore() {
		return store;
	}
	public int getExecute() {
		return execute;
	}
	public int getOverhead() {
		return overhead;
	}

	/**
	 * 
	 */
	public void moveLoadToFetch() {
		int loadC = load = 0;
		fetch += loadC;
	}

	/**
	 * @return
	 */
	public int getAndResetLoad() {
		return load = 0;
	}
	
	public void saveState() {
		this.savedFetches = fetch;
		this.savedExecutes = execute;
		this.savedLoads = load;
		this.savedStores = store;
		this.savedOverheads = overhead;
	}
	
	public void restoreState() {
		fetch = savedFetches;
		execute = savedExecutes;
		load = savedLoads;
		store = savedStores;
		overhead = savedOverheads;
	}
	
//	public CycleCounts clone() {
//		CycleCounts c = new CycleCounts();
//		copyTo(c);
//		return c;
//	}
	public void copyTo(CycleCounts c) {
		c.fetch = fetch;
		c.execute = execute;
		c.load = load;
		c.store = store;
		c.overhead = overhead;
	}
}
