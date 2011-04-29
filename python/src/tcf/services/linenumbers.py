# *******************************************************************************
# * Copyright (c) 2011 Wind River Systems, Inc. and others.
# * All rights reserved. This program and the accompanying materials
# * are made available under the terms of the Eclipse Public License v1.0
# * which accompanies this distribution, and is available at
# * http://www.eclipse.org/legal/epl-v10.html
# *
# * Contributors:
# *     Wind River Systems - initial API and implementation
# *******************************************************************************

"""
Line numbers service associates locations in the source files with the corresponding
machine instruction addresses in the executable object.
"""

import exceptions
from tcf import services

NAME = "LineNumbers"

class CodeArea(object):
    """
    A CodeArea represents a continues area in source text mapped to
    continues range of code addresses.
    Line and columns are counted starting from 1.
    File name can be a relative path, in this case the client should
    use the CodeArea directory name as origin for the path.
    File and directory names are valid on a host where code was compiled.
    It is client responsibility to map names to local host file system.
    """
    def __init__(self, directory, file, start_line, start_column,
            end_line, end_column, start_address, end_address, isa,
            is_statement, basic_block, prologue_end, epilogue_begin):
        self.directory = directory
        self.file = file
        self.start_line = start_line
        self.start_column = start_column
        self.end_line = end_line
        self.end_column = end_column
        self.start_address = start_address
        self.end_address = end_address
        self.isa = isa
        self.is_statement = is_statement
        self.basic_block = basic_block
        self.prologue_end = prologue_end
        self.epilogue_begin = epilogue_begin

    def __eq__(self, o):
        if self == o: return True
        if not isinstance(o, CodeArea): return False
        if self.start_line != o.start_line: return False
        if self.start_column != o.start_column: return False
        if self.end_line != o.end_line: return False
        if self.end_column != o.end_column: return False
        if self.isa != o.isa: return False
        if self.is_statement != o.is_statement: return False
        if self.basic_block != o.basic_block: return False
        if self.prologue_end != o.prologue_end: return False
        if self.epilogue_begin != o.epilogue_begin: return False
        if self.start_address != o.start_address: return False
        if self.end_address != o.end_address: return False
        if self.file != o.file: return False
        if self.directory != o.directory: return False
        return True

    def __hash__(self):
        h = 0
        if file: h += hash(file)
        return h + self.start_line + self.start_column + self.end_line + self.end_column

    def __str__(self):
        import cStringIO
        bf = cStringIO.StringIO()
        bf.write('[')
        if self.directory:
            bf.write(self.directory)
            bf.write(':')
        if self.file:
            bf.write(self.file)
            bf.write(':')
        bf.write(str(self.start_line))
        if self.start_column:
            bf.write('.')
            bf.write(self.start_column)
        bf.write("..")
        bf.write(self.end_line)
        if self.end_column:
            bf.write('.')
            bf.write(self.end_column)
        bf.write(" -> ")
        if self.start_address:
            bf.write("0x")
            bf.write(hex(self.start_address))
        else:
            bf.write('0')
        bf.write("..")
        if self.end_address:
            bf.write("0x")
            bf.write(hex(self.end_address))
        else:
            bf.write('0')
        if self.isa:
            bf.write(",isa ")
            bf.write(self.isa)
        if self.is_statement:
            bf.write(",statement")
        if self.basic_block:
            bf.write(",basic block")
        if self.prologue_end:
            bf.write(",prologue end")
        if self.epilogue_begin:
            bf.write(",epilogue begin")
        bf.write(']')
        return bf.getvalue()

class LineNumbersService(services.Service):
    def getName(self):
        return NAME

    def mapToSource(self, context_id, start_address, end_address, done):
        raise exceptions.NotImplementedError("Abstract method")

    def mapToMemory(self, context_id, file, line, column, done):
        raise exceptions.NotImplementedError("Abstract method")

class DoneMapToSource(object):
    def doneMapToSource(self, token, error, areas):
        pass

class DoneMapToMemory(object):
    def doneMapToMemory(self, token, error, areas):
        pass
