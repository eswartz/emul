/**
 * 
 */
package v9t9.common.cpu;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author ejs
 *
 */
public class CycleCounts {
	private AtomicInteger fetch = new AtomicInteger();
	private AtomicInteger load = new AtomicInteger();
	private AtomicInteger store = new AtomicInteger();
	private AtomicInteger execute = new AtomicInteger();
	private AtomicInteger overhead = new AtomicInteger();
	
	public CycleCounts() {
	}

	public void addFetch(int cycles) {
		fetch.addAndGet(cycles);
	}
	
	public void addLoad(int cycles) {
		load.addAndGet(cycles);
	}
	
	public void addStore(int cycles) {
		store.addAndGet(cycles);
	}
	
	public void addExecute(int cycles) {
		execute.addAndGet(cycles);
	}
	
	public void addOverhead(int cycles) {
		overhead.addAndGet(cycles);
	}
	
	public int getAndResetTotal() {
		return fetch.getAndSet(0) + load.getAndSet(0) 
				+ store.getAndSet(0) + execute.getAndSet(0)
				+ overhead.getAndSet(0);
	}

	public int getTotal() {
		return fetch.get() + load.get() + store.get() + execute.get() + overhead.get();
	}
	
	public int getFetch() {
		return fetch.get();
	}
	public int getLoad() {
		return load.get();
	}
	public int getStore() {
		return store.get();
	}
	public int getExecute() {
		return execute.get();
	}
	public int getOverhead() {
		return overhead.get();
	}

	/**
	 * 
	 */
	public void moveLoadToFetch() {
		int loadC = load.getAndSet(0);
		fetch.addAndGet(loadC);
	}

	/**
	 * @return
	 */
	public int getAndResetLoad() {
		return load.getAndSet(0);
	}
}
