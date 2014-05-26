/*
  AllTests.java

  (c) 2011-2013 Ed Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.test;

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
