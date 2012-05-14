
#ifndef	__ERROR__
extern	int Error;
#endif

void	die(char *file);
void	tierror(char *file);


#define	BADPATH	1
#define	NOTTIEMUL 2
#define BADFILE 3
#define NOSPACE 4
#define	NOTIFILE 5
#define BADSEEK 6
#define BADREAD 7
#define BADDISK 8
#define	EXISTS 9
#define	NOMEMORY 10
#define	INCOMPAT 11
#define BADSIDE 12
#define NOFILE 13
