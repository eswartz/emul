#include "utypes.h"




int	writesector(int handle, word secnum, byte *buf);
int	readsector(int handle, word secnum, byte *buf);
void	closedisk(int handle);
int	formattrack(int handle, byte side, byte track, byte secs);
int	createdisk(char *filename, char *dn, byte tracks, byte secs,
		   byte sides, byte dens, int *handle);


int	opendisk(char *filename, struct tifile *ff);

int	getfreetisector(struct tifile *ff,int fdr);
int	freetisector(struct tifile *ff,word secnum);
int	finddoadfdr(int handle, struct fdrstruc *fdr,char *filename);
int	adddoadentry(struct tifile *ff, int secnum);
int	removedoadentry(struct tifile *ff, int secnum);




int	opendoad(char *diskname, char *filename, int *handle);
int	createdoad(char *diskname, char *filename,
		   byte type, byte reclen, int *handle);
int	deletedoad(char *diskname, char *filename);

int     initdoadptrs(int handle);
int	readdoadptrs(int handle);
int     writedoadptrs(int handle);

int	wilddoadinit(char *diskname, char *wildcard, char *first);
int	wilddoad(char *name);
