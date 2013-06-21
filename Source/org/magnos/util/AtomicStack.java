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

import java.util.concurrent.atomic.AtomicReference;

/**
 * An implementation of a Lock-Free Stack. This implementation only provides a
 * guarantee for the push and pop operations. The peek and size operations are
 * only valid at the exact instance they are invoked, as soon as a result 
 * returns to the invokers that information could be out dated.
 * 
 * TODO: find Wait-Free stack.
 * 
 * @author Philip Diffenderfer
 *
 * @param <E>
 * 		The element type.
 */
public class AtomicStack<E>
{
	
	/**
	 * A node in the AtomicStack.
	 * 
	 * @author Philip Diffenderfer
	 *
	 * @param <E>
	 * 		The element type.
	 */
	static class Node<E> 
	{
		private final E element;
		private Node<E> next;

		public Node(E element) {
			this.element = element;
		}
	}

	// The head of the stack
	private AtomicReference<Node<E>> head = new AtomicReference<Node<E>>();

	
	/**
	 * Instantiates a new AtomicStack.
	 */
	public AtomicStack()
	{
		
	}
	
	/**
	 * Pushes the given element on top of the stack.
	 * 
	 * @param element
	 * 		The element to push on the stack.
	 */
	public void push(E element)
	{
		Node<E> newHead = new Node<E>(element);
		Node<E> oldHead;
		do {
			oldHead = head.get();
			newHead.next = oldHead;
		} while (!head.compareAndSet(oldHead, newHead));
	}
	
	/**
	 * Pops an element from the stop of the stack.
	 * 
	 * @return
	 * 		The element popped from the stack.
	 */
	public E pop()
	{
		Node<E> newHead;
		Node<E> oldHead;
		do {
			oldHead = head.get();
			if (oldHead == null) {
				return null;
			}
			newHead = oldHead.next;
		} while (!head.compareAndSet(oldHead, newHead));
		return oldHead.element;
	}
	
	/**
	 * Returns the element on the top of the stack. By the time this method 
	 * returns the element may already have been popped off of the stack or 
	 * another element has been pushed on top of it.
	 * 
	 * @return
	 * 		The element at the top of the stack.
	 */
	public E peek()
	{
		Node<E> currentHead = head.get();
		if (currentHead == null) {
			return null;
		}
		return currentHead.element;
	}
	
	/**
	 * Returns the approximate size of the stack. This will iterate through the
	 * nodes in the stack and for each element a counter will be incremented by
	 * one. Its possible that if this method returns some number x that all x
	 * nodes could have been popped off by the time this method returns (meaning
	 * the current size of the stack is actually zero opposed to x).
	 * 
	 * @return
	 * 		The number of elements in the stack.
	 */
	public int size()
	{
		int size = 0;
		for (Node<E> current = head.get(); current != null; current = current.next) {
			size++;
		}
		return size;
	}

}
