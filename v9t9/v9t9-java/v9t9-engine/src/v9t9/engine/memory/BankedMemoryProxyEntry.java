/*
  BankedMemoryProxyEntry.java

  (c) 2009-2011 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
 */
package v9t9.engine.memory;


/**
 * This entry is used to provide flattened views of banked entries
 * for the debugger
 * @author Ed
 *
 */
public class BankedMemoryProxyEntry extends MemoryEntry {

	private final int bank;
	private final BankedMemoryEntry banked;

	public BankedMemoryProxyEntry(BankedMemoryEntry banked, int bank) {
		super(banked.getName() + " #" +bank,
				banked.getDomain(),
				banked.getAddr(),
				banked.getSize(),
				banked.area);
		this.banked = banked;
		this.bank = bank;
		addrOffset = (bank * banked.getBankSize());
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
	
	@Override
	public boolean contains(int addr) {
		return super.contains(addr + banked.getCurrentBank() * banked.getBankSize());
	}
}
