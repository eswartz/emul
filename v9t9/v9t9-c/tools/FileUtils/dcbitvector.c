/*
dcbitvector.c

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
#include "v9t9_common.h"
#include "xmalloc.h"

#include "dcbitvector.h"


#if 0
#define VALIDATE(bv) \
	if (((bv)->count != (bv)->hi - (bv)->lo) || ((bv)->count && !(bv)->bits)) \
	COMPILERERROR;
#else
#define VALIDATE(bv)
#endif

static void bv_free(void *ptr)
{
	xfree(ptr);
}

static void *bv_malloc(long count)
{
	return xmalloc((count ? count : 1) * sizeof(BV_BITS_TYPE));
}

static void *bv_calloc(long count)
{
	return xcalloc((count ? count : 1) * sizeof(BV_BITS_TYPE));
}

static void *bv_realloc(void *ptr, long oldcount, long count)
{
	return xrealloc(ptr, (count ? count : 1) * sizeof(BV_BITS_TYPE));
}

#define bv_set(mem, val, sz)	if (sz > 0) { memset(mem, val, (sz) * sizeof(BV_BITS_TYPE)); }
#define bv_move(mem1, mem2, sz)	if (sz > 0) { memmove(mem1, mem2, (sz) * sizeof(BV_BITS_TYPE)); }

// Resize a bitvector to accomodate the given index range

static void bv_resize(BitVector *v, uint32 lo, uint32 hi)
{
	uint32 count;
	BV_BITS_TYPE *bits;
	
	// conserve some energy in the universe
	if (v->lo == lo && v->hi == hi) return;
	
	if (lo >= hi) 
	{
		// empty
		bv_Clear(v);
		return;
	}
	
	// see what the new count is
	count = hi - lo;

	// get space for new vector
	bits = bv_malloc(count);
	
	// copy the old bits into the right spot in the new vector,
	// and clear new bits
	if (v->hi <= lo || hi <= v->lo)
	{
		// [old] [new]  or [new] [old]
		// nothing in new vector
		bv_set(bits, 0, count);
	}
	else if (v->lo <= lo && v->hi >= hi)
	{
		// [ old [new] ]
		bv_move(bits, v->bits + lo - v->lo, hi - lo);
	}
	else if (v->lo <= lo)
	{
		// [ old ( new ] )
		bv_move(bits, v->bits + lo - v->lo, v->hi - lo);
		bv_set(bits + v->hi - lo, 0, hi - v->hi);
	}
	else if (lo <= v->lo && hi >= v->hi)
	{
		// [ new [old] ]
		bv_set(bits, 0, v->lo - lo);
		bv_move(bits + v->lo - lo, v->bits, v->hi - v->lo);
		bv_set(bits + v->hi - lo, 0, hi - v->hi);
	}
	else
	{
		// [ new (old] )
		bv_set(bits, 0, v->lo - lo);
		bv_move(bits + v->lo - lo, v->bits, hi - v->lo);
	}
	
	// fixup bitvector
	bv_free(v->bits);
	v->bits = bits;
	v->lo = lo;
	v->hi = hi;
	v->count = count;
}

#if 0
#pragma mark -
#endif


void bv_Dump(void (*Printf)(void *file, const char *, ...), void *file, const char *str, const BitVector *v)
{
	int i, runStart;
	bool inRun = false;
	bool first = true;
	
	Printf(file, "%s", str);
	if (v == NULL)
		Printf(file, "NULL");
	else
	{
		Printf(file, "[%d,%d) ", v->lo << BV_BIT_SHIFT, v->hi << BV_BIT_SHIFT);
		for (i = v->lo << BV_BIT_SHIFT; i < v->hi << BV_BIT_SHIFT; i++)
		{
			if (bv_BitSet(i, v))
			{
				if (!inRun)
				{
					if (!first)
					{
						Printf(file, ",");
					}
					first = false;
					Printf(file, "%d", i);
					inRun = true;
					runStart = i;
				}
			}
			else
			{
				if (inRun)
				{
					inRun = false;
					if (i != runStart + 1)
					{
						// for longer runs, just show range
						Printf(file, "-%d", i - 1);
					}
				}
			}
		}
		
		// handle runs that go to the end of the bit vector
		if (inRun)
		{
			if (i != runStart + 1)
			{
				// for longer runs, just show range
				Printf(file, "-%d", i - 1);
			}
		}
	}
	
	Printf(file, "\n");
}

// Initialize a bit vector

void bv_InitVector(BitVector *v)
{
	v->lo = v->hi = 0;
	v->count = 0;
	v->bits = 0L;
	VALIDATE(v);
}

// Initialize a bit vector with an expected range (may be expanded later...)

void bv_InitVectorWith(BitVector *v, uint32 lobit, uint32 hibit)
{
	v->count = BIT_TO_INDEX_HI(hibit) - BIT_TO_INDEX_LO(lobit);
	v->bits = bv_calloc(v->count);
	v->lo = lobit; v->hi = hibit;
	VALIDATE(v);
}

// Allocate and initialize a bit vector

void bv_AllocVector(BitVector **v, int unused_compat)
{
#pragma unused(unused_compat)
	*v = xcalloc(sizeof(BitVector));
	bv_Clear(*v);
	VALIDATE(*v);
}

void bv_AllocVectorWith(BitVector **v, uint32 lobit, uint32 hibit)
{
	*v = xcalloc(sizeof(BitVector));
	bv_Clear(*v);
	bv_Resize(*v, lobit, hibit);
	VALIDATE(*v);
}

void bv_FreeVector(BitVector **v)
{
	if (!*v) return;
	bv_Clear(*v);
	xfree(*v);
	*v = 0L;
}


// Resize a bitvector to accomodate the given bit range

void bv_Resize(BitVector *v, uint32 lobit, uint32 hibit)
{
	uint32 lo = BIT_TO_INDEX_LO(lobit);
	uint32 hi = BIT_TO_INDEX_HI(hibit);
	bv_resize(v, lo, hi);
	VALIDATE(v);
}

// Shrink a bitvector to minimum size

void bv_Shrink(BitVector *v)
{
	uint32 lo = v->lo;
	uint32 hi = v->hi;
	while (lo < hi && !v->bits[lo - v->lo])
		lo++;
	while (hi > lo && !v->bits[hi - v->lo - 1])
		hi--;
	bv_resize(v, lo, hi);
	VALIDATE(v);
}

// Clear all bits

void bv_Clear(BitVector *v)
{
	v->lo = v->hi = v->count = 0;
	bv_free(v->bits);
	v->bits = 0L;
	VALIDATE(v);
}


// Set all bits

void bv_Set(BitVector *v, uint32 lobit, uint32 hibit)
{
	bv_Resize(v, lobit, hibit);
	bv_set(v->bits, -1, v->hi - v->lo);
	VALIDATE(v);
}

// Set a range of bits [a..b)

void bv_SetBitRange(uint32 a, uint32 b, BitVector *v)
{
	uint32 ai, bi;
	BV_BITS_TYPE mask;
	
	if (a >= b) return;

	// make sure memory exists
	bv_IncludeBit(a, v);
	bv_IncludeBit(b-1, v);

	ai = BIT_TO_INDEX(v, a);
	bi = BIT_TO_INDEX(v, b);

	if (ai == bi)
	{
		// set bits in one word only
		mask = BITS_TO_MASK(a & BV_BIT_MASK, b & BV_BIT_MASK);
		*INDEX_TO_PTR(v, ai) |= mask;
	}
	else
	{
		// set bits in many words
		mask = BITS_TO_MASK_ABOVE(a & BV_BIT_MASK);
		*INDEX_TO_PTR(v, ai) |= mask;
		ai++;
		while (ai < bi)
		{
			*INDEX_TO_PTR(v, ai) = ~0;
			ai++;
		}
		mask = BITS_TO_MASK_BELOW(b & BV_BIT_MASK);
		*INDEX_TO_PTR(v, ai) |= mask;
	}
	VALIDATE(v);
}

// Reset a range of bits [a..b)

void bv_ClearBitRange(uint32 a, uint32 b, BitVector *v)
{
	uint32 ai, bi;
	BV_BITS_TYPE mask;
	
	// truncate to width of vector
	if (a < bv_FIRST_BIT(v)) a = bv_FIRST_BIT(v);
	if (b > bv_LAST_BIT(v)) b = bv_LAST_BIT(v);

	if (a >= b) return;

	ai = BIT_TO_INDEX(v, a);
	bi = BIT_TO_INDEX(v, b);

	if (ai == bi)
	{
		// set bits in one word only
		mask = BITS_TO_MASK(a & BV_BIT_MASK, b & BV_BIT_MASK);
		*INDEX_TO_PTR(v, ai) &= ~mask;
	}
	else
	{
		// set bits in many words
		mask = BITS_TO_MASK_ABOVE(a & BV_BIT_MASK);
		*INDEX_TO_PTR(v, ai) &= ~mask;
		ai++;
		while (ai < bi)
		{
			*INDEX_TO_PTR(v, ai) = BV_ZEROES;
			ai++;
		}
		mask = BITS_TO_MASK_BELOW(b & BV_BIT_MASK);
		*INDEX_TO_PTR(v, ai) &= ~mask;
	}
	
	bv_Shrink(v);
}

// Check to see if a bit vector is empty

bool bv_IsEmpty(const BitVector *v)
{
	uint32 i;
	
	if (!v->bits) return true;
	for (i = v->lo; i < v->hi; i++)
	{
		if (v->bits[i - v->lo])
		{
			return false;
		}
	}
	return true;
}

// Expand bitvector to include the given bit

uint32 bv_IncludeBit(uint32 b, BitVector *v)
{
	uint32 lo = BIT_TO_INDEX_LO(b);
	uint32 hi = BIT_TO_INDEX_HI(b);

	if (lo < v->lo)
		bv_resize(v, lo, v->bits ? v->hi : hi);
	else if (hi > v->hi)
		bv_resize(v, v->bits ? v->lo : lo, hi);
	else if (!v->bits)
		bv_resize(v, lo, hi);
	
	lo = BIT_TO_INDEX(v, b);

	return lo;
}


// Copy v1 to v2

void bv_Copy(const BitVector *v1, BitVector *v2)
{
	bv_resize(v2, v1->lo, v1->hi);
	bv_move(v2->bits, v1->bits, v1->count);
	VALIDATE(v2);
}


// And v1 into v2

void bv_And(const BitVector *v1, BitVector *v2)
{
	uint32 i;
	uint32 lo, hi;
	
	// for AND, we usually decrease the amount of space in v2,
	// since 0 & x == 0
	lo = (v1->lo > v2->lo) ? v1->lo : v2->lo;
	hi = (v1->hi < v2->hi) ? v1->hi : v2->hi;
	bv_resize(v2, lo, hi);

	// only mask over bits in v2
	for (i = lo; i < hi; i++)
	{
		if (i >= v2->lo && i < v2->hi)
			v2->bits[i - v2->lo] &= v1->bits[i - v1->lo];
	}
	VALIDATE(v2);
}

// Subtract v1 from v2

void bv_Minus(const BitVector *v1, BitVector *v2)
{
	uint32 i;
	
	// for minus, we do not change size of v2,
	// since x & ~0 == x

	// only mask over bits in v1
	for (i = v1->lo; i < v1->hi; i++)
	{
		if (i >= v2->lo && i < v2->hi)
			v2->bits[i - v2->lo] &= ~v1->bits[i - v1->lo];
	}
	VALIDATE(v2);
}


// Or v1 into v2

void bv_Or(const BitVector *v1, BitVector *v2)
{
	uint32 i;
	uint32 lo, hi;

	// for OR, we can increase the amount of space in v2,
	// since 1 & x == 1
	lo = (v1->lo < v2->lo) ? v1->lo : v2->lo;
	hi = (v1->hi > v2->hi) ? v1->hi : v2->hi;
	bv_resize(v2, lo, hi);
	
	// only combine bits from v1
	for (i = lo; i < hi; i++)
	{
		if (i >= v1->lo && i < v1->hi)
			v2->bits[i - v2->lo] |= v1->bits[i - v1->lo];
	}
	VALIDATE(v2);
}

// Compare v1 and v2

bool bv_Compare(const BitVector *v1, const BitVector *v2)
{
	uint32 i;
	uint32 lo, hi;

	// for compare, check bits in common segments, and test that
	// nonoverlapping parts are zero
	lo = (v1->lo < v2->lo) ? v1->lo : v2->lo;
	hi = (v1->hi > v2->hi) ? v1->hi : v2->hi;
	
	for (i = lo; i < hi; i++)
	{
		if (i >= v1->lo && i < v1->hi && i >= v2->lo && i < v2->hi)
		{
			if (v2->bits[i - v2->lo] != v1->bits[i - v1->lo])
				return false;
		}
		else if (i >= v1->lo && i < v1->hi)
		{
			if (v1->bits[i - v1->lo])
				return false;
		}
		else if (i >= v2->lo && i < v2->hi)
		{
			if (v2->bits[i - v2->lo])
				return false;
		}
	}
	return true;
}

// Check if v1 if a subset of v2

bool bv_IsSubset(const BitVector *v1, const BitVector *v2)
{
	uint32 i;

	// for subset, check bits in common segments, and test that
	// nonoverlapping parts in v1 are zero
	
	for (i = v1->lo; i < v1->hi; i++)
	{
		if (i >= v2->lo && i < v2->hi)
		{
			if (v1->bits[i - v1->lo] & ~v2->bits[i - v2->lo])
				return false;
		}
		else
		{
			if (v1->bits[i - v1->lo])
				return false;
		}
	}
	return true;
}


// count of number of bits in a byte

static char bits_per_byte[256], bpb_inited = false;

static void init_bpb_array(void)
{
	int x,b,y;
	for (x = 0; x < 256; x++)
	{
		for (b = 1, y = 0; b < 256; b <<= 1)
			y += (x & b) != 0;
		bits_per_byte[x] = y;
	}
}

//	Count bits in 'bv' from byte indices [start, end)
static int bv_CountBitsInLongRange(const BitVector *bv, uint32 start, uint32 end);

static int bv_CountBitsInByteRange(const BitVector *bv, uint32 start, uint32 end)
{
	uint32 idx;
	uint32 set = 0;

	if (start < bv->lo*sizeof(BV_BITS_TYPE) || start > end || end > bv->hi*sizeof(BV_BITS_TYPE))
		abort();
	
	idx = start;
	while (idx < end)
	{
		// try to consume a word at a time		
		if ((idx & (sizeof(BV_BITS_TYPE)-1)) == 0 && 
			idx / sizeof(BV_BITS_TYPE) != end / sizeof(BV_BITS_TYPE))
		{
			set += bv_CountBitsInLongRange(bv, idx / sizeof(BV_BITS_TYPE), end / sizeof(BV_BITS_TYPE));
			idx = (end / sizeof(BV_BITS_TYPE)) * sizeof(BV_BITS_TYPE);
		}
		else
		{
			set += bits_per_byte[((uint8 *)bv->bits)[idx-bv->lo*sizeof(BV_BITS_TYPE)]];
			idx++;
		}
	}
	
	return set;
}

//	Count bits in 'bv' from bits[] indices [start, end)

static int bv_CountBitsInLongRange(const BitVector *bv, uint32 start, uint32 end)
{
	uint32 idx;
	uint32 set = 0;

	if (start < bv->lo || start > end || end > bv->hi)
		abort();
	
	idx = start;
	while (idx < end)
	{
		// try to consume a word at a time		
		if (bv->bits[idx-bv->lo] == BV_ZEROES)
		{
			// no bits set
		}
		else if (bv->bits[idx-bv->lo] == BV_ONES)
		{
			// all bits set
			set += BV_BIT_COUNT;
		}
		else
		{
			// count the bits in each byte of the element
			uint8 *ptr = (uint8 *)&bv->bits[idx-bv->lo];
			switch (BV_BIT_COUNT / 8)
			{
				case 8:
					set += bits_per_byte[ptr[4]] + bits_per_byte[ptr[5]]
						+  bits_per_byte[ptr[6]] + bits_per_byte[ptr[7]];
					// fall through
					
				case 4:
					set += bits_per_byte[ptr[0]] + bits_per_byte[ptr[1]]
						+  bits_per_byte[ptr[2]] + bits_per_byte[ptr[3]];
					break;
				
				default:
				{
					// oh my
					int cnt = sizeof(BV_BITS_TYPE);
					while (cnt--)
						set += bits_per_byte[*ptr++];
					break;
				}
			}
		}
		idx++;
	}
	
	return set;
}

//	count number of bits set in bv bit range [startbit, endbit)

int bv_CountBitsInBitRange(const BitVector *bv, uint32 startbit, uint32 endbit)
{
	uint32 idxbit;
	uint32 set = 0;
	
	if (!bpb_inited)
	{
		init_bpb_array();
		bpb_inited = true;
	}
	
	if (startbit < bv_FIRST_BIT(bv))
		startbit = bv_FIRST_BIT(bv);
	if (endbit > bv_LAST_BIT(bv))
		endbit = bv_LAST_BIT(bv);
	
	idxbit = startbit;
	
	// eat up bits up to the first byte
	while (idxbit < endbit && (idxbit & 7))
	{
		if (bv_BitSet(idxbit, bv))
			set++;
		idxbit++;
	}
	
	// eat up entire bytes
	if (idxbit + 8 < endbit)
	{
		set += bv_CountBitsInByteRange(bv, idxbit / 8, endbit / 8);
		idxbit = (endbit / 8) * 8;
	}
	
	// eat up remaining bits
	while (idxbit < endbit)
	{
		if (bv_BitSet(idxbit, bv))
			set++;
		idxbit++;
	}

/*
	// validate
	{
		long oldset = 0;
		for (idx=startbit; idx<endbit; idx++)
			oldset += bv_BitSet(idx,bv) != 0;
		if (oldset != set)
			abort();
	}
*/

	return set;
}

//	count number of bits set in bv

int bv_CountBits(const BitVector *bv)
{
	uint32 i;
	int set = 0;
	
	if (!bpb_inited)
	{
		init_bpb_array();
		bpb_inited = true;
	}

	for (i = bv->lo; i < bv->hi; i++)
	{
		BV_BITS_TYPE b;
		if ((b = bv->bits[i - bv->lo]))
		{
			if (b == BV_ONES)
				set += BV_BIT_COUNT;
			else
			{
				set += bits_per_byte[b & 0xff];
#if BV_BIT_COUNT > 8
				set += bits_per_byte[(b >> 8) & 0xff];
#if BV_BIT_COUNT > 16
				set += bits_per_byte[(b >> 16) & 0xff];
				set += bits_per_byte[(b >> 24) & 0xff];
#if BV_BIT_COUNT > 32
				set += bits_per_byte[(b >> 32) & 0xff];
				set += bits_per_byte[(b >> 40) & 0xff];
				set += bits_per_byte[(b >> 48) & 0xff];
				set += bits_per_byte[(b >> 56) & 0xff];
#if BV_BIT_COUNT > 64
#error
#endif	// 64
#endif	// 32
#endif	// 16
#endif	// 8
			}
		}		
	}
	return set;
}
