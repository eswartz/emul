/**
 * 
 */
package org.ejs.eulang.llvm.instrs;

public enum ECast {
	BITCAST("bitcast"),
	TRUNC("trunc"),
	ZEXT("zext"),
	SEXT("sext"),
	FPTRUNC("fptrunc"),
	FPEXT("fpext"),
	FPTOUI("fptoui"),
	FPTOSI("fptosi"),
	UITOFP("uitofp"),
	SITOFP("sitofp"),
	PTRTOINT("ptrtoint"),
	INTTOPTR("inttoptr");
	
	private String op;

	ECast(String op) {
		this.op = op;
	}
	public String getOp() { return op; }
}