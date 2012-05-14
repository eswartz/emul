#include "utypes.h"

int	openfiad(char *filename, int *handle);
int	createfiad(char *filename, byte type, byte reclen, int *handle);
int	deletefiad(char *filename);
int	wildfiadinit(char *path, char *wildcard, char *first);
int	wildfiad(char *name);
