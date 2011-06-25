
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
