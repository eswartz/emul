/*
  SymbolsToConstants.java

  (c) 2014 Ed Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tools.forthcomp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ejs.base.utils.HexUtils;

/**
 * Convert the BSS/equates and their values from a previous assembly step into
 * constants that Forth can use.
 * @author ejs
 *
 */
public class SymbolsToConstants {

	private static final String PROGNAME = SymbolsToConstants.class.getSimpleName();

	public static void main(String[] args) throws Exception {

		if (args.length < 3) {
			System.err.println("Run as: " + PROGNAME + " input-asm-file input-list-file output-constants-file [...]");
			System.exit(1);
		}
		
		for (int idx = 0; idx < args.length; idx += 3) {
		
			File inputFile = new File(args[idx+0]);
			if (!inputFile.exists()) {
				System.err.println("Could not find " + inputFile);
				System.exit(1);
			}
			File outputConstantsFile = new File(args[idx+2]);
			outputConstantsFile.delete();
			
			File inputSymbolFile = new File(args[idx+1]);
			if (!inputSymbolFile.exists()) {
				System.err.println("Could not find " + inputSymbolFile);
				System.exit(1);
			}
			
			System.out.println("Reading " + inputFile);
			Map<String, String> symbols = readSymbols(inputFile);
			
			System.out.println("Matching against " + inputSymbolFile);
			Map<String, Integer> values = findSymbolValues(inputSymbolFile, symbols);
			if (values.isEmpty()) {
				System.err.println("No BSS/equates found (bad assembly?) in " + inputSymbolFile);
				System.exit(1);
			}
	
			System.out.println("Writing " + outputConstantsFile);
			writeConstants(inputFile, inputSymbolFile, outputConstantsFile,
					values);
		}
	}

	protected static Map<String, String> readSymbols(File inputEquateFile)
			throws FileNotFoundException, IOException {
		Map<String, String> equateSymbols = new HashMap<String, String>();
		
		Pattern equPattern = Pattern.compile("(?i)^(\\S+)\\s+(?:equ|bss)\\s+.*?(?:FORTH:\\s+(\\S+).*)?");
		BufferedReader reader = new BufferedReader(new FileReader(inputEquateFile));
		
		String line;
		while ((line = reader.readLine()) != null) {
			Matcher m = equPattern.matcher(line);
			if (m.matches()) {
				String asmName = m.group(1);
				String forthName = asmName;
				if (m.group(2) != null)
					forthName = m.group(2);
				equateSymbols.put(asmName, forthName);
			}
		}
		reader.close();
		return equateSymbols;
	}
	
	protected static Map<String, Integer> findSymbolValues(File inputSymbolFile, Map<String, String> symbols)
			throws FileNotFoundException, IOException {
		
		Map<String, Integer> constants = new HashMap<String, Integer>();
		Pattern equPattern = Pattern.compile("^(\\S+)\\s+(\\S+)");
		BufferedReader reader = new BufferedReader(new FileReader(inputSymbolFile));
		
		String line;
		while ((line = reader.readLine()) != null) {
			Matcher m = equPattern.matcher(line);
			if (m.matches()) {
				int value = Integer.parseInt(m.group(1), 16);
				String name = m.group(2);
				
				String forthName = symbols.remove(name); 
				if (forthName != null) {
					constants.put(forthName, value);
				}
			}
		}
		reader.close();
		
		
		if (!symbols.isEmpty()) {
			System.err.println("Not all symbols had values:");
			for (String sym : symbols.keySet()) {
				System.err.println("\t"+sym);
			}
		}

		return constants;
	}
	
	protected static void writeConstants(File inputEquateFile,
			File inputSymbolFile, File outputConstantsFile,
			Map<String, Integer> equates) throws IOException {
		Writer writer = new BufferedWriter(new FileWriter(outputConstantsFile));
		writer.write(MessageFormat
				.format("\\ This file was automatically generated from {0} and {1} by {2}; do not modify!\n\n",
						inputEquateFile.getName(), inputSymbolFile.getName(), PROGNAME));
		
		List<Integer> values = new ArrayList<Integer>(equates.values());
		Collections.sort(values);
		
		// very slow loop, but few very constants expected
		for (Integer value : values) {
			for (Iterator<Entry<String, Integer>> iterator = equates.entrySet()
					.iterator(); iterator.hasNext();) {
				Map.Entry<String, Integer> equ = iterator.next();
				if (equ.getValue() == value) {
					writer.write(MessageFormat.format("${0}\tConstant\t{1}\n", HexUtils.toHex4(equ.getValue()),
							equ.getKey()));
					iterator.remove();
				}
			}
		}
		writer.close();
	}


}
