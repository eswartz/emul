/**
 * 
 */
package com.silabs.jenkins;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Filter the lengthy ant build log for errors and issues of interest.
 * @author edswartz
 *
 */
public class FilterLog {

	/**
	 * 
	 */
	public FilterLog() {
	}

	public static void main(String[] args) throws IOException {
		if (args.length < 2) {
			System.err.println("Run as: " + FilterLog.class.getName() + " <infile> <outfile>");
			System.exit(1);
		}
		
		BufferedReader in = new BufferedReader(new FileReader(new File(args[0])));
		PrintStream out = args[1].equals("-") ? System.out : new PrintStream(new File(args[1]));
		
		new FilterLog().process(in, out);
		
		out.close();
		in.close();
	}

	private static Pattern MKDIR_FOR_PLUGIN_LINE = Pattern.compile(".*\\[mkdir\\].*(?:/|\\\\)([^/\\\\]+)(?:/|\\\\)@dot");
	
	private static Pattern JAVAC_LINE = Pattern.compile(".*\\[javac\\]\\s(.*)");
	private static Pattern ECLIPSE_BRAND_LINE = Pattern.compile(".*\\[eclipse\\.brand[^]]+\\]\\s(.*)");
	private static Pattern P2_LINE = Pattern.compile(".*\\[p2.director\\]\\s(.*)");
	
	private static Pattern MISSING_TEST_LINE = Pattern.compile(".*\\[exec\\]\\s(.*Could not find plugin.*)");

	private static Pattern CDT_BUILD_INFO_LINE = Pattern.compile(".*\\[(?:echo|exec)\\]\\s+(.*\\*+.*(Building|Rebuild).*)");
	//    [exec] Internal Builder: Cannot run program "gcc": Launching failed
	private static Pattern CDT_INTERNAL_BUILD_ERROR_LINE = Pattern.compile(".*\\[exec\\]\\s+(Internal Builder:.*)");
	private static Pattern GCC_COMPILE_MESSAGE_LINE = Pattern.compile(".*\\[exec\\]\\s+(.*(src|inc).*(cpp|h):.*)");
	private static Pattern GCC_LINK_UNDEFINED_LINE = Pattern.compile(".*\\[exec\\]\\s+(.*\\.o:.*undefined.*)");
	private static Pattern GCC_LINK_EXIT_LINE = Pattern.compile(".*\\[exec\\]\\s+(collect2.*)");

	private void process(BufferedReader in, PrintStream out) throws IOException {
		String lastPlugin = null;
		
		final Pattern[] patterns = new Pattern[] { JAVAC_LINE, ECLIPSE_BRAND_LINE, P2_LINE,
				CDT_BUILD_INFO_LINE, CDT_INTERNAL_BUILD_ERROR_LINE,
				GCC_COMPILE_MESSAGE_LINE, GCC_LINK_UNDEFINED_LINE, GCC_LINK_EXIT_LINE,
				MISSING_TEST_LINE
				}; 
		String line;
		while ((line = in.readLine()) != null) {
			Matcher match = MKDIR_FOR_PLUGIN_LINE.matcher(line);
			if (match.matches()) {
				lastPlugin = match.group(1);
				out.println("\n\n**** Building " + lastPlugin + "\n");
				continue;
			}
			for (Pattern patt : patterns) {
				match = patt.matcher(line);
				if (match.matches()) {
					out.println(match.group(1));
					break;
				}
			}
		}			
	}
}
