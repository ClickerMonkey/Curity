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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * A thread safe set of objects. This provides better performance then any naive
 * approach to synchronize an entire set by only synchronizing the chain which
 * the element exists on or is being added to. A set can be iterated but the
 * elements it iterates are a snapshot of the set at the time of invocation.
 * The methods to return the size or determine whether the set is empty should
 * be used sparingly since they iterate over the entire set to approximate the
 * size (design choice to avoid locking entire set or using AtomicInteger).
 * Once a set has been created its internally table cannot and will not
 * change size. No matter the minimum table size given to the constructor the
 * internal table size of the set will always be a power of 2.
 * 
 * @author Philip Diffenderfer
 *
 * @param <E>
 * 		The element to contain in the set.
 */
public class ConcurrentSet<E> implements Set<E>
{
	
	/**
	 * A node that holds an element and a pointer.
	 * 
	 * @author Philip Diffenderfer
	 *
	 */
	private class Node<T>
	{
		private final T element;
		private Node<T> next;
		public Node(T element, Node<T> next) {
			this.element = element;
			this.next = next;
		}
	}
	
	// The table of nodes.
	private final Node<E>[] table;
	
	// The number of nodes in the table.
	private final int capacity;
	
	// A value used to calculate the index of an object on the table given
	// its hashCode. This is essentially capacity - 1.
	private final int mod;

	
	/**
	 * Instantiates a ConcurrentSet with a table size of 32.
	 */
	public ConcurrentSet() 
	{
		this(32);
	}
	
	/**
	 * Instantiates a ConcurrentSet.
	 * 
	 * @param minTableSize
	 * 		The minimum size of the internal hash table. The actual size of the
	 * 		internal hash table will be calculated by finding the next highest
	 * 		power of 2 number.
	 */
	@SuppressWarnings("unchecked")
	public ConcurrentSet(int minTableSize) 
	{
		this.capacity = powerOf2(minTableSize);
		this.mod = capacity - 1;
		this.table = (Node<E>[])Array.newInstance(Node.class, capacity);
		for (int i = 0; i < capacity; i++) {
			table[i] = new Node<E>(null, null);
		}
	}
	
	/**
	 * Returns a power of 2 number greater than or equal to x.
	 * 
	 * @param x
	 * 		The number in question.
	 * @return
	 * 		A power of 2 number >= x.
	 */
	private final int powerOf2(int x) 
	{
		return (Integer.highestOneBit(x - 1) << 1);
	}
	
	/**
	 * Returns the index in the table an object should go with the given hash.
	 * 
	 * @param hash
	 * 		The hash of the object.
	 * @return
	 * 		The index in the table, 0 <= i < capacity.
	 */
	private final int index(int hash) 
	{
		return (hash & mod);
	}
	
	/**
	 * Returns the size of the internal hash table.
	 * 
	 * @return
	 * 		The size of the internal hash table.
	 */
	public final int getCapacity()
	{
		return capacity;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean add(E e) 
	{
		if (e != null) {
			int i = index(e.hashCode());
			synchronized (table[i]) {
				table[i].next = new Node<E>(e, table[i].next);
			}
		}
		return (e != null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean addAll(Collection<? extends E> c) 
	{
		boolean added = false;
		for (E e : c) {
			added |= add(e);
		}
		return added;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clear() 
	{
		for (int i = 0; i < capacity; i++){ 
			synchronized (table[i]) {
				table[i].next = null;
			}
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean contains(Object o) 
	{
		if (o != null) {
			int i = index(o.hashCode());
			synchronized (table[i]) {
				Node<E> c = table[i].next;
				while (c != null) {
					if (c.element == o || c.element.equals(o)) {
						return true;
					}
					c = c.next;
				}
			} 
		}
		return false;
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean containsAll(Collection<?> c) 
	{
		for (Object o : c) {
			if (!contains(o)) {
				return false;
			}
		}
		return true;
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean remove(Object o) 
	{
		if (o != null) {
			int i = index(o.hashCode());
			synchronized (table[i]) {
				Node<E> p = table[i], n = p.next;
				while (n != null) {
					if (n.element == o || n.element.equals(o)) {
						p.next = n.next;
						return true;
					}
					p = n;
					n = n.next;
				}
			}
		}
		return false;
	}
	
	/**
	 * Removes all objects from this set that are equivalent to the given item.
	 * 
	 * @param o
	 * 		The object to completely remove.
	 */
	public void purge(Object o)
	{
		while (remove(o)) {
			// Do nothing, just wait for all instances to be removed.
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean removeAll(Collection<?> c) 
	{
		boolean removed = false;
		for (Object o : c) {
			removed |= remove(o);
		}
		return removed;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean retainAll(Collection<?> c) 
	{
		boolean retained = false;
		for (int i = 0; i < capacity; i++) {
			synchronized (table[i]) {
				Node<E> p = table[i], n = p.next;
				while (n != null) {
					if (!c.contains(n.element)) {
						p.next = n.next;
						retained = true;
					}
					else {
						p = n;
					}
					n = n.next;
				}
			}
		}
		return retained;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int size() 
	{
		return toList().size();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isEmpty() 
	{
		return toList().isEmpty();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterator<E> iterator() 
	{
		return toList().iterator();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object[] toArray() 
	{
		return toList().toArray();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> T[] toArray(T[] a) 
	{
		return toList().toArray(a);
	}

	
	/**
	 * Returns the contents of this set as a list. The built list is a snapshot
	 * of the set at the time of invocation, therefore if the list is going
	 * to be iterated in any fashion some to all of its elements may no longer
	 * actually exist in the set.
	 * 
	 * @return
	 * 		The reference to the list of elements in this set.
	 */
	public List<E> toList() 
	{
		ArrayList<E> elements = new ArrayList<E>(capacity);
		for (int i = 0; i < capacity; i++) {
			synchronized (table[i]) {
				Node<E> c = table[i].next;
				while (c != null) {
					elements.add(c.element);
					c = c.next;
				}
			}
		}
		return elements;
	}


}
