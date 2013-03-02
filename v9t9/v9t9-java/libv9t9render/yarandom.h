/*
  yarandom.h

  (c) 2010-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
#ifndef __YARANDOM_H__
#define __YARANDOM_H__

#undef random
#undef rand
#undef drand48
#undef srandom
#undef srand
#undef srand48
#undef frand
#undef RAND_MAX

#ifdef VMS
# include "vms-gtod.h"
#endif

extern unsigned int ya_random (void);
extern void ya_rand_init (unsigned int);

#define random()   ya_random()
#define RAND_MAX   0xFFFFFFFF

/*#define srandom(i) ya_rand_init(0)*/

/* Define these away to keep people from using the wrong APIs in xscreensaver.
 */
#define rand          __ERROR_use_random_not_rand_in_xscreensaver__
#define drand48       __ERROR_use_frand_not_drand48_in_xscreensaver__
#define srandom       __ERROR_do_not_call_srandom_in_xscreensaver__
#define srand         __ERROR_do_not_call_srand_in_xscreensaver__
#define srand48       __ERROR_do_not_call_srand48_in_xscreensaver__
#define ya_rand_init  __ERROR_do_not_call_ya_rand_init_in_xscreensaver__


#if defined (__GNUC__) && (__GNUC__ >= 2)
 /* Implement frand using GCC's statement-expression extension. */

# define frand(f)							\
  __extension__								\
  ({ double tmp = ((((double) random()) * ((double) (f))) /		\
		   ((double) ((unsigned int)~0)));			\
     tmp < 0 ? (-tmp) : tmp; })

#else /* not GCC2 - implement frand using a global variable.*/

static double _frand_tmp_;
# define frand(f)							\
  (_frand_tmp_ = ((((double) random()) * ((double) (f))) /		\
		  ((double) ((unsigned int)~0))),			\
   _frand_tmp_ < 0 ? (-_frand_tmp_) : _frand_tmp_)

#endif /* not GCC2 */

#endif /* __YARANDOM_H__ */
