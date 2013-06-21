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
import org.magnos.util.State;



public class TestState extends BaseTest 
{


	/**
	 * Statuses {
	 *   Inactive,
	 *   Active {Waiting, Running},
	 *   Complete {Error, Success}
	 * }
	 */
	private final int Inactive = State.create(0);
	private final int Active = State.create(1);
	private final int Waiting = State.create(2);	// sub Active
	private final int Running = State.create(3); 	// sub Active
	private final int Complete = State.create(4);
	private final int Error = State.create(5); 		// sub Complete
	private final int Success = State.create(6); 	// sub Complete
	
	
	@Test
	public void testConstructor()
	{
		State s = new State(Success);
		
		assertEquals( Success, s.get() );
	}
	
	@Test
	public void testSetGet()
	{
		State s = new State();
		
		s.set(Inactive);
		 
		assertEquals( Inactive, s.get() );
		
		s.set(Active | Running);
		
		assertEquals( Active | Running, s.get() );
	}
	
	@Test
	public void testHas()
	{
		State s = new State();
		
		s.set(Complete | Error);
		
		assertTrue( s.has(Complete) );
		assertTrue( s.has(Error) );
		assertTrue( s.has(Complete | Error) );
		assertTrue( s.has(Complete | Active | Waiting) );
		
		assertFalse( s.has(Active) );
		assertFalse( s.has(Inactive) );
		assertFalse( s.has(Inactive | Active | Waiting | Running) );
	}
	
	@Test
	public void testEquals()
	{
		State s = new State();
		
		s.set(Active | Running);

		assertTrue( s.equals(Active | Running) );
		
		assertFalse( s.equals(Active) );
		assertFalse( s.equals(Running) );
		assertFalse( s.equals(Inactive) );
		assertFalse( s.equals(Active | Running | Waiting) );
	}
	
	@Test
	public void testStates()
	{
		State s = new State();
	
		assertEquals( 0, s.states() );
		
		s.set(Active);
		
		assertEquals( 1, s.states() );
		
		s.add(Running);
		
		assertEquals(2, s.states() );
		
		s.add(Waiting);
		
		assertEquals( 3, s.states() );
		
		s.set(Complete | Error);
		
		assertEquals( 2, s.states()  );
	}
	
	@Test
	public void testClear()
	{
		State s = new State();
		
		s.set(Active | Running);
		
		assertEquals( 2, s.states() );
		
		s.clear();
		
		assertEquals( 0, s.states() );
		assertEquals( 0, s.get() );
	}
	
	@Test
	public void testRemove()
	{
		State s = new State();
		
		s.set(Active | Running);
		
		assertTrue( s.equals(Active | Running) );

		s.remove(Running);
		
		assertTrue( s.equals(Active) );
		
		s.remove(Active);
		
		assertTrue( s.equals(0) );
	}

	@Test
	public void testCas()
	{
		State s = new State();
		
		s.set(Inactive);
		
		assertFalse( s.cas(Active | Running, Complete) );
		assertFalse( s.equals(Complete) );
		
		assertTrue( s.cas(Inactive | Active, Complete | Error) );
		assertTrue( s.equals(Complete | Error) );
	}
	
	@Test
	public void testWaitFor()
	{
		final State s = new State();
		s.set(Inactive);
		
		GroupTask.initialize(2);
		
		GroupTask.add(new Runnable() {
			public void run() {
				watch.start("Waiting for active...");
				assertTrue( s.waitFor(Active) );
				watch.stop(" reached in %.3fs\n");
				assertTrue( s.has(Active) );
			}
		});
		
		GroupTask.add(new Runnable() {
			public void run() {
				sleep(500);
				s.set(Active);
			}
		});
		
		GroupTask.execute();
	}
	
	@Test
	public void testTimeout()
	{
		final State s = new State();
		s.set(Inactive);
		
		GroupTask.initialize(1);
		
		GroupTask.add(new Runnable() {
			public void run() {
				watch.start("Waiting for active...");
				assertFalse( s.waitFor(Active, 500) );
				watch.stop(" timed out after %.3fs\n");
				assertFalse( s.has(Active) );
			}
		});
		
		GroupTask.execute();
	}

	
	@Test
	public void testTimeoutInterruption()
	{

		final State s = new State();
		
		s.set(Active | Running);
		
		Thread t = new Thread(new Runnable() {
			public void run() {
				assertFalse( s.waitFor(Complete, 500) );
			}
		});
		t.start();
		
		sleep(250);
		
		t.interrupt();
		
		sleep(500);
	}
	
	@Test(expected = RuntimeException.class)
	public void testCreateFail() {
		State.create(32);
	}
	
	@Test
	public void testLock() {
		State s = new State();
		
		synchronized (s) {
			s.set(Inactive);
		}
		
		assertTrue(s.equals(Inactive));
	}
	
	@Test
	public void waitForInterrupt()
	{
		final State s = new State();
		
		s.set(Active | Running);
		
		Thread t = new Thread(new Runnable() {
			public void run() {
				assertFalse( s.waitFor(Complete) );
			}
		});
		
		t.start();
		
		sleep(500);
		
		t.interrupt();
		
		sleep(500);
	}
	
	@Test
	public void testWaitForChange()
	{
		final State s = new State();
		s.set(Active | Waiting);
		
		GroupTask.initialize(1);
		GroupTask.add(new Runnable() {
			public void run() {
				assertEquals(Complete | Success, s.waitForChange());
			}
		});
		GroupTask.begin();
		
		sleep(500);
		
		s.set(Complete | Success);
		
		GroupTask.finish();
	}
	
	@Test
	public void testWaitForChangeInterrupt()
	{

		final State s = new State();
		
		s.set(Active | Running);
		
		Thread t = new Thread(new Runnable() {
			public void run() {
				assertEquals( Active | Running, s.waitForChange() );
			}
		});
		t.start();
		
		sleep(500);
		
		t.interrupt();
		
		sleep(500);
	}
	
	@Test
	public void testLazy()
	{
		State s = new State();
		
		synchronized (s) {
			s.lazySet(Active | Running);
			
			assertEquals( Active | Running, s.lazyGet() );
			
			s.lazyRemove(Running);
			
			assertEquals( Active, s.lazyGet() );
			
			s.lazyAdd(Running);
			
			assertEquals( Active | Running, s.lazyGet() );
			
			s.wakeup();
		}
	}
	
}
