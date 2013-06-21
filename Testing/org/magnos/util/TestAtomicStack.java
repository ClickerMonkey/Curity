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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.magnos.test.BaseTest;
import org.magnos.util.AtomicStack;



public class TestAtomicStack extends BaseTest 
{

	@Test
	public void testStacking()
	{
		AtomicStack<String> s = new AtomicStack<String>();
		
		assertNull( s.pop() );
		assertNull( s.peek() );
		
		s.push("Hello");
		
		s.push("World");

		assertEquals( "World", s.peek() );
		assertEquals( "World", s.pop() );
		
		s.push("Mother");
		
		assertEquals( "Mother", s.peek() );
		assertEquals( "Mother", s.pop() );
		
		assertEquals( "Hello", s.peek() );
		assertEquals( "Hello", s.pop() );

		assertNull( s.pop() );
		assertNull( s.peek() );
	}
	
	@Test
	public void testSize()
	{
		AtomicStack<String> s = new AtomicStack<String>();
		
		assertEquals( 0, s.size() );
		
		s.push("H");	// 1
		s.push("E");	// 2
		
		assertEquals( 2, s.size() );
		
		s.push("L");	// 3
		s.pop();		// 2
		s.push("LL");	// 3
		
		assertEquals( 3, s.size() );
		
		s.push(")");	// 4
		s.pop();		// 3
		s.push("O");	// 4
		
		assertEquals( 4, s.size() );
	}
	
//	@Test
	public void testHeavyConcurrentAccess()
	{
		final int MAX = 1 << 20;
		
		// Writers pop from this.
		final AtomicStack<Integer> source = new AtomicStack<Integer>();
		// Writers push to this, Readers pop from this (concurrent action!).
		final AtomicStack<Integer> transfer = new AtomicStack<Integer>();
		// Readers push to this.
		final AtomicStack<Integer> target = new AtomicStack<Integer>();
		
		// Start watch to get reference times
		watch.start();
		
		Runnable writer = new Runnable() {
			public void run() {
				System.out.format("Writer [%d] started at %d\n", Thread.currentThread().getId(), watch.millis());
				int written = 0;
				for (Integer x = source.pop(); x != null; x = source.pop()) {
					transfer.push(x);
					written++;
				}
				System.out.format("Writer [%d] stopped at %d with %d writes\n", Thread.currentThread().getId(), watch.millis(), written);
			}
		};
		
		Runnable reader = new Runnable() {
			public void run() {
				sleep(2);		// start off slower so there are items to pull
				System.out.format("Reader [%d] started at %d\n", Thread.currentThread().getId(), watch.millis());
				int read = 0;
				for (Integer x = transfer.pop(); x != null; x = transfer.pop()) {
					target.push(x);
					read++;
				}
				System.out.format("Reader [%d] stopped at %d with %d reads\n", Thread.currentThread().getId(), watch.millis(), read);
			}
		};
		
		// Fill source
		for (int i = 0; i < MAX; i++) {
			source.push(i);
		}
		
		GroupTask.initialize(6);
		GroupTask.add(writer, 2);
		GroupTask.add(reader, 4);
		GroupTask.execute();
		
		// Check target: sort numbers then check for existence
		List<Integer> result = new ArrayList<Integer>();
		for (Integer x = target.pop(); x != null; x = target.pop()) {
			result.add(x);
		}
		Collections.sort(result);
		
		assertEquals( MAX, result.size() );
		
		for (int i = 0; i < MAX; i++) {
			assertEquals( Integer.valueOf(i), result.get(i) );
		}
	}
	
	
}
