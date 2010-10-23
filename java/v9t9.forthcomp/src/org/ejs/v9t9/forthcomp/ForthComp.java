/**
 * 
 */
package org.ejs.v9t9.forthcomp;

import gnu.getopt.Getopt;

import java.io.File;
import java.io.IOException;

import org.ejs.v9t9.forthcomp.words.CharComma;
import org.ejs.v9t9.forthcomp.words.Colon;
import org.ejs.v9t9.forthcomp.words.Comma;
import org.ejs.v9t9.forthcomp.words.Create;
import org.ejs.v9t9.forthcomp.words.Else;
import org.ejs.v9t9.forthcomp.words.Lbracket;
import org.ejs.v9t9.forthcomp.words.Leave;
import org.ejs.v9t9.forthcomp.words.Loop;
import org.ejs.v9t9.forthcomp.words.Paren;
import org.ejs.v9t9.forthcomp.words.PlusLoop;
import org.ejs.v9t9.forthcomp.words.QuestionDo;
import org.ejs.v9t9.forthcomp.words.Rbracket;
import org.ejs.v9t9.forthcomp.words.SemiColon;
import org.ejs.v9t9.forthcomp.words.Then;
import org.ejs.v9t9.forthcomp.words.UPlusLoop;
import org.ejs.v9t9.forthcomp.words.Variable;

/**
 * This class compiles FORTH programs into ROM images for V9t9
 * @author ejs
 *
 */
public class ForthComp {

	
	private static final String PROGNAME = ForthComp.class.getSimpleName();

	public static void main(String[] args) throws Exception {
		
		TargetContext targetContext = null;
		
        Getopt getopt = new Getopt(PROGNAME, args, "?");
        int opt;
        while ((opt = getopt.getopt()) != -1) {
            switch (opt) {
            case '?':
                help();
                break;
            }
        }
        
        if (targetContext == null)
        	targetContext = new F99TargetContext(65536);
        ForthComp comp = new ForthComp(targetContext);
        
        if (getopt.getOptind() < args.length) {
        	String name = args[getopt.getOptind()];
        	comp.parseFile(name);
        } else {
        	System.err.println(PROGNAME + ": no files specified");
        }
        
	}

	private static void help() {
		System.out.println("Help me!");
	}
	
	private HostContext hostContext;
	private TargetContext targetContext;
	private TokenStream tokenStream;
	private HostVariable baseVar;
	private HostVariable stateVar;

	public ForthComp(TargetContext targetContext) {
		hostContext = new HostContext();
		tokenStream = hostContext.getStream();
		this.targetContext = targetContext;
		
		baseVar = (HostVariable) hostContext.define("base", new HostVariable(10));
		stateVar = (HostVariable) hostContext.define("state", new HostVariable(0));
		hostContext.define("csp", new HostVariable(0));
		
		hostContext.define("create", new Create());
		hostContext.define("variable", new Variable());
		hostContext.define("!", new HostStore());
		hostContext.define("@", new HostFetch());
		hostContext.define(":", new Colon());
		hostContext.define(";", new SemiColon());
		
		hostContext.define("if", new IfParser());
		hostContext.define("else", new Else());
	 	hostContext.define("then", new Then());
	 	hostContext.define("do", new DoParser());
	 	hostContext.define("?do", new QuestionDo());
	 	hostContext.define("leave", new Leave());
	 	hostContext.define("loop", new Loop());
	 	hostContext.define("+loop", new PlusLoop());
	 	hostContext.define("u+loop", new UPlusLoop());

	 	hostContext.define("(", new Paren());
	 	
	 	hostContext.define("[", new Lbracket());
	 	hostContext.define("]", new Rbracket());
	 	
	 	hostContext.define(",", new Comma());
	 	hostContext.define("c,", new CharComma());
	 	
	 	targetContext.defineCompilerWords(hostContext);
	}

	/**
	 * @return the hostContext
	 */
	public HostContext getHostContext() {
		return hostContext;
	}
	public void parseFile(String file) throws IOException, AbortException {
		tokenStream.push(new File(file));
		parse();
		tokenStream.pop();
	}

	public void parseString(String text) throws AbortException {
		tokenStream.push(text);
		parse();
		tokenStream.pop();
	}
	public void parse() throws AbortException {
		String token;
		try {
			while ((token = tokenStream.read()) != null)
				parse(token);
		} catch (IOException e) {
			throw abort(e.getMessage());
		}
	}

	private void parse(String token) throws AbortException {
		IWord word;
		
		word = targetContext.find(token);
		if (word instanceof ITargetWord && ((ITargetWord) word).getEntry().isHidden())
			word = null;
		
		if (word == null) {
			word = hostContext.find(token);
			
		}
		
		if (word == null) {
			word = parseLiteral(token);
		}
		if (word == null) {
			throw abort("unknown: " + token);
		}
		
		if (stateVar.getValue() == 0 || word.isImmediate()) {
			word.execute(hostContext, targetContext);
		} else {
			// compiling
			if (word instanceof ITargetWord) {
				targetContext.compile((ITargetWord) word);
			} else if (word instanceof Literal) {
				targetContext.compileLiteral(((Literal) word).getValue(), ((Literal) word).isUnsigned());
			} else if (word instanceof DoubleLiteral) {
				targetContext.compileDoubleLiteral(((DoubleLiteral) word).getValue(), ((DoubleLiteral) word).isUnsigned());
				
			} else {
				throw abort("unknown compile-time semantics for " + token);
			}
		}
	}

	private AbortException abort(String string) {
		return tokenStream.abort(string);
	}

	private IWord parseLiteral(String token) {
		int radix = baseVar.getValue();
		boolean isNeg = token.startsWith("-");
		if (isNeg) {
			token = token.substring(1);
		}
		if (token.startsWith("$")) {
			radix = 16;
			token = token.substring(1);
		}
		boolean isDouble = false;
		if (token.contains(".")) {
			isDouble = true;
		}
		token = token.replaceAll("\\.", "");
		boolean isUnsigned = false;
		if (token.toUpperCase().endsWith("U")) {
			isUnsigned = true;
			token = token.substring(0, token.length() - 1);
		}
		try {
			long val = Long.parseLong(token, radix);
			if (isNeg)
				val = -val;
			if (isDouble)
				return new DoubleLiteral(val, isUnsigned);
			else
				return new Literal((int) val, isUnsigned);
		} catch (NumberFormatException e) {
			return null;
		}
	}
}
