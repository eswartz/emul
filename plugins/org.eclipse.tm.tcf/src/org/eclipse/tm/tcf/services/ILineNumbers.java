/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.tcf.services;

import org.eclipse.tm.tcf.protocol.IService;
import org.eclipse.tm.tcf.protocol.IToken;

/**
 * Line numbers service associates locations in the source files with the corresponding
 * machine instruction addresses in the executable object.
 */
public interface ILineNumbers extends IService {

    static final String NAME = "LineNumbers";
    
    /**
     * TextArea represent a continues area in source text mapped to
     * continues range of code addresses.
     * Line and columns are counted starting from 0.
     * File name can be relative path, in such case client should 
     * use TextArea directory name as origin for the path.
     * File and directory names are valid on a host where code was compiled.
     * It is client responsibility to map names to this host file system.    
     */
    final class CodeArea {
        public final String directory;
        public final String file;
        public final int start_line;
        public final int start_column;
        public final int end_line;
        public final int end_column;
        public final Number start_address;
        public final Number end_address;
        public final int isa;
        public final boolean is_statement;
        public final boolean basic_block;
        public final boolean prologue_end;
        public final boolean epilogue_begin;
        
        public CodeArea(String directory, String file, int start_line, int start_column,
                int end_line, int end_column, Number start_address, Number end_address, int isa,
                boolean is_statement, boolean basic_block,
                boolean prologue_end, boolean epilogue_begin) {
            this.directory = directory;
            this.file = file;
            this.start_line = start_line;
            this.start_column = start_column;
            this.end_line = end_line;
            this.end_column = end_column;
            this.start_address = start_address;
            this.end_address = end_address;
            this.isa = isa;
            this.is_statement = is_statement;
            this.basic_block = basic_block;
            this.prologue_end = prologue_end;
            this.epilogue_begin = epilogue_begin;
        }
    }
    
    IToken mapToSource(String context_id, Number start_address, Number end_address, DoneMapToSource done);
    
    interface DoneMapToSource {
        void doneMapToSource(IToken token, Exception error, CodeArea[] areas);
    }
}
