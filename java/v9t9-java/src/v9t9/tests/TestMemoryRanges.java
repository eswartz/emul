/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Feb 21, 2006
 *
 */
package v9t9.tests;

import junit.framework.TestCase;
import v9t9.tools.asm.common.MemoryRange;
import v9t9.tools.asm.common.MemoryRanges;

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
