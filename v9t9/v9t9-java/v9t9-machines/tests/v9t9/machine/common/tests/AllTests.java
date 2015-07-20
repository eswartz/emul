/*
  AllTests.java

  (c) 2012-2013 Ed Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.machine.common.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * @author ejs
 *
 */
@RunWith(Suite.class)
@SuiteClasses({ TestCycles9900.class, TestInsts9900.class,
		TestMD5SumFilters.class, TestMemoryRanges.class })
public class AllTests {

}
