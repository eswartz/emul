/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Feb 21, 2006
 *
 */
package v9t9.engine.files;

public interface IFDRFlags {
    public final int ff_variable = 0x80;
    public final int ff_backup = 0x10;       // set by MYARC HD
    public final int ff_protected = 0x8;
    public final int ff_internal = 0x2;
    public final int ff_program = 0x1;
    public final int FF_VALID_FLAGS = ff_variable|ff_backup|ff_protected|ff_internal|ff_program;
}

