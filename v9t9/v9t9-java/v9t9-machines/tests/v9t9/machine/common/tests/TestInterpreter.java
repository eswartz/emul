/*
  TestInterpreter.java

  (c) 2014 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.machine.common.tests;

import java.util.Arrays;
import java.util.List;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * @author ejs
 *
 */
@RunWith(Parameterized.class)
public class TestInterpreter {
	@Parameters
 	public static List<Object[]> data() {
 		return Arrays.asList(new Object[][] { });
 	}
 
 	public TestInterpreter() {
 	}
	
}
