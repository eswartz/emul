/**
 * 
 */
package v9t9.emulator.clients.builtin.swt.debugger;

/**
 * @author ejs
 *
 */
public class InstRow {

	private final String addr;
	private final String inst;

	private static int gCounter;
	private final int count = gCounter++;
	/**
	 * @param addr
	 * @param inst
	 */
	public InstRow(String addr, String inst) {
		this.addr = addr;
		this.inst = inst;
	}

	/**
	 * @return
	 */
	public String getAddress() {
		return addr;
	}

	/**
	 * @return
	 */
	public String getInst() {
		return inst;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((addr == null) ? 0 : addr.hashCode());
		result = prime * result + ((inst == null) ? 0 : inst.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		InstRow other = (InstRow) obj;
		if (count != other.count) {
			return false;
		}
		if (addr == null) {
			if (other.addr != null) {
				return false;
			}
		} else if (!addr.equals(other.addr)) {
			return false;
		}
		if (inst == null) {
			if (other.inst != null) {
				return false;
			}
		} else if (!inst.equals(other.inst)) {
			return false;
		}
		return true;
	}

	
}
