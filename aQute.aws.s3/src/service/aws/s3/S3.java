package service.aws.s3;

import java.util.*;

public interface S3 {
	public enum StorageClass {
		STANDARD, REDUCED_REDUNDANCY
	}

	Collection<Bucket> listBuckets() throws Exception;

	Bucket getBucket(String name) throws Exception;

	void deleteBucket(String name) throws Exception;

	Bucket createBucket(String name, String ... region) throws Exception;
}
