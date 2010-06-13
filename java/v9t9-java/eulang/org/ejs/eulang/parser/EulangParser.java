// $ANTLR 3.2 Sep 23, 2009 12:02:23 /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g 2010-06-13 09:16:02

package org.ejs.eulang.parser;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import org.antlr.runtime.tree.*;

public class EulangParser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "SCOPE", "ADDSCOPE", "EXTENDSCOPE", "ATTRS", "LIST_COMPREHENSION", "CODE", "STMTLIST", "PROTO", "ARGLIST", "ARGDEF", "REF", "ALLOC", "ALLOC_TUPLE", "ASSIGN", "DEFINE", "EXPR", "LIST", "TYPE", "STMTEXPR", "CONDLIST", "CONDTEST", "CALL", "INLINE", "CAST", "COND", "BITAND", "BITOR", "BITXOR", "ADD", "SUB", "MUL", "DIV", "UDIV", "MOD", "NEG", "INV", "POSTINC", "POSTDEC", "PREINC", "PREDEC", "LIT", "IDREF", "IDLIST", "LABEL", "GOTO", "BLOCK", "TUPLE", "TUPLETYPE", "LABELSTMT", "BINDING", "FIELDREF", "ARRAY", "INDEX", "POINTER", "DEREF", "ADDRREF", "ADDROF", "SIZEOF", "TYPEOF", "INITEXPR", "INITLIST", "INSTANCE", "GENERIC", "SEMI", "FORWARD", "ID", "COMMA", "COLON_EQUALS", "COLON", "EQUALS", "PLUS", "PLUS_EQ", "LBRACKET", "RBRACKET", "EQUALS_COLON", "LBRACE", "RBRACE", "FOR", "IN", "ATTR", "LPAREN", "RPAREN", "ARROW", "NIL", "QUESTION", "CARET", "MINUS_EQ", "STAR_EQ", "SLASH_EQ", "REM_EQ", "UDIV_EQ", "UREM_EQ", "MOD_EQ", "AND_EQ", "OR_EQ", "XOR_EQ", "LSHIFT_EQ", "RSHIFT_EQ", "URSHIFT_EQ", "CLSHIFT_EQ", "CRSHIFT_EQ", "PERIOD", "DO", "WHILE", "REPEAT", "AND", "BY", "AT", "BREAK", "ATSIGN", "IF", "THEN", "ELIF", "ELSE", "FI", "OR", "NOT", "COMPEQ", "COMPNE", "COMPLE", "COMPGE", "COMPULE", "COMPUGE", "LESS", "ULESS", "GREATER", "UGREATER", "BAR", "TILDE", "AMP", "LSHIFT", "RSHIFT", "URSHIFT", "CRSHIFT", "CLSHIFT", "MINUS", "STAR", "SLASH", "REM", "UREM", "PLUSPLUS", "MINUSMINUS", "NUMBER", "CHAR_LITERAL", "STRING_LITERAL", "AS", "FALSE", "TRUE", "COLONS", "DATA", "STATIC", "COLON_COLON_EQUALS", "HASH", "POINTS", "BAR_BAR", "SELECT", "WHEN", "UNTIL", "WITH", "END", "IDSUFFIX", "LETTERLIKE", "DIGIT", "NEWLINE", "WS", "SINGLE_COMMENT", "MULTI_COMMENT"
    };
    public static final int CAST=27;
    public static final int CONDTEST=24;
    public static final int STAR=140;
    public static final int WHILE=107;
    public static final int GENERIC=66;
    public static final int MOD=37;
    public static final int POINTER=57;
    public static final int LSHIFT_EQ=100;
    public static final int PREDEC=43;
    public static final int REM_EQ=93;
    public static final int DEREF=58;
    public static final int MINUSMINUS=145;
    public static final int DO=106;
    public static final int ARGLIST=12;
    public static final int EQUALS=73;
    public static final int NOT=120;
    public static final int EOF=-1;
    public static final int BREAK=112;
    public static final int TYPE=21;
    public static final int CODE=9;
    public static final int LBRACKET=76;
    public static final int TUPLE=50;
    public static final int RPAREN=85;
    public static final int STRING_LITERAL=148;
    public static final int GREATER=129;
    public static final int ADDRREF=59;
    public static final int ADDSCOPE=5;
    public static final int UREM_EQ=95;
    public static final int EXTENDSCOPE=6;
    public static final int COMPLE=123;
    public static final int AND_EQ=97;
    public static final int CARET=89;
    public static final int LESS=127;
    public static final int XOR_EQ=99;
    public static final int INITEXPR=63;
    public static final int INITLIST=64;
    public static final int ATSIGN=113;
    public static final int GOTO=48;
    public static final int SELECT=159;
    public static final int CLSHIFT_EQ=103;
    public static final int ARRAY=55;
    public static final int LABELSTMT=52;
    public static final int CRSHIFT=137;
    public static final int RBRACE=80;
    public static final int STMTEXPR=22;
    public static final int STATIC=154;
    public static final int PERIOD=105;
    public static final int LSHIFT=134;
    public static final int INV=39;
    public static final int ADDROF=60;
    public static final int ELSE=117;
    public static final int NUMBER=146;
    public static final int UDIV=36;
    public static final int LIT=44;
    public static final int CRSHIFT_EQ=104;
    public static final int UDIV_EQ=94;
    public static final int LIST=20;
    public static final int PLUS_EQ=75;
    public static final int MUL=34;
    public static final int RSHIFT_EQ=101;
    public static final int ARGDEF=13;
    public static final int FI=118;
    public static final int MINUS_EQ=90;
    public static final int ELIF=116;
    public static final int WS=168;
    public static final int OR_EQ=98;
    public static final int BITOR=30;
    public static final int NIL=87;
    public static final int TYPEOF=62;
    public static final int UNTIL=161;
    public static final int STMTLIST=10;
    public static final int OR=119;
    public static final int SIZEOF=61;
    public static final int ALLOC=15;
    public static final int IDLIST=46;
    public static final int REPEAT=108;
    public static final int INLINE=26;
    public static final int CALL=25;
    public static final int POSTINC=40;
    public static final int END=163;
    public static final int FALSE=150;
    public static final int COMPULE=125;
    public static final int POSTDEC=41;
    public static final int MOD_EQ=96;
    public static final int BINDING=53;
    public static final int FORWARD=68;
    public static final int BAR_BAR=158;
    public static final int AMP=133;
    public static final int POINTS=157;
    public static final int PLUSPLUS=144;
    public static final int UGREATER=130;
    public static final int LBRACE=79;
    public static final int MULTI_COMMENT=170;
    public static final int FIELDREF=54;
    public static final int FOR=81;
    public static final int SUB=33;
    public static final int AND=109;
    public static final int ID=69;
    public static final int DEFINE=18;
    public static final int UREM=143;
    public static final int BITAND=29;
    public static final int LPAREN=84;
    public static final int IF=114;
    public static final int COLONS=152;
    public static final int COLON_COLON_EQUALS=155;
    public static final int AT=111;
    public static final int AS=149;
    public static final int INDEX=56;
    public static final int CONDLIST=23;
    public static final int IDSUFFIX=164;
    public static final int SLASH=141;
    public static final int EXPR=19;
    public static final int THEN=115;
    public static final int IN=82;
    public static final int SCOPE=4;
    public static final int COMMA=70;
    public static final int PREINC=42;
    public static final int BITXOR=31;
    public static final int TILDE=132;
    public static final int PLUS=74;
    public static final int SINGLE_COMMENT=169;
    public static final int DIGIT=166;
    public static final int RBRACKET=77;
    public static final int RSHIFT=135;
    public static final int ATTR=83;
    public static final int WITH=162;
    public static final int ADD=32;
    public static final int EQUALS_COLON=78;
    public static final int COMPGE=124;
    public static final int URSHIFT_EQ=102;
    public static final int ULESS=128;
    public static final int BY=110;
    public static final int LETTERLIKE=165;
    public static final int LIST_COMPREHENSION=8;
    public static final int HASH=156;
    public static final int CLSHIFT=138;
    public static final int ATTRS=7;
    public static final int STAR_EQ=91;
    public static final int REM=142;
    public static final int MINUS=139;
    public static final int TRUE=151;
    public static final int SEMI=67;
    public static final int REF=14;
    public static final int COLON=72;
    public static final int TUPLETYPE=51;
    public static final int COLON_EQUALS=71;
    public static final int NEWLINE=167;
    public static final int QUESTION=88;
    public static final int CHAR_LITERAL=147;
    public static final int LABEL=47;
    public static final int WHEN=160;
    public static final int INSTANCE=65;
    public static final int BLOCK=49;
    public static final int NEG=38;
    public static final int ASSIGN=17;
    public static final int URSHIFT=136;
    public static final int ARROW=86;
    public static final int COMPEQ=121;
    public static final int IDREF=45;
    public static final int DIV=35;
    public static final int COND=28;
    public static final int PROTO=11;
    public static final int COMPNE=122;
    public static final int DATA=153;
    public static final int BAR=131;
    public static final int COMPUGE=126;
    public static final int ALLOC_TUPLE=16;
    public static final int SLASH_EQ=92;

    // delegates
    // delegators


        public EulangParser(TokenStream input) {
            this(input, new RecognizerSharedState());
        }
        public EulangParser(TokenStream input, RecognizerSharedState state) {
            super(input, state);
             
        }
        
    protected TreeAdaptor adaptor = new CommonTreeAdaptor();

    public void setTreeAdaptor(TreeAdaptor adaptor) {
        this.adaptor = adaptor;
    }
    public TreeAdaptor getTreeAdaptor() {
        return adaptor;
    }

    public String[] getTokenNames() { return EulangParser.tokenNames; }
    public String getGrammarFileName() { return "/home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g"; }


        public String getTokenErrorDisplay(Token t) {
            return '\'' + t.getText() + '\'';
        }

      protected CommonTree split(CommonTree items) {
            if (items == null) return null;
            StringBuilder sb = new StringBuilder();
            if (items.getText()!=null) sb.append(items.getText());
            for (int  i = 0; i < items.getChildCount(); i++)
              sb.append(items.getChild(i).getText());
            //return new CommonTree(new CommonToken(COLONS, sb.toString()));
            CommonTree out = new CommonTree();
            for (int i = 0; i < sb.length(); i++) 
              out.addChild(new CommonTree(new CommonToken(COLON, ":")));
              return out;
          }


    public static class prog_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "prog"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:129:1: prog : toplevelstmts EOF ;
    public final EulangParser.prog_return prog() throws RecognitionException {
        EulangParser.prog_return retval = new EulangParser.prog_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token EOF2=null;
        EulangParser.toplevelstmts_return toplevelstmts1 = null;


        CommonTree EOF2_tree=null;

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:129:5: ( toplevelstmts EOF )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:129:9: toplevelstmts EOF
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_toplevelstmts_in_prog440);
            toplevelstmts1=toplevelstmts();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, toplevelstmts1.getTree());
            EOF2=(Token)match(input,EOF,FOLLOW_EOF_in_prog442); if (state.failed) return retval;

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "prog"

    public static class toplevelstmts_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "toplevelstmts"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:132:1: toplevelstmts : ( toplevelstat )* -> ^( STMTLIST ( toplevelstat )* ) ;
    public final EulangParser.toplevelstmts_return toplevelstmts() throws RecognitionException {
        EulangParser.toplevelstmts_return retval = new EulangParser.toplevelstmts_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.toplevelstat_return toplevelstat3 = null;


        RewriteRuleSubtreeStream stream_toplevelstat=new RewriteRuleSubtreeStream(adaptor,"rule toplevelstat");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:132:14: ( ( toplevelstat )* -> ^( STMTLIST ( toplevelstat )* ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:132:16: ( toplevelstat )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:132:16: ( toplevelstat )*
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( (LA1_0==CODE||(LA1_0>=SIZEOF && LA1_0<=TYPEOF)||(LA1_0>=FORWARD && LA1_0<=ID)||LA1_0==COLON||LA1_0==LBRACE||LA1_0==LPAREN||LA1_0==NIL||LA1_0==IF||LA1_0==NOT||(LA1_0>=TILDE && LA1_0<=AMP)||LA1_0==MINUS||(LA1_0>=PLUSPLUS && LA1_0<=STRING_LITERAL)||(LA1_0>=FALSE && LA1_0<=COLONS)) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:132:16: toplevelstat
            	    {
            	    pushFollow(FOLLOW_toplevelstat_in_toplevelstmts471);
            	    toplevelstat3=toplevelstat();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_toplevelstat.add(toplevelstat3.getTree());

            	    }
            	    break;

            	default :
            	    break loop1;
                }
            } while (true);



            // AST REWRITE
            // elements: toplevelstat
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 132:35: -> ^( STMTLIST ( toplevelstat )* )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:132:38: ^( STMTLIST ( toplevelstat )* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(STMTLIST, "STMTLIST"), root_1);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:132:49: ( toplevelstat )*
                while ( stream_toplevelstat.hasNext() ) {
                    adaptor.addChild(root_1, stream_toplevelstat.nextTree());

                }
                stream_toplevelstat.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "toplevelstmts"

    public static class toplevelstmtsNoAlloc_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "toplevelstmtsNoAlloc"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:134:1: toplevelstmtsNoAlloc : ( toplevelstatNoAlloc )* -> ^( STMTLIST ( toplevelstatNoAlloc )* ) ;
    public final EulangParser.toplevelstmtsNoAlloc_return toplevelstmtsNoAlloc() throws RecognitionException {
        EulangParser.toplevelstmtsNoAlloc_return retval = new EulangParser.toplevelstmtsNoAlloc_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.toplevelstatNoAlloc_return toplevelstatNoAlloc4 = null;


        RewriteRuleSubtreeStream stream_toplevelstatNoAlloc=new RewriteRuleSubtreeStream(adaptor,"rule toplevelstatNoAlloc");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:134:21: ( ( toplevelstatNoAlloc )* -> ^( STMTLIST ( toplevelstatNoAlloc )* ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:134:23: ( toplevelstatNoAlloc )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:134:23: ( toplevelstatNoAlloc )*
            loop2:
            do {
                int alt2=2;
                int LA2_0 = input.LA(1);

                if ( (LA2_0==CODE||(LA2_0>=SIZEOF && LA2_0<=TYPEOF)||(LA2_0>=FORWARD && LA2_0<=ID)||LA2_0==COLON||LA2_0==LBRACE||LA2_0==LPAREN||LA2_0==NIL||LA2_0==IF||LA2_0==NOT||(LA2_0>=TILDE && LA2_0<=AMP)||LA2_0==MINUS||(LA2_0>=PLUSPLUS && LA2_0<=STRING_LITERAL)||(LA2_0>=FALSE && LA2_0<=COLONS)) ) {
                    alt2=1;
                }


                switch (alt2) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:134:23: toplevelstatNoAlloc
            	    {
            	    pushFollow(FOLLOW_toplevelstatNoAlloc_in_toplevelstmtsNoAlloc498);
            	    toplevelstatNoAlloc4=toplevelstatNoAlloc();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_toplevelstatNoAlloc.add(toplevelstatNoAlloc4.getTree());

            	    }
            	    break;

            	default :
            	    break loop2;
                }
            } while (true);



            // AST REWRITE
            // elements: toplevelstatNoAlloc
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 134:49: -> ^( STMTLIST ( toplevelstatNoAlloc )* )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:134:52: ^( STMTLIST ( toplevelstatNoAlloc )* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(STMTLIST, "STMTLIST"), root_1);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:134:63: ( toplevelstatNoAlloc )*
                while ( stream_toplevelstatNoAlloc.hasNext() ) {
                    adaptor.addChild(root_1, stream_toplevelstatNoAlloc.nextTree());

                }
                stream_toplevelstatNoAlloc.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "toplevelstmtsNoAlloc"

    public static class toplevelstat_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "toplevelstat"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:137:1: toplevelstat : ( toplevelstatNoAlloc -> toplevelstatNoAlloc | toplevelAlloc SEMI -> toplevelAlloc );
    public final EulangParser.toplevelstat_return toplevelstat() throws RecognitionException {
        EulangParser.toplevelstat_return retval = new EulangParser.toplevelstat_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token SEMI7=null;
        EulangParser.toplevelstatNoAlloc_return toplevelstatNoAlloc5 = null;

        EulangParser.toplevelAlloc_return toplevelAlloc6 = null;


        CommonTree SEMI7_tree=null;
        RewriteRuleTokenStream stream_SEMI=new RewriteRuleTokenStream(adaptor,"token SEMI");
        RewriteRuleSubtreeStream stream_toplevelstatNoAlloc=new RewriteRuleSubtreeStream(adaptor,"rule toplevelstatNoAlloc");
        RewriteRuleSubtreeStream stream_toplevelAlloc=new RewriteRuleSubtreeStream(adaptor,"rule toplevelAlloc");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:137:14: ( toplevelstatNoAlloc -> toplevelstatNoAlloc | toplevelAlloc SEMI -> toplevelAlloc )
            int alt3=2;
            alt3 = dfa3.predict(input);
            switch (alt3) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:137:16: toplevelstatNoAlloc
                    {
                    pushFollow(FOLLOW_toplevelstatNoAlloc_in_toplevelstat527);
                    toplevelstatNoAlloc5=toplevelstatNoAlloc();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_toplevelstatNoAlloc.add(toplevelstatNoAlloc5.getTree());


                    // AST REWRITE
                    // elements: toplevelstatNoAlloc
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 137:36: -> toplevelstatNoAlloc
                    {
                        adaptor.addChild(root_0, stream_toplevelstatNoAlloc.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:138:7: toplevelAlloc SEMI
                    {
                    pushFollow(FOLLOW_toplevelAlloc_in_toplevelstat540);
                    toplevelAlloc6=toplevelAlloc();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_toplevelAlloc.add(toplevelAlloc6.getTree());
                    SEMI7=(Token)match(input,SEMI,FOLLOW_SEMI_in_toplevelstat542); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_SEMI.add(SEMI7);



                    // AST REWRITE
                    // elements: toplevelAlloc
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 138:26: -> toplevelAlloc
                    {
                        adaptor.addChild(root_0, stream_toplevelAlloc.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "toplevelstat"

    public static class toplevelstatNoAlloc_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "toplevelstatNoAlloc"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:140:1: toplevelstatNoAlloc : ( defineStmt | scopeExtension ( SEMI )? -> scopeExtension | FORWARD ID ( COMMA ID )* SEMI -> ( ^( FORWARD ID ) )+ | rhsExpr SEMI -> ^( EXPR rhsExpr ) | ( LBRACE )=> xscope ( SEMI )? );
    public final EulangParser.toplevelstatNoAlloc_return toplevelstatNoAlloc() throws RecognitionException {
        EulangParser.toplevelstatNoAlloc_return retval = new EulangParser.toplevelstatNoAlloc_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token SEMI10=null;
        Token FORWARD11=null;
        Token ID12=null;
        Token COMMA13=null;
        Token ID14=null;
        Token SEMI15=null;
        Token SEMI17=null;
        Token SEMI19=null;
        EulangParser.defineStmt_return defineStmt8 = null;

        EulangParser.scopeExtension_return scopeExtension9 = null;

        EulangParser.rhsExpr_return rhsExpr16 = null;

        EulangParser.xscope_return xscope18 = null;


        CommonTree SEMI10_tree=null;
        CommonTree FORWARD11_tree=null;
        CommonTree ID12_tree=null;
        CommonTree COMMA13_tree=null;
        CommonTree ID14_tree=null;
        CommonTree SEMI15_tree=null;
        CommonTree SEMI17_tree=null;
        CommonTree SEMI19_tree=null;
        RewriteRuleTokenStream stream_FORWARD=new RewriteRuleTokenStream(adaptor,"token FORWARD");
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleTokenStream stream_SEMI=new RewriteRuleTokenStream(adaptor,"token SEMI");
        RewriteRuleSubtreeStream stream_scopeExtension=new RewriteRuleSubtreeStream(adaptor,"rule scopeExtension");
        RewriteRuleSubtreeStream stream_rhsExpr=new RewriteRuleSubtreeStream(adaptor,"rule rhsExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:140:20: ( defineStmt | scopeExtension ( SEMI )? -> scopeExtension | FORWARD ID ( COMMA ID )* SEMI -> ( ^( FORWARD ID ) )+ | rhsExpr SEMI -> ^( EXPR rhsExpr ) | ( LBRACE )=> xscope ( SEMI )? )
            int alt7=5;
            alt7 = dfa7.predict(input);
            switch (alt7) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:140:22: defineStmt
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_defineStmt_in_toplevelstatNoAlloc557);
                    defineStmt8=defineStmt();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, defineStmt8.getTree());

                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:141:7: scopeExtension ( SEMI )?
                    {
                    pushFollow(FOLLOW_scopeExtension_in_toplevelstatNoAlloc565);
                    scopeExtension9=scopeExtension();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_scopeExtension.add(scopeExtension9.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:141:22: ( SEMI )?
                    int alt4=2;
                    int LA4_0 = input.LA(1);

                    if ( (LA4_0==SEMI) ) {
                        alt4=1;
                    }
                    switch (alt4) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:141:22: SEMI
                            {
                            SEMI10=(Token)match(input,SEMI,FOLLOW_SEMI_in_toplevelstatNoAlloc567); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_SEMI.add(SEMI10);


                            }
                            break;

                    }



                    // AST REWRITE
                    // elements: scopeExtension
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 141:28: -> scopeExtension
                    {
                        adaptor.addChild(root_0, stream_scopeExtension.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 3 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:142:7: FORWARD ID ( COMMA ID )* SEMI
                    {
                    FORWARD11=(Token)match(input,FORWARD,FOLLOW_FORWARD_in_toplevelstatNoAlloc581); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_FORWARD.add(FORWARD11);

                    ID12=(Token)match(input,ID,FOLLOW_ID_in_toplevelstatNoAlloc583); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ID.add(ID12);

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:142:18: ( COMMA ID )*
                    loop5:
                    do {
                        int alt5=2;
                        int LA5_0 = input.LA(1);

                        if ( (LA5_0==COMMA) ) {
                            alt5=1;
                        }


                        switch (alt5) {
                    	case 1 :
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:142:19: COMMA ID
                    	    {
                    	    COMMA13=(Token)match(input,COMMA,FOLLOW_COMMA_in_toplevelstatNoAlloc586); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA13);

                    	    ID14=(Token)match(input,ID,FOLLOW_ID_in_toplevelstatNoAlloc588); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_ID.add(ID14);


                    	    }
                    	    break;

                    	default :
                    	    break loop5;
                        }
                    } while (true);

                    SEMI15=(Token)match(input,SEMI,FOLLOW_SEMI_in_toplevelstatNoAlloc592); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_SEMI.add(SEMI15);



                    // AST REWRITE
                    // elements: FORWARD, ID
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 142:35: -> ( ^( FORWARD ID ) )+
                    {
                        if ( !(stream_FORWARD.hasNext()||stream_ID.hasNext()) ) {
                            throw new RewriteEarlyExitException();
                        }
                        while ( stream_FORWARD.hasNext()||stream_ID.hasNext() ) {
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:142:38: ^( FORWARD ID )
                            {
                            CommonTree root_1 = (CommonTree)adaptor.nil();
                            root_1 = (CommonTree)adaptor.becomeRoot(stream_FORWARD.nextNode(), root_1);

                            adaptor.addChild(root_1, stream_ID.nextNode());

                            adaptor.addChild(root_0, root_1);
                            }

                        }
                        stream_FORWARD.reset();
                        stream_ID.reset();

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 4 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:143:7: rhsExpr SEMI
                    {
                    pushFollow(FOLLOW_rhsExpr_in_toplevelstatNoAlloc609);
                    rhsExpr16=rhsExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_rhsExpr.add(rhsExpr16.getTree());
                    SEMI17=(Token)match(input,SEMI,FOLLOW_SEMI_in_toplevelstatNoAlloc628); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_SEMI.add(SEMI17);



                    // AST REWRITE
                    // elements: rhsExpr
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 143:38: -> ^( EXPR rhsExpr )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:143:41: ^( EXPR rhsExpr )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(EXPR, "EXPR"), root_1);

                        adaptor.addChild(root_1, stream_rhsExpr.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 5 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:144:7: ( LBRACE )=> xscope ( SEMI )?
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_xscope_in_toplevelstatNoAlloc651);
                    xscope18=xscope();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, xscope18.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:144:26: ( SEMI )?
                    int alt6=2;
                    int LA6_0 = input.LA(1);

                    if ( (LA6_0==SEMI) ) {
                        alt6=1;
                    }
                    switch (alt6) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:144:26: SEMI
                            {
                            SEMI19=(Token)match(input,SEMI,FOLLOW_SEMI_in_toplevelstatNoAlloc653); if (state.failed) return retval;
                            if ( state.backtracking==0 ) {
                            SEMI19_tree = (CommonTree)adaptor.create(SEMI19);
                            adaptor.addChild(root_0, SEMI19_tree);
                            }

                            }
                            break;

                    }


                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "toplevelstatNoAlloc"

    public static class toplevelAlloc_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "toplevelAlloc"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:147:1: toplevelAlloc : ( toplevelSingleVarDecl | toplevelTupleVarDecl );
    public final EulangParser.toplevelAlloc_return toplevelAlloc() throws RecognitionException {
        EulangParser.toplevelAlloc_return retval = new EulangParser.toplevelAlloc_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.toplevelSingleVarDecl_return toplevelSingleVarDecl20 = null;

        EulangParser.toplevelTupleVarDecl_return toplevelTupleVarDecl21 = null;



        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:147:15: ( toplevelSingleVarDecl | toplevelTupleVarDecl )
            int alt8=2;
            int LA8_0 = input.LA(1);

            if ( (LA8_0==ID) ) {
                alt8=1;
            }
            else if ( (LA8_0==LPAREN) ) {
                alt8=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 8, 0, input);

                throw nvae;
            }
            switch (alt8) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:147:17: toplevelSingleVarDecl
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_toplevelSingleVarDecl_in_toplevelAlloc668);
                    toplevelSingleVarDecl20=toplevelSingleVarDecl();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, toplevelSingleVarDecl20.getTree());

                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:147:41: toplevelTupleVarDecl
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_toplevelTupleVarDecl_in_toplevelAlloc672);
                    toplevelTupleVarDecl21=toplevelTupleVarDecl();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, toplevelTupleVarDecl21.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "toplevelAlloc"

    public static class toplevelSingleVarDecl_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "toplevelSingleVarDecl"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:149:1: toplevelSingleVarDecl : ID ( attrs )? ( ( COLON_EQUALS rhsExprOrInitList -> ^( ALLOC ( attrs )? TYPE ID rhsExprOrInitList ) ) | ( COLON type ( EQUALS rhsExprOrInitList )? -> ^( ALLOC ( attrs )? type ID ( rhsExprOrInitList )* ) ) | ( COMMA ID )+ ( ( COLON_EQUALS ( PLUS )? rhsExprOrInitList ( COMMA rhsExprOrInitList )* ) -> ^( ALLOC ( attrs )? TYPE ^( LIST ( ID )+ ) ( PLUS )? ^( LIST ( rhsExprOrInitList )+ ) ) | ( COLON type ( EQUALS ( PLUS )? rhsExprOrInitList ( COMMA rhsExprOrInitList )* )? ) -> ^( ALLOC ( attrs )? type ^( LIST ( ID )+ ) ( PLUS )? ( ^( LIST ( rhsExprOrInitList )+ ) )? ) ) ) ;
    public final EulangParser.toplevelSingleVarDecl_return toplevelSingleVarDecl() throws RecognitionException {
        EulangParser.toplevelSingleVarDecl_return retval = new EulangParser.toplevelSingleVarDecl_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ID22=null;
        Token COLON_EQUALS24=null;
        Token COLON26=null;
        Token EQUALS28=null;
        Token COMMA30=null;
        Token ID31=null;
        Token COLON_EQUALS32=null;
        Token PLUS33=null;
        Token COMMA35=null;
        Token COLON37=null;
        Token EQUALS39=null;
        Token PLUS40=null;
        Token COMMA42=null;
        EulangParser.attrs_return attrs23 = null;

        EulangParser.rhsExprOrInitList_return rhsExprOrInitList25 = null;

        EulangParser.type_return type27 = null;

        EulangParser.rhsExprOrInitList_return rhsExprOrInitList29 = null;

        EulangParser.rhsExprOrInitList_return rhsExprOrInitList34 = null;

        EulangParser.rhsExprOrInitList_return rhsExprOrInitList36 = null;

        EulangParser.type_return type38 = null;

        EulangParser.rhsExprOrInitList_return rhsExprOrInitList41 = null;

        EulangParser.rhsExprOrInitList_return rhsExprOrInitList43 = null;


        CommonTree ID22_tree=null;
        CommonTree COLON_EQUALS24_tree=null;
        CommonTree COLON26_tree=null;
        CommonTree EQUALS28_tree=null;
        CommonTree COMMA30_tree=null;
        CommonTree ID31_tree=null;
        CommonTree COLON_EQUALS32_tree=null;
        CommonTree PLUS33_tree=null;
        CommonTree COMMA35_tree=null;
        CommonTree COLON37_tree=null;
        CommonTree EQUALS39_tree=null;
        CommonTree PLUS40_tree=null;
        CommonTree COMMA42_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleTokenStream stream_PLUS=new RewriteRuleTokenStream(adaptor,"token PLUS");
        RewriteRuleTokenStream stream_EQUALS=new RewriteRuleTokenStream(adaptor,"token EQUALS");
        RewriteRuleTokenStream stream_COLON_EQUALS=new RewriteRuleTokenStream(adaptor,"token COLON_EQUALS");
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleSubtreeStream stream_rhsExprOrInitList=new RewriteRuleSubtreeStream(adaptor,"rule rhsExprOrInitList");
        RewriteRuleSubtreeStream stream_attrs=new RewriteRuleSubtreeStream(adaptor,"rule attrs");
        RewriteRuleSubtreeStream stream_type=new RewriteRuleSubtreeStream(adaptor,"rule type");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:149:22: ( ID ( attrs )? ( ( COLON_EQUALS rhsExprOrInitList -> ^( ALLOC ( attrs )? TYPE ID rhsExprOrInitList ) ) | ( COLON type ( EQUALS rhsExprOrInitList )? -> ^( ALLOC ( attrs )? type ID ( rhsExprOrInitList )* ) ) | ( COMMA ID )+ ( ( COLON_EQUALS ( PLUS )? rhsExprOrInitList ( COMMA rhsExprOrInitList )* ) -> ^( ALLOC ( attrs )? TYPE ^( LIST ( ID )+ ) ( PLUS )? ^( LIST ( rhsExprOrInitList )+ ) ) | ( COLON type ( EQUALS ( PLUS )? rhsExprOrInitList ( COMMA rhsExprOrInitList )* )? ) -> ^( ALLOC ( attrs )? type ^( LIST ( ID )+ ) ( PLUS )? ( ^( LIST ( rhsExprOrInitList )+ ) )? ) ) ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:150:5: ID ( attrs )? ( ( COLON_EQUALS rhsExprOrInitList -> ^( ALLOC ( attrs )? TYPE ID rhsExprOrInitList ) ) | ( COLON type ( EQUALS rhsExprOrInitList )? -> ^( ALLOC ( attrs )? type ID ( rhsExprOrInitList )* ) ) | ( COMMA ID )+ ( ( COLON_EQUALS ( PLUS )? rhsExprOrInitList ( COMMA rhsExprOrInitList )* ) -> ^( ALLOC ( attrs )? TYPE ^( LIST ( ID )+ ) ( PLUS )? ^( LIST ( rhsExprOrInitList )+ ) ) | ( COLON type ( EQUALS ( PLUS )? rhsExprOrInitList ( COMMA rhsExprOrInitList )* )? ) -> ^( ALLOC ( attrs )? type ^( LIST ( ID )+ ) ( PLUS )? ( ^( LIST ( rhsExprOrInitList )+ ) )? ) ) )
            {
            ID22=(Token)match(input,ID,FOLLOW_ID_in_toplevelSingleVarDecl683); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_ID.add(ID22);

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:150:8: ( attrs )?
            int alt9=2;
            int LA9_0 = input.LA(1);

            if ( (LA9_0==ATTR) ) {
                alt9=1;
            }
            switch (alt9) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:150:8: attrs
                    {
                    pushFollow(FOLLOW_attrs_in_toplevelSingleVarDecl685);
                    attrs23=attrs();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_attrs.add(attrs23.getTree());

                    }
                    break;

            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:150:15: ( ( COLON_EQUALS rhsExprOrInitList -> ^( ALLOC ( attrs )? TYPE ID rhsExprOrInitList ) ) | ( COLON type ( EQUALS rhsExprOrInitList )? -> ^( ALLOC ( attrs )? type ID ( rhsExprOrInitList )* ) ) | ( COMMA ID )+ ( ( COLON_EQUALS ( PLUS )? rhsExprOrInitList ( COMMA rhsExprOrInitList )* ) -> ^( ALLOC ( attrs )? TYPE ^( LIST ( ID )+ ) ( PLUS )? ^( LIST ( rhsExprOrInitList )+ ) ) | ( COLON type ( EQUALS ( PLUS )? rhsExprOrInitList ( COMMA rhsExprOrInitList )* )? ) -> ^( ALLOC ( attrs )? type ^( LIST ( ID )+ ) ( PLUS )? ( ^( LIST ( rhsExprOrInitList )+ ) )? ) ) )
            int alt18=3;
            switch ( input.LA(1) ) {
            case COLON_EQUALS:
                {
                alt18=1;
                }
                break;
            case COLON:
                {
                alt18=2;
                }
                break;
            case COMMA:
                {
                alt18=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 18, 0, input);

                throw nvae;
            }

            switch (alt18) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:151:9: ( COLON_EQUALS rhsExprOrInitList -> ^( ALLOC ( attrs )? TYPE ID rhsExprOrInitList ) )
                    {
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:151:9: ( COLON_EQUALS rhsExprOrInitList -> ^( ALLOC ( attrs )? TYPE ID rhsExprOrInitList ) )
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:151:11: COLON_EQUALS rhsExprOrInitList
                    {
                    COLON_EQUALS24=(Token)match(input,COLON_EQUALS,FOLLOW_COLON_EQUALS_in_toplevelSingleVarDecl700); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COLON_EQUALS.add(COLON_EQUALS24);

                    pushFollow(FOLLOW_rhsExprOrInitList_in_toplevelSingleVarDecl702);
                    rhsExprOrInitList25=rhsExprOrInitList();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_rhsExprOrInitList.add(rhsExprOrInitList25.getTree());


                    // AST REWRITE
                    // elements: rhsExprOrInitList, attrs, ID
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 151:50: -> ^( ALLOC ( attrs )? TYPE ID rhsExprOrInitList )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:151:53: ^( ALLOC ( attrs )? TYPE ID rhsExprOrInitList )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ALLOC, "ALLOC"), root_1);

                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:151:61: ( attrs )?
                        if ( stream_attrs.hasNext() ) {
                            adaptor.addChild(root_1, stream_attrs.nextTree());

                        }
                        stream_attrs.reset();
                        adaptor.addChild(root_1, (CommonTree)adaptor.create(TYPE, "TYPE"));
                        adaptor.addChild(root_1, stream_ID.nextNode());
                        adaptor.addChild(root_1, stream_rhsExprOrInitList.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }


                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:152:9: ( COLON type ( EQUALS rhsExprOrInitList )? -> ^( ALLOC ( attrs )? type ID ( rhsExprOrInitList )* ) )
                    {
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:152:9: ( COLON type ( EQUALS rhsExprOrInitList )? -> ^( ALLOC ( attrs )? type ID ( rhsExprOrInitList )* ) )
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:152:11: COLON type ( EQUALS rhsExprOrInitList )?
                    {
                    COLON26=(Token)match(input,COLON,FOLLOW_COLON_in_toplevelSingleVarDecl739); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COLON.add(COLON26);

                    pushFollow(FOLLOW_type_in_toplevelSingleVarDecl741);
                    type27=type();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_type.add(type27.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:152:22: ( EQUALS rhsExprOrInitList )?
                    int alt10=2;
                    int LA10_0 = input.LA(1);

                    if ( (LA10_0==EQUALS) ) {
                        alt10=1;
                    }
                    switch (alt10) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:152:23: EQUALS rhsExprOrInitList
                            {
                            EQUALS28=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_toplevelSingleVarDecl744); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_EQUALS.add(EQUALS28);

                            pushFollow(FOLLOW_rhsExprOrInitList_in_toplevelSingleVarDecl746);
                            rhsExprOrInitList29=rhsExprOrInitList();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_rhsExprOrInitList.add(rhsExprOrInitList29.getTree());

                            }
                            break;

                    }



                    // AST REWRITE
                    // elements: ID, rhsExprOrInitList, attrs, type
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 152:51: -> ^( ALLOC ( attrs )? type ID ( rhsExprOrInitList )* )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:152:54: ^( ALLOC ( attrs )? type ID ( rhsExprOrInitList )* )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ALLOC, "ALLOC"), root_1);

                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:152:62: ( attrs )?
                        if ( stream_attrs.hasNext() ) {
                            adaptor.addChild(root_1, stream_attrs.nextTree());

                        }
                        stream_attrs.reset();
                        adaptor.addChild(root_1, stream_type.nextTree());
                        adaptor.addChild(root_1, stream_ID.nextNode());
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:152:77: ( rhsExprOrInitList )*
                        while ( stream_rhsExprOrInitList.hasNext() ) {
                            adaptor.addChild(root_1, stream_rhsExprOrInitList.nextTree());

                        }
                        stream_rhsExprOrInitList.reset();

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }


                    }
                    break;
                case 3 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:153:9: ( COMMA ID )+ ( ( COLON_EQUALS ( PLUS )? rhsExprOrInitList ( COMMA rhsExprOrInitList )* ) -> ^( ALLOC ( attrs )? TYPE ^( LIST ( ID )+ ) ( PLUS )? ^( LIST ( rhsExprOrInitList )+ ) ) | ( COLON type ( EQUALS ( PLUS )? rhsExprOrInitList ( COMMA rhsExprOrInitList )* )? ) -> ^( ALLOC ( attrs )? type ^( LIST ( ID )+ ) ( PLUS )? ( ^( LIST ( rhsExprOrInitList )+ ) )? ) )
                    {
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:153:9: ( COMMA ID )+
                    int cnt11=0;
                    loop11:
                    do {
                        int alt11=2;
                        int LA11_0 = input.LA(1);

                        if ( (LA11_0==COMMA) ) {
                            alt11=1;
                        }


                        switch (alt11) {
                    	case 1 :
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:153:11: COMMA ID
                    	    {
                    	    COMMA30=(Token)match(input,COMMA,FOLLOW_COMMA_in_toplevelSingleVarDecl779); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA30);

                    	    ID31=(Token)match(input,ID,FOLLOW_ID_in_toplevelSingleVarDecl781); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_ID.add(ID31);


                    	    }
                    	    break;

                    	default :
                    	    if ( cnt11 >= 1 ) break loop11;
                    	    if (state.backtracking>0) {state.failed=true; return retval;}
                                EarlyExitException eee =
                                    new EarlyExitException(11, input);
                                throw eee;
                        }
                        cnt11++;
                    } while (true);

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:154:9: ( ( COLON_EQUALS ( PLUS )? rhsExprOrInitList ( COMMA rhsExprOrInitList )* ) -> ^( ALLOC ( attrs )? TYPE ^( LIST ( ID )+ ) ( PLUS )? ^( LIST ( rhsExprOrInitList )+ ) ) | ( COLON type ( EQUALS ( PLUS )? rhsExprOrInitList ( COMMA rhsExprOrInitList )* )? ) -> ^( ALLOC ( attrs )? type ^( LIST ( ID )+ ) ( PLUS )? ( ^( LIST ( rhsExprOrInitList )+ ) )? ) )
                    int alt17=2;
                    int LA17_0 = input.LA(1);

                    if ( (LA17_0==COLON_EQUALS) ) {
                        alt17=1;
                    }
                    else if ( (LA17_0==COLON) ) {
                        alt17=2;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 17, 0, input);

                        throw nvae;
                    }
                    switch (alt17) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:154:12: ( COLON_EQUALS ( PLUS )? rhsExprOrInitList ( COMMA rhsExprOrInitList )* )
                            {
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:154:12: ( COLON_EQUALS ( PLUS )? rhsExprOrInitList ( COMMA rhsExprOrInitList )* )
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:154:14: COLON_EQUALS ( PLUS )? rhsExprOrInitList ( COMMA rhsExprOrInitList )*
                            {
                            COLON_EQUALS32=(Token)match(input,COLON_EQUALS,FOLLOW_COLON_EQUALS_in_toplevelSingleVarDecl800); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_COLON_EQUALS.add(COLON_EQUALS32);

                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:154:27: ( PLUS )?
                            int alt12=2;
                            int LA12_0 = input.LA(1);

                            if ( (LA12_0==PLUS) ) {
                                alt12=1;
                            }
                            switch (alt12) {
                                case 1 :
                                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:154:27: PLUS
                                    {
                                    PLUS33=(Token)match(input,PLUS,FOLLOW_PLUS_in_toplevelSingleVarDecl802); if (state.failed) return retval; 
                                    if ( state.backtracking==0 ) stream_PLUS.add(PLUS33);


                                    }
                                    break;

                            }

                            pushFollow(FOLLOW_rhsExprOrInitList_in_toplevelSingleVarDecl805);
                            rhsExprOrInitList34=rhsExprOrInitList();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_rhsExprOrInitList.add(rhsExprOrInitList34.getTree());
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:154:51: ( COMMA rhsExprOrInitList )*
                            loop13:
                            do {
                                int alt13=2;
                                int LA13_0 = input.LA(1);

                                if ( (LA13_0==COMMA) ) {
                                    alt13=1;
                                }


                                switch (alt13) {
                            	case 1 :
                            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:154:52: COMMA rhsExprOrInitList
                            	    {
                            	    COMMA35=(Token)match(input,COMMA,FOLLOW_COMMA_in_toplevelSingleVarDecl808); if (state.failed) return retval; 
                            	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA35);

                            	    pushFollow(FOLLOW_rhsExprOrInitList_in_toplevelSingleVarDecl810);
                            	    rhsExprOrInitList36=rhsExprOrInitList();

                            	    state._fsp--;
                            	    if (state.failed) return retval;
                            	    if ( state.backtracking==0 ) stream_rhsExprOrInitList.add(rhsExprOrInitList36.getTree());

                            	    }
                            	    break;

                            	default :
                            	    break loop13;
                                }
                            } while (true);


                            }



                            // AST REWRITE
                            // elements: PLUS, ID, rhsExprOrInitList, attrs
                            // token labels: 
                            // rule labels: retval
                            // token list labels: 
                            // rule list labels: 
                            // wildcard labels: 
                            if ( state.backtracking==0 ) {
                            retval.tree = root_0;
                            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                            root_0 = (CommonTree)adaptor.nil();
                            // 155:15: -> ^( ALLOC ( attrs )? TYPE ^( LIST ( ID )+ ) ( PLUS )? ^( LIST ( rhsExprOrInitList )+ ) )
                            {
                                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:155:18: ^( ALLOC ( attrs )? TYPE ^( LIST ( ID )+ ) ( PLUS )? ^( LIST ( rhsExprOrInitList )+ ) )
                                {
                                CommonTree root_1 = (CommonTree)adaptor.nil();
                                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ALLOC, "ALLOC"), root_1);

                                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:155:26: ( attrs )?
                                if ( stream_attrs.hasNext() ) {
                                    adaptor.addChild(root_1, stream_attrs.nextTree());

                                }
                                stream_attrs.reset();
                                adaptor.addChild(root_1, (CommonTree)adaptor.create(TYPE, "TYPE"));
                                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:155:38: ^( LIST ( ID )+ )
                                {
                                CommonTree root_2 = (CommonTree)adaptor.nil();
                                root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(LIST, "LIST"), root_2);

                                if ( !(stream_ID.hasNext()) ) {
                                    throw new RewriteEarlyExitException();
                                }
                                while ( stream_ID.hasNext() ) {
                                    adaptor.addChild(root_2, stream_ID.nextNode());

                                }
                                stream_ID.reset();

                                adaptor.addChild(root_1, root_2);
                                }
                                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:155:50: ( PLUS )?
                                if ( stream_PLUS.hasNext() ) {
                                    adaptor.addChild(root_1, stream_PLUS.nextNode());

                                }
                                stream_PLUS.reset();
                                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:155:57: ^( LIST ( rhsExprOrInitList )+ )
                                {
                                CommonTree root_2 = (CommonTree)adaptor.nil();
                                root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(LIST, "LIST"), root_2);

                                if ( !(stream_rhsExprOrInitList.hasNext()) ) {
                                    throw new RewriteEarlyExitException();
                                }
                                while ( stream_rhsExprOrInitList.hasNext() ) {
                                    adaptor.addChild(root_2, stream_rhsExprOrInitList.nextTree());

                                }
                                stream_rhsExprOrInitList.reset();

                                adaptor.addChild(root_1, root_2);
                                }

                                adaptor.addChild(root_0, root_1);
                                }

                            }

                            retval.tree = root_0;}
                            }
                            break;
                        case 2 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:156:12: ( COLON type ( EQUALS ( PLUS )? rhsExprOrInitList ( COMMA rhsExprOrInitList )* )? )
                            {
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:156:12: ( COLON type ( EQUALS ( PLUS )? rhsExprOrInitList ( COMMA rhsExprOrInitList )* )? )
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:156:14: COLON type ( EQUALS ( PLUS )? rhsExprOrInitList ( COMMA rhsExprOrInitList )* )?
                            {
                            COLON37=(Token)match(input,COLON,FOLLOW_COLON_in_toplevelSingleVarDecl873); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_COLON.add(COLON37);

                            pushFollow(FOLLOW_type_in_toplevelSingleVarDecl875);
                            type38=type();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_type.add(type38.getTree());
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:156:25: ( EQUALS ( PLUS )? rhsExprOrInitList ( COMMA rhsExprOrInitList )* )?
                            int alt16=2;
                            int LA16_0 = input.LA(1);

                            if ( (LA16_0==EQUALS) ) {
                                alt16=1;
                            }
                            switch (alt16) {
                                case 1 :
                                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:156:26: EQUALS ( PLUS )? rhsExprOrInitList ( COMMA rhsExprOrInitList )*
                                    {
                                    EQUALS39=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_toplevelSingleVarDecl878); if (state.failed) return retval; 
                                    if ( state.backtracking==0 ) stream_EQUALS.add(EQUALS39);

                                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:156:33: ( PLUS )?
                                    int alt14=2;
                                    int LA14_0 = input.LA(1);

                                    if ( (LA14_0==PLUS) ) {
                                        alt14=1;
                                    }
                                    switch (alt14) {
                                        case 1 :
                                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:156:33: PLUS
                                            {
                                            PLUS40=(Token)match(input,PLUS,FOLLOW_PLUS_in_toplevelSingleVarDecl880); if (state.failed) return retval; 
                                            if ( state.backtracking==0 ) stream_PLUS.add(PLUS40);


                                            }
                                            break;

                                    }

                                    pushFollow(FOLLOW_rhsExprOrInitList_in_toplevelSingleVarDecl883);
                                    rhsExprOrInitList41=rhsExprOrInitList();

                                    state._fsp--;
                                    if (state.failed) return retval;
                                    if ( state.backtracking==0 ) stream_rhsExprOrInitList.add(rhsExprOrInitList41.getTree());
                                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:156:57: ( COMMA rhsExprOrInitList )*
                                    loop15:
                                    do {
                                        int alt15=2;
                                        int LA15_0 = input.LA(1);

                                        if ( (LA15_0==COMMA) ) {
                                            alt15=1;
                                        }


                                        switch (alt15) {
                                    	case 1 :
                                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:156:58: COMMA rhsExprOrInitList
                                    	    {
                                    	    COMMA42=(Token)match(input,COMMA,FOLLOW_COMMA_in_toplevelSingleVarDecl886); if (state.failed) return retval; 
                                    	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA42);

                                    	    pushFollow(FOLLOW_rhsExprOrInitList_in_toplevelSingleVarDecl888);
                                    	    rhsExprOrInitList43=rhsExprOrInitList();

                                    	    state._fsp--;
                                    	    if (state.failed) return retval;
                                    	    if ( state.backtracking==0 ) stream_rhsExprOrInitList.add(rhsExprOrInitList43.getTree());

                                    	    }
                                    	    break;

                                    	default :
                                    	    break loop15;
                                        }
                                    } while (true);


                                    }
                                    break;

                            }


                            }



                            // AST REWRITE
                            // elements: type, attrs, ID, rhsExprOrInitList, PLUS
                            // token labels: 
                            // rule labels: retval
                            // token list labels: 
                            // rule list labels: 
                            // wildcard labels: 
                            if ( state.backtracking==0 ) {
                            retval.tree = root_0;
                            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                            root_0 = (CommonTree)adaptor.nil();
                            // 157:15: -> ^( ALLOC ( attrs )? type ^( LIST ( ID )+ ) ( PLUS )? ( ^( LIST ( rhsExprOrInitList )+ ) )? )
                            {
                                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:157:18: ^( ALLOC ( attrs )? type ^( LIST ( ID )+ ) ( PLUS )? ( ^( LIST ( rhsExprOrInitList )+ ) )? )
                                {
                                CommonTree root_1 = (CommonTree)adaptor.nil();
                                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ALLOC, "ALLOC"), root_1);

                                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:157:26: ( attrs )?
                                if ( stream_attrs.hasNext() ) {
                                    adaptor.addChild(root_1, stream_attrs.nextTree());

                                }
                                stream_attrs.reset();
                                adaptor.addChild(root_1, stream_type.nextTree());
                                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:157:38: ^( LIST ( ID )+ )
                                {
                                CommonTree root_2 = (CommonTree)adaptor.nil();
                                root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(LIST, "LIST"), root_2);

                                if ( !(stream_ID.hasNext()) ) {
                                    throw new RewriteEarlyExitException();
                                }
                                while ( stream_ID.hasNext() ) {
                                    adaptor.addChild(root_2, stream_ID.nextNode());

                                }
                                stream_ID.reset();

                                adaptor.addChild(root_1, root_2);
                                }
                                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:157:50: ( PLUS )?
                                if ( stream_PLUS.hasNext() ) {
                                    adaptor.addChild(root_1, stream_PLUS.nextNode());

                                }
                                stream_PLUS.reset();
                                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:157:56: ( ^( LIST ( rhsExprOrInitList )+ ) )?
                                if ( stream_rhsExprOrInitList.hasNext() ) {
                                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:157:56: ^( LIST ( rhsExprOrInitList )+ )
                                    {
                                    CommonTree root_2 = (CommonTree)adaptor.nil();
                                    root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(LIST, "LIST"), root_2);

                                    if ( !(stream_rhsExprOrInitList.hasNext()) ) {
                                        throw new RewriteEarlyExitException();
                                    }
                                    while ( stream_rhsExprOrInitList.hasNext() ) {
                                        adaptor.addChild(root_2, stream_rhsExprOrInitList.nextTree());

                                    }
                                    stream_rhsExprOrInitList.reset();

                                    adaptor.addChild(root_1, root_2);
                                    }

                                }
                                stream_rhsExprOrInitList.reset();

                                adaptor.addChild(root_0, root_1);
                                }

                            }

                            retval.tree = root_0;}
                            }
                            break;

                    }


                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "toplevelSingleVarDecl"

    public static class toplevelTupleVarDecl_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "toplevelTupleVarDecl"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:162:1: toplevelTupleVarDecl : idTuple ( ( COLON_EQUALS rhsExprOrInitList -> ^( ALLOC_TUPLE TYPE idTuple rhsExprOrInitList ) ) | ( COLON type ( EQUALS rhsExprOrInitList )? -> ^( ALLOC_TUPLE type idTuple ( rhsExprOrInitList )* ) ) ) ;
    public final EulangParser.toplevelTupleVarDecl_return toplevelTupleVarDecl() throws RecognitionException {
        EulangParser.toplevelTupleVarDecl_return retval = new EulangParser.toplevelTupleVarDecl_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COLON_EQUALS45=null;
        Token COLON47=null;
        Token EQUALS49=null;
        EulangParser.idTuple_return idTuple44 = null;

        EulangParser.rhsExprOrInitList_return rhsExprOrInitList46 = null;

        EulangParser.type_return type48 = null;

        EulangParser.rhsExprOrInitList_return rhsExprOrInitList50 = null;


        CommonTree COLON_EQUALS45_tree=null;
        CommonTree COLON47_tree=null;
        CommonTree EQUALS49_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleTokenStream stream_EQUALS=new RewriteRuleTokenStream(adaptor,"token EQUALS");
        RewriteRuleTokenStream stream_COLON_EQUALS=new RewriteRuleTokenStream(adaptor,"token COLON_EQUALS");
        RewriteRuleSubtreeStream stream_idTuple=new RewriteRuleSubtreeStream(adaptor,"rule idTuple");
        RewriteRuleSubtreeStream stream_rhsExprOrInitList=new RewriteRuleSubtreeStream(adaptor,"rule rhsExprOrInitList");
        RewriteRuleSubtreeStream stream_type=new RewriteRuleSubtreeStream(adaptor,"rule type");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:162:21: ( idTuple ( ( COLON_EQUALS rhsExprOrInitList -> ^( ALLOC_TUPLE TYPE idTuple rhsExprOrInitList ) ) | ( COLON type ( EQUALS rhsExprOrInitList )? -> ^( ALLOC_TUPLE type idTuple ( rhsExprOrInitList )* ) ) ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:163:5: idTuple ( ( COLON_EQUALS rhsExprOrInitList -> ^( ALLOC_TUPLE TYPE idTuple rhsExprOrInitList ) ) | ( COLON type ( EQUALS rhsExprOrInitList )? -> ^( ALLOC_TUPLE type idTuple ( rhsExprOrInitList )* ) ) )
            {
            pushFollow(FOLLOW_idTuple_in_toplevelTupleVarDecl982);
            idTuple44=idTuple();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_idTuple.add(idTuple44.getTree());
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:164:7: ( ( COLON_EQUALS rhsExprOrInitList -> ^( ALLOC_TUPLE TYPE idTuple rhsExprOrInitList ) ) | ( COLON type ( EQUALS rhsExprOrInitList )? -> ^( ALLOC_TUPLE type idTuple ( rhsExprOrInitList )* ) ) )
            int alt20=2;
            int LA20_0 = input.LA(1);

            if ( (LA20_0==COLON_EQUALS) ) {
                alt20=1;
            }
            else if ( (LA20_0==COLON) ) {
                alt20=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 20, 0, input);

                throw nvae;
            }
            switch (alt20) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:165:9: ( COLON_EQUALS rhsExprOrInitList -> ^( ALLOC_TUPLE TYPE idTuple rhsExprOrInitList ) )
                    {
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:165:9: ( COLON_EQUALS rhsExprOrInitList -> ^( ALLOC_TUPLE TYPE idTuple rhsExprOrInitList ) )
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:165:11: COLON_EQUALS rhsExprOrInitList
                    {
                    COLON_EQUALS45=(Token)match(input,COLON_EQUALS,FOLLOW_COLON_EQUALS_in_toplevelTupleVarDecl1004); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COLON_EQUALS.add(COLON_EQUALS45);

                    pushFollow(FOLLOW_rhsExprOrInitList_in_toplevelTupleVarDecl1006);
                    rhsExprOrInitList46=rhsExprOrInitList();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_rhsExprOrInitList.add(rhsExprOrInitList46.getTree());


                    // AST REWRITE
                    // elements: rhsExprOrInitList, idTuple
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 165:50: -> ^( ALLOC_TUPLE TYPE idTuple rhsExprOrInitList )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:165:53: ^( ALLOC_TUPLE TYPE idTuple rhsExprOrInitList )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ALLOC_TUPLE, "ALLOC_TUPLE"), root_1);

                        adaptor.addChild(root_1, (CommonTree)adaptor.create(TYPE, "TYPE"));
                        adaptor.addChild(root_1, stream_idTuple.nextTree());
                        adaptor.addChild(root_1, stream_rhsExprOrInitList.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }


                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:166:9: ( COLON type ( EQUALS rhsExprOrInitList )? -> ^( ALLOC_TUPLE type idTuple ( rhsExprOrInitList )* ) )
                    {
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:166:9: ( COLON type ( EQUALS rhsExprOrInitList )? -> ^( ALLOC_TUPLE type idTuple ( rhsExprOrInitList )* ) )
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:166:11: COLON type ( EQUALS rhsExprOrInitList )?
                    {
                    COLON47=(Token)match(input,COLON,FOLLOW_COLON_in_toplevelTupleVarDecl1040); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COLON.add(COLON47);

                    pushFollow(FOLLOW_type_in_toplevelTupleVarDecl1042);
                    type48=type();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_type.add(type48.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:166:22: ( EQUALS rhsExprOrInitList )?
                    int alt19=2;
                    int LA19_0 = input.LA(1);

                    if ( (LA19_0==EQUALS) ) {
                        alt19=1;
                    }
                    switch (alt19) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:166:23: EQUALS rhsExprOrInitList
                            {
                            EQUALS49=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_toplevelTupleVarDecl1045); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_EQUALS.add(EQUALS49);

                            pushFollow(FOLLOW_rhsExprOrInitList_in_toplevelTupleVarDecl1047);
                            rhsExprOrInitList50=rhsExprOrInitList();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_rhsExprOrInitList.add(rhsExprOrInitList50.getTree());

                            }
                            break;

                    }



                    // AST REWRITE
                    // elements: type, rhsExprOrInitList, idTuple
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 166:51: -> ^( ALLOC_TUPLE type idTuple ( rhsExprOrInitList )* )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:166:54: ^( ALLOC_TUPLE type idTuple ( rhsExprOrInitList )* )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ALLOC_TUPLE, "ALLOC_TUPLE"), root_1);

                        adaptor.addChild(root_1, stream_type.nextTree());
                        adaptor.addChild(root_1, stream_idTuple.nextTree());
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:166:81: ( rhsExprOrInitList )*
                        while ( stream_rhsExprOrInitList.hasNext() ) {
                            adaptor.addChild(root_1, stream_rhsExprOrInitList.nextTree());

                        }
                        stream_rhsExprOrInitList.reset();

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }


                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "toplevelTupleVarDecl"

    public static class rhsExprOrInitList_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "rhsExprOrInitList"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:170:1: rhsExprOrInitList : ( rhsExpr | initList );
    public final EulangParser.rhsExprOrInitList_return rhsExprOrInitList() throws RecognitionException {
        EulangParser.rhsExprOrInitList_return retval = new EulangParser.rhsExprOrInitList_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.rhsExpr_return rhsExpr51 = null;

        EulangParser.initList_return initList52 = null;



        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:170:19: ( rhsExpr | initList )
            int alt21=2;
            int LA21_0 = input.LA(1);

            if ( (LA21_0==CODE||(LA21_0>=SIZEOF && LA21_0<=TYPEOF)||LA21_0==ID||LA21_0==COLON||LA21_0==LPAREN||LA21_0==NIL||LA21_0==IF||LA21_0==NOT||(LA21_0>=TILDE && LA21_0<=AMP)||LA21_0==MINUS||(LA21_0>=PLUSPLUS && LA21_0<=STRING_LITERAL)||(LA21_0>=FALSE && LA21_0<=COLONS)) ) {
                alt21=1;
            }
            else if ( (LA21_0==LBRACKET) ) {
                alt21=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 21, 0, input);

                throw nvae;
            }
            switch (alt21) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:170:21: rhsExpr
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_rhsExpr_in_rhsExprOrInitList1090);
                    rhsExpr51=rhsExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, rhsExpr51.getTree());

                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:170:31: initList
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_initList_in_rhsExprOrInitList1094);
                    initList52=initList();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, initList52.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "rhsExprOrInitList"

    public static class scopeExtension_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "scopeExtension"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:172:1: scopeExtension : namespaceRef PLUS_EQ xscopeNoAlloc -> ^( EXTENDSCOPE namespaceRef xscopeNoAlloc ) ;
    public final EulangParser.scopeExtension_return scopeExtension() throws RecognitionException {
        EulangParser.scopeExtension_return retval = new EulangParser.scopeExtension_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token PLUS_EQ54=null;
        EulangParser.namespaceRef_return namespaceRef53 = null;

        EulangParser.xscopeNoAlloc_return xscopeNoAlloc55 = null;


        CommonTree PLUS_EQ54_tree=null;
        RewriteRuleTokenStream stream_PLUS_EQ=new RewriteRuleTokenStream(adaptor,"token PLUS_EQ");
        RewriteRuleSubtreeStream stream_xscopeNoAlloc=new RewriteRuleSubtreeStream(adaptor,"rule xscopeNoAlloc");
        RewriteRuleSubtreeStream stream_namespaceRef=new RewriteRuleSubtreeStream(adaptor,"rule namespaceRef");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:172:16: ( namespaceRef PLUS_EQ xscopeNoAlloc -> ^( EXTENDSCOPE namespaceRef xscopeNoAlloc ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:172:18: namespaceRef PLUS_EQ xscopeNoAlloc
            {
            pushFollow(FOLLOW_namespaceRef_in_scopeExtension1103);
            namespaceRef53=namespaceRef();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_namespaceRef.add(namespaceRef53.getTree());
            PLUS_EQ54=(Token)match(input,PLUS_EQ,FOLLOW_PLUS_EQ_in_scopeExtension1105); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_PLUS_EQ.add(PLUS_EQ54);

            pushFollow(FOLLOW_xscopeNoAlloc_in_scopeExtension1107);
            xscopeNoAlloc55=xscopeNoAlloc();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_xscopeNoAlloc.add(xscopeNoAlloc55.getTree());


            // AST REWRITE
            // elements: xscopeNoAlloc, namespaceRef
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 172:53: -> ^( EXTENDSCOPE namespaceRef xscopeNoAlloc )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:172:56: ^( EXTENDSCOPE namespaceRef xscopeNoAlloc )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(EXTENDSCOPE, "EXTENDSCOPE"), root_1);

                adaptor.addChild(root_1, stream_namespaceRef.nextTree());
                adaptor.addChild(root_1, stream_xscopeNoAlloc.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "scopeExtension"

    public static class defineStmt_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "defineStmt"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:174:1: defineStmt : ( ( ID EQUALS LBRACKET )=> ID EQUALS LBRACKET idlistOrEmpty RBRACKET toplevelvalue SEMI -> ^( DEFINE ID idlistOrEmpty toplevelvalue ) | ( ID EQUALS_COLON )=> ID EQUALS_COLON type SEMI -> ^( DEFINE ID type ) | ( ID EQUALS )=> ID EQUALS toplevelvalue SEMI -> ^( DEFINE ID toplevelvalue ) );
    public final EulangParser.defineStmt_return defineStmt() throws RecognitionException {
        EulangParser.defineStmt_return retval = new EulangParser.defineStmt_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ID56=null;
        Token EQUALS57=null;
        Token LBRACKET58=null;
        Token RBRACKET60=null;
        Token SEMI62=null;
        Token ID63=null;
        Token EQUALS_COLON64=null;
        Token SEMI66=null;
        Token ID67=null;
        Token EQUALS68=null;
        Token SEMI70=null;
        EulangParser.idlistOrEmpty_return idlistOrEmpty59 = null;

        EulangParser.toplevelvalue_return toplevelvalue61 = null;

        EulangParser.type_return type65 = null;

        EulangParser.toplevelvalue_return toplevelvalue69 = null;


        CommonTree ID56_tree=null;
        CommonTree EQUALS57_tree=null;
        CommonTree LBRACKET58_tree=null;
        CommonTree RBRACKET60_tree=null;
        CommonTree SEMI62_tree=null;
        CommonTree ID63_tree=null;
        CommonTree EQUALS_COLON64_tree=null;
        CommonTree SEMI66_tree=null;
        CommonTree ID67_tree=null;
        CommonTree EQUALS68_tree=null;
        CommonTree SEMI70_tree=null;
        RewriteRuleTokenStream stream_LBRACKET=new RewriteRuleTokenStream(adaptor,"token LBRACKET");
        RewriteRuleTokenStream stream_EQUALS=new RewriteRuleTokenStream(adaptor,"token EQUALS");
        RewriteRuleTokenStream stream_RBRACKET=new RewriteRuleTokenStream(adaptor,"token RBRACKET");
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
        RewriteRuleTokenStream stream_SEMI=new RewriteRuleTokenStream(adaptor,"token SEMI");
        RewriteRuleTokenStream stream_EQUALS_COLON=new RewriteRuleTokenStream(adaptor,"token EQUALS_COLON");
        RewriteRuleSubtreeStream stream_toplevelvalue=new RewriteRuleSubtreeStream(adaptor,"rule toplevelvalue");
        RewriteRuleSubtreeStream stream_type=new RewriteRuleSubtreeStream(adaptor,"rule type");
        RewriteRuleSubtreeStream stream_idlistOrEmpty=new RewriteRuleSubtreeStream(adaptor,"rule idlistOrEmpty");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:174:12: ( ( ID EQUALS LBRACKET )=> ID EQUALS LBRACKET idlistOrEmpty RBRACKET toplevelvalue SEMI -> ^( DEFINE ID idlistOrEmpty toplevelvalue ) | ( ID EQUALS_COLON )=> ID EQUALS_COLON type SEMI -> ^( DEFINE ID type ) | ( ID EQUALS )=> ID EQUALS toplevelvalue SEMI -> ^( DEFINE ID toplevelvalue ) )
            int alt22=3;
            alt22 = dfa22.predict(input);
            switch (alt22) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:174:14: ( ID EQUALS LBRACKET )=> ID EQUALS LBRACKET idlistOrEmpty RBRACKET toplevelvalue SEMI
                    {
                    ID56=(Token)match(input,ID,FOLLOW_ID_in_defineStmt1136); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ID.add(ID56);

                    EQUALS57=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_defineStmt1138); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_EQUALS.add(EQUALS57);

                    LBRACKET58=(Token)match(input,LBRACKET,FOLLOW_LBRACKET_in_defineStmt1140); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LBRACKET.add(LBRACKET58);

                    pushFollow(FOLLOW_idlistOrEmpty_in_defineStmt1142);
                    idlistOrEmpty59=idlistOrEmpty();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_idlistOrEmpty.add(idlistOrEmpty59.getTree());
                    RBRACKET60=(Token)match(input,RBRACKET,FOLLOW_RBRACKET_in_defineStmt1144); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RBRACKET.add(RBRACKET60);

                    pushFollow(FOLLOW_toplevelvalue_in_defineStmt1147);
                    toplevelvalue61=toplevelvalue();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_toplevelvalue.add(toplevelvalue61.getTree());
                    SEMI62=(Token)match(input,SEMI,FOLLOW_SEMI_in_defineStmt1153); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_SEMI.add(SEMI62);



                    // AST REWRITE
                    // elements: idlistOrEmpty, toplevelvalue, ID
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 174:105: -> ^( DEFINE ID idlistOrEmpty toplevelvalue )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:174:108: ^( DEFINE ID idlistOrEmpty toplevelvalue )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(DEFINE, "DEFINE"), root_1);

                        adaptor.addChild(root_1, stream_ID.nextNode());
                        adaptor.addChild(root_1, stream_idlistOrEmpty.nextTree());
                        adaptor.addChild(root_1, stream_toplevelvalue.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:175:7: ( ID EQUALS_COLON )=> ID EQUALS_COLON type SEMI
                    {
                    ID63=(Token)match(input,ID,FOLLOW_ID_in_defineStmt1183); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ID.add(ID63);

                    EQUALS_COLON64=(Token)match(input,EQUALS_COLON,FOLLOW_EQUALS_COLON_in_defineStmt1185); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_EQUALS_COLON.add(EQUALS_COLON64);

                    pushFollow(FOLLOW_type_in_defineStmt1187);
                    type65=type();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_type.add(type65.getTree());
                    SEMI66=(Token)match(input,SEMI,FOLLOW_SEMI_in_defineStmt1193); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_SEMI.add(SEMI66);



                    // AST REWRITE
                    // elements: ID, type
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 175:59: -> ^( DEFINE ID type )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:175:62: ^( DEFINE ID type )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(DEFINE, "DEFINE"), root_1);

                        adaptor.addChild(root_1, stream_ID.nextNode());
                        adaptor.addChild(root_1, stream_type.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 3 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:176:7: ( ID EQUALS )=> ID EQUALS toplevelvalue SEMI
                    {
                    ID67=(Token)match(input,ID,FOLLOW_ID_in_defineStmt1220); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ID.add(ID67);

                    EQUALS68=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_defineStmt1222); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_EQUALS.add(EQUALS68);

                    pushFollow(FOLLOW_toplevelvalue_in_defineStmt1224);
                    toplevelvalue69=toplevelvalue();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_toplevelvalue.add(toplevelvalue69.getTree());
                    SEMI70=(Token)match(input,SEMI,FOLLOW_SEMI_in_defineStmt1230); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_SEMI.add(SEMI70);



                    // AST REWRITE
                    // elements: ID, toplevelvalue
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 176:56: -> ^( DEFINE ID toplevelvalue )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:176:59: ^( DEFINE ID toplevelvalue )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(DEFINE, "DEFINE"), root_1);

                        adaptor.addChild(root_1, stream_ID.nextNode());
                        adaptor.addChild(root_1, stream_toplevelvalue.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "defineStmt"

    public static class toplevelvalue_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "toplevelvalue"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:179:1: toplevelvalue : ( ( LBRACE )=> xscope | namespaceRef PLUS data -> ^( ADDSCOPE namespaceRef data ) | namespaceRef PLUS xscope -> ^( ADDSCOPE namespaceRef xscope ) | selector | rhsExpr | data );
    public final EulangParser.toplevelvalue_return toplevelvalue() throws RecognitionException {
        EulangParser.toplevelvalue_return retval = new EulangParser.toplevelvalue_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token PLUS73=null;
        Token PLUS76=null;
        EulangParser.xscope_return xscope71 = null;

        EulangParser.namespaceRef_return namespaceRef72 = null;

        EulangParser.data_return data74 = null;

        EulangParser.namespaceRef_return namespaceRef75 = null;

        EulangParser.xscope_return xscope77 = null;

        EulangParser.selector_return selector78 = null;

        EulangParser.rhsExpr_return rhsExpr79 = null;

        EulangParser.data_return data80 = null;


        CommonTree PLUS73_tree=null;
        CommonTree PLUS76_tree=null;
        RewriteRuleTokenStream stream_PLUS=new RewriteRuleTokenStream(adaptor,"token PLUS");
        RewriteRuleSubtreeStream stream_xscope=new RewriteRuleSubtreeStream(adaptor,"rule xscope");
        RewriteRuleSubtreeStream stream_data=new RewriteRuleSubtreeStream(adaptor,"rule data");
        RewriteRuleSubtreeStream stream_namespaceRef=new RewriteRuleSubtreeStream(adaptor,"rule namespaceRef");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:179:15: ( ( LBRACE )=> xscope | namespaceRef PLUS data -> ^( ADDSCOPE namespaceRef data ) | namespaceRef PLUS xscope -> ^( ADDSCOPE namespaceRef xscope ) | selector | rhsExpr | data )
            int alt23=6;
            alt23 = dfa23.predict(input);
            switch (alt23) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:179:17: ( LBRACE )=> xscope
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_xscope_in_toplevelvalue1258);
                    xscope71=xscope();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, xscope71.getTree());

                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:180:7: namespaceRef PLUS data
                    {
                    pushFollow(FOLLOW_namespaceRef_in_toplevelvalue1266);
                    namespaceRef72=namespaceRef();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_namespaceRef.add(namespaceRef72.getTree());
                    PLUS73=(Token)match(input,PLUS,FOLLOW_PLUS_in_toplevelvalue1268); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_PLUS.add(PLUS73);

                    pushFollow(FOLLOW_data_in_toplevelvalue1270);
                    data74=data();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_data.add(data74.getTree());


                    // AST REWRITE
                    // elements: data, namespaceRef
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 180:30: -> ^( ADDSCOPE namespaceRef data )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:180:33: ^( ADDSCOPE namespaceRef data )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ADDSCOPE, "ADDSCOPE"), root_1);

                        adaptor.addChild(root_1, stream_namespaceRef.nextTree());
                        adaptor.addChild(root_1, stream_data.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 3 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:181:7: namespaceRef PLUS xscope
                    {
                    pushFollow(FOLLOW_namespaceRef_in_toplevelvalue1288);
                    namespaceRef75=namespaceRef();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_namespaceRef.add(namespaceRef75.getTree());
                    PLUS76=(Token)match(input,PLUS,FOLLOW_PLUS_in_toplevelvalue1290); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_PLUS.add(PLUS76);

                    pushFollow(FOLLOW_xscope_in_toplevelvalue1292);
                    xscope77=xscope();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_xscope.add(xscope77.getTree());


                    // AST REWRITE
                    // elements: namespaceRef, xscope
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 181:32: -> ^( ADDSCOPE namespaceRef xscope )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:181:35: ^( ADDSCOPE namespaceRef xscope )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ADDSCOPE, "ADDSCOPE"), root_1);

                        adaptor.addChild(root_1, stream_namespaceRef.nextTree());
                        adaptor.addChild(root_1, stream_xscope.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 4 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:182:7: selector
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_selector_in_toplevelvalue1310);
                    selector78=selector();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, selector78.getTree());

                    }
                    break;
                case 5 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:183:7: rhsExpr
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_rhsExpr_in_toplevelvalue1318);
                    rhsExpr79=rhsExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, rhsExpr79.getTree());

                    }
                    break;
                case 6 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:184:7: data
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_data_in_toplevelvalue1326);
                    data80=data();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, data80.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "toplevelvalue"

    public static class selector_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "selector"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:188:1: selector : LBRACKET selectors RBRACKET -> ^( LIST ( selectors )* ) ;
    public final EulangParser.selector_return selector() throws RecognitionException {
        EulangParser.selector_return retval = new EulangParser.selector_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LBRACKET81=null;
        Token RBRACKET83=null;
        EulangParser.selectors_return selectors82 = null;


        CommonTree LBRACKET81_tree=null;
        CommonTree RBRACKET83_tree=null;
        RewriteRuleTokenStream stream_LBRACKET=new RewriteRuleTokenStream(adaptor,"token LBRACKET");
        RewriteRuleTokenStream stream_RBRACKET=new RewriteRuleTokenStream(adaptor,"token RBRACKET");
        RewriteRuleSubtreeStream stream_selectors=new RewriteRuleSubtreeStream(adaptor,"rule selectors");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:188:9: ( LBRACKET selectors RBRACKET -> ^( LIST ( selectors )* ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:188:11: LBRACKET selectors RBRACKET
            {
            LBRACKET81=(Token)match(input,LBRACKET,FOLLOW_LBRACKET_in_selector1339); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LBRACKET.add(LBRACKET81);

            pushFollow(FOLLOW_selectors_in_selector1341);
            selectors82=selectors();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_selectors.add(selectors82.getTree());
            RBRACKET83=(Token)match(input,RBRACKET,FOLLOW_RBRACKET_in_selector1343); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RBRACKET.add(RBRACKET83);



            // AST REWRITE
            // elements: selectors
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 188:42: -> ^( LIST ( selectors )* )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:188:45: ^( LIST ( selectors )* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(LIST, "LIST"), root_1);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:188:52: ( selectors )*
                while ( stream_selectors.hasNext() ) {
                    adaptor.addChild(root_1, stream_selectors.nextTree());

                }
                stream_selectors.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "selector"

    public static class selectors_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "selectors"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:191:1: selectors : ( selectoritem ( COMMA selectoritem )* ( COMMA )? )? -> ( selectoritem )* ;
    public final EulangParser.selectors_return selectors() throws RecognitionException {
        EulangParser.selectors_return retval = new EulangParser.selectors_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COMMA85=null;
        Token COMMA87=null;
        EulangParser.selectoritem_return selectoritem84 = null;

        EulangParser.selectoritem_return selectoritem86 = null;


        CommonTree COMMA85_tree=null;
        CommonTree COMMA87_tree=null;
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleSubtreeStream stream_selectoritem=new RewriteRuleSubtreeStream(adaptor,"rule selectoritem");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:191:10: ( ( selectoritem ( COMMA selectoritem )* ( COMMA )? )? -> ( selectoritem )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:191:12: ( selectoritem ( COMMA selectoritem )* ( COMMA )? )?
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:191:12: ( selectoritem ( COMMA selectoritem )* ( COMMA )? )?
            int alt26=2;
            int LA26_0 = input.LA(1);

            if ( (LA26_0==CODE||(LA26_0>=SIZEOF && LA26_0<=TYPEOF)||LA26_0==ID||LA26_0==COLON||LA26_0==LPAREN||LA26_0==NIL||LA26_0==IF||LA26_0==NOT||(LA26_0>=TILDE && LA26_0<=AMP)||LA26_0==MINUS||(LA26_0>=PLUSPLUS && LA26_0<=STRING_LITERAL)||(LA26_0>=FALSE && LA26_0<=COLONS)) ) {
                alt26=1;
            }
            switch (alt26) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:191:13: selectoritem ( COMMA selectoritem )* ( COMMA )?
                    {
                    pushFollow(FOLLOW_selectoritem_in_selectors1369);
                    selectoritem84=selectoritem();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_selectoritem.add(selectoritem84.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:191:26: ( COMMA selectoritem )*
                    loop24:
                    do {
                        int alt24=2;
                        int LA24_0 = input.LA(1);

                        if ( (LA24_0==COMMA) ) {
                            int LA24_1 = input.LA(2);

                            if ( (LA24_1==CODE||(LA24_1>=SIZEOF && LA24_1<=TYPEOF)||LA24_1==ID||LA24_1==COLON||LA24_1==LPAREN||LA24_1==NIL||LA24_1==IF||LA24_1==NOT||(LA24_1>=TILDE && LA24_1<=AMP)||LA24_1==MINUS||(LA24_1>=PLUSPLUS && LA24_1<=STRING_LITERAL)||(LA24_1>=FALSE && LA24_1<=COLONS)) ) {
                                alt24=1;
                            }


                        }


                        switch (alt24) {
                    	case 1 :
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:191:28: COMMA selectoritem
                    	    {
                    	    COMMA85=(Token)match(input,COMMA,FOLLOW_COMMA_in_selectors1373); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA85);

                    	    pushFollow(FOLLOW_selectoritem_in_selectors1375);
                    	    selectoritem86=selectoritem();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_selectoritem.add(selectoritem86.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop24;
                        }
                    } while (true);

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:191:50: ( COMMA )?
                    int alt25=2;
                    int LA25_0 = input.LA(1);

                    if ( (LA25_0==COMMA) ) {
                        alt25=1;
                    }
                    switch (alt25) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:191:50: COMMA
                            {
                            COMMA87=(Token)match(input,COMMA,FOLLOW_COMMA_in_selectors1380); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_COMMA.add(COMMA87);


                            }
                            break;

                    }


                    }
                    break;

            }



            // AST REWRITE
            // elements: selectoritem
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 191:62: -> ( selectoritem )*
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:191:65: ( selectoritem )*
                while ( stream_selectoritem.hasNext() ) {
                    adaptor.addChild(root_0, stream_selectoritem.nextTree());

                }
                stream_selectoritem.reset();

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "selectors"

    public static class selectoritem_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "selectoritem"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:194:1: selectoritem : rhsExpr ;
    public final EulangParser.selectoritem_return selectoritem() throws RecognitionException {
        EulangParser.selectoritem_return retval = new EulangParser.selectoritem_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.rhsExpr_return rhsExpr88 = null;



        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:194:14: ( rhsExpr )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:194:17: rhsExpr
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_rhsExpr_in_selectoritem1411);
            rhsExpr88=rhsExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, rhsExpr88.getTree());

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "selectoritem"

    public static class xscope_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "xscope"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:198:1: xscope : LBRACE toplevelstmts RBRACE -> ^( SCOPE ( toplevelstmts )* ) ;
    public final EulangParser.xscope_return xscope() throws RecognitionException {
        EulangParser.xscope_return retval = new EulangParser.xscope_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LBRACE89=null;
        Token RBRACE91=null;
        EulangParser.toplevelstmts_return toplevelstmts90 = null;


        CommonTree LBRACE89_tree=null;
        CommonTree RBRACE91_tree=null;
        RewriteRuleTokenStream stream_RBRACE=new RewriteRuleTokenStream(adaptor,"token RBRACE");
        RewriteRuleTokenStream stream_LBRACE=new RewriteRuleTokenStream(adaptor,"token LBRACE");
        RewriteRuleSubtreeStream stream_toplevelstmts=new RewriteRuleSubtreeStream(adaptor,"rule toplevelstmts");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:198:8: ( LBRACE toplevelstmts RBRACE -> ^( SCOPE ( toplevelstmts )* ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:198:10: LBRACE toplevelstmts RBRACE
            {
            LBRACE89=(Token)match(input,LBRACE,FOLLOW_LBRACE_in_xscope1422); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LBRACE.add(LBRACE89);

            pushFollow(FOLLOW_toplevelstmts_in_xscope1424);
            toplevelstmts90=toplevelstmts();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_toplevelstmts.add(toplevelstmts90.getTree());
            RBRACE91=(Token)match(input,RBRACE,FOLLOW_RBRACE_in_xscope1426); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RBRACE.add(RBRACE91);



            // AST REWRITE
            // elements: toplevelstmts
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 198:41: -> ^( SCOPE ( toplevelstmts )* )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:198:44: ^( SCOPE ( toplevelstmts )* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(SCOPE, "SCOPE"), root_1);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:198:52: ( toplevelstmts )*
                while ( stream_toplevelstmts.hasNext() ) {
                    adaptor.addChild(root_1, stream_toplevelstmts.nextTree());

                }
                stream_toplevelstmts.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "xscope"

    public static class xscopeNoAlloc_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "xscopeNoAlloc"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:200:1: xscopeNoAlloc : LBRACE toplevelstmtsNoAlloc RBRACE -> ^( SCOPE ( toplevelstmtsNoAlloc )* ) ;
    public final EulangParser.xscopeNoAlloc_return xscopeNoAlloc() throws RecognitionException {
        EulangParser.xscopeNoAlloc_return retval = new EulangParser.xscopeNoAlloc_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LBRACE92=null;
        Token RBRACE94=null;
        EulangParser.toplevelstmtsNoAlloc_return toplevelstmtsNoAlloc93 = null;


        CommonTree LBRACE92_tree=null;
        CommonTree RBRACE94_tree=null;
        RewriteRuleTokenStream stream_RBRACE=new RewriteRuleTokenStream(adaptor,"token RBRACE");
        RewriteRuleTokenStream stream_LBRACE=new RewriteRuleTokenStream(adaptor,"token LBRACE");
        RewriteRuleSubtreeStream stream_toplevelstmtsNoAlloc=new RewriteRuleSubtreeStream(adaptor,"rule toplevelstmtsNoAlloc");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:200:15: ( LBRACE toplevelstmtsNoAlloc RBRACE -> ^( SCOPE ( toplevelstmtsNoAlloc )* ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:200:17: LBRACE toplevelstmtsNoAlloc RBRACE
            {
            LBRACE92=(Token)match(input,LBRACE,FOLLOW_LBRACE_in_xscopeNoAlloc1451); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LBRACE.add(LBRACE92);

            pushFollow(FOLLOW_toplevelstmtsNoAlloc_in_xscopeNoAlloc1453);
            toplevelstmtsNoAlloc93=toplevelstmtsNoAlloc();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_toplevelstmtsNoAlloc.add(toplevelstmtsNoAlloc93.getTree());
            RBRACE94=(Token)match(input,RBRACE,FOLLOW_RBRACE_in_xscopeNoAlloc1455); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RBRACE.add(RBRACE94);



            // AST REWRITE
            // elements: toplevelstmtsNoAlloc
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 200:55: -> ^( SCOPE ( toplevelstmtsNoAlloc )* )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:200:58: ^( SCOPE ( toplevelstmtsNoAlloc )* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(SCOPE, "SCOPE"), root_1);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:200:66: ( toplevelstmtsNoAlloc )*
                while ( stream_toplevelstmtsNoAlloc.hasNext() ) {
                    adaptor.addChild(root_1, stream_toplevelstmtsNoAlloc.nextTree());

                }
                stream_toplevelstmtsNoAlloc.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "xscopeNoAlloc"

    public static class listCompr_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "listCompr"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:205:1: listCompr : ( forIn )+ COLON listiterable -> ^( LIST_COMPREHENSION ( forIn )+ listiterable ) ;
    public final EulangParser.listCompr_return listCompr() throws RecognitionException {
        EulangParser.listCompr_return retval = new EulangParser.listCompr_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COLON96=null;
        EulangParser.forIn_return forIn95 = null;

        EulangParser.listiterable_return listiterable97 = null;


        CommonTree COLON96_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleSubtreeStream stream_forIn=new RewriteRuleSubtreeStream(adaptor,"rule forIn");
        RewriteRuleSubtreeStream stream_listiterable=new RewriteRuleSubtreeStream(adaptor,"rule listiterable");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:205:10: ( ( forIn )+ COLON listiterable -> ^( LIST_COMPREHENSION ( forIn )+ listiterable ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:205:12: ( forIn )+ COLON listiterable
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:205:12: ( forIn )+
            int cnt27=0;
            loop27:
            do {
                int alt27=2;
                int LA27_0 = input.LA(1);

                if ( (LA27_0==FOR) ) {
                    alt27=1;
                }


                switch (alt27) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:205:12: forIn
            	    {
            	    pushFollow(FOLLOW_forIn_in_listCompr1482);
            	    forIn95=forIn();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_forIn.add(forIn95.getTree());

            	    }
            	    break;

            	default :
            	    if ( cnt27 >= 1 ) break loop27;
            	    if (state.backtracking>0) {state.failed=true; return retval;}
                        EarlyExitException eee =
                            new EarlyExitException(27, input);
                        throw eee;
                }
                cnt27++;
            } while (true);

            COLON96=(Token)match(input,COLON,FOLLOW_COLON_in_listCompr1485); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_COLON.add(COLON96);

            pushFollow(FOLLOW_listiterable_in_listCompr1487);
            listiterable97=listiterable();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_listiterable.add(listiterable97.getTree());


            // AST REWRITE
            // elements: listiterable, forIn
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 205:42: -> ^( LIST_COMPREHENSION ( forIn )+ listiterable )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:205:45: ^( LIST_COMPREHENSION ( forIn )+ listiterable )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(LIST_COMPREHENSION, "LIST_COMPREHENSION"), root_1);

                if ( !(stream_forIn.hasNext()) ) {
                    throw new RewriteEarlyExitException();
                }
                while ( stream_forIn.hasNext() ) {
                    adaptor.addChild(root_1, stream_forIn.nextTree());

                }
                stream_forIn.reset();
                adaptor.addChild(root_1, stream_listiterable.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "listCompr"

    public static class forIn_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "forIn"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:208:1: forIn : FOR idlist IN list -> ^( FOR idlist list ) ;
    public final EulangParser.forIn_return forIn() throws RecognitionException {
        EulangParser.forIn_return retval = new EulangParser.forIn_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token FOR98=null;
        Token IN100=null;
        EulangParser.idlist_return idlist99 = null;

        EulangParser.list_return list101 = null;


        CommonTree FOR98_tree=null;
        CommonTree IN100_tree=null;
        RewriteRuleTokenStream stream_FOR=new RewriteRuleTokenStream(adaptor,"token FOR");
        RewriteRuleTokenStream stream_IN=new RewriteRuleTokenStream(adaptor,"token IN");
        RewriteRuleSubtreeStream stream_list=new RewriteRuleSubtreeStream(adaptor,"rule list");
        RewriteRuleSubtreeStream stream_idlist=new RewriteRuleSubtreeStream(adaptor,"rule idlist");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:208:7: ( FOR idlist IN list -> ^( FOR idlist list ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:208:9: FOR idlist IN list
            {
            FOR98=(Token)match(input,FOR,FOLLOW_FOR_in_forIn1519); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_FOR.add(FOR98);

            pushFollow(FOLLOW_idlist_in_forIn1521);
            idlist99=idlist();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_idlist.add(idlist99.getTree());
            IN100=(Token)match(input,IN,FOLLOW_IN_in_forIn1523); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_IN.add(IN100);

            pushFollow(FOLLOW_list_in_forIn1525);
            list101=list();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_list.add(list101.getTree());


            // AST REWRITE
            // elements: idlist, FOR, list
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 208:33: -> ^( FOR idlist list )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:208:36: ^( FOR idlist list )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot(stream_FOR.nextNode(), root_1);

                adaptor.addChild(root_1, stream_idlist.nextTree());
                adaptor.addChild(root_1, stream_list.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "forIn"

    public static class idlist_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "idlist"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:210:1: idlist : ID ( COMMA ID )* -> ^( IDLIST ( ID )+ ) ;
    public final EulangParser.idlist_return idlist() throws RecognitionException {
        EulangParser.idlist_return retval = new EulangParser.idlist_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ID102=null;
        Token COMMA103=null;
        Token ID104=null;

        CommonTree ID102_tree=null;
        CommonTree COMMA103_tree=null;
        CommonTree ID104_tree=null;
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:210:8: ( ID ( COMMA ID )* -> ^( IDLIST ( ID )+ ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:210:10: ID ( COMMA ID )*
            {
            ID102=(Token)match(input,ID,FOLLOW_ID_in_idlist1550); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_ID.add(ID102);

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:210:13: ( COMMA ID )*
            loop28:
            do {
                int alt28=2;
                int LA28_0 = input.LA(1);

                if ( (LA28_0==COMMA) ) {
                    alt28=1;
                }


                switch (alt28) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:210:14: COMMA ID
            	    {
            	    COMMA103=(Token)match(input,COMMA,FOLLOW_COMMA_in_idlist1553); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA103);

            	    ID104=(Token)match(input,ID,FOLLOW_ID_in_idlist1555); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_ID.add(ID104);


            	    }
            	    break;

            	default :
            	    break loop28;
                }
            } while (true);



            // AST REWRITE
            // elements: ID
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 210:28: -> ^( IDLIST ( ID )+ )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:210:31: ^( IDLIST ( ID )+ )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(IDLIST, "IDLIST"), root_1);

                if ( !(stream_ID.hasNext()) ) {
                    throw new RewriteEarlyExitException();
                }
                while ( stream_ID.hasNext() ) {
                    adaptor.addChild(root_1, stream_ID.nextNode());

                }
                stream_ID.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "idlist"

    public static class idlistOrEmpty_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "idlistOrEmpty"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:212:1: idlistOrEmpty : ( idlist -> idlist | -> ^( IDLIST ) );
    public final EulangParser.idlistOrEmpty_return idlistOrEmpty() throws RecognitionException {
        EulangParser.idlistOrEmpty_return retval = new EulangParser.idlistOrEmpty_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.idlist_return idlist105 = null;


        RewriteRuleSubtreeStream stream_idlist=new RewriteRuleSubtreeStream(adaptor,"rule idlist");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:212:15: ( idlist -> idlist | -> ^( IDLIST ) )
            int alt29=2;
            int LA29_0 = input.LA(1);

            if ( (LA29_0==ID) ) {
                alt29=1;
            }
            else if ( (LA29_0==RBRACKET) ) {
                alt29=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 29, 0, input);

                throw nvae;
            }
            switch (alt29) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:212:17: idlist
                    {
                    pushFollow(FOLLOW_idlist_in_idlistOrEmpty1581);
                    idlist105=idlist();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_idlist.add(idlist105.getTree());


                    // AST REWRITE
                    // elements: idlist
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 212:24: -> idlist
                    {
                        adaptor.addChild(root_0, stream_idlist.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:212:36: 
                    {

                    // AST REWRITE
                    // elements: 
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 212:36: -> ^( IDLIST )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:212:39: ^( IDLIST )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(IDLIST, "IDLIST"), root_1);

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "idlistOrEmpty"

    public static class listiterable_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "listiterable"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:214:1: listiterable : code ;
    public final EulangParser.listiterable_return listiterable() throws RecognitionException {
        EulangParser.listiterable_return retval = new EulangParser.listiterable_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.code_return code106 = null;



        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:214:14: ( code )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:214:16: code
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_code_in_listiterable1602);
            code106=code();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, code106.getTree());

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "listiterable"

    public static class list_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "list"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:216:1: list : LBRACKET listitems RBRACKET -> ^( LIST ( listitems )* ) ;
    public final EulangParser.list_return list() throws RecognitionException {
        EulangParser.list_return retval = new EulangParser.list_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LBRACKET107=null;
        Token RBRACKET109=null;
        EulangParser.listitems_return listitems108 = null;


        CommonTree LBRACKET107_tree=null;
        CommonTree RBRACKET109_tree=null;
        RewriteRuleTokenStream stream_LBRACKET=new RewriteRuleTokenStream(adaptor,"token LBRACKET");
        RewriteRuleTokenStream stream_RBRACKET=new RewriteRuleTokenStream(adaptor,"token RBRACKET");
        RewriteRuleSubtreeStream stream_listitems=new RewriteRuleSubtreeStream(adaptor,"rule listitems");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:216:6: ( LBRACKET listitems RBRACKET -> ^( LIST ( listitems )* ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:216:8: LBRACKET listitems RBRACKET
            {
            LBRACKET107=(Token)match(input,LBRACKET,FOLLOW_LBRACKET_in_list1616); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LBRACKET.add(LBRACKET107);

            pushFollow(FOLLOW_listitems_in_list1618);
            listitems108=listitems();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_listitems.add(listitems108.getTree());
            RBRACKET109=(Token)match(input,RBRACKET,FOLLOW_RBRACKET_in_list1620); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RBRACKET.add(RBRACKET109);



            // AST REWRITE
            // elements: listitems
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 216:40: -> ^( LIST ( listitems )* )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:216:43: ^( LIST ( listitems )* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(LIST, "LIST"), root_1);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:216:50: ( listitems )*
                while ( stream_listitems.hasNext() ) {
                    adaptor.addChild(root_1, stream_listitems.nextTree());

                }
                stream_listitems.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "list"

    public static class listitems_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "listitems"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:219:1: listitems : ( listitem ( COMMA listitem )* ( COMMA )? )? ;
    public final EulangParser.listitems_return listitems() throws RecognitionException {
        EulangParser.listitems_return retval = new EulangParser.listitems_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COMMA111=null;
        Token COMMA113=null;
        EulangParser.listitem_return listitem110 = null;

        EulangParser.listitem_return listitem112 = null;


        CommonTree COMMA111_tree=null;
        CommonTree COMMA113_tree=null;

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:219:10: ( ( listitem ( COMMA listitem )* ( COMMA )? )? )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:219:12: ( listitem ( COMMA listitem )* ( COMMA )? )?
            {
            root_0 = (CommonTree)adaptor.nil();

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:219:12: ( listitem ( COMMA listitem )* ( COMMA )? )?
            int alt32=2;
            int LA32_0 = input.LA(1);

            if ( (LA32_0==CODE||(LA32_0>=SIZEOF && LA32_0<=TYPEOF)||LA32_0==ID||LA32_0==COLON||LA32_0==LBRACKET||LA32_0==LBRACE||LA32_0==LPAREN||LA32_0==NIL||LA32_0==IF||LA32_0==NOT||(LA32_0>=TILDE && LA32_0<=AMP)||LA32_0==MINUS||(LA32_0>=PLUSPLUS && LA32_0<=STRING_LITERAL)||(LA32_0>=FALSE && LA32_0<=DATA)) ) {
                alt32=1;
            }
            switch (alt32) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:219:13: listitem ( COMMA listitem )* ( COMMA )?
                    {
                    pushFollow(FOLLOW_listitem_in_listitems1650);
                    listitem110=listitem();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, listitem110.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:219:22: ( COMMA listitem )*
                    loop30:
                    do {
                        int alt30=2;
                        int LA30_0 = input.LA(1);

                        if ( (LA30_0==COMMA) ) {
                            int LA30_1 = input.LA(2);

                            if ( (LA30_1==CODE||(LA30_1>=SIZEOF && LA30_1<=TYPEOF)||LA30_1==ID||LA30_1==COLON||LA30_1==LBRACKET||LA30_1==LBRACE||LA30_1==LPAREN||LA30_1==NIL||LA30_1==IF||LA30_1==NOT||(LA30_1>=TILDE && LA30_1<=AMP)||LA30_1==MINUS||(LA30_1>=PLUSPLUS && LA30_1<=STRING_LITERAL)||(LA30_1>=FALSE && LA30_1<=DATA)) ) {
                                alt30=1;
                            }


                        }


                        switch (alt30) {
                    	case 1 :
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:219:24: COMMA listitem
                    	    {
                    	    COMMA111=(Token)match(input,COMMA,FOLLOW_COMMA_in_listitems1654); if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) {
                    	    COMMA111_tree = (CommonTree)adaptor.create(COMMA111);
                    	    adaptor.addChild(root_0, COMMA111_tree);
                    	    }
                    	    pushFollow(FOLLOW_listitem_in_listitems1656);
                    	    listitem112=listitem();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) adaptor.addChild(root_0, listitem112.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop30;
                        }
                    } while (true);

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:219:42: ( COMMA )?
                    int alt31=2;
                    int LA31_0 = input.LA(1);

                    if ( (LA31_0==COMMA) ) {
                        alt31=1;
                    }
                    switch (alt31) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:219:42: COMMA
                            {
                            COMMA113=(Token)match(input,COMMA,FOLLOW_COMMA_in_listitems1661); if (state.failed) return retval;
                            if ( state.backtracking==0 ) {
                            COMMA113_tree = (CommonTree)adaptor.create(COMMA113);
                            adaptor.addChild(root_0, COMMA113_tree);
                            }

                            }
                            break;

                    }


                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "listitems"

    public static class listitem_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "listitem"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:222:1: listitem : toplevelvalue ;
    public final EulangParser.listitem_return listitem() throws RecognitionException {
        EulangParser.listitem_return retval = new EulangParser.listitem_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.toplevelvalue_return toplevelvalue114 = null;



        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:222:10: ( toplevelvalue )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:222:12: toplevelvalue
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_toplevelvalue_in_listitem1687);
            toplevelvalue114=toplevelvalue();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, toplevelvalue114.getTree());

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "listitem"

    public static class code_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "code"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:227:1: code : CODE ( attrs )? ( proto )? LBRACE codestmtlist RBRACE -> ^( CODE ( attrs )? ( proto )? ( codestmtlist )* ) ;
    public final EulangParser.code_return code() throws RecognitionException {
        EulangParser.code_return retval = new EulangParser.code_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token CODE115=null;
        Token LBRACE118=null;
        Token RBRACE120=null;
        EulangParser.attrs_return attrs116 = null;

        EulangParser.proto_return proto117 = null;

        EulangParser.codestmtlist_return codestmtlist119 = null;


        CommonTree CODE115_tree=null;
        CommonTree LBRACE118_tree=null;
        CommonTree RBRACE120_tree=null;
        RewriteRuleTokenStream stream_RBRACE=new RewriteRuleTokenStream(adaptor,"token RBRACE");
        RewriteRuleTokenStream stream_LBRACE=new RewriteRuleTokenStream(adaptor,"token LBRACE");
        RewriteRuleTokenStream stream_CODE=new RewriteRuleTokenStream(adaptor,"token CODE");
        RewriteRuleSubtreeStream stream_attrs=new RewriteRuleSubtreeStream(adaptor,"rule attrs");
        RewriteRuleSubtreeStream stream_codestmtlist=new RewriteRuleSubtreeStream(adaptor,"rule codestmtlist");
        RewriteRuleSubtreeStream stream_proto=new RewriteRuleSubtreeStream(adaptor,"rule proto");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:227:6: ( CODE ( attrs )? ( proto )? LBRACE codestmtlist RBRACE -> ^( CODE ( attrs )? ( proto )? ( codestmtlist )* ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:227:8: CODE ( attrs )? ( proto )? LBRACE codestmtlist RBRACE
            {
            CODE115=(Token)match(input,CODE,FOLLOW_CODE_in_code1705); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_CODE.add(CODE115);

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:227:13: ( attrs )?
            int alt33=2;
            int LA33_0 = input.LA(1);

            if ( (LA33_0==ATTR) ) {
                alt33=1;
            }
            switch (alt33) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:227:13: attrs
                    {
                    pushFollow(FOLLOW_attrs_in_code1707);
                    attrs116=attrs();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_attrs.add(attrs116.getTree());

                    }
                    break;

            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:227:20: ( proto )?
            int alt34=2;
            int LA34_0 = input.LA(1);

            if ( (LA34_0==LPAREN) ) {
                alt34=1;
            }
            switch (alt34) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:227:20: proto
                    {
                    pushFollow(FOLLOW_proto_in_code1710);
                    proto117=proto();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_proto.add(proto117.getTree());

                    }
                    break;

            }

            LBRACE118=(Token)match(input,LBRACE,FOLLOW_LBRACE_in_code1713); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LBRACE.add(LBRACE118);

            pushFollow(FOLLOW_codestmtlist_in_code1715);
            codestmtlist119=codestmtlist();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_codestmtlist.add(codestmtlist119.getTree());
            RBRACE120=(Token)match(input,RBRACE,FOLLOW_RBRACE_in_code1717); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RBRACE.add(RBRACE120);



            // AST REWRITE
            // elements: codestmtlist, proto, CODE, attrs
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 227:54: -> ^( CODE ( attrs )? ( proto )? ( codestmtlist )* )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:227:57: ^( CODE ( attrs )? ( proto )? ( codestmtlist )* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot(stream_CODE.nextNode(), root_1);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:227:64: ( attrs )?
                if ( stream_attrs.hasNext() ) {
                    adaptor.addChild(root_1, stream_attrs.nextTree());

                }
                stream_attrs.reset();
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:227:71: ( proto )?
                if ( stream_proto.hasNext() ) {
                    adaptor.addChild(root_1, stream_proto.nextTree());

                }
                stream_proto.reset();
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:227:78: ( codestmtlist )*
                while ( stream_codestmtlist.hasNext() ) {
                    adaptor.addChild(root_1, stream_codestmtlist.nextTree());

                }
                stream_codestmtlist.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "code"

    public static class attrs_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "attrs"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:230:1: attrs : ( ATTR )+ -> ^( ATTRS ( ATTR )+ ) ;
    public final EulangParser.attrs_return attrs() throws RecognitionException {
        EulangParser.attrs_return retval = new EulangParser.attrs_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ATTR121=null;

        CommonTree ATTR121_tree=null;
        RewriteRuleTokenStream stream_ATTR=new RewriteRuleTokenStream(adaptor,"token ATTR");

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:230:7: ( ( ATTR )+ -> ^( ATTRS ( ATTR )+ ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:230:9: ( ATTR )+
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:230:9: ( ATTR )+
            int cnt35=0;
            loop35:
            do {
                int alt35=2;
                int LA35_0 = input.LA(1);

                if ( (LA35_0==ATTR) ) {
                    alt35=1;
                }


                switch (alt35) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:230:9: ATTR
            	    {
            	    ATTR121=(Token)match(input,ATTR,FOLLOW_ATTR_in_attrs1747); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_ATTR.add(ATTR121);


            	    }
            	    break;

            	default :
            	    if ( cnt35 >= 1 ) break loop35;
            	    if (state.backtracking>0) {state.failed=true; return retval;}
                        EarlyExitException eee =
                            new EarlyExitException(35, input);
                        throw eee;
                }
                cnt35++;
            } while (true);



            // AST REWRITE
            // elements: ATTR
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 230:17: -> ^( ATTRS ( ATTR )+ )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:230:19: ^( ATTRS ( ATTR )+ )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ATTRS, "ATTRS"), root_1);

                if ( !(stream_ATTR.hasNext()) ) {
                    throw new RewriteEarlyExitException();
                }
                while ( stream_ATTR.hasNext() ) {
                    adaptor.addChild(root_1, stream_ATTR.nextNode());

                }
                stream_ATTR.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "attrs"

    public static class argdefs_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "argdefs"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:235:1: argdefs options {backtrack=true; } : ( | argdefsWithTypes | argdefWithType | argdefsWithNames );
    public final EulangParser.argdefs_return argdefs() throws RecognitionException {
        EulangParser.argdefs_return retval = new EulangParser.argdefs_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.argdefsWithTypes_return argdefsWithTypes122 = null;

        EulangParser.argdefWithType_return argdefWithType123 = null;

        EulangParser.argdefsWithNames_return argdefsWithNames124 = null;



        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:235:40: ( | argdefsWithTypes | argdefWithType | argdefsWithNames )
            int alt36=4;
            int LA36_0 = input.LA(1);

            if ( ((LA36_0>=RPAREN && LA36_0<=ARROW)) ) {
                alt36=1;
            }
            else if ( (LA36_0==ID) ) {
                int LA36_3 = input.LA(2);

                if ( (synpred7_Eulang()) ) {
                    alt36=2;
                }
                else if ( (synpred8_Eulang()) ) {
                    alt36=3;
                }
                else if ( (true) ) {
                    alt36=4;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 36, 3, input);

                    throw nvae;
                }
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 36, 0, input);

                throw nvae;
            }
            switch (alt36) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:236:3: 
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:236:5: argdefsWithTypes
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_argdefsWithTypes_in_argdefs1791);
                    argdefsWithTypes122=argdefsWithTypes();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, argdefsWithTypes122.getTree());

                    }
                    break;
                case 3 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:237:5: argdefWithType
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_argdefWithType_in_argdefs1798);
                    argdefWithType123=argdefWithType();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, argdefWithType123.getTree());

                    }
                    break;
                case 4 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:238:6: argdefsWithNames
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_argdefsWithNames_in_argdefs1805);
                    argdefsWithNames124=argdefsWithNames();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, argdefsWithNames124.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "argdefs"

    public static class argdefsWithTypes_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "argdefsWithTypes"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:241:1: argdefsWithTypes : ( argdefWithType ( SEMI argdefWithType )+ ( SEMI )? ) -> ( argdefWithType )* ;
    public final EulangParser.argdefsWithTypes_return argdefsWithTypes() throws RecognitionException {
        EulangParser.argdefsWithTypes_return retval = new EulangParser.argdefsWithTypes_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token SEMI126=null;
        Token SEMI128=null;
        EulangParser.argdefWithType_return argdefWithType125 = null;

        EulangParser.argdefWithType_return argdefWithType127 = null;


        CommonTree SEMI126_tree=null;
        CommonTree SEMI128_tree=null;
        RewriteRuleTokenStream stream_SEMI=new RewriteRuleTokenStream(adaptor,"token SEMI");
        RewriteRuleSubtreeStream stream_argdefWithType=new RewriteRuleSubtreeStream(adaptor,"rule argdefWithType");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:241:17: ( ( argdefWithType ( SEMI argdefWithType )+ ( SEMI )? ) -> ( argdefWithType )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:241:19: ( argdefWithType ( SEMI argdefWithType )+ ( SEMI )? )
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:241:19: ( argdefWithType ( SEMI argdefWithType )+ ( SEMI )? )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:241:20: argdefWithType ( SEMI argdefWithType )+ ( SEMI )?
            {
            pushFollow(FOLLOW_argdefWithType_in_argdefsWithTypes1821);
            argdefWithType125=argdefWithType();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_argdefWithType.add(argdefWithType125.getTree());
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:241:35: ( SEMI argdefWithType )+
            int cnt37=0;
            loop37:
            do {
                int alt37=2;
                int LA37_0 = input.LA(1);

                if ( (LA37_0==SEMI) ) {
                    int LA37_1 = input.LA(2);

                    if ( (LA37_1==ID) ) {
                        alt37=1;
                    }


                }


                switch (alt37) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:241:37: SEMI argdefWithType
            	    {
            	    SEMI126=(Token)match(input,SEMI,FOLLOW_SEMI_in_argdefsWithTypes1825); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_SEMI.add(SEMI126);

            	    pushFollow(FOLLOW_argdefWithType_in_argdefsWithTypes1827);
            	    argdefWithType127=argdefWithType();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_argdefWithType.add(argdefWithType127.getTree());

            	    }
            	    break;

            	default :
            	    if ( cnt37 >= 1 ) break loop37;
            	    if (state.backtracking>0) {state.failed=true; return retval;}
                        EarlyExitException eee =
                            new EarlyExitException(37, input);
                        throw eee;
                }
                cnt37++;
            } while (true);

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:241:59: ( SEMI )?
            int alt38=2;
            int LA38_0 = input.LA(1);

            if ( (LA38_0==SEMI) ) {
                alt38=1;
            }
            switch (alt38) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:241:59: SEMI
                    {
                    SEMI128=(Token)match(input,SEMI,FOLLOW_SEMI_in_argdefsWithTypes1831); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_SEMI.add(SEMI128);


                    }
                    break;

            }


            }



            // AST REWRITE
            // elements: argdefWithType
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 241:73: -> ( argdefWithType )*
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:241:76: ( argdefWithType )*
                while ( stream_argdefWithType.hasNext() ) {
                    adaptor.addChild(root_0, stream_argdefWithType.nextTree());

                }
                stream_argdefWithType.reset();

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "argdefsWithTypes"

    public static class argdefWithType_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "argdefWithType"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:245:1: argdefWithType : ID ( COMMA ID )* ( attrs )? ( COLON type )? ( EQUALS init= rhsExpr )? -> ( ^( ARGDEF ID ( attrs )? ( type )* ( $init)? ) )+ ;
    public final EulangParser.argdefWithType_return argdefWithType() throws RecognitionException {
        EulangParser.argdefWithType_return retval = new EulangParser.argdefWithType_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ID129=null;
        Token COMMA130=null;
        Token ID131=null;
        Token COLON133=null;
        Token EQUALS135=null;
        EulangParser.rhsExpr_return init = null;

        EulangParser.attrs_return attrs132 = null;

        EulangParser.type_return type134 = null;


        CommonTree ID129_tree=null;
        CommonTree COMMA130_tree=null;
        CommonTree ID131_tree=null;
        CommonTree COLON133_tree=null;
        CommonTree EQUALS135_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleTokenStream stream_EQUALS=new RewriteRuleTokenStream(adaptor,"token EQUALS");
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleSubtreeStream stream_attrs=new RewriteRuleSubtreeStream(adaptor,"rule attrs");
        RewriteRuleSubtreeStream stream_rhsExpr=new RewriteRuleSubtreeStream(adaptor,"rule rhsExpr");
        RewriteRuleSubtreeStream stream_type=new RewriteRuleSubtreeStream(adaptor,"rule type");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:245:15: ( ID ( COMMA ID )* ( attrs )? ( COLON type )? ( EQUALS init= rhsExpr )? -> ( ^( ARGDEF ID ( attrs )? ( type )* ( $init)? ) )+ )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:245:18: ID ( COMMA ID )* ( attrs )? ( COLON type )? ( EQUALS init= rhsExpr )?
            {
            ID129=(Token)match(input,ID,FOLLOW_ID_in_argdefWithType1860); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_ID.add(ID129);

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:245:21: ( COMMA ID )*
            loop39:
            do {
                int alt39=2;
                int LA39_0 = input.LA(1);

                if ( (LA39_0==COMMA) ) {
                    alt39=1;
                }


                switch (alt39) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:245:22: COMMA ID
            	    {
            	    COMMA130=(Token)match(input,COMMA,FOLLOW_COMMA_in_argdefWithType1863); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA130);

            	    ID131=(Token)match(input,ID,FOLLOW_ID_in_argdefWithType1865); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_ID.add(ID131);


            	    }
            	    break;

            	default :
            	    break loop39;
                }
            } while (true);

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:245:33: ( attrs )?
            int alt40=2;
            int LA40_0 = input.LA(1);

            if ( (LA40_0==ATTR) ) {
                alt40=1;
            }
            switch (alt40) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:245:33: attrs
                    {
                    pushFollow(FOLLOW_attrs_in_argdefWithType1869);
                    attrs132=attrs();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_attrs.add(attrs132.getTree());

                    }
                    break;

            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:245:40: ( COLON type )?
            int alt41=2;
            int LA41_0 = input.LA(1);

            if ( (LA41_0==COLON) ) {
                alt41=1;
            }
            switch (alt41) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:245:41: COLON type
                    {
                    COLON133=(Token)match(input,COLON,FOLLOW_COLON_in_argdefWithType1873); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COLON.add(COLON133);

                    pushFollow(FOLLOW_type_in_argdefWithType1875);
                    type134=type();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_type.add(type134.getTree());

                    }
                    break;

            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:245:54: ( EQUALS init= rhsExpr )?
            int alt42=2;
            int LA42_0 = input.LA(1);

            if ( (LA42_0==EQUALS) ) {
                alt42=1;
            }
            switch (alt42) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:245:55: EQUALS init= rhsExpr
                    {
                    EQUALS135=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_argdefWithType1880); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_EQUALS.add(EQUALS135);

                    pushFollow(FOLLOW_rhsExpr_in_argdefWithType1884);
                    init=rhsExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_rhsExpr.add(init.getTree());

                    }
                    break;

            }



            // AST REWRITE
            // elements: init, type, ID, attrs
            // token labels: 
            // rule labels: retval, init
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            RewriteRuleSubtreeStream stream_init=new RewriteRuleSubtreeStream(adaptor,"rule init",init!=null?init.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 245:78: -> ( ^( ARGDEF ID ( attrs )? ( type )* ( $init)? ) )+
            {
                if ( !(stream_ID.hasNext()) ) {
                    throw new RewriteEarlyExitException();
                }
                while ( stream_ID.hasNext() ) {
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:245:81: ^( ARGDEF ID ( attrs )? ( type )* ( $init)? )
                    {
                    CommonTree root_1 = (CommonTree)adaptor.nil();
                    root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ARGDEF, "ARGDEF"), root_1);

                    adaptor.addChild(root_1, stream_ID.nextNode());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:245:93: ( attrs )?
                    if ( stream_attrs.hasNext() ) {
                        adaptor.addChild(root_1, stream_attrs.nextTree());

                    }
                    stream_attrs.reset();
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:245:100: ( type )*
                    while ( stream_type.hasNext() ) {
                        adaptor.addChild(root_1, stream_type.nextTree());

                    }
                    stream_type.reset();
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:245:106: ( $init)?
                    if ( stream_init.hasNext() ) {
                        adaptor.addChild(root_1, stream_init.nextTree());

                    }
                    stream_init.reset();

                    adaptor.addChild(root_0, root_1);
                    }

                }
                stream_ID.reset();

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "argdefWithType"

    public static class argdefsWithNames_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "argdefsWithNames"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:248:1: argdefsWithNames : ( argdefWithName ( COMMA argdefWithName )+ ( COMMA )? ) -> ( argdefWithName )* ;
    public final EulangParser.argdefsWithNames_return argdefsWithNames() throws RecognitionException {
        EulangParser.argdefsWithNames_return retval = new EulangParser.argdefsWithNames_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COMMA137=null;
        Token COMMA139=null;
        EulangParser.argdefWithName_return argdefWithName136 = null;

        EulangParser.argdefWithName_return argdefWithName138 = null;


        CommonTree COMMA137_tree=null;
        CommonTree COMMA139_tree=null;
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleSubtreeStream stream_argdefWithName=new RewriteRuleSubtreeStream(adaptor,"rule argdefWithName");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:248:18: ( ( argdefWithName ( COMMA argdefWithName )+ ( COMMA )? ) -> ( argdefWithName )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:248:21: ( argdefWithName ( COMMA argdefWithName )+ ( COMMA )? )
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:248:21: ( argdefWithName ( COMMA argdefWithName )+ ( COMMA )? )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:248:22: argdefWithName ( COMMA argdefWithName )+ ( COMMA )?
            {
            pushFollow(FOLLOW_argdefWithName_in_argdefsWithNames1919);
            argdefWithName136=argdefWithName();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_argdefWithName.add(argdefWithName136.getTree());
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:248:37: ( COMMA argdefWithName )+
            int cnt43=0;
            loop43:
            do {
                int alt43=2;
                int LA43_0 = input.LA(1);

                if ( (LA43_0==COMMA) ) {
                    int LA43_1 = input.LA(2);

                    if ( (LA43_1==ID) ) {
                        alt43=1;
                    }


                }


                switch (alt43) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:248:39: COMMA argdefWithName
            	    {
            	    COMMA137=(Token)match(input,COMMA,FOLLOW_COMMA_in_argdefsWithNames1923); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA137);

            	    pushFollow(FOLLOW_argdefWithName_in_argdefsWithNames1925);
            	    argdefWithName138=argdefWithName();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_argdefWithName.add(argdefWithName138.getTree());

            	    }
            	    break;

            	default :
            	    if ( cnt43 >= 1 ) break loop43;
            	    if (state.backtracking>0) {state.failed=true; return retval;}
                        EarlyExitException eee =
                            new EarlyExitException(43, input);
                        throw eee;
                }
                cnt43++;
            } while (true);

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:248:62: ( COMMA )?
            int alt44=2;
            int LA44_0 = input.LA(1);

            if ( (LA44_0==COMMA) ) {
                alt44=1;
            }
            switch (alt44) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:248:62: COMMA
                    {
                    COMMA139=(Token)match(input,COMMA,FOLLOW_COMMA_in_argdefsWithNames1929); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COMMA.add(COMMA139);


                    }
                    break;

            }


            }



            // AST REWRITE
            // elements: argdefWithName
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 248:73: -> ( argdefWithName )*
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:248:76: ( argdefWithName )*
                while ( stream_argdefWithName.hasNext() ) {
                    adaptor.addChild(root_0, stream_argdefWithName.nextTree());

                }
                stream_argdefWithName.reset();

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "argdefsWithNames"

    public static class argdefWithName_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "argdefWithName"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:250:1: argdefWithName : ID ( attrs )? -> ^( ARGDEF ID ( attrs )? ) ;
    public final EulangParser.argdefWithName_return argdefWithName() throws RecognitionException {
        EulangParser.argdefWithName_return retval = new EulangParser.argdefWithName_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ID140=null;
        EulangParser.attrs_return attrs141 = null;


        CommonTree ID140_tree=null;
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
        RewriteRuleSubtreeStream stream_attrs=new RewriteRuleSubtreeStream(adaptor,"rule attrs");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:250:15: ( ID ( attrs )? -> ^( ARGDEF ID ( attrs )? ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:250:17: ID ( attrs )?
            {
            ID140=(Token)match(input,ID,FOLLOW_ID_in_argdefWithName1951); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_ID.add(ID140);

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:250:20: ( attrs )?
            int alt45=2;
            int LA45_0 = input.LA(1);

            if ( (LA45_0==ATTR) ) {
                alt45=1;
            }
            switch (alt45) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:250:20: attrs
                    {
                    pushFollow(FOLLOW_attrs_in_argdefWithName1953);
                    attrs141=attrs();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_attrs.add(attrs141.getTree());

                    }
                    break;

            }



            // AST REWRITE
            // elements: attrs, ID
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 250:28: -> ^( ARGDEF ID ( attrs )? )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:250:31: ^( ARGDEF ID ( attrs )? )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ARGDEF, "ARGDEF"), root_1);

                adaptor.addChild(root_1, stream_ID.nextNode());
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:250:43: ( attrs )?
                if ( stream_attrs.hasNext() ) {
                    adaptor.addChild(root_1, stream_attrs.nextTree());

                }
                stream_attrs.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "argdefWithName"

    public static class proto_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "proto"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:254:1: proto : LPAREN argdefs ( xreturns )? RPAREN -> ^( PROTO ( xreturns )? ( argdefs )* ) ;
    public final EulangParser.proto_return proto() throws RecognitionException {
        EulangParser.proto_return retval = new EulangParser.proto_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LPAREN142=null;
        Token RPAREN145=null;
        EulangParser.argdefs_return argdefs143 = null;

        EulangParser.xreturns_return xreturns144 = null;


        CommonTree LPAREN142_tree=null;
        CommonTree RPAREN145_tree=null;
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleSubtreeStream stream_argdefs=new RewriteRuleSubtreeStream(adaptor,"rule argdefs");
        RewriteRuleSubtreeStream stream_xreturns=new RewriteRuleSubtreeStream(adaptor,"rule xreturns");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:254:7: ( LPAREN argdefs ( xreturns )? RPAREN -> ^( PROTO ( xreturns )? ( argdefs )* ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:254:9: LPAREN argdefs ( xreturns )? RPAREN
            {
            LPAREN142=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_proto1978); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN142);

            pushFollow(FOLLOW_argdefs_in_proto1980);
            argdefs143=argdefs();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_argdefs.add(argdefs143.getTree());
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:254:24: ( xreturns )?
            int alt46=2;
            int LA46_0 = input.LA(1);

            if ( (LA46_0==ARROW) ) {
                alt46=1;
            }
            switch (alt46) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:254:24: xreturns
                    {
                    pushFollow(FOLLOW_xreturns_in_proto1982);
                    xreturns144=xreturns();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_xreturns.add(xreturns144.getTree());

                    }
                    break;

            }

            RPAREN145=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_proto1985); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN145);



            // AST REWRITE
            // elements: xreturns, argdefs
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 254:59: -> ^( PROTO ( xreturns )? ( argdefs )* )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:254:62: ^( PROTO ( xreturns )? ( argdefs )* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(PROTO, "PROTO"), root_1);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:254:70: ( xreturns )?
                if ( stream_xreturns.hasNext() ) {
                    adaptor.addChild(root_1, stream_xreturns.nextTree());

                }
                stream_xreturns.reset();
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:254:80: ( argdefs )*
                while ( stream_argdefs.hasNext() ) {
                    adaptor.addChild(root_1, stream_argdefs.nextTree());

                }
                stream_argdefs.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "proto"

    public static class xreturns_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "xreturns"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:257:1: xreturns : ( ARROW type -> type | ARROW NIL -> ^( TYPE NIL ) );
    public final EulangParser.xreturns_return xreturns() throws RecognitionException {
        EulangParser.xreturns_return retval = new EulangParser.xreturns_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ARROW146=null;
        Token ARROW148=null;
        Token NIL149=null;
        EulangParser.type_return type147 = null;


        CommonTree ARROW146_tree=null;
        CommonTree ARROW148_tree=null;
        CommonTree NIL149_tree=null;
        RewriteRuleTokenStream stream_ARROW=new RewriteRuleTokenStream(adaptor,"token ARROW");
        RewriteRuleTokenStream stream_NIL=new RewriteRuleTokenStream(adaptor,"token NIL");
        RewriteRuleSubtreeStream stream_type=new RewriteRuleSubtreeStream(adaptor,"rule type");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:257:10: ( ARROW type -> type | ARROW NIL -> ^( TYPE NIL ) )
            int alt47=2;
            int LA47_0 = input.LA(1);

            if ( (LA47_0==ARROW) ) {
                int LA47_1 = input.LA(2);

                if ( (LA47_1==NIL) ) {
                    alt47=2;
                }
                else if ( (LA47_1==CODE||LA47_1==ID||LA47_1==COLON||LA47_1==LPAREN||(LA47_1>=COLONS && LA47_1<=DATA)) ) {
                    alt47=1;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 47, 1, input);

                    throw nvae;
                }
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 47, 0, input);

                throw nvae;
            }
            switch (alt47) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:257:12: ARROW type
                    {
                    ARROW146=(Token)match(input,ARROW,FOLLOW_ARROW_in_xreturns2028); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ARROW.add(ARROW146);

                    pushFollow(FOLLOW_type_in_xreturns2030);
                    type147=type();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_type.add(type147.getTree());


                    // AST REWRITE
                    // elements: type
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 257:28: -> type
                    {
                        adaptor.addChild(root_0, stream_type.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:259:5: ARROW NIL
                    {
                    ARROW148=(Token)match(input,ARROW,FOLLOW_ARROW_in_xreturns2047); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ARROW.add(ARROW148);

                    NIL149=(Token)match(input,NIL,FOLLOW_NIL_in_xreturns2049); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_NIL.add(NIL149);



                    // AST REWRITE
                    // elements: NIL
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 259:26: -> ^( TYPE NIL )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:259:29: ^( TYPE NIL )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(TYPE, "TYPE"), root_1);

                        adaptor.addChild(root_1, stream_NIL.nextNode());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "xreturns"

    public static class argtuple_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "argtuple"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:262:1: argtuple : LPAREN tupleargdefs RPAREN -> ^( TUPLETYPE tupleargdefs ) ;
    public final EulangParser.argtuple_return argtuple() throws RecognitionException {
        EulangParser.argtuple_return retval = new EulangParser.argtuple_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LPAREN150=null;
        Token RPAREN152=null;
        EulangParser.tupleargdefs_return tupleargdefs151 = null;


        CommonTree LPAREN150_tree=null;
        CommonTree RPAREN152_tree=null;
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleSubtreeStream stream_tupleargdefs=new RewriteRuleSubtreeStream(adaptor,"rule tupleargdefs");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:262:10: ( LPAREN tupleargdefs RPAREN -> ^( TUPLETYPE tupleargdefs ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:262:12: LPAREN tupleargdefs RPAREN
            {
            LPAREN150=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_argtuple2079); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN150);

            pushFollow(FOLLOW_tupleargdefs_in_argtuple2081);
            tupleargdefs151=tupleargdefs();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_tupleargdefs.add(tupleargdefs151.getTree());
            RPAREN152=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_argtuple2083); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN152);



            // AST REWRITE
            // elements: tupleargdefs
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 262:42: -> ^( TUPLETYPE tupleargdefs )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:262:45: ^( TUPLETYPE tupleargdefs )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(TUPLETYPE, "TUPLETYPE"), root_1);

                adaptor.addChild(root_1, stream_tupleargdefs.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "argtuple"

    public static class tupleargdefs_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "tupleargdefs"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:265:1: tupleargdefs : ( tupleargdef ( COMMA tupleargdef )+ ) -> ( tupleargdef )* ;
    public final EulangParser.tupleargdefs_return tupleargdefs() throws RecognitionException {
        EulangParser.tupleargdefs_return retval = new EulangParser.tupleargdefs_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COMMA154=null;
        EulangParser.tupleargdef_return tupleargdef153 = null;

        EulangParser.tupleargdef_return tupleargdef155 = null;


        CommonTree COMMA154_tree=null;
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleSubtreeStream stream_tupleargdef=new RewriteRuleSubtreeStream(adaptor,"rule tupleargdef");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:265:13: ( ( tupleargdef ( COMMA tupleargdef )+ ) -> ( tupleargdef )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:265:15: ( tupleargdef ( COMMA tupleargdef )+ )
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:265:15: ( tupleargdef ( COMMA tupleargdef )+ )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:265:16: tupleargdef ( COMMA tupleargdef )+
            {
            pushFollow(FOLLOW_tupleargdef_in_tupleargdefs2105);
            tupleargdef153=tupleargdef();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_tupleargdef.add(tupleargdef153.getTree());
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:265:28: ( COMMA tupleargdef )+
            int cnt48=0;
            loop48:
            do {
                int alt48=2;
                int LA48_0 = input.LA(1);

                if ( (LA48_0==COMMA) ) {
                    alt48=1;
                }


                switch (alt48) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:265:30: COMMA tupleargdef
            	    {
            	    COMMA154=(Token)match(input,COMMA,FOLLOW_COMMA_in_tupleargdefs2109); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA154);

            	    pushFollow(FOLLOW_tupleargdef_in_tupleargdefs2111);
            	    tupleargdef155=tupleargdef();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_tupleargdef.add(tupleargdef155.getTree());

            	    }
            	    break;

            	default :
            	    if ( cnt48 >= 1 ) break loop48;
            	    if (state.backtracking>0) {state.failed=true; return retval;}
                        EarlyExitException eee =
                            new EarlyExitException(48, input);
                        throw eee;
                }
                cnt48++;
            } while (true);


            }



            // AST REWRITE
            // elements: tupleargdef
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 265:75: -> ( tupleargdef )*
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:265:78: ( tupleargdef )*
                while ( stream_tupleargdef.hasNext() ) {
                    adaptor.addChild(root_0, stream_tupleargdef.nextTree());

                }
                stream_tupleargdef.reset();

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "tupleargdefs"

    public static class tupleargdef_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "tupleargdef"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:268:1: tupleargdef : ( type -> type | QUESTION -> ^( TYPE NIL ) | -> ^( TYPE NIL ) );
    public final EulangParser.tupleargdef_return tupleargdef() throws RecognitionException {
        EulangParser.tupleargdef_return retval = new EulangParser.tupleargdef_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token QUESTION157=null;
        EulangParser.type_return type156 = null;


        CommonTree QUESTION157_tree=null;
        RewriteRuleTokenStream stream_QUESTION=new RewriteRuleTokenStream(adaptor,"token QUESTION");
        RewriteRuleSubtreeStream stream_type=new RewriteRuleSubtreeStream(adaptor,"rule type");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:268:12: ( type -> type | QUESTION -> ^( TYPE NIL ) | -> ^( TYPE NIL ) )
            int alt49=3;
            switch ( input.LA(1) ) {
            case CODE:
            case ID:
            case COLON:
            case LPAREN:
            case COLONS:
            case DATA:
                {
                alt49=1;
                }
                break;
            case QUESTION:
                {
                alt49=2;
                }
                break;
            case COMMA:
            case RPAREN:
                {
                alt49=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 49, 0, input);

                throw nvae;
            }

            switch (alt49) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:268:14: type
                    {
                    pushFollow(FOLLOW_type_in_tupleargdef2156);
                    type156=type();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_type.add(type156.getTree());


                    // AST REWRITE
                    // elements: type
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 268:22: -> type
                    {
                        adaptor.addChild(root_0, stream_type.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:269:5: QUESTION
                    {
                    QUESTION157=(Token)match(input,QUESTION,FOLLOW_QUESTION_in_tupleargdef2169); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_QUESTION.add(QUESTION157);



                    // AST REWRITE
                    // elements: 
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 269:21: -> ^( TYPE NIL )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:269:24: ^( TYPE NIL )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(TYPE, "TYPE"), root_1);

                        adaptor.addChild(root_1, (CommonTree)adaptor.create(NIL, "NIL"));

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 3 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:270:21: 
                    {

                    // AST REWRITE
                    // elements: 
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 270:21: -> ^( TYPE NIL )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:270:24: ^( TYPE NIL )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(TYPE, "TYPE"), root_1);

                        adaptor.addChild(root_1, (CommonTree)adaptor.create(NIL, "NIL"));

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "tupleargdef"

    public static class type_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "type"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:276:1: type : ( nonArrayType -> nonArrayType ) ( ( ( ( arraySuff )+ )=> ( arraySuff )+ -> ^( TYPE ^( ARRAY $type ( arraySuff )+ ) ) ) | ( LBRACKET rhsExpr ( COMMA rhsExpr )+ RBRACKET -> ^( TYPE ^( ARRAY $type ( rhsExpr )+ ) ) ) | ( CARET -> ^( TYPE ^( POINTER $type) ) ) )* ;
    public final EulangParser.type_return type() throws RecognitionException {
        EulangParser.type_return retval = new EulangParser.type_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LBRACKET160=null;
        Token COMMA162=null;
        Token RBRACKET164=null;
        Token CARET165=null;
        EulangParser.nonArrayType_return nonArrayType158 = null;

        EulangParser.arraySuff_return arraySuff159 = null;

        EulangParser.rhsExpr_return rhsExpr161 = null;

        EulangParser.rhsExpr_return rhsExpr163 = null;


        CommonTree LBRACKET160_tree=null;
        CommonTree COMMA162_tree=null;
        CommonTree RBRACKET164_tree=null;
        CommonTree CARET165_tree=null;
        RewriteRuleTokenStream stream_LBRACKET=new RewriteRuleTokenStream(adaptor,"token LBRACKET");
        RewriteRuleTokenStream stream_RBRACKET=new RewriteRuleTokenStream(adaptor,"token RBRACKET");
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleTokenStream stream_CARET=new RewriteRuleTokenStream(adaptor,"token CARET");
        RewriteRuleSubtreeStream stream_arraySuff=new RewriteRuleSubtreeStream(adaptor,"rule arraySuff");
        RewriteRuleSubtreeStream stream_rhsExpr=new RewriteRuleSubtreeStream(adaptor,"rule rhsExpr");
        RewriteRuleSubtreeStream stream_nonArrayType=new RewriteRuleSubtreeStream(adaptor,"rule nonArrayType");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:276:6: ( ( nonArrayType -> nonArrayType ) ( ( ( ( arraySuff )+ )=> ( arraySuff )+ -> ^( TYPE ^( ARRAY $type ( arraySuff )+ ) ) ) | ( LBRACKET rhsExpr ( COMMA rhsExpr )+ RBRACKET -> ^( TYPE ^( ARRAY $type ( rhsExpr )+ ) ) ) | ( CARET -> ^( TYPE ^( POINTER $type) ) ) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:277:5: ( nonArrayType -> nonArrayType ) ( ( ( ( arraySuff )+ )=> ( arraySuff )+ -> ^( TYPE ^( ARRAY $type ( arraySuff )+ ) ) ) | ( LBRACKET rhsExpr ( COMMA rhsExpr )+ RBRACKET -> ^( TYPE ^( ARRAY $type ( rhsExpr )+ ) ) ) | ( CARET -> ^( TYPE ^( POINTER $type) ) ) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:277:5: ( nonArrayType -> nonArrayType )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:277:6: nonArrayType
            {
            pushFollow(FOLLOW_nonArrayType_in_type2234);
            nonArrayType158=nonArrayType();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_nonArrayType.add(nonArrayType158.getTree());


            // AST REWRITE
            // elements: nonArrayType
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 277:19: -> nonArrayType
            {
                adaptor.addChild(root_0, stream_nonArrayType.nextTree());

            }

            retval.tree = root_0;}
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:278:6: ( ( ( ( arraySuff )+ )=> ( arraySuff )+ -> ^( TYPE ^( ARRAY $type ( arraySuff )+ ) ) ) | ( LBRACKET rhsExpr ( COMMA rhsExpr )+ RBRACKET -> ^( TYPE ^( ARRAY $type ( rhsExpr )+ ) ) ) | ( CARET -> ^( TYPE ^( POINTER $type) ) ) )*
            loop52:
            do {
                int alt52=4;
                alt52 = dfa52.predict(input);
                switch (alt52) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:279:7: ( ( ( arraySuff )+ )=> ( arraySuff )+ -> ^( TYPE ^( ARRAY $type ( arraySuff )+ ) ) )
            	    {
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:279:7: ( ( ( arraySuff )+ )=> ( arraySuff )+ -> ^( TYPE ^( ARRAY $type ( arraySuff )+ ) ) )
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:280:9: ( ( arraySuff )+ )=> ( arraySuff )+
            	    {
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:280:25: ( arraySuff )+
            	    int cnt50=0;
            	    loop50:
            	    do {
            	        int alt50=2;
            	        int LA50_0 = input.LA(1);

            	        if ( (LA50_0==LBRACKET) ) {
            	            alt50=1;
            	        }


            	        switch (alt50) {
            	    	case 1 :
            	    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:280:25: arraySuff
            	    	    {
            	    	    pushFollow(FOLLOW_arraySuff_in_type2272);
            	    	    arraySuff159=arraySuff();

            	    	    state._fsp--;
            	    	    if (state.failed) return retval;
            	    	    if ( state.backtracking==0 ) stream_arraySuff.add(arraySuff159.getTree());

            	    	    }
            	    	    break;

            	    	default :
            	    	    if ( cnt50 >= 1 ) break loop50;
            	    	    if (state.backtracking>0) {state.failed=true; return retval;}
            	                EarlyExitException eee =
            	                    new EarlyExitException(50, input);
            	                throw eee;
            	        }
            	        cnt50++;
            	    } while (true);



            	    // AST REWRITE
            	    // elements: arraySuff, type
            	    // token labels: 
            	    // rule labels: retval
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 280:36: -> ^( TYPE ^( ARRAY $type ( arraySuff )+ ) )
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:280:39: ^( TYPE ^( ARRAY $type ( arraySuff )+ ) )
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(TYPE, "TYPE"), root_1);

            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:280:46: ^( ARRAY $type ( arraySuff )+ )
            	        {
            	        CommonTree root_2 = (CommonTree)adaptor.nil();
            	        root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ARRAY, "ARRAY"), root_2);

            	        adaptor.addChild(root_2, stream_retval.nextTree());
            	        if ( !(stream_arraySuff.hasNext()) ) {
            	            throw new RewriteEarlyExitException();
            	        }
            	        while ( stream_arraySuff.hasNext() ) {
            	            adaptor.addChild(root_2, stream_arraySuff.nextTree());

            	        }
            	        stream_arraySuff.reset();

            	        adaptor.addChild(root_1, root_2);
            	        }

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }


            	    }
            	    break;
            	case 2 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:283:8: ( LBRACKET rhsExpr ( COMMA rhsExpr )+ RBRACKET -> ^( TYPE ^( ARRAY $type ( rhsExpr )+ ) ) )
            	    {
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:283:8: ( LBRACKET rhsExpr ( COMMA rhsExpr )+ RBRACKET -> ^( TYPE ^( ARRAY $type ( rhsExpr )+ ) ) )
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:284:9: LBRACKET rhsExpr ( COMMA rhsExpr )+ RBRACKET
            	    {
            	    LBRACKET160=(Token)match(input,LBRACKET,FOLLOW_LBRACKET_in_type2327); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_LBRACKET.add(LBRACKET160);

            	    pushFollow(FOLLOW_rhsExpr_in_type2329);
            	    rhsExpr161=rhsExpr();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_rhsExpr.add(rhsExpr161.getTree());
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:284:26: ( COMMA rhsExpr )+
            	    int cnt51=0;
            	    loop51:
            	    do {
            	        int alt51=2;
            	        int LA51_0 = input.LA(1);

            	        if ( (LA51_0==COMMA) ) {
            	            alt51=1;
            	        }


            	        switch (alt51) {
            	    	case 1 :
            	    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:284:28: COMMA rhsExpr
            	    	    {
            	    	    COMMA162=(Token)match(input,COMMA,FOLLOW_COMMA_in_type2333); if (state.failed) return retval; 
            	    	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA162);

            	    	    pushFollow(FOLLOW_rhsExpr_in_type2335);
            	    	    rhsExpr163=rhsExpr();

            	    	    state._fsp--;
            	    	    if (state.failed) return retval;
            	    	    if ( state.backtracking==0 ) stream_rhsExpr.add(rhsExpr163.getTree());

            	    	    }
            	    	    break;

            	    	default :
            	    	    if ( cnt51 >= 1 ) break loop51;
            	    	    if (state.backtracking>0) {state.failed=true; return retval;}
            	                EarlyExitException eee =
            	                    new EarlyExitException(51, input);
            	                throw eee;
            	        }
            	        cnt51++;
            	    } while (true);

            	    RBRACKET164=(Token)match(input,RBRACKET,FOLLOW_RBRACKET_in_type2340); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_RBRACKET.add(RBRACKET164);



            	    // AST REWRITE
            	    // elements: rhsExpr, type
            	    // token labels: 
            	    // rule labels: retval
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 284:54: -> ^( TYPE ^( ARRAY $type ( rhsExpr )+ ) )
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:284:57: ^( TYPE ^( ARRAY $type ( rhsExpr )+ ) )
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(TYPE, "TYPE"), root_1);

            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:284:64: ^( ARRAY $type ( rhsExpr )+ )
            	        {
            	        CommonTree root_2 = (CommonTree)adaptor.nil();
            	        root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ARRAY, "ARRAY"), root_2);

            	        adaptor.addChild(root_2, stream_retval.nextTree());
            	        if ( !(stream_rhsExpr.hasNext()) ) {
            	            throw new RewriteEarlyExitException();
            	        }
            	        while ( stream_rhsExpr.hasNext() ) {
            	            adaptor.addChild(root_2, stream_rhsExpr.nextTree());

            	        }
            	        stream_rhsExpr.reset();

            	        adaptor.addChild(root_1, root_2);
            	        }

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }


            	    }
            	    break;
            	case 3 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:287:8: ( CARET -> ^( TYPE ^( POINTER $type) ) )
            	    {
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:287:8: ( CARET -> ^( TYPE ^( POINTER $type) ) )
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:288:10: CARET
            	    {
            	    CARET165=(Token)match(input,CARET,FOLLOW_CARET_in_type2399); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_CARET.add(CARET165);



            	    // AST REWRITE
            	    // elements: type
            	    // token labels: 
            	    // rule labels: retval
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 288:16: -> ^( TYPE ^( POINTER $type) )
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:288:19: ^( TYPE ^( POINTER $type) )
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(TYPE, "TYPE"), root_1);

            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:288:26: ^( POINTER $type)
            	        {
            	        CommonTree root_2 = (CommonTree)adaptor.nil();
            	        root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(POINTER, "POINTER"), root_2);

            	        adaptor.addChild(root_2, stream_retval.nextTree());

            	        adaptor.addChild(root_1, root_2);
            	        }

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }


            	    }
            	    break;

            	default :
            	    break loop52;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "type"

    public static class nonArrayType_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "nonArrayType"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:293:1: nonArrayType : ( ( idExpr -> ^( TYPE idExpr ) ) | ( CODE ( proto )? -> ^( TYPE ^( CODE ( proto )? ) ) ) | data -> ^( TYPE data ) | argtuple );
    public final EulangParser.nonArrayType_return nonArrayType() throws RecognitionException {
        EulangParser.nonArrayType_return retval = new EulangParser.nonArrayType_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token CODE167=null;
        EulangParser.idExpr_return idExpr166 = null;

        EulangParser.proto_return proto168 = null;

        EulangParser.data_return data169 = null;

        EulangParser.argtuple_return argtuple170 = null;


        CommonTree CODE167_tree=null;
        RewriteRuleTokenStream stream_CODE=new RewriteRuleTokenStream(adaptor,"token CODE");
        RewriteRuleSubtreeStream stream_idExpr=new RewriteRuleSubtreeStream(adaptor,"rule idExpr");
        RewriteRuleSubtreeStream stream_data=new RewriteRuleSubtreeStream(adaptor,"rule data");
        RewriteRuleSubtreeStream stream_proto=new RewriteRuleSubtreeStream(adaptor,"rule proto");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:293:14: ( ( idExpr -> ^( TYPE idExpr ) ) | ( CODE ( proto )? -> ^( TYPE ^( CODE ( proto )? ) ) ) | data -> ^( TYPE data ) | argtuple )
            int alt54=4;
            switch ( input.LA(1) ) {
            case ID:
            case COLON:
            case COLONS:
                {
                alt54=1;
                }
                break;
            case CODE:
                {
                alt54=2;
                }
                break;
            case DATA:
                {
                alt54=3;
                }
                break;
            case LPAREN:
                {
                alt54=4;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 54, 0, input);

                throw nvae;
            }

            switch (alt54) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:294:4: ( idExpr -> ^( TYPE idExpr ) )
                    {
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:294:4: ( idExpr -> ^( TYPE idExpr ) )
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:294:6: idExpr
                    {
                    pushFollow(FOLLOW_idExpr_in_nonArrayType2451);
                    idExpr166=idExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_idExpr.add(idExpr166.getTree());


                    // AST REWRITE
                    // elements: idExpr
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 294:13: -> ^( TYPE idExpr )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:294:16: ^( TYPE idExpr )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(TYPE, "TYPE"), root_1);

                        adaptor.addChild(root_1, stream_idExpr.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }


                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:295:5: ( CODE ( proto )? -> ^( TYPE ^( CODE ( proto )? ) ) )
                    {
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:295:5: ( CODE ( proto )? -> ^( TYPE ^( CODE ( proto )? ) ) )
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:295:7: CODE ( proto )?
                    {
                    CODE167=(Token)match(input,CODE,FOLLOW_CODE_in_nonArrayType2469); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_CODE.add(CODE167);

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:295:12: ( proto )?
                    int alt53=2;
                    int LA53_0 = input.LA(1);

                    if ( (LA53_0==LPAREN) ) {
                        alt53=1;
                    }
                    switch (alt53) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:295:12: proto
                            {
                            pushFollow(FOLLOW_proto_in_nonArrayType2471);
                            proto168=proto();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_proto.add(proto168.getTree());

                            }
                            break;

                    }



                    // AST REWRITE
                    // elements: CODE, proto
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 295:19: -> ^( TYPE ^( CODE ( proto )? ) )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:295:22: ^( TYPE ^( CODE ( proto )? ) )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(TYPE, "TYPE"), root_1);

                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:295:29: ^( CODE ( proto )? )
                        {
                        CommonTree root_2 = (CommonTree)adaptor.nil();
                        root_2 = (CommonTree)adaptor.becomeRoot(stream_CODE.nextNode(), root_2);

                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:295:36: ( proto )?
                        if ( stream_proto.hasNext() ) {
                            adaptor.addChild(root_2, stream_proto.nextTree());

                        }
                        stream_proto.reset();

                        adaptor.addChild(root_1, root_2);
                        }

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }


                    }
                    break;
                case 3 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:296:5: data
                    {
                    pushFollow(FOLLOW_data_in_nonArrayType2494);
                    data169=data();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_data.add(data169.getTree());


                    // AST REWRITE
                    // elements: data
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 296:10: -> ^( TYPE data )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:296:13: ^( TYPE data )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(TYPE, "TYPE"), root_1);

                        adaptor.addChild(root_1, stream_data.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 4 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:297:5: argtuple
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_argtuple_in_nonArrayType2510);
                    argtuple170=argtuple();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, argtuple170.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "nonArrayType"

    public static class arraySuff_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "arraySuff"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:299:1: arraySuff : ( LBRACKET rhsExpr RBRACKET -> rhsExpr | LBRACKET RBRACKET -> FALSE );
    public final EulangParser.arraySuff_return arraySuff() throws RecognitionException {
        EulangParser.arraySuff_return retval = new EulangParser.arraySuff_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LBRACKET171=null;
        Token RBRACKET173=null;
        Token LBRACKET174=null;
        Token RBRACKET175=null;
        EulangParser.rhsExpr_return rhsExpr172 = null;


        CommonTree LBRACKET171_tree=null;
        CommonTree RBRACKET173_tree=null;
        CommonTree LBRACKET174_tree=null;
        CommonTree RBRACKET175_tree=null;
        RewriteRuleTokenStream stream_LBRACKET=new RewriteRuleTokenStream(adaptor,"token LBRACKET");
        RewriteRuleTokenStream stream_RBRACKET=new RewriteRuleTokenStream(adaptor,"token RBRACKET");
        RewriteRuleSubtreeStream stream_rhsExpr=new RewriteRuleSubtreeStream(adaptor,"rule rhsExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:299:11: ( LBRACKET rhsExpr RBRACKET -> rhsExpr | LBRACKET RBRACKET -> FALSE )
            int alt55=2;
            int LA55_0 = input.LA(1);

            if ( (LA55_0==LBRACKET) ) {
                int LA55_1 = input.LA(2);

                if ( (LA55_1==RBRACKET) ) {
                    alt55=2;
                }
                else if ( (LA55_1==CODE||(LA55_1>=SIZEOF && LA55_1<=TYPEOF)||LA55_1==ID||LA55_1==COLON||LA55_1==LPAREN||LA55_1==NIL||LA55_1==IF||LA55_1==NOT||(LA55_1>=TILDE && LA55_1<=AMP)||LA55_1==MINUS||(LA55_1>=PLUSPLUS && LA55_1<=STRING_LITERAL)||(LA55_1>=FALSE && LA55_1<=COLONS)) ) {
                    alt55=1;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 55, 1, input);

                    throw nvae;
                }
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 55, 0, input);

                throw nvae;
            }
            switch (alt55) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:299:13: LBRACKET rhsExpr RBRACKET
                    {
                    LBRACKET171=(Token)match(input,LBRACKET,FOLLOW_LBRACKET_in_arraySuff2526); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LBRACKET.add(LBRACKET171);

                    pushFollow(FOLLOW_rhsExpr_in_arraySuff2528);
                    rhsExpr172=rhsExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_rhsExpr.add(rhsExpr172.getTree());
                    RBRACKET173=(Token)match(input,RBRACKET,FOLLOW_RBRACKET_in_arraySuff2530); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RBRACKET.add(RBRACKET173);



                    // AST REWRITE
                    // elements: rhsExpr
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 299:39: -> rhsExpr
                    {
                        adaptor.addChild(root_0, stream_rhsExpr.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:300:7: LBRACKET RBRACKET
                    {
                    LBRACKET174=(Token)match(input,LBRACKET,FOLLOW_LBRACKET_in_arraySuff2542); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LBRACKET.add(LBRACKET174);

                    RBRACKET175=(Token)match(input,RBRACKET,FOLLOW_RBRACKET_in_arraySuff2544); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RBRACKET.add(RBRACKET175);



                    // AST REWRITE
                    // elements: 
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 300:25: -> FALSE
                    {
                        adaptor.addChild(root_0, (CommonTree)adaptor.create(FALSE, "FALSE"));

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "arraySuff"

    public static class codestmtlist_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "codestmtlist"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:302:1: codestmtlist : ( codeStmt ( SEMI ( codeStmt )? )* -> ^( STMTLIST ( codeStmt )* ) | -> ^( STMTLIST ) );
    public final EulangParser.codestmtlist_return codestmtlist() throws RecognitionException {
        EulangParser.codestmtlist_return retval = new EulangParser.codestmtlist_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token SEMI177=null;
        EulangParser.codeStmt_return codeStmt176 = null;

        EulangParser.codeStmt_return codeStmt178 = null;


        CommonTree SEMI177_tree=null;
        RewriteRuleTokenStream stream_SEMI=new RewriteRuleTokenStream(adaptor,"token SEMI");
        RewriteRuleSubtreeStream stream_codeStmt=new RewriteRuleSubtreeStream(adaptor,"rule codeStmt");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:302:13: ( codeStmt ( SEMI ( codeStmt )? )* -> ^( STMTLIST ( codeStmt )* ) | -> ^( STMTLIST ) )
            int alt58=2;
            int LA58_0 = input.LA(1);

            if ( (LA58_0==CODE||LA58_0==GOTO||(LA58_0>=SIZEOF && LA58_0<=TYPEOF)||LA58_0==ID||LA58_0==COLON||LA58_0==LBRACE||LA58_0==FOR||LA58_0==LPAREN||LA58_0==NIL||(LA58_0>=DO && LA58_0<=REPEAT)||(LA58_0>=ATSIGN && LA58_0<=IF)||LA58_0==NOT||(LA58_0>=TILDE && LA58_0<=AMP)||LA58_0==MINUS||(LA58_0>=PLUSPLUS && LA58_0<=STRING_LITERAL)||(LA58_0>=FALSE && LA58_0<=COLONS)) ) {
                alt58=1;
            }
            else if ( (LA58_0==RBRACE) ) {
                alt58=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 58, 0, input);

                throw nvae;
            }
            switch (alt58) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:302:16: codeStmt ( SEMI ( codeStmt )? )*
                    {
                    pushFollow(FOLLOW_codeStmt_in_codestmtlist2560);
                    codeStmt176=codeStmt();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_codeStmt.add(codeStmt176.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:302:25: ( SEMI ( codeStmt )? )*
                    loop57:
                    do {
                        int alt57=2;
                        int LA57_0 = input.LA(1);

                        if ( (LA57_0==SEMI) ) {
                            alt57=1;
                        }


                        switch (alt57) {
                    	case 1 :
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:302:26: SEMI ( codeStmt )?
                    	    {
                    	    SEMI177=(Token)match(input,SEMI,FOLLOW_SEMI_in_codestmtlist2563); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_SEMI.add(SEMI177);

                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:302:31: ( codeStmt )?
                    	    int alt56=2;
                    	    int LA56_0 = input.LA(1);

                    	    if ( (LA56_0==CODE||LA56_0==GOTO||(LA56_0>=SIZEOF && LA56_0<=TYPEOF)||LA56_0==ID||LA56_0==COLON||LA56_0==LBRACE||LA56_0==FOR||LA56_0==LPAREN||LA56_0==NIL||(LA56_0>=DO && LA56_0<=REPEAT)||(LA56_0>=ATSIGN && LA56_0<=IF)||LA56_0==NOT||(LA56_0>=TILDE && LA56_0<=AMP)||LA56_0==MINUS||(LA56_0>=PLUSPLUS && LA56_0<=STRING_LITERAL)||(LA56_0>=FALSE && LA56_0<=COLONS)) ) {
                    	        alt56=1;
                    	    }
                    	    switch (alt56) {
                    	        case 1 :
                    	            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:302:31: codeStmt
                    	            {
                    	            pushFollow(FOLLOW_codeStmt_in_codestmtlist2565);
                    	            codeStmt178=codeStmt();

                    	            state._fsp--;
                    	            if (state.failed) return retval;
                    	            if ( state.backtracking==0 ) stream_codeStmt.add(codeStmt178.getTree());

                    	            }
                    	            break;

                    	    }


                    	    }
                    	    break;

                    	default :
                    	    break loop57;
                        }
                    } while (true);



                    // AST REWRITE
                    // elements: codeStmt
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 302:44: -> ^( STMTLIST ( codeStmt )* )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:302:48: ^( STMTLIST ( codeStmt )* )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(STMTLIST, "STMTLIST"), root_1);

                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:302:59: ( codeStmt )*
                        while ( stream_codeStmt.hasNext() ) {
                            adaptor.addChild(root_1, stream_codeStmt.nextTree());

                        }
                        stream_codeStmt.reset();

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:303:7: 
                    {

                    // AST REWRITE
                    // elements: 
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 303:7: -> ^( STMTLIST )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:303:10: ^( STMTLIST )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(STMTLIST, "STMTLIST"), root_1);

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "codestmtlist"

    public static class codeStmt_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "codeStmt"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:306:1: codeStmt : ( labelStmt codeStmtExpr -> ^( LABELSTMT labelStmt codeStmtExpr ) | codeStmtExpr -> codeStmtExpr );
    public final EulangParser.codeStmt_return codeStmt() throws RecognitionException {
        EulangParser.codeStmt_return retval = new EulangParser.codeStmt_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.labelStmt_return labelStmt179 = null;

        EulangParser.codeStmtExpr_return codeStmtExpr180 = null;

        EulangParser.codeStmtExpr_return codeStmtExpr181 = null;


        RewriteRuleSubtreeStream stream_labelStmt=new RewriteRuleSubtreeStream(adaptor,"rule labelStmt");
        RewriteRuleSubtreeStream stream_codeStmtExpr=new RewriteRuleSubtreeStream(adaptor,"rule codeStmtExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:306:10: ( labelStmt codeStmtExpr -> ^( LABELSTMT labelStmt codeStmtExpr ) | codeStmtExpr -> codeStmtExpr )
            int alt59=2;
            int LA59_0 = input.LA(1);

            if ( (LA59_0==ATSIGN) ) {
                alt59=1;
            }
            else if ( (LA59_0==CODE||LA59_0==GOTO||(LA59_0>=SIZEOF && LA59_0<=TYPEOF)||LA59_0==ID||LA59_0==COLON||LA59_0==LBRACE||LA59_0==FOR||LA59_0==LPAREN||LA59_0==NIL||(LA59_0>=DO && LA59_0<=REPEAT)||LA59_0==IF||LA59_0==NOT||(LA59_0>=TILDE && LA59_0<=AMP)||LA59_0==MINUS||(LA59_0>=PLUSPLUS && LA59_0<=STRING_LITERAL)||(LA59_0>=FALSE && LA59_0<=COLONS)) ) {
                alt59=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 59, 0, input);

                throw nvae;
            }
            switch (alt59) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:306:12: labelStmt codeStmtExpr
                    {
                    pushFollow(FOLLOW_labelStmt_in_codeStmt2609);
                    labelStmt179=labelStmt();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_labelStmt.add(labelStmt179.getTree());
                    pushFollow(FOLLOW_codeStmtExpr_in_codeStmt2611);
                    codeStmtExpr180=codeStmtExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_codeStmtExpr.add(codeStmtExpr180.getTree());


                    // AST REWRITE
                    // elements: labelStmt, codeStmtExpr
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 306:36: -> ^( LABELSTMT labelStmt codeStmtExpr )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:306:39: ^( LABELSTMT labelStmt codeStmtExpr )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(LABELSTMT, "LABELSTMT"), root_1);

                        adaptor.addChild(root_1, stream_labelStmt.nextTree());
                        adaptor.addChild(root_1, stream_codeStmtExpr.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:307:9: codeStmtExpr
                    {
                    pushFollow(FOLLOW_codeStmtExpr_in_codeStmt2632);
                    codeStmtExpr181=codeStmtExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_codeStmtExpr.add(codeStmtExpr181.getTree());


                    // AST REWRITE
                    // elements: codeStmtExpr
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 307:22: -> codeStmtExpr
                    {
                        adaptor.addChild(root_0, stream_codeStmtExpr.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "codeStmt"

    public static class codeStmtExpr_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "codeStmtExpr"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:310:1: codeStmtExpr : ( ( varDecl )=> varDecl -> varDecl | ( assignStmt )=> assignStmt -> assignStmt | rhsExpr -> ^( STMTEXPR rhsExpr ) | ( LBRACE )=> blockStmt -> blockStmt | gotoStmt -> gotoStmt | controlStmt -> controlStmt );
    public final EulangParser.codeStmtExpr_return codeStmtExpr() throws RecognitionException {
        EulangParser.codeStmtExpr_return retval = new EulangParser.codeStmtExpr_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.varDecl_return varDecl182 = null;

        EulangParser.assignStmt_return assignStmt183 = null;

        EulangParser.rhsExpr_return rhsExpr184 = null;

        EulangParser.blockStmt_return blockStmt185 = null;

        EulangParser.gotoStmt_return gotoStmt186 = null;

        EulangParser.controlStmt_return controlStmt187 = null;


        RewriteRuleSubtreeStream stream_assignStmt=new RewriteRuleSubtreeStream(adaptor,"rule assignStmt");
        RewriteRuleSubtreeStream stream_gotoStmt=new RewriteRuleSubtreeStream(adaptor,"rule gotoStmt");
        RewriteRuleSubtreeStream stream_blockStmt=new RewriteRuleSubtreeStream(adaptor,"rule blockStmt");
        RewriteRuleSubtreeStream stream_varDecl=new RewriteRuleSubtreeStream(adaptor,"rule varDecl");
        RewriteRuleSubtreeStream stream_rhsExpr=new RewriteRuleSubtreeStream(adaptor,"rule rhsExpr");
        RewriteRuleSubtreeStream stream_controlStmt=new RewriteRuleSubtreeStream(adaptor,"rule controlStmt");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:310:14: ( ( varDecl )=> varDecl -> varDecl | ( assignStmt )=> assignStmt -> assignStmt | rhsExpr -> ^( STMTEXPR rhsExpr ) | ( LBRACE )=> blockStmt -> blockStmt | gotoStmt -> gotoStmt | controlStmt -> controlStmt )
            int alt60=6;
            alt60 = dfa60.predict(input);
            switch (alt60) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:311:7: ( varDecl )=> varDecl
                    {
                    pushFollow(FOLLOW_varDecl_in_codeStmtExpr2664);
                    varDecl182=varDecl();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_varDecl.add(varDecl182.getTree());


                    // AST REWRITE
                    // elements: varDecl
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 311:32: -> varDecl
                    {
                        adaptor.addChild(root_0, stream_varDecl.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:312:9: ( assignStmt )=> assignStmt
                    {
                    pushFollow(FOLLOW_assignStmt_in_codeStmtExpr2687);
                    assignStmt183=assignStmt();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_assignStmt.add(assignStmt183.getTree());


                    // AST REWRITE
                    // elements: assignStmt
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 312:39: -> assignStmt
                    {
                        adaptor.addChild(root_0, stream_assignStmt.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 3 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:313:9: rhsExpr
                    {
                    pushFollow(FOLLOW_rhsExpr_in_codeStmtExpr2704);
                    rhsExpr184=rhsExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_rhsExpr.add(rhsExpr184.getTree());


                    // AST REWRITE
                    // elements: rhsExpr
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 313:23: -> ^( STMTEXPR rhsExpr )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:313:27: ^( STMTEXPR rhsExpr )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(STMTEXPR, "STMTEXPR"), root_1);

                        adaptor.addChild(root_1, stream_rhsExpr.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 4 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:314:9: ( LBRACE )=> blockStmt
                    {
                    pushFollow(FOLLOW_blockStmt_in_codeStmtExpr2737);
                    blockStmt185=blockStmt();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_blockStmt.add(blockStmt185.getTree());


                    // AST REWRITE
                    // elements: blockStmt
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 314:41: -> blockStmt
                    {
                        adaptor.addChild(root_0, stream_blockStmt.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 5 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:315:9: gotoStmt
                    {
                    pushFollow(FOLLOW_gotoStmt_in_codeStmtExpr2759);
                    gotoStmt186=gotoStmt();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_gotoStmt.add(gotoStmt186.getTree());


                    // AST REWRITE
                    // elements: gotoStmt
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 315:23: -> gotoStmt
                    {
                        adaptor.addChild(root_0, stream_gotoStmt.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 6 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:317:9: controlStmt
                    {
                    pushFollow(FOLLOW_controlStmt_in_codeStmtExpr2785);
                    controlStmt187=controlStmt();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_controlStmt.add(controlStmt187.getTree());


                    // AST REWRITE
                    // elements: controlStmt
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 317:26: -> controlStmt
                    {
                        adaptor.addChild(root_0, stream_controlStmt.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "codeStmtExpr"

    public static class varDecl_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "varDecl"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:320:1: varDecl : ( singleVarDecl | tupleVarDecl );
    public final EulangParser.varDecl_return varDecl() throws RecognitionException {
        EulangParser.varDecl_return retval = new EulangParser.varDecl_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.singleVarDecl_return singleVarDecl188 = null;

        EulangParser.tupleVarDecl_return tupleVarDecl189 = null;



        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:320:8: ( singleVarDecl | tupleVarDecl )
            int alt61=2;
            int LA61_0 = input.LA(1);

            if ( (LA61_0==ID) ) {
                alt61=1;
            }
            else if ( (LA61_0==LPAREN) ) {
                alt61=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 61, 0, input);

                throw nvae;
            }
            switch (alt61) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:320:10: singleVarDecl
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_singleVarDecl_in_varDecl2808);
                    singleVarDecl188=singleVarDecl();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, singleVarDecl188.getTree());

                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:320:26: tupleVarDecl
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_tupleVarDecl_in_varDecl2812);
                    tupleVarDecl189=tupleVarDecl();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, tupleVarDecl189.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "varDecl"

    public static class singleVarDecl_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "singleVarDecl"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:322:1: singleVarDecl : ID ( ( ( attrs )? COLON_EQUALS assignOrInitExpr -> ^( ALLOC ( attrs )? TYPE ID assignOrInitExpr ) ) | ( ( attrs )? COLON type ( EQUALS assignOrInitExpr )? -> ^( ALLOC ( attrs )? type ID ( assignOrInitExpr )* ) ) | ( COMMA ID )+ ( attrs )? ( ( COLON_EQUALS ( PLUS )? assignOrInitExpr ( COMMA assignOrInitExpr )* ) -> ^( ALLOC ( attrs )? TYPE ^( LIST ( ID )+ ) ( PLUS )? ^( LIST ( assignOrInitExpr )+ ) ) | ( COLON type ( EQUALS ( PLUS )? assignOrInitExpr ( COMMA assignOrInitExpr )* )? ) -> ^( ALLOC ( attrs )? type ^( LIST ( ID )+ ) ( PLUS )? ( ^( LIST ( assignOrInitExpr )+ ) )? ) ) ) ;
    public final EulangParser.singleVarDecl_return singleVarDecl() throws RecognitionException {
        EulangParser.singleVarDecl_return retval = new EulangParser.singleVarDecl_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ID190=null;
        Token COLON_EQUALS192=null;
        Token COLON195=null;
        Token EQUALS197=null;
        Token COMMA199=null;
        Token ID200=null;
        Token COLON_EQUALS202=null;
        Token PLUS203=null;
        Token COMMA205=null;
        Token COLON207=null;
        Token EQUALS209=null;
        Token PLUS210=null;
        Token COMMA212=null;
        EulangParser.attrs_return attrs191 = null;

        EulangParser.assignOrInitExpr_return assignOrInitExpr193 = null;

        EulangParser.attrs_return attrs194 = null;

        EulangParser.type_return type196 = null;

        EulangParser.assignOrInitExpr_return assignOrInitExpr198 = null;

        EulangParser.attrs_return attrs201 = null;

        EulangParser.assignOrInitExpr_return assignOrInitExpr204 = null;

        EulangParser.assignOrInitExpr_return assignOrInitExpr206 = null;

        EulangParser.type_return type208 = null;

        EulangParser.assignOrInitExpr_return assignOrInitExpr211 = null;

        EulangParser.assignOrInitExpr_return assignOrInitExpr213 = null;


        CommonTree ID190_tree=null;
        CommonTree COLON_EQUALS192_tree=null;
        CommonTree COLON195_tree=null;
        CommonTree EQUALS197_tree=null;
        CommonTree COMMA199_tree=null;
        CommonTree ID200_tree=null;
        CommonTree COLON_EQUALS202_tree=null;
        CommonTree PLUS203_tree=null;
        CommonTree COMMA205_tree=null;
        CommonTree COLON207_tree=null;
        CommonTree EQUALS209_tree=null;
        CommonTree PLUS210_tree=null;
        CommonTree COMMA212_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleTokenStream stream_PLUS=new RewriteRuleTokenStream(adaptor,"token PLUS");
        RewriteRuleTokenStream stream_EQUALS=new RewriteRuleTokenStream(adaptor,"token EQUALS");
        RewriteRuleTokenStream stream_COLON_EQUALS=new RewriteRuleTokenStream(adaptor,"token COLON_EQUALS");
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleSubtreeStream stream_attrs=new RewriteRuleSubtreeStream(adaptor,"rule attrs");
        RewriteRuleSubtreeStream stream_assignOrInitExpr=new RewriteRuleSubtreeStream(adaptor,"rule assignOrInitExpr");
        RewriteRuleSubtreeStream stream_type=new RewriteRuleSubtreeStream(adaptor,"rule type");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:322:14: ( ID ( ( ( attrs )? COLON_EQUALS assignOrInitExpr -> ^( ALLOC ( attrs )? TYPE ID assignOrInitExpr ) ) | ( ( attrs )? COLON type ( EQUALS assignOrInitExpr )? -> ^( ALLOC ( attrs )? type ID ( assignOrInitExpr )* ) ) | ( COMMA ID )+ ( attrs )? ( ( COLON_EQUALS ( PLUS )? assignOrInitExpr ( COMMA assignOrInitExpr )* ) -> ^( ALLOC ( attrs )? TYPE ^( LIST ( ID )+ ) ( PLUS )? ^( LIST ( assignOrInitExpr )+ ) ) | ( COLON type ( EQUALS ( PLUS )? assignOrInitExpr ( COMMA assignOrInitExpr )* )? ) -> ^( ALLOC ( attrs )? type ^( LIST ( ID )+ ) ( PLUS )? ( ^( LIST ( assignOrInitExpr )+ ) )? ) ) ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:323:5: ID ( ( ( attrs )? COLON_EQUALS assignOrInitExpr -> ^( ALLOC ( attrs )? TYPE ID assignOrInitExpr ) ) | ( ( attrs )? COLON type ( EQUALS assignOrInitExpr )? -> ^( ALLOC ( attrs )? type ID ( assignOrInitExpr )* ) ) | ( COMMA ID )+ ( attrs )? ( ( COLON_EQUALS ( PLUS )? assignOrInitExpr ( COMMA assignOrInitExpr )* ) -> ^( ALLOC ( attrs )? TYPE ^( LIST ( ID )+ ) ( PLUS )? ^( LIST ( assignOrInitExpr )+ ) ) | ( COLON type ( EQUALS ( PLUS )? assignOrInitExpr ( COMMA assignOrInitExpr )* )? ) -> ^( ALLOC ( attrs )? type ^( LIST ( ID )+ ) ( PLUS )? ( ^( LIST ( assignOrInitExpr )+ ) )? ) ) )
            {
            ID190=(Token)match(input,ID,FOLLOW_ID_in_singleVarDecl2824); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_ID.add(ID190);

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:323:8: ( ( ( attrs )? COLON_EQUALS assignOrInitExpr -> ^( ALLOC ( attrs )? TYPE ID assignOrInitExpr ) ) | ( ( attrs )? COLON type ( EQUALS assignOrInitExpr )? -> ^( ALLOC ( attrs )? type ID ( assignOrInitExpr )* ) ) | ( COMMA ID )+ ( attrs )? ( ( COLON_EQUALS ( PLUS )? assignOrInitExpr ( COMMA assignOrInitExpr )* ) -> ^( ALLOC ( attrs )? TYPE ^( LIST ( ID )+ ) ( PLUS )? ^( LIST ( assignOrInitExpr )+ ) ) | ( COLON type ( EQUALS ( PLUS )? assignOrInitExpr ( COMMA assignOrInitExpr )* )? ) -> ^( ALLOC ( attrs )? type ^( LIST ( ID )+ ) ( PLUS )? ( ^( LIST ( assignOrInitExpr )+ ) )? ) ) )
            int alt73=3;
            alt73 = dfa73.predict(input);
            switch (alt73) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:324:9: ( ( attrs )? COLON_EQUALS assignOrInitExpr -> ^( ALLOC ( attrs )? TYPE ID assignOrInitExpr ) )
                    {
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:324:9: ( ( attrs )? COLON_EQUALS assignOrInitExpr -> ^( ALLOC ( attrs )? TYPE ID assignOrInitExpr ) )
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:324:11: ( attrs )? COLON_EQUALS assignOrInitExpr
                    {
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:324:11: ( attrs )?
                    int alt62=2;
                    int LA62_0 = input.LA(1);

                    if ( (LA62_0==ATTR) ) {
                        alt62=1;
                    }
                    switch (alt62) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:324:11: attrs
                            {
                            pushFollow(FOLLOW_attrs_in_singleVarDecl2838);
                            attrs191=attrs();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_attrs.add(attrs191.getTree());

                            }
                            break;

                    }

                    COLON_EQUALS192=(Token)match(input,COLON_EQUALS,FOLLOW_COLON_EQUALS_in_singleVarDecl2841); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COLON_EQUALS.add(COLON_EQUALS192);

                    pushFollow(FOLLOW_assignOrInitExpr_in_singleVarDecl2843);
                    assignOrInitExpr193=assignOrInitExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_assignOrInitExpr.add(assignOrInitExpr193.getTree());


                    // AST REWRITE
                    // elements: ID, assignOrInitExpr, attrs
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 324:56: -> ^( ALLOC ( attrs )? TYPE ID assignOrInitExpr )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:324:59: ^( ALLOC ( attrs )? TYPE ID assignOrInitExpr )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ALLOC, "ALLOC"), root_1);

                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:324:67: ( attrs )?
                        if ( stream_attrs.hasNext() ) {
                            adaptor.addChild(root_1, stream_attrs.nextTree());

                        }
                        stream_attrs.reset();
                        adaptor.addChild(root_1, (CommonTree)adaptor.create(TYPE, "TYPE"));
                        adaptor.addChild(root_1, stream_ID.nextNode());
                        adaptor.addChild(root_1, stream_assignOrInitExpr.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }


                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:325:9: ( ( attrs )? COLON type ( EQUALS assignOrInitExpr )? -> ^( ALLOC ( attrs )? type ID ( assignOrInitExpr )* ) )
                    {
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:325:9: ( ( attrs )? COLON type ( EQUALS assignOrInitExpr )? -> ^( ALLOC ( attrs )? type ID ( assignOrInitExpr )* ) )
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:325:11: ( attrs )? COLON type ( EQUALS assignOrInitExpr )?
                    {
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:325:11: ( attrs )?
                    int alt63=2;
                    int LA63_0 = input.LA(1);

                    if ( (LA63_0==ATTR) ) {
                        alt63=1;
                    }
                    switch (alt63) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:325:11: attrs
                            {
                            pushFollow(FOLLOW_attrs_in_singleVarDecl2880);
                            attrs194=attrs();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_attrs.add(attrs194.getTree());

                            }
                            break;

                    }

                    COLON195=(Token)match(input,COLON,FOLLOW_COLON_in_singleVarDecl2883); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COLON.add(COLON195);

                    pushFollow(FOLLOW_type_in_singleVarDecl2885);
                    type196=type();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_type.add(type196.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:325:29: ( EQUALS assignOrInitExpr )?
                    int alt64=2;
                    int LA64_0 = input.LA(1);

                    if ( (LA64_0==EQUALS) ) {
                        alt64=1;
                    }
                    switch (alt64) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:325:30: EQUALS assignOrInitExpr
                            {
                            EQUALS197=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_singleVarDecl2888); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_EQUALS.add(EQUALS197);

                            pushFollow(FOLLOW_assignOrInitExpr_in_singleVarDecl2890);
                            assignOrInitExpr198=assignOrInitExpr();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_assignOrInitExpr.add(assignOrInitExpr198.getTree());

                            }
                            break;

                    }



                    // AST REWRITE
                    // elements: ID, attrs, type, assignOrInitExpr
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 325:57: -> ^( ALLOC ( attrs )? type ID ( assignOrInitExpr )* )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:325:60: ^( ALLOC ( attrs )? type ID ( assignOrInitExpr )* )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ALLOC, "ALLOC"), root_1);

                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:325:68: ( attrs )?
                        if ( stream_attrs.hasNext() ) {
                            adaptor.addChild(root_1, stream_attrs.nextTree());

                        }
                        stream_attrs.reset();
                        adaptor.addChild(root_1, stream_type.nextTree());
                        adaptor.addChild(root_1, stream_ID.nextNode());
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:325:83: ( assignOrInitExpr )*
                        while ( stream_assignOrInitExpr.hasNext() ) {
                            adaptor.addChild(root_1, stream_assignOrInitExpr.nextTree());

                        }
                        stream_assignOrInitExpr.reset();

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }


                    }
                    break;
                case 3 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:326:9: ( COMMA ID )+ ( attrs )? ( ( COLON_EQUALS ( PLUS )? assignOrInitExpr ( COMMA assignOrInitExpr )* ) -> ^( ALLOC ( attrs )? TYPE ^( LIST ( ID )+ ) ( PLUS )? ^( LIST ( assignOrInitExpr )+ ) ) | ( COLON type ( EQUALS ( PLUS )? assignOrInitExpr ( COMMA assignOrInitExpr )* )? ) -> ^( ALLOC ( attrs )? type ^( LIST ( ID )+ ) ( PLUS )? ( ^( LIST ( assignOrInitExpr )+ ) )? ) )
                    {
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:326:9: ( COMMA ID )+
                    int cnt65=0;
                    loop65:
                    do {
                        int alt65=2;
                        int LA65_0 = input.LA(1);

                        if ( (LA65_0==COMMA) ) {
                            alt65=1;
                        }


                        switch (alt65) {
                    	case 1 :
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:326:11: COMMA ID
                    	    {
                    	    COMMA199=(Token)match(input,COMMA,FOLLOW_COMMA_in_singleVarDecl2923); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA199);

                    	    ID200=(Token)match(input,ID,FOLLOW_ID_in_singleVarDecl2925); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_ID.add(ID200);


                    	    }
                    	    break;

                    	default :
                    	    if ( cnt65 >= 1 ) break loop65;
                    	    if (state.backtracking>0) {state.failed=true; return retval;}
                                EarlyExitException eee =
                                    new EarlyExitException(65, input);
                                throw eee;
                        }
                        cnt65++;
                    } while (true);

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:326:23: ( attrs )?
                    int alt66=2;
                    int LA66_0 = input.LA(1);

                    if ( (LA66_0==ATTR) ) {
                        alt66=1;
                    }
                    switch (alt66) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:326:23: attrs
                            {
                            pushFollow(FOLLOW_attrs_in_singleVarDecl2930);
                            attrs201=attrs();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_attrs.add(attrs201.getTree());

                            }
                            break;

                    }

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:327:9: ( ( COLON_EQUALS ( PLUS )? assignOrInitExpr ( COMMA assignOrInitExpr )* ) -> ^( ALLOC ( attrs )? TYPE ^( LIST ( ID )+ ) ( PLUS )? ^( LIST ( assignOrInitExpr )+ ) ) | ( COLON type ( EQUALS ( PLUS )? assignOrInitExpr ( COMMA assignOrInitExpr )* )? ) -> ^( ALLOC ( attrs )? type ^( LIST ( ID )+ ) ( PLUS )? ( ^( LIST ( assignOrInitExpr )+ ) )? ) )
                    int alt72=2;
                    int LA72_0 = input.LA(1);

                    if ( (LA72_0==COLON_EQUALS) ) {
                        alt72=1;
                    }
                    else if ( (LA72_0==COLON) ) {
                        alt72=2;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 72, 0, input);

                        throw nvae;
                    }
                    switch (alt72) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:327:12: ( COLON_EQUALS ( PLUS )? assignOrInitExpr ( COMMA assignOrInitExpr )* )
                            {
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:327:12: ( COLON_EQUALS ( PLUS )? assignOrInitExpr ( COMMA assignOrInitExpr )* )
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:327:14: COLON_EQUALS ( PLUS )? assignOrInitExpr ( COMMA assignOrInitExpr )*
                            {
                            COLON_EQUALS202=(Token)match(input,COLON_EQUALS,FOLLOW_COLON_EQUALS_in_singleVarDecl2948); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_COLON_EQUALS.add(COLON_EQUALS202);

                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:327:27: ( PLUS )?
                            int alt67=2;
                            int LA67_0 = input.LA(1);

                            if ( (LA67_0==PLUS) ) {
                                alt67=1;
                            }
                            switch (alt67) {
                                case 1 :
                                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:327:27: PLUS
                                    {
                                    PLUS203=(Token)match(input,PLUS,FOLLOW_PLUS_in_singleVarDecl2950); if (state.failed) return retval; 
                                    if ( state.backtracking==0 ) stream_PLUS.add(PLUS203);


                                    }
                                    break;

                            }

                            pushFollow(FOLLOW_assignOrInitExpr_in_singleVarDecl2953);
                            assignOrInitExpr204=assignOrInitExpr();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_assignOrInitExpr.add(assignOrInitExpr204.getTree());
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:327:50: ( COMMA assignOrInitExpr )*
                            loop68:
                            do {
                                int alt68=2;
                                int LA68_0 = input.LA(1);

                                if ( (LA68_0==COMMA) ) {
                                    alt68=1;
                                }


                                switch (alt68) {
                            	case 1 :
                            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:327:51: COMMA assignOrInitExpr
                            	    {
                            	    COMMA205=(Token)match(input,COMMA,FOLLOW_COMMA_in_singleVarDecl2956); if (state.failed) return retval; 
                            	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA205);

                            	    pushFollow(FOLLOW_assignOrInitExpr_in_singleVarDecl2958);
                            	    assignOrInitExpr206=assignOrInitExpr();

                            	    state._fsp--;
                            	    if (state.failed) return retval;
                            	    if ( state.backtracking==0 ) stream_assignOrInitExpr.add(assignOrInitExpr206.getTree());

                            	    }
                            	    break;

                            	default :
                            	    break loop68;
                                }
                            } while (true);


                            }



                            // AST REWRITE
                            // elements: attrs, ID, PLUS, assignOrInitExpr
                            // token labels: 
                            // rule labels: retval
                            // token list labels: 
                            // rule list labels: 
                            // wildcard labels: 
                            if ( state.backtracking==0 ) {
                            retval.tree = root_0;
                            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                            root_0 = (CommonTree)adaptor.nil();
                            // 328:15: -> ^( ALLOC ( attrs )? TYPE ^( LIST ( ID )+ ) ( PLUS )? ^( LIST ( assignOrInitExpr )+ ) )
                            {
                                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:328:18: ^( ALLOC ( attrs )? TYPE ^( LIST ( ID )+ ) ( PLUS )? ^( LIST ( assignOrInitExpr )+ ) )
                                {
                                CommonTree root_1 = (CommonTree)adaptor.nil();
                                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ALLOC, "ALLOC"), root_1);

                                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:328:26: ( attrs )?
                                if ( stream_attrs.hasNext() ) {
                                    adaptor.addChild(root_1, stream_attrs.nextTree());

                                }
                                stream_attrs.reset();
                                adaptor.addChild(root_1, (CommonTree)adaptor.create(TYPE, "TYPE"));
                                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:328:38: ^( LIST ( ID )+ )
                                {
                                CommonTree root_2 = (CommonTree)adaptor.nil();
                                root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(LIST, "LIST"), root_2);

                                if ( !(stream_ID.hasNext()) ) {
                                    throw new RewriteEarlyExitException();
                                }
                                while ( stream_ID.hasNext() ) {
                                    adaptor.addChild(root_2, stream_ID.nextNode());

                                }
                                stream_ID.reset();

                                adaptor.addChild(root_1, root_2);
                                }
                                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:328:50: ( PLUS )?
                                if ( stream_PLUS.hasNext() ) {
                                    adaptor.addChild(root_1, stream_PLUS.nextNode());

                                }
                                stream_PLUS.reset();
                                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:328:56: ^( LIST ( assignOrInitExpr )+ )
                                {
                                CommonTree root_2 = (CommonTree)adaptor.nil();
                                root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(LIST, "LIST"), root_2);

                                if ( !(stream_assignOrInitExpr.hasNext()) ) {
                                    throw new RewriteEarlyExitException();
                                }
                                while ( stream_assignOrInitExpr.hasNext() ) {
                                    adaptor.addChild(root_2, stream_assignOrInitExpr.nextTree());

                                }
                                stream_assignOrInitExpr.reset();

                                adaptor.addChild(root_1, root_2);
                                }

                                adaptor.addChild(root_0, root_1);
                                }

                            }

                            retval.tree = root_0;}
                            }
                            break;
                        case 2 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:329:12: ( COLON type ( EQUALS ( PLUS )? assignOrInitExpr ( COMMA assignOrInitExpr )* )? )
                            {
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:329:12: ( COLON type ( EQUALS ( PLUS )? assignOrInitExpr ( COMMA assignOrInitExpr )* )? )
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:329:14: COLON type ( EQUALS ( PLUS )? assignOrInitExpr ( COMMA assignOrInitExpr )* )?
                            {
                            COLON207=(Token)match(input,COLON,FOLLOW_COLON_in_singleVarDecl3020); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_COLON.add(COLON207);

                            pushFollow(FOLLOW_type_in_singleVarDecl3022);
                            type208=type();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_type.add(type208.getTree());
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:329:25: ( EQUALS ( PLUS )? assignOrInitExpr ( COMMA assignOrInitExpr )* )?
                            int alt71=2;
                            int LA71_0 = input.LA(1);

                            if ( (LA71_0==EQUALS) ) {
                                alt71=1;
                            }
                            switch (alt71) {
                                case 1 :
                                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:329:26: EQUALS ( PLUS )? assignOrInitExpr ( COMMA assignOrInitExpr )*
                                    {
                                    EQUALS209=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_singleVarDecl3025); if (state.failed) return retval; 
                                    if ( state.backtracking==0 ) stream_EQUALS.add(EQUALS209);

                                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:329:33: ( PLUS )?
                                    int alt69=2;
                                    int LA69_0 = input.LA(1);

                                    if ( (LA69_0==PLUS) ) {
                                        alt69=1;
                                    }
                                    switch (alt69) {
                                        case 1 :
                                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:329:33: PLUS
                                            {
                                            PLUS210=(Token)match(input,PLUS,FOLLOW_PLUS_in_singleVarDecl3027); if (state.failed) return retval; 
                                            if ( state.backtracking==0 ) stream_PLUS.add(PLUS210);


                                            }
                                            break;

                                    }

                                    pushFollow(FOLLOW_assignOrInitExpr_in_singleVarDecl3030);
                                    assignOrInitExpr211=assignOrInitExpr();

                                    state._fsp--;
                                    if (state.failed) return retval;
                                    if ( state.backtracking==0 ) stream_assignOrInitExpr.add(assignOrInitExpr211.getTree());
                                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:329:56: ( COMMA assignOrInitExpr )*
                                    loop70:
                                    do {
                                        int alt70=2;
                                        int LA70_0 = input.LA(1);

                                        if ( (LA70_0==COMMA) ) {
                                            alt70=1;
                                        }


                                        switch (alt70) {
                                    	case 1 :
                                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:329:57: COMMA assignOrInitExpr
                                    	    {
                                    	    COMMA212=(Token)match(input,COMMA,FOLLOW_COMMA_in_singleVarDecl3033); if (state.failed) return retval; 
                                    	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA212);

                                    	    pushFollow(FOLLOW_assignOrInitExpr_in_singleVarDecl3035);
                                    	    assignOrInitExpr213=assignOrInitExpr();

                                    	    state._fsp--;
                                    	    if (state.failed) return retval;
                                    	    if ( state.backtracking==0 ) stream_assignOrInitExpr.add(assignOrInitExpr213.getTree());

                                    	    }
                                    	    break;

                                    	default :
                                    	    break loop70;
                                        }
                                    } while (true);


                                    }
                                    break;

                            }


                            }



                            // AST REWRITE
                            // elements: ID, type, assignOrInitExpr, PLUS, attrs
                            // token labels: 
                            // rule labels: retval
                            // token list labels: 
                            // rule list labels: 
                            // wildcard labels: 
                            if ( state.backtracking==0 ) {
                            retval.tree = root_0;
                            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                            root_0 = (CommonTree)adaptor.nil();
                            // 330:15: -> ^( ALLOC ( attrs )? type ^( LIST ( ID )+ ) ( PLUS )? ( ^( LIST ( assignOrInitExpr )+ ) )? )
                            {
                                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:330:18: ^( ALLOC ( attrs )? type ^( LIST ( ID )+ ) ( PLUS )? ( ^( LIST ( assignOrInitExpr )+ ) )? )
                                {
                                CommonTree root_1 = (CommonTree)adaptor.nil();
                                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ALLOC, "ALLOC"), root_1);

                                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:330:26: ( attrs )?
                                if ( stream_attrs.hasNext() ) {
                                    adaptor.addChild(root_1, stream_attrs.nextTree());

                                }
                                stream_attrs.reset();
                                adaptor.addChild(root_1, stream_type.nextTree());
                                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:330:38: ^( LIST ( ID )+ )
                                {
                                CommonTree root_2 = (CommonTree)adaptor.nil();
                                root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(LIST, "LIST"), root_2);

                                if ( !(stream_ID.hasNext()) ) {
                                    throw new RewriteEarlyExitException();
                                }
                                while ( stream_ID.hasNext() ) {
                                    adaptor.addChild(root_2, stream_ID.nextNode());

                                }
                                stream_ID.reset();

                                adaptor.addChild(root_1, root_2);
                                }
                                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:330:50: ( PLUS )?
                                if ( stream_PLUS.hasNext() ) {
                                    adaptor.addChild(root_1, stream_PLUS.nextNode());

                                }
                                stream_PLUS.reset();
                                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:330:56: ( ^( LIST ( assignOrInitExpr )+ ) )?
                                if ( stream_assignOrInitExpr.hasNext() ) {
                                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:330:56: ^( LIST ( assignOrInitExpr )+ )
                                    {
                                    CommonTree root_2 = (CommonTree)adaptor.nil();
                                    root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(LIST, "LIST"), root_2);

                                    if ( !(stream_assignOrInitExpr.hasNext()) ) {
                                        throw new RewriteEarlyExitException();
                                    }
                                    while ( stream_assignOrInitExpr.hasNext() ) {
                                        adaptor.addChild(root_2, stream_assignOrInitExpr.nextTree());

                                    }
                                    stream_assignOrInitExpr.reset();

                                    adaptor.addChild(root_1, root_2);
                                    }

                                }
                                stream_assignOrInitExpr.reset();

                                adaptor.addChild(root_0, root_1);
                                }

                            }

                            retval.tree = root_0;}
                            }
                            break;

                    }


                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "singleVarDecl"

    public static class tupleVarDecl_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "tupleVarDecl"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:334:1: tupleVarDecl : idTuple ( attrs )? ( ( COLON_EQUALS assignOrInitExpr -> ^( ALLOC_TUPLE ( attrs )? TYPE idTuple assignOrInitExpr ) ) | ( COLON type ( EQUALS assignOrInitExpr )? -> ^( ALLOC_TUPLE ( attrs )? type idTuple ( assignOrInitExpr )* ) ) ) ;
    public final EulangParser.tupleVarDecl_return tupleVarDecl() throws RecognitionException {
        EulangParser.tupleVarDecl_return retval = new EulangParser.tupleVarDecl_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COLON_EQUALS216=null;
        Token COLON218=null;
        Token EQUALS220=null;
        EulangParser.idTuple_return idTuple214 = null;

        EulangParser.attrs_return attrs215 = null;

        EulangParser.assignOrInitExpr_return assignOrInitExpr217 = null;

        EulangParser.type_return type219 = null;

        EulangParser.assignOrInitExpr_return assignOrInitExpr221 = null;


        CommonTree COLON_EQUALS216_tree=null;
        CommonTree COLON218_tree=null;
        CommonTree EQUALS220_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleTokenStream stream_EQUALS=new RewriteRuleTokenStream(adaptor,"token EQUALS");
        RewriteRuleTokenStream stream_COLON_EQUALS=new RewriteRuleTokenStream(adaptor,"token COLON_EQUALS");
        RewriteRuleSubtreeStream stream_idTuple=new RewriteRuleSubtreeStream(adaptor,"rule idTuple");
        RewriteRuleSubtreeStream stream_attrs=new RewriteRuleSubtreeStream(adaptor,"rule attrs");
        RewriteRuleSubtreeStream stream_assignOrInitExpr=new RewriteRuleSubtreeStream(adaptor,"rule assignOrInitExpr");
        RewriteRuleSubtreeStream stream_type=new RewriteRuleSubtreeStream(adaptor,"rule type");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:334:13: ( idTuple ( attrs )? ( ( COLON_EQUALS assignOrInitExpr -> ^( ALLOC_TUPLE ( attrs )? TYPE idTuple assignOrInitExpr ) ) | ( COLON type ( EQUALS assignOrInitExpr )? -> ^( ALLOC_TUPLE ( attrs )? type idTuple ( assignOrInitExpr )* ) ) ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:335:5: idTuple ( attrs )? ( ( COLON_EQUALS assignOrInitExpr -> ^( ALLOC_TUPLE ( attrs )? TYPE idTuple assignOrInitExpr ) ) | ( COLON type ( EQUALS assignOrInitExpr )? -> ^( ALLOC_TUPLE ( attrs )? type idTuple ( assignOrInitExpr )* ) ) )
            {
            pushFollow(FOLLOW_idTuple_in_tupleVarDecl3124);
            idTuple214=idTuple();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_idTuple.add(idTuple214.getTree());
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:335:13: ( attrs )?
            int alt74=2;
            int LA74_0 = input.LA(1);

            if ( (LA74_0==ATTR) ) {
                alt74=1;
            }
            switch (alt74) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:335:13: attrs
                    {
                    pushFollow(FOLLOW_attrs_in_tupleVarDecl3126);
                    attrs215=attrs();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_attrs.add(attrs215.getTree());

                    }
                    break;

            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:336:7: ( ( COLON_EQUALS assignOrInitExpr -> ^( ALLOC_TUPLE ( attrs )? TYPE idTuple assignOrInitExpr ) ) | ( COLON type ( EQUALS assignOrInitExpr )? -> ^( ALLOC_TUPLE ( attrs )? type idTuple ( assignOrInitExpr )* ) ) )
            int alt76=2;
            int LA76_0 = input.LA(1);

            if ( (LA76_0==COLON_EQUALS) ) {
                alt76=1;
            }
            else if ( (LA76_0==COLON) ) {
                alt76=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 76, 0, input);

                throw nvae;
            }
            switch (alt76) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:336:10: ( COLON_EQUALS assignOrInitExpr -> ^( ALLOC_TUPLE ( attrs )? TYPE idTuple assignOrInitExpr ) )
                    {
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:336:10: ( COLON_EQUALS assignOrInitExpr -> ^( ALLOC_TUPLE ( attrs )? TYPE idTuple assignOrInitExpr ) )
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:336:12: COLON_EQUALS assignOrInitExpr
                    {
                    COLON_EQUALS216=(Token)match(input,COLON_EQUALS,FOLLOW_COLON_EQUALS_in_tupleVarDecl3141); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COLON_EQUALS.add(COLON_EQUALS216);

                    pushFollow(FOLLOW_assignOrInitExpr_in_tupleVarDecl3143);
                    assignOrInitExpr217=assignOrInitExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_assignOrInitExpr.add(assignOrInitExpr217.getTree());


                    // AST REWRITE
                    // elements: attrs, idTuple, assignOrInitExpr
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 336:50: -> ^( ALLOC_TUPLE ( attrs )? TYPE idTuple assignOrInitExpr )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:336:53: ^( ALLOC_TUPLE ( attrs )? TYPE idTuple assignOrInitExpr )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ALLOC_TUPLE, "ALLOC_TUPLE"), root_1);

                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:336:67: ( attrs )?
                        if ( stream_attrs.hasNext() ) {
                            adaptor.addChild(root_1, stream_attrs.nextTree());

                        }
                        stream_attrs.reset();
                        adaptor.addChild(root_1, (CommonTree)adaptor.create(TYPE, "TYPE"));
                        adaptor.addChild(root_1, stream_idTuple.nextTree());
                        adaptor.addChild(root_1, stream_assignOrInitExpr.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }


                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:337:9: ( COLON type ( EQUALS assignOrInitExpr )? -> ^( ALLOC_TUPLE ( attrs )? type idTuple ( assignOrInitExpr )* ) )
                    {
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:337:9: ( COLON type ( EQUALS assignOrInitExpr )? -> ^( ALLOC_TUPLE ( attrs )? type idTuple ( assignOrInitExpr )* ) )
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:337:11: COLON type ( EQUALS assignOrInitExpr )?
                    {
                    COLON218=(Token)match(input,COLON,FOLLOW_COLON_in_tupleVarDecl3180); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COLON.add(COLON218);

                    pushFollow(FOLLOW_type_in_tupleVarDecl3182);
                    type219=type();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_type.add(type219.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:337:22: ( EQUALS assignOrInitExpr )?
                    int alt75=2;
                    int LA75_0 = input.LA(1);

                    if ( (LA75_0==EQUALS) ) {
                        alt75=1;
                    }
                    switch (alt75) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:337:23: EQUALS assignOrInitExpr
                            {
                            EQUALS220=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_tupleVarDecl3185); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_EQUALS.add(EQUALS220);

                            pushFollow(FOLLOW_assignOrInitExpr_in_tupleVarDecl3187);
                            assignOrInitExpr221=assignOrInitExpr();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_assignOrInitExpr.add(assignOrInitExpr221.getTree());

                            }
                            break;

                    }



                    // AST REWRITE
                    // elements: type, assignOrInitExpr, attrs, idTuple
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 337:50: -> ^( ALLOC_TUPLE ( attrs )? type idTuple ( assignOrInitExpr )* )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:337:53: ^( ALLOC_TUPLE ( attrs )? type idTuple ( assignOrInitExpr )* )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ALLOC_TUPLE, "ALLOC_TUPLE"), root_1);

                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:337:67: ( attrs )?
                        if ( stream_attrs.hasNext() ) {
                            adaptor.addChild(root_1, stream_attrs.nextTree());

                        }
                        stream_attrs.reset();
                        adaptor.addChild(root_1, stream_type.nextTree());
                        adaptor.addChild(root_1, stream_idTuple.nextTree());
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:337:87: ( assignOrInitExpr )*
                        while ( stream_assignOrInitExpr.hasNext() ) {
                            adaptor.addChild(root_1, stream_assignOrInitExpr.nextTree());

                        }
                        stream_assignOrInitExpr.reset();

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }


                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "tupleVarDecl"

    public static class assignStmt_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "assignStmt"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:342:1: assignStmt : ( ( lhs assignEqOp )=> lhs assignEqOp assignOrInitExpr -> ^( ASSIGN assignEqOp lhs assignOrInitExpr ) | idTuple EQUALS assignOrInitExpr -> ^( ASSIGN EQUALS idTuple assignOrInitExpr ) | ( lhs ( COMMA lhs )+ assignEqOp )=> lhs ( COMMA lhs )+ assignEqOp ( PLUS )? assignOrInitExpr ( COMMA assignOrInitExpr )* -> ^( ASSIGN assignEqOp ^( LIST ( lhs )+ ) ( PLUS )? ^( LIST ( assignOrInitExpr )+ ) ) );
    public final EulangParser.assignStmt_return assignStmt() throws RecognitionException {
        EulangParser.assignStmt_return retval = new EulangParser.assignStmt_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token EQUALS226=null;
        Token COMMA229=null;
        Token PLUS232=null;
        Token COMMA234=null;
        EulangParser.lhs_return lhs222 = null;

        EulangParser.assignEqOp_return assignEqOp223 = null;

        EulangParser.assignOrInitExpr_return assignOrInitExpr224 = null;

        EulangParser.idTuple_return idTuple225 = null;

        EulangParser.assignOrInitExpr_return assignOrInitExpr227 = null;

        EulangParser.lhs_return lhs228 = null;

        EulangParser.lhs_return lhs230 = null;

        EulangParser.assignEqOp_return assignEqOp231 = null;

        EulangParser.assignOrInitExpr_return assignOrInitExpr233 = null;

        EulangParser.assignOrInitExpr_return assignOrInitExpr235 = null;


        CommonTree EQUALS226_tree=null;
        CommonTree COMMA229_tree=null;
        CommonTree PLUS232_tree=null;
        CommonTree COMMA234_tree=null;
        RewriteRuleTokenStream stream_PLUS=new RewriteRuleTokenStream(adaptor,"token PLUS");
        RewriteRuleTokenStream stream_EQUALS=new RewriteRuleTokenStream(adaptor,"token EQUALS");
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleSubtreeStream stream_idTuple=new RewriteRuleSubtreeStream(adaptor,"rule idTuple");
        RewriteRuleSubtreeStream stream_assignEqOp=new RewriteRuleSubtreeStream(adaptor,"rule assignEqOp");
        RewriteRuleSubtreeStream stream_assignOrInitExpr=new RewriteRuleSubtreeStream(adaptor,"rule assignOrInitExpr");
        RewriteRuleSubtreeStream stream_lhs=new RewriteRuleSubtreeStream(adaptor,"rule lhs");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:342:12: ( ( lhs assignEqOp )=> lhs assignEqOp assignOrInitExpr -> ^( ASSIGN assignEqOp lhs assignOrInitExpr ) | idTuple EQUALS assignOrInitExpr -> ^( ASSIGN EQUALS idTuple assignOrInitExpr ) | ( lhs ( COMMA lhs )+ assignEqOp )=> lhs ( COMMA lhs )+ assignEqOp ( PLUS )? assignOrInitExpr ( COMMA assignOrInitExpr )* -> ^( ASSIGN assignEqOp ^( LIST ( lhs )+ ) ( PLUS )? ^( LIST ( assignOrInitExpr )+ ) ) )
            int alt80=3;
            alt80 = dfa80.predict(input);
            switch (alt80) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:342:14: ( lhs assignEqOp )=> lhs assignEqOp assignOrInitExpr
                    {
                    pushFollow(FOLLOW_lhs_in_assignStmt3238);
                    lhs222=lhs();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_lhs.add(lhs222.getTree());
                    pushFollow(FOLLOW_assignEqOp_in_assignStmt3240);
                    assignEqOp223=assignEqOp();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_assignEqOp.add(assignEqOp223.getTree());
                    pushFollow(FOLLOW_assignOrInitExpr_in_assignStmt3242);
                    assignOrInitExpr224=assignOrInitExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_assignOrInitExpr.add(assignOrInitExpr224.getTree());


                    // AST REWRITE
                    // elements: lhs, assignEqOp, assignOrInitExpr
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 342:73: -> ^( ASSIGN assignEqOp lhs assignOrInitExpr )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:342:76: ^( ASSIGN assignEqOp lhs assignOrInitExpr )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ASSIGN, "ASSIGN"), root_1);

                        adaptor.addChild(root_1, stream_assignEqOp.nextTree());
                        adaptor.addChild(root_1, stream_lhs.nextTree());
                        adaptor.addChild(root_1, stream_assignOrInitExpr.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:343:7: idTuple EQUALS assignOrInitExpr
                    {
                    pushFollow(FOLLOW_idTuple_in_assignStmt3269);
                    idTuple225=idTuple();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_idTuple.add(idTuple225.getTree());
                    EQUALS226=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_assignStmt3271); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_EQUALS.add(EQUALS226);

                    pushFollow(FOLLOW_assignOrInitExpr_in_assignStmt3273);
                    assignOrInitExpr227=assignOrInitExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_assignOrInitExpr.add(assignOrInitExpr227.getTree());


                    // AST REWRITE
                    // elements: EQUALS, idTuple, assignOrInitExpr
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 343:53: -> ^( ASSIGN EQUALS idTuple assignOrInitExpr )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:343:56: ^( ASSIGN EQUALS idTuple assignOrInitExpr )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ASSIGN, "ASSIGN"), root_1);

                        adaptor.addChild(root_1, stream_EQUALS.nextNode());
                        adaptor.addChild(root_1, stream_idTuple.nextTree());
                        adaptor.addChild(root_1, stream_assignOrInitExpr.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 3 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:345:7: ( lhs ( COMMA lhs )+ assignEqOp )=> lhs ( COMMA lhs )+ assignEqOp ( PLUS )? assignOrInitExpr ( COMMA assignOrInitExpr )*
                    {
                    pushFollow(FOLLOW_lhs_in_assignStmt3328);
                    lhs228=lhs();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_lhs.add(lhs228.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:345:45: ( COMMA lhs )+
                    int cnt77=0;
                    loop77:
                    do {
                        int alt77=2;
                        int LA77_0 = input.LA(1);

                        if ( (LA77_0==COMMA) ) {
                            alt77=1;
                        }


                        switch (alt77) {
                    	case 1 :
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:345:46: COMMA lhs
                    	    {
                    	    COMMA229=(Token)match(input,COMMA,FOLLOW_COMMA_in_assignStmt3331); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA229);

                    	    pushFollow(FOLLOW_lhs_in_assignStmt3333);
                    	    lhs230=lhs();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_lhs.add(lhs230.getTree());

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt77 >= 1 ) break loop77;
                    	    if (state.backtracking>0) {state.failed=true; return retval;}
                                EarlyExitException eee =
                                    new EarlyExitException(77, input);
                                throw eee;
                        }
                        cnt77++;
                    } while (true);

                    pushFollow(FOLLOW_assignEqOp_in_assignStmt3337);
                    assignEqOp231=assignEqOp();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_assignEqOp.add(assignEqOp231.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:345:69: ( PLUS )?
                    int alt78=2;
                    int LA78_0 = input.LA(1);

                    if ( (LA78_0==PLUS) ) {
                        alt78=1;
                    }
                    switch (alt78) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:345:69: PLUS
                            {
                            PLUS232=(Token)match(input,PLUS,FOLLOW_PLUS_in_assignStmt3339); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_PLUS.add(PLUS232);


                            }
                            break;

                    }

                    pushFollow(FOLLOW_assignOrInitExpr_in_assignStmt3342);
                    assignOrInitExpr233=assignOrInitExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_assignOrInitExpr.add(assignOrInitExpr233.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:345:92: ( COMMA assignOrInitExpr )*
                    loop79:
                    do {
                        int alt79=2;
                        int LA79_0 = input.LA(1);

                        if ( (LA79_0==COMMA) ) {
                            alt79=1;
                        }


                        switch (alt79) {
                    	case 1 :
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:345:93: COMMA assignOrInitExpr
                    	    {
                    	    COMMA234=(Token)match(input,COMMA,FOLLOW_COMMA_in_assignStmt3345); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA234);

                    	    pushFollow(FOLLOW_assignOrInitExpr_in_assignStmt3347);
                    	    assignOrInitExpr235=assignOrInitExpr();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_assignOrInitExpr.add(assignOrInitExpr235.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop79;
                        }
                    } while (true);



                    // AST REWRITE
                    // elements: assignEqOp, assignOrInitExpr, PLUS, lhs
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 346:9: -> ^( ASSIGN assignEqOp ^( LIST ( lhs )+ ) ( PLUS )? ^( LIST ( assignOrInitExpr )+ ) )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:346:12: ^( ASSIGN assignEqOp ^( LIST ( lhs )+ ) ( PLUS )? ^( LIST ( assignOrInitExpr )+ ) )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ASSIGN, "ASSIGN"), root_1);

                        adaptor.addChild(root_1, stream_assignEqOp.nextTree());
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:346:32: ^( LIST ( lhs )+ )
                        {
                        CommonTree root_2 = (CommonTree)adaptor.nil();
                        root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(LIST, "LIST"), root_2);

                        if ( !(stream_lhs.hasNext()) ) {
                            throw new RewriteEarlyExitException();
                        }
                        while ( stream_lhs.hasNext() ) {
                            adaptor.addChild(root_2, stream_lhs.nextTree());

                        }
                        stream_lhs.reset();

                        adaptor.addChild(root_1, root_2);
                        }
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:346:45: ( PLUS )?
                        if ( stream_PLUS.hasNext() ) {
                            adaptor.addChild(root_1, stream_PLUS.nextNode());

                        }
                        stream_PLUS.reset();
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:346:51: ^( LIST ( assignOrInitExpr )+ )
                        {
                        CommonTree root_2 = (CommonTree)adaptor.nil();
                        root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(LIST, "LIST"), root_2);

                        if ( !(stream_assignOrInitExpr.hasNext()) ) {
                            throw new RewriteEarlyExitException();
                        }
                        while ( stream_assignOrInitExpr.hasNext() ) {
                            adaptor.addChild(root_2, stream_assignOrInitExpr.nextTree());

                        }
                        stream_assignOrInitExpr.reset();

                        adaptor.addChild(root_1, root_2);
                        }

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "assignStmt"

    public static class assignOrInitExpr_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "assignOrInitExpr"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:349:1: assignOrInitExpr : ( assignExpr | initList );
    public final EulangParser.assignOrInitExpr_return assignOrInitExpr() throws RecognitionException {
        EulangParser.assignOrInitExpr_return retval = new EulangParser.assignOrInitExpr_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.assignExpr_return assignExpr236 = null;

        EulangParser.initList_return initList237 = null;



        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:349:18: ( assignExpr | initList )
            int alt81=2;
            int LA81_0 = input.LA(1);

            if ( (LA81_0==CODE||(LA81_0>=SIZEOF && LA81_0<=TYPEOF)||LA81_0==ID||LA81_0==COLON||LA81_0==LPAREN||LA81_0==NIL||LA81_0==IF||LA81_0==NOT||(LA81_0>=TILDE && LA81_0<=AMP)||LA81_0==MINUS||(LA81_0>=PLUSPLUS && LA81_0<=STRING_LITERAL)||(LA81_0>=FALSE && LA81_0<=COLONS)) ) {
                alt81=1;
            }
            else if ( (LA81_0==LBRACKET) ) {
                alt81=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 81, 0, input);

                throw nvae;
            }
            switch (alt81) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:349:20: assignExpr
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_assignExpr_in_assignOrInitExpr3408);
                    assignExpr236=assignExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, assignExpr236.getTree());

                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:349:33: initList
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_initList_in_assignOrInitExpr3412);
                    initList237=initList();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, initList237.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "assignOrInitExpr"

    public static class assignExpr_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "assignExpr"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:352:1: assignExpr : ( ( lhs assignEqOp )=> lhs assignEqOp assignExpr -> ^( ASSIGN assignEqOp lhs assignExpr ) | ( idTuple EQUALS )=> idTuple EQUALS assignExpr -> ^( ASSIGN EQUALS idTuple assignExpr ) | rhsExpr -> rhsExpr );
    public final EulangParser.assignExpr_return assignExpr() throws RecognitionException {
        EulangParser.assignExpr_return retval = new EulangParser.assignExpr_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token EQUALS242=null;
        EulangParser.lhs_return lhs238 = null;

        EulangParser.assignEqOp_return assignEqOp239 = null;

        EulangParser.assignExpr_return assignExpr240 = null;

        EulangParser.idTuple_return idTuple241 = null;

        EulangParser.assignExpr_return assignExpr243 = null;

        EulangParser.rhsExpr_return rhsExpr244 = null;


        CommonTree EQUALS242_tree=null;
        RewriteRuleTokenStream stream_EQUALS=new RewriteRuleTokenStream(adaptor,"token EQUALS");
        RewriteRuleSubtreeStream stream_idTuple=new RewriteRuleSubtreeStream(adaptor,"rule idTuple");
        RewriteRuleSubtreeStream stream_assignEqOp=new RewriteRuleSubtreeStream(adaptor,"rule assignEqOp");
        RewriteRuleSubtreeStream stream_rhsExpr=new RewriteRuleSubtreeStream(adaptor,"rule rhsExpr");
        RewriteRuleSubtreeStream stream_assignExpr=new RewriteRuleSubtreeStream(adaptor,"rule assignExpr");
        RewriteRuleSubtreeStream stream_lhs=new RewriteRuleSubtreeStream(adaptor,"rule lhs");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:352:12: ( ( lhs assignEqOp )=> lhs assignEqOp assignExpr -> ^( ASSIGN assignEqOp lhs assignExpr ) | ( idTuple EQUALS )=> idTuple EQUALS assignExpr -> ^( ASSIGN EQUALS idTuple assignExpr ) | rhsExpr -> rhsExpr )
            int alt82=3;
            alt82 = dfa82.predict(input);
            switch (alt82) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:352:14: ( lhs assignEqOp )=> lhs assignEqOp assignExpr
                    {
                    pushFollow(FOLLOW_lhs_in_assignExpr3430);
                    lhs238=lhs();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_lhs.add(lhs238.getTree());
                    pushFollow(FOLLOW_assignEqOp_in_assignExpr3432);
                    assignEqOp239=assignEqOp();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_assignEqOp.add(assignEqOp239.getTree());
                    pushFollow(FOLLOW_assignExpr_in_assignExpr3434);
                    assignExpr240=assignExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_assignExpr.add(assignExpr240.getTree());


                    // AST REWRITE
                    // elements: assignExpr, assignEqOp, lhs
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 352:67: -> ^( ASSIGN assignEqOp lhs assignExpr )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:352:70: ^( ASSIGN assignEqOp lhs assignExpr )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ASSIGN, "ASSIGN"), root_1);

                        adaptor.addChild(root_1, stream_assignEqOp.nextTree());
                        adaptor.addChild(root_1, stream_lhs.nextTree());
                        adaptor.addChild(root_1, stream_assignExpr.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:353:7: ( idTuple EQUALS )=> idTuple EQUALS assignExpr
                    {
                    pushFollow(FOLLOW_idTuple_in_assignExpr3469);
                    idTuple241=idTuple();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_idTuple.add(idTuple241.getTree());
                    EQUALS242=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_assignExpr3471); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_EQUALS.add(EQUALS242);

                    pushFollow(FOLLOW_assignExpr_in_assignExpr3473);
                    assignExpr243=assignExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_assignExpr.add(assignExpr243.getTree());


                    // AST REWRITE
                    // elements: EQUALS, idTuple, assignExpr
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 353:67: -> ^( ASSIGN EQUALS idTuple assignExpr )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:353:70: ^( ASSIGN EQUALS idTuple assignExpr )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ASSIGN, "ASSIGN"), root_1);

                        adaptor.addChild(root_1, stream_EQUALS.nextNode());
                        adaptor.addChild(root_1, stream_idTuple.nextTree());
                        adaptor.addChild(root_1, stream_assignExpr.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 3 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:354:7: rhsExpr
                    {
                    pushFollow(FOLLOW_rhsExpr_in_assignExpr3507);
                    rhsExpr244=rhsExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_rhsExpr.add(rhsExpr244.getTree());


                    // AST REWRITE
                    // elements: rhsExpr
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 354:43: -> rhsExpr
                    {
                        adaptor.addChild(root_0, stream_rhsExpr.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "assignExpr"

    public static class assignOp_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "assignOp"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:357:1: assignOp : ( PLUS_EQ | MINUS_EQ | STAR_EQ | SLASH_EQ | REM_EQ | UDIV_EQ | UREM_EQ | MOD_EQ | AND_EQ | OR_EQ | XOR_EQ | LSHIFT_EQ | RSHIFT_EQ | URSHIFT_EQ | CLSHIFT_EQ | CRSHIFT_EQ );
    public final EulangParser.assignOp_return assignOp() throws RecognitionException {
        EulangParser.assignOp_return retval = new EulangParser.assignOp_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token set245=null;

        CommonTree set245_tree=null;

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:357:10: ( PLUS_EQ | MINUS_EQ | STAR_EQ | SLASH_EQ | REM_EQ | UDIV_EQ | UREM_EQ | MOD_EQ | AND_EQ | OR_EQ | XOR_EQ | LSHIFT_EQ | RSHIFT_EQ | URSHIFT_EQ | CLSHIFT_EQ | CRSHIFT_EQ )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:
            {
            root_0 = (CommonTree)adaptor.nil();

            set245=(Token)input.LT(1);
            if ( input.LA(1)==PLUS_EQ||(input.LA(1)>=MINUS_EQ && input.LA(1)<=CRSHIFT_EQ) ) {
                input.consume();
                if ( state.backtracking==0 ) adaptor.addChild(root_0, (CommonTree)adaptor.create(set245));
                state.errorRecovery=false;state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "assignOp"

    public static class assignEqOp_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "assignEqOp"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:359:1: assignEqOp : ( EQUALS | assignOp );
    public final EulangParser.assignEqOp_return assignEqOp() throws RecognitionException {
        EulangParser.assignEqOp_return retval = new EulangParser.assignEqOp_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token EQUALS246=null;
        EulangParser.assignOp_return assignOp247 = null;


        CommonTree EQUALS246_tree=null;

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:359:12: ( EQUALS | assignOp )
            int alt83=2;
            int LA83_0 = input.LA(1);

            if ( (LA83_0==EQUALS) ) {
                alt83=1;
            }
            else if ( (LA83_0==PLUS_EQ||(LA83_0>=MINUS_EQ && LA83_0<=CRSHIFT_EQ)) ) {
                alt83=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 83, 0, input);

                throw nvae;
            }
            switch (alt83) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:359:14: EQUALS
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    EQUALS246=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_assignEqOp3622); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    EQUALS246_tree = (CommonTree)adaptor.create(EQUALS246);
                    adaptor.addChild(root_0, EQUALS246_tree);
                    }

                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:359:23: assignOp
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_assignOp_in_assignEqOp3626);
                    assignOp247=assignOp();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, assignOp247.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "assignEqOp"

    public static class initList_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "initList"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:361:1: initList : LBRACKET ( initExpr ( COMMA initExpr )* )? RBRACKET -> ^( INITLIST ( initExpr )* ) ;
    public final EulangParser.initList_return initList() throws RecognitionException {
        EulangParser.initList_return retval = new EulangParser.initList_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LBRACKET248=null;
        Token COMMA250=null;
        Token RBRACKET252=null;
        EulangParser.initExpr_return initExpr249 = null;

        EulangParser.initExpr_return initExpr251 = null;


        CommonTree LBRACKET248_tree=null;
        CommonTree COMMA250_tree=null;
        CommonTree RBRACKET252_tree=null;
        RewriteRuleTokenStream stream_LBRACKET=new RewriteRuleTokenStream(adaptor,"token LBRACKET");
        RewriteRuleTokenStream stream_RBRACKET=new RewriteRuleTokenStream(adaptor,"token RBRACKET");
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleSubtreeStream stream_initExpr=new RewriteRuleSubtreeStream(adaptor,"rule initExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:361:10: ( LBRACKET ( initExpr ( COMMA initExpr )* )? RBRACKET -> ^( INITLIST ( initExpr )* ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:361:12: LBRACKET ( initExpr ( COMMA initExpr )* )? RBRACKET
            {
            LBRACKET248=(Token)match(input,LBRACKET,FOLLOW_LBRACKET_in_initList3635); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LBRACKET.add(LBRACKET248);

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:361:21: ( initExpr ( COMMA initExpr )* )?
            int alt85=2;
            int LA85_0 = input.LA(1);

            if ( (LA85_0==CODE||(LA85_0>=SIZEOF && LA85_0<=TYPEOF)||LA85_0==ID||LA85_0==COLON||LA85_0==LBRACKET||LA85_0==LPAREN||LA85_0==NIL||LA85_0==PERIOD||LA85_0==IF||LA85_0==NOT||(LA85_0>=TILDE && LA85_0<=AMP)||LA85_0==MINUS||(LA85_0>=PLUSPLUS && LA85_0<=STRING_LITERAL)||(LA85_0>=FALSE && LA85_0<=COLONS)) ) {
                alt85=1;
            }
            switch (alt85) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:361:22: initExpr ( COMMA initExpr )*
                    {
                    pushFollow(FOLLOW_initExpr_in_initList3638);
                    initExpr249=initExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_initExpr.add(initExpr249.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:361:31: ( COMMA initExpr )*
                    loop84:
                    do {
                        int alt84=2;
                        int LA84_0 = input.LA(1);

                        if ( (LA84_0==COMMA) ) {
                            alt84=1;
                        }


                        switch (alt84) {
                    	case 1 :
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:361:32: COMMA initExpr
                    	    {
                    	    COMMA250=(Token)match(input,COMMA,FOLLOW_COMMA_in_initList3641); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA250);

                    	    pushFollow(FOLLOW_initExpr_in_initList3643);
                    	    initExpr251=initExpr();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_initExpr.add(initExpr251.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop84;
                        }
                    } while (true);


                    }
                    break;

            }

            RBRACKET252=(Token)match(input,RBRACKET,FOLLOW_RBRACKET_in_initList3649); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RBRACKET.add(RBRACKET252);



            // AST REWRITE
            // elements: initExpr
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 361:64: -> ^( INITLIST ( initExpr )* )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:361:67: ^( INITLIST ( initExpr )* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(INITLIST, "INITLIST"), root_1);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:361:78: ( initExpr )*
                while ( stream_initExpr.hasNext() ) {
                    adaptor.addChild(root_1, stream_initExpr.nextTree());

                }
                stream_initExpr.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "initList"

    public static class initExpr_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "initExpr"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:362:1: initExpr : ( ( rhsExpr )=>e= rhsExpr -> ^( INITEXPR $e) | PERIOD ID EQUALS ei= initElement -> ^( INITEXPR $ei ID ) | ( LBRACKET i= rhsExpr RBRACKET )=> LBRACKET i= rhsExpr RBRACKET EQUALS ei= initElement -> ^( INITEXPR $ei $i) | initList );
    public final EulangParser.initExpr_return initExpr() throws RecognitionException {
        EulangParser.initExpr_return retval = new EulangParser.initExpr_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token PERIOD253=null;
        Token ID254=null;
        Token EQUALS255=null;
        Token LBRACKET256=null;
        Token RBRACKET257=null;
        Token EQUALS258=null;
        EulangParser.rhsExpr_return e = null;

        EulangParser.initElement_return ei = null;

        EulangParser.rhsExpr_return i = null;

        EulangParser.initList_return initList259 = null;


        CommonTree PERIOD253_tree=null;
        CommonTree ID254_tree=null;
        CommonTree EQUALS255_tree=null;
        CommonTree LBRACKET256_tree=null;
        CommonTree RBRACKET257_tree=null;
        CommonTree EQUALS258_tree=null;
        RewriteRuleTokenStream stream_LBRACKET=new RewriteRuleTokenStream(adaptor,"token LBRACKET");
        RewriteRuleTokenStream stream_EQUALS=new RewriteRuleTokenStream(adaptor,"token EQUALS");
        RewriteRuleTokenStream stream_RBRACKET=new RewriteRuleTokenStream(adaptor,"token RBRACKET");
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
        RewriteRuleTokenStream stream_PERIOD=new RewriteRuleTokenStream(adaptor,"token PERIOD");
        RewriteRuleSubtreeStream stream_initElement=new RewriteRuleSubtreeStream(adaptor,"rule initElement");
        RewriteRuleSubtreeStream stream_rhsExpr=new RewriteRuleSubtreeStream(adaptor,"rule rhsExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:363:5: ( ( rhsExpr )=>e= rhsExpr -> ^( INITEXPR $e) | PERIOD ID EQUALS ei= initElement -> ^( INITEXPR $ei ID ) | ( LBRACKET i= rhsExpr RBRACKET )=> LBRACKET i= rhsExpr RBRACKET EQUALS ei= initElement -> ^( INITEXPR $ei $i) | initList )
            int alt86=4;
            alt86 = dfa86.predict(input);
            switch (alt86) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:363:7: ( rhsExpr )=>e= rhsExpr
                    {
                    pushFollow(FOLLOW_rhsExpr_in_initExpr3684);
                    e=rhsExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_rhsExpr.add(e.getTree());


                    // AST REWRITE
                    // elements: e
                    // token labels: 
                    // rule labels: retval, e
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                    RewriteRuleSubtreeStream stream_e=new RewriteRuleSubtreeStream(adaptor,"rule e",e!=null?e.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 363:75: -> ^( INITEXPR $e)
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:363:78: ^( INITEXPR $e)
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(INITEXPR, "INITEXPR"), root_1);

                        adaptor.addChild(root_1, stream_e.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:364:7: PERIOD ID EQUALS ei= initElement
                    {
                    PERIOD253=(Token)match(input,PERIOD,FOLLOW_PERIOD_in_initExpr3747); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_PERIOD.add(PERIOD253);

                    ID254=(Token)match(input,ID,FOLLOW_ID_in_initExpr3749); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ID.add(ID254);

                    EQUALS255=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_initExpr3751); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_EQUALS.add(EQUALS255);

                    pushFollow(FOLLOW_initElement_in_initExpr3755);
                    ei=initElement();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_initElement.add(ei.getTree());


                    // AST REWRITE
                    // elements: ei, ID
                    // token labels: 
                    // rule labels: retval, ei
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                    RewriteRuleSubtreeStream stream_ei=new RewriteRuleSubtreeStream(adaptor,"rule ei",ei!=null?ei.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 364:72: -> ^( INITEXPR $ei ID )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:364:75: ^( INITEXPR $ei ID )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(INITEXPR, "INITEXPR"), root_1);

                        adaptor.addChild(root_1, stream_ei.nextTree());
                        adaptor.addChild(root_1, stream_ID.nextNode());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 3 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:365:7: ( LBRACKET i= rhsExpr RBRACKET )=> LBRACKET i= rhsExpr RBRACKET EQUALS ei= initElement
                    {
                    LBRACKET256=(Token)match(input,LBRACKET,FOLLOW_LBRACKET_in_initExpr3820); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LBRACKET.add(LBRACKET256);

                    pushFollow(FOLLOW_rhsExpr_in_initExpr3824);
                    i=rhsExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_rhsExpr.add(i.getTree());
                    RBRACKET257=(Token)match(input,RBRACKET,FOLLOW_RBRACKET_in_initExpr3826); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RBRACKET.add(RBRACKET257);

                    EQUALS258=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_initExpr3828); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_EQUALS.add(EQUALS258);

                    pushFollow(FOLLOW_initElement_in_initExpr3832);
                    ei=initElement();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_initElement.add(ei.getTree());


                    // AST REWRITE
                    // elements: ei, i
                    // token labels: 
                    // rule labels: retval, ei, i
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                    RewriteRuleSubtreeStream stream_ei=new RewriteRuleSubtreeStream(adaptor,"rule ei",ei!=null?ei.tree:null);
                    RewriteRuleSubtreeStream stream_i=new RewriteRuleSubtreeStream(adaptor,"rule i",i!=null?i.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 365:107: -> ^( INITEXPR $ei $i)
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:365:110: ^( INITEXPR $ei $i)
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(INITEXPR, "INITEXPR"), root_1);

                        adaptor.addChild(root_1, stream_ei.nextTree());
                        adaptor.addChild(root_1, stream_i.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 4 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:366:7: initList
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_initList_in_initExpr3869);
                    initList259=initList();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, initList259.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "initExpr"

    public static class initElement_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "initElement"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:369:1: initElement : ( rhsExpr | initList );
    public final EulangParser.initElement_return initElement() throws RecognitionException {
        EulangParser.initElement_return retval = new EulangParser.initElement_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.rhsExpr_return rhsExpr260 = null;

        EulangParser.initList_return initList261 = null;



        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:369:13: ( rhsExpr | initList )
            int alt87=2;
            int LA87_0 = input.LA(1);

            if ( (LA87_0==CODE||(LA87_0>=SIZEOF && LA87_0<=TYPEOF)||LA87_0==ID||LA87_0==COLON||LA87_0==LPAREN||LA87_0==NIL||LA87_0==IF||LA87_0==NOT||(LA87_0>=TILDE && LA87_0<=AMP)||LA87_0==MINUS||(LA87_0>=PLUSPLUS && LA87_0<=STRING_LITERAL)||(LA87_0>=FALSE && LA87_0<=COLONS)) ) {
                alt87=1;
            }
            else if ( (LA87_0==LBRACKET) ) {
                alt87=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 87, 0, input);

                throw nvae;
            }
            switch (alt87) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:369:15: rhsExpr
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_rhsExpr_in_initElement3883);
                    rhsExpr260=rhsExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, rhsExpr260.getTree());

                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:369:25: initList
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_initList_in_initElement3887);
                    initList261=initList();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, initList261.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "initElement"

    public static class controlStmt_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "controlStmt"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:371:1: controlStmt : ( doWhile | whileDo | repeat | forIter );
    public final EulangParser.controlStmt_return controlStmt() throws RecognitionException {
        EulangParser.controlStmt_return retval = new EulangParser.controlStmt_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.doWhile_return doWhile262 = null;

        EulangParser.whileDo_return whileDo263 = null;

        EulangParser.repeat_return repeat264 = null;

        EulangParser.forIter_return forIter265 = null;



        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:371:13: ( doWhile | whileDo | repeat | forIter )
            int alt88=4;
            switch ( input.LA(1) ) {
            case DO:
                {
                alt88=1;
                }
                break;
            case WHILE:
                {
                alt88=2;
                }
                break;
            case REPEAT:
                {
                alt88=3;
                }
                break;
            case FOR:
                {
                alt88=4;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 88, 0, input);

                throw nvae;
            }

            switch (alt88) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:371:15: doWhile
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_doWhile_in_controlStmt3899);
                    doWhile262=doWhile();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, doWhile262.getTree());

                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:371:25: whileDo
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_whileDo_in_controlStmt3903);
                    whileDo263=whileDo();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, whileDo263.getTree());

                    }
                    break;
                case 3 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:371:35: repeat
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_repeat_in_controlStmt3907);
                    repeat264=repeat();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, repeat264.getTree());

                    }
                    break;
                case 4 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:371:44: forIter
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_forIter_in_controlStmt3911);
                    forIter265=forIter();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, forIter265.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "controlStmt"

    public static class doWhile_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "doWhile"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:373:1: doWhile : DO codeStmtExpr WHILE rhsExpr -> ^( DO codeStmtExpr rhsExpr ) ;
    public final EulangParser.doWhile_return doWhile() throws RecognitionException {
        EulangParser.doWhile_return retval = new EulangParser.doWhile_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token DO266=null;
        Token WHILE268=null;
        EulangParser.codeStmtExpr_return codeStmtExpr267 = null;

        EulangParser.rhsExpr_return rhsExpr269 = null;


        CommonTree DO266_tree=null;
        CommonTree WHILE268_tree=null;
        RewriteRuleTokenStream stream_DO=new RewriteRuleTokenStream(adaptor,"token DO");
        RewriteRuleTokenStream stream_WHILE=new RewriteRuleTokenStream(adaptor,"token WHILE");
        RewriteRuleSubtreeStream stream_codeStmtExpr=new RewriteRuleSubtreeStream(adaptor,"rule codeStmtExpr");
        RewriteRuleSubtreeStream stream_rhsExpr=new RewriteRuleSubtreeStream(adaptor,"rule rhsExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:373:9: ( DO codeStmtExpr WHILE rhsExpr -> ^( DO codeStmtExpr rhsExpr ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:373:11: DO codeStmtExpr WHILE rhsExpr
            {
            DO266=(Token)match(input,DO,FOLLOW_DO_in_doWhile3920); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_DO.add(DO266);

            pushFollow(FOLLOW_codeStmtExpr_in_doWhile3922);
            codeStmtExpr267=codeStmtExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_codeStmtExpr.add(codeStmtExpr267.getTree());
            WHILE268=(Token)match(input,WHILE,FOLLOW_WHILE_in_doWhile3924); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_WHILE.add(WHILE268);

            pushFollow(FOLLOW_rhsExpr_in_doWhile3926);
            rhsExpr269=rhsExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_rhsExpr.add(rhsExpr269.getTree());


            // AST REWRITE
            // elements: rhsExpr, codeStmtExpr, DO
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 373:43: -> ^( DO codeStmtExpr rhsExpr )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:373:46: ^( DO codeStmtExpr rhsExpr )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot(stream_DO.nextNode(), root_1);

                adaptor.addChild(root_1, stream_codeStmtExpr.nextTree());
                adaptor.addChild(root_1, stream_rhsExpr.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "doWhile"

    public static class whileDo_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "whileDo"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:376:1: whileDo : WHILE rhsExpr ( COLON | DO ) codeStmtExpr -> ^( WHILE rhsExpr codeStmtExpr ) ;
    public final EulangParser.whileDo_return whileDo() throws RecognitionException {
        EulangParser.whileDo_return retval = new EulangParser.whileDo_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token WHILE270=null;
        Token COLON272=null;
        Token DO273=null;
        EulangParser.rhsExpr_return rhsExpr271 = null;

        EulangParser.codeStmtExpr_return codeStmtExpr274 = null;


        CommonTree WHILE270_tree=null;
        CommonTree COLON272_tree=null;
        CommonTree DO273_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleTokenStream stream_DO=new RewriteRuleTokenStream(adaptor,"token DO");
        RewriteRuleTokenStream stream_WHILE=new RewriteRuleTokenStream(adaptor,"token WHILE");
        RewriteRuleSubtreeStream stream_codeStmtExpr=new RewriteRuleSubtreeStream(adaptor,"rule codeStmtExpr");
        RewriteRuleSubtreeStream stream_rhsExpr=new RewriteRuleSubtreeStream(adaptor,"rule rhsExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:376:9: ( WHILE rhsExpr ( COLON | DO ) codeStmtExpr -> ^( WHILE rhsExpr codeStmtExpr ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:376:11: WHILE rhsExpr ( COLON | DO ) codeStmtExpr
            {
            WHILE270=(Token)match(input,WHILE,FOLLOW_WHILE_in_whileDo3949); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_WHILE.add(WHILE270);

            pushFollow(FOLLOW_rhsExpr_in_whileDo3951);
            rhsExpr271=rhsExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_rhsExpr.add(rhsExpr271.getTree());
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:376:25: ( COLON | DO )
            int alt89=2;
            int LA89_0 = input.LA(1);

            if ( (LA89_0==COLON) ) {
                alt89=1;
            }
            else if ( (LA89_0==DO) ) {
                alt89=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 89, 0, input);

                throw nvae;
            }
            switch (alt89) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:376:26: COLON
                    {
                    COLON272=(Token)match(input,COLON,FOLLOW_COLON_in_whileDo3954); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COLON.add(COLON272);


                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:376:32: DO
                    {
                    DO273=(Token)match(input,DO,FOLLOW_DO_in_whileDo3956); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_DO.add(DO273);


                    }
                    break;

            }

            pushFollow(FOLLOW_codeStmtExpr_in_whileDo3959);
            codeStmtExpr274=codeStmtExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_codeStmtExpr.add(codeStmtExpr274.getTree());


            // AST REWRITE
            // elements: WHILE, codeStmtExpr, rhsExpr
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 376:51: -> ^( WHILE rhsExpr codeStmtExpr )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:376:54: ^( WHILE rhsExpr codeStmtExpr )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot(stream_WHILE.nextNode(), root_1);

                adaptor.addChild(root_1, stream_rhsExpr.nextTree());
                adaptor.addChild(root_1, stream_codeStmtExpr.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "whileDo"

    public static class repeat_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "repeat"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:379:1: repeat : REPEAT rhsExpr ( COLON | DO ) codeStmt -> ^( REPEAT rhsExpr codeStmt ) ;
    public final EulangParser.repeat_return repeat() throws RecognitionException {
        EulangParser.repeat_return retval = new EulangParser.repeat_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token REPEAT275=null;
        Token COLON277=null;
        Token DO278=null;
        EulangParser.rhsExpr_return rhsExpr276 = null;

        EulangParser.codeStmt_return codeStmt279 = null;


        CommonTree REPEAT275_tree=null;
        CommonTree COLON277_tree=null;
        CommonTree DO278_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleTokenStream stream_DO=new RewriteRuleTokenStream(adaptor,"token DO");
        RewriteRuleTokenStream stream_REPEAT=new RewriteRuleTokenStream(adaptor,"token REPEAT");
        RewriteRuleSubtreeStream stream_codeStmt=new RewriteRuleSubtreeStream(adaptor,"rule codeStmt");
        RewriteRuleSubtreeStream stream_rhsExpr=new RewriteRuleSubtreeStream(adaptor,"rule rhsExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:379:8: ( REPEAT rhsExpr ( COLON | DO ) codeStmt -> ^( REPEAT rhsExpr codeStmt ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:379:10: REPEAT rhsExpr ( COLON | DO ) codeStmt
            {
            REPEAT275=(Token)match(input,REPEAT,FOLLOW_REPEAT_in_repeat3984); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_REPEAT.add(REPEAT275);

            pushFollow(FOLLOW_rhsExpr_in_repeat3986);
            rhsExpr276=rhsExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_rhsExpr.add(rhsExpr276.getTree());
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:379:25: ( COLON | DO )
            int alt90=2;
            int LA90_0 = input.LA(1);

            if ( (LA90_0==COLON) ) {
                alt90=1;
            }
            else if ( (LA90_0==DO) ) {
                alt90=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 90, 0, input);

                throw nvae;
            }
            switch (alt90) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:379:26: COLON
                    {
                    COLON277=(Token)match(input,COLON,FOLLOW_COLON_in_repeat3989); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COLON.add(COLON277);


                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:379:32: DO
                    {
                    DO278=(Token)match(input,DO,FOLLOW_DO_in_repeat3991); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_DO.add(DO278);


                    }
                    break;

            }

            pushFollow(FOLLOW_codeStmt_in_repeat3994);
            codeStmt279=codeStmt();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_codeStmt.add(codeStmt279.getTree());


            // AST REWRITE
            // elements: REPEAT, rhsExpr, codeStmt
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 379:53: -> ^( REPEAT rhsExpr codeStmt )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:379:56: ^( REPEAT rhsExpr codeStmt )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot(stream_REPEAT.nextNode(), root_1);

                adaptor.addChild(root_1, stream_rhsExpr.nextTree());
                adaptor.addChild(root_1, stream_codeStmt.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "repeat"

    public static class forIter_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "forIter"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:382:1: forIter : FOR forIds ( forMovement )? IN rhsExpr ( COLON | DO ) codeStmt -> ^( FOR ^( LIST forIds ) ( forMovement )? rhsExpr codeStmt ) ;
    public final EulangParser.forIter_return forIter() throws RecognitionException {
        EulangParser.forIter_return retval = new EulangParser.forIter_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token FOR280=null;
        Token IN283=null;
        Token COLON285=null;
        Token DO286=null;
        EulangParser.forIds_return forIds281 = null;

        EulangParser.forMovement_return forMovement282 = null;

        EulangParser.rhsExpr_return rhsExpr284 = null;

        EulangParser.codeStmt_return codeStmt287 = null;


        CommonTree FOR280_tree=null;
        CommonTree IN283_tree=null;
        CommonTree COLON285_tree=null;
        CommonTree DO286_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleTokenStream stream_FOR=new RewriteRuleTokenStream(adaptor,"token FOR");
        RewriteRuleTokenStream stream_DO=new RewriteRuleTokenStream(adaptor,"token DO");
        RewriteRuleTokenStream stream_IN=new RewriteRuleTokenStream(adaptor,"token IN");
        RewriteRuleSubtreeStream stream_forMovement=new RewriteRuleSubtreeStream(adaptor,"rule forMovement");
        RewriteRuleSubtreeStream stream_codeStmt=new RewriteRuleSubtreeStream(adaptor,"rule codeStmt");
        RewriteRuleSubtreeStream stream_forIds=new RewriteRuleSubtreeStream(adaptor,"rule forIds");
        RewriteRuleSubtreeStream stream_rhsExpr=new RewriteRuleSubtreeStream(adaptor,"rule rhsExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:382:9: ( FOR forIds ( forMovement )? IN rhsExpr ( COLON | DO ) codeStmt -> ^( FOR ^( LIST forIds ) ( forMovement )? rhsExpr codeStmt ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:382:11: FOR forIds ( forMovement )? IN rhsExpr ( COLON | DO ) codeStmt
            {
            FOR280=(Token)match(input,FOR,FOLLOW_FOR_in_forIter4024); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_FOR.add(FOR280);

            pushFollow(FOLLOW_forIds_in_forIter4026);
            forIds281=forIds();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_forIds.add(forIds281.getTree());
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:382:22: ( forMovement )?
            int alt91=2;
            int LA91_0 = input.LA(1);

            if ( ((LA91_0>=BY && LA91_0<=AT)) ) {
                alt91=1;
            }
            switch (alt91) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:382:22: forMovement
                    {
                    pushFollow(FOLLOW_forMovement_in_forIter4028);
                    forMovement282=forMovement();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_forMovement.add(forMovement282.getTree());

                    }
                    break;

            }

            IN283=(Token)match(input,IN,FOLLOW_IN_in_forIter4031); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_IN.add(IN283);

            pushFollow(FOLLOW_rhsExpr_in_forIter4033);
            rhsExpr284=rhsExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_rhsExpr.add(rhsExpr284.getTree());
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:382:46: ( COLON | DO )
            int alt92=2;
            int LA92_0 = input.LA(1);

            if ( (LA92_0==COLON) ) {
                alt92=1;
            }
            else if ( (LA92_0==DO) ) {
                alt92=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 92, 0, input);

                throw nvae;
            }
            switch (alt92) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:382:47: COLON
                    {
                    COLON285=(Token)match(input,COLON,FOLLOW_COLON_in_forIter4036); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COLON.add(COLON285);


                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:382:53: DO
                    {
                    DO286=(Token)match(input,DO,FOLLOW_DO_in_forIter4038); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_DO.add(DO286);


                    }
                    break;

            }

            pushFollow(FOLLOW_codeStmt_in_forIter4041);
            codeStmt287=codeStmt();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_codeStmt.add(codeStmt287.getTree());


            // AST REWRITE
            // elements: codeStmt, forMovement, forIds, rhsExpr, FOR
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 382:72: -> ^( FOR ^( LIST forIds ) ( forMovement )? rhsExpr codeStmt )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:382:75: ^( FOR ^( LIST forIds ) ( forMovement )? rhsExpr codeStmt )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot(stream_FOR.nextNode(), root_1);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:382:81: ^( LIST forIds )
                {
                CommonTree root_2 = (CommonTree)adaptor.nil();
                root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(LIST, "LIST"), root_2);

                adaptor.addChild(root_2, stream_forIds.nextTree());

                adaptor.addChild(root_1, root_2);
                }
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:382:96: ( forMovement )?
                if ( stream_forMovement.hasNext() ) {
                    adaptor.addChild(root_1, stream_forMovement.nextTree());

                }
                stream_forMovement.reset();
                adaptor.addChild(root_1, stream_rhsExpr.nextTree());
                adaptor.addChild(root_1, stream_codeStmt.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "forIter"

    public static class forIds_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "forIds"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:385:1: forIds : ID ( AND ID )* -> ( ID )+ ;
    public final EulangParser.forIds_return forIds() throws RecognitionException {
        EulangParser.forIds_return retval = new EulangParser.forIds_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ID288=null;
        Token AND289=null;
        Token ID290=null;

        CommonTree ID288_tree=null;
        CommonTree AND289_tree=null;
        CommonTree ID290_tree=null;
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
        RewriteRuleTokenStream stream_AND=new RewriteRuleTokenStream(adaptor,"token AND");

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:385:8: ( ID ( AND ID )* -> ( ID )+ )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:385:10: ID ( AND ID )*
            {
            ID288=(Token)match(input,ID,FOLLOW_ID_in_forIds4078); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_ID.add(ID288);

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:385:13: ( AND ID )*
            loop93:
            do {
                int alt93=2;
                int LA93_0 = input.LA(1);

                if ( (LA93_0==AND) ) {
                    alt93=1;
                }


                switch (alt93) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:385:14: AND ID
            	    {
            	    AND289=(Token)match(input,AND,FOLLOW_AND_in_forIds4081); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_AND.add(AND289);

            	    ID290=(Token)match(input,ID,FOLLOW_ID_in_forIds4083); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_ID.add(ID290);


            	    }
            	    break;

            	default :
            	    break loop93;
                }
            } while (true);



            // AST REWRITE
            // elements: ID
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 385:23: -> ( ID )+
            {
                if ( !(stream_ID.hasNext()) ) {
                    throw new RewriteEarlyExitException();
                }
                while ( stream_ID.hasNext() ) {
                    adaptor.addChild(root_0, stream_ID.nextNode());

                }
                stream_ID.reset();

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "forIds"

    public static class forMovement_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "forMovement"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:387:1: forMovement : ( atId | stepping );
    public final EulangParser.forMovement_return forMovement() throws RecognitionException {
        EulangParser.forMovement_return retval = new EulangParser.forMovement_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.atId_return atId291 = null;

        EulangParser.stepping_return stepping292 = null;



        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:387:13: ( atId | stepping )
            int alt94=2;
            int LA94_0 = input.LA(1);

            if ( (LA94_0==AT) ) {
                alt94=1;
            }
            else if ( (LA94_0==BY) ) {
                alt94=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 94, 0, input);

                throw nvae;
            }
            switch (alt94) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:387:15: atId
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_atId_in_forMovement4099);
                    atId291=atId();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, atId291.getTree());

                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:387:22: stepping
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_stepping_in_forMovement4103);
                    stepping292=stepping();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, stepping292.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "forMovement"

    public static class stepping_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "stepping"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:389:1: stepping : BY rhsExpr -> ^( BY rhsExpr ) ;
    public final EulangParser.stepping_return stepping() throws RecognitionException {
        EulangParser.stepping_return retval = new EulangParser.stepping_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token BY293=null;
        EulangParser.rhsExpr_return rhsExpr294 = null;


        CommonTree BY293_tree=null;
        RewriteRuleTokenStream stream_BY=new RewriteRuleTokenStream(adaptor,"token BY");
        RewriteRuleSubtreeStream stream_rhsExpr=new RewriteRuleSubtreeStream(adaptor,"rule rhsExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:389:10: ( BY rhsExpr -> ^( BY rhsExpr ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:389:12: BY rhsExpr
            {
            BY293=(Token)match(input,BY,FOLLOW_BY_in_stepping4112); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_BY.add(BY293);

            pushFollow(FOLLOW_rhsExpr_in_stepping4114);
            rhsExpr294=rhsExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_rhsExpr.add(rhsExpr294.getTree());


            // AST REWRITE
            // elements: rhsExpr, BY
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 389:23: -> ^( BY rhsExpr )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:389:26: ^( BY rhsExpr )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot(stream_BY.nextNode(), root_1);

                adaptor.addChild(root_1, stream_rhsExpr.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "stepping"

    public static class atId_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "atId"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:391:1: atId : AT ID -> ^( AT ID ) ;
    public final EulangParser.atId_return atId() throws RecognitionException {
        EulangParser.atId_return retval = new EulangParser.atId_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token AT295=null;
        Token ID296=null;

        CommonTree AT295_tree=null;
        CommonTree ID296_tree=null;
        RewriteRuleTokenStream stream_AT=new RewriteRuleTokenStream(adaptor,"token AT");
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:391:6: ( AT ID -> ^( AT ID ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:391:8: AT ID
            {
            AT295=(Token)match(input,AT,FOLLOW_AT_in_atId4131); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_AT.add(AT295);

            ID296=(Token)match(input,ID,FOLLOW_ID_in_atId4133); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_ID.add(ID296);



            // AST REWRITE
            // elements: ID, AT
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 391:17: -> ^( AT ID )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:391:20: ^( AT ID )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot(stream_AT.nextNode(), root_1);

                adaptor.addChild(root_1, stream_ID.nextNode());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "atId"

    public static class breakStmt_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "breakStmt"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:395:1: breakStmt : BREAK rhsExpr -> ^( BREAK rhsExpr ) ;
    public final EulangParser.breakStmt_return breakStmt() throws RecognitionException {
        EulangParser.breakStmt_return retval = new EulangParser.breakStmt_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token BREAK297=null;
        EulangParser.rhsExpr_return rhsExpr298 = null;


        CommonTree BREAK297_tree=null;
        RewriteRuleTokenStream stream_BREAK=new RewriteRuleTokenStream(adaptor,"token BREAK");
        RewriteRuleSubtreeStream stream_rhsExpr=new RewriteRuleSubtreeStream(adaptor,"rule rhsExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:395:11: ( BREAK rhsExpr -> ^( BREAK rhsExpr ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:395:13: BREAK rhsExpr
            {
            BREAK297=(Token)match(input,BREAK,FOLLOW_BREAK_in_breakStmt4161); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_BREAK.add(BREAK297);

            pushFollow(FOLLOW_rhsExpr_in_breakStmt4163);
            rhsExpr298=rhsExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_rhsExpr.add(rhsExpr298.getTree());


            // AST REWRITE
            // elements: rhsExpr, BREAK
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 395:27: -> ^( BREAK rhsExpr )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:395:31: ^( BREAK rhsExpr )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot(stream_BREAK.nextNode(), root_1);

                adaptor.addChild(root_1, stream_rhsExpr.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "breakStmt"

    public static class labelStmt_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "labelStmt"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:403:1: labelStmt : ATSIGN ID COLON -> ^( LABEL ID ) ;
    public final EulangParser.labelStmt_return labelStmt() throws RecognitionException {
        EulangParser.labelStmt_return retval = new EulangParser.labelStmt_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ATSIGN299=null;
        Token ID300=null;
        Token COLON301=null;

        CommonTree ATSIGN299_tree=null;
        CommonTree ID300_tree=null;
        CommonTree COLON301_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleTokenStream stream_ATSIGN=new RewriteRuleTokenStream(adaptor,"token ATSIGN");
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:403:10: ( ATSIGN ID COLON -> ^( LABEL ID ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:403:12: ATSIGN ID COLON
            {
            ATSIGN299=(Token)match(input,ATSIGN,FOLLOW_ATSIGN_in_labelStmt4191); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_ATSIGN.add(ATSIGN299);

            ID300=(Token)match(input,ID,FOLLOW_ID_in_labelStmt4193); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_ID.add(ID300);

            COLON301=(Token)match(input,COLON,FOLLOW_COLON_in_labelStmt4195); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_COLON.add(COLON301);



            // AST REWRITE
            // elements: ID
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 403:47: -> ^( LABEL ID )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:403:50: ^( LABEL ID )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(LABEL, "LABEL"), root_1);

                adaptor.addChild(root_1, stream_ID.nextNode());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "labelStmt"

    public static class gotoStmt_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "gotoStmt"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:405:1: gotoStmt : GOTO idOrScopeRef ( IF rhsExpr )? -> ^( GOTO idOrScopeRef ( rhsExpr )? ) ;
    public final EulangParser.gotoStmt_return gotoStmt() throws RecognitionException {
        EulangParser.gotoStmt_return retval = new EulangParser.gotoStmt_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token GOTO302=null;
        Token IF304=null;
        EulangParser.idOrScopeRef_return idOrScopeRef303 = null;

        EulangParser.rhsExpr_return rhsExpr305 = null;


        CommonTree GOTO302_tree=null;
        CommonTree IF304_tree=null;
        RewriteRuleTokenStream stream_GOTO=new RewriteRuleTokenStream(adaptor,"token GOTO");
        RewriteRuleTokenStream stream_IF=new RewriteRuleTokenStream(adaptor,"token IF");
        RewriteRuleSubtreeStream stream_idOrScopeRef=new RewriteRuleSubtreeStream(adaptor,"rule idOrScopeRef");
        RewriteRuleSubtreeStream stream_rhsExpr=new RewriteRuleSubtreeStream(adaptor,"rule rhsExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:405:9: ( GOTO idOrScopeRef ( IF rhsExpr )? -> ^( GOTO idOrScopeRef ( rhsExpr )? ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:405:11: GOTO idOrScopeRef ( IF rhsExpr )?
            {
            GOTO302=(Token)match(input,GOTO,FOLLOW_GOTO_in_gotoStmt4231); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_GOTO.add(GOTO302);

            pushFollow(FOLLOW_idOrScopeRef_in_gotoStmt4233);
            idOrScopeRef303=idOrScopeRef();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_idOrScopeRef.add(idOrScopeRef303.getTree());
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:405:29: ( IF rhsExpr )?
            int alt95=2;
            int LA95_0 = input.LA(1);

            if ( (LA95_0==IF) ) {
                alt95=1;
            }
            switch (alt95) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:405:30: IF rhsExpr
                    {
                    IF304=(Token)match(input,IF,FOLLOW_IF_in_gotoStmt4236); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_IF.add(IF304);

                    pushFollow(FOLLOW_rhsExpr_in_gotoStmt4238);
                    rhsExpr305=rhsExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_rhsExpr.add(rhsExpr305.getTree());

                    }
                    break;

            }



            // AST REWRITE
            // elements: rhsExpr, idOrScopeRef, GOTO
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 405:53: -> ^( GOTO idOrScopeRef ( rhsExpr )? )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:405:56: ^( GOTO idOrScopeRef ( rhsExpr )? )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot(stream_GOTO.nextNode(), root_1);

                adaptor.addChild(root_1, stream_idOrScopeRef.nextTree());
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:405:76: ( rhsExpr )?
                if ( stream_rhsExpr.hasNext() ) {
                    adaptor.addChild(root_1, stream_rhsExpr.nextTree());

                }
                stream_rhsExpr.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "gotoStmt"

    public static class blockStmt_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "blockStmt"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:408:1: blockStmt : LBRACE codestmtlist RBRACE -> ^( BLOCK codestmtlist ) ;
    public final EulangParser.blockStmt_return blockStmt() throws RecognitionException {
        EulangParser.blockStmt_return retval = new EulangParser.blockStmt_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LBRACE306=null;
        Token RBRACE308=null;
        EulangParser.codestmtlist_return codestmtlist307 = null;


        CommonTree LBRACE306_tree=null;
        CommonTree RBRACE308_tree=null;
        RewriteRuleTokenStream stream_RBRACE=new RewriteRuleTokenStream(adaptor,"token RBRACE");
        RewriteRuleTokenStream stream_LBRACE=new RewriteRuleTokenStream(adaptor,"token LBRACE");
        RewriteRuleSubtreeStream stream_codestmtlist=new RewriteRuleSubtreeStream(adaptor,"rule codestmtlist");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:408:10: ( LBRACE codestmtlist RBRACE -> ^( BLOCK codestmtlist ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:408:12: LBRACE codestmtlist RBRACE
            {
            LBRACE306=(Token)match(input,LBRACE,FOLLOW_LBRACE_in_blockStmt4273); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LBRACE.add(LBRACE306);

            pushFollow(FOLLOW_codestmtlist_in_blockStmt4275);
            codestmtlist307=codestmtlist();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_codestmtlist.add(codestmtlist307.getTree());
            RBRACE308=(Token)match(input,RBRACE,FOLLOW_RBRACE_in_blockStmt4277); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RBRACE.add(RBRACE308);



            // AST REWRITE
            // elements: codestmtlist
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 408:43: -> ^( BLOCK codestmtlist )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:408:46: ^( BLOCK codestmtlist )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(BLOCK, "BLOCK"), root_1);

                adaptor.addChild(root_1, stream_codestmtlist.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "blockStmt"

    public static class tuple_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "tuple"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:411:1: tuple : LPAREN tupleEntries RPAREN -> ^( TUPLE ( tupleEntries )+ ) ;
    public final EulangParser.tuple_return tuple() throws RecognitionException {
        EulangParser.tuple_return retval = new EulangParser.tuple_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LPAREN309=null;
        Token RPAREN311=null;
        EulangParser.tupleEntries_return tupleEntries310 = null;


        CommonTree LPAREN309_tree=null;
        CommonTree RPAREN311_tree=null;
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleSubtreeStream stream_tupleEntries=new RewriteRuleSubtreeStream(adaptor,"rule tupleEntries");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:411:7: ( LPAREN tupleEntries RPAREN -> ^( TUPLE ( tupleEntries )+ ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:411:9: LPAREN tupleEntries RPAREN
            {
            LPAREN309=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_tuple4300); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN309);

            pushFollow(FOLLOW_tupleEntries_in_tuple4302);
            tupleEntries310=tupleEntries();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_tupleEntries.add(tupleEntries310.getTree());
            RPAREN311=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_tuple4304); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN311);



            // AST REWRITE
            // elements: tupleEntries
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 411:41: -> ^( TUPLE ( tupleEntries )+ )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:411:44: ^( TUPLE ( tupleEntries )+ )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(TUPLE, "TUPLE"), root_1);

                if ( !(stream_tupleEntries.hasNext()) ) {
                    throw new RewriteEarlyExitException();
                }
                while ( stream_tupleEntries.hasNext() ) {
                    adaptor.addChild(root_1, stream_tupleEntries.nextTree());

                }
                stream_tupleEntries.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "tuple"

    public static class tupleEntries_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "tupleEntries"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:414:1: tupleEntries : assignExpr ( COMMA assignExpr )+ -> ( assignExpr )+ ;
    public final EulangParser.tupleEntries_return tupleEntries() throws RecognitionException {
        EulangParser.tupleEntries_return retval = new EulangParser.tupleEntries_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COMMA313=null;
        EulangParser.assignExpr_return assignExpr312 = null;

        EulangParser.assignExpr_return assignExpr314 = null;


        CommonTree COMMA313_tree=null;
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleSubtreeStream stream_assignExpr=new RewriteRuleSubtreeStream(adaptor,"rule assignExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:414:14: ( assignExpr ( COMMA assignExpr )+ -> ( assignExpr )+ )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:414:16: assignExpr ( COMMA assignExpr )+
            {
            pushFollow(FOLLOW_assignExpr_in_tupleEntries4332);
            assignExpr312=assignExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_assignExpr.add(assignExpr312.getTree());
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:414:27: ( COMMA assignExpr )+
            int cnt96=0;
            loop96:
            do {
                int alt96=2;
                int LA96_0 = input.LA(1);

                if ( (LA96_0==COMMA) ) {
                    alt96=1;
                }


                switch (alt96) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:414:28: COMMA assignExpr
            	    {
            	    COMMA313=(Token)match(input,COMMA,FOLLOW_COMMA_in_tupleEntries4335); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA313);

            	    pushFollow(FOLLOW_assignExpr_in_tupleEntries4337);
            	    assignExpr314=assignExpr();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_assignExpr.add(assignExpr314.getTree());

            	    }
            	    break;

            	default :
            	    if ( cnt96 >= 1 ) break loop96;
            	    if (state.backtracking>0) {state.failed=true; return retval;}
                        EarlyExitException eee =
                            new EarlyExitException(96, input);
                        throw eee;
                }
                cnt96++;
            } while (true);



            // AST REWRITE
            // elements: assignExpr
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 414:48: -> ( assignExpr )+
            {
                if ( !(stream_assignExpr.hasNext()) ) {
                    throw new RewriteEarlyExitException();
                }
                while ( stream_assignExpr.hasNext() ) {
                    adaptor.addChild(root_0, stream_assignExpr.nextTree());

                }
                stream_assignExpr.reset();

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "tupleEntries"

    public static class idTuple_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "idTuple"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:417:1: idTuple : LPAREN idTupleEntries RPAREN -> ^( TUPLE ( idTupleEntries )+ ) ;
    public final EulangParser.idTuple_return idTuple() throws RecognitionException {
        EulangParser.idTuple_return retval = new EulangParser.idTuple_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LPAREN315=null;
        Token RPAREN317=null;
        EulangParser.idTupleEntries_return idTupleEntries316 = null;


        CommonTree LPAREN315_tree=null;
        CommonTree RPAREN317_tree=null;
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleSubtreeStream stream_idTupleEntries=new RewriteRuleSubtreeStream(adaptor,"rule idTupleEntries");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:417:9: ( LPAREN idTupleEntries RPAREN -> ^( TUPLE ( idTupleEntries )+ ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:417:11: LPAREN idTupleEntries RPAREN
            {
            LPAREN315=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_idTuple4356); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN315);

            pushFollow(FOLLOW_idTupleEntries_in_idTuple4358);
            idTupleEntries316=idTupleEntries();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_idTupleEntries.add(idTupleEntries316.getTree());
            RPAREN317=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_idTuple4360); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN317);



            // AST REWRITE
            // elements: idTupleEntries
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 417:45: -> ^( TUPLE ( idTupleEntries )+ )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:417:48: ^( TUPLE ( idTupleEntries )+ )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(TUPLE, "TUPLE"), root_1);

                if ( !(stream_idTupleEntries.hasNext()) ) {
                    throw new RewriteEarlyExitException();
                }
                while ( stream_idTupleEntries.hasNext() ) {
                    adaptor.addChild(root_1, stream_idTupleEntries.nextTree());

                }
                stream_idTupleEntries.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "idTuple"

    public static class idTupleEntries_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "idTupleEntries"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:420:1: idTupleEntries : idOrScopeRef ( COMMA idOrScopeRef )+ -> ( idOrScopeRef )+ ;
    public final EulangParser.idTupleEntries_return idTupleEntries() throws RecognitionException {
        EulangParser.idTupleEntries_return retval = new EulangParser.idTupleEntries_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COMMA319=null;
        EulangParser.idOrScopeRef_return idOrScopeRef318 = null;

        EulangParser.idOrScopeRef_return idOrScopeRef320 = null;


        CommonTree COMMA319_tree=null;
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleSubtreeStream stream_idOrScopeRef=new RewriteRuleSubtreeStream(adaptor,"rule idOrScopeRef");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:420:16: ( idOrScopeRef ( COMMA idOrScopeRef )+ -> ( idOrScopeRef )+ )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:420:18: idOrScopeRef ( COMMA idOrScopeRef )+
            {
            pushFollow(FOLLOW_idOrScopeRef_in_idTupleEntries4388);
            idOrScopeRef318=idOrScopeRef();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_idOrScopeRef.add(idOrScopeRef318.getTree());
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:420:31: ( COMMA idOrScopeRef )+
            int cnt97=0;
            loop97:
            do {
                int alt97=2;
                int LA97_0 = input.LA(1);

                if ( (LA97_0==COMMA) ) {
                    alt97=1;
                }


                switch (alt97) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:420:32: COMMA idOrScopeRef
            	    {
            	    COMMA319=(Token)match(input,COMMA,FOLLOW_COMMA_in_idTupleEntries4391); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA319);

            	    pushFollow(FOLLOW_idOrScopeRef_in_idTupleEntries4393);
            	    idOrScopeRef320=idOrScopeRef();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_idOrScopeRef.add(idOrScopeRef320.getTree());

            	    }
            	    break;

            	default :
            	    if ( cnt97 >= 1 ) break loop97;
            	    if (state.backtracking>0) {state.failed=true; return retval;}
                        EarlyExitException eee =
                            new EarlyExitException(97, input);
                        throw eee;
                }
                cnt97++;
            } while (true);



            // AST REWRITE
            // elements: idOrScopeRef
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 420:54: -> ( idOrScopeRef )+
            {
                if ( !(stream_idOrScopeRef.hasNext()) ) {
                    throw new RewriteEarlyExitException();
                }
                while ( stream_idOrScopeRef.hasNext() ) {
                    adaptor.addChild(root_0, stream_idOrScopeRef.nextTree());

                }
                stream_idOrScopeRef.reset();

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "idTupleEntries"

    public static class rhsExpr_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "rhsExpr"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:423:1: rhsExpr : condStar -> condStar ;
    public final EulangParser.rhsExpr_return rhsExpr() throws RecognitionException {
        EulangParser.rhsExpr_return retval = new EulangParser.rhsExpr_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.condStar_return condStar321 = null;


        RewriteRuleSubtreeStream stream_condStar=new RewriteRuleSubtreeStream(adaptor,"rule condStar");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:423:9: ( condStar -> condStar )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:423:13: condStar
            {
            pushFollow(FOLLOW_condStar_in_rhsExpr4414);
            condStar321=condStar();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_condStar.add(condStar321.getTree());


            // AST REWRITE
            // elements: condStar
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 423:22: -> condStar
            {
                adaptor.addChild(root_0, stream_condStar.nextTree());

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "rhsExpr"

    public static class arglist_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "arglist"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:426:1: arglist : ( arg ( COMMA arg )* ( COMMA )? )? -> ^( ARGLIST ( arg )* ) ;
    public final EulangParser.arglist_return arglist() throws RecognitionException {
        EulangParser.arglist_return retval = new EulangParser.arglist_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COMMA323=null;
        Token COMMA325=null;
        EulangParser.arg_return arg322 = null;

        EulangParser.arg_return arg324 = null;


        CommonTree COMMA323_tree=null;
        CommonTree COMMA325_tree=null;
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleSubtreeStream stream_arg=new RewriteRuleSubtreeStream(adaptor,"rule arg");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:426:8: ( ( arg ( COMMA arg )* ( COMMA )? )? -> ^( ARGLIST ( arg )* ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:426:10: ( arg ( COMMA arg )* ( COMMA )? )?
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:426:10: ( arg ( COMMA arg )* ( COMMA )? )?
            int alt100=2;
            int LA100_0 = input.LA(1);

            if ( (LA100_0==CODE||LA100_0==GOTO||(LA100_0>=SIZEOF && LA100_0<=TYPEOF)||LA100_0==ID||LA100_0==COLON||LA100_0==LBRACE||LA100_0==LPAREN||LA100_0==NIL||LA100_0==IF||LA100_0==NOT||(LA100_0>=TILDE && LA100_0<=AMP)||LA100_0==MINUS||(LA100_0>=PLUSPLUS && LA100_0<=STRING_LITERAL)||(LA100_0>=FALSE && LA100_0<=COLONS)) ) {
                alt100=1;
            }
            switch (alt100) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:426:11: arg ( COMMA arg )* ( COMMA )?
                    {
                    pushFollow(FOLLOW_arg_in_arglist4435);
                    arg322=arg();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_arg.add(arg322.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:426:15: ( COMMA arg )*
                    loop98:
                    do {
                        int alt98=2;
                        int LA98_0 = input.LA(1);

                        if ( (LA98_0==COMMA) ) {
                            int LA98_1 = input.LA(2);

                            if ( (LA98_1==CODE||LA98_1==GOTO||(LA98_1>=SIZEOF && LA98_1<=TYPEOF)||LA98_1==ID||LA98_1==COLON||LA98_1==LBRACE||LA98_1==LPAREN||LA98_1==NIL||LA98_1==IF||LA98_1==NOT||(LA98_1>=TILDE && LA98_1<=AMP)||LA98_1==MINUS||(LA98_1>=PLUSPLUS && LA98_1<=STRING_LITERAL)||(LA98_1>=FALSE && LA98_1<=COLONS)) ) {
                                alt98=1;
                            }


                        }


                        switch (alt98) {
                    	case 1 :
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:426:17: COMMA arg
                    	    {
                    	    COMMA323=(Token)match(input,COMMA,FOLLOW_COMMA_in_arglist4439); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA323);

                    	    pushFollow(FOLLOW_arg_in_arglist4441);
                    	    arg324=arg();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_arg.add(arg324.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop98;
                        }
                    } while (true);

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:426:29: ( COMMA )?
                    int alt99=2;
                    int LA99_0 = input.LA(1);

                    if ( (LA99_0==COMMA) ) {
                        alt99=1;
                    }
                    switch (alt99) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:426:29: COMMA
                            {
                            COMMA325=(Token)match(input,COMMA,FOLLOW_COMMA_in_arglist4445); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_COMMA.add(COMMA325);


                            }
                            break;

                    }


                    }
                    break;

            }



            // AST REWRITE
            // elements: arg
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 426:61: -> ^( ARGLIST ( arg )* )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:426:64: ^( ARGLIST ( arg )* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ARGLIST, "ARGLIST"), root_1);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:426:74: ( arg )*
                while ( stream_arg.hasNext() ) {
                    adaptor.addChild(root_1, stream_arg.nextTree());

                }
                stream_arg.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "arglist"

    public static class arg_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "arg"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:429:1: arg : ( assignExpr -> ^( EXPR assignExpr ) | LBRACE codestmtlist RBRACE -> ^( EXPR ^( CODE codestmtlist ) ) | gotoStmt -> ^( EXPR gotoStmt ) );
    public final EulangParser.arg_return arg() throws RecognitionException {
        EulangParser.arg_return retval = new EulangParser.arg_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LBRACE327=null;
        Token RBRACE329=null;
        EulangParser.assignExpr_return assignExpr326 = null;

        EulangParser.codestmtlist_return codestmtlist328 = null;

        EulangParser.gotoStmt_return gotoStmt330 = null;


        CommonTree LBRACE327_tree=null;
        CommonTree RBRACE329_tree=null;
        RewriteRuleTokenStream stream_RBRACE=new RewriteRuleTokenStream(adaptor,"token RBRACE");
        RewriteRuleTokenStream stream_LBRACE=new RewriteRuleTokenStream(adaptor,"token LBRACE");
        RewriteRuleSubtreeStream stream_gotoStmt=new RewriteRuleSubtreeStream(adaptor,"rule gotoStmt");
        RewriteRuleSubtreeStream stream_codestmtlist=new RewriteRuleSubtreeStream(adaptor,"rule codestmtlist");
        RewriteRuleSubtreeStream stream_assignExpr=new RewriteRuleSubtreeStream(adaptor,"rule assignExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:429:4: ( assignExpr -> ^( EXPR assignExpr ) | LBRACE codestmtlist RBRACE -> ^( EXPR ^( CODE codestmtlist ) ) | gotoStmt -> ^( EXPR gotoStmt ) )
            int alt101=3;
            switch ( input.LA(1) ) {
            case CODE:
            case SIZEOF:
            case TYPEOF:
            case ID:
            case COLON:
            case LPAREN:
            case NIL:
            case IF:
            case NOT:
            case TILDE:
            case AMP:
            case MINUS:
            case PLUSPLUS:
            case MINUSMINUS:
            case NUMBER:
            case CHAR_LITERAL:
            case STRING_LITERAL:
            case FALSE:
            case TRUE:
            case COLONS:
                {
                alt101=1;
                }
                break;
            case LBRACE:
                {
                alt101=2;
                }
                break;
            case GOTO:
                {
                alt101=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 101, 0, input);

                throw nvae;
            }

            switch (alt101) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:429:7: assignExpr
                    {
                    pushFollow(FOLLOW_assignExpr_in_arg4494);
                    assignExpr326=assignExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_assignExpr.add(assignExpr326.getTree());


                    // AST REWRITE
                    // elements: assignExpr
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 429:37: -> ^( EXPR assignExpr )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:429:40: ^( EXPR assignExpr )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(EXPR, "EXPR"), root_1);

                        adaptor.addChild(root_1, stream_assignExpr.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:430:5: LBRACE codestmtlist RBRACE
                    {
                    LBRACE327=(Token)match(input,LBRACE,FOLLOW_LBRACE_in_arg4527); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LBRACE.add(LBRACE327);

                    pushFollow(FOLLOW_codestmtlist_in_arg4529);
                    codestmtlist328=codestmtlist();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_codestmtlist.add(codestmtlist328.getTree());
                    RBRACE329=(Token)match(input,RBRACE,FOLLOW_RBRACE_in_arg4531); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RBRACE.add(RBRACE329);



                    // AST REWRITE
                    // elements: codestmtlist
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 430:37: -> ^( EXPR ^( CODE codestmtlist ) )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:430:40: ^( EXPR ^( CODE codestmtlist ) )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(EXPR, "EXPR"), root_1);

                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:430:47: ^( CODE codestmtlist )
                        {
                        CommonTree root_2 = (CommonTree)adaptor.nil();
                        root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(CODE, "CODE"), root_2);

                        adaptor.addChild(root_2, stream_codestmtlist.nextTree());

                        adaptor.addChild(root_1, root_2);
                        }

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 3 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:431:5: gotoStmt
                    {
                    pushFollow(FOLLOW_gotoStmt_in_arg4555);
                    gotoStmt330=gotoStmt();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_gotoStmt.add(gotoStmt330.getTree());


                    // AST REWRITE
                    // elements: gotoStmt
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 431:37: -> ^( EXPR gotoStmt )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:431:40: ^( EXPR gotoStmt )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(EXPR, "EXPR"), root_1);

                        adaptor.addChild(root_1, stream_gotoStmt.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "arg"

    public static class condStar_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "condStar"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:452:1: condStar : ( cond -> cond | IF ifExprs -> ifExprs );
    public final EulangParser.condStar_return condStar() throws RecognitionException {
        EulangParser.condStar_return retval = new EulangParser.condStar_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token IF332=null;
        EulangParser.cond_return cond331 = null;

        EulangParser.ifExprs_return ifExprs333 = null;


        CommonTree IF332_tree=null;
        RewriteRuleTokenStream stream_IF=new RewriteRuleTokenStream(adaptor,"token IF");
        RewriteRuleSubtreeStream stream_ifExprs=new RewriteRuleSubtreeStream(adaptor,"rule ifExprs");
        RewriteRuleSubtreeStream stream_cond=new RewriteRuleSubtreeStream(adaptor,"rule cond");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:452:9: ( cond -> cond | IF ifExprs -> ifExprs )
            int alt102=2;
            int LA102_0 = input.LA(1);

            if ( (LA102_0==CODE||(LA102_0>=SIZEOF && LA102_0<=TYPEOF)||LA102_0==ID||LA102_0==COLON||LA102_0==LPAREN||LA102_0==NIL||LA102_0==NOT||(LA102_0>=TILDE && LA102_0<=AMP)||LA102_0==MINUS||(LA102_0>=PLUSPLUS && LA102_0<=STRING_LITERAL)||(LA102_0>=FALSE && LA102_0<=COLONS)) ) {
                alt102=1;
            }
            else if ( (LA102_0==IF) ) {
                alt102=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 102, 0, input);

                throw nvae;
            }
            switch (alt102) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:452:11: cond
                    {
                    pushFollow(FOLLOW_cond_in_condStar4616);
                    cond331=cond();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_cond.add(cond331.getTree());


                    // AST REWRITE
                    // elements: cond
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 452:16: -> cond
                    {
                        adaptor.addChild(root_0, stream_cond.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:453:6: IF ifExprs
                    {
                    IF332=(Token)match(input,IF,FOLLOW_IF_in_condStar4627); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_IF.add(IF332);

                    pushFollow(FOLLOW_ifExprs_in_condStar4629);
                    ifExprs333=ifExprs();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ifExprs.add(ifExprs333.getTree());


                    // AST REWRITE
                    // elements: ifExprs
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 453:17: -> ifExprs
                    {
                        adaptor.addChild(root_0, stream_ifExprs.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "condStar"

    public static class ifExprs_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "ifExprs"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:459:1: ifExprs : thenClause elses -> ^( CONDLIST thenClause elses ) ;
    public final EulangParser.ifExprs_return ifExprs() throws RecognitionException {
        EulangParser.ifExprs_return retval = new EulangParser.ifExprs_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.thenClause_return thenClause334 = null;

        EulangParser.elses_return elses335 = null;


        RewriteRuleSubtreeStream stream_thenClause=new RewriteRuleSubtreeStream(adaptor,"rule thenClause");
        RewriteRuleSubtreeStream stream_elses=new RewriteRuleSubtreeStream(adaptor,"rule elses");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:459:9: ( thenClause elses -> ^( CONDLIST thenClause elses ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:459:11: thenClause elses
            {
            pushFollow(FOLLOW_thenClause_in_ifExprs4648);
            thenClause334=thenClause();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_thenClause.add(thenClause334.getTree());
            pushFollow(FOLLOW_elses_in_ifExprs4650);
            elses335=elses();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_elses.add(elses335.getTree());


            // AST REWRITE
            // elements: thenClause, elses
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 459:28: -> ^( CONDLIST thenClause elses )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:459:31: ^( CONDLIST thenClause elses )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(CONDLIST, "CONDLIST"), root_1);

                adaptor.addChild(root_1, stream_thenClause.nextTree());
                adaptor.addChild(root_1, stream_elses.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "ifExprs"

    public static class thenClause_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "thenClause"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:461:1: thenClause : t= assignExpr ( THEN | COLON ) v= condStmtExpr -> ^( CONDTEST $t $v) ;
    public final EulangParser.thenClause_return thenClause() throws RecognitionException {
        EulangParser.thenClause_return retval = new EulangParser.thenClause_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token THEN336=null;
        Token COLON337=null;
        EulangParser.assignExpr_return t = null;

        EulangParser.condStmtExpr_return v = null;


        CommonTree THEN336_tree=null;
        CommonTree COLON337_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleTokenStream stream_THEN=new RewriteRuleTokenStream(adaptor,"token THEN");
        RewriteRuleSubtreeStream stream_condStmtExpr=new RewriteRuleSubtreeStream(adaptor,"rule condStmtExpr");
        RewriteRuleSubtreeStream stream_assignExpr=new RewriteRuleSubtreeStream(adaptor,"rule assignExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:461:12: (t= assignExpr ( THEN | COLON ) v= condStmtExpr -> ^( CONDTEST $t $v) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:461:14: t= assignExpr ( THEN | COLON ) v= condStmtExpr
            {
            pushFollow(FOLLOW_assignExpr_in_thenClause4672);
            t=assignExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_assignExpr.add(t.getTree());
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:462:3: ( THEN | COLON )
            int alt103=2;
            int LA103_0 = input.LA(1);

            if ( (LA103_0==THEN) ) {
                alt103=1;
            }
            else if ( (LA103_0==COLON) ) {
                alt103=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 103, 0, input);

                throw nvae;
            }
            switch (alt103) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:462:4: THEN
                    {
                    THEN336=(Token)match(input,THEN,FOLLOW_THEN_in_thenClause4678); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_THEN.add(THEN336);


                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:462:9: COLON
                    {
                    COLON337=(Token)match(input,COLON,FOLLOW_COLON_in_thenClause4680); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COLON.add(COLON337);


                    }
                    break;

            }

            pushFollow(FOLLOW_condStmtExpr_in_thenClause4685);
            v=condStmtExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_condStmtExpr.add(v.getTree());


            // AST REWRITE
            // elements: t, v
            // token labels: 
            // rule labels: v, retval, t
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_v=new RewriteRuleSubtreeStream(adaptor,"rule v",v!=null?v.tree:null);
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            RewriteRuleSubtreeStream stream_t=new RewriteRuleSubtreeStream(adaptor,"rule t",t!=null?t.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 462:33: -> ^( CONDTEST $t $v)
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:462:36: ^( CONDTEST $t $v)
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(CONDTEST, "CONDTEST"), root_1);

                adaptor.addChild(root_1, stream_t.nextTree());
                adaptor.addChild(root_1, stream_v.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "thenClause"

    public static class elses_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "elses"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:464:1: elses : ( elif )* elseClause -> ( elif )* elseClause ;
    public final EulangParser.elses_return elses() throws RecognitionException {
        EulangParser.elses_return retval = new EulangParser.elses_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.elif_return elif338 = null;

        EulangParser.elseClause_return elseClause339 = null;


        RewriteRuleSubtreeStream stream_elseClause=new RewriteRuleSubtreeStream(adaptor,"rule elseClause");
        RewriteRuleSubtreeStream stream_elif=new RewriteRuleSubtreeStream(adaptor,"rule elif");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:464:7: ( ( elif )* elseClause -> ( elif )* elseClause )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:464:9: ( elif )* elseClause
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:464:9: ( elif )*
            loop104:
            do {
                int alt104=2;
                int LA104_0 = input.LA(1);

                if ( (LA104_0==ELIF) ) {
                    alt104=1;
                }


                switch (alt104) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:464:9: elif
            	    {
            	    pushFollow(FOLLOW_elif_in_elses4713);
            	    elif338=elif();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_elif.add(elif338.getTree());

            	    }
            	    break;

            	default :
            	    break loop104;
                }
            } while (true);

            pushFollow(FOLLOW_elseClause_in_elses4716);
            elseClause339=elseClause();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_elseClause.add(elseClause339.getTree());


            // AST REWRITE
            // elements: elseClause, elif
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 464:29: -> ( elif )* elseClause
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:464:32: ( elif )*
                while ( stream_elif.hasNext() ) {
                    adaptor.addChild(root_0, stream_elif.nextTree());

                }
                stream_elif.reset();
                adaptor.addChild(root_0, stream_elseClause.nextTree());

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "elses"

    public static class elif_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "elif"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:466:1: elif : ELIF t= assignExpr ( THEN | COLON ) v= condStmtExpr -> ^( CONDTEST $t $v) ;
    public final EulangParser.elif_return elif() throws RecognitionException {
        EulangParser.elif_return retval = new EulangParser.elif_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ELIF340=null;
        Token THEN341=null;
        Token COLON342=null;
        EulangParser.assignExpr_return t = null;

        EulangParser.condStmtExpr_return v = null;


        CommonTree ELIF340_tree=null;
        CommonTree THEN341_tree=null;
        CommonTree COLON342_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleTokenStream stream_ELIF=new RewriteRuleTokenStream(adaptor,"token ELIF");
        RewriteRuleTokenStream stream_THEN=new RewriteRuleTokenStream(adaptor,"token THEN");
        RewriteRuleSubtreeStream stream_condStmtExpr=new RewriteRuleSubtreeStream(adaptor,"rule condStmtExpr");
        RewriteRuleSubtreeStream stream_assignExpr=new RewriteRuleSubtreeStream(adaptor,"rule assignExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:466:6: ( ELIF t= assignExpr ( THEN | COLON ) v= condStmtExpr -> ^( CONDTEST $t $v) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:466:8: ELIF t= assignExpr ( THEN | COLON ) v= condStmtExpr
            {
            ELIF340=(Token)match(input,ELIF,FOLLOW_ELIF_in_elif4739); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_ELIF.add(ELIF340);

            pushFollow(FOLLOW_assignExpr_in_elif4743);
            t=assignExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_assignExpr.add(t.getTree());
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:466:26: ( THEN | COLON )
            int alt105=2;
            int LA105_0 = input.LA(1);

            if ( (LA105_0==THEN) ) {
                alt105=1;
            }
            else if ( (LA105_0==COLON) ) {
                alt105=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 105, 0, input);

                throw nvae;
            }
            switch (alt105) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:466:27: THEN
                    {
                    THEN341=(Token)match(input,THEN,FOLLOW_THEN_in_elif4746); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_THEN.add(THEN341);


                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:466:32: COLON
                    {
                    COLON342=(Token)match(input,COLON,FOLLOW_COLON_in_elif4748); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COLON.add(COLON342);


                    }
                    break;

            }

            pushFollow(FOLLOW_condStmtExpr_in_elif4753);
            v=condStmtExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_condStmtExpr.add(v.getTree());


            // AST REWRITE
            // elements: v, t
            // token labels: 
            // rule labels: v, retval, t
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_v=new RewriteRuleSubtreeStream(adaptor,"rule v",v!=null?v.tree:null);
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            RewriteRuleSubtreeStream stream_t=new RewriteRuleSubtreeStream(adaptor,"rule t",t!=null?t.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 466:55: -> ^( CONDTEST $t $v)
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:466:58: ^( CONDTEST $t $v)
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(CONDTEST, "CONDTEST"), root_1);

                adaptor.addChild(root_1, stream_t.nextTree());
                adaptor.addChild(root_1, stream_v.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "elif"

    public static class elseClause_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "elseClause"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:468:1: elseClause : ( ELSE condStmtExpr -> ^( CONDTEST ^( LIT TRUE ) condStmtExpr ) | FI -> ^( CONDTEST ^( LIT TRUE ) ^( LIT NIL ) ) );
    public final EulangParser.elseClause_return elseClause() throws RecognitionException {
        EulangParser.elseClause_return retval = new EulangParser.elseClause_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ELSE343=null;
        Token FI345=null;
        EulangParser.condStmtExpr_return condStmtExpr344 = null;


        CommonTree ELSE343_tree=null;
        CommonTree FI345_tree=null;
        RewriteRuleTokenStream stream_FI=new RewriteRuleTokenStream(adaptor,"token FI");
        RewriteRuleTokenStream stream_ELSE=new RewriteRuleTokenStream(adaptor,"token ELSE");
        RewriteRuleSubtreeStream stream_condStmtExpr=new RewriteRuleSubtreeStream(adaptor,"rule condStmtExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:468:12: ( ELSE condStmtExpr -> ^( CONDTEST ^( LIT TRUE ) condStmtExpr ) | FI -> ^( CONDTEST ^( LIT TRUE ) ^( LIT NIL ) ) )
            int alt106=2;
            int LA106_0 = input.LA(1);

            if ( (LA106_0==ELSE) ) {
                alt106=1;
            }
            else if ( (LA106_0==FI) ) {
                alt106=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 106, 0, input);

                throw nvae;
            }
            switch (alt106) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:468:14: ELSE condStmtExpr
                    {
                    ELSE343=(Token)match(input,ELSE,FOLLOW_ELSE_in_elseClause4779); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ELSE.add(ELSE343);

                    pushFollow(FOLLOW_condStmtExpr_in_elseClause4781);
                    condStmtExpr344=condStmtExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_condStmtExpr.add(condStmtExpr344.getTree());


                    // AST REWRITE
                    // elements: condStmtExpr
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 468:38: -> ^( CONDTEST ^( LIT TRUE ) condStmtExpr )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:468:41: ^( CONDTEST ^( LIT TRUE ) condStmtExpr )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(CONDTEST, "CONDTEST"), root_1);

                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:468:52: ^( LIT TRUE )
                        {
                        CommonTree root_2 = (CommonTree)adaptor.nil();
                        root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(LIT, "LIT"), root_2);

                        adaptor.addChild(root_2, (CommonTree)adaptor.create(TRUE, "TRUE"));

                        adaptor.addChild(root_1, root_2);
                        }
                        adaptor.addChild(root_1, stream_condStmtExpr.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:469:6: FI
                    {
                    FI345=(Token)match(input,FI,FOLLOW_FI_in_elseClause4808); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_FI.add(FI345);



                    // AST REWRITE
                    // elements: 
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 469:9: -> ^( CONDTEST ^( LIT TRUE ) ^( LIT NIL ) )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:469:12: ^( CONDTEST ^( LIT TRUE ) ^( LIT NIL ) )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(CONDTEST, "CONDTEST"), root_1);

                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:469:23: ^( LIT TRUE )
                        {
                        CommonTree root_2 = (CommonTree)adaptor.nil();
                        root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(LIT, "LIT"), root_2);

                        adaptor.addChild(root_2, (CommonTree)adaptor.create(TRUE, "TRUE"));

                        adaptor.addChild(root_1, root_2);
                        }
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:469:35: ^( LIT NIL )
                        {
                        CommonTree root_2 = (CommonTree)adaptor.nil();
                        root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(LIT, "LIT"), root_2);

                        adaptor.addChild(root_2, (CommonTree)adaptor.create(NIL, "NIL"));

                        adaptor.addChild(root_1, root_2);
                        }

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "elseClause"

    public static class condStmtExpr_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "condStmtExpr"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:472:1: condStmtExpr : ( arg | breakStmt );
    public final EulangParser.condStmtExpr_return condStmtExpr() throws RecognitionException {
        EulangParser.condStmtExpr_return retval = new EulangParser.condStmtExpr_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.arg_return arg346 = null;

        EulangParser.breakStmt_return breakStmt347 = null;



        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:472:14: ( arg | breakStmt )
            int alt107=2;
            int LA107_0 = input.LA(1);

            if ( (LA107_0==CODE||LA107_0==GOTO||(LA107_0>=SIZEOF && LA107_0<=TYPEOF)||LA107_0==ID||LA107_0==COLON||LA107_0==LBRACE||LA107_0==LPAREN||LA107_0==NIL||LA107_0==IF||LA107_0==NOT||(LA107_0>=TILDE && LA107_0<=AMP)||LA107_0==MINUS||(LA107_0>=PLUSPLUS && LA107_0<=STRING_LITERAL)||(LA107_0>=FALSE && LA107_0<=COLONS)) ) {
                alt107=1;
            }
            else if ( (LA107_0==BREAK) ) {
                alt107=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 107, 0, input);

                throw nvae;
            }
            switch (alt107) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:472:16: arg
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_arg_in_condStmtExpr4839);
                    arg346=arg();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, arg346.getTree());

                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:472:22: breakStmt
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_breakStmt_in_condStmtExpr4843);
                    breakStmt347=breakStmt();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, breakStmt347.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "condStmtExpr"

    public static class cond_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "cond"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:474:1: cond : ( logor -> logor ) ( QUESTION t= logor COLON f= logor -> ^( CONDLIST ^( CONDTEST $cond $t) ^( CONDTEST ^( LIT TRUE ) $f) ) )* ;
    public final EulangParser.cond_return cond() throws RecognitionException {
        EulangParser.cond_return retval = new EulangParser.cond_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token QUESTION349=null;
        Token COLON350=null;
        EulangParser.logor_return t = null;

        EulangParser.logor_return f = null;

        EulangParser.logor_return logor348 = null;


        CommonTree QUESTION349_tree=null;
        CommonTree COLON350_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleTokenStream stream_QUESTION=new RewriteRuleTokenStream(adaptor,"token QUESTION");
        RewriteRuleSubtreeStream stream_logor=new RewriteRuleSubtreeStream(adaptor,"rule logor");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:474:5: ( ( logor -> logor ) ( QUESTION t= logor COLON f= logor -> ^( CONDLIST ^( CONDTEST $cond $t) ^( CONDTEST ^( LIT TRUE ) $f) ) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:474:10: ( logor -> logor ) ( QUESTION t= logor COLON f= logor -> ^( CONDLIST ^( CONDTEST $cond $t) ^( CONDTEST ^( LIT TRUE ) $f) ) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:474:10: ( logor -> logor )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:474:12: logor
            {
            pushFollow(FOLLOW_logor_in_cond4860);
            logor348=logor();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_logor.add(logor348.getTree());


            // AST REWRITE
            // elements: logor
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 474:19: -> logor
            {
                adaptor.addChild(root_0, stream_logor.nextTree());

            }

            retval.tree = root_0;}
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:475:7: ( QUESTION t= logor COLON f= logor -> ^( CONDLIST ^( CONDTEST $cond $t) ^( CONDTEST ^( LIT TRUE ) $f) ) )*
            loop108:
            do {
                int alt108=2;
                int LA108_0 = input.LA(1);

                if ( (LA108_0==QUESTION) ) {
                    alt108=1;
                }


                switch (alt108) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:475:9: QUESTION t= logor COLON f= logor
            	    {
            	    QUESTION349=(Token)match(input,QUESTION,FOLLOW_QUESTION_in_cond4877); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_QUESTION.add(QUESTION349);

            	    pushFollow(FOLLOW_logor_in_cond4881);
            	    t=logor();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_logor.add(t.getTree());
            	    COLON350=(Token)match(input,COLON,FOLLOW_COLON_in_cond4883); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COLON.add(COLON350);

            	    pushFollow(FOLLOW_logor_in_cond4887);
            	    f=logor();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_logor.add(f.getTree());


            	    // AST REWRITE
            	    // elements: f, cond, t
            	    // token labels: 
            	    // rule labels: f, retval, t
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_f=new RewriteRuleSubtreeStream(adaptor,"rule f",f!=null?f.tree:null);
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            	    RewriteRuleSubtreeStream stream_t=new RewriteRuleSubtreeStream(adaptor,"rule t",t!=null?t.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 475:40: -> ^( CONDLIST ^( CONDTEST $cond $t) ^( CONDTEST ^( LIT TRUE ) $f) )
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:475:43: ^( CONDLIST ^( CONDTEST $cond $t) ^( CONDTEST ^( LIT TRUE ) $f) )
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(CONDLIST, "CONDLIST"), root_1);

            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:475:54: ^( CONDTEST $cond $t)
            	        {
            	        CommonTree root_2 = (CommonTree)adaptor.nil();
            	        root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(CONDTEST, "CONDTEST"), root_2);

            	        adaptor.addChild(root_2, stream_retval.nextTree());
            	        adaptor.addChild(root_2, stream_t.nextTree());

            	        adaptor.addChild(root_1, root_2);
            	        }
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:475:75: ^( CONDTEST ^( LIT TRUE ) $f)
            	        {
            	        CommonTree root_2 = (CommonTree)adaptor.nil();
            	        root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(CONDTEST, "CONDTEST"), root_2);

            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:475:86: ^( LIT TRUE )
            	        {
            	        CommonTree root_3 = (CommonTree)adaptor.nil();
            	        root_3 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(LIT, "LIT"), root_3);

            	        adaptor.addChild(root_3, (CommonTree)adaptor.create(TRUE, "TRUE"));

            	        adaptor.addChild(root_2, root_3);
            	        }
            	        adaptor.addChild(root_2, stream_f.nextTree());

            	        adaptor.addChild(root_1, root_2);
            	        }

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }
            	    break;

            	default :
            	    break loop108;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "cond"

    public static class logor_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "logor"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:478:1: logor : ( logand -> logand ) ( OR r= logand -> ^( OR $logor $r) )* ;
    public final EulangParser.logor_return logor() throws RecognitionException {
        EulangParser.logor_return retval = new EulangParser.logor_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token OR352=null;
        EulangParser.logand_return r = null;

        EulangParser.logand_return logand351 = null;


        CommonTree OR352_tree=null;
        RewriteRuleTokenStream stream_OR=new RewriteRuleTokenStream(adaptor,"token OR");
        RewriteRuleSubtreeStream stream_logand=new RewriteRuleSubtreeStream(adaptor,"rule logand");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:478:7: ( ( logand -> logand ) ( OR r= logand -> ^( OR $logor $r) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:478:9: ( logand -> logand ) ( OR r= logand -> ^( OR $logor $r) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:478:9: ( logand -> logand )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:478:11: logand
            {
            pushFollow(FOLLOW_logand_in_logor4931);
            logand351=logand();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_logand.add(logand351.getTree());


            // AST REWRITE
            // elements: logand
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 478:19: -> logand
            {
                adaptor.addChild(root_0, stream_logand.nextTree());

            }

            retval.tree = root_0;}
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:479:7: ( OR r= logand -> ^( OR $logor $r) )*
            loop109:
            do {
                int alt109=2;
                int LA109_0 = input.LA(1);

                if ( (LA109_0==OR) ) {
                    alt109=1;
                }


                switch (alt109) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:479:9: OR r= logand
            	    {
            	    OR352=(Token)match(input,OR,FOLLOW_OR_in_logor4948); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_OR.add(OR352);

            	    pushFollow(FOLLOW_logand_in_logor4952);
            	    r=logand();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_logand.add(r.getTree());


            	    // AST REWRITE
            	    // elements: OR, r, logor
            	    // token labels: 
            	    // rule labels: retval, r
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            	    RewriteRuleSubtreeStream stream_r=new RewriteRuleSubtreeStream(adaptor,"rule r",r!=null?r.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 479:21: -> ^( OR $logor $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:479:24: ^( OR $logor $r)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot(stream_OR.nextNode(), root_1);

            	        adaptor.addChild(root_1, stream_retval.nextTree());
            	        adaptor.addChild(root_1, stream_r.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }
            	    break;

            	default :
            	    break loop109;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "logor"

    public static class logand_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "logand"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:481:1: logand : ( not -> not ) ( AND r= not -> ^( AND $logand $r) )* ;
    public final EulangParser.logand_return logand() throws RecognitionException {
        EulangParser.logand_return retval = new EulangParser.logand_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token AND354=null;
        EulangParser.not_return r = null;

        EulangParser.not_return not353 = null;


        CommonTree AND354_tree=null;
        RewriteRuleTokenStream stream_AND=new RewriteRuleTokenStream(adaptor,"token AND");
        RewriteRuleSubtreeStream stream_not=new RewriteRuleSubtreeStream(adaptor,"rule not");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:481:8: ( ( not -> not ) ( AND r= not -> ^( AND $logand $r) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:481:10: ( not -> not ) ( AND r= not -> ^( AND $logand $r) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:481:10: ( not -> not )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:481:12: not
            {
            pushFollow(FOLLOW_not_in_logand4983);
            not353=not();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_not.add(not353.getTree());


            // AST REWRITE
            // elements: not
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 481:16: -> not
            {
                adaptor.addChild(root_0, stream_not.nextTree());

            }

            retval.tree = root_0;}
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:482:7: ( AND r= not -> ^( AND $logand $r) )*
            loop110:
            do {
                int alt110=2;
                int LA110_0 = input.LA(1);

                if ( (LA110_0==AND) ) {
                    alt110=1;
                }


                switch (alt110) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:482:9: AND r= not
            	    {
            	    AND354=(Token)match(input,AND,FOLLOW_AND_in_logand4999); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_AND.add(AND354);

            	    pushFollow(FOLLOW_not_in_logand5003);
            	    r=not();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_not.add(r.getTree());


            	    // AST REWRITE
            	    // elements: r, AND, logand
            	    // token labels: 
            	    // rule labels: retval, r
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            	    RewriteRuleSubtreeStream stream_r=new RewriteRuleSubtreeStream(adaptor,"rule r",r!=null?r.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 482:19: -> ^( AND $logand $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:482:22: ^( AND $logand $r)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot(stream_AND.nextNode(), root_1);

            	        adaptor.addChild(root_1, stream_retval.nextTree());
            	        adaptor.addChild(root_1, stream_r.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }
            	    break;

            	default :
            	    break loop110;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "logand"

    public static class not_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "not"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:485:1: not : ( comp -> comp | NOT u= comp -> ^( NOT $u) );
    public final EulangParser.not_return not() throws RecognitionException {
        EulangParser.not_return retval = new EulangParser.not_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token NOT356=null;
        EulangParser.comp_return u = null;

        EulangParser.comp_return comp355 = null;


        CommonTree NOT356_tree=null;
        RewriteRuleTokenStream stream_NOT=new RewriteRuleTokenStream(adaptor,"token NOT");
        RewriteRuleSubtreeStream stream_comp=new RewriteRuleSubtreeStream(adaptor,"rule comp");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:485:5: ( comp -> comp | NOT u= comp -> ^( NOT $u) )
            int alt111=2;
            int LA111_0 = input.LA(1);

            if ( (LA111_0==CODE||(LA111_0>=SIZEOF && LA111_0<=TYPEOF)||LA111_0==ID||LA111_0==COLON||LA111_0==LPAREN||LA111_0==NIL||(LA111_0>=TILDE && LA111_0<=AMP)||LA111_0==MINUS||(LA111_0>=PLUSPLUS && LA111_0<=STRING_LITERAL)||(LA111_0>=FALSE && LA111_0<=COLONS)) ) {
                alt111=1;
            }
            else if ( (LA111_0==NOT) ) {
                alt111=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 111, 0, input);

                throw nvae;
            }
            switch (alt111) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:485:8: comp
                    {
                    pushFollow(FOLLOW_comp_in_not5049);
                    comp355=comp();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_comp.add(comp355.getTree());


                    // AST REWRITE
                    // elements: comp
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 485:17: -> comp
                    {
                        adaptor.addChild(root_0, stream_comp.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:486:7: NOT u= comp
                    {
                    NOT356=(Token)match(input,NOT,FOLLOW_NOT_in_not5065); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_NOT.add(NOT356);

                    pushFollow(FOLLOW_comp_in_not5069);
                    u=comp();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_comp.add(u.getTree());


                    // AST REWRITE
                    // elements: NOT, u
                    // token labels: 
                    // rule labels: retval, u
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                    RewriteRuleSubtreeStream stream_u=new RewriteRuleSubtreeStream(adaptor,"rule u",u!=null?u.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 486:22: -> ^( NOT $u)
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:486:25: ^( NOT $u)
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot(stream_NOT.nextNode(), root_1);

                        adaptor.addChild(root_1, stream_u.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "not"

    public static class comp_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "comp"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:489:1: comp : ( bitor -> bitor ) ( COMPEQ r= bitor -> ^( COMPEQ $comp $r) | COMPNE r= bitor -> ^( COMPNE $comp $r) | COMPLE r= bitor -> ^( COMPLE $comp $r) | COMPGE r= bitor -> ^( COMPGE $comp $r) | COMPULE r= bitor -> ^( COMPULE $comp $r) | COMPUGE r= bitor -> ^( COMPUGE $comp $r) | LESS r= bitor -> ^( LESS $comp $r) | ULESS r= bitor -> ^( ULESS $comp $r) | GREATER r= bitor -> ^( GREATER $comp $r) | UGREATER r= bitor -> ^( UGREATER $comp $r) )* ;
    public final EulangParser.comp_return comp() throws RecognitionException {
        EulangParser.comp_return retval = new EulangParser.comp_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COMPEQ358=null;
        Token COMPNE359=null;
        Token COMPLE360=null;
        Token COMPGE361=null;
        Token COMPULE362=null;
        Token COMPUGE363=null;
        Token LESS364=null;
        Token ULESS365=null;
        Token GREATER366=null;
        Token UGREATER367=null;
        EulangParser.bitor_return r = null;

        EulangParser.bitor_return bitor357 = null;


        CommonTree COMPEQ358_tree=null;
        CommonTree COMPNE359_tree=null;
        CommonTree COMPLE360_tree=null;
        CommonTree COMPGE361_tree=null;
        CommonTree COMPULE362_tree=null;
        CommonTree COMPUGE363_tree=null;
        CommonTree LESS364_tree=null;
        CommonTree ULESS365_tree=null;
        CommonTree GREATER366_tree=null;
        CommonTree UGREATER367_tree=null;
        RewriteRuleTokenStream stream_COMPGE=new RewriteRuleTokenStream(adaptor,"token COMPGE");
        RewriteRuleTokenStream stream_ULESS=new RewriteRuleTokenStream(adaptor,"token ULESS");
        RewriteRuleTokenStream stream_COMPEQ=new RewriteRuleTokenStream(adaptor,"token COMPEQ");
        RewriteRuleTokenStream stream_GREATER=new RewriteRuleTokenStream(adaptor,"token GREATER");
        RewriteRuleTokenStream stream_COMPLE=new RewriteRuleTokenStream(adaptor,"token COMPLE");
        RewriteRuleTokenStream stream_COMPNE=new RewriteRuleTokenStream(adaptor,"token COMPNE");
        RewriteRuleTokenStream stream_COMPULE=new RewriteRuleTokenStream(adaptor,"token COMPULE");
        RewriteRuleTokenStream stream_UGREATER=new RewriteRuleTokenStream(adaptor,"token UGREATER");
        RewriteRuleTokenStream stream_COMPUGE=new RewriteRuleTokenStream(adaptor,"token COMPUGE");
        RewriteRuleTokenStream stream_LESS=new RewriteRuleTokenStream(adaptor,"token LESS");
        RewriteRuleSubtreeStream stream_bitor=new RewriteRuleSubtreeStream(adaptor,"rule bitor");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:489:5: ( ( bitor -> bitor ) ( COMPEQ r= bitor -> ^( COMPEQ $comp $r) | COMPNE r= bitor -> ^( COMPNE $comp $r) | COMPLE r= bitor -> ^( COMPLE $comp $r) | COMPGE r= bitor -> ^( COMPGE $comp $r) | COMPULE r= bitor -> ^( COMPULE $comp $r) | COMPUGE r= bitor -> ^( COMPUGE $comp $r) | LESS r= bitor -> ^( LESS $comp $r) | ULESS r= bitor -> ^( ULESS $comp $r) | GREATER r= bitor -> ^( GREATER $comp $r) | UGREATER r= bitor -> ^( UGREATER $comp $r) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:489:9: ( bitor -> bitor ) ( COMPEQ r= bitor -> ^( COMPEQ $comp $r) | COMPNE r= bitor -> ^( COMPNE $comp $r) | COMPLE r= bitor -> ^( COMPLE $comp $r) | COMPGE r= bitor -> ^( COMPGE $comp $r) | COMPULE r= bitor -> ^( COMPULE $comp $r) | COMPUGE r= bitor -> ^( COMPUGE $comp $r) | LESS r= bitor -> ^( LESS $comp $r) | ULESS r= bitor -> ^( ULESS $comp $r) | GREATER r= bitor -> ^( GREATER $comp $r) | UGREATER r= bitor -> ^( UGREATER $comp $r) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:489:9: ( bitor -> bitor )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:489:11: bitor
            {
            pushFollow(FOLLOW_bitor_in_comp5103);
            bitor357=bitor();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_bitor.add(bitor357.getTree());


            // AST REWRITE
            // elements: bitor
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 489:24: -> bitor
            {
                adaptor.addChild(root_0, stream_bitor.nextTree());

            }

            retval.tree = root_0;}
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:490:7: ( COMPEQ r= bitor -> ^( COMPEQ $comp $r) | COMPNE r= bitor -> ^( COMPNE $comp $r) | COMPLE r= bitor -> ^( COMPLE $comp $r) | COMPGE r= bitor -> ^( COMPGE $comp $r) | COMPULE r= bitor -> ^( COMPULE $comp $r) | COMPUGE r= bitor -> ^( COMPUGE $comp $r) | LESS r= bitor -> ^( LESS $comp $r) | ULESS r= bitor -> ^( ULESS $comp $r) | GREATER r= bitor -> ^( GREATER $comp $r) | UGREATER r= bitor -> ^( UGREATER $comp $r) )*
            loop112:
            do {
                int alt112=11;
                switch ( input.LA(1) ) {
                case COMPEQ:
                    {
                    alt112=1;
                    }
                    break;
                case COMPNE:
                    {
                    alt112=2;
                    }
                    break;
                case COMPLE:
                    {
                    alt112=3;
                    }
                    break;
                case COMPGE:
                    {
                    alt112=4;
                    }
                    break;
                case COMPULE:
                    {
                    alt112=5;
                    }
                    break;
                case COMPUGE:
                    {
                    alt112=6;
                    }
                    break;
                case LESS:
                    {
                    alt112=7;
                    }
                    break;
                case ULESS:
                    {
                    alt112=8;
                    }
                    break;
                case GREATER:
                    {
                    alt112=9;
                    }
                    break;
                case UGREATER:
                    {
                    alt112=10;
                    }
                    break;

                }

                switch (alt112) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:490:9: COMPEQ r= bitor
            	    {
            	    COMPEQ358=(Token)match(input,COMPEQ,FOLLOW_COMPEQ_in_comp5136); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COMPEQ.add(COMPEQ358);

            	    pushFollow(FOLLOW_bitor_in_comp5140);
            	    r=bitor();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_bitor.add(r.getTree());


            	    // AST REWRITE
            	    // elements: COMPEQ, r, comp
            	    // token labels: 
            	    // rule labels: retval, r
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            	    RewriteRuleSubtreeStream stream_r=new RewriteRuleSubtreeStream(adaptor,"rule r",r!=null?r.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 490:24: -> ^( COMPEQ $comp $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:490:27: ^( COMPEQ $comp $r)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot(stream_COMPEQ.nextNode(), root_1);

            	        adaptor.addChild(root_1, stream_retval.nextTree());
            	        adaptor.addChild(root_1, stream_r.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }
            	    break;
            	case 2 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:491:9: COMPNE r= bitor
            	    {
            	    COMPNE359=(Token)match(input,COMPNE,FOLLOW_COMPNE_in_comp5162); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COMPNE.add(COMPNE359);

            	    pushFollow(FOLLOW_bitor_in_comp5166);
            	    r=bitor();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_bitor.add(r.getTree());


            	    // AST REWRITE
            	    // elements: COMPNE, r, comp
            	    // token labels: 
            	    // rule labels: retval, r
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            	    RewriteRuleSubtreeStream stream_r=new RewriteRuleSubtreeStream(adaptor,"rule r",r!=null?r.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 491:24: -> ^( COMPNE $comp $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:491:27: ^( COMPNE $comp $r)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot(stream_COMPNE.nextNode(), root_1);

            	        adaptor.addChild(root_1, stream_retval.nextTree());
            	        adaptor.addChild(root_1, stream_r.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }
            	    break;
            	case 3 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:492:9: COMPLE r= bitor
            	    {
            	    COMPLE360=(Token)match(input,COMPLE,FOLLOW_COMPLE_in_comp5188); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COMPLE.add(COMPLE360);

            	    pushFollow(FOLLOW_bitor_in_comp5192);
            	    r=bitor();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_bitor.add(r.getTree());


            	    // AST REWRITE
            	    // elements: COMPLE, comp, r
            	    // token labels: 
            	    // rule labels: retval, r
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            	    RewriteRuleSubtreeStream stream_r=new RewriteRuleSubtreeStream(adaptor,"rule r",r!=null?r.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 492:27: -> ^( COMPLE $comp $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:492:30: ^( COMPLE $comp $r)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot(stream_COMPLE.nextNode(), root_1);

            	        adaptor.addChild(root_1, stream_retval.nextTree());
            	        adaptor.addChild(root_1, stream_r.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }
            	    break;
            	case 4 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:493:9: COMPGE r= bitor
            	    {
            	    COMPGE361=(Token)match(input,COMPGE,FOLLOW_COMPGE_in_comp5217); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COMPGE.add(COMPGE361);

            	    pushFollow(FOLLOW_bitor_in_comp5221);
            	    r=bitor();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_bitor.add(r.getTree());


            	    // AST REWRITE
            	    // elements: comp, r, COMPGE
            	    // token labels: 
            	    // rule labels: retval, r
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            	    RewriteRuleSubtreeStream stream_r=new RewriteRuleSubtreeStream(adaptor,"rule r",r!=null?r.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 493:27: -> ^( COMPGE $comp $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:493:30: ^( COMPGE $comp $r)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot(stream_COMPGE.nextNode(), root_1);

            	        adaptor.addChild(root_1, stream_retval.nextTree());
            	        adaptor.addChild(root_1, stream_r.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }
            	    break;
            	case 5 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:494:9: COMPULE r= bitor
            	    {
            	    COMPULE362=(Token)match(input,COMPULE,FOLLOW_COMPULE_in_comp5246); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COMPULE.add(COMPULE362);

            	    pushFollow(FOLLOW_bitor_in_comp5250);
            	    r=bitor();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_bitor.add(r.getTree());


            	    // AST REWRITE
            	    // elements: comp, COMPULE, r
            	    // token labels: 
            	    // rule labels: retval, r
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            	    RewriteRuleSubtreeStream stream_r=new RewriteRuleSubtreeStream(adaptor,"rule r",r!=null?r.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 494:28: -> ^( COMPULE $comp $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:494:31: ^( COMPULE $comp $r)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot(stream_COMPULE.nextNode(), root_1);

            	        adaptor.addChild(root_1, stream_retval.nextTree());
            	        adaptor.addChild(root_1, stream_r.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }
            	    break;
            	case 6 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:495:9: COMPUGE r= bitor
            	    {
            	    COMPUGE363=(Token)match(input,COMPUGE,FOLLOW_COMPUGE_in_comp5275); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COMPUGE.add(COMPUGE363);

            	    pushFollow(FOLLOW_bitor_in_comp5279);
            	    r=bitor();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_bitor.add(r.getTree());


            	    // AST REWRITE
            	    // elements: comp, r, COMPUGE
            	    // token labels: 
            	    // rule labels: retval, r
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            	    RewriteRuleSubtreeStream stream_r=new RewriteRuleSubtreeStream(adaptor,"rule r",r!=null?r.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 495:28: -> ^( COMPUGE $comp $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:495:31: ^( COMPUGE $comp $r)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot(stream_COMPUGE.nextNode(), root_1);

            	        adaptor.addChild(root_1, stream_retval.nextTree());
            	        adaptor.addChild(root_1, stream_r.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }
            	    break;
            	case 7 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:496:9: LESS r= bitor
            	    {
            	    LESS364=(Token)match(input,LESS,FOLLOW_LESS_in_comp5304); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_LESS.add(LESS364);

            	    pushFollow(FOLLOW_bitor_in_comp5308);
            	    r=bitor();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_bitor.add(r.getTree());


            	    // AST REWRITE
            	    // elements: r, LESS, comp
            	    // token labels: 
            	    // rule labels: retval, r
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            	    RewriteRuleSubtreeStream stream_r=new RewriteRuleSubtreeStream(adaptor,"rule r",r!=null?r.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 496:26: -> ^( LESS $comp $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:496:29: ^( LESS $comp $r)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot(stream_LESS.nextNode(), root_1);

            	        adaptor.addChild(root_1, stream_retval.nextTree());
            	        adaptor.addChild(root_1, stream_r.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }
            	    break;
            	case 8 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:497:9: ULESS r= bitor
            	    {
            	    ULESS365=(Token)match(input,ULESS,FOLLOW_ULESS_in_comp5334); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_ULESS.add(ULESS365);

            	    pushFollow(FOLLOW_bitor_in_comp5338);
            	    r=bitor();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_bitor.add(r.getTree());


            	    // AST REWRITE
            	    // elements: ULESS, comp, r
            	    // token labels: 
            	    // rule labels: retval, r
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            	    RewriteRuleSubtreeStream stream_r=new RewriteRuleSubtreeStream(adaptor,"rule r",r!=null?r.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 497:27: -> ^( ULESS $comp $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:497:30: ^( ULESS $comp $r)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot(stream_ULESS.nextNode(), root_1);

            	        adaptor.addChild(root_1, stream_retval.nextTree());
            	        adaptor.addChild(root_1, stream_r.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }
            	    break;
            	case 9 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:498:9: GREATER r= bitor
            	    {
            	    GREATER366=(Token)match(input,GREATER,FOLLOW_GREATER_in_comp5364); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_GREATER.add(GREATER366);

            	    pushFollow(FOLLOW_bitor_in_comp5368);
            	    r=bitor();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_bitor.add(r.getTree());


            	    // AST REWRITE
            	    // elements: r, GREATER, comp
            	    // token labels: 
            	    // rule labels: retval, r
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            	    RewriteRuleSubtreeStream stream_r=new RewriteRuleSubtreeStream(adaptor,"rule r",r!=null?r.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 498:28: -> ^( GREATER $comp $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:498:31: ^( GREATER $comp $r)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot(stream_GREATER.nextNode(), root_1);

            	        adaptor.addChild(root_1, stream_retval.nextTree());
            	        adaptor.addChild(root_1, stream_r.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }
            	    break;
            	case 10 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:499:9: UGREATER r= bitor
            	    {
            	    UGREATER367=(Token)match(input,UGREATER,FOLLOW_UGREATER_in_comp5393); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_UGREATER.add(UGREATER367);

            	    pushFollow(FOLLOW_bitor_in_comp5397);
            	    r=bitor();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_bitor.add(r.getTree());


            	    // AST REWRITE
            	    // elements: comp, UGREATER, r
            	    // token labels: 
            	    // rule labels: retval, r
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            	    RewriteRuleSubtreeStream stream_r=new RewriteRuleSubtreeStream(adaptor,"rule r",r!=null?r.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 499:29: -> ^( UGREATER $comp $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:499:32: ^( UGREATER $comp $r)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot(stream_UGREATER.nextNode(), root_1);

            	        adaptor.addChild(root_1, stream_retval.nextTree());
            	        adaptor.addChild(root_1, stream_r.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }
            	    break;

            	default :
            	    break loop112;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "comp"

    public static class bitor_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "bitor"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:504:1: bitor : ( bitxor -> bitxor ) ( BAR r= bitxor -> ^( BITOR $bitor $r) )* ;
    public final EulangParser.bitor_return bitor() throws RecognitionException {
        EulangParser.bitor_return retval = new EulangParser.bitor_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token BAR369=null;
        EulangParser.bitxor_return r = null;

        EulangParser.bitxor_return bitxor368 = null;


        CommonTree BAR369_tree=null;
        RewriteRuleTokenStream stream_BAR=new RewriteRuleTokenStream(adaptor,"token BAR");
        RewriteRuleSubtreeStream stream_bitxor=new RewriteRuleSubtreeStream(adaptor,"rule bitxor");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:504:6: ( ( bitxor -> bitxor ) ( BAR r= bitxor -> ^( BITOR $bitor $r) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:504:8: ( bitxor -> bitxor ) ( BAR r= bitxor -> ^( BITOR $bitor $r) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:504:8: ( bitxor -> bitxor )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:504:10: bitxor
            {
            pushFollow(FOLLOW_bitxor_in_bitor5447);
            bitxor368=bitxor();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_bitxor.add(bitxor368.getTree());


            // AST REWRITE
            // elements: bitxor
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 504:22: -> bitxor
            {
                adaptor.addChild(root_0, stream_bitxor.nextTree());

            }

            retval.tree = root_0;}
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:505:7: ( BAR r= bitxor -> ^( BITOR $bitor $r) )*
            loop113:
            do {
                int alt113=2;
                int LA113_0 = input.LA(1);

                if ( (LA113_0==BAR) ) {
                    alt113=1;
                }


                switch (alt113) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:505:9: BAR r= bitxor
            	    {
            	    BAR369=(Token)match(input,BAR,FOLLOW_BAR_in_bitor5475); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_BAR.add(BAR369);

            	    pushFollow(FOLLOW_bitxor_in_bitor5479);
            	    r=bitxor();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_bitxor.add(r.getTree());


            	    // AST REWRITE
            	    // elements: r, bitor
            	    // token labels: 
            	    // rule labels: retval, r
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            	    RewriteRuleSubtreeStream stream_r=new RewriteRuleSubtreeStream(adaptor,"rule r",r!=null?r.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 505:23: -> ^( BITOR $bitor $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:505:26: ^( BITOR $bitor $r)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(BITOR, "BITOR"), root_1);

            	        adaptor.addChild(root_1, stream_retval.nextTree());
            	        adaptor.addChild(root_1, stream_r.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }
            	    break;

            	default :
            	    break loop113;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "bitor"

    public static class bitxor_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "bitxor"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:507:1: bitxor : ( bitand -> bitand ) ( TILDE r= bitand -> ^( BITXOR $bitxor $r) )* ;
    public final EulangParser.bitxor_return bitxor() throws RecognitionException {
        EulangParser.bitxor_return retval = new EulangParser.bitxor_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token TILDE371=null;
        EulangParser.bitand_return r = null;

        EulangParser.bitand_return bitand370 = null;


        CommonTree TILDE371_tree=null;
        RewriteRuleTokenStream stream_TILDE=new RewriteRuleTokenStream(adaptor,"token TILDE");
        RewriteRuleSubtreeStream stream_bitand=new RewriteRuleSubtreeStream(adaptor,"rule bitand");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:507:7: ( ( bitand -> bitand ) ( TILDE r= bitand -> ^( BITXOR $bitxor $r) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:507:9: ( bitand -> bitand ) ( TILDE r= bitand -> ^( BITXOR $bitxor $r) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:507:9: ( bitand -> bitand )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:507:11: bitand
            {
            pushFollow(FOLLOW_bitand_in_bitxor5505);
            bitand370=bitand();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_bitand.add(bitand370.getTree());


            // AST REWRITE
            // elements: bitand
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 507:23: -> bitand
            {
                adaptor.addChild(root_0, stream_bitand.nextTree());

            }

            retval.tree = root_0;}
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:508:7: ( TILDE r= bitand -> ^( BITXOR $bitxor $r) )*
            loop114:
            do {
                int alt114=2;
                int LA114_0 = input.LA(1);

                if ( (LA114_0==TILDE) ) {
                    alt114=1;
                }


                switch (alt114) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:508:9: TILDE r= bitand
            	    {
            	    TILDE371=(Token)match(input,TILDE,FOLLOW_TILDE_in_bitxor5533); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_TILDE.add(TILDE371);

            	    pushFollow(FOLLOW_bitand_in_bitxor5537);
            	    r=bitand();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_bitand.add(r.getTree());


            	    // AST REWRITE
            	    // elements: r, bitxor
            	    // token labels: 
            	    // rule labels: retval, r
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            	    RewriteRuleSubtreeStream stream_r=new RewriteRuleSubtreeStream(adaptor,"rule r",r!=null?r.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 508:25: -> ^( BITXOR $bitxor $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:508:28: ^( BITXOR $bitxor $r)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(BITXOR, "BITXOR"), root_1);

            	        adaptor.addChild(root_1, stream_retval.nextTree());
            	        adaptor.addChild(root_1, stream_r.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }
            	    break;

            	default :
            	    break loop114;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "bitxor"

    public static class bitand_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "bitand"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:510:1: bitand : ( shift -> shift ) ( AMP r= shift -> ^( BITAND $bitand $r) )* ;
    public final EulangParser.bitand_return bitand() throws RecognitionException {
        EulangParser.bitand_return retval = new EulangParser.bitand_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token AMP373=null;
        EulangParser.shift_return r = null;

        EulangParser.shift_return shift372 = null;


        CommonTree AMP373_tree=null;
        RewriteRuleTokenStream stream_AMP=new RewriteRuleTokenStream(adaptor,"token AMP");
        RewriteRuleSubtreeStream stream_shift=new RewriteRuleSubtreeStream(adaptor,"rule shift");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:510:7: ( ( shift -> shift ) ( AMP r= shift -> ^( BITAND $bitand $r) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:510:9: ( shift -> shift ) ( AMP r= shift -> ^( BITAND $bitand $r) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:510:9: ( shift -> shift )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:510:11: shift
            {
            pushFollow(FOLLOW_shift_in_bitand5562);
            shift372=shift();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_shift.add(shift372.getTree());


            // AST REWRITE
            // elements: shift
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 510:22: -> shift
            {
                adaptor.addChild(root_0, stream_shift.nextTree());

            }

            retval.tree = root_0;}
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:511:7: ( AMP r= shift -> ^( BITAND $bitand $r) )*
            loop115:
            do {
                int alt115=2;
                int LA115_0 = input.LA(1);

                if ( (LA115_0==AMP) ) {
                    alt115=1;
                }


                switch (alt115) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:511:9: AMP r= shift
            	    {
            	    AMP373=(Token)match(input,AMP,FOLLOW_AMP_in_bitand5590); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_AMP.add(AMP373);

            	    pushFollow(FOLLOW_shift_in_bitand5594);
            	    r=shift();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_shift.add(r.getTree());


            	    // AST REWRITE
            	    // elements: bitand, r
            	    // token labels: 
            	    // rule labels: retval, r
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            	    RewriteRuleSubtreeStream stream_r=new RewriteRuleSubtreeStream(adaptor,"rule r",r!=null?r.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 511:22: -> ^( BITAND $bitand $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:511:25: ^( BITAND $bitand $r)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(BITAND, "BITAND"), root_1);

            	        adaptor.addChild(root_1, stream_retval.nextTree());
            	        adaptor.addChild(root_1, stream_r.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }
            	    break;

            	default :
            	    break loop115;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "bitand"

    public static class shift_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "shift"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:514:1: shift : ( factor -> factor ) ( ( LSHIFT r= factor -> ^( LSHIFT $shift $r) ) | ( RSHIFT r= factor -> ^( RSHIFT $shift $r) ) | ( URSHIFT r= factor -> ^( URSHIFT $shift $r) ) | ( CRSHIFT r= factor -> ^( CRSHIFT $shift $r) ) | ( CLSHIFT r= factor -> ^( CLSHIFT $shift $r) ) )* ;
    public final EulangParser.shift_return shift() throws RecognitionException {
        EulangParser.shift_return retval = new EulangParser.shift_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LSHIFT375=null;
        Token RSHIFT376=null;
        Token URSHIFT377=null;
        Token CRSHIFT378=null;
        Token CLSHIFT379=null;
        EulangParser.factor_return r = null;

        EulangParser.factor_return factor374 = null;


        CommonTree LSHIFT375_tree=null;
        CommonTree RSHIFT376_tree=null;
        CommonTree URSHIFT377_tree=null;
        CommonTree CRSHIFT378_tree=null;
        CommonTree CLSHIFT379_tree=null;
        RewriteRuleTokenStream stream_URSHIFT=new RewriteRuleTokenStream(adaptor,"token URSHIFT");
        RewriteRuleTokenStream stream_CLSHIFT=new RewriteRuleTokenStream(adaptor,"token CLSHIFT");
        RewriteRuleTokenStream stream_CRSHIFT=new RewriteRuleTokenStream(adaptor,"token CRSHIFT");
        RewriteRuleTokenStream stream_RSHIFT=new RewriteRuleTokenStream(adaptor,"token RSHIFT");
        RewriteRuleTokenStream stream_LSHIFT=new RewriteRuleTokenStream(adaptor,"token LSHIFT");
        RewriteRuleSubtreeStream stream_factor=new RewriteRuleSubtreeStream(adaptor,"rule factor");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:514:6: ( ( factor -> factor ) ( ( LSHIFT r= factor -> ^( LSHIFT $shift $r) ) | ( RSHIFT r= factor -> ^( RSHIFT $shift $r) ) | ( URSHIFT r= factor -> ^( URSHIFT $shift $r) ) | ( CRSHIFT r= factor -> ^( CRSHIFT $shift $r) ) | ( CLSHIFT r= factor -> ^( CLSHIFT $shift $r) ) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:514:9: ( factor -> factor ) ( ( LSHIFT r= factor -> ^( LSHIFT $shift $r) ) | ( RSHIFT r= factor -> ^( RSHIFT $shift $r) ) | ( URSHIFT r= factor -> ^( URSHIFT $shift $r) ) | ( CRSHIFT r= factor -> ^( CRSHIFT $shift $r) ) | ( CLSHIFT r= factor -> ^( CLSHIFT $shift $r) ) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:514:9: ( factor -> factor )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:514:11: factor
            {
            pushFollow(FOLLOW_factor_in_shift5621);
            factor374=factor();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_factor.add(factor374.getTree());


            // AST REWRITE
            // elements: factor
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 514:25: -> factor
            {
                adaptor.addChild(root_0, stream_factor.nextTree());

            }

            retval.tree = root_0;}
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:515:7: ( ( LSHIFT r= factor -> ^( LSHIFT $shift $r) ) | ( RSHIFT r= factor -> ^( RSHIFT $shift $r) ) | ( URSHIFT r= factor -> ^( URSHIFT $shift $r) ) | ( CRSHIFT r= factor -> ^( CRSHIFT $shift $r) ) | ( CLSHIFT r= factor -> ^( CLSHIFT $shift $r) ) )*
            loop116:
            do {
                int alt116=6;
                switch ( input.LA(1) ) {
                case LSHIFT:
                    {
                    alt116=1;
                    }
                    break;
                case RSHIFT:
                    {
                    alt116=2;
                    }
                    break;
                case URSHIFT:
                    {
                    alt116=3;
                    }
                    break;
                case CRSHIFT:
                    {
                    alt116=4;
                    }
                    break;
                case CLSHIFT:
                    {
                    alt116=5;
                    }
                    break;

                }

                switch (alt116) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:515:9: ( LSHIFT r= factor -> ^( LSHIFT $shift $r) )
            	    {
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:515:9: ( LSHIFT r= factor -> ^( LSHIFT $shift $r) )
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:515:11: LSHIFT r= factor
            	    {
            	    LSHIFT375=(Token)match(input,LSHIFT,FOLLOW_LSHIFT_in_shift5655); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_LSHIFT.add(LSHIFT375);

            	    pushFollow(FOLLOW_factor_in_shift5659);
            	    r=factor();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_factor.add(r.getTree());


            	    // AST REWRITE
            	    // elements: LSHIFT, shift, r
            	    // token labels: 
            	    // rule labels: retval, r
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            	    RewriteRuleSubtreeStream stream_r=new RewriteRuleSubtreeStream(adaptor,"rule r",r!=null?r.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 515:29: -> ^( LSHIFT $shift $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:515:32: ^( LSHIFT $shift $r)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot(stream_LSHIFT.nextNode(), root_1);

            	        adaptor.addChild(root_1, stream_retval.nextTree());
            	        adaptor.addChild(root_1, stream_r.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }


            	    }
            	    break;
            	case 2 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:516:9: ( RSHIFT r= factor -> ^( RSHIFT $shift $r) )
            	    {
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:516:9: ( RSHIFT r= factor -> ^( RSHIFT $shift $r) )
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:516:11: RSHIFT r= factor
            	    {
            	    RSHIFT376=(Token)match(input,RSHIFT,FOLLOW_RSHIFT_in_shift5688); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_RSHIFT.add(RSHIFT376);

            	    pushFollow(FOLLOW_factor_in_shift5692);
            	    r=factor();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_factor.add(r.getTree());


            	    // AST REWRITE
            	    // elements: shift, RSHIFT, r
            	    // token labels: 
            	    // rule labels: retval, r
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            	    RewriteRuleSubtreeStream stream_r=new RewriteRuleSubtreeStream(adaptor,"rule r",r!=null?r.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 516:29: -> ^( RSHIFT $shift $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:516:32: ^( RSHIFT $shift $r)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot(stream_RSHIFT.nextNode(), root_1);

            	        adaptor.addChild(root_1, stream_retval.nextTree());
            	        adaptor.addChild(root_1, stream_r.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }


            	    }
            	    break;
            	case 3 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:517:9: ( URSHIFT r= factor -> ^( URSHIFT $shift $r) )
            	    {
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:517:9: ( URSHIFT r= factor -> ^( URSHIFT $shift $r) )
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:517:11: URSHIFT r= factor
            	    {
            	    URSHIFT377=(Token)match(input,URSHIFT,FOLLOW_URSHIFT_in_shift5720); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_URSHIFT.add(URSHIFT377);

            	    pushFollow(FOLLOW_factor_in_shift5724);
            	    r=factor();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_factor.add(r.getTree());


            	    // AST REWRITE
            	    // elements: shift, URSHIFT, r
            	    // token labels: 
            	    // rule labels: retval, r
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            	    RewriteRuleSubtreeStream stream_r=new RewriteRuleSubtreeStream(adaptor,"rule r",r!=null?r.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 517:30: -> ^( URSHIFT $shift $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:517:33: ^( URSHIFT $shift $r)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot(stream_URSHIFT.nextNode(), root_1);

            	        adaptor.addChild(root_1, stream_retval.nextTree());
            	        adaptor.addChild(root_1, stream_r.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }


            	    }
            	    break;
            	case 4 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:518:9: ( CRSHIFT r= factor -> ^( CRSHIFT $shift $r) )
            	    {
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:518:9: ( CRSHIFT r= factor -> ^( CRSHIFT $shift $r) )
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:518:11: CRSHIFT r= factor
            	    {
            	    CRSHIFT378=(Token)match(input,CRSHIFT,FOLLOW_CRSHIFT_in_shift5752); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_CRSHIFT.add(CRSHIFT378);

            	    pushFollow(FOLLOW_factor_in_shift5756);
            	    r=factor();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_factor.add(r.getTree());


            	    // AST REWRITE
            	    // elements: shift, CRSHIFT, r
            	    // token labels: 
            	    // rule labels: retval, r
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            	    RewriteRuleSubtreeStream stream_r=new RewriteRuleSubtreeStream(adaptor,"rule r",r!=null?r.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 518:30: -> ^( CRSHIFT $shift $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:518:33: ^( CRSHIFT $shift $r)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot(stream_CRSHIFT.nextNode(), root_1);

            	        adaptor.addChild(root_1, stream_retval.nextTree());
            	        adaptor.addChild(root_1, stream_r.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }


            	    }
            	    break;
            	case 5 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:519:9: ( CLSHIFT r= factor -> ^( CLSHIFT $shift $r) )
            	    {
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:519:9: ( CLSHIFT r= factor -> ^( CLSHIFT $shift $r) )
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:519:11: CLSHIFT r= factor
            	    {
            	    CLSHIFT379=(Token)match(input,CLSHIFT,FOLLOW_CLSHIFT_in_shift5784); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_CLSHIFT.add(CLSHIFT379);

            	    pushFollow(FOLLOW_factor_in_shift5788);
            	    r=factor();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_factor.add(r.getTree());


            	    // AST REWRITE
            	    // elements: CLSHIFT, shift, r
            	    // token labels: 
            	    // rule labels: retval, r
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            	    RewriteRuleSubtreeStream stream_r=new RewriteRuleSubtreeStream(adaptor,"rule r",r!=null?r.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 519:30: -> ^( CLSHIFT $shift $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:519:33: ^( CLSHIFT $shift $r)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot(stream_CLSHIFT.nextNode(), root_1);

            	        adaptor.addChild(root_1, stream_retval.nextTree());
            	        adaptor.addChild(root_1, stream_r.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }


            	    }
            	    break;

            	default :
            	    break loop116;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "shift"

    public static class factor_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "factor"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:522:1: factor : ( term -> term ) ( PLUS r= term -> ^( ADD $factor $r) | ( MINUS term )=> MINUS r= term -> ^( SUB $factor $r) )* ;
    public final EulangParser.factor_return factor() throws RecognitionException {
        EulangParser.factor_return retval = new EulangParser.factor_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token PLUS381=null;
        Token MINUS382=null;
        EulangParser.term_return r = null;

        EulangParser.term_return term380 = null;


        CommonTree PLUS381_tree=null;
        CommonTree MINUS382_tree=null;
        RewriteRuleTokenStream stream_PLUS=new RewriteRuleTokenStream(adaptor,"token PLUS");
        RewriteRuleTokenStream stream_MINUS=new RewriteRuleTokenStream(adaptor,"token MINUS");
        RewriteRuleSubtreeStream stream_term=new RewriteRuleSubtreeStream(adaptor,"rule term");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:523:5: ( ( term -> term ) ( PLUS r= term -> ^( ADD $factor $r) | ( MINUS term )=> MINUS r= term -> ^( SUB $factor $r) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:523:7: ( term -> term ) ( PLUS r= term -> ^( ADD $factor $r) | ( MINUS term )=> MINUS r= term -> ^( SUB $factor $r) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:523:7: ( term -> term )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:523:9: term
            {
            pushFollow(FOLLOW_term_in_factor5830);
            term380=term();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_term.add(term380.getTree());


            // AST REWRITE
            // elements: term
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 523:27: -> term
            {
                adaptor.addChild(root_0, stream_term.nextTree());

            }

            retval.tree = root_0;}
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:524:9: ( PLUS r= term -> ^( ADD $factor $r) | ( MINUS term )=> MINUS r= term -> ^( SUB $factor $r) )*
            loop117:
            do {
                int alt117=3;
                int LA117_0 = input.LA(1);

                if ( (LA117_0==PLUS) ) {
                    alt117=1;
                }
                else if ( (LA117_0==MINUS) && (synpred19_Eulang())) {
                    alt117=2;
                }


                switch (alt117) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:524:13: PLUS r= term
            	    {
            	    PLUS381=(Token)match(input,PLUS,FOLLOW_PLUS_in_factor5863); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_PLUS.add(PLUS381);

            	    pushFollow(FOLLOW_term_in_factor5867);
            	    r=term();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_term.add(r.getTree());


            	    // AST REWRITE
            	    // elements: r, factor
            	    // token labels: 
            	    // rule labels: retval, r
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            	    RewriteRuleSubtreeStream stream_r=new RewriteRuleSubtreeStream(adaptor,"rule r",r!=null?r.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 524:33: -> ^( ADD $factor $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:524:36: ^( ADD $factor $r)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ADD, "ADD"), root_1);

            	        adaptor.addChild(root_1, stream_retval.nextTree());
            	        adaptor.addChild(root_1, stream_r.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }
            	    break;
            	case 2 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:525:12: ( MINUS term )=> MINUS r= term
            	    {
            	    MINUS382=(Token)match(input,MINUS,FOLLOW_MINUS_in_factor5909); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_MINUS.add(MINUS382);

            	    pushFollow(FOLLOW_term_in_factor5913);
            	    r=term();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_term.add(r.getTree());


            	    // AST REWRITE
            	    // elements: factor, r
            	    // token labels: 
            	    // rule labels: retval, r
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            	    RewriteRuleSubtreeStream stream_r=new RewriteRuleSubtreeStream(adaptor,"rule r",r!=null?r.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 525:49: -> ^( SUB $factor $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:525:52: ^( SUB $factor $r)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(SUB, "SUB"), root_1);

            	        adaptor.addChild(root_1, stream_retval.nextTree());
            	        adaptor.addChild(root_1, stream_r.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }
            	    break;

            	default :
            	    break loop117;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "factor"

    public static class term_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "term"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:529:1: term : ( unary -> unary ) ( ( STAR unary )=> STAR r= unary -> ^( MUL $term $r) | SLASH r= unary -> ^( DIV $term $r) | REM r= unary -> ^( REM $term $r) | UDIV r= unary -> ^( UDIV $term $r) | UREM r= unary -> ^( UREM $term $r) | MOD r= unary -> ^( MOD $term $r) )* ;
    public final EulangParser.term_return term() throws RecognitionException {
        EulangParser.term_return retval = new EulangParser.term_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token STAR384=null;
        Token SLASH385=null;
        Token REM386=null;
        Token UDIV387=null;
        Token UREM388=null;
        Token MOD389=null;
        EulangParser.unary_return r = null;

        EulangParser.unary_return unary383 = null;


        CommonTree STAR384_tree=null;
        CommonTree SLASH385_tree=null;
        CommonTree REM386_tree=null;
        CommonTree UDIV387_tree=null;
        CommonTree UREM388_tree=null;
        CommonTree MOD389_tree=null;
        RewriteRuleTokenStream stream_STAR=new RewriteRuleTokenStream(adaptor,"token STAR");
        RewriteRuleTokenStream stream_SLASH=new RewriteRuleTokenStream(adaptor,"token SLASH");
        RewriteRuleTokenStream stream_REM=new RewriteRuleTokenStream(adaptor,"token REM");
        RewriteRuleTokenStream stream_UREM=new RewriteRuleTokenStream(adaptor,"token UREM");
        RewriteRuleTokenStream stream_MOD=new RewriteRuleTokenStream(adaptor,"token MOD");
        RewriteRuleTokenStream stream_UDIV=new RewriteRuleTokenStream(adaptor,"token UDIV");
        RewriteRuleSubtreeStream stream_unary=new RewriteRuleSubtreeStream(adaptor,"rule unary");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:529:6: ( ( unary -> unary ) ( ( STAR unary )=> STAR r= unary -> ^( MUL $term $r) | SLASH r= unary -> ^( DIV $term $r) | REM r= unary -> ^( REM $term $r) | UDIV r= unary -> ^( UDIV $term $r) | UREM r= unary -> ^( UREM $term $r) | MOD r= unary -> ^( MOD $term $r) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:529:8: ( unary -> unary ) ( ( STAR unary )=> STAR r= unary -> ^( MUL $term $r) | SLASH r= unary -> ^( DIV $term $r) | REM r= unary -> ^( REM $term $r) | UDIV r= unary -> ^( UDIV $term $r) | UREM r= unary -> ^( UREM $term $r) | MOD r= unary -> ^( MOD $term $r) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:529:8: ( unary -> unary )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:529:10: unary
            {
            pushFollow(FOLLOW_unary_in_term5958);
            unary383=unary();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_unary.add(unary383.getTree());


            // AST REWRITE
            // elements: unary
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 529:33: -> unary
            {
                adaptor.addChild(root_0, stream_unary.nextTree());

            }

            retval.tree = root_0;}
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:530:9: ( ( STAR unary )=> STAR r= unary -> ^( MUL $term $r) | SLASH r= unary -> ^( DIV $term $r) | REM r= unary -> ^( REM $term $r) | UDIV r= unary -> ^( UDIV $term $r) | UREM r= unary -> ^( UREM $term $r) | MOD r= unary -> ^( MOD $term $r) )*
            loop118:
            do {
                int alt118=7;
                int LA118_0 = input.LA(1);

                if ( (LA118_0==STAR) && (synpred20_Eulang())) {
                    alt118=1;
                }
                else if ( (LA118_0==SLASH) ) {
                    alt118=2;
                }
                else if ( (LA118_0==REM) ) {
                    alt118=3;
                }
                else if ( (LA118_0==UDIV) ) {
                    alt118=4;
                }
                else if ( (LA118_0==UREM) ) {
                    alt118=5;
                }
                else if ( (LA118_0==MOD) ) {
                    alt118=6;
                }


                switch (alt118) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:530:11: ( STAR unary )=> STAR r= unary
            	    {
            	    STAR384=(Token)match(input,STAR,FOLLOW_STAR_in_term6002); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_STAR.add(STAR384);

            	    pushFollow(FOLLOW_unary_in_term6006);
            	    r=unary();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_unary.add(r.getTree());


            	    // AST REWRITE
            	    // elements: r, term
            	    // token labels: 
            	    // rule labels: retval, r
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            	    RewriteRuleSubtreeStream stream_r=new RewriteRuleSubtreeStream(adaptor,"rule r",r!=null?r.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 530:52: -> ^( MUL $term $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:530:55: ^( MUL $term $r)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(MUL, "MUL"), root_1);

            	        adaptor.addChild(root_1, stream_retval.nextTree());
            	        adaptor.addChild(root_1, stream_r.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }
            	    break;
            	case 2 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:531:11: SLASH r= unary
            	    {
            	    SLASH385=(Token)match(input,SLASH,FOLLOW_SLASH_in_term6042); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_SLASH.add(SLASH385);

            	    pushFollow(FOLLOW_unary_in_term6046);
            	    r=unary();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_unary.add(r.getTree());


            	    // AST REWRITE
            	    // elements: term, r
            	    // token labels: 
            	    // rule labels: retval, r
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            	    RewriteRuleSubtreeStream stream_r=new RewriteRuleSubtreeStream(adaptor,"rule r",r!=null?r.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 531:36: -> ^( DIV $term $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:531:39: ^( DIV $term $r)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(DIV, "DIV"), root_1);

            	        adaptor.addChild(root_1, stream_retval.nextTree());
            	        adaptor.addChild(root_1, stream_r.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }
            	    break;
            	case 3 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:532:11: REM r= unary
            	    {
            	    REM386=(Token)match(input,REM,FOLLOW_REM_in_term6081); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_REM.add(REM386);

            	    pushFollow(FOLLOW_unary_in_term6085);
            	    r=unary();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_unary.add(r.getTree());


            	    // AST REWRITE
            	    // elements: term, REM, r
            	    // token labels: 
            	    // rule labels: retval, r
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            	    RewriteRuleSubtreeStream stream_r=new RewriteRuleSubtreeStream(adaptor,"rule r",r!=null?r.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 532:34: -> ^( REM $term $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:532:37: ^( REM $term $r)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot(stream_REM.nextNode(), root_1);

            	        adaptor.addChild(root_1, stream_retval.nextTree());
            	        adaptor.addChild(root_1, stream_r.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }
            	    break;
            	case 4 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:533:11: UDIV r= unary
            	    {
            	    UDIV387=(Token)match(input,UDIV,FOLLOW_UDIV_in_term6120); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_UDIV.add(UDIV387);

            	    pushFollow(FOLLOW_unary_in_term6124);
            	    r=unary();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_unary.add(r.getTree());


            	    // AST REWRITE
            	    // elements: r, UDIV, term
            	    // token labels: 
            	    // rule labels: retval, r
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            	    RewriteRuleSubtreeStream stream_r=new RewriteRuleSubtreeStream(adaptor,"rule r",r!=null?r.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 533:35: -> ^( UDIV $term $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:533:38: ^( UDIV $term $r)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot(stream_UDIV.nextNode(), root_1);

            	        adaptor.addChild(root_1, stream_retval.nextTree());
            	        adaptor.addChild(root_1, stream_r.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }
            	    break;
            	case 5 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:534:11: UREM r= unary
            	    {
            	    UREM388=(Token)match(input,UREM,FOLLOW_UREM_in_term6159); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_UREM.add(UREM388);

            	    pushFollow(FOLLOW_unary_in_term6163);
            	    r=unary();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_unary.add(r.getTree());


            	    // AST REWRITE
            	    // elements: term, r, UREM
            	    // token labels: 
            	    // rule labels: retval, r
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            	    RewriteRuleSubtreeStream stream_r=new RewriteRuleSubtreeStream(adaptor,"rule r",r!=null?r.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 534:35: -> ^( UREM $term $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:534:38: ^( UREM $term $r)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot(stream_UREM.nextNode(), root_1);

            	        adaptor.addChild(root_1, stream_retval.nextTree());
            	        adaptor.addChild(root_1, stream_r.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }
            	    break;
            	case 6 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:535:11: MOD r= unary
            	    {
            	    MOD389=(Token)match(input,MOD,FOLLOW_MOD_in_term6198); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_MOD.add(MOD389);

            	    pushFollow(FOLLOW_unary_in_term6202);
            	    r=unary();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_unary.add(r.getTree());


            	    // AST REWRITE
            	    // elements: r, term, MOD
            	    // token labels: 
            	    // rule labels: retval, r
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            	    RewriteRuleSubtreeStream stream_r=new RewriteRuleSubtreeStream(adaptor,"rule r",r!=null?r.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 535:34: -> ^( MOD $term $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:535:37: ^( MOD $term $r)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot(stream_MOD.nextNode(), root_1);

            	        adaptor.addChild(root_1, stream_retval.nextTree());
            	        adaptor.addChild(root_1, stream_r.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }
            	    break;

            	default :
            	    break loop118;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "term"

    public static class unary_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "unary"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:540:1: unary : ( MINUS u= unary -> ^( NEG $u) | TILDE u= unary -> ^( INV $u) | ( lhs PLUSPLUS )=>a= lhs PLUSPLUS -> ^( POSTINC $a) | ( lhs MINUSMINUS )=>a= lhs MINUSMINUS -> ^( POSTDEC $a) | ( atom -> atom ) | PLUSPLUS a= lhs -> ^( PREINC $a) | MINUSMINUS a= lhs -> ^( PREDEC $a) | AMP lhs -> ^( ADDROF lhs ) | SIZEOF atom -> ^( SIZEOF atom ) | TYPEOF atom -> ^( TYPEOF atom ) );
    public final EulangParser.unary_return unary() throws RecognitionException {
        EulangParser.unary_return retval = new EulangParser.unary_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token MINUS390=null;
        Token TILDE391=null;
        Token PLUSPLUS392=null;
        Token MINUSMINUS393=null;
        Token PLUSPLUS395=null;
        Token MINUSMINUS396=null;
        Token AMP397=null;
        Token SIZEOF399=null;
        Token TYPEOF401=null;
        EulangParser.unary_return u = null;

        EulangParser.lhs_return a = null;

        EulangParser.atom_return atom394 = null;

        EulangParser.lhs_return lhs398 = null;

        EulangParser.atom_return atom400 = null;

        EulangParser.atom_return atom402 = null;


        CommonTree MINUS390_tree=null;
        CommonTree TILDE391_tree=null;
        CommonTree PLUSPLUS392_tree=null;
        CommonTree MINUSMINUS393_tree=null;
        CommonTree PLUSPLUS395_tree=null;
        CommonTree MINUSMINUS396_tree=null;
        CommonTree AMP397_tree=null;
        CommonTree SIZEOF399_tree=null;
        CommonTree TYPEOF401_tree=null;
        RewriteRuleTokenStream stream_SIZEOF=new RewriteRuleTokenStream(adaptor,"token SIZEOF");
        RewriteRuleTokenStream stream_MINUSMINUS=new RewriteRuleTokenStream(adaptor,"token MINUSMINUS");
        RewriteRuleTokenStream stream_MINUS=new RewriteRuleTokenStream(adaptor,"token MINUS");
        RewriteRuleTokenStream stream_TYPEOF=new RewriteRuleTokenStream(adaptor,"token TYPEOF");
        RewriteRuleTokenStream stream_AMP=new RewriteRuleTokenStream(adaptor,"token AMP");
        RewriteRuleTokenStream stream_PLUSPLUS=new RewriteRuleTokenStream(adaptor,"token PLUSPLUS");
        RewriteRuleTokenStream stream_TILDE=new RewriteRuleTokenStream(adaptor,"token TILDE");
        RewriteRuleSubtreeStream stream_unary=new RewriteRuleSubtreeStream(adaptor,"rule unary");
        RewriteRuleSubtreeStream stream_atom=new RewriteRuleSubtreeStream(adaptor,"rule atom");
        RewriteRuleSubtreeStream stream_lhs=new RewriteRuleSubtreeStream(adaptor,"rule lhs");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:540:6: ( MINUS u= unary -> ^( NEG $u) | TILDE u= unary -> ^( INV $u) | ( lhs PLUSPLUS )=>a= lhs PLUSPLUS -> ^( POSTINC $a) | ( lhs MINUSMINUS )=>a= lhs MINUSMINUS -> ^( POSTDEC $a) | ( atom -> atom ) | PLUSPLUS a= lhs -> ^( PREINC $a) | MINUSMINUS a= lhs -> ^( PREDEC $a) | AMP lhs -> ^( ADDROF lhs ) | SIZEOF atom -> ^( SIZEOF atom ) | TYPEOF atom -> ^( TYPEOF atom ) )
            int alt119=10;
            alt119 = dfa119.predict(input);
            switch (alt119) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:540:9: MINUS u= unary
                    {
                    MINUS390=(Token)match(input,MINUS,FOLLOW_MINUS_in_unary6275); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_MINUS.add(MINUS390);

                    pushFollow(FOLLOW_unary_in_unary6279);
                    u=unary();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_unary.add(u.getTree());


                    // AST REWRITE
                    // elements: u
                    // token labels: 
                    // rule labels: retval, u
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                    RewriteRuleSubtreeStream stream_u=new RewriteRuleSubtreeStream(adaptor,"rule u",u!=null?u.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 540:23: -> ^( NEG $u)
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:540:26: ^( NEG $u)
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(NEG, "NEG"), root_1);

                        adaptor.addChild(root_1, stream_u.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:541:9: TILDE u= unary
                    {
                    TILDE391=(Token)match(input,TILDE,FOLLOW_TILDE_in_unary6299); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_TILDE.add(TILDE391);

                    pushFollow(FOLLOW_unary_in_unary6303);
                    u=unary();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_unary.add(u.getTree());


                    // AST REWRITE
                    // elements: u
                    // token labels: 
                    // rule labels: retval, u
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                    RewriteRuleSubtreeStream stream_u=new RewriteRuleSubtreeStream(adaptor,"rule u",u!=null?u.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 541:27: -> ^( INV $u)
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:541:30: ^( INV $u)
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(INV, "INV"), root_1);

                        adaptor.addChild(root_1, stream_u.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 3 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:542:9: ( lhs PLUSPLUS )=>a= lhs PLUSPLUS
                    {
                    pushFollow(FOLLOW_lhs_in_unary6338);
                    a=lhs();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_lhs.add(a.getTree());
                    PLUSPLUS392=(Token)match(input,PLUSPLUS,FOLLOW_PLUSPLUS_in_unary6340); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_PLUSPLUS.add(PLUSPLUS392);



                    // AST REWRITE
                    // elements: a
                    // token labels: 
                    // rule labels: retval, a
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                    RewriteRuleSubtreeStream stream_a=new RewriteRuleSubtreeStream(adaptor,"rule a",a!=null?a.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 542:44: -> ^( POSTINC $a)
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:542:47: ^( POSTINC $a)
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(POSTINC, "POSTINC"), root_1);

                        adaptor.addChild(root_1, stream_a.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 4 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:543:9: ( lhs MINUSMINUS )=>a= lhs MINUSMINUS
                    {
                    pushFollow(FOLLOW_lhs_in_unary6371);
                    a=lhs();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_lhs.add(a.getTree());
                    MINUSMINUS393=(Token)match(input,MINUSMINUS,FOLLOW_MINUSMINUS_in_unary6373); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_MINUSMINUS.add(MINUSMINUS393);



                    // AST REWRITE
                    // elements: a
                    // token labels: 
                    // rule labels: retval, a
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                    RewriteRuleSubtreeStream stream_a=new RewriteRuleSubtreeStream(adaptor,"rule a",a!=null?a.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 543:47: -> ^( POSTDEC $a)
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:543:50: ^( POSTDEC $a)
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(POSTDEC, "POSTDEC"), root_1);

                        adaptor.addChild(root_1, stream_a.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 5 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:544:9: ( atom -> atom )
                    {
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:544:9: ( atom -> atom )
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:544:11: atom
                    {
                    pushFollow(FOLLOW_atom_in_unary6394);
                    atom394=atom();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_atom.add(atom394.getTree());


                    // AST REWRITE
                    // elements: atom
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 544:23: -> atom
                    {
                        adaptor.addChild(root_0, stream_atom.nextTree());

                    }

                    retval.tree = root_0;}
                    }


                    }
                    break;
                case 6 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:545:9: PLUSPLUS a= lhs
                    {
                    PLUSPLUS395=(Token)match(input,PLUSPLUS,FOLLOW_PLUSPLUS_in_unary6425); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_PLUSPLUS.add(PLUSPLUS395);

                    pushFollow(FOLLOW_lhs_in_unary6429);
                    a=lhs();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_lhs.add(a.getTree());


                    // AST REWRITE
                    // elements: a
                    // token labels: 
                    // rule labels: retval, a
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                    RewriteRuleSubtreeStream stream_a=new RewriteRuleSubtreeStream(adaptor,"rule a",a!=null?a.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 545:26: -> ^( PREINC $a)
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:545:29: ^( PREINC $a)
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(PREINC, "PREINC"), root_1);

                        adaptor.addChild(root_1, stream_a.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 7 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:546:9: MINUSMINUS a= lhs
                    {
                    MINUSMINUS396=(Token)match(input,MINUSMINUS,FOLLOW_MINUSMINUS_in_unary6450); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_MINUSMINUS.add(MINUSMINUS396);

                    pushFollow(FOLLOW_lhs_in_unary6454);
                    a=lhs();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_lhs.add(a.getTree());


                    // AST REWRITE
                    // elements: a
                    // token labels: 
                    // rule labels: retval, a
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                    RewriteRuleSubtreeStream stream_a=new RewriteRuleSubtreeStream(adaptor,"rule a",a!=null?a.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 546:26: -> ^( PREDEC $a)
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:546:29: ^( PREDEC $a)
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(PREDEC, "PREDEC"), root_1);

                        adaptor.addChild(root_1, stream_a.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 8 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:547:10: AMP lhs
                    {
                    AMP397=(Token)match(input,AMP,FOLLOW_AMP_in_unary6474); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_AMP.add(AMP397);

                    pushFollow(FOLLOW_lhs_in_unary6476);
                    lhs398=lhs();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_lhs.add(lhs398.getTree());


                    // AST REWRITE
                    // elements: lhs
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 547:41: -> ^( ADDROF lhs )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:547:44: ^( ADDROF lhs )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ADDROF, "ADDROF"), root_1);

                        adaptor.addChild(root_1, stream_lhs.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 9 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:548:9: SIZEOF atom
                    {
                    SIZEOF399=(Token)match(input,SIZEOF,FOLLOW_SIZEOF_in_unary6517); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_SIZEOF.add(SIZEOF399);

                    pushFollow(FOLLOW_atom_in_unary6519);
                    atom400=atom();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_atom.add(atom400.getTree());


                    // AST REWRITE
                    // elements: SIZEOF, atom
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 548:27: -> ^( SIZEOF atom )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:548:30: ^( SIZEOF atom )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot(stream_SIZEOF.nextNode(), root_1);

                        adaptor.addChild(root_1, stream_atom.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 10 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:549:9: TYPEOF atom
                    {
                    TYPEOF401=(Token)match(input,TYPEOF,FOLLOW_TYPEOF_in_unary6543); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_TYPEOF.add(TYPEOF401);

                    pushFollow(FOLLOW_atom_in_unary6545);
                    atom402=atom();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_atom.add(atom402.getTree());


                    // AST REWRITE
                    // elements: atom, TYPEOF
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 549:27: -> ^( TYPEOF atom )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:549:30: ^( TYPEOF atom )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot(stream_TYPEOF.nextNode(), root_1);

                        adaptor.addChild(root_1, stream_atom.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "unary"

    public static class lhs_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "lhs"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:552:1: lhs : ( idExpr -> idExpr | ( tuple )=> tuple -> tuple | NUMBER -> ^( LIT NUMBER ) | CHAR_LITERAL -> ^( LIT CHAR_LITERAL ) | STRING_LITERAL -> ^( LIT STRING_LITERAL ) | LPAREN a1= assignExpr RPAREN -> $a1) ( ( PERIOD ID -> ^( FIELDREF $lhs ID ) ) | ( LPAREN arglist RPAREN -> ^( CALL $lhs arglist ) ) | ( ( LBRACKET )=> arrayAccess -> ^( INDEX $lhs arrayAccess ) ) | ( CARET -> ^( DEREF $lhs) ) | ( LBRACE ( PLUS )? type RBRACE -> ^( CAST ( PLUS )? type $lhs) ) )* ( AS ( PLUS )? type -> ^( CAST ( PLUS )? type $lhs) )? ;
    public final EulangParser.lhs_return lhs() throws RecognitionException {
        EulangParser.lhs_return retval = new EulangParser.lhs_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token NUMBER405=null;
        Token CHAR_LITERAL406=null;
        Token STRING_LITERAL407=null;
        Token LPAREN408=null;
        Token RPAREN409=null;
        Token PERIOD410=null;
        Token ID411=null;
        Token LPAREN412=null;
        Token RPAREN414=null;
        Token CARET416=null;
        Token LBRACE417=null;
        Token PLUS418=null;
        Token RBRACE420=null;
        Token AS421=null;
        Token PLUS422=null;
        EulangParser.assignExpr_return a1 = null;

        EulangParser.idExpr_return idExpr403 = null;

        EulangParser.tuple_return tuple404 = null;

        EulangParser.arglist_return arglist413 = null;

        EulangParser.arrayAccess_return arrayAccess415 = null;

        EulangParser.type_return type419 = null;

        EulangParser.type_return type423 = null;


        CommonTree NUMBER405_tree=null;
        CommonTree CHAR_LITERAL406_tree=null;
        CommonTree STRING_LITERAL407_tree=null;
        CommonTree LPAREN408_tree=null;
        CommonTree RPAREN409_tree=null;
        CommonTree PERIOD410_tree=null;
        CommonTree ID411_tree=null;
        CommonTree LPAREN412_tree=null;
        CommonTree RPAREN414_tree=null;
        CommonTree CARET416_tree=null;
        CommonTree LBRACE417_tree=null;
        CommonTree PLUS418_tree=null;
        CommonTree RBRACE420_tree=null;
        CommonTree AS421_tree=null;
        CommonTree PLUS422_tree=null;
        RewriteRuleTokenStream stream_AS=new RewriteRuleTokenStream(adaptor,"token AS");
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_PLUS=new RewriteRuleTokenStream(adaptor,"token PLUS");
        RewriteRuleTokenStream stream_STRING_LITERAL=new RewriteRuleTokenStream(adaptor,"token STRING_LITERAL");
        RewriteRuleTokenStream stream_RBRACE=new RewriteRuleTokenStream(adaptor,"token RBRACE");
        RewriteRuleTokenStream stream_CHAR_LITERAL=new RewriteRuleTokenStream(adaptor,"token CHAR_LITERAL");
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
        RewriteRuleTokenStream stream_PERIOD=new RewriteRuleTokenStream(adaptor,"token PERIOD");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleTokenStream stream_CARET=new RewriteRuleTokenStream(adaptor,"token CARET");
        RewriteRuleTokenStream stream_LBRACE=new RewriteRuleTokenStream(adaptor,"token LBRACE");
        RewriteRuleTokenStream stream_NUMBER=new RewriteRuleTokenStream(adaptor,"token NUMBER");
        RewriteRuleSubtreeStream stream_arglist=new RewriteRuleSubtreeStream(adaptor,"rule arglist");
        RewriteRuleSubtreeStream stream_arrayAccess=new RewriteRuleSubtreeStream(adaptor,"rule arrayAccess");
        RewriteRuleSubtreeStream stream_idExpr=new RewriteRuleSubtreeStream(adaptor,"rule idExpr");
        RewriteRuleSubtreeStream stream_tuple=new RewriteRuleSubtreeStream(adaptor,"rule tuple");
        RewriteRuleSubtreeStream stream_assignExpr=new RewriteRuleSubtreeStream(adaptor,"rule assignExpr");
        RewriteRuleSubtreeStream stream_type=new RewriteRuleSubtreeStream(adaptor,"rule type");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:552:5: ( ( idExpr -> idExpr | ( tuple )=> tuple -> tuple | NUMBER -> ^( LIT NUMBER ) | CHAR_LITERAL -> ^( LIT CHAR_LITERAL ) | STRING_LITERAL -> ^( LIT STRING_LITERAL ) | LPAREN a1= assignExpr RPAREN -> $a1) ( ( PERIOD ID -> ^( FIELDREF $lhs ID ) ) | ( LPAREN arglist RPAREN -> ^( CALL $lhs arglist ) ) | ( ( LBRACKET )=> arrayAccess -> ^( INDEX $lhs arrayAccess ) ) | ( CARET -> ^( DEREF $lhs) ) | ( LBRACE ( PLUS )? type RBRACE -> ^( CAST ( PLUS )? type $lhs) ) )* ( AS ( PLUS )? type -> ^( CAST ( PLUS )? type $lhs) )? )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:553:3: ( idExpr -> idExpr | ( tuple )=> tuple -> tuple | NUMBER -> ^( LIT NUMBER ) | CHAR_LITERAL -> ^( LIT CHAR_LITERAL ) | STRING_LITERAL -> ^( LIT STRING_LITERAL ) | LPAREN a1= assignExpr RPAREN -> $a1) ( ( PERIOD ID -> ^( FIELDREF $lhs ID ) ) | ( LPAREN arglist RPAREN -> ^( CALL $lhs arglist ) ) | ( ( LBRACKET )=> arrayAccess -> ^( INDEX $lhs arrayAccess ) ) | ( CARET -> ^( DEREF $lhs) ) | ( LBRACE ( PLUS )? type RBRACE -> ^( CAST ( PLUS )? type $lhs) ) )* ( AS ( PLUS )? type -> ^( CAST ( PLUS )? type $lhs) )?
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:553:3: ( idExpr -> idExpr | ( tuple )=> tuple -> tuple | NUMBER -> ^( LIT NUMBER ) | CHAR_LITERAL -> ^( LIT CHAR_LITERAL ) | STRING_LITERAL -> ^( LIT STRING_LITERAL ) | LPAREN a1= assignExpr RPAREN -> $a1)
            int alt120=6;
            switch ( input.LA(1) ) {
            case ID:
            case COLON:
            case COLONS:
                {
                alt120=1;
                }
                break;
            case LPAREN:
                {
                int LA120_3 = input.LA(2);

                if ( (synpred23_Eulang()) ) {
                    alt120=2;
                }
                else if ( (true) ) {
                    alt120=6;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 120, 3, input);

                    throw nvae;
                }
                }
                break;
            case NUMBER:
                {
                alt120=3;
                }
                break;
            case CHAR_LITERAL:
                {
                alt120=4;
                }
                break;
            case STRING_LITERAL:
                {
                alt120=5;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 120, 0, input);

                throw nvae;
            }

            switch (alt120) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:554:8: idExpr
                    {
                    pushFollow(FOLLOW_idExpr_in_lhs6580);
                    idExpr403=idExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_idExpr.add(idExpr403.getTree());


                    // AST REWRITE
                    // elements: idExpr
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 554:40: -> idExpr
                    {
                        adaptor.addChild(root_0, stream_idExpr.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:555:9: ( tuple )=> tuple
                    {
                    pushFollow(FOLLOW_tuple_in_lhs6627);
                    tuple404=tuple();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_tuple.add(tuple404.getTree());


                    // AST REWRITE
                    // elements: tuple
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 555:53: -> tuple
                    {
                        adaptor.addChild(root_0, stream_tuple.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 3 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:556:9: NUMBER
                    {
                    NUMBER405=(Token)match(input,NUMBER,FOLLOW_NUMBER_in_lhs6666); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_NUMBER.add(NUMBER405);



                    // AST REWRITE
                    // elements: NUMBER
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 556:41: -> ^( LIT NUMBER )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:556:44: ^( LIT NUMBER )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(LIT, "LIT"), root_1);

                        adaptor.addChild(root_1, stream_NUMBER.nextNode());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 4 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:557:9: CHAR_LITERAL
                    {
                    CHAR_LITERAL406=(Token)match(input,CHAR_LITERAL,FOLLOW_CHAR_LITERAL_in_lhs6709); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_CHAR_LITERAL.add(CHAR_LITERAL406);



                    // AST REWRITE
                    // elements: CHAR_LITERAL
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 557:39: -> ^( LIT CHAR_LITERAL )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:557:42: ^( LIT CHAR_LITERAL )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(LIT, "LIT"), root_1);

                        adaptor.addChild(root_1, stream_CHAR_LITERAL.nextNode());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 5 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:558:9: STRING_LITERAL
                    {
                    STRING_LITERAL407=(Token)match(input,STRING_LITERAL,FOLLOW_STRING_LITERAL_in_lhs6744); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_STRING_LITERAL.add(STRING_LITERAL407);



                    // AST REWRITE
                    // elements: STRING_LITERAL
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 558:39: -> ^( LIT STRING_LITERAL )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:558:42: ^( LIT STRING_LITERAL )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(LIT, "LIT"), root_1);

                        adaptor.addChild(root_1, stream_STRING_LITERAL.nextNode());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 6 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:559:9: LPAREN a1= assignExpr RPAREN
                    {
                    LPAREN408=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_lhs6777); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN408);

                    pushFollow(FOLLOW_assignExpr_in_lhs6781);
                    a1=assignExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_assignExpr.add(a1.getTree());
                    RPAREN409=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_lhs6783); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN409);



                    // AST REWRITE
                    // elements: a1
                    // token labels: 
                    // rule labels: retval, a1
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                    RewriteRuleSubtreeStream stream_a1=new RewriteRuleSubtreeStream(adaptor,"rule a1",a1!=null?a1.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 559:51: -> $a1
                    {
                        adaptor.addChild(root_0, stream_a1.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:561:5: ( ( PERIOD ID -> ^( FIELDREF $lhs ID ) ) | ( LPAREN arglist RPAREN -> ^( CALL $lhs arglist ) ) | ( ( LBRACKET )=> arrayAccess -> ^( INDEX $lhs arrayAccess ) ) | ( CARET -> ^( DEREF $lhs) ) | ( LBRACE ( PLUS )? type RBRACE -> ^( CAST ( PLUS )? type $lhs) ) )*
            loop122:
            do {
                int alt122=6;
                int LA122_0 = input.LA(1);

                if ( (LA122_0==PERIOD) ) {
                    alt122=1;
                }
                else if ( (LA122_0==LPAREN) ) {
                    alt122=2;
                }
                else if ( (LA122_0==LBRACKET) && (synpred24_Eulang())) {
                    alt122=3;
                }
                else if ( (LA122_0==CARET) ) {
                    alt122=4;
                }
                else if ( (LA122_0==LBRACE) ) {
                    alt122=5;
                }


                switch (alt122) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:562:7: ( PERIOD ID -> ^( FIELDREF $lhs ID ) )
            	    {
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:562:7: ( PERIOD ID -> ^( FIELDREF $lhs ID ) )
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:562:9: PERIOD ID
            	    {
            	    PERIOD410=(Token)match(input,PERIOD,FOLLOW_PERIOD_in_lhs6826); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_PERIOD.add(PERIOD410);

            	    ID411=(Token)match(input,ID,FOLLOW_ID_in_lhs6828); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_ID.add(ID411);



            	    // AST REWRITE
            	    // elements: lhs, ID
            	    // token labels: 
            	    // rule labels: retval
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 562:20: -> ^( FIELDREF $lhs ID )
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:562:23: ^( FIELDREF $lhs ID )
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(FIELDREF, "FIELDREF"), root_1);

            	        adaptor.addChild(root_1, stream_retval.nextTree());
            	        adaptor.addChild(root_1, stream_ID.nextNode());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }


            	    }
            	    break;
            	case 2 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:563:7: ( LPAREN arglist RPAREN -> ^( CALL $lhs arglist ) )
            	    {
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:563:7: ( LPAREN arglist RPAREN -> ^( CALL $lhs arglist ) )
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:563:10: LPAREN arglist RPAREN
            	    {
            	    LPAREN412=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_lhs6853); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN412);

            	    pushFollow(FOLLOW_arglist_in_lhs6855);
            	    arglist413=arglist();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_arglist.add(arglist413.getTree());
            	    RPAREN414=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_lhs6857); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN414);



            	    // AST REWRITE
            	    // elements: arglist, lhs
            	    // token labels: 
            	    // rule labels: retval
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 563:34: -> ^( CALL $lhs arglist )
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:563:37: ^( CALL $lhs arglist )
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(CALL, "CALL"), root_1);

            	        adaptor.addChild(root_1, stream_retval.nextTree());
            	        adaptor.addChild(root_1, stream_arglist.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }


            	    }
            	    break;
            	case 3 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:564:7: ( ( LBRACKET )=> arrayAccess -> ^( INDEX $lhs arrayAccess ) )
            	    {
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:564:7: ( ( LBRACKET )=> arrayAccess -> ^( INDEX $lhs arrayAccess ) )
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:564:9: ( LBRACKET )=> arrayAccess
            	    {
            	    pushFollow(FOLLOW_arrayAccess_in_lhs6890);
            	    arrayAccess415=arrayAccess();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_arrayAccess.add(arrayAccess415.getTree());


            	    // AST REWRITE
            	    // elements: lhs, arrayAccess
            	    // token labels: 
            	    // rule labels: retval
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 564:39: -> ^( INDEX $lhs arrayAccess )
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:564:42: ^( INDEX $lhs arrayAccess )
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(INDEX, "INDEX"), root_1);

            	        adaptor.addChild(root_1, stream_retval.nextTree());
            	        adaptor.addChild(root_1, stream_arrayAccess.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }


            	    }
            	    break;
            	case 4 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:565:7: ( CARET -> ^( DEREF $lhs) )
            	    {
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:565:7: ( CARET -> ^( DEREF $lhs) )
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:565:9: CARET
            	    {
            	    CARET416=(Token)match(input,CARET,FOLLOW_CARET_in_lhs6915); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_CARET.add(CARET416);



            	    // AST REWRITE
            	    // elements: lhs
            	    // token labels: 
            	    // rule labels: retval
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 565:15: -> ^( DEREF $lhs)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:565:18: ^( DEREF $lhs)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(DEREF, "DEREF"), root_1);

            	        adaptor.addChild(root_1, stream_retval.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }


            	    }
            	    break;
            	case 5 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:566:7: ( LBRACE ( PLUS )? type RBRACE -> ^( CAST ( PLUS )? type $lhs) )
            	    {
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:566:7: ( LBRACE ( PLUS )? type RBRACE -> ^( CAST ( PLUS )? type $lhs) )
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:566:9: LBRACE ( PLUS )? type RBRACE
            	    {
            	    LBRACE417=(Token)match(input,LBRACE,FOLLOW_LBRACE_in_lhs6936); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_LBRACE.add(LBRACE417);

            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:566:16: ( PLUS )?
            	    int alt121=2;
            	    int LA121_0 = input.LA(1);

            	    if ( (LA121_0==PLUS) ) {
            	        alt121=1;
            	    }
            	    switch (alt121) {
            	        case 1 :
            	            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:566:16: PLUS
            	            {
            	            PLUS418=(Token)match(input,PLUS,FOLLOW_PLUS_in_lhs6938); if (state.failed) return retval; 
            	            if ( state.backtracking==0 ) stream_PLUS.add(PLUS418);


            	            }
            	            break;

            	    }

            	    pushFollow(FOLLOW_type_in_lhs6941);
            	    type419=type();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_type.add(type419.getTree());
            	    RBRACE420=(Token)match(input,RBRACE,FOLLOW_RBRACE_in_lhs6943); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_RBRACE.add(RBRACE420);



            	    // AST REWRITE
            	    // elements: PLUS, type, lhs
            	    // token labels: 
            	    // rule labels: retval
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 566:34: -> ^( CAST ( PLUS )? type $lhs)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:566:37: ^( CAST ( PLUS )? type $lhs)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(CAST, "CAST"), root_1);

            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:566:44: ( PLUS )?
            	        if ( stream_PLUS.hasNext() ) {
            	            adaptor.addChild(root_1, stream_PLUS.nextNode());

            	        }
            	        stream_PLUS.reset();
            	        adaptor.addChild(root_1, stream_type.nextTree());
            	        adaptor.addChild(root_1, stream_retval.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }


            	    }
            	    break;

            	default :
            	    break loop122;
                }
            } while (true);

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:569:5: ( AS ( PLUS )? type -> ^( CAST ( PLUS )? type $lhs) )?
            int alt124=2;
            int LA124_0 = input.LA(1);

            if ( (LA124_0==AS) ) {
                alt124=1;
            }
            switch (alt124) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:570:7: AS ( PLUS )? type
                    {
                    AS421=(Token)match(input,AS,FOLLOW_AS_in_lhs6984); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_AS.add(AS421);

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:570:10: ( PLUS )?
                    int alt123=2;
                    int LA123_0 = input.LA(1);

                    if ( (LA123_0==PLUS) ) {
                        alt123=1;
                    }
                    switch (alt123) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:570:10: PLUS
                            {
                            PLUS422=(Token)match(input,PLUS,FOLLOW_PLUS_in_lhs6986); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_PLUS.add(PLUS422);


                            }
                            break;

                    }

                    pushFollow(FOLLOW_type_in_lhs6989);
                    type423=type();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_type.add(type423.getTree());


                    // AST REWRITE
                    // elements: PLUS, lhs, type
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 570:21: -> ^( CAST ( PLUS )? type $lhs)
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:570:24: ^( CAST ( PLUS )? type $lhs)
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(CAST, "CAST"), root_1);

                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:570:31: ( PLUS )?
                        if ( stream_PLUS.hasNext() ) {
                            adaptor.addChild(root_1, stream_PLUS.nextNode());

                        }
                        stream_PLUS.reset();
                        adaptor.addChild(root_1, stream_type.nextTree());
                        adaptor.addChild(root_1, stream_retval.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "lhs"

    public static class atom_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "atom"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:574:1: atom : ( NUMBER -> ^( LIT NUMBER ) | FALSE -> ^( LIT FALSE ) | TRUE -> ^( LIT TRUE ) | CHAR_LITERAL -> ^( LIT CHAR_LITERAL ) | STRING_LITERAL -> ^( LIT STRING_LITERAL ) | NIL -> ^( LIT NIL ) | idExpr -> idExpr | ( tuple )=> tuple -> tuple | ( LPAREN varDecl )=> LPAREN a0= varDecl RPAREN -> $a0 | LPAREN a1= assignExpr RPAREN -> $a1 | code -> code ) ( ( PERIOD ID -> ^( FIELDREF $atom ID ) ) | ( LPAREN arglist RPAREN -> ^( CALL $atom arglist ) ) | ( ( LBRACKET )=> arrayAccess -> ^( INDEX $atom arrayAccess ) ) | ( CARET -> ^( DEREF $atom) ) | ( LBRACE ( PLUS )? type RBRACE -> ^( CAST ( PLUS )? type $atom) ) )* ( AS type -> ^( CAST type $atom) )? ;
    public final EulangParser.atom_return atom() throws RecognitionException {
        EulangParser.atom_return retval = new EulangParser.atom_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token NUMBER424=null;
        Token FALSE425=null;
        Token TRUE426=null;
        Token CHAR_LITERAL427=null;
        Token STRING_LITERAL428=null;
        Token NIL429=null;
        Token LPAREN432=null;
        Token RPAREN433=null;
        Token LPAREN434=null;
        Token RPAREN435=null;
        Token PERIOD437=null;
        Token ID438=null;
        Token LPAREN439=null;
        Token RPAREN441=null;
        Token CARET443=null;
        Token LBRACE444=null;
        Token PLUS445=null;
        Token RBRACE447=null;
        Token AS448=null;
        EulangParser.varDecl_return a0 = null;

        EulangParser.assignExpr_return a1 = null;

        EulangParser.idExpr_return idExpr430 = null;

        EulangParser.tuple_return tuple431 = null;

        EulangParser.code_return code436 = null;

        EulangParser.arglist_return arglist440 = null;

        EulangParser.arrayAccess_return arrayAccess442 = null;

        EulangParser.type_return type446 = null;

        EulangParser.type_return type449 = null;


        CommonTree NUMBER424_tree=null;
        CommonTree FALSE425_tree=null;
        CommonTree TRUE426_tree=null;
        CommonTree CHAR_LITERAL427_tree=null;
        CommonTree STRING_LITERAL428_tree=null;
        CommonTree NIL429_tree=null;
        CommonTree LPAREN432_tree=null;
        CommonTree RPAREN433_tree=null;
        CommonTree LPAREN434_tree=null;
        CommonTree RPAREN435_tree=null;
        CommonTree PERIOD437_tree=null;
        CommonTree ID438_tree=null;
        CommonTree LPAREN439_tree=null;
        CommonTree RPAREN441_tree=null;
        CommonTree CARET443_tree=null;
        CommonTree LBRACE444_tree=null;
        CommonTree PLUS445_tree=null;
        CommonTree RBRACE447_tree=null;
        CommonTree AS448_tree=null;
        RewriteRuleTokenStream stream_AS=new RewriteRuleTokenStream(adaptor,"token AS");
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_STRING_LITERAL=new RewriteRuleTokenStream(adaptor,"token STRING_LITERAL");
        RewriteRuleTokenStream stream_RBRACE=new RewriteRuleTokenStream(adaptor,"token RBRACE");
        RewriteRuleTokenStream stream_NIL=new RewriteRuleTokenStream(adaptor,"token NIL");
        RewriteRuleTokenStream stream_CHAR_LITERAL=new RewriteRuleTokenStream(adaptor,"token CHAR_LITERAL");
        RewriteRuleTokenStream stream_PERIOD=new RewriteRuleTokenStream(adaptor,"token PERIOD");
        RewriteRuleTokenStream stream_CARET=new RewriteRuleTokenStream(adaptor,"token CARET");
        RewriteRuleTokenStream stream_LBRACE=new RewriteRuleTokenStream(adaptor,"token LBRACE");
        RewriteRuleTokenStream stream_NUMBER=new RewriteRuleTokenStream(adaptor,"token NUMBER");
        RewriteRuleTokenStream stream_PLUS=new RewriteRuleTokenStream(adaptor,"token PLUS");
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
        RewriteRuleTokenStream stream_FALSE=new RewriteRuleTokenStream(adaptor,"token FALSE");
        RewriteRuleTokenStream stream_TRUE=new RewriteRuleTokenStream(adaptor,"token TRUE");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleSubtreeStream stream_arglist=new RewriteRuleSubtreeStream(adaptor,"rule arglist");
        RewriteRuleSubtreeStream stream_arrayAccess=new RewriteRuleSubtreeStream(adaptor,"rule arrayAccess");
        RewriteRuleSubtreeStream stream_idExpr=new RewriteRuleSubtreeStream(adaptor,"rule idExpr");
        RewriteRuleSubtreeStream stream_tuple=new RewriteRuleSubtreeStream(adaptor,"rule tuple");
        RewriteRuleSubtreeStream stream_varDecl=new RewriteRuleSubtreeStream(adaptor,"rule varDecl");
        RewriteRuleSubtreeStream stream_assignExpr=new RewriteRuleSubtreeStream(adaptor,"rule assignExpr");
        RewriteRuleSubtreeStream stream_code=new RewriteRuleSubtreeStream(adaptor,"rule code");
        RewriteRuleSubtreeStream stream_type=new RewriteRuleSubtreeStream(adaptor,"rule type");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:574:6: ( ( NUMBER -> ^( LIT NUMBER ) | FALSE -> ^( LIT FALSE ) | TRUE -> ^( LIT TRUE ) | CHAR_LITERAL -> ^( LIT CHAR_LITERAL ) | STRING_LITERAL -> ^( LIT STRING_LITERAL ) | NIL -> ^( LIT NIL ) | idExpr -> idExpr | ( tuple )=> tuple -> tuple | ( LPAREN varDecl )=> LPAREN a0= varDecl RPAREN -> $a0 | LPAREN a1= assignExpr RPAREN -> $a1 | code -> code ) ( ( PERIOD ID -> ^( FIELDREF $atom ID ) ) | ( LPAREN arglist RPAREN -> ^( CALL $atom arglist ) ) | ( ( LBRACKET )=> arrayAccess -> ^( INDEX $atom arrayAccess ) ) | ( CARET -> ^( DEREF $atom) ) | ( LBRACE ( PLUS )? type RBRACE -> ^( CAST ( PLUS )? type $atom) ) )* ( AS type -> ^( CAST type $atom) )? )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:575:3: ( NUMBER -> ^( LIT NUMBER ) | FALSE -> ^( LIT FALSE ) | TRUE -> ^( LIT TRUE ) | CHAR_LITERAL -> ^( LIT CHAR_LITERAL ) | STRING_LITERAL -> ^( LIT STRING_LITERAL ) | NIL -> ^( LIT NIL ) | idExpr -> idExpr | ( tuple )=> tuple -> tuple | ( LPAREN varDecl )=> LPAREN a0= varDecl RPAREN -> $a0 | LPAREN a1= assignExpr RPAREN -> $a1 | code -> code ) ( ( PERIOD ID -> ^( FIELDREF $atom ID ) ) | ( LPAREN arglist RPAREN -> ^( CALL $atom arglist ) ) | ( ( LBRACKET )=> arrayAccess -> ^( INDEX $atom arrayAccess ) ) | ( CARET -> ^( DEREF $atom) ) | ( LBRACE ( PLUS )? type RBRACE -> ^( CAST ( PLUS )? type $atom) ) )* ( AS type -> ^( CAST type $atom) )?
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:575:3: ( NUMBER -> ^( LIT NUMBER ) | FALSE -> ^( LIT FALSE ) | TRUE -> ^( LIT TRUE ) | CHAR_LITERAL -> ^( LIT CHAR_LITERAL ) | STRING_LITERAL -> ^( LIT STRING_LITERAL ) | NIL -> ^( LIT NIL ) | idExpr -> idExpr | ( tuple )=> tuple -> tuple | ( LPAREN varDecl )=> LPAREN a0= varDecl RPAREN -> $a0 | LPAREN a1= assignExpr RPAREN -> $a1 | code -> code )
            int alt125=11;
            alt125 = dfa125.predict(input);
            switch (alt125) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:576:7: NUMBER
                    {
                    NUMBER424=(Token)match(input,NUMBER,FOLLOW_NUMBER_in_atom7038); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_NUMBER.add(NUMBER424);



                    // AST REWRITE
                    // elements: NUMBER
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 576:39: -> ^( LIT NUMBER )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:576:42: ^( LIT NUMBER )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(LIT, "LIT"), root_1);

                        adaptor.addChild(root_1, stream_NUMBER.nextNode());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:577:9: FALSE
                    {
                    FALSE425=(Token)match(input,FALSE,FOLLOW_FALSE_in_atom7081); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_FALSE.add(FALSE425);



                    // AST REWRITE
                    // elements: FALSE
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 577:39: -> ^( LIT FALSE )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:577:42: ^( LIT FALSE )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(LIT, "LIT"), root_1);

                        adaptor.addChild(root_1, stream_FALSE.nextNode());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 3 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:578:9: TRUE
                    {
                    TRUE426=(Token)match(input,TRUE,FOLLOW_TRUE_in_atom7123); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_TRUE.add(TRUE426);



                    // AST REWRITE
                    // elements: TRUE
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 578:39: -> ^( LIT TRUE )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:578:42: ^( LIT TRUE )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(LIT, "LIT"), root_1);

                        adaptor.addChild(root_1, stream_TRUE.nextNode());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 4 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:579:9: CHAR_LITERAL
                    {
                    CHAR_LITERAL427=(Token)match(input,CHAR_LITERAL,FOLLOW_CHAR_LITERAL_in_atom7166); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_CHAR_LITERAL.add(CHAR_LITERAL427);



                    // AST REWRITE
                    // elements: CHAR_LITERAL
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 579:39: -> ^( LIT CHAR_LITERAL )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:579:42: ^( LIT CHAR_LITERAL )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(LIT, "LIT"), root_1);

                        adaptor.addChild(root_1, stream_CHAR_LITERAL.nextNode());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 5 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:580:9: STRING_LITERAL
                    {
                    STRING_LITERAL428=(Token)match(input,STRING_LITERAL,FOLLOW_STRING_LITERAL_in_atom7201); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_STRING_LITERAL.add(STRING_LITERAL428);



                    // AST REWRITE
                    // elements: STRING_LITERAL
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 580:39: -> ^( LIT STRING_LITERAL )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:580:42: ^( LIT STRING_LITERAL )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(LIT, "LIT"), root_1);

                        adaptor.addChild(root_1, stream_STRING_LITERAL.nextNode());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 6 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:581:9: NIL
                    {
                    NIL429=(Token)match(input,NIL,FOLLOW_NIL_in_atom7234); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_NIL.add(NIL429);



                    // AST REWRITE
                    // elements: NIL
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 581:38: -> ^( LIT NIL )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:581:41: ^( LIT NIL )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(LIT, "LIT"), root_1);

                        adaptor.addChild(root_1, stream_NIL.nextNode());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 7 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:582:9: idExpr
                    {
                    pushFollow(FOLLOW_idExpr_in_atom7277);
                    idExpr430=idExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_idExpr.add(idExpr430.getTree());


                    // AST REWRITE
                    // elements: idExpr
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 582:41: -> idExpr
                    {
                        adaptor.addChild(root_0, stream_idExpr.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 8 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:583:9: ( tuple )=> tuple
                    {
                    pushFollow(FOLLOW_tuple_in_atom7324);
                    tuple431=tuple();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_tuple.add(tuple431.getTree());


                    // AST REWRITE
                    // elements: tuple
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 583:53: -> tuple
                    {
                        adaptor.addChild(root_0, stream_tuple.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 9 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:584:9: ( LPAREN varDecl )=> LPAREN a0= varDecl RPAREN
                    {
                    LPAREN432=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_atom7373); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN432);

                    pushFollow(FOLLOW_varDecl_in_atom7377);
                    a0=varDecl();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_varDecl.add(a0.getTree());
                    RPAREN433=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_atom7379); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN433);



                    // AST REWRITE
                    // elements: a0
                    // token labels: 
                    // rule labels: retval, a0
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                    RewriteRuleSubtreeStream stream_a0=new RewriteRuleSubtreeStream(adaptor,"rule a0",a0!=null?a0.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 584:70: -> $a0
                    {
                        adaptor.addChild(root_0, stream_a0.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 10 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:585:9: LPAREN a1= assignExpr RPAREN
                    {
                    LPAREN434=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_atom7408); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN434);

                    pushFollow(FOLLOW_assignExpr_in_atom7412);
                    a1=assignExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_assignExpr.add(a1.getTree());
                    RPAREN435=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_atom7414); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN435);



                    // AST REWRITE
                    // elements: a1
                    // token labels: 
                    // rule labels: retval, a1
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                    RewriteRuleSubtreeStream stream_a1=new RewriteRuleSubtreeStream(adaptor,"rule a1",a1!=null?a1.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 585:51: -> $a1
                    {
                        adaptor.addChild(root_0, stream_a1.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 11 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:586:9: code
                    {
                    pushFollow(FOLLOW_code_in_atom7443);
                    code436=code();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_code.add(code436.getTree());


                    // AST REWRITE
                    // elements: code
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 586:40: -> code
                    {
                        adaptor.addChild(root_0, stream_code.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:590:5: ( ( PERIOD ID -> ^( FIELDREF $atom ID ) ) | ( LPAREN arglist RPAREN -> ^( CALL $atom arglist ) ) | ( ( LBRACKET )=> arrayAccess -> ^( INDEX $atom arrayAccess ) ) | ( CARET -> ^( DEREF $atom) ) | ( LBRACE ( PLUS )? type RBRACE -> ^( CAST ( PLUS )? type $atom) ) )*
            loop127:
            do {
                int alt127=6;
                int LA127_0 = input.LA(1);

                if ( (LA127_0==PERIOD) ) {
                    alt127=1;
                }
                else if ( (LA127_0==LPAREN) ) {
                    alt127=2;
                }
                else if ( (LA127_0==LBRACKET) && (synpred27_Eulang())) {
                    alt127=3;
                }
                else if ( (LA127_0==CARET) ) {
                    alt127=4;
                }
                else if ( (LA127_0==LBRACE) ) {
                    alt127=5;
                }


                switch (alt127) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:591:7: ( PERIOD ID -> ^( FIELDREF $atom ID ) )
            	    {
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:591:7: ( PERIOD ID -> ^( FIELDREF $atom ID ) )
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:591:9: PERIOD ID
            	    {
            	    PERIOD437=(Token)match(input,PERIOD,FOLLOW_PERIOD_in_atom7502); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_PERIOD.add(PERIOD437);

            	    ID438=(Token)match(input,ID,FOLLOW_ID_in_atom7504); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_ID.add(ID438);



            	    // AST REWRITE
            	    // elements: atom, ID
            	    // token labels: 
            	    // rule labels: retval
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 591:20: -> ^( FIELDREF $atom ID )
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:591:23: ^( FIELDREF $atom ID )
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(FIELDREF, "FIELDREF"), root_1);

            	        adaptor.addChild(root_1, stream_retval.nextTree());
            	        adaptor.addChild(root_1, stream_ID.nextNode());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }


            	    }
            	    break;
            	case 2 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:592:7: ( LPAREN arglist RPAREN -> ^( CALL $atom arglist ) )
            	    {
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:592:7: ( LPAREN arglist RPAREN -> ^( CALL $atom arglist ) )
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:592:10: LPAREN arglist RPAREN
            	    {
            	    LPAREN439=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_atom7529); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN439);

            	    pushFollow(FOLLOW_arglist_in_atom7531);
            	    arglist440=arglist();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_arglist.add(arglist440.getTree());
            	    RPAREN441=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_atom7533); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN441);



            	    // AST REWRITE
            	    // elements: atom, arglist
            	    // token labels: 
            	    // rule labels: retval
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 592:34: -> ^( CALL $atom arglist )
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:592:37: ^( CALL $atom arglist )
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(CALL, "CALL"), root_1);

            	        adaptor.addChild(root_1, stream_retval.nextTree());
            	        adaptor.addChild(root_1, stream_arglist.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }


            	    }
            	    break;
            	case 3 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:593:7: ( ( LBRACKET )=> arrayAccess -> ^( INDEX $atom arrayAccess ) )
            	    {
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:593:7: ( ( LBRACKET )=> arrayAccess -> ^( INDEX $atom arrayAccess ) )
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:593:9: ( LBRACKET )=> arrayAccess
            	    {
            	    pushFollow(FOLLOW_arrayAccess_in_atom7566);
            	    arrayAccess442=arrayAccess();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_arrayAccess.add(arrayAccess442.getTree());


            	    // AST REWRITE
            	    // elements: atom, arrayAccess
            	    // token labels: 
            	    // rule labels: retval
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 593:39: -> ^( INDEX $atom arrayAccess )
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:593:42: ^( INDEX $atom arrayAccess )
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(INDEX, "INDEX"), root_1);

            	        adaptor.addChild(root_1, stream_retval.nextTree());
            	        adaptor.addChild(root_1, stream_arrayAccess.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }


            	    }
            	    break;
            	case 4 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:594:7: ( CARET -> ^( DEREF $atom) )
            	    {
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:594:7: ( CARET -> ^( DEREF $atom) )
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:594:9: CARET
            	    {
            	    CARET443=(Token)match(input,CARET,FOLLOW_CARET_in_atom7591); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_CARET.add(CARET443);



            	    // AST REWRITE
            	    // elements: atom
            	    // token labels: 
            	    // rule labels: retval
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 594:15: -> ^( DEREF $atom)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:594:18: ^( DEREF $atom)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(DEREF, "DEREF"), root_1);

            	        adaptor.addChild(root_1, stream_retval.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }


            	    }
            	    break;
            	case 5 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:595:7: ( LBRACE ( PLUS )? type RBRACE -> ^( CAST ( PLUS )? type $atom) )
            	    {
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:595:7: ( LBRACE ( PLUS )? type RBRACE -> ^( CAST ( PLUS )? type $atom) )
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:595:9: LBRACE ( PLUS )? type RBRACE
            	    {
            	    LBRACE444=(Token)match(input,LBRACE,FOLLOW_LBRACE_in_atom7612); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_LBRACE.add(LBRACE444);

            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:595:16: ( PLUS )?
            	    int alt126=2;
            	    int LA126_0 = input.LA(1);

            	    if ( (LA126_0==PLUS) ) {
            	        alt126=1;
            	    }
            	    switch (alt126) {
            	        case 1 :
            	            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:595:16: PLUS
            	            {
            	            PLUS445=(Token)match(input,PLUS,FOLLOW_PLUS_in_atom7614); if (state.failed) return retval; 
            	            if ( state.backtracking==0 ) stream_PLUS.add(PLUS445);


            	            }
            	            break;

            	    }

            	    pushFollow(FOLLOW_type_in_atom7617);
            	    type446=type();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_type.add(type446.getTree());
            	    RBRACE447=(Token)match(input,RBRACE,FOLLOW_RBRACE_in_atom7619); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_RBRACE.add(RBRACE447);



            	    // AST REWRITE
            	    // elements: PLUS, type, atom
            	    // token labels: 
            	    // rule labels: retval
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 595:34: -> ^( CAST ( PLUS )? type $atom)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:595:37: ^( CAST ( PLUS )? type $atom)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(CAST, "CAST"), root_1);

            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:595:44: ( PLUS )?
            	        if ( stream_PLUS.hasNext() ) {
            	            adaptor.addChild(root_1, stream_PLUS.nextNode());

            	        }
            	        stream_PLUS.reset();
            	        adaptor.addChild(root_1, stream_type.nextTree());
            	        adaptor.addChild(root_1, stream_retval.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }


            	    }
            	    break;

            	default :
            	    break loop127;
                }
            } while (true);

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:598:5: ( AS type -> ^( CAST type $atom) )?
            int alt128=2;
            int LA128_0 = input.LA(1);

            if ( (LA128_0==AS) ) {
                alt128=1;
            }
            switch (alt128) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:599:7: AS type
                    {
                    AS448=(Token)match(input,AS,FOLLOW_AS_in_atom7660); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_AS.add(AS448);

                    pushFollow(FOLLOW_type_in_atom7662);
                    type449=type();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_type.add(type449.getTree());


                    // AST REWRITE
                    // elements: type, atom
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 599:15: -> ^( CAST type $atom)
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:599:18: ^( CAST type $atom)
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(CAST, "CAST"), root_1);

                        adaptor.addChild(root_1, stream_type.nextTree());
                        adaptor.addChild(root_1, stream_retval.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "atom"

    public static class arrayAccess_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "arrayAccess"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:603:1: arrayAccess : LBRACKET assignExpr ( COMMA assignExpr )* RBRACKET -> ( assignExpr )+ ;
    public final EulangParser.arrayAccess_return arrayAccess() throws RecognitionException {
        EulangParser.arrayAccess_return retval = new EulangParser.arrayAccess_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LBRACKET450=null;
        Token COMMA452=null;
        Token RBRACKET454=null;
        EulangParser.assignExpr_return assignExpr451 = null;

        EulangParser.assignExpr_return assignExpr453 = null;


        CommonTree LBRACKET450_tree=null;
        CommonTree COMMA452_tree=null;
        CommonTree RBRACKET454_tree=null;
        RewriteRuleTokenStream stream_LBRACKET=new RewriteRuleTokenStream(adaptor,"token LBRACKET");
        RewriteRuleTokenStream stream_RBRACKET=new RewriteRuleTokenStream(adaptor,"token RBRACKET");
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleSubtreeStream stream_assignExpr=new RewriteRuleSubtreeStream(adaptor,"rule assignExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:603:13: ( LBRACKET assignExpr ( COMMA assignExpr )* RBRACKET -> ( assignExpr )+ )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:603:15: LBRACKET assignExpr ( COMMA assignExpr )* RBRACKET
            {
            LBRACKET450=(Token)match(input,LBRACKET,FOLLOW_LBRACKET_in_arrayAccess7696); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LBRACKET.add(LBRACKET450);

            pushFollow(FOLLOW_assignExpr_in_arrayAccess7698);
            assignExpr451=assignExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_assignExpr.add(assignExpr451.getTree());
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:603:35: ( COMMA assignExpr )*
            loop129:
            do {
                int alt129=2;
                int LA129_0 = input.LA(1);

                if ( (LA129_0==COMMA) ) {
                    alt129=1;
                }


                switch (alt129) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:603:36: COMMA assignExpr
            	    {
            	    COMMA452=(Token)match(input,COMMA,FOLLOW_COMMA_in_arrayAccess7701); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA452);

            	    pushFollow(FOLLOW_assignExpr_in_arrayAccess7703);
            	    assignExpr453=assignExpr();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_assignExpr.add(assignExpr453.getTree());

            	    }
            	    break;

            	default :
            	    break loop129;
                }
            } while (true);

            RBRACKET454=(Token)match(input,RBRACKET,FOLLOW_RBRACKET_in_arrayAccess7707); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RBRACKET.add(RBRACKET454);



            // AST REWRITE
            // elements: assignExpr
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 603:65: -> ( assignExpr )+
            {
                if ( !(stream_assignExpr.hasNext()) ) {
                    throw new RewriteEarlyExitException();
                }
                while ( stream_assignExpr.hasNext() ) {
                    adaptor.addChild(root_0, stream_assignExpr.nextTree());

                }
                stream_assignExpr.reset();

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "arrayAccess"

    public static class idExpr_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "idExpr"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:605:1: idExpr : ( idOrScopeRef -> idOrScopeRef ) ( PERIOD ID -> ^( FIELDREF $idExpr ID ) )* ( ( instantiation )=> instantiation -> ^( INSTANCE $idExpr instantiation ) )? ;
    public final EulangParser.idExpr_return idExpr() throws RecognitionException {
        EulangParser.idExpr_return retval = new EulangParser.idExpr_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token PERIOD456=null;
        Token ID457=null;
        EulangParser.idOrScopeRef_return idOrScopeRef455 = null;

        EulangParser.instantiation_return instantiation458 = null;


        CommonTree PERIOD456_tree=null;
        CommonTree ID457_tree=null;
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
        RewriteRuleTokenStream stream_PERIOD=new RewriteRuleTokenStream(adaptor,"token PERIOD");
        RewriteRuleSubtreeStream stream_idOrScopeRef=new RewriteRuleSubtreeStream(adaptor,"rule idOrScopeRef");
        RewriteRuleSubtreeStream stream_instantiation=new RewriteRuleSubtreeStream(adaptor,"rule instantiation");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:605:8: ( ( idOrScopeRef -> idOrScopeRef ) ( PERIOD ID -> ^( FIELDREF $idExpr ID ) )* ( ( instantiation )=> instantiation -> ^( INSTANCE $idExpr instantiation ) )? )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:606:5: ( idOrScopeRef -> idOrScopeRef ) ( PERIOD ID -> ^( FIELDREF $idExpr ID ) )* ( ( instantiation )=> instantiation -> ^( INSTANCE $idExpr instantiation ) )?
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:606:5: ( idOrScopeRef -> idOrScopeRef )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:606:7: idOrScopeRef
            {
            pushFollow(FOLLOW_idOrScopeRef_in_idExpr7729);
            idOrScopeRef455=idOrScopeRef();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_idOrScopeRef.add(idOrScopeRef455.getTree());


            // AST REWRITE
            // elements: idOrScopeRef
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 606:20: -> idOrScopeRef
            {
                adaptor.addChild(root_0, stream_idOrScopeRef.nextTree());

            }

            retval.tree = root_0;}
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:607:7: ( PERIOD ID -> ^( FIELDREF $idExpr ID ) )*
            loop130:
            do {
                int alt130=2;
                int LA130_0 = input.LA(1);

                if ( (LA130_0==PERIOD) ) {
                    int LA130_2 = input.LA(2);

                    if ( (LA130_2==ID) ) {
                        alt130=1;
                    }


                }


                switch (alt130) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:607:9: PERIOD ID
            	    {
            	    PERIOD456=(Token)match(input,PERIOD,FOLLOW_PERIOD_in_idExpr7745); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_PERIOD.add(PERIOD456);

            	    ID457=(Token)match(input,ID,FOLLOW_ID_in_idExpr7747); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_ID.add(ID457);



            	    // AST REWRITE
            	    // elements: idExpr, ID
            	    // token labels: 
            	    // rule labels: retval
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 607:20: -> ^( FIELDREF $idExpr ID )
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:607:23: ^( FIELDREF $idExpr ID )
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(FIELDREF, "FIELDREF"), root_1);

            	        adaptor.addChild(root_1, stream_retval.nextTree());
            	        adaptor.addChild(root_1, stream_ID.nextNode());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }
            	    break;

            	default :
            	    break loop130;
                }
            } while (true);

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:608:5: ( ( instantiation )=> instantiation -> ^( INSTANCE $idExpr instantiation ) )?
            int alt131=2;
            alt131 = dfa131.predict(input);
            switch (alt131) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:608:7: ( instantiation )=> instantiation
                    {
                    pushFollow(FOLLOW_instantiation_in_idExpr7777);
                    instantiation458=instantiation();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_instantiation.add(instantiation458.getTree());


                    // AST REWRITE
                    // elements: idExpr, instantiation
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 608:41: -> ^( INSTANCE $idExpr instantiation )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:608:44: ^( INSTANCE $idExpr instantiation )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(INSTANCE, "INSTANCE"), root_1);

                        adaptor.addChild(root_1, stream_retval.nextTree());
                        adaptor.addChild(root_1, stream_instantiation.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "idExpr"

    public static class namespaceRef_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "namespaceRef"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:610:1: namespaceRef : ( ID ( PERIOD ID )* -> ^( IDREF ( ID )+ ) | c= colons ID ( PERIOD ID )* -> ^( IDREF ( ID )+ ) );
    public final EulangParser.namespaceRef_return namespaceRef() throws RecognitionException {
        EulangParser.namespaceRef_return retval = new EulangParser.namespaceRef_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ID459=null;
        Token PERIOD460=null;
        Token ID461=null;
        Token ID462=null;
        Token PERIOD463=null;
        Token ID464=null;
        EulangParser.colons_return c = null;


        CommonTree ID459_tree=null;
        CommonTree PERIOD460_tree=null;
        CommonTree ID461_tree=null;
        CommonTree ID462_tree=null;
        CommonTree PERIOD463_tree=null;
        CommonTree ID464_tree=null;
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
        RewriteRuleTokenStream stream_PERIOD=new RewriteRuleTokenStream(adaptor,"token PERIOD");
        RewriteRuleSubtreeStream stream_colons=new RewriteRuleSubtreeStream(adaptor,"rule colons");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:610:14: ( ID ( PERIOD ID )* -> ^( IDREF ( ID )+ ) | c= colons ID ( PERIOD ID )* -> ^( IDREF ( ID )+ ) )
            int alt134=2;
            int LA134_0 = input.LA(1);

            if ( (LA134_0==ID) ) {
                alt134=1;
            }
            else if ( (LA134_0==COLON||LA134_0==COLONS) ) {
                alt134=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 134, 0, input);

                throw nvae;
            }
            switch (alt134) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:611:5: ID ( PERIOD ID )*
                    {
                    ID459=(Token)match(input,ID,FOLLOW_ID_in_namespaceRef7808); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ID.add(ID459);

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:611:8: ( PERIOD ID )*
                    loop132:
                    do {
                        int alt132=2;
                        int LA132_0 = input.LA(1);

                        if ( (LA132_0==PERIOD) ) {
                            alt132=1;
                        }


                        switch (alt132) {
                    	case 1 :
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:611:9: PERIOD ID
                    	    {
                    	    PERIOD460=(Token)match(input,PERIOD,FOLLOW_PERIOD_in_namespaceRef7811); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_PERIOD.add(PERIOD460);

                    	    ID461=(Token)match(input,ID,FOLLOW_ID_in_namespaceRef7813); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_ID.add(ID461);


                    	    }
                    	    break;

                    	default :
                    	    break loop132;
                        }
                    } while (true);



                    // AST REWRITE
                    // elements: ID
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 611:22: -> ^( IDREF ( ID )+ )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:611:25: ^( IDREF ( ID )+ )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(IDREF, "IDREF"), root_1);

                        if ( !(stream_ID.hasNext()) ) {
                            throw new RewriteEarlyExitException();
                        }
                        while ( stream_ID.hasNext() ) {
                            adaptor.addChild(root_1, stream_ID.nextNode());

                        }
                        stream_ID.reset();

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:612:7: c= colons ID ( PERIOD ID )*
                    {
                    pushFollow(FOLLOW_colons_in_namespaceRef7837);
                    c=colons();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_colons.add(c.getTree());
                    ID462=(Token)match(input,ID,FOLLOW_ID_in_namespaceRef7839); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ID.add(ID462);

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:612:19: ( PERIOD ID )*
                    loop133:
                    do {
                        int alt133=2;
                        int LA133_0 = input.LA(1);

                        if ( (LA133_0==PERIOD) ) {
                            alt133=1;
                        }


                        switch (alt133) {
                    	case 1 :
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:612:20: PERIOD ID
                    	    {
                    	    PERIOD463=(Token)match(input,PERIOD,FOLLOW_PERIOD_in_namespaceRef7842); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_PERIOD.add(PERIOD463);

                    	    ID464=(Token)match(input,ID,FOLLOW_ID_in_namespaceRef7844); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_ID.add(ID464);


                    	    }
                    	    break;

                    	default :
                    	    break loop133;
                        }
                    } while (true);



                    // AST REWRITE
                    // elements: ID
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 612:33: -> ^( IDREF ( ID )+ )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:612:36: ^( IDREF ( ID )+ )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(IDREF, "IDREF"), root_1);

                        adaptor.addChild(root_1, split((c!=null?((CommonTree)c.tree):null)));
                        if ( !(stream_ID.hasNext()) ) {
                            throw new RewriteEarlyExitException();
                        }
                        while ( stream_ID.hasNext() ) {
                            adaptor.addChild(root_1, stream_ID.nextNode());

                        }
                        stream_ID.reset();

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "namespaceRef"

    public static class instantiation_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "instantiation"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:615:1: instantiation : LESS ( instanceExpr ( COMMA instanceExpr )* )? GREATER -> ^( LIST ( instanceExpr )* ) ;
    public final EulangParser.instantiation_return instantiation() throws RecognitionException {
        EulangParser.instantiation_return retval = new EulangParser.instantiation_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LESS465=null;
        Token COMMA467=null;
        Token GREATER469=null;
        EulangParser.instanceExpr_return instanceExpr466 = null;

        EulangParser.instanceExpr_return instanceExpr468 = null;


        CommonTree LESS465_tree=null;
        CommonTree COMMA467_tree=null;
        CommonTree GREATER469_tree=null;
        RewriteRuleTokenStream stream_GREATER=new RewriteRuleTokenStream(adaptor,"token GREATER");
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleTokenStream stream_LESS=new RewriteRuleTokenStream(adaptor,"token LESS");
        RewriteRuleSubtreeStream stream_instanceExpr=new RewriteRuleSubtreeStream(adaptor,"rule instanceExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:615:15: ( LESS ( instanceExpr ( COMMA instanceExpr )* )? GREATER -> ^( LIST ( instanceExpr )* ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:615:17: LESS ( instanceExpr ( COMMA instanceExpr )* )? GREATER
            {
            LESS465=(Token)match(input,LESS,FOLLOW_LESS_in_instantiation7873); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LESS.add(LESS465);

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:615:22: ( instanceExpr ( COMMA instanceExpr )* )?
            int alt136=2;
            int LA136_0 = input.LA(1);

            if ( (LA136_0==CODE||LA136_0==ID||LA136_0==COLON||LA136_0==LPAREN||LA136_0==NIL||(LA136_0>=NUMBER && LA136_0<=STRING_LITERAL)||(LA136_0>=FALSE && LA136_0<=DATA)) ) {
                alt136=1;
            }
            switch (alt136) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:615:23: instanceExpr ( COMMA instanceExpr )*
                    {
                    pushFollow(FOLLOW_instanceExpr_in_instantiation7876);
                    instanceExpr466=instanceExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_instanceExpr.add(instanceExpr466.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:615:36: ( COMMA instanceExpr )*
                    loop135:
                    do {
                        int alt135=2;
                        int LA135_0 = input.LA(1);

                        if ( (LA135_0==COMMA) ) {
                            alt135=1;
                        }


                        switch (alt135) {
                    	case 1 :
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:615:37: COMMA instanceExpr
                    	    {
                    	    COMMA467=(Token)match(input,COMMA,FOLLOW_COMMA_in_instantiation7879); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA467);

                    	    pushFollow(FOLLOW_instanceExpr_in_instantiation7881);
                    	    instanceExpr468=instanceExpr();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_instanceExpr.add(instanceExpr468.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop135;
                        }
                    } while (true);


                    }
                    break;

            }

            GREATER469=(Token)match(input,GREATER,FOLLOW_GREATER_in_instantiation7887); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_GREATER.add(GREATER469);



            // AST REWRITE
            // elements: instanceExpr
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 615:70: -> ^( LIST ( instanceExpr )* )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:615:73: ^( LIST ( instanceExpr )* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(LIST, "LIST"), root_1);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:615:80: ( instanceExpr )*
                while ( stream_instanceExpr.hasNext() ) {
                    adaptor.addChild(root_1, stream_instanceExpr.nextTree());

                }
                stream_instanceExpr.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "instantiation"

    public static class instanceExpr_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "instanceExpr"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:618:1: instanceExpr options {backtrack=true; } : ( type | atom );
    public final EulangParser.instanceExpr_return instanceExpr() throws RecognitionException {
        EulangParser.instanceExpr_return retval = new EulangParser.instanceExpr_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.type_return type470 = null;

        EulangParser.atom_return atom471 = null;



        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:618:41: ( type | atom )
            int alt137=2;
            alt137 = dfa137.predict(input);
            switch (alt137) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:618:43: type
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_type_in_instanceExpr7919);
                    type470=type();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, type470.getTree());

                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:618:50: atom
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_atom_in_instanceExpr7923);
                    atom471=atom();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, atom471.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "instanceExpr"

    public static class idOrScopeRef_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "idOrScopeRef"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:619:1: idOrScopeRef : ( ID -> ^( IDREF ID ) | c= colons ID -> ^( IDREF ID ) );
    public final EulangParser.idOrScopeRef_return idOrScopeRef() throws RecognitionException {
        EulangParser.idOrScopeRef_return retval = new EulangParser.idOrScopeRef_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ID472=null;
        Token ID473=null;
        EulangParser.colons_return c = null;


        CommonTree ID472_tree=null;
        CommonTree ID473_tree=null;
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
        RewriteRuleSubtreeStream stream_colons=new RewriteRuleSubtreeStream(adaptor,"rule colons");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:619:14: ( ID -> ^( IDREF ID ) | c= colons ID -> ^( IDREF ID ) )
            int alt138=2;
            int LA138_0 = input.LA(1);

            if ( (LA138_0==ID) ) {
                alt138=1;
            }
            else if ( (LA138_0==COLON||LA138_0==COLONS) ) {
                alt138=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 138, 0, input);

                throw nvae;
            }
            switch (alt138) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:619:16: ID
                    {
                    ID472=(Token)match(input,ID,FOLLOW_ID_in_idOrScopeRef7931); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ID.add(ID472);



                    // AST REWRITE
                    // elements: ID
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 619:20: -> ^( IDREF ID )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:619:23: ^( IDREF ID )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(IDREF, "IDREF"), root_1);

                        adaptor.addChild(root_1, stream_ID.nextNode());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:620:9: c= colons ID
                    {
                    pushFollow(FOLLOW_colons_in_idOrScopeRef7954);
                    c=colons();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_colons.add(c.getTree());
                    ID473=(Token)match(input,ID,FOLLOW_ID_in_idOrScopeRef7956); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ID.add(ID473);



                    // AST REWRITE
                    // elements: ID
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 620:21: -> ^( IDREF ID )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:620:24: ^( IDREF ID )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(IDREF, "IDREF"), root_1);

                        adaptor.addChild(root_1, split((c!=null?((CommonTree)c.tree):null)));
                        adaptor.addChild(root_1, stream_ID.nextNode());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "idOrScopeRef"

    public static class colons_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "colons"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:623:1: colons : ( COLON | COLONS )+ ;
    public final EulangParser.colons_return colons() throws RecognitionException {
        EulangParser.colons_return retval = new EulangParser.colons_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token set474=null;

        CommonTree set474_tree=null;

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:623:8: ( ( COLON | COLONS )+ )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:623:10: ( COLON | COLONS )+
            {
            root_0 = (CommonTree)adaptor.nil();

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:623:10: ( COLON | COLONS )+
            int cnt139=0;
            loop139:
            do {
                int alt139=2;
                int LA139_0 = input.LA(1);

                if ( (LA139_0==COLON||LA139_0==COLONS) ) {
                    alt139=1;
                }


                switch (alt139) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:
            	    {
            	    set474=(Token)input.LT(1);
            	    if ( input.LA(1)==COLON||input.LA(1)==COLONS ) {
            	        input.consume();
            	        if ( state.backtracking==0 ) adaptor.addChild(root_0, (CommonTree)adaptor.create(set474));
            	        state.errorRecovery=false;state.failed=false;
            	    }
            	    else {
            	        if (state.backtracking>0) {state.failed=true; return retval;}
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        throw mse;
            	    }


            	    }
            	    break;

            	default :
            	    if ( cnt139 >= 1 ) break loop139;
            	    if (state.backtracking>0) {state.failed=true; return retval;}
                        EarlyExitException eee =
                            new EarlyExitException(139, input);
                        throw eee;
                }
                cnt139++;
            } while (true);


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "colons"

    public static class data_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "data"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:625:1: data : DATA LBRACE ( fieldDecl )* RBRACE -> ^( DATA ( fieldDecl )* ) ;
    public final EulangParser.data_return data() throws RecognitionException {
        EulangParser.data_return retval = new EulangParser.data_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token DATA475=null;
        Token LBRACE476=null;
        Token RBRACE478=null;
        EulangParser.fieldDecl_return fieldDecl477 = null;


        CommonTree DATA475_tree=null;
        CommonTree LBRACE476_tree=null;
        CommonTree RBRACE478_tree=null;
        RewriteRuleTokenStream stream_RBRACE=new RewriteRuleTokenStream(adaptor,"token RBRACE");
        RewriteRuleTokenStream stream_DATA=new RewriteRuleTokenStream(adaptor,"token DATA");
        RewriteRuleTokenStream stream_LBRACE=new RewriteRuleTokenStream(adaptor,"token LBRACE");
        RewriteRuleSubtreeStream stream_fieldDecl=new RewriteRuleSubtreeStream(adaptor,"rule fieldDecl");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:625:6: ( DATA LBRACE ( fieldDecl )* RBRACE -> ^( DATA ( fieldDecl )* ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:625:8: DATA LBRACE ( fieldDecl )* RBRACE
            {
            DATA475=(Token)match(input,DATA,FOLLOW_DATA_in_data7999); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_DATA.add(DATA475);

            LBRACE476=(Token)match(input,LBRACE,FOLLOW_LBRACE_in_data8001); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LBRACE.add(LBRACE476);

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:625:20: ( fieldDecl )*
            loop140:
            do {
                int alt140=2;
                int LA140_0 = input.LA(1);

                if ( ((LA140_0>=FORWARD && LA140_0<=ID)||LA140_0==LPAREN) ) {
                    alt140=1;
                }


                switch (alt140) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:625:20: fieldDecl
            	    {
            	    pushFollow(FOLLOW_fieldDecl_in_data8003);
            	    fieldDecl477=fieldDecl();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_fieldDecl.add(fieldDecl477.getTree());

            	    }
            	    break;

            	default :
            	    break loop140;
                }
            } while (true);

            RBRACE478=(Token)match(input,RBRACE,FOLLOW_RBRACE_in_data8006); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RBRACE.add(RBRACE478);



            // AST REWRITE
            // elements: DATA, fieldDecl
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 625:39: -> ^( DATA ( fieldDecl )* )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:625:42: ^( DATA ( fieldDecl )* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot(stream_DATA.nextNode(), root_1);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:625:49: ( fieldDecl )*
                while ( stream_fieldDecl.hasNext() ) {
                    adaptor.addChild(root_1, stream_fieldDecl.nextTree());

                }
                stream_fieldDecl.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "data"

    public static class fieldDecl_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "fieldDecl"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:627:1: fieldDecl : ( varDecl SEMI -> varDecl | defineStmt | FORWARD ID ( COMMA ID )* SEMI -> ( ^( FORWARD ID ) )+ );
    public final EulangParser.fieldDecl_return fieldDecl() throws RecognitionException {
        EulangParser.fieldDecl_return retval = new EulangParser.fieldDecl_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token SEMI480=null;
        Token FORWARD482=null;
        Token ID483=null;
        Token COMMA484=null;
        Token ID485=null;
        Token SEMI486=null;
        EulangParser.varDecl_return varDecl479 = null;

        EulangParser.defineStmt_return defineStmt481 = null;


        CommonTree SEMI480_tree=null;
        CommonTree FORWARD482_tree=null;
        CommonTree ID483_tree=null;
        CommonTree COMMA484_tree=null;
        CommonTree ID485_tree=null;
        CommonTree SEMI486_tree=null;
        RewriteRuleTokenStream stream_FORWARD=new RewriteRuleTokenStream(adaptor,"token FORWARD");
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleTokenStream stream_SEMI=new RewriteRuleTokenStream(adaptor,"token SEMI");
        RewriteRuleSubtreeStream stream_varDecl=new RewriteRuleSubtreeStream(adaptor,"rule varDecl");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:627:11: ( varDecl SEMI -> varDecl | defineStmt | FORWARD ID ( COMMA ID )* SEMI -> ( ^( FORWARD ID ) )+ )
            int alt142=3;
            switch ( input.LA(1) ) {
            case ID:
                {
                int LA142_1 = input.LA(2);

                if ( (LA142_1==EQUALS||LA142_1==EQUALS_COLON) ) {
                    alt142=2;
                }
                else if ( ((LA142_1>=COMMA && LA142_1<=COLON)||LA142_1==ATTR) ) {
                    alt142=1;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 142, 1, input);

                    throw nvae;
                }
                }
                break;
            case LPAREN:
                {
                alt142=1;
                }
                break;
            case FORWARD:
                {
                alt142=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 142, 0, input);

                throw nvae;
            }

            switch (alt142) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:627:13: varDecl SEMI
                    {
                    pushFollow(FOLLOW_varDecl_in_fieldDecl8025);
                    varDecl479=varDecl();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_varDecl.add(varDecl479.getTree());
                    SEMI480=(Token)match(input,SEMI,FOLLOW_SEMI_in_fieldDecl8027); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_SEMI.add(SEMI480);



                    // AST REWRITE
                    // elements: varDecl
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 627:26: -> varDecl
                    {
                        adaptor.addChild(root_0, stream_varDecl.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:628:7: defineStmt
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_defineStmt_in_fieldDecl8040);
                    defineStmt481=defineStmt();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, defineStmt481.getTree());

                    }
                    break;
                case 3 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:629:7: FORWARD ID ( COMMA ID )* SEMI
                    {
                    FORWARD482=(Token)match(input,FORWARD,FOLLOW_FORWARD_in_fieldDecl8048); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_FORWARD.add(FORWARD482);

                    ID483=(Token)match(input,ID,FOLLOW_ID_in_fieldDecl8050); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ID.add(ID483);

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:629:18: ( COMMA ID )*
                    loop141:
                    do {
                        int alt141=2;
                        int LA141_0 = input.LA(1);

                        if ( (LA141_0==COMMA) ) {
                            alt141=1;
                        }


                        switch (alt141) {
                    	case 1 :
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:629:19: COMMA ID
                    	    {
                    	    COMMA484=(Token)match(input,COMMA,FOLLOW_COMMA_in_fieldDecl8053); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA484);

                    	    ID485=(Token)match(input,ID,FOLLOW_ID_in_fieldDecl8055); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_ID.add(ID485);


                    	    }
                    	    break;

                    	default :
                    	    break loop141;
                        }
                    } while (true);

                    SEMI486=(Token)match(input,SEMI,FOLLOW_SEMI_in_fieldDecl8059); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_SEMI.add(SEMI486);



                    // AST REWRITE
                    // elements: ID, FORWARD
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 629:35: -> ( ^( FORWARD ID ) )+
                    {
                        if ( !(stream_ID.hasNext()||stream_FORWARD.hasNext()) ) {
                            throw new RewriteEarlyExitException();
                        }
                        while ( stream_ID.hasNext()||stream_FORWARD.hasNext() ) {
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:629:38: ^( FORWARD ID )
                            {
                            CommonTree root_1 = (CommonTree)adaptor.nil();
                            root_1 = (CommonTree)adaptor.becomeRoot(stream_FORWARD.nextNode(), root_1);

                            adaptor.addChild(root_1, stream_ID.nextNode());

                            adaptor.addChild(root_0, root_1);
                            }

                        }
                        stream_ID.reset();
                        stream_FORWARD.reset();

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "fieldDecl"

    // $ANTLR start synpred1_Eulang
    public final void synpred1_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:144:7: ( LBRACE )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:144:8: LBRACE
        {
        match(input,LBRACE,FOLLOW_LBRACE_in_synpred1_Eulang646); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred1_Eulang

    // $ANTLR start synpred2_Eulang
    public final void synpred2_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:174:14: ( ID EQUALS LBRACKET )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:174:15: ID EQUALS LBRACKET
        {
        match(input,ID,FOLLOW_ID_in_synpred2_Eulang1127); if (state.failed) return ;
        match(input,EQUALS,FOLLOW_EQUALS_in_synpred2_Eulang1129); if (state.failed) return ;
        match(input,LBRACKET,FOLLOW_LBRACKET_in_synpred2_Eulang1131); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred2_Eulang

    // $ANTLR start synpred3_Eulang
    public final void synpred3_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:175:7: ( ID EQUALS_COLON )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:175:8: ID EQUALS_COLON
        {
        match(input,ID,FOLLOW_ID_in_synpred3_Eulang1176); if (state.failed) return ;
        match(input,EQUALS_COLON,FOLLOW_EQUALS_COLON_in_synpred3_Eulang1178); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred3_Eulang

    // $ANTLR start synpred4_Eulang
    public final void synpred4_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:176:7: ( ID EQUALS )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:176:8: ID EQUALS
        {
        match(input,ID,FOLLOW_ID_in_synpred4_Eulang1213); if (state.failed) return ;
        match(input,EQUALS,FOLLOW_EQUALS_in_synpred4_Eulang1215); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred4_Eulang

    // $ANTLR start synpred5_Eulang
    public final void synpred5_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:179:17: ( LBRACE )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:179:18: LBRACE
        {
        match(input,LBRACE,FOLLOW_LBRACE_in_synpred5_Eulang1253); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred5_Eulang

    // $ANTLR start synpred7_Eulang
    public final void synpred7_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:236:5: ( argdefsWithTypes )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:236:5: argdefsWithTypes
        {
        pushFollow(FOLLOW_argdefsWithTypes_in_synpred7_Eulang1791);
        argdefsWithTypes();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred7_Eulang

    // $ANTLR start synpred8_Eulang
    public final void synpred8_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:237:5: ( argdefWithType )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:237:5: argdefWithType
        {
        pushFollow(FOLLOW_argdefWithType_in_synpred8_Eulang1798);
        argdefWithType();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred8_Eulang

    // $ANTLR start synpred9_Eulang
    public final void synpred9_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:280:9: ( ( arraySuff )+ )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:280:10: ( arraySuff )+
        {
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:280:10: ( arraySuff )+
        int cnt143=0;
        loop143:
        do {
            int alt143=2;
            int LA143_0 = input.LA(1);

            if ( (LA143_0==LBRACKET) ) {
                alt143=1;
            }


            switch (alt143) {
        	case 1 :
        	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:280:10: arraySuff
        	    {
        	    pushFollow(FOLLOW_arraySuff_in_synpred9_Eulang2266);
        	    arraySuff();

        	    state._fsp--;
        	    if (state.failed) return ;

        	    }
        	    break;

        	default :
        	    if ( cnt143 >= 1 ) break loop143;
        	    if (state.backtracking>0) {state.failed=true; return ;}
                    EarlyExitException eee =
                        new EarlyExitException(143, input);
                    throw eee;
            }
            cnt143++;
        } while (true);


        }
    }
    // $ANTLR end synpred9_Eulang

    // $ANTLR start synpred10_Eulang
    public final void synpred10_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:311:7: ( varDecl )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:311:9: varDecl
        {
        pushFollow(FOLLOW_varDecl_in_synpred10_Eulang2659);
        varDecl();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred10_Eulang

    // $ANTLR start synpred11_Eulang
    public final void synpred11_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:312:9: ( assignStmt )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:312:10: assignStmt
        {
        pushFollow(FOLLOW_assignStmt_in_synpred11_Eulang2682);
        assignStmt();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred11_Eulang

    // $ANTLR start synpred12_Eulang
    public final void synpred12_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:314:9: ( LBRACE )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:314:11: LBRACE
        {
        match(input,LBRACE,FOLLOW_LBRACE_in_synpred12_Eulang2731); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred12_Eulang

    // $ANTLR start synpred13_Eulang
    public final void synpred13_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:342:14: ( lhs assignEqOp )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:342:15: lhs assignEqOp
        {
        pushFollow(FOLLOW_lhs_in_synpred13_Eulang3231);
        lhs();

        state._fsp--;
        if (state.failed) return ;
        pushFollow(FOLLOW_assignEqOp_in_synpred13_Eulang3233);
        assignEqOp();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred13_Eulang

    // $ANTLR start synpred14_Eulang
    public final void synpred14_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:345:7: ( lhs ( COMMA lhs )+ assignEqOp )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:345:8: lhs ( COMMA lhs )+ assignEqOp
        {
        pushFollow(FOLLOW_lhs_in_synpred14_Eulang3313);
        lhs();

        state._fsp--;
        if (state.failed) return ;
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:345:12: ( COMMA lhs )+
        int cnt144=0;
        loop144:
        do {
            int alt144=2;
            int LA144_0 = input.LA(1);

            if ( (LA144_0==COMMA) ) {
                alt144=1;
            }


            switch (alt144) {
        	case 1 :
        	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:345:13: COMMA lhs
        	    {
        	    match(input,COMMA,FOLLOW_COMMA_in_synpred14_Eulang3316); if (state.failed) return ;
        	    pushFollow(FOLLOW_lhs_in_synpred14_Eulang3318);
        	    lhs();

        	    state._fsp--;
        	    if (state.failed) return ;

        	    }
        	    break;

        	default :
        	    if ( cnt144 >= 1 ) break loop144;
        	    if (state.backtracking>0) {state.failed=true; return ;}
                    EarlyExitException eee =
                        new EarlyExitException(144, input);
                    throw eee;
            }
            cnt144++;
        } while (true);

        pushFollow(FOLLOW_assignEqOp_in_synpred14_Eulang3322);
        assignEqOp();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred14_Eulang

    // $ANTLR start synpred15_Eulang
    public final void synpred15_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:352:14: ( lhs assignEqOp )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:352:15: lhs assignEqOp
        {
        pushFollow(FOLLOW_lhs_in_synpred15_Eulang3423);
        lhs();

        state._fsp--;
        if (state.failed) return ;
        pushFollow(FOLLOW_assignEqOp_in_synpred15_Eulang3425);
        assignEqOp();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred15_Eulang

    // $ANTLR start synpred16_Eulang
    public final void synpred16_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:353:7: ( idTuple EQUALS )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:353:8: idTuple EQUALS
        {
        pushFollow(FOLLOW_idTuple_in_synpred16_Eulang3462);
        idTuple();

        state._fsp--;
        if (state.failed) return ;
        match(input,EQUALS,FOLLOW_EQUALS_in_synpred16_Eulang3464); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred16_Eulang

    // $ANTLR start synpred17_Eulang
    public final void synpred17_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:363:7: ( rhsExpr )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:363:8: rhsExpr
        {
        pushFollow(FOLLOW_rhsExpr_in_synpred17_Eulang3677);
        rhsExpr();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred17_Eulang

    // $ANTLR start synpred18_Eulang
    public final void synpred18_Eulang_fragment() throws RecognitionException {   
        EulangParser.rhsExpr_return i = null;


        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:365:7: ( LBRACKET i= rhsExpr RBRACKET )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:365:8: LBRACKET i= rhsExpr RBRACKET
        {
        match(input,LBRACKET,FOLLOW_LBRACKET_in_synpred18_Eulang3809); if (state.failed) return ;
        pushFollow(FOLLOW_rhsExpr_in_synpred18_Eulang3813);
        i=rhsExpr();

        state._fsp--;
        if (state.failed) return ;
        match(input,RBRACKET,FOLLOW_RBRACKET_in_synpred18_Eulang3815); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred18_Eulang

    // $ANTLR start synpred19_Eulang
    public final void synpred19_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:525:12: ( MINUS term )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:525:14: MINUS term
        {
        match(input,MINUS,FOLLOW_MINUS_in_synpred19_Eulang5902); if (state.failed) return ;
        pushFollow(FOLLOW_term_in_synpred19_Eulang5904);
        term();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred19_Eulang

    // $ANTLR start synpred20_Eulang
    public final void synpred20_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:530:11: ( STAR unary )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:530:13: STAR unary
        {
        match(input,STAR,FOLLOW_STAR_in_synpred20_Eulang5995); if (state.failed) return ;
        pushFollow(FOLLOW_unary_in_synpred20_Eulang5997);
        unary();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred20_Eulang

    // $ANTLR start synpred21_Eulang
    public final void synpred21_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:542:9: ( lhs PLUSPLUS )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:542:11: lhs PLUSPLUS
        {
        pushFollow(FOLLOW_lhs_in_synpred21_Eulang6329);
        lhs();

        state._fsp--;
        if (state.failed) return ;
        match(input,PLUSPLUS,FOLLOW_PLUSPLUS_in_synpred21_Eulang6331); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred21_Eulang

    // $ANTLR start synpred22_Eulang
    public final void synpred22_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:543:9: ( lhs MINUSMINUS )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:543:11: lhs MINUSMINUS
        {
        pushFollow(FOLLOW_lhs_in_synpred22_Eulang6362);
        lhs();

        state._fsp--;
        if (state.failed) return ;
        match(input,MINUSMINUS,FOLLOW_MINUSMINUS_in_synpred22_Eulang6364); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred22_Eulang

    // $ANTLR start synpred23_Eulang
    public final void synpred23_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:555:9: ( tuple )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:555:11: tuple
        {
        pushFollow(FOLLOW_tuple_in_synpred23_Eulang6621);
        tuple();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred23_Eulang

    // $ANTLR start synpred24_Eulang
    public final void synpred24_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:564:9: ( LBRACKET )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:564:11: LBRACKET
        {
        match(input,LBRACKET,FOLLOW_LBRACKET_in_synpred24_Eulang6884); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred24_Eulang

    // $ANTLR start synpred25_Eulang
    public final void synpred25_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:583:9: ( tuple )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:583:11: tuple
        {
        pushFollow(FOLLOW_tuple_in_synpred25_Eulang7318);
        tuple();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred25_Eulang

    // $ANTLR start synpred26_Eulang
    public final void synpred26_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:584:9: ( LPAREN varDecl )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:584:11: LPAREN varDecl
        {
        match(input,LPAREN,FOLLOW_LPAREN_in_synpred26_Eulang7365); if (state.failed) return ;
        pushFollow(FOLLOW_varDecl_in_synpred26_Eulang7367);
        varDecl();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred26_Eulang

    // $ANTLR start synpred27_Eulang
    public final void synpred27_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:593:9: ( LBRACKET )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:593:11: LBRACKET
        {
        match(input,LBRACKET,FOLLOW_LBRACKET_in_synpred27_Eulang7560); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred27_Eulang

    // $ANTLR start synpred28_Eulang
    public final void synpred28_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:608:7: ( instantiation )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:608:8: instantiation
        {
        pushFollow(FOLLOW_instantiation_in_synpred28_Eulang7771);
        instantiation();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred28_Eulang

    // $ANTLR start synpred29_Eulang
    public final void synpred29_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:618:43: ( type )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:618:43: type
        {
        pushFollow(FOLLOW_type_in_synpred29_Eulang7919);
        type();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred29_Eulang

    // Delegated rules

    public final boolean synpred18_Eulang() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred18_Eulang_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred17_Eulang() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred17_Eulang_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred5_Eulang() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred5_Eulang_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred19_Eulang() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred19_Eulang_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred3_Eulang() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred3_Eulang_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred25_Eulang() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred25_Eulang_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred13_Eulang() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred13_Eulang_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred4_Eulang() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred4_Eulang_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred11_Eulang() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred11_Eulang_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred12_Eulang() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred12_Eulang_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred21_Eulang() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred21_Eulang_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred28_Eulang() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred28_Eulang_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred14_Eulang() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred14_Eulang_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred7_Eulang() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred7_Eulang_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred8_Eulang() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred8_Eulang_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred22_Eulang() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred22_Eulang_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred15_Eulang() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred15_Eulang_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred10_Eulang() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred10_Eulang_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred23_Eulang() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred23_Eulang_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred9_Eulang() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred9_Eulang_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred24_Eulang() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred24_Eulang_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred2_Eulang() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred2_Eulang_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred1_Eulang() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred1_Eulang_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred16_Eulang() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred16_Eulang_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred29_Eulang() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred29_Eulang_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred20_Eulang() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred20_Eulang_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred26_Eulang() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred26_Eulang_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred27_Eulang() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred27_Eulang_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }


    protected DFA3 dfa3 = new DFA3(this);
    protected DFA7 dfa7 = new DFA7(this);
    protected DFA22 dfa22 = new DFA22(this);
    protected DFA23 dfa23 = new DFA23(this);
    protected DFA52 dfa52 = new DFA52(this);
    protected DFA60 dfa60 = new DFA60(this);
    protected DFA73 dfa73 = new DFA73(this);
    protected DFA80 dfa80 = new DFA80(this);
    protected DFA82 dfa82 = new DFA82(this);
    protected DFA86 dfa86 = new DFA86(this);
    protected DFA119 dfa119 = new DFA119(this);
    protected DFA125 dfa125 = new DFA125(this);
    protected DFA131 dfa131 = new DFA131(this);
    protected DFA137 dfa137 = new DFA137(this);
    static final String DFA3_eotS =
        "\17\uffff";
    static final String DFA3_eofS =
        "\17\uffff";
    static final String DFA3_minS =
        "\1\11\1\44\1\uffff\1\11\1\uffff\1\44\1\105\1\11\2\44\1\105\1\11"+
        "\3\44";
    static final String DFA3_maxS =
        "\1\u0098\1\u0095\1\uffff\1\u0098\1\uffff\1\u0095\2\u0098\2\u0095"+
        "\2\u0098\3\u0095";
    static final String DFA3_acceptS =
        "\2\uffff\1\1\1\uffff\1\2\12\uffff";
    static final String DFA3_specialS =
        "\17\uffff}>";
    static final String[] DFA3_transitionS = {
            "\1\2\63\uffff\2\2\5\uffff\1\2\1\1\2\uffff\1\2\6\uffff\1\2\4"+
            "\uffff\1\3\2\uffff\1\2\32\uffff\1\2\5\uffff\1\2\13\uffff\2\2"+
            "\5\uffff\1\2\4\uffff\5\2\1\uffff\3\2",
            "\2\2\35\uffff\1\2\2\uffff\3\4\4\2\1\uffff\2\2\3\uffff\1\4\1"+
            "\2\3\uffff\2\2\17\uffff\1\2\3\uffff\1\2\11\uffff\1\2\1\uffff"+
            "\31\2\3\uffff\1\2",
            "",
            "\1\2\63\uffff\2\2\6\uffff\1\5\2\uffff\1\6\13\uffff\1\2\2\uffff"+
            "\1\2\32\uffff\1\2\5\uffff\1\2\13\uffff\2\2\5\uffff\1\2\4\uffff"+
            "\5\2\1\uffff\2\2\1\6",
            "",
            "\2\2\40\uffff\1\7\6\2\2\uffff\1\2\3\uffff\3\2\2\uffff\22\2"+
            "\3\uffff\1\2\11\uffff\1\2\1\uffff\31\2\3\uffff\1\2",
            "\1\10\2\uffff\1\6\117\uffff\1\6",
            "\1\2\63\uffff\2\2\6\uffff\1\11\2\uffff\1\12\13\uffff\1\2\2"+
            "\uffff\1\2\32\uffff\1\2\5\uffff\1\2\13\uffff\2\2\5\uffff\1\2"+
            "\4\uffff\5\2\1\uffff\2\2\1\12",
            "\2\2\40\uffff\1\13\2\uffff\4\2\2\uffff\1\2\4\uffff\2\2\2\uffff"+
            "\22\2\3\uffff\1\2\11\uffff\1\2\1\uffff\31\2\3\uffff\1\2",
            "\2\2\40\uffff\1\7\6\2\2\uffff\1\2\3\uffff\2\2\1\14\2\uffff"+
            "\22\2\3\uffff\1\2\11\uffff\1\2\1\uffff\31\2\3\uffff\1\2",
            "\1\15\2\uffff\1\12\117\uffff\1\12",
            "\1\2\63\uffff\2\2\6\uffff\1\16\2\uffff\1\12\13\uffff\1\2\2"+
            "\uffff\1\2\32\uffff\1\2\5\uffff\1\2\13\uffff\2\2\5\uffff\1\2"+
            "\4\uffff\5\2\1\uffff\2\2\1\12",
            "\2\2\35\uffff\1\2\3\uffff\2\4\1\uffff\1\2\1\uffff\1\2\2\uffff"+
            "\1\2\4\uffff\1\2\3\uffff\2\2\17\uffff\1\2\3\uffff\1\2\11\uffff"+
            "\1\2\1\uffff\31\2\3\uffff\1\2",
            "\2\2\40\uffff\1\13\2\uffff\4\2\2\uffff\1\2\4\uffff\1\2\1\14"+
            "\2\uffff\22\2\3\uffff\1\2\11\uffff\1\2\1\uffff\31\2\3\uffff"+
            "\1\2",
            "\2\2\40\uffff\1\13\2\uffff\4\2\2\uffff\1\2\4\uffff\1\2\1\14"+
            "\2\uffff\22\2\3\uffff\1\2\11\uffff\1\2\1\uffff\31\2\3\uffff"+
            "\1\2"
    };

    static final short[] DFA3_eot = DFA.unpackEncodedString(DFA3_eotS);
    static final short[] DFA3_eof = DFA.unpackEncodedString(DFA3_eofS);
    static final char[] DFA3_min = DFA.unpackEncodedStringToUnsignedChars(DFA3_minS);
    static final char[] DFA3_max = DFA.unpackEncodedStringToUnsignedChars(DFA3_maxS);
    static final short[] DFA3_accept = DFA.unpackEncodedString(DFA3_acceptS);
    static final short[] DFA3_special = DFA.unpackEncodedString(DFA3_specialS);
    static final short[][] DFA3_transition;

    static {
        int numStates = DFA3_transitionS.length;
        DFA3_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA3_transition[i] = DFA.unpackEncodedString(DFA3_transitionS[i]);
        }
    }

    class DFA3 extends DFA {

        public DFA3(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 3;
            this.eot = DFA3_eot;
            this.eof = DFA3_eof;
            this.min = DFA3_min;
            this.max = DFA3_max;
            this.accept = DFA3_accept;
            this.special = DFA3_special;
            this.transition = DFA3_transition;
        }
        public String getDescription() {
            return "137:1: toplevelstat : ( toplevelstatNoAlloc -> toplevelstatNoAlloc | toplevelAlloc SEMI -> toplevelAlloc );";
        }
    }
    static final String DFA7_eotS =
        "\15\uffff";
    static final String DFA7_eofS =
        "\15\uffff";
    static final String DFA7_minS =
        "\1\11\1\44\1\105\4\uffff\1\105\1\uffff\2\44\1\105\1\44";
    static final String DFA7_maxS =
        "\1\u0098\1\u0095\1\u0098\4\uffff\1\105\1\uffff\2\u0095\1\105\1\u0095";
    static final String DFA7_acceptS =
        "\3\uffff\1\3\1\4\1\5\1\1\1\uffff\1\2\4\uffff";
    static final String DFA7_specialS =
        "\1\0\14\uffff}>";
    static final String[] DFA7_transitionS = {
            "\1\4\63\uffff\2\4\5\uffff\1\3\1\1\2\uffff\1\2\6\uffff\1\5\4"+
            "\uffff\1\4\2\uffff\1\4\32\uffff\1\4\5\uffff\1\4\13\uffff\2\4"+
            "\5\uffff\1\4\4\uffff\5\4\1\uffff\2\4\1\2",
            "\2\4\35\uffff\1\4\5\uffff\1\6\1\4\1\10\1\4\1\uffff\1\6\1\4"+
            "\4\uffff\1\4\3\uffff\2\4\17\uffff\1\7\3\uffff\1\4\11\uffff\1"+
            "\4\1\uffff\31\4\3\uffff\1\4",
            "\1\11\2\uffff\1\2\117\uffff\1\2",
            "",
            "",
            "",
            "",
            "\1\12",
            "",
            "\2\4\35\uffff\1\4\6\uffff\1\4\1\10\1\4\2\uffff\1\4\4\uffff"+
            "\1\4\3\uffff\2\4\17\uffff\1\13\3\uffff\1\4\11\uffff\1\4\1\uffff"+
            "\31\4\3\uffff\1\4",
            "\2\4\35\uffff\1\4\6\uffff\1\4\1\10\1\4\2\uffff\1\4\4\uffff"+
            "\1\4\3\uffff\2\4\17\uffff\1\7\3\uffff\1\4\11\uffff\1\4\1\uffff"+
            "\31\4\3\uffff\1\4",
            "\1\14",
            "\2\4\35\uffff\1\4\6\uffff\1\4\1\10\1\4\2\uffff\1\4\4\uffff"+
            "\1\4\3\uffff\2\4\17\uffff\1\13\3\uffff\1\4\11\uffff\1\4\1\uffff"+
            "\31\4\3\uffff\1\4"
    };

    static final short[] DFA7_eot = DFA.unpackEncodedString(DFA7_eotS);
    static final short[] DFA7_eof = DFA.unpackEncodedString(DFA7_eofS);
    static final char[] DFA7_min = DFA.unpackEncodedStringToUnsignedChars(DFA7_minS);
    static final char[] DFA7_max = DFA.unpackEncodedStringToUnsignedChars(DFA7_maxS);
    static final short[] DFA7_accept = DFA.unpackEncodedString(DFA7_acceptS);
    static final short[] DFA7_special = DFA.unpackEncodedString(DFA7_specialS);
    static final short[][] DFA7_transition;

    static {
        int numStates = DFA7_transitionS.length;
        DFA7_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA7_transition[i] = DFA.unpackEncodedString(DFA7_transitionS[i]);
        }
    }

    class DFA7 extends DFA {

        public DFA7(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 7;
            this.eot = DFA7_eot;
            this.eof = DFA7_eof;
            this.min = DFA7_min;
            this.max = DFA7_max;
            this.accept = DFA7_accept;
            this.special = DFA7_special;
            this.transition = DFA7_transition;
        }
        public String getDescription() {
            return "140:1: toplevelstatNoAlloc : ( defineStmt | scopeExtension ( SEMI )? -> scopeExtension | FORWARD ID ( COMMA ID )* SEMI -> ( ^( FORWARD ID ) )+ | rhsExpr SEMI -> ^( EXPR rhsExpr ) | ( LBRACE )=> xscope ( SEMI )? );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA7_0 = input.LA(1);

                         
                        int index7_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA7_0==ID) ) {s = 1;}

                        else if ( (LA7_0==COLON||LA7_0==COLONS) ) {s = 2;}

                        else if ( (LA7_0==FORWARD) ) {s = 3;}

                        else if ( (LA7_0==CODE||(LA7_0>=SIZEOF && LA7_0<=TYPEOF)||LA7_0==LPAREN||LA7_0==NIL||LA7_0==IF||LA7_0==NOT||(LA7_0>=TILDE && LA7_0<=AMP)||LA7_0==MINUS||(LA7_0>=PLUSPLUS && LA7_0<=STRING_LITERAL)||(LA7_0>=FALSE && LA7_0<=TRUE)) ) {s = 4;}

                        else if ( (LA7_0==LBRACE) && (synpred1_Eulang())) {s = 5;}

                         
                        input.seek(index7_0);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 7, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA22_eotS =
        "\u00a4\uffff";
    static final String DFA22_eofS =
        "\u00a4\uffff";
    static final String DFA22_minS =
        "\1\105\1\111\1\11\1\uffff\1\11\27\uffff\1\44\20\uffff\1\11\44\uffff"+
        "\1\11\30\uffff\1\44\70\uffff";
    static final String DFA22_maxS =
        "\1\105\1\116\1\u0099\1\uffff\1\u0098\27\uffff\1\u0095\20\uffff\1"+
        "\u0099\44\uffff\1\u0098\30\uffff\1\u0095\70\uffff";
    static final String DFA22_acceptS =
        "\3\uffff\1\2\1\uffff\27\3\1\uffff\20\3\1\uffff\44\3\1\uffff\2\3"+
        "\26\1\1\uffff\70\3";
    static final String DFA22_specialS =
        "\1\uffff\1\4\1\3\1\uffff\1\2\27\uffff\1\0\20\uffff\1\5\44\uffff"+
        "\1\6\30\uffff\1\1\70\uffff}>";
    static final String[] DFA22_transitionS = {
            "\1\1",
            "\1\2\4\uffff\1\3",
            "\1\21\63\uffff\1\25\1\26\6\uffff\1\6\2\uffff\1\7\3\uffff\1"+
            "\4\2\uffff\1\5\4\uffff\1\12\2\uffff\1\20\32\uffff\1\30\5\uffff"+
            "\1\27\13\uffff\1\11\1\24\5\uffff\1\10\4\uffff\1\22\1\23\1\13"+
            "\1\14\1\15\1\uffff\1\16\1\17\1\7\1\31",
            "",
            "\1\45\63\uffff\1\51\1\52\6\uffff\1\34\2\uffff\1\35\4\uffff"+
            "\1\55\6\uffff\1\36\2\uffff\1\44\32\uffff\1\54\5\uffff\1\53\13"+
            "\uffff\1\33\1\50\5\uffff\1\32\4\uffff\1\46\1\47\1\37\1\40\1"+
            "\41\1\uffff\1\42\1\43\1\35",
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
            "\1\71\1\73\40\uffff\1\122\3\uffff\1\74\1\uffff\1\61\1\55\1"+
            "\uffff\1\63\4\uffff\1\60\3\uffff\1\121\1\62\17\uffff\1\56\3"+
            "\uffff\1\117\11\uffff\1\120\1\uffff\1\106\1\107\1\110\1\111"+
            "\1\112\1\113\1\57\1\114\1\115\1\116\1\105\1\104\1\103\1\76\1"+
            "\77\1\100\1\101\1\102\1\75\1\66\1\67\1\70\1\72\1\65\1\123\3"+
            "\uffff\1\64",
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
            "\1\142\63\uffff\1\146\1\147\4\uffff\1\124\1\uffff\1\126\2\uffff"+
            "\1\127\3\uffff\1\130\2\uffff\1\125\4\uffff\1\133\2\uffff\1\141"+
            "\32\uffff\1\151\5\uffff\1\150\13\uffff\1\132\1\145\5\uffff\1"+
            "\131\4\uffff\1\143\1\144\1\134\1\135\1\136\1\uffff\1\137\1\140"+
            "\1\127\1\152",
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
            "\1\167\63\uffff\1\173\1\174\6\uffff\1\153\2\uffff\1\157\4\uffff"+
            "\1\154\6\uffff\1\160\2\uffff\1\166\32\uffff\1\176\5\uffff\1"+
            "\175\13\uffff\1\156\1\172\5\uffff\1\155\4\uffff\1\170\1\171"+
            "\1\161\1\162\1\163\1\uffff\1\164\1\165\1\157",
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
            "\1\u008a\1\u008c\40\uffff\1\122\3\uffff\1\u008d\1\uffff\1\u0082"+
            "\1\55\1\uffff\1\u0084\4\uffff\1\u0081\3\uffff\1\u00a2\1\u0083"+
            "\17\uffff\1\177\3\uffff\1\u00a0\11\uffff\1\u00a1\1\uffff\1\u0097"+
            "\1\u0098\1\u0099\1\u009a\1\u009b\1\u009c\1\u0080\1\u009d\1\u009e"+
            "\1\u009f\1\u0096\1\u0095\1\u0094\1\u008f\1\u0090\1\u0091\1\u0092"+
            "\1\u0093\1\u008e\1\u0087\1\u0088\1\u0089\1\u008b\1\u0086\1\u00a3"+
            "\3\uffff\1\u0085",
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
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA22_eot = DFA.unpackEncodedString(DFA22_eotS);
    static final short[] DFA22_eof = DFA.unpackEncodedString(DFA22_eofS);
    static final char[] DFA22_min = DFA.unpackEncodedStringToUnsignedChars(DFA22_minS);
    static final char[] DFA22_max = DFA.unpackEncodedStringToUnsignedChars(DFA22_maxS);
    static final short[] DFA22_accept = DFA.unpackEncodedString(DFA22_acceptS);
    static final short[] DFA22_special = DFA.unpackEncodedString(DFA22_specialS);
    static final short[][] DFA22_transition;

    static {
        int numStates = DFA22_transitionS.length;
        DFA22_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA22_transition[i] = DFA.unpackEncodedString(DFA22_transitionS[i]);
        }
    }

    class DFA22 extends DFA {

        public DFA22(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 22;
            this.eot = DFA22_eot;
            this.eof = DFA22_eof;
            this.min = DFA22_min;
            this.max = DFA22_max;
            this.accept = DFA22_accept;
            this.special = DFA22_special;
            this.transition = DFA22_transition;
        }
        public String getDescription() {
            return "174:1: defineStmt : ( ( ID EQUALS LBRACKET )=> ID EQUALS LBRACKET idlistOrEmpty RBRACKET toplevelvalue SEMI -> ^( DEFINE ID idlistOrEmpty toplevelvalue ) | ( ID EQUALS_COLON )=> ID EQUALS_COLON type SEMI -> ^( DEFINE ID type ) | ( ID EQUALS )=> ID EQUALS toplevelvalue SEMI -> ^( DEFINE ID toplevelvalue ) );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA22_28 = input.LA(1);

                         
                        int index22_28 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA22_28==PERIOD) && (synpred4_Eulang())) {s = 46;}

                        else if ( (LA22_28==LESS) && (synpred4_Eulang())) {s = 47;}

                        else if ( (LA22_28==LPAREN) && (synpred4_Eulang())) {s = 48;}

                        else if ( (LA22_28==LBRACKET) && (synpred4_Eulang())) {s = 49;}

                        else if ( (LA22_28==CARET) && (synpred4_Eulang())) {s = 50;}

                        else if ( (LA22_28==LBRACE) && (synpred4_Eulang())) {s = 51;}

                        else if ( (LA22_28==AS) && (synpred4_Eulang())) {s = 52;}

                        else if ( (LA22_28==PLUSPLUS) && (synpred4_Eulang())) {s = 53;}

                        else if ( (LA22_28==STAR) && (synpred4_Eulang())) {s = 54;}

                        else if ( (LA22_28==SLASH) && (synpred4_Eulang())) {s = 55;}

                        else if ( (LA22_28==REM) && (synpred4_Eulang())) {s = 56;}

                        else if ( (LA22_28==UDIV) && (synpred4_Eulang())) {s = 57;}

                        else if ( (LA22_28==UREM) && (synpred4_Eulang())) {s = 58;}

                        else if ( (LA22_28==MOD) && (synpred4_Eulang())) {s = 59;}

                        else if ( (LA22_28==PLUS) && (synpred4_Eulang())) {s = 60;}

                        else if ( (LA22_28==MINUS) && (synpred4_Eulang())) {s = 61;}

                        else if ( (LA22_28==LSHIFT) && (synpred4_Eulang())) {s = 62;}

                        else if ( (LA22_28==RSHIFT) && (synpred4_Eulang())) {s = 63;}

                        else if ( (LA22_28==URSHIFT) && (synpred4_Eulang())) {s = 64;}

                        else if ( (LA22_28==CRSHIFT) && (synpred4_Eulang())) {s = 65;}

                        else if ( (LA22_28==CLSHIFT) && (synpred4_Eulang())) {s = 66;}

                        else if ( (LA22_28==AMP) && (synpred4_Eulang())) {s = 67;}

                        else if ( (LA22_28==TILDE) && (synpred4_Eulang())) {s = 68;}

                        else if ( (LA22_28==BAR) && (synpred4_Eulang())) {s = 69;}

                        else if ( (LA22_28==COMPEQ) && (synpred4_Eulang())) {s = 70;}

                        else if ( (LA22_28==COMPNE) && (synpred4_Eulang())) {s = 71;}

                        else if ( (LA22_28==COMPLE) && (synpred4_Eulang())) {s = 72;}

                        else if ( (LA22_28==COMPGE) && (synpred4_Eulang())) {s = 73;}

                        else if ( (LA22_28==COMPULE) && (synpred4_Eulang())) {s = 74;}

                        else if ( (LA22_28==COMPUGE) && (synpred4_Eulang())) {s = 75;}

                        else if ( (LA22_28==ULESS) && (synpred4_Eulang())) {s = 76;}

                        else if ( (LA22_28==GREATER) && (synpred4_Eulang())) {s = 77;}

                        else if ( (LA22_28==UGREATER) && (synpred4_Eulang())) {s = 78;}

                        else if ( (LA22_28==AND) && (synpred4_Eulang())) {s = 79;}

                        else if ( (LA22_28==OR) && (synpred4_Eulang())) {s = 80;}

                        else if ( (LA22_28==QUESTION) && (synpred4_Eulang())) {s = 81;}

                        else if ( (LA22_28==COMMA) ) {s = 82;}

                        else if ( (LA22_28==RBRACKET) ) {s = 45;}

                        else if ( (LA22_28==MINUSMINUS) && (synpred4_Eulang())) {s = 83;}

                         
                        input.seek(index22_28);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA22_107 = input.LA(1);

                         
                        int index22_107 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA22_107==PERIOD) && (synpred4_Eulang())) {s = 127;}

                        else if ( (LA22_107==LESS) && (synpred4_Eulang())) {s = 128;}

                        else if ( (LA22_107==LPAREN) && (synpred4_Eulang())) {s = 129;}

                        else if ( (LA22_107==LBRACKET) && (synpred4_Eulang())) {s = 130;}

                        else if ( (LA22_107==CARET) && (synpred4_Eulang())) {s = 131;}

                        else if ( (LA22_107==LBRACE) && (synpred4_Eulang())) {s = 132;}

                        else if ( (LA22_107==AS) && (synpred4_Eulang())) {s = 133;}

                        else if ( (LA22_107==PLUSPLUS) && (synpred4_Eulang())) {s = 134;}

                        else if ( (LA22_107==RBRACKET) ) {s = 45;}

                        else if ( (LA22_107==COMMA) ) {s = 82;}

                        else if ( (LA22_107==STAR) && (synpred4_Eulang())) {s = 135;}

                        else if ( (LA22_107==SLASH) && (synpred4_Eulang())) {s = 136;}

                        else if ( (LA22_107==REM) && (synpred4_Eulang())) {s = 137;}

                        else if ( (LA22_107==UDIV) && (synpred4_Eulang())) {s = 138;}

                        else if ( (LA22_107==UREM) && (synpred4_Eulang())) {s = 139;}

                        else if ( (LA22_107==MOD) && (synpred4_Eulang())) {s = 140;}

                        else if ( (LA22_107==PLUS) && (synpred4_Eulang())) {s = 141;}

                        else if ( (LA22_107==MINUS) && (synpred4_Eulang())) {s = 142;}

                        else if ( (LA22_107==LSHIFT) && (synpred4_Eulang())) {s = 143;}

                        else if ( (LA22_107==RSHIFT) && (synpred4_Eulang())) {s = 144;}

                        else if ( (LA22_107==URSHIFT) && (synpred4_Eulang())) {s = 145;}

                        else if ( (LA22_107==CRSHIFT) && (synpred4_Eulang())) {s = 146;}

                        else if ( (LA22_107==CLSHIFT) && (synpred4_Eulang())) {s = 147;}

                        else if ( (LA22_107==AMP) && (synpred4_Eulang())) {s = 148;}

                        else if ( (LA22_107==TILDE) && (synpred4_Eulang())) {s = 149;}

                        else if ( (LA22_107==BAR) && (synpred4_Eulang())) {s = 150;}

                        else if ( (LA22_107==COMPEQ) && (synpred4_Eulang())) {s = 151;}

                        else if ( (LA22_107==COMPNE) && (synpred4_Eulang())) {s = 152;}

                        else if ( (LA22_107==COMPLE) && (synpred4_Eulang())) {s = 153;}

                        else if ( (LA22_107==COMPGE) && (synpred4_Eulang())) {s = 154;}

                        else if ( (LA22_107==COMPULE) && (synpred4_Eulang())) {s = 155;}

                        else if ( (LA22_107==COMPUGE) && (synpred4_Eulang())) {s = 156;}

                        else if ( (LA22_107==ULESS) && (synpred4_Eulang())) {s = 157;}

                        else if ( (LA22_107==GREATER) && (synpred4_Eulang())) {s = 158;}

                        else if ( (LA22_107==UGREATER) && (synpred4_Eulang())) {s = 159;}

                        else if ( (LA22_107==AND) && (synpred4_Eulang())) {s = 160;}

                        else if ( (LA22_107==OR) && (synpred4_Eulang())) {s = 161;}

                        else if ( (LA22_107==QUESTION) && (synpred4_Eulang())) {s = 162;}

                        else if ( (LA22_107==MINUSMINUS) && (synpred4_Eulang())) {s = 163;}

                         
                        input.seek(index22_107);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA22_4 = input.LA(1);

                         
                        int index22_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA22_4==MINUS) && (synpred4_Eulang())) {s = 26;}

                        else if ( (LA22_4==TILDE) && (synpred4_Eulang())) {s = 27;}

                        else if ( (LA22_4==ID) ) {s = 28;}

                        else if ( (LA22_4==COLON||LA22_4==COLONS) && (synpred4_Eulang())) {s = 29;}

                        else if ( (LA22_4==LPAREN) && (synpred4_Eulang())) {s = 30;}

                        else if ( (LA22_4==NUMBER) && (synpred4_Eulang())) {s = 31;}

                        else if ( (LA22_4==CHAR_LITERAL) && (synpred4_Eulang())) {s = 32;}

                        else if ( (LA22_4==STRING_LITERAL) && (synpred4_Eulang())) {s = 33;}

                        else if ( (LA22_4==FALSE) && (synpred4_Eulang())) {s = 34;}

                        else if ( (LA22_4==TRUE) && (synpred4_Eulang())) {s = 35;}

                        else if ( (LA22_4==NIL) && (synpred4_Eulang())) {s = 36;}

                        else if ( (LA22_4==CODE) && (synpred4_Eulang())) {s = 37;}

                        else if ( (LA22_4==PLUSPLUS) && (synpred4_Eulang())) {s = 38;}

                        else if ( (LA22_4==MINUSMINUS) && (synpred4_Eulang())) {s = 39;}

                        else if ( (LA22_4==AMP) && (synpred4_Eulang())) {s = 40;}

                        else if ( (LA22_4==SIZEOF) && (synpred4_Eulang())) {s = 41;}

                        else if ( (LA22_4==TYPEOF) && (synpred4_Eulang())) {s = 42;}

                        else if ( (LA22_4==NOT) && (synpred4_Eulang())) {s = 43;}

                        else if ( (LA22_4==IF) && (synpred4_Eulang())) {s = 44;}

                        else if ( (LA22_4==RBRACKET) ) {s = 45;}

                         
                        input.seek(index22_4);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA22_2 = input.LA(1);

                         
                        int index22_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA22_2==LBRACKET) ) {s = 4;}

                        else if ( (LA22_2==LBRACE) && (synpred4_Eulang())) {s = 5;}

                        else if ( (LA22_2==ID) && (synpred4_Eulang())) {s = 6;}

                        else if ( (LA22_2==COLON||LA22_2==COLONS) && (synpred4_Eulang())) {s = 7;}

                        else if ( (LA22_2==MINUS) && (synpred4_Eulang())) {s = 8;}

                        else if ( (LA22_2==TILDE) && (synpred4_Eulang())) {s = 9;}

                        else if ( (LA22_2==LPAREN) && (synpred4_Eulang())) {s = 10;}

                        else if ( (LA22_2==NUMBER) && (synpred4_Eulang())) {s = 11;}

                        else if ( (LA22_2==CHAR_LITERAL) && (synpred4_Eulang())) {s = 12;}

                        else if ( (LA22_2==STRING_LITERAL) && (synpred4_Eulang())) {s = 13;}

                        else if ( (LA22_2==FALSE) && (synpred4_Eulang())) {s = 14;}

                        else if ( (LA22_2==TRUE) && (synpred4_Eulang())) {s = 15;}

                        else if ( (LA22_2==NIL) && (synpred4_Eulang())) {s = 16;}

                        else if ( (LA22_2==CODE) && (synpred4_Eulang())) {s = 17;}

                        else if ( (LA22_2==PLUSPLUS) && (synpred4_Eulang())) {s = 18;}

                        else if ( (LA22_2==MINUSMINUS) && (synpred4_Eulang())) {s = 19;}

                        else if ( (LA22_2==AMP) && (synpred4_Eulang())) {s = 20;}

                        else if ( (LA22_2==SIZEOF) && (synpred4_Eulang())) {s = 21;}

                        else if ( (LA22_2==TYPEOF) && (synpred4_Eulang())) {s = 22;}

                        else if ( (LA22_2==NOT) && (synpred4_Eulang())) {s = 23;}

                        else if ( (LA22_2==IF) && (synpred4_Eulang())) {s = 24;}

                        else if ( (LA22_2==DATA) && (synpred4_Eulang())) {s = 25;}

                         
                        input.seek(index22_2);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA22_1 = input.LA(1);

                         
                        int index22_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA22_1==EQUALS) ) {s = 2;}

                        else if ( (LA22_1==EQUALS_COLON) && (synpred3_Eulang())) {s = 3;}

                         
                        input.seek(index22_1);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA22_45 = input.LA(1);

                         
                        int index22_45 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA22_45==SEMI) && (synpred4_Eulang())) {s = 84;}

                        else if ( (LA22_45==LBRACE) && (synpred2_Eulang())) {s = 85;}

                        else if ( (LA22_45==ID) && (synpred2_Eulang())) {s = 86;}

                        else if ( (LA22_45==COLON||LA22_45==COLONS) && (synpred2_Eulang())) {s = 87;}

                        else if ( (LA22_45==LBRACKET) && (synpred2_Eulang())) {s = 88;}

                        else if ( (LA22_45==MINUS) && (synpred2_Eulang())) {s = 89;}

                        else if ( (LA22_45==TILDE) && (synpred2_Eulang())) {s = 90;}

                        else if ( (LA22_45==LPAREN) && (synpred2_Eulang())) {s = 91;}

                        else if ( (LA22_45==NUMBER) && (synpred2_Eulang())) {s = 92;}

                        else if ( (LA22_45==CHAR_LITERAL) && (synpred2_Eulang())) {s = 93;}

                        else if ( (LA22_45==STRING_LITERAL) && (synpred2_Eulang())) {s = 94;}

                        else if ( (LA22_45==FALSE) && (synpred2_Eulang())) {s = 95;}

                        else if ( (LA22_45==TRUE) && (synpred2_Eulang())) {s = 96;}

                        else if ( (LA22_45==NIL) && (synpred2_Eulang())) {s = 97;}

                        else if ( (LA22_45==CODE) && (synpred2_Eulang())) {s = 98;}

                        else if ( (LA22_45==PLUSPLUS) && (synpred2_Eulang())) {s = 99;}

                        else if ( (LA22_45==MINUSMINUS) && (synpred2_Eulang())) {s = 100;}

                        else if ( (LA22_45==AMP) && (synpred2_Eulang())) {s = 101;}

                        else if ( (LA22_45==SIZEOF) && (synpred2_Eulang())) {s = 102;}

                        else if ( (LA22_45==TYPEOF) && (synpred2_Eulang())) {s = 103;}

                        else if ( (LA22_45==NOT) && (synpred2_Eulang())) {s = 104;}

                        else if ( (LA22_45==IF) && (synpred2_Eulang())) {s = 105;}

                        else if ( (LA22_45==DATA) && (synpred2_Eulang())) {s = 106;}

                         
                        input.seek(index22_45);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA22_82 = input.LA(1);

                         
                        int index22_82 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA22_82==ID) ) {s = 107;}

                        else if ( (LA22_82==RBRACKET) && (synpred4_Eulang())) {s = 108;}

                        else if ( (LA22_82==MINUS) && (synpred4_Eulang())) {s = 109;}

                        else if ( (LA22_82==TILDE) && (synpred4_Eulang())) {s = 110;}

                        else if ( (LA22_82==COLON||LA22_82==COLONS) && (synpred4_Eulang())) {s = 111;}

                        else if ( (LA22_82==LPAREN) && (synpred4_Eulang())) {s = 112;}

                        else if ( (LA22_82==NUMBER) && (synpred4_Eulang())) {s = 113;}

                        else if ( (LA22_82==CHAR_LITERAL) && (synpred4_Eulang())) {s = 114;}

                        else if ( (LA22_82==STRING_LITERAL) && (synpred4_Eulang())) {s = 115;}

                        else if ( (LA22_82==FALSE) && (synpred4_Eulang())) {s = 116;}

                        else if ( (LA22_82==TRUE) && (synpred4_Eulang())) {s = 117;}

                        else if ( (LA22_82==NIL) && (synpred4_Eulang())) {s = 118;}

                        else if ( (LA22_82==CODE) && (synpred4_Eulang())) {s = 119;}

                        else if ( (LA22_82==PLUSPLUS) && (synpred4_Eulang())) {s = 120;}

                        else if ( (LA22_82==MINUSMINUS) && (synpred4_Eulang())) {s = 121;}

                        else if ( (LA22_82==AMP) && (synpred4_Eulang())) {s = 122;}

                        else if ( (LA22_82==SIZEOF) && (synpred4_Eulang())) {s = 123;}

                        else if ( (LA22_82==TYPEOF) && (synpred4_Eulang())) {s = 124;}

                        else if ( (LA22_82==NOT) && (synpred4_Eulang())) {s = 125;}

                        else if ( (LA22_82==IF) && (synpred4_Eulang())) {s = 126;}

                         
                        input.seek(index22_82);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 22, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA23_eotS =
        "\17\uffff";
    static final String DFA23_eofS =
        "\17\uffff";
    static final String DFA23_minS =
        "\1\11\1\uffff\1\44\1\105\3\uffff\1\105\1\11\2\44\2\uffff\1\105\1"+
        "\44";
    static final String DFA23_maxS =
        "\1\u0099\1\uffff\1\u0095\1\u0098\3\uffff\1\105\1\u0099\2\u0095\2"+
        "\uffff\1\105\1\u0095";
    static final String DFA23_acceptS =
        "\1\uffff\1\1\2\uffff\1\4\1\5\1\6\4\uffff\1\3\1\2\2\uffff";
    static final String DFA23_specialS =
        "\1\0\16\uffff}>";
    static final String[] DFA23_transitionS = {
            "\1\5\63\uffff\2\5\6\uffff\1\2\2\uffff\1\3\3\uffff\1\4\2\uffff"+
            "\1\1\4\uffff\1\5\2\uffff\1\5\32\uffff\1\5\5\uffff\1\5\13\uffff"+
            "\2\5\5\uffff\1\5\4\uffff\5\5\1\uffff\2\5\1\3\1\6",
            "",
            "\2\5\35\uffff\1\5\2\uffff\1\5\3\uffff\1\10\1\uffff\2\5\1\uffff"+
            "\1\5\4\uffff\1\5\3\uffff\2\5\17\uffff\1\7\3\uffff\1\5\11\uffff"+
            "\1\5\1\uffff\31\5\3\uffff\1\5",
            "\1\11\2\uffff\1\3\117\uffff\1\3",
            "",
            "",
            "",
            "\1\12",
            "\1\5\63\uffff\2\5\6\uffff\1\5\2\uffff\1\5\6\uffff\1\13\4\uffff"+
            "\1\5\2\uffff\1\5\54\uffff\2\5\5\uffff\1\5\4\uffff\5\5\1\uffff"+
            "\3\5\1\14",
            "\2\5\35\uffff\1\5\2\uffff\1\5\3\uffff\1\10\1\uffff\2\5\1\uffff"+
            "\1\5\4\uffff\1\5\3\uffff\2\5\17\uffff\1\15\3\uffff\1\5\11\uffff"+
            "\1\5\1\uffff\31\5\3\uffff\1\5",
            "\2\5\35\uffff\1\5\2\uffff\1\5\3\uffff\1\10\1\uffff\2\5\1\uffff"+
            "\1\5\4\uffff\1\5\3\uffff\2\5\17\uffff\1\7\3\uffff\1\5\11\uffff"+
            "\1\5\1\uffff\31\5\3\uffff\1\5",
            "",
            "",
            "\1\16",
            "\2\5\35\uffff\1\5\2\uffff\1\5\3\uffff\1\10\1\uffff\2\5\1\uffff"+
            "\1\5\4\uffff\1\5\3\uffff\2\5\17\uffff\1\15\3\uffff\1\5\11\uffff"+
            "\1\5\1\uffff\31\5\3\uffff\1\5"
    };

    static final short[] DFA23_eot = DFA.unpackEncodedString(DFA23_eotS);
    static final short[] DFA23_eof = DFA.unpackEncodedString(DFA23_eofS);
    static final char[] DFA23_min = DFA.unpackEncodedStringToUnsignedChars(DFA23_minS);
    static final char[] DFA23_max = DFA.unpackEncodedStringToUnsignedChars(DFA23_maxS);
    static final short[] DFA23_accept = DFA.unpackEncodedString(DFA23_acceptS);
    static final short[] DFA23_special = DFA.unpackEncodedString(DFA23_specialS);
    static final short[][] DFA23_transition;

    static {
        int numStates = DFA23_transitionS.length;
        DFA23_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA23_transition[i] = DFA.unpackEncodedString(DFA23_transitionS[i]);
        }
    }

    class DFA23 extends DFA {

        public DFA23(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 23;
            this.eot = DFA23_eot;
            this.eof = DFA23_eof;
            this.min = DFA23_min;
            this.max = DFA23_max;
            this.accept = DFA23_accept;
            this.special = DFA23_special;
            this.transition = DFA23_transition;
        }
        public String getDescription() {
            return "179:1: toplevelvalue : ( ( LBRACE )=> xscope | namespaceRef PLUS data -> ^( ADDSCOPE namespaceRef data ) | namespaceRef PLUS xscope -> ^( ADDSCOPE namespaceRef xscope ) | selector | rhsExpr | data );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA23_0 = input.LA(1);

                         
                        int index23_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA23_0==LBRACE) && (synpred5_Eulang())) {s = 1;}

                        else if ( (LA23_0==ID) ) {s = 2;}

                        else if ( (LA23_0==COLON||LA23_0==COLONS) ) {s = 3;}

                        else if ( (LA23_0==LBRACKET) ) {s = 4;}

                        else if ( (LA23_0==CODE||(LA23_0>=SIZEOF && LA23_0<=TYPEOF)||LA23_0==LPAREN||LA23_0==NIL||LA23_0==IF||LA23_0==NOT||(LA23_0>=TILDE && LA23_0<=AMP)||LA23_0==MINUS||(LA23_0>=PLUSPLUS && LA23_0<=STRING_LITERAL)||(LA23_0>=FALSE && LA23_0<=TRUE)) ) {s = 5;}

                        else if ( (LA23_0==DATA) ) {s = 6;}

                         
                        input.seek(index23_0);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 23, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA52_eotS =
        "\65\uffff";
    static final String DFA52_eofS =
        "\1\1\64\uffff";
    static final String DFA52_minS =
        "\1\44\60\uffff\1\0\3\uffff";
    static final String DFA52_maxS =
        "\1\u0091\60\uffff\1\0\3\uffff";
    static final String DFA52_acceptS =
        "\1\uffff\1\4\60\uffff\1\3\1\1\1\2";
    static final String DFA52_specialS =
        "\61\uffff\1\0\3\uffff}>";
    static final String[] DFA52_transitionS = {
            "\2\1\35\uffff\1\1\2\uffff\1\1\1\uffff\4\1\1\61\1\1\2\uffff\1"+
            "\1\1\uffff\1\1\2\uffff\2\1\1\uffff\1\1\1\62\17\1\1\uffff\2\1"+
            "\1\uffff\1\1\5\uffff\5\1\1\uffff\31\1",
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
            "\1\uffff",
            "",
            "",
            ""
    };

    static final short[] DFA52_eot = DFA.unpackEncodedString(DFA52_eotS);
    static final short[] DFA52_eof = DFA.unpackEncodedString(DFA52_eofS);
    static final char[] DFA52_min = DFA.unpackEncodedStringToUnsignedChars(DFA52_minS);
    static final char[] DFA52_max = DFA.unpackEncodedStringToUnsignedChars(DFA52_maxS);
    static final short[] DFA52_accept = DFA.unpackEncodedString(DFA52_acceptS);
    static final short[] DFA52_special = DFA.unpackEncodedString(DFA52_specialS);
    static final short[][] DFA52_transition;

    static {
        int numStates = DFA52_transitionS.length;
        DFA52_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA52_transition[i] = DFA.unpackEncodedString(DFA52_transitionS[i]);
        }
    }

    class DFA52 extends DFA {

        public DFA52(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 52;
            this.eot = DFA52_eot;
            this.eof = DFA52_eof;
            this.min = DFA52_min;
            this.max = DFA52_max;
            this.accept = DFA52_accept;
            this.special = DFA52_special;
            this.transition = DFA52_transition;
        }
        public String getDescription() {
            return "()* loopback of 278:6: ( ( ( ( arraySuff )+ )=> ( arraySuff )+ -> ^( TYPE ^( ARRAY $type ( arraySuff )+ ) ) ) | ( LBRACKET rhsExpr ( COMMA rhsExpr )+ RBRACKET -> ^( TYPE ^( ARRAY $type ( rhsExpr )+ ) ) ) | ( CARET -> ^( TYPE ^( POINTER $type) ) ) )*";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA52_49 = input.LA(1);

                         
                        int index52_49 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_Eulang()) ) {s = 51;}

                        else if ( (true) ) {s = 52;}

                         
                        input.seek(index52_49);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 52, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA60_eotS =
        "\34\uffff";
    static final String DFA60_eofS =
        "\34\uffff";
    static final String DFA60_minS =
        "\1\11\6\0\25\uffff";
    static final String DFA60_maxS =
        "\1\u0098\6\0\25\uffff";
    static final String DFA60_acceptS =
        "\7\uffff\1\3\14\uffff\1\4\1\5\1\6\3\uffff\1\1\1\2";
    static final String DFA60_specialS =
        "\1\0\1\1\1\2\1\3\1\4\1\5\1\6\25\uffff}>";
    static final String[] DFA60_transitionS = {
            "\1\7\46\uffff\1\25\14\uffff\2\7\6\uffff\1\1\2\uffff\1\3\6\uffff"+
            "\1\24\1\uffff\1\26\2\uffff\1\2\2\uffff\1\7\22\uffff\3\26\5\uffff"+
            "\1\7\5\uffff\1\7\13\uffff\2\7\5\uffff\1\7\4\uffff\2\7\1\4\1"+
            "\5\1\6\1\uffff\2\7\1\3",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
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
            ""
    };

    static final short[] DFA60_eot = DFA.unpackEncodedString(DFA60_eotS);
    static final short[] DFA60_eof = DFA.unpackEncodedString(DFA60_eofS);
    static final char[] DFA60_min = DFA.unpackEncodedStringToUnsignedChars(DFA60_minS);
    static final char[] DFA60_max = DFA.unpackEncodedStringToUnsignedChars(DFA60_maxS);
    static final short[] DFA60_accept = DFA.unpackEncodedString(DFA60_acceptS);
    static final short[] DFA60_special = DFA.unpackEncodedString(DFA60_specialS);
    static final short[][] DFA60_transition;

    static {
        int numStates = DFA60_transitionS.length;
        DFA60_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA60_transition[i] = DFA.unpackEncodedString(DFA60_transitionS[i]);
        }
    }

    class DFA60 extends DFA {

        public DFA60(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 60;
            this.eot = DFA60_eot;
            this.eof = DFA60_eof;
            this.min = DFA60_min;
            this.max = DFA60_max;
            this.accept = DFA60_accept;
            this.special = DFA60_special;
            this.transition = DFA60_transition;
        }
        public String getDescription() {
            return "310:1: codeStmtExpr : ( ( varDecl )=> varDecl -> varDecl | ( assignStmt )=> assignStmt -> assignStmt | rhsExpr -> ^( STMTEXPR rhsExpr ) | ( LBRACE )=> blockStmt -> blockStmt | gotoStmt -> gotoStmt | controlStmt -> controlStmt );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA60_0 = input.LA(1);

                         
                        int index60_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA60_0==ID) ) {s = 1;}

                        else if ( (LA60_0==LPAREN) ) {s = 2;}

                        else if ( (LA60_0==COLON||LA60_0==COLONS) ) {s = 3;}

                        else if ( (LA60_0==NUMBER) ) {s = 4;}

                        else if ( (LA60_0==CHAR_LITERAL) ) {s = 5;}

                        else if ( (LA60_0==STRING_LITERAL) ) {s = 6;}

                        else if ( (LA60_0==CODE||(LA60_0>=SIZEOF && LA60_0<=TYPEOF)||LA60_0==NIL||LA60_0==IF||LA60_0==NOT||(LA60_0>=TILDE && LA60_0<=AMP)||LA60_0==MINUS||(LA60_0>=PLUSPLUS && LA60_0<=MINUSMINUS)||(LA60_0>=FALSE && LA60_0<=TRUE)) ) {s = 7;}

                        else if ( (LA60_0==LBRACE) && (synpred12_Eulang())) {s = 20;}

                        else if ( (LA60_0==GOTO) ) {s = 21;}

                        else if ( (LA60_0==FOR||(LA60_0>=DO && LA60_0<=REPEAT)) ) {s = 22;}

                         
                        input.seek(index60_0);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA60_1 = input.LA(1);

                         
                        int index60_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_Eulang()) ) {s = 26;}

                        else if ( (synpred11_Eulang()) ) {s = 27;}

                        else if ( (true) ) {s = 7;}

                         
                        input.seek(index60_1);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA60_2 = input.LA(1);

                         
                        int index60_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_Eulang()) ) {s = 26;}

                        else if ( (synpred11_Eulang()) ) {s = 27;}

                        else if ( (true) ) {s = 7;}

                         
                        input.seek(index60_2);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA60_3 = input.LA(1);

                         
                        int index60_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred11_Eulang()) ) {s = 27;}

                        else if ( (true) ) {s = 7;}

                         
                        input.seek(index60_3);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA60_4 = input.LA(1);

                         
                        int index60_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred11_Eulang()) ) {s = 27;}

                        else if ( (true) ) {s = 7;}

                         
                        input.seek(index60_4);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA60_5 = input.LA(1);

                         
                        int index60_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred11_Eulang()) ) {s = 27;}

                        else if ( (true) ) {s = 7;}

                         
                        input.seek(index60_5);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA60_6 = input.LA(1);

                         
                        int index60_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred11_Eulang()) ) {s = 27;}

                        else if ( (true) ) {s = 7;}

                         
                        input.seek(index60_6);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 60, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA73_eotS =
        "\5\uffff";
    static final String DFA73_eofS =
        "\5\uffff";
    static final String DFA73_minS =
        "\1\106\1\107\3\uffff";
    static final String DFA73_maxS =
        "\2\123\3\uffff";
    static final String DFA73_acceptS =
        "\2\uffff\1\1\1\2\1\3";
    static final String DFA73_specialS =
        "\5\uffff}>";
    static final String[] DFA73_transitionS = {
            "\1\4\1\2\1\3\12\uffff\1\1",
            "\1\2\1\3\12\uffff\1\1",
            "",
            "",
            ""
    };

    static final short[] DFA73_eot = DFA.unpackEncodedString(DFA73_eotS);
    static final short[] DFA73_eof = DFA.unpackEncodedString(DFA73_eofS);
    static final char[] DFA73_min = DFA.unpackEncodedStringToUnsignedChars(DFA73_minS);
    static final char[] DFA73_max = DFA.unpackEncodedStringToUnsignedChars(DFA73_maxS);
    static final short[] DFA73_accept = DFA.unpackEncodedString(DFA73_acceptS);
    static final short[] DFA73_special = DFA.unpackEncodedString(DFA73_specialS);
    static final short[][] DFA73_transition;

    static {
        int numStates = DFA73_transitionS.length;
        DFA73_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA73_transition[i] = DFA.unpackEncodedString(DFA73_transitionS[i]);
        }
    }

    class DFA73 extends DFA {

        public DFA73(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 73;
            this.eot = DFA73_eot;
            this.eof = DFA73_eof;
            this.min = DFA73_min;
            this.max = DFA73_max;
            this.accept = DFA73_accept;
            this.special = DFA73_special;
            this.transition = DFA73_transition;
        }
        public String getDescription() {
            return "323:8: ( ( ( attrs )? COLON_EQUALS assignOrInitExpr -> ^( ALLOC ( attrs )? TYPE ID assignOrInitExpr ) ) | ( ( attrs )? COLON type ( EQUALS assignOrInitExpr )? -> ^( ALLOC ( attrs )? type ID ( assignOrInitExpr )* ) ) | ( COMMA ID )+ ( attrs )? ( ( COLON_EQUALS ( PLUS )? assignOrInitExpr ( COMMA assignOrInitExpr )* ) -> ^( ALLOC ( attrs )? TYPE ^( LIST ( ID )+ ) ( PLUS )? ^( LIST ( assignOrInitExpr )+ ) ) | ( COLON type ( EQUALS ( PLUS )? assignOrInitExpr ( COMMA assignOrInitExpr )* )? ) -> ^( ALLOC ( attrs )? type ^( LIST ( ID )+ ) ( PLUS )? ( ^( LIST ( assignOrInitExpr )+ ) )? ) ) )";
        }
    }
    static final String DFA80_eotS =
        "\12\uffff";
    static final String DFA80_eofS =
        "\12\uffff";
    static final String DFA80_minS =
        "\1\105\6\0\3\uffff";
    static final String DFA80_maxS =
        "\1\u0098\6\0\3\uffff";
    static final String DFA80_acceptS =
        "\7\uffff\1\1\1\3\1\2";
    static final String DFA80_specialS =
        "\1\uffff\1\0\1\1\1\2\1\3\1\4\1\5\3\uffff}>";
    static final String[] DFA80_transitionS = {
            "\1\1\2\uffff\1\2\13\uffff\1\3\75\uffff\1\4\1\5\1\6\3\uffff\1"+
            "\2",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "",
            "",
            ""
    };

    static final short[] DFA80_eot = DFA.unpackEncodedString(DFA80_eotS);
    static final short[] DFA80_eof = DFA.unpackEncodedString(DFA80_eofS);
    static final char[] DFA80_min = DFA.unpackEncodedStringToUnsignedChars(DFA80_minS);
    static final char[] DFA80_max = DFA.unpackEncodedStringToUnsignedChars(DFA80_maxS);
    static final short[] DFA80_accept = DFA.unpackEncodedString(DFA80_acceptS);
    static final short[] DFA80_special = DFA.unpackEncodedString(DFA80_specialS);
    static final short[][] DFA80_transition;

    static {
        int numStates = DFA80_transitionS.length;
        DFA80_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA80_transition[i] = DFA.unpackEncodedString(DFA80_transitionS[i]);
        }
    }

    class DFA80 extends DFA {

        public DFA80(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 80;
            this.eot = DFA80_eot;
            this.eof = DFA80_eof;
            this.min = DFA80_min;
            this.max = DFA80_max;
            this.accept = DFA80_accept;
            this.special = DFA80_special;
            this.transition = DFA80_transition;
        }
        public String getDescription() {
            return "342:1: assignStmt : ( ( lhs assignEqOp )=> lhs assignEqOp assignOrInitExpr -> ^( ASSIGN assignEqOp lhs assignOrInitExpr ) | idTuple EQUALS assignOrInitExpr -> ^( ASSIGN EQUALS idTuple assignOrInitExpr ) | ( lhs ( COMMA lhs )+ assignEqOp )=> lhs ( COMMA lhs )+ assignEqOp ( PLUS )? assignOrInitExpr ( COMMA assignOrInitExpr )* -> ^( ASSIGN assignEqOp ^( LIST ( lhs )+ ) ( PLUS )? ^( LIST ( assignOrInitExpr )+ ) ) );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA80_1 = input.LA(1);

                         
                        int index80_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred13_Eulang()) ) {s = 7;}

                        else if ( (synpred14_Eulang()) ) {s = 8;}

                         
                        input.seek(index80_1);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA80_2 = input.LA(1);

                         
                        int index80_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred13_Eulang()) ) {s = 7;}

                        else if ( (synpred14_Eulang()) ) {s = 8;}

                         
                        input.seek(index80_2);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA80_3 = input.LA(1);

                         
                        int index80_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred13_Eulang()) ) {s = 7;}

                        else if ( (true) ) {s = 9;}

                        else if ( (synpred14_Eulang()) ) {s = 8;}

                         
                        input.seek(index80_3);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA80_4 = input.LA(1);

                         
                        int index80_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred13_Eulang()) ) {s = 7;}

                        else if ( (synpred14_Eulang()) ) {s = 8;}

                         
                        input.seek(index80_4);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA80_5 = input.LA(1);

                         
                        int index80_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred13_Eulang()) ) {s = 7;}

                        else if ( (synpred14_Eulang()) ) {s = 8;}

                         
                        input.seek(index80_5);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA80_6 = input.LA(1);

                         
                        int index80_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred13_Eulang()) ) {s = 7;}

                        else if ( (synpred14_Eulang()) ) {s = 8;}

                         
                        input.seek(index80_6);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 80, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA82_eotS =
        "\26\uffff";
    static final String DFA82_eofS =
        "\26\uffff";
    static final String DFA82_minS =
        "\1\11\6\0\17\uffff";
    static final String DFA82_maxS =
        "\1\u0098\6\0\17\uffff";
    static final String DFA82_acceptS =
        "\7\uffff\1\3\14\uffff\1\1\1\2";
    static final String DFA82_specialS =
        "\1\uffff\1\0\1\1\1\2\1\3\1\4\1\5\17\uffff}>";
    static final String[] DFA82_transitionS = {
            "\1\7\63\uffff\2\7\6\uffff\1\1\2\uffff\1\2\13\uffff\1\3\2\uffff"+
            "\1\7\32\uffff\1\7\5\uffff\1\7\13\uffff\2\7\5\uffff\1\7\4\uffff"+
            "\2\7\1\4\1\5\1\6\1\uffff\2\7\1\2",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
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
            ""
    };

    static final short[] DFA82_eot = DFA.unpackEncodedString(DFA82_eotS);
    static final short[] DFA82_eof = DFA.unpackEncodedString(DFA82_eofS);
    static final char[] DFA82_min = DFA.unpackEncodedStringToUnsignedChars(DFA82_minS);
    static final char[] DFA82_max = DFA.unpackEncodedStringToUnsignedChars(DFA82_maxS);
    static final short[] DFA82_accept = DFA.unpackEncodedString(DFA82_acceptS);
    static final short[] DFA82_special = DFA.unpackEncodedString(DFA82_specialS);
    static final short[][] DFA82_transition;

    static {
        int numStates = DFA82_transitionS.length;
        DFA82_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA82_transition[i] = DFA.unpackEncodedString(DFA82_transitionS[i]);
        }
    }

    class DFA82 extends DFA {

        public DFA82(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 82;
            this.eot = DFA82_eot;
            this.eof = DFA82_eof;
            this.min = DFA82_min;
            this.max = DFA82_max;
            this.accept = DFA82_accept;
            this.special = DFA82_special;
            this.transition = DFA82_transition;
        }
        public String getDescription() {
            return "352:1: assignExpr : ( ( lhs assignEqOp )=> lhs assignEqOp assignExpr -> ^( ASSIGN assignEqOp lhs assignExpr ) | ( idTuple EQUALS )=> idTuple EQUALS assignExpr -> ^( ASSIGN EQUALS idTuple assignExpr ) | rhsExpr -> rhsExpr );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA82_1 = input.LA(1);

                         
                        int index82_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred15_Eulang()) ) {s = 20;}

                        else if ( (true) ) {s = 7;}

                         
                        input.seek(index82_1);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA82_2 = input.LA(1);

                         
                        int index82_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred15_Eulang()) ) {s = 20;}

                        else if ( (true) ) {s = 7;}

                         
                        input.seek(index82_2);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA82_3 = input.LA(1);

                         
                        int index82_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred15_Eulang()) ) {s = 20;}

                        else if ( (synpred16_Eulang()) ) {s = 21;}

                        else if ( (true) ) {s = 7;}

                         
                        input.seek(index82_3);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA82_4 = input.LA(1);

                         
                        int index82_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred15_Eulang()) ) {s = 20;}

                        else if ( (true) ) {s = 7;}

                         
                        input.seek(index82_4);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA82_5 = input.LA(1);

                         
                        int index82_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred15_Eulang()) ) {s = 20;}

                        else if ( (true) ) {s = 7;}

                         
                        input.seek(index82_5);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA82_6 = input.LA(1);

                         
                        int index82_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred15_Eulang()) ) {s = 20;}

                        else if ( (true) ) {s = 7;}

                         
                        input.seek(index82_6);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 82, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA86_eotS =
        "\30\uffff";
    static final String DFA86_eofS =
        "\30\uffff";
    static final String DFA86_minS =
        "\1\11\24\uffff\1\0\2\uffff";
    static final String DFA86_maxS =
        "\1\u0098\24\uffff\1\0\2\uffff";
    static final String DFA86_acceptS =
        "\1\uffff\23\1\1\2\1\uffff\1\3\1\4";
    static final String DFA86_specialS =
        "\1\0\24\uffff\1\1\2\uffff}>";
    static final String[] DFA86_transitionS = {
            "\1\14\63\uffff\1\20\1\21\6\uffff\1\3\2\uffff\1\4\3\uffff\1\25"+
            "\7\uffff\1\5\2\uffff\1\13\21\uffff\1\24\10\uffff\1\23\5\uffff"+
            "\1\22\13\uffff\1\2\1\17\5\uffff\1\1\4\uffff\1\15\1\16\1\6\1"+
            "\7\1\10\1\uffff\1\11\1\12\1\4",
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
            "\1\uffff",
            "",
            ""
    };

    static final short[] DFA86_eot = DFA.unpackEncodedString(DFA86_eotS);
    static final short[] DFA86_eof = DFA.unpackEncodedString(DFA86_eofS);
    static final char[] DFA86_min = DFA.unpackEncodedStringToUnsignedChars(DFA86_minS);
    static final char[] DFA86_max = DFA.unpackEncodedStringToUnsignedChars(DFA86_maxS);
    static final short[] DFA86_accept = DFA.unpackEncodedString(DFA86_acceptS);
    static final short[] DFA86_special = DFA.unpackEncodedString(DFA86_specialS);
    static final short[][] DFA86_transition;

    static {
        int numStates = DFA86_transitionS.length;
        DFA86_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA86_transition[i] = DFA.unpackEncodedString(DFA86_transitionS[i]);
        }
    }

    class DFA86 extends DFA {

        public DFA86(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 86;
            this.eot = DFA86_eot;
            this.eof = DFA86_eof;
            this.min = DFA86_min;
            this.max = DFA86_max;
            this.accept = DFA86_accept;
            this.special = DFA86_special;
            this.transition = DFA86_transition;
        }
        public String getDescription() {
            return "362:1: initExpr : ( ( rhsExpr )=>e= rhsExpr -> ^( INITEXPR $e) | PERIOD ID EQUALS ei= initElement -> ^( INITEXPR $ei ID ) | ( LBRACKET i= rhsExpr RBRACKET )=> LBRACKET i= rhsExpr RBRACKET EQUALS ei= initElement -> ^( INITEXPR $ei $i) | initList );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA86_0 = input.LA(1);

                         
                        int index86_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA86_0==MINUS) && (synpred17_Eulang())) {s = 1;}

                        else if ( (LA86_0==TILDE) && (synpred17_Eulang())) {s = 2;}

                        else if ( (LA86_0==ID) && (synpred17_Eulang())) {s = 3;}

                        else if ( (LA86_0==COLON||LA86_0==COLONS) && (synpred17_Eulang())) {s = 4;}

                        else if ( (LA86_0==LPAREN) && (synpred17_Eulang())) {s = 5;}

                        else if ( (LA86_0==NUMBER) && (synpred17_Eulang())) {s = 6;}

                        else if ( (LA86_0==CHAR_LITERAL) && (synpred17_Eulang())) {s = 7;}

                        else if ( (LA86_0==STRING_LITERAL) && (synpred17_Eulang())) {s = 8;}

                        else if ( (LA86_0==FALSE) && (synpred17_Eulang())) {s = 9;}

                        else if ( (LA86_0==TRUE) && (synpred17_Eulang())) {s = 10;}

                        else if ( (LA86_0==NIL) && (synpred17_Eulang())) {s = 11;}

                        else if ( (LA86_0==CODE) && (synpred17_Eulang())) {s = 12;}

                        else if ( (LA86_0==PLUSPLUS) && (synpred17_Eulang())) {s = 13;}

                        else if ( (LA86_0==MINUSMINUS) && (synpred17_Eulang())) {s = 14;}

                        else if ( (LA86_0==AMP) && (synpred17_Eulang())) {s = 15;}

                        else if ( (LA86_0==SIZEOF) && (synpred17_Eulang())) {s = 16;}

                        else if ( (LA86_0==TYPEOF) && (synpred17_Eulang())) {s = 17;}

                        else if ( (LA86_0==NOT) && (synpred17_Eulang())) {s = 18;}

                        else if ( (LA86_0==IF) && (synpred17_Eulang())) {s = 19;}

                        else if ( (LA86_0==PERIOD) ) {s = 20;}

                        else if ( (LA86_0==LBRACKET) ) {s = 21;}

                         
                        input.seek(index86_0);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA86_21 = input.LA(1);

                         
                        int index86_21 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred18_Eulang()) ) {s = 22;}

                        else if ( (true) ) {s = 23;}

                         
                        input.seek(index86_21);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 86, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA119_eotS =
        "\24\uffff";
    static final String DFA119_eofS =
        "\24\uffff";
    static final String DFA119_minS =
        "\1\11\2\uffff\6\0\13\uffff";
    static final String DFA119_maxS =
        "\1\u0098\2\uffff\6\0\13\uffff";
    static final String DFA119_acceptS =
        "\1\uffff\1\1\1\2\6\uffff\1\5\3\uffff\1\6\1\7\1\10\1\11\1\12\1\3"+
        "\1\4";
    static final String DFA119_specialS =
        "\3\uffff\1\0\1\1\1\2\1\3\1\4\1\5\13\uffff}>";
    static final String[] DFA119_transitionS = {
            "\1\11\63\uffff\1\20\1\21\6\uffff\1\3\2\uffff\1\4\13\uffff\1"+
            "\5\2\uffff\1\11\54\uffff\1\2\1\17\5\uffff\1\1\4\uffff\1\15\1"+
            "\16\1\6\1\7\1\10\1\uffff\2\11\1\4",
            "",
            "",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
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
            ""
    };

    static final short[] DFA119_eot = DFA.unpackEncodedString(DFA119_eotS);
    static final short[] DFA119_eof = DFA.unpackEncodedString(DFA119_eofS);
    static final char[] DFA119_min = DFA.unpackEncodedStringToUnsignedChars(DFA119_minS);
    static final char[] DFA119_max = DFA.unpackEncodedStringToUnsignedChars(DFA119_maxS);
    static final short[] DFA119_accept = DFA.unpackEncodedString(DFA119_acceptS);
    static final short[] DFA119_special = DFA.unpackEncodedString(DFA119_specialS);
    static final short[][] DFA119_transition;

    static {
        int numStates = DFA119_transitionS.length;
        DFA119_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA119_transition[i] = DFA.unpackEncodedString(DFA119_transitionS[i]);
        }
    }

    class DFA119 extends DFA {

        public DFA119(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 119;
            this.eot = DFA119_eot;
            this.eof = DFA119_eof;
            this.min = DFA119_min;
            this.max = DFA119_max;
            this.accept = DFA119_accept;
            this.special = DFA119_special;
            this.transition = DFA119_transition;
        }
        public String getDescription() {
            return "540:1: unary : ( MINUS u= unary -> ^( NEG $u) | TILDE u= unary -> ^( INV $u) | ( lhs PLUSPLUS )=>a= lhs PLUSPLUS -> ^( POSTINC $a) | ( lhs MINUSMINUS )=>a= lhs MINUSMINUS -> ^( POSTDEC $a) | ( atom -> atom ) | PLUSPLUS a= lhs -> ^( PREINC $a) | MINUSMINUS a= lhs -> ^( PREDEC $a) | AMP lhs -> ^( ADDROF lhs ) | SIZEOF atom -> ^( SIZEOF atom ) | TYPEOF atom -> ^( TYPEOF atom ) );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA119_3 = input.LA(1);

                         
                        int index119_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred21_Eulang()) ) {s = 18;}

                        else if ( (synpred22_Eulang()) ) {s = 19;}

                        else if ( (true) ) {s = 9;}

                         
                        input.seek(index119_3);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA119_4 = input.LA(1);

                         
                        int index119_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred21_Eulang()) ) {s = 18;}

                        else if ( (synpred22_Eulang()) ) {s = 19;}

                        else if ( (true) ) {s = 9;}

                         
                        input.seek(index119_4);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA119_5 = input.LA(1);

                         
                        int index119_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred21_Eulang()) ) {s = 18;}

                        else if ( (synpred22_Eulang()) ) {s = 19;}

                        else if ( (true) ) {s = 9;}

                         
                        input.seek(index119_5);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA119_6 = input.LA(1);

                         
                        int index119_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred21_Eulang()) ) {s = 18;}

                        else if ( (synpred22_Eulang()) ) {s = 19;}

                        else if ( (true) ) {s = 9;}

                         
                        input.seek(index119_6);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA119_7 = input.LA(1);

                         
                        int index119_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred21_Eulang()) ) {s = 18;}

                        else if ( (synpred22_Eulang()) ) {s = 19;}

                        else if ( (true) ) {s = 9;}

                         
                        input.seek(index119_7);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA119_8 = input.LA(1);

                         
                        int index119_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred21_Eulang()) ) {s = 18;}

                        else if ( (synpred22_Eulang()) ) {s = 19;}

                        else if ( (true) ) {s = 9;}

                         
                        input.seek(index119_8);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 119, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA125_eotS =
        "\16\uffff";
    static final String DFA125_eofS =
        "\16\uffff";
    static final String DFA125_minS =
        "\1\11\10\uffff\1\0\4\uffff";
    static final String DFA125_maxS =
        "\1\u0098\10\uffff\1\0\4\uffff";
    static final String DFA125_acceptS =
        "\1\uffff\1\1\1\2\1\3\1\4\1\5\1\6\1\7\2\uffff\1\13\1\10\1\11\1\12";
    static final String DFA125_specialS =
        "\11\uffff\1\0\4\uffff}>";
    static final String[] DFA125_transitionS = {
            "\1\12\73\uffff\1\7\2\uffff\1\7\13\uffff\1\11\2\uffff\1\6\72"+
            "\uffff\1\1\1\4\1\5\1\uffff\1\2\1\3\1\7",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\uffff",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA125_eot = DFA.unpackEncodedString(DFA125_eotS);
    static final short[] DFA125_eof = DFA.unpackEncodedString(DFA125_eofS);
    static final char[] DFA125_min = DFA.unpackEncodedStringToUnsignedChars(DFA125_minS);
    static final char[] DFA125_max = DFA.unpackEncodedStringToUnsignedChars(DFA125_maxS);
    static final short[] DFA125_accept = DFA.unpackEncodedString(DFA125_acceptS);
    static final short[] DFA125_special = DFA.unpackEncodedString(DFA125_specialS);
    static final short[][] DFA125_transition;

    static {
        int numStates = DFA125_transitionS.length;
        DFA125_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA125_transition[i] = DFA.unpackEncodedString(DFA125_transitionS[i]);
        }
    }

    class DFA125 extends DFA {

        public DFA125(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 125;
            this.eot = DFA125_eot;
            this.eof = DFA125_eof;
            this.min = DFA125_min;
            this.max = DFA125_max;
            this.accept = DFA125_accept;
            this.special = DFA125_special;
            this.transition = DFA125_transition;
        }
        public String getDescription() {
            return "575:3: ( NUMBER -> ^( LIT NUMBER ) | FALSE -> ^( LIT FALSE ) | TRUE -> ^( LIT TRUE ) | CHAR_LITERAL -> ^( LIT CHAR_LITERAL ) | STRING_LITERAL -> ^( LIT STRING_LITERAL ) | NIL -> ^( LIT NIL ) | idExpr -> idExpr | ( tuple )=> tuple -> tuple | ( LPAREN varDecl )=> LPAREN a0= varDecl RPAREN -> $a0 | LPAREN a1= assignExpr RPAREN -> $a1 | code -> code )";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA125_9 = input.LA(1);

                         
                        int index125_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred25_Eulang()) ) {s = 11;}

                        else if ( (synpred26_Eulang()) ) {s = 12;}

                        else if ( (true) ) {s = 13;}

                         
                        input.seek(index125_9);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 125, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA131_eotS =
        "\70\uffff";
    static final String DFA131_eofS =
        "\1\2\67\uffff";
    static final String DFA131_minS =
        "\1\44\1\0\66\uffff";
    static final String DFA131_maxS =
        "\1\u0095\1\0\66\uffff";
    static final String DFA131_acceptS =
        "\2\uffff\1\2\64\uffff\1\1";
    static final String DFA131_specialS =
        "\1\uffff\1\0\66\uffff}>";
    static final String[] DFA131_transitionS = {
            "\2\2\35\uffff\1\2\2\uffff\1\2\1\uffff\6\2\1\uffff\2\2\1\uffff"+
            "\1\2\1\uffff\3\2\1\uffff\24\2\1\uffff\1\2\5\uffff\5\2\1\uffff"+
            "\6\2\1\1\22\2\3\uffff\1\2",
            "\1\uffff",
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
            "",
            "",
            "",
            ""
    };

    static final short[] DFA131_eot = DFA.unpackEncodedString(DFA131_eotS);
    static final short[] DFA131_eof = DFA.unpackEncodedString(DFA131_eofS);
    static final char[] DFA131_min = DFA.unpackEncodedStringToUnsignedChars(DFA131_minS);
    static final char[] DFA131_max = DFA.unpackEncodedStringToUnsignedChars(DFA131_maxS);
    static final short[] DFA131_accept = DFA.unpackEncodedString(DFA131_acceptS);
    static final short[] DFA131_special = DFA.unpackEncodedString(DFA131_specialS);
    static final short[][] DFA131_transition;

    static {
        int numStates = DFA131_transitionS.length;
        DFA131_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA131_transition[i] = DFA.unpackEncodedString(DFA131_transitionS[i]);
        }
    }

    class DFA131 extends DFA {

        public DFA131(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 131;
            this.eot = DFA131_eot;
            this.eof = DFA131_eof;
            this.min = DFA131_min;
            this.max = DFA131_max;
            this.accept = DFA131_accept;
            this.special = DFA131_special;
            this.transition = DFA131_transition;
        }
        public String getDescription() {
            return "608:5: ( ( instantiation )=> instantiation -> ^( INSTANCE $idExpr instantiation ) )?";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA131_1 = input.LA(1);

                         
                        int index131_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred28_Eulang()) ) {s = 55;}

                        else if ( (true) ) {s = 2;}

                         
                        input.seek(index131_1);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 131, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA137_eotS =
        "\14\uffff";
    static final String DFA137_eofS =
        "\14\uffff";
    static final String DFA137_minS =
        "\1\11\3\0\1\uffff\1\0\6\uffff";
    static final String DFA137_maxS =
        "\1\u0099\3\0\1\uffff\1\0\6\uffff";
    static final String DFA137_acceptS =
        "\4\uffff\1\1\1\uffff\1\2\5\uffff";
    static final String DFA137_specialS =
        "\1\uffff\1\0\1\1\1\2\1\uffff\1\3\6\uffff}>";
    static final String[] DFA137_transitionS = {
            "\1\3\73\uffff\1\1\2\uffff\1\2\13\uffff\1\5\2\uffff\1\6\72\uffff"+
            "\3\6\1\uffff\2\6\1\2\1\4",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "",
            "\1\uffff",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA137_eot = DFA.unpackEncodedString(DFA137_eotS);
    static final short[] DFA137_eof = DFA.unpackEncodedString(DFA137_eofS);
    static final char[] DFA137_min = DFA.unpackEncodedStringToUnsignedChars(DFA137_minS);
    static final char[] DFA137_max = DFA.unpackEncodedStringToUnsignedChars(DFA137_maxS);
    static final short[] DFA137_accept = DFA.unpackEncodedString(DFA137_acceptS);
    static final short[] DFA137_special = DFA.unpackEncodedString(DFA137_specialS);
    static final short[][] DFA137_transition;

    static {
        int numStates = DFA137_transitionS.length;
        DFA137_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA137_transition[i] = DFA.unpackEncodedString(DFA137_transitionS[i]);
        }
    }

    class DFA137 extends DFA {

        public DFA137(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 137;
            this.eot = DFA137_eot;
            this.eof = DFA137_eof;
            this.min = DFA137_min;
            this.max = DFA137_max;
            this.accept = DFA137_accept;
            this.special = DFA137_special;
            this.transition = DFA137_transition;
        }
        public String getDescription() {
            return "618:1: instanceExpr options {backtrack=true; } : ( type | atom );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA137_1 = input.LA(1);

                         
                        int index137_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred29_Eulang()) ) {s = 4;}

                        else if ( (true) ) {s = 6;}

                         
                        input.seek(index137_1);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA137_2 = input.LA(1);

                         
                        int index137_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred29_Eulang()) ) {s = 4;}

                        else if ( (true) ) {s = 6;}

                         
                        input.seek(index137_2);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA137_3 = input.LA(1);

                         
                        int index137_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred29_Eulang()) ) {s = 4;}

                        else if ( (true) ) {s = 6;}

                         
                        input.seek(index137_3);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA137_5 = input.LA(1);

                         
                        int index137_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred29_Eulang()) ) {s = 4;}

                        else if ( (true) ) {s = 6;}

                         
                        input.seek(index137_5);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 137, _s, input);
            error(nvae);
            throw nvae;
        }
    }
 

    public static final BitSet FOLLOW_toplevelstmts_in_prog440 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_prog442 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_toplevelstat_in_toplevelstmts471 = new BitSet(new long[]{0x6000000000000202L,0x0104000000908130L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_toplevelstatNoAlloc_in_toplevelstmtsNoAlloc498 = new BitSet(new long[]{0x6000000000000202L,0x0104000000908130L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_toplevelstatNoAlloc_in_toplevelstat527 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_toplevelAlloc_in_toplevelstat540 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_SEMI_in_toplevelstat542 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_defineStmt_in_toplevelstatNoAlloc557 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_scopeExtension_in_toplevelstatNoAlloc565 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000008L});
    public static final BitSet FOLLOW_SEMI_in_toplevelstatNoAlloc567 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FORWARD_in_toplevelstatNoAlloc581 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000020L});
    public static final BitSet FOLLOW_ID_in_toplevelstatNoAlloc583 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000048L});
    public static final BitSet FOLLOW_COMMA_in_toplevelstatNoAlloc586 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000020L});
    public static final BitSet FOLLOW_ID_in_toplevelstatNoAlloc588 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000048L});
    public static final BitSet FOLLOW_SEMI_in_toplevelstatNoAlloc592 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rhsExpr_in_toplevelstatNoAlloc609 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_SEMI_in_toplevelstatNoAlloc628 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_xscope_in_toplevelstatNoAlloc651 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000008L});
    public static final BitSet FOLLOW_SEMI_in_toplevelstatNoAlloc653 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_toplevelSingleVarDecl_in_toplevelAlloc668 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_toplevelTupleVarDecl_in_toplevelAlloc672 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_toplevelSingleVarDecl683 = new BitSet(new long[]{0x0000000000000000L,0x00000000000801C0L});
    public static final BitSet FOLLOW_attrs_in_toplevelSingleVarDecl685 = new BitSet(new long[]{0x0000000000000000L,0x00000000000001C0L});
    public static final BitSet FOLLOW_COLON_EQUALS_in_toplevelSingleVarDecl700 = new BitSet(new long[]{0x6000000000000200L,0x0104000000901120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_rhsExprOrInitList_in_toplevelSingleVarDecl702 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COLON_in_toplevelSingleVarDecl739 = new BitSet(new long[]{0x0000000000000200L,0x0000000000100120L,0x0000000003000000L});
    public static final BitSet FOLLOW_type_in_toplevelSingleVarDecl741 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000200L});
    public static final BitSet FOLLOW_EQUALS_in_toplevelSingleVarDecl744 = new BitSet(new long[]{0x6000000000000200L,0x0104000000901120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_rhsExprOrInitList_in_toplevelSingleVarDecl746 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COMMA_in_toplevelSingleVarDecl779 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000020L});
    public static final BitSet FOLLOW_ID_in_toplevelSingleVarDecl781 = new BitSet(new long[]{0x0000000000000000L,0x00000000000001C0L});
    public static final BitSet FOLLOW_COLON_EQUALS_in_toplevelSingleVarDecl800 = new BitSet(new long[]{0x6000000000000200L,0x0104000000901520L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_PLUS_in_toplevelSingleVarDecl802 = new BitSet(new long[]{0x6000000000000200L,0x0104000000901120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_rhsExprOrInitList_in_toplevelSingleVarDecl805 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000040L});
    public static final BitSet FOLLOW_COMMA_in_toplevelSingleVarDecl808 = new BitSet(new long[]{0x6000000000000200L,0x0104000000901120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_rhsExprOrInitList_in_toplevelSingleVarDecl810 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000040L});
    public static final BitSet FOLLOW_COLON_in_toplevelSingleVarDecl873 = new BitSet(new long[]{0x0000000000000200L,0x0000000000100120L,0x0000000003000000L});
    public static final BitSet FOLLOW_type_in_toplevelSingleVarDecl875 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000200L});
    public static final BitSet FOLLOW_EQUALS_in_toplevelSingleVarDecl878 = new BitSet(new long[]{0x6000000000000200L,0x0104000000901520L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_PLUS_in_toplevelSingleVarDecl880 = new BitSet(new long[]{0x6000000000000200L,0x0104000000901120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_rhsExprOrInitList_in_toplevelSingleVarDecl883 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000040L});
    public static final BitSet FOLLOW_COMMA_in_toplevelSingleVarDecl886 = new BitSet(new long[]{0x6000000000000200L,0x0104000000901120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_rhsExprOrInitList_in_toplevelSingleVarDecl888 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000040L});
    public static final BitSet FOLLOW_idTuple_in_toplevelTupleVarDecl982 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000180L});
    public static final BitSet FOLLOW_COLON_EQUALS_in_toplevelTupleVarDecl1004 = new BitSet(new long[]{0x6000000000000200L,0x0104000000901120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_rhsExprOrInitList_in_toplevelTupleVarDecl1006 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COLON_in_toplevelTupleVarDecl1040 = new BitSet(new long[]{0x0000000000000200L,0x0000000000100120L,0x0000000003000000L});
    public static final BitSet FOLLOW_type_in_toplevelTupleVarDecl1042 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000200L});
    public static final BitSet FOLLOW_EQUALS_in_toplevelTupleVarDecl1045 = new BitSet(new long[]{0x6000000000000200L,0x0104000000901120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_rhsExprOrInitList_in_toplevelTupleVarDecl1047 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rhsExpr_in_rhsExprOrInitList1090 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_initList_in_rhsExprOrInitList1094 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_namespaceRef_in_scopeExtension1103 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000800L});
    public static final BitSet FOLLOW_PLUS_EQ_in_scopeExtension1105 = new BitSet(new long[]{0x0000000000000000L,0x0000000000008000L});
    public static final BitSet FOLLOW_xscopeNoAlloc_in_scopeExtension1107 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_defineStmt1136 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000200L});
    public static final BitSet FOLLOW_EQUALS_in_defineStmt1138 = new BitSet(new long[]{0x0000000000000000L,0x0000000000001000L});
    public static final BitSet FOLLOW_LBRACKET_in_defineStmt1140 = new BitSet(new long[]{0x0000000000000000L,0x0000000000002020L});
    public static final BitSet FOLLOW_idlistOrEmpty_in_defineStmt1142 = new BitSet(new long[]{0x0000000000000000L,0x0000000000002000L});
    public static final BitSet FOLLOW_RBRACKET_in_defineStmt1144 = new BitSet(new long[]{0x6000000000000200L,0x0104000000909130L,0x0000000003DF0830L});
    public static final BitSet FOLLOW_toplevelvalue_in_defineStmt1147 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_SEMI_in_defineStmt1153 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_defineStmt1183 = new BitSet(new long[]{0x0000000000000000L,0x0000000000004000L});
    public static final BitSet FOLLOW_EQUALS_COLON_in_defineStmt1185 = new BitSet(new long[]{0x0000000000000200L,0x0000000000100120L,0x0000000003000000L});
    public static final BitSet FOLLOW_type_in_defineStmt1187 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_SEMI_in_defineStmt1193 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_defineStmt1220 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000200L});
    public static final BitSet FOLLOW_EQUALS_in_defineStmt1222 = new BitSet(new long[]{0x6000000000000200L,0x0104000000909130L,0x0000000003DF0830L});
    public static final BitSet FOLLOW_toplevelvalue_in_defineStmt1224 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_SEMI_in_defineStmt1230 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_xscope_in_toplevelvalue1258 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_namespaceRef_in_toplevelvalue1266 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000400L});
    public static final BitSet FOLLOW_PLUS_in_toplevelvalue1268 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000002000000L});
    public static final BitSet FOLLOW_data_in_toplevelvalue1270 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_namespaceRef_in_toplevelvalue1288 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000400L});
    public static final BitSet FOLLOW_PLUS_in_toplevelvalue1290 = new BitSet(new long[]{0x6000000000000200L,0x0104000000908130L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_xscope_in_toplevelvalue1292 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_selector_in_toplevelvalue1310 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rhsExpr_in_toplevelvalue1318 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_data_in_toplevelvalue1326 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACKET_in_selector1339 = new BitSet(new long[]{0x6000000000000200L,0x0104000000902120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_selectors_in_selector1341 = new BitSet(new long[]{0x0000000000000000L,0x0000000000002000L});
    public static final BitSet FOLLOW_RBRACKET_in_selector1343 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_selectoritem_in_selectors1369 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000040L});
    public static final BitSet FOLLOW_COMMA_in_selectors1373 = new BitSet(new long[]{0x6000000000000200L,0x0104000000900120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_selectoritem_in_selectors1375 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000040L});
    public static final BitSet FOLLOW_COMMA_in_selectors1380 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rhsExpr_in_selectoritem1411 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACE_in_xscope1422 = new BitSet(new long[]{0x6000000000000200L,0x0104000000908130L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_toplevelstmts_in_xscope1424 = new BitSet(new long[]{0x0000000000000000L,0x0000000000010000L});
    public static final BitSet FOLLOW_RBRACE_in_xscope1426 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACE_in_xscopeNoAlloc1451 = new BitSet(new long[]{0x6000000000000200L,0x0104000000918130L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_toplevelstmtsNoAlloc_in_xscopeNoAlloc1453 = new BitSet(new long[]{0x0000000000000000L,0x0000000000010000L});
    public static final BitSet FOLLOW_RBRACE_in_xscopeNoAlloc1455 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_forIn_in_listCompr1482 = new BitSet(new long[]{0x0000000000000000L,0x0000000000020100L});
    public static final BitSet FOLLOW_COLON_in_listCompr1485 = new BitSet(new long[]{0x0000000000000200L,0x0000000000900120L,0x0000000001DC0000L});
    public static final BitSet FOLLOW_listiterable_in_listCompr1487 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FOR_in_forIn1519 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000020L});
    public static final BitSet FOLLOW_idlist_in_forIn1521 = new BitSet(new long[]{0x0000000000000000L,0x0000000000040000L});
    public static final BitSet FOLLOW_IN_in_forIn1523 = new BitSet(new long[]{0x0000000000000000L,0x0000000000001000L});
    public static final BitSet FOLLOW_list_in_forIn1525 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_idlist1550 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000040L});
    public static final BitSet FOLLOW_COMMA_in_idlist1553 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000020L});
    public static final BitSet FOLLOW_ID_in_idlist1555 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000040L});
    public static final BitSet FOLLOW_idlist_in_idlistOrEmpty1581 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_code_in_listiterable1602 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACKET_in_list1616 = new BitSet(new long[]{0x6000000000000200L,0x010400000090B130L,0x0000000003DF0830L});
    public static final BitSet FOLLOW_listitems_in_list1618 = new BitSet(new long[]{0x0000000000000000L,0x0000000000002000L});
    public static final BitSet FOLLOW_RBRACKET_in_list1620 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_listitem_in_listitems1650 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000040L});
    public static final BitSet FOLLOW_COMMA_in_listitems1654 = new BitSet(new long[]{0x6000000000000200L,0x0104000000909130L,0x0000000003DF0830L});
    public static final BitSet FOLLOW_listitem_in_listitems1656 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000040L});
    public static final BitSet FOLLOW_COMMA_in_listitems1661 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_toplevelvalue_in_listitem1687 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CODE_in_code1705 = new BitSet(new long[]{0x0000000000000000L,0x0000000000188000L});
    public static final BitSet FOLLOW_attrs_in_code1707 = new BitSet(new long[]{0x0000000000000000L,0x0000000000108000L});
    public static final BitSet FOLLOW_proto_in_code1710 = new BitSet(new long[]{0x0000000000000000L,0x0000000000008000L});
    public static final BitSet FOLLOW_LBRACE_in_code1713 = new BitSet(new long[]{0x6001000000000200L,0x01061C00009B83B0L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_codestmtlist_in_code1715 = new BitSet(new long[]{0x0000000000000000L,0x0000000000010000L});
    public static final BitSet FOLLOW_RBRACE_in_code1717 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ATTR_in_attrs1747 = new BitSet(new long[]{0x0000000000000002L,0x0000000000080000L});
    public static final BitSet FOLLOW_argdefsWithTypes_in_argdefs1791 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_argdefWithType_in_argdefs1798 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_argdefsWithNames_in_argdefs1805 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_argdefWithType_in_argdefsWithTypes1821 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_SEMI_in_argdefsWithTypes1825 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000020L});
    public static final BitSet FOLLOW_argdefWithType_in_argdefsWithTypes1827 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000008L});
    public static final BitSet FOLLOW_SEMI_in_argdefsWithTypes1831 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_argdefWithType1860 = new BitSet(new long[]{0x0000000000000002L,0x0000000000080340L});
    public static final BitSet FOLLOW_COMMA_in_argdefWithType1863 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000020L});
    public static final BitSet FOLLOW_ID_in_argdefWithType1865 = new BitSet(new long[]{0x0000000000000002L,0x0000000000080340L});
    public static final BitSet FOLLOW_attrs_in_argdefWithType1869 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000300L});
    public static final BitSet FOLLOW_COLON_in_argdefWithType1873 = new BitSet(new long[]{0x0000000000000200L,0x0000000000100120L,0x0000000003000000L});
    public static final BitSet FOLLOW_type_in_argdefWithType1875 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000200L});
    public static final BitSet FOLLOW_EQUALS_in_argdefWithType1880 = new BitSet(new long[]{0x6000000000000200L,0x0104000000900120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_rhsExpr_in_argdefWithType1884 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_argdefWithName_in_argdefsWithNames1919 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
    public static final BitSet FOLLOW_COMMA_in_argdefsWithNames1923 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000020L});
    public static final BitSet FOLLOW_argdefWithName_in_argdefsWithNames1925 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000040L});
    public static final BitSet FOLLOW_COMMA_in_argdefsWithNames1929 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_argdefWithName1951 = new BitSet(new long[]{0x0000000000000002L,0x0000000000080000L});
    public static final BitSet FOLLOW_attrs_in_argdefWithName1953 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_proto1978 = new BitSet(new long[]{0x0000000000000000L,0x0000000000600020L});
    public static final BitSet FOLLOW_argdefs_in_proto1980 = new BitSet(new long[]{0x0000000000000000L,0x0000000000600000L});
    public static final BitSet FOLLOW_xreturns_in_proto1982 = new BitSet(new long[]{0x0000000000000000L,0x0000000000200000L});
    public static final BitSet FOLLOW_RPAREN_in_proto1985 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ARROW_in_xreturns2028 = new BitSet(new long[]{0x0000000000000200L,0x0000000000100120L,0x0000000003000000L});
    public static final BitSet FOLLOW_type_in_xreturns2030 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ARROW_in_xreturns2047 = new BitSet(new long[]{0x0000000000000000L,0x0000000000800000L});
    public static final BitSet FOLLOW_NIL_in_xreturns2049 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_argtuple2079 = new BitSet(new long[]{0x0000000000000200L,0x0000000001100160L,0x0000000003000000L});
    public static final BitSet FOLLOW_tupleargdefs_in_argtuple2081 = new BitSet(new long[]{0x0000000000000000L,0x0000000000200000L});
    public static final BitSet FOLLOW_RPAREN_in_argtuple2083 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_tupleargdef_in_tupleargdefs2105 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
    public static final BitSet FOLLOW_COMMA_in_tupleargdefs2109 = new BitSet(new long[]{0x0000000000000200L,0x0000000001100160L,0x0000000003000000L});
    public static final BitSet FOLLOW_tupleargdef_in_tupleargdefs2111 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000040L});
    public static final BitSet FOLLOW_type_in_tupleargdef2156 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_QUESTION_in_tupleargdef2169 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_nonArrayType_in_type2234 = new BitSet(new long[]{0x0000000000000002L,0x0000000002001000L});
    public static final BitSet FOLLOW_arraySuff_in_type2272 = new BitSet(new long[]{0x0000000000000002L,0x0000000002001000L});
    public static final BitSet FOLLOW_LBRACKET_in_type2327 = new BitSet(new long[]{0x6000000000000200L,0x0104000000900120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_rhsExpr_in_type2329 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
    public static final BitSet FOLLOW_COMMA_in_type2333 = new BitSet(new long[]{0x6000000000000200L,0x0104000000900120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_rhsExpr_in_type2335 = new BitSet(new long[]{0x0000000000000000L,0x0000000000002040L});
    public static final BitSet FOLLOW_RBRACKET_in_type2340 = new BitSet(new long[]{0x0000000000000002L,0x0000000002001000L});
    public static final BitSet FOLLOW_CARET_in_type2399 = new BitSet(new long[]{0x0000000000000002L,0x0000000002001000L});
    public static final BitSet FOLLOW_idExpr_in_nonArrayType2451 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CODE_in_nonArrayType2469 = new BitSet(new long[]{0x0000000000000002L,0x0000000000100000L});
    public static final BitSet FOLLOW_proto_in_nonArrayType2471 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_data_in_nonArrayType2494 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_argtuple_in_nonArrayType2510 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACKET_in_arraySuff2526 = new BitSet(new long[]{0x6000000000000200L,0x0104000000900120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_rhsExpr_in_arraySuff2528 = new BitSet(new long[]{0x0000000000000000L,0x0000000000002000L});
    public static final BitSet FOLLOW_RBRACKET_in_arraySuff2530 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACKET_in_arraySuff2542 = new BitSet(new long[]{0x0000000000000000L,0x0000000000002000L});
    public static final BitSet FOLLOW_RBRACKET_in_arraySuff2544 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_codeStmt_in_codestmtlist2560 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000008L});
    public static final BitSet FOLLOW_SEMI_in_codestmtlist2563 = new BitSet(new long[]{0x6001000000000202L,0x01061C00009A83B8L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_codeStmt_in_codestmtlist2565 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000008L});
    public static final BitSet FOLLOW_labelStmt_in_codeStmt2609 = new BitSet(new long[]{0x6001000000000200L,0x01061C00009A83B0L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_codeStmtExpr_in_codeStmt2611 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_codeStmtExpr_in_codeStmt2632 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_varDecl_in_codeStmtExpr2664 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_assignStmt_in_codeStmtExpr2687 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rhsExpr_in_codeStmtExpr2704 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_blockStmt_in_codeStmtExpr2737 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_gotoStmt_in_codeStmtExpr2759 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_controlStmt_in_codeStmtExpr2785 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_singleVarDecl_in_varDecl2808 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_tupleVarDecl_in_varDecl2812 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_singleVarDecl2824 = new BitSet(new long[]{0x0000000000000000L,0x00000000000801C0L});
    public static final BitSet FOLLOW_attrs_in_singleVarDecl2838 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000080L});
    public static final BitSet FOLLOW_COLON_EQUALS_in_singleVarDecl2841 = new BitSet(new long[]{0x6000000000000200L,0x0104000000909330L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_assignOrInitExpr_in_singleVarDecl2843 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_attrs_in_singleVarDecl2880 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000100L});
    public static final BitSet FOLLOW_COLON_in_singleVarDecl2883 = new BitSet(new long[]{0x0000000000000200L,0x0000000000100120L,0x0000000003000000L});
    public static final BitSet FOLLOW_type_in_singleVarDecl2885 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000200L});
    public static final BitSet FOLLOW_EQUALS_in_singleVarDecl2888 = new BitSet(new long[]{0x6000000000000200L,0x0104000000909330L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_assignOrInitExpr_in_singleVarDecl2890 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COMMA_in_singleVarDecl2923 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000020L});
    public static final BitSet FOLLOW_ID_in_singleVarDecl2925 = new BitSet(new long[]{0x0000000000000000L,0x00000000000801C0L});
    public static final BitSet FOLLOW_attrs_in_singleVarDecl2930 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000180L});
    public static final BitSet FOLLOW_COLON_EQUALS_in_singleVarDecl2948 = new BitSet(new long[]{0x6000000000000200L,0x0104000000909730L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_PLUS_in_singleVarDecl2950 = new BitSet(new long[]{0x6000000000000200L,0x0104000000909330L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_assignOrInitExpr_in_singleVarDecl2953 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000040L});
    public static final BitSet FOLLOW_COMMA_in_singleVarDecl2956 = new BitSet(new long[]{0x6000000000000200L,0x0104000000909330L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_assignOrInitExpr_in_singleVarDecl2958 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000040L});
    public static final BitSet FOLLOW_COLON_in_singleVarDecl3020 = new BitSet(new long[]{0x0000000000000200L,0x0000000000100120L,0x0000000003000000L});
    public static final BitSet FOLLOW_type_in_singleVarDecl3022 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000200L});
    public static final BitSet FOLLOW_EQUALS_in_singleVarDecl3025 = new BitSet(new long[]{0x6000000000000200L,0x0104000000909730L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_PLUS_in_singleVarDecl3027 = new BitSet(new long[]{0x6000000000000200L,0x0104000000909330L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_assignOrInitExpr_in_singleVarDecl3030 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000040L});
    public static final BitSet FOLLOW_COMMA_in_singleVarDecl3033 = new BitSet(new long[]{0x6000000000000200L,0x0104000000909330L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_assignOrInitExpr_in_singleVarDecl3035 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000040L});
    public static final BitSet FOLLOW_idTuple_in_tupleVarDecl3124 = new BitSet(new long[]{0x0000000000000000L,0x0000000000080180L});
    public static final BitSet FOLLOW_attrs_in_tupleVarDecl3126 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000180L});
    public static final BitSet FOLLOW_COLON_EQUALS_in_tupleVarDecl3141 = new BitSet(new long[]{0x6000000000000200L,0x0104000000909330L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_assignOrInitExpr_in_tupleVarDecl3143 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COLON_in_tupleVarDecl3180 = new BitSet(new long[]{0x0000000000000200L,0x0000000000100120L,0x0000000003000000L});
    public static final BitSet FOLLOW_type_in_tupleVarDecl3182 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000200L});
    public static final BitSet FOLLOW_EQUALS_in_tupleVarDecl3185 = new BitSet(new long[]{0x6000000000000200L,0x0104000000909330L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_assignOrInitExpr_in_tupleVarDecl3187 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_lhs_in_assignStmt3238 = new BitSet(new long[]{0x0000000000000000L,0x000001FFFC000A00L});
    public static final BitSet FOLLOW_assignEqOp_in_assignStmt3240 = new BitSet(new long[]{0x6000000000000200L,0x0104000000909330L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_assignOrInitExpr_in_assignStmt3242 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_idTuple_in_assignStmt3269 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000200L});
    public static final BitSet FOLLOW_EQUALS_in_assignStmt3271 = new BitSet(new long[]{0x6000000000000200L,0x0104000000909330L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_assignOrInitExpr_in_assignStmt3273 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_lhs_in_assignStmt3328 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
    public static final BitSet FOLLOW_COMMA_in_assignStmt3331 = new BitSet(new long[]{0x0000000000000000L,0x0000000000100120L,0x00000000011C0000L});
    public static final BitSet FOLLOW_lhs_in_assignStmt3333 = new BitSet(new long[]{0x0000000000000000L,0x000001FFFC000A40L});
    public static final BitSet FOLLOW_assignEqOp_in_assignStmt3337 = new BitSet(new long[]{0x6000000000000200L,0x0104000000909730L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_PLUS_in_assignStmt3339 = new BitSet(new long[]{0x6000000000000200L,0x0104000000909330L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_assignOrInitExpr_in_assignStmt3342 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000040L});
    public static final BitSet FOLLOW_COMMA_in_assignStmt3345 = new BitSet(new long[]{0x6000000000000200L,0x0104000000909330L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_assignOrInitExpr_in_assignStmt3347 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000040L});
    public static final BitSet FOLLOW_assignExpr_in_assignOrInitExpr3408 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_initList_in_assignOrInitExpr3412 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_lhs_in_assignExpr3430 = new BitSet(new long[]{0x0000000000000000L,0x000001FFFC000A00L});
    public static final BitSet FOLLOW_assignEqOp_in_assignExpr3432 = new BitSet(new long[]{0x6000000000000200L,0x0104000000908330L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_assignExpr_in_assignExpr3434 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_idTuple_in_assignExpr3469 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000200L});
    public static final BitSet FOLLOW_EQUALS_in_assignExpr3471 = new BitSet(new long[]{0x6000000000000200L,0x0104000000908330L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_assignExpr_in_assignExpr3473 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rhsExpr_in_assignExpr3507 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_assignOp0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_EQUALS_in_assignEqOp3622 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_assignOp_in_assignEqOp3626 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACKET_in_initList3635 = new BitSet(new long[]{0x6000000000000200L,0x0104020000903120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_initExpr_in_initList3638 = new BitSet(new long[]{0x0000000000000000L,0x0000000000002040L});
    public static final BitSet FOLLOW_COMMA_in_initList3641 = new BitSet(new long[]{0x6000000000000200L,0x0104020000901120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_initExpr_in_initList3643 = new BitSet(new long[]{0x0000000000000000L,0x0000000000002040L});
    public static final BitSet FOLLOW_RBRACKET_in_initList3649 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rhsExpr_in_initExpr3684 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PERIOD_in_initExpr3747 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000020L});
    public static final BitSet FOLLOW_ID_in_initExpr3749 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000200L});
    public static final BitSet FOLLOW_EQUALS_in_initExpr3751 = new BitSet(new long[]{0x6000000000000200L,0x0104000000901120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_initElement_in_initExpr3755 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACKET_in_initExpr3820 = new BitSet(new long[]{0x6000000000000200L,0x0104000000900120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_rhsExpr_in_initExpr3824 = new BitSet(new long[]{0x0000000000000000L,0x0000000000002000L});
    public static final BitSet FOLLOW_RBRACKET_in_initExpr3826 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000200L});
    public static final BitSet FOLLOW_EQUALS_in_initExpr3828 = new BitSet(new long[]{0x6000000000000200L,0x0104000000901120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_initElement_in_initExpr3832 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_initList_in_initExpr3869 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rhsExpr_in_initElement3883 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_initList_in_initElement3887 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_doWhile_in_controlStmt3899 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_whileDo_in_controlStmt3903 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_repeat_in_controlStmt3907 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_forIter_in_controlStmt3911 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DO_in_doWhile3920 = new BitSet(new long[]{0x6001000000000200L,0x01061C00009A83B0L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_codeStmtExpr_in_doWhile3922 = new BitSet(new long[]{0x0000000000000000L,0x0000080000000000L});
    public static final BitSet FOLLOW_WHILE_in_doWhile3924 = new BitSet(new long[]{0x6000000000000200L,0x0104000000900120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_rhsExpr_in_doWhile3926 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_WHILE_in_whileDo3949 = new BitSet(new long[]{0x6000000000000200L,0x0104000000900120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_rhsExpr_in_whileDo3951 = new BitSet(new long[]{0x0000000000000000L,0x0000040000000100L});
    public static final BitSet FOLLOW_COLON_in_whileDo3954 = new BitSet(new long[]{0x6001000000000200L,0x01061C00009A83B0L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_DO_in_whileDo3956 = new BitSet(new long[]{0x6001000000000200L,0x01061C00009A83B0L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_codeStmtExpr_in_whileDo3959 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_REPEAT_in_repeat3984 = new BitSet(new long[]{0x6000000000000200L,0x0104000000900120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_rhsExpr_in_repeat3986 = new BitSet(new long[]{0x0000000000000000L,0x0000040000000100L});
    public static final BitSet FOLLOW_COLON_in_repeat3989 = new BitSet(new long[]{0x6001000000000200L,0x01061C00009A83B0L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_DO_in_repeat3991 = new BitSet(new long[]{0x6001000000000200L,0x01061C00009A83B0L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_codeStmt_in_repeat3994 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FOR_in_forIter4024 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000020L});
    public static final BitSet FOLLOW_forIds_in_forIter4026 = new BitSet(new long[]{0x0000000000000000L,0x0000C00000040000L});
    public static final BitSet FOLLOW_forMovement_in_forIter4028 = new BitSet(new long[]{0x0000000000000000L,0x0000000000040000L});
    public static final BitSet FOLLOW_IN_in_forIter4031 = new BitSet(new long[]{0x6000000000000200L,0x0104000000900120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_rhsExpr_in_forIter4033 = new BitSet(new long[]{0x0000000000000000L,0x0000040000000100L});
    public static final BitSet FOLLOW_COLON_in_forIter4036 = new BitSet(new long[]{0x6001000000000200L,0x01061C00009A83B0L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_DO_in_forIter4038 = new BitSet(new long[]{0x6001000000000200L,0x01061C00009A83B0L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_codeStmt_in_forIter4041 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_forIds4078 = new BitSet(new long[]{0x0000000000000002L,0x0000200000000000L});
    public static final BitSet FOLLOW_AND_in_forIds4081 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000020L});
    public static final BitSet FOLLOW_ID_in_forIds4083 = new BitSet(new long[]{0x0000000000000002L,0x0000200000000000L});
    public static final BitSet FOLLOW_atId_in_forMovement4099 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_stepping_in_forMovement4103 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_BY_in_stepping4112 = new BitSet(new long[]{0x6000000000000200L,0x0104000000900120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_rhsExpr_in_stepping4114 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_AT_in_atId4131 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000020L});
    public static final BitSet FOLLOW_ID_in_atId4133 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_BREAK_in_breakStmt4161 = new BitSet(new long[]{0x6000000000000200L,0x0104000000900120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_rhsExpr_in_breakStmt4163 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ATSIGN_in_labelStmt4191 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000020L});
    public static final BitSet FOLLOW_ID_in_labelStmt4193 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000100L});
    public static final BitSet FOLLOW_COLON_in_labelStmt4195 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_GOTO_in_gotoStmt4231 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000120L,0x0000000001000000L});
    public static final BitSet FOLLOW_idOrScopeRef_in_gotoStmt4233 = new BitSet(new long[]{0x0000000000000002L,0x0004000000000000L});
    public static final BitSet FOLLOW_IF_in_gotoStmt4236 = new BitSet(new long[]{0x6000000000000200L,0x0104000000900120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_rhsExpr_in_gotoStmt4238 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACE_in_blockStmt4273 = new BitSet(new long[]{0x6001000000000200L,0x01061C00009B83B0L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_codestmtlist_in_blockStmt4275 = new BitSet(new long[]{0x0000000000000000L,0x0000000000010000L});
    public static final BitSet FOLLOW_RBRACE_in_blockStmt4277 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_tuple4300 = new BitSet(new long[]{0x6000000000000200L,0x0104000000908330L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_tupleEntries_in_tuple4302 = new BitSet(new long[]{0x0000000000000000L,0x0000000000200000L});
    public static final BitSet FOLLOW_RPAREN_in_tuple4304 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_assignExpr_in_tupleEntries4332 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
    public static final BitSet FOLLOW_COMMA_in_tupleEntries4335 = new BitSet(new long[]{0x6000000000000200L,0x0104000000908330L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_assignExpr_in_tupleEntries4337 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000040L});
    public static final BitSet FOLLOW_LPAREN_in_idTuple4356 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000120L,0x0000000001000000L});
    public static final BitSet FOLLOW_idTupleEntries_in_idTuple4358 = new BitSet(new long[]{0x0000000000000000L,0x0000000000200000L});
    public static final BitSet FOLLOW_RPAREN_in_idTuple4360 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_idOrScopeRef_in_idTupleEntries4388 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
    public static final BitSet FOLLOW_COMMA_in_idTupleEntries4391 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000120L,0x0000000001000000L});
    public static final BitSet FOLLOW_idOrScopeRef_in_idTupleEntries4393 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000040L});
    public static final BitSet FOLLOW_condStar_in_rhsExpr4414 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_arg_in_arglist4435 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000040L});
    public static final BitSet FOLLOW_COMMA_in_arglist4439 = new BitSet(new long[]{0x6001000000000200L,0x0104000000908330L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_arg_in_arglist4441 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000040L});
    public static final BitSet FOLLOW_COMMA_in_arglist4445 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_assignExpr_in_arg4494 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACE_in_arg4527 = new BitSet(new long[]{0x6001000000000200L,0x01061C00009B83B0L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_codestmtlist_in_arg4529 = new BitSet(new long[]{0x0000000000000000L,0x0000000000010000L});
    public static final BitSet FOLLOW_RBRACE_in_arg4531 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_gotoStmt_in_arg4555 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cond_in_condStar4616 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IF_in_condStar4627 = new BitSet(new long[]{0x6000000000000200L,0x0104000000908330L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_ifExprs_in_condStar4629 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_thenClause_in_ifExprs4648 = new BitSet(new long[]{0x0000000000000000L,0x0070000000000000L});
    public static final BitSet FOLLOW_elses_in_ifExprs4650 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_assignExpr_in_thenClause4672 = new BitSet(new long[]{0x0000000000000000L,0x0008000000000100L});
    public static final BitSet FOLLOW_THEN_in_thenClause4678 = new BitSet(new long[]{0x6001000000000200L,0x0105000000908330L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_COLON_in_thenClause4680 = new BitSet(new long[]{0x6001000000000200L,0x0105000000908330L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_condStmtExpr_in_thenClause4685 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_elif_in_elses4713 = new BitSet(new long[]{0x0000000000000000L,0x0070000000000000L});
    public static final BitSet FOLLOW_elseClause_in_elses4716 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ELIF_in_elif4739 = new BitSet(new long[]{0x6000000000000200L,0x0104000000908330L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_assignExpr_in_elif4743 = new BitSet(new long[]{0x0000000000000000L,0x0008000000000100L});
    public static final BitSet FOLLOW_THEN_in_elif4746 = new BitSet(new long[]{0x6001000000000200L,0x0105000000908330L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_COLON_in_elif4748 = new BitSet(new long[]{0x6001000000000200L,0x0105000000908330L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_condStmtExpr_in_elif4753 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ELSE_in_elseClause4779 = new BitSet(new long[]{0x6001000000000200L,0x0105000000908330L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_condStmtExpr_in_elseClause4781 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FI_in_elseClause4808 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_arg_in_condStmtExpr4839 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_breakStmt_in_condStmtExpr4843 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_logor_in_cond4860 = new BitSet(new long[]{0x0000000000000002L,0x0000000001000000L});
    public static final BitSet FOLLOW_QUESTION_in_cond4877 = new BitSet(new long[]{0x6000000000000200L,0x0100000000900120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_logor_in_cond4881 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000100L});
    public static final BitSet FOLLOW_COLON_in_cond4883 = new BitSet(new long[]{0x6000000000000200L,0x0100000000900120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_logor_in_cond4887 = new BitSet(new long[]{0x0000000000000002L,0x0000000001000000L});
    public static final BitSet FOLLOW_logand_in_logor4931 = new BitSet(new long[]{0x0000000000000002L,0x0080000000000000L});
    public static final BitSet FOLLOW_OR_in_logor4948 = new BitSet(new long[]{0x6000000000000200L,0x0100000000900120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_logand_in_logor4952 = new BitSet(new long[]{0x0000000000000002L,0x0080000000000000L});
    public static final BitSet FOLLOW_not_in_logand4983 = new BitSet(new long[]{0x0000000000000002L,0x0000200000000000L});
    public static final BitSet FOLLOW_AND_in_logand4999 = new BitSet(new long[]{0x6000000000000200L,0x0100000000900120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_not_in_logand5003 = new BitSet(new long[]{0x0000000000000002L,0x0000200000000000L});
    public static final BitSet FOLLOW_comp_in_not5049 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NOT_in_not5065 = new BitSet(new long[]{0x6000000000000200L,0x0000000000900120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_comp_in_not5069 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_bitor_in_comp5103 = new BitSet(new long[]{0x0000000000000002L,0xFE00000000000000L,0x0000000000000007L});
    public static final BitSet FOLLOW_COMPEQ_in_comp5136 = new BitSet(new long[]{0x6000000000000200L,0x0000000000900120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_bitor_in_comp5140 = new BitSet(new long[]{0x0000000000000002L,0xFE00000000000000L,0x0000000000000007L});
    public static final BitSet FOLLOW_COMPNE_in_comp5162 = new BitSet(new long[]{0x6000000000000200L,0x0000000000900120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_bitor_in_comp5166 = new BitSet(new long[]{0x0000000000000002L,0xFE00000000000000L,0x0000000000000007L});
    public static final BitSet FOLLOW_COMPLE_in_comp5188 = new BitSet(new long[]{0x6000000000000200L,0x0000000000900120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_bitor_in_comp5192 = new BitSet(new long[]{0x0000000000000002L,0xFE00000000000000L,0x0000000000000007L});
    public static final BitSet FOLLOW_COMPGE_in_comp5217 = new BitSet(new long[]{0x6000000000000200L,0x0000000000900120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_bitor_in_comp5221 = new BitSet(new long[]{0x0000000000000002L,0xFE00000000000000L,0x0000000000000007L});
    public static final BitSet FOLLOW_COMPULE_in_comp5246 = new BitSet(new long[]{0x6000000000000200L,0x0000000000900120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_bitor_in_comp5250 = new BitSet(new long[]{0x0000000000000002L,0xFE00000000000000L,0x0000000000000007L});
    public static final BitSet FOLLOW_COMPUGE_in_comp5275 = new BitSet(new long[]{0x6000000000000200L,0x0000000000900120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_bitor_in_comp5279 = new BitSet(new long[]{0x0000000000000002L,0xFE00000000000000L,0x0000000000000007L});
    public static final BitSet FOLLOW_LESS_in_comp5304 = new BitSet(new long[]{0x6000000000000200L,0x0000000000900120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_bitor_in_comp5308 = new BitSet(new long[]{0x0000000000000002L,0xFE00000000000000L,0x0000000000000007L});
    public static final BitSet FOLLOW_ULESS_in_comp5334 = new BitSet(new long[]{0x6000000000000200L,0x0000000000900120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_bitor_in_comp5338 = new BitSet(new long[]{0x0000000000000002L,0xFE00000000000000L,0x0000000000000007L});
    public static final BitSet FOLLOW_GREATER_in_comp5364 = new BitSet(new long[]{0x6000000000000200L,0x0000000000900120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_bitor_in_comp5368 = new BitSet(new long[]{0x0000000000000002L,0xFE00000000000000L,0x0000000000000007L});
    public static final BitSet FOLLOW_UGREATER_in_comp5393 = new BitSet(new long[]{0x6000000000000200L,0x0000000000900120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_bitor_in_comp5397 = new BitSet(new long[]{0x0000000000000002L,0xFE00000000000000L,0x0000000000000007L});
    public static final BitSet FOLLOW_bitxor_in_bitor5447 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_BAR_in_bitor5475 = new BitSet(new long[]{0x6000000000000200L,0x0000000000900120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_bitxor_in_bitor5479 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_bitand_in_bitxor5505 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_TILDE_in_bitxor5533 = new BitSet(new long[]{0x6000000000000200L,0x0000000000900120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_bitand_in_bitxor5537 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_shift_in_bitand5562 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000000000000020L});
    public static final BitSet FOLLOW_AMP_in_bitand5590 = new BitSet(new long[]{0x6000000000000200L,0x0000000000900120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_shift_in_bitand5594 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000000000000020L});
    public static final BitSet FOLLOW_factor_in_shift5621 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x00000000000007C0L});
    public static final BitSet FOLLOW_LSHIFT_in_shift5655 = new BitSet(new long[]{0x6000000000000200L,0x0000000000900120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_factor_in_shift5659 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x00000000000007C0L});
    public static final BitSet FOLLOW_RSHIFT_in_shift5688 = new BitSet(new long[]{0x6000000000000200L,0x0000000000900120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_factor_in_shift5692 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x00000000000007C0L});
    public static final BitSet FOLLOW_URSHIFT_in_shift5720 = new BitSet(new long[]{0x6000000000000200L,0x0000000000900120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_factor_in_shift5724 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x00000000000007C0L});
    public static final BitSet FOLLOW_CRSHIFT_in_shift5752 = new BitSet(new long[]{0x6000000000000200L,0x0000000000900120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_factor_in_shift5756 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x00000000000007C0L});
    public static final BitSet FOLLOW_CLSHIFT_in_shift5784 = new BitSet(new long[]{0x6000000000000200L,0x0000000000900120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_factor_in_shift5788 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x00000000000007C0L});
    public static final BitSet FOLLOW_term_in_factor5830 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000400L,0x0000000000000800L});
    public static final BitSet FOLLOW_PLUS_in_factor5863 = new BitSet(new long[]{0x6000000000000200L,0x0000000000900120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_term_in_factor5867 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000400L,0x0000000000000800L});
    public static final BitSet FOLLOW_MINUS_in_factor5909 = new BitSet(new long[]{0x6000000000000200L,0x0000000000900120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_term_in_factor5913 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000400L,0x0000000000000800L});
    public static final BitSet FOLLOW_unary_in_term5958 = new BitSet(new long[]{0x0000003000000002L,0x0000000000000000L,0x000000000000F000L});
    public static final BitSet FOLLOW_STAR_in_term6002 = new BitSet(new long[]{0x6000000000000200L,0x0000000000900120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_unary_in_term6006 = new BitSet(new long[]{0x0000003000000002L,0x0000000000000000L,0x000000000000F000L});
    public static final BitSet FOLLOW_SLASH_in_term6042 = new BitSet(new long[]{0x6000000000000200L,0x0000000000900120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_unary_in_term6046 = new BitSet(new long[]{0x0000003000000002L,0x0000000000000000L,0x000000000000F000L});
    public static final BitSet FOLLOW_REM_in_term6081 = new BitSet(new long[]{0x6000000000000200L,0x0000000000900120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_unary_in_term6085 = new BitSet(new long[]{0x0000003000000002L,0x0000000000000000L,0x000000000000F000L});
    public static final BitSet FOLLOW_UDIV_in_term6120 = new BitSet(new long[]{0x6000000000000200L,0x0000000000900120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_unary_in_term6124 = new BitSet(new long[]{0x0000003000000002L,0x0000000000000000L,0x000000000000F000L});
    public static final BitSet FOLLOW_UREM_in_term6159 = new BitSet(new long[]{0x6000000000000200L,0x0000000000900120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_unary_in_term6163 = new BitSet(new long[]{0x0000003000000002L,0x0000000000000000L,0x000000000000F000L});
    public static final BitSet FOLLOW_MOD_in_term6198 = new BitSet(new long[]{0x6000000000000200L,0x0000000000900120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_unary_in_term6202 = new BitSet(new long[]{0x0000003000000002L,0x0000000000000000L,0x000000000000F000L});
    public static final BitSet FOLLOW_MINUS_in_unary6275 = new BitSet(new long[]{0x6000000000000200L,0x0000000000900120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_unary_in_unary6279 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TILDE_in_unary6299 = new BitSet(new long[]{0x6000000000000200L,0x0000000000900120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_unary_in_unary6303 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_lhs_in_unary6338 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000010000L});
    public static final BitSet FOLLOW_PLUSPLUS_in_unary6340 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_lhs_in_unary6371 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000020000L});
    public static final BitSet FOLLOW_MINUSMINUS_in_unary6373 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_atom_in_unary6394 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PLUSPLUS_in_unary6425 = new BitSet(new long[]{0x0000000000000000L,0x0000000000100120L,0x00000000011C0000L});
    public static final BitSet FOLLOW_lhs_in_unary6429 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MINUSMINUS_in_unary6450 = new BitSet(new long[]{0x0000000000000000L,0x0000000000100120L,0x00000000011C0000L});
    public static final BitSet FOLLOW_lhs_in_unary6454 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_AMP_in_unary6474 = new BitSet(new long[]{0x0000000000000000L,0x0000000000100120L,0x00000000011C0000L});
    public static final BitSet FOLLOW_lhs_in_unary6476 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SIZEOF_in_unary6517 = new BitSet(new long[]{0x0000000000000200L,0x0000000000900120L,0x0000000001DC0000L});
    public static final BitSet FOLLOW_atom_in_unary6519 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TYPEOF_in_unary6543 = new BitSet(new long[]{0x0000000000000200L,0x0000000000900120L,0x0000000001DC0000L});
    public static final BitSet FOLLOW_atom_in_unary6545 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_idExpr_in_lhs6580 = new BitSet(new long[]{0x0000000000000002L,0x0000020002109000L,0x0000000000200000L});
    public static final BitSet FOLLOW_tuple_in_lhs6627 = new BitSet(new long[]{0x0000000000000002L,0x0000020002109000L,0x0000000000200000L});
    public static final BitSet FOLLOW_NUMBER_in_lhs6666 = new BitSet(new long[]{0x0000000000000002L,0x0000020002109000L,0x0000000000200000L});
    public static final BitSet FOLLOW_CHAR_LITERAL_in_lhs6709 = new BitSet(new long[]{0x0000000000000002L,0x0000020002109000L,0x0000000000200000L});
    public static final BitSet FOLLOW_STRING_LITERAL_in_lhs6744 = new BitSet(new long[]{0x0000000000000002L,0x0000020002109000L,0x0000000000200000L});
    public static final BitSet FOLLOW_LPAREN_in_lhs6777 = new BitSet(new long[]{0x6000000000000200L,0x0104000000908330L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_assignExpr_in_lhs6781 = new BitSet(new long[]{0x0000000000000000L,0x0000000000200000L});
    public static final BitSet FOLLOW_RPAREN_in_lhs6783 = new BitSet(new long[]{0x0000000000000002L,0x0000020002109000L,0x0000000000200000L});
    public static final BitSet FOLLOW_PERIOD_in_lhs6826 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000020L});
    public static final BitSet FOLLOW_ID_in_lhs6828 = new BitSet(new long[]{0x0000000000000002L,0x0000020002109000L,0x0000000000200000L});
    public static final BitSet FOLLOW_LPAREN_in_lhs6853 = new BitSet(new long[]{0x6001000000000200L,0x0104000000B08330L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_arglist_in_lhs6855 = new BitSet(new long[]{0x0000000000000000L,0x0000000000200000L});
    public static final BitSet FOLLOW_RPAREN_in_lhs6857 = new BitSet(new long[]{0x0000000000000002L,0x0000020002109000L,0x0000000000200000L});
    public static final BitSet FOLLOW_arrayAccess_in_lhs6890 = new BitSet(new long[]{0x0000000000000002L,0x0000020002109000L,0x0000000000200000L});
    public static final BitSet FOLLOW_CARET_in_lhs6915 = new BitSet(new long[]{0x0000000000000002L,0x0000020002109000L,0x0000000000200000L});
    public static final BitSet FOLLOW_LBRACE_in_lhs6936 = new BitSet(new long[]{0x0000000000000200L,0x0000000000100520L,0x0000000003000000L});
    public static final BitSet FOLLOW_PLUS_in_lhs6938 = new BitSet(new long[]{0x0000000000000200L,0x0000000000100120L,0x0000000003000000L});
    public static final BitSet FOLLOW_type_in_lhs6941 = new BitSet(new long[]{0x0000000000000000L,0x0000000000010000L});
    public static final BitSet FOLLOW_RBRACE_in_lhs6943 = new BitSet(new long[]{0x0000000000000002L,0x0000020002109000L,0x0000000000200000L});
    public static final BitSet FOLLOW_AS_in_lhs6984 = new BitSet(new long[]{0x0000000000000200L,0x0000000000100520L,0x0000000003000000L});
    public static final BitSet FOLLOW_PLUS_in_lhs6986 = new BitSet(new long[]{0x0000000000000200L,0x0000000000100120L,0x0000000003000000L});
    public static final BitSet FOLLOW_type_in_lhs6989 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NUMBER_in_atom7038 = new BitSet(new long[]{0x0000000000000002L,0x0000020002109000L,0x0000000000200000L});
    public static final BitSet FOLLOW_FALSE_in_atom7081 = new BitSet(new long[]{0x0000000000000002L,0x0000020002109000L,0x0000000000200000L});
    public static final BitSet FOLLOW_TRUE_in_atom7123 = new BitSet(new long[]{0x0000000000000002L,0x0000020002109000L,0x0000000000200000L});
    public static final BitSet FOLLOW_CHAR_LITERAL_in_atom7166 = new BitSet(new long[]{0x0000000000000002L,0x0000020002109000L,0x0000000000200000L});
    public static final BitSet FOLLOW_STRING_LITERAL_in_atom7201 = new BitSet(new long[]{0x0000000000000002L,0x0000020002109000L,0x0000000000200000L});
    public static final BitSet FOLLOW_NIL_in_atom7234 = new BitSet(new long[]{0x0000000000000002L,0x0000020002109000L,0x0000000000200000L});
    public static final BitSet FOLLOW_idExpr_in_atom7277 = new BitSet(new long[]{0x0000000000000002L,0x0000020002109000L,0x0000000000200000L});
    public static final BitSet FOLLOW_tuple_in_atom7324 = new BitSet(new long[]{0x0000000000000002L,0x0000020002109000L,0x0000000000200000L});
    public static final BitSet FOLLOW_LPAREN_in_atom7373 = new BitSet(new long[]{0x6000000000000200L,0x01040000009881B0L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_varDecl_in_atom7377 = new BitSet(new long[]{0x0000000000000000L,0x0000000000200000L});
    public static final BitSet FOLLOW_RPAREN_in_atom7379 = new BitSet(new long[]{0x0000000000000002L,0x0000020002109000L,0x0000000000200000L});
    public static final BitSet FOLLOW_LPAREN_in_atom7408 = new BitSet(new long[]{0x6000000000000200L,0x0104000000908330L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_assignExpr_in_atom7412 = new BitSet(new long[]{0x0000000000000000L,0x0000000000200000L});
    public static final BitSet FOLLOW_RPAREN_in_atom7414 = new BitSet(new long[]{0x0000000000000002L,0x0000020002109000L,0x0000000000200000L});
    public static final BitSet FOLLOW_code_in_atom7443 = new BitSet(new long[]{0x0000000000000002L,0x0000020002109000L,0x0000000000200000L});
    public static final BitSet FOLLOW_PERIOD_in_atom7502 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000020L});
    public static final BitSet FOLLOW_ID_in_atom7504 = new BitSet(new long[]{0x0000000000000002L,0x0000020002109000L,0x0000000000200000L});
    public static final BitSet FOLLOW_LPAREN_in_atom7529 = new BitSet(new long[]{0x6001000000000200L,0x0104000000B08330L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_arglist_in_atom7531 = new BitSet(new long[]{0x0000000000000000L,0x0000000000200000L});
    public static final BitSet FOLLOW_RPAREN_in_atom7533 = new BitSet(new long[]{0x0000000000000002L,0x0000020002109000L,0x0000000000200000L});
    public static final BitSet FOLLOW_arrayAccess_in_atom7566 = new BitSet(new long[]{0x0000000000000002L,0x0000020002109000L,0x0000000000200000L});
    public static final BitSet FOLLOW_CARET_in_atom7591 = new BitSet(new long[]{0x0000000000000002L,0x0000020002109000L,0x0000000000200000L});
    public static final BitSet FOLLOW_LBRACE_in_atom7612 = new BitSet(new long[]{0x0000000000000200L,0x0000000000100520L,0x0000000003000000L});
    public static final BitSet FOLLOW_PLUS_in_atom7614 = new BitSet(new long[]{0x0000000000000200L,0x0000000000100120L,0x0000000003000000L});
    public static final BitSet FOLLOW_type_in_atom7617 = new BitSet(new long[]{0x0000000000000000L,0x0000000000010000L});
    public static final BitSet FOLLOW_RBRACE_in_atom7619 = new BitSet(new long[]{0x0000000000000002L,0x0000020002109000L,0x0000000000200000L});
    public static final BitSet FOLLOW_AS_in_atom7660 = new BitSet(new long[]{0x0000000000000200L,0x0000000000100120L,0x0000000003000000L});
    public static final BitSet FOLLOW_type_in_atom7662 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACKET_in_arrayAccess7696 = new BitSet(new long[]{0x6000000000000200L,0x0104000000908330L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_assignExpr_in_arrayAccess7698 = new BitSet(new long[]{0x0000000000000000L,0x0000000000002040L});
    public static final BitSet FOLLOW_COMMA_in_arrayAccess7701 = new BitSet(new long[]{0x6000000000000200L,0x0104000000908330L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_assignExpr_in_arrayAccess7703 = new BitSet(new long[]{0x0000000000000000L,0x0000000000002040L});
    public static final BitSet FOLLOW_RBRACKET_in_arrayAccess7707 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_idOrScopeRef_in_idExpr7729 = new BitSet(new long[]{0x0000000000000002L,0x8000020000000000L});
    public static final BitSet FOLLOW_PERIOD_in_idExpr7745 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000020L});
    public static final BitSet FOLLOW_ID_in_idExpr7747 = new BitSet(new long[]{0x0000000000000002L,0x8000020000000000L});
    public static final BitSet FOLLOW_instantiation_in_idExpr7777 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_namespaceRef7808 = new BitSet(new long[]{0x0000000000000002L,0x0000020000000000L});
    public static final BitSet FOLLOW_PERIOD_in_namespaceRef7811 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000020L});
    public static final BitSet FOLLOW_ID_in_namespaceRef7813 = new BitSet(new long[]{0x0000000000000002L,0x0000020000000000L});
    public static final BitSet FOLLOW_colons_in_namespaceRef7837 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000020L});
    public static final BitSet FOLLOW_ID_in_namespaceRef7839 = new BitSet(new long[]{0x0000000000000002L,0x0000020000000000L});
    public static final BitSet FOLLOW_PERIOD_in_namespaceRef7842 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000020L});
    public static final BitSet FOLLOW_ID_in_namespaceRef7844 = new BitSet(new long[]{0x0000000000000002L,0x0000020000000000L});
    public static final BitSet FOLLOW_LESS_in_instantiation7873 = new BitSet(new long[]{0x0000000000000200L,0x0000000000900120L,0x0000000003DC0002L});
    public static final BitSet FOLLOW_instanceExpr_in_instantiation7876 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L,0x0000000000000002L});
    public static final BitSet FOLLOW_COMMA_in_instantiation7879 = new BitSet(new long[]{0x0000000000000200L,0x0000000000900120L,0x0000000003DC0000L});
    public static final BitSet FOLLOW_instanceExpr_in_instantiation7881 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L,0x0000000000000002L});
    public static final BitSet FOLLOW_GREATER_in_instantiation7887 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_type_in_instanceExpr7919 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_atom_in_instanceExpr7923 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_idOrScopeRef7931 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_colons_in_idOrScopeRef7954 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000020L});
    public static final BitSet FOLLOW_ID_in_idOrScopeRef7956 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_colons7982 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000100L,0x0000000001000000L});
    public static final BitSet FOLLOW_DATA_in_data7999 = new BitSet(new long[]{0x0000000000000000L,0x0000000000008000L});
    public static final BitSet FOLLOW_LBRACE_in_data8001 = new BitSet(new long[]{0x6000000000000200L,0x01040000009981B0L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_fieldDecl_in_data8003 = new BitSet(new long[]{0x6000000000000200L,0x01040000009981B0L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_RBRACE_in_data8006 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_varDecl_in_fieldDecl8025 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_SEMI_in_fieldDecl8027 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_defineStmt_in_fieldDecl8040 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FORWARD_in_fieldDecl8048 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000020L});
    public static final BitSet FOLLOW_ID_in_fieldDecl8050 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000048L});
    public static final BitSet FOLLOW_COMMA_in_fieldDecl8053 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000020L});
    public static final BitSet FOLLOW_ID_in_fieldDecl8055 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000048L});
    public static final BitSet FOLLOW_SEMI_in_fieldDecl8059 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACE_in_synpred1_Eulang646 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_synpred2_Eulang1127 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000200L});
    public static final BitSet FOLLOW_EQUALS_in_synpred2_Eulang1129 = new BitSet(new long[]{0x0000000000000000L,0x0000000000001000L});
    public static final BitSet FOLLOW_LBRACKET_in_synpred2_Eulang1131 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_synpred3_Eulang1176 = new BitSet(new long[]{0x0000000000000000L,0x0000000000004000L});
    public static final BitSet FOLLOW_EQUALS_COLON_in_synpred3_Eulang1178 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_synpred4_Eulang1213 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000200L});
    public static final BitSet FOLLOW_EQUALS_in_synpred4_Eulang1215 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACE_in_synpred5_Eulang1253 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_argdefsWithTypes_in_synpred7_Eulang1791 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_argdefWithType_in_synpred8_Eulang1798 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_arraySuff_in_synpred9_Eulang2266 = new BitSet(new long[]{0x0000000000000002L,0x0000000000001000L});
    public static final BitSet FOLLOW_varDecl_in_synpred10_Eulang2659 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_assignStmt_in_synpred11_Eulang2682 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACE_in_synpred12_Eulang2731 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_lhs_in_synpred13_Eulang3231 = new BitSet(new long[]{0x0000000000000000L,0x000001FFFC000A00L});
    public static final BitSet FOLLOW_assignEqOp_in_synpred13_Eulang3233 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_lhs_in_synpred14_Eulang3313 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
    public static final BitSet FOLLOW_COMMA_in_synpred14_Eulang3316 = new BitSet(new long[]{0x0000000000000000L,0x0000000000100120L,0x00000000011C0000L});
    public static final BitSet FOLLOW_lhs_in_synpred14_Eulang3318 = new BitSet(new long[]{0x0000000000000000L,0x000001FFFC000A40L});
    public static final BitSet FOLLOW_assignEqOp_in_synpred14_Eulang3322 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_lhs_in_synpred15_Eulang3423 = new BitSet(new long[]{0x0000000000000000L,0x000001FFFC000A00L});
    public static final BitSet FOLLOW_assignEqOp_in_synpred15_Eulang3425 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_idTuple_in_synpred16_Eulang3462 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000200L});
    public static final BitSet FOLLOW_EQUALS_in_synpred16_Eulang3464 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rhsExpr_in_synpred17_Eulang3677 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACKET_in_synpred18_Eulang3809 = new BitSet(new long[]{0x6000000000000200L,0x0104000000900120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_rhsExpr_in_synpred18_Eulang3813 = new BitSet(new long[]{0x0000000000000000L,0x0000000000002000L});
    public static final BitSet FOLLOW_RBRACKET_in_synpred18_Eulang3815 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MINUS_in_synpred19_Eulang5902 = new BitSet(new long[]{0x6000000000000200L,0x0000000000900120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_term_in_synpred19_Eulang5904 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STAR_in_synpred20_Eulang5995 = new BitSet(new long[]{0x6000000000000200L,0x0000000000900120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_unary_in_synpred20_Eulang5997 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_lhs_in_synpred21_Eulang6329 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000010000L});
    public static final BitSet FOLLOW_PLUSPLUS_in_synpred21_Eulang6331 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_lhs_in_synpred22_Eulang6362 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000020000L});
    public static final BitSet FOLLOW_MINUSMINUS_in_synpred22_Eulang6364 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_tuple_in_synpred23_Eulang6621 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACKET_in_synpred24_Eulang6884 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_tuple_in_synpred25_Eulang7318 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_synpred26_Eulang7365 = new BitSet(new long[]{0x6000000000000200L,0x01040000009881B0L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_varDecl_in_synpred26_Eulang7367 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACKET_in_synpred27_Eulang7560 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_instantiation_in_synpred28_Eulang7771 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_type_in_synpred29_Eulang7919 = new BitSet(new long[]{0x0000000000000002L});

}