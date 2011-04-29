/**
 * Apr 28, 2011
 */
package org.ejs.eulang.llvm;

import java.io.File;

/**
 * Adapt to llvm installation differences
 * @author ejs
 *
 */
public class LLVMEnv {
	private static String[] pathParts;
	public static String getAs() {
		return findProg("llvm-as");
	}
	public static String getDis() {
		return findProg("llvm-dis");
		
	}
	public static String getOpt() {
		return findProg("opt");
	}
	
	
	/**
	 * @param string
	 * @return
	 */
	private static String findProg(String base) {
		File file;
		if ((file = findOnPath(base)) != null)
			return file.getAbsolutePath();
		if ((file = findOnPath(base + "-2.7")) != null)
			return file.getAbsolutePath();
		return base;
	}
	/**
	 * @param string
	 * @return
	 */
	private static File findOnPath(String string) {
		if (pathParts == null) {
			String env = System.getenv("PATH");
			if (env == null)
				env = System.getenv("path");
			if (env != null)
				pathParts = env.split(File.separatorChar == '/' ? ":" : ";");
			else
				pathParts = new String[0];
		}
		for (String path : pathParts) {
			File cand = new File(path, string + (File.separatorChar == '\\' ? ".exe" : ""));
			if (cand.exists())
				return cand;
		}
		return null;
	}
	/**
	 * @return
	 */
	public static String getStdOpts() {
		return "-preverify -domtree -verify //-lowersetjmp"
				//+ "-raiseallocs "
				+ "-simplifycfg -domtree -domfrontier -mem2reg -globalopt "
				+ "-globaldce -ipconstprop -deadargelim -instcombine -simplifycfg -basiccg -prune-eh -functionattrs -inline -argpromotion"
				+ " -simplify-libcalls -instcombine -jump-threading -simplifycfg -domtree -domfrontier -scalarrepl -instcombine "
				+ "-break-crit-edges "
				//+ "-condprop "
				+ "-tailcallelim -simplifycfg -reassociate -domtree -loops -loopsimplify -domfrontier "
				+ "-lcssa -loop-rotate -licm -lcssa -loop-unswitch -instcombine -scalar-evolution -lcssa -iv-users "
				//+ "-indvars "  // oops, this introduces 17 bit numbers O.o ... a bit of wizardry which also increases code size
				+ "-loop-deletion -lcssa -loop-unroll -instcombine -memdep -gvn -memdep -memcpyopt -sccp -instcombine "
				+ "-break-crit-edges "
				//+ "-condprop "
				+ "-domtree -memdep -dse -adce -simplifycfg -strip-dead-prototypes "
				+ "-print-used-types -deadtypeelim -constmerge -preverify -domtree -verify "
				+ "-std-link-opts -verify";

	}
	
}
