/*	V9t9: the TI Emulator! v6.0 Source 
	Copyright (c) 1996 by Edward Swartz
*/
/*
	Modules list manager.

	Very simple program which will let you tag up to 32 modules
	from the MODULES.INF file, and will set the variable in the
	V9t9.CNF file.
*/


#include <alloc.h>
#include <stdio.h>
#include <conio.h>
#include <memory.h>
#include <stdlib.h>
#include <string.h>
#include <sys\stat.h>

#include "param.h"
#include "dosfiles.h"
#include "config.h"

#include "select.h"

typedef	unsigned char byte;
typedef	unsigned int word;
typedef unsigned long longint;

char	configfilename[80];
char	modulesfilename[80];


void	losespaces(char *oln)
{
	char	tmp[128];
	char	*tp;
	char	*ln;

	ln=oln;
	tp=tmp;
	*tp=0;
	while (*ln)
	{
		if (!(*ln==' ' || *ln==9))
			*tp++=*ln;
		ln++;
	}
	*tp=0;
	strcpy(oln,tmp);
}


void	losefrontspaces(char *oln)
{
	char	tmp[128];
	char	*tp;
	char	*ln;

	strcpy(tmp,oln);

	tp=tmp;
	while (*tp && (*tp==' ' || *tp==9))
		tp++;
	strcpy(oln,tp);
}


byte	match(char *word)
{
static	struct
	{
		char 	word[9];
		byte    val;
	}	words[]=
	{{"ROM",	M_ROM1 },
	{"ROM1",M_ROM1 },
	{"ROM2",M_ROM2 },
	{"BANKED",M_ROM1|M_ROM2 },
	{"GROM",M_GROM },
	{"MMRAM", M_MINI },
	{"", 0}};

	int	index;

	index=0;
	while (*words[index].word)
		if (stricmp(word,words[index].word)==0)
			return words[index].val;
		else
			index++;

	return 0;
}


int	parse(char *ln, struct modrec *m)
{
	char	*ptr;
	char	modifier[12];
	byte	val;

	memset(m,0,sizeof(struct modrec));

	if (*ln++!='"')
		return 0;
	ptr=strchr(ln,'"');
	if (ptr==NULL)
		return 0;
	*ptr=0;
	strcpy(m->title,ln);
	ln=ptr+2;

	losespaces(ln);
	strcat(ln,",");

	ptr=strchr(ln,',');
	if (ptr==NULL)
		return 0;
	*ptr=0;
	strcpy(m->basename,ln);
	ln=ptr+1;

	do
	{
		ptr=strchr(ln,',');
		if (ptr)
		{
			*ptr=0;
			strcpy(modifier,ln);
			val=match(modifier);
			if (!val)
				return 0;
			m->opts|=val;
			ln=ptr+1;
		}
	}	while (ptr);

	return	1;
}


int	stripln(char *ln)
{
	char	*ptr;

	ptr=ln+strlen(ln)-1;
	while (ptr>=ln && (*ptr=='\n' || *ptr=='\r'))
		*ptr--=0;

	return 0;
}

int	readmodulesfile(void)
{
	char	line[128];
	FILE	*i;
	struct	modrec temp;

	mods=farcalloc(MAXMODS,sizeof(struct modrec));

	i=fopen(modulesfilename,"r");
	if (i==NULL)
		return 0;

	nummods=0;
	while (!feof(i) && nummods<MAXMODS)
	{
		fgets(line,128,i);
		if (*(line+strlen(line)-1)!='\n')
			return 1;
		stripln(line);
		losefrontspaces(line);
		if (*line)
		{
			if (!parse(line,&temp))
			{
				fclose(i);
				return 0;
			}
			_fmemcpy(&mods[nummods],&temp,sizeof(temp));
			nummods++;
		}
	}
	fclose(i);
	return 1;
}





int	clearmodlist(void)
{
	memset(selected,0,sizeof(selected));

	return 0;
}

int	getmodlist(char *buf)
{
	int	index;


	index=0;
	while (*buf && index<32)
	{
		selected[index]=atoi(buf);
		buf=strchr(buf,',');
		if (*buf)
			buf++;
		if (selected[index]<nummods)
			index++;
	}
	selected[index]=0;
	return	0;
}


int	makemodlist(char *buf)
{
	int	index;

	strcpy(buf,"");
	index=0;
	while (selected[index])
	{
		buf+=sprintf(buf,"%d",selected[index]);
		index++;
		if (selected[index])
			buf+=sprintf(buf,",");
	}
	return	0;
}


int	checkexists(void)
{
	int	index;
	char	modpath[64];
	char	filename[80];
	char	ext[6];
	int	which;

	struct	modrec m;
        struct	stat st;

	int	any=0;

	if (!getvar(configfilename,"ModulesPath",modpath))
		strcpy(modpath,".\\");

	if (!getvar(configfilename,"DefaultModuleExtension",ext))
		strcpy(ext,"HEX");

	makedirof(modpath);

	for (index=0; index<nummods; index++)
	{
		_fmemcpy(&m,&mods[index],sizeof(struct modrec));
		m.exist=0;
		for (which=1; which<8; which+=which)
			if (m.opts&which)
			{
				strcpy(filename,modpath);
				strcat(filename,m.basename);
				strcat(filename,
				(which==1 ? "C." :
				(which==2 ? "D." : "G.")));
				strcat(filename,ext);

				if (!stat(filename,&st))
				{
					m.exist|=which;
					any=1;
				}
			}
		_fmemcpy(&mods[index],&m,sizeof(struct modrec));
	}

	return any;
}



void	help(void)
{
	printf("\n"
	       "MODULES V9t9 Module Startup List Manager v1.0\n"
	       "\n"
	       "MODULES will read the module database, interactively let the\n"
	       "user tag modules which will appear in the V9t9 startup list, and\n"
	       "update the V9t9 configuration file.\n"
	       "\n"
	       "Usage:\n"
	       "\n"
	       "MODULES [options]\n"
	       "\n"
	       "Options:\t/Cxxx\t-- specify configuration file (default: V9t9.CNF)\n"
	       "\t\t/Mxxx\t-- specify modules database (default: MODULES.INF)\n"
	       "\n"
	       );
	exit(1);
}


void	main(int argc, char **argv)
{
	char	drive[4];
	char	path[64];
	char	name[10];
	char	ext[6];
	char	*sptr;

	char	ourpath[80];
	char	temp[80];
	char	opt;

	struct	stat st;

	char	modlist[128];

	if (paraminit(0,temp)!=0)
		help();

	_fullpath(ourpath,argv[0],80);
	sptr=strrchr(ourpath,'\\');
	*(sptr+1)=0;

	strcpy(configfilename,ourpath);
	strcpy(modulesfilename,ourpath);
	strcat(configfilename,"V9t9.CNF");
	strcat(modulesfilename,"MODULES.INF");

	while ((opt=getopt())!=0)
	{
		switch	(opt)
		{
		case 'C':
			getoptstr(configfilename,1);
			break;
		case 'M':
			getoptstr(modulesfilename,1);
			break;
		case '?':
		case 'H':
			help();
			break;
		default:
			printf("Illegal option:  /%c\n",opt);
			exit(1);
		}
	}

	if (stat(configfilename,&st) || stat(modulesfilename,&st))
	{
		printf("Error:\n"
		"One of the configuration files %s\n"
		"or %s cannot be found.\n"
		"Make sure you're running MODULES from the directory where\n"
		"these files exist, or run MODULES with the /C and/or /M options.\n",
		configfilename,modulesfilename);
		exit(1);
	}


	if (!readmodulesfile())
	{
		printf("Error:\n"
		"%s has some invalid entries in it.\n",
		modulesfilename);
		exit(1);
	}

	if (!checkexists())
	{
		printf("Error:\n"
		"No module images referenced in %s\n"
		"appear to exist!  Be sure the ModulesPath variable in\n"
		"%s is correct.\n",modulesfilename,configfilename);

		exit(1);
	}

	clearmodlist();
	if (getvar(configfilename,"Modules",modlist))
		getmodlist(modlist);

	if (getvar(configfilename,"DefaultModule",temp))
		defaultmodule=atoi(temp);
	else
		defaultmodule=1;

	if (!select())
		exit(0);

	makemodlist(modlist);
	if (!changevar(configfilename,"Modules",modlist))
		printf("\n\nError!\n"
		       "Couldn't change \"Modules\" entry in %s!\n",
		       configfilename);

	sprintf(temp,"%d",defaultmodule);
	changevar(configfilename,"DefaultModule",temp);
}
