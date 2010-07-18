/*
 * $Id: iostructures.h,v 1.11 2008/10/09 15:00:14 sb-sf Exp $  
 */
#ifndef __ASSEMBLER__

#ifndef  __IOSTRUCTURES_H__
#define __IOSTRUCTURES_H__

/*
 * This declares some usefull structures 
 * which describe IO ports.
 * (c) 2002, 2003  diwil@mail.ru
 */

#ifdef MSP430_BITFIELDS_UNSIGNED
#define __MSP430_UNS__
#else
#define __MSP430_UNS__ unsigned
#endif

#ifndef __MSP430_PORT_INIT_
#define __VOLATILE volatile
#else
#define __VOLATILE
#endif

typedef union port {
  __VOLATILE __MSP430_UNS__ char reg_p;
  __VOLATILE struct {
    __MSP430_UNS__ char __p0:1,
      __p1:1,
      __p2:1,
      __p3:1,
      __p4:1,
      __p5:1,
      __p6:1,
      __p7:1;
  } __pin;
} __attribute__ ((packed)) ioregister_t;

#undef __MSP430_UNS__

#define pin0    __pin.__p0
#define pin1    __pin.__p1
#define pin2    __pin.__p2
#define pin3    __pin.__p3
#define pin4    __pin.__p4
#define pin5    __pin.__p5
#define pin6    __pin.__p6
#define pin7    __pin.__p7

#define inport  in.reg_p
#define outport out.reg_p
#define dirport dir.reg_p
#define ifgport ifg.reg_p
#define iesport ies.reg_p
#define ieport  ie.reg_p
#define selport sel.reg_p

#define IO_DIRPIN_INPUT         0       /* direction input */
#define IO_DIRPIN_OUTPUT        1       /* direction output */

#define IO_DIRPORT_INPUT        0
#define IO_DIRPORT_OUTPUT       0xff

#define IO_IESPIN_RISING        0       /* interrupt edge on low-to-high transition (front edge, rising edge) */
#define IO_IESPIN_FALLING       1       /* -"- -"- high-to-low */

#define IO_IESPORT_RISING       0
#define IO_IESPORT_FALLING      0xff

#define IO_IRQPIN_DISABLE       0       /* interrupt request disabled */
#define IO_IRQPIN_ENABLE        1       /* -"- -"- enabled*/

#define IO_IRQPORT_DISABLE      0
#define IO_IRQPORT_ENABLE       0xff

#define IO_PINPIN_SELECT        0       /* pin 'pin' function select */
#define IO_ALTPIN_SELECT        1       /* alternative function select */

#define IO_PINPORT_SELECT       0
#define IO_ALTPORT_SELECT       0xff


#ifdef __cplusplus
#define __MSP430_EXTERN__ extern "C"
#else
#define __MSP430_EXTERN__
#endif //__cplusplus

/****************************************************************/
#if defined(__MSP430_HAS_PORT0__)
struct port0_t {
  ioregister_t	in;	/* Input */
  ioregister_t	out;	/* Output */
  ioregister_t	dir;	/* Direction */
  ioregister_t	ifg;	/* Interrupt Flag */
  ioregister_t	ies;	/* Interrupt Edge Select */
  ioregister_t	ie;	/* Interrupt Enable */
};

__MSP430_EXTERN__ struct port0_t port0 asm("0x0010");
#endif

/****************************************************************/
#if defined(__MSP430_HAS_PORT1__) || defined(__MSP430_HAS_PORT2__) \
 || defined(__MSP430_HAS_PORT1_R__) || defined(__MSP430_HAS_PORT2_R__)
struct port_full_t {
  ioregister_t	in;     /* Input */
  ioregister_t	out;    /* Output */
  ioregister_t	dir;    /* Direction */
  ioregister_t	ifg;    /* Interrupt Flag */
  ioregister_t	ies;    /* Interrupt Edge Select */
  ioregister_t	ie;     /* Interrupt Enable */
  ioregister_t	sel;    /* Selection */
#if defined(__MSP430_HAS_PORT1_R__) || defined(__MSP430_HAS_PORT2_R__)
  ioregister_t ren;     /* Pull up or down resistor enable */
#endif
};
#endif

#if defined(__MSP430_HAS_PORT3__) || defined(__MSP430_HAS_PORT4__) \
 || defined(__MSP430_HAS_PORT5__) || defined(__MSP430_HAS_PORT6__) \
 || defined(__MSP430_HAS_PORT3_R__) || defined(__MSP430_HAS_PORT4_R__) \
 || defined(__MSP430_HAS_PORT5_R__) || defined(__MSP430_HAS_PORT6_R__)
struct port_simple_t {
  ioregister_t	in;     /* Input */
  ioregister_t	out;    /* Output */
  ioregister_t	dir;    /* Direction */
  ioregister_t	sel;    /* Selection */
};
#endif

/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
#if defined(__MSP430_HAS_PORT1__)  ||  defined(__MSP430_HAS_PORT1_R__)
__MSP430_EXTERN__ struct port_full_t port1 asm("0x0020");
#endif

#if defined(__MSP430_HAS_PORT2__)  ||  defined(__MSP430_HAS_PORT2_R__)
__MSP430_EXTERN__ struct port_full_t port2 asm("0x0028");
#endif

#if defined(__MSP430_HAS_PORT3__)  ||  defined(__MSP430_HAS_PORT3_R__)
__MSP430_EXTERN__ struct port_simple_t port3 asm("0x0018");
#endif

#if defined(__MSP430_HAS_PORT4__)  ||  defined(__MSP430_HAS_PORT4_R__)
__MSP430_EXTERN__ struct port_simple_t port4 asm("0x001c");
#endif

#if defined(__MSP430_HAS_PORT5__)  ||  defined(__MSP430_HAS_PORT5_R__)
__MSP430_EXTERN__ struct port_simple_t port5 asm("0x0030");
#endif

#if defined(__MSP430_HAS_PORT6__)  ||  defined(__MSP430_HAS_PORT6_R__)
__MSP430_EXTERN__ struct port_simple_t port6 asm("0x0034");
#endif

#undef __MSP430_EXTERN__

#ifdef __MSP430_PORT_INIT_
#undef __VOLATILE
#endif


#endif /* __IOSTRUCTURES_H__ */

#endif /* __ASSEMBLER__ */

