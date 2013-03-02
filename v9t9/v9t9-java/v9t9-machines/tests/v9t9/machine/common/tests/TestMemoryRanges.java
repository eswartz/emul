/*
  TestMemoryRanges.java

  (c) 2008-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.machine.common.tests;

import junit.framework.TestCase;
import v9t9.common.asm.MemoryRange;
import v9t9.common.asm.MemoryRanges;

public class TestMemoryRanges extends TestCase {
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }
    public void testRanges() {
    	MemoryRanges ranges = new MemoryRanges();
        MemoryRange range;
        ranges.addRange(0, 0x2000, true);
        range = ranges.getRangeContaining(0);
        assertNotNull(range);
        range = ranges.getRangeContaining(0x40);
        assertNotNull(range);
        MemoryRange range2 = ranges.getRangeContaining(0x1ffE);
        assertEquals(range, range2);
        range = ranges.getRangeContaining(0x2000);
        assertFalse(range.isCode());
        assertEquals(MemoryRange.EMPTY, range.getType());
    }

}
