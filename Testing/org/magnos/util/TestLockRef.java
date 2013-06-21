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
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

import org.junit.Test;
import org.magnos.test.BaseTest;
import org.magnos.util.LockRef;


public class TestLockRef extends BaseTest
{

	@Test
	public void testEmptyConstructor()
	{
		LockRef<String> ref = new LockRef<String>();
		
		assertNull( ref.get() );
	}
	
	@Test
	public void testGetWithoutContention()
	{
		LockRef<String> mod = new LockRef<String>("Hi");
		
		System.out.print("Empty method call time: ");
		for (int i = 0; i < 1000; i++) {
			watch.start();
			emptyMethod();
			watch.stop();
		}
		System.out.println(watch.nanos() + "ns");
		
		
		System.out.print("Module get time: ");
		for (int i = 0; i < 1000; i++) {
			watch.start();
			mod.get();
			watch.stop();
		}
		System.out.println(watch.nanos() + "ns");
	}
	
	private String emptyMethod() {
		return "Hi";
	}
	
	@Test
	public void testWriteBlock()
	{
		final LockRef<String> mod = new LockRef<String>("Hello World");
		
		System.out.println("Locking");
		mod.lock();
		
		Runnable runner = new Runnable() {
			public void run() {
				System.out.println("Before get ["+Thread.currentThread().getId()+"]");
				System.out.println(mod.get());
				System.out.println("After get ["+Thread.currentThread().getId()+"]");
			}
		};

		GroupTask.initialize(4);
		GroupTask.add(runner, 4);
		GroupTask.begin();
		
		System.out.println("Waiting...");
		sleep(100);
		
		mod.unlock("A new value!");
		
		GroupTask.finish();
	}
	
	@Test
	public void testConstantReadingAndWriting()
	{
		final LockRef<String> mod = new LockRef<String>("Hello World");
		final AtomicInteger finished = new AtomicInteger();
		final AtomicBoolean running = new AtomicBoolean(true); 
		
		Runnable writer = new Runnable() {
			public void run() {
				String newValue = null;
				for (int i = 0; i < 100; i++) {
					mod.lock();
					sleep(10); 
					newValue = "Random:" + Math.random();
					mod.unlock(newValue);
					assertEquals( newValue, mod.get() );
					sleep(40);
					assertEquals( newValue, mod.get() );
				}
				running.set(false);
				System.out.println("Writer finished");
			}
		};
		
		Runnable reader = new Runnable() {
			public void run() {
				while (running.get()) {
					mod.get();
					sleep(2);
				}
				System.out.println("Reader ["+Thread.currentThread().getId()+"] finished");
				finished.incrementAndGet();
			}
		};

		GroupTask.initialize(5);
		GroupTask.add(reader, 4);
		GroupTask.add(writer);
		GroupTask.execute();
		
		assertEquals(4, finished.get());
	}
	
	@Test
	public void testSet()
	{
		LockRef<String> ref = new LockRef<String>();
		
		ref.set("Hello World!");
		
		assertEquals( "Hello World!", ref.get() );
	}
	
	@Test
	public void testHasNewValue()
	{
		LockRef<String> ref = new LockRef<String>();
		
		assertFalse( ref.hasNewValue() );
		
		ref.set("Hi!");
		
		assertTrue( ref.hasNewValue() );
		assertFalse( ref.hasNewValue() );
	}
	
}
