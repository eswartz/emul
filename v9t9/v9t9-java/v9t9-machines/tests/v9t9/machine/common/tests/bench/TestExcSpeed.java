/*
  TestExcSpeed.java

  (c) 2012 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
 */
package v9t9.machine.common.tests.bench;

/**
 * @author ejs
 *
 */
public class TestExcSpeed {
	public static void main(String[] args) throws Exception {
		ISomething something;
		if (args.length > 0) {
			something = (ISomething) Class.forName(args[0]).newInstance();
		} else {
			something = new StdSomething();
		}
		long start = System.currentTimeMillis();
		
		int excs = 0;
		final int COUNT = 10000000;
		for (int i = 0; i < COUNT; i++) {
			try {
				something.run();
			} catch (Throwable t) {
				excs++;
			}
		}
		long end = System.currentTimeMillis();
		
		System.out.println(excs + " exceptions in " + (end-start) + " ms: " + excs * 1000 / (end-start ) + " per sec");
	}
}
