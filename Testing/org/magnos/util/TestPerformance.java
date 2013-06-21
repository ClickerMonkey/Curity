/* 
 * NOTICE OF LICENSE
 * 
 * This source file is subject to the Open Software License (OSL 3.0) that is 
 * bundled with this package in the file LICENSE.txt. It is also available 
 * through the world-wide-web at http://opensource.org/licenses/osl-3.0.php
 * If you did not receive a copy of the license and are unable to obtain it 
 * through the world-wide-web, please send an email to pdiffenderfer@gmail.com 
 * so we can send you a copy immediately. If you use any of this software please
 * notify me via my website or email, your feedback is much appreciated. 
 * 
 * @copyright   Copyright (c) 2011 Magnos Software (http://www.magnos.org)
 * @license     http://opensource.org/licenses/osl-3.0.php
 * 				Open Software License (OSL 3.0)
 */

package org.magnos.util;


import org.junit.Test;
import org.magnos.test.BaseTest;

public class TestPerformance extends BaseTest 
{

	@Test
	@SuppressWarnings("unused")
	public void testSynchronize()
	{
		final int ITERATIONS = 1000000;
		
		watch.start("Without synchronized... ");
		int j = 0;
		for (int i = 0; i < ITERATIONS; i++) {
			j += (i + 23) * 25;
			j >>= 1;
			j |= (i << 3);
			j &= 0xFFFFFFF;
		}
		watch.stop("%.6f seconds.\n");

		long x = watch.nanos();
		
		final Object lock = new Object();
		
		watch.start("With synchronized... ");
		int k = 0;
		for (int i = 0; i < ITERATIONS; i++) {
			synchronized (lock) {
				k += (i + 23) * 25;
				k >>= 1;
				k |= (i << 3);
				k &= 0xFFFFFFF;	
			}
		}
		watch.stop("%.6f seconds.\n");
		
		long y = watch.nanos();
		
		double overhead = ((y - x) / (double)ITERATIONS);
		
		System.out.format("synchronized overhead: %.3f ns\n", overhead);
	}
	


	@Test
	@SuppressWarnings("unused")
	public void testSynchronizeString()
	{
		final int ITERATIONS = 1000000;
		
		watch.start("Without synchronized... ");
		String j = "";
		for (int i = 0; i < ITERATIONS; i++) {
			j += i;
			if (i % 20 == 19) {
				j = "";
			}
		}
		watch.stop("%.6f seconds.\n");

		long x = watch.nanos();
		
		final Object lock = new Object();
		
		watch.start("With synchronized... ");
		String k = "";
		for (int i = 0; i < ITERATIONS; i++) {
			synchronized (lock) {
				k += i;
				if (i % 20 == 19) {
					k = "";
				}
			}
		}
		watch.stop("%.6f seconds.\n");
		
		long y = watch.nanos();
		
		double overhead = ((y - x) / (double)ITERATIONS);
		
		System.out.format("synchronized overhead: %.3f ns\n", overhead);
	}
	
}
