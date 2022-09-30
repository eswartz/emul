/*
  RomTables.java

  (c) 2009-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.speech;

/**
 * This file is adapted from TMS5220 listings via MAME
 * @author ejs
 *
 */
public class RomTables {
	/* TMS5220 ROM Tables */
	

	final static public int energytablex[] = {
			0, 52, 87, 123, 174, 246, 348, 491,
			694, 981, 1385, 1957, 2764, 3904, 5514, 7789
	};
	
	/* This is the energy lookup table (4-bits -> 10-bits) */
	final static public short energytable[] = {
		0x0000, 0x00C0, 0x0140, 0x01C0, 0x0280, 0x0380, 0x0500, 0x0740,
		0x0A00, 0x0E40, 0x1440, 0x1C80, 0x2840, 0x38C0, 0x5040, 0x7FC0
	};

	final static public int pitchtablex[] = {
		0, 15, 16, 17, 18, 19, 20, 21,
		22, 23, 24, 25, 26, 27, 28, 29,
		30, 31, 32, 33, 34, 35, 36, 37,
		38, 39, 40, 41, 42, 44, 46, 48, 
		50, 52, 53, 56, 58, 60, 62, 65,
		68, 70, 72, 76, 78, 80, 84, 86,
		91, 94, 98, 101, 105, 109, 114, 118,
		112, 127, 132, 137, 142, 148, 153, 159,
	};
	
	/* This is the pitch lookup table (6-bits -> 8-bits) */

	final static public int pitchtable[] = {
		0x0000, 0x1000, 0x1100, 0x1200, 0x1300, 0x1400, 0x1500, 0x1600,
		0x1700, 0x1800, 0x1900, 0x1A00, 0x1B00, 0x1C00, 0x1D00, 0x1E00,
		0x1F00, 0x2000, 0x2100, 0x2200, 0x2300, 0x2400, 0x2500, 0x2600,
		0x2700, 0x2800, 0x2900, 0x2A00, 0x2B00, 0x2D00, 0x2F00, 0x3100,
		0x3300, 0x3500, 0x3600, 0x3900, 0x3B00, 0x3D00, 0x3F00, 0x4200,
		0x4500, 0x4700, 0x4900, 0x4D00, 0x4F00, 0x5100, 0x5500, 0x5700,
		0x5C00, 0x5F00, 0x6300, 0x6600, 0x6A00, 0x6E00, 0x7300, 0x7700,
		0x7B00, 0x8000, 0x8500, 0x8A00, 
		0x8F00, 0x9500, 0x9A00, 0xA000
	};

	/* These are the reflection coefficient lookup tables */

	/* K1 is (5-bits -> 9 bits+sign, 2's comp. fractional (-1 < x < 1) */

	final static public double k1tablef[] = {
			-0.97850, -0.97270, -0.97070, -0.96680,
			-0.96290, -0.95900, -0.95310, -0.94140,
			-0.93360, -0.92580, -0.91600, -0.90620,
			-0.89650, -0.88280, -0.86910, -0.85350,
			-0.80420, -0.74058, -0.66019, -0.56116,
			-0.44296, -0.30706, -0.15735, -0.00005,
			0.15725, 0.30696, 0.44288, 0.56109,
			0.66013, 0.74054, 0.80416, 0.85350,
	};
	
	final static public short k1table[] = {

		(short) 0x82C0, (short) 0x8380, (short) 0x83C0, (short) 0x8440,
		(short) 0x84C0, (short) 0x8540, (short) 0x8600, (short) 0x8780,
		(short) 0x8880, (short) 0x8980, (short) 0x8AC0, (short) 0x8C00,
		(short) 0x8D40, (short) 0x8F00, (short) 0x90C0, (short) 0x92C0,
		(short) 0x9900, (short) 0xA140, (short) 0xAB80, (short) 0xB840,
		(short) 0xC740, (short) 0xD8C0, (short) 0xEBC0, 0x0000,
		0x1440, 0x2740, 0x38C0, 0x47C0, 0x5480, 0x5EC0, 0x6700, 0x6D40
	};

	/* K2 is (5-bits -> 9 bits+sign, 2's comp. fractional (-1 < x < 1) */
	final static public double k2tablef[] = {
			-0.64000, -0.58999, -0.53500, -0.47507,
			-0.47507, -0.41039, -0.34129, -0.26830, 
			-0.19209, -0.11350, -0.03345, 0.04802, 
			0.12690, 0.20515, 0.28087, 0.35325, 
			0.42163, 0.54464, 0.59878, 0.64796,
			0.69227, 0.73190, 0.76714, 0.79828,
			0.82567, 0.84695, 0.87057, 0.88875,
			0.90451, 0.91813, 0.92988, 0.98830,
	
	};
	final static public short k2table[] = {
		(short) 0xAE00, (short) 0xB480, (short) 0xBB80, (short) 0xC340,
		(short) 0xCB80, (short) 0xD440, (short) 0xDDC0, (short) 0xE780,
		(short) 0xF180, (short) 0xFBC0, 0x0600, 0x1040, 0x1A40, 0x2400, 0x2D40,
		0x3600,
		0x3E40, 0x45C0, 0x4CC0, 0x5300, 0x5880, 0x5DC0, 0x6240, 0x6640,
		0x69C0, 0x6CC0, 0x6F80, 0x71C0, 0x73C0, 0x7580, 0x7700, 0x7E80
	};

	final static public double k3tablef[] = {
			-0.64000, -0.53145, -0.42289, -0.31434,
			-0.20579, -0.09723, 0.01132, 0.11987,
			0.22843, 0.33698, 0.44553, 0.55409,
			0.66264, 0.77119, 0.87975, 0.98830

	};
	/* K3 is (4-bits -> 9 bits+sign, 2's comp. fractional (-1 < x < 1) */

	final static public short k3table[] = {

		(short) 0x9200, (short) 0x9F00, (short) 0xAD00, (short) 0xBA00,
		(short) 0xC800, (short) 0xD500, (short) 0xE300, (short) 0xF000,
		(short) 0xFE00, 0x0B00, 0x1900, 0x2600, 0x3400, 0x4100, 0x4F00, 0x5C00
	};

	/* K4 is (4-bits -> 9 bits+sign, 2's comp. fractional (-1 < x < 1) */

	final static public short k4table[] = {

		(short) 0xAE00, (short) 0xBC00, (short) 0xCA00, (short) 0xD800,
		(short) 0xE600, (short) 0xF400, 0x0100, 0x0F00,
		0x1D00, 0x2B00, 0x3900, 0x4700, 0x5500, 0x6300, 0x7100, 0x7E00
	};

	/* K5 is (4-bits -> 9 bits+sign, 2's comp. fractional (-1 < x < 1) */

	final static public short k5table[] = {

		(short) 0xAE00, (short) 0xBA00, (short) 0xC500, (short) 0xD100,
		(short) 0xDD00, (short) 0xE800, (short) 0xF400, (short) 0xFF00,
		0x0B00, 0x1700, 0x2200, 0x2E00, 0x3900, 0x4500, 0x5100, 0x5C00
	};

	/* K6 is (4-bits -> 9 bits+sign, 2's comp. fractional (-1 < x < 1) */

	final static public short k6table[] = {

		(short) 0xC000, (short) 0xCB00, (short) 0xD600, (short) 0xE100,
		(short) 0xEC00, (short) 0xF700, 0x0300, 0x0E00,
		0x1900, 0x2400, 0x2F00, 0x3A00, 0x4500, 0x5000, 0x5B00, 0x6600
	};

	/* K7 is (4-bits -> 9 bits+sign, 2's comp. fractional (-1 < x < 1) */

	final static public short k7table[] = {

		(short) 0xB300, (short) 0xBF00, (short) 0xCB00, (short) 0xD700,
		(short) 0xE300, (short) 0xEF00, (short) 0xFB00, 0x0700,
		0x1300, 0x1F00, 0x2B00, 0x3700, 0x4300, 0x4F00, 0x5A00, 0x6600
	};

	/* K8 is (3-bits -> 9 bits+sign, 2's comp. fractional (-1 < x < 1) */

	final static public short k8table[] = {

		(short) 0xC000, (short) 0xD800, (short) 0xF000, 0x0700, 0x1F00,
		0x3700, 0x4F00, 0x6600
	};

	/* K9 is (3-bits -> 9 bits+sign, 2's comp. fractional (-1 < x < 1) */

	final static public short k9table[] = {

		(short) 0xC000, (short) 0xD400, (short) 0xE800, (short) 0xFC00,
		0x1000, 0x2500, 0x3900, 0x4D00
	};

	/* K10 is (3-bits -> 9 bits+sign, 2's comp. fractional (-1 < x < 1) */

	final static public short k10table[] = {

		(short) 0xCD00, (short) 0xDF00, (short) 0xF100, 0x0400, 0x1600,
		0x2000, 0x3B00, 0x4D00
	};

	/* chirp table */

	final static public byte chirptable[] = {
		0x00, 0x2a, (byte) 0xd4, 0x32,
		(byte) 0xb2, 0x12, 0x25, 0x14,
		0x02, (byte) 0xe1, (byte) 0xc5, 0x02,
		0x5f, 0x5a, 0x05, 0x0f,
		0x26, (byte) 0xfc, (byte) 0xa5, (byte) 0xa5,
		(byte) 0xd6, (byte) 0xdd, (byte) 0xdc, (byte) 0xfc,
		0x25, 0x2b, 0x22, 0x21,
		0x0f, (byte) 0xff, (byte) 0xf8, (byte) 0xee,
		(byte) 0xed, (byte) 0xef, (byte) 0xf7, (byte) 0xf6,
		(byte) 0xfa, 0x00, 0x03, 0x02,
		0x01, 0x00, 0x00, 0x00,
		0x00, 0x00, 0x00, 0x00,
		0x00, 0x00, 0x00
	};

	/* interpolation coefficients */

	final static public byte interp_coeff[] = {
		3, 3, 3, 2, 2, 1, 1, 0
	};

	public final static short[][] ktable = {
		k1table, k2table, k3table, /*k4table*/ k3table, k5table, k6table,
		k7table, k8table, k9table, k10table
	};

	static {
		
		for (int i = 0; i < pitchtable.length; i++) {
			short nv = (short) (pitchtablex[i] << 8);
			pitchtable[i] = nv; 
		}
		
		for (int i = 0; i < energytable.length; i++) {
			short nv = (short) (energytablex[i] << 4);
			energytable[i] = nv; 
		}
		
		for (int i = 0; i < k1table.length; i++) {
			short nv = (short) Math.round(k1tablef[i] * 0x7fff);
			k1table[i] = nv; 
		}
		
		for (int i = 0; i < k2table.length; i++) {
			short nv = (short) Math.round(k2tablef[i] * 0x7fff);
			k2table[i] = nv; 
		}
		
		for (int i = 0; i < k3table.length; i++) {
			short nv = (short) Math.round(k3tablef[i] * 0x7fff);
			k3table[i] = nv; 
		}
	}


}
