// $ANTLR 3.2 Sep 23, 2009 12:02:23 /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g 2010-06-19 23:37:33

package org.ejs.eulang.llvm.parser;
import org.ejs.eulang.symbols.*;
import org.ejs.eulang.llvm.*;
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
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "EQUALS", "INT_TYPE", "NAMED_ID", "UNNAMED_ID", "QUOTED_ID", "NUMBER", "CHAR_LITERAL", "STRING_LITERAL", "CSTRING_LITERAL", "NUMSUFFIX", "SYM_PFX", "NAME_SUFFIX", "NUMBER_SUFFIX", "NEWLINE", "WS", "SINGLE_COMMENT", "MULTI_COMMENT", "'target'", "'datalayout'", "'triple'", "'type'", "'void'", "'*'", "'{'", "'}'", "'['", "'x'", "']'", "'('", "')'", "','", "'global'", "'private'", "'linker_private'", "'internal'", "'available_externally'", "'linkonce'", "'weak'", "'common'", "'appending'", "'extern_weak'", "'linkonce_odr'", "'weak_odr'", "'externally_visible'", "'dllimport'", "'dllexport'", "'zeroinitializer'"
    };
    public static final int T__29=29;
    public static final int T__28=28;
    public static final int T__27=27;
    public static final int T__26=26;
    public static final int T__25=25;
    public static final int T__24=24;
    public static final int T__23=23;
    public static final int T__22=22;
    public static final int T__21=21;
    public static final int MULTI_COMMENT=20;
    public static final int EQUALS=4;
    public static final int EOF=-1;
    public static final int QUOTED_ID=8;
    public static final int NUMSUFFIX=13;
    public static final int STRING_LITERAL=11;
    public static final int SINGLE_COMMENT=19;
    public static final int T__50=50;
    public static final int T__42=42;
    public static final int T__43=43;
    public static final int T__40=40;
    public static final int T__41=41;
    public static final int T__46=46;
    public static final int T__47=47;
    public static final int T__44=44;
    public static final int T__45=45;
    public static final int NUMBER_SUFFIX=16;
    public static final int T__48=48;
    public static final int T__49=49;
    public static final int NUMBER=9;
    public static final int NAMED_ID=6;
    public static final int INT_TYPE=5;
    public static final int SYM_PFX=14;
    public static final int T__30=30;
    public static final int T__31=31;
    public static final int T__32=32;
    public static final int WS=18;
    public static final int T__33=33;
    public static final int T__34=34;
    public static final int NEWLINE=17;
    public static final int NAME_SUFFIX=15;
    public static final int T__35=35;
    public static final int T__36=36;
    public static final int CHAR_LITERAL=10;
    public static final int T__37=37;
    public static final int T__38=38;
    public static final int T__39=39;
    public static final int UNNAMED_ID=7;
    public static final int CSTRING_LITERAL=12;

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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:49:1: prog : toplevelstmts EOF ;
    public final LLVMParser.prog_return prog() throws RecognitionException {
        LLVMParser.prog_return retval = new LLVMParser.prog_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token EOF2=null;
        LLVMParser.toplevelstmts_return toplevelstmts1 = null;


        CommonTree EOF2_tree=null;

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:49:5: ( toplevelstmts EOF )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:49:9: toplevelstmts EOF
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:52:1: toplevelstmts : ( directive )* ;
    public final LLVMParser.toplevelstmts_return toplevelstmts() throws RecognitionException {
        LLVMParser.toplevelstmts_return retval = new LLVMParser.toplevelstmts_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        LLVMParser.directive_return directive3 = null;



        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:52:14: ( ( directive )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:52:17: ( directive )*
            {
            root_0 = (CommonTree)adaptor.nil();

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:52:17: ( directive )*
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( ((LA1_0>=NAMED_ID && LA1_0<=QUOTED_ID)||LA1_0==21) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:52:17: directive
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:55:1: directive : ( targetDataLayoutDirective | targetTripleDirective | typeDefinition | globalDataDirective );
    public final LLVMParser.directive_return directive() throws RecognitionException {
        LLVMParser.directive_return retval = new LLVMParser.directive_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        LLVMParser.targetDataLayoutDirective_return targetDataLayoutDirective4 = null;

        LLVMParser.targetTripleDirective_return targetTripleDirective5 = null;

        LLVMParser.typeDefinition_return typeDefinition6 = null;

        LLVMParser.globalDataDirective_return globalDataDirective7 = null;



        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:55:11: ( targetDataLayoutDirective | targetTripleDirective | typeDefinition | globalDataDirective )
            int alt2=4;
            alt2 = dfa2.predict(input);
            switch (alt2) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:55:13: targetDataLayoutDirective
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_targetDataLayoutDirective_in_directive116);
                    targetDataLayoutDirective4=targetDataLayoutDirective();

                    state._fsp--;

                    adaptor.addChild(root_0, targetDataLayoutDirective4.getTree());

                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:56:5: targetTripleDirective
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_targetTripleDirective_in_directive123);
                    targetTripleDirective5=targetTripleDirective();

                    state._fsp--;

                    adaptor.addChild(root_0, targetTripleDirective5.getTree());

                    }
                    break;
                case 3 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:57:5: typeDefinition
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_typeDefinition_in_directive129);
                    typeDefinition6=typeDefinition();

                    state._fsp--;

                    adaptor.addChild(root_0, typeDefinition6.getTree());

                    }
                    break;
                case 4 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:58:5: globalDataDirective
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_globalDataDirective_in_directive135);
                    globalDataDirective7=globalDataDirective();

                    state._fsp--;

                    adaptor.addChild(root_0, globalDataDirective7.getTree());

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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:63:1: targetDataLayoutDirective : 'target' 'datalayout' EQUALS stringLiteral ;
    public final LLVMParser.targetDataLayoutDirective_return targetDataLayoutDirective() throws RecognitionException {
        LLVMParser.targetDataLayoutDirective_return retval = new LLVMParser.targetDataLayoutDirective_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token string_literal8=null;
        Token string_literal9=null;
        Token EQUALS10=null;
        LLVMParser.stringLiteral_return stringLiteral11 = null;


        CommonTree string_literal8_tree=null;
        CommonTree string_literal9_tree=null;
        CommonTree EQUALS10_tree=null;

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:63:27: ( 'target' 'datalayout' EQUALS stringLiteral )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:63:29: 'target' 'datalayout' EQUALS stringLiteral
            {
            root_0 = (CommonTree)adaptor.nil();

            string_literal8=(Token)match(input,21,FOLLOW_21_in_targetDataLayoutDirective154); 
            string_literal8_tree = (CommonTree)adaptor.create(string_literal8);
            adaptor.addChild(root_0, string_literal8_tree);

            string_literal9=(Token)match(input,22,FOLLOW_22_in_targetDataLayoutDirective156); 
            string_literal9_tree = (CommonTree)adaptor.create(string_literal9);
            adaptor.addChild(root_0, string_literal9_tree);

            EQUALS10=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_targetDataLayoutDirective158); 
            EQUALS10_tree = (CommonTree)adaptor.create(EQUALS10);
            adaptor.addChild(root_0, EQUALS10_tree);

            pushFollow(FOLLOW_stringLiteral_in_targetDataLayoutDirective160);
            stringLiteral11=stringLiteral();

            state._fsp--;

            adaptor.addChild(root_0, stringLiteral11.getTree());
             helper.addTargetDataLayoutDirective((stringLiteral11!=null?stringLiteral11.theText:null)); 

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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:67:1: targetTripleDirective : 'target' 'triple' EQUALS stringLiteral ;
    public final LLVMParser.targetTripleDirective_return targetTripleDirective() throws RecognitionException {
        LLVMParser.targetTripleDirective_return retval = new LLVMParser.targetTripleDirective_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token string_literal12=null;
        Token string_literal13=null;
        Token EQUALS14=null;
        LLVMParser.stringLiteral_return stringLiteral15 = null;


        CommonTree string_literal12_tree=null;
        CommonTree string_literal13_tree=null;
        CommonTree EQUALS14_tree=null;

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:67:23: ( 'target' 'triple' EQUALS stringLiteral )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:67:25: 'target' 'triple' EQUALS stringLiteral
            {
            root_0 = (CommonTree)adaptor.nil();

            string_literal12=(Token)match(input,21,FOLLOW_21_in_targetTripleDirective176); 
            string_literal12_tree = (CommonTree)adaptor.create(string_literal12);
            adaptor.addChild(root_0, string_literal12_tree);

            string_literal13=(Token)match(input,23,FOLLOW_23_in_targetTripleDirective178); 
            string_literal13_tree = (CommonTree)adaptor.create(string_literal13);
            adaptor.addChild(root_0, string_literal13_tree);

            EQUALS14=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_targetTripleDirective180); 
            EQUALS14_tree = (CommonTree)adaptor.create(EQUALS14);
            adaptor.addChild(root_0, EQUALS14_tree);

            pushFollow(FOLLOW_stringLiteral_in_targetTripleDirective182);
            stringLiteral15=stringLiteral();

            state._fsp--;

            adaptor.addChild(root_0, stringLiteral15.getTree());
             helper.addTargetTripleDirective((stringLiteral15!=null?stringLiteral15.theText:null)); 

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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:71:1: typeDefinition : identifier EQUALS 'type' type ;
    public final LLVMParser.typeDefinition_return typeDefinition() throws RecognitionException {
        LLVMParser.typeDefinition_return retval = new LLVMParser.typeDefinition_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token EQUALS17=null;
        Token string_literal18=null;
        LLVMParser.identifier_return identifier16 = null;

        LLVMParser.type_return type19 = null;


        CommonTree EQUALS17_tree=null;
        CommonTree string_literal18_tree=null;

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:71:16: ( identifier EQUALS 'type' type )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:71:18: identifier EQUALS 'type' type
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_identifier_in_typeDefinition197);
            identifier16=identifier();

            state._fsp--;

            adaptor.addChild(root_0, identifier16.getTree());
            EQUALS17=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_typeDefinition199); 
            EQUALS17_tree = (CommonTree)adaptor.create(EQUALS17);
            adaptor.addChild(root_0, EQUALS17_tree);

            string_literal18=(Token)match(input,24,FOLLOW_24_in_typeDefinition201); 
            string_literal18_tree = (CommonTree)adaptor.create(string_literal18);
            adaptor.addChild(root_0, string_literal18_tree);

            pushFollow(FOLLOW_type_in_typeDefinition205);
            type19=type();

            state._fsp--;

            adaptor.addChild(root_0, type19.getTree());
             
              	helper.addNewType((identifier16!=null?identifier16.theId:null), (type19!=null?type19.theType:null)); 
              

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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:78:1: type returns [LLType theType] : (t0= inttype | t1= structtype | t2= arraytype | 'void' | t3= symboltype ) ( '*' )* ( paramstype )? ;
    public final LLVMParser.type_return type() throws RecognitionException {
        LLVMParser.type_return retval = new LLVMParser.type_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token string_literal20=null;
        Token char_literal21=null;
        LLVMParser.inttype_return t0 = null;

        LLVMParser.structtype_return t1 = null;

        LLVMParser.arraytype_return t2 = null;

        LLVMParser.symboltype_return t3 = null;

        LLVMParser.paramstype_return paramstype22 = null;


        CommonTree string_literal20_tree=null;
        CommonTree char_literal21_tree=null;


        	  	// ensure we recognize temp symbols like percent 0 as pointing
        	  	// to types rather than variables
        		helper.inTypeContext++;
            
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:90:5: ( (t0= inttype | t1= structtype | t2= arraytype | 'void' | t3= symboltype ) ( '*' )* ( paramstype )? )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:91:2: (t0= inttype | t1= structtype | t2= arraytype | 'void' | t3= symboltype ) ( '*' )* ( paramstype )?
            {
            root_0 = (CommonTree)adaptor.nil();

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:91:2: (t0= inttype | t1= structtype | t2= arraytype | 'void' | t3= symboltype )
            int alt3=5;
            switch ( input.LA(1) ) {
            case INT_TYPE:
                {
                alt3=1;
                }
                break;
            case 27:
                {
                alt3=2;
                }
                break;
            case 29:
                {
                alt3=3;
                }
                break;
            case 25:
                {
                alt3=4;
                }
                break;
            case NAMED_ID:
            case UNNAMED_ID:
            case QUOTED_ID:
                {
                alt3=5;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 3, 0, input);

                throw nvae;
            }

            switch (alt3) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:91:5: t0= inttype
                    {
                    pushFollow(FOLLOW_inttype_in_type258);
                    t0=inttype();

                    state._fsp--;

                    adaptor.addChild(root_0, t0.getTree());
                     retval.theType = (t0!=null?t0.theType:null); 

                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:92:5: t1= structtype
                    {
                    pushFollow(FOLLOW_structtype_in_type269);
                    t1=structtype();

                    state._fsp--;

                    adaptor.addChild(root_0, t1.getTree());
                     retval.theType = (t1!=null?t1.theType:null); 

                    }
                    break;
                case 3 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:93:5: t2= arraytype
                    {
                    pushFollow(FOLLOW_arraytype_in_type279);
                    t2=arraytype();

                    state._fsp--;

                    adaptor.addChild(root_0, t2.getTree());
                     retval.theType = (t2!=null?t2.theType:null); 

                    }
                    break;
                case 4 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:94:5: 'void'
                    {
                    string_literal20=(Token)match(input,25,FOLLOW_25_in_type287); 
                    string_literal20_tree = (CommonTree)adaptor.create(string_literal20);
                    adaptor.addChild(root_0, string_literal20_tree);

                     retval.theType = helper.typeEngine.VOID; 

                    }
                    break;
                case 5 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:95:5: t3= symboltype
                    {
                    pushFollow(FOLLOW_symboltype_in_type304);
                    t3=symboltype();

                    state._fsp--;

                    adaptor.addChild(root_0, t3.getTree());
                     retval.theType = (t3!=null?t3.theType:null); 

                    }
                    break;

            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:98:2: ( '*' )*
            loop4:
            do {
                int alt4=2;
                int LA4_0 = input.LA(1);

                if ( (LA4_0==26) ) {
                    alt4=1;
                }


                switch (alt4) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:98:4: '*'
            	    {
            	    char_literal21=(Token)match(input,26,FOLLOW_26_in_type321); 
            	    char_literal21_tree = (CommonTree)adaptor.create(char_literal21);
            	    adaptor.addChild(root_0, char_literal21_tree);

            	     retval.theType = helper.addPointerType(retval.theType); 

            	    }
            	    break;

            	default :
            	    break loop4;
                }
            } while (true);

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:100:2: ( paramstype )?
            int alt5=2;
            int LA5_0 = input.LA(1);

            if ( (LA5_0==32) ) {
                alt5=1;
            }
            switch (alt5) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:100:3: paramstype
                    {
                    pushFollow(FOLLOW_paramstype_in_type333);
                    paramstype22=paramstype();

                    state._fsp--;

                    adaptor.addChild(root_0, paramstype22.getTree());
                     retval.theType = helper.addCodeType(retval.theType, (paramstype22!=null?paramstype22.theArgs:null)); 

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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:104:1: inttype returns [LLType theType] : INT_TYPE ;
    public final LLVMParser.inttype_return inttype() throws RecognitionException {
        LLVMParser.inttype_return retval = new LLVMParser.inttype_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token INT_TYPE23=null;

        CommonTree INT_TYPE23_tree=null;

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:104:34: ( INT_TYPE )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:104:36: INT_TYPE
            {
            root_0 = (CommonTree)adaptor.nil();

            INT_TYPE23=(Token)match(input,INT_TYPE,FOLLOW_INT_TYPE_in_inttype355); 
            INT_TYPE23_tree = (CommonTree)adaptor.create(INT_TYPE23);
            adaptor.addChild(root_0, INT_TYPE23_tree);

             retval.theType = helper.addIntType((INT_TYPE23!=null?INT_TYPE23.getText():null)); 

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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:108:1: structtype returns [LLType theType] : '{' typeList '}' ;
    public final LLVMParser.structtype_return structtype() throws RecognitionException {
        LLVMParser.structtype_return retval = new LLVMParser.structtype_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token char_literal24=null;
        Token char_literal26=null;
        LLVMParser.typeList_return typeList25 = null;


        CommonTree char_literal24_tree=null;
        CommonTree char_literal26_tree=null;

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:108:38: ( '{' typeList '}' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:108:40: '{' typeList '}'
            {
            root_0 = (CommonTree)adaptor.nil();

            char_literal24=(Token)match(input,27,FOLLOW_27_in_structtype374); 
            char_literal24_tree = (CommonTree)adaptor.create(char_literal24);
            adaptor.addChild(root_0, char_literal24_tree);

            pushFollow(FOLLOW_typeList_in_structtype376);
            typeList25=typeList();

            state._fsp--;

            adaptor.addChild(root_0, typeList25.getTree());
            char_literal26=(Token)match(input,28,FOLLOW_28_in_structtype378); 
            char_literal26_tree = (CommonTree)adaptor.create(char_literal26);
            adaptor.addChild(root_0, char_literal26_tree);


            		retval.theType = helper.addTupleType((typeList25!=null?typeList25.theTypes:null)); 
            	

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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:114:1: arraytype returns [LLType theType] : '[' number 'x' type ']' ;
    public final LLVMParser.arraytype_return arraytype() throws RecognitionException {
        LLVMParser.arraytype_return retval = new LLVMParser.arraytype_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token char_literal27=null;
        Token char_literal29=null;
        Token char_literal31=null;
        LLVMParser.number_return number28 = null;

        LLVMParser.type_return type30 = null;


        CommonTree char_literal27_tree=null;
        CommonTree char_literal29_tree=null;
        CommonTree char_literal31_tree=null;

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:114:36: ( '[' number 'x' type ']' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:114:39: '[' number 'x' type ']'
            {
            root_0 = (CommonTree)adaptor.nil();

            char_literal27=(Token)match(input,29,FOLLOW_29_in_arraytype399); 
            char_literal27_tree = (CommonTree)adaptor.create(char_literal27);
            adaptor.addChild(root_0, char_literal27_tree);

            pushFollow(FOLLOW_number_in_arraytype401);
            number28=number();

            state._fsp--;

            adaptor.addChild(root_0, number28.getTree());
            char_literal29=(Token)match(input,30,FOLLOW_30_in_arraytype403); 
            char_literal29_tree = (CommonTree)adaptor.create(char_literal29);
            adaptor.addChild(root_0, char_literal29_tree);

            pushFollow(FOLLOW_type_in_arraytype405);
            type30=type();

            state._fsp--;

            adaptor.addChild(root_0, type30.getTree());
            char_literal31=(Token)match(input,31,FOLLOW_31_in_arraytype407); 
            char_literal31_tree = (CommonTree)adaptor.create(char_literal31);
            adaptor.addChild(root_0, char_literal31_tree);

             retval.theType = helper.addArrayType((number28!=null?number28.value:0), (type30!=null?type30.theType:null)); 

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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:118:1: paramstype returns [LLType[] theArgs] : '(' typeList ')' ;
    public final LLVMParser.paramstype_return paramstype() throws RecognitionException {
        LLVMParser.paramstype_return retval = new LLVMParser.paramstype_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token char_literal32=null;
        Token char_literal34=null;
        LLVMParser.typeList_return typeList33 = null;


        CommonTree char_literal32_tree=null;
        CommonTree char_literal34_tree=null;

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:118:40: ( '(' typeList ')' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:118:42: '(' typeList ')'
            {
            root_0 = (CommonTree)adaptor.nil();

            char_literal32=(Token)match(input,32,FOLLOW_32_in_paramstype425); 
            char_literal32_tree = (CommonTree)adaptor.create(char_literal32);
            adaptor.addChild(root_0, char_literal32_tree);

            pushFollow(FOLLOW_typeList_in_paramstype427);
            typeList33=typeList();

            state._fsp--;

            adaptor.addChild(root_0, typeList33.getTree());
            char_literal34=(Token)match(input,33,FOLLOW_33_in_paramstype429); 
            char_literal34_tree = (CommonTree)adaptor.create(char_literal34);
            adaptor.addChild(root_0, char_literal34_tree);

             retval.theArgs = (typeList33!=null?typeList33.theTypes:null); 

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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:122:1: typeList returns [LLType[] theTypes] : (t= type ( ',' u= type )* )? ;
    public final LLVMParser.typeList_return typeList() throws RecognitionException {
        LLVMParser.typeList_return retval = new LLVMParser.typeList_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token char_literal35=null;
        LLVMParser.type_return t = null;

        LLVMParser.type_return u = null;


        CommonTree char_literal35_tree=null;


            List<LLType> types = new ArrayList<LLType>();
          
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:131:3: ( (t= type ( ',' u= type )* )? )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:131:5: (t= type ( ',' u= type )* )?
            {
            root_0 = (CommonTree)adaptor.nil();

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:131:5: (t= type ( ',' u= type )* )?
            int alt7=2;
            int LA7_0 = input.LA(1);

            if ( ((LA7_0>=INT_TYPE && LA7_0<=QUOTED_ID)||LA7_0==25||LA7_0==27||LA7_0==29) ) {
                alt7=1;
            }
            switch (alt7) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:131:6: t= type ( ',' u= type )*
                    {
                    pushFollow(FOLLOW_type_in_typeList471);
                    t=type();

                    state._fsp--;

                    adaptor.addChild(root_0, t.getTree());
                     types.add((t!=null?t.theType:null)); 
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:132:7: ( ',' u= type )*
                    loop6:
                    do {
                        int alt6=2;
                        int LA6_0 = input.LA(1);

                        if ( (LA6_0==34) ) {
                            alt6=1;
                        }


                        switch (alt6) {
                    	case 1 :
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:132:8: ',' u= type
                    	    {
                    	    char_literal35=(Token)match(input,34,FOLLOW_34_in_typeList489); 
                    	    char_literal35_tree = (CommonTree)adaptor.create(char_literal35);
                    	    adaptor.addChild(root_0, char_literal35_tree);

                    	    pushFollow(FOLLOW_type_in_typeList493);
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:137:1: symboltype returns [LLType theType] : identifier ;
    public final LLVMParser.symboltype_return symboltype() throws RecognitionException {
        LLVMParser.symboltype_return retval = new LLVMParser.symboltype_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        LLVMParser.identifier_return identifier36 = null;



        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:137:37: ( identifier )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:137:39: identifier
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_identifier_in_symboltype535);
            identifier36=identifier();

            state._fsp--;

            adaptor.addChild(root_0, identifier36.getTree());
             retval.theType = helper.findOrForwardNameType((identifier36!=null?identifier36.theId:null)); 

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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:141:1: globalDataDirective : identifier EQUALS ( linkage )? 'global' typedconstant ;
    public final LLVMParser.globalDataDirective_return globalDataDirective() throws RecognitionException {
        LLVMParser.globalDataDirective_return retval = new LLVMParser.globalDataDirective_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token EQUALS38=null;
        Token string_literal40=null;
        LLVMParser.identifier_return identifier37 = null;

        LLVMParser.linkage_return linkage39 = null;

        LLVMParser.typedconstant_return typedconstant41 = null;


        CommonTree EQUALS38_tree=null;
        CommonTree string_literal40_tree=null;

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:141:21: ( identifier EQUALS ( linkage )? 'global' typedconstant )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:141:23: identifier EQUALS ( linkage )? 'global' typedconstant
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_identifier_in_globalDataDirective549);
            identifier37=identifier();

            state._fsp--;

            adaptor.addChild(root_0, identifier37.getTree());
            EQUALS38=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_globalDataDirective551); 
            EQUALS38_tree = (CommonTree)adaptor.create(EQUALS38);
            adaptor.addChild(root_0, EQUALS38_tree);

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:141:41: ( linkage )?
            int alt8=2;
            int LA8_0 = input.LA(1);

            if ( ((LA8_0>=36 && LA8_0<=49)) ) {
                alt8=1;
            }
            switch (alt8) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:141:41: linkage
                    {
                    pushFollow(FOLLOW_linkage_in_globalDataDirective553);
                    linkage39=linkage();

                    state._fsp--;

                    adaptor.addChild(root_0, linkage39.getTree());

                    }
                    break;

            }

            string_literal40=(Token)match(input,35,FOLLOW_35_in_globalDataDirective556); 
            string_literal40_tree = (CommonTree)adaptor.create(string_literal40);
            adaptor.addChild(root_0, string_literal40_tree);

            pushFollow(FOLLOW_typedconstant_in_globalDataDirective558);
            typedconstant41=typedconstant();

            state._fsp--;

            adaptor.addChild(root_0, typedconstant41.getTree());
             helper.addGlobalDataDirective((identifier37!=null?input.toString(identifier37.start,identifier37.stop):null), (linkage39!=null?linkage39.value:null), (typedconstant41!=null?typedconstant41.op:null)); 

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

    public static class linkage_return extends ParserRuleReturnScope {
        public LLLinkage value;
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "linkage"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:145:1: linkage returns [ LLLinkage value ] : ( 'private' | 'linker_private' | 'internal' | 'available_externally' | 'linkonce' | 'weak' | 'common' | 'appending' | 'extern_weak' | 'linkonce_odr' | 'weak_odr' | 'externally_visible' | 'dllimport' | 'dllexport' ) ;
    public final LLVMParser.linkage_return linkage() throws RecognitionException {
        LLVMParser.linkage_return retval = new LLVMParser.linkage_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token set42=null;

        CommonTree set42_tree=null;

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:145:37: ( ( 'private' | 'linker_private' | 'internal' | 'available_externally' | 'linkonce' | 'weak' | 'common' | 'appending' | 'extern_weak' | 'linkonce_odr' | 'weak_odr' | 'externally_visible' | 'dllimport' | 'dllexport' ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:145:39: ( 'private' | 'linker_private' | 'internal' | 'available_externally' | 'linkonce' | 'weak' | 'common' | 'appending' | 'extern_weak' | 'linkonce_odr' | 'weak_odr' | 'externally_visible' | 'dllimport' | 'dllexport' )
            {
            root_0 = (CommonTree)adaptor.nil();

            set42=(Token)input.LT(1);
            if ( (input.LA(1)>=36 && input.LA(1)<=49) ) {
                input.consume();
                adaptor.addChild(root_0, (CommonTree)adaptor.create(set42));
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

    public static class typedconstant_return extends ParserRuleReturnScope {
        public LLOperand op;
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "typedconstant"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:151:1: typedconstant returns [ LLOperand op ] : type ( number | charconst | stringconst | structconst | arrayconst | symbolconst | 'zeroinitializer' ) ;
    public final LLVMParser.typedconstant_return typedconstant() throws RecognitionException {
        LLVMParser.typedconstant_return retval = new LLVMParser.typedconstant_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token string_literal50=null;
        LLVMParser.type_return type43 = null;

        LLVMParser.number_return number44 = null;

        LLVMParser.charconst_return charconst45 = null;

        LLVMParser.stringconst_return stringconst46 = null;

        LLVMParser.structconst_return structconst47 = null;

        LLVMParser.arrayconst_return arrayconst48 = null;

        LLVMParser.symbolconst_return symbolconst49 = null;


        CommonTree string_literal50_tree=null;

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:151:40: ( type ( number | charconst | stringconst | structconst | arrayconst | symbolconst | 'zeroinitializer' ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:151:42: type ( number | charconst | stringconst | structconst | arrayconst | symbolconst | 'zeroinitializer' )
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_type_in_typedconstant658);
            type43=type();

            state._fsp--;

            adaptor.addChild(root_0, type43.getTree());
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:152:2: ( number | charconst | stringconst | structconst | arrayconst | symbolconst | 'zeroinitializer' )
            int alt9=7;
            switch ( input.LA(1) ) {
            case NUMBER:
                {
                alt9=1;
                }
                break;
            case CHAR_LITERAL:
                {
                alt9=2;
                }
                break;
            case CSTRING_LITERAL:
                {
                alt9=3;
                }
                break;
            case 27:
                {
                alt9=4;
                }
                break;
            case 29:
                {
                alt9=5;
                }
                break;
            case NAMED_ID:
            case UNNAMED_ID:
            case QUOTED_ID:
                {
                alt9=6;
                }
                break;
            case 50:
                {
                alt9=7;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 9, 0, input);

                throw nvae;
            }

            switch (alt9) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:152:5: number
                    {
                    pushFollow(FOLLOW_number_in_typedconstant664);
                    number44=number();

                    state._fsp--;

                    adaptor.addChild(root_0, number44.getTree());
                     retval.op = new LLConstOp((type43!=null?type43.theType:null), (number44!=null?number44.value:0)); 

                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:153:4: charconst
                    {
                    pushFollow(FOLLOW_charconst_in_typedconstant671);
                    charconst45=charconst();

                    state._fsp--;

                    adaptor.addChild(root_0, charconst45.getTree());
                     retval.op = new LLConstOp((type43!=null?type43.theType:null), (int)(charconst45!=null?charconst45.value:0)); 

                    }
                    break;
                case 3 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:154:4: stringconst
                    {
                    pushFollow(FOLLOW_stringconst_in_typedconstant678);
                    stringconst46=stringconst();

                    state._fsp--;

                    adaptor.addChild(root_0, stringconst46.getTree());
                     retval.op = new LLStringLitOp((LLArrayType)(type43!=null?type43.theType:null), (stringconst46!=null?stringconst46.value:null)); 

                    }
                    break;
                case 4 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:155:4: structconst
                    {
                    pushFollow(FOLLOW_structconst_in_typedconstant685);
                    structconst47=structconst();

                    state._fsp--;

                    adaptor.addChild(root_0, structconst47.getTree());
                     retval.op = new LLStructOp((LLAggregateType)(type43!=null?type43.theType:null), (structconst47!=null?structconst47.values:null)); 

                    }
                    break;
                case 5 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:156:4: arrayconst
                    {
                    pushFollow(FOLLOW_arrayconst_in_typedconstant692);
                    arrayconst48=arrayconst();

                    state._fsp--;

                    adaptor.addChild(root_0, arrayconst48.getTree());
                     retval.op = new LLArrayOp((LLArrayType)(type43!=null?type43.theType:null), (arrayconst48!=null?arrayconst48.values:null)); 

                    }
                    break;
                case 6 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:157:4: symbolconst
                    {
                    pushFollow(FOLLOW_symbolconst_in_typedconstant700);
                    symbolconst49=symbolconst();

                    state._fsp--;

                    adaptor.addChild(root_0, symbolconst49.getTree());
                     retval.op = helper.getSymbolOp((symbolconst49!=null?symbolconst49.theId:null), (symbolconst49!=null?symbolconst49.theSymbol:null)); 

                    }
                    break;
                case 7 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:158:4: 'zeroinitializer'
                    {
                    string_literal50=(Token)match(input,50,FOLLOW_50_in_typedconstant708); 
                    string_literal50_tree = (CommonTree)adaptor.create(string_literal50);
                    adaptor.addChild(root_0, string_literal50_tree);

                     retval.op = new LLZeroInitOp((type43!=null?type43.theType:null)); 

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
    // $ANTLR end "typedconstant"

    public static class symbolconst_return extends ParserRuleReturnScope {
        public String theId;
        public ISymbol theSymbol;
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "symbolconst"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:162:1: symbolconst returns [ String theId, ISymbol theSymbol ] : identifier ;
    public final LLVMParser.symbolconst_return symbolconst() throws RecognitionException {
        LLVMParser.symbolconst_return retval = new LLVMParser.symbolconst_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        LLVMParser.identifier_return identifier51 = null;



        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:162:57: ( identifier )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:163:3: identifier
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_identifier_in_symbolconst730);
            identifier51=identifier();

            state._fsp--;

            adaptor.addChild(root_0, identifier51.getTree());
             retval.theSymbol = helper.findSymbol((identifier51!=null?identifier51.theId:null)); retval.theId = (identifier51!=null?identifier51.theId:null); 

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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:167:1: charconst returns [ char value ] : charLiteral ;
    public final LLVMParser.charconst_return charconst() throws RecognitionException {
        LLVMParser.charconst_return retval = new LLVMParser.charconst_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        LLVMParser.charLiteral_return charLiteral52 = null;



        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:167:34: ( charLiteral )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:168:2: charLiteral
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_charLiteral_in_charconst754);
            charLiteral52=charLiteral();

            state._fsp--;

            adaptor.addChild(root_0, charLiteral52.getTree());
             
            		retval.value = (charLiteral52!=null?charLiteral52.theText:null).charAt(0);
            	

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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:173:1: stringconst returns [ String value ] : cstringLiteral ;
    public final LLVMParser.stringconst_return stringconst() throws RecognitionException {
        LLVMParser.stringconst_return retval = new LLVMParser.stringconst_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        LLVMParser.cstringLiteral_return cstringLiteral53 = null;



        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:173:39: ( cstringLiteral )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:174:2: cstringLiteral
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_cstringLiteral_in_stringconst771);
            cstringLiteral53=cstringLiteral();

            state._fsp--;

            adaptor.addChild(root_0, cstringLiteral53.getTree());

            		retval.value = (cstringLiteral53!=null?cstringLiteral53.theText:null);
            	

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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:179:1: structconst returns [ LLOperand[] values ] : '{' (t0= typedconstant ( ',' t1= typedconstant )* )? '}' ;
    public final LLVMParser.structconst_return structconst() throws RecognitionException {
        LLVMParser.structconst_return retval = new LLVMParser.structconst_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token char_literal54=null;
        Token char_literal55=null;
        Token char_literal56=null;
        LLVMParser.typedconstant_return t0 = null;

        LLVMParser.typedconstant_return t1 = null;


        CommonTree char_literal54_tree=null;
        CommonTree char_literal55_tree=null;
        CommonTree char_literal56_tree=null;


            List<LLOperand> ops = new ArrayList<LLOperand>();
          
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:186:3: ( '{' (t0= typedconstant ( ',' t1= typedconstant )* )? '}' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:187:3: '{' (t0= typedconstant ( ',' t1= typedconstant )* )? '}'
            {
            root_0 = (CommonTree)adaptor.nil();

            char_literal54=(Token)match(input,27,FOLLOW_27_in_structconst808); 
            char_literal54_tree = (CommonTree)adaptor.create(char_literal54);
            adaptor.addChild(root_0, char_literal54_tree);

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:187:7: (t0= typedconstant ( ',' t1= typedconstant )* )?
            int alt11=2;
            int LA11_0 = input.LA(1);

            if ( ((LA11_0>=INT_TYPE && LA11_0<=QUOTED_ID)||LA11_0==25||LA11_0==27||LA11_0==29) ) {
                alt11=1;
            }
            switch (alt11) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:187:8: t0= typedconstant ( ',' t1= typedconstant )*
                    {
                    pushFollow(FOLLOW_typedconstant_in_structconst813);
                    t0=typedconstant();

                    state._fsp--;

                    adaptor.addChild(root_0, t0.getTree());
                     ops.add((t0!=null?t0.op:null)); 
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:188:5: ( ',' t1= typedconstant )*
                    loop10:
                    do {
                        int alt10=2;
                        int LA10_0 = input.LA(1);

                        if ( (LA10_0==34) ) {
                            alt10=1;
                        }


                        switch (alt10) {
                    	case 1 :
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:188:6: ',' t1= typedconstant
                    	    {
                    	    char_literal55=(Token)match(input,34,FOLLOW_34_in_structconst823); 
                    	    char_literal55_tree = (CommonTree)adaptor.create(char_literal55);
                    	    adaptor.addChild(root_0, char_literal55_tree);

                    	    pushFollow(FOLLOW_typedconstant_in_structconst827);
                    	    t1=typedconstant();

                    	    state._fsp--;

                    	    adaptor.addChild(root_0, t1.getTree());
                    	     ops.add((t1!=null?t1.op:null)); 

                    	    }
                    	    break;

                    	default :
                    	    break loop10;
                        }
                    } while (true);


                    }
                    break;

            }

            char_literal56=(Token)match(input,28,FOLLOW_28_in_structconst848); 
            char_literal56_tree = (CommonTree)adaptor.create(char_literal56);
            adaptor.addChild(root_0, char_literal56_tree);


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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:195:1: arrayconst returns [ LLOperand[] values ] : '[' (t0= typedconstant ( ',' t1= typedconstant )* )? ']' ;
    public final LLVMParser.arrayconst_return arrayconst() throws RecognitionException {
        LLVMParser.arrayconst_return retval = new LLVMParser.arrayconst_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token char_literal57=null;
        Token char_literal58=null;
        Token char_literal59=null;
        LLVMParser.typedconstant_return t0 = null;

        LLVMParser.typedconstant_return t1 = null;


        CommonTree char_literal57_tree=null;
        CommonTree char_literal58_tree=null;
        CommonTree char_literal59_tree=null;


            List<LLOperand> ops = new ArrayList<LLOperand>();
          
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:202:3: ( '[' (t0= typedconstant ( ',' t1= typedconstant )* )? ']' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:203:3: '[' (t0= typedconstant ( ',' t1= typedconstant )* )? ']'
            {
            root_0 = (CommonTree)adaptor.nil();

            char_literal57=(Token)match(input,29,FOLLOW_29_in_arrayconst886); 
            char_literal57_tree = (CommonTree)adaptor.create(char_literal57);
            adaptor.addChild(root_0, char_literal57_tree);

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:203:7: (t0= typedconstant ( ',' t1= typedconstant )* )?
            int alt13=2;
            int LA13_0 = input.LA(1);

            if ( ((LA13_0>=INT_TYPE && LA13_0<=QUOTED_ID)||LA13_0==25||LA13_0==27||LA13_0==29) ) {
                alt13=1;
            }
            switch (alt13) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:203:8: t0= typedconstant ( ',' t1= typedconstant )*
                    {
                    pushFollow(FOLLOW_typedconstant_in_arrayconst891);
                    t0=typedconstant();

                    state._fsp--;

                    adaptor.addChild(root_0, t0.getTree());
                     ops.add((t0!=null?t0.op:null)); 
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:204:5: ( ',' t1= typedconstant )*
                    loop12:
                    do {
                        int alt12=2;
                        int LA12_0 = input.LA(1);

                        if ( (LA12_0==34) ) {
                            alt12=1;
                        }


                        switch (alt12) {
                    	case 1 :
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:204:6: ',' t1= typedconstant
                    	    {
                    	    char_literal58=(Token)match(input,34,FOLLOW_34_in_arrayconst901); 
                    	    char_literal58_tree = (CommonTree)adaptor.create(char_literal58);
                    	    adaptor.addChild(root_0, char_literal58_tree);

                    	    pushFollow(FOLLOW_typedconstant_in_arrayconst905);
                    	    t1=typedconstant();

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

            char_literal59=(Token)match(input,31,FOLLOW_31_in_arrayconst926); 
            char_literal59_tree = (CommonTree)adaptor.create(char_literal59);
            adaptor.addChild(root_0, char_literal59_tree);


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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:210:1: identifier returns [String theId] : ( NAMED_ID | UNNAMED_ID | QUOTED_ID ) ;
    public final LLVMParser.identifier_return identifier() throws RecognitionException {
        LLVMParser.identifier_return retval = new LLVMParser.identifier_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token NAMED_ID60=null;
        Token UNNAMED_ID61=null;
        Token QUOTED_ID62=null;

        CommonTree NAMED_ID60_tree=null;
        CommonTree UNNAMED_ID61_tree=null;
        CommonTree QUOTED_ID62_tree=null;

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:210:35: ( ( NAMED_ID | UNNAMED_ID | QUOTED_ID ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:211:3: ( NAMED_ID | UNNAMED_ID | QUOTED_ID )
            {
            root_0 = (CommonTree)adaptor.nil();

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:211:3: ( NAMED_ID | UNNAMED_ID | QUOTED_ID )
            int alt14=3;
            switch ( input.LA(1) ) {
            case NAMED_ID:
                {
                alt14=1;
                }
                break;
            case UNNAMED_ID:
                {
                alt14=2;
                }
                break;
            case QUOTED_ID:
                {
                alt14=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 14, 0, input);

                throw nvae;
            }

            switch (alt14) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:212:2: NAMED_ID
                    {
                    NAMED_ID60=(Token)match(input,NAMED_ID,FOLLOW_NAMED_ID_in_identifier950); 
                    NAMED_ID60_tree = (CommonTree)adaptor.create(NAMED_ID60);
                    adaptor.addChild(root_0, NAMED_ID60_tree);

                     retval.theId = (NAMED_ID60!=null?NAMED_ID60.getText():null); 

                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:213:5: UNNAMED_ID
                    {
                    UNNAMED_ID61=(Token)match(input,UNNAMED_ID,FOLLOW_UNNAMED_ID_in_identifier961); 
                    UNNAMED_ID61_tree = (CommonTree)adaptor.create(UNNAMED_ID61);
                    adaptor.addChild(root_0, UNNAMED_ID61_tree);

                     retval.theId = (UNNAMED_ID61!=null?UNNAMED_ID61.getText():null); 

                    }
                    break;
                case 3 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:214:5: QUOTED_ID
                    {
                    QUOTED_ID62=(Token)match(input,QUOTED_ID,FOLLOW_QUOTED_ID_in_identifier969); 
                    QUOTED_ID62_tree = (CommonTree)adaptor.create(QUOTED_ID62);
                    adaptor.addChild(root_0, QUOTED_ID62_tree);

                     retval.theId = (QUOTED_ID62!=null?QUOTED_ID62.getText():null).substring(0,1) 
                      						+ helper.unescape((QUOTED_ID62!=null?QUOTED_ID62.getText():null).substring(1), '"'); 

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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:219:1: number returns [int value] : NUMBER ;
    public final LLVMParser.number_return number() throws RecognitionException {
        LLVMParser.number_return retval = new LLVMParser.number_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token NUMBER63=null;

        CommonTree NUMBER63_tree=null;

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:219:28: ( NUMBER )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:219:30: NUMBER
            {
            root_0 = (CommonTree)adaptor.nil();

            NUMBER63=(Token)match(input,NUMBER,FOLLOW_NUMBER_in_number991); 
            NUMBER63_tree = (CommonTree)adaptor.create(NUMBER63);
            adaptor.addChild(root_0, NUMBER63_tree);

             retval.value = Integer.parseInt((NUMBER63!=null?NUMBER63.getText():null)); 

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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:222:1: charLiteral returns [String theText] : CHAR_LITERAL ;
    public final LLVMParser.charLiteral_return charLiteral() throws RecognitionException {
        LLVMParser.charLiteral_return retval = new LLVMParser.charLiteral_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token CHAR_LITERAL64=null;

        CommonTree CHAR_LITERAL64_tree=null;

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:222:38: ( CHAR_LITERAL )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:222:40: CHAR_LITERAL
            {
            root_0 = (CommonTree)adaptor.nil();

            CHAR_LITERAL64=(Token)match(input,CHAR_LITERAL,FOLLOW_CHAR_LITERAL_in_charLiteral1009); 
            CHAR_LITERAL64_tree = (CommonTree)adaptor.create(CHAR_LITERAL64);
            adaptor.addChild(root_0, CHAR_LITERAL64_tree);

             
              retval.theText = LLParserHelper.unescape((CHAR_LITERAL64!=null?CHAR_LITERAL64.getText():null), '\'');
              

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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:228:1: stringLiteral returns [String theText] : STRING_LITERAL ;
    public final LLVMParser.stringLiteral_return stringLiteral() throws RecognitionException {
        LLVMParser.stringLiteral_return retval = new LLVMParser.stringLiteral_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token STRING_LITERAL65=null;

        CommonTree STRING_LITERAL65_tree=null;

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:228:40: ( STRING_LITERAL )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:228:42: STRING_LITERAL
            {
            root_0 = (CommonTree)adaptor.nil();

            STRING_LITERAL65=(Token)match(input,STRING_LITERAL,FOLLOW_STRING_LITERAL_in_stringLiteral1029); 
            STRING_LITERAL65_tree = (CommonTree)adaptor.create(STRING_LITERAL65);
            adaptor.addChild(root_0, STRING_LITERAL65_tree);


              retval.theText = LLParserHelper.unescape((STRING_LITERAL65!=null?STRING_LITERAL65.getText():null), '"');
              

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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:234:1: cstringLiteral returns [String theText] : CSTRING_LITERAL ;
    public final LLVMParser.cstringLiteral_return cstringLiteral() throws RecognitionException {
        LLVMParser.cstringLiteral_return retval = new LLVMParser.cstringLiteral_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token CSTRING_LITERAL66=null;

        CommonTree CSTRING_LITERAL66_tree=null;

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:234:41: ( CSTRING_LITERAL )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:234:43: CSTRING_LITERAL
            {
            root_0 = (CommonTree)adaptor.nil();

            CSTRING_LITERAL66=(Token)match(input,CSTRING_LITERAL,FOLLOW_CSTRING_LITERAL_in_cstringLiteral1050); 
            CSTRING_LITERAL66_tree = (CommonTree)adaptor.create(CSTRING_LITERAL66);
            adaptor.addChild(root_0, CSTRING_LITERAL66_tree);


              retval.theText = LLParserHelper.unescape((CSTRING_LITERAL66!=null?CSTRING_LITERAL66.getText():null).substring(1), '"');
              

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

    // Delegated rules


    protected DFA2 dfa2 = new DFA2(this);
    static final String DFA2_eotS =
        "\12\uffff";
    static final String DFA2_eofS =
        "\12\uffff";
    static final String DFA2_minS =
        "\1\6\1\26\3\4\2\uffff\1\30\2\uffff";
    static final String DFA2_maxS =
        "\1\25\1\27\3\4\2\uffff\1\61\2\uffff";
    static final String DFA2_acceptS =
        "\5\uffff\1\1\1\2\1\uffff\1\3\1\4";
    static final String DFA2_specialS =
        "\12\uffff}>";
    static final String[] DFA2_transitionS = {
            "\1\2\1\3\1\4\14\uffff\1\1",
            "\1\5\1\6",
            "\1\7",
            "\1\7",
            "\1\7",
            "",
            "",
            "\1\10\12\uffff\17\11",
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
            return "55:1: directive : ( targetDataLayoutDirective | targetTripleDirective | typeDefinition | globalDataDirective );";
        }
    }
 

    public static final BitSet FOLLOW_toplevelstmts_in_prog69 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_prog71 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_directive_in_toplevelstmts101 = new BitSet(new long[]{0x00000000002001C2L});
    public static final BitSet FOLLOW_targetDataLayoutDirective_in_directive116 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_targetTripleDirective_in_directive123 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_typeDefinition_in_directive129 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_globalDataDirective_in_directive135 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_21_in_targetDataLayoutDirective154 = new BitSet(new long[]{0x0000000000400000L});
    public static final BitSet FOLLOW_22_in_targetDataLayoutDirective156 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_EQUALS_in_targetDataLayoutDirective158 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_stringLiteral_in_targetDataLayoutDirective160 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_21_in_targetTripleDirective176 = new BitSet(new long[]{0x0000000000800000L});
    public static final BitSet FOLLOW_23_in_targetTripleDirective178 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_EQUALS_in_targetTripleDirective180 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_stringLiteral_in_targetTripleDirective182 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_identifier_in_typeDefinition197 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_EQUALS_in_typeDefinition199 = new BitSet(new long[]{0x0000000001000000L});
    public static final BitSet FOLLOW_24_in_typeDefinition201 = new BitSet(new long[]{0x000000002A0001E0L});
    public static final BitSet FOLLOW_type_in_typeDefinition205 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_inttype_in_type258 = new BitSet(new long[]{0x0000000104000002L});
    public static final BitSet FOLLOW_structtype_in_type269 = new BitSet(new long[]{0x0000000104000002L});
    public static final BitSet FOLLOW_arraytype_in_type279 = new BitSet(new long[]{0x0000000104000002L});
    public static final BitSet FOLLOW_25_in_type287 = new BitSet(new long[]{0x0000000104000002L});
    public static final BitSet FOLLOW_symboltype_in_type304 = new BitSet(new long[]{0x0000000104000002L});
    public static final BitSet FOLLOW_26_in_type321 = new BitSet(new long[]{0x0000000104000002L});
    public static final BitSet FOLLOW_paramstype_in_type333 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INT_TYPE_in_inttype355 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_27_in_structtype374 = new BitSet(new long[]{0x000000003A0001E0L});
    public static final BitSet FOLLOW_typeList_in_structtype376 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_28_in_structtype378 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_29_in_arraytype399 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_number_in_arraytype401 = new BitSet(new long[]{0x0000000040000000L});
    public static final BitSet FOLLOW_30_in_arraytype403 = new BitSet(new long[]{0x000000002A0001E0L});
    public static final BitSet FOLLOW_type_in_arraytype405 = new BitSet(new long[]{0x0000000080000000L});
    public static final BitSet FOLLOW_31_in_arraytype407 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_32_in_paramstype425 = new BitSet(new long[]{0x000000022A0001E0L});
    public static final BitSet FOLLOW_typeList_in_paramstype427 = new BitSet(new long[]{0x0000000200000000L});
    public static final BitSet FOLLOW_33_in_paramstype429 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_type_in_typeList471 = new BitSet(new long[]{0x0000000400000002L});
    public static final BitSet FOLLOW_34_in_typeList489 = new BitSet(new long[]{0x000000002A0001E0L});
    public static final BitSet FOLLOW_type_in_typeList493 = new BitSet(new long[]{0x0000000400000002L});
    public static final BitSet FOLLOW_identifier_in_symboltype535 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_identifier_in_globalDataDirective549 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_EQUALS_in_globalDataDirective551 = new BitSet(new long[]{0x0003FFF800000000L});
    public static final BitSet FOLLOW_linkage_in_globalDataDirective553 = new BitSet(new long[]{0x0000000800000000L});
    public static final BitSet FOLLOW_35_in_globalDataDirective556 = new BitSet(new long[]{0x000000002A0001E0L});
    public static final BitSet FOLLOW_typedconstant_in_globalDataDirective558 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_linkage576 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_type_in_typedconstant658 = new BitSet(new long[]{0x00040000280017C0L});
    public static final BitSet FOLLOW_number_in_typedconstant664 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_charconst_in_typedconstant671 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_stringconst_in_typedconstant678 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_structconst_in_typedconstant685 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_arrayconst_in_typedconstant692 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_symbolconst_in_typedconstant700 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_50_in_typedconstant708 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_identifier_in_symbolconst730 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_charLiteral_in_charconst754 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cstringLiteral_in_stringconst771 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_27_in_structconst808 = new BitSet(new long[]{0x000000003A0001E0L});
    public static final BitSet FOLLOW_typedconstant_in_structconst813 = new BitSet(new long[]{0x0000000410000000L});
    public static final BitSet FOLLOW_34_in_structconst823 = new BitSet(new long[]{0x000000002A0001E0L});
    public static final BitSet FOLLOW_typedconstant_in_structconst827 = new BitSet(new long[]{0x0000000410000000L});
    public static final BitSet FOLLOW_28_in_structconst848 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_29_in_arrayconst886 = new BitSet(new long[]{0x00000000AA0001E0L});
    public static final BitSet FOLLOW_typedconstant_in_arrayconst891 = new BitSet(new long[]{0x0000000480000000L});
    public static final BitSet FOLLOW_34_in_arrayconst901 = new BitSet(new long[]{0x000000002A0001E0L});
    public static final BitSet FOLLOW_typedconstant_in_arrayconst905 = new BitSet(new long[]{0x0000000480000000L});
    public static final BitSet FOLLOW_31_in_arrayconst926 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NAMED_ID_in_identifier950 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_UNNAMED_ID_in_identifier961 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_QUOTED_ID_in_identifier969 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NUMBER_in_number991 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CHAR_LITERAL_in_charLiteral1009 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_LITERAL_in_stringLiteral1029 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CSTRING_LITERAL_in_cstringLiteral1050 = new BitSet(new long[]{0x0000000000000002L});

}