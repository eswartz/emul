/*******************************************************************************
 * Copyright (c) 1996, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/

/*
 * DWARF Debugging Information Format.
 */

#define TAG_padding                 0x0000
#define TAG_array_type              0x0001
#define TAG_class_type              0x0002
#define TAG_entry_point             0x0003
#define TAG_enumeration_type        0x0004
#define TAG_formal_parameter        0x0005
#define TAG_global_subroutine       0x0006
#define TAG_global_variable         0x0007
#define TAG_imported_declaration    0x0008
#define TAG_label                   0x000a
#define TAG_lexical_block           0x000b
#define TAG_local_variable          0x000c
#define TAG_member                  0x000d
#define TAG_pointer_type            0x000f
#define TAG_reference_type          0x0010
#define TAG_compile_unit            0x0011
#define TAG_source_file             0x0011
#define TAG_string_type             0x0012
#define TAG_structure_type          0x0013
#define TAG_subroutine              0x0014
#define TAG_subroutine_type         0x0015
#define TAG_typedef                 0x0016
#define TAG_union_type              0x0017
#define TAG_unspecified_parameters  0x0018
#define TAG_variant                 0x0019
#define TAG_common_block            0x001a
#define TAG_common_inclusion        0x001b
#define TAG_inheritance             0x001c
#define TAG_inlined_subroutine      0x001d
#define TAG_module                  0x001e
#define TAG_ptr_to_member_type      0x001f
#define TAG_set_type                0x0020
#define TAG_subrange_type           0x0021
#define TAG_with_stmt               0x0022
#define TAG_access_declaration      0x0023
#define TAG_base_type               0x0024
#define TAG_catch_block             0x0025
#define TAG_const_type              0x0026
#define TAG_constant                0x0027
#define TAG_enumerator              0x0028
#define TAG_file_type               0x0029
#define TAG_friend                  0x002a
#define TAG_namelist                0x002b
#define TAG_namelist_item           0x002c
#define TAG_packed_type             0x002d
#define TAG_subprogram              0x002e
#define TAG_template_type_param     0x002f
#define TAG_template_value_param    0x0030
#define TAG_thrown_type             0x0031
#define TAG_try_block               0x0032
#define TAG_variant_part            0x0033
#define TAG_variable                0x0034
#define TAG_volatile_type           0x0035
#define TAG_dwarf_procedure         0x0036
#define TAG_restrict_type           0x0037
#define TAG_interface_type          0x0038
#define TAG_namespace               0x0039
#define TAG_imported_module         0x003a
#define TAG_unspecified_type        0x003b
#define TAG_partial_unit            0x003c
#define TAG_imported_unit           0x003d
#define TAG_mutable_type            0x003e
#define TAG_condition               0x003f
#define TAG_shared_type             0x0040
#define TAG_lo_user                 0x4080
#define TAG_wrs_thrown_object       0x4080
#define TAG_wrs_throw_breakpoint    0x4081
#define TAG_wrs_catch_breakpoint    0x4082
#define TAG_wrs_extern_subroutine   0x4083
#define TAG_hi_user                 0xffff

#define CHILDREN_no                 0x00
#define CHILDREN_yes                0x01

#define FORM_ADDR                   0x0001
#define FORM_REF                    0x0002
#define FORM_BLOCK2                 0x0003
#define FORM_BLOCK4                 0x0004
#define FORM_DATA2                  0x0005
#define FORM_DATA4                  0x0006
#define FORM_DATA8                  0x0007
#define FORM_STRING                 0x0008
#define FORM_BLOCK                  0x0009
#define FORM_BLOCK1                 0x000a
#define FORM_DATA1                  0x000b
#define FORM_FLAG                   0x000c
#define FORM_SDATA                  0x000d
#define FORM_STRP                   0x000e
#define FORM_UDATA                  0x000f
#define FORM_REF_ADDR               0x0010
#define FORM_REF1                   0x0011
#define FORM_REF2                   0x0012
#define FORM_REF4                   0x0013
#define FORM_REF8                   0x0014
#define FORM_REF_UDATA              0x0015
#define FORM_INDIRECT               0x0016

#define AT_sibling                  0x0001
#define AT_location                 0x0002
#define AT_name                     0x0003
#define AT_fund_type                0x0005
#define AT_mod_fund_type            0x0006
#define AT_user_def_type            0x0007
#define AT_mod_u_d_type             0x0008
#define AT_ordering                 0x0009
#define AT_subscr_data              0x000a
#define AT_byte_size                0x000b
#define AT_bit_offset               0x000c
#define AT_bit_size                 0x000d
#define AT_element_list             0x000f
#define AT_stmt_list                0x0010
#define AT_low_pc                   0x0011
#define AT_high_pc                  0x0012
#define AT_language                 0x0013
#define AT_member                   0x0014
#define AT_discr                    0x0015
#define AT_discr_value              0x0016
#define AT_visibility               0x0017
#define AT_import                   0x0018
#define AT_string_length            0x0019
#define AT_common_reference         0x001a
#define AT_comp_dir                 0x001b
#define AT_const_value              0x001c
#define AT_constaining_type         0x001d
#define AT_default_value            0x001e
#define AT_friends                  0x001f
#define AT_inline                   0x0020
#define AT_is_optional              0x0021
#define AT_lower_bound              0x0022
#define AT_program                  0x0023
#define AT_private                  0x0024
#define AT_producer                 0x0025
#define AT_protected                0x0026
#define AT_prototyped               0x0027
#define AT_public                   0x0028
#define AT_pure_virtual             0x0029
#define AT_return_addr              0x002a
#define AT_specification_v1         0x002b
#define AT_start_scope              0x002c
#define AT_stride_size              0x002e
#define AT_upper_bound              0x002f
#define AT_virtual                  0x0030
#define AT_abstract_origin          0x0031
#define AT_accessibility            0x0032
#define AT_address_class            0x0033
#define AT_artificial               0x0034
#define AT_base_types               0x0035
#define AT_calling_convention       0x0036
#define AT_count                    0x0037
#define AT_data_member_location     0x0038
#define AT_decl_column              0x0039
#define AT_decl_file                0x003a
#define AT_decl_line                0x003b
#define AT_declaration              0x003c
#define AT_distr_list               0x003d
#define AT_encoding                 0x003e
#define AT_external                 0x003f
#define AT_frame_base               0x0040
#define AT_friend                   0x0041
#define AT_identifier_case          0x0042
#define AT_macro_info               0x0043
#define AT_namelist_info            0x0044  /* typo? item */
#define AT_priority                 0x0045
#define AT_segment                  0x0046
#define AT_specification_v2         0x0047  /* v2 */
#define AT_static_link              0x0048
#define AT_type                     0x0049
#define AT_use_location             0x004a
#define AT_variable_parameter       0x004b
#define AT_virtuality               0x004c
#define AT_vtable_elem_location     0x004d
#define AT_allocated                0x004e  /* v3 */
#define AT_associated               0x004f  /* v3 */
#define AT_mangled                  0x0050  /* v1 */
#define AT_data_location            0x0050  /* v2 */
#define AT_stride                   0x0051  /* v3 */
#define AT_entry_pc                 0x0052  /* v3 */
#define AT_use_UTF8                 0x0053  /* v3 */
#define AT_extension                0x0054  /* v3 */
#define AT_ranges                   0x0055  /* v3 */
#define AT_trampoline               0x0056  /* v3 */
#define AT_call_column              0x0057  /* v3 */
#define AT_call_file                0x0058  /* v3 */
#define AT_call_line                0x0059  /* v3 */
#define AT_description              0x005a  /* v3 */
#define AT_lo_user_v1               0x0200
#define AT_hi_user_v1               0x03ff
#define AT_push_mask                0x0220
#define AT_frame_size               0x0221
#define AT_main_unit                0x0222
#define AT_stack_use                0x0223
#define AT_source_file_names        0x0800
#define AT_source_info              0x0810
#define AT_lo_user_v2               0x2000
#define AT_wrs_options              0x2001
#define AT_hi_user_v2               0x3fff


#define OP_reg                      0x01  /* v1 */
#define OP_basereg                  0x02  /* v1 */
#define OP_addr                     0x03
#define OP_const                    0x04  /* v1 */
#define OP_deref2                   0x05  /* v1 */
#define OP_deref                    0x06
#define OP_add                      0x07  /* v1 */
#define OP_const1u                  0x08
#define OP_const1s                  0x09
#define OP_const2u                  0x0a
#define OP_const2s                  0x0b
#define OP_const4u                  0x0c
#define OP_const4s                  0x0d
#define OP_const8u                  0x0e
#define OP_const8s                  0x0f
#define OP_constu                   0x10
#define OP_consts                   0x11
#define OP_dup                      0x12
#define OP_drop                     0x13
#define OP_over                     0x14
#define OP_pick                     0x15
#define OP_swap                     0x16
#define OP_rot                      0x17
#define OP_xderef                   0x18
#define OP_abs                      0x19
#define OP_and                      0x1a
#define OP_div                      0x1b
#define OP_minus                    0x1c
#define OP_mod                      0x1d
#define OP_mul                      0x1e
#define OP_neg                      0x1f
#define OP_not                      0x20
#define OP_or                       0x21
#define OP_plus                     0x22
#define OP_plus_uconst              0x23
#define OP_shl                      0x24
#define OP_shr                      0x25
#define OP_shra                     0x26
#define OP_xor                      0x27
#define OP_bra                      0x28
#define OP_eq                       0x29
#define OP_ge                       0x2a
#define OP_gt                       0x2b
#define OP_le                       0x2c
#define OP_lt                       0x2d
#define OP_ne                       0x2e
#define OP_skip                     0x2f
#define OP_lit0                     0x30
#define OP_lit1                     0x31
#define OP_lit2                     0x32
#define OP_lit3                     0x33
#define OP_lit4                     0x34
#define OP_lit5                     0x35
#define OP_lit6                     0x36
#define OP_lit7                     0x37
#define OP_lit8                     0x38
#define OP_lit9                     0x39
#define OP_lit10                    0x3a
#define OP_lit11                    0x3b
#define OP_lit12                    0x3c
#define OP_lit13                    0x3d
#define OP_lit14                    0x3e
#define OP_lit15                    0x3f
#define OP_lit16                    0x40
#define OP_lit17                    0x41
#define OP_lit18                    0x42
#define OP_lit19                    0x43
#define OP_lit20                    0x44
#define OP_lit21                    0x45
#define OP_lit22                    0x46
#define OP_lit23                    0x47
#define OP_lit24                    0x48
#define OP_lit25                    0x49
#define OP_lit26                    0x4a
#define OP_lit27                    0x4b
#define OP_lit28                    0x4c
#define OP_lit29                    0x4d
#define OP_lit30                    0x4e
#define OP_lit31                    0x4f
#define OP_reg0                     0x50
#define OP_reg1                     0x51
#define OP_reg2                     0x52
#define OP_reg3                     0x53
#define OP_reg4                     0x54
#define OP_reg5                     0x55
#define OP_reg6                     0x56
#define OP_reg7                     0x57
#define OP_reg8                     0x58
#define OP_reg9                     0x59
#define OP_reg10                    0x5a
#define OP_reg11                    0x5b
#define OP_reg12                    0x5c
#define OP_reg13                    0x5d
#define OP_reg14                    0x5e
#define OP_reg15                    0x5f
#define OP_reg16                    0x60
#define OP_reg17                    0x61
#define OP_reg18                    0x62
#define OP_reg19                    0x63
#define OP_reg20                    0x64
#define OP_reg21                    0x65
#define OP_reg22                    0x66
#define OP_reg23                    0x67
#define OP_reg24                    0x68
#define OP_reg25                    0x69
#define OP_reg26                    0x6a
#define OP_reg27                    0x6b
#define OP_reg28                    0x6c
#define OP_reg29                    0x6d
#define OP_reg30                    0x6e
#define OP_reg31                    0x6f
#define OP_breg0                    0x70
#define OP_breg1                    0x71
#define OP_breg2                    0x72
#define OP_breg3                    0x73
#define OP_breg4                    0x74
#define OP_breg5                    0x75
#define OP_breg6                    0x76
#define OP_breg7                    0x77
#define OP_breg8                    0x78
#define OP_breg9                    0x79
#define OP_breg10                   0x7a
#define OP_breg11                   0x7b
#define OP_breg12                   0x7c
#define OP_breg13                   0x7d
#define OP_breg14                   0x7e
#define OP_breg15                   0x7f
#define OP_breg16                   0x80
#define OP_breg17                   0x81
#define OP_breg18                   0x82
#define OP_breg19                   0x83
#define OP_breg20                   0x84
#define OP_breg21                   0x85
#define OP_breg22                   0x86
#define OP_breg23                   0x87
#define OP_breg24                   0x88
#define OP_breg25                   0x89
#define OP_breg26                   0x8a
#define OP_breg27                   0x8b
#define OP_breg28                   0x8c
#define OP_breg29                   0x8d
#define OP_breg30                   0x8e
#define OP_breg31                   0x8f
#define OP_regx                     0x90
#define OP_fbreg                    0x91
#define OP_bregx                    0x92
#define OP_piece                    0x93
#define OP_deref_size               0x94
#define OP_xderef_size              0x95
#define OP_nop                      0x96
#define OP_push_object_address      0x97
#define OP_call2                    0x98
#define OP_call4                    0x99
#define OP_calli                    0x9a  /* typo? */
#define OP_ref                      0x9a
#define OP_call_ref                 0x9a
#define OP_bit_piece                0x9d
#define OP_lo_user                  0xe0
#define OP_hi_user                  0xff

#define FT_char                     0x0001
#define FT_signed_char              0x0002
#define FT_unsigned_char            0x0003
#define FT_short                    0x0004
#define FT_signed_short             0x0005
#define FT_unsigned_short           0x0006
#define FT_integer                  0x0007
#define FT_signed_integer           0x0008
#define FT_unsigned_integer         0x0009
#define FT_long                     0x000a
#define FT_signed_long              0x000b
#define FT_unsigned_long            0x000c
#define FT_pointer                  0x000d
#define FT_float                    0x000e
#define FT_dbl_prec_float           0x000f
#define FT_ext_prec_float           0x0010
#define FT_complex                  0x0011
#define FT_dbl_prec_complex         0x0012
#define FT_void                     0x0014
#define FT_boolean                  0x0015
#define FT_ext_prec_complex         0x0016
#define FT_label                    0x0017
#define FT_lo_user                  0x8000
#define FT_hi_user                  0xffff
#define FT_longlong                 0x8008
#define FT_signed_longlong          0x8108
#define FT_unsigned_longlong        0x8208
#define FT_vector_signed_char       0xa002
#define FT_vector_unsigned_char     0xa003
#define FT_vector_signed_short      0xa005
#define FT_vector_unsigned_short    0xa006
#define FT_vector_signed_int        0xa008
#define FT_vector_unsigned_int      0xa009
#define FT_vector_float             0xa00e
#define FT_ev64_s16                 0xb005
#define FT_ev64_u16                 0xb006
#define FT_ev64_s32                 0xb008
#define FT_ev64_u32                 0xb009
#define FT_ev64_s64                 0xb208
#define FT_ev64_u64                 0xb209
#define FT_ev64_fs                  0xb00e
#define FT_ev64_opaque              0xb020

#define MOD_pointer_to              0x01
#define MOD_reference_to            0x02
#define MOD_const                   0x03
#define MOD_volatile                0x04
#define MOD_lo_user                 0x80
#define MOD_hi_user                 0xff

#define LANG_C89                    0x00000001
#define LANG_C                      0x00000002
#define LANG_ADA83                  0x00000003
#define LANG_C_PLUS_PLUS            0x00000004
#define LANG_COBOL74                0x00000005
#define LANG_COBOL85                0x00000006
#define LANG_FORTRAN77              0x00000007
#define LANG_FORTRAN90              0x00000008
#define LANG_PASCAL83               0x00000009
#define LANG_MODULA2                0x0000000a
#define LANG_JAVA                   0x0000000b  /* v3 */
#define LANG_C99                    0x0000000c  /* v3 */
#define LANG_ADA95                  0x0000000d  /* v3 */
#define LANG_FORTRAN95              0x0000000e  /* v3 */
#define LANG_PLI                    0x0000000f
#define LANG_lo_user                0x00008000
#define LANG_hi_user                0x0000ffff

#define ORD_row_major               0
#define ORD_col_major               1

#define FMT_FT_C_C                  0x0
#define FMT_FT_C_X                  0x1
#define FMT_FT_X_C                  0x2
#define FMT_FT_X_X                  0x3
#define FMT_UT_C_C                  0x4
#define FMT_UT_C_X                  0x5
#define FMT_UT_X_C                  0x6
#define FMT_UT_X_X                  0x7
#define FMT_ET                      0x8

#define ATE_address                 0x01
#define ATE_boolean                 0x02
#define ATE_complex_float           0x03
#define ATE_float                   0x04
#define ATE_signed                  0x05
#define ATE_signed_char             0x06
#define ATE_unsigned                0x07
#define ATE_unsigned_char           0x08
#define ATE_imaginary_float         0x09  /* v3 */
#define ATE_lo_user                 0x80
#define ATE_hi_user                 0xff

#define DW_LNS_copy                 1
#define DW_LNS_advance_pc           2
#define DW_LNS_advance_line         3
#define DW_LNS_set_file             4
#define DW_LNS_set_column           5
#define DW_LNS_negate_stmt          6
#define DW_LNS_set_basic_block      7
#define DW_LNS_const_add_pc         8
#define DW_LNS_fixed_advance_pc     9
#define DW_LNS_set_prologue_end     0xa  /* v3 */
#define DW_LNS_set_epilogue_begin   0xb  /* v3 */
#define DW_LNS_set_isa              0xc  /* v3 */
#define DW_LNS_expected_opcode_base 0xd  /* highest standard opcode plus one */
#define DW_LNS_vendor_extension     0x100
#define DW_LNS_special_opcode       0x101

#define DW_LNE_end_sequence         1
#define DW_LNE_set_address          2
#define DW_LNE_define_file          3
#define DW_LNE_lo_user              0x80  /* v3 */
#define DW_LNE_hi_user              0xff  /* v3 */

#define ACCESS_public               1
#define ACCESS_protected            2
#define ACCESS_private              3

#define VIS_local                   1
#define VIS_exported                2
#define VIS_qualified               3

#define VIRTUALITY_none             0
#define VIRTUALITY_virtual          1
#define VIRTUALITY_pure_virtual     2

#define ID_case_sensitive           0
#define ID_up_case                  1
#define ID_down_case                2
#define ID_case_insensitive         3

#define CC_normal                   0x01
#define CC_program                  0x02
#define CC_nocall                   0x03
#define CC_lo_user                  0x40
#define CC_hi_user                  0xff

#define INL_not_inlined             0
#define INL_inlined                 1
#define INL_declared_not_inlined    2
#define INL_declared_inlined        3

#define DSC_label                   0
#define DSC_range                   1

#define MACINFO_define              1
#define MACINFO_undef               2
#define MACINFO_start_file          3
#define MACINFO_end_file            4
#define MACINFO_vendor_ext          0xff

/* The following three defines represent */
/* the high 2 bits only.                 */
#define CFA_advance_loc             0x01
#define CFA_offset                  0x02
#define CFA_restore                 0x03

#define CFA_nop                     0x00
#define CFA_set_loc                 0x01
#define CFA_advance_loc1            0x02
#define CFA_advance_loc2            0x03
#define CFA_advance_loc4            0x04
#define CFA_offset_extended         0x05
#define CFA_restore_extended        0x06
#define CFA_undefined               0x07
#define CFA_same_value              0x08
#define CFA_register                0x09
#define CFA_remember_state          0x0a
#define CFA_restore_state           0x0b
#define CFA_def_cfa                 0x0c
#define CFA_def_cfa_register        0x0d
#define CFA_def_cfa_offset          0x0e
#define CFA_def_cfa_expression      0x0f
#define CFA_expression              0x10  /* v3 */
#define CFA_offset_extended_sf      0x11  /* v3 */
#define CFA_def_cfa_sf              0x12  /* v3 */
#define CFA_def_cfa_offset_sf       0x13  /* v3 */
#define CFA_lo_user                 0x1c
#define CFA_hi_user                 0x3f


#define ADDR_none                   0
#define ADDR_near16                 1
#define ADDR_far16                  2
#define ADDR_huge16                 3
#define ADDR_near32                 4
#define ADDR_far32                  5


