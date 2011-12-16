
all: ../../hrtlib.dll
clean: 
	rm ../../hrtlib.dll
	
PREFIX=com_vladium_utils_timing
JAVAHOME=c:/program files/java/jdk1.6.0_11

../../hrtlib.dll: $(PREFIX)_HRTimer.c $(PREFIX)_HRTimer.h
	gcc -shared -o $@ $(PREFIX)_HRTimer.c -I"$(JAVAHOME)/include" \
	"-I$(JAVAHOME)/include/win32" \
	-mno-cygwin -lwinmm -Wl,--kill-at -DNDEBUG
