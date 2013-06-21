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

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;
import org.magnos.test.BaseTest;
import org.magnos.util.Notifier;



public class TestNotifier extends BaseTest 
{

	private interface EventListener {
		public void onEvent(String event);
	}
	
	@Test
	public void testProxy()
	{
		final AtomicInteger invocations = new AtomicInteger(); 
		final AtomicReference<String> lastEvent = new AtomicReference<String>();
		
		Notifier<EventListener> notifier = Notifier.create(EventListener.class);
		
		// Invoke with no listeners.
		notifier.proxy().onEvent("A");
		assertNull( lastEvent.get() );
		assertEquals( 0, invocations.get() );
		
		// Add the first listener
		notifier.add(new EventListener() {
			public void onEvent(String event) {
				invocations.addAndGet(1);
				lastEvent.set(event);
			}
		});

		// Invoke with only one listener
		notifier.proxy().onEvent("B");
		assertEquals( "B", lastEvent.get() );
		assertEquals( 1, invocations.get() );
		
		// Add the second listener
		notifier.add(new EventListener() {
			public void onEvent(String event) {
				invocations.addAndGet(2);
				lastEvent.set(event);
			}
		});	

		// Invoke with two listeners
		notifier.proxy().onEvent("C");
		assertEquals( "C", lastEvent.get() );
		assertEquals( 4, invocations.get() );
		
		// Add the third listener
		notifier.add(new EventListener() {
			public void onEvent(String event) {
				invocations.addAndGet(4);
				lastEvent.set(event);
			}
		});
		
		// Invoke with three listeners
		notifier.proxy().onEvent("D");
		assertEquals( "D", lastEvent.get() );
		assertEquals( 11, invocations.get() );
	}
	
}
