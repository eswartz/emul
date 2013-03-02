;   v9t9render_utils.i
; 
;   (c) 2010-2011 Edward Swartz
; 
;   All rights reserved. This program and the accompanying materials
;   are made available under the terms of the Eclipse Public License v1.0
;   which accompanies this distribution, and is available at
;   http://www.eclipse.org/legal/epl-v10.html
; 

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