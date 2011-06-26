
all: config
	make -C v9t9
	make -C tools
	
config: v9t9/configure tools/configure

v9t9/configure:
	( cd v9t9 && sh autogen.sh )

tools/configure:
	( cd tools && sh autogen.sh )
	
clean: config
	-make -C v9t9 clean
	-make -C tools clean

reallyclean: clean
	rm -f v9t9/configure tools/configure

