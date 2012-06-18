package com.libsync.service.cafs;

import java.io.*;
import java.util.*;

/**
 * A contents addressable file store.
 * 
 */
public interface ContentAdressableFileStore {
	/**
	 * Store an Input Stream and return the digest of the stream. After this
	 * method returns the contents is uniquely stored. If the content was
	 * already stored then the method returns quickly.
	 * 
	 * @param in
	 *            The stream containing uncompressed content
	 * @return the digest of this stream
	 * @throws Exception
	 *             if anything fails
	 */
	byte[] store(InputStream in) throws Exception;

	/**
	 * Retrieve the content identified by a digest.
	 * 
	 * @param digest
	 *            the digest
	 * @return the input stream
	 * @throws Exception
	 *             if anything fails
	 */
	InputStream retrieve(byte[] digest) throws Exception;

	/**
	 * Delete the given digests from the store
	 * 
	 * @param digests
	 *            an array of digests to delete
	 * @throws Exception
	 */
	void delete(byte[]... digests) throws Exception;

	/**
	 * Calculates the digests that do not exist in the given list.
	 * 
	 * @param digests
	 *            the digests that are sought
	 * @return the digests that are in the sought list but do not exist in the
	 *         db
	 * @throws Exception
	 */
	Iterator<byte[]> exists(Collection<byte[]> filter) throws Exception;

}
