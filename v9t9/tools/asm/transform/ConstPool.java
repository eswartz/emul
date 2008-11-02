/**
 * 
 */
package v9t9.tools.asm.transform;

import java.util.HashMap;
import java.util.Map;

import v9t9.tools.asm.Assembler;
import v9t9.tools.asm.Symbol;
import v9t9.tools.asm.operand.hl.AddrOperand;
import v9t9.tools.asm.operand.hl.AssemblerOperand;
import v9t9.tools.asm.operand.hl.BinaryOperand;
import v9t9.tools.asm.operand.hl.NumberOperand;
import v9t9.tools.asm.operand.hl.SymbolOperand;

/**
 * This keeps track of constants referenced as #... operands in instructions.
 * <p>
 * It tries to pack constants into bytes if possible, unless a word instruction
 * also uses the value.
 * <p>
 * For instance: bytes 1, 2 will be allocated >0102
 * For bytes 1 and word >0100 will be allocated >0100 (can also hold byte 0)
 * For word >0001 and >0100, these are two words.
 * @author Ed
 *
 */
public class ConstPool {
	private static int count;
	
	private Map<Integer, Integer> constWordMap = new HashMap<Integer, Integer>();
	private Symbol tableAddr;
	private int tableSize;
	private boolean lastAccessWasByte;
	
	public ConstPool() {
	}
	
	public void clear() {
		constWordMap.clear();
		tableAddr = null;
		tableSize = 0;
		lastAccessWasByte = false;
	}

	public int allocateByte(int value) {
		value &= 0xff;
		
		// see if we have an entry in a low or high part of word
		for (Map.Entry<Integer, Integer> entry : constWordMap.entrySet()) {
			if ((entry.getKey() & 0xff00) >> 8 == value)
				return entry.getValue();
			if ((entry.getKey() & 0x00ff) == value)
				return entry.getValue() + 1;
		}
		
		// else, register a byte
		
		// if we last registered a byte, and no words access it yet,
		// we can put the new byte there
		if ((tableSize & 1) == 1 && lastAccessWasByte) {
			// find the word for this
			for (Map.Entry<Integer, Integer> entry : constWordMap.entrySet()) {
				if (entry.getValue() == tableSize - 1) {
					constWordMap.remove(entry.getKey());
					constWordMap.put(entry.getKey() | value, tableSize - 1);
					lastAccessWasByte = true;
					return tableSize++;
				}
			}
		}
		
		// bump size if necessary
		if ((tableSize & 1) == 1)
			tableSize++;
		
		int addr = tableSize;
		constWordMap.put(value << 8, addr);
		tableSize++;
		lastAccessWasByte = true;
		
		return addr;
	}
	
	public int allocateWord(int value) {
		value &= 0xffff;
		
		// see if we have an entry as a word
		Integer offset = constWordMap.get(value);
		if (offset != null) {
			if (offset == tableSize - 2)
				lastAccessWasByte = false;
			return offset;
		}
			
		// else, register a word 
		if ((tableSize & 1) == 1) {
			tableSize++;
			
		}
		int addr = tableSize;
		tableSize += 2;
		constWordMap.put(value, addr);
		
		lastAccessWasByte = false;
		return addr;
	}

	public Symbol getTableAddr() {
		return tableAddr;
	}

	/**
	 * Get @@table+offset
	 * @param assembler TODO
	 * @param value
	 * @return
	 */
	public AssemblerOperand createTableOffset(Assembler assembler, int value) {
		if (tableAddr == null)
			tableAddr = assembler.getSymbolTable().createSymbol("$consttable" + count++);
		
		return new AddrOperand(new BinaryOperand('+', new SymbolOperand(tableAddr), new NumberOperand(value)));
	}

	public byte[] getBytes() {
		byte[] bytes = new byte[(tableSize + 1) & 0xfffe];
		for (Map.Entry<Integer, Integer> entry : constWordMap.entrySet()) {
			int addr = entry.getValue();
			int key = entry.getKey();
			bytes[addr] = (byte) (key >> 8);
			bytes[addr+1] = (byte) (key & 0xff);
		}
		return bytes;
	}
}
