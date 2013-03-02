/*
  doad.h

  (c) 1991-2012 Edward Swartz

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