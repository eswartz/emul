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
Expressions service allows TCF client to perform expression evaluation on remote target.
The service can be used to retrieve or modify values of variables or any data structures in remote target memory.
"""

from tcf import services

# Service name.
NAME = "Expressions"

class Expression(object):
    """
    Expression object represent an expression that can be evaluated by remote target.
    It has a unique ID and contains all information necessary to compute a value.
    The object data usually includes:
      1. process, thread or stack frame ID that should be used to resolve symbol names
      2. a script that can compute a value, like "x.y + z"
    """
    def __init__(self, props):
        self._props = props or {}

    def __str__(self):
        return "[Expression Context %s]" % self._props

    def getID(self):
        """
        Get context ID.
        @return context ID.
        """
        return self._props.get(PROP_ID)

    def getParentID(self):
        """
        Get parent context ID.
        @return parent context ID.
        """
        return self._props.get(PROP_PARENT_ID)

    def getLanguage(self):
        """
        Get expression script language ID.
        @return language ID.
        """
        return self._props.get(PROP_LANGUAGE)

    def getExpression(self):
        """
        Return expression string - the script part of the context.
        @return expression script string
        """
        return self._props.get(PROP_EXPRESSION)

    def getSymbolID(self):
        """
        Return symbol ID if the expression represents a symbol (e.g. local variable).
        @return symbol ID
        """
        return self._props.get(PROP_SYMBOL_ID)

    def getBits(self):
        """
        Get size of expression value in bits.
        Can be 0 if value size is even number of bytes, use getSize() in such case.
        @return size in bits.
        """
        return self._props.get(PROP_BITS, 0)

    def getSize(self):
        """
        Get size in bytes. The size can include extra (unused) bits.
        This is "static" or "declared" size - as determined by expression type.
        @return size in bytes.
        """
        return self._props.get(PROP_SIZE, 0)

    def getTypeID(self):
        """
        Get expression type ID. Symbols service can be used to get type properties.
        This is "static" or "declared" type ID, actual type of a value can be different -
        if expression language supports dynamic typing.
        @return type ID.
        """
        return self._props.get(PROP_TYPE)

    def canAssign(self):
        """
        Check if the expression can be assigned a new value.
        @return true if can assign.
        """
        return self._props.get(PROP_CAN_ASSIGN)

    def getProperties(self):
        """
        Get complete map of context properties.
        @return map of context properties.
        """
        return self._props

# Expression context property names.
PROP_ID = "ID"
PROP_PARENT_ID = "ParentID"
PROP_SYMBOL_ID = "SymbolID"
PROP_LANGUAGE = "Language"
PROP_EXPRESSION = "Expression"
PROP_BITS = "Bits"
PROP_SIZE = "Size"
PROP_TYPE = "Type"
PROP_CAN_ASSIGN = "CanAssign"

class Value(object):
    """
    Value represents result of expression evaluation.
    Note that same expression can be evaluated multiple times with different results.
    """
    def __init__(self, value, props):
        self._value = value
        self._props = props or {}

    def __str__(self):
        return "[Expression Value %s %s]" % (self._value, self._props)

    def getTypeClass(self):
        """
        Get value type class.
        @see symbols.TypeClass
        @return type class
        """
        return self._props.get(VAL_CLASS, 0)

    def getTypeID(self):
        """
        Get value type ID. Symbols service can be used to get type properties.
        @return type ID.
        """
        return self._props.get(VAL_TYPE)

    def isBigEndian(self):
        """
        Check endianness of the values.
        Big-endian means decreasing numeric significance with increasing byte number.
        @return true if big-endian.
        """
        return self._props.get(VAL_BIG_ENDIAN)

    def getValue(self):
        """
        Get value as array of bytes.
        @return value as array of bytes.
        """
        return self._value

    def getProperties(self):
        """
        Get complete map of value properties.
        @return map of value properties.
        """
        return self._props

# Expression value property names.
VAL_CLASS = "Class"
VAL_TYPE = "Type"
VAL_BIG_ENDIAN = "BigEndian"

class ExpressionsService(services.Service):
    def getName(self):
        return NAME

    def getContext(self, id, done):
        """
        Retrieve expression context info for given context ID.
        @see Expression

        @param id - context ID.
        @param done - call back interface called when operation is completed.
        @return - pending command handle.
        """
        raise NotImplementedError("Abstract method")

    def getChildren(self, parent_context_id, done):
        """
        Retrieve children IDs for given parent ID.
        Meaning of the operation depends on parent kind:
        1. expression with type of a struct, union, or class - fields
        2. expression with type of an enumeration - enumerators
        3. expression with type of an array - array elements
        4. stack frame - function arguments and local variables
        5. thread - top stack frame function arguments and local variables
        6. process - global variables

        Children list *does not* include IDs of expressions that were created by clients
        using "create" command.

        @param parent_context_id - parent context ID.
        @param done - call back interface called when operation is completed.
        @return - pending command handle.
        """
        raise NotImplementedError("Abstract method")

    def create(self, parent_id, language, expression, done):
        """
        Create an expression context.
        The context should be disposed after use.
        @param parent_id - a context ID that can be used to resolve symbol names.
        @param language - language of expression script, None means default language
        @param expression - expression script
        @param done - call back interface called when operation is completed.
        @return - pending command handle.
        """
        raise NotImplementedError("Abstract method")

    def dispose(self, id, done):
        """
        Dispose an expression context that was created by create()
        @param id - the expression context ID
        @param done - call back interface called when operation is completed.
        @return - pending command handle.
        """
        raise NotImplementedError("Abstract method")

    def evaluate(self, id, done):
        """
        Evaluate value of an expression context.
        @param id - the expression context ID
        @param done - call back interface called when operation is completed.
        @return - pending command handle.
        """
        raise NotImplementedError("Abstract method")

    def assign(self, id, value, done):
        """
        Assign a value to memory location determined by an expression.
        @param id - expression ID.
        @param value - value as an array of bytes.
        @param done - call back interface called when operation is completed.
        @return - pending command handle.
        """
        raise NotImplementedError("Abstract method")

    def addListener(self, listener):
        """
        Add expressions service event listener.
        @param listener - event listener implementation.
        """
        raise NotImplementedError("Abstract method")

    def removeListener(self, listener):
        """
        Remove expressions service event listener.
        @param listener - event listener implementation.
        """
        raise NotImplementedError("Abstract method")

class DoneGetContext(object):
    """
    Client call back interface for getContext().
    """
    def doneGetContext(self, token, error, context):
        """
        Called when context data retrieval is done.
        @param token - command handle
        @param error - error description if operation failed, None if succeeded.
        @param context - context properties.
        """
        pass

class DoneGetChildren(object):
    """
    Client call back interface for getChildren().
    """
    def doneGetChildren(self, token, error, context_ids):
        """
        Called when context list retrieval is done.
        @param token - command handle
        @param error - error description if operation failed, None if succeeded.
        @param context_ids - array of available context IDs.
        """
        pass


class DoneCreate(object):
    """
    Client call back interface for create().
    """
    def doneCreate(self, token, error, context):
        """
        Called when context create context command is done.
        @param token - command handle
        @param error - error description if operation failed, None if succeeded.
        @param context - context properties.
        """
        pass

class DoneDispose(object):
    """
    Client call back interface for dispose().
    """
    def doneDispose(self, token, error):
        """
        Called when context dispose command is done.
        @param token - command handle
        @param error - error description if operation failed, None if succeeded.
        """
        pass

class DoneEvaluate(object):
    """
    Client call back interface for evaluate().
    """
    def doneEvaluate(self, token, error, value):
        """
        Called when context dispose command is done.
        @param token - command handle
        @param error - error description if operation failed, None if succeeded.
        @param value - expression evaluation result
        """
        pass

class DoneAssign(object):
    """
    Client call back interface for assign().
    """
    def doneAssign(self, token, error):
        """
        Called when assign command is done.
        @param token - command handle
        @param error - error description if operation failed, None if succeeded.
        """
        pass

class ExpressionsListener(object):
    """
    Registers event listener is notified when registers context hierarchy
    changes, and when a register is modified by the service commands.
    """
    def valueChanged(self, id):
        """
        Called when expression value was changed and clients
        need to update themselves. Clients, at least, should invalidate
        corresponding cached expression data.
        Not every change is notified - it is not possible,
        only those, which are not caused by normal execution of the debuggee.
        At least, changes caused by "assign" command should be notified.
        @param id - expression context ID.
        """
        pass
