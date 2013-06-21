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

import java.util.concurrent.atomic.AtomicBoolean;


import static org.junit.Assert.*;
import org.junit.Test;
import org.magnos.test.BaseTest;

public class TestNestedSync extends BaseTest 
{

	@Test
	public void testNested() 
	{
		final AtomicBoolean flag = new AtomicBoolean(false);
		final Object lock = new Object();
		
		GroupTask.initialize(2);
		
		GroupTask.add(new Runnable() {
			public void run() {
				synchronized (lock) {
					synchronized (lock) {
						try {
							lock.wait();
							flag.set(true);	
						}
						catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
				assertTrue(flag.get());
			}
		});
		
		GroupTask.add(new Runnable() {
			public void run() {
				while (flag.get() == false) {
					synchronized (lock) {
						lock.notifyAll();
					}
					sleep(1);
				}
				assertTrue(flag.get());
			}
		});
		
		GroupTask.execute();
		
		assertTrue(true);
	}
	
	
}
