// $ANTLR 3.1.3 Mar 17, 2009 19:23:44 /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g 2010-05-13 22:35:10

package org.ejs.eulang.parser;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

public class EulangLexer extends Lexer {
    public static final int CONDTEST=22;
    public static final int STAR=134;
    public static final int WHILE=102;
    public static final int GENERIC=61;
    public static final int MOD=34;
    public static final int POINTER=54;
    public static final int PERCENT_EQ=89;
    public static final int LSHIFT_EQ=95;
    public static final int PREDEC=40;
    public static final int UMOD_EQ=90;
    public static final int DEREF=55;
    public static final int MINUSMINUS=139;
    public static final int DO=101;
    public static final int ARGLIST=11;
    public static final int EQUALS=64;
    public static final int NOT=116;
    public static final int EOF=-1;
    public static final int BREAK=107;
    public static final int TYPE=19;
    public static final int CODE=7;
    public static final int LBRACKET=69;
    public static final int RPAREN=78;
    public static final int TUPLE=47;
    public static final int GREATER=84;
    public static final int STRING_LITERAL=144;
    public static final int ADDRREF=56;
    public static final int ADDSCOPE=5;
    public static final int COMPLE=119;
    public static final int AND_EQ=92;
    public static final int CARET=82;
    public static final int LESS=83;
    public static final int XOR_EQ=94;
    public static final int INITEXPR=58;
    public static final int INITLIST=59;
    public static final int ATSIGN=76;
    public static final int GOTO=45;
    public static final int SELECT=152;
    public static final int CLSHIFT_EQ=98;
    public static final int ARRAY=52;
    public static final int LABELSTMT=48;
    public static final int IDEXPR=50;
    public static final int CRSHIFT=131;
    public static final int RBRACE=73;
    public static final int STMTEXPR=20;
    public static final int STATIC=147;
    public static final int UMOD=137;
    public static final int PERIOD=100;
    public static final int LSHIFT=128;
    public static final int INV=36;
    public static final int ADDROF=57;
    public static final int ELSE=110;
    public static final int NUMBER=140;
    public static final int LIT=41;
    public static final int UDIV=33;
    public static final int CRSHIFT_EQ=99;
    public static final int UDIV_EQ=91;
    public static final int LIST=18;
    public static final int PLUS_EQ=85;
    public static final int MUL=31;
    public static final int RSHIFT_EQ=96;
    public static final int ARGDEF=12;
    public static final int FI=114;
    public static final int MINUS_EQ=86;
    public static final int ELIF=113;
    public static final int WS=160;
    public static final int OR_EQ=93;
    public static final int BITOR=27;
    public static final int NIL=80;
    public static final int UNTIL=154;
    public static final int STMTLIST=9;
    public static final int OR=115;
    public static final int ALLOC=14;
    public static final int IDLIST=43;
    public static final int REPEAT=103;
    public static final int INLINE=24;
    public static final int CALL=23;
    public static final int POSTINC=37;
    public static final int END=155;
    public static final int FALSE=141;
    public static final int COMPULE=121;
    public static final int POSTDEC=38;
    public static final int BINDING=49;
    public static final int FORWARD=67;
    public static final int BAR_BAR=151;
    public static final int AMP=127;
    public static final int POINTS=150;
    public static final int PLUSPLUS=138;
    public static final int UGREATER=124;
    public static final int LBRACE=72;
    public static final int FIELDREF=51;
    public static final int MULTI_COMMENT=162;
    public static final int FOR=74;
    public static final int SUB=30;
    public static final int ID=62;
    public static final int AND=104;
    public static final int DEFINE=16;
    public static final int BITAND=26;
    public static final int LPAREN=77;
    public static final int IF=108;
    public static final int COLONS=145;
    public static final int AT=106;
    public static final int COLON_COLON_EQUALS=148;
    public static final int INDEX=53;
    public static final int AS=111;
    public static final int CONDLIST=21;
    public static final int IDSUFFIX=156;
    public static final int EXPR=17;
    public static final int SLASH=135;
    public static final int IN=75;
    public static final int THEN=112;
    public static final int SCOPE=4;
    public static final int COMMA=68;
    public static final int PREINC=39;
    public static final int BITXOR=28;
    public static final int TILDE=126;
    public static final int PLUS=71;
    public static final int SINGLE_COMMENT=161;
    public static final int DIGIT=158;
    public static final int RBRACKET=70;
    public static final int RSHIFT=129;
    public static final int WITH=109;
    public static final int ADD=29;
    public static final int URSHIFT_EQ=97;
    public static final int COMPGE=120;
    public static final int BY=105;
    public static final int ULESS=123;
    public static final int PERCENT=136;
    public static final int LIST_COMPREHENSION=6;
    public static final int LETTERLIKE=157;
    public static final int HASH=149;
    public static final int CLSHIFT=132;
    public static final int STAR_EQ=87;
    public static final int MINUS=133;
    public static final int SEMI=65;
    public static final int TRUE=142;
    public static final int REF=13;
    public static final int COLON=63;
    public static final int COLON_EQUALS=66;
    public static final int QUESTION=81;
    public static final int NEWLINE=159;
    public static final int CHAR_LITERAL=143;
    public static final int LABEL=44;
    public static final int WHEN=153;
    public static final int INSTANCE=60;
    public static final int BLOCK=46;
    public static final int NEG=35;
    public static final int ASSIGN=15;
    public static final int URSHIFT=130;
    public static final int ARROW=79;
    public static final int COMPEQ=117;
    public static final int IDREF=42;
    public static final int DIV=32;
    public static final int COND=25;
    public static final int PROTO=10;
    public static final int MACRO=8;
    public static final int COMPNE=118;
    public static final int BAR=125;
    public static final int DATA=146;
    public static final int COMPUGE=122;
    public static final int SLASH_EQ=88;

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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:566:9: ( 'forward' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:566:11: 'forward'
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:567:8: ( 'static' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:567:10: 'static'
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:569:7: ( ':' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:569:9: ':'
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:570:7: ( ',' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:570:9: ','
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:571:8: ( '=' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:571:10: '='
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:572:14: ( ':=' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:572:16: ':='
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:573:20: ( '::=' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:573:22: '::='
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:574:6: ( '+' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:574:8: '+'
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:575:9: ( '+=' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:575:11: '+='
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:576:7: ( '-' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:576:9: '-'
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:577:10: ( '-=' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:577:12: '-='
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:578:6: ( '*' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:578:8: '*'
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:579:9: ( '*=' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:579:11: '*='
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:580:7: ( '/' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:580:9: '/'
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:581:10: ( '/=' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:581:12: '/='
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

    // $ANTLR start "LPAREN"
    public final void mLPAREN() throws RecognitionException {
        try {
            int _type = LPAREN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:582:8: ( '(' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:582:10: '('
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:583:8: ( ')' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:583:10: ')'
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:584:8: ( '{' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:584:10: '{'
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:585:8: ( '}' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:585:10: '}'
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:586:10: ( '[' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:586:12: '['
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:587:10: ( ']' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:587:12: ']'
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:588:6: ( '#' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:588:8: '#'
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:590:5: ( 'not' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:590:7: 'not'
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:591:7: ( '~' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:591:9: '~'
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:592:8: ( '@' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:592:10: '@'
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:593:5: ( '&' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:593:7: '&'
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:594:5: ( '|' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:594:7: '|'
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:595:7: ( '^' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:595:9: '^'
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:596:6: ( ';' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:596:8: ';'
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:597:10: ( '?' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:597:12: '?'
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:598:5: ( 'and' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:598:7: 'and'
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:599:8: ( '&=' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:599:10: '&='
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:600:4: ( 'or' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:600:6: 'or'
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:601:7: ( '|=' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:601:9: '|='
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:603:8: ( '~=' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:603:10: '~='
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:604:8: ( '==' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:604:10: '=='
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:605:8: ( '!=' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:605:10: '!='
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:606:8: ( '>=' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:606:10: '>='
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:607:9: ( '+>=' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:607:11: '+>='
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:608:8: ( '<=' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:608:10: '<='
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:609:9: ( '+<=' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:609:11: '+<='
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:610:9: ( '>' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:610:11: '>'
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:611:10: ( '+>' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:611:12: '+>'
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:612:6: ( '<' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:612:8: '<'
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:613:7: ( '+<' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:613:9: '+<'
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:614:8: ( '<<' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:614:10: '<<'
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:615:11: ( '<<=' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:615:13: '<<='
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

    // $ANTLR start "RSHIFT"
    public final void mRSHIFT() throws RecognitionException {
        try {
            int _type = RSHIFT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:616:8: ( '>>' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:616:10: '>>'
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

    // $ANTLR start "RSHIFT_EQ"
    public final void mRSHIFT_EQ() throws RecognitionException {
        try {
            int _type = RSHIFT_EQ;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:617:11: ( '>>=' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:617:13: '>>='
            {
            match(">>="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "RSHIFT_EQ"

    // $ANTLR start "URSHIFT"
    public final void mURSHIFT() throws RecognitionException {
        try {
            int _type = URSHIFT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:618:9: ( '+>>' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:618:11: '+>>'
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:619:12: ( '+>>=' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:619:14: '+>>='
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

    // $ANTLR start "CRSHIFT"
    public final void mCRSHIFT() throws RecognitionException {
        try {
            int _type = CRSHIFT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:620:9: ( '>>|' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:620:11: '>>|'
            {
            match(">>|"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "CRSHIFT"

    // $ANTLR start "CRSHIFT_EQ"
    public final void mCRSHIFT_EQ() throws RecognitionException {
        try {
            int _type = CRSHIFT_EQ;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:621:12: ( '>>|=' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:621:14: '>>|='
            {
            match(">>|="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "CRSHIFT_EQ"

    // $ANTLR start "CLSHIFT"
    public final void mCLSHIFT() throws RecognitionException {
        try {
            int _type = CLSHIFT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:622:9: ( '<<|' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:622:11: '<<|'
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:623:12: ( '<<|=' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:623:14: '<<|='
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

    // $ANTLR start "UDIV"
    public final void mUDIV() throws RecognitionException {
        try {
            int _type = UDIV;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:624:6: ( '+/' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:624:8: '+/'
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:625:9: ( '+/=' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:625:11: '+/='
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

    // $ANTLR start "PERCENT"
    public final void mPERCENT() throws RecognitionException {
        try {
            int _type = PERCENT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:626:9: ( '%' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:626:11: '%'
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

    // $ANTLR start "PERCENT_EQ"
    public final void mPERCENT_EQ() throws RecognitionException {
        try {
            int _type = PERCENT_EQ;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:627:12: ( '%=' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:627:14: '%='
            {
            match("%="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "PERCENT_EQ"

    // $ANTLR start "UMOD"
    public final void mUMOD() throws RecognitionException {
        try {
            int _type = UMOD;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:628:6: ( '+%' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:628:8: '+%'
            {
            match("+%"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "UMOD"

    // $ANTLR start "UMOD_EQ"
    public final void mUMOD_EQ() throws RecognitionException {
        try {
            int _type = UMOD_EQ;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:629:9: ( '+%=' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:629:11: '+%='
            {
            match("+%="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "UMOD_EQ"

    // $ANTLR start "ARROW"
    public final void mARROW() throws RecognitionException {
        try {
            int _type = ARROW;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:630:7: ( '=>' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:630:9: '=>'
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:631:8: ( '.' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:631:10: '.'
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:632:10: ( '++' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:632:12: '++'
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:633:12: ( '--' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:633:14: '--'
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:635:8: ( '->' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:635:10: '->'
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:636:9: ( '||' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:636:11: '||'
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:638:8: ( 'select' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:638:10: 'select'
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

    // $ANTLR start "IF"
    public final void mIF() throws RecognitionException {
        try {
            int _type = IF;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:640:3: ( 'if' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:640:5: 'if'
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:641:6: ( 'then' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:641:8: 'then'
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:642:6: ( 'else' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:642:8: 'else'
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:643:6: ( 'elif' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:643:8: 'elif'
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:644:4: ( 'fi' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:644:6: 'fi'
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:645:4: ( 'do' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:645:6: 'do'
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:646:7: ( 'while' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:646:9: 'while'
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:647:4: ( 'at' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:647:6: 'at'
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:648:6: ( 'when' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:648:8: 'when'
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:649:7: ( 'until' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:649:9: 'until'
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:650:7: ( 'break' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:650:9: 'break'
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:651:8: ( 'repeat' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:651:10: 'repeat'
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:652:4: ( 'by' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:652:6: 'by'
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:655:6: ( 'code' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:655:8: 'code'
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:656:6: ( 'data' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:656:8: 'data'
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

    // $ANTLR start "MACRO"
    public final void mMACRO() throws RecognitionException {
        try {
            int _type = MACRO;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:657:7: ( 'macro' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:657:9: 'macro'
            {
            match("macro"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "MACRO"

    // $ANTLR start "FOR"
    public final void mFOR() throws RecognitionException {
        try {
            int _type = FOR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:658:5: ( 'for' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:658:7: 'for'
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:659:4: ( 'in' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:659:6: 'in'
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:660:5: ( 'goto' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:660:7: 'goto'
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:661:6: ( 'false' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:661:8: 'false'
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:662:5: ( 'true' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:662:7: 'true'
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:663:4: ( 'nil' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:663:6: 'nil'
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:665:5: ( 'with' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:665:7: 'with'
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:666:3: ( 'as' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:666:5: 'as'
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:667:4: ( 'end' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:667:6: 'end'
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

    // $ANTLR start "NUMBER"
    public final void mNUMBER() throws RecognitionException {
        try {
            int _type = NUMBER;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:672:7: ( '0' .. '9' ( IDSUFFIX ( '.' IDSUFFIX )? ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:672:9: '0' .. '9' ( IDSUFFIX ( '.' IDSUFFIX )? )
            {
            matchRange('0','9'); 
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:672:18: ( IDSUFFIX ( '.' IDSUFFIX )? )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:672:19: IDSUFFIX ( '.' IDSUFFIX )?
            {
            mIDSUFFIX(); 
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:672:28: ( '.' IDSUFFIX )?
            int alt1=2;
            int LA1_0 = input.LA(1);

            if ( (LA1_0=='.') ) {
                alt1=1;
            }
            switch (alt1) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:672:30: '.' IDSUFFIX
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:679:8: ( COLON ( COLON )+ )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:679:10: COLON ( COLON )+
            {
            mCOLON(); 
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:679:16: ( COLON )+
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:679:16: COLON
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

    // $ANTLR start "ID"
    public final void mID() throws RecognitionException {
        try {
            int _type = ID;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:681:4: ( LETTERLIKE IDSUFFIX )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:681:6: LETTERLIKE IDSUFFIX
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:682:19: ( ( LETTERLIKE | DIGIT )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:682:21: ( LETTERLIKE | DIGIT )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:682:21: ( LETTERLIKE | DIGIT )*
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:683:20: ( 'a' .. 'z' | 'A' .. 'Z' | '_' )
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:684:15: ( '0' .. '9' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:684:17: '0' .. '9'
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:689:13: ( '\\'' (~ ( '\\'' ) )* '\\'' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:689:15: '\\'' (~ ( '\\'' ) )* '\\''
            {
            match('\''); 
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:689:20: (~ ( '\\'' ) )*
            loop4:
            do {
                int alt4=2;
                int LA4_0 = input.LA(1);

                if ( ((LA4_0>='\u0000' && LA4_0<='&')||(LA4_0>='(' && LA4_0<='\uFFFF')) ) {
                    alt4=1;
                }


                switch (alt4) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:689:20: ~ ( '\\'' )
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
            	    break loop4;
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:690:15: ( '\"' (~ ( '\"' ) )* '\"' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:690:17: '\"' (~ ( '\"' ) )* '\"'
            {
            match('\"'); 
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:690:21: (~ ( '\"' ) )*
            loop5:
            do {
                int alt5=2;
                int LA5_0 = input.LA(1);

                if ( ((LA5_0>='\u0000' && LA5_0<='!')||(LA5_0>='#' && LA5_0<='\uFFFF')) ) {
                    alt5=1;
                }


                switch (alt5) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:690:21: ~ ( '\"' )
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
            	    break loop5;
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

    // $ANTLR start "NEWLINE"
    public final void mNEWLINE() throws RecognitionException {
        try {
            int _type = NEWLINE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:695:8: ( ( ( '\\r' )? '\\n' )+ )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:695:10: ( ( '\\r' )? '\\n' )+
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:695:10: ( ( '\\r' )? '\\n' )+
            int cnt7=0;
            loop7:
            do {
                int alt7=2;
                int LA7_0 = input.LA(1);

                if ( (LA7_0=='\n'||LA7_0=='\r') ) {
                    alt7=1;
                }


                switch (alt7) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:695:11: ( '\\r' )? '\\n'
            	    {
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:695:11: ( '\\r' )?
            	    int alt6=2;
            	    int LA6_0 = input.LA(1);

            	    if ( (LA6_0=='\r') ) {
            	        alt6=1;
            	    }
            	    switch (alt6) {
            	        case 1 :
            	            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:695:11: '\\r'
            	            {
            	            match('\r'); 

            	            }
            	            break;

            	    }

            	    match('\n'); 

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
    // $ANTLR end "NEWLINE"

    // $ANTLR start "WS"
    public final void mWS() throws RecognitionException {
        try {
            int _type = WS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:696:5: ( ( ' ' | '\\t' )+ )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:696:9: ( ' ' | '\\t' )+
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:696:9: ( ' ' | '\\t' )+
            int cnt8=0;
            loop8:
            do {
                int alt8=2;
                int LA8_0 = input.LA(1);

                if ( (LA8_0=='\t'||LA8_0==' ') ) {
                    alt8=1;
                }


                switch (alt8) {
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
            	    if ( cnt8 >= 1 ) break loop8;
                        EarlyExitException eee =
                            new EarlyExitException(8, input);
                        throw eee;
                }
                cnt8++;
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:700:15: ( '//' (~ ( '\\r' | '\\n' ) )* NEWLINE )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:700:17: '//' (~ ( '\\r' | '\\n' ) )* NEWLINE
            {
            match("//"); 

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:700:22: (~ ( '\\r' | '\\n' ) )*
            loop9:
            do {
                int alt9=2;
                int LA9_0 = input.LA(1);

                if ( ((LA9_0>='\u0000' && LA9_0<='\t')||(LA9_0>='\u000B' && LA9_0<='\f')||(LA9_0>='\u000E' && LA9_0<='\uFFFF')) ) {
                    alt9=1;
                }


                switch (alt9) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:700:22: ~ ( '\\r' | '\\n' )
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
            	    break loop9;
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:705:3: ( '/*' ( . )* '*/' ( NEWLINE )? )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:705:5: '/*' ( . )* '*/' ( NEWLINE )?
            {
            match("/*"); 

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:705:10: ( . )*
            loop10:
            do {
                int alt10=2;
                int LA10_0 = input.LA(1);

                if ( (LA10_0=='*') ) {
                    int LA10_1 = input.LA(2);

                    if ( (LA10_1=='/') ) {
                        alt10=2;
                    }
                    else if ( ((LA10_1>='\u0000' && LA10_1<='.')||(LA10_1>='0' && LA10_1<='\uFFFF')) ) {
                        alt10=1;
                    }


                }
                else if ( ((LA10_0>='\u0000' && LA10_0<=')')||(LA10_0>='+' && LA10_0<='\uFFFF')) ) {
                    alt10=1;
                }


                switch (alt10) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:705:10: .
            	    {
            	    matchAny(); 

            	    }
            	    break;

            	default :
            	    break loop10;
                }
            } while (true);

            match("*/"); 

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:705:18: ( NEWLINE )?
            int alt11=2;
            int LA11_0 = input.LA(1);

            if ( (LA11_0=='\n'||LA11_0=='\r') ) {
                alt11=1;
            }
            switch (alt11) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:705:18: NEWLINE
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
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:8: ( FORWARD | STATIC | COLON | COMMA | EQUALS | COLON_EQUALS | COLON_COLON_EQUALS | PLUS | PLUS_EQ | MINUS | MINUS_EQ | STAR | STAR_EQ | SLASH | SLASH_EQ | LPAREN | RPAREN | LBRACE | RBRACE | LBRACKET | RBRACKET | HASH | NOT | TILDE | ATSIGN | AMP | BAR | CARET | SEMI | QUESTION | AND | AND_EQ | OR | OR_EQ | XOR_EQ | COMPEQ | COMPNE | COMPGE | COMPUGE | COMPLE | COMPULE | GREATER | UGREATER | LESS | ULESS | LSHIFT | LSHIFT_EQ | RSHIFT | RSHIFT_EQ | URSHIFT | URSHIFT_EQ | CRSHIFT | CRSHIFT_EQ | CLSHIFT | CLSHIFT_EQ | UDIV | UDIV_EQ | PERCENT | PERCENT_EQ | UMOD | UMOD_EQ | ARROW | PERIOD | PLUSPLUS | MINUSMINUS | POINTS | BAR_BAR | SELECT | IF | THEN | ELSE | ELIF | FI | DO | WHILE | AT | WHEN | UNTIL | BREAK | REPEAT | BY | CODE | DATA | MACRO | FOR | IN | GOTO | FALSE | TRUE | NIL | WITH | AS | END | NUMBER | COLONS | ID | CHAR_LITERAL | STRING_LITERAL | NEWLINE | WS | SINGLE_COMMENT | MULTI_COMMENT )
        int alt12=102;
        alt12 = dfa12.predict(input);
        switch (alt12) {
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
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:44: COLON_EQUALS
                {
                mCOLON_EQUALS(); 

                }
                break;
            case 7 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:57: COLON_COLON_EQUALS
                {
                mCOLON_COLON_EQUALS(); 

                }
                break;
            case 8 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:76: PLUS
                {
                mPLUS(); 

                }
                break;
            case 9 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:81: PLUS_EQ
                {
                mPLUS_EQ(); 

                }
                break;
            case 10 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:89: MINUS
                {
                mMINUS(); 

                }
                break;
            case 11 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:95: MINUS_EQ
                {
                mMINUS_EQ(); 

                }
                break;
            case 12 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:104: STAR
                {
                mSTAR(); 

                }
                break;
            case 13 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:109: STAR_EQ
                {
                mSTAR_EQ(); 

                }
                break;
            case 14 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:117: SLASH
                {
                mSLASH(); 

                }
                break;
            case 15 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:123: SLASH_EQ
                {
                mSLASH_EQ(); 

                }
                break;
            case 16 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:132: LPAREN
                {
                mLPAREN(); 

                }
                break;
            case 17 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:139: RPAREN
                {
                mRPAREN(); 

                }
                break;
            case 18 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:146: LBRACE
                {
                mLBRACE(); 

                }
                break;
            case 19 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:153: RBRACE
                {
                mRBRACE(); 

                }
                break;
            case 20 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:160: LBRACKET
                {
                mLBRACKET(); 

                }
                break;
            case 21 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:169: RBRACKET
                {
                mRBRACKET(); 

                }
                break;
            case 22 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:178: HASH
                {
                mHASH(); 

                }
                break;
            case 23 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:183: NOT
                {
                mNOT(); 

                }
                break;
            case 24 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:187: TILDE
                {
                mTILDE(); 

                }
                break;
            case 25 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:193: ATSIGN
                {
                mATSIGN(); 

                }
                break;
            case 26 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:200: AMP
                {
                mAMP(); 

                }
                break;
            case 27 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:204: BAR
                {
                mBAR(); 

                }
                break;
            case 28 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:208: CARET
                {
                mCARET(); 

                }
                break;
            case 29 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:214: SEMI
                {
                mSEMI(); 

                }
                break;
            case 30 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:219: QUESTION
                {
                mQUESTION(); 

                }
                break;
            case 31 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:228: AND
                {
                mAND(); 

                }
                break;
            case 32 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:232: AND_EQ
                {
                mAND_EQ(); 

                }
                break;
            case 33 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:239: OR
                {
                mOR(); 

                }
                break;
            case 34 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:242: OR_EQ
                {
                mOR_EQ(); 

                }
                break;
            case 35 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:248: XOR_EQ
                {
                mXOR_EQ(); 

                }
                break;
            case 36 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:255: COMPEQ
                {
                mCOMPEQ(); 

                }
                break;
            case 37 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:262: COMPNE
                {
                mCOMPNE(); 

                }
                break;
            case 38 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:269: COMPGE
                {
                mCOMPGE(); 

                }
                break;
            case 39 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:276: COMPUGE
                {
                mCOMPUGE(); 

                }
                break;
            case 40 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:284: COMPLE
                {
                mCOMPLE(); 

                }
                break;
            case 41 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:291: COMPULE
                {
                mCOMPULE(); 

                }
                break;
            case 42 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:299: GREATER
                {
                mGREATER(); 

                }
                break;
            case 43 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:307: UGREATER
                {
                mUGREATER(); 

                }
                break;
            case 44 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:316: LESS
                {
                mLESS(); 

                }
                break;
            case 45 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:321: ULESS
                {
                mULESS(); 

                }
                break;
            case 46 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:327: LSHIFT
                {
                mLSHIFT(); 

                }
                break;
            case 47 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:334: LSHIFT_EQ
                {
                mLSHIFT_EQ(); 

                }
                break;
            case 48 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:344: RSHIFT
                {
                mRSHIFT(); 

                }
                break;
            case 49 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:351: RSHIFT_EQ
                {
                mRSHIFT_EQ(); 

                }
                break;
            case 50 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:361: URSHIFT
                {
                mURSHIFT(); 

                }
                break;
            case 51 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:369: URSHIFT_EQ
                {
                mURSHIFT_EQ(); 

                }
                break;
            case 52 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:380: CRSHIFT
                {
                mCRSHIFT(); 

                }
                break;
            case 53 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:388: CRSHIFT_EQ
                {
                mCRSHIFT_EQ(); 

                }
                break;
            case 54 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:399: CLSHIFT
                {
                mCLSHIFT(); 

                }
                break;
            case 55 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:407: CLSHIFT_EQ
                {
                mCLSHIFT_EQ(); 

                }
                break;
            case 56 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:418: UDIV
                {
                mUDIV(); 

                }
                break;
            case 57 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:423: UDIV_EQ
                {
                mUDIV_EQ(); 

                }
                break;
            case 58 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:431: PERCENT
                {
                mPERCENT(); 

                }
                break;
            case 59 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:439: PERCENT_EQ
                {
                mPERCENT_EQ(); 

                }
                break;
            case 60 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:450: UMOD
                {
                mUMOD(); 

                }
                break;
            case 61 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:455: UMOD_EQ
                {
                mUMOD_EQ(); 

                }
                break;
            case 62 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:463: ARROW
                {
                mARROW(); 

                }
                break;
            case 63 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:469: PERIOD
                {
                mPERIOD(); 

                }
                break;
            case 64 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:476: PLUSPLUS
                {
                mPLUSPLUS(); 

                }
                break;
            case 65 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:485: MINUSMINUS
                {
                mMINUSMINUS(); 

                }
                break;
            case 66 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:496: POINTS
                {
                mPOINTS(); 

                }
                break;
            case 67 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:503: BAR_BAR
                {
                mBAR_BAR(); 

                }
                break;
            case 68 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:511: SELECT
                {
                mSELECT(); 

                }
                break;
            case 69 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:518: IF
                {
                mIF(); 

                }
                break;
            case 70 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:521: THEN
                {
                mTHEN(); 

                }
                break;
            case 71 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:526: ELSE
                {
                mELSE(); 

                }
                break;
            case 72 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:531: ELIF
                {
                mELIF(); 

                }
                break;
            case 73 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:536: FI
                {
                mFI(); 

                }
                break;
            case 74 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:539: DO
                {
                mDO(); 

                }
                break;
            case 75 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:542: WHILE
                {
                mWHILE(); 

                }
                break;
            case 76 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:548: AT
                {
                mAT(); 

                }
                break;
            case 77 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:551: WHEN
                {
                mWHEN(); 

                }
                break;
            case 78 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:556: UNTIL
                {
                mUNTIL(); 

                }
                break;
            case 79 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:562: BREAK
                {
                mBREAK(); 

                }
                break;
            case 80 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:568: REPEAT
                {
                mREPEAT(); 

                }
                break;
            case 81 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:575: BY
                {
                mBY(); 

                }
                break;
            case 82 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:578: CODE
                {
                mCODE(); 

                }
                break;
            case 83 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:583: DATA
                {
                mDATA(); 

                }
                break;
            case 84 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:588: MACRO
                {
                mMACRO(); 

                }
                break;
            case 85 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:594: FOR
                {
                mFOR(); 

                }
                break;
            case 86 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:598: IN
                {
                mIN(); 

                }
                break;
            case 87 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:601: GOTO
                {
                mGOTO(); 

                }
                break;
            case 88 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:606: FALSE
                {
                mFALSE(); 

                }
                break;
            case 89 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:612: TRUE
                {
                mTRUE(); 

                }
                break;
            case 90 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:617: NIL
                {
                mNIL(); 

                }
                break;
            case 91 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:621: WITH
                {
                mWITH(); 

                }
                break;
            case 92 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:626: AS
                {
                mAS(); 

                }
                break;
            case 93 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:629: END
                {
                mEND(); 

                }
                break;
            case 94 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:633: NUMBER
                {
                mNUMBER(); 

                }
                break;
            case 95 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:640: COLONS
                {
                mCOLONS(); 

                }
                break;
            case 96 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:647: ID
                {
                mID(); 

                }
                break;
            case 97 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:650: CHAR_LITERAL
                {
                mCHAR_LITERAL(); 

                }
                break;
            case 98 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:663: STRING_LITERAL
                {
                mSTRING_LITERAL(); 

                }
                break;
            case 99 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:678: NEWLINE
                {
                mNEWLINE(); 

                }
                break;
            case 100 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:686: WS
                {
                mWS(); 

                }
                break;
            case 101 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:689: SINGLE_COMMENT
                {
                mSINGLE_COMMENT(); 

                }
                break;
            case 102 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:1:704: MULTI_COMMENT
                {
                mMULTI_COMMENT(); 

                }
                break;

        }

    }


    protected DFA12 dfa12 = new DFA12(this);
    static final String DFA12_eotS =
        "\1\uffff\2\54\1\70\1\uffff\1\73\1\102\1\106\1\110\1\114\7\uffff"+
        "\1\54\1\120\1\uffff\1\122\1\125\3\uffff\2\54\1\uffff\1\134\1\137"+
        "\1\141\1\uffff\13\54\6\uffff\1\54\1\164\3\54\1\uffff\1\171\5\uffff"+
        "\1\174\1\176\1\u0080\1\u0082\14\uffff\2\54\7\uffff\1\54\1\u0086"+
        "\1\u0087\1\u0088\1\uffff\1\u008b\2\uffff\1\u008e\3\uffff\1\u008f"+
        "\1\u0090\4\54\1\u0096\5\54\1\u009d\4\54\1\u00a3\1\uffff\3\54\3\uffff"+
        "\1\u00a8\7\uffff\1\u00a9\1\u00aa\1\u00ab\4\uffff\1\u00ad\2\uffff"+
        "\1\u00af\3\uffff\4\54\1\u00b4\1\uffff\6\54\1\uffff\5\54\1\uffff"+
        "\3\54\11\uffff\1\u00c3\1\u00c4\1\u00c5\1\u00c6\1\uffff\1\u00c7\1"+
        "\54\1\u00c9\1\u00ca\3\54\1\u00ce\1\54\1\u00d0\1\54\1\u00d2\2\54"+
        "\5\uffff\1\u00d5\2\uffff\1\u00d6\1\u00d7\1\54\1\uffff\1\u00d9\1"+
        "\uffff\1\54\1\uffff\1\u00db\1\u00dc\3\uffff\1\u00dd\1\uffff\1\u00de"+
        "\4\uffff";
    static final String DFA12_eofS =
        "\u00df\uffff";
    static final String DFA12_minS =
        "\1\11\1\141\1\145\1\72\1\uffff\1\75\1\45\1\55\1\75\1\52\7\uffff"+
        "\1\151\1\75\1\uffff\2\75\3\uffff\1\156\1\162\1\uffff\1\75\1\74\1"+
        "\75\1\uffff\1\146\1\150\1\154\1\141\1\150\1\156\1\162\1\145\1\157"+
        "\1\141\1\157\6\uffff\1\162\1\60\1\154\1\141\1\154\1\uffff\1\75\5"+
        "\uffff\4\75\14\uffff\1\164\1\154\7\uffff\1\144\3\60\1\uffff\1\75"+
        "\2\uffff\1\75\3\uffff\2\60\1\145\1\165\1\151\1\144\1\60\1\164\1"+
        "\145\2\164\1\145\1\60\1\160\1\144\1\143\1\164\1\60\1\uffff\1\163"+
        "\1\164\1\145\3\uffff\1\75\7\uffff\3\60\4\uffff\1\75\2\uffff\1\75"+
        "\3\uffff\1\156\2\145\1\146\1\60\1\uffff\1\141\1\154\1\156\1\150"+
        "\1\151\1\141\1\uffff\2\145\1\162\1\157\1\141\1\uffff\1\145\1\151"+
        "\1\143\11\uffff\4\60\1\uffff\1\60\1\145\2\60\1\154\1\153\1\141\1"+
        "\60\1\157\1\60\1\162\1\60\1\143\1\164\5\uffff\1\60\2\uffff\2\60"+
        "\1\164\1\uffff\1\60\1\uffff\1\144\1\uffff\2\60\3\uffff\1\60\1\uffff"+
        "\1\60\4\uffff";
    static final String DFA12_maxS =
        "\1\176\1\157\1\164\1\75\1\uffff\3\76\2\75\7\uffff\1\157\1\75\1\uffff"+
        "\1\75\1\174\3\uffff\1\164\1\162\1\uffff\1\76\2\75\1\uffff\1\156"+
        "\1\162\1\156\1\157\1\151\1\156\1\171\1\145\1\157\1\141\1\157\6\uffff"+
        "\1\162\1\172\1\154\1\141\1\154\1\uffff\1\75\5\uffff\1\76\3\75\14"+
        "\uffff\1\164\1\154\7\uffff\1\144\3\172\1\uffff\1\174\2\uffff\1\174"+
        "\3\uffff\2\172\1\145\1\165\1\163\1\144\1\172\1\164\1\151\2\164\1"+
        "\145\1\172\1\160\1\144\1\143\1\164\1\172\1\uffff\1\163\1\164\1\145"+
        "\3\uffff\1\75\7\uffff\3\172\4\uffff\1\75\2\uffff\1\75\3\uffff\1"+
        "\156\2\145\1\146\1\172\1\uffff\1\141\1\154\1\156\1\150\1\151\1\141"+
        "\1\uffff\2\145\1\162\1\157\1\141\1\uffff\1\145\1\151\1\143\11\uffff"+
        "\4\172\1\uffff\1\172\1\145\2\172\1\154\1\153\1\141\1\172\1\157\1"+
        "\172\1\162\1\172\1\143\1\164\5\uffff\1\172\2\uffff\2\172\1\164\1"+
        "\uffff\1\172\1\uffff\1\144\1\uffff\2\172\3\uffff\1\172\1\uffff\1"+
        "\172\4\uffff";
    static final String DFA12_acceptS =
        "\4\uffff\1\4\5\uffff\1\20\1\21\1\22\1\23\1\24\1\25\1\26\2\uffff"+
        "\1\31\2\uffff\1\34\1\35\1\36\2\uffff\1\45\3\uffff\1\77\13\uffff"+
        "\1\136\1\140\1\141\1\142\1\143\1\144\5\uffff\1\6\1\uffff\1\3\1\44"+
        "\1\76\1\5\1\11\4\uffff\1\100\1\10\1\13\1\101\1\102\1\12\1\15\1\14"+
        "\1\17\1\145\1\146\1\16\2\uffff\1\43\1\30\1\40\1\32\1\42\1\103\1"+
        "\33\4\uffff\1\46\1\uffff\1\52\1\50\1\uffff\1\54\1\73\1\72\22\uffff"+
        "\1\111\3\uffff\1\7\1\137\1\47\1\uffff\1\53\1\51\1\55\1\71\1\70\1"+
        "\75\1\74\3\uffff\1\114\1\134\1\41\1\61\1\uffff\1\60\1\57\1\uffff"+
        "\1\56\1\105\1\126\5\uffff\1\112\6\uffff\1\121\5\uffff\1\125\3\uffff"+
        "\1\63\1\62\1\27\1\132\1\37\1\65\1\64\1\67\1\66\4\uffff\1\135\16"+
        "\uffff\1\106\1\131\1\107\1\110\1\123\1\uffff\1\115\1\133\3\uffff"+
        "\1\122\1\uffff\1\127\1\uffff\1\130\2\uffff\1\113\1\116\1\117\1\uffff"+
        "\1\124\1\uffff\1\2\1\104\1\120\1\1";
    static final String DFA12_specialS =
        "\u00df\uffff}>";
    static final String[] DFA12_transitionS = {
            "\1\60\1\57\2\uffff\1\57\22\uffff\1\60\1\33\1\56\1\20\1\uffff"+
            "\1\36\1\24\1\55\1\12\1\13\1\10\1\6\1\4\1\7\1\37\1\11\12\53\1"+
            "\3\1\27\1\35\1\5\1\34\1\30\1\23\32\54\1\16\1\uffff\1\17\1\26"+
            "\1\54\1\uffff\1\31\1\46\1\50\1\43\1\42\1\1\1\52\1\54\1\40\3"+
            "\54\1\51\1\21\1\32\2\54\1\47\1\2\1\41\1\45\1\54\1\44\3\54\1"+
            "\14\1\25\1\15\1\22",
            "\1\63\7\uffff\1\62\5\uffff\1\61",
            "\1\65\16\uffff\1\64",
            "\1\67\2\uffff\1\66",
            "",
            "\1\71\1\72",
            "\1\100\5\uffff\1\101\3\uffff\1\77\14\uffff\1\76\1\74\1\75",
            "\1\104\17\uffff\1\103\1\105",
            "\1\107",
            "\1\113\4\uffff\1\112\15\uffff\1\111",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\116\5\uffff\1\115",
            "\1\117",
            "",
            "\1\121",
            "\1\123\76\uffff\1\124",
            "",
            "",
            "",
            "\1\126\4\uffff\1\130\1\127",
            "\1\131",
            "",
            "\1\132\1\133",
            "\1\136\1\135",
            "\1\140",
            "",
            "\1\142\7\uffff\1\143",
            "\1\144\11\uffff\1\145",
            "\1\146\1\uffff\1\147",
            "\1\151\15\uffff\1\150",
            "\1\152\1\153",
            "\1\154",
            "\1\155\6\uffff\1\156",
            "\1\157",
            "\1\160",
            "\1\161",
            "\1\162",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\163",
            "\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32\54",
            "\1\165",
            "\1\166",
            "\1\167",
            "",
            "\1\170",
            "",
            "",
            "",
            "",
            "",
            "\1\172\1\173",
            "\1\175",
            "\1\177",
            "\1\u0081",
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
            "\1\u0083",
            "\1\u0084",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\u0085",
            "\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32\54",
            "\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32\54",
            "\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32\54",
            "",
            "\1\u0089\76\uffff\1\u008a",
            "",
            "",
            "\1\u008c\76\uffff\1\u008d",
            "",
            "",
            "",
            "\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32\54",
            "\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32\54",
            "\1\u0091",
            "\1\u0092",
            "\1\u0094\11\uffff\1\u0093",
            "\1\u0095",
            "\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32\54",
            "\1\u0097",
            "\1\u0099\3\uffff\1\u0098",
            "\1\u009a",
            "\1\u009b",
            "\1\u009c",
            "\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32\54",
            "\1\u009e",
            "\1\u009f",
            "\1\u00a0",
            "\1\u00a1",
            "\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\26\54\1\u00a2\3\54",
            "",
            "\1\u00a4",
            "\1\u00a5",
            "\1\u00a6",
            "",
            "",
            "",
            "\1\u00a7",
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
            "",
            "",
            "",
            "",
            "\1\u00ac",
            "",
            "",
            "\1\u00ae",
            "",
            "",
            "",
            "\1\u00b0",
            "\1\u00b1",
            "\1\u00b2",
            "\1\u00b3",
            "\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32\54",
            "",
            "\1\u00b5",
            "\1\u00b6",
            "\1\u00b7",
            "\1\u00b8",
            "\1\u00b9",
            "\1\u00ba",
            "",
            "\1\u00bb",
            "\1\u00bc",
            "\1\u00bd",
            "\1\u00be",
            "\1\u00bf",
            "",
            "\1\u00c0",
            "\1\u00c1",
            "\1\u00c2",
            "",
            "",
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
            "",
            "\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32\54",
            "\1\u00c8",
            "\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32\54",
            "\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32\54",
            "\1\u00cb",
            "\1\u00cc",
            "\1\u00cd",
            "\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32\54",
            "\1\u00cf",
            "\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32\54",
            "\1\u00d1",
            "\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32\54",
            "\1\u00d3",
            "\1\u00d4",
            "",
            "",
            "",
            "",
            "",
            "\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32\54",
            "",
            "",
            "\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32\54",
            "\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32\54",
            "\1\u00d8",
            "",
            "\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32\54",
            "",
            "\1\u00da",
            "",
            "\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32\54",
            "\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32\54",
            "",
            "",
            "",
            "\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32\54",
            "",
            "\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32\54",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA12_eot = DFA.unpackEncodedString(DFA12_eotS);
    static final short[] DFA12_eof = DFA.unpackEncodedString(DFA12_eofS);
    static final char[] DFA12_min = DFA.unpackEncodedStringToUnsignedChars(DFA12_minS);
    static final char[] DFA12_max = DFA.unpackEncodedStringToUnsignedChars(DFA12_maxS);
    static final short[] DFA12_accept = DFA.unpackEncodedString(DFA12_acceptS);
    static final short[] DFA12_special = DFA.unpackEncodedString(DFA12_specialS);
    static final short[][] DFA12_transition;

    static {
        int numStates = DFA12_transitionS.length;
        DFA12_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA12_transition[i] = DFA.unpackEncodedString(DFA12_transitionS[i]);
        }
    }

    class DFA12 extends DFA {

        public DFA12(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 12;
            this.eot = DFA12_eot;
            this.eof = DFA12_eof;
            this.min = DFA12_min;
            this.max = DFA12_max;
            this.accept = DFA12_accept;
            this.special = DFA12_special;
            this.transition = DFA12_transition;
        }
        public String getDescription() {
            return "1:1: Tokens : ( FORWARD | STATIC | COLON | COMMA | EQUALS | COLON_EQUALS | COLON_COLON_EQUALS | PLUS | PLUS_EQ | MINUS | MINUS_EQ | STAR | STAR_EQ | SLASH | SLASH_EQ | LPAREN | RPAREN | LBRACE | RBRACE | LBRACKET | RBRACKET | HASH | NOT | TILDE | ATSIGN | AMP | BAR | CARET | SEMI | QUESTION | AND | AND_EQ | OR | OR_EQ | XOR_EQ | COMPEQ | COMPNE | COMPGE | COMPUGE | COMPLE | COMPULE | GREATER | UGREATER | LESS | ULESS | LSHIFT | LSHIFT_EQ | RSHIFT | RSHIFT_EQ | URSHIFT | URSHIFT_EQ | CRSHIFT | CRSHIFT_EQ | CLSHIFT | CLSHIFT_EQ | UDIV | UDIV_EQ | PERCENT | PERCENT_EQ | UMOD | UMOD_EQ | ARROW | PERIOD | PLUSPLUS | MINUSMINUS | POINTS | BAR_BAR | SELECT | IF | THEN | ELSE | ELIF | FI | DO | WHILE | AT | WHEN | UNTIL | BREAK | REPEAT | BY | CODE | DATA | MACRO | FOR | IN | GOTO | FALSE | TRUE | NIL | WITH | AS | END | NUMBER | COLONS | ID | CHAR_LITERAL | STRING_LITERAL | NEWLINE | WS | SINGLE_COMMENT | MULTI_COMMENT );";
        }
    }
 

}