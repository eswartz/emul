typedef	unsigned int	word;
typedef unsigned char	byte;




#define	WARNING(s) {	\
			fprintf(stderr,"Warning:  %s (%d): %s\n",inname,linenum,s); \
			if (list) \
			fprintf(list,"Warning:  %s (%d): %s\n",inname,linenum,s); \
			warnings++; \
		   }

#define	ERROR(s,v,r) {	\
			sprintf(em,s,v); \
			fprintf(stderr,"Error:  %s (%d): %s\n",inname,linenum,em); \
			if (list) \
			fprintf(list,"Error:  %s (%d): %s\n",inname,linenum,em); \
			errors++; \
			return r; \
		   }



#define	FATAL(s) {	\
			fprintf(stderr,"Fatal:  %s (%d): %s\n",inname,linenum,s); \
			if (list) \
			fprintf(list,"Fatal:  %s (%d): %s\n",inname,linenum,s); \
			return 1; \
		 }
