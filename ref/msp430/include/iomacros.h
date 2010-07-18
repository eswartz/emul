/*
 * Copyright (c) 2001 Dmitry Dicky diwil@eis.ru
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS `AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 * $Id: iomacros.h,v 1.36 2008/10/09 15:00:13 sb-sf Exp $
 */

#if !defined(__IOMACROS_H_)
#define __IOMACROS_H_

#if !defined(__ASSEMBLER__)

#include <sys/inttypes.h>

#ifndef BV
	#define BV(x) (1 << (x))
#endif

#ifdef __cplusplus
#ifndef NAKED
#define NAKED(x) \
extern "C" void x(void); \
void x (void) __attribute__ ((naked)); \
void x (void)
#endif
#else
#ifndef NAKED
#define NAKED(x) \
void x (void) __attribute__ ((naked)); \
void x (void)
#endif
#endif

#define Critical __attribute__ ((critical))
#define CRITICAL __attribute__ ((critical))
#define critical __attribute__ ((critical))

#define noinit	__attribute__ ((section(".noinit"))) 
#define NOINIT	__attribute__ ((section(".noinit"))) 
#define NoInit	__attribute__ ((section(".noinit"))) 

/* Reentrant */
#define reentrant 	__attribute__ ((reentrant))
#define _REENTRANT	__attribute__ ((reentrant))

/* Save Prologues/epilogues */
#define saveprologue __attribute__ ((saveprologue))
#define SavePrologue __attribute__ ((saveprologue))
#define SAVEPROLOGUE __attribute__ ((saveprologue))

/* Reserve RAM defenition */
#define RESERVE_RAM(x)	__attribute__ ((reserve(x)))

/*
 *  Status register. Cannot do
 *	register SR asm("r2"); cause it
 *  is not general register
 */

#define WRITE_SR(x) \
	__asm__ __volatile__("mov	%0, r2" : : "r" ((uint16_t) x))

#define READ_SR \
({ \
	uint16_t __x; \
	__asm__ __volatile__( \
		"mov	r2, %0" \
		: "=r" ((uint16_t) __x) \
		:); \
	__x; \
})


/*
 *  Can do assembler assignement with r1
 *  but wrote next two for consistency
 */

#define WRITE_SP(x) \
	__asm__ __volatile__("mov %0, r1" : : "r" ((uint16_t) x))

#define READ_SP \
({ \
	uint16_t __x; \
	__asm__ __volatile__( \
		"mov	r1, %0" \
		: "=r" ((uint16_t) __x) \
		:); \
	__x; \
})

#define _BIS_SR(x)              __asm__ __volatile__("bis	%0, r2" : : "ir" ((uint16_t) x))
#define _BIC_SR(x)              __asm__ __volatile__("bic	%0, r2" : : "ir" ((uint16_t) x))

#define __bis_SR_register(x)    __asm__ __volatile__("bis	%0, r2" : : "ir" ((uint16_t) x))
#define __bic_SR_register(x)    __asm__ __volatile__("bic	%0, r2" : : "ir" ((uint16_t) x))

#if __GNUC_MINOR__ >= 4

extern void __bis_sr_irq(int);
extern void __bic_sr_irq(int);
extern void *__get_frame_address(void);

#define _BIS_SR_IRQ(x)                  __bis_sr_irq(x)
#define _BIC_SR_IRQ(x)                  __bic_sr_irq(x)

#define __bis_SR_register_on_exit(x)    __bis_sr_irq(x)
#define __bic_SR_register_on_exit(x)    __bic_sr_irq(x)

#define GET_FRAME_ADDR_F(x)	__get_frame_address()
#define __EXIT_LPM(x) do {int *p = __get_frame_address(); *p = x;} while(0)

#else
/* Only use the following two macros within interrupt functions */
#define _BIS_SR_IRQ(x) \
    __asm__ __volatile__ ( \
        "bis %0, .L__FrameOffset_" __FUNCTION__ "(r1)" \
        : : "ir" ((uint16_t) x) \
    )

#define _BIC_SR_IRQ(x) \
    __asm__ __volatile__ ( \
        "bic %0, .L__FrameOffset_" __FUNCTION__ "(r1)" \
        : : "ir" ((uint16_t) x) \
    )

#define __bis_SR_register_on_exit(x) \
    __asm__ __volatile__ ( \
        "bis %0, .L__FrameOffset_" __FUNCTION__ "(r1)" \
        : : "ir" ((uint16_t) x) \
    )

#define __bic_SR_register_on_exit(x) \
    __asm__ __volatile__ ( \
        "bic %0, .L__FrameOffset_" __FUNCTION__ "(r1)" \
        : : "ir" ((uint16_t) x) \
    )

#define GET_FRAME_ADDR(name) \
({ \
	uint16_t __x; \
	__asm__ __volatile__( \
		"mov    r1, %0\n\t" \
		"add    #.L__FrameOffset_" #name ", %0" \
		: "=r" (__x)); \
	__x; \
})


#define GET_FRAME_ADDR_F(name) \
({ \
	uint16_t __x; \
	__asm__ __volatile__( \
		"mov    r1, %0\n\t" \
		"add    #.L__FrameOffset_" name ", %0" \
		: "=r" (__x)); \
	__x; \
})


#define __EXIT_LPM(x) \
do \
{ \
	int *p = GET_FRAME_ADDR_F(__FUNCTION__); \
	*p = x; \
} while (0)

#endif

#ifdef __cplusplus
    #define sfrb_(x,x_) \
	    extern "C" volatile unsigned char x asm(#x_)
    
    #define sfrw_(x,x_) \
	    extern "C" volatile unsigned int x asm(#x_)

#if defined(__MSP430X__)
    #define sfra_(x,x_) \
	    extern "C" volatile unsigned long int x asm(#x_)
#endif
#else //__cplusplus
    #define sfrb_(x,x_) \
	    volatile unsigned char x asm(#x_)
    
    #define sfrw_(x,x_) \
	    volatile unsigned int x asm(#x_)

#if defined(__MSP430X__)
    #define sfra_(x,x_) \
	    volatile unsigned long int x asm(#x_)
#endif
#endif //__cplusplus

#define sfrb(x,x_) sfrb_(x,x_)

#define sfrw(x,x_) sfrw_(x,x_)

#if defined(__MSP430X__)
#define sfra(x,x_) sfra_(x,x_)
#endif

/***** USEFUL MACRO DEFINITIONS *********/

/* No operation */
#define nop()	__asm__ __volatile__("nop"::)

/* IAR compatibility functions */
#define _NOP()              nop()
#define __no_operation()    nop()

/* Set SFR with command. Useful for bitwise operations  */
#define SFR_CMD(cmd,s,v) \
({ \
	typedef _ts = (s); \
	__asm__ __volatile__( #cmd "	%1, %0" \
		: "=m" ((_ts)s) \
		: "Imr" ((_ts)v)); \
})

#define WRITE_PERIPHERAL_REGISTER(addr, val) \
({ \
	volatile uint8_t *__x = (uint8_t *) addr; \
	__asm__ __volatile__("mov.b\t%1, %0" \
		: "=m" ((uint8_t) *__x) \
		:  "ir" ((uint8_t) val)); \
})
		
#define READ_PERIPHERAL_REGISTER(addr) \
({ \
	volatile uint8_t *__x = (uint8_t *) addr; \
	(uint8_t)*((uint8_t *) __x); \
})

#define AND_LOW(v, i) \
({ \
	int __t; \
	__asm__ __volatile__ ( \
	"mov	%A1, %0 \n\t" \
	"and	%A2, %0" \
	: "=r" (__t) \
	: "mr" (v), \
	  "ir"   (i) \
	); \
	__t; \
})

#define AND_HI(v, i) \
({ \
	int __t; \
	__asm__ __volatile__ ( \
	"mov	%B1, %0 \n\t" \
	"and	%B2, %0" \
	: "=r" (__t) \
	: "mr" (v), \
	  "ir"   (i) \
	); \
	__t; \
})

#define AND_LOW_V(v, i) \
({ \
	int __t; \
	__asm__ __volatile__ ( \
	"mov	%A1, %0 \n\t" \
	"and	%A2, %0" \
	: "=r" (__t) \
	: "m" (v), \
	  "ir"   (i) \
	); \
	__t; \
})

#define AND_HI_V(v, i) \
({ \
	int __t; \
	__asm__ __volatile__ ( \
	"mov	%B1, %0 \n\t" \
	"and	%B2, %0" \
	: "=r" (__t) \
	: "m" (v), \
	  "ir"   (i) \
	); \
	__t; \
})

#define MARK_VOLATILE	__asm__ __volatile__("; volatile")

#endif /* not __ASSEMBLER__ */

/*
 *  Defines for assembler.
 *  Hope there is a better way to do this.
 */
#if defined(__ASSEMBLER__)

#define sfrb(x,x_) x=x_
#define sfrw(x,x_) x=x_
#if defined(__MSP430X__)
#define sfra(x,x_) x=x_
#endif

#endif

#endif /* __IOMACROS_H_ */
