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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.magnos.test.BaseTest;
import org.magnos.util.ConcurrentSet;



public class TestConcurrentSet extends BaseTest 
{

	@Test
	public void testConstructor()
	{
		assertEquals( 32, new ConcurrentSet<Integer>().getCapacity() );
		assertEquals( 16, new ConcurrentSet<Integer>(16).getCapacity() );
		assertEquals( 64, new ConcurrentSet<Integer>(64).getCapacity() );
		assertEquals( 64, new ConcurrentSet<Integer>(63).getCapacity() );
		assertEquals( 64, new ConcurrentSet<Integer>(33).getCapacity() );
	}
	
	@Test
	public void testAdd()
	{
		ConcurrentSet<Integer> set = new ConcurrentSet<Integer>();

		assertTrue( set.add(0) );
		assertTrue( set.add(1) );
		assertTrue( set.add(2) );
		assertTrue( set.add(3) );
		assertFalse( set.add(null) );
		
		assertEquals( 4, set.size() );
	}
	
	@Test
	public void testClear()
	{
		ConcurrentSet<Integer> set = new ConcurrentSet<Integer>();

		assertTrue( set.add(0) );
		assertTrue( set.add(1) );
		assertTrue( set.add(2) );
		assertTrue( set.add(3) );
		assertEquals( 4, set.size() );
		
		set.clear();

		assertEquals( 0, set.size() );
	}
	
	@Test
	public void testContains()
	{
		ConcurrentSet<Integer> set = new ConcurrentSet<Integer>(4);

		assertTrue( set.add(0) );
		assertTrue( set.add(1) );
		assertTrue( set.add(2) );
		assertTrue( set.add(3) );
		assertTrue( set.add(4) );
		assertTrue( set.add(5) );
		assertTrue( set.add(6) );
		assertTrue( set.add(8) );
		assertTrue( set.add(9) );
		assertTrue( set.add(12) );

		assertEquals( 10, set.size() );

		assertFalse( set.contains(null) );
		assertFalse( set.contains(-2) );
		assertFalse( set.contains(-1) );
		assertTrue( set.contains(0) );
		assertTrue( set.contains(1) );
		assertTrue( set.contains(2) );
		assertTrue( set.contains(3) );
		assertTrue( set.contains(4) );
		assertTrue( set.contains(5) );
		assertTrue( set.contains(6) );
		assertFalse( set.contains(7) );
		assertTrue( set.contains(8) );
		assertTrue( set.contains(9) );
		assertFalse( set.contains(10) );
		assertFalse( set.contains(11) );
		assertTrue( set.contains(12) );
		assertFalse( set.contains(13) );
		assertFalse( set.contains(14) );
	}
	
	@Test
	public void testRemove()
	{
		ConcurrentSet<Integer> set = new ConcurrentSet<Integer>(4);

		assertEquals( 4, set.getCapacity() );
		
		set.add(0); set.add(4); set.add(8); set.add(12);
		set.add(1); set.add(5); set.add(9); 
		set.add(2); set.add(6); 
		set.add(3); 
		
		assertEquals( 10, set.size() );
		assertFalse( set.remove(null) );
		assertFalse( set.remove(-1) );
		assertFalse( set.remove(-2) );
		assertFalse( set.remove(10) );
		assertEquals( 10, set.size() );
	
		// Remove end (12)
		assertTrue( set.remove(12) );
		assertFalse( set.contains(12) );
		
		// Remove middle (4)
		assertTrue( set.remove(4) );
		assertFalse( set.contains(4) );
		
		// Remove first with next
		assertTrue( set.remove(2) );
		assertFalse( set.contains(2) );
		
		// Remove first without next
		assertTrue( set.remove(3) );
		assertFalse( set.contains(3) );

		assertEquals( 6, set.size() );
		assertTrue( set.contains(0) );
		assertTrue( set.contains(1) );
		assertTrue( set.contains(5) );
		assertTrue( set.contains(6) );
		assertTrue( set.contains(8) );
		assertTrue( set.contains(9) );
		
	}
	
	@Test
	public void testContainsAll()
	{
		ConcurrentSet<Integer> set = new ConcurrentSet<Integer>(4);

		set.add(0); set.add(4); set.add(8);
		set.add(1); set.add(5);
		set.add(2);

		assertTrue( set.containsAll(newList()) );
		assertTrue( set.containsAll(newList(0, 1, 2)) );
		assertTrue( set.containsAll(newList(0, 4, 8, 1, 5, 2)) );
		assertTrue( set.containsAll(newList(0)) );
		assertFalse( set.containsAll(newList(0, 1, 2, 3, 4)) );
		assertFalse( set.containsAll(newList(-1)) );
	}
	
	@Test
	public void testAddAll()
	{
		ConcurrentSet<Integer> set = new ConcurrentSet<Integer>(4);

		assertTrue( set.addAll(newList(0, 1, 2, 3, 4, 5, 8)) );
		assertTrue( set.containsAll(newList(0, 1, 2, 3, 4, 5, 8)) );
	}
	
	@Test
	public void testPurge()
	{
		ConcurrentSet<Integer> set = new ConcurrentSet<Integer>();

		assertTrue( set.add(0) );
		assertTrue( set.add(0) );
		assertTrue( set.add(0) );
		assertEquals( 3, set.size() );
		
		assertTrue( set.remove(0) );
		assertEquals( 2, set.size() );
		
		set.purge(0);
		assertEquals( 0, set.size() );
	}
	
	@Test
	public void testRemoveAll()
	{
		ConcurrentSet<Integer> set = new ConcurrentSet<Integer>(4);

		assertTrue( set.addAll(newList(0, 1, 2, 3, 4, 5, 8)) );
		assertFalse( set.removeAll(newList()));
		assertTrue( set.removeAll(newList(8, 4, 0, 2)));
		assertTrue( set.containsAll(newList(1, 3, 5)) );

		assertFalse( set.contains(8) );
		assertFalse( set.contains(4) );
		assertFalse( set.contains(0) );
		assertFalse( set.contains(2) );
	}
	
	@Test
	public void testRetainAll()
	{
		ConcurrentSet<Integer> set = new ConcurrentSet<Integer>(4);
		
		assertTrue( set.addAll(newList(0, 1, 2, 3, 4, 5, 8)) );
		assertTrue( set.retainAll(newList(0, 3, 4)));
		assertTrue( set.containsAll(newList(0, 3, 4)) );
		assertEquals( 3, set.size() );
		
		assertFalse( set.contains(1) );
		assertFalse( set.contains(2) );
		assertFalse( set.contains(5) );
		assertFalse( set.contains(8) );
	}
	
	@Test
	public void testToArray()
	{
		ConcurrentSet<Integer> set = new ConcurrentSet<Integer>(4);
		
		assertTrue( set.addAll(newList(0, 1, 2, 3, 4, 5, 8)) );
		assertEquals( 7, set.size() );
		
		Object[] data = set.toArray();
		Arrays.sort(data);
		
		assertEquals( 7, data.length );

		assertTrue( 0 <= Arrays.binarySearch(data, 0) );
		assertTrue( 0 <= Arrays.binarySearch(data, 1) );
		assertTrue( 0 <= Arrays.binarySearch(data, 2) );
		assertTrue( 0 <= Arrays.binarySearch(data, 3) );
		assertTrue( 0 <= Arrays.binarySearch(data, 4) );
		assertTrue( 0 <= Arrays.binarySearch(data, 5) );
		assertTrue( 0 <= Arrays.binarySearch(data, 8) );
	}
	
	private <T> Collection<T> newList(T ... elements) {
		List<T> list = new ArrayList<T>();
		for (T el : elements) {
			list.add(el);
		}
		return list;
	}
	
}
