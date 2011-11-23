package test;

import java.io.*;

import junit.framework.*;
import service.aws.s3.*;
import service.aws.s3.Bucket.Content;
import aQute.aws.s3.*;
/*
 * 47 45 54 0a 0a 0a 57 65 64 2c 20 32 33 20 4e 6f 76 20 32 30 31 31 20 31 35 3a 33 39 3a 31 32 20 55 54 43 0a 2f 6c 69 62 73 79 6e 63 2d 6f 69 64 2f
 * G  E  T  \n \n \n W  e  d                                                                                   /  l  i  b  s  y  n  c  -  o  i  d  / 
 */
public class S3Test extends TestCase {
	
	public void testSimple() throws Exception {
		S3Impl s3 = new S3Impl("AKIAI62VQLAKOGGY5AUA",
				"Tm/mWXn3ydMEDmgoSmB51dWxrnepI34p49lziWsk");
		
		Bucket bucket = s3.createBucket("libsync-oid");

		System.out.println(s3.listBuckets());
		
		bucket.putObject("hullo").put(new ByteArrayInputStream("Hullo".getBytes()));
		bucket.putObject("bye").put(new ByteArrayInputStream("Bye".getBytes()));
		
		for ( Content content : bucket.listObjects().maxKeys(1)) {
			System.out.printf("%-30s %10s %-40s %s\n",content.key,content.size, content.lastModified, content.etag);
		}
		bucket.delete("hullo");
		bucket.delete("bye");
		s3.deleteBucket("libsync-oid");
	}
}
