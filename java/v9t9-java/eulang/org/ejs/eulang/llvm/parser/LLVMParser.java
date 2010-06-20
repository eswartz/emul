// $ANTLR 3.2 Sep 23, 2009 12:02:23 /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g 2010-06-20 15:19:19

package org.ejs.eulang.llvm.parser;
import org.ejs.eulang.symbols.*;
import org.ejs.eulang.llvm.*;
import org.ejs.eulang.llvm.directives.*;
import org.ejs.eulang.llvm.ops.*;
import org.ejs.eulang.llvm.instrs.*;
import org.ejs.eulang.types.*;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;


import org.antlr.runtime.tree.*;

public class LLVMParser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "NEWLINE", "EQUALS", "INT_TYPE", "NAMED_ID", "UNNAMED_ID", "QUOTED_ID", "NUMBER", "CHAR_LITERAL", "STRING_LITERAL", "CSTRING_LITERAL", "DEFINE", "LABEL", "NUMSUFFIX", "SYM_PFX", "NUMBER_SUFFIX", "NAME_SUFFIX", "WS", "SINGLE_COMMENT", "MULTI_COMMENT", "'target'", "'datalayout'", "'triple'", "'type'", "'void'", "'label'", "'*'", "'{'", "'}'", "'['", "'x'", "']'", "'('", "')'", "','", "'global'", "'constant'", "'addrspace'", "'private'", "'linker_private'", "'internal'", "'available_externally'", "'linkonce'", "'weak'", "'common'", "'appending'", "'extern_weak'", "'linkonce_odr'", "'weak_odr'", "'externally_visible'", "'dllimport'", "'dllexport'", "'zeroinitializer'", "'to'", "'trunc'", "'zext'", "'sext'", "'fptrunc'", "'fpext'", "'fptoui'", "'fptosi'", "'uitofp'", "'sitofp'", "'ptrtoint'", "'inttoptr'", "'bitcast'", "'default'", "'hidden'", "'protected'", "'ccc'", "'fastcc'", "'coldcc'", "'cc 10'", "'cc'", "'zeroext'", "'signext'", "'inreg'", "'byval'", "'sret'", "'noalias'", "'nocapture'", "'nest'", "'alignstack'", "'alwaysinline'", "'inlinehint'", "'noinline'", "'optsize'", "'noreturn'", "'nounwind'", "'readnone'", "'readonly'", "'ssp'", "'sspreq'", "'noredzone'", "'noimplicitfloat'", "'naked'", "':'", "'alloca'", "'store'", "'ret'", "'load'", "'add'", "'fadd'", "'sub'", "'fsub'", "'mul'", "'fmul'", "'udiv'", "'sdiv'", "'fdiv'", "'urem'", "'srem'", "'frem'", "'shl'", "'lshr'", "'ashr'", "'and'", "'or'", "'xor'", "'nuw'", "'nsw'", "'exact'", "'br'"
    };
    public static final int T__29=29;
    public static final int T__28=28;
    public static final int T__27=27;
    public static final int T__26=26;
    public static final int T__25=25;
    public static final int T__24=24;
    public static final int T__23=23;
    public static final int EQUALS=5;
    public static final int EOF=-1;
    public static final int T__93=93;
    public static final int T__94=94;
    public static final int T__91=91;
    public static final int T__92=92;
    public static final int STRING_LITERAL=12;
    public static final int T__90=90;
    public static final int T__99=99;
    public static final int T__98=98;
    public static final int T__97=97;
    public static final int T__96=96;
    public static final int T__95=95;
    public static final int T__80=80;
    public static final int T__81=81;
    public static final int T__82=82;
    public static final int T__83=83;
    public static final int NUMBER_SUFFIX=18;
    public static final int NUMBER=10;
    public static final int NAMED_ID=7;
    public static final int INT_TYPE=6;
    public static final int T__85=85;
    public static final int T__84=84;
    public static final int T__87=87;
    public static final int T__86=86;
    public static final int T__89=89;
    public static final int T__88=88;
    public static final int SYM_PFX=17;
    public static final int T__125=125;
    public static final int WS=20;
    public static final int T__71=71;
    public static final int T__72=72;
    public static final int NAME_SUFFIX=19;
    public static final int T__70=70;
    public static final int T__76=76;
    public static final int T__75=75;
    public static final int T__74=74;
    public static final int T__73=73;
    public static final int T__79=79;
    public static final int T__78=78;
    public static final int T__77=77;
    public static final int T__68=68;
    public static final int T__69=69;
    public static final int T__66=66;
    public static final int T__67=67;
    public static final int T__64=64;
    public static final int T__65=65;
    public static final int T__62=62;
    public static final int T__63=63;
    public static final int T__118=118;
    public static final int T__119=119;
    public static final int T__116=116;
    public static final int T__117=117;
    public static final int T__114=114;
    public static final int MULTI_COMMENT=22;
    public static final int T__115=115;
    public static final int T__124=124;
    public static final int T__123=123;
    public static final int T__122=122;
    public static final int T__121=121;
    public static final int T__120=120;
    public static final int T__61=61;
    public static final int DEFINE=14;
    public static final int T__60=60;
    public static final int QUOTED_ID=9;
    public static final int NUMSUFFIX=16;
    public static final int T__55=55;
    public static final int T__56=56;
    public static final int T__57=57;
    public static final int T__58=58;
    public static final int T__51=51;
    public static final int T__52=52;
    public static final int T__53=53;
    public static final int T__54=54;
    public static final int T__107=107;
    public static final int T__108=108;
    public static final int T__109=109;
    public static final int T__59=59;
    public static final int T__103=103;
    public static final int T__104=104;
    public static final int T__105=105;
    public static final int T__106=106;
    public static final int T__111=111;
    public static final int T__110=110;
    public static final int T__113=113;
    public static final int T__112=112;
    public static final int SINGLE_COMMENT=21;
    public static final int T__50=50;
    public static final int T__42=42;
    public static final int T__43=43;
    public static final int T__40=40;
    public static final int T__41=41;
    public static final int T__46=46;
    public static final int T__47=47;
    public static final int T__44=44;
    public static final int T__45=45;
    public static final int T__48=48;
    public static final int T__49=49;
    public static final int T__102=102;
    public static final int T__101=101;
    public static final int T__100=100;
    public static final int T__30=30;
    public static final int T__31=31;
    public static final int T__32=32;
    public static final int T__33=33;
    public static final int T__34=34;
    public static final int NEWLINE=4;
    public static final int T__35=35;
    public static final int CHAR_LITERAL=11;
    public static final int T__36=36;
    public static final int LABEL=15;
    public static final int T__37=37;
    public static final int T__38=38;
    public static final int T__39=39;
    public static final int CSTRING_LITERAL=13;
    public static final int UNNAMED_ID=8;

    // delegates
    // delegators


        public LLVMParser(TokenStream input) {
            this(input, new RecognizerSharedState());
        }
        public LLVMParser(TokenStream input, RecognizerSharedState state) {
            super(input, state);
             
        }
        
    protected TreeAdaptor adaptor = new CommonTreeAdaptor();

    public void setTreeAdaptor(TreeAdaptor adaptor) {
        this.adaptor = adaptor;
    }
    public TreeAdaptor getTreeAdaptor() {
        return adaptor;
    }

    public String[] getTokenNames() { return LLVMParser.tokenNames; }
    public String getGrammarFileName() { return "/home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g"; }


        public String getTokenErrorDisplay(Token t) {
            return '\'' + t.getText() + '\'';
        }

        LLParserHelper helper;   
         
        public LLVMParser(TokenStream input, LLParserHelper helper) {
            this(input);
            this.helper = helper;
        }
      


    public static class prog_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "prog"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:50:1: prog : toplevelstmts EOF ;
    public final LLVMParser.prog_return prog() throws RecognitionException {
        LLVMParser.prog_return retval = new LLVMParser.prog_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token EOF2=null;
        LLVMParser.toplevelstmts_return toplevelstmts1 = null;


        CommonTree EOF2_tree=null;

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:50:5: ( toplevelstmts EOF )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:50:9: toplevelstmts EOF
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_toplevelstmts_in_prog69);
            toplevelstmts1=toplevelstmts();

            state._fsp--;

            adaptor.addChild(root_0, toplevelstmts1.getTree());
            EOF2=(Token)match(input,EOF,FOLLOW_EOF_in_prog71); 

            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:53:1: toplevelstmts : ( directive )* ;
    public final LLVMParser.toplevelstmts_return toplevelstmts() throws RecognitionException {
        LLVMParser.toplevelstmts_return retval = new LLVMParser.toplevelstmts_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        LLVMParser.directive_return directive3 = null;



        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:53:14: ( ( directive )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:53:17: ( directive )*
            {
            root_0 = (CommonTree)adaptor.nil();

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:53:17: ( directive )*
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( (LA1_0==NEWLINE||(LA1_0>=NAMED_ID && LA1_0<=QUOTED_ID)||LA1_0==DEFINE||LA1_0==23) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:53:17: directive
            	    {
            	    pushFollow(FOLLOW_directive_in_toplevelstmts101);
            	    directive3=directive();

            	    state._fsp--;

            	    adaptor.addChild(root_0, directive3.getTree());

            	    }
            	    break;

            	default :
            	    break loop1;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

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

    public static class directive_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "directive"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:56:1: directive : ( targetDataLayoutDirective ( NEWLINE | EOF ) | targetTripleDirective ( NEWLINE | EOF ) | typeDefinition ( NEWLINE | EOF ) | globalDataDirective ( NEWLINE | EOF ) | constantDirective ( NEWLINE | EOF ) | defineDirective ( NEWLINE | EOF ) | NEWLINE );
    public final LLVMParser.directive_return directive() throws RecognitionException {
        LLVMParser.directive_return retval = new LLVMParser.directive_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token set5=null;
        Token set7=null;
        Token set9=null;
        Token set11=null;
        Token set13=null;
        Token set15=null;
        Token NEWLINE16=null;
        LLVMParser.targetDataLayoutDirective_return targetDataLayoutDirective4 = null;

        LLVMParser.targetTripleDirective_return targetTripleDirective6 = null;

        LLVMParser.typeDefinition_return typeDefinition8 = null;

        LLVMParser.globalDataDirective_return globalDataDirective10 = null;

        LLVMParser.constantDirective_return constantDirective12 = null;

        LLVMParser.defineDirective_return defineDirective14 = null;


        CommonTree set5_tree=null;
        CommonTree set7_tree=null;
        CommonTree set9_tree=null;
        CommonTree set11_tree=null;
        CommonTree set13_tree=null;
        CommonTree set15_tree=null;
        CommonTree NEWLINE16_tree=null;

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:56:12: ( targetDataLayoutDirective ( NEWLINE | EOF ) | targetTripleDirective ( NEWLINE | EOF ) | typeDefinition ( NEWLINE | EOF ) | globalDataDirective ( NEWLINE | EOF ) | constantDirective ( NEWLINE | EOF ) | defineDirective ( NEWLINE | EOF ) | NEWLINE )
            int alt2=7;
            alt2 = dfa2.predict(input);
            switch (alt2) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:56:14: targetDataLayoutDirective ( NEWLINE | EOF )
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_targetDataLayoutDirective_in_directive122);
                    targetDataLayoutDirective4=targetDataLayoutDirective();

                    state._fsp--;

                    adaptor.addChild(root_0, targetDataLayoutDirective4.getTree());
                    set5=(Token)input.LT(1);
                    if ( input.LA(1)==EOF||input.LA(1)==NEWLINE ) {
                        input.consume();
                        adaptor.addChild(root_0, (CommonTree)adaptor.create(set5));
                        state.errorRecovery=false;
                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        throw mse;
                    }


                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:57:5: targetTripleDirective ( NEWLINE | EOF )
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_targetTripleDirective_in_directive137);
                    targetTripleDirective6=targetTripleDirective();

                    state._fsp--;

                    adaptor.addChild(root_0, targetTripleDirective6.getTree());
                    set7=(Token)input.LT(1);
                    if ( input.LA(1)==EOF||input.LA(1)==NEWLINE ) {
                        input.consume();
                        adaptor.addChild(root_0, (CommonTree)adaptor.create(set7));
                        state.errorRecovery=false;
                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        throw mse;
                    }


                    }
                    break;
                case 3 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:58:5: typeDefinition ( NEWLINE | EOF )
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_typeDefinition_in_directive152);
                    typeDefinition8=typeDefinition();

                    state._fsp--;

                    adaptor.addChild(root_0, typeDefinition8.getTree());
                    set9=(Token)input.LT(1);
                    if ( input.LA(1)==EOF||input.LA(1)==NEWLINE ) {
                        input.consume();
                        adaptor.addChild(root_0, (CommonTree)adaptor.create(set9));
                        state.errorRecovery=false;
                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        throw mse;
                    }


                    }
                    break;
                case 4 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:59:5: globalDataDirective ( NEWLINE | EOF )
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_globalDataDirective_in_directive167);
                    globalDataDirective10=globalDataDirective();

                    state._fsp--;

                    adaptor.addChild(root_0, globalDataDirective10.getTree());
                    set11=(Token)input.LT(1);
                    if ( input.LA(1)==EOF||input.LA(1)==NEWLINE ) {
                        input.consume();
                        adaptor.addChild(root_0, (CommonTree)adaptor.create(set11));
                        state.errorRecovery=false;
                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        throw mse;
                    }


                    }
                    break;
                case 5 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:60:5: constantDirective ( NEWLINE | EOF )
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_constantDirective_in_directive182);
                    constantDirective12=constantDirective();

                    state._fsp--;

                    adaptor.addChild(root_0, constantDirective12.getTree());
                    set13=(Token)input.LT(1);
                    if ( input.LA(1)==EOF||input.LA(1)==NEWLINE ) {
                        input.consume();
                        adaptor.addChild(root_0, (CommonTree)adaptor.create(set13));
                        state.errorRecovery=false;
                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        throw mse;
                    }


                    }
                    break;
                case 6 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:61:5: defineDirective ( NEWLINE | EOF )
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_defineDirective_in_directive196);
                    defineDirective14=defineDirective();

                    state._fsp--;

                    adaptor.addChild(root_0, defineDirective14.getTree());
                    set15=(Token)input.LT(1);
                    if ( input.LA(1)==EOF||input.LA(1)==NEWLINE ) {
                        input.consume();
                        adaptor.addChild(root_0, (CommonTree)adaptor.create(set15));
                        state.errorRecovery=false;
                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        throw mse;
                    }


                    }
                    break;
                case 7 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:62:5: NEWLINE
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    NEWLINE16=(Token)match(input,NEWLINE,FOLLOW_NEWLINE_in_directive210); 
                    NEWLINE16_tree = (CommonTree)adaptor.create(NEWLINE16);
                    adaptor.addChild(root_0, NEWLINE16_tree);


                    }
                    break;

            }
            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

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
    // $ANTLR end "directive"

    public static class targetDataLayoutDirective_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "targetDataLayoutDirective"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:65:1: targetDataLayoutDirective : 'target' 'datalayout' EQUALS stringLiteral ;
    public final LLVMParser.targetDataLayoutDirective_return targetDataLayoutDirective() throws RecognitionException {
        LLVMParser.targetDataLayoutDirective_return retval = new LLVMParser.targetDataLayoutDirective_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token string_literal17=null;
        Token string_literal18=null;
        Token EQUALS19=null;
        LLVMParser.stringLiteral_return stringLiteral20 = null;


        CommonTree string_literal17_tree=null;
        CommonTree string_literal18_tree=null;
        CommonTree EQUALS19_tree=null;

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:65:27: ( 'target' 'datalayout' EQUALS stringLiteral )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:65:29: 'target' 'datalayout' EQUALS stringLiteral
            {
            root_0 = (CommonTree)adaptor.nil();

            string_literal17=(Token)match(input,23,FOLLOW_23_in_targetDataLayoutDirective223); 
            string_literal17_tree = (CommonTree)adaptor.create(string_literal17);
            adaptor.addChild(root_0, string_literal17_tree);

            string_literal18=(Token)match(input,24,FOLLOW_24_in_targetDataLayoutDirective225); 
            string_literal18_tree = (CommonTree)adaptor.create(string_literal18);
            adaptor.addChild(root_0, string_literal18_tree);

            EQUALS19=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_targetDataLayoutDirective227); 
            EQUALS19_tree = (CommonTree)adaptor.create(EQUALS19);
            adaptor.addChild(root_0, EQUALS19_tree);

            pushFollow(FOLLOW_stringLiteral_in_targetDataLayoutDirective229);
            stringLiteral20=stringLiteral();

            state._fsp--;

            adaptor.addChild(root_0, stringLiteral20.getTree());
             helper.addTargetDataLayoutDirective((stringLiteral20!=null?stringLiteral20.theText:null)); 

            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

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
    // $ANTLR end "targetDataLayoutDirective"

    public static class targetTripleDirective_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "targetTripleDirective"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:69:1: targetTripleDirective : 'target' 'triple' EQUALS stringLiteral ;
    public final LLVMParser.targetTripleDirective_return targetTripleDirective() throws RecognitionException {
        LLVMParser.targetTripleDirective_return retval = new LLVMParser.targetTripleDirective_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token string_literal21=null;
        Token string_literal22=null;
        Token EQUALS23=null;
        LLVMParser.stringLiteral_return stringLiteral24 = null;


        CommonTree string_literal21_tree=null;
        CommonTree string_literal22_tree=null;
        CommonTree EQUALS23_tree=null;

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:69:23: ( 'target' 'triple' EQUALS stringLiteral )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:69:25: 'target' 'triple' EQUALS stringLiteral
            {
            root_0 = (CommonTree)adaptor.nil();

            string_literal21=(Token)match(input,23,FOLLOW_23_in_targetTripleDirective245); 
            string_literal21_tree = (CommonTree)adaptor.create(string_literal21);
            adaptor.addChild(root_0, string_literal21_tree);

            string_literal22=(Token)match(input,25,FOLLOW_25_in_targetTripleDirective247); 
            string_literal22_tree = (CommonTree)adaptor.create(string_literal22);
            adaptor.addChild(root_0, string_literal22_tree);

            EQUALS23=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_targetTripleDirective249); 
            EQUALS23_tree = (CommonTree)adaptor.create(EQUALS23);
            adaptor.addChild(root_0, EQUALS23_tree);

            pushFollow(FOLLOW_stringLiteral_in_targetTripleDirective251);
            stringLiteral24=stringLiteral();

            state._fsp--;

            adaptor.addChild(root_0, stringLiteral24.getTree());
             helper.addTargetTripleDirective((stringLiteral24!=null?stringLiteral24.theText:null)); 

            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

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
    // $ANTLR end "targetTripleDirective"

    public static class typeDefinition_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "typeDefinition"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:73:1: typeDefinition : identifier EQUALS 'type' type ;
    public final LLVMParser.typeDefinition_return typeDefinition() throws RecognitionException {
        LLVMParser.typeDefinition_return retval = new LLVMParser.typeDefinition_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token EQUALS26=null;
        Token string_literal27=null;
        LLVMParser.identifier_return identifier25 = null;

        LLVMParser.type_return type28 = null;


        CommonTree EQUALS26_tree=null;
        CommonTree string_literal27_tree=null;

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:73:16: ( identifier EQUALS 'type' type )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:73:18: identifier EQUALS 'type' type
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_identifier_in_typeDefinition266);
            identifier25=identifier();

            state._fsp--;

            adaptor.addChild(root_0, identifier25.getTree());
            EQUALS26=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_typeDefinition268); 
            EQUALS26_tree = (CommonTree)adaptor.create(EQUALS26);
            adaptor.addChild(root_0, EQUALS26_tree);

            string_literal27=(Token)match(input,26,FOLLOW_26_in_typeDefinition270); 
            string_literal27_tree = (CommonTree)adaptor.create(string_literal27);
            adaptor.addChild(root_0, string_literal27_tree);

            pushFollow(FOLLOW_type_in_typeDefinition274);
            type28=type();

            state._fsp--;

            adaptor.addChild(root_0, type28.getTree());
             
              	helper.addNewType((identifier25!=null?identifier25.theId:null), (type28!=null?type28.theType:null)); 
              

            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

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
    // $ANTLR end "typeDefinition"

    public static class type_return extends ParserRuleReturnScope {
        public LLType theType;
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "type"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:80:1: type returns [LLType theType] : (t0= inttype | t1= structtype | t2= arraytype | 'void' | 'label' | t3= symboltype ) ( '*' )* ( paramstype )? ;
    public final LLVMParser.type_return type() throws RecognitionException {
        LLVMParser.type_return retval = new LLVMParser.type_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token string_literal29=null;
        Token string_literal30=null;
        Token char_literal31=null;
        LLVMParser.inttype_return t0 = null;

        LLVMParser.structtype_return t1 = null;

        LLVMParser.arraytype_return t2 = null;

        LLVMParser.symboltype_return t3 = null;

        LLVMParser.paramstype_return paramstype32 = null;


        CommonTree string_literal29_tree=null;
        CommonTree string_literal30_tree=null;
        CommonTree char_literal31_tree=null;


        	  	// ensure we recognize temp symbols like percent 0 as pointing
        	  	// to types rather than variables
        		helper.inTypeContext++;
            
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:92:5: ( (t0= inttype | t1= structtype | t2= arraytype | 'void' | 'label' | t3= symboltype ) ( '*' )* ( paramstype )? )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:93:2: (t0= inttype | t1= structtype | t2= arraytype | 'void' | 'label' | t3= symboltype ) ( '*' )* ( paramstype )?
            {
            root_0 = (CommonTree)adaptor.nil();

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:93:2: (t0= inttype | t1= structtype | t2= arraytype | 'void' | 'label' | t3= symboltype )
            int alt3=6;
            switch ( input.LA(1) ) {
            case INT_TYPE:
                {
                alt3=1;
                }
                break;
            case 30:
                {
                alt3=2;
                }
                break;
            case 32:
                {
                alt3=3;
                }
                break;
            case 27:
                {
                alt3=4;
                }
                break;
            case 28:
                {
                alt3=5;
                }
                break;
            case NAMED_ID:
            case UNNAMED_ID:
            case QUOTED_ID:
                {
                alt3=6;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 3, 0, input);

                throw nvae;
            }

            switch (alt3) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:93:5: t0= inttype
                    {
                    pushFollow(FOLLOW_inttype_in_type327);
                    t0=inttype();

                    state._fsp--;

                    adaptor.addChild(root_0, t0.getTree());
                     retval.theType = (t0!=null?t0.theType:null); 

                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:94:5: t1= structtype
                    {
                    pushFollow(FOLLOW_structtype_in_type338);
                    t1=structtype();

                    state._fsp--;

                    adaptor.addChild(root_0, t1.getTree());
                     retval.theType = (t1!=null?t1.theType:null); 

                    }
                    break;
                case 3 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:95:5: t2= arraytype
                    {
                    pushFollow(FOLLOW_arraytype_in_type348);
                    t2=arraytype();

                    state._fsp--;

                    adaptor.addChild(root_0, t2.getTree());
                     retval.theType = (t2!=null?t2.theType:null); 

                    }
                    break;
                case 4 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:96:5: 'void'
                    {
                    string_literal29=(Token)match(input,27,FOLLOW_27_in_type356); 
                    string_literal29_tree = (CommonTree)adaptor.create(string_literal29);
                    adaptor.addChild(root_0, string_literal29_tree);

                     retval.theType = helper.typeEngine.VOID; 

                    }
                    break;
                case 5 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:97:5: 'label'
                    {
                    string_literal30=(Token)match(input,28,FOLLOW_28_in_type371); 
                    string_literal30_tree = (CommonTree)adaptor.create(string_literal30);
                    adaptor.addChild(root_0, string_literal30_tree);

                     retval.theType = helper.typeEngine.LABEL; 

                    }
                    break;
                case 6 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:98:5: t3= symboltype
                    {
                    pushFollow(FOLLOW_symboltype_in_type388);
                    t3=symboltype();

                    state._fsp--;

                    adaptor.addChild(root_0, t3.getTree());
                     retval.theType = (t3!=null?t3.theType:null); 

                    }
                    break;

            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:101:2: ( '*' )*
            loop4:
            do {
                int alt4=2;
                int LA4_0 = input.LA(1);

                if ( (LA4_0==29) ) {
                    alt4=1;
                }


                switch (alt4) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:101:4: '*'
            	    {
            	    char_literal31=(Token)match(input,29,FOLLOW_29_in_type405); 
            	    char_literal31_tree = (CommonTree)adaptor.create(char_literal31);
            	    adaptor.addChild(root_0, char_literal31_tree);

            	     retval.theType = helper.addPointerType(retval.theType); 

            	    }
            	    break;

            	default :
            	    break loop4;
                }
            } while (true);

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:103:2: ( paramstype )?
            int alt5=2;
            int LA5_0 = input.LA(1);

            if ( (LA5_0==35) ) {
                alt5=1;
            }
            switch (alt5) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:103:3: paramstype
                    {
                    pushFollow(FOLLOW_paramstype_in_type417);
                    paramstype32=paramstype();

                    state._fsp--;

                    adaptor.addChild(root_0, paramstype32.getTree());
                     retval.theType = helper.addCodeType(retval.theType, (paramstype32!=null?paramstype32.theArgs:null)); 

                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);


                // done 
                  helper.inTypeContext--;
                
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

    public static class inttype_return extends ParserRuleReturnScope {
        public LLType theType;
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "inttype"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:107:1: inttype returns [LLType theType] : INT_TYPE ;
    public final LLVMParser.inttype_return inttype() throws RecognitionException {
        LLVMParser.inttype_return retval = new LLVMParser.inttype_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token INT_TYPE33=null;

        CommonTree INT_TYPE33_tree=null;

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:107:34: ( INT_TYPE )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:107:36: INT_TYPE
            {
            root_0 = (CommonTree)adaptor.nil();

            INT_TYPE33=(Token)match(input,INT_TYPE,FOLLOW_INT_TYPE_in_inttype439); 
            INT_TYPE33_tree = (CommonTree)adaptor.create(INT_TYPE33);
            adaptor.addChild(root_0, INT_TYPE33_tree);

             retval.theType = helper.addIntType((INT_TYPE33!=null?INT_TYPE33.getText():null)); 

            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

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
    // $ANTLR end "inttype"

    public static class structtype_return extends ParserRuleReturnScope {
        public LLType theType;
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "structtype"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:111:1: structtype returns [LLType theType] : '{' typeList '}' ;
    public final LLVMParser.structtype_return structtype() throws RecognitionException {
        LLVMParser.structtype_return retval = new LLVMParser.structtype_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token char_literal34=null;
        Token char_literal36=null;
        LLVMParser.typeList_return typeList35 = null;


        CommonTree char_literal34_tree=null;
        CommonTree char_literal36_tree=null;

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:111:38: ( '{' typeList '}' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:111:40: '{' typeList '}'
            {
            root_0 = (CommonTree)adaptor.nil();

            char_literal34=(Token)match(input,30,FOLLOW_30_in_structtype458); 
            char_literal34_tree = (CommonTree)adaptor.create(char_literal34);
            adaptor.addChild(root_0, char_literal34_tree);

            pushFollow(FOLLOW_typeList_in_structtype460);
            typeList35=typeList();

            state._fsp--;

            adaptor.addChild(root_0, typeList35.getTree());
            char_literal36=(Token)match(input,31,FOLLOW_31_in_structtype462); 
            char_literal36_tree = (CommonTree)adaptor.create(char_literal36);
            adaptor.addChild(root_0, char_literal36_tree);


            		retval.theType = helper.addTupleType((typeList35!=null?typeList35.theTypes:null)); 
            	

            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

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
    // $ANTLR end "structtype"

    public static class arraytype_return extends ParserRuleReturnScope {
        public LLType theType;
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "arraytype"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:117:1: arraytype returns [LLType theType] : '[' number 'x' type ']' ;
    public final LLVMParser.arraytype_return arraytype() throws RecognitionException {
        LLVMParser.arraytype_return retval = new LLVMParser.arraytype_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token char_literal37=null;
        Token char_literal39=null;
        Token char_literal41=null;
        LLVMParser.number_return number38 = null;

        LLVMParser.type_return type40 = null;


        CommonTree char_literal37_tree=null;
        CommonTree char_literal39_tree=null;
        CommonTree char_literal41_tree=null;

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:117:36: ( '[' number 'x' type ']' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:117:39: '[' number 'x' type ']'
            {
            root_0 = (CommonTree)adaptor.nil();

            char_literal37=(Token)match(input,32,FOLLOW_32_in_arraytype483); 
            char_literal37_tree = (CommonTree)adaptor.create(char_literal37);
            adaptor.addChild(root_0, char_literal37_tree);

            pushFollow(FOLLOW_number_in_arraytype485);
            number38=number();

            state._fsp--;

            adaptor.addChild(root_0, number38.getTree());
            char_literal39=(Token)match(input,33,FOLLOW_33_in_arraytype487); 
            char_literal39_tree = (CommonTree)adaptor.create(char_literal39);
            adaptor.addChild(root_0, char_literal39_tree);

            pushFollow(FOLLOW_type_in_arraytype489);
            type40=type();

            state._fsp--;

            adaptor.addChild(root_0, type40.getTree());
            char_literal41=(Token)match(input,34,FOLLOW_34_in_arraytype491); 
            char_literal41_tree = (CommonTree)adaptor.create(char_literal41);
            adaptor.addChild(root_0, char_literal41_tree);

             retval.theType = helper.addArrayType((number38!=null?number38.value:0), (type40!=null?type40.theType:null)); 

            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

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
    // $ANTLR end "arraytype"

    public static class paramstype_return extends ParserRuleReturnScope {
        public LLType[] theArgs;
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "paramstype"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:121:1: paramstype returns [LLType[] theArgs] : '(' typeList ')' ;
    public final LLVMParser.paramstype_return paramstype() throws RecognitionException {
        LLVMParser.paramstype_return retval = new LLVMParser.paramstype_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token char_literal42=null;
        Token char_literal44=null;
        LLVMParser.typeList_return typeList43 = null;


        CommonTree char_literal42_tree=null;
        CommonTree char_literal44_tree=null;

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:121:40: ( '(' typeList ')' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:121:42: '(' typeList ')'
            {
            root_0 = (CommonTree)adaptor.nil();

            char_literal42=(Token)match(input,35,FOLLOW_35_in_paramstype509); 
            char_literal42_tree = (CommonTree)adaptor.create(char_literal42);
            adaptor.addChild(root_0, char_literal42_tree);

            pushFollow(FOLLOW_typeList_in_paramstype511);
            typeList43=typeList();

            state._fsp--;

            adaptor.addChild(root_0, typeList43.getTree());
            char_literal44=(Token)match(input,36,FOLLOW_36_in_paramstype513); 
            char_literal44_tree = (CommonTree)adaptor.create(char_literal44);
            adaptor.addChild(root_0, char_literal44_tree);

             retval.theArgs = (typeList43!=null?typeList43.theTypes:null); 

            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

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
    // $ANTLR end "paramstype"

    public static class typeList_return extends ParserRuleReturnScope {
        public LLType[] theTypes;
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "typeList"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:125:1: typeList returns [LLType[] theTypes] : (t= type ( ',' u= type )* )? ;
    public final LLVMParser.typeList_return typeList() throws RecognitionException {
        LLVMParser.typeList_return retval = new LLVMParser.typeList_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token char_literal45=null;
        LLVMParser.type_return t = null;

        LLVMParser.type_return u = null;


        CommonTree char_literal45_tree=null;


            List<LLType> types = new ArrayList<LLType>();
          
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:134:3: ( (t= type ( ',' u= type )* )? )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:134:5: (t= type ( ',' u= type )* )?
            {
            root_0 = (CommonTree)adaptor.nil();

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:134:5: (t= type ( ',' u= type )* )?
            int alt7=2;
            int LA7_0 = input.LA(1);

            if ( ((LA7_0>=INT_TYPE && LA7_0<=QUOTED_ID)||(LA7_0>=27 && LA7_0<=28)||LA7_0==30||LA7_0==32) ) {
                alt7=1;
            }
            switch (alt7) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:134:6: t= type ( ',' u= type )*
                    {
                    pushFollow(FOLLOW_type_in_typeList555);
                    t=type();

                    state._fsp--;

                    adaptor.addChild(root_0, t.getTree());
                     types.add((t!=null?t.theType:null)); 
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:135:7: ( ',' u= type )*
                    loop6:
                    do {
                        int alt6=2;
                        int LA6_0 = input.LA(1);

                        if ( (LA6_0==37) ) {
                            alt6=1;
                        }


                        switch (alt6) {
                    	case 1 :
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:135:8: ',' u= type
                    	    {
                    	    char_literal45=(Token)match(input,37,FOLLOW_37_in_typeList573); 
                    	    char_literal45_tree = (CommonTree)adaptor.create(char_literal45);
                    	    adaptor.addChild(root_0, char_literal45_tree);

                    	    pushFollow(FOLLOW_type_in_typeList577);
                    	    u=type();

                    	    state._fsp--;

                    	    adaptor.addChild(root_0, u.getTree());
                    	     types.add((u!=null?u.theType:null)); 

                    	    }
                    	    break;

                    	default :
                    	    break loop6;
                        }
                    } while (true);


                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);


              retval.theTypes = types.toArray(new LLType[types.size()]);
              
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
    // $ANTLR end "typeList"

    public static class symboltype_return extends ParserRuleReturnScope {
        public LLType theType;
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "symboltype"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:140:1: symboltype returns [LLType theType] : identifier ;
    public final LLVMParser.symboltype_return symboltype() throws RecognitionException {
        LLVMParser.symboltype_return retval = new LLVMParser.symboltype_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        LLVMParser.identifier_return identifier46 = null;



        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:140:37: ( identifier )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:140:39: identifier
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_identifier_in_symboltype619);
            identifier46=identifier();

            state._fsp--;

            adaptor.addChild(root_0, identifier46.getTree());
             retval.theType = helper.findOrForwardNameType((identifier46!=null?identifier46.theId:null)); 

            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

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
    // $ANTLR end "symboltype"

    public static class globalDataDirective_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "globalDataDirective"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:144:1: globalDataDirective : identifier EQUALS ( linkage )? 'global' typedop ;
    public final LLVMParser.globalDataDirective_return globalDataDirective() throws RecognitionException {
        LLVMParser.globalDataDirective_return retval = new LLVMParser.globalDataDirective_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token EQUALS48=null;
        Token string_literal50=null;
        LLVMParser.identifier_return identifier47 = null;

        LLVMParser.linkage_return linkage49 = null;

        LLVMParser.typedop_return typedop51 = null;


        CommonTree EQUALS48_tree=null;
        CommonTree string_literal50_tree=null;

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:144:21: ( identifier EQUALS ( linkage )? 'global' typedop )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:144:23: identifier EQUALS ( linkage )? 'global' typedop
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_identifier_in_globalDataDirective633);
            identifier47=identifier();

            state._fsp--;

            adaptor.addChild(root_0, identifier47.getTree());
            EQUALS48=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_globalDataDirective635); 
            EQUALS48_tree = (CommonTree)adaptor.create(EQUALS48);
            adaptor.addChild(root_0, EQUALS48_tree);

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:144:41: ( linkage )?
            int alt8=2;
            int LA8_0 = input.LA(1);

            if ( ((LA8_0>=41 && LA8_0<=54)) ) {
                alt8=1;
            }
            switch (alt8) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:144:41: linkage
                    {
                    pushFollow(FOLLOW_linkage_in_globalDataDirective637);
                    linkage49=linkage();

                    state._fsp--;

                    adaptor.addChild(root_0, linkage49.getTree());

                    }
                    break;

            }

            string_literal50=(Token)match(input,38,FOLLOW_38_in_globalDataDirective640); 
            string_literal50_tree = (CommonTree)adaptor.create(string_literal50);
            adaptor.addChild(root_0, string_literal50_tree);

            pushFollow(FOLLOW_typedop_in_globalDataDirective642);
            typedop51=typedop();

            state._fsp--;

            adaptor.addChild(root_0, typedop51.getTree());
             helper.addGlobalDataDirective((identifier47!=null?identifier47.theId:null), (linkage49!=null?linkage49.value:null), (typedop51!=null?typedop51.op:null)); 

            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

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
    // $ANTLR end "globalDataDirective"

    public static class constantDirective_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "constantDirective"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:148:1: constantDirective : identifier EQUALS ( addrspace )? 'constant' typedop ;
    public final LLVMParser.constantDirective_return constantDirective() throws RecognitionException {
        LLVMParser.constantDirective_return retval = new LLVMParser.constantDirective_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token EQUALS53=null;
        Token string_literal55=null;
        LLVMParser.identifier_return identifier52 = null;

        LLVMParser.addrspace_return addrspace54 = null;

        LLVMParser.typedop_return typedop56 = null;


        CommonTree EQUALS53_tree=null;
        CommonTree string_literal55_tree=null;

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:148:19: ( identifier EQUALS ( addrspace )? 'constant' typedop )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:148:21: identifier EQUALS ( addrspace )? 'constant' typedop
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_identifier_in_constantDirective656);
            identifier52=identifier();

            state._fsp--;

            adaptor.addChild(root_0, identifier52.getTree());
            EQUALS53=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_constantDirective658); 
            EQUALS53_tree = (CommonTree)adaptor.create(EQUALS53);
            adaptor.addChild(root_0, EQUALS53_tree);

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:148:39: ( addrspace )?
            int alt9=2;
            int LA9_0 = input.LA(1);

            if ( (LA9_0==40) ) {
                alt9=1;
            }
            switch (alt9) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:148:39: addrspace
                    {
                    pushFollow(FOLLOW_addrspace_in_constantDirective660);
                    addrspace54=addrspace();

                    state._fsp--;

                    adaptor.addChild(root_0, addrspace54.getTree());

                    }
                    break;

            }

            string_literal55=(Token)match(input,39,FOLLOW_39_in_constantDirective663); 
            string_literal55_tree = (CommonTree)adaptor.create(string_literal55);
            adaptor.addChild(root_0, string_literal55_tree);

            pushFollow(FOLLOW_typedop_in_constantDirective665);
            typedop56=typedop();

            state._fsp--;

            adaptor.addChild(root_0, typedop56.getTree());
             helper.addConstantDirective((identifier52!=null?identifier52.theId:null), (addrspace54!=null?addrspace54.value:0), (typedop56!=null?typedop56.op:null)); 

            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

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
    // $ANTLR end "constantDirective"

    public static class addrspace_return extends ParserRuleReturnScope {
        public int value;
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "addrspace"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:152:1: addrspace returns [ int value ] : 'addrspace' '(' number ')' ;
    public final LLVMParser.addrspace_return addrspace() throws RecognitionException {
        LLVMParser.addrspace_return retval = new LLVMParser.addrspace_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token string_literal57=null;
        Token char_literal58=null;
        Token char_literal60=null;
        LLVMParser.number_return number59 = null;


        CommonTree string_literal57_tree=null;
        CommonTree char_literal58_tree=null;
        CommonTree char_literal60_tree=null;

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:152:33: ( 'addrspace' '(' number ')' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:152:35: 'addrspace' '(' number ')'
            {
            root_0 = (CommonTree)adaptor.nil();

            string_literal57=(Token)match(input,40,FOLLOW_40_in_addrspace686); 
            string_literal57_tree = (CommonTree)adaptor.create(string_literal57);
            adaptor.addChild(root_0, string_literal57_tree);

            char_literal58=(Token)match(input,35,FOLLOW_35_in_addrspace688); 
            char_literal58_tree = (CommonTree)adaptor.create(char_literal58);
            adaptor.addChild(root_0, char_literal58_tree);

            pushFollow(FOLLOW_number_in_addrspace690);
            number59=number();

            state._fsp--;

            adaptor.addChild(root_0, number59.getTree());
            char_literal60=(Token)match(input,36,FOLLOW_36_in_addrspace692); 
            char_literal60_tree = (CommonTree)adaptor.create(char_literal60);
            adaptor.addChild(root_0, char_literal60_tree);

             retval.value = (number59!=null?number59.value:0); 

            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

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
    // $ANTLR end "addrspace"

    public static class linkage_return extends ParserRuleReturnScope {
        public LLLinkage value;
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "linkage"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:155:1: linkage returns [ LLLinkage value ] : ( 'private' | 'linker_private' | 'internal' | 'available_externally' | 'linkonce' | 'weak' | 'common' | 'appending' | 'extern_weak' | 'linkonce_odr' | 'weak_odr' | 'externally_visible' | 'dllimport' | 'dllexport' ) ;
    public final LLVMParser.linkage_return linkage() throws RecognitionException {
        LLVMParser.linkage_return retval = new LLVMParser.linkage_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token set61=null;

        CommonTree set61_tree=null;

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:155:37: ( ( 'private' | 'linker_private' | 'internal' | 'available_externally' | 'linkonce' | 'weak' | 'common' | 'appending' | 'extern_weak' | 'linkonce_odr' | 'weak_odr' | 'externally_visible' | 'dllimport' | 'dllexport' ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:155:39: ( 'private' | 'linker_private' | 'internal' | 'available_externally' | 'linkonce' | 'weak' | 'common' | 'appending' | 'extern_weak' | 'linkonce_odr' | 'weak_odr' | 'externally_visible' | 'dllimport' | 'dllexport' )
            {
            root_0 = (CommonTree)adaptor.nil();

            set61=(Token)input.LT(1);
            if ( (input.LA(1)>=41 && input.LA(1)<=54) ) {
                input.consume();
                adaptor.addChild(root_0, (CommonTree)adaptor.create(set61));
                state.errorRecovery=false;
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }

             retval.value = LLLinkage.getForToken(input.toString(retval.start,input.LT(-1))); 

            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

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
    // $ANTLR end "linkage"

    public static class typedop_return extends ParserRuleReturnScope {
        public LLOperand op;
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "typedop"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:161:1: typedop returns [ LLOperand op ] : type op ;
    public final LLVMParser.typedop_return typedop() throws RecognitionException {
        LLVMParser.typedop_return retval = new LLVMParser.typedop_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        LLVMParser.type_return type62 = null;

        LLVMParser.op_return op63 = null;



        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:161:34: ( type op )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:161:36: type op
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_type_in_typedop795);
            type62=type();

            state._fsp--;

            adaptor.addChild(root_0, type62.getTree());
            pushFollow(FOLLOW_op_in_typedop797);
            op63=op();

            state._fsp--;

            adaptor.addChild(root_0, op63.getTree());
             retval.op = (op63!=null?op63.op:null); retval.op.setType((type62!=null?type62.theType:null)); 

            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

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
    // $ANTLR end "typedop"

    public static class op_return extends ParserRuleReturnScope {
        public LLOperand op;
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "op"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:165:1: op returns [ LLOperand op ] : ( number | charconst | stringconst | structconst | arrayconst | symbolconst | 'zeroinitializer' | constcastexpr ) ;
    public final LLVMParser.op_return op() throws RecognitionException {
        LLVMParser.op_return retval = new LLVMParser.op_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token string_literal70=null;
        LLVMParser.number_return number64 = null;

        LLVMParser.charconst_return charconst65 = null;

        LLVMParser.stringconst_return stringconst66 = null;

        LLVMParser.structconst_return structconst67 = null;

        LLVMParser.arrayconst_return arrayconst68 = null;

        LLVMParser.symbolconst_return symbolconst69 = null;

        LLVMParser.constcastexpr_return constcastexpr71 = null;


        CommonTree string_literal70_tree=null;

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:165:29: ( ( number | charconst | stringconst | structconst | arrayconst | symbolconst | 'zeroinitializer' | constcastexpr ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:166:3: ( number | charconst | stringconst | structconst | arrayconst | symbolconst | 'zeroinitializer' | constcastexpr )
            {
            root_0 = (CommonTree)adaptor.nil();

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:166:3: ( number | charconst | stringconst | structconst | arrayconst | symbolconst | 'zeroinitializer' | constcastexpr )
            int alt10=8;
            switch ( input.LA(1) ) {
            case NUMBER:
                {
                alt10=1;
                }
                break;
            case CHAR_LITERAL:
                {
                alt10=2;
                }
                break;
            case CSTRING_LITERAL:
                {
                alt10=3;
                }
                break;
            case 30:
                {
                alt10=4;
                }
                break;
            case 32:
                {
                alt10=5;
                }
                break;
            case NAMED_ID:
            case UNNAMED_ID:
            case QUOTED_ID:
                {
                alt10=6;
                }
                break;
            case 55:
                {
                alt10=7;
                }
                break;
            case 57:
            case 58:
            case 59:
            case 60:
            case 61:
            case 62:
            case 63:
            case 64:
            case 65:
            case 66:
            case 67:
            case 68:
                {
                alt10=8;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 10, 0, input);

                throw nvae;
            }

            switch (alt10) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:166:6: number
                    {
                    pushFollow(FOLLOW_number_in_op823);
                    number64=number();

                    state._fsp--;

                    adaptor.addChild(root_0, number64.getTree());
                     retval.op = new LLConstOp(null, (number64!=null?number64.value:0)); 

                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:167:5: charconst
                    {
                    pushFollow(FOLLOW_charconst_in_op831);
                    charconst65=charconst();

                    state._fsp--;

                    adaptor.addChild(root_0, charconst65.getTree());
                     retval.op = new LLConstOp(null, (int)(charconst65!=null?charconst65.value:0)); 

                    }
                    break;
                case 3 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:168:5: stringconst
                    {
                    pushFollow(FOLLOW_stringconst_in_op839);
                    stringconst66=stringconst();

                    state._fsp--;

                    adaptor.addChild(root_0, stringconst66.getTree());
                     retval.op = new LLStringLitOp((LLArrayType)null, (stringconst66!=null?stringconst66.value:null)); 

                    }
                    break;
                case 4 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:169:5: structconst
                    {
                    pushFollow(FOLLOW_structconst_in_op847);
                    structconst67=structconst();

                    state._fsp--;

                    adaptor.addChild(root_0, structconst67.getTree());
                     retval.op = new LLStructOp((LLAggregateType)null, (structconst67!=null?structconst67.values:null)); 

                    }
                    break;
                case 5 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:170:5: arrayconst
                    {
                    pushFollow(FOLLOW_arrayconst_in_op855);
                    arrayconst68=arrayconst();

                    state._fsp--;

                    adaptor.addChild(root_0, arrayconst68.getTree());
                     retval.op = new LLArrayOp((LLArrayType)null, (arrayconst68!=null?arrayconst68.values:null)); 

                    }
                    break;
                case 6 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:171:5: symbolconst
                    {
                    pushFollow(FOLLOW_symbolconst_in_op864);
                    symbolconst69=symbolconst();

                    state._fsp--;

                    adaptor.addChild(root_0, symbolconst69.getTree());
                     retval.op = helper.getSymbolOp((symbolconst69!=null?symbolconst69.theId:null), (symbolconst69!=null?symbolconst69.theSymbol:null)); 

                    }
                    break;
                case 7 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:172:5: 'zeroinitializer'
                    {
                    string_literal70=(Token)match(input,55,FOLLOW_55_in_op873); 
                    string_literal70_tree = (CommonTree)adaptor.create(string_literal70);
                    adaptor.addChild(root_0, string_literal70_tree);

                     retval.op = new LLZeroInitOp(null); 

                    }
                    break;
                case 8 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:173:5: constcastexpr
                    {
                    pushFollow(FOLLOW_constcastexpr_in_op882);
                    constcastexpr71=constcastexpr();

                    state._fsp--;

                    adaptor.addChild(root_0, constcastexpr71.getTree());
                     retval.op = (constcastexpr71!=null?constcastexpr71.op:null); 

                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

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
    // $ANTLR end "op"

    public static class constcastexpr_return extends ParserRuleReturnScope {
        public LLOperand op;
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "constcastexpr"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:177:1: constcastexpr returns [ LLOperand op ] : casttype '(' typedop 'to' type ')' ;
    public final LLVMParser.constcastexpr_return constcastexpr() throws RecognitionException {
        LLVMParser.constcastexpr_return retval = new LLVMParser.constcastexpr_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token char_literal73=null;
        Token string_literal75=null;
        Token char_literal77=null;
        LLVMParser.casttype_return casttype72 = null;

        LLVMParser.typedop_return typedop74 = null;

        LLVMParser.type_return type76 = null;


        CommonTree char_literal73_tree=null;
        CommonTree string_literal75_tree=null;
        CommonTree char_literal77_tree=null;

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:177:40: ( casttype '(' typedop 'to' type ')' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:177:42: casttype '(' typedop 'to' type ')'
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_casttype_in_constcastexpr905);
            casttype72=casttype();

            state._fsp--;

            adaptor.addChild(root_0, casttype72.getTree());
            char_literal73=(Token)match(input,35,FOLLOW_35_in_constcastexpr907); 
            char_literal73_tree = (CommonTree)adaptor.create(char_literal73);
            adaptor.addChild(root_0, char_literal73_tree);

            pushFollow(FOLLOW_typedop_in_constcastexpr909);
            typedop74=typedop();

            state._fsp--;

            adaptor.addChild(root_0, typedop74.getTree());
            string_literal75=(Token)match(input,56,FOLLOW_56_in_constcastexpr911); 
            string_literal75_tree = (CommonTree)adaptor.create(string_literal75);
            adaptor.addChild(root_0, string_literal75_tree);

            pushFollow(FOLLOW_type_in_constcastexpr913);
            type76=type();

            state._fsp--;

            adaptor.addChild(root_0, type76.getTree());
            char_literal77=(Token)match(input,36,FOLLOW_36_in_constcastexpr915); 
            char_literal77_tree = (CommonTree)adaptor.create(char_literal77);
            adaptor.addChild(root_0, char_literal77_tree);


                retval.op = new LLCastOp((casttype72!=null?casttype72.cast:null), (type76!=null?type76.theType:null), (typedop74!=null?typedop74.op:null));
                

            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

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
    // $ANTLR end "constcastexpr"

    public static class casttype_return extends ParserRuleReturnScope {
        public ECast cast;
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "casttype"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:183:1: casttype returns [ ECast cast ] : ( 'trunc' | 'zext' | 'sext' | 'fptrunc' | 'fpext' | 'fptoui' | 'fptosi' | 'uitofp' | 'sitofp' | 'ptrtoint' | 'inttoptr' | 'bitcast' ) ;
    public final LLVMParser.casttype_return casttype() throws RecognitionException {
        LLVMParser.casttype_return retval = new LLVMParser.casttype_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token string_literal78=null;
        Token string_literal79=null;
        Token string_literal80=null;
        Token string_literal81=null;
        Token string_literal82=null;
        Token string_literal83=null;
        Token string_literal84=null;
        Token string_literal85=null;
        Token string_literal86=null;
        Token string_literal87=null;
        Token string_literal88=null;
        Token string_literal89=null;

        CommonTree string_literal78_tree=null;
        CommonTree string_literal79_tree=null;
        CommonTree string_literal80_tree=null;
        CommonTree string_literal81_tree=null;
        CommonTree string_literal82_tree=null;
        CommonTree string_literal83_tree=null;
        CommonTree string_literal84_tree=null;
        CommonTree string_literal85_tree=null;
        CommonTree string_literal86_tree=null;
        CommonTree string_literal87_tree=null;
        CommonTree string_literal88_tree=null;
        CommonTree string_literal89_tree=null;

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:183:33: ( ( 'trunc' | 'zext' | 'sext' | 'fptrunc' | 'fpext' | 'fptoui' | 'fptosi' | 'uitofp' | 'sitofp' | 'ptrtoint' | 'inttoptr' | 'bitcast' ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:184:3: ( 'trunc' | 'zext' | 'sext' | 'fptrunc' | 'fpext' | 'fptoui' | 'fptosi' | 'uitofp' | 'sitofp' | 'ptrtoint' | 'inttoptr' | 'bitcast' )
            {
            root_0 = (CommonTree)adaptor.nil();


              ECast cast = null;
              
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:187:3: ( 'trunc' | 'zext' | 'sext' | 'fptrunc' | 'fpext' | 'fptoui' | 'fptosi' | 'uitofp' | 'sitofp' | 'ptrtoint' | 'inttoptr' | 'bitcast' )
            int alt11=12;
            switch ( input.LA(1) ) {
            case 57:
                {
                alt11=1;
                }
                break;
            case 58:
                {
                alt11=2;
                }
                break;
            case 59:
                {
                alt11=3;
                }
                break;
            case 60:
                {
                alt11=4;
                }
                break;
            case 61:
                {
                alt11=5;
                }
                break;
            case 62:
                {
                alt11=6;
                }
                break;
            case 63:
                {
                alt11=7;
                }
                break;
            case 64:
                {
                alt11=8;
                }
                break;
            case 65:
                {
                alt11=9;
                }
                break;
            case 66:
                {
                alt11=10;
                }
                break;
            case 67:
                {
                alt11=11;
                }
                break;
            case 68:
                {
                alt11=12;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 11, 0, input);

                throw nvae;
            }

            switch (alt11) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:187:5: 'trunc'
                    {
                    string_literal78=(Token)match(input,57,FOLLOW_57_in_casttype953); 
                    string_literal78_tree = (CommonTree)adaptor.create(string_literal78);
                    adaptor.addChild(root_0, string_literal78_tree);

                     cast=ECast.TRUNC; 

                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:188:5: 'zext'
                    {
                    string_literal79=(Token)match(input,58,FOLLOW_58_in_casttype961); 
                    string_literal79_tree = (CommonTree)adaptor.create(string_literal79);
                    adaptor.addChild(root_0, string_literal79_tree);

                     cast=ECast.ZEXT; 

                    }
                    break;
                case 3 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:189:5: 'sext'
                    {
                    string_literal80=(Token)match(input,59,FOLLOW_59_in_casttype969); 
                    string_literal80_tree = (CommonTree)adaptor.create(string_literal80);
                    adaptor.addChild(root_0, string_literal80_tree);

                     cast=ECast.SEXT; 

                    }
                    break;
                case 4 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:190:5: 'fptrunc'
                    {
                    string_literal81=(Token)match(input,60,FOLLOW_60_in_casttype977); 
                    string_literal81_tree = (CommonTree)adaptor.create(string_literal81);
                    adaptor.addChild(root_0, string_literal81_tree);

                     cast=ECast.FPTRUNC; 

                    }
                    break;
                case 5 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:191:5: 'fpext'
                    {
                    string_literal82=(Token)match(input,61,FOLLOW_61_in_casttype985); 
                    string_literal82_tree = (CommonTree)adaptor.create(string_literal82);
                    adaptor.addChild(root_0, string_literal82_tree);

                     cast=ECast.FPEXT; 

                    }
                    break;
                case 6 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:192:5: 'fptoui'
                    {
                    string_literal83=(Token)match(input,62,FOLLOW_62_in_casttype993); 
                    string_literal83_tree = (CommonTree)adaptor.create(string_literal83);
                    adaptor.addChild(root_0, string_literal83_tree);

                     cast=ECast.FPTOUI; 

                    }
                    break;
                case 7 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:193:5: 'fptosi'
                    {
                    string_literal84=(Token)match(input,63,FOLLOW_63_in_casttype1001); 
                    string_literal84_tree = (CommonTree)adaptor.create(string_literal84);
                    adaptor.addChild(root_0, string_literal84_tree);

                     cast=ECast.FPTOSI; 

                    }
                    break;
                case 8 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:194:5: 'uitofp'
                    {
                    string_literal85=(Token)match(input,64,FOLLOW_64_in_casttype1009); 
                    string_literal85_tree = (CommonTree)adaptor.create(string_literal85);
                    adaptor.addChild(root_0, string_literal85_tree);

                     cast=ECast.UITOFP; 

                    }
                    break;
                case 9 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:195:5: 'sitofp'
                    {
                    string_literal86=(Token)match(input,65,FOLLOW_65_in_casttype1017); 
                    string_literal86_tree = (CommonTree)adaptor.create(string_literal86);
                    adaptor.addChild(root_0, string_literal86_tree);

                     cast=ECast.SITOFP; 

                    }
                    break;
                case 10 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:196:5: 'ptrtoint'
                    {
                    string_literal87=(Token)match(input,66,FOLLOW_66_in_casttype1025); 
                    string_literal87_tree = (CommonTree)adaptor.create(string_literal87);
                    adaptor.addChild(root_0, string_literal87_tree);

                     cast=ECast.PTRTOINT; 

                    }
                    break;
                case 11 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:197:5: 'inttoptr'
                    {
                    string_literal88=(Token)match(input,67,FOLLOW_67_in_casttype1034); 
                    string_literal88_tree = (CommonTree)adaptor.create(string_literal88);
                    adaptor.addChild(root_0, string_literal88_tree);

                     cast=ECast.INTTOPTR; 

                    }
                    break;
                case 12 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:198:5: 'bitcast'
                    {
                    string_literal89=(Token)match(input,68,FOLLOW_68_in_casttype1042); 
                    string_literal89_tree = (CommonTree)adaptor.create(string_literal89);
                    adaptor.addChild(root_0, string_literal89_tree);

                     cast=ECast.BITCAST; 

                    }
                    break;

            }


              retval.cast = cast;
              

            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

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
    // $ANTLR end "casttype"

    public static class symbolconst_return extends ParserRuleReturnScope {
        public String theId;
        public ISymbol theSymbol;
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "symbolconst"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:205:1: symbolconst returns [ String theId, ISymbol theSymbol ] : identifier ;
    public final LLVMParser.symbolconst_return symbolconst() throws RecognitionException {
        LLVMParser.symbolconst_return retval = new LLVMParser.symbolconst_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        LLVMParser.identifier_return identifier90 = null;



        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:205:57: ( identifier )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:206:3: identifier
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_identifier_in_symbolconst1071);
            identifier90=identifier();

            state._fsp--;

            adaptor.addChild(root_0, identifier90.getTree());
             retval.theSymbol = helper.findSymbol((identifier90!=null?identifier90.theId:null)); retval.theId = (identifier90!=null?identifier90.theId:null); 

            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

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
    // $ANTLR end "symbolconst"

    public static class charconst_return extends ParserRuleReturnScope {
        public char value;
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "charconst"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:210:1: charconst returns [ char value ] : charLiteral ;
    public final LLVMParser.charconst_return charconst() throws RecognitionException {
        LLVMParser.charconst_return retval = new LLVMParser.charconst_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        LLVMParser.charLiteral_return charLiteral91 = null;



        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:210:34: ( charLiteral )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:211:2: charLiteral
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_charLiteral_in_charconst1095);
            charLiteral91=charLiteral();

            state._fsp--;

            adaptor.addChild(root_0, charLiteral91.getTree());
             
            		retval.value = (charLiteral91!=null?charLiteral91.theText:null).charAt(0);
            	

            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

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
    // $ANTLR end "charconst"

    public static class stringconst_return extends ParserRuleReturnScope {
        public String value;
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "stringconst"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:216:1: stringconst returns [ String value ] : cstringLiteral ;
    public final LLVMParser.stringconst_return stringconst() throws RecognitionException {
        LLVMParser.stringconst_return retval = new LLVMParser.stringconst_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        LLVMParser.cstringLiteral_return cstringLiteral92 = null;



        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:216:39: ( cstringLiteral )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:217:2: cstringLiteral
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_cstringLiteral_in_stringconst1112);
            cstringLiteral92=cstringLiteral();

            state._fsp--;

            adaptor.addChild(root_0, cstringLiteral92.getTree());

            		retval.value = (cstringLiteral92!=null?cstringLiteral92.theText:null);
            	

            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

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
    // $ANTLR end "stringconst"

    public static class structconst_return extends ParserRuleReturnScope {
        public LLOperand[] values;
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "structconst"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:222:1: structconst returns [ LLOperand[] values ] : '{' (t0= typedop ( ',' t1= typedop )* )? '}' ;
    public final LLVMParser.structconst_return structconst() throws RecognitionException {
        LLVMParser.structconst_return retval = new LLVMParser.structconst_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token char_literal93=null;
        Token char_literal94=null;
        Token char_literal95=null;
        LLVMParser.typedop_return t0 = null;

        LLVMParser.typedop_return t1 = null;


        CommonTree char_literal93_tree=null;
        CommonTree char_literal94_tree=null;
        CommonTree char_literal95_tree=null;


            List<LLOperand> ops = new ArrayList<LLOperand>();
          
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:229:3: ( '{' (t0= typedop ( ',' t1= typedop )* )? '}' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:230:3: '{' (t0= typedop ( ',' t1= typedop )* )? '}'
            {
            root_0 = (CommonTree)adaptor.nil();

            char_literal93=(Token)match(input,30,FOLLOW_30_in_structconst1149); 
            char_literal93_tree = (CommonTree)adaptor.create(char_literal93);
            adaptor.addChild(root_0, char_literal93_tree);

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:230:7: (t0= typedop ( ',' t1= typedop )* )?
            int alt13=2;
            int LA13_0 = input.LA(1);

            if ( ((LA13_0>=INT_TYPE && LA13_0<=QUOTED_ID)||(LA13_0>=27 && LA13_0<=28)||LA13_0==30||LA13_0==32) ) {
                alt13=1;
            }
            switch (alt13) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:230:8: t0= typedop ( ',' t1= typedop )*
                    {
                    pushFollow(FOLLOW_typedop_in_structconst1154);
                    t0=typedop();

                    state._fsp--;

                    adaptor.addChild(root_0, t0.getTree());
                     ops.add((t0!=null?t0.op:null)); 
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:231:5: ( ',' t1= typedop )*
                    loop12:
                    do {
                        int alt12=2;
                        int LA12_0 = input.LA(1);

                        if ( (LA12_0==37) ) {
                            alt12=1;
                        }


                        switch (alt12) {
                    	case 1 :
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:231:6: ',' t1= typedop
                    	    {
                    	    char_literal94=(Token)match(input,37,FOLLOW_37_in_structconst1164); 
                    	    char_literal94_tree = (CommonTree)adaptor.create(char_literal94);
                    	    adaptor.addChild(root_0, char_literal94_tree);

                    	    pushFollow(FOLLOW_typedop_in_structconst1168);
                    	    t1=typedop();

                    	    state._fsp--;

                    	    adaptor.addChild(root_0, t1.getTree());
                    	     ops.add((t1!=null?t1.op:null)); 

                    	    }
                    	    break;

                    	default :
                    	    break loop12;
                        }
                    } while (true);


                    }
                    break;

            }

            char_literal95=(Token)match(input,31,FOLLOW_31_in_structconst1189); 
            char_literal95_tree = (CommonTree)adaptor.create(char_literal95);
            adaptor.addChild(root_0, char_literal95_tree);


            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);


                retval.values = ops.toArray(new LLOperand[ops.size()]);
              
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
    // $ANTLR end "structconst"

    public static class arrayconst_return extends ParserRuleReturnScope {
        public LLOperand[] values;
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "arrayconst"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:238:1: arrayconst returns [ LLOperand[] values ] : '[' (t0= typedop ( ',' t1= typedop )* )? ']' ;
    public final LLVMParser.arrayconst_return arrayconst() throws RecognitionException {
        LLVMParser.arrayconst_return retval = new LLVMParser.arrayconst_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token char_literal96=null;
        Token char_literal97=null;
        Token char_literal98=null;
        LLVMParser.typedop_return t0 = null;

        LLVMParser.typedop_return t1 = null;


        CommonTree char_literal96_tree=null;
        CommonTree char_literal97_tree=null;
        CommonTree char_literal98_tree=null;


            List<LLOperand> ops = new ArrayList<LLOperand>();
          
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:245:3: ( '[' (t0= typedop ( ',' t1= typedop )* )? ']' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:246:3: '[' (t0= typedop ( ',' t1= typedop )* )? ']'
            {
            root_0 = (CommonTree)adaptor.nil();

            char_literal96=(Token)match(input,32,FOLLOW_32_in_arrayconst1227); 
            char_literal96_tree = (CommonTree)adaptor.create(char_literal96);
            adaptor.addChild(root_0, char_literal96_tree);

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:246:7: (t0= typedop ( ',' t1= typedop )* )?
            int alt15=2;
            int LA15_0 = input.LA(1);

            if ( ((LA15_0>=INT_TYPE && LA15_0<=QUOTED_ID)||(LA15_0>=27 && LA15_0<=28)||LA15_0==30||LA15_0==32) ) {
                alt15=1;
            }
            switch (alt15) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:246:8: t0= typedop ( ',' t1= typedop )*
                    {
                    pushFollow(FOLLOW_typedop_in_arrayconst1232);
                    t0=typedop();

                    state._fsp--;

                    adaptor.addChild(root_0, t0.getTree());
                     ops.add((t0!=null?t0.op:null)); 
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:247:5: ( ',' t1= typedop )*
                    loop14:
                    do {
                        int alt14=2;
                        int LA14_0 = input.LA(1);

                        if ( (LA14_0==37) ) {
                            alt14=1;
                        }


                        switch (alt14) {
                    	case 1 :
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:247:6: ',' t1= typedop
                    	    {
                    	    char_literal97=(Token)match(input,37,FOLLOW_37_in_arrayconst1242); 
                    	    char_literal97_tree = (CommonTree)adaptor.create(char_literal97);
                    	    adaptor.addChild(root_0, char_literal97_tree);

                    	    pushFollow(FOLLOW_typedop_in_arrayconst1246);
                    	    t1=typedop();

                    	    state._fsp--;

                    	    adaptor.addChild(root_0, t1.getTree());
                    	     ops.add((t1!=null?t1.op:null)); 

                    	    }
                    	    break;

                    	default :
                    	    break loop14;
                        }
                    } while (true);


                    }
                    break;

            }

            char_literal98=(Token)match(input,34,FOLLOW_34_in_arrayconst1267); 
            char_literal98_tree = (CommonTree)adaptor.create(char_literal98);
            adaptor.addChild(root_0, char_literal98_tree);


            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);


                retval.values = ops.toArray(new LLOperand[ops.size()]);
              
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
    // $ANTLR end "arrayconst"

    public static class identifier_return extends ParserRuleReturnScope {
        public String theId;
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "identifier"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:253:1: identifier returns [String theId] : ( NAMED_ID | UNNAMED_ID | QUOTED_ID ) ;
    public final LLVMParser.identifier_return identifier() throws RecognitionException {
        LLVMParser.identifier_return retval = new LLVMParser.identifier_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token NAMED_ID99=null;
        Token UNNAMED_ID100=null;
        Token QUOTED_ID101=null;

        CommonTree NAMED_ID99_tree=null;
        CommonTree UNNAMED_ID100_tree=null;
        CommonTree QUOTED_ID101_tree=null;

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:253:35: ( ( NAMED_ID | UNNAMED_ID | QUOTED_ID ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:254:3: ( NAMED_ID | UNNAMED_ID | QUOTED_ID )
            {
            root_0 = (CommonTree)adaptor.nil();

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:254:3: ( NAMED_ID | UNNAMED_ID | QUOTED_ID )
            int alt16=3;
            switch ( input.LA(1) ) {
            case NAMED_ID:
                {
                alt16=1;
                }
                break;
            case UNNAMED_ID:
                {
                alt16=2;
                }
                break;
            case QUOTED_ID:
                {
                alt16=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 16, 0, input);

                throw nvae;
            }

            switch (alt16) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:255:2: NAMED_ID
                    {
                    NAMED_ID99=(Token)match(input,NAMED_ID,FOLLOW_NAMED_ID_in_identifier1291); 
                    NAMED_ID99_tree = (CommonTree)adaptor.create(NAMED_ID99);
                    adaptor.addChild(root_0, NAMED_ID99_tree);

                     retval.theId = (NAMED_ID99!=null?NAMED_ID99.getText():null); 

                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:256:5: UNNAMED_ID
                    {
                    UNNAMED_ID100=(Token)match(input,UNNAMED_ID,FOLLOW_UNNAMED_ID_in_identifier1302); 
                    UNNAMED_ID100_tree = (CommonTree)adaptor.create(UNNAMED_ID100);
                    adaptor.addChild(root_0, UNNAMED_ID100_tree);

                     retval.theId = (UNNAMED_ID100!=null?UNNAMED_ID100.getText():null); 

                    }
                    break;
                case 3 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:257:5: QUOTED_ID
                    {
                    QUOTED_ID101=(Token)match(input,QUOTED_ID,FOLLOW_QUOTED_ID_in_identifier1310); 
                    QUOTED_ID101_tree = (CommonTree)adaptor.create(QUOTED_ID101);
                    adaptor.addChild(root_0, QUOTED_ID101_tree);

                     retval.theId = (QUOTED_ID101!=null?QUOTED_ID101.getText():null).substring(0,1) 
                      						+ helper.unescape((QUOTED_ID101!=null?QUOTED_ID101.getText():null).substring(1), '"'); 

                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

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
    // $ANTLR end "identifier"

    public static class number_return extends ParserRuleReturnScope {
        public int value;
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "number"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:262:1: number returns [int value] : NUMBER ;
    public final LLVMParser.number_return number() throws RecognitionException {
        LLVMParser.number_return retval = new LLVMParser.number_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token NUMBER102=null;

        CommonTree NUMBER102_tree=null;

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:262:28: ( NUMBER )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:262:30: NUMBER
            {
            root_0 = (CommonTree)adaptor.nil();

            NUMBER102=(Token)match(input,NUMBER,FOLLOW_NUMBER_in_number1332); 
            NUMBER102_tree = (CommonTree)adaptor.create(NUMBER102);
            adaptor.addChild(root_0, NUMBER102_tree);

             retval.value = Integer.parseInt((NUMBER102!=null?NUMBER102.getText():null)); 

            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

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
    // $ANTLR end "number"

    public static class charLiteral_return extends ParserRuleReturnScope {
        public String theText;
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "charLiteral"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:265:1: charLiteral returns [String theText] : CHAR_LITERAL ;
    public final LLVMParser.charLiteral_return charLiteral() throws RecognitionException {
        LLVMParser.charLiteral_return retval = new LLVMParser.charLiteral_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token CHAR_LITERAL103=null;

        CommonTree CHAR_LITERAL103_tree=null;

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:265:38: ( CHAR_LITERAL )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:265:40: CHAR_LITERAL
            {
            root_0 = (CommonTree)adaptor.nil();

            CHAR_LITERAL103=(Token)match(input,CHAR_LITERAL,FOLLOW_CHAR_LITERAL_in_charLiteral1350); 
            CHAR_LITERAL103_tree = (CommonTree)adaptor.create(CHAR_LITERAL103);
            adaptor.addChild(root_0, CHAR_LITERAL103_tree);

             
              retval.theText = LLParserHelper.unescape((CHAR_LITERAL103!=null?CHAR_LITERAL103.getText():null), '\'');
              

            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

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
    // $ANTLR end "charLiteral"

    public static class stringLiteral_return extends ParserRuleReturnScope {
        public String theText;
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "stringLiteral"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:271:1: stringLiteral returns [String theText] : STRING_LITERAL ;
    public final LLVMParser.stringLiteral_return stringLiteral() throws RecognitionException {
        LLVMParser.stringLiteral_return retval = new LLVMParser.stringLiteral_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token STRING_LITERAL104=null;

        CommonTree STRING_LITERAL104_tree=null;

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:271:40: ( STRING_LITERAL )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:271:42: STRING_LITERAL
            {
            root_0 = (CommonTree)adaptor.nil();

            STRING_LITERAL104=(Token)match(input,STRING_LITERAL,FOLLOW_STRING_LITERAL_in_stringLiteral1370); 
            STRING_LITERAL104_tree = (CommonTree)adaptor.create(STRING_LITERAL104);
            adaptor.addChild(root_0, STRING_LITERAL104_tree);


              retval.theText = LLParserHelper.unescape((STRING_LITERAL104!=null?STRING_LITERAL104.getText():null), '"');
              

            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

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
    // $ANTLR end "stringLiteral"

    public static class cstringLiteral_return extends ParserRuleReturnScope {
        public String theText;
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "cstringLiteral"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:277:1: cstringLiteral returns [String theText] : CSTRING_LITERAL ;
    public final LLVMParser.cstringLiteral_return cstringLiteral() throws RecognitionException {
        LLVMParser.cstringLiteral_return retval = new LLVMParser.cstringLiteral_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token CSTRING_LITERAL105=null;

        CommonTree CSTRING_LITERAL105_tree=null;

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:277:41: ( CSTRING_LITERAL )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:277:43: CSTRING_LITERAL
            {
            root_0 = (CommonTree)adaptor.nil();

            CSTRING_LITERAL105=(Token)match(input,CSTRING_LITERAL,FOLLOW_CSTRING_LITERAL_in_cstringLiteral1391); 
            CSTRING_LITERAL105_tree = (CommonTree)adaptor.create(CSTRING_LITERAL105);
            adaptor.addChild(root_0, CSTRING_LITERAL105_tree);


              retval.theText = LLParserHelper.unescape((CSTRING_LITERAL105!=null?CSTRING_LITERAL105.getText():null).substring(1), '"');
              

            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

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
    // $ANTLR end "cstringLiteral"

    public static class defineDirective_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "defineDirective"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:283:1: defineDirective : DEFINE ( linkage )? ( visibility )? ( cconv )? attrs type identifier arglist fn_attrs NEWLINE '{' NEWLINE defineStmts '}' ;
    public final LLVMParser.defineDirective_return defineDirective() throws RecognitionException {
        LLVMParser.defineDirective_return retval = new LLVMParser.defineDirective_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token DEFINE106=null;
        Token NEWLINE115=null;
        Token char_literal116=null;
        Token NEWLINE117=null;
        Token char_literal119=null;
        LLVMParser.linkage_return linkage107 = null;

        LLVMParser.visibility_return visibility108 = null;

        LLVMParser.cconv_return cconv109 = null;

        LLVMParser.attrs_return attrs110 = null;

        LLVMParser.type_return type111 = null;

        LLVMParser.identifier_return identifier112 = null;

        LLVMParser.arglist_return arglist113 = null;

        LLVMParser.fn_attrs_return fn_attrs114 = null;

        LLVMParser.defineStmts_return defineStmts118 = null;


        CommonTree DEFINE106_tree=null;
        CommonTree NEWLINE115_tree=null;
        CommonTree char_literal116_tree=null;
        CommonTree NEWLINE117_tree=null;
        CommonTree char_literal119_tree=null;

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:283:17: ( DEFINE ( linkage )? ( visibility )? ( cconv )? attrs type identifier arglist fn_attrs NEWLINE '{' NEWLINE defineStmts '}' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:283:19: DEFINE ( linkage )? ( visibility )? ( cconv )? attrs type identifier arglist fn_attrs NEWLINE '{' NEWLINE defineStmts '}'
            {
            root_0 = (CommonTree)adaptor.nil();

            DEFINE106=(Token)match(input,DEFINE,FOLLOW_DEFINE_in_defineDirective1406); 
            DEFINE106_tree = (CommonTree)adaptor.create(DEFINE106);
            adaptor.addChild(root_0, DEFINE106_tree);

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:283:26: ( linkage )?
            int alt17=2;
            int LA17_0 = input.LA(1);

            if ( ((LA17_0>=41 && LA17_0<=54)) ) {
                alt17=1;
            }
            switch (alt17) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:283:26: linkage
                    {
                    pushFollow(FOLLOW_linkage_in_defineDirective1408);
                    linkage107=linkage();

                    state._fsp--;

                    adaptor.addChild(root_0, linkage107.getTree());

                    }
                    break;

            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:283:35: ( visibility )?
            int alt18=2;
            int LA18_0 = input.LA(1);

            if ( ((LA18_0>=69 && LA18_0<=71)) ) {
                alt18=1;
            }
            switch (alt18) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:283:35: visibility
                    {
                    pushFollow(FOLLOW_visibility_in_defineDirective1411);
                    visibility108=visibility();

                    state._fsp--;

                    adaptor.addChild(root_0, visibility108.getTree());

                    }
                    break;

            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:283:47: ( cconv )?
            int alt19=2;
            int LA19_0 = input.LA(1);

            if ( ((LA19_0>=72 && LA19_0<=76)) ) {
                alt19=1;
            }
            switch (alt19) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:283:47: cconv
                    {
                    pushFollow(FOLLOW_cconv_in_defineDirective1414);
                    cconv109=cconv();

                    state._fsp--;

                    adaptor.addChild(root_0, cconv109.getTree());

                    }
                    break;

            }

            pushFollow(FOLLOW_attrs_in_defineDirective1417);
            attrs110=attrs();

            state._fsp--;

            adaptor.addChild(root_0, attrs110.getTree());
            pushFollow(FOLLOW_type_in_defineDirective1419);
            type111=type();

            state._fsp--;

            adaptor.addChild(root_0, type111.getTree());
            pushFollow(FOLLOW_identifier_in_defineDirective1421);
            identifier112=identifier();

            state._fsp--;

            adaptor.addChild(root_0, identifier112.getTree());
            pushFollow(FOLLOW_arglist_in_defineDirective1423);
            arglist113=arglist();

            state._fsp--;

            adaptor.addChild(root_0, arglist113.getTree());
            pushFollow(FOLLOW_fn_attrs_in_defineDirective1425);
            fn_attrs114=fn_attrs();

            state._fsp--;

            adaptor.addChild(root_0, fn_attrs114.getTree());
            NEWLINE115=(Token)match(input,NEWLINE,FOLLOW_NEWLINE_in_defineDirective1427); 
            NEWLINE115_tree = (CommonTree)adaptor.create(NEWLINE115);
            adaptor.addChild(root_0, NEWLINE115_tree);


                helper.openNewDefine(
                  (identifier112!=null?identifier112.theId:null),
                    (linkage107!=null?linkage107.value:null), (visibility108!=null?visibility108.vis:null), (cconv109!=null?input.toString(cconv109.start,cconv109.stop):null), 
                    new LLAttrType(new LLAttrs((attrs110!=null?attrs110.attrs:null)), (type111!=null?type111.theType:null)),
                    (arglist113!=null?arglist113.argAttrs:null), new LLFuncAttrs((fn_attrs114!=null?fn_attrs114.attrs:null)),
                    null, //section
                    0, //align
                    null //gc
                    );
                
            char_literal116=(Token)match(input,30,FOLLOW_30_in_defineDirective1446); 
            char_literal116_tree = (CommonTree)adaptor.create(char_literal116);
            adaptor.addChild(root_0, char_literal116_tree);

            NEWLINE117=(Token)match(input,NEWLINE,FOLLOW_NEWLINE_in_defineDirective1448); 
            NEWLINE117_tree = (CommonTree)adaptor.create(NEWLINE117);
            adaptor.addChild(root_0, NEWLINE117_tree);

            pushFollow(FOLLOW_defineStmts_in_defineDirective1454);
            defineStmts118=defineStmts();

            state._fsp--;

            adaptor.addChild(root_0, defineStmts118.getTree());
            char_literal119=(Token)match(input,31,FOLLOW_31_in_defineDirective1461); 
            char_literal119_tree = (CommonTree)adaptor.create(char_literal119);
            adaptor.addChild(root_0, char_literal119_tree);


                helper.closeDefine();
                

            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

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
    // $ANTLR end "defineDirective"

    public static class visibility_return extends ParserRuleReturnScope {
        public LLVisibility vis;
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "visibility"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:306:1: visibility returns [LLVisibility vis] : ( 'default' | 'hidden' | 'protected' ) ;
    public final LLVMParser.visibility_return visibility() throws RecognitionException {
        LLVMParser.visibility_return retval = new LLVMParser.visibility_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token set120=null;

        CommonTree set120_tree=null;

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:306:39: ( ( 'default' | 'hidden' | 'protected' ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:306:41: ( 'default' | 'hidden' | 'protected' )
            {
            root_0 = (CommonTree)adaptor.nil();

            set120=(Token)input.LT(1);
            if ( (input.LA(1)>=69 && input.LA(1)<=71) ) {
                input.consume();
                adaptor.addChild(root_0, (CommonTree)adaptor.create(set120));
                state.errorRecovery=false;
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }

             retval.vis = LLVisibility.getForToken(input.toString(retval.start,input.LT(-1))); 

            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

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
    // $ANTLR end "visibility"

    public static class cconv_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "cconv"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:309:1: cconv : ( 'ccc' | 'fastcc' | 'coldcc' | 'cc 10' | 'cc' number ) ;
    public final LLVMParser.cconv_return cconv() throws RecognitionException {
        LLVMParser.cconv_return retval = new LLVMParser.cconv_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token string_literal121=null;
        Token string_literal122=null;
        Token string_literal123=null;
        Token string_literal124=null;
        Token string_literal125=null;
        LLVMParser.number_return number126 = null;


        CommonTree string_literal121_tree=null;
        CommonTree string_literal122_tree=null;
        CommonTree string_literal123_tree=null;
        CommonTree string_literal124_tree=null;
        CommonTree string_literal125_tree=null;

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:309:7: ( ( 'ccc' | 'fastcc' | 'coldcc' | 'cc 10' | 'cc' number ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:309:9: ( 'ccc' | 'fastcc' | 'coldcc' | 'cc 10' | 'cc' number )
            {
            root_0 = (CommonTree)adaptor.nil();

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:309:9: ( 'ccc' | 'fastcc' | 'coldcc' | 'cc 10' | 'cc' number )
            int alt20=5;
            switch ( input.LA(1) ) {
            case 72:
                {
                alt20=1;
                }
                break;
            case 73:
                {
                alt20=2;
                }
                break;
            case 74:
                {
                alt20=3;
                }
                break;
            case 75:
                {
                alt20=4;
                }
                break;
            case 76:
                {
                alt20=5;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 20, 0, input);

                throw nvae;
            }

            switch (alt20) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:309:10: 'ccc'
                    {
                    string_literal121=(Token)match(input,72,FOLLOW_72_in_cconv1520); 
                    string_literal121_tree = (CommonTree)adaptor.create(string_literal121);
                    adaptor.addChild(root_0, string_literal121_tree);


                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:309:18: 'fastcc'
                    {
                    string_literal122=(Token)match(input,73,FOLLOW_73_in_cconv1524); 
                    string_literal122_tree = (CommonTree)adaptor.create(string_literal122);
                    adaptor.addChild(root_0, string_literal122_tree);


                    }
                    break;
                case 3 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:309:29: 'coldcc'
                    {
                    string_literal123=(Token)match(input,74,FOLLOW_74_in_cconv1528); 
                    string_literal123_tree = (CommonTree)adaptor.create(string_literal123);
                    adaptor.addChild(root_0, string_literal123_tree);


                    }
                    break;
                case 4 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:309:40: 'cc 10'
                    {
                    string_literal124=(Token)match(input,75,FOLLOW_75_in_cconv1532); 
                    string_literal124_tree = (CommonTree)adaptor.create(string_literal124);
                    adaptor.addChild(root_0, string_literal124_tree);


                    }
                    break;
                case 5 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:309:50: 'cc' number
                    {
                    string_literal125=(Token)match(input,76,FOLLOW_76_in_cconv1536); 
                    string_literal125_tree = (CommonTree)adaptor.create(string_literal125);
                    adaptor.addChild(root_0, string_literal125_tree);

                    pushFollow(FOLLOW_number_in_cconv1538);
                    number126=number();

                    state._fsp--;

                    adaptor.addChild(root_0, number126.getTree());

                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

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
    // $ANTLR end "cconv"

    public static class attrs_return extends ParserRuleReturnScope {
        public String[] attrs;
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "attrs"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:312:1: attrs returns [String[] attrs] : ( attr )* ;
    public final LLVMParser.attrs_return attrs() throws RecognitionException {
        LLVMParser.attrs_return retval = new LLVMParser.attrs_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        LLVMParser.attr_return attr127 = null;




            List<String> attrs = new ArrayList<String>();
          
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:320:3: ( ( attr )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:320:5: ( attr )*
            {
            root_0 = (CommonTree)adaptor.nil();

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:320:5: ( attr )*
            loop21:
            do {
                int alt21=2;
                int LA21_0 = input.LA(1);

                if ( ((LA21_0>=77 && LA21_0<=84)) ) {
                    alt21=1;
                }


                switch (alt21) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:320:7: attr
            	    {
            	    pushFollow(FOLLOW_attr_in_attrs1577);
            	    attr127=attr();

            	    state._fsp--;

            	    adaptor.addChild(root_0, attr127.getTree());
            	     attrs.add((attr127!=null?input.toString(attr127.start,attr127.stop):null)); 

            	    }
            	    break;

            	default :
            	    break loop21;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);


                retval.attrs = attrs.toArray(new String[attrs.size()]);
              
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

    public static class attr_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "attr"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:323:1: attr : ( 'zeroext' | 'signext' | 'inreg' | 'byval' | 'sret' | 'noalias' | 'nocapture' | 'nest' );
    public final LLVMParser.attr_return attr() throws RecognitionException {
        LLVMParser.attr_return retval = new LLVMParser.attr_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token set128=null;

        CommonTree set128_tree=null;

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:323:6: ( 'zeroext' | 'signext' | 'inreg' | 'byval' | 'sret' | 'noalias' | 'nocapture' | 'nest' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:
            {
            root_0 = (CommonTree)adaptor.nil();

            set128=(Token)input.LT(1);
            if ( (input.LA(1)>=77 && input.LA(1)<=84) ) {
                input.consume();
                adaptor.addChild(root_0, (CommonTree)adaptor.create(set128));
                state.errorRecovery=false;
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

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
    // $ANTLR end "attr"

    public static class fn_attrs_return extends ParserRuleReturnScope {
        public String[] attrs;
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "fn_attrs"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:326:1: fn_attrs returns [String[] attrs] : ( fn_attr )* ;
    public final LLVMParser.fn_attrs_return fn_attrs() throws RecognitionException {
        LLVMParser.fn_attrs_return retval = new LLVMParser.fn_attrs_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        LLVMParser.fn_attr_return fn_attr129 = null;




            List<String> attrs = new ArrayList<String>();
          
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:334:3: ( ( fn_attr )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:334:5: ( fn_attr )*
            {
            root_0 = (CommonTree)adaptor.nil();

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:334:5: ( fn_attr )*
            loop22:
            do {
                int alt22=2;
                int LA22_0 = input.LA(1);

                if ( ((LA22_0>=85 && LA22_0<=98)) ) {
                    alt22=1;
                }


                switch (alt22) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:334:7: fn_attr
            	    {
            	    pushFollow(FOLLOW_fn_attr_in_fn_attrs1656);
            	    fn_attr129=fn_attr();

            	    state._fsp--;

            	    adaptor.addChild(root_0, fn_attr129.getTree());
            	     attrs.add((fn_attr129!=null?input.toString(fn_attr129.start,fn_attr129.stop):null)); 

            	    }
            	    break;

            	default :
            	    break loop22;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);


                retval.attrs = attrs.toArray(new String[attrs.size()]);
              
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
    // $ANTLR end "fn_attrs"

    public static class fn_attr_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "fn_attr"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:337:1: fn_attr : ( ( 'alignstack' '(' number ')' ) | 'alwaysinline' | 'inlinehint' | 'noinline' | 'optsize' | 'noreturn' | 'nounwind' | 'readnone' | 'readonly' | 'ssp' | 'sspreq' | 'noredzone' | 'noimplicitfloat' | 'naked' );
    public final LLVMParser.fn_attr_return fn_attr() throws RecognitionException {
        LLVMParser.fn_attr_return retval = new LLVMParser.fn_attr_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token string_literal130=null;
        Token char_literal131=null;
        Token char_literal133=null;
        Token string_literal134=null;
        Token string_literal135=null;
        Token string_literal136=null;
        Token string_literal137=null;
        Token string_literal138=null;
        Token string_literal139=null;
        Token string_literal140=null;
        Token string_literal141=null;
        Token string_literal142=null;
        Token string_literal143=null;
        Token string_literal144=null;
        Token string_literal145=null;
        Token string_literal146=null;
        LLVMParser.number_return number132 = null;


        CommonTree string_literal130_tree=null;
        CommonTree char_literal131_tree=null;
        CommonTree char_literal133_tree=null;
        CommonTree string_literal134_tree=null;
        CommonTree string_literal135_tree=null;
        CommonTree string_literal136_tree=null;
        CommonTree string_literal137_tree=null;
        CommonTree string_literal138_tree=null;
        CommonTree string_literal139_tree=null;
        CommonTree string_literal140_tree=null;
        CommonTree string_literal141_tree=null;
        CommonTree string_literal142_tree=null;
        CommonTree string_literal143_tree=null;
        CommonTree string_literal144_tree=null;
        CommonTree string_literal145_tree=null;
        CommonTree string_literal146_tree=null;

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:337:9: ( ( 'alignstack' '(' number ')' ) | 'alwaysinline' | 'inlinehint' | 'noinline' | 'optsize' | 'noreturn' | 'nounwind' | 'readnone' | 'readonly' | 'ssp' | 'sspreq' | 'noredzone' | 'noimplicitfloat' | 'naked' )
            int alt23=14;
            switch ( input.LA(1) ) {
            case 85:
                {
                alt23=1;
                }
                break;
            case 86:
                {
                alt23=2;
                }
                break;
            case 87:
                {
                alt23=3;
                }
                break;
            case 88:
                {
                alt23=4;
                }
                break;
            case 89:
                {
                alt23=5;
                }
                break;
            case 90:
                {
                alt23=6;
                }
                break;
            case 91:
                {
                alt23=7;
                }
                break;
            case 92:
                {
                alt23=8;
                }
                break;
            case 93:
                {
                alt23=9;
                }
                break;
            case 94:
                {
                alt23=10;
                }
                break;
            case 95:
                {
                alt23=11;
                }
                break;
            case 96:
                {
                alt23=12;
                }
                break;
            case 97:
                {
                alt23=13;
                }
                break;
            case 98:
                {
                alt23=14;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 23, 0, input);

                throw nvae;
            }

            switch (alt23) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:337:11: ( 'alignstack' '(' number ')' )
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:337:11: ( 'alignstack' '(' number ')' )
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:337:13: 'alignstack' '(' number ')'
                    {
                    string_literal130=(Token)match(input,85,FOLLOW_85_in_fn_attr1675); 
                    string_literal130_tree = (CommonTree)adaptor.create(string_literal130);
                    adaptor.addChild(root_0, string_literal130_tree);

                    char_literal131=(Token)match(input,35,FOLLOW_35_in_fn_attr1677); 
                    char_literal131_tree = (CommonTree)adaptor.create(char_literal131);
                    adaptor.addChild(root_0, char_literal131_tree);

                    pushFollow(FOLLOW_number_in_fn_attr1679);
                    number132=number();

                    state._fsp--;

                    adaptor.addChild(root_0, number132.getTree());
                    char_literal133=(Token)match(input,36,FOLLOW_36_in_fn_attr1681); 
                    char_literal133_tree = (CommonTree)adaptor.create(char_literal133);
                    adaptor.addChild(root_0, char_literal133_tree);


                    }


                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:337:45: 'alwaysinline'
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    string_literal134=(Token)match(input,86,FOLLOW_86_in_fn_attr1687); 
                    string_literal134_tree = (CommonTree)adaptor.create(string_literal134);
                    adaptor.addChild(root_0, string_literal134_tree);


                    }
                    break;
                case 3 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:337:62: 'inlinehint'
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    string_literal135=(Token)match(input,87,FOLLOW_87_in_fn_attr1691); 
                    string_literal135_tree = (CommonTree)adaptor.create(string_literal135);
                    adaptor.addChild(root_0, string_literal135_tree);


                    }
                    break;
                case 4 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:337:77: 'noinline'
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    string_literal136=(Token)match(input,88,FOLLOW_88_in_fn_attr1695); 
                    string_literal136_tree = (CommonTree)adaptor.create(string_literal136);
                    adaptor.addChild(root_0, string_literal136_tree);


                    }
                    break;
                case 5 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:337:90: 'optsize'
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    string_literal137=(Token)match(input,89,FOLLOW_89_in_fn_attr1699); 
                    string_literal137_tree = (CommonTree)adaptor.create(string_literal137);
                    adaptor.addChild(root_0, string_literal137_tree);


                    }
                    break;
                case 6 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:338:7: 'noreturn'
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    string_literal138=(Token)match(input,90,FOLLOW_90_in_fn_attr1708); 
                    string_literal138_tree = (CommonTree)adaptor.create(string_literal138);
                    adaptor.addChild(root_0, string_literal138_tree);


                    }
                    break;
                case 7 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:338:20: 'nounwind'
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    string_literal139=(Token)match(input,91,FOLLOW_91_in_fn_attr1712); 
                    string_literal139_tree = (CommonTree)adaptor.create(string_literal139);
                    adaptor.addChild(root_0, string_literal139_tree);


                    }
                    break;
                case 8 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:338:33: 'readnone'
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    string_literal140=(Token)match(input,92,FOLLOW_92_in_fn_attr1716); 
                    string_literal140_tree = (CommonTree)adaptor.create(string_literal140);
                    adaptor.addChild(root_0, string_literal140_tree);


                    }
                    break;
                case 9 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:338:46: 'readonly'
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    string_literal141=(Token)match(input,93,FOLLOW_93_in_fn_attr1720); 
                    string_literal141_tree = (CommonTree)adaptor.create(string_literal141);
                    adaptor.addChild(root_0, string_literal141_tree);


                    }
                    break;
                case 10 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:338:59: 'ssp'
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    string_literal142=(Token)match(input,94,FOLLOW_94_in_fn_attr1724); 
                    string_literal142_tree = (CommonTree)adaptor.create(string_literal142);
                    adaptor.addChild(root_0, string_literal142_tree);


                    }
                    break;
                case 11 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:338:67: 'sspreq'
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    string_literal143=(Token)match(input,95,FOLLOW_95_in_fn_attr1728); 
                    string_literal143_tree = (CommonTree)adaptor.create(string_literal143);
                    adaptor.addChild(root_0, string_literal143_tree);


                    }
                    break;
                case 12 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:338:78: 'noredzone'
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    string_literal144=(Token)match(input,96,FOLLOW_96_in_fn_attr1732); 
                    string_literal144_tree = (CommonTree)adaptor.create(string_literal144);
                    adaptor.addChild(root_0, string_literal144_tree);


                    }
                    break;
                case 13 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:338:92: 'noimplicitfloat'
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    string_literal145=(Token)match(input,97,FOLLOW_97_in_fn_attr1736); 
                    string_literal145_tree = (CommonTree)adaptor.create(string_literal145);
                    adaptor.addChild(root_0, string_literal145_tree);


                    }
                    break;
                case 14 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:338:112: 'naked'
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    string_literal146=(Token)match(input,98,FOLLOW_98_in_fn_attr1740); 
                    string_literal146_tree = (CommonTree)adaptor.create(string_literal146);
                    adaptor.addChild(root_0, string_literal146_tree);


                    }
                    break;

            }
            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

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
    // $ANTLR end "fn_attr"

    public static class arglist_return extends ParserRuleReturnScope {
        public LLArgAttrType[] argAttrs;
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "arglist"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:344:1: arglist returns [ LLArgAttrType[] argAttrs ] : '(' (f0= funcarg ( ',' f1= funcarg )* )? ')' ;
    public final LLVMParser.arglist_return arglist() throws RecognitionException {
        LLVMParser.arglist_return retval = new LLVMParser.arglist_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token char_literal147=null;
        Token char_literal148=null;
        Token char_literal149=null;
        LLVMParser.funcarg_return f0 = null;

        LLVMParser.funcarg_return f1 = null;


        CommonTree char_literal147_tree=null;
        CommonTree char_literal148_tree=null;
        CommonTree char_literal149_tree=null;


            List<LLArgAttrType> attrs = new ArrayList<LLArgAttrType>();
          
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:352:3: ( '(' (f0= funcarg ( ',' f1= funcarg )* )? ')' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:352:5: '(' (f0= funcarg ( ',' f1= funcarg )* )? ')'
            {
            root_0 = (CommonTree)adaptor.nil();

            char_literal147=(Token)match(input,35,FOLLOW_35_in_arglist1779); 
            char_literal147_tree = (CommonTree)adaptor.create(char_literal147);
            adaptor.addChild(root_0, char_literal147_tree);

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:353:7: (f0= funcarg ( ',' f1= funcarg )* )?
            int alt25=2;
            int LA25_0 = input.LA(1);

            if ( ((LA25_0>=INT_TYPE && LA25_0<=QUOTED_ID)||(LA25_0>=27 && LA25_0<=28)||LA25_0==30||LA25_0==32) ) {
                alt25=1;
            }
            switch (alt25) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:353:9: f0= funcarg ( ',' f1= funcarg )*
                    {
                    pushFollow(FOLLOW_funcarg_in_arglist1792);
                    f0=funcarg();

                    state._fsp--;

                    adaptor.addChild(root_0, f0.getTree());
                     attrs.add((f0!=null?f0.argAttr:null)); 
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:354:9: ( ',' f1= funcarg )*
                    loop24:
                    do {
                        int alt24=2;
                        int LA24_0 = input.LA(1);

                        if ( (LA24_0==37) ) {
                            alt24=1;
                        }


                        switch (alt24) {
                    	case 1 :
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:354:11: ',' f1= funcarg
                    	    {
                    	    char_literal148=(Token)match(input,37,FOLLOW_37_in_arglist1814); 
                    	    char_literal148_tree = (CommonTree)adaptor.create(char_literal148);
                    	    adaptor.addChild(root_0, char_literal148_tree);

                    	    pushFollow(FOLLOW_funcarg_in_arglist1818);
                    	    f1=funcarg();

                    	    state._fsp--;

                    	    adaptor.addChild(root_0, f1.getTree());
                    	     attrs.add((f1!=null?f1.argAttr:null)); 

                    	    }
                    	    break;

                    	default :
                    	    break loop24;
                        }
                    } while (true);


                    }
                    break;

            }

            char_literal149=(Token)match(input,36,FOLLOW_36_in_arglist1852); 
            char_literal149_tree = (CommonTree)adaptor.create(char_literal149);
            adaptor.addChild(root_0, char_literal149_tree);


            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);


                retval.argAttrs = attrs.toArray(new LLArgAttrType[attrs.size()]);
              
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

    public static class funcarg_return extends ParserRuleReturnScope {
        public LLArgAttrType argAttr;
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "funcarg"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:360:1: funcarg returns [ LLArgAttrType argAttr ] : type attrs identifier ;
    public final LLVMParser.funcarg_return funcarg() throws RecognitionException {
        LLVMParser.funcarg_return retval = new LLVMParser.funcarg_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        LLVMParser.type_return type150 = null;

        LLVMParser.attrs_return attrs151 = null;

        LLVMParser.identifier_return identifier152 = null;



        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:360:43: ( type attrs identifier )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:361:3: type attrs identifier
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_type_in_funcarg1870);
            type150=type();

            state._fsp--;

            adaptor.addChild(root_0, type150.getTree());
            pushFollow(FOLLOW_attrs_in_funcarg1872);
            attrs151=attrs();

            state._fsp--;

            adaptor.addChild(root_0, attrs151.getTree());
            pushFollow(FOLLOW_identifier_in_funcarg1874);
            identifier152=identifier();

            state._fsp--;

            adaptor.addChild(root_0, identifier152.getTree());
             retval.argAttr = new LLArgAttrType((identifier152!=null?identifier152.theId:null).substring(1), new LLAttrs((attrs151!=null?attrs151.attrs:null)), (type150!=null?type150.theType:null)); 

            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

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
    // $ANTLR end "funcarg"

    public static class defineStmts_return extends ParserRuleReturnScope {
        public List<LLBlock> blocks;
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "defineStmts"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:364:1: defineStmts returns [ List<LLBlock> blocks ] : ( block )+ ;
    public final LLVMParser.defineStmts_return defineStmts() throws RecognitionException {
        LLVMParser.defineStmts_return retval = new LLVMParser.defineStmts_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        LLVMParser.block_return block153 = null;




            List<LLBlock> blocks = new ArrayList<LLBlock>();
          
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:372:3: ( ( block )+ )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:372:5: ( block )+
            {
            root_0 = (CommonTree)adaptor.nil();

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:372:5: ( block )+
            int cnt26=0;
            loop26:
            do {
                int alt26=2;
                int LA26_0 = input.LA(1);

                if ( (LA26_0==LABEL) ) {
                    alt26=1;
                }


                switch (alt26) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:372:7: block
            	    {
            	    pushFollow(FOLLOW_block_in_defineStmts1914);
            	    block153=block();

            	    state._fsp--;

            	    adaptor.addChild(root_0, block153.getTree());
            	     blocks.add((block153!=null?block153.block:null)); 

            	    }
            	    break;

            	default :
            	    if ( cnt26 >= 1 ) break loop26;
                        EarlyExitException eee =
                            new EarlyExitException(26, input);
                        throw eee;
                }
                cnt26++;
            } while (true);


            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);


                retval.blocks = blocks;
              
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
    // $ANTLR end "defineStmts"

    public static class block_return extends ParserRuleReturnScope {
        public LLBlock block;
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "block"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:375:1: block returns [ LLBlock block ] : blocklabel ( instr NEWLINE )+ ;
    public final LLVMParser.block_return block() throws RecognitionException {
        LLVMParser.block_return retval = new LLVMParser.block_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token NEWLINE156=null;
        LLVMParser.blocklabel_return blocklabel154 = null;

        LLVMParser.instr_return instr155 = null;


        CommonTree NEWLINE156_tree=null;


            LLBlock block;
          
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:382:3: ( blocklabel ( instr NEWLINE )+ )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:383:3: blocklabel ( instr NEWLINE )+
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_blocklabel_in_block1958);
            blocklabel154=blocklabel();

            state._fsp--;

            adaptor.addChild(root_0, blocklabel154.getTree());
             block = helper.currentTarget.addBlock((blocklabel154!=null?blocklabel154.theSym:null)); 
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:385:3: ( instr NEWLINE )+
            int cnt27=0;
            loop27:
            do {
                int alt27=2;
                int LA27_0 = input.LA(1);

                if ( ((LA27_0>=NAMED_ID && LA27_0<=UNNAMED_ID)||(LA27_0>=101 && LA27_0<=102)||LA27_0==125) ) {
                    alt27=1;
                }


                switch (alt27) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:385:5: instr NEWLINE
            	    {
            	    pushFollow(FOLLOW_instr_in_block1972);
            	    instr155=instr();

            	    state._fsp--;

            	    adaptor.addChild(root_0, instr155.getTree());
            	    NEWLINE156=(Token)match(input,NEWLINE,FOLLOW_NEWLINE_in_block1974); 
            	    NEWLINE156_tree = (CommonTree)adaptor.create(NEWLINE156);
            	    adaptor.addChild(root_0, NEWLINE156_tree);

            	     block.instrs().add((instr155!=null?instr155.inst:null)); System.out.println((instr155!=null?instr155.inst:null)); 

            	    }
            	    break;

            	default :
            	    if ( cnt27 >= 1 ) break loop27;
                        EarlyExitException eee =
                            new EarlyExitException(27, input);
                        throw eee;
                }
                cnt27++;
            } while (true);


            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);


                retval.block = block;
              
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
    // $ANTLR end "block"

    public static class blocklabel_return extends ParserRuleReturnScope {
        public ISymbol theSym;
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "blocklabel"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:389:1: blocklabel returns [ ISymbol theSym ] : LABEL ':' NEWLINE ;
    public final LLVMParser.blocklabel_return blocklabel() throws RecognitionException {
        LLVMParser.blocklabel_return retval = new LLVMParser.blocklabel_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LABEL157=null;
        Token char_literal158=null;
        Token NEWLINE159=null;

        CommonTree LABEL157_tree=null;
        CommonTree char_literal158_tree=null;
        CommonTree NEWLINE159_tree=null;

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:389:39: ( LABEL ':' NEWLINE )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:389:41: LABEL ':' NEWLINE
            {
            root_0 = (CommonTree)adaptor.nil();

            LABEL157=(Token)match(input,LABEL,FOLLOW_LABEL_in_blocklabel2000); 
            LABEL157_tree = (CommonTree)adaptor.create(LABEL157);
            adaptor.addChild(root_0, LABEL157_tree);

            char_literal158=(Token)match(input,99,FOLLOW_99_in_blocklabel2002); 
            char_literal158_tree = (CommonTree)adaptor.create(char_literal158);
            adaptor.addChild(root_0, char_literal158_tree);

            NEWLINE159=(Token)match(input,NEWLINE,FOLLOW_NEWLINE_in_blocklabel2004); 
            NEWLINE159_tree = (CommonTree)adaptor.create(NEWLINE159);
            adaptor.addChild(root_0, NEWLINE159_tree);

             
                retval.theSym = helper.addLabel((LABEL157!=null?LABEL157.getText():null));
                

            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

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
    // $ANTLR end "blocklabel"

    public static class instr_return extends ParserRuleReturnScope {
        public LLInstr inst;
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "instr"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:395:1: instr returns [LLInstr inst ] : ( ( allocaInstr ) | ( storeInstr ) | ( branchInstr ) | ( uncondBranchInstr ) | ( retInstr ) | ( assignInstr ) );
    public final LLVMParser.instr_return instr() throws RecognitionException {
        LLVMParser.instr_return retval = new LLVMParser.instr_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        LLVMParser.allocaInstr_return allocaInstr160 = null;

        LLVMParser.storeInstr_return storeInstr161 = null;

        LLVMParser.branchInstr_return branchInstr162 = null;

        LLVMParser.uncondBranchInstr_return uncondBranchInstr163 = null;

        LLVMParser.retInstr_return retInstr164 = null;

        LLVMParser.assignInstr_return assignInstr165 = null;



        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:395:31: ( ( allocaInstr ) | ( storeInstr ) | ( branchInstr ) | ( uncondBranchInstr ) | ( retInstr ) | ( assignInstr ) )
            int alt28=6;
            alt28 = dfa28.predict(input);
            switch (alt28) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:396:3: ( allocaInstr )
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:396:3: ( allocaInstr )
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:396:5: allocaInstr
                    {
                    pushFollow(FOLLOW_allocaInstr_in_instr2033);
                    allocaInstr160=allocaInstr();

                    state._fsp--;

                    adaptor.addChild(root_0, allocaInstr160.getTree());
                     retval.inst = (allocaInstr160!=null?allocaInstr160.inst:null); 

                    }


                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:397:5: ( storeInstr )
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:397:5: ( storeInstr )
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:397:7: storeInstr
                    {
                    pushFollow(FOLLOW_storeInstr_in_instr2056);
                    storeInstr161=storeInstr();

                    state._fsp--;

                    adaptor.addChild(root_0, storeInstr161.getTree());
                     retval.inst = (storeInstr161!=null?storeInstr161.inst:null); 

                    }


                    }
                    break;
                case 3 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:398:5: ( branchInstr )
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:398:5: ( branchInstr )
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:398:7: branchInstr
                    {
                    pushFollow(FOLLOW_branchInstr_in_instr2077);
                    branchInstr162=branchInstr();

                    state._fsp--;

                    adaptor.addChild(root_0, branchInstr162.getTree());
                     retval.inst = (branchInstr162!=null?branchInstr162.inst:null); 

                    }


                    }
                    break;
                case 4 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:399:5: ( uncondBranchInstr )
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:399:5: ( uncondBranchInstr )
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:399:7: uncondBranchInstr
                    {
                    pushFollow(FOLLOW_uncondBranchInstr_in_instr2097);
                    uncondBranchInstr163=uncondBranchInstr();

                    state._fsp--;

                    adaptor.addChild(root_0, uncondBranchInstr163.getTree());
                     retval.inst = (uncondBranchInstr163!=null?uncondBranchInstr163.inst:null); 

                    }


                    }
                    break;
                case 5 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:400:5: ( retInstr )
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:400:5: ( retInstr )
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:400:7: retInstr
                    {
                    pushFollow(FOLLOW_retInstr_in_instr2111);
                    retInstr164=retInstr();

                    state._fsp--;

                    adaptor.addChild(root_0, retInstr164.getTree());
                     retval.inst = (retInstr164!=null?retInstr164.inst:null); 

                    }


                    }
                    break;
                case 6 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:401:5: ( assignInstr )
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:401:5: ( assignInstr )
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:401:7: assignInstr
                    {
                    pushFollow(FOLLOW_assignInstr_in_instr2134);
                    assignInstr165=assignInstr();

                    state._fsp--;

                    adaptor.addChild(root_0, assignInstr165.getTree());
                     retval.inst = (assignInstr165!=null?assignInstr165.inst:null); 

                    }


                    }
                    break;

            }
            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

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
    // $ANTLR end "instr"

    public static class ret_return extends ParserRuleReturnScope {
        public LLOperand op;
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "ret"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:404:1: ret returns [LLOperand op] : UNNAMED_ID ;
    public final LLVMParser.ret_return ret() throws RecognitionException {
        LLVMParser.ret_return retval = new LLVMParser.ret_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token UNNAMED_ID166=null;

        CommonTree UNNAMED_ID166_tree=null;

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:404:28: ( UNNAMED_ID )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:405:5: UNNAMED_ID
            {
            root_0 = (CommonTree)adaptor.nil();

            UNNAMED_ID166=(Token)match(input,UNNAMED_ID,FOLLOW_UNNAMED_ID_in_ret2166); 
            UNNAMED_ID166_tree = (CommonTree)adaptor.create(UNNAMED_ID166);
            adaptor.addChild(root_0, UNNAMED_ID166_tree);


                  ISymbol tmpSym = helper.defineSymbol((UNNAMED_ID166!=null?UNNAMED_ID166.getText():null));
                  String tmp = (UNNAMED_ID166!=null?UNNAMED_ID166.getText():null).substring(1);
                  retval.op = new LLTempOp(Integer.parseInt(tmp), null);
                

            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

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
    // $ANTLR end "ret"

    public static class local_return extends ParserRuleReturnScope {
        public LLOperand op;
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "local"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:413:1: local returns [LLOperand op] : NAMED_ID ;
    public final LLVMParser.local_return local() throws RecognitionException {
        LLVMParser.local_return retval = new LLVMParser.local_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token NAMED_ID167=null;

        CommonTree NAMED_ID167_tree=null;

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:413:30: ( NAMED_ID )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:414:5: NAMED_ID
            {
            root_0 = (CommonTree)adaptor.nil();

            NAMED_ID167=(Token)match(input,NAMED_ID,FOLLOW_NAMED_ID_in_local2198); 
            NAMED_ID167_tree = (CommonTree)adaptor.create(NAMED_ID167);
            adaptor.addChild(root_0, NAMED_ID167_tree);

             retval.op = new LLSymbolOp(helper.defineSymbol((NAMED_ID167!=null?NAMED_ID167.getText():null))); 

            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

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
    // $ANTLR end "local"

    public static class allocaInstr_return extends ParserRuleReturnScope {
        public LLAllocaInstr inst;
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "allocaInstr"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:417:1: allocaInstr returns [LLAllocaInstr inst] : local EQUALS 'alloca' type ( typedop )? ;
    public final LLVMParser.allocaInstr_return allocaInstr() throws RecognitionException {
        LLVMParser.allocaInstr_return retval = new LLVMParser.allocaInstr_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token EQUALS169=null;
        Token string_literal170=null;
        LLVMParser.local_return local168 = null;

        LLVMParser.type_return type171 = null;

        LLVMParser.typedop_return typedop172 = null;


        CommonTree EQUALS169_tree=null;
        CommonTree string_literal170_tree=null;

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:417:42: ( local EQUALS 'alloca' type ( typedop )? )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:418:3: local EQUALS 'alloca' type ( typedop )?
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_local_in_allocaInstr2222);
            local168=local();

            state._fsp--;

            adaptor.addChild(root_0, local168.getTree());
            EQUALS169=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_allocaInstr2224); 
            EQUALS169_tree = (CommonTree)adaptor.create(EQUALS169);
            adaptor.addChild(root_0, EQUALS169_tree);

            string_literal170=(Token)match(input,100,FOLLOW_100_in_allocaInstr2226); 
            string_literal170_tree = (CommonTree)adaptor.create(string_literal170);
            adaptor.addChild(root_0, string_literal170_tree);

            pushFollow(FOLLOW_type_in_allocaInstr2228);
            type171=type();

            state._fsp--;

            adaptor.addChild(root_0, type171.getTree());
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:418:30: ( typedop )?
            int alt29=2;
            int LA29_0 = input.LA(1);

            if ( ((LA29_0>=INT_TYPE && LA29_0<=QUOTED_ID)||(LA29_0>=27 && LA29_0<=28)||LA29_0==30||LA29_0==32) ) {
                alt29=1;
            }
            switch (alt29) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:418:30: typedop
                    {
                    pushFollow(FOLLOW_typedop_in_allocaInstr2230);
                    typedop172=typedop();

                    state._fsp--;

                    adaptor.addChild(root_0, typedop172.getTree());

                    }
                    break;

            }

             
              
              LLType ptrType = helper.typeEngine.getPointerType((type171!=null?type171.theType:null));
              retval.inst = (typedop172!=null?typedop172.op:null) == null 
                ? new LLAllocaInstr((local168!=null?local168.op:null), ptrType) 
                : new LLAllocaInstr((local168!=null?local168.op:null), ptrType, (typedop172!=null?typedop172.op:null)); 
              
              // fixup types
              (local168!=null?local168.op:null).setType(ptrType);
              retval.inst.setType(ptrType);
              ((LLSymbolOp)retval.inst.getResult()).getSymbol().setType(ptrType);
              retval.inst.getResult().setType(ptrType);  
              

            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

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
    // $ANTLR end "allocaInstr"

    public static class storeInstr_return extends ParserRuleReturnScope {
        public LLStoreInstr inst;
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "storeInstr"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:434:1: storeInstr returns [LLStoreInstr inst] : 'store' o1= typedop ',' o2= typedop ;
    public final LLVMParser.storeInstr_return storeInstr() throws RecognitionException {
        LLVMParser.storeInstr_return retval = new LLVMParser.storeInstr_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token string_literal173=null;
        Token char_literal174=null;
        LLVMParser.typedop_return o1 = null;

        LLVMParser.typedop_return o2 = null;


        CommonTree string_literal173_tree=null;
        CommonTree char_literal174_tree=null;

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:434:40: ( 'store' o1= typedop ',' o2= typedop )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:435:3: 'store' o1= typedop ',' o2= typedop
            {
            root_0 = (CommonTree)adaptor.nil();

            string_literal173=(Token)match(input,101,FOLLOW_101_in_storeInstr2255); 
            string_literal173_tree = (CommonTree)adaptor.create(string_literal173);
            adaptor.addChild(root_0, string_literal173_tree);

            pushFollow(FOLLOW_typedop_in_storeInstr2259);
            o1=typedop();

            state._fsp--;

            adaptor.addChild(root_0, o1.getTree());
            char_literal174=(Token)match(input,37,FOLLOW_37_in_storeInstr2261); 
            char_literal174_tree = (CommonTree)adaptor.create(char_literal174);
            adaptor.addChild(root_0, char_literal174_tree);

            pushFollow(FOLLOW_typedop_in_storeInstr2265);
            o2=typedop();

            state._fsp--;

            adaptor.addChild(root_0, o2.getTree());
             retval.inst = new LLStoreInstr((o2!=null?o2.op:null).getType(), (o1!=null?o1.op:null), (o2!=null?o2.op:null)); 

            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

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
    // $ANTLR end "storeInstr"

    public static class retInstr_return extends ParserRuleReturnScope {
        public LLRetInstr inst;
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "retInstr"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:439:1: retInstr returns [LLRetInstr inst] : 'ret' ( ( 'void' ) | (o1= typedop ) ) ;
    public final LLVMParser.retInstr_return retInstr() throws RecognitionException {
        LLVMParser.retInstr_return retval = new LLVMParser.retInstr_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token string_literal175=null;
        Token string_literal176=null;
        LLVMParser.typedop_return o1 = null;


        CommonTree string_literal175_tree=null;
        CommonTree string_literal176_tree=null;

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:439:36: ( 'ret' ( ( 'void' ) | (o1= typedop ) ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:440:3: 'ret' ( ( 'void' ) | (o1= typedop ) )
            {
            root_0 = (CommonTree)adaptor.nil();

            string_literal175=(Token)match(input,102,FOLLOW_102_in_retInstr2291); 
            string_literal175_tree = (CommonTree)adaptor.create(string_literal175);
            adaptor.addChild(root_0, string_literal175_tree);

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:440:9: ( ( 'void' ) | (o1= typedop ) )
            int alt30=2;
            int LA30_0 = input.LA(1);

            if ( (LA30_0==27) ) {
                int LA30_1 = input.LA(2);

                if ( ((LA30_1>=NAMED_ID && LA30_1<=CHAR_LITERAL)||LA30_1==CSTRING_LITERAL||(LA30_1>=29 && LA30_1<=30)||LA30_1==32||LA30_1==35||LA30_1==55||(LA30_1>=57 && LA30_1<=68)) ) {
                    alt30=2;
                }
                else if ( (LA30_1==NEWLINE) ) {
                    alt30=1;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 30, 1, input);

                    throw nvae;
                }
            }
            else if ( ((LA30_0>=INT_TYPE && LA30_0<=QUOTED_ID)||LA30_0==28||LA30_0==30||LA30_0==32) ) {
                alt30=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 30, 0, input);

                throw nvae;
            }
            switch (alt30) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:440:11: ( 'void' )
                    {
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:440:11: ( 'void' )
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:440:13: 'void'
                    {
                    string_literal176=(Token)match(input,27,FOLLOW_27_in_retInstr2297); 
                    string_literal176_tree = (CommonTree)adaptor.create(string_literal176);
                    adaptor.addChild(root_0, string_literal176_tree);

                     retval.inst = new LLRetInstr(helper.typeEngine.VOID); 

                    }


                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:441:13: (o1= typedop )
                    {
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:441:13: (o1= typedop )
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:441:15: o1= typedop
                    {
                    pushFollow(FOLLOW_typedop_in_retInstr2328);
                    o1=typedop();

                    state._fsp--;

                    adaptor.addChild(root_0, o1.getTree());
                     retval.inst = new LLRetInstr((o1!=null?o1.op:null).getType(), (o1!=null?o1.op:null)); 

                    }


                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

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
    // $ANTLR end "retInstr"

    public static class assignInstr_return extends ParserRuleReturnScope {
        public LLAssignInstr inst;
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "assignInstr"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:445:1: assignInstr returns [LLAssignInstr inst] : ret EQUALS ( ( 'load' loadop= typedop ) | ( binexpr ( binexprsuffix )? type bop1= op ',' bop2= op ) ) ;
    public final LLVMParser.assignInstr_return assignInstr() throws RecognitionException {
        LLVMParser.assignInstr_return retval = new LLVMParser.assignInstr_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token EQUALS178=null;
        Token string_literal179=null;
        Token char_literal183=null;
        LLVMParser.typedop_return loadop = null;

        LLVMParser.op_return bop1 = null;

        LLVMParser.op_return bop2 = null;

        LLVMParser.ret_return ret177 = null;

        LLVMParser.binexpr_return binexpr180 = null;

        LLVMParser.binexprsuffix_return binexprsuffix181 = null;

        LLVMParser.type_return type182 = null;


        CommonTree EQUALS178_tree=null;
        CommonTree string_literal179_tree=null;
        CommonTree char_literal183_tree=null;

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:445:42: ( ret EQUALS ( ( 'load' loadop= typedop ) | ( binexpr ( binexprsuffix )? type bop1= op ',' bop2= op ) ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:446:5: ret EQUALS ( ( 'load' loadop= typedop ) | ( binexpr ( binexprsuffix )? type bop1= op ',' bop2= op ) )
            {
            root_0 = (CommonTree)adaptor.nil();


                LLOperand ret;
                
            pushFollow(FOLLOW_ret_in_assignInstr2370);
            ret177=ret();

            state._fsp--;

            adaptor.addChild(root_0, ret177.getTree());
             ret = (ret177!=null?ret177.op:null); 
            EQUALS178=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_assignInstr2384); 
            EQUALS178_tree = (CommonTree)adaptor.create(EQUALS178);
            adaptor.addChild(root_0, EQUALS178_tree);

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:452:5: ( ( 'load' loadop= typedop ) | ( binexpr ( binexprsuffix )? type bop1= op ',' bop2= op ) )
            int alt32=2;
            int LA32_0 = input.LA(1);

            if ( (LA32_0==103) ) {
                alt32=1;
            }
            else if ( ((LA32_0>=104 && LA32_0<=121)) ) {
                alt32=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 32, 0, input);

                throw nvae;
            }
            switch (alt32) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:453:5: ( 'load' loadop= typedop )
                    {
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:453:5: ( 'load' loadop= typedop )
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:453:7: 'load' loadop= typedop
                    {
                    string_literal179=(Token)match(input,103,FOLLOW_103_in_assignInstr2398); 
                    string_literal179_tree = (CommonTree)adaptor.create(string_literal179);
                    adaptor.addChild(root_0, string_literal179_tree);

                    pushFollow(FOLLOW_typedop_in_assignInstr2402);
                    loadop=typedop();

                    state._fsp--;

                    adaptor.addChild(root_0, loadop.getTree());
                     retval.inst = new LLLoadInstr(ret, (loadop!=null?loadop.op:null).getType(), (loadop!=null?loadop.op:null)); 

                    }


                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:454:7: ( binexpr ( binexprsuffix )? type bop1= op ',' bop2= op )
                    {
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:454:7: ( binexpr ( binexprsuffix )? type bop1= op ',' bop2= op )
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:454:9: binexpr ( binexprsuffix )? type bop1= op ',' bop2= op
                    {
                    pushFollow(FOLLOW_binexpr_in_assignInstr2416);
                    binexpr180=binexpr();

                    state._fsp--;

                    adaptor.addChild(root_0, binexpr180.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:454:17: ( binexprsuffix )?
                    int alt31=2;
                    int LA31_0 = input.LA(1);

                    if ( ((LA31_0>=122 && LA31_0<=124)) ) {
                        alt31=1;
                    }
                    switch (alt31) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:454:17: binexprsuffix
                            {
                            pushFollow(FOLLOW_binexprsuffix_in_assignInstr2418);
                            binexprsuffix181=binexprsuffix();

                            state._fsp--;

                            adaptor.addChild(root_0, binexprsuffix181.getTree());

                            }
                            break;

                    }

                    pushFollow(FOLLOW_type_in_assignInstr2421);
                    type182=type();

                    state._fsp--;

                    adaptor.addChild(root_0, type182.getTree());
                    pushFollow(FOLLOW_op_in_assignInstr2425);
                    bop1=op();

                    state._fsp--;

                    adaptor.addChild(root_0, bop1.getTree());
                    char_literal183=(Token)match(input,37,FOLLOW_37_in_assignInstr2427); 
                    char_literal183_tree = (CommonTree)adaptor.create(char_literal183);
                    adaptor.addChild(root_0, char_literal183_tree);

                    pushFollow(FOLLOW_op_in_assignInstr2431);
                    bop2=op();

                    state._fsp--;

                    adaptor.addChild(root_0, bop2.getTree());

                              (bop1!=null?bop1.op:null).setType((type182!=null?type182.theType:null)); 
                              (bop2!=null?bop2.op:null).setType((type182!=null?type182.theType:null)); 
                              ret.setType((type182!=null?type182.theType:null));
                              String op = (binexpr180!=null?input.toString(binexpr180.start,binexpr180.stop):null);
                              if ((binexprsuffix181!=null?input.toString(binexprsuffix181.start,binexprsuffix181.stop):null) != null)
                                op += ' ' + (binexprsuffix181!=null?input.toString(binexprsuffix181.start,binexprsuffix181.stop):null);
                              retval.inst = new LLBinaryInstr(op, ret, ret.getType(), (bop1!=null?bop1.op:null), (bop2!=null?bop2.op:null)); 
                            

                    }


                    }
                    break;

            }


                // fixup types, since we don't know 'ret''s until now
                ((LLTempOp)ret).setType(retval.inst.getType()); 
                ISymbol local = helper.findSymbol(ret.toString());
                local.setType(ret.getType());
                

            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

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
    // $ANTLR end "assignInstr"

    public static class binexpr_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "binexpr"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:475:1: binexpr : ( 'add' | 'fadd' | 'sub' | 'fsub' | 'mul' | 'fmul' | 'udiv' | 'sdiv' | 'fdiv' | 'urem' | 'srem' | 'frem' | 'shl' | 'lshr' | 'ashr' | 'and' | 'or' | 'xor' );
    public final LLVMParser.binexpr_return binexpr() throws RecognitionException {
        LLVMParser.binexpr_return retval = new LLVMParser.binexpr_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token set184=null;

        CommonTree set184_tree=null;

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:475:9: ( 'add' | 'fadd' | 'sub' | 'fsub' | 'mul' | 'fmul' | 'udiv' | 'sdiv' | 'fdiv' | 'urem' | 'srem' | 'frem' | 'shl' | 'lshr' | 'ashr' | 'and' | 'or' | 'xor' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:
            {
            root_0 = (CommonTree)adaptor.nil();

            set184=(Token)input.LT(1);
            if ( (input.LA(1)>=104 && input.LA(1)<=121) ) {
                input.consume();
                adaptor.addChild(root_0, (CommonTree)adaptor.create(set184));
                state.errorRecovery=false;
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

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
    // $ANTLR end "binexpr"

    public static class binexprsuffix_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "binexprsuffix"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:481:1: binexprsuffix : ( 'nuw' | 'nsw' | 'exact' )+ ;
    public final LLVMParser.binexprsuffix_return binexprsuffix() throws RecognitionException {
        LLVMParser.binexprsuffix_return retval = new LLVMParser.binexprsuffix_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token set185=null;

        CommonTree set185_tree=null;

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:481:15: ( ( 'nuw' | 'nsw' | 'exact' )+ )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:481:17: ( 'nuw' | 'nsw' | 'exact' )+
            {
            root_0 = (CommonTree)adaptor.nil();

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:481:17: ( 'nuw' | 'nsw' | 'exact' )+
            int cnt33=0;
            loop33:
            do {
                int alt33=2;
                int LA33_0 = input.LA(1);

                if ( ((LA33_0>=122 && LA33_0<=124)) ) {
                    alt33=1;
                }


                switch (alt33) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:
            	    {
            	    set185=(Token)input.LT(1);
            	    if ( (input.LA(1)>=122 && input.LA(1)<=124) ) {
            	        input.consume();
            	        adaptor.addChild(root_0, (CommonTree)adaptor.create(set185));
            	        state.errorRecovery=false;
            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        throw mse;
            	    }


            	    }
            	    break;

            	default :
            	    if ( cnt33 >= 1 ) break loop33;
                        EarlyExitException eee =
                            new EarlyExitException(33, input);
                        throw eee;
                }
                cnt33++;
            } while (true);


            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

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
    // $ANTLR end "binexprsuffix"

    public static class branchInstr_return extends ParserRuleReturnScope {
        public LLInstr inst;
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "branchInstr"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:483:1: branchInstr returns [LLInstr inst] : 'br' typedop ',' 'label' t= identifier ',' 'label' f= identifier ;
    public final LLVMParser.branchInstr_return branchInstr() throws RecognitionException {
        LLVMParser.branchInstr_return retval = new LLVMParser.branchInstr_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token string_literal186=null;
        Token char_literal188=null;
        Token string_literal189=null;
        Token char_literal190=null;
        Token string_literal191=null;
        LLVMParser.identifier_return t = null;

        LLVMParser.identifier_return f = null;

        LLVMParser.typedop_return typedop187 = null;


        CommonTree string_literal186_tree=null;
        CommonTree char_literal188_tree=null;
        CommonTree string_literal189_tree=null;
        CommonTree char_literal190_tree=null;
        CommonTree string_literal191_tree=null;

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:483:36: ( 'br' typedop ',' 'label' t= identifier ',' 'label' f= identifier )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:484:3: 'br' typedop ',' 'label' t= identifier ',' 'label' f= identifier
            {
            root_0 = (CommonTree)adaptor.nil();

            string_literal186=(Token)match(input,125,FOLLOW_125_in_branchInstr2599); 
            string_literal186_tree = (CommonTree)adaptor.create(string_literal186);
            adaptor.addChild(root_0, string_literal186_tree);

            pushFollow(FOLLOW_typedop_in_branchInstr2601);
            typedop187=typedop();

            state._fsp--;

            adaptor.addChild(root_0, typedop187.getTree());
            char_literal188=(Token)match(input,37,FOLLOW_37_in_branchInstr2603); 
            char_literal188_tree = (CommonTree)adaptor.create(char_literal188);
            adaptor.addChild(root_0, char_literal188_tree);

            string_literal189=(Token)match(input,28,FOLLOW_28_in_branchInstr2605); 
            string_literal189_tree = (CommonTree)adaptor.create(string_literal189);
            adaptor.addChild(root_0, string_literal189_tree);

            pushFollow(FOLLOW_identifier_in_branchInstr2609);
            t=identifier();

            state._fsp--;

            adaptor.addChild(root_0, t.getTree());
            char_literal190=(Token)match(input,37,FOLLOW_37_in_branchInstr2611); 
            char_literal190_tree = (CommonTree)adaptor.create(char_literal190);
            adaptor.addChild(root_0, char_literal190_tree);

            string_literal191=(Token)match(input,28,FOLLOW_28_in_branchInstr2613); 
            string_literal191_tree = (CommonTree)adaptor.create(string_literal191);
            adaptor.addChild(root_0, string_literal191_tree);

            pushFollow(FOLLOW_identifier_in_branchInstr2617);
            f=identifier();

            state._fsp--;

            adaptor.addChild(root_0, f.getTree());
             retval.inst = new LLBranchInstr((typedop187!=null?typedop187.op:null).getType(), (typedop187!=null?typedop187.op:null), helper.getSymbolOp((t!=null?t.theId:null), null), helper.getSymbolOp((f!=null?f.theId:null), null)); 

            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

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
    // $ANTLR end "branchInstr"

    public static class uncondBranchInstr_return extends ParserRuleReturnScope {
        public LLInstr inst;
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "uncondBranchInstr"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:488:1: uncondBranchInstr returns [LLInstr inst] : 'br' 'label' identifier ;
    public final LLVMParser.uncondBranchInstr_return uncondBranchInstr() throws RecognitionException {
        LLVMParser.uncondBranchInstr_return retval = new LLVMParser.uncondBranchInstr_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token string_literal192=null;
        Token string_literal193=null;
        LLVMParser.identifier_return identifier194 = null;


        CommonTree string_literal192_tree=null;
        CommonTree string_literal193_tree=null;

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:488:42: ( 'br' 'label' identifier )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:489:3: 'br' 'label' identifier
            {
            root_0 = (CommonTree)adaptor.nil();

            string_literal192=(Token)match(input,125,FOLLOW_125_in_uncondBranchInstr2642); 
            string_literal192_tree = (CommonTree)adaptor.create(string_literal192);
            adaptor.addChild(root_0, string_literal192_tree);

            string_literal193=(Token)match(input,28,FOLLOW_28_in_uncondBranchInstr2644); 
            string_literal193_tree = (CommonTree)adaptor.create(string_literal193);
            adaptor.addChild(root_0, string_literal193_tree);

            pushFollow(FOLLOW_identifier_in_uncondBranchInstr2646);
            identifier194=identifier();

            state._fsp--;

            adaptor.addChild(root_0, identifier194.getTree());
             retval.inst = new LLUncondBranchInstr(helper.getSymbolOp((identifier194!=null?identifier194.theId:null), null)); 

            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

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
    // $ANTLR end "uncondBranchInstr"

    // Delegated rules


    protected DFA2 dfa2 = new DFA2(this);
    protected DFA28 dfa28 = new DFA28(this);
    static final String DFA2_eotS =
        "\15\uffff";
    static final String DFA2_eofS =
        "\15\uffff";
    static final String DFA2_minS =
        "\1\4\1\30\3\5\4\uffff\1\32\3\uffff";
    static final String DFA2_maxS =
        "\1\27\1\31\3\5\4\uffff\1\66\3\uffff";
    static final String DFA2_acceptS =
        "\5\uffff\1\6\1\7\1\1\1\2\1\uffff\1\3\1\5\1\4";
    static final String DFA2_specialS =
        "\15\uffff}>";
    static final String[] DFA2_transitionS = {
            "\1\6\2\uffff\1\2\1\3\1\4\4\uffff\1\5\10\uffff\1\1",
            "\1\7\1\10",
            "\1\11",
            "\1\11",
            "\1\11",
            "",
            "",
            "",
            "",
            "\1\12\13\uffff\1\14\2\13\16\14",
            "",
            "",
            ""
    };

    static final short[] DFA2_eot = DFA.unpackEncodedString(DFA2_eotS);
    static final short[] DFA2_eof = DFA.unpackEncodedString(DFA2_eofS);
    static final char[] DFA2_min = DFA.unpackEncodedStringToUnsignedChars(DFA2_minS);
    static final char[] DFA2_max = DFA.unpackEncodedStringToUnsignedChars(DFA2_maxS);
    static final short[] DFA2_accept = DFA.unpackEncodedString(DFA2_acceptS);
    static final short[] DFA2_special = DFA.unpackEncodedString(DFA2_specialS);
    static final short[][] DFA2_transition;

    static {
        int numStates = DFA2_transitionS.length;
        DFA2_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA2_transition[i] = DFA.unpackEncodedString(DFA2_transitionS[i]);
        }
    }

    class DFA2 extends DFA {

        public DFA2(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 2;
            this.eot = DFA2_eot;
            this.eof = DFA2_eof;
            this.min = DFA2_min;
            this.max = DFA2_max;
            this.accept = DFA2_accept;
            this.special = DFA2_special;
            this.transition = DFA2_transition;
        }
        public String getDescription() {
            return "56:1: directive : ( targetDataLayoutDirective ( NEWLINE | EOF ) | targetTripleDirective ( NEWLINE | EOF ) | typeDefinition ( NEWLINE | EOF ) | globalDataDirective ( NEWLINE | EOF ) | constantDirective ( NEWLINE | EOF ) | defineDirective ( NEWLINE | EOF ) | NEWLINE );";
        }
    }
    static final String DFA28_eotS =
        "\14\uffff";
    static final String DFA28_eofS =
        "\14\uffff";
    static final String DFA28_minS =
        "\1\7\2\uffff\1\6\2\uffff\1\7\1\uffff\3\4\1\uffff";
    static final String DFA28_maxS =
        "\1\175\2\uffff\1\40\2\uffff\1\104\1\uffff\3\45\1\uffff";
    static final String DFA28_acceptS =
        "\1\uffff\1\1\1\2\1\uffff\1\5\1\6\1\uffff\1\3\3\uffff\1\4";
    static final String DFA28_specialS =
        "\14\uffff}>";
    static final String[] DFA28_transitionS = {
            "\1\1\1\5\134\uffff\1\2\1\4\26\uffff\1\3",
            "",
            "",
            "\4\7\21\uffff\1\7\1\6\1\uffff\1\7\1\uffff\1\7",
            "",
            "",
            "\1\10\1\11\1\12\2\7\1\uffff\1\7\17\uffff\2\7\1\uffff\1\7\2"+
            "\uffff\1\7\23\uffff\1\7\1\uffff\14\7",
            "",
            "\1\13\40\uffff\1\7",
            "\1\13\40\uffff\1\7",
            "\1\13\40\uffff\1\7",
            ""
    };

    static final short[] DFA28_eot = DFA.unpackEncodedString(DFA28_eotS);
    static final short[] DFA28_eof = DFA.unpackEncodedString(DFA28_eofS);
    static final char[] DFA28_min = DFA.unpackEncodedStringToUnsignedChars(DFA28_minS);
    static final char[] DFA28_max = DFA.unpackEncodedStringToUnsignedChars(DFA28_maxS);
    static final short[] DFA28_accept = DFA.unpackEncodedString(DFA28_acceptS);
    static final short[] DFA28_special = DFA.unpackEncodedString(DFA28_specialS);
    static final short[][] DFA28_transition;

    static {
        int numStates = DFA28_transitionS.length;
        DFA28_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA28_transition[i] = DFA.unpackEncodedString(DFA28_transitionS[i]);
        }
    }

    class DFA28 extends DFA {

        public DFA28(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 28;
            this.eot = DFA28_eot;
            this.eof = DFA28_eof;
            this.min = DFA28_min;
            this.max = DFA28_max;
            this.accept = DFA28_accept;
            this.special = DFA28_special;
            this.transition = DFA28_transition;
        }
        public String getDescription() {
            return "395:1: instr returns [LLInstr inst ] : ( ( allocaInstr ) | ( storeInstr ) | ( branchInstr ) | ( uncondBranchInstr ) | ( retInstr ) | ( assignInstr ) );";
        }
    }
 

    public static final BitSet FOLLOW_toplevelstmts_in_prog69 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_prog71 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_directive_in_toplevelstmts101 = new BitSet(new long[]{0x0000000000804392L});
    public static final BitSet FOLLOW_targetDataLayoutDirective_in_directive122 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_set_in_directive124 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_targetTripleDirective_in_directive137 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_set_in_directive140 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_typeDefinition_in_directive152 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_set_in_directive155 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_globalDataDirective_in_directive167 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_set_in_directive170 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_constantDirective_in_directive182 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_set_in_directive184 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_defineDirective_in_directive196 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_set_in_directive198 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NEWLINE_in_directive210 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_23_in_targetDataLayoutDirective223 = new BitSet(new long[]{0x0000000001000000L});
    public static final BitSet FOLLOW_24_in_targetDataLayoutDirective225 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_EQUALS_in_targetDataLayoutDirective227 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_stringLiteral_in_targetDataLayoutDirective229 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_23_in_targetTripleDirective245 = new BitSet(new long[]{0x0000000002000000L});
    public static final BitSet FOLLOW_25_in_targetTripleDirective247 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_EQUALS_in_targetTripleDirective249 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_stringLiteral_in_targetTripleDirective251 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_identifier_in_typeDefinition266 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_EQUALS_in_typeDefinition268 = new BitSet(new long[]{0x0000000004000000L});
    public static final BitSet FOLLOW_26_in_typeDefinition270 = new BitSet(new long[]{0x00000001580003C0L});
    public static final BitSet FOLLOW_type_in_typeDefinition274 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_inttype_in_type327 = new BitSet(new long[]{0x0000000820000002L});
    public static final BitSet FOLLOW_structtype_in_type338 = new BitSet(new long[]{0x0000000820000002L});
    public static final BitSet FOLLOW_arraytype_in_type348 = new BitSet(new long[]{0x0000000820000002L});
    public static final BitSet FOLLOW_27_in_type356 = new BitSet(new long[]{0x0000000820000002L});
    public static final BitSet FOLLOW_28_in_type371 = new BitSet(new long[]{0x0000000820000002L});
    public static final BitSet FOLLOW_symboltype_in_type388 = new BitSet(new long[]{0x0000000820000002L});
    public static final BitSet FOLLOW_29_in_type405 = new BitSet(new long[]{0x0000000820000002L});
    public static final BitSet FOLLOW_paramstype_in_type417 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INT_TYPE_in_inttype439 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_30_in_structtype458 = new BitSet(new long[]{0x00000001D80003C0L});
    public static final BitSet FOLLOW_typeList_in_structtype460 = new BitSet(new long[]{0x0000000080000000L});
    public static final BitSet FOLLOW_31_in_structtype462 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_32_in_arraytype483 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_number_in_arraytype485 = new BitSet(new long[]{0x0000000200000000L});
    public static final BitSet FOLLOW_33_in_arraytype487 = new BitSet(new long[]{0x00000001580003C0L});
    public static final BitSet FOLLOW_type_in_arraytype489 = new BitSet(new long[]{0x0000000400000000L});
    public static final BitSet FOLLOW_34_in_arraytype491 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_35_in_paramstype509 = new BitSet(new long[]{0x00000011580003C0L});
    public static final BitSet FOLLOW_typeList_in_paramstype511 = new BitSet(new long[]{0x0000001000000000L});
    public static final BitSet FOLLOW_36_in_paramstype513 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_type_in_typeList555 = new BitSet(new long[]{0x0000002000000002L});
    public static final BitSet FOLLOW_37_in_typeList573 = new BitSet(new long[]{0x00000001580003C0L});
    public static final BitSet FOLLOW_type_in_typeList577 = new BitSet(new long[]{0x0000002000000002L});
    public static final BitSet FOLLOW_identifier_in_symboltype619 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_identifier_in_globalDataDirective633 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_EQUALS_in_globalDataDirective635 = new BitSet(new long[]{0x007FFE4000000000L});
    public static final BitSet FOLLOW_linkage_in_globalDataDirective637 = new BitSet(new long[]{0x0000004000000000L});
    public static final BitSet FOLLOW_38_in_globalDataDirective640 = new BitSet(new long[]{0x00000001580003C0L});
    public static final BitSet FOLLOW_typedop_in_globalDataDirective642 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_identifier_in_constantDirective656 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_EQUALS_in_constantDirective658 = new BitSet(new long[]{0x0000018000000000L});
    public static final BitSet FOLLOW_addrspace_in_constantDirective660 = new BitSet(new long[]{0x0000008000000000L});
    public static final BitSet FOLLOW_39_in_constantDirective663 = new BitSet(new long[]{0x00000001580003C0L});
    public static final BitSet FOLLOW_typedop_in_constantDirective665 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_40_in_addrspace686 = new BitSet(new long[]{0x0000000800000000L});
    public static final BitSet FOLLOW_35_in_addrspace688 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_number_in_addrspace690 = new BitSet(new long[]{0x0000001000000000L});
    public static final BitSet FOLLOW_36_in_addrspace692 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_linkage713 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_type_in_typedop795 = new BitSet(new long[]{0xFE80000140002F80L,0x000000000000001FL});
    public static final BitSet FOLLOW_op_in_typedop797 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_number_in_op823 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_charconst_in_op831 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_stringconst_in_op839 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_structconst_in_op847 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_arrayconst_in_op855 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_symbolconst_in_op864 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_55_in_op873 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_constcastexpr_in_op882 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_casttype_in_constcastexpr905 = new BitSet(new long[]{0x0000000800000000L});
    public static final BitSet FOLLOW_35_in_constcastexpr907 = new BitSet(new long[]{0x00000001580003C0L});
    public static final BitSet FOLLOW_typedop_in_constcastexpr909 = new BitSet(new long[]{0x0100000000000000L});
    public static final BitSet FOLLOW_56_in_constcastexpr911 = new BitSet(new long[]{0x00000001580003C0L});
    public static final BitSet FOLLOW_type_in_constcastexpr913 = new BitSet(new long[]{0x0000001000000000L});
    public static final BitSet FOLLOW_36_in_constcastexpr915 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_57_in_casttype953 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_58_in_casttype961 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_59_in_casttype969 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_60_in_casttype977 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_61_in_casttype985 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_62_in_casttype993 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_63_in_casttype1001 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_64_in_casttype1009 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_65_in_casttype1017 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_66_in_casttype1025 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_67_in_casttype1034 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_68_in_casttype1042 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_identifier_in_symbolconst1071 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_charLiteral_in_charconst1095 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cstringLiteral_in_stringconst1112 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_30_in_structconst1149 = new BitSet(new long[]{0x00000001D80003C0L});
    public static final BitSet FOLLOW_typedop_in_structconst1154 = new BitSet(new long[]{0x0000002080000000L});
    public static final BitSet FOLLOW_37_in_structconst1164 = new BitSet(new long[]{0x00000001580003C0L});
    public static final BitSet FOLLOW_typedop_in_structconst1168 = new BitSet(new long[]{0x0000002080000000L});
    public static final BitSet FOLLOW_31_in_structconst1189 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_32_in_arrayconst1227 = new BitSet(new long[]{0x00000005580003C0L});
    public static final BitSet FOLLOW_typedop_in_arrayconst1232 = new BitSet(new long[]{0x0000002400000000L});
    public static final BitSet FOLLOW_37_in_arrayconst1242 = new BitSet(new long[]{0x00000001580003C0L});
    public static final BitSet FOLLOW_typedop_in_arrayconst1246 = new BitSet(new long[]{0x0000002400000000L});
    public static final BitSet FOLLOW_34_in_arrayconst1267 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NAMED_ID_in_identifier1291 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_UNNAMED_ID_in_identifier1302 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_QUOTED_ID_in_identifier1310 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NUMBER_in_number1332 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CHAR_LITERAL_in_charLiteral1350 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_LITERAL_in_stringLiteral1370 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CSTRING_LITERAL_in_cstringLiteral1391 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DEFINE_in_defineDirective1406 = new BitSet(new long[]{0x007FFE01580003C0L,0x00000000001FFFE0L});
    public static final BitSet FOLLOW_linkage_in_defineDirective1408 = new BitSet(new long[]{0x00000001580003C0L,0x00000000001FFFE0L});
    public static final BitSet FOLLOW_visibility_in_defineDirective1411 = new BitSet(new long[]{0x00000001580003C0L,0x00000000001FFF00L});
    public static final BitSet FOLLOW_cconv_in_defineDirective1414 = new BitSet(new long[]{0x00000001580003C0L,0x00000000001FE000L});
    public static final BitSet FOLLOW_attrs_in_defineDirective1417 = new BitSet(new long[]{0x00000001580003C0L});
    public static final BitSet FOLLOW_type_in_defineDirective1419 = new BitSet(new long[]{0x0000000000000380L});
    public static final BitSet FOLLOW_identifier_in_defineDirective1421 = new BitSet(new long[]{0x0000000800000000L});
    public static final BitSet FOLLOW_arglist_in_defineDirective1423 = new BitSet(new long[]{0x0000000000000010L,0x00000007FFE00000L});
    public static final BitSet FOLLOW_fn_attrs_in_defineDirective1425 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_NEWLINE_in_defineDirective1427 = new BitSet(new long[]{0x0000000040000000L});
    public static final BitSet FOLLOW_30_in_defineDirective1446 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_NEWLINE_in_defineDirective1448 = new BitSet(new long[]{0x0000000000008000L});
    public static final BitSet FOLLOW_defineStmts_in_defineDirective1454 = new BitSet(new long[]{0x0000000080000000L});
    public static final BitSet FOLLOW_31_in_defineDirective1461 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_visibility1489 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_72_in_cconv1520 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_73_in_cconv1524 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_74_in_cconv1528 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_75_in_cconv1532 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_76_in_cconv1536 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_number_in_cconv1538 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_attr_in_attrs1577 = new BitSet(new long[]{0x0000000000000002L,0x00000000001FE000L});
    public static final BitSet FOLLOW_set_in_attr0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_fn_attr_in_fn_attrs1656 = new BitSet(new long[]{0x0000000000000002L,0x00000007FFE00000L});
    public static final BitSet FOLLOW_85_in_fn_attr1675 = new BitSet(new long[]{0x0000000800000000L});
    public static final BitSet FOLLOW_35_in_fn_attr1677 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_number_in_fn_attr1679 = new BitSet(new long[]{0x0000001000000000L});
    public static final BitSet FOLLOW_36_in_fn_attr1681 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_86_in_fn_attr1687 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_87_in_fn_attr1691 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_88_in_fn_attr1695 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_89_in_fn_attr1699 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_90_in_fn_attr1708 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_91_in_fn_attr1712 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_92_in_fn_attr1716 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_93_in_fn_attr1720 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_94_in_fn_attr1724 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_95_in_fn_attr1728 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_96_in_fn_attr1732 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_97_in_fn_attr1736 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_98_in_fn_attr1740 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_35_in_arglist1779 = new BitSet(new long[]{0x00000011580003C0L});
    public static final BitSet FOLLOW_funcarg_in_arglist1792 = new BitSet(new long[]{0x0000003000000000L});
    public static final BitSet FOLLOW_37_in_arglist1814 = new BitSet(new long[]{0x00000001580003C0L});
    public static final BitSet FOLLOW_funcarg_in_arglist1818 = new BitSet(new long[]{0x0000003000000000L});
    public static final BitSet FOLLOW_36_in_arglist1852 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_type_in_funcarg1870 = new BitSet(new long[]{0x0000000000000380L,0x00000000001FE000L});
    public static final BitSet FOLLOW_attrs_in_funcarg1872 = new BitSet(new long[]{0x0000000000000380L});
    public static final BitSet FOLLOW_identifier_in_funcarg1874 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_block_in_defineStmts1914 = new BitSet(new long[]{0x0000000000008002L});
    public static final BitSet FOLLOW_blocklabel_in_block1958 = new BitSet(new long[]{0x0000000000000180L,0x2000006000000000L});
    public static final BitSet FOLLOW_instr_in_block1972 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_NEWLINE_in_block1974 = new BitSet(new long[]{0x0000000000000182L,0x2000006000000000L});
    public static final BitSet FOLLOW_LABEL_in_blocklabel2000 = new BitSet(new long[]{0x0000000000000000L,0x0000000800000000L});
    public static final BitSet FOLLOW_99_in_blocklabel2002 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_NEWLINE_in_blocklabel2004 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_allocaInstr_in_instr2033 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_storeInstr_in_instr2056 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_branchInstr_in_instr2077 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_uncondBranchInstr_in_instr2097 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_retInstr_in_instr2111 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_assignInstr_in_instr2134 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_UNNAMED_ID_in_ret2166 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NAMED_ID_in_local2198 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_local_in_allocaInstr2222 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_EQUALS_in_allocaInstr2224 = new BitSet(new long[]{0x0000000000000000L,0x0000001000000000L});
    public static final BitSet FOLLOW_100_in_allocaInstr2226 = new BitSet(new long[]{0x00000001580003C0L});
    public static final BitSet FOLLOW_type_in_allocaInstr2228 = new BitSet(new long[]{0x00000001580003C2L});
    public static final BitSet FOLLOW_typedop_in_allocaInstr2230 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_101_in_storeInstr2255 = new BitSet(new long[]{0x00000001580003C0L});
    public static final BitSet FOLLOW_typedop_in_storeInstr2259 = new BitSet(new long[]{0x0000002000000000L});
    public static final BitSet FOLLOW_37_in_storeInstr2261 = new BitSet(new long[]{0x00000001580003C0L});
    public static final BitSet FOLLOW_typedop_in_storeInstr2265 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_102_in_retInstr2291 = new BitSet(new long[]{0x00000001580003C0L});
    public static final BitSet FOLLOW_27_in_retInstr2297 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_typedop_in_retInstr2328 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ret_in_assignInstr2370 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_EQUALS_in_assignInstr2384 = new BitSet(new long[]{0x0000000000000000L,0x03FFFF8000000000L});
    public static final BitSet FOLLOW_103_in_assignInstr2398 = new BitSet(new long[]{0x00000001580003C0L});
    public static final BitSet FOLLOW_typedop_in_assignInstr2402 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_binexpr_in_assignInstr2416 = new BitSet(new long[]{0x00000001580003C0L,0x1C00000000000000L});
    public static final BitSet FOLLOW_binexprsuffix_in_assignInstr2418 = new BitSet(new long[]{0x00000001580003C0L});
    public static final BitSet FOLLOW_type_in_assignInstr2421 = new BitSet(new long[]{0xFE80000140002F80L,0x000000000000001FL});
    public static final BitSet FOLLOW_op_in_assignInstr2425 = new BitSet(new long[]{0x0000002000000000L});
    public static final BitSet FOLLOW_37_in_assignInstr2427 = new BitSet(new long[]{0xFE80000140002F80L,0x000000000000001FL});
    public static final BitSet FOLLOW_op_in_assignInstr2431 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_binexpr0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_binexprsuffix2571 = new BitSet(new long[]{0x0000000000000002L,0x1C00000000000000L});
    public static final BitSet FOLLOW_125_in_branchInstr2599 = new BitSet(new long[]{0x00000001580003C0L});
    public static final BitSet FOLLOW_typedop_in_branchInstr2601 = new BitSet(new long[]{0x0000002000000000L});
    public static final BitSet FOLLOW_37_in_branchInstr2603 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_28_in_branchInstr2605 = new BitSet(new long[]{0x0000000000000380L});
    public static final BitSet FOLLOW_identifier_in_branchInstr2609 = new BitSet(new long[]{0x0000002000000000L});
    public static final BitSet FOLLOW_37_in_branchInstr2611 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_28_in_branchInstr2613 = new BitSet(new long[]{0x0000000000000380L});
    public static final BitSet FOLLOW_identifier_in_branchInstr2617 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_125_in_uncondBranchInstr2642 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_28_in_uncondBranchInstr2644 = new BitSet(new long[]{0x0000000000000380L});
    public static final BitSet FOLLOW_identifier_in_uncondBranchInstr2646 = new BitSet(new long[]{0x0000000000000002L});

}