;
;	Equates that f9900rom.f99 needs.
;
;	These are in assembly syntax.  A post-assembly build step (EquatesToConstants)
;	converts them into f9900gromequs.fi for use by the following 
;	FORTH ROM build step.
;

	aorg				>0

grom_sig			bss 2		; FORTH: >grom_sig
grom_dict			bss	2		; FORTH: >grom_dict
grom_dictend		bss	2		; FORTH: >grom_dictend
grom_logo			bss 2		; FORTH: >grom_logo
grom_kbdlist		bss 2		; FORTH: >grom_kbdlist
grom_font8x8		bss 2		; FORTH: >grom_font8x8
grom_font5x6		bss 2		; FORTH: >grom_font5x6
grom_errptrs		bss 2		; FORTH: >grom_errptrs
grom_editorhelp		bss 2		; FORTH: >grom_editorhelp
grom_menuhelp		bss 2		; FORTH: >grom_menuhelp

gram_dict_base		equ	>4000  	; FORTH: >gram_dict_base
gram_dict_end		equ	>8000  	; FORTH: >gram_dict_end
gram_disk_base		equ	>8000  	; FORTH: >gram_disk_base

