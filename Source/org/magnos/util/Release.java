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

/**
 * A utility for unblocking blocking calls. This release will ensure blocking
 * sections cannot be entered while the release is locked, as well as unblock
 * the blockers when it is locked. Each blocker should add a Sleepable to the
 * Notifier to ensure the blockers can successfully be unblocked.
 * 
 * @author Philip Diffenderfer
 *
 */
public class Release 
{

	// Whether the release is currently being held in order
	private volatile boolean locked = false;
	
	// A list of things that invoke blockable calls. Each one can be awoken,
	// and when invoked they should release any blocking calls.
	private final Notifier<Sleepable> blockers;
	
	// The number of blockers currently in their blocking section.
	private final AtomicInteger holds;
	
	
	/**
	 * Instantiates a new Release. 
	 */
	public Release() 
	{
		blockers = Notifier.create(Sleepable.class);
		holds = new AtomicInteger();
	}
	
	/**
	 * Must be invoked when a blocker enters their blocking section. After the
	 * blocker is done blocking the exit method must be invoked. For every 
	 * invokation of enter there must be an invokation of exit, if not then
	 * the awaking thread will enter an infinite loop until the number of
	 * enter invokations is equal to the number of exit invokations. Invokers of
	 * the enter and exit methods must have a Sleepable added to the list of
	 * blockers to ensure they can be unblocked.
	 */
	public boolean enter() 
	{
		synchronized (this) 
		{
			// If locked quick get out!
			if (locked) {
				return false;
			}
			
			// One more blocker in its blocking section
			holds.incrementAndGet();
			
			return true;
		}
	}
	
	/**
	 * Must be invoked after a blocker has exited their blocking section. For 
	 * every invokation of enter there must be an invokation of exit, if not 
	 * then the awaking thread will enter an infinite loop until the number of
	 * enter invokations is equal to the number of exit invokations. Invokers of
	 * the enter and exit methods must have a Sleepable added to the list of
	 * blockers to ensure they can be unblocked.
	 */
	public void exit() 
	{
		synchronized (this) 
		{
			// One less blocker in its blocking section
			holds.decrementAndGet();
			
			// Notify this release of an exit if its waiting.
			this.notifyAll();
		}
	}
	
	/**
	 * Awakes all blockers in their blocking sections. This will loop infinitely
	 * awaking all blockers added to the notifier, while pausing inbetween.
	 * When this method returns all blockers will have exited their section.
	 */
	public void awake() 
	{
		synchronized (this) 
		{
			// Continue to loop until all blockers have exited their section.
			while (holds.get() > 0) 
			{
				// Awake all blockers!
				blockers.proxy().awake();
				
				// Wait at most 1 millisecond or for a notification.
				try {
					this.wait(1);
				} catch (Exception e) {
				}
			}
		}
	}
	
	/**
	 * Locks this release so that any blockers do not enter their blocking 
	 * section and the owner of the lock can perform the awake method safely.
	 * A single thread should be locking and unlocking a Release, anymore will
	 * cause undesirable problems.
	 */
	public void lock() 
	{
		synchronized (this) {
			locked = true;	
		}
	}
	
	/**
	 * Unlocks this release so that any blockers can now reenter their blocking
	 * section. This is typically done after the release has awaken the blockers
	 * and has appropriately handled them. A single thread should be locking and 
	 * unlocking a Release, anymore will cause undesirable problems.
	 */
	public void unlock() 
	{
		synchronized (this) {
			locked = false;	
		}
	}
	
	/**
	 * Returns whether a thread has control over the blockers and is currently
	 * denying them entrance to their blocking section.
	 * 
	 * @return
	 * 		True if blockers cannot enter their blocking section.
	 */
	public boolean isLocked() 
	{
		return locked;
	}

	/**
	 * Returns the notifier which manages the blockers to awake. Sleepables can 
	 * be directly added and removed to the notifier. Avoid invoking the methods
	 * of the proxy object in the notifier since it may notify the blockers 
	 * falsely when an update has not actually occurred with the strategy. 
	 * 
	 * @return
	 * 		The reference to the Sleepable notifier.
	 */
	public Notifier<Sleepable> getBlockers() 
	{
		return blockers;
	}
	
}
