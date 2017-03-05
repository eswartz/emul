/*
  TestStockModules.java

  (c) 2015 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.machine.common.tests;

import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import v9t9.common.client.ISettingsHandler;
import v9t9.common.machine.IMachine;
import v9t9.common.modules.IModule;
import v9t9.common.settings.BasicSettingsHandler;
import v9t9.machine.ti99.machine.StandardTI994AMachineModel;

/**
 * @author ejs
 *
 */
public class TestStockModules {

	private ISettingsHandler settings;
	private IMachine machine;

	@Before
	public void setup() throws Exception {
		settings = new BasicSettingsHandler();  
		machine = new StandardTI994AMachineModel().createMachine(settings);
	}
	
	/** Make sure stock modules don't overlap */
	@Test
	public void testNoDuplicateMd5StockModules() throws Exception {
		IModule[] stocks = machine.getModuleManager().getStockModules();
		Map<String, IModule> md5Map = new HashMap<String, IModule>();
		for (IModule stock : stocks) {
			String md5 = stock.getMD5();
			IModule old = md5Map.put(md5, stock);
			if (old != null && old.getKeywords().equals(stock.getKeywords())) {
				fail(stock.getMD5() + " -> " + stock.getName() + " / " + old.getName());
			}
		}
	}
	/** Make sure no stock modules have the same name */
	@Test
	public void testNoDuplicateNameStockModules() throws Exception {
		IModule[] stocks = machine.getModuleManager().getStockModules();
		Map<String, IModule> nameMap = new HashMap<String, IModule>();
		StringBuilder sb = new StringBuilder();
		for (IModule stock : stocks) {
			IModule old = nameMap.put(stock.getName(), stock);
			if (old != null && old.getKeywords().equals(stock.getKeywords())) {
				sb.append(stock.getMD5() + " -> " + stock.getName() + " / " + old.getName() + '\n');
			}
		}
		if (sb.length() > 0)
			fail(sb.toString());
	}

}
