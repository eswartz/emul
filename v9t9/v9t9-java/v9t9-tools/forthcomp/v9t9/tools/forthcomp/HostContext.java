/*
  HostContext.java

  (c) 2010-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tools.forthcomp;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Stack;

import v9t9.tools.forthcomp.words.AbortQuote;
import v9t9.tools.forthcomp.words.Again;
import v9t9.tools.forthcomp.words.Allot;
import v9t9.tools.forthcomp.words.BackSlash;
import v9t9.tools.forthcomp.words.BarExportNext;
import v9t9.tools.forthcomp.words.BarHideNext;
import v9t9.tools.forthcomp.words.BaseHostBranch;
import v9t9.tools.forthcomp.words.BaseStdWord;
import v9t9.tools.forthcomp.words.BaseWord;
import v9t9.tools.forthcomp.words.Begin;
import v9t9.tools.forthcomp.words.BracketChar;
import v9t9.tools.forthcomp.words.BracketCompile;
import v9t9.tools.forthcomp.words.BracketElse;
import v9t9.tools.forthcomp.words.BracketIf;
import v9t9.tools.forthcomp.words.BracketIfdef;
import v9t9.tools.forthcomp.words.BracketIfndef;
import v9t9.tools.forthcomp.words.BracketThen;
import v9t9.tools.forthcomp.words.CR;
import v9t9.tools.forthcomp.words.Char;
import v9t9.tools.forthcomp.words.CharComma;
import v9t9.tools.forthcomp.words.Cmove;
import v9t9.tools.forthcomp.words.Colon;
import v9t9.tools.forthcomp.words.ColonColon;
import v9t9.tools.forthcomp.words.Comma;
import v9t9.tools.forthcomp.words.CompileComma;
import v9t9.tools.forthcomp.words.Constant;
import v9t9.tools.forthcomp.words.Create;
import v9t9.tools.forthcomp.words.DConstant;
import v9t9.tools.forthcomp.words.DLiteral;
import v9t9.tools.forthcomp.words.DVariable;
import v9t9.tools.forthcomp.words.Do;
import v9t9.tools.forthcomp.words.Dot;
import v9t9.tools.forthcomp.words.DotQuote;
import v9t9.tools.forthcomp.words.Else;
import v9t9.tools.forthcomp.words.ErrorQuote;
import v9t9.tools.forthcomp.words.Exit;
import v9t9.tools.forthcomp.words.Here;
import v9t9.tools.forthcomp.words.Host0Branch;
import v9t9.tools.forthcomp.words.Host2Dup;
import v9t9.tools.forthcomp.words.HostBehavior;
import v9t9.tools.forthcomp.words.HostBinOp;
import v9t9.tools.forthcomp.words.HostBranch;
import v9t9.tools.forthcomp.words.HostDecimal;
import v9t9.tools.forthcomp.words.HostDoes;
import v9t9.tools.forthcomp.words.HostDrop;
import v9t9.tools.forthcomp.words.HostDup;
import v9t9.tools.forthcomp.words.HostEmit;
import v9t9.tools.forthcomp.words.HostExitWord;
import v9t9.tools.forthcomp.words.HostFetch;
import v9t9.tools.forthcomp.words.HostHex;
import v9t9.tools.forthcomp.words.HostLiteral;
import v9t9.tools.forthcomp.words.HostOver;
import v9t9.tools.forthcomp.words.HostPlusStore;
import v9t9.tools.forthcomp.words.HostPushReturn;
import v9t9.tools.forthcomp.words.HostRecurse;
import v9t9.tools.forthcomp.words.HostReturnPop;
import v9t9.tools.forthcomp.words.HostRot;
import v9t9.tools.forthcomp.words.HostStore;
import v9t9.tools.forthcomp.words.HostSwap;
import v9t9.tools.forthcomp.words.HostTargetOnly;
import v9t9.tools.forthcomp.words.HostType;
import v9t9.tools.forthcomp.words.HostUnaryOp;
import v9t9.tools.forthcomp.words.HostVariable;
import v9t9.tools.forthcomp.words.If;
import v9t9.tools.forthcomp.words.Immediate;
import v9t9.tools.forthcomp.words.Include;
import v9t9.tools.forthcomp.words.Inline;
import v9t9.tools.forthcomp.words.LastXt;
import v9t9.tools.forthcomp.words.Lbracket;
import v9t9.tools.forthcomp.words.Leave;
import v9t9.tools.forthcomp.words.Literal;
import v9t9.tools.forthcomp.words.LoopCompile;
import v9t9.tools.forthcomp.words.Paren;
import v9t9.tools.forthcomp.words.ParsedTick;
import v9t9.tools.forthcomp.words.PopExportState;
import v9t9.tools.forthcomp.words.Postpone;
import v9t9.tools.forthcomp.words.PushExportState;
import v9t9.tools.forthcomp.words.QuestionDo;
import v9t9.tools.forthcomp.words.Rbracket;
import v9t9.tools.forthcomp.words.Repeat;
import v9t9.tools.forthcomp.words.SQuote;
import v9t9.tools.forthcomp.words.SemiColon;
import v9t9.tools.forthcomp.words.SetDP;
import v9t9.tools.forthcomp.words.TargetContext;
import v9t9.tools.forthcomp.words.TargetOnlyDoes;
import v9t9.tools.forthcomp.words.TestQuote;
import v9t9.tools.forthcomp.words.Then;
import v9t9.tools.forthcomp.words.Tick;
import v9t9.tools.forthcomp.words.To;
import v9t9.tools.forthcomp.words.ToLocal;
import v9t9.tools.forthcomp.words.UDot;
import v9t9.tools.forthcomp.words.Until;
import v9t9.tools.forthcomp.words.User;
import v9t9.tools.forthcomp.words.Value;
import v9t9.tools.forthcomp.words.Variable;
import v9t9.tools.forthcomp.words.While;
import ejs.base.utils.HexUtils;

/**
 * @author ejs
 *
 */
public class HostContext extends Context {
	public static boolean DEBUG = true;
	
	private Stack<Integer> dataStack;
	private TokenStream tokenStream;
	private Stack<Integer> returnStack;
	
	private Stack<Integer> callStack;
	private int hostDp;
	private LinkedHashMap<Integer, IWord> hostWords;
	private int hostPc;
	private Map<Integer, Integer> fixupMap;

	private int cellSize;

	private final TargetContext targetContext;
	
	/**
	 * @param targetContext 
	 * 
	 */
	public HostContext(TargetContext targetContext) {
		super();
		this.targetContext = targetContext;
		this.cellSize = targetContext.getCellSize();
		dataStack = new Stack<Integer>();
		returnStack = new Stack<Integer>();
		callStack = new Stack<Integer>();
		tokenStream = new TokenStream();
		hostPc = -1;
		hostWords = new LinkedHashMap<Integer, IWord>();
		fixupMap = new HashMap<Integer, Integer>();
	}

	/**
	 * 
	 */
	public void defineHostCompilerWords() {
		define("csp", new HostVariable(0));
		
		define("(define-prims)", new BaseWord() {
			{
				setExecutionSemantics(new ISemantics() {
					
					public void execute(HostContext hostContext, TargetContext targetContext)
							throws AbortException {
						targetContext.defineBuiltins();
					}
				});
			}
			
		});
		define("undef", new BaseWord() {
			{
				setInterpretationSemantics(new ISemantics() {
					
					public void execute(HostContext hostContext, TargetContext targetContext)
							throws AbortException {
						String name = hostContext.readToken();
						hostContext.undefine(name);
					}
				});
			}
			
		});
		define("include", new Include());
		
		define("[if]", new BracketIf());
		define("[ifdef]", new BracketIfdef());
		define("[ifundef]", new BracketIfndef());
		define("[else]", new BracketElse());
		define("[then]", new BracketThen());
		define("[endif]", new BracketThen());
		
		define("<EXPORT", new PushExportState());
		define("EXPORT>", new PopExportState());
		define("|", new BarHideNext());
		define("|+", new BarExportNext());
		
		define("create", new Create());
		define("variable", new Variable());
		define("dvariable", new DVariable());
		define("constant", new Constant());
		define("dconstant", new DConstant());
		define("user", new User());
		define("value", new Value());
		
		define("POSTPONE", new Postpone());
		define("IMMEDIATE", new Immediate());
		define("HOST(", new HostBehavior());
		
		define("allot", new Allot());
		define("'", new Tick());
		define("[']", new ParsedTick());
		
		define("!", new HostStore());
		define("@", new HostFetch());
		
		
		define(":", new Colon());
//		define("HOST:", new HostOnlyColon());
		define("::", new ColonColon());
		define(":>", new ToLocal());
		
		define(";", new SemiColon());
		define("[CHAR]", new BracketChar());
		define("CHAR", new Char());
		
		define("INLINE:", new Inline());
		
		define("TO", new To());
		
		define("recurse", new HostRecurse());
		
		define("if", new If());
		define("else", new Else());
	 	define("then", new Then());
	 	define("begin", new Begin());
	 	define("again", new Again());
	 	define("until", new Until());
	 	define("while", new While());
	 	define("repeat", new Repeat());
	 	define("do", new Do());
	 	define("?do", new QuestionDo());
	 	define("leave", new Leave());
	 	define("loop", new LoopCompile("(loop)"));
//	 	define("uloop", new LoopCompile("(uloop)"));
	 	define("+loop", new LoopCompile("(+loop)"));
//	 	define("u+loop", new LoopCompile("(u+loop)"));
	 	define("exit", new Exit());

	 	define("(", new Paren());
	 	define("\\", new BackSlash());
	 	
	 	define("[", new Lbracket());
	 	define("]", new Rbracket());
	 	
	 	define(",", new Comma());
	 	define("c,", new CharComma());
	 	
	 	define("compile,", new CompileComma());
	 	define("does>", new HostDoes());
	 	define("target-does>", new TargetOnlyDoes());
	 	
		define("DP!", new SetDP());
		define("HERE", new Here());
		define("LASTXT", new LastXt());
		
		define("CMOVE", new Cmove());
		
		define("[compile]", new BracketCompile());
		
		define("S\"", new SQuote());
		define(".\"", new DotQuote());
		define(".", new Dot());
		define("U.", new UDot());
		define("emit", new HostEmit());
		define("cr", new CR());
		define("type", new HostType());
		define("decimal", new HostDecimal());
		define("hex", new HostHex());
		
		define("TEST\"", new TestQuote());
		define("ERROR\"", new ErrorQuote());
		define("ABORT\"", new AbortQuote());
		
		/////////////////////
		
		define("LITERAL", new Literal(true));
		define("DLITERAL", new DLiteral(true));
		define("(LITERAL)", new Literal(false));
		define("(DLITERAL)", new DLiteral(false));

		
		define("0branch", new Host0Branch());
		define("branch", new HostBranch());
		
		define("+!", new HostPlusStore());
		define("r>", new HostReturnPop());
		define(">r", new HostPushReturn());
		
		define("target-only", new HostTargetOnly());
		
		define("+", new HostBinOp("+") {
			public int getResult(int l, int r) { return l+r; }
		});
		define("-", new HostBinOp("-") {
			public int getResult(int l, int r) { return l-r; }
		});
		define("*", new HostBinOp("*") {
			public int getResult(int l, int r) { return l*r; }
		});
		define("U*", new HostBinOp("U*") {
			public int getResult(int l, int r) { return l*r; }
		});
		define("/", new HostBinOp("/") {
			public int getResult(int l, int r) { return l/r; }
		});
		define("U/", new HostBinOp("U/") {
			public int getResult(int l, int r) { return unsigned(l)/unsigned(r); }
		});
		define("OR", new HostBinOp("OR") {
			public int getResult(int l, int r) { return l|r; }
		});
		define("XOR", new HostBinOp("XOR") {
			public int getResult(int l, int r) { return l^r; }
		});
		define("AND", new HostBinOp("AND") {
			public int getResult(int l, int r) { return l&r; }
		});
		define("NEGATE", new HostUnaryOp("NEGATE") {
			public int getResult(int v) { return -v; }
		});
		define("INVERT", new HostUnaryOp("INVERT") {
			public int getResult(int v) { return ~v; }
		});
		define("LSHIFT", new HostBinOp("LSHIFT") {
			public int getResult(int l, int r) { return l << r; }
		});

		define("true", new HostLiteral(-1, false));
		define("false", new HostLiteral(0, false));
		
		define("#char", new HostLiteral(1, false));
		define("#cell", new HostLiteral(cellSize, false));
		define("cell", new HostLiteral(cellSize, false));
		if (cellSize == 1)
			define("cell<<", new HostLiteral(0, false));
		else if (cellSize == 2)
			define("cell<<", new HostLiteral(1, false));
		else
			assert false;
		define("cells", new HostUnaryOp("cells") {
			public int getResult(int v) { return v * cellSize; } 
		});
		
		define(">", new HostBinOp(">") {
			public int getResult(int l, int r) { return (l>r)?-1:0; }
		});
		define(">=", new HostBinOp(">=") {
			public int getResult(int l, int r) { return (l>=r)?-1:0; }
		});
		define("<", new HostBinOp("<") {
			public int getResult(int l, int r) { return (l<r)?-1:0; }
		});
		define("<=", new HostBinOp("<=") {
			public int getResult(int l, int r) { return (l<=r)?-1:0; }
		});
		
		define("U>", new HostBinOp("U>") {
			public int getResult(int l, int r) { return (unsigned(l)>unsigned(r))?-1:0; }
		});
		define("U>=", new HostBinOp("U>=") {
			public int getResult(int l, int r) { return (unsigned(l)>=unsigned(r))?-1:0; }
		});
		define("U<", new HostBinOp("U<") {
			public int getResult(int l, int r) { return (unsigned(l)<unsigned(r))?-1:0; }
		});
		define("U<=", new HostBinOp("U<=") {
			public int getResult(int l, int r) { return (unsigned(l)<=unsigned(r))?-1:0; }
		});
		
		define("=", new HostBinOp("=") {
			public int getResult(int l, int r) { return (l==r)?-1:0; }
		});
		
		define("0>", new HostUnaryOp("0>") {
			public int getResult(int v) { return v>0?-1:0; }
		});
		define("0>=", new HostUnaryOp("0>=") {
			public int getResult(int v) { return v>=0?-1:0; }
		});
		define("0<", new HostUnaryOp("0<") {
			public int getResult(int v) { return v<0?-1:0; }
		});
		define("0<=", new HostUnaryOp("0<=") {
			public int getResult(int v) { return v<=0?-1:0; }
		});
		define("0=", new HostUnaryOp("0=") {
			public int getResult(int v) { return v==0?-1:0; }
		});
		define("NOT", new HostUnaryOp("NOT") {
			public int getResult(int v) { return v==0?-1:0; }
		});
		
		define("1-", new HostUnaryOp("1-") {
			public int getResult(int v) { return v-1; }
		});
		define("2-", new HostUnaryOp("2-") {
			public int getResult(int v) { return v-2; }
		});
		define("1+", new HostUnaryOp("1+") {
			public int getResult(int v) { return v+1; }
		});
		define("2+", new HostUnaryOp("2+") {
			public int getResult(int v) { return v+2; }
		});
		define("2*", new HostUnaryOp("2*") {
			public int getResult(int v) { return v*2; }
		});
		define("2/", new HostUnaryOp("2/") {
			public int getResult(int v) { return v/2; }
		});
		define("DUP", new HostDup());
		define("2DUP", new Host2Dup());
		define("SWAP", new HostSwap());
		define("DROP", new HostDrop());
		define("OVER", new HostOver());
		define("ROT", new HostRot());
		
	}

	/**
	 * @param r
	 * @return
	 */
	protected int unsigned(int r) {
		return cellSize == 2 ? (r & 0xffff): r;
	}

	/* (non-Javadoc)
	 * @see v9t9.forthcomp.IContext#pushData(int)
	 */
	public void pushData(int value) {
		dataStack.push(value);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.forthcomp.IContext#popData()
	 */
	public int popData() {
		return dataStack.pop();
	}
	/**
	 * @return
	 */
	public TokenStream getStream() {
		return tokenStream;
	}
	/**
	 * @return
	 * @throws AbortException 
	 * @throws AbortException 
	 */
	public String readToken() throws AbortException  {
		try {
			return tokenStream.read();
		} catch (IOException e) {
			throw abort("end of file");
		}
	}
	/**
	 * @param string
	 * @return
	 */
	public AbortException abort(String string) {
		return tokenStream.abort(string);
	}

	/**
	 * @param i
	 */
	public void pushReturn(int i) {
		returnStack.push(i);
	}
	
	public int popReturn() {
		return returnStack.pop();
	}
	public int peekReturn() {
		return returnStack.peek();
	}

	/**
	 * @return
	 */
	public Stack<Integer> getDataStack() {
		return dataStack;
	}
	/**
	 * @return the returnStack
	 */
	public Stack<Integer> getReturnStack() {
		return returnStack;
	}

	/**
	 * @return
	 * @throws AbortException 
	 */
	public boolean isCompiling() throws AbortException {
		return readVar("state") != 0;
	}
	
	/**
	 * @return 
	 * @throws AbortException 
	 * 
	 */
	public void assertCompiling() throws AbortException {
		if (!isCompiling())
			throw abort("not defining");

	}

	public void setCompiling(boolean b) throws AbortException {
		int val = b ? 1 : 0;
		if (readVar("state") == val)
			throw abort("already defining");
		writeVar("state", val);
		writeVar("(state)", val);
		
	}

	/**
	 * @throws AbortException 
	 * 
	 */
	public void stopCompiling() throws AbortException {
		assertCompiling();
		writeVar("state", 0);
		
		compileExit();
		
		fixupMap.clear();
	}

	/**
	 * @param i
	 * @throws AbortException 
	 */
	public void assertPairs(int i) throws AbortException {
		if (popData() != i)
			throw abort("mismatched conditional: " + i);
	}

	/**
	 * @param i
	 */
	public void pushPairs(int i) {
		pushData(i);
	}

	/**
	 * @return
	 */
	public int peekData() {
		return dataStack.peek();
	}

	public boolean inCSP() throws AbortException {
		return readVar("csp") != dataStack.size();
	}
	public void setCSP() throws AbortException {
		writeVar("csp", dataStack.size());
	}
	public void assertCSP(TargetContext targetContext) throws AbortException {
		int cspVal = readVar("csp");
		if (cspVal != dataStack.size())
			throw abort("at " + targetContext.getLatest().getName() + ": mismatched conditionals or other stack damage: was " + cspVal +"; now " + dataStack.size() + " " + stack() );
	}
	
	public void pushCall(int pc) {
		callStack.push(hostPc);
		hostPc = pc;
	}
	public int popCall() {
		return (hostPc = callStack.pop());
	}
	
	

	public int getLocalDP() {
		return hostDp;
	}
	public void build(IWord word) {
		if (DEBUG) System.out.println("H>" + hostDp +": "+ word);
		assert !hostWords.containsKey((Integer)hostDp);
		if (word instanceof BaseHostBranch)
			word = (IWord) ((BaseHostBranch)word).clone();
		hostWords.put(hostDp, word);
		hostDp++;
	}
	
	public void interpret(HostContext hostContext, TargetContext targetContext) throws AbortException {
		while (hostPc >= 0) {
			IWord word = hostWords.get(hostPc);
			if (word == null)
				throw hostContext.abort("broken dictionary entry at " + hostPc);
			if (DEBUG) System.out.println(
					//stack() + "\n" + 
					"H> exec " + hostPc + ": " + word);
			hostPc++;
			
			/*
			if (word instanceof ITargetWord) {
				ITargetWord targetWord = (ITargetWord) word;
				IWord hostWord = targetWord.getEntry().getHostBehavior();
				if (hostWord != null) {
					if (DEBUG) System.out.println("On host: " + hostWord);

					Stack<Integer> origDataStack = new Stack<Integer>(); 
					origDataStack.addAll(dataStack);
					Stack<Integer> origReturnStack = new Stack<Integer>();
					origReturnStack.addAll(returnStack);
					int dp = targetContext.getDP();
					
					hostWord.getExecutionSemantics().execute(hostContext, targetContext);
					
					targetContext.setDP(dp);
					dataStack = origDataStack;
					returnStack = origReturnStack;
				}
				
			}
			*/
			
			if (word.getExecutionSemantics() == null)
				throw abort(word.getName() +  " has unknown runtime semantics");
			word.getExecutionSemantics().execute(hostContext, targetContext);
		}
	}

	/**
	 * @return
	 */
	String stack() {
		StringBuilder sb = new StringBuilder();
		sb.append('[');
		for (int t = 0; t < dataStack.size(); t++ ) {
			sb.append(dataStack.get(t)).append(" ");
		}
		sb.append(']');
		return sb.toString();
	}

	/**
	 * 
	 */
	public void compileExit() {
		build(new HostExitWord());
	}

	/**
	 * @param hostPc the hostPc to set
	 */
	public void setHostPc(int hostPc) {
		this.hostPc = hostPc;
	}
	/**
	 * @return the hostPc
	 */
	public int getHostPc() {
		return hostPc;
	}

	/**
	 * here over - swap !
	 * @param diff 
	 * @param opAddr 
	 * @throws AbortException 
	 */
	public void resolveFixup(int opAddr, int toAddr) {
		int hostOpAddr = fixupMap.get(opAddr);
		int hostToAddr;
		Integer oldToAddr = fixupMap.get(toAddr);
		if (oldToAddr == null) {
			hostToAddr = hostDp;
		}
		else {
			hostToAddr = oldToAddr;
		}
		
		IWord brWord = hostWords.get(hostOpAddr);
		if (brWord instanceof BaseHostBranch)
			((BaseHostBranch) brWord).setTarget(hostToAddr);
		else
			System.out.println("Suspicious fixup from " + HexUtils.toHex4(opAddr) + " to " + HexUtils.toHex4(toAddr)+"; hits " + brWord + " in host");
	}

	/**
	 * @param dp
	 */
	public void markFixup(int dp) {
		//assert hostDp > 0 && hostWords.get(hostDp - 1) instanceof BaseHostBranch;
		
		fixupMap.put(dp, hostDp - 1);
	}

	/**
	 * @param targetContext
	 * @param word
	 * @throws AbortException 
	 */
	public void compileWord(TargetContext targetContext, IWord hostWord, ITargetWord targetWord) throws AbortException {
		
		boolean hadSemantics = false;
		
		if (targetWord != null) {
			if (targetWord.getCompilationSemantics() != null) {
				if (!targetWord.getEntry().isTargetOnly()) {
					boolean saveState = hostWord != null && hostWord != targetWord
						&& targetWord.getEntry().getHostBehavior() != null;
					int dp = 0;
					Stack<Integer> origDataStack = null;
					Stack<Integer> origReturnStack = null;
					if (saveState) {
						if (DEBUG) System.out.println("On host: " + hostWord);

						origDataStack = new Stack<Integer>(); 
						origDataStack.addAll(dataStack);
						origReturnStack = new Stack<Integer>();
						origReturnStack.addAll(returnStack);
						dp = targetContext.getDP();
					}
					targetWord.getCompilationSemantics().execute(this, targetContext);
					if (saveState) {
						targetContext.setDP(dp);
						dataStack = origDataStack;
						returnStack = origReturnStack;
					} else {
						hadSemantics = true;
					}
				}
			} else {
				targetContext.buildCall(targetWord);
			}
		}
		if (hostWord != null) {
			if (hostWord.getCompilationSemantics() != null) {
				if (!hadSemantics) {
					hostWord.getCompilationSemantics().execute(this, targetContext);
					
					// feels hacky -- detect when ANS/compiler words are used in
					// target definitions but never defined
					if (targetWord == null 
							&& !(hostWord instanceof BaseStdWord && ((BaseStdWord) hostWord).isImmediate())
							&& hostWord.getName() != null
							&& (getLatest() == null
							|| !hostWord.getName().equals(getLatest().getName()))) {
						targetContext.defineForward(hostWord.getName(), getStream().getLocation());
					}
					
				} else if (hostWord != targetWord || !targetWord.getEntry().isImmediate()) {
					build(hostWord);
				}
			}
			else {
				build(hostWord);
			}
		}
			
	}

	/**
	 * @param string
	 * @param targetContext 
	 * @return
	 * @throws AbortException 
	 */
	public int readVariable(String string, ITargetContext targetContext) throws AbortException {
		return ((HostVariable)require(string)).getValue();
	}

	/**
	 * @param string
	 * @return
	 * @throws AbortException 
	 */
	public int readVar(String string) throws AbortException {
		IWord var = targetContext.find(string);
		int val;
		if (var != null) {
			var.getExecutionSemantics().execute(this, targetContext);
			val = targetContext.readCell(popData() & 0xffff);	 //TODO
		}
		else {
			var = find(string);
			val = ((HostVariable) var).getValue();
		}

		return val;
	}
	public void writeVar(String string,int val) throws AbortException {
		IWord var = targetContext.find(string);
		if (var != null) {
			var.getExecutionSemantics().execute(this, targetContext);
			targetContext.writeCell(popData() & 0xffff, val);	//TODO
		}
		else {
			var = find(string);
			((HostVariable) var).setValue(val);
		}
	}

	/**
	 * @param subContext
	 */
	public void copyFrom(HostContext subContext) {
		hostWords = subContext.hostWords;
		hostPc = subContext.hostPc;
		hostDp = subContext.hostDp;
		fixupMap.putAll(subContext.fixupMap);
	}
	public void copyTo(HostContext hostContext) {
		hostContext.hostWords.putAll(hostWords);
		hostContext.hostPc = hostPc;
		hostContext.hostDp = hostDp;
		hostContext.fixupMap.putAll(fixupMap);
	}

}
