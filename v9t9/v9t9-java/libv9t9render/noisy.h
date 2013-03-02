/*
  noisy.h

  (c) 2010-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
#ifdef _WIN32
__declspec(dllexport) 	//__stdcall
#endif
void addNoiseRGBA(unsigned char *dststart, unsigned char *srcstart,
		int offset, int end,
		int width, int height, int rowstride,
		int realWidth, int realHeight, int fullHeight);

#ifdef _WIN32
__declspec(dllexport) 	//__stdcall
#endif
void addNoiseRGBAMonitor(unsigned char *dststart, unsigned char *srcstart,
		int offset, int end,
		int width, int height, int rowstride,
		int realWidth, int realHeight, int fullHeight);
