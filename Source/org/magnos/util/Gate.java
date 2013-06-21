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

/**
 * A gate will block invokers until it is opened. Once a gate is opened, all
 * invokers will be notified and their execution will resume. Invokers may also
 * wait at the gate maximum amount of time before they give up waiting for it
 * to open (a timeout). A gate can be closed after it is open but typical uses
 * of a gate will only open it once. A gate by default is closed. The waiters
 * of a gate can be notified to wake-up, meaning cease waiting for the gate to
 * open and just resume execution even though the gate is closed. A gate also
 * can hold a given value and waiters can try to acquire that item by waiting
 * for the gate to open before the item is returned.
 *
 * @author Philip Diffenderfer
 *
 * @param <T>
 * 		The item type.
 */
public class Gate<T>
{

	// The item the gate is holding behind it.
	private final T item;

	// Whether the gate is open or closed.
	private volatile boolean closed = true;


	/**
	 * Instantiates a new Gate with no item.
	 */
	public Gate()
	{
		this.item = null;
	}

	/**
	 * Instantiates a new Gate.
	 *
	 * @param item
	 * 		The item the gate is holding behind it.
	 */
	public Gate(T item)
	{
		this.item = item;
	}

	/**
	 * Returns the item held by the gate. This will not wait for the gate to be
	 * opened.
	 *
	 * @return
	 * 		The reference to the item held by the gate.
	 */
	public T get()
	{
		return item;
	}

	/**
	 * Returns the item held by the gate waiting if necessary until the gate is
	 * open. If the gate was not be opened then null will be returned.
	 *
	 * @return
	 *		The reference to the item held by the gate, or null if the gate
	 *		was not be opened.
	 */
	public T acquire()
	{
		return (await(0) ? item : null);
	}

	/**
	 * Returns the item held by the gate waiting if necessary until the gate
	 * is open or a maximum amount of time has elapsed. If the gate was not
	 * opened then null will be returned.
	 *
	 * @param timeout
	 * 		The maximum amount of time in milliseconds to wait for the gate
	 * 		to open.
	 * @return
	 *		The reference to the item held by the gate, or null if the gate
	 *		was not be opened in time.
	 */
	public T acquire(long timeout)
	{
		return (await(timeout) ? item : null);
	}

	/**
	 * Returns whether the gate is closed. When this method returns the gate
	 * could toggle openness making the value returned unreliable.
	 *
	 * @return
	 *		True if the gate is currently closed, otherwise false.
	 */
	public boolean isClosed()
	{
		return closed;
	}

	/**
	 * Returns whether this gate is open. When this method returns the gate
	 * could toggle openness making the value returned unreliable.
	 *
	 * @return
	 * 		True if the gate is currently open, otherwise false.
	 */
	public boolean isOpen()
	{
		return !closed;
	}

	/**
	 * Waits an indefinite amount of time for the gate to open and returns
	 * whether the gate is now open. The invoking thread may be awaken
	 * (another thread invokes wake-up) before the gate is opened. When this
	 * method returns the gate could toggle openness making the value returned
	 * unreliable.
	 *
	 * @return
	 * 		True if the gate is currently open, otherwise false.
	 */
	public boolean await()
	{
		return await(0);
	}

	/**
	 * Waits a maximum amount of time for the gate to open and returns
	 * whether the gate is now open. The invoking thread may be awaken
	 * (another thread invokes wake-up) before the gate is opened. When this
	 * method returns the gate could toggle openness making the value returned
	 * unreliable.
	 *
	 * @param timeout
	 * 		The maximum amount of time in milliseconds to wait for the gate
	 * 		to open.
	 * @return
	 * 		True if the gate is currently open, otherwise false.
	 */
	public boolean await(long timeout)
	{
		if (closed) {
			synchronized (this) {
				if (closed) {
					try {
						this.wait(timeout);
					}
					catch (InterruptedException e) {
						// Ignore interruption, but let it recurse.
						Thread.currentThread().interrupt();
					}
				}
			}
		}
		return !closed;
	}

	/**
	 * Opens the gate if its currently closed and notifies all waiters.
	 */
	public void open()
	{
		if (closed) {
			closed = false;
			synchronized (this) {
				this.notifyAll();
			}
		}
	}

	/**
	 * Closes the gate.
	 */
	public void close()
	{
		closed = true;
	}

	/**
	 * Forces threads to stop waiting for the gate to open if its closed.
	 */
	public void wakeup()
	{
		if (closed) {
			synchronized (this) {
				if (closed) {
					this.notifyAll();
				}
			}
		}
	}


}
