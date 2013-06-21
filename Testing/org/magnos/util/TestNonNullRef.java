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

import static org.junit.Assert.*;

import org.junit.Test;
import org.magnos.test.BaseTest;
import org.magnos.util.NonNullRef;



public class TestNonNullRef extends BaseTest 
{

	@Test
	public void testNonNull()
	{
		NonNullRef<String> ref = new NonNullRef<String>();
		assertFalse( ref.has() );
		
		ref.set("Hello World");
		assertTrue( ref.has() );
		
		assertEquals( "Hello World", ref.get() );
	}
	
	@Test
	public void testNull()
	{
		final NonNullRef<String> ref = new NonNullRef<String>();
		assertFalse( ref.has() );
		
		GroupTask.initialize(1);
		GroupTask.add(new Runnable() {
			public void run() {
				System.out.println("Before Get");
				assertEquals( "Hello World", ref.get() );
			}
		});
		GroupTask.begin();
		
		sleep(500);
		System.out.println("Before Set");
		ref.set("Hello World");
		
		GroupTask.finish();
	}
	
}
