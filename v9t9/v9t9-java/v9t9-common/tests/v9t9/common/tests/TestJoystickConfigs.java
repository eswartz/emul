/*
  TestJoystickConfigs.java

  (c) 2017 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.tests;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import v9t9.common.keyboard.ControllerConfig;
import v9t9.common.keyboard.ControllerIdentifier;
import v9t9.common.keyboard.JoystickRole;

/**
 * @author ejs
 *
 */
public class TestJoystickConfigs {

	private ControllerIdentifier gp_x = new ControllerIdentifier("USB Gamepad", 5, "x"); 
	private ControllerIdentifier gp_y = new ControllerIdentifier("USB Gamepad", 7, "y");
	private ControllerIdentifier gp_b = new ControllerIdentifier("USB Gamepad", 0, "0");
	
	private ControllerConfig gp_config = new ControllerConfig();
	
	private ControllerIdentifier ps_x0 = new ControllerIdentifier("Fancy \"PS\" Controller!", 8, "x"); 
	private ControllerIdentifier ps_x1 = new ControllerIdentifier("Fancy \"PS\" Controller!", 9, "rx"); 
	private ControllerIdentifier ps_y0 = new ControllerIdentifier("Fancy \"PS\" Controller!", 4, "y");
	private ControllerIdentifier ps_y1 = new ControllerIdentifier("Fancy \"PS\" Controller!", 5, "ry");
	private ControllerIdentifier ps_b0 = new ControllerIdentifier("Fancy \"PS\" Controller!", 2, "b0");
	private ControllerIdentifier ps_b1 = new ControllerIdentifier("Fancy \"PS\" Controller!", 3, "b1");
	private ControllerIdentifier ps_b2 = new ControllerIdentifier("Fancy \"PS\" Controller!", 10, "b2");
	private ControllerIdentifier ps_b3 = new ControllerIdentifier("Fancy \"PS\" Controller!", 11, "b3");
	
	private ControllerConfig ps_config = new ControllerConfig();
	
	@Before
	public void setUp() {
		gp_config.clear();

		assertNull(gp_config.map(gp_x, JoystickRole.X_AXIS));
		assertNull(gp_config.map(gp_y, JoystickRole.Y_AXIS));
		assertNull(gp_config.map(gp_b, JoystickRole.BUTTON));
		
		ps_config.clear();
		
		assertNull(ps_config.map(ps_x0, JoystickRole.X_AXIS));
		assertNull(ps_config.map(ps_x1, JoystickRole.X_AXIS));
		assertNull(ps_config.map(ps_y0, JoystickRole.Y_AXIS));
		assertNull(ps_config.map(ps_y1, JoystickRole.Y_AXIS));
		assertNull(ps_config.map(ps_b0, JoystickRole.BUTTON));
		assertNull(ps_config.map(ps_b1, JoystickRole.BUTTON));
		assertNull(ps_config.map(ps_b2, JoystickRole.BUTTON));
		assertNull(ps_config.map(ps_b3, JoystickRole.BUTTON));
	
	}
	@Test
	public void testMapped() {
			
		assertEquals(JoystickRole.X_AXIS, gp_config.find(gp_x));
		assertEquals(JoystickRole.Y_AXIS, gp_config.find(gp_y));
		assertEquals(JoystickRole.BUTTON, gp_config.find(gp_b));
		
		assertEquals(JoystickRole.X_AXIS, ps_config.find(ps_x0));
		assertEquals(JoystickRole.X_AXIS, ps_config.find(ps_x1));
		assertEquals(JoystickRole.Y_AXIS, ps_config.find(ps_y0));
		assertEquals(JoystickRole.Y_AXIS, ps_config.find(ps_y1));
		assertEquals(JoystickRole.BUTTON, ps_config.find(ps_b0));
		assertEquals(JoystickRole.BUTTON, ps_config.find(ps_b1));
		assertEquals(JoystickRole.BUTTON, ps_config.find(ps_b2));
		assertEquals(JoystickRole.BUTTON, ps_config.find(ps_b3));


	}
	
	@Test
	public void testMappedNew() {
		
		assertEquals(JoystickRole.X_AXIS, gp_config.find(new ControllerIdentifier("USB Gamepad", 5, "x")));
		assertEquals(JoystickRole.Y_AXIS, gp_config.find(new ControllerIdentifier("USB Gamepad", 7, "y")));
		assertEquals(JoystickRole.BUTTON, gp_config.find(new ControllerIdentifier("USB Gamepad", 0, "0")));
		
		assertEquals(JoystickRole.X_AXIS, ps_config.find(new ControllerIdentifier("Fancy \"PS\" Controller!", 9, "rx")));
		assertEquals(JoystickRole.Y_AXIS, ps_config.find(new ControllerIdentifier("Fancy \"PS\" Controller!", 4, "y")));
		assertEquals(JoystickRole.BUTTON, ps_config.find(new ControllerIdentifier("Fancy \"PS\" Controller!", 10, "b2")));
		
	}
	
	@Test
	public void testToFromString() {
		String text = gp_config.toString();
		ControllerConfig copy = new ControllerConfig();

		try {
			copy.fromString(text);
		} catch (ControllerConfig.ParseException e) {
			fail(e.getMessage());
		}
		
		assertEquals(copy, gp_config);
		
		
		text = ps_config.toString();
		copy = new ControllerConfig();
		
		try {
			copy.fromString(text);
		} catch (ControllerConfig.ParseException e) {
			fail(e.getMessage());
		}
		
		assertEquals(copy, ps_config);
	}
	
	@Test
	public void testFromUserString() {
		ControllerConfig cfg = new ControllerConfig();
		
		try {
			cfg.fromString("\n"
					+ "# my config!\n"
					+ "  # (c) 1992 John Q Public\r\n"
					+ "\"Whac-a-mole\", \"X_0\", 9 = X_AXIS\n"
					+ "\"Whac-a-mole\", \"Y_0\", 19 = Y_AXIS\n"
					+ "# this one sometimes disappears\n"
					+ "\"Whac-a-mole\", \"mah button\", 2 = BUTTON\n"
					+ "# this one is in the new version\n"
					+ "\"Whac-a-mole\", \"extry button\", 9 = STRAFE\n"
					);
		} catch (ControllerConfig.ParseException e) {
			fail(e.getMessage());
		}
	
		assertEquals(JoystickRole.X_AXIS, cfg.find(new ControllerIdentifier("Whac-a-mole", 9, "X_0")));
		assertEquals(JoystickRole.Y_AXIS, cfg.find(new ControllerIdentifier("Whac-a-mole", 19, "Y_0")));
		assertEquals(JoystickRole.BUTTON, cfg.find(new ControllerIdentifier("Whac-a-mole", 2, "mah button")));
		assertEquals(JoystickRole.IGNORE, cfg.find(new ControllerIdentifier("Whac-a-mole", 9, "extry button")));
	}
	

	@Test
	public void testFromBadString() {
		ControllerConfig cfg = new ControllerConfig();

		try {
			cfg.fromString("BLAH!");
			fail();
		} catch (ControllerConfig.ParseException e) {
		}
	}
	
}
