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
 * A reference to some value.
 * 
 * @author Philip Diffenderfer
 *
 * @param <E>
 * 		The value type.
 */
public interface Ref<E> 
{
	
	/**
	 * Returns the reference to the value. Depending on the implementation this
	 * method may block or cause additional computation, thus is not gauranteed 
	 * to return immediately after being invoked.
	 * 
	 * @return
	 * 		The reference to the value.
	 */
	public E get();
	
	/**
	 * Sets the reference to the value. Depending on the implementation this
	 * method may block or cause additional computation, thus is not gauranteed 
	 * to return immediately after being invoked.
	 * 
	 * @param value
	 * 		The new reference value to set.
	 */
	public void set(E value);
	
}
