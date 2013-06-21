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
 * Provides a way for several threads to send signals between each other. The
 * send method will return the number of unrecieved signals have been sent and
 * it will increment the number of signals. The recieve method will return
 * the number of signals sent while reseting the signal counter back to zero. If
 * a signals are being sent during a receive they will not be overriden, receive
 * will only return the number of signals sent at the beginning of the method
 * invocation. In other words no signals will be lost by receiving because it
 * doesn't technically reset it back to zero. The recieved method returns if
 * more than 0 signals have been sent. Any thread can send and receive signals.
 * 
 * @author Philip Diffenderfer
 *
 */
public class Signal
{

	// The atomic number of signals sent and not yet received.
	private final AtomicInteger signals = new AtomicInteger();

	/**
	 * Sends a single signal and returns how many signals have not been received
	 * before the signal was sent.
	 *  
	 * @return
	 * 		The number of signals sent and not received before this invokation.
	 */
	public int send() 
	{
		return signals.getAndIncrement();
	}
	
	/**
	 * Recieves all signals sent and returns how many were sent. If signals are
	 * being sent while this method is executing they will remain in the signal
	 * counter and not be ignored.
	 * 
	 * @return
	 * 		The number of signals received
	 */
	public int receive() 
	{
		int total = signals.get();
		signals.getAndAdd(-total);
		return total;
	}
	
	/**
	 * Receives all signals sent and returns true if any signals were received.
	 * 
	 * @return
	 * 		True if signals were sent, false otherwise.
	 */
	public boolean recieved() 
	{
		return receive() > 0;
	}
	
}
