/*
dcbitvector.h

(c) 1994-2011 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
 */
#ifndef __DCBITVECTORS_H__
#define __DCBITVECTORS_H__

#define INLINE static inline
#define uint32 u32
#define uint64 u64
#define uint16 u16
#define uint8 u8

#define BV_BITS_TYPE		uint32
#define BV_BIT_COUNT		32			// bits per index
#define BV_BIT_SHIFT		5			// shift between index <--> bit
#define BV_BIT_MASK		0x1f		// mask from bit to one index
#define BV_ZEROES			0UL			// mask for zeroes
#define BV_ONES			(~0UL)		// mask for ones
#define BV_ONE				(1UL)

typedef struct BitVector
{
	uint32			lo,hi;		// range of bits represented in vector, scaled to BV_BITS_PER_SIZE
	uint32			count;		// number of indices allocated in bits
	BV_BITS_TYPE 	*bits;		// allocated bits, [lo...lo+size)
} BitVector;

// index for low range bit
#define BIT_TO_INDEX_LO(b)		((b) >> BV_BIT_SHIFT)

// index for high range bit
#define BIT_TO_INDEX_HI(b)		(((b) + BV_BIT_COUNT) >> BV_BIT_SHIFT)

// get index into (v)->bits from bit.  This must be checked against
// INDEX_IN_RANGE to avoid invalid dereferences
#define BIT_TO_INDEX(v, b) 		(BIT_TO_INDEX_LO(b) - (v)->lo)
	
// we assume that a negative index is always larger than (v)->size
#define INDEX_IN_RANGE(v, i) 	((i) < (v)->count)
	
// get bit[] entry from index
#define INDEX_TO_PTR(v, i)		(&(v)->bits[(i)])
	
// get mask for bit
#define BIT_TO_MASK(b) 			(BV_ONE << ((b) & BV_BIT_MASK))

// get mask for series of bits IN ONE WORD
#define BITS_TO_MASK_BELOW(a)		((a) == BV_BIT_COUNT ? BV_ONES : (~(BV_ONES << (a))))
#define BITS_TO_MASK_ABOVE(a)		(BV_ONES << (a))
#define BITS_TO_MASK(a, b)			(BITS_TO_MASK_BELOW(b) & BITS_TO_MASK_ABOVE(a))

#define bv_FIRST_BIT(v)	((v)->lo << BV_BIT_SHIFT)
#define bv_LAST_BIT(v)	((v)->hi << BV_BIT_SHIFT)

void bv_AllocVector(BitVector **v, int unused_compat);
void bv_AllocVectorWith(BitVector **v, uint32 lobit, uint32 bitcount);
void bv_FreeVector(BitVector **v);

void bv_InitVector(BitVector *v);
void bv_InitVectorWith(BitVector *v, uint32 lobit, uint32 bitcount);
void bv_Resize(BitVector *v, uint32 lo, uint32 hi);
void bv_Shrink(BitVector *v);

void bv_Set(BitVector *v, uint32 lobit, uint32 hibit);
void bv_Clear(BitVector *v);
bool bv_IsEmpty(const BitVector *v);

uint32 bv_IncludeBit(uint32 b, BitVector *v);
INLINE void bv_SetBit(uint32 b, BitVector *v);
INLINE void bv_ClearBit(uint32 b, BitVector *v);
INLINE bool bv_BitSet(uint32 b, const BitVector *v);
void bv_SetBitRange(uint32 a, uint32 b, BitVector *v);
void bv_ClearBitRange(uint32 a, uint32 b, BitVector *v);

void bv_And(const BitVector *v1, BitVector *v2);
void bv_Or(const BitVector *v1, BitVector *v2);
INLINE bool bv_BitsInCommon(const BitVector *b1, const BitVector *b2);
bool bv_Compare(const BitVector *v1, const BitVector *v2);
void bv_Minus(const BitVector *v1, BitVector *v2);
void bv_Copy(const BitVector *v1, BitVector *v2);
bool bv_IsSubset(const BitVector *v1, const BitVector *v2);

int bv_CountBitsInBitRange(const BitVector *bv, uint32 startbit, uint32 endbit);
int bv_CountBits(const BitVector *bv);

void bv_Dump(void (*Printf)(void *file, const char *, ...), void *file, const char *str, const BitVector *v);

// Set a bit
	
INLINE void bv_SetBit(uint32 b, BitVector *v)
{
	uint32 i = BIT_TO_INDEX(v, b);
	if (!INDEX_IN_RANGE(v, i))
		i = bv_IncludeBit(b, v);
	*INDEX_TO_PTR(v, i) |= BIT_TO_MASK(b);
}


// Clear a bit

INLINE void bv_ClearBit(uint32 b, BitVector *v)
{
	uint32 i = BIT_TO_INDEX(v, b);

	// out-of-range bits are assumed to be zero
	if (!INDEX_IN_RANGE(v, i)) return;
	
	*INDEX_TO_PTR(v, i) &= ~BIT_TO_MASK(b);
}


// Test if a bit is set

INLINE bool bv_BitSet(uint32 b, const BitVector *v)
{
	uint32 i = BIT_TO_INDEX(v, b);

	// out-of-range bits are assumed to be zero
	if (!INDEX_IN_RANGE(v, i)) return false;
	
	return (*INDEX_TO_PTR(v, i) & BIT_TO_MASK(b)) != BV_ZEROES;
}

// Determine if v1 and v2 have any bits set in common

INLINE bool bv_BitsInCommon(const BitVector *v1, const BitVector *v2)
{
	uint32 lo, hi;
	uint32 i;
	
	// for common, check bits in common segments
	lo = (v1->lo > v2->lo) ? v1->lo : v2->lo;
	hi = (v1->hi < v2->hi) ? v1->hi : v2->hi;
	
	for (i = lo; i < hi; i++)
	{
		if (v2->bits[i - v2->lo] & v1->bits[i - v1->lo])
			return true;
	}
	return false;
}



#endif	// __SMALLBITVECTORS_H__
