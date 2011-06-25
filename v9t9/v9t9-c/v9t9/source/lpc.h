
#include "v9t9_types.h"

#include "centry.h"

extern void LPCinit();
extern void LPCstop();

int 
LPCavailable() ;
void
LPCreadEquation(u32 (*LPCfetch)(int), int forceUnvoiced);
void
LPCexec(s16 *data, u32 length);


/**	Do LPCavailable, LPCreadEquation, and LPCfetch.
 * @return 1 to continue, 0 if end of frame */
extern int LPCframe(u32 (*LPCfetch)(int count), s8 *data, u32 length);

extern void* LPCallocState();
extern int LPCstateSize();
extern void LPCgetState(void *data);
extern void LPCsetState(void *data);
extern void LPCfreeState(void *data);

#include "cexit.h"
