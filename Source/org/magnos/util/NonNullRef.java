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
 * A reference which blocks on get if the interval value is null. Once the value
 * is set to a non-null value the block is released.
 * 
 * @author Philip Diffenderfer
 *
 * @param <E>
 * 		The value type.
 */
public class NonNullRef<E> implements Ref<E>, Sleepable
{

	// The value of the reference.
	private volatile E value;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public E get() 
	{
		// double checking
		if (value == null) 
		{
			synchronized (this) 
			{
				if (value == null) 
				{
					try {
						this.wait();
					} catch (InterruptedException e) {
					}
				}
			}
		}
		return value;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void set(E newValue) 
	{
		// double checking
		if (newValue != null) 
		{
			if (value == null) 
			{
				synchronized (this) 
				{
					if (value == null) {
						this.notifyAll();
						this.value = newValue;
					}
				}
			}
		}
		this.value = newValue;	
	}
	
	/**
	 * Returns whether this reference has a non-null value. This method will
	 * not block. When the method returns the result may be innaccurate due
	 * to the nature of concurrent applications.
	 * 
	 * @return
	 * 		True if this reference has a non-null value, otherwise false.
	 */
	public boolean has() 
	{
		return (value != null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void awake() 
	{
		synchronized (this) {
			this.notifyAll();
		}
	}

}
