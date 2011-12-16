/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.tcf.services;

import java.util.Map;

import org.eclipse.tm.tcf.protocol.IService;
import org.eclipse.tm.tcf.protocol.IToken;

/**
 * TCF Disassembly service interface.
 */

public interface IDisassembly extends IService {

    static final String NAME = "Disassembly";

    static final String
        /** The name of the instruction set architecture, String */
        CAPABILITY_ISA = "ISA",

        /** If true, simplified mnemonics are supported or requested, Boolean */
        CAPABILITY_SIMPLIFIED = "Simplified",

        /** If true, pseudo-instructions are supported or requested, Boolean */
        CAPABILITY_PSEUDO = "Pseudo";

    /**
     * Retrieve disassembly service capabilities a given context-id.
     * @param context_id - a context ID, usually one returned by Run Control or Memory services.
     * @param done - command result call back object.
     * @return - pending command handle.
     */
    IToken getCapabilities(String context_id, DoneGetCapabilities done);

    /**
     * Call back interface for 'getCapabilities' command.
     */
    interface DoneGetCapabilities {
        /**
         * Called when capabilities retrieval is done.
         * @param token - command handle.
         * @param error - error object or null.
         * @param capabilities - array of capabilities, see CAPABILITY_* for contents of each array element.
         */
        void doneGetCapabilities(IToken token, Throwable error, Map<String,Object>[] capabilities);
    }

    /**
     * Disassemble instruction code from a specified range of memory addresses, in a specified context.
     * @param context_id - a context ID, usually one returned by Run Control or Memory services.
     * @param addr - address of first instruction to disassemble.
     * @param size - size in bytes of the address range.
     * @param params - properties to control the disassembly output, an element of capabilities array, see getCapabilities.
     * @param done - command result call back object.
     * @return - pending command handle.
     */
    IToken disassemble(String context_id, Number addr, int size, Map<String,Object> params, DoneDisassemble done);

    /**
     * Call back interface for 'disassemble' command.
     */
    interface DoneDisassemble {
        /**
         * Called when disassembling is done.
         * @param token - command handle.
         * @param error - error object or null.
         * @param disassembly - array of disassembly lines.
         */
        void doneDisassemble(IToken token, Throwable error, IDisassemblyLine[] disassembly);
    }

    /**
     * Interface to represent a single disassembly line.
     */
    interface IDisassemblyLine {

        /**
         * @return instruction address.
         */
        Number getAddress();

        /**
         * @return instruction size in bytes.
         */
        int getSize();

        /**
         * @return array of instruction fields, each field is a collection of field properties, see FIELD_*.
         */
        Map<String,Object>[] getInstruction();
    }

    /** Instruction field properties */
    static final String
        /** The type of the instruction field. See FTYPE_*, String. */
        FIELD_TYPE = "Type",

        /** Value of the field for “String” and “Register” types, String. */
        FIELD_TEXT = "Text",

        /** Value of the field for “Address,” “Displacement,” or “Immediate” types, Number. */
        FIELD_VALUE = "Value",

        /** Context ID of the address space used with “Address” types, String. */
        FIELD_ADDRESS_SPACE = "AddressSpace";

    /** Instruction field types */
    static final String
        FTYPE_STRING = "String",
        FTYPE_Register = "Register",
        FTYPE_ADDRESS = "Address",
        FTYPE_DISPLACEMENT = "Displacement",
        FTYPE_IMMEDIATE = "Immediate";

}
