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

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * An efficient reentrant read-write lock on a reference. This class ensures 
 * writes to the referenced value are thread-safe by using the lock and unlock
 * methods. This lock prefers writers over readers, meaning all writes must 
 * finish before reading can occur. Being a reentrant lock the thread which is
 * holding the lock can call the lock and unlock methods without causing 
 * deadlocking. Writes are on a first come first serve basis.
 * 
 * <h6>Writing with set</h6>
 * <pre>
 * LockRef&lt;String&gt; ref = new LockRef("Hello World");
 *  
 * ref.lock();
 * try {
 *		// compute new value (an error could occur here, hence the finally block)
 *	 	String newValue = ...
 *		// sets the new value by doing a nested lock and unlock
 *	 	ref.set(newValue);
 * }
 * finally {
 * 		ref.unlock();
 * }
 * </pre>
 * <h6>Writing with unlock</h6>
 * <pre>
 * try {
 * 		ref.lock();
 * 		// compute new value (an error could occur here, hence the unlock in the catch)
 * 		String newValue = ...
 *		// unlock and set the new value
 * 		ref.unlock(newValue);
 * }
 * catch (Exception e) {
 * 		ref.unlock();
 * 		// [handle exception]
 * }
 * </pre>
 * <h6>Reading</h6>
 * <pre>
 * String value = ref.get();
 * </pre>
 * 
 * @author Philip Diffenderfer
 *
 * @param <E>
 * 		The value type.
 */
public class LockRef<E> implements Ref<E>
{
	
	// A write barrier for the reference. This is locked to make readers block 
	// when a writer is holding a lock.
	private final ReentrantLock barrier = new ReentrantLock();
	
	// The number of writes currently happening. If this is greater than 1 other
	// writers are waiting for the first writer to unlock the barrier.
	private final AtomicInteger writes = new AtomicInteger();

	// The signal to send when a new value has been set.
	private final Signal writeSignal = new Signal();
	
	// The value referenced.
	private volatile E value;


	/**
	 * Instantiates a new Ref with an initial value of null.
	 */
	public LockRef() 
	{
	}
	
	/**
	 * Instantiates a new Ref given an initial value.
	 *
	 * @param initialValue
	 * 		The initial referenced value.
	 */
	public LockRef(E initialValue) 
	{
		value = initialValue;
	}

	/**
	 * Locks the reference to the value so reads of the value will block until
	 * unlock is invoked. The current thread must call an unlock for each lock
	 * it acquires.The locking mechanism for this class is not reentrant.
	 * 
	 * @return
	 * 		The reference to the current value.
	 */
	public final E lock() 
	{
		writes.incrementAndGet();
		barrier.lock();
		return value;
	}


	/**
	 * Unlocks the reference to the value so reads can now be made to that
	 * value. The current thread must call this after each lock made. The
	 * locking mechanism for this class is not reentrant.
	 */
	public final void unlock() 
	{
		barrier.unlock();
		writes.decrementAndGet();
	}


	/**
	 * Unlocks the reference to the value so reads can now be made to that
	 * value. The current thread must call this after lock() was invoked. The
	 * locking mechanism for this class is not reentrant.
	 *
	 * @param newValue
	 * 		The new value to set before the lock is released.
	 */
	public final void unlock(E newValue) 
	{
		// Only set the value and send the signal if its different.
		if (newValue != value) {
			value = newValue;
			writeSignal.send();	
		}
		unlock();
	}

	/**
	 * Safely sets the value. This will block any reads until this method exits.
	 * This method should not be called if the current thread has locked this
	 * reference. The locking mechanism for this class is not reentrant.
	 *
	 * @param newValue
	 * 		The new value to set.
	 */
	public final void set(E newValue) 
	{
		lock();
		unlock(newValue);
	}
	

	/**
	 * Returns the referred value. If the reference is currently locked this
	 * method will block until the holding thread unlocks this reference.
	 *
	 * @return
	 * 		The value.
	 */
	public final E get() 
	{
		// If writing is occurring continue to wait for it to finish. This will
		// let all writes complete before read occurs.
		while (writes.get() > 0) {
			barrier.lock();
			barrier.unlock();
		}
		return value;
	}
	
	/**
	 * Returns whether the value referenced has been set to a new value since
	 * the last time this method was invoked.
	 * 
	 * @return
	 * 		True if a new value has been set since the last time checked.
	 */
	public final boolean hasNewValue() 
	{
		return writeSignal.recieved();
	}
	
}
