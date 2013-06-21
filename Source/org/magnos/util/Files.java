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

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

/**
 * A simple utility for copying file and folder structures.
 * 
 * @author Philip Diffenderfer
 *
 */
public class Files 
{

	/**
	 * Copies the source file to the destination file. If the source file is a
	 * single file it will be placed in the destination directory or file. 
	 * If the source file is a directory it will copied to the given destination
	 * directory. If the destination directory does not exist it and all 
	 * required directories will attempted to be created.
	 * 
	 * @param src
	 * 		The source to copy.
	 * @param dst
	 * 		The destination of the copy.
	 * @throws IOException
	 * 		An exception occurred copying.
	 */
	public static void copy(File src, File dst) throws IOException 
	{
		copy(src, dst, new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return true;
			}
		});
	}
	
	/**
	 * Copies the source file to the destination file. If the source file is a
	 * single file it will be placed in the destination directory or file. 
	 * If the source file is a directory it will copied to the given destination
	 * directory. If the destination directory does not exist it and all 
	 * required directories will attempted to be created.
	 * 
	 * @param src
	 * 		The source to copy.
	 * @param dst
	 * 		The destination of the copy.
	 * @param filter
	 * 		The filter to use to selectively copy any child files over.
	 * @throws IOException
	 * 		An exception occurred copying.
	 */
	public static void copy(File src, File dst, FilenameFilter filter) throws IOException 
	{
		if (src.isFile()) {
			if (dst.isFile()) {
				copyFile(src, dst);
			}
			else {
				copyTo(src, dst);
			}
		}
		else {
			dst.mkdirs();
			
			for (File f : src.listFiles(filter)) {
				if (f.isDirectory()) {
					copy(f, new File(dst, f.getName()));
				}
				else {
					copyTo(f, dst);
				}
			}
		}
	}
	
	/**
	 * Copies a source file to a destination file.
	 * 
	 * @param src
	 * 		The source file to copy.
	 * @param dst
	 * 		The destination file of the copy.
	 * @throws IOException
	 * 		An exception occurred copying.
	 */
	private static void copyFile(File src, File dst) throws IOException 
	{
		if (!dst.isFile()) {
			dst.createNewFile();
		}

		RandomAccessFile outstream = new RandomAccessFile(dst, "rw");
		RandomAccessFile instream = new RandomAccessFile(src, "r");
		
		try
		{
			FileChannel out = outstream.getChannel();	
			FileChannel in = instream.getChannel();

			in.transferTo(0, in.size(), out);
		}
		finally
		{
			instream.close();
			outstream.close();
		}
	}
	
	/**
	 * Copies a source file to a destination directory.
	 * 
	 * @param src
	 * 		The source file to copy.
	 * @param dstDir
	 * 		The destination directory to copy to.
	 * @throws IOException
	 * 		An exception occurred copying.
	 */
	private static void copyTo(File src, File dstDir) throws IOException 
	{
		copyFile(src, new File(dstDir, src.getName()));
	}

}
