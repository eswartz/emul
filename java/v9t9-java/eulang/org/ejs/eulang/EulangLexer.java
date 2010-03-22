// $ANTLR 3.1.3 Mar 17, 2009 19:23:44 /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g 2010-03-21 20:30:52

package org.ejs.eulang;
import java.util.HashMap;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

public class EulangLexer extends Lexer {
    public static final int STAR=13;
    public static final int LBRACE_STAR=5;
    public static final int AMP=25;
    public static final int LBRACE=17;
    public static final int MULTI_COMMENT=56;
    public static final int EQUALS=8;
    public static final int EXCL=22;
    public static final int ID=48;
    public static final int SPACE=52;
    public static final int EOF=-1;
    public static final int LPAREN=15;
    public static final int LBRACKET=19;
    public static final int COLON_COLON_EQUALS=10;
    public static final int AT=24;
    public static final int RPAREN=16;
    public static final int IDSUFFIX=45;
    public static final int STRING_LITERAL=51;
    public static final int SLASH=14;
    public static final int GREATER=36;
    public static final int COMMA=7;
    public static final int COMPLE=35;
    public static final int CARET=27;
    public static final int TILDE=23;
    public static final int LESS=37;
    public static final int PLUS=11;
    public static final int SINGLE_COMMENT=55;
    public static final int COMPAND=30;
    public static final int DIGIT=49;
    public static final int RBRACKET=20;
    public static final int RSHIFT=39;
    public static final int RETURNS=44;
    public static final int COMPGE=34;
    public static final int RBRACE=18;
    public static final int PERCENT=42;
    public static final int LETTERLIKE=47;
    public static final int UMOD=43;
    public static final int LSHIFT=38;
    public static final int COMPOR=31;
    public static final int NUMBER=46;
    public static final int LBRACE_LPAREN=4;
    public static final int HASH=21;
    public static final int MINUS=12;
    public static final int SEMI=28;
    public static final int COLON=6;
    public static final int WS=54;
    public static final int COLON_EQUALS=9;
    public static final int NEWLINE=53;
    public static final int QUESTION=29;
    public static final int CHAR_LITERAL=50;
    public static final int URSHIFT=40;
    public static final int COMPEQ=32;
    public static final int COMPNE=33;
    public static final int BAR=26;
    public static final int BACKSLASH=41;

    // delegates
    // delegators

    public EulangLexer() {;} 
    public EulangLexer(CharStream input) {
        this(input, new RecognizerSharedState());
    }
    public EulangLexer(CharStream input, RecognizerSharedState state) {
        super(input,state);

    }
    public String getGrammarFileName() { return "/home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g"; }

    // $ANTLR start "LBRACE_LPAREN"
    public final void mLBRACE_LPAREN() throws RecognitionException {
        try {
            int _type = LBRACE_LPAREN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:9:15: ( '{(' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:9:17: '{('
            {
            match("{("); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LBRACE_LPAREN"

    // $ANTLR start "LBRACE_STAR"
    public final void mLBRACE_STAR() throws RecognitionException {
        try {
            int _type = LBRACE_STAR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:10:13: ( '{*' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:10:15: '{*'
            {
            match("{*"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LBRACE_STAR"

    // $ANTLR start "COLON"
    public final void mCOLON() throws RecognitionException {
        try {
            int _type = COLON;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:11:7: ( ':' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:11:9: ':'
            {
            match(':'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "COLON"

    // $ANTLR start "COMMA"
    public final void mCOMMA() throws RecognitionException {
        try {
            int _type = COMMA;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:12:7: ( ',' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:12:9: ','
            {
            match(','); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "COMMA"

    // $ANTLR start "EQUALS"
    public final void mEQUALS() throws RecognitionException {
        try {
            int _type = EQUALS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:13:8: ( '=' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:13:10: '='
            {
            match('='); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "EQUALS"

    // $ANTLR start "COLON_EQUALS"
    public final void mCOLON_EQUALS() throws RecognitionException {
        try {
            int _type = COLON_EQUALS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:14:14: ( ':=' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:14:16: ':='
            {
            match(":="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "COLON_EQUALS"

    // $ANTLR start "COLON_COLON_EQUALS"
    public final void mCOLON_COLON_EQUALS() throws RecognitionException {
        try {
            int _type = COLON_COLON_EQUALS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:15:20: ( '::=' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:15:22: '::='
            {
            match("::="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "COLON_COLON_EQUALS"

    // $ANTLR start "PLUS"
    public final void mPLUS() throws RecognitionException {
        try {
            int _type = PLUS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:16:6: ( '+' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:16:8: '+'
            {
            match('+'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "PLUS"

    // $ANTLR start "MINUS"
    public final void mMINUS() throws RecognitionException {
        try {
            int _type = MINUS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:17:7: ( '-' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:17:9: '-'
            {
            match('-'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "MINUS"

    // $ANTLR start "STAR"
    public final void mSTAR() throws RecognitionException {
        try {
            int _type = STAR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:18:6: ( '*' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:18:8: '*'
            {
            match('*'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "STAR"

    // $ANTLR start "SLASH"
    public final void mSLASH() throws RecognitionException {
        try {
            int _type = SLASH;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:19:7: ( '/' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:19:9: '/'
            {
            match('/'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "SLASH"

    // $ANTLR start "LPAREN"
    public final void mLPAREN() throws RecognitionException {
        try {
            int _type = LPAREN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:20:8: ( '(' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:20:10: '('
            {
            match('('); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LPAREN"

    // $ANTLR start "RPAREN"
    public final void mRPAREN() throws RecognitionException {
        try {
            int _type = RPAREN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:21:8: ( ')' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:21:10: ')'
            {
            match(')'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "RPAREN"

    // $ANTLR start "LBRACE"
    public final void mLBRACE() throws RecognitionException {
        try {
            int _type = LBRACE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:22:8: ( '{' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:22:10: '{'
            {
            match('{'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LBRACE"

    // $ANTLR start "RBRACE"
    public final void mRBRACE() throws RecognitionException {
        try {
            int _type = RBRACE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:23:8: ( '}' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:23:10: '}'
            {
            match('}'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "RBRACE"

    // $ANTLR start "LBRACKET"
    public final void mLBRACKET() throws RecognitionException {
        try {
            int _type = LBRACKET;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:24:10: ( '[' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:24:12: '['
            {
            match('['); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LBRACKET"

    // $ANTLR start "RBRACKET"
    public final void mRBRACKET() throws RecognitionException {
        try {
            int _type = RBRACKET;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:25:10: ( ']' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:25:12: ']'
            {
            match(']'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "RBRACKET"

    // $ANTLR start "HASH"
    public final void mHASH() throws RecognitionException {
        try {
            int _type = HASH;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:26:6: ( '#' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:26:8: '#'
            {
            match('#'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "HASH"

    // $ANTLR start "EXCL"
    public final void mEXCL() throws RecognitionException {
        try {
            int _type = EXCL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:27:6: ( '!' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:27:8: '!'
            {
            match('!'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "EXCL"

    // $ANTLR start "TILDE"
    public final void mTILDE() throws RecognitionException {
        try {
            int _type = TILDE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:28:7: ( '~' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:28:9: '~'
            {
            match('~'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "TILDE"

    // $ANTLR start "AT"
    public final void mAT() throws RecognitionException {
        try {
            int _type = AT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:29:4: ( '@' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:29:6: '@'
            {
            match('@'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "AT"

    // $ANTLR start "AMP"
    public final void mAMP() throws RecognitionException {
        try {
            int _type = AMP;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:30:5: ( '&' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:30:7: '&'
            {
            match('&'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "AMP"

    // $ANTLR start "BAR"
    public final void mBAR() throws RecognitionException {
        try {
            int _type = BAR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:31:5: ( '|' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:31:7: '|'
            {
            match('|'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "BAR"

    // $ANTLR start "CARET"
    public final void mCARET() throws RecognitionException {
        try {
            int _type = CARET;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:32:7: ( '^' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:32:9: '^'
            {
            match('^'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "CARET"

    // $ANTLR start "SEMI"
    public final void mSEMI() throws RecognitionException {
        try {
            int _type = SEMI;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:33:6: ( ';' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:33:8: ';'
            {
            match(';'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "SEMI"

    // $ANTLR start "QUESTION"
    public final void mQUESTION() throws RecognitionException {
        try {
            int _type = QUESTION;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:34:10: ( '?' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:34:12: '?'
            {
            match('?'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "QUESTION"

    // $ANTLR start "COMPAND"
    public final void mCOMPAND() throws RecognitionException {
        try {
            int _type = COMPAND;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:35:9: ( '&&' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:35:11: '&&'
            {
            match("&&"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "COMPAND"

    // $ANTLR start "COMPOR"
    public final void mCOMPOR() throws RecognitionException {
        try {
            int _type = COMPOR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:36:8: ( '||' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:36:10: '||'
            {
            match("||"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "COMPOR"

    // $ANTLR start "COMPEQ"
    public final void mCOMPEQ() throws RecognitionException {
        try {
            int _type = COMPEQ;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:37:8: ( '==' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:37:10: '=='
            {
            match("=="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "COMPEQ"

    // $ANTLR start "COMPNE"
    public final void mCOMPNE() throws RecognitionException {
        try {
            int _type = COMPNE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:38:8: ( '!=' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:38:10: '!='
            {
            match("!="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "COMPNE"

    // $ANTLR start "COMPGE"
    public final void mCOMPGE() throws RecognitionException {
        try {
            int _type = COMPGE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:39:8: ( '>=' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:39:10: '>='
            {
            match(">="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "COMPGE"

    // $ANTLR start "COMPLE"
    public final void mCOMPLE() throws RecognitionException {
        try {
            int _type = COMPLE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:40:8: ( '<=' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:40:10: '<='
            {
            match("<="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "COMPLE"

    // $ANTLR start "GREATER"
    public final void mGREATER() throws RecognitionException {
        try {
            int _type = GREATER;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:41:9: ( '>' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:41:11: '>'
            {
            match('>'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "GREATER"

    // $ANTLR start "LESS"
    public final void mLESS() throws RecognitionException {
        try {
            int _type = LESS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:42:6: ( '<' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:42:8: '<'
            {
            match('<'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LESS"

    // $ANTLR start "LSHIFT"
    public final void mLSHIFT() throws RecognitionException {
        try {
            int _type = LSHIFT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:43:8: ( '<<' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:43:10: '<<'
            {
            match("<<"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LSHIFT"

    // $ANTLR start "RSHIFT"
    public final void mRSHIFT() throws RecognitionException {
        try {
            int _type = RSHIFT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:44:8: ( '>>' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:44:10: '>>'
            {
            match(">>"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "RSHIFT"

    // $ANTLR start "URSHIFT"
    public final void mURSHIFT() throws RecognitionException {
        try {
            int _type = URSHIFT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:45:9: ( '>>>' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:45:11: '>>>'
            {
            match(">>>"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "URSHIFT"

    // $ANTLR start "BACKSLASH"
    public final void mBACKSLASH() throws RecognitionException {
        try {
            int _type = BACKSLASH;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:46:11: ( '\\\\' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:46:13: '\\\\'
            {
            match('\\'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "BACKSLASH"

    // $ANTLR start "PERCENT"
    public final void mPERCENT() throws RecognitionException {
        try {
            int _type = PERCENT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:47:9: ( '%' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:47:11: '%'
            {
            match('%'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "PERCENT"

    // $ANTLR start "UMOD"
    public final void mUMOD() throws RecognitionException {
        try {
            int _type = UMOD;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:48:6: ( '%%' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:48:8: '%%'
            {
            match("%%"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "UMOD"

    // $ANTLR start "RETURNS"
    public final void mRETURNS() throws RecognitionException {
        try {
            int _type = RETURNS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:49:9: ( '=>' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:49:11: '=>'
            {
            match("=>"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "RETURNS"

    // $ANTLR start "NUMBER"
    public final void mNUMBER() throws RecognitionException {
        try {
            int _type = NUMBER;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:56:7: ( '0' .. '9' ( IDSUFFIX ( '.' IDSUFFIX )? ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:56:9: '0' .. '9' ( IDSUFFIX ( '.' IDSUFFIX )? )
            {
            matchRange('0','9'); 
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:56:18: ( IDSUFFIX ( '.' IDSUFFIX )? )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:56:19: IDSUFFIX ( '.' IDSUFFIX )?
            {
            mIDSUFFIX(); 
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:56:28: ( '.' IDSUFFIX )?
            int alt1=2;
            int LA1_0 = input.LA(1);

            if ( (LA1_0=='.') ) {
                alt1=1;
            }
            switch (alt1) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:56:30: '.' IDSUFFIX
                    {
                    match('.'); 
                    mIDSUFFIX(); 

                    }
                    break;

            }


            }


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "NUMBER"

    // $ANTLR start "ID"
    public final void mID() throws RecognitionException {
        try {
            int _type = ID;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:61:4: ( LETTERLIKE IDSUFFIX )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:61:6: LETTERLIKE IDSUFFIX
            {
            mLETTERLIKE(); 
            mIDSUFFIX(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "ID"

    // $ANTLR start "IDSUFFIX"
    public final void mIDSUFFIX() throws RecognitionException {
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:62:19: ( ( LETTERLIKE | DIGIT )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:62:21: ( LETTERLIKE | DIGIT )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:62:21: ( LETTERLIKE | DIGIT )*
            loop2:
            do {
                int alt2=2;
                int LA2_0 = input.LA(1);

                if ( ((LA2_0>='0' && LA2_0<='9')||(LA2_0>='A' && LA2_0<='Z')||LA2_0=='_'||(LA2_0>='a' && LA2_0<='z')) ) {
                    alt2=1;
                }


                switch (alt2) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:
            	    {
            	    if ( (input.LA(1)>='0' && input.LA(1)<='9')||(input.LA(1)>='A' && input.LA(1)<='Z')||input.LA(1)=='_'||(input.LA(1)>='a' && input.LA(1)<='z') ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    break loop2;
                }
            } while (true);


            }

        }
        finally {
        }
    }
    // $ANTLR end "IDSUFFIX"

    // $ANTLR start "LETTERLIKE"
    public final void mLETTERLIKE() throws RecognitionException {
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:63:20: ( 'a' .. 'z' | 'A' .. 'Z' | '_' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:
            {
            if ( (input.LA(1)>='A' && input.LA(1)<='Z')||input.LA(1)=='_'||(input.LA(1)>='a' && input.LA(1)<='z') ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "LETTERLIKE"

    // $ANTLR start "DIGIT"
    public final void mDIGIT() throws RecognitionException {
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:64:15: ( '0' .. '9' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:64:17: '0' .. '9'
            {
            matchRange('0','9'); 

            }

        }
        finally {
        }
    }
    // $ANTLR end "DIGIT"

    // $ANTLR start "CHAR_LITERAL"
    public final void mCHAR_LITERAL() throws RecognitionException {
        try {
            int _type = CHAR_LITERAL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:69:13: ( '\\'' (~ ( '\\'' ) )* '\\'' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:69:15: '\\'' (~ ( '\\'' ) )* '\\''
            {
            match('\''); 
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:69:20: (~ ( '\\'' ) )*
            loop3:
            do {
                int alt3=2;
                int LA3_0 = input.LA(1);

                if ( ((LA3_0>='\u0000' && LA3_0<='&')||(LA3_0>='(' && LA3_0<='\uFFFF')) ) {
                    alt3=1;
                }


                switch (alt3) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:69:20: ~ ( '\\'' )
            	    {
            	    if ( (input.LA(1)>='\u0000' && input.LA(1)<='&')||(input.LA(1)>='(' && input.LA(1)<='\uFFFF') ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    break loop3;
                }
            } while (true);

            match('\''); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "CHAR_LITERAL"

    // $ANTLR start "STRING_LITERAL"
    public final void mSTRING_LITERAL() throws RecognitionException {
        try {
            int _type = STRING_LITERAL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:70:15: ( '\"' (~ ( '\"' ) )* '\"' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:70:17: '\"' (~ ( '\"' ) )* '\"'
            {
            match('\"'); 
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:70:21: (~ ( '\"' ) )*
            loop4:
            do {
                int alt4=2;
                int LA4_0 = input.LA(1);

                if ( ((LA4_0>='\u0000' && LA4_0<='!')||(LA4_0>='#' && LA4_0<='\uFFFF')) ) {
                    alt4=1;
                }


                switch (alt4) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:70:21: ~ ( '\"' )
            	    {
            	    if ( (input.LA(1)>='\u0000' && input.LA(1)<='!')||(input.LA(1)>='#' && input.LA(1)<='\uFFFF') ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    break loop4;
                }
            } while (true);

            match('\"'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "STRING_LITERAL"

    // $ANTLR start "SPACE"
    public final void mSPACE() throws RecognitionException {
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:71:15: ( ' ' | '\\t' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:
            {
            if ( input.LA(1)=='\t'||input.LA(1)==' ' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "SPACE"

    // $ANTLR start "NEWLINE"
    public final void mNEWLINE() throws RecognitionException {
        try {
            int _type = NEWLINE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:76:8: ( ( ( '\\r' )? '\\n' )+ )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:76:10: ( ( '\\r' )? '\\n' )+
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:76:10: ( ( '\\r' )? '\\n' )+
            int cnt6=0;
            loop6:
            do {
                int alt6=2;
                int LA6_0 = input.LA(1);

                if ( (LA6_0=='\n'||LA6_0=='\r') ) {
                    alt6=1;
                }


                switch (alt6) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:76:11: ( '\\r' )? '\\n'
            	    {
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:76:11: ( '\\r' )?
            	    int alt5=2;
            	    int LA5_0 = input.LA(1);

            	    if ( (LA5_0=='\r') ) {
            	        alt5=1;
            	    }
            	    switch (alt5) {
            	        case 1 :
            	            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:76:11: '\\r'
            	            {
            	            match('\r'); 

            	            }
            	            break;

            	    }

            	    match('\n'); 

            	    }
            	    break;

            	default :
            	    if ( cnt6 >= 1 ) break loop6;
                        EarlyExitException eee =
                            new EarlyExitException(6, input);
                        throw eee;
                }
                cnt6++;
            } while (true);

             _channel = HIDDEN; 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "NEWLINE"

    // $ANTLR start "WS"
    public final void mWS() throws RecognitionException {
        try {
            int _type = WS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:77:5: ( ( ' ' | '\\t' )+ )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:77:9: ( ' ' | '\\t' )+
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:77:9: ( ' ' | '\\t' )+
            int cnt7=0;
            loop7:
            do {
                int alt7=2;
                int LA7_0 = input.LA(1);

                if ( (LA7_0=='\t'||LA7_0==' ') ) {
                    alt7=1;
                }


                switch (alt7) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:
            	    {
            	    if ( input.LA(1)=='\t'||input.LA(1)==' ' ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    if ( cnt7 >= 1 ) break loop7;
                        EarlyExitException eee =
                            new EarlyExitException(7, input);
                        throw eee;
                }
                cnt7++;
            } while (true);

             _channel = HIDDEN; 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "WS"

    // $ANTLR start "SINGLE_COMMENT"
    public final void mSINGLE_COMMENT() throws RecognitionException {
        try {
            int _type = SINGLE_COMMENT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:81:15: ( '//' (~ ( '\\r' | '\\n' ) )* NEWLINE )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:81:17: '//' (~ ( '\\r' | '\\n' ) )* NEWLINE
            {
            match("//"); 

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:81:22: (~ ( '\\r' | '\\n' ) )*
            loop8:
            do {
                int alt8=2;
                int LA8_0 = input.LA(1);

                if ( ((LA8_0>='\u0000' && LA8_0<='\t')||(LA8_0>='\u000B' && LA8_0<='\f')||(LA8_0>='\u000E' && LA8_0<='\uFFFF')) ) {
                    alt8=1;
                }


                switch (alt8) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:81:22: ~ ( '\\r' | '\\n' )
            	    {
            	    if ( (input.LA(1)>='\u0000' && input.LA(1)<='\t')||(input.LA(1)>='\u000B' && input.LA(1)<='\f')||(input.LA(1)>='\u000E' && input.LA(1)<='\uFFFF') ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    break loop8;
                }
            } while (true);

            mNEWLINE(); 
             skip(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "SINGLE_COMMENT"

    // $ANTLR start "MULTI_COMMENT"
    public final void mMULTI_COMMENT() throws RecognitionException {
        try {
            int _type = MULTI_COMMENT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:86:3: ( '/*' ( . )* '*/' ( NEWLINE )? )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:86:5: '/*' ( . )* '*/' ( NEWLINE )?
            {
            match("/*"); 

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:86:10: ( . )*
            loop9:
            do {
                int alt9=2;
                int LA9_0 = input.LA(1);

                if ( (LA9_0=='*') ) {
                    int LA9_1 = input.LA(2);

                    if ( (LA9_1=='/') ) {
                        alt9=2;
                    }
                    else if ( ((LA9_1>='\u0000' && LA9_1<='.')||(LA9_1>='0' && LA9_1<='\uFFFF')) ) {
                        alt9=1;
                    }


                }
                else if ( ((LA9_0>='\u0000' && LA9_0<=')')||(LA9_0>='+' && LA9_0<='\uFFFF')) ) {
                    alt9=1;
                }


                switch (alt9) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:86:10: .
            	    {
            	    matchAny(); 

            	    }
            	    break;

            	default :
            	    break loop9;
                }
            } while (true);

            match("*/"); 

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:86:18: ( NEWLINE )?
            int alt10=2;
            int LA10_0 = input.LA(1);

            if ( (LA10_0=='\n'||LA10_0=='\r') ) {
                alt10=1;
            }
            switch (alt10) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:86:18: NEWLINE
                    {
                    mNEWLINE(); 

                    }
                    break;

            }

             skip(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "MULTI_COMMENT"

    public void mTokens() throws RecognitionException {
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:1:8: ( LBRACE_LPAREN | LBRACE_STAR | COLON | COMMA | EQUALS | COLON_EQUALS | COLON_COLON_EQUALS | PLUS | MINUS | STAR | SLASH | LPAREN | RPAREN | LBRACE | RBRACE | LBRACKET | RBRACKET | HASH | EXCL | TILDE | AT | AMP | BAR | CARET | SEMI | QUESTION | COMPAND | COMPOR | COMPEQ | COMPNE | COMPGE | COMPLE | GREATER | LESS | LSHIFT | RSHIFT | URSHIFT | BACKSLASH | PERCENT | UMOD | RETURNS | NUMBER | ID | CHAR_LITERAL | STRING_LITERAL | NEWLINE | WS | SINGLE_COMMENT | MULTI_COMMENT )
        int alt11=49;
        alt11 = dfa11.predict(input);
        switch (alt11) {
            case 1 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:1:10: LBRACE_LPAREN
                {
                mLBRACE_LPAREN(); 

                }
                break;
            case 2 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:1:24: LBRACE_STAR
                {
                mLBRACE_STAR(); 

                }
                break;
            case 3 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:1:36: COLON
                {
                mCOLON(); 

                }
                break;
            case 4 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:1:42: COMMA
                {
                mCOMMA(); 

                }
                break;
            case 5 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:1:48: EQUALS
                {
                mEQUALS(); 

                }
                break;
            case 6 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:1:55: COLON_EQUALS
                {
                mCOLON_EQUALS(); 

                }
                break;
            case 7 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:1:68: COLON_COLON_EQUALS
                {
                mCOLON_COLON_EQUALS(); 

                }
                break;
            case 8 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:1:87: PLUS
                {
                mPLUS(); 

                }
                break;
            case 9 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:1:92: MINUS
                {
                mMINUS(); 

                }
                break;
            case 10 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:1:98: STAR
                {
                mSTAR(); 

                }
                break;
            case 11 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:1:103: SLASH
                {
                mSLASH(); 

                }
                break;
            case 12 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:1:109: LPAREN
                {
                mLPAREN(); 

                }
                break;
            case 13 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:1:116: RPAREN
                {
                mRPAREN(); 

                }
                break;
            case 14 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:1:123: LBRACE
                {
                mLBRACE(); 

                }
                break;
            case 15 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:1:130: RBRACE
                {
                mRBRACE(); 

                }
                break;
            case 16 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:1:137: LBRACKET
                {
                mLBRACKET(); 

                }
                break;
            case 17 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:1:146: RBRACKET
                {
                mRBRACKET(); 

                }
                break;
            case 18 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:1:155: HASH
                {
                mHASH(); 

                }
                break;
            case 19 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:1:160: EXCL
                {
                mEXCL(); 

                }
                break;
            case 20 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:1:165: TILDE
                {
                mTILDE(); 

                }
                break;
            case 21 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:1:171: AT
                {
                mAT(); 

                }
                break;
            case 22 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:1:174: AMP
                {
                mAMP(); 

                }
                break;
            case 23 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:1:178: BAR
                {
                mBAR(); 

                }
                break;
            case 24 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:1:182: CARET
                {
                mCARET(); 

                }
                break;
            case 25 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:1:188: SEMI
                {
                mSEMI(); 

                }
                break;
            case 26 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:1:193: QUESTION
                {
                mQUESTION(); 

                }
                break;
            case 27 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:1:202: COMPAND
                {
                mCOMPAND(); 

                }
                break;
            case 28 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:1:210: COMPOR
                {
                mCOMPOR(); 

                }
                break;
            case 29 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:1:217: COMPEQ
                {
                mCOMPEQ(); 

                }
                break;
            case 30 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:1:224: COMPNE
                {
                mCOMPNE(); 

                }
                break;
            case 31 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:1:231: COMPGE
                {
                mCOMPGE(); 

                }
                break;
            case 32 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:1:238: COMPLE
                {
                mCOMPLE(); 

                }
                break;
            case 33 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:1:245: GREATER
                {
                mGREATER(); 

                }
                break;
            case 34 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:1:253: LESS
                {
                mLESS(); 

                }
                break;
            case 35 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:1:258: LSHIFT
                {
                mLSHIFT(); 

                }
                break;
            case 36 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:1:265: RSHIFT
                {
                mRSHIFT(); 

                }
                break;
            case 37 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:1:272: URSHIFT
                {
                mURSHIFT(); 

                }
                break;
            case 38 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:1:280: BACKSLASH
                {
                mBACKSLASH(); 

                }
                break;
            case 39 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:1:290: PERCENT
                {
                mPERCENT(); 

                }
                break;
            case 40 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:1:298: UMOD
                {
                mUMOD(); 

                }
                break;
            case 41 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:1:303: RETURNS
                {
                mRETURNS(); 

                }
                break;
            case 42 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:1:311: NUMBER
                {
                mNUMBER(); 

                }
                break;
            case 43 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:1:318: ID
                {
                mID(); 

                }
                break;
            case 44 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:1:321: CHAR_LITERAL
                {
                mCHAR_LITERAL(); 

                }
                break;
            case 45 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:1:334: STRING_LITERAL
                {
                mSTRING_LITERAL(); 

                }
                break;
            case 46 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:1:349: NEWLINE
                {
                mNEWLINE(); 

                }
                break;
            case 47 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:1:357: WS
                {
                mWS(); 

                }
                break;
            case 48 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:1:360: SINGLE_COMMENT
                {
                mSINGLE_COMMENT(); 

                }
                break;
            case 49 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangLexer.g:1:375: MULTI_COMMENT
                {
                mMULTI_COMMENT(); 

                }
                break;

        }

    }


    protected DFA11 dfa11 = new DFA11(this);
    static final String DFA11_eotS =
        "\1\uffff\1\43\1\46\1\uffff\1\51\3\uffff\1\54\6\uffff\1\56\2\uffff"+
        "\1\60\1\62\3\uffff\1\65\1\70\1\uffff\1\72\31\uffff\1\74\10\uffff";
    static final String DFA11_eofS =
        "\75\uffff";
    static final String DFA11_minS =
        "\1\11\1\50\1\72\1\uffff\1\75\3\uffff\1\52\6\uffff\1\75\2\uffff\1"+
        "\46\1\174\3\uffff\1\75\1\74\1\uffff\1\45\31\uffff\1\76\10\uffff";
    static final String DFA11_maxS =
        "\1\176\1\52\1\75\1\uffff\1\76\3\uffff\1\57\6\uffff\1\75\2\uffff"+
        "\1\46\1\174\3\uffff\1\76\1\75\1\uffff\1\45\31\uffff\1\76\10\uffff";
    static final String DFA11_acceptS =
        "\3\uffff\1\4\1\uffff\1\10\1\11\1\12\1\uffff\1\14\1\15\1\17\1\20"+
        "\1\21\1\22\1\uffff\1\24\1\25\2\uffff\1\30\1\31\1\32\2\uffff\1\46"+
        "\1\uffff\1\52\1\53\1\54\1\55\1\56\1\57\1\1\1\2\1\16\1\6\1\7\1\3"+
        "\1\35\1\51\1\5\1\60\1\61\1\13\1\36\1\23\1\33\1\26\1\34\1\27\1\37"+
        "\1\uffff\1\41\1\40\1\43\1\42\1\50\1\47\1\45\1\44";
    static final String DFA11_specialS =
        "\75\uffff}>";
    static final String[] DFA11_transitionS = {
            "\1\40\1\37\2\uffff\1\37\22\uffff\1\40\1\17\1\36\1\16\1\uffff"+
            "\1\32\1\22\1\35\1\11\1\12\1\7\1\5\1\3\1\6\1\uffff\1\10\12\33"+
            "\1\2\1\25\1\30\1\4\1\27\1\26\1\21\32\34\1\14\1\31\1\15\1\24"+
            "\1\34\1\uffff\32\34\1\1\1\23\1\13\1\20",
            "\1\41\1\uffff\1\42",
            "\1\45\2\uffff\1\44",
            "",
            "\1\47\1\50",
            "",
            "",
            "",
            "\1\53\4\uffff\1\52",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\55",
            "",
            "",
            "\1\57",
            "\1\61",
            "",
            "",
            "",
            "\1\63\1\64",
            "\1\67\1\66",
            "",
            "\1\71",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\73",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA11_eot = DFA.unpackEncodedString(DFA11_eotS);
    static final short[] DFA11_eof = DFA.unpackEncodedString(DFA11_eofS);
    static final char[] DFA11_min = DFA.unpackEncodedStringToUnsignedChars(DFA11_minS);
    static final char[] DFA11_max = DFA.unpackEncodedStringToUnsignedChars(DFA11_maxS);
    static final short[] DFA11_accept = DFA.unpackEncodedString(DFA11_acceptS);
    static final short[] DFA11_special = DFA.unpackEncodedString(DFA11_specialS);
    static final short[][] DFA11_transition;

    static {
        int numStates = DFA11_transitionS.length;
        DFA11_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA11_transition[i] = DFA.unpackEncodedString(DFA11_transitionS[i]);
        }
    }

    class DFA11 extends DFA {

        public DFA11(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 11;
            this.eot = DFA11_eot;
            this.eof = DFA11_eof;
            this.min = DFA11_min;
            this.max = DFA11_max;
            this.accept = DFA11_accept;
            this.special = DFA11_special;
            this.transition = DFA11_transition;
        }
        public String getDescription() {
            return "1:1: Tokens : ( LBRACE_LPAREN | LBRACE_STAR | COLON | COMMA | EQUALS | COLON_EQUALS | COLON_COLON_EQUALS | PLUS | MINUS | STAR | SLASH | LPAREN | RPAREN | LBRACE | RBRACE | LBRACKET | RBRACKET | HASH | EXCL | TILDE | AT | AMP | BAR | CARET | SEMI | QUESTION | COMPAND | COMPOR | COMPEQ | COMPNE | COMPGE | COMPLE | GREATER | LESS | LSHIFT | RSHIFT | URSHIFT | BACKSLASH | PERCENT | UMOD | RETURNS | NUMBER | ID | CHAR_LITERAL | STRING_LITERAL | NEWLINE | WS | SINGLE_COMMENT | MULTI_COMMENT );";
        }
    }
 

}