/**
 * 
 */
package v9t9.machine.ti99.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import v9t9.machine.ti99.tests.dsr.TestEmuDiskDSR;
import v9t9.machine.ti99.tests.dsr.TestEmuDiskDSRDiskLike;
import v9t9.machine.ti99.tests.dsr.TestRealDiskImage;

/**
 * @author ejs
 *
 */
@RunWith(Suite.class)
@SuiteClasses({ DiskMemoryEntryTest.class, MemoryEntryTest.class,
		MemoryTest.class, TestEmuDiskDSR.class, TestEmuDiskDSRDiskLike.class,
		TestRealDiskImage.class })
public class AllTests {

}
