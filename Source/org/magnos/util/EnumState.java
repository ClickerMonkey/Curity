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
 * A Thread-safe Finite State Machine utility. The control of he states this 
 * machine holds is the responsibility of the developer. This machine can
 * hold one state at a time. Invokers can wait for states to be reached, and 
 * can timeout if a maximum wait time is given. 
 * 
 * <h1>Example Usage</h1>
 * <pre>
 * public enum ProcessState {
 * 	Initialized, Running, Stopped, Error, Success
 * }
 * 
 * EnumState&ltProcessState&gt s = new EnumState&ltProcessState&gt(ProcessState.Initialized);
 * 
 * if (s.cas(ProcessState.Initialize, ProcessState.Running)) {
 * 	// updated!
 * }
 * 
 * // locks entirely for more advanced state manipulation
 * synchronized (s) {
 * 	if (s.equals(ProcessState.Running)) {
 * 		try {
 * 			// do something (small) that could throw an error
 * 			s.set(ProcessState.Success);
 * 		}
 * 		catch (Exception e) {
 * 			s.set(ProcessState.Error);
 * 		}
 * 		if (s.equals(Error)) {
 * 			// An error occurred!
 * 		}
 * 	}
 * }
 * </pre>
 * 
 * @author Philip Diffenderfer
 * 
 */
public class EnumState<E extends Enum<E>>
{
	
	// The current state(s) of the machine.
	private volatile E state;
	
	/**
	 * Instantiates a new State machine.
	 */
	public EnumState() 
	{
		
	}
	
	/**
	 * Instantiates a new State machine given its initial state.
	 * 
	 * @param initialState
	 * 		The initial state(s) of the machine.
	 */
	public EnumState(E initialState) 
	{
		state = initialState;
	}
	
	/**
	 * Overrides the current state with the given state. If any thread is 
	 * waiting for the provided state they will be notified and that thread
	 * will resume execution. 
	 * 
	 * @param newState
	 * 		The new state of the machine.
	 */
	public void set(E newState) 
	{
		synchronized (this) {
			state = newState;
			this.notifyAll();
		}
	}
	
	/**
	 * Removes state (set to null) from this machine.
	 */
	public void clear() 
	{
		synchronized (this) {
			state = null;
			this.notifyAll();
		}
	}
	
	/**
	 * Returns the state of the machine.
	 * 
	 * @return
	 * 		The current state of the machine.
	 */
	public E get() 
	{
		synchronized (this) {
			return state;
		}
	}
	
	/**
	 * Determines whether this machine has the exact states given.
	 * 
	 * @param exactState
	 * 		The state this machine must have exactly.
	 * @return
	 * 		True if this machine has the exact state, otherwise false.
	 */
	public boolean equals(E exactState) 
	{
		synchronized (this) {
			return (state == exactState);
		}
	}
	
	/**
	 * Performs a Compare-And-Set on the state. If this machine has any of the
	 * desired states then this machine's state will be set to the newState.
	 * 
	 * <h1>Example</h1>
	 * <pre>
	 * EnumState&ltProcessState&gt s = ...
	 * // if the state is running or waiting, set it to paused
	 * if (s.cas(ProcessState.Running, ProcessState.Paused)) {
	 * 	// state has been successfully paused!
	 * }
	 * </pre>
	 * 
	 * @param desiredState
	 * 		The set of states to check if any exist.
	 * @param newState
	 * 		The new state(s) of the machine.
	 * @return
	 * 		True if this machine was set to the new state(s).
	 */
	public boolean cas(E desiredState, E newState) 
	{
		synchronized (this) {
			boolean equals = lazyEquals(desiredState); 
			if (equals) {
				state = newState;
				this.notifyAll();
			}
			return equals;
		}
	}
	
	/**
	 * Indefinitely waits for the given state to be reached. If the current
	 * thread is interrupted this will return before the state is reached.
	 * 
	 * @param desiredState
	 * 		The state to wait for.
	 * @return
	 * 		True if the given state is reached, otherwise false.
	 */
	public boolean waitFor(E desiredState) 
	{
		synchronized (this) {
			// Continue while any of the desired states are no reached.
			while (!lazyEquals(desiredState)) {
				try {
					// Wait for a change in state.
					this.wait();
				} catch (InterruptedException e) {
					// Ignore interruption, but let it recurse.
					Thread.currentThread().interrupt();
					break;
				}
			}
			// Return whether any of the desired states were reached.
			return lazyEquals(desiredState);
		}
	}

	/**
	 * Waits for the given state to be reached, or for a specific amount of
	 * time to elapse. If the current thread is interrupted or the thread has
	 * waited more than the provided timeout time this will return before the 
	 * state is reached.
	 * 
	 * @param desiredState
	 * 		The state to wait for.
	 * @param timeout
	 * 		The maximum amount of time in milliseconds to wait for the given 
	 * 		state.
	 * @return
	 * 		True if the given state is reached, otherwise false.
	 */
	public boolean waitFor(E desiredState, long timeout) 
	{
		// Get start time before acquiring the lock.
		final long startTime = System.currentTimeMillis();
		synchronized (this) 
		{
			// Continue while any of the desired states are no reached.
			while (!lazyEquals(desiredState)) 
			{
				// The number of milliseconds to wait this iteration...
				long remaining = timeout - (System.currentTimeMillis() - startTime);
				// If its less than or equal to zero, timeout!
				if (remaining <= 0) {
					break;
				}
				try {
					// Wait for a change in state.
					this.wait(remaining);
				}
				catch (InterruptedException e) {
					break;
				}
			}
			// Return whether any of the desired states were reached.
			return lazyEquals(desiredState);
		}
	}
	
	/**
	 * Notifies all threads waiting for a state. The waiting threads will be
	 * woken and one at a time they will check the state of the machine for
	 * their desired state.
	 */
	public void wakeup()
	{
		synchronized (this) {
			this.notifyAll();
		}
	}
	
	/**
	 * Overrides the current state with the given state. This will not obtain
	 * the state machine lock before setting the state. This should only be
	 * used if the invoking thread already has synchronized on this state
	 * state and will invoke wakeup() before the synchronized block ends.
	 * 
	 * <h1>Example</h1>
	 * <pre>
	 * EnumState&ltProcessState&gt s = ...
	 * synchronized (s) {
	 * 	s.lazySet(ProcessState.Running);
	 * 	// other processing...
	 * 	s.wakeup();
	 * }
	 * </pre>
	 * 
	 * @param newState
	 * 		The new state of the machine.
	 */
	public void lazySet(E newState) 
	{
		state = newState;
	}
	
	/**
	 * Compares the current state to the given state without blocking.
	 * 
	 * @return 
	 * 		True if the current state matches the given state.
	 */
	public boolean lazyEquals(E otherState)
	{
		return (state == otherState);
	}
	
	/**
	 * Returns the state of the machine. This will not obtain the state machine 
	 * lock before setting the state. This should only be used if the invoking 
	 * thread already has synchronized on this state.
	 * 
	 * <h1>Example</h1>
	 * <pre>
	 * EnumState&ltProcessState&gt s = ...
	 * synchronized (s) {
	 * 	ProcessState state = s.lazyGet();
	 * 	// other processing...
	 * }
	 * </pre>
	 * 
	 * @return
	 * 		The current state of the machine.
	 */
	public E lazyGet() 
	{
		return state;
	}
	
	/**
	 * Waits for any change in state and returns the new state. If the current
	 * thread is interrupted this may return the original state. This may be
	 * useful if a thread is waiting for a specific state.
	 * 
	 * <h1>Example</h1>
	 * <pre>
	 * EnumState&ltProcessState&gt s = ...
	 * synchronized (s) {
	 * 	while (!s.equals(ProcessState.Waiting)) {
	 * 		s.waitForChange();
	 * 	}
	 * }
	 * </pre>
	 * 
	 * @return
	 * 		The state of the machine.
	 */
	public E waitForChange() 
	{
		synchronized (this) {
			try {
				this.wait();
			}
			catch (InterruptedException e) {
				// Interruptions stop waiting
			}	
			return state;
		}
	}
	
}
