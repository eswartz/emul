/**
 * 
 */
package v9t9.engine.memory;

import v9t9.utils.Utils;

/**
 * This entry is used to provide flattened views of banked entries
 * for the debugger
 * @author Ed
 *
 */
public class BankedMemoryProxyEntry extends MemoryEntry {

	private final int bank;

	public BankedMemoryProxyEntry(BankedMemoryEntry banked, int bank) {
		super(banked.getName() + " #" +bank,
				banked.domain,
				banked.addr,
				banked.size,
				banked.area);
		this.bank = bank;
		addrOffset = bank * banked.getBankSize();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof WindowBankedMemoryEntry) {
			WindowBankedMemoryEntry entry = (WindowBankedMemoryEntry) obj;
			return  entry.area == area &&
			entry.getCurrentBank() == bank; 
		}
		return super.equals(obj);
	}
}
