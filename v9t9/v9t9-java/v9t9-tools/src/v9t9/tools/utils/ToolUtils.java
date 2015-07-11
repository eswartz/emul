/*
  ToolUtils.java

  (c) 2013-2014 Ed Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tools.utils;

import java.io.File;
import java.io.IOException;
import java.util.List;

import v9t9.common.asm.RawInstruction;
import v9t9.common.files.NativeFile;
import v9t9.common.files.NativeFileFactory;
import v9t9.common.machine.IMachine;
import v9t9.common.memory.IMemoryDomain;
import v9t9.common.memory.IMemoryEntry;
import v9t9.engine.memory.NativeFileMemoryEntry;
import v9t9.server.EmulatorLocalServer;
import v9t9.server.client.EmulatorServerBase;
import ejs.base.utils.HexUtils;
import ejs.base.utils.Pair;

/**
 * @author ejs
 *
 */
public class ToolUtils {

	public static IMachine createMachine() {
		return createMachine(null);
	}
	public static IMachine createMachine(String modelId) {
		
		EmulatorServerBase server = new EmulatorLocalServer();
		if (modelId == null)
			modelId = server.getMachineModelFactory().getDefaultModel();
		IMachine machine = createMachine(server, modelId);
		return machine;
	}

	/**
	 * @param modelId
	 * @return
	 */
	public static IMachine createMachine(EmulatorServerBase server, String modelId) {
		if (modelId == null)
			modelId = server.getMachineModelFactory().getDefaultModel();
		try {
			server.init(modelId);
		} catch (IOException e) {
			System.err.println("Failed to contact or create server:" + modelId);
			e.printStackTrace();
			System.exit(23);
			return null;
		}
		return server.getMachine();
	}


	/**
	 * @param machine
	 * @param name
	 */
	public static void loadMemoryImage(IMachine machine, String name, List<Pair<Integer, Integer>> codeRanges) throws IOException {
		int loadNext = 0;
		do {
			int size = 0;
			int addr = 0;
			System.err.println("loading " + name);
			NativeFile file = NativeFileFactory.INSTANCE.createNativeFile(new File(name));
			byte[] contents = new byte[file.getFileSize()];
			file.readContents(contents, 0, 0, contents.length);
			if (contents.length < 6) {
				throw new IOException("not enough data for memory image header");
			}
			loadNext = ((contents[0] & 0xff) << 8) | (contents[1] & 0xff);
			size = ((contents[2] & 0xff) << 8) | (contents[3] & 0xff);
			addr = ((contents[4] & 0xff) << 8) | (contents[5] & 0xff);
			if (!((addr >= 0x2000 && addr < 0x4000) || (addr >= 0xA000 || addr <= 0xffff))) {
				throw new IOException("malformed memory image header: content not targeting RAM");
			}
			if (addr + size > 0x10000) {
				throw new IOException("malformed memory image header: addr + size > 64k");
			}
			
			IMemoryDomain console = machine.getConsole();
			IMemoryEntry ramEntry = NativeFileMemoryEntry.newMemoryFromFile(addr, name, console, file, 6); 
			console.mapEntry(ramEntry);
			
			codeRanges.add(new Pair<Integer, Integer>(addr, size));
			
			name = name.substring(0, name.length() - 1) +
					(char) (name.charAt(name.length() - 1) + 1);
		} while (loadNext != 0);
	}

	public static void loadMemory(IMachine machine, String domainName, String fileName,
			String addrStr, List<Pair<Integer, Integer>> codeRanges) throws IOException {
    	IMemoryDomain domain = machine.getMemory().getDomain(domainName);
    	if (domain == null) {
    		throw new IOException("could not resolve memory domain '"+ domainName +'"');
    	}
    	
    	File file = new File(fileName);
    	int addr = HexUtils.parseHexInt(addrStr);
    	
    	try {
    		NativeFile nativeFile = NativeFileFactory.INSTANCE.createNativeFile(file);
    		IMemoryDomain console = machine.getConsole();
			IMemoryEntry ramEntry = NativeFileMemoryEntry.newMemoryFromFile(
					addr, file.getName(), console, nativeFile, 0); 
			console.mapEntry(ramEntry);

			if (IMemoryDomain.NAME_CPU.equals(domainName)) {
				codeRanges.add(new Pair<Integer, Integer>(addr, nativeFile.getFileSize()));
			}

    	} catch (IOException e) {
    		throw new IOException("could not load memory to '"+ domainName +"' from '" + fileName +"'", e);
		}
	}

	/**
	 * @param sb
	 * @param instr
	 */
	public static void appendInstructionCode(
			IMachine machine, 
			StringBuilder sb,
			RawInstruction instr) {
		int pc;
		int maxLength = machine.getInstructionFactory().getMaxInstrLength();
		if (machine.getConsole().isWordAccess()) {
			for (pc = instr.pc; pc < instr.pc + instr.getSize(); pc += 2) {
				sb.append(HexUtils.toHex4(machine.getConsole().flatReadWord(pc))).append(' ');
			}
			while (pc < instr.pc + maxLength) {
				sb.append("     ");
				pc += 2;
			}
		} else {
			for (pc = instr.pc; pc < instr.pc + instr.getSize(); pc ++) {
				sb.append(HexUtils.toHex2(machine.getConsole().flatReadByte(pc))).append(' ');
			}
			while (pc < instr.pc + maxLength) {
				sb.append("   ");
				pc ++;
			}
		}
		
	}
}
