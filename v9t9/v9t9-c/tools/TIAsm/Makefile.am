AUTOMAKE_OPTIONS = foreign no-dependencies

TOP = $(top_srcdir)

BIN = $(TOP)/../../build/bin

TIASM_JAR = $(BIN)/tiasm.jar

all: $(TIASM_JAR)
	
$(TIASM_JAR):
	ant

clean:
	rm -f $(TIASM_JAR)
	