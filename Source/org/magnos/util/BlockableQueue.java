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

import java.util.AbstractQueue;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

/**
 * A queue implementation which can toggle between blocking and non-blocking 
 * mode. The queue can be awoken from blocking by invoking the wakeup method
 * rather than the typical interrupt method. When in blocking mode the invoking
 * threads will wait on a peek or a poll if the queue is empty, until an element
 * is added to the queue. If the queue is not in blocking mode and it is empty
 * then null will be returned instantly when poll and peek are invoked.
 * 
 * @author Philip Diffenderfer
 *
 * @param <E>
 * 		The element type.
 */
public class BlockableQueue<E> extends AbstractQueue<E> 
{
	
	// The source queue containing the elements. This is typically a
	// ConcurrentLinkedQueue for its efficiency and safety but other Queues can
	// be given as a source queue.
	private final Queue<E> queue;
	
	// The maximum amount of time to suspend this thread waiting for an element
	// before it tries to restart if this queue is in blocking mode.
	private long timeout = Long.MAX_VALUE;
	
	// Whether this queue is in blocking mode. In blocking mode, peeks and polls
	// will wait for elements to arrive in the queue if none currently exist.
	private volatile boolean blocking = false;
	
	// The lock providing access to the waiting counter and blocking.
	private final Object lock = new Object();
	
	
	/**
	 * Instantiates a new BlockableQueue using the default ConcurrentLinkedQueue
	 * as the internal implementation.
	 */
	public BlockableQueue() 
	{
		this(new ConcurrentLinkedQueue<E>());
	}
	
	/**
	 * Instantiates a new BlockableQueue given an internal implementation of the
	 * queue.
	 * 
	 * @param source
	 * 		The queue implementation to use internally.
	 */
	public BlockableQueue(Queue<E> source) 
	{
		this.queue = source;
	}
	
	/**
	 * Sets this queue into blocking mode.
	 * 
	 * @param blocking
	 * 		Whether this queue should block on peeks and polls if no elements
	 * 		exist in the queue until elements are offered.
	 */
	public void setBlocking(boolean blocking) 
	{
		this.blocking = blocking;
	}
	
	/**
	 * Returns whether this queue is in blocking mode.
	 * 
	 * @return
	 * 		True if this queue is in blocking mode, otherwise false.
	 */
	public boolean isBlocking() 
	{
		return blocking;
	}
	
	/**
	 * Sets the timeout of the blocking in milliseconds. The blocking threads
	 * will wait a maximum of this amount of time before retrying polling or
	 * peeking elements.
	 * 
	 * @param millis
	 * 		The maximum number of milliseconds to block a thread before it retries.
	 */
	public void setTimeout(long millis) 
	{
		this.timeout = millis;
	}
	
	/**
	 * Sets the timeout of the blocking in the given unit. The blocking threads
	 * will wait a maximum of this amount of time before retrying polling or
	 * peeking elements.
	 * 
	 * @param time
	 * 		The amount of time.
	 * @param unit
	 * 		The unit of time.
	 */
	public void setTimeout(long time, TimeUnit unit) 
	{
		this.timeout = unit.toMillis(time);
	}
	
	/**
	 * The timeout of the blocking in milliseconds. The blocking threads
	 * will wait a maximum of this amount of time before retrying polling or
	 * peeking elements.
	 * 
	 * @return
	 * 		The timeout in milliseconds.
	 */
	public long getTimeout()
	{
		return timeout;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterator<E> iterator() 
	{
		return queue.iterator();
	}

	/**
	 * Returns the number of elements currently in the queue. This is a
	 * non-blocking method.
	 */
	@Override
	public int size() 
	{
		return queue.size();
	}

	/**
	 * Inserts the specified element into this queue. If this queue is in
	 * blocking mode and any peeks or polls are blocking this will notify
	 * those waiting that an element has been added. The receiver of the element
	 * being added is on a first come first serve basis.
	 * 
	 * @param e
	 * 		The element to add to the end of the queue.
	 */
	@Override
	public boolean offer(E e) 
	{
		boolean offered = queue.offer(e);
		if (blocking) {
			synchronized (lock) {
				lock.notify();
			}
		}
		return offered;
	}

	/**
	 * Returns, but does not remove, the head of this queue. If this queue is in
	 * blocking mode and the queue is empty the current thread will be paused
	 * until an element is added or this queue is awoken. If the queue is not
	 * in blocking mode and this queue is empty this will return null.
	 * 
	 * @return
	 * 		The element at the front of the queue.
	 */
	@Override
	public E peek() 
	{
		// This ensures a previous wakeup has ended.
		E item = queue.peek();
		if (blocking && item == null) {
			synchronized (lock) {
				// Only block if the item is null, else return it.
				item = queue.peek();
				if (item == null) {
					try {
						lock.wait(timeout);
					} catch (InterruptedException e) { }
					item = queue.peek();
				}
			}
		}
		return item;
	}


	/**
	 * Retrieves and removes the head of this queue. If this queue is in
	 * blocking mode and the queue is empty the current thread will be paused
	 * until an element is added or this queue is awoken. If the queue is not
	 * in blocking mode and this queue is empty this will return null.
	 * 
	 * @return
	 * 		The previous element in the front of the queue.
	 */
	@Override
	public E poll() 
	{
		E item = queue.poll();
		if (blocking && item == null) {
			synchronized (lock) {
				// Only block if the item is null, else return it.
				item = queue.poll();
				if (item == null) {
					try {
						lock.wait(timeout);
					} catch (InterruptedException e) { }
					item = queue.poll();
				}
			}
		}
		return item;
	}

	/**
	 * Sends a message to all blocking threads on poll and peek (given this
	 * queue is in blocking mode) to stop blocking and return null. The wakeup
	 * flag is set to true and is only reset to false once new invokations of
	 * peek and poll occur.
	 */
	public void wakeup() 
	{
		synchronized (lock) {
			lock.notifyAll();	
		}
	}

}