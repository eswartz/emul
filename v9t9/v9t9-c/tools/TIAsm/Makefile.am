AUTOMAKE_OPTIONS = foreign no-dependencies

TOP = $(top_srcdir)

BIN = $(TOP)/../../build/bin

TIASM_JAR = $(BIN)/tiasm.jar

all: $(TIASM_JAR)
	
$(TIASM_JAR):
	ant -f $(TOP)/../../v9t9-java/v9t9-tools/99asm.xml

clean:
	rm -f $(TIASM_JAR)
	
