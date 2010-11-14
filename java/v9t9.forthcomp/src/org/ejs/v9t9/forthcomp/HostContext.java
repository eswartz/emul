/**
 * 
 */
package org.ejs.v9t9.forthcomp;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Stack;

import org.ejs.coffee.core.utils.HexUtils;
import org.ejs.v9t9.forthcomp.words.Again;
import org.ejs.v9t9.forthcomp.words.Allot;
import org.ejs.v9t9.forthcomp.words.BackSlash;
import org.ejs.v9t9.forthcomp.words.BaseHostBranch;
import org.ejs.v9t9.forthcomp.words.BaseWord;
import org.ejs.v9t9.forthcomp.words.Begin;
import org.ejs.v9t9.forthcomp.words.BracketChar;
import org.ejs.v9t9.forthcomp.words.BracketElse;
import org.ejs.v9t9.forthcomp.words.BracketIf;
import org.ejs.v9t9.forthcomp.words.BracketIfdef;
import org.ejs.v9t9.forthcomp.words.BracketIfndef;
import org.ejs.v9t9.forthcomp.words.BracketThen;
import org.ejs.v9t9.forthcomp.words.CR;
import org.ejs.v9t9.forthcomp.words.CharComma;
import org.ejs.v9t9.forthcomp.words.Colon;
import org.ejs.v9t9.forthcomp.words.ColonColon;
import org.ejs.v9t9.forthcomp.words.Comma;
import org.ejs.v9t9.forthcomp.words.CompileComma;
import org.ejs.v9t9.forthcomp.words.Constant;
import org.ejs.v9t9.forthcomp.words.Create;
import org.ejs.v9t9.forthcomp.words.DConstant;
import org.ejs.v9t9.forthcomp.words.DLiteral;
import org.ejs.v9t9.forthcomp.words.DVariable;
import org.ejs.v9t9.forthcomp.words.Do;
import org.ejs.v9t9.forthcomp.words.Dot;
import org.ejs.v9t9.forthcomp.words.DotQuote;
import org.ejs.v9t9.forthcomp.words.Else;
import org.ejs.v9t9.forthcomp.words.Exit;
import org.ejs.v9t9.forthcomp.words.Here;
import org.ejs.v9t9.forthcomp.words.Host0Branch;
import org.ejs.v9t9.forthcomp.words.HostBehavior;
import org.ejs.v9t9.forthcomp.words.HostBinOp;
import org.ejs.v9t9.forthcomp.words.HostBranch;
import org.ejs.v9t9.forthcomp.words.HostConstant;
import org.ejs.v9t9.forthcomp.words.HostDecimal;
import org.ejs.v9t9.forthcomp.words.HostDoes;
import org.ejs.v9t9.forthcomp.words.HostDoubleLiteral;
import org.ejs.v9t9.forthcomp.words.HostDrop;
import org.ejs.v9t9.forthcomp.words.HostDup;
import org.ejs.v9t9.forthcomp.words.HostEmit;
import org.ejs.v9t9.forthcomp.words.HostExitWord;
import org.ejs.v9t9.forthcomp.words.HostFetch;
import org.ejs.v9t9.forthcomp.words.HostHex;
import org.ejs.v9t9.forthcomp.words.HostLiteral;
import org.ejs.v9t9.forthcomp.words.HostOver;
import org.ejs.v9t9.forthcomp.words.HostPlusStore;
import org.ejs.v9t9.forthcomp.words.HostReturnRead;
import org.ejs.v9t9.forthcomp.words.HostStore;
import org.ejs.v9t9.forthcomp.words.HostSwap;
import org.ejs.v9t9.forthcomp.words.HostTargetOnly;
import org.ejs.v9t9.forthcomp.words.HostType;
import org.ejs.v9t9.forthcomp.words.HostUnaryOp;
import org.ejs.v9t9.forthcomp.words.HostVariable;
import org.ejs.v9t9.forthcomp.words.If;
import org.ejs.v9t9.forthcomp.words.Immediate;
import org.ejs.v9t9.forthcomp.words.Include;
import org.ejs.v9t9.forthcomp.words.LastXt;
import org.ejs.v9t9.forthcomp.words.Lbracket;
import org.ejs.v9t9.forthcomp.words.Leave;
import org.ejs.v9t9.forthcomp.words.Literal;
import org.ejs.v9t9.forthcomp.words.Loop;
import org.ejs.v9t9.forthcomp.words.Paren;
import org.ejs.v9t9.forthcomp.words.ParsedTick;
import org.ejs.v9t9.forthcomp.words.PlusLoop;
import org.ejs.v9t9.forthcomp.words.PopExportState;
import org.ejs.v9t9.forthcomp.words.Postpone;
import org.ejs.v9t9.forthcomp.words.PushExportState;
import org.ejs.v9t9.forthcomp.words.QuestionDo;
import org.ejs.v9t9.forthcomp.words.Rbracket;
import org.ejs.v9t9.forthcomp.words.Repeat;
import org.ejs.v9t9.forthcomp.words.SQuote;
import org.ejs.v9t9.forthcomp.words.SemiColon;
import org.ejs.v9t9.forthcomp.words.SetDP;
import org.ejs.v9t9.forthcomp.words.TargetContext;
import org.ejs.v9t9.forthcomp.words.Then;
import org.ejs.v9t9.forthcomp.words.Tick;
import org.ejs.v9t9.forthcomp.words.To;
import org.ejs.v9t9.forthcomp.words.UDot;
import org.ejs.v9t9.forthcomp.words.UPlusLoop;
import org.ejs.v9t9.forthcomp.words.Until;
import org.ejs.v9t9.forthcomp.words.User;
import org.ejs.v9t9.forthcomp.words.Value;
import org.ejs.v9t9.forthcomp.words.Variable;
import org.ejs.v9t9.forthcomp.words.While;

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
	
	/**
	 * @param targetContext 
	 * 
	 */
	public HostContext(TargetContext targetContext) {
		super();
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
		define("include", new Include());
		
		define("[if]", new BracketIf());
		define("[ifdef]", new BracketIfdef());
		define("[ifndef]", new BracketIfndef());
		define("[else]", new BracketElse());
		define("[then]", new BracketThen());
		define("[endif]", new BracketThen());
		
		define("<EXPORT", new PushExportState());
		define("EXPORT>", new PopExportState());
		
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
		define("::", new ColonColon());
		define(";", new SemiColon());
		define("[CHAR]", new BracketChar());
		
		define("TO", new To());
		
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
	 	define("loop", new Loop());
	 	define("+loop", new PlusLoop());
	 	define("u+loop", new UPlusLoop());
	 	define("exit", new Exit());

	 	define("(", new Paren());
	 	define("\\", new BackSlash());
	 	
	 	define("[", new Lbracket());
	 	define("]", new Rbracket());
	 	
	 	define(",", new Comma());
	 	define("c,", new CharComma());
	 	
	 	define("compile,", new CompileComma());
	 	define("does>", new HostDoes());
	 	
		define("DP!", new SetDP());
		define("HERE", new Here());
		define("LastXt", new LastXt());
		
		define("S\"", new SQuote());
		define(".\"", new DotQuote());
		define(".", new Dot());
		define("U.", new UDot());
		define("emit", new HostEmit());
		define("cr", new CR());
		define("type", new HostType());
		define("decimal", new HostDecimal());
		define("hex", new HostHex());
		
		/////////////////////
		
		define("LITERAL", new Literal(true));
		define("DLITERAL", new DLiteral(true));
		define("(LITERAL)", new Literal(false));
		define("(DLITERAL)", new DLiteral(false));

		
		define("0branch", new Host0Branch());
		define("branch", new HostBranch());
		
		define("+!", new HostPlusStore());
		define("r>", new HostReturnRead());
		
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
		define("/", new HostBinOp("/") {
			public int getResult(int l, int r) { return l/r; }
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
		define("true", new HostConstant(-1));
		define("false", new HostConstant(0));
		
		define("cell", new HostConstant(cellSize));
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
		define("DUP", new HostDup());
		define("SWAP", new HostSwap());
		define("DROP", new HostDrop());
		define("OVER", new HostOver());
		
	}

	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.IContext#pushData(int)
	 */
	public void pushData(int value) {
		dataStack.push(value);
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.IContext#popData()
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
		HostVariable state = (HostVariable)require("state");
		return (state.getValue() != 0);
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
		HostVariable state = (HostVariable)require("state");
		int val = b ? 1 : 0;
		if (state.getValue() == val)
			throw abort("already defining");
		state.setValue(val);
		
	}

	/**
	 * @throws AbortException 
	 * 
	 */
	public void stopCompiling() throws AbortException {
		assertCompiling();
		HostVariable state = (HostVariable)require("state");
		state.setValue(0);
		
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

	public void setCSP() throws AbortException {
		HostVariable csp = (HostVariable)require("csp");
		csp.setValue(dataStack.size());
	}
	public void assertCSP(TargetContext targetContext) throws AbortException {
		HostVariable csp = (HostVariable)require("csp");
		if (csp.getValue() != dataStack.size())
			throw abort("at " + targetContext.getLatest().getName() + ": mismatched conditionals or other stack damage: was " + csp.getValue()+"; now " + dataStack.size() + " " + stack() );
	}
	
	public void pushCall(int pc) {
		if (DEBUG) System.out.println("call " + pc);
		callStack.push(hostPc);
		hostPc = pc;
	}
	public int popCall() {
		return (hostPc = callStack.pop());
	}
	
	

	public int getLocalDP() {
		return hostDp;
	}
	public void compile(IWord word) {
		if (DEBUG) System.out.println(hostDp +": "+ word);
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
					"exec " + hostPc + ": " + word);
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
		compile(new HostExitWord());
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
		assert hostDp > 0 && hostWords.get(hostDp - 1) instanceof BaseHostBranch;
		
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
			}
			else
				targetContext.compile(targetWord);
		}
		if (hostWord != null) {
			if (hostWord.getCompilationSemantics() != null) {
				if (!hadSemantics)
					hostWord.getCompilationSemantics().execute(this, targetContext);
				else if (hostWord != targetWord || !targetWord.getEntry().isImmediate())
					compile(hostWord);
			}
			else
				compile(hostWord);
		}
			
	}

	/**
	 * @param string
	 * @param targetContext 
	 * @return
	 * @throws AbortException 
	 */
	public int readVariable(String string, TargetContext targetContext) throws AbortException {
		return ((HostVariable)require(string)).getValue();
	}

}
