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

import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;


import org.junit.Test;
import org.magnos.test.BaseTest;
import org.magnos.util.BlockableQueue;


/**
 * Tests the BlockableQueue class.
 * 
 * @author Philip Diffenderfer
 *
 */
public class TestBlockableQueue extends BaseTest
{

	@Test
	public void testIsBlocking()
	{
		BlockableQueue<String> q = new BlockableQueue<String>();
		
		q.setBlocking(true);
		assertTrue( q.isBlocking() );
		
		q.setBlocking(false);
		assertFalse( q.isBlocking() );
	}
	
	@Test
	public void testIterator()
	{
		BlockableQueue<String> q = new BlockableQueue<String>();
		q.offer("Hello");
		q.offer("World");
		
		Iterator<String> iter = q.iterator();
		assertEquals( "Hello", iter.next() );
		assertEquals( "World", iter.next() );
		assertFalse( iter.hasNext() );
	}
	
	@Test
	public void testSize()
	{
		BlockableQueue<String> q = new BlockableQueue<String>();

		assertEquals( 0, q.size() );
		q.offer("Hello");
		assertEquals( 1, q.size() );
		q.offer("Mommy");
		assertEquals( 2, q.size() );
		q.poll();
		assertEquals( 1, q.size() );
		q.offer("Know All");
		assertEquals( 2, q.size() );
		q.poll();
		assertEquals( 1, q.size() );
		q.offer("See All");
		assertEquals( 2, q.size() );
		
		q.clear();

		assertEquals( 0, q.size() );
	}
	
	@Test
	public void testTimeout()
	{
		BlockableQueue<String> q = new BlockableQueue<String>();

		q.setTimeout(100);
		assertEquals( 100, q.getTimeout() );
		
		q.setTimeout(2, TimeUnit.SECONDS);
		assertEquals( 2000, q.getTimeout() );
	}
	
	@Test
	public void testPeekTimeout()
	{
		BlockableQueue<String> q = new BlockableQueue<String>();
		q.setBlocking(true);
		q.setTimeout(100);
		
		assertNull( q.peek() );
	}
	
	@Test
	public void testPollTimeout()
	{
		BlockableQueue<String> q = new BlockableQueue<String>();
		q.setBlocking(true);
		q.setTimeout(100);
		
		assertNull( q.poll() );
	}
	
	@Test
	public void testPeekInterrupt()
	{
		final AtomicReference<Thread> thread = new AtomicReference<Thread>();
		final BlockableQueue<String> q = new BlockableQueue<String>();
		q.setBlocking(true);
		q.setTimeout(500);
		
		GroupTask.initialize(1);
		GroupTask.add(new Runnable() {
			public void run() {
				thread.set(Thread.currentThread());
				assertNull( q.peek() );
			}
		});
		GroupTask.begin();
		
		sleep(100);
		
		thread.get().interrupt();
		
		GroupTask.finish();
	}
	
	@Test
	public void testPollInterrupt()
	{
		final AtomicReference<Thread> thread = new AtomicReference<Thread>();
		final BlockableQueue<String> q = new BlockableQueue<String>();
		q.setBlocking(true);
		q.setTimeout(500);
		
		GroupTask.initialize(1);
		GroupTask.add(new Runnable() {
			public void run() {
				thread.set(Thread.currentThread());
				assertNull( q.poll() );
			}
		});
		GroupTask.begin();
		
		sleep(100);
		
		thread.get().interrupt();
		
		GroupTask.finish();
	}
	
	@Test
	public void testNonblocking()
	{
		BlockableQueue<String> q = new BlockableQueue<String>();
		q.setBlocking(false);
		
		assertTrue( q.offer("Hello") );
		assertTrue( q.offer("World") );

		assertEquals( "Hello", q.peek() );
		assertEquals( "Hello", q.poll() );
		
		assertEquals( "World", q.peek() );
		assertEquals( "World", q.poll() );
		
		assertNull( q.poll() );
	}
	
	
	@Test
	public void testWakeup()
	{
		final BlockableQueue<String> q = new BlockableQueue<String>();
		q.setBlocking(true);
		
		GroupTask.initialize(1);
		GroupTask.add(new Runnable() {
			public void run() {
				sleep(500);
				q.wakeup();
				sleep(500);
				q.wakeup();
			}
		});
		GroupTask.begin();
		
		watch.start("Before poll...");
		assertNull( null, q.poll() );
		watch.stop(" poll awoken (%.3f), done.\n");
		
		watch.start("Before peek...");
		assertNull( null, q.peek() );
		watch.stop(" peek awoken (%.3f), done.\n");
		
		GroupTask.finish();
	}
	
	@Test
	public void testSingleReaderManyWriters()
	{
		final int MESSAGES = 10000;
		final int WRITERS = 10;
		final int MESSAGES_PER_WRITER = MESSAGES / WRITERS;
		
		final AtomicInteger nullCounter = new AtomicInteger();
		final BlockableQueue<Integer> q = new BlockableQueue<Integer>();
		q.setBlocking(true);
		
		Runnable reader = new Runnable() {
			public void run() {
				int total = 0;
				double minWait = Double.MAX_VALUE;
				double maxWait = -Double.MAX_VALUE;
				Stopwatch watch = new Stopwatch();
				while (total < MESSAGES) {
					watch.start();
					Integer value = q.poll();
					watch.stop();
					if (value != null) {
						minWait = Math.min(minWait, watch.seconds());
						maxWait = Math.max(maxWait, watch.seconds());
						total++;	
					} else {
						nullCounter.incrementAndGet();
					}
				}
				System.out.format("Wait: min[%.6fs] max[%.6fs]\n", minWait, maxWait);
			}
		};
		
		Runnable writer = new Runnable() {
			public void run() {
				for (int i = 0; i < MESSAGES_PER_WRITER; i++) {
					assertTrue( q.offer(i) );
					// Causes the reader to wait some amount of time
					sleep(1);
				}
			}
		};
		
		GroupTask.initialize(WRITERS + 1);
		GroupTask.add(writer, WRITERS);
		GroupTask.add(reader);
		GroupTask.execute();
		
		System.out.println("Null polls: " + nullCounter.get());
	}
	
	@Test
	public void testSingleWriterManyReads()
	{
		final int MESSAGES = 10000;
		final int READERS = 10;
		final int MESSAGES_PER_READER = MESSAGES / READERS;

		final AtomicInteger nullCounter = new AtomicInteger();
		final BlockableQueue<Integer> q = new BlockableQueue<Integer>();
		q.setBlocking(true);
		
		Runnable reader = new Runnable() {
			public void run() {
				int total = 0;
				double minWait = Double.MAX_VALUE;
				double maxWait = -Double.MAX_VALUE;
				Stopwatch watch = new Stopwatch();
				while (total < MESSAGES_PER_READER) {
					watch.start();
					Integer value = q.poll();
					watch.stop();
					if (value != null) {
						minWait = Math.min(minWait, watch.seconds());
						maxWait = Math.max(maxWait, watch.seconds());
						total++;	
					} else {
						nullCounter.incrementAndGet();
					}
				}
				System.out.format("Wait: min[%.6fs] max[%.6fs]\n", minWait, maxWait);
			}
		};
		
		Runnable writer = new Runnable() {
			public void run() {
				for (int i = 0; i < MESSAGES; i++) {
					assertTrue( q.offer(i) );
				}
			}
		};
		
		GroupTask.initialize(READERS + 1);
		GroupTask.add(reader, READERS);
		GroupTask.add(writer);
		GroupTask.execute();
		
		System.out.println("Null polls: " + nullCounter.get());
	}
	
}
