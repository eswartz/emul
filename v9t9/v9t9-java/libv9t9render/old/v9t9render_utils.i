
%module V9t9RenderUtils
%include "various.i";
%include "typemaps.i";
%array_functions(char, byteArray);
%apply int *INOUT { int *result };

%{
#include "render.h"
%}

void        scaleImage(
		char* BYTE,
        const char* BYTE,  int offset,
        int width, int height, int rowstride,
        int destWidth, int destHeight, int destRowstride,
        int upx, int upy, int upwidth, int upheight);

void        scaleImageToRGBA(
		int* result,
        const char* BYTE, int offset,
        int width, int height, int rowstride,
        int destWidth, int destHeight, int destRowstride,
        int upx, int upy, int upwidth, int upheight);

void addNoise(char *BYTE, int offset, int destWidth, int destHeight, int destrowstride,
		int width, int height);
		
void addNoiseRGBA(int *result, int offset, int destWidth, int destHeight, int destrowstride,
		int width, int height);		