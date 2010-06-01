/**
 * 
 */
package org.ejs.eulang.test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.ejs.eulang.llvm.tms9900.DataBlock;
import org.ejs.eulang.llvm.tms9900.app.Simulator;
import org.ejs.eulang.test.SimulationTestCase.SimulationRunnable;

/**
 * @author ejs
 *
 */
public class Test9900Simulation  {

	public static TestSuite suite()  {
	
		DebuggableTestSuite suite = new DebuggableTestSuite();
		
		try {
			addSuite(suite, "00_simple.txt");
			addSuite(suite, "01_arrays.txt");
			addSuite(suite, "02_tuples.txt");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		return suite;
	}

	enum State {
		OUTSIDE_TEST,
		TEST_SOURCE,
		ACTIONS,
	}
	private static void addSuite(DebuggableTestSuite suite, String fname) throws IOException {
		InputStream is = Test9900Simulation.class.getResourceAsStream("tests/" + fname);
		InputStreamReader ir = new InputStreamReader(is);
		BufferedReader reader = new BufferedReader(ir);
		State state = State.OUTSIDE_TEST;

		StringBuilder source = new StringBuilder();
		List<SimulationRunnable> prereqs = new ArrayList<SimulationRunnable>();
		List<SimulationRunnable> tests = new ArrayList<SimulationRunnable>();
		String routineName = null;
		int startLine = 0;
		String comment = "";
		
		boolean only = false;
		boolean skip = false;
		
		String line;
		int lineNum = 0;
		
		while ((line = reader.readLine()) != null) {
			line = line.trim();
			lineNum++;
			if (state == State.OUTSIDE_TEST) {
				if (line.isEmpty())
					continue;
				if (line.startsWith("<<<")) {
					state = State.TEST_SOURCE;
					source.setLength(0);
					prereqs.clear();
					tests.clear();
					comment = line.substring(3).trim();
					routineName = null;
					startLine = lineNum;
					continue;
				}
				else if (line.equals("only")) {
					only = true;
					suite.setOnlyOneTest(true);
					Enumeration<Test> testEnum = suite.tests();
					while (testEnum.hasMoreElements()) {
						Test test = testEnum.nextElement();
						if (test instanceof DebuggableTest)
							((DebuggableTest) test).setSkipping(true);
					}
					continue;
				}
				else if (line.equals("skip")) {
					skip = true;
					continue;
				}
				throw new IOException("unexpected: " + line);
			}
			else if (state == State.TEST_SOURCE) {
				if (line.equals("===")) {
					state = State.ACTIONS;
					continue;
				} 
				
				source.append(line);
				source.append('\n');
			}
			else if (state == State.ACTIONS) {
				if (line.isEmpty())
					continue;
				if (line.equals(">>>")) {
					addTestCase(suite, suite.isOnlyOneTest() || skip, only, fname, startLine, comment, source, 
							prereqs.toArray(new SimulationRunnable[prereqs.size()]),
							routineName,
							tests.toArray(new SimulationRunnable[tests.size()]));
					state = State.OUTSIDE_TEST;
					only = false;
					continue;
				}
				
				String[] tokens = line.split("\\s+");
				if ("write".equals(tokens[0]) || "assert".equals(tokens[0])) {
					int idx = 1;
					boolean byteToken = "byte".equals(tokens[idx]);
					boolean wordToken = "word".equals(tokens[idx]);
					
					final boolean isByte = byteToken && !wordToken;
					if (byteToken || wordToken)
						idx++;
					
					final String symbol = tokens[idx++];
					final boolean isReg = symbol.matches("R\\d+");
					
					int offs_ = 0;
					if (!isReg) {
						if ("+".equals(tokens[idx])) {
							idx++;
							offs_ = parseInt(tokens[idx++]);
						}
					} else {
						offs_ = parseInt(symbol.substring(1)) * 2;
					}
						
					final int offs = offs_;
					
					
					if ("write".equals(tokens[0])) {
						// setup
						final int val = parseInt(tokens[idx++]);
						SimulationRunnable run = new SimulationRunnable() {
							@Override
							public void run(Simulator sim)
									throws Exception {
								short addr;
								if (!isReg) {
									DataBlock dataBlock = sim.getBuildOutput().lookupDataBlock(symbol);
									assertNotNull(dataBlock);
									addr = (short) (sim.getAddress(dataBlock.getName()) + offs);
								} else {
									addr = (short) (sim.getCPU().getWP() + offs);
								}
								if (isByte)
									sim.getMemory().writeByte(addr, (byte) val);
								else
									sim.getMemory().writeWord(addr, (short) val);
							}
						};
						prereqs.add(run);
					} else if ("assert".equals(tokens[0])) {
						// test
						boolean equalToken = "==".equals(tokens[idx]);
						boolean notEqualToken = "!=".equals(tokens[idx]);
						final boolean isEquals = equalToken && !notEqualToken;
						
						if (equalToken || notEqualToken)
							idx++;
						else
							throw new IOException(tokens[idx]+": unknown test");
						
						final int exp = parseInt(tokens[idx++]);
						
						SimulationRunnable run = new SimulationRunnable() {
							@Override
							public void run(Simulator sim) throws Exception {
								short addr;
								if (!isReg) {
									DataBlock dataBlock = sim.getBuildOutput().lookupDataBlock(symbol);
									assertNotNull("Failed to find data: " + symbol, dataBlock);
									addr = (short) (sim.getAddress(dataBlock.getName()) + offs);
								} else {
									addr = (short) (sim.getCPU().getWP() + offs);
								}
								
								int val;
								int exp_;
								if (isByte) {
									val = sim.getMemory().readByte(addr) & 0xff;
									exp_ = exp & 0xff;
								}
								else {
									val = sim.getMemory().readWord(addr) & 0xffff;
									exp_ = exp & 0xffff;
								}
								
								if (isEquals)
									assertEquals(exp + " != " + val, exp_, val);
								else
									assertFalse(exp_ == val);
							}
						};
						tests.add(run);
						
					}
					
				}
				else if ("call".equals(tokens[0])) {
					routineName = tokens[1];
				}
				else {
					throw new IOException("unknown command: " + tokens[0]);
				}
			}
		}
		
		if (state != State.OUTSIDE_TEST)
			throw new IOException("truncated test " + fname + ":" + comment);
	}
	
	private static int parseInt(String string) {
		int radix = 10;
		if (string.toLowerCase().startsWith("0x")) {
			string = string.substring(2);
			radix = 16;
		}
		return Integer.parseInt(string, radix);
	}

	private static void addTestCase(TestSuite suite, boolean skipping, boolean only, String fname, int line,
			String comment, StringBuilder source, SimulationRunnable[] prereqs, String routineName,
			SimulationRunnable[] tests) {
		suite.addTest(new SimulationTestCase(fname + ":" + line, comment, source.toString(), skipping && !only, only, 
				prereqs, routineName, tests));
	}
	
	
}
