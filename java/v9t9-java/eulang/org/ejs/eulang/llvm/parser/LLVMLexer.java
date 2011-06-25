// $ANTLR 3.2 Sep 23, 2009 12:02:23 /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g 2010-11-26 08:32:04

package org.ejs.eulang.llvm.parser;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

public class LLVMLexer extends Lexer {
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
    public static final int T__139=139;
    public static final int T__138=138;
    public static final int T__137=137;
    public static final int T__136=136;
    public static final int T__80=80;
    public static final int T__81=81;
    public static final int T__82=82;
    public static final int T__83=83;
    public static final int NUMBER_SUFFIX=18;
    public static final int NUMBER=10;
    public static final int NAMED_ID=7;
    public static final int INT_TYPE=6;
    public static final int T__85=85;
    public static final int T__141=141;
    public static final int T__84=84;
    public static final int T__142=142;
    public static final int T__87=87;
    public static final int T__86=86;
    public static final int T__140=140;
    public static final int T__89=89;
    public static final int T__145=145;
    public static final int T__146=146;
    public static final int T__88=88;
    public static final int SYM_PFX=17;
    public static final int T__143=143;
    public static final int T__144=144;
    public static final int T__126=126;
    public static final int T__125=125;
    public static final int T__128=128;
    public static final int T__127=127;
    public static final int WS=20;
    public static final int T__71=71;
    public static final int T__129=129;
    public static final int T__72=72;
    public static final int NAME_SUFFIX=19;
    public static final int T__70=70;
    public static final int T__76=76;
    public static final int T__75=75;
    public static final int T__130=130;
    public static final int T__74=74;
    public static final int T__131=131;
    public static final int T__73=73;
    public static final int T__132=132;
    public static final int T__133=133;
    public static final int T__79=79;
    public static final int T__134=134;
    public static final int T__78=78;
    public static final int T__135=135;
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
    public static final int T__60=60;
    public static final int DEFINE=14;
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
    public static final int T__103=103;
    public static final int T__59=59;
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
    public static final int T__35=35;
    public static final int NEWLINE=4;
    public static final int T__36=36;
    public static final int CHAR_LITERAL=11;
    public static final int T__37=37;
    public static final int LABEL=15;
    public static final int T__38=38;
    public static final int T__39=39;
    public static final int UNNAMED_ID=8;
    public static final int CSTRING_LITERAL=13;

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

    public LLVMLexer() {;} 
    public LLVMLexer(CharStream input) {
        this(input, new RecognizerSharedState());
    }
    public LLVMLexer(CharStream input, RecognizerSharedState state) {
        super(input,state);

    }
    public String getGrammarFileName() { return "/home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g"; }

    // $ANTLR start "T__23"
    public final void mT__23() throws RecognitionException {
        try {
            int _type = T__23;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:22:7: ( 'target' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:22:9: 'target'
            {
            match("target"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__23"

    // $ANTLR start "T__24"
    public final void mT__24() throws RecognitionException {
        try {
            int _type = T__24;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:23:7: ( 'datalayout' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:23:9: 'datalayout'
            {
            match("datalayout"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__24"

    // $ANTLR start "T__25"
    public final void mT__25() throws RecognitionException {
        try {
            int _type = T__25;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:24:7: ( 'triple' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:24:9: 'triple'
            {
            match("triple"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__25"

    // $ANTLR start "T__26"
    public final void mT__26() throws RecognitionException {
        try {
            int _type = T__26;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:25:7: ( 'type' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:25:9: 'type'
            {
            match("type"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__26"

    // $ANTLR start "T__27"
    public final void mT__27() throws RecognitionException {
        try {
            int _type = T__27;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:26:7: ( 'void' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:26:9: 'void'
            {
            match("void"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__27"

    // $ANTLR start "T__28"
    public final void mT__28() throws RecognitionException {
        try {
            int _type = T__28;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:27:7: ( 'label' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:27:9: 'label'
            {
            match("label"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__28"

    // $ANTLR start "T__29"
    public final void mT__29() throws RecognitionException {
        try {
            int _type = T__29;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:28:7: ( '*' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:28:9: '*'
            {
            match('*'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__29"

    // $ANTLR start "T__30"
    public final void mT__30() throws RecognitionException {
        try {
            int _type = T__30;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:29:7: ( '{' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:29:9: '{'
            {
            match('{'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__30"

    // $ANTLR start "T__31"
    public final void mT__31() throws RecognitionException {
        try {
            int _type = T__31;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:30:7: ( '}' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:30:9: '}'
            {
            match('}'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__31"

    // $ANTLR start "T__32"
    public final void mT__32() throws RecognitionException {
        try {
            int _type = T__32;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:31:7: ( '[' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:31:9: '['
            {
            match('['); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__32"

    // $ANTLR start "T__33"
    public final void mT__33() throws RecognitionException {
        try {
            int _type = T__33;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:32:7: ( 'x' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:32:9: 'x'
            {
            match('x'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__33"

    // $ANTLR start "T__34"
    public final void mT__34() throws RecognitionException {
        try {
            int _type = T__34;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:33:7: ( ']' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:33:9: ']'
            {
            match(']'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__34"

    // $ANTLR start "T__35"
    public final void mT__35() throws RecognitionException {
        try {
            int _type = T__35;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:34:7: ( '(' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:34:9: '('
            {
            match('('); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__35"

    // $ANTLR start "T__36"
    public final void mT__36() throws RecognitionException {
        try {
            int _type = T__36;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:35:7: ( ')' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:35:9: ')'
            {
            match(')'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__36"

    // $ANTLR start "T__37"
    public final void mT__37() throws RecognitionException {
        try {
            int _type = T__37;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:36:7: ( ',' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:36:9: ','
            {
            match(','); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__37"

    // $ANTLR start "T__38"
    public final void mT__38() throws RecognitionException {
        try {
            int _type = T__38;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:37:7: ( 'global' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:37:9: 'global'
            {
            match("global"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__38"

    // $ANTLR start "T__39"
    public final void mT__39() throws RecognitionException {
        try {
            int _type = T__39;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:38:7: ( 'constant' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:38:9: 'constant'
            {
            match("constant"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__39"

    // $ANTLR start "T__40"
    public final void mT__40() throws RecognitionException {
        try {
            int _type = T__40;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:39:7: ( 'addrspace' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:39:9: 'addrspace'
            {
            match("addrspace"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__40"

    // $ANTLR start "T__41"
    public final void mT__41() throws RecognitionException {
        try {
            int _type = T__41;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:40:7: ( 'private' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:40:9: 'private'
            {
            match("private"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__41"

    // $ANTLR start "T__42"
    public final void mT__42() throws RecognitionException {
        try {
            int _type = T__42;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:41:7: ( 'linker_private' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:41:9: 'linker_private'
            {
            match("linker_private"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__42"

    // $ANTLR start "T__43"
    public final void mT__43() throws RecognitionException {
        try {
            int _type = T__43;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:42:7: ( 'internal' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:42:9: 'internal'
            {
            match("internal"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__43"

    // $ANTLR start "T__44"
    public final void mT__44() throws RecognitionException {
        try {
            int _type = T__44;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:43:7: ( 'available_externally' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:43:9: 'available_externally'
            {
            match("available_externally"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__44"

    // $ANTLR start "T__45"
    public final void mT__45() throws RecognitionException {
        try {
            int _type = T__45;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:44:7: ( 'linkonce' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:44:9: 'linkonce'
            {
            match("linkonce"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__45"

    // $ANTLR start "T__46"
    public final void mT__46() throws RecognitionException {
        try {
            int _type = T__46;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:45:7: ( 'weak' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:45:9: 'weak'
            {
            match("weak"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__46"

    // $ANTLR start "T__47"
    public final void mT__47() throws RecognitionException {
        try {
            int _type = T__47;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:46:7: ( 'common' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:46:9: 'common'
            {
            match("common"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__47"

    // $ANTLR start "T__48"
    public final void mT__48() throws RecognitionException {
        try {
            int _type = T__48;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:47:7: ( 'appending' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:47:9: 'appending'
            {
            match("appending"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__48"

    // $ANTLR start "T__49"
    public final void mT__49() throws RecognitionException {
        try {
            int _type = T__49;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:48:7: ( 'extern_weak' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:48:9: 'extern_weak'
            {
            match("extern_weak"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__49"

    // $ANTLR start "T__50"
    public final void mT__50() throws RecognitionException {
        try {
            int _type = T__50;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:49:7: ( 'linkonce_odr' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:49:9: 'linkonce_odr'
            {
            match("linkonce_odr"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__50"

    // $ANTLR start "T__51"
    public final void mT__51() throws RecognitionException {
        try {
            int _type = T__51;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:50:7: ( 'weak_odr' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:50:9: 'weak_odr'
            {
            match("weak_odr"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__51"

    // $ANTLR start "T__52"
    public final void mT__52() throws RecognitionException {
        try {
            int _type = T__52;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:51:7: ( 'externally_visible' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:51:9: 'externally_visible'
            {
            match("externally_visible"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__52"

    // $ANTLR start "T__53"
    public final void mT__53() throws RecognitionException {
        try {
            int _type = T__53;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:52:7: ( 'dllimport' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:52:9: 'dllimport'
            {
            match("dllimport"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__53"

    // $ANTLR start "T__54"
    public final void mT__54() throws RecognitionException {
        try {
            int _type = T__54;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:53:7: ( 'dllexport' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:53:9: 'dllexport'
            {
            match("dllexport"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__54"

    // $ANTLR start "T__55"
    public final void mT__55() throws RecognitionException {
        try {
            int _type = T__55;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:54:7: ( 'zeroinitializer' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:54:9: 'zeroinitializer'
            {
            match("zeroinitializer"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__55"

    // $ANTLR start "T__56"
    public final void mT__56() throws RecognitionException {
        try {
            int _type = T__56;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:55:7: ( 'null' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:55:9: 'null'
            {
            match("null"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__56"

    // $ANTLR start "T__57"
    public final void mT__57() throws RecognitionException {
        try {
            int _type = T__57;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:56:7: ( 'undef' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:56:9: 'undef'
            {
            match("undef"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__57"

    // $ANTLR start "T__58"
    public final void mT__58() throws RecognitionException {
        try {
            int _type = T__58;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:57:7: ( 'to' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:57:9: 'to'
            {
            match("to"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__58"

    // $ANTLR start "T__59"
    public final void mT__59() throws RecognitionException {
        try {
            int _type = T__59;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:58:7: ( 'trunc' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:58:9: 'trunc'
            {
            match("trunc"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__59"

    // $ANTLR start "T__60"
    public final void mT__60() throws RecognitionException {
        try {
            int _type = T__60;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:59:7: ( 'zext' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:59:9: 'zext'
            {
            match("zext"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__60"

    // $ANTLR start "T__61"
    public final void mT__61() throws RecognitionException {
        try {
            int _type = T__61;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:60:7: ( 'sext' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:60:9: 'sext'
            {
            match("sext"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__61"

    // $ANTLR start "T__62"
    public final void mT__62() throws RecognitionException {
        try {
            int _type = T__62;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:61:7: ( 'fptrunc' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:61:9: 'fptrunc'
            {
            match("fptrunc"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__62"

    // $ANTLR start "T__63"
    public final void mT__63() throws RecognitionException {
        try {
            int _type = T__63;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:62:7: ( 'fpext' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:62:9: 'fpext'
            {
            match("fpext"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__63"

    // $ANTLR start "T__64"
    public final void mT__64() throws RecognitionException {
        try {
            int _type = T__64;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:63:7: ( 'fptoui' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:63:9: 'fptoui'
            {
            match("fptoui"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__64"

    // $ANTLR start "T__65"
    public final void mT__65() throws RecognitionException {
        try {
            int _type = T__65;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:64:7: ( 'fptosi' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:64:9: 'fptosi'
            {
            match("fptosi"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__65"

    // $ANTLR start "T__66"
    public final void mT__66() throws RecognitionException {
        try {
            int _type = T__66;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:65:7: ( 'uitofp' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:65:9: 'uitofp'
            {
            match("uitofp"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__66"

    // $ANTLR start "T__67"
    public final void mT__67() throws RecognitionException {
        try {
            int _type = T__67;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:66:7: ( 'sitofp' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:66:9: 'sitofp'
            {
            match("sitofp"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__67"

    // $ANTLR start "T__68"
    public final void mT__68() throws RecognitionException {
        try {
            int _type = T__68;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:67:7: ( 'ptrtoint' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:67:9: 'ptrtoint'
            {
            match("ptrtoint"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__68"

    // $ANTLR start "T__69"
    public final void mT__69() throws RecognitionException {
        try {
            int _type = T__69;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:68:7: ( 'inttoptr' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:68:9: 'inttoptr'
            {
            match("inttoptr"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__69"

    // $ANTLR start "T__70"
    public final void mT__70() throws RecognitionException {
        try {
            int _type = T__70;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:69:7: ( 'bitcast' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:69:9: 'bitcast'
            {
            match("bitcast"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__70"

    // $ANTLR start "T__71"
    public final void mT__71() throws RecognitionException {
        try {
            int _type = T__71;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:70:7: ( 'default' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:70:9: 'default'
            {
            match("default"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__71"

    // $ANTLR start "T__72"
    public final void mT__72() throws RecognitionException {
        try {
            int _type = T__72;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:71:7: ( 'hidden' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:71:9: 'hidden'
            {
            match("hidden"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__72"

    // $ANTLR start "T__73"
    public final void mT__73() throws RecognitionException {
        try {
            int _type = T__73;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:72:7: ( 'protected' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:72:9: 'protected'
            {
            match("protected"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__73"

    // $ANTLR start "T__74"
    public final void mT__74() throws RecognitionException {
        try {
            int _type = T__74;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:73:7: ( 'ccc' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:73:9: 'ccc'
            {
            match("ccc"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__74"

    // $ANTLR start "T__75"
    public final void mT__75() throws RecognitionException {
        try {
            int _type = T__75;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:74:7: ( 'fastcc' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:74:9: 'fastcc'
            {
            match("fastcc"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__75"

    // $ANTLR start "T__76"
    public final void mT__76() throws RecognitionException {
        try {
            int _type = T__76;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:75:7: ( 'coldcc' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:75:9: 'coldcc'
            {
            match("coldcc"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__76"

    // $ANTLR start "T__77"
    public final void mT__77() throws RecognitionException {
        try {
            int _type = T__77;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:76:7: ( 'cc 10' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:76:9: 'cc 10'
            {
            match("cc 10"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__77"

    // $ANTLR start "T__78"
    public final void mT__78() throws RecognitionException {
        try {
            int _type = T__78;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:77:7: ( 'cc' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:77:9: 'cc'
            {
            match("cc"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__78"

    // $ANTLR start "T__79"
    public final void mT__79() throws RecognitionException {
        try {
            int _type = T__79;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:78:7: ( 'zeroext' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:78:9: 'zeroext'
            {
            match("zeroext"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__79"

    // $ANTLR start "T__80"
    public final void mT__80() throws RecognitionException {
        try {
            int _type = T__80;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:79:7: ( 'signext' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:79:9: 'signext'
            {
            match("signext"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__80"

    // $ANTLR start "T__81"
    public final void mT__81() throws RecognitionException {
        try {
            int _type = T__81;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:80:7: ( 'inreg' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:80:9: 'inreg'
            {
            match("inreg"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__81"

    // $ANTLR start "T__82"
    public final void mT__82() throws RecognitionException {
        try {
            int _type = T__82;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:81:7: ( 'byval' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:81:9: 'byval'
            {
            match("byval"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__82"

    // $ANTLR start "T__83"
    public final void mT__83() throws RecognitionException {
        try {
            int _type = T__83;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:82:7: ( 'sret' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:82:9: 'sret'
            {
            match("sret"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__83"

    // $ANTLR start "T__84"
    public final void mT__84() throws RecognitionException {
        try {
            int _type = T__84;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:83:7: ( 'noalias' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:83:9: 'noalias'
            {
            match("noalias"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__84"

    // $ANTLR start "T__85"
    public final void mT__85() throws RecognitionException {
        try {
            int _type = T__85;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:84:7: ( 'nocapture' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:84:9: 'nocapture'
            {
            match("nocapture"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__85"

    // $ANTLR start "T__86"
    public final void mT__86() throws RecognitionException {
        try {
            int _type = T__86;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:85:7: ( 'nest' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:85:9: 'nest'
            {
            match("nest"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__86"

    // $ANTLR start "T__87"
    public final void mT__87() throws RecognitionException {
        try {
            int _type = T__87;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:86:7: ( 'alignstack' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:86:9: 'alignstack'
            {
            match("alignstack"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__87"

    // $ANTLR start "T__88"
    public final void mT__88() throws RecognitionException {
        try {
            int _type = T__88;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:87:7: ( 'alwaysinline' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:87:9: 'alwaysinline'
            {
            match("alwaysinline"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__88"

    // $ANTLR start "T__89"
    public final void mT__89() throws RecognitionException {
        try {
            int _type = T__89;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:88:7: ( 'inlinehint' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:88:9: 'inlinehint'
            {
            match("inlinehint"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__89"

    // $ANTLR start "T__90"
    public final void mT__90() throws RecognitionException {
        try {
            int _type = T__90;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:89:7: ( 'noinline' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:89:9: 'noinline'
            {
            match("noinline"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__90"

    // $ANTLR start "T__91"
    public final void mT__91() throws RecognitionException {
        try {
            int _type = T__91;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:90:7: ( 'optsize' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:90:9: 'optsize'
            {
            match("optsize"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__91"

    // $ANTLR start "T__92"
    public final void mT__92() throws RecognitionException {
        try {
            int _type = T__92;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:91:7: ( 'noreturn' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:91:9: 'noreturn'
            {
            match("noreturn"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__92"

    // $ANTLR start "T__93"
    public final void mT__93() throws RecognitionException {
        try {
            int _type = T__93;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:92:7: ( 'nounwind' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:92:9: 'nounwind'
            {
            match("nounwind"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__93"

    // $ANTLR start "T__94"
    public final void mT__94() throws RecognitionException {
        try {
            int _type = T__94;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:93:7: ( 'readnone' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:93:9: 'readnone'
            {
            match("readnone"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__94"

    // $ANTLR start "T__95"
    public final void mT__95() throws RecognitionException {
        try {
            int _type = T__95;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:94:7: ( 'readonly' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:94:9: 'readonly'
            {
            match("readonly"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__95"

    // $ANTLR start "T__96"
    public final void mT__96() throws RecognitionException {
        try {
            int _type = T__96;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:95:7: ( 'ssp' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:95:9: 'ssp'
            {
            match("ssp"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__96"

    // $ANTLR start "T__97"
    public final void mT__97() throws RecognitionException {
        try {
            int _type = T__97;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:96:7: ( 'sspreq' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:96:9: 'sspreq'
            {
            match("sspreq"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__97"

    // $ANTLR start "T__98"
    public final void mT__98() throws RecognitionException {
        try {
            int _type = T__98;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:97:7: ( 'noredzone' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:97:9: 'noredzone'
            {
            match("noredzone"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__98"

    // $ANTLR start "T__99"
    public final void mT__99() throws RecognitionException {
        try {
            int _type = T__99;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:98:7: ( 'noimplicitfloat' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:98:9: 'noimplicitfloat'
            {
            match("noimplicitfloat"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__99"

    // $ANTLR start "T__100"
    public final void mT__100() throws RecognitionException {
        try {
            int _type = T__100;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:99:8: ( 'naked' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:99:10: 'naked'
            {
            match("naked"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__100"

    // $ANTLR start "T__101"
    public final void mT__101() throws RecognitionException {
        try {
            int _type = T__101;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:100:8: ( ':' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:100:10: ':'
            {
            match(':'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__101"

    // $ANTLR start "T__102"
    public final void mT__102() throws RecognitionException {
        try {
            int _type = T__102;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:101:8: ( 'align' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:101:10: 'align'
            {
            match("align"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__102"

    // $ANTLR start "T__103"
    public final void mT__103() throws RecognitionException {
        try {
            int _type = T__103;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:102:8: ( 'alloca' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:102:10: 'alloca'
            {
            match("alloca"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__103"

    // $ANTLR start "T__104"
    public final void mT__104() throws RecognitionException {
        try {
            int _type = T__104;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:103:8: ( 'phi' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:103:10: 'phi'
            {
            match("phi"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__104"

    // $ANTLR start "T__105"
    public final void mT__105() throws RecognitionException {
        try {
            int _type = T__105;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:104:8: ( 'store' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:104:10: 'store'
            {
            match("store"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__105"

    // $ANTLR start "T__106"
    public final void mT__106() throws RecognitionException {
        try {
            int _type = T__106;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:105:8: ( 'ret' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:105:10: 'ret'
            {
            match("ret"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__106"

    // $ANTLR start "T__107"
    public final void mT__107() throws RecognitionException {
        try {
            int _type = T__107;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:106:8: ( 'load' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:106:10: 'load'
            {
            match("load"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__107"

    // $ANTLR start "T__108"
    public final void mT__108() throws RecognitionException {
        try {
            int _type = T__108;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:107:8: ( 'icmp' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:107:10: 'icmp'
            {
            match("icmp"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__108"

    // $ANTLR start "T__109"
    public final void mT__109() throws RecognitionException {
        try {
            int _type = T__109;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:108:8: ( 'fcmp' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:108:10: 'fcmp'
            {
            match("fcmp"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__109"

    // $ANTLR start "T__110"
    public final void mT__110() throws RecognitionException {
        try {
            int _type = T__110;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:109:8: ( 'getelementptr' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:109:10: 'getelementptr'
            {
            match("getelementptr"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__110"

    // $ANTLR start "T__111"
    public final void mT__111() throws RecognitionException {
        try {
            int _type = T__111;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:110:8: ( 'insertvalue' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:110:10: 'insertvalue'
            {
            match("insertvalue"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__111"

    // $ANTLR start "T__112"
    public final void mT__112() throws RecognitionException {
        try {
            int _type = T__112;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:111:8: ( 'extractvalue' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:111:10: 'extractvalue'
            {
            match("extractvalue"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__112"

    // $ANTLR start "T__113"
    public final void mT__113() throws RecognitionException {
        try {
            int _type = T__113;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:112:8: ( 'tail' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:112:10: 'tail'
            {
            match("tail"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__113"

    // $ANTLR start "T__114"
    public final void mT__114() throws RecognitionException {
        try {
            int _type = T__114;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:113:8: ( 'call' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:113:10: 'call'
            {
            match("call"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__114"

    // $ANTLR start "T__115"
    public final void mT__115() throws RecognitionException {
        try {
            int _type = T__115;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:114:8: ( 'add' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:114:10: 'add'
            {
            match("add"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__115"

    // $ANTLR start "T__116"
    public final void mT__116() throws RecognitionException {
        try {
            int _type = T__116;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:115:8: ( 'fadd' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:115:10: 'fadd'
            {
            match("fadd"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__116"

    // $ANTLR start "T__117"
    public final void mT__117() throws RecognitionException {
        try {
            int _type = T__117;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:116:8: ( 'sub' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:116:10: 'sub'
            {
            match("sub"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__117"

    // $ANTLR start "T__118"
    public final void mT__118() throws RecognitionException {
        try {
            int _type = T__118;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:117:8: ( 'fsub' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:117:10: 'fsub'
            {
            match("fsub"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__118"

    // $ANTLR start "T__119"
    public final void mT__119() throws RecognitionException {
        try {
            int _type = T__119;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:118:8: ( 'mul' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:118:10: 'mul'
            {
            match("mul"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__119"

    // $ANTLR start "T__120"
    public final void mT__120() throws RecognitionException {
        try {
            int _type = T__120;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:119:8: ( 'fmul' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:119:10: 'fmul'
            {
            match("fmul"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__120"

    // $ANTLR start "T__121"
    public final void mT__121() throws RecognitionException {
        try {
            int _type = T__121;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:120:8: ( 'udiv' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:120:10: 'udiv'
            {
            match("udiv"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__121"

    // $ANTLR start "T__122"
    public final void mT__122() throws RecognitionException {
        try {
            int _type = T__122;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:121:8: ( 'sdiv' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:121:10: 'sdiv'
            {
            match("sdiv"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__122"

    // $ANTLR start "T__123"
    public final void mT__123() throws RecognitionException {
        try {
            int _type = T__123;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:122:8: ( 'fdiv' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:122:10: 'fdiv'
            {
            match("fdiv"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__123"

    // $ANTLR start "T__124"
    public final void mT__124() throws RecognitionException {
        try {
            int _type = T__124;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:123:8: ( 'urem' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:123:10: 'urem'
            {
            match("urem"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__124"

    // $ANTLR start "T__125"
    public final void mT__125() throws RecognitionException {
        try {
            int _type = T__125;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:124:8: ( 'srem' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:124:10: 'srem'
            {
            match("srem"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__125"

    // $ANTLR start "T__126"
    public final void mT__126() throws RecognitionException {
        try {
            int _type = T__126;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:125:8: ( 'frem' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:125:10: 'frem'
            {
            match("frem"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__126"

    // $ANTLR start "T__127"
    public final void mT__127() throws RecognitionException {
        try {
            int _type = T__127;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:126:8: ( 'shl' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:126:10: 'shl'
            {
            match("shl"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__127"

    // $ANTLR start "T__128"
    public final void mT__128() throws RecognitionException {
        try {
            int _type = T__128;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:127:8: ( 'lshr' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:127:10: 'lshr'
            {
            match("lshr"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__128"

    // $ANTLR start "T__129"
    public final void mT__129() throws RecognitionException {
        try {
            int _type = T__129;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:128:8: ( 'ashr' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:128:10: 'ashr'
            {
            match("ashr"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__129"

    // $ANTLR start "T__130"
    public final void mT__130() throws RecognitionException {
        try {
            int _type = T__130;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:129:8: ( 'and' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:129:10: 'and'
            {
            match("and"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__130"

    // $ANTLR start "T__131"
    public final void mT__131() throws RecognitionException {
        try {
            int _type = T__131;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:130:8: ( 'or' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:130:10: 'or'
            {
            match("or"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__131"

    // $ANTLR start "T__132"
    public final void mT__132() throws RecognitionException {
        try {
            int _type = T__132;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:131:8: ( 'xor' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:131:10: 'xor'
            {
            match("xor"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__132"

    // $ANTLR start "T__133"
    public final void mT__133() throws RecognitionException {
        try {
            int _type = T__133;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:132:8: ( 'nuw' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:132:10: 'nuw'
            {
            match("nuw"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__133"

    // $ANTLR start "T__134"
    public final void mT__134() throws RecognitionException {
        try {
            int _type = T__134;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:133:8: ( 'nsw' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:133:10: 'nsw'
            {
            match("nsw"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__134"

    // $ANTLR start "T__135"
    public final void mT__135() throws RecognitionException {
        try {
            int _type = T__135;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:134:8: ( 'exact' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:134:10: 'exact'
            {
            match("exact"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__135"

    // $ANTLR start "T__136"
    public final void mT__136() throws RecognitionException {
        try {
            int _type = T__136;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:135:8: ( 'eq' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:135:10: 'eq'
            {
            match("eq"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__136"

    // $ANTLR start "T__137"
    public final void mT__137() throws RecognitionException {
        try {
            int _type = T__137;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:136:8: ( 'ne' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:136:10: 'ne'
            {
            match("ne"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__137"

    // $ANTLR start "T__138"
    public final void mT__138() throws RecognitionException {
        try {
            int _type = T__138;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:137:8: ( 'ugt' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:137:10: 'ugt'
            {
            match("ugt"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__138"

    // $ANTLR start "T__139"
    public final void mT__139() throws RecognitionException {
        try {
            int _type = T__139;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:138:8: ( 'uge' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:138:10: 'uge'
            {
            match("uge"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__139"

    // $ANTLR start "T__140"
    public final void mT__140() throws RecognitionException {
        try {
            int _type = T__140;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:139:8: ( 'ult' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:139:10: 'ult'
            {
            match("ult"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__140"

    // $ANTLR start "T__141"
    public final void mT__141() throws RecognitionException {
        try {
            int _type = T__141;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:140:8: ( 'ule' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:140:10: 'ule'
            {
            match("ule"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__141"

    // $ANTLR start "T__142"
    public final void mT__142() throws RecognitionException {
        try {
            int _type = T__142;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:141:8: ( 'sgt' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:141:10: 'sgt'
            {
            match("sgt"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__142"

    // $ANTLR start "T__143"
    public final void mT__143() throws RecognitionException {
        try {
            int _type = T__143;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:142:8: ( 'sge' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:142:10: 'sge'
            {
            match("sge"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__143"

    // $ANTLR start "T__144"
    public final void mT__144() throws RecognitionException {
        try {
            int _type = T__144;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:143:8: ( 'slt' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:143:10: 'slt'
            {
            match("slt"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__144"

    // $ANTLR start "T__145"
    public final void mT__145() throws RecognitionException {
        try {
            int _type = T__145;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:144:8: ( 'sle' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:144:10: 'sle'
            {
            match("sle"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__145"

    // $ANTLR start "T__146"
    public final void mT__146() throws RecognitionException {
        try {
            int _type = T__146;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:145:8: ( 'br' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:145:10: 'br'
            {
            match("br"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__146"

    // $ANTLR start "EQUALS"
    public final void mEQUALS() throws RecognitionException {
        try {
            int _type = EQUALS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:594:8: ( '=' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:594:10: '='
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

    // $ANTLR start "INT_TYPE"
    public final void mINT_TYPE() throws RecognitionException {
        try {
            int _type = INT_TYPE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:596:10: ( 'i' ( '0' .. '9' )+ )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:596:12: 'i' ( '0' .. '9' )+
            {
            match('i'); 
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:596:16: ( '0' .. '9' )+
            int cnt1=0;
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( ((LA1_0>='0' && LA1_0<='9')) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:596:17: '0' .. '9'
            	    {
            	    matchRange('0','9'); 

            	    }
            	    break;

            	default :
            	    if ( cnt1 >= 1 ) break loop1;
                        EarlyExitException eee =
                            new EarlyExitException(1, input);
                        throw eee;
                }
                cnt1++;
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "INT_TYPE"

    // $ANTLR start "NUMBER"
    public final void mNUMBER() throws RecognitionException {
        try {
            int _type = NUMBER;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:601:8: ( ( '-' )? '0' .. '9' ( NUMSUFFIX ( '.' NUMSUFFIX )? ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:601:10: ( '-' )? '0' .. '9' ( NUMSUFFIX ( '.' NUMSUFFIX )? )
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:601:10: ( '-' )?
            int alt2=2;
            int LA2_0 = input.LA(1);

            if ( (LA2_0=='-') ) {
                alt2=1;
            }
            switch (alt2) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:601:10: '-'
                    {
                    match('-'); 

                    }
                    break;

            }

            matchRange('0','9'); 
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:601:24: ( NUMSUFFIX ( '.' NUMSUFFIX )? )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:601:25: NUMSUFFIX ( '.' NUMSUFFIX )?
            {
            mNUMSUFFIX(); 
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:601:35: ( '.' NUMSUFFIX )?
            int alt3=2;
            int LA3_0 = input.LA(1);

            if ( (LA3_0=='.') ) {
                alt3=1;
            }
            switch (alt3) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:601:37: '.' NUMSUFFIX
                    {
                    match('.'); 
                    mNUMSUFFIX(); 

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

    // $ANTLR start "UNNAMED_ID"
    public final void mUNNAMED_ID() throws RecognitionException {
        String theId = null;

        try {
            int _type = UNNAMED_ID;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            CommonToken SYM_PFX1=null;
            CommonToken NUMBER_SUFFIX2=null;

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:606:35: ( SYM_PFX NUMBER_SUFFIX )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:606:37: SYM_PFX NUMBER_SUFFIX
            {
            int SYM_PFX1Start1092 = getCharIndex();
            mSYM_PFX(); 
            SYM_PFX1 = new CommonToken(input, Token.INVALID_TOKEN_TYPE, Token.DEFAULT_CHANNEL, SYM_PFX1Start1092, getCharIndex()-1);
            int NUMBER_SUFFIX2Start1094 = getCharIndex();
            mNUMBER_SUFFIX(); 
            NUMBER_SUFFIX2 = new CommonToken(input, Token.INVALID_TOKEN_TYPE, Token.DEFAULT_CHANNEL, NUMBER_SUFFIX2Start1094, getCharIndex()-1);
             theId = (SYM_PFX1!=null?SYM_PFX1.getText():null) + (NUMBER_SUFFIX2!=null?NUMBER_SUFFIX2.getText():null); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "UNNAMED_ID"

    // $ANTLR start "NAMED_ID"
    public final void mNAMED_ID() throws RecognitionException {
        String theId = null;

        try {
            int _type = NAMED_ID;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            CommonToken SYM_PFX3=null;
            CommonToken NAME_SUFFIX4=null;

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:607:33: ( SYM_PFX NAME_SUFFIX )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:607:35: SYM_PFX NAME_SUFFIX
            {
            int SYM_PFX3Start1108 = getCharIndex();
            mSYM_PFX(); 
            SYM_PFX3 = new CommonToken(input, Token.INVALID_TOKEN_TYPE, Token.DEFAULT_CHANNEL, SYM_PFX3Start1108, getCharIndex()-1);
            int NAME_SUFFIX4Start1110 = getCharIndex();
            mNAME_SUFFIX(); 
            NAME_SUFFIX4 = new CommonToken(input, Token.INVALID_TOKEN_TYPE, Token.DEFAULT_CHANNEL, NAME_SUFFIX4Start1110, getCharIndex()-1);
             theId = (SYM_PFX3!=null?SYM_PFX3.getText():null) + (NAME_SUFFIX4!=null?NAME_SUFFIX4.getText():null); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "NAMED_ID"

    // $ANTLR start "QUOTED_ID"
    public final void mQUOTED_ID() throws RecognitionException {
        String theId = null;

        try {
            int _type = QUOTED_ID;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            CommonToken SYM_PFX5=null;
            CommonToken STRING_LITERAL6=null;

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:608:34: ( SYM_PFX STRING_LITERAL )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:608:36: SYM_PFX STRING_LITERAL
            {
            int SYM_PFX5Start1125 = getCharIndex();
            mSYM_PFX(); 
            SYM_PFX5 = new CommonToken(input, Token.INVALID_TOKEN_TYPE, Token.DEFAULT_CHANNEL, SYM_PFX5Start1125, getCharIndex()-1);
            int STRING_LITERAL6Start1127 = getCharIndex();
            mSTRING_LITERAL(); 
            STRING_LITERAL6 = new CommonToken(input, Token.INVALID_TOKEN_TYPE, Token.DEFAULT_CHANNEL, STRING_LITERAL6Start1127, getCharIndex()-1);
             theId = (SYM_PFX5!=null?SYM_PFX5.getText():null) + LLParserHelper.unescape((STRING_LITERAL6!=null?STRING_LITERAL6.getText():null), '"'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "QUOTED_ID"

    // $ANTLR start "SYM_PFX"
    public final void mSYM_PFX() throws RecognitionException {
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:611:9: ( '%' | '@' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:
            {
            if ( input.LA(1)=='%'||input.LA(1)=='@' ) {
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
    // $ANTLR end "SYM_PFX"

    // $ANTLR start "DEFINE"
    public final void mDEFINE() throws RecognitionException {
        try {
            int _type = DEFINE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:614:8: ( 'define' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:614:10: 'define'
            {
            match("define"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "DEFINE"

    // $ANTLR start "LABEL"
    public final void mLABEL() throws RecognitionException {
        try {
            int _type = LABEL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:616:7: ( NAME_SUFFIX )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:616:9: NAME_SUFFIX
            {
            mNAME_SUFFIX(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LABEL"

    // $ANTLR start "NAME_SUFFIX"
    public final void mNAME_SUFFIX() throws RecognitionException {
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:618:22: ( ( 'a' .. 'z' | 'A' .. 'Z' | '$' | '.' | '_' ) ( 'a' .. 'z' | 'A' .. 'Z' | '$' | '.' | '0' .. '9' | '_' )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:618:24: ( 'a' .. 'z' | 'A' .. 'Z' | '$' | '.' | '_' ) ( 'a' .. 'z' | 'A' .. 'Z' | '$' | '.' | '0' .. '9' | '_' )*
            {
            if ( input.LA(1)=='$'||input.LA(1)=='.'||(input.LA(1)>='A' && input.LA(1)<='Z')||input.LA(1)=='_'||(input.LA(1)>='a' && input.LA(1)<='z') ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:618:66: ( 'a' .. 'z' | 'A' .. 'Z' | '$' | '.' | '0' .. '9' | '_' )*
            loop4:
            do {
                int alt4=2;
                int LA4_0 = input.LA(1);

                if ( (LA4_0=='$'||LA4_0=='.'||(LA4_0>='0' && LA4_0<='9')||(LA4_0>='A' && LA4_0<='Z')||LA4_0=='_'||(LA4_0>='a' && LA4_0<='z')) ) {
                    alt4=1;
                }


                switch (alt4) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:
            	    {
            	    if ( input.LA(1)=='$'||input.LA(1)=='.'||(input.LA(1)>='0' && input.LA(1)<='9')||(input.LA(1)>='A' && input.LA(1)<='Z')||input.LA(1)=='_'||(input.LA(1)>='a' && input.LA(1)<='z') ) {
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


            }

        }
        finally {
        }
    }
    // $ANTLR end "NAME_SUFFIX"

    // $ANTLR start "NUMBER_SUFFIX"
    public final void mNUMBER_SUFFIX() throws RecognitionException {
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:619:24: ( ( '0' .. '9' )+ )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:619:26: ( '0' .. '9' )+
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:619:26: ( '0' .. '9' )+
            int cnt5=0;
            loop5:
            do {
                int alt5=2;
                int LA5_0 = input.LA(1);

                if ( ((LA5_0>='0' && LA5_0<='9')) ) {
                    alt5=1;
                }


                switch (alt5) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:619:27: '0' .. '9'
            	    {
            	    matchRange('0','9'); 

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


            }

        }
        finally {
        }
    }
    // $ANTLR end "NUMBER_SUFFIX"

    // $ANTLR start "NUMSUFFIX"
    public final void mNUMSUFFIX() throws RecognitionException {
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:620:20: ( ( '0' .. '9' | 'A' .. 'Z' | 'a' .. 'z' )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:620:22: ( '0' .. '9' | 'A' .. 'Z' | 'a' .. 'z' )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:620:22: ( '0' .. '9' | 'A' .. 'Z' | 'a' .. 'z' )*
            loop6:
            do {
                int alt6=2;
                int LA6_0 = input.LA(1);

                if ( ((LA6_0>='0' && LA6_0<='9')||(LA6_0>='A' && LA6_0<='Z')||(LA6_0>='a' && LA6_0<='z')) ) {
                    alt6=1;
                }


                switch (alt6) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:
            	    {
            	    if ( (input.LA(1)>='0' && input.LA(1)<='9')||(input.LA(1)>='A' && input.LA(1)<='Z')||(input.LA(1)>='a' && input.LA(1)<='z') ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    break loop6;
                }
            } while (true);


            }

        }
        finally {
        }
    }
    // $ANTLR end "NUMSUFFIX"

    // $ANTLR start "CHAR_LITERAL"
    public final void mCHAR_LITERAL() throws RecognitionException {
        try {
            int _type = CHAR_LITERAL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:627:14: ( '\\'' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:627:16: '\\''
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:644:16: ( '\"' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:644:18: '\"'
            {
            match('\"'); 

              while (true) {
            		 int ch = input.LA(1);
            		 if (ch == '\\') {
            		    input.consume();	// backslash
            		    input.consume();	// escaped
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

    // $ANTLR start "CSTRING_LITERAL"
    public final void mCSTRING_LITERAL() throws RecognitionException {
        try {
            int _type = CSTRING_LITERAL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:661:17: ( 'c\"' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:661:19: 'c\"'
            {
            match("c\""); 


              while (true) {
                 int ch = input.LA(1);
                 if (ch == '\\') {
                    input.consume();  // backslash
                    input.consume();  // escaped
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
    // $ANTLR end "CSTRING_LITERAL"

    // $ANTLR start "NEWLINE"
    public final void mNEWLINE() throws RecognitionException {
        try {
            int _type = NEWLINE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:681:8: ( ( ( '\\r' )? '\\n' ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:681:10: ( ( '\\r' )? '\\n' )
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:681:10: ( ( '\\r' )? '\\n' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:681:11: ( '\\r' )? '\\n'
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:681:11: ( '\\r' )?
            int alt7=2;
            int LA7_0 = input.LA(1);

            if ( (LA7_0=='\r') ) {
                alt7=1;
            }
            switch (alt7) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:681:11: '\\r'
                    {
                    match('\r'); 

                    }
                    break;

            }

            match('\n'); 

            }


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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:682:5: ( ( ' ' | '\\t' )+ )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:682:9: ( ' ' | '\\t' )+
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:682:9: ( ' ' | '\\t' )+
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:686:15: ( ';' (~ ( '\\r' | '\\n' ) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:686:17: ';' (~ ( '\\r' | '\\n' ) )*
            {
            match(';'); 
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:686:21: (~ ( '\\r' | '\\n' ) )*
            loop9:
            do {
                int alt9=2;
                int LA9_0 = input.LA(1);

                if ( ((LA9_0>='\u0000' && LA9_0<='\t')||(LA9_0>='\u000B' && LA9_0<='\f')||(LA9_0>='\u000E' && LA9_0<='\uFFFF')) ) {
                    alt9=1;
                }


                switch (alt9) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:686:21: ~ ( '\\r' | '\\n' )
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:691:3: ( '/*' ( . )* '*/' ( NEWLINE )? )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:691:5: '/*' ( . )* '*/' ( NEWLINE )?
            {
            match("/*"); 

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:691:10: ( . )*
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:691:10: .
            	    {
            	    matchAny(); 

            	    }
            	    break;

            	default :
            	    break loop10;
                }
            } while (true);

            match("*/"); 

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:691:18: ( NEWLINE )?
            int alt11=2;
            int LA11_0 = input.LA(1);

            if ( (LA11_0=='\n'||LA11_0=='\r') ) {
                alt11=1;
            }
            switch (alt11) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:691:18: NEWLINE
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
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:8: ( T__23 | T__24 | T__25 | T__26 | T__27 | T__28 | T__29 | T__30 | T__31 | T__32 | T__33 | T__34 | T__35 | T__36 | T__37 | T__38 | T__39 | T__40 | T__41 | T__42 | T__43 | T__44 | T__45 | T__46 | T__47 | T__48 | T__49 | T__50 | T__51 | T__52 | T__53 | T__54 | T__55 | T__56 | T__57 | T__58 | T__59 | T__60 | T__61 | T__62 | T__63 | T__64 | T__65 | T__66 | T__67 | T__68 | T__69 | T__70 | T__71 | T__72 | T__73 | T__74 | T__75 | T__76 | T__77 | T__78 | T__79 | T__80 | T__81 | T__82 | T__83 | T__84 | T__85 | T__86 | T__87 | T__88 | T__89 | T__90 | T__91 | T__92 | T__93 | T__94 | T__95 | T__96 | T__97 | T__98 | T__99 | T__100 | T__101 | T__102 | T__103 | T__104 | T__105 | T__106 | T__107 | T__108 | T__109 | T__110 | T__111 | T__112 | T__113 | T__114 | T__115 | T__116 | T__117 | T__118 | T__119 | T__120 | T__121 | T__122 | T__123 | T__124 | T__125 | T__126 | T__127 | T__128 | T__129 | T__130 | T__131 | T__132 | T__133 | T__134 | T__135 | T__136 | T__137 | T__138 | T__139 | T__140 | T__141 | T__142 | T__143 | T__144 | T__145 | T__146 | EQUALS | INT_TYPE | NUMBER | UNNAMED_ID | NAMED_ID | QUOTED_ID | DEFINE | LABEL | CHAR_LITERAL | STRING_LITERAL | CSTRING_LITERAL | NEWLINE | WS | SINGLE_COMMENT | MULTI_COMMENT )
        int alt12=139;
        alt12 = dfa12.predict(input);
        switch (alt12) {
            case 1 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:10: T__23
                {
                mT__23(); 

                }
                break;
            case 2 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:16: T__24
                {
                mT__24(); 

                }
                break;
            case 3 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:22: T__25
                {
                mT__25(); 

                }
                break;
            case 4 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:28: T__26
                {
                mT__26(); 

                }
                break;
            case 5 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:34: T__27
                {
                mT__27(); 

                }
                break;
            case 6 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:40: T__28
                {
                mT__28(); 

                }
                break;
            case 7 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:46: T__29
                {
                mT__29(); 

                }
                break;
            case 8 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:52: T__30
                {
                mT__30(); 

                }
                break;
            case 9 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:58: T__31
                {
                mT__31(); 

                }
                break;
            case 10 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:64: T__32
                {
                mT__32(); 

                }
                break;
            case 11 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:70: T__33
                {
                mT__33(); 

                }
                break;
            case 12 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:76: T__34
                {
                mT__34(); 

                }
                break;
            case 13 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:82: T__35
                {
                mT__35(); 

                }
                break;
            case 14 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:88: T__36
                {
                mT__36(); 

                }
                break;
            case 15 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:94: T__37
                {
                mT__37(); 

                }
                break;
            case 16 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:100: T__38
                {
                mT__38(); 

                }
                break;
            case 17 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:106: T__39
                {
                mT__39(); 

                }
                break;
            case 18 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:112: T__40
                {
                mT__40(); 

                }
                break;
            case 19 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:118: T__41
                {
                mT__41(); 

                }
                break;
            case 20 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:124: T__42
                {
                mT__42(); 

                }
                break;
            case 21 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:130: T__43
                {
                mT__43(); 

                }
                break;
            case 22 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:136: T__44
                {
                mT__44(); 

                }
                break;
            case 23 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:142: T__45
                {
                mT__45(); 

                }
                break;
            case 24 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:148: T__46
                {
                mT__46(); 

                }
                break;
            case 25 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:154: T__47
                {
                mT__47(); 

                }
                break;
            case 26 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:160: T__48
                {
                mT__48(); 

                }
                break;
            case 27 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:166: T__49
                {
                mT__49(); 

                }
                break;
            case 28 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:172: T__50
                {
                mT__50(); 

                }
                break;
            case 29 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:178: T__51
                {
                mT__51(); 

                }
                break;
            case 30 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:184: T__52
                {
                mT__52(); 

                }
                break;
            case 31 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:190: T__53
                {
                mT__53(); 

                }
                break;
            case 32 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:196: T__54
                {
                mT__54(); 

                }
                break;
            case 33 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:202: T__55
                {
                mT__55(); 

                }
                break;
            case 34 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:208: T__56
                {
                mT__56(); 

                }
                break;
            case 35 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:214: T__57
                {
                mT__57(); 

                }
                break;
            case 36 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:220: T__58
                {
                mT__58(); 

                }
                break;
            case 37 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:226: T__59
                {
                mT__59(); 

                }
                break;
            case 38 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:232: T__60
                {
                mT__60(); 

                }
                break;
            case 39 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:238: T__61
                {
                mT__61(); 

                }
                break;
            case 40 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:244: T__62
                {
                mT__62(); 

                }
                break;
            case 41 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:250: T__63
                {
                mT__63(); 

                }
                break;
            case 42 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:256: T__64
                {
                mT__64(); 

                }
                break;
            case 43 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:262: T__65
                {
                mT__65(); 

                }
                break;
            case 44 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:268: T__66
                {
                mT__66(); 

                }
                break;
            case 45 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:274: T__67
                {
                mT__67(); 

                }
                break;
            case 46 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:280: T__68
                {
                mT__68(); 

                }
                break;
            case 47 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:286: T__69
                {
                mT__69(); 

                }
                break;
            case 48 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:292: T__70
                {
                mT__70(); 

                }
                break;
            case 49 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:298: T__71
                {
                mT__71(); 

                }
                break;
            case 50 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:304: T__72
                {
                mT__72(); 

                }
                break;
            case 51 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:310: T__73
                {
                mT__73(); 

                }
                break;
            case 52 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:316: T__74
                {
                mT__74(); 

                }
                break;
            case 53 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:322: T__75
                {
                mT__75(); 

                }
                break;
            case 54 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:328: T__76
                {
                mT__76(); 

                }
                break;
            case 55 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:334: T__77
                {
                mT__77(); 

                }
                break;
            case 56 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:340: T__78
                {
                mT__78(); 

                }
                break;
            case 57 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:346: T__79
                {
                mT__79(); 

                }
                break;
            case 58 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:352: T__80
                {
                mT__80(); 

                }
                break;
            case 59 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:358: T__81
                {
                mT__81(); 

                }
                break;
            case 60 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:364: T__82
                {
                mT__82(); 

                }
                break;
            case 61 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:370: T__83
                {
                mT__83(); 

                }
                break;
            case 62 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:376: T__84
                {
                mT__84(); 

                }
                break;
            case 63 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:382: T__85
                {
                mT__85(); 

                }
                break;
            case 64 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:388: T__86
                {
                mT__86(); 

                }
                break;
            case 65 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:394: T__87
                {
                mT__87(); 

                }
                break;
            case 66 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:400: T__88
                {
                mT__88(); 

                }
                break;
            case 67 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:406: T__89
                {
                mT__89(); 

                }
                break;
            case 68 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:412: T__90
                {
                mT__90(); 

                }
                break;
            case 69 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:418: T__91
                {
                mT__91(); 

                }
                break;
            case 70 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:424: T__92
                {
                mT__92(); 

                }
                break;
            case 71 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:430: T__93
                {
                mT__93(); 

                }
                break;
            case 72 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:436: T__94
                {
                mT__94(); 

                }
                break;
            case 73 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:442: T__95
                {
                mT__95(); 

                }
                break;
            case 74 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:448: T__96
                {
                mT__96(); 

                }
                break;
            case 75 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:454: T__97
                {
                mT__97(); 

                }
                break;
            case 76 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:460: T__98
                {
                mT__98(); 

                }
                break;
            case 77 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:466: T__99
                {
                mT__99(); 

                }
                break;
            case 78 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:472: T__100
                {
                mT__100(); 

                }
                break;
            case 79 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:479: T__101
                {
                mT__101(); 

                }
                break;
            case 80 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:486: T__102
                {
                mT__102(); 

                }
                break;
            case 81 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:493: T__103
                {
                mT__103(); 

                }
                break;
            case 82 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:500: T__104
                {
                mT__104(); 

                }
                break;
            case 83 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:507: T__105
                {
                mT__105(); 

                }
                break;
            case 84 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:514: T__106
                {
                mT__106(); 

                }
                break;
            case 85 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:521: T__107
                {
                mT__107(); 

                }
                break;
            case 86 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:528: T__108
                {
                mT__108(); 

                }
                break;
            case 87 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:535: T__109
                {
                mT__109(); 

                }
                break;
            case 88 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:542: T__110
                {
                mT__110(); 

                }
                break;
            case 89 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:549: T__111
                {
                mT__111(); 

                }
                break;
            case 90 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:556: T__112
                {
                mT__112(); 

                }
                break;
            case 91 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:563: T__113
                {
                mT__113(); 

                }
                break;
            case 92 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:570: T__114
                {
                mT__114(); 

                }
                break;
            case 93 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:577: T__115
                {
                mT__115(); 

                }
                break;
            case 94 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:584: T__116
                {
                mT__116(); 

                }
                break;
            case 95 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:591: T__117
                {
                mT__117(); 

                }
                break;
            case 96 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:598: T__118
                {
                mT__118(); 

                }
                break;
            case 97 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:605: T__119
                {
                mT__119(); 

                }
                break;
            case 98 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:612: T__120
                {
                mT__120(); 

                }
                break;
            case 99 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:619: T__121
                {
                mT__121(); 

                }
                break;
            case 100 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:626: T__122
                {
                mT__122(); 

                }
                break;
            case 101 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:633: T__123
                {
                mT__123(); 

                }
                break;
            case 102 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:640: T__124
                {
                mT__124(); 

                }
                break;
            case 103 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:647: T__125
                {
                mT__125(); 

                }
                break;
            case 104 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:654: T__126
                {
                mT__126(); 

                }
                break;
            case 105 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:661: T__127
                {
                mT__127(); 

                }
                break;
            case 106 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:668: T__128
                {
                mT__128(); 

                }
                break;
            case 107 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:675: T__129
                {
                mT__129(); 

                }
                break;
            case 108 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:682: T__130
                {
                mT__130(); 

                }
                break;
            case 109 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:689: T__131
                {
                mT__131(); 

                }
                break;
            case 110 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:696: T__132
                {
                mT__132(); 

                }
                break;
            case 111 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:703: T__133
                {
                mT__133(); 

                }
                break;
            case 112 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:710: T__134
                {
                mT__134(); 

                }
                break;
            case 113 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:717: T__135
                {
                mT__135(); 

                }
                break;
            case 114 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:724: T__136
                {
                mT__136(); 

                }
                break;
            case 115 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:731: T__137
                {
                mT__137(); 

                }
                break;
            case 116 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:738: T__138
                {
                mT__138(); 

                }
                break;
            case 117 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:745: T__139
                {
                mT__139(); 

                }
                break;
            case 118 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:752: T__140
                {
                mT__140(); 

                }
                break;
            case 119 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:759: T__141
                {
                mT__141(); 

                }
                break;
            case 120 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:766: T__142
                {
                mT__142(); 

                }
                break;
            case 121 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:773: T__143
                {
                mT__143(); 

                }
                break;
            case 122 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:780: T__144
                {
                mT__144(); 

                }
                break;
            case 123 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:787: T__145
                {
                mT__145(); 

                }
                break;
            case 124 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:794: T__146
                {
                mT__146(); 

                }
                break;
            case 125 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:801: EQUALS
                {
                mEQUALS(); 

                }
                break;
            case 126 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:808: INT_TYPE
                {
                mINT_TYPE(); 

                }
                break;
            case 127 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:817: NUMBER
                {
                mNUMBER(); 

                }
                break;
            case 128 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:824: UNNAMED_ID
                {
                mUNNAMED_ID(); 

                }
                break;
            case 129 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:835: NAMED_ID
                {
                mNAMED_ID(); 

                }
                break;
            case 130 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:844: QUOTED_ID
                {
                mQUOTED_ID(); 

                }
                break;
            case 131 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:854: DEFINE
                {
                mDEFINE(); 

                }
                break;
            case 132 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:861: LABEL
                {
                mLABEL(); 

                }
                break;
            case 133 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:867: CHAR_LITERAL
                {
                mCHAR_LITERAL(); 

                }
                break;
            case 134 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:880: STRING_LITERAL
                {
                mSTRING_LITERAL(); 

                }
                break;
            case 135 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:895: CSTRING_LITERAL
                {
                mCSTRING_LITERAL(); 

                }
                break;
            case 136 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:911: NEWLINE
                {
                mNEWLINE(); 

                }
                break;
            case 137 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:919: WS
                {
                mWS(); 

                }
                break;
            case 138 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:922: SINGLE_COMMENT
                {
                mSINGLE_COMMENT(); 

                }
                break;
            case 139 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:937: MULTI_COMMENT
                {
                mMULTI_COMMENT(); 

                }
                break;

        }

    }


    protected DFA12 dfa12 = new DFA12(this);
    static final String DFA12_eotS =
        "\1\uffff\4\43\4\uffff\1\67\4\uffff\20\43\1\uffff\1\43\12\uffff\3"+
        "\43\1\172\11\43\1\uffff\3\43\1\u008b\1\43\1\uffff\13\43\1\u009e"+
        "\2\43\1\u00a2\3\43\1\u00ad\33\43\1\u00d0\2\43\1\u00d3\2\43\3\uffff"+
        "\5\43\1\uffff\10\43\1\u00e6\5\43\1\u00ec\2\uffff\1\43\1\u00ef\6"+
        "\43\1\u00f6\3\43\1\u00fa\5\43\1\uffff\3\43\1\uffff\3\43\1\u0108"+
        "\6\43\1\uffff\1\43\1\u0111\4\43\1\u0116\1\u0117\1\u0118\1\u0119"+
        "\4\43\1\u0120\1\43\1\u0122\1\43\1\u0124\1\u0125\1\u0126\1\u0127"+
        "\1\u0128\13\43\1\uffff\2\43\1\uffff\1\43\1\u0138\1\u0139\1\43\1"+
        "\u013b\2\43\1\u013e\5\43\1\u0144\2\43\1\u0148\1\u0149\1\uffff\5"+
        "\43\1\uffff\1\u014f\1\43\1\uffff\5\43\1\u0156\1\uffff\3\43\1\uffff"+
        "\5\43\1\u015f\1\u0161\4\43\1\u0167\1\u0168\1\uffff\6\43\1\u0170"+
        "\1\43\1\uffff\2\43\1\u0174\1\u0175\4\uffff\1\u0176\2\43\1\u0179"+
        "\1\u017a\1\43\1\uffff\1\43\1\uffff\1\u017d\5\uffff\4\43\1\u0183"+
        "\1\u0184\1\u0185\1\u0186\1\u0187\1\u0188\5\43\2\uffff\1\43\1\uffff"+
        "\1\43\1\u0191\1\uffff\5\43\1\uffff\1\u0197\2\43\2\uffff\5\43\1\uffff"+
        "\3\43\1\u01a3\2\43\1\uffff\5\43\1\u01ab\2\43\1\uffff\1\43\1\uffff"+
        "\2\43\1\u01b1\2\43\2\uffff\7\43\1\uffff\1\u01bb\1\u01bc\1\43\3\uffff"+
        "\2\43\2\uffff\1\43\1\u01c1\1\uffff\3\43\1\u01c5\1\43\6\uffff\1\43"+
        "\1\u01c8\4\43\1\u01cd\1\u01ce\1\uffff\4\43\1\u01d3\1\uffff\2\43"+
        "\1\u01d6\2\43\1\u01d9\1\u01da\4\43\1\uffff\1\43\1\u01e0\5\43\1\uffff"+
        "\5\43\1\uffff\11\43\2\uffff\1\u01f5\1\u01f6\1\43\1\u01f8\1\uffff"+
        "\1\43\1\u01fa\1\u01fb\1\uffff\1\u01fc\1\43\1\uffff\1\u01fe\3\43"+
        "\2\uffff\3\43\1\u0205\1\uffff\2\43\1\uffff\2\43\2\uffff\5\43\1\uffff"+
        "\1\u020f\13\43\1\u021b\1\u021c\6\43\2\uffff\1\u0223\1\uffff\1\u0224"+
        "\3\uffff\1\u0225\1\uffff\1\u0226\5\43\1\uffff\1\43\1\u022e\1\43"+
        "\1\u0230\5\43\1\uffff\1\43\1\u0237\1\u0238\1\u0239\2\43\1\u023c"+
        "\4\43\2\uffff\1\43\1\u0242\1\43\1\u0244\1\43\1\u0246\4\uffff\1\u0247"+
        "\1\u0248\1\43\1\u024a\1\u024b\2\43\1\uffff\1\43\1\uffff\1\u024f"+
        "\1\43\1\u0251\2\43\1\u0254\3\uffff\2\43\1\uffff\4\43\1\u025b\1\uffff"+
        "\1\43\1\uffff\1\u025d\3\uffff\1\u025e\2\uffff\3\43\1\uffff\1\43"+
        "\1\uffff\1\u0263\1\43\1\uffff\1\u0265\5\43\1\uffff\1\43\2\uffff"+
        "\4\43\1\uffff\1\43\1\uffff\1\u0271\1\u0272\5\43\1\u0278\2\43\1\u027b"+
        "\2\uffff\1\43\1\u027d\3\43\1\uffff\1\u0281\1\43\1\uffff\1\43\1\uffff"+
        "\2\43\1\u0286\1\uffff\4\43\1\uffff\2\43\1\u028d\1\u028e\2\43\2\uffff"+
        "\3\43\1\u0294\1\43\1\uffff\1\u0296\1\uffff";
    static final String DFA12_eofS =
        "\u0297\uffff";
    static final String DFA12_minS =
        "\1\11\2\141\1\157\1\141\4\uffff\1\44\4\uffff\1\145\1\42\1\144\1"+
        "\150\1\60\1\145\1\161\1\145\1\141\2\144\1\141\2\151\1\160\1\145"+
        "\1\uffff\1\165\2\uffff\1\42\7\uffff\2\151\1\160\1\44\1\164\1\154"+
        "\1\146\1\151\1\142\1\156\1\141\1\150\1\162\1\uffff\1\157\1\164\1"+
        "\154\1\40\1\154\1\uffff\1\144\1\141\1\160\1\151\1\150\1\144\1\151"+
        "\1\162\1\151\1\154\1\155\1\44\2\141\1\44\1\162\1\154\1\141\1\44"+
        "\1\153\1\167\1\144\1\164\1\151\3\145\1\170\1\147\1\145\1\160\1\157"+
        "\1\142\1\151\1\154\3\145\1\144\1\155\2\165\1\151\1\145\1\164\1\166"+
        "\1\44\1\144\1\164\1\44\1\141\1\154\3\uffff\1\147\1\154\1\160\1\156"+
        "\1\145\1\uffff\1\141\1\145\1\141\1\144\1\145\1\153\1\144\1\162\1"+
        "\44\1\142\1\145\1\163\1\155\1\144\1\44\2\uffff\1\154\1\44\1\151"+
        "\1\145\1\147\1\141\1\157\1\162\1\44\1\166\2\164\1\44\2\145\1\151"+
        "\1\145\1\160\1\uffff\1\153\1\145\1\143\1\uffff\1\157\1\164\1\154"+
        "\1\44\1\154\1\141\1\155\1\145\1\156\1\164\1\uffff\1\145\1\44\1\145"+
        "\1\157\1\166\1\155\4\44\1\164\1\157\1\156\1\155\1\44\1\162\1\44"+
        "\1\166\5\44\1\157\1\170\1\164\1\144\1\160\1\142\1\154\1\166\1\155"+
        "\1\143\1\141\1\uffff\1\144\1\163\1\uffff\1\144\2\44\1\145\1\44\1"+
        "\154\1\143\1\44\1\154\1\155\1\170\1\165\1\156\1\44\1\154\1\145\2"+
        "\44\1\uffff\1\141\1\154\1\164\1\157\1\143\1\uffff\1\44\1\163\1\uffff"+
        "\1\154\2\156\1\171\1\143\1\44\1\uffff\1\141\1\145\1\157\1\uffff"+
        "\1\162\1\157\1\147\1\156\1\162\2\44\1\162\1\141\1\164\1\145\2\44"+
        "\1\uffff\1\151\1\160\1\154\1\160\1\144\1\167\1\44\1\144\1\uffff"+
        "\2\146\2\44\4\uffff\1\44\1\146\1\145\2\44\1\145\1\uffff\1\145\1"+
        "\uffff\1\44\5\uffff\1\165\1\163\1\164\1\143\6\44\1\141\1\154\1\145"+
        "\1\151\1\156\2\uffff\1\164\1\uffff\1\145\1\44\1\uffff\1\141\2\160"+
        "\1\154\1\145\1\uffff\1\44\1\162\1\156\2\uffff\1\154\1\145\1\141"+
        "\1\156\1\143\1\uffff\1\160\1\141\1\144\1\44\1\163\1\141\1\uffff"+
        "\1\164\1\143\1\151\1\156\1\160\1\44\1\145\1\164\1\uffff\1\157\1"+
        "\uffff\1\156\1\143\1\44\1\156\1\170\2\uffff\1\141\1\164\1\151\1"+
        "\154\1\165\1\172\1\151\1\uffff\2\44\1\160\3\uffff\1\160\1\170\2"+
        "\uffff\1\161\1\44\1\uffff\1\156\2\151\1\44\1\143\6\uffff\1\163\1"+
        "\44\1\156\1\172\1\157\1\156\2\44\1\uffff\1\171\2\157\1\164\1\44"+
        "\1\uffff\1\137\1\143\1\44\1\155\1\156\2\44\1\141\1\142\1\151\1\164"+
        "\1\uffff\1\151\1\44\1\145\1\164\1\156\1\141\1\164\1\uffff\1\150"+
        "\1\166\1\144\1\137\1\164\1\uffff\1\151\1\164\1\163\1\165\1\156\1"+
        "\151\1\162\1\157\1\156\2\uffff\2\44\1\164\1\44\1\uffff\1\143\2\44"+
        "\1\uffff\1\44\1\164\1\uffff\1\44\1\145\1\156\1\154\2\uffff\1\157"+
        "\2\162\1\44\1\uffff\1\160\1\145\1\uffff\1\145\1\164\2\uffff\1\143"+
        "\1\154\1\156\1\141\1\156\1\uffff\1\44\1\145\1\164\1\154\1\162\1"+
        "\151\1\141\1\162\1\167\1\154\1\166\1\164\2\44\1\162\1\145\1\143"+
        "\2\156\1\144\2\uffff\1\44\1\uffff\1\44\3\uffff\1\44\1\uffff\1\44"+
        "\1\145\1\171\1\165\2\164\1\uffff\1\162\1\44\1\156\1\44\2\145\1\147"+
        "\1\143\1\154\1\uffff\1\144\3\44\1\156\1\154\1\44\1\145\1\154\1\141"+
        "\1\151\2\uffff\1\145\1\44\1\151\1\44\1\145\1\44\4\uffff\2\44\1\164"+
        "\2\44\1\151\1\157\1\uffff\1\164\1\uffff\1\44\1\137\1\44\1\153\1"+
        "\151\1\44\3\uffff\1\164\1\165\1\uffff\1\141\1\171\1\154\1\141\1"+
        "\44\1\uffff\1\164\1\uffff\1\44\3\uffff\1\44\2\uffff\1\166\1\144"+
        "\1\160\1\uffff\1\145\1\uffff\1\44\1\156\1\uffff\1\44\1\145\1\153"+
        "\1\137\1\165\1\154\1\uffff\1\146\2\uffff\1\141\1\162\1\164\1\170"+
        "\1\uffff\1\145\1\uffff\2\44\1\166\1\145\1\151\1\154\1\164\1\44\1"+
        "\162\1\164\1\44\2\uffff\1\151\1\44\1\172\1\157\1\145\1\uffff\1\44"+
        "\1\145\1\uffff\1\163\1\uffff\1\145\1\141\1\44\1\uffff\1\162\1\151"+
        "\1\162\1\164\1\uffff\1\156\1\142\2\44\1\141\1\154\2\uffff\1\154"+
        "\1\145\1\154\1\44\1\171\1\uffff\1\44\1\uffff";
    static final String DFA12_maxS =
        "\1\175\1\171\1\154\1\157\1\163\4\uffff\1\172\4\uffff\1\154\1\157"+
        "\1\166\1\164\1\156\1\145\1\170\1\145\1\165\1\162\1\165\1\163\1\171"+
        "\1\151\1\162\1\145\1\uffff\1\165\2\uffff\1\172\7\uffff\1\162\1\165"+
        "\1\160\1\172\1\164\1\154\1\146\1\151\1\142\1\156\1\141\1\150\1\162"+
        "\1\uffff\1\157\1\164\1\156\1\172\1\154\1\uffff\1\144\1\141\1\160"+
        "\1\167\1\150\1\144\1\157\1\162\1\151\1\164\1\155\1\172\1\141\1\164"+
        "\1\172\1\170\1\167\1\165\1\172\1\153\1\167\1\144\1\164\1\151\1\145"+
        "\2\164\1\170\1\164\1\145\1\160\1\157\1\142\1\151\1\154\3\164\1\163"+
        "\1\155\2\165\1\151\1\145\1\164\1\166\1\172\1\144\1\164\1\172\1\164"+
        "\1\154\3\uffff\1\147\1\154\1\160\1\156\1\145\1\uffff\1\141\2\151"+
        "\1\144\1\145\1\153\1\144\1\162\1\172\1\142\1\145\1\163\1\155\1\144"+
        "\1\172\2\uffff\1\154\1\172\1\151\1\145\1\147\1\141\1\157\1\162\1"+
        "\172\1\166\2\164\1\172\1\164\1\145\1\151\1\145\1\160\1\uffff\1\153"+
        "\1\162\1\143\1\uffff\1\157\1\164\1\154\1\172\1\154\1\141\1\156\1"+
        "\145\1\156\1\164\1\uffff\1\145\1\172\1\145\1\157\1\166\1\155\4\172"+
        "\1\164\1\157\1\156\1\164\1\172\1\162\1\172\1\166\5\172\1\162\1\170"+
        "\1\164\1\144\1\160\1\142\1\154\1\166\1\155\1\143\1\141\1\uffff\1"+
        "\144\1\163\1\uffff\1\144\2\172\1\145\1\172\1\154\1\143\1\172\1\154"+
        "\1\155\1\170\1\165\1\156\1\172\1\154\1\157\2\172\1\uffff\1\141\1"+
        "\154\1\164\1\157\1\143\1\uffff\1\172\1\163\1\uffff\1\154\2\156\1"+
        "\171\1\143\1\172\1\uffff\1\141\1\145\1\157\1\uffff\1\162\1\157\1"+
        "\147\1\156\1\162\2\172\1\162\1\141\1\164\1\151\2\172\1\uffff\1\151"+
        "\1\160\1\154\1\160\1\164\1\167\1\172\1\144\1\uffff\2\146\2\172\4"+
        "\uffff\1\172\1\146\1\145\2\172\1\145\1\uffff\1\145\1\uffff\1\172"+
        "\5\uffff\2\165\1\164\1\143\6\172\1\141\1\154\1\145\1\151\1\157\2"+
        "\uffff\1\164\1\uffff\1\145\1\172\1\uffff\1\141\2\160\1\154\1\145"+
        "\1\uffff\1\172\1\162\1\156\2\uffff\1\154\1\145\1\141\1\156\1\143"+
        "\1\uffff\1\160\1\141\1\144\1\172\1\163\1\141\1\uffff\1\164\1\143"+
        "\1\151\1\156\1\160\1\172\1\145\1\164\1\uffff\1\157\1\uffff\1\156"+
        "\1\143\1\172\1\156\1\170\2\uffff\1\141\1\164\1\151\1\154\1\165\1"+
        "\172\1\151\1\uffff\2\172\1\160\3\uffff\1\160\1\170\2\uffff\1\161"+
        "\1\172\1\uffff\1\156\2\151\1\172\1\143\6\uffff\1\163\1\172\1\156"+
        "\1\172\1\157\1\156\2\172\1\uffff\1\171\2\157\1\164\1\172\1\uffff"+
        "\1\137\1\143\1\172\1\155\1\156\2\172\1\141\1\142\1\151\1\164\1\uffff"+
        "\1\151\1\172\1\145\1\164\1\156\1\141\1\164\1\uffff\1\150\1\166\1"+
        "\144\1\141\1\164\1\uffff\1\151\1\164\1\163\1\165\1\156\1\151\1\162"+
        "\1\157\1\156\2\uffff\2\172\1\164\1\172\1\uffff\1\143\2\172\1\uffff"+
        "\1\172\1\164\1\uffff\1\172\1\145\1\156\1\154\2\uffff\1\157\2\162"+
        "\1\172\1\uffff\1\160\1\145\1\uffff\1\145\1\164\2\uffff\1\143\1\154"+
        "\1\156\1\141\1\156\1\uffff\1\172\1\145\1\164\1\154\1\162\1\151\1"+
        "\141\1\162\1\167\1\154\1\166\1\164\2\172\1\162\1\145\1\143\2\156"+
        "\1\144\2\uffff\1\172\1\uffff\1\172\3\uffff\1\172\1\uffff\1\172\1"+
        "\145\1\171\1\165\2\164\1\uffff\1\162\1\172\1\156\1\172\2\145\1\147"+
        "\1\143\1\154\1\uffff\1\144\3\172\1\156\1\154\1\172\1\145\1\154\1"+
        "\141\1\151\2\uffff\1\145\1\172\1\151\1\172\1\145\1\172\4\uffff\2"+
        "\172\1\164\2\172\1\151\1\157\1\uffff\1\164\1\uffff\1\172\1\137\1"+
        "\172\1\153\1\151\1\172\3\uffff\1\164\1\165\1\uffff\1\141\1\171\1"+
        "\154\1\141\1\172\1\uffff\1\164\1\uffff\1\172\3\uffff\1\172\2\uffff"+
        "\1\166\1\144\1\160\1\uffff\1\145\1\uffff\1\172\1\156\1\uffff\1\172"+
        "\1\145\1\153\1\137\1\165\1\154\1\uffff\1\146\2\uffff\1\141\1\162"+
        "\1\164\1\170\1\uffff\1\145\1\uffff\2\172\1\166\1\145\1\151\1\154"+
        "\1\164\1\172\1\162\1\164\1\172\2\uffff\1\151\2\172\1\157\1\145\1"+
        "\uffff\1\172\1\145\1\uffff\1\163\1\uffff\1\145\1\141\1\172\1\uffff"+
        "\1\162\1\151\1\162\1\164\1\uffff\1\156\1\142\2\172\1\141\1\154\2"+
        "\uffff\1\154\1\145\1\154\1\172\1\171\1\uffff\1\172\1\uffff";
    static final String DFA12_acceptS =
        "\5\uffff\1\7\1\10\1\11\1\12\1\uffff\1\14\1\15\1\16\1\17\20\uffff"+
        "\1\117\1\uffff\1\175\1\177\1\uffff\1\u0084\1\u0085\1\u0086\1\u0088"+
        "\1\u0089\1\u008a\1\u008b\15\uffff\1\13\5\uffff\1\u0087\64\uffff"+
        "\1\u0081\1\u0082\1\u0080\5\uffff\1\44\17\uffff\1\67\1\70\22\uffff"+
        "\1\176\3\uffff\1\162\12\uffff\1\163\42\uffff\1\174\2\uffff\1\155"+
        "\22\uffff\1\156\5\uffff\1\64\2\uffff\1\135\6\uffff\1\154\3\uffff"+
        "\1\122\15\uffff\1\157\10\uffff\1\160\4\uffff\1\164\1\165\1\166\1"+
        "\167\6\uffff\1\112\1\uffff\1\137\1\uffff\1\151\1\170\1\171\1\172"+
        "\1\173\17\uffff\1\124\1\141\1\uffff\1\133\2\uffff\1\4\5\uffff\1"+
        "\5\3\uffff\1\125\1\152\5\uffff\1\134\6\uffff\1\153\10\uffff\1\126"+
        "\1\uffff\1\30\5\uffff\1\46\1\42\7\uffff\1\100\3\uffff\1\143\1\146"+
        "\1\47\2\uffff\1\75\1\147\2\uffff\1\144\5\uffff\1\136\1\127\1\140"+
        "\1\142\1\145\1\150\10\uffff\1\45\5\uffff\1\6\13\uffff\1\120\7\uffff"+
        "\1\73\5\uffff\1\161\11\uffff\1\116\1\43\4\uffff\1\123\3\uffff\1"+
        "\51\2\uffff\1\74\4\uffff\1\1\1\3\4\uffff\1\u0083\2\uffff\1\20\2"+
        "\uffff\1\31\1\66\5\uffff\1\121\24\uffff\1\54\1\55\1\uffff\1\113"+
        "\1\uffff\1\52\1\53\1\65\1\uffff\1\62\6\uffff\1\61\11\uffff\1\23"+
        "\13\uffff\1\71\1\76\6\uffff\1\72\1\50\1\60\1\105\7\uffff\1\27\1"+
        "\uffff\1\21\6\uffff\1\56\1\25\1\57\2\uffff\1\35\5\uffff\1\104\1"+
        "\uffff\1\106\1\uffff\1\107\1\110\1\111\1\uffff\1\37\1\40\3\uffff"+
        "\1\22\1\uffff\1\32\2\uffff\1\63\6\uffff\1\77\1\uffff\1\114\1\2\4"+
        "\uffff\1\101\1\uffff\1\103\13\uffff\1\131\1\33\5\uffff\1\34\2\uffff"+
        "\1\102\1\uffff\1\132\3\uffff\1\130\4\uffff\1\24\6\uffff\1\41\1\115"+
        "\5\uffff\1\36\1\uffff\1\26";
    static final String DFA12_specialS =
        "\u0297\uffff}>";
    static final String[] DFA12_transitionS = {
            "\1\47\1\46\2\uffff\1\46\22\uffff\1\47\1\uffff\1\45\1\uffff\1"+
            "\43\1\42\1\uffff\1\44\1\13\1\14\1\5\1\uffff\1\15\1\41\1\43\1"+
            "\51\12\41\1\36\1\50\1\uffff\1\40\2\uffff\1\42\32\43\1\10\1\uffff"+
            "\1\12\1\uffff\1\43\1\uffff\1\20\1\32\1\17\1\2\1\24\1\31\1\16"+
            "\1\33\1\22\2\43\1\4\1\37\1\26\1\34\1\21\1\43\1\35\1\30\1\1\1"+
            "\27\1\3\1\23\1\11\1\43\1\25\1\6\1\uffff\1\7",
            "\1\52\15\uffff\1\55\2\uffff\1\53\6\uffff\1\54",
            "\1\56\3\uffff\1\60\6\uffff\1\57",
            "\1\61",
            "\1\62\7\uffff\1\63\5\uffff\1\64\3\uffff\1\65",
            "",
            "",
            "",
            "",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\16\43\1\66\13\43",
            "",
            "",
            "",
            "",
            "\1\71\6\uffff\1\70",
            "\1\75\76\uffff\1\74\1\uffff\1\73\13\uffff\1\72",
            "\1\76\7\uffff\1\101\1\uffff\1\103\1\uffff\1\100\2\uffff\1\102"+
            "\2\uffff\1\77",
            "\1\106\11\uffff\1\104\1\uffff\1\105",
            "\12\111\51\uffff\1\110\12\uffff\1\107",
            "\1\112",
            "\1\114\6\uffff\1\113",
            "\1\115",
            "\1\121\3\uffff\1\120\11\uffff\1\117\3\uffff\1\122\1\uffff\1"+
            "\116",
            "\1\125\2\uffff\1\127\1\uffff\1\124\2\uffff\1\130\1\uffff\1"+
            "\123\3\uffff\1\126",
            "\1\137\1\131\1\uffff\1\141\1\140\1\132\2\uffff\1\142\5\uffff"+
            "\1\133\1\134\1\135\1\136",
            "\1\144\1\uffff\1\145\1\150\10\uffff\1\147\2\uffff\1\143\1\uffff"+
            "\1\151\1\146",
            "\1\152\10\uffff\1\154\6\uffff\1\153",
            "\1\155",
            "\1\156\1\uffff\1\157",
            "\1\160",
            "",
            "\1\161",
            "",
            "",
            "\1\163\1\uffff\1\162\11\uffff\1\162\1\uffff\12\164\7\uffff"+
            "\32\162\4\uffff\1\162\1\uffff\32\162",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\166\10\uffff\1\165",
            "\1\167\13\uffff\1\170",
            "\1\171",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\173",
            "\1\174",
            "\1\175",
            "\1\176",
            "\1\177",
            "\1\u0080",
            "\1\u0081",
            "\1\u0082",
            "\1\u0083",
            "",
            "\1\u0084",
            "\1\u0085",
            "\1\u0088\1\u0087\1\u0086",
            "\1\u008a\3\uffff\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32"+
            "\43\4\uffff\1\43\1\uffff\2\43\1\u0089\27\43",
            "\1\u008c",
            "",
            "\1\u008d",
            "\1\u008e",
            "\1\u008f",
            "\1\u0090\2\uffff\1\u0092\12\uffff\1\u0091",
            "\1\u0093",
            "\1\u0094",
            "\1\u0095\5\uffff\1\u0096",
            "\1\u0097",
            "\1\u0098",
            "\1\u009b\5\uffff\1\u009a\1\u009c\1\u0099",
            "\1\u009d",
            "\1\43\11\uffff\1\43\1\uffff\12\111\7\uffff\32\43\4\uffff\1"+
            "\43\1\uffff\32\43",
            "\1\u009f",
            "\1\u00a1\22\uffff\1\u00a0",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\u00a3\5\uffff\1\u00a4",
            "\1\u00a5\12\uffff\1\u00a6",
            "\1\u00a7\1\uffff\1\u00a8\5\uffff\1\u00a9\10\uffff\1\u00aa\2"+
            "\uffff\1\u00ab",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\22\43\1\u00ac\7\43",
            "\1\u00ae",
            "\1\u00af",
            "\1\u00b0",
            "\1\u00b1",
            "\1\u00b2",
            "\1\u00b3",
            "\1\u00b5\16\uffff\1\u00b4",
            "\1\u00b7\16\uffff\1\u00b6",
            "\1\u00b8",
            "\1\u00ba\14\uffff\1\u00b9",
            "\1\u00bb",
            "\1\u00bc",
            "\1\u00bd",
            "\1\u00be",
            "\1\u00bf",
            "\1\u00c0",
            "\1\u00c2\16\uffff\1\u00c1",
            "\1\u00c4\16\uffff\1\u00c3",
            "\1\u00c6\16\uffff\1\u00c5",
            "\1\u00c8\16\uffff\1\u00c7",
            "\1\u00c9",
            "\1\u00ca",
            "\1\u00cb",
            "\1\u00cc",
            "\1\u00cd",
            "\1\u00ce",
            "\1\u00cf",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\u00d1",
            "\1\u00d2",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\u00d4\22\uffff\1\u00d5",
            "\1\u00d6",
            "",
            "",
            "",
            "\1\u00d7",
            "\1\u00d8",
            "\1\u00d9",
            "\1\u00da",
            "\1\u00db",
            "",
            "\1\u00dc",
            "\1\u00de\3\uffff\1\u00dd",
            "\1\u00df\7\uffff\1\u00e0",
            "\1\u00e1",
            "\1\u00e2",
            "\1\u00e3",
            "\1\u00e4",
            "\1\u00e5",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\u00e7",
            "\1\u00e8",
            "\1\u00e9",
            "\1\u00ea",
            "\1\u00eb",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "",
            "",
            "\1\u00ed",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\21\43\1\u00ee\10\43",
            "\1\u00f0",
            "\1\u00f1",
            "\1\u00f2",
            "\1\u00f3",
            "\1\u00f4",
            "\1\u00f5",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\u00f7",
            "\1\u00f8",
            "\1\u00f9",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\u00fb\16\uffff\1\u00fc",
            "\1\u00fd",
            "\1\u00fe",
            "\1\u00ff",
            "\1\u0100",
            "",
            "\1\u0101",
            "\1\u0102\14\uffff\1\u0103",
            "\1\u0104",
            "",
            "\1\u0105",
            "\1\u0106",
            "\1\u0107",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\u0109",
            "\1\u010a",
            "\1\u010c\1\u010b",
            "\1\u010d",
            "\1\u010e",
            "\1\u010f",
            "",
            "\1\u0110",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\u0112",
            "\1\u0113",
            "\1\u0114",
            "\1\u0115",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\u011a",
            "\1\u011b",
            "\1\u011c",
            "\1\u011e\6\uffff\1\u011d",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\21\43\1\u011f\10\43",
            "\1\u0121",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\u0123",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\u012a\2\uffff\1\u0129",
            "\1\u012b",
            "\1\u012c",
            "\1\u012d",
            "\1\u012e",
            "\1\u012f",
            "\1\u0130",
            "\1\u0131",
            "\1\u0132",
            "\1\u0133",
            "\1\u0134",
            "",
            "\1\u0135",
            "\1\u0136",
            "",
            "\1\u0137",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\u013a",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\u013c",
            "\1\u013d",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\u013f",
            "\1\u0140",
            "\1\u0141",
            "\1\u0142",
            "\1\u0143",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\u0145",
            "\1\u0146\11\uffff\1\u0147",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "",
            "\1\u014a",
            "\1\u014b",
            "\1\u014c",
            "\1\u014d",
            "\1\u014e",
            "",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\u0150",
            "",
            "\1\u0151",
            "\1\u0152",
            "\1\u0153",
            "\1\u0154",
            "\1\u0155",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "",
            "\1\u0157",
            "\1\u0158",
            "\1\u0159",
            "",
            "\1\u015a",
            "\1\u015b",
            "\1\u015c",
            "\1\u015d",
            "\1\u015e",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\u0160"+
            "\1\uffff\32\43",
            "\1\u0162",
            "\1\u0163",
            "\1\u0164",
            "\1\u0166\3\uffff\1\u0165",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "",
            "\1\u0169",
            "\1\u016a",
            "\1\u016b",
            "\1\u016c",
            "\1\u016e\17\uffff\1\u016d",
            "\1\u016f",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\u0171",
            "",
            "\1\u0172",
            "\1\u0173",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "",
            "",
            "",
            "",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\u0177",
            "\1\u0178",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\u017b",
            "",
            "\1\u017c",
            "",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "",
            "",
            "",
            "",
            "",
            "\1\u017e",
            "\1\u0180\1\uffff\1\u017f",
            "\1\u0181",
            "\1\u0182",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\u0189",
            "\1\u018a",
            "\1\u018b",
            "\1\u018c",
            "\1\u018d\1\u018e",
            "",
            "",
            "\1\u018f",
            "",
            "\1\u0190",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "",
            "\1\u0192",
            "\1\u0193",
            "\1\u0194",
            "\1\u0195",
            "\1\u0196",
            "",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\u0198",
            "\1\u0199",
            "",
            "",
            "\1\u019a",
            "\1\u019b",
            "\1\u019c",
            "\1\u019d",
            "\1\u019e",
            "",
            "\1\u019f",
            "\1\u01a0",
            "\1\u01a1",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\22\43\1\u01a2\7\43",
            "\1\u01a4",
            "\1\u01a5",
            "",
            "\1\u01a6",
            "\1\u01a7",
            "\1\u01a8",
            "\1\u01a9",
            "\1\u01aa",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\u01ac",
            "\1\u01ad",
            "",
            "\1\u01ae",
            "",
            "\1\u01af",
            "\1\u01b0",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\u01b2",
            "\1\u01b3",
            "",
            "",
            "\1\u01b4",
            "\1\u01b5",
            "\1\u01b6",
            "\1\u01b7",
            "\1\u01b8",
            "\1\u01b9",
            "\1\u01ba",
            "",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\u01bd",
            "",
            "",
            "",
            "\1\u01be",
            "\1\u01bf",
            "",
            "",
            "\1\u01c0",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "",
            "\1\u01c2",
            "\1\u01c3",
            "\1\u01c4",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\u01c6",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\u01c7",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\u01c9",
            "\1\u01ca",
            "\1\u01cb",
            "\1\u01cc",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "",
            "\1\u01cf",
            "\1\u01d0",
            "\1\u01d1",
            "\1\u01d2",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "",
            "\1\u01d4",
            "\1\u01d5",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\u01d7",
            "\1\u01d8",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\u01db",
            "\1\u01dc",
            "\1\u01dd",
            "\1\u01de",
            "",
            "\1\u01df",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\u01e1",
            "\1\u01e2",
            "\1\u01e3",
            "\1\u01e4",
            "\1\u01e5",
            "",
            "\1\u01e6",
            "\1\u01e7",
            "\1\u01e8",
            "\1\u01e9\1\uffff\1\u01ea",
            "\1\u01eb",
            "",
            "\1\u01ec",
            "\1\u01ed",
            "\1\u01ee",
            "\1\u01ef",
            "\1\u01f0",
            "\1\u01f1",
            "\1\u01f2",
            "\1\u01f3",
            "\1\u01f4",
            "",
            "",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\u01f7",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "",
            "\1\u01f9",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\u01fd",
            "",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\u01ff",
            "\1\u0200",
            "\1\u0201",
            "",
            "",
            "\1\u0202",
            "\1\u0203",
            "\1\u0204",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "",
            "\1\u0206",
            "\1\u0207",
            "",
            "\1\u0208",
            "\1\u0209",
            "",
            "",
            "\1\u020a",
            "\1\u020b",
            "\1\u020c",
            "\1\u020d",
            "\1\u020e",
            "",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\u0210",
            "\1\u0211",
            "\1\u0212",
            "\1\u0213",
            "\1\u0214",
            "\1\u0215",
            "\1\u0216",
            "\1\u0217",
            "\1\u0218",
            "\1\u0219",
            "\1\u021a",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\u021d",
            "\1\u021e",
            "\1\u021f",
            "\1\u0220",
            "\1\u0221",
            "\1\u0222",
            "",
            "",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "",
            "",
            "",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\u0227",
            "\1\u0228",
            "\1\u0229",
            "\1\u022a",
            "\1\u022b",
            "",
            "\1\u022c",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\u022d"+
            "\1\uffff\32\43",
            "\1\u022f",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\u0231",
            "\1\u0232",
            "\1\u0233",
            "\1\u0234",
            "\1\u0235",
            "",
            "\1\u0236",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\u023a",
            "\1\u023b",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\u023d",
            "\1\u023e",
            "\1\u023f",
            "\1\u0240",
            "",
            "",
            "\1\u0241",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\u0243",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\u0245",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "",
            "",
            "",
            "",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\u0249",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\u024c",
            "\1\u024d",
            "",
            "\1\u024e",
            "",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\u0250",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\u0252",
            "\1\u0253",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "",
            "",
            "",
            "\1\u0255",
            "\1\u0256",
            "",
            "\1\u0257",
            "\1\u0258",
            "\1\u0259",
            "\1\u025a",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "",
            "\1\u025c",
            "",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "",
            "",
            "",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "",
            "",
            "\1\u025f",
            "\1\u0260",
            "\1\u0261",
            "",
            "\1\u0262",
            "",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\u0264",
            "",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\u0266",
            "\1\u0267",
            "\1\u0268",
            "\1\u0269",
            "\1\u026a",
            "",
            "\1\u026b",
            "",
            "",
            "\1\u026c",
            "\1\u026d",
            "\1\u026e",
            "\1\u026f",
            "",
            "\1\u0270",
            "",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\u0273",
            "\1\u0274",
            "\1\u0275",
            "\1\u0276",
            "\1\u0277",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\u0279",
            "\1\u027a",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "",
            "",
            "\1\u027c",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\u027e",
            "\1\u027f",
            "\1\u0280",
            "",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\u0282",
            "",
            "\1\u0283",
            "",
            "\1\u0284",
            "\1\u0285",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "",
            "\1\u0287",
            "\1\u0288",
            "\1\u0289",
            "\1\u028a",
            "",
            "\1\u028b",
            "\1\u028c",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\u028f",
            "\1\u0290",
            "",
            "",
            "\1\u0291",
            "\1\u0292",
            "\1\u0293",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
            "\1\u0295",
            "",
            "\1\43\11\uffff\1\43\1\uffff\12\43\7\uffff\32\43\4\uffff\1\43"+
            "\1\uffff\32\43",
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
            return "1:1: Tokens : ( T__23 | T__24 | T__25 | T__26 | T__27 | T__28 | T__29 | T__30 | T__31 | T__32 | T__33 | T__34 | T__35 | T__36 | T__37 | T__38 | T__39 | T__40 | T__41 | T__42 | T__43 | T__44 | T__45 | T__46 | T__47 | T__48 | T__49 | T__50 | T__51 | T__52 | T__53 | T__54 | T__55 | T__56 | T__57 | T__58 | T__59 | T__60 | T__61 | T__62 | T__63 | T__64 | T__65 | T__66 | T__67 | T__68 | T__69 | T__70 | T__71 | T__72 | T__73 | T__74 | T__75 | T__76 | T__77 | T__78 | T__79 | T__80 | T__81 | T__82 | T__83 | T__84 | T__85 | T__86 | T__87 | T__88 | T__89 | T__90 | T__91 | T__92 | T__93 | T__94 | T__95 | T__96 | T__97 | T__98 | T__99 | T__100 | T__101 | T__102 | T__103 | T__104 | T__105 | T__106 | T__107 | T__108 | T__109 | T__110 | T__111 | T__112 | T__113 | T__114 | T__115 | T__116 | T__117 | T__118 | T__119 | T__120 | T__121 | T__122 | T__123 | T__124 | T__125 | T__126 | T__127 | T__128 | T__129 | T__130 | T__131 | T__132 | T__133 | T__134 | T__135 | T__136 | T__137 | T__138 | T__139 | T__140 | T__141 | T__142 | T__143 | T__144 | T__145 | T__146 | EQUALS | INT_TYPE | NUMBER | UNNAMED_ID | NAMED_ID | QUOTED_ID | DEFINE | LABEL | CHAR_LITERAL | STRING_LITERAL | CSTRING_LITERAL | NEWLINE | WS | SINGLE_COMMENT | MULTI_COMMENT );";
        }
    }
 

}