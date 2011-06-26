
/**
 *	Interpret binary data as TMS5200 speech data and
 *	emit sound data for it
 *
 */
#include <unistd.h>
#include <stdio.h>
#include "lpc.h"

int stUnvoiced = 0;
int stRepeat = 1;
int stReverse = 0;
int addr = 0;
u8 stByte, stBit;


static u8   swapped_nybbles[16] = 
{ 
	0x0, 0x8, 0x4, 0xc,
	0x2, 0xa, 0x6, 0xe,
	0x1, 0x9, 0x5, 0xd,
	0x3, 0xb, 0x7, 0xf
};

static      u8
swapbits(u8 in)
{
	return (swapped_nybbles[in & 0xf] << 4) |
		(swapped_nybbles[(in & 0xf0) >> 4]);
}

u8 getByte() {
	int			c;
	// initialize
	c = getc(stdin);
	stByte = c & 0xff;
	if (stReverse)
		stByte = swapbits(stByte);

	if (c != -1) {
//		fprintf(stderr, "Fetched byte from >%04X / %d = >%02X\n", addr, stBit, stByte);
		addr++;
	}
	return stByte;
}

u32 fetch(int bits) {
	u32         cur;

//	fprintf(stderr, "Fetching %d bits\n", bits);
	if (stBit + bits > 8) {
		cur = stByte;
		cur <<= 8;
		cur |= getByte();
	} else
		cur = stByte << 8;
	

	/*  Get the bits we want.  */
	cur = (cur << (stBit + 16)) >> (32 - bits);

	/*  Adjust bit ptr  */
	stBit = (stBit + bits) & 7;
	if (stBit == 0)
		getByte();

	return cur;
}

int main(int argc, char **argv) {

	#define LENGTH 200
	s8* speech_data = xmalloc(LENGTH);
	u32 speech_length = LENGTH;
	void *data;

	int skipByte = 0;

	int c;
	while ((c=getopt(argc, argv, "sr:ub:S")) != -1) {
		switch (c) {
		case 'b':
			speech_length = atoi(optarg);
			//if (speech_length < 200)
			//	speech_length = 200;
			xfree(speech_data);
			speech_data = xmalloc(speech_length);
			break;
		case 's':
			stReverse = 1;
			break;
		case 'r':
			stRepeat = atoi(optarg);
			break;
		case 'u':
			stUnvoiced = 1;
			break;
		case 'S':
			skipByte = 1;
			break;
		}
	}

	stBit = 0;
	addr = 0;
	getByte();
	while (!feof(stdin)) {
		LPCinit();

		fprintf(stderr, "Starting new equation at >%04X...\n", addr);

		while (LPCavailable()) {

			LPCreadEquation(fetch, stUnvoiced);
			int cnt = stRepeat;
			while (cnt-- > 0) {
				LPCexec(speech_data, speech_length);
				fwrite(speech_data, speech_length, 1, stdout);
			}
//			fprintf(stderr, "... >%04X / %d...\n", addr, stBit);
		}
//		fprintf(stderr, "Equation ended at >%04X / %d\n", addr, stBit);
		if (skipByte)
			getByte();
		if (stBit != 0)
			getByte();
		stBit = 0;
//		fprintf(stderr, "Synced to >%04X / %d\n", addr, stBit);
	}
	fprintf(stderr, "Finished!\n");
	return 0;

}
