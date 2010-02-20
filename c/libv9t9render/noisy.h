
#ifdef _WIN32
__declspec(dllexport) __stdcall
#endif
void add_noise(unsigned char *data, int offset,
		int width, int height, int rowstride,
		int realWidth, int realHeight);

#ifdef _WIN32
__declspec(dllexport) __stdcall
#endif
void add_noise_rgba(unsigned char *data, unsigned char *src,
		int offset,
		int width, int height, int rowstride,
		int realWidth, int realHeight);
