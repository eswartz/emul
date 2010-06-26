// $ANTLR 3.2 Sep 23, 2009 12:02:23 /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g 2010-06-26 12:01:02

package org.ejs.eulang.parser;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

public class EulangLexer extends Lexer {
    public static final int CAST=32;
    public static final int CONDTEST=29;
    public static final int STAR=141;
    public static final int WHILE=112;
    public static final int GENERIC=71;
    public static final int MOD=42;
    public static final int POINTER=62;
    public static final int LSHIFT_EQ=105;
    public static final int PREDEC=48;
    public static final int REM_EQ=98;
    public static final int DEREF=63;
    public static final int MINUSMINUS=146;
    public static final int DO=111;
    public static final int ARGLIST=12;
    public static final int EQUALS=78;
    public static final int NOT=125;
    public static final int EOF=-1;
    public static final int BREAK=117;
    public static final int TYPE=22;
    public static final int CODE=9;
    public static final int LBRACKET=85;
    public static final int RPAREN=83;
    public static final int TUPLE=55;
    public static final int GREATER=106;
    public static final int STRING_LITERAL=149;
    public static final int ADDRREF=64;
    public static final int ADDSCOPE=5;
    public static final int EXTENDSCOPE=6;
    public static final int UREM_EQ=100;
    public static final int COMPLE=128;
    public static final int AND_EQ=102;
    public static final int CARET=94;
    public static final int XOR_EQ=104;
    public static final int LESS=131;
    public static final int INITEXPR=68;
    public static final int INITLIST=69;
    public static final int ATSIGN=118;
    public static final int GOTO=53;
    public static final int SELECT=160;
    public static final int CLSHIFT_EQ=109;
    public static final int ARRAY=60;
    public static final int LABELSTMT=57;
    public static final int CRSHIFT=26;
    public static final int RBRACE=82;
    public static final int STMTEXPR=23;
    public static final int STATIC=156;
    public static final int PERIOD=110;
    public static final int LSHIFT=137;
    public static final int INV=44;
    public static final int ADDROF=65;
    public static final int ELSE=122;
    public static final int NUMBER=147;
    public static final int UDIV=41;
    public static final int LIT=49;
    public static final int UDIV_EQ=99;
    public static final int CRSHIFT_EQ=27;
    public static final int LIST=21;
    public static final int PLUS_EQ=84;
    public static final int MUL=39;
    public static final int RSHIFT_EQ=25;
    public static final int ARGDEF=13;
    public static final int FI=123;
    public static final int MINUS_EQ=95;
    public static final int ELIF=121;
    public static final int WS=169;
    public static final int OR_EQ=103;
    public static final int BITOR=35;
    public static final int NIL=92;
    public static final int TYPEOF=67;
    public static final int UNTIL=162;
    public static final int STMTLIST=10;
    public static final int OR=124;
    public static final int SIZEOF=66;
    public static final int ALLOC=15;
    public static final int IDLIST=51;
    public static final int REPEAT=113;
    public static final int INLINE=31;
    public static final int CALL=30;
    public static final int POSTINC=45;
    public static final int END=164;
    public static final int FALSE=151;
    public static final int COMPULE=129;
    public static final int POSTDEC=46;
    public static final int MOD_EQ=101;
    public static final int REDEF=19;
    public static final int BINDING=58;
    public static final int FORWARD=73;
    public static final int BAR_BAR=159;
    public static final int AMP=136;
    public static final int PLUSPLUS=145;
    public static final int POINTS=158;
    public static final int UGREATER=133;
    public static final int LBRACE=81;
    public static final int FIELDREF=59;
    public static final int MULTI_COMMENT=171;
    public static final int FOR=88;
    public static final int SUB=38;
    public static final int ID=74;
    public static final int AND=114;
    public static final int DEFINE=18;
    public static final int UREM=144;
    public static final int BITAND=34;
    public static final int LPAREN=80;
    public static final int IF=119;
    public static final int COLONS=153;
    public static final int AT=116;
    public static final int COLON_COLON_EQUALS=155;
    public static final int INDEX=61;
    public static final int AS=150;
    public static final int CONDLIST=28;
    public static final int IDSUFFIX=165;
    public static final int EXPR=20;
    public static final int SLASH=142;
    public static final int IN=89;
    public static final int THEN=120;
    public static final int SCOPE=4;
    public static final int COMMA=75;
    public static final int PREINC=47;
    public static final int BITXOR=36;
    public static final int TILDE=135;
    public static final int PLUS=79;
    public static final int SINGLE_COMMENT=170;
    public static final int DIGIT=167;
    public static final int RBRACKET=86;
    public static final int RSHIFT=24;
    public static final int ATTR=90;
    public static final int WITH=163;
    public static final int EQUALS_COLON=87;
    public static final int ADD=37;
    public static final int COMPGE=107;
    public static final int URSHIFT_EQ=108;
    public static final int BY=115;
    public static final int ULESS=132;
    public static final int LIST_COMPREHENSION=8;
    public static final int LETTERLIKE=166;
    public static final int HASH=157;
    public static final int ATTRS=7;
    public static final int CLSHIFT=139;
    public static final int STAR_EQ=96;
    public static final int MINUS=140;
    public static final int REM=143;
    public static final int SEMI=72;
    public static final int TRUE=152;
    public static final int REF=14;
    public static final int COLON=77;
    public static final int TUPLETYPE=56;
    public static final int COLON_EQUALS=76;
    public static final int QUESTION=93;
    public static final int NEWLINE=168;
    public static final int CHAR_LITERAL=148;
    public static final int LABEL=52;
    public static final int WHEN=161;
    public static final int INSTANCE=70;
    public static final int BLOCK=54;
    public static final int NEG=43;
    public static final int ASSIGN=17;
    public static final int URSHIFT=138;
    public static final int ARROW=91;
    public static final int COMPEQ=126;
    public static final int IDREF=50;
    public static final int DIV=40;
    public static final int COND=33;
    public static final int PROTO=11;
    public static final int COMPNE=127;
    public static final int BAR=134;
    public static final int DATA=154;
    public static final int ALLOC_TUPLE=16;
    public static final int COMPUGE=130;
    public static final int SLASH_EQ=97;

      class EOFException extends RecognitionException {
        String message;
        EOFException(IntStream input, String message) {
           super(input);
           this.message = message;
        }
        public String toString() { return message; } 
      }
      


    // delegates
    // delegators

    public EulangLexer() {;} 
    public EulangLexer(CharStream input) {
        this(input, new RecognizerSharedState());
    }
    public EulangLexer(CharStream input, RecognizerSharedState state) {
        super(input,state);

    }
    public String getGrammarFileName() { return "/home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g"; }

    // $ANTLR start "FORWARD"
    public final void mFORWARD() throws RecognitionException {
        try {
            int _type = FORWARD;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:645:9: ( 'forward' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:645:11: 'forward'
            {
            match("forward"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "FORWARD"

    // $ANTLR start "STATIC"
    public final void mSTATIC() throws RecognitionException {
        try {
            int _type = STATIC;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:646:8: ( 'static' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:646:10: 'static'
            {
            match("static"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "STATIC"

    // $ANTLR start "COLON"
    public final void mCOLON() throws RecognitionException {
        try {
            int _type = COLON;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:648:7: ( ':' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:648:9: ':'
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:649:7: ( ',' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:649:9: ','
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:650:8: ( '=' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:650:10: '='
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

    // $ANTLR start "EQUALS_COLON"
    public final void mEQUALS_COLON() throws RecognitionException {
        try {
            int _type = EQUALS_COLON;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:651:14: ( '=:' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:651:16: '=:'
            {
            match("=:"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "EQUALS_COLON"

    // $ANTLR start "COLON_EQUALS"
    public final void mCOLON_EQUALS() throws RecognitionException {
        try {
            int _type = COLON_EQUALS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:652:14: ( ':=' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:652:16: ':='
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:653:20: ( '::=' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:653:22: '::='
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:654:6: ( '+' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:654:8: '+'
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

    // $ANTLR start "PLUS_EQ"
    public final void mPLUS_EQ() throws RecognitionException {
        try {
            int _type = PLUS_EQ;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:655:9: ( '+=' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:655:11: '+='
            {
            match("+="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "PLUS_EQ"

    // $ANTLR start "MINUS"
    public final void mMINUS() throws RecognitionException {
        try {
            int _type = MINUS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:656:7: ( '-' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:656:9: '-'
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

    // $ANTLR start "MINUS_EQ"
    public final void mMINUS_EQ() throws RecognitionException {
        try {
            int _type = MINUS_EQ;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:657:10: ( '-=' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:657:12: '-='
            {
            match("-="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "MINUS_EQ"

    // $ANTLR start "STAR"
    public final void mSTAR() throws RecognitionException {
        try {
            int _type = STAR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:658:6: ( '*' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:658:8: '*'
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

    // $ANTLR start "STAR_EQ"
    public final void mSTAR_EQ() throws RecognitionException {
        try {
            int _type = STAR_EQ;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:659:9: ( '*=' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:659:11: '*='
            {
            match("*="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "STAR_EQ"

    // $ANTLR start "SLASH"
    public final void mSLASH() throws RecognitionException {
        try {
            int _type = SLASH;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:660:7: ( '/' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:660:9: '/'
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

    // $ANTLR start "SLASH_EQ"
    public final void mSLASH_EQ() throws RecognitionException {
        try {
            int _type = SLASH_EQ;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:661:10: ( '/=' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:661:12: '/='
            {
            match("/="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "SLASH_EQ"

    // $ANTLR start "UDIV"
    public final void mUDIV() throws RecognitionException {
        try {
            int _type = UDIV;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:662:6: ( '+/' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:662:8: '+/'
            {
            match("+/"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "UDIV"

    // $ANTLR start "UDIV_EQ"
    public final void mUDIV_EQ() throws RecognitionException {
        try {
            int _type = UDIV_EQ;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:663:9: ( '+/=' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:663:11: '+/='
            {
            match("+/="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "UDIV_EQ"

    // $ANTLR start "REM"
    public final void mREM() throws RecognitionException {
        try {
            int _type = REM;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:664:5: ( '\\\\' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:664:7: '\\\\'
            {
            match('\\'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "REM"

    // $ANTLR start "REM_EQ"
    public final void mREM_EQ() throws RecognitionException {
        try {
            int _type = REM_EQ;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:665:8: ( '\\\\=' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:665:10: '\\\\='
            {
            match("\\="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "REM_EQ"

    // $ANTLR start "UREM"
    public final void mUREM() throws RecognitionException {
        try {
            int _type = UREM;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:666:6: ( '+\\\\' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:666:8: '+\\\\'
            {
            match("+\\"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "UREM"

    // $ANTLR start "UREM_EQ"
    public final void mUREM_EQ() throws RecognitionException {
        try {
            int _type = UREM_EQ;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:667:9: ( '+\\\\=' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:667:11: '+\\\\='
            {
            match("+\\="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "UREM_EQ"

    // $ANTLR start "MOD"
    public final void mMOD() throws RecognitionException {
        try {
            int _type = MOD;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:668:5: ( '%' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:668:7: '%'
            {
            match('%'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "MOD"

    // $ANTLR start "MOD_EQ"
    public final void mMOD_EQ() throws RecognitionException {
        try {
            int _type = MOD_EQ;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:669:8: ( '%=' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:669:10: '%='
            {
            match("%="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "MOD_EQ"

    // $ANTLR start "LPAREN"
    public final void mLPAREN() throws RecognitionException {
        try {
            int _type = LPAREN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:670:8: ( '(' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:670:10: '('
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:671:8: ( ')' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:671:10: ')'
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:672:8: ( '{' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:672:10: '{'
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:673:8: ( '}' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:673:10: '}'
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:674:10: ( '[' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:674:12: '['
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:675:10: ( ']' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:675:12: ']'
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:676:6: ( '#' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:676:8: '#'
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

    // $ANTLR start "NOT"
    public final void mNOT() throws RecognitionException {
        try {
            int _type = NOT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:678:5: ( 'not' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:678:7: 'not'
            {
            match("not"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "NOT"

    // $ANTLR start "TILDE"
    public final void mTILDE() throws RecognitionException {
        try {
            int _type = TILDE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:679:7: ( '~' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:679:9: '~'
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

    // $ANTLR start "ATSIGN"
    public final void mATSIGN() throws RecognitionException {
        try {
            int _type = ATSIGN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:680:8: ( '@' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:680:10: '@'
            {
            match('@'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "ATSIGN"

    // $ANTLR start "AMP"
    public final void mAMP() throws RecognitionException {
        try {
            int _type = AMP;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:681:5: ( '&' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:681:7: '&'
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:682:5: ( '|' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:682:7: '|'
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:683:7: ( '^' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:683:9: '^'
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:684:6: ( ';' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:684:8: ';'
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:685:10: ( '?' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:685:12: '?'
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

    // $ANTLR start "AND"
    public final void mAND() throws RecognitionException {
        try {
            int _type = AND;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:686:5: ( 'and' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:686:7: 'and'
            {
            match("and"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "AND"

    // $ANTLR start "AND_EQ"
    public final void mAND_EQ() throws RecognitionException {
        try {
            int _type = AND_EQ;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:687:8: ( '&=' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:687:10: '&='
            {
            match("&="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "AND_EQ"

    // $ANTLR start "OR"
    public final void mOR() throws RecognitionException {
        try {
            int _type = OR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:688:4: ( 'or' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:688:6: 'or'
            {
            match("or"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "OR"

    // $ANTLR start "OR_EQ"
    public final void mOR_EQ() throws RecognitionException {
        try {
            int _type = OR_EQ;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:689:7: ( '|=' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:689:9: '|='
            {
            match("|="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "OR_EQ"

    // $ANTLR start "XOR_EQ"
    public final void mXOR_EQ() throws RecognitionException {
        try {
            int _type = XOR_EQ;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:691:8: ( '~=' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:691:10: '~='
            {
            match("~="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "XOR_EQ"

    // $ANTLR start "COMPEQ"
    public final void mCOMPEQ() throws RecognitionException {
        try {
            int _type = COMPEQ;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:692:8: ( '==' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:692:10: '=='
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:693:8: ( '!=' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:693:10: '!='
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:694:8: ( '>=' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:694:10: '>='
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

    // $ANTLR start "COMPUGE"
    public final void mCOMPUGE() throws RecognitionException {
        try {
            int _type = COMPUGE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:695:9: ( '+>=' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:695:11: '+>='
            {
            match("+>="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "COMPUGE"

    // $ANTLR start "COMPLE"
    public final void mCOMPLE() throws RecognitionException {
        try {
            int _type = COMPLE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:696:8: ( '<=' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:696:10: '<='
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

    // $ANTLR start "COMPULE"
    public final void mCOMPULE() throws RecognitionException {
        try {
            int _type = COMPULE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:697:9: ( '+<=' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:697:11: '+<='
            {
            match("+<="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "COMPULE"

    // $ANTLR start "GREATER"
    public final void mGREATER() throws RecognitionException {
        try {
            int _type = GREATER;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:698:9: ( '>' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:698:11: '>'
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

    // $ANTLR start "UGREATER"
    public final void mUGREATER() throws RecognitionException {
        try {
            int _type = UGREATER;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:699:10: ( '+>' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:699:12: '+>'
            {
            match("+>"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "UGREATER"

    // $ANTLR start "LESS"
    public final void mLESS() throws RecognitionException {
        try {
            int _type = LESS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:700:6: ( '<' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:700:8: '<'
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

    // $ANTLR start "ULESS"
    public final void mULESS() throws RecognitionException {
        try {
            int _type = ULESS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:701:7: ( '+<' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:701:9: '+<'
            {
            match("+<"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "ULESS"

    // $ANTLR start "LSHIFT"
    public final void mLSHIFT() throws RecognitionException {
        try {
            int _type = LSHIFT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:702:8: ( '<<' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:702:10: '<<'
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

    // $ANTLR start "LSHIFT_EQ"
    public final void mLSHIFT_EQ() throws RecognitionException {
        try {
            int _type = LSHIFT_EQ;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:703:11: ( '<<=' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:703:13: '<<='
            {
            match("<<="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LSHIFT_EQ"

    // $ANTLR start "URSHIFT"
    public final void mURSHIFT() throws RecognitionException {
        try {
            int _type = URSHIFT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:706:9: ( '+>>' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:706:11: '+>>'
            {
            match("+>>"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "URSHIFT"

    // $ANTLR start "URSHIFT_EQ"
    public final void mURSHIFT_EQ() throws RecognitionException {
        try {
            int _type = URSHIFT_EQ;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:707:12: ( '+>>=' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:707:14: '+>>='
            {
            match("+>>="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "URSHIFT_EQ"

    // $ANTLR start "CLSHIFT"
    public final void mCLSHIFT() throws RecognitionException {
        try {
            int _type = CLSHIFT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:710:9: ( '<<|' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:710:11: '<<|'
            {
            match("<<|"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "CLSHIFT"

    // $ANTLR start "CLSHIFT_EQ"
    public final void mCLSHIFT_EQ() throws RecognitionException {
        try {
            int _type = CLSHIFT_EQ;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:711:12: ( '<<|=' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:711:14: '<<|='
            {
            match("<<|="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "CLSHIFT_EQ"

    // $ANTLR start "ARROW"
    public final void mARROW() throws RecognitionException {
        try {
            int _type = ARROW;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:712:7: ( '=>' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:712:9: '=>'
            {
            match("=>"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "ARROW"

    // $ANTLR start "PERIOD"
    public final void mPERIOD() throws RecognitionException {
        try {
            int _type = PERIOD;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:713:8: ( '.' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:713:10: '.'
            {
            match('.'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "PERIOD"

    // $ANTLR start "PLUSPLUS"
    public final void mPLUSPLUS() throws RecognitionException {
        try {
            int _type = PLUSPLUS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:714:10: ( '++' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:714:12: '++'
            {
            match("++"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "PLUSPLUS"

    // $ANTLR start "MINUSMINUS"
    public final void mMINUSMINUS() throws RecognitionException {
        try {
            int _type = MINUSMINUS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:715:12: ( '--' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:715:14: '--'
            {
            match("--"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "MINUSMINUS"

    // $ANTLR start "POINTS"
    public final void mPOINTS() throws RecognitionException {
        try {
            int _type = POINTS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:717:8: ( '->' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:717:10: '->'
            {
            match("->"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "POINTS"

    // $ANTLR start "BAR_BAR"
    public final void mBAR_BAR() throws RecognitionException {
        try {
            int _type = BAR_BAR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:718:9: ( '||' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:718:11: '||'
            {
            match("||"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "BAR_BAR"

    // $ANTLR start "SELECT"
    public final void mSELECT() throws RecognitionException {
        try {
            int _type = SELECT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:720:8: ( 'select' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:720:10: 'select'
            {
            match("select"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "SELECT"

    // $ANTLR start "COND"
    public final void mCOND() throws RecognitionException {
        try {
            int _type = COND;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:722:5: ( 'cond' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:722:7: 'cond'
            {
            match("cond"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "COND"

    // $ANTLR start "IF"
    public final void mIF() throws RecognitionException {
        try {
            int _type = IF;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:723:3: ( 'if' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:723:5: 'if'
            {
            match("if"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "IF"

    // $ANTLR start "THEN"
    public final void mTHEN() throws RecognitionException {
        try {
            int _type = THEN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:724:6: ( 'then' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:724:8: 'then'
            {
            match("then"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "THEN"

    // $ANTLR start "ELSE"
    public final void mELSE() throws RecognitionException {
        try {
            int _type = ELSE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:725:6: ( 'else' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:725:8: 'else'
            {
            match("else"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "ELSE"

    // $ANTLR start "ELIF"
    public final void mELIF() throws RecognitionException {
        try {
            int _type = ELIF;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:726:6: ( 'elif' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:726:8: 'elif'
            {
            match("elif"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "ELIF"

    // $ANTLR start "FI"
    public final void mFI() throws RecognitionException {
        try {
            int _type = FI;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:727:4: ( 'fi' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:727:6: 'fi'
            {
            match("fi"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "FI"

    // $ANTLR start "DO"
    public final void mDO() throws RecognitionException {
        try {
            int _type = DO;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:728:4: ( 'do' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:728:6: 'do'
            {
            match("do"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "DO"

    // $ANTLR start "WHILE"
    public final void mWHILE() throws RecognitionException {
        try {
            int _type = WHILE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:729:7: ( 'while' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:729:9: 'while'
            {
            match("while"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "WHILE"

    // $ANTLR start "AT"
    public final void mAT() throws RecognitionException {
        try {
            int _type = AT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:730:4: ( 'at' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:730:6: 'at'
            {
            match("at"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "AT"

    // $ANTLR start "WHEN"
    public final void mWHEN() throws RecognitionException {
        try {
            int _type = WHEN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:731:6: ( 'when' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:731:8: 'when'
            {
            match("when"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "WHEN"

    // $ANTLR start "UNTIL"
    public final void mUNTIL() throws RecognitionException {
        try {
            int _type = UNTIL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:732:7: ( 'until' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:732:9: 'until'
            {
            match("until"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "UNTIL"

    // $ANTLR start "BREAK"
    public final void mBREAK() throws RecognitionException {
        try {
            int _type = BREAK;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:733:7: ( 'break' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:733:9: 'break'
            {
            match("break"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "BREAK"

    // $ANTLR start "REPEAT"
    public final void mREPEAT() throws RecognitionException {
        try {
            int _type = REPEAT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:734:8: ( 'repeat' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:734:10: 'repeat'
            {
            match("repeat"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "REPEAT"

    // $ANTLR start "BY"
    public final void mBY() throws RecognitionException {
        try {
            int _type = BY;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:735:4: ( 'by' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:735:6: 'by'
            {
            match("by"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "BY"

    // $ANTLR start "CODE"
    public final void mCODE() throws RecognitionException {
        try {
            int _type = CODE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:738:6: ( 'code' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:738:8: 'code'
            {
            match("code"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "CODE"

    // $ANTLR start "DATA"
    public final void mDATA() throws RecognitionException {
        try {
            int _type = DATA;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:739:6: ( 'data' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:739:8: 'data'
            {
            match("data"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "DATA"

    // $ANTLR start "FOR"
    public final void mFOR() throws RecognitionException {
        try {
            int _type = FOR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:742:5: ( 'for' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:742:7: 'for'
            {
            match("for"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "FOR"

    // $ANTLR start "IN"
    public final void mIN() throws RecognitionException {
        try {
            int _type = IN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:743:4: ( 'in' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:743:6: 'in'
            {
            match("in"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "IN"

    // $ANTLR start "GOTO"
    public final void mGOTO() throws RecognitionException {
        try {
            int _type = GOTO;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:744:5: ( 'goto' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:744:7: 'goto'
            {
            match("goto"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "GOTO"

    // $ANTLR start "FALSE"
    public final void mFALSE() throws RecognitionException {
        try {
            int _type = FALSE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:745:6: ( 'false' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:745:8: 'false'
            {
            match("false"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "FALSE"

    // $ANTLR start "TRUE"
    public final void mTRUE() throws RecognitionException {
        try {
            int _type = TRUE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:746:5: ( 'true' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:746:7: 'true'
            {
            match("true"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "TRUE"

    // $ANTLR start "NIL"
    public final void mNIL() throws RecognitionException {
        try {
            int _type = NIL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:747:4: ( 'nil' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:747:6: 'nil'
            {
            match("nil"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "NIL"

    // $ANTLR start "WITH"
    public final void mWITH() throws RecognitionException {
        try {
            int _type = WITH;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:749:5: ( 'with' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:749:7: 'with'
            {
            match("with"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "WITH"

    // $ANTLR start "AS"
    public final void mAS() throws RecognitionException {
        try {
            int _type = AS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:750:3: ( 'as' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:750:5: 'as'
            {
            match("as"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "AS"

    // $ANTLR start "END"
    public final void mEND() throws RecognitionException {
        try {
            int _type = END;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:751:4: ( 'end' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:751:6: 'end'
            {
            match("end"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "END"

    // $ANTLR start "SIZEOF"
    public final void mSIZEOF() throws RecognitionException {
        try {
            int _type = SIZEOF;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:753:7: ( 'sizeof' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:753:9: 'sizeof'
            {
            match("sizeof"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "SIZEOF"

    // $ANTLR start "TYPEOF"
    public final void mTYPEOF() throws RecognitionException {
        try {
            int _type = TYPEOF;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:754:7: ( 'typeof' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:754:9: 'typeof'
            {
            match("typeof"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "TYPEOF"

    // $ANTLR start "NUMBER"
    public final void mNUMBER() throws RecognitionException {
        try {
            int _type = NUMBER;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:759:7: ( '0' .. '9' ( IDSUFFIX ( '.' IDSUFFIX )? ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:759:9: '0' .. '9' ( IDSUFFIX ( '.' IDSUFFIX )? )
            {
            matchRange('0','9'); 
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:759:18: ( IDSUFFIX ( '.' IDSUFFIX )? )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:759:19: IDSUFFIX ( '.' IDSUFFIX )?
            {
            mIDSUFFIX(); 
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:759:28: ( '.' IDSUFFIX )?
            int alt1=2;
            int LA1_0 = input.LA(1);

            if ( (LA1_0=='.') ) {
                alt1=1;
            }
            switch (alt1) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:759:30: '.' IDSUFFIX
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

    // $ANTLR start "COLONS"
    public final void mCOLONS() throws RecognitionException {
        try {
            int _type = COLONS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:766:8: ( COLON ( COLON )+ )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:766:10: COLON ( COLON )+
            {
            mCOLON(); 
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:766:16: ( COLON )+
            int cnt2=0;
            loop2:
            do {
                int alt2=2;
                int LA2_0 = input.LA(1);

                if ( (LA2_0==':') ) {
                    alt2=1;
                }


                switch (alt2) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:766:16: COLON
            	    {
            	    mCOLON(); 

            	    }
            	    break;

            	default :
            	    if ( cnt2 >= 1 ) break loop2;
                        EarlyExitException eee =
                            new EarlyExitException(2, input);
                        throw eee;
                }
                cnt2++;
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "COLONS"

    // $ANTLR start "ATTR"
    public final void mATTR() throws RecognitionException {
        try {
            int _type = ATTR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:768:6: ( '#' ID )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:768:8: '#' ID
            {
            match('#'); 
            mID(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "ATTR"

    // $ANTLR start "ID"
    public final void mID() throws RecognitionException {
        try {
            int _type = ID;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:770:4: ( LETTERLIKE IDSUFFIX )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:770:6: LETTERLIKE IDSUFFIX
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:771:19: ( ( LETTERLIKE | DIGIT )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:771:21: ( LETTERLIKE | DIGIT )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:771:21: ( LETTERLIKE | DIGIT )*
            loop3:
            do {
                int alt3=2;
                int LA3_0 = input.LA(1);

                if ( ((LA3_0>='0' && LA3_0<='9')||(LA3_0>='A' && LA3_0<='Z')||LA3_0=='_'||(LA3_0>='a' && LA3_0<='z')) ) {
                    alt3=1;
                }


                switch (alt3) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:
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
            	    break loop3;
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:772:20: ( 'a' .. 'z' | 'A' .. 'Z' | '_' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:773:15: ( '0' .. '9' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:773:17: '0' .. '9'
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:779:14: ( '\\'' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:779:16: '\\''
            {
            match('\''); 

              while (true) {
            		 int ch = input.LA(1);
            		 if (ch == '\\') {
            		    input.consume();
            		    input.consume();
            		 } else if (ch == -1) {
                    match('\'');
            		 } else if (ch != '\'') {
            		    input.consume();
            		 } else {
            		    match('\'');
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
    // $ANTLR end "CHAR_LITERAL"

    // $ANTLR start "STRING_LITERAL"
    public final void mSTRING_LITERAL() throws RecognitionException {
        try {
            int _type = STRING_LITERAL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:797:15: ( '\"' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:797:17: '\"'
            {
            match('\"'); 

              while (true) {
            		 int ch = input.LA(1);
            		 if (ch == '\\') {
            		    input.consume();
            		    input.consume();
                 } else if (ch == -1) {
                    match('\"');
            		 } else if (ch != '\"') {
            		    input.consume();
            		 } else {
            		    match('\"');
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
    // $ANTLR end "STRING_LITERAL"

    // $ANTLR start "NEWLINE"
    public final void mNEWLINE() throws RecognitionException {
        try {
            int _type = NEWLINE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:816:8: ( ( ( '\\r' )? '\\n' )+ )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:816:10: ( ( '\\r' )? '\\n' )+
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:816:10: ( ( '\\r' )? '\\n' )+
            int cnt5=0;
            loop5:
            do {
                int alt5=2;
                int LA5_0 = input.LA(1);

                if ( (LA5_0=='\n'||LA5_0=='\r') ) {
                    alt5=1;
                }


                switch (alt5) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:816:11: ( '\\r' )? '\\n'
            	    {
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:816:11: ( '\\r' )?
            	    int alt4=2;
            	    int LA4_0 = input.LA(1);

            	    if ( (LA4_0=='\r') ) {
            	        alt4=1;
            	    }
            	    switch (alt4) {
            	        case 1 :
            	            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:816:11: '\\r'
            	            {
            	            match('\r'); 

            	            }
            	            break;

            	    }

            	    match('\n'); 

            	    }
            	    break;

            	default :
            	    if ( cnt5 >= 1 ) break loop5;
                        EarlyExitException eee =
                            new EarlyExitException(5, input);
                        throw eee;
                }
                cnt5++;
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:817:5: ( ( ' ' | '\\t' )+ )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:817:9: ( ' ' | '\\t' )+
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:817:9: ( ' ' | '\\t' )+
            int cnt6=0;
            loop6:
            do {
                int alt6=2;
                int LA6_0 = input.LA(1);

                if ( (LA6_0=='\t'||LA6_0==' ') ) {
                    alt6=1;
                }


                switch (alt6) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:
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
    // $ANTLR end "WS"

    // $ANTLR start "SINGLE_COMMENT"
    public final void mSINGLE_COMMENT() throws RecognitionException {
        try {
            int _type = SINGLE_COMMENT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:821:15: ( '//' (~ ( '\\r' | '\\n' ) )* NEWLINE )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:821:17: '//' (~ ( '\\r' | '\\n' ) )* NEWLINE
            {
            match("//"); 

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:821:22: (~ ( '\\r' | '\\n' ) )*
            loop7:
            do {
                int alt7=2;
                int LA7_0 = input.LA(1);

                if ( ((LA7_0>='\u0000' && LA7_0<='\t')||(LA7_0>='\u000B' && LA7_0<='\f')||(LA7_0>='\u000E' && LA7_0<='\uFFFF')) ) {
                    alt7=1;
                }


                switch (alt7) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:821:22: ~ ( '\\r' | '\\n' )
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
            	    break loop7;
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:826:3: ( '/*' ( . )* '*/' ( NEWLINE )? )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:826:5: '/*' ( . )* '*/' ( NEWLINE )?
            {
            match("/*"); 

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:826:10: ( . )*
            loop8:
            do {
                int alt8=2;
                int LA8_0 = input.LA(1);

                if ( (LA8_0=='*') ) {
                    int LA8_1 = input.LA(2);

                    if ( (LA8_1=='/') ) {
                        alt8=2;
                    }
                    else if ( ((LA8_1>='\u0000' && LA8_1<='.')||(LA8_1>='0' && LA8_1<='\uFFFF')) ) {
                        alt8=1;
                    }


                }
                else if ( ((LA8_0>='\u0000' && LA8_0<=')')||(LA8_0>='+' && LA8_0<='\uFFFF')) ) {
                    alt8=1;
                }


                switch (alt8) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:826:10: .
            	    {
            	    matchAny(); 

            	    }
            	    break;

            	default :
            	    break loop8;
                }
            } while (true);

            match("*/"); 

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:826:18: ( NEWLINE )?
            int alt9=2;
            int LA9_0 = input.LA(1);

            if ( (LA9_0=='\n'||LA9_0=='\r') ) {
                alt9=1;
            }
            switch (alt9) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:826:18: NEWLINE
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
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:8: ( FORWARD | STATIC | COLON | COMMA | EQUALS | EQUALS_COLON | COLON_EQUALS | COLON_COLON_EQUALS | PLUS | PLUS_EQ | MINUS | MINUS_EQ | STAR | STAR_EQ | SLASH | SLASH_EQ | UDIV | UDIV_EQ | REM | REM_EQ | UREM | UREM_EQ | MOD | MOD_EQ | LPAREN | RPAREN | LBRACE | RBRACE | LBRACKET | RBRACKET | HASH | NOT | TILDE | ATSIGN | AMP | BAR | CARET | SEMI | QUESTION | AND | AND_EQ | OR | OR_EQ | XOR_EQ | COMPEQ | COMPNE | COMPGE | COMPUGE | COMPLE | COMPULE | GREATER | UGREATER | LESS | ULESS | LSHIFT | LSHIFT_EQ | URSHIFT | URSHIFT_EQ | CLSHIFT | CLSHIFT_EQ | ARROW | PERIOD | PLUSPLUS | MINUSMINUS | POINTS | BAR_BAR | SELECT | COND | IF | THEN | ELSE | ELIF | FI | DO | WHILE | AT | WHEN | UNTIL | BREAK | REPEAT | BY | CODE | DATA | FOR | IN | GOTO | FALSE | TRUE | NIL | WITH | AS | END | SIZEOF | TYPEOF | NUMBER | COLONS | ATTR | ID | CHAR_LITERAL | STRING_LITERAL | NEWLINE | WS | SINGLE_COMMENT | MULTI_COMMENT )
        int alt10=104;
        alt10 = dfa10.predict(input);
        switch (alt10) {
            case 1 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:10: FORWARD
                {
                mFORWARD(); 

                }
                break;
            case 2 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:18: STATIC
                {
                mSTATIC(); 

                }
                break;
            case 3 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:25: COLON
                {
                mCOLON(); 

                }
                break;
            case 4 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:31: COMMA
                {
                mCOMMA(); 

                }
                break;
            case 5 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:37: EQUALS
                {
                mEQUALS(); 

                }
                break;
            case 6 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:44: EQUALS_COLON
                {
                mEQUALS_COLON(); 

                }
                break;
            case 7 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:57: COLON_EQUALS
                {
                mCOLON_EQUALS(); 

                }
                break;
            case 8 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:70: COLON_COLON_EQUALS
                {
                mCOLON_COLON_EQUALS(); 

                }
                break;
            case 9 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:89: PLUS
                {
                mPLUS(); 

                }
                break;
            case 10 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:94: PLUS_EQ
                {
                mPLUS_EQ(); 

                }
                break;
            case 11 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:102: MINUS
                {
                mMINUS(); 

                }
                break;
            case 12 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:108: MINUS_EQ
                {
                mMINUS_EQ(); 

                }
                break;
            case 13 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:117: STAR
                {
                mSTAR(); 

                }
                break;
            case 14 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:122: STAR_EQ
                {
                mSTAR_EQ(); 

                }
                break;
            case 15 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:130: SLASH
                {
                mSLASH(); 

                }
                break;
            case 16 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:136: SLASH_EQ
                {
                mSLASH_EQ(); 

                }
                break;
            case 17 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:145: UDIV
                {
                mUDIV(); 

                }
                break;
            case 18 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:150: UDIV_EQ
                {
                mUDIV_EQ(); 

                }
                break;
            case 19 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:158: REM
                {
                mREM(); 

                }
                break;
            case 20 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:162: REM_EQ
                {
                mREM_EQ(); 

                }
                break;
            case 21 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:169: UREM
                {
                mUREM(); 

                }
                break;
            case 22 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:174: UREM_EQ
                {
                mUREM_EQ(); 

                }
                break;
            case 23 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:182: MOD
                {
                mMOD(); 

                }
                break;
            case 24 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:186: MOD_EQ
                {
                mMOD_EQ(); 

                }
                break;
            case 25 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:193: LPAREN
                {
                mLPAREN(); 

                }
                break;
            case 26 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:200: RPAREN
                {
                mRPAREN(); 

                }
                break;
            case 27 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:207: LBRACE
                {
                mLBRACE(); 

                }
                break;
            case 28 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:214: RBRACE
                {
                mRBRACE(); 

                }
                break;
            case 29 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:221: LBRACKET
                {
                mLBRACKET(); 

                }
                break;
            case 30 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:230: RBRACKET
                {
                mRBRACKET(); 

                }
                break;
            case 31 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:239: HASH
                {
                mHASH(); 

                }
                break;
            case 32 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:244: NOT
                {
                mNOT(); 

                }
                break;
            case 33 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:248: TILDE
                {
                mTILDE(); 

                }
                break;
            case 34 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:254: ATSIGN
                {
                mATSIGN(); 

                }
                break;
            case 35 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:261: AMP
                {
                mAMP(); 

                }
                break;
            case 36 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:265: BAR
                {
                mBAR(); 

                }
                break;
            case 37 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:269: CARET
                {
                mCARET(); 

                }
                break;
            case 38 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:275: SEMI
                {
                mSEMI(); 

                }
                break;
            case 39 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:280: QUESTION
                {
                mQUESTION(); 

                }
                break;
            case 40 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:289: AND
                {
                mAND(); 

                }
                break;
            case 41 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:293: AND_EQ
                {
                mAND_EQ(); 

                }
                break;
            case 42 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:300: OR
                {
                mOR(); 

                }
                break;
            case 43 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:303: OR_EQ
                {
                mOR_EQ(); 

                }
                break;
            case 44 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:309: XOR_EQ
                {
                mXOR_EQ(); 

                }
                break;
            case 45 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:316: COMPEQ
                {
                mCOMPEQ(); 

                }
                break;
            case 46 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:323: COMPNE
                {
                mCOMPNE(); 

                }
                break;
            case 47 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:330: COMPGE
                {
                mCOMPGE(); 

                }
                break;
            case 48 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:337: COMPUGE
                {
                mCOMPUGE(); 

                }
                break;
            case 49 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:345: COMPLE
                {
                mCOMPLE(); 

                }
                break;
            case 50 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:352: COMPULE
                {
                mCOMPULE(); 

                }
                break;
            case 51 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:360: GREATER
                {
                mGREATER(); 

                }
                break;
            case 52 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:368: UGREATER
                {
                mUGREATER(); 

                }
                break;
            case 53 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:377: LESS
                {
                mLESS(); 

                }
                break;
            case 54 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:382: ULESS
                {
                mULESS(); 

                }
                break;
            case 55 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:388: LSHIFT
                {
                mLSHIFT(); 

                }
                break;
            case 56 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:395: LSHIFT_EQ
                {
                mLSHIFT_EQ(); 

                }
                break;
            case 57 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:405: URSHIFT
                {
                mURSHIFT(); 

                }
                break;
            case 58 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:413: URSHIFT_EQ
                {
                mURSHIFT_EQ(); 

                }
                break;
            case 59 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:424: CLSHIFT
                {
                mCLSHIFT(); 

                }
                break;
            case 60 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:432: CLSHIFT_EQ
                {
                mCLSHIFT_EQ(); 

                }
                break;
            case 61 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:443: ARROW
                {
                mARROW(); 

                }
                break;
            case 62 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:449: PERIOD
                {
                mPERIOD(); 

                }
                break;
            case 63 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:456: PLUSPLUS
                {
                mPLUSPLUS(); 

                }
                break;
            case 64 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:465: MINUSMINUS
                {
                mMINUSMINUS(); 

                }
                break;
            case 65 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:476: POINTS
                {
                mPOINTS(); 

                }
                break;
            case 66 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:483: BAR_BAR
                {
                mBAR_BAR(); 

                }
                break;
            case 67 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:491: SELECT
                {
                mSELECT(); 

                }
                break;
            case 68 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:498: COND
                {
                mCOND(); 

                }
                break;
            case 69 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:503: IF
                {
                mIF(); 

                }
                break;
            case 70 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:506: THEN
                {
                mTHEN(); 

                }
                break;
            case 71 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:511: ELSE
                {
                mELSE(); 

                }
                break;
            case 72 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:516: ELIF
                {
                mELIF(); 

                }
                break;
            case 73 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:521: FI
                {
                mFI(); 

                }
                break;
            case 74 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:524: DO
                {
                mDO(); 

                }
                break;
            case 75 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:527: WHILE
                {
                mWHILE(); 

                }
                break;
            case 76 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:533: AT
                {
                mAT(); 

                }
                break;
            case 77 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:536: WHEN
                {
                mWHEN(); 

                }
                break;
            case 78 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:541: UNTIL
                {
                mUNTIL(); 

                }
                break;
            case 79 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:547: BREAK
                {
                mBREAK(); 

                }
                break;
            case 80 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:553: REPEAT
                {
                mREPEAT(); 

                }
                break;
            case 81 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:560: BY
                {
                mBY(); 

                }
                break;
            case 82 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:563: CODE
                {
                mCODE(); 

                }
                break;
            case 83 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:568: DATA
                {
                mDATA(); 

                }
                break;
            case 84 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:573: FOR
                {
                mFOR(); 

                }
                break;
            case 85 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:577: IN
                {
                mIN(); 

                }
                break;
            case 86 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:580: GOTO
                {
                mGOTO(); 

                }
                break;
            case 87 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:585: FALSE
                {
                mFALSE(); 

                }
                break;
            case 88 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:591: TRUE
                {
                mTRUE(); 

                }
                break;
            case 89 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:596: NIL
                {
                mNIL(); 

                }
                break;
            case 90 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:600: WITH
                {
                mWITH(); 

                }
                break;
            case 91 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:605: AS
                {
                mAS(); 

                }
                break;
            case 92 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:608: END
                {
                mEND(); 

                }
                break;
            case 93 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:612: SIZEOF
                {
                mSIZEOF(); 

                }
                break;
            case 94 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:619: TYPEOF
                {
                mTYPEOF(); 

                }
                break;
            case 95 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:626: NUMBER
                {
                mNUMBER(); 

                }
                break;
            case 96 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:633: COLONS
                {
                mCOLONS(); 

                }
                break;
            case 97 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:640: ATTR
                {
                mATTR(); 

                }
                break;
            case 98 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:645: ID
                {
                mID(); 

                }
                break;
            case 99 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:648: CHAR_LITERAL
                {
                mCHAR_LITERAL(); 

                }
                break;
            case 100 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:661: STRING_LITERAL
                {
                mSTRING_LITERAL(); 

                }
                break;
            case 101 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:676: NEWLINE
                {
                mNEWLINE(); 

                }
                break;
            case 102 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:684: WS
                {
                mWS(); 

                }
                break;
            case 103 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:687: SINGLE_COMMENT
                {
                mSINGLE_COMMENT(); 

                }
                break;
            case 104 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:702: MULTI_COMMENT
                {
                mMULTI_COMMENT(); 

                }
                break;

        }

    }


    protected DFA10 dfa10 = new DFA10(this);
    static final String DFA10_eotS =
        "\1\uffff\2\54\1\71\1\uffff\1\75\1\104\1\110\1\112\1\116\1\120\1"+
        "\122\6\uffff\1\124\1\54\1\130\1\uffff\1\132\1\135\3\uffff\2\54\1"+
        "\uffff\1\143\1\146\1\uffff\12\54\6\uffff\1\54\1\171\4\54\1\uffff"+
        "\1\177\6\uffff\1\u0081\1\u0083\1\u0086\1\u0088\22\uffff\2\54\7\uffff"+
        "\1\54\1\u008c\1\u008d\1\u008e\3\uffff\1\u0091\1\uffff\1\54\1\u0094"+
        "\1\u0095\5\54\1\u009c\5\54\1\u00a3\2\54\1\u00a7\1\uffff\4\54\7\uffff"+
        "\1\u00ad\3\uffff\1\u00ae\1\u00af\1\u00b0\4\uffff\1\u00b2\1\uffff"+
        "\2\54\2\uffff\5\54\1\u00ba\1\uffff\6\54\1\uffff\3\54\1\uffff\4\54"+
        "\7\uffff\1\u00c8\1\u00c9\1\u00ca\1\u00cb\1\54\1\u00cd\1\u00ce\1"+
        "\uffff\1\u00cf\1\54\1\u00d1\1\u00d2\3\54\1\u00d6\1\54\1\u00d8\3"+
        "\54\4\uffff\1\54\3\uffff\1\u00dd\2\uffff\1\u00de\1\u00df\1\54\1"+
        "\uffff\1\54\1\uffff\1\u00e2\1\u00e3\1\u00e4\1\u00e5\3\uffff\1\u00e6"+
        "\1\u00e7\6\uffff";
    static final String DFA10_eofS =
        "\u00e8\uffff";
    static final String DFA10_minS =
        "\1\11\1\141\1\145\1\72\1\uffff\1\72\1\53\1\55\1\75\1\52\2\75\6\uffff"+
        "\1\101\1\151\1\75\1\uffff\2\75\3\uffff\1\156\1\162\1\uffff\1\75"+
        "\1\74\1\uffff\1\157\1\146\1\150\1\154\1\141\1\150\1\156\1\162\1"+
        "\145\1\157\6\uffff\1\162\1\60\1\154\1\141\1\154\1\172\1\uffff\1"+
        "\75\6\uffff\4\75\22\uffff\1\164\1\154\7\uffff\1\144\3\60\3\uffff"+
        "\1\75\1\uffff\1\144\2\60\1\145\1\165\1\160\1\151\1\144\1\60\1\164"+
        "\1\145\2\164\1\145\1\60\1\160\1\164\1\60\1\uffff\1\163\1\164\2\145"+
        "\7\uffff\1\75\3\uffff\3\60\4\uffff\1\75\1\uffff\1\144\1\145\2\uffff"+
        "\1\156\3\145\1\146\1\60\1\uffff\1\141\1\154\1\156\1\150\1\151\1"+
        "\141\1\uffff\1\145\1\157\1\141\1\uffff\1\145\1\151\1\143\1\157\7"+
        "\uffff\4\60\1\157\2\60\1\uffff\1\60\1\145\2\60\1\154\1\153\1\141"+
        "\1\60\1\162\1\60\1\143\1\164\1\146\4\uffff\1\146\3\uffff\1\60\2"+
        "\uffff\2\60\1\164\1\uffff\1\144\1\uffff\4\60\3\uffff\2\60\6\uffff";
    static final String DFA10_maxS =
        "\1\176\1\157\1\164\1\75\1\uffff\1\76\1\134\1\76\4\75\6\uffff\1\172"+
        "\1\157\1\75\1\uffff\1\75\1\174\3\uffff\1\164\1\162\1\uffff\2\75"+
        "\1\uffff\1\157\1\156\1\171\1\156\1\157\1\151\1\156\1\171\1\145\1"+
        "\157\6\uffff\1\162\1\172\1\154\1\141\1\154\1\172\1\uffff\1\75\6"+
        "\uffff\2\75\1\76\1\75\22\uffff\1\164\1\154\7\uffff\1\144\3\172\3"+
        "\uffff\1\174\1\uffff\1\156\2\172\1\145\1\165\1\160\1\163\1\144\1"+
        "\172\1\164\1\151\2\164\1\145\1\172\1\160\1\164\1\172\1\uffff\1\163"+
        "\1\164\2\145\7\uffff\1\75\3\uffff\3\172\4\uffff\1\75\1\uffff\1\144"+
        "\1\145\2\uffff\1\156\3\145\1\146\1\172\1\uffff\1\141\1\154\1\156"+
        "\1\150\1\151\1\141\1\uffff\1\145\1\157\1\141\1\uffff\1\145\1\151"+
        "\1\143\1\157\7\uffff\4\172\1\157\2\172\1\uffff\1\172\1\145\2\172"+
        "\1\154\1\153\1\141\1\172\1\162\1\172\1\143\1\164\1\146\4\uffff\1"+
        "\146\3\uffff\1\172\2\uffff\2\172\1\164\1\uffff\1\144\1\uffff\4\172"+
        "\3\uffff\2\172\6\uffff";
    static final String DFA10_acceptS =
        "\4\uffff\1\4\7\uffff\1\31\1\32\1\33\1\34\1\35\1\36\3\uffff\1\42"+
        "\2\uffff\1\45\1\46\1\47\2\uffff\1\56\2\uffff\1\76\12\uffff\1\137"+
        "\1\142\1\143\1\144\1\145\1\146\6\uffff\1\7\1\uffff\1\3\1\6\1\55"+
        "\1\75\1\5\1\12\4\uffff\1\77\1\11\1\14\1\100\1\101\1\13\1\16\1\15"+
        "\1\20\1\147\1\150\1\17\1\24\1\23\1\30\1\27\1\141\1\37\2\uffff\1"+
        "\54\1\41\1\51\1\43\1\53\1\102\1\44\4\uffff\1\57\1\63\1\61\1\uffff"+
        "\1\65\22\uffff\1\111\4\uffff\1\10\1\140\1\22\1\21\1\26\1\25\1\60"+
        "\1\uffff\1\64\1\62\1\66\3\uffff\1\114\1\133\1\52\1\70\1\uffff\1"+
        "\67\2\uffff\1\105\1\125\6\uffff\1\112\6\uffff\1\121\3\uffff\1\124"+
        "\4\uffff\1\72\1\71\1\40\1\131\1\50\1\74\1\73\7\uffff\1\134\15\uffff"+
        "\1\104\1\122\1\106\1\130\1\uffff\1\107\1\110\1\123\1\uffff\1\115"+
        "\1\132\3\uffff\1\126\1\uffff\1\127\4\uffff\1\113\1\116\1\117\2\uffff"+
        "\1\2\1\103\1\135\1\136\1\120\1\1";
    static final String DFA10_specialS =
        "\u00e8\uffff}>";
    static final String[] DFA10_transitionS = {
            "\1\60\1\57\2\uffff\1\57\22\uffff\1\60\1\35\1\56\1\22\1\uffff"+
            "\1\13\1\26\1\55\1\14\1\15\1\10\1\6\1\4\1\7\1\40\1\11\12\53\1"+
            "\3\1\31\1\37\1\5\1\36\1\32\1\25\32\54\1\20\1\12\1\21\1\30\1"+
            "\54\1\uffff\1\33\1\50\1\41\1\45\1\44\1\1\1\52\1\54\1\42\4\54"+
            "\1\23\1\34\2\54\1\51\1\2\1\43\1\47\1\54\1\46\3\54\1\16\1\27"+
            "\1\17\1\24",
            "\1\63\7\uffff\1\62\5\uffff\1\61",
            "\1\65\3\uffff\1\66\12\uffff\1\64",
            "\1\70\2\uffff\1\67",
            "",
            "\1\72\2\uffff\1\73\1\74",
            "\1\103\3\uffff\1\77\14\uffff\1\102\1\76\1\101\35\uffff\1\100",
            "\1\106\17\uffff\1\105\1\107",
            "\1\111",
            "\1\115\4\uffff\1\114\15\uffff\1\113",
            "\1\117",
            "\1\121",
            "",
            "",
            "",
            "",
            "",
            "",
            "\32\123\4\uffff\1\123\1\uffff\32\123",
            "\1\126\5\uffff\1\125",
            "\1\127",
            "",
            "\1\131",
            "\1\133\76\uffff\1\134",
            "",
            "",
            "",
            "\1\136\4\uffff\1\140\1\137",
            "\1\141",
            "",
            "\1\142",
            "\1\145\1\144",
            "",
            "\1\147",
            "\1\150\7\uffff\1\151",
            "\1\152\11\uffff\1\153\6\uffff\1\154",
            "\1\155\1\uffff\1\156",
            "\1\160\15\uffff\1\157",
            "\1\161\1\162",
            "\1\163",
            "\1\164\6\uffff\1\165",
            "\1\166",
            "\1\167",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\170",
            "\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32\54",
            "\1\172",
            "\1\173",
            "\1\174",
            "\1\175",
            "",
            "\1\176",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\u0080",
            "\1\u0082",
            "\1\u0084\1\u0085",
            "\1\u0087",
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
            "\1\u0089",
            "\1\u008a",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\u008b",
            "\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32\54",
            "\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32\54",
            "\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32\54",
            "",
            "",
            "",
            "\1\u008f\76\uffff\1\u0090",
            "",
            "\1\u0093\11\uffff\1\u0092",
            "\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32\54",
            "\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32\54",
            "\1\u0096",
            "\1\u0097",
            "\1\u0098",
            "\1\u009a\11\uffff\1\u0099",
            "\1\u009b",
            "\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32\54",
            "\1\u009d",
            "\1\u009f\3\uffff\1\u009e",
            "\1\u00a0",
            "\1\u00a1",
            "\1\u00a2",
            "\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32\54",
            "\1\u00a4",
            "\1\u00a5",
            "\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\26\54\1\u00a6\3\54",
            "",
            "\1\u00a8",
            "\1\u00a9",
            "\1\u00aa",
            "\1\u00ab",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\u00ac",
            "",
            "",
            "",
            "\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32\54",
            "\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32\54",
            "\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32\54",
            "",
            "",
            "",
            "",
            "\1\u00b1",
            "",
            "\1\u00b3",
            "\1\u00b4",
            "",
            "",
            "\1\u00b5",
            "\1\u00b6",
            "\1\u00b7",
            "\1\u00b8",
            "\1\u00b9",
            "\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32\54",
            "",
            "\1\u00bb",
            "\1\u00bc",
            "\1\u00bd",
            "\1\u00be",
            "\1\u00bf",
            "\1\u00c0",
            "",
            "\1\u00c1",
            "\1\u00c2",
            "\1\u00c3",
            "",
            "\1\u00c4",
            "\1\u00c5",
            "\1\u00c6",
            "\1\u00c7",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32\54",
            "\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32\54",
            "\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32\54",
            "\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32\54",
            "\1\u00cc",
            "\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32\54",
            "\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32\54",
            "",
            "\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32\54",
            "\1\u00d0",
            "\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32\54",
            "\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32\54",
            "\1\u00d3",
            "\1\u00d4",
            "\1\u00d5",
            "\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32\54",
            "\1\u00d7",
            "\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32\54",
            "\1\u00d9",
            "\1\u00da",
            "\1\u00db",
            "",
            "",
            "",
            "",
            "\1\u00dc",
            "",
            "",
            "",
            "\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32\54",
            "",
            "",
            "\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32\54",
            "\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32\54",
            "\1\u00e0",
            "",
            "\1\u00e1",
            "",
            "\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32\54",
            "\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32\54",
            "\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32\54",
            "\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32\54",
            "",
            "",
            "",
            "\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32\54",
            "\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32\54",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA10_eot = DFA.unpackEncodedString(DFA10_eotS);
    static final short[] DFA10_eof = DFA.unpackEncodedString(DFA10_eofS);
    static final char[] DFA10_min = DFA.unpackEncodedStringToUnsignedChars(DFA10_minS);
    static final char[] DFA10_max = DFA.unpackEncodedStringToUnsignedChars(DFA10_maxS);
    static final short[] DFA10_accept = DFA.unpackEncodedString(DFA10_acceptS);
    static final short[] DFA10_special = DFA.unpackEncodedString(DFA10_specialS);
    static final short[][] DFA10_transition;

    static {
        int numStates = DFA10_transitionS.length;
        DFA10_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA10_transition[i] = DFA.unpackEncodedString(DFA10_transitionS[i]);
        }
    }

    class DFA10 extends DFA {

        public DFA10(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 10;
            this.eot = DFA10_eot;
            this.eof = DFA10_eof;
            this.min = DFA10_min;
            this.max = DFA10_max;
            this.accept = DFA10_accept;
            this.special = DFA10_special;
            this.transition = DFA10_transition;
        }
        public String getDescription() {
            return "1:1: Tokens : ( FORWARD | STATIC | COLON | COMMA | EQUALS | EQUALS_COLON | COLON_EQUALS | COLON_COLON_EQUALS | PLUS | PLUS_EQ | MINUS | MINUS_EQ | STAR | STAR_EQ | SLASH | SLASH_EQ | UDIV | UDIV_EQ | REM | REM_EQ | UREM | UREM_EQ | MOD | MOD_EQ | LPAREN | RPAREN | LBRACE | RBRACE | LBRACKET | RBRACKET | HASH | NOT | TILDE | ATSIGN | AMP | BAR | CARET | SEMI | QUESTION | AND | AND_EQ | OR | OR_EQ | XOR_EQ | COMPEQ | COMPNE | COMPGE | COMPUGE | COMPLE | COMPULE | GREATER | UGREATER | LESS | ULESS | LSHIFT | LSHIFT_EQ | URSHIFT | URSHIFT_EQ | CLSHIFT | CLSHIFT_EQ | ARROW | PERIOD | PLUSPLUS | MINUSMINUS | POINTS | BAR_BAR | SELECT | COND | IF | THEN | ELSE | ELIF | FI | DO | WHILE | AT | WHEN | UNTIL | BREAK | REPEAT | BY | CODE | DATA | FOR | IN | GOTO | FALSE | TRUE | NIL | WITH | AS | END | SIZEOF | TYPEOF | NUMBER | COLONS | ATTR | ID | CHAR_LITERAL | STRING_LITERAL | NEWLINE | WS | SINGLE_COMMENT | MULTI_COMMENT );";
        }
    }
 

}