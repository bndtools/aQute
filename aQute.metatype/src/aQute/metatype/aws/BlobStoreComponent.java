package aQute.metatype.aws;

import java.io.*;
import java.util.*;

import aQute.bnd.annotation.component.*;
import aQute.bnd.annotation.metatype.*;

import com.amazonaws.auth.*;
import com.amazonaws.services.s3.*;
import com.amazonaws.services.s3.model.*;

interface Store {
	InputStream get(String name);

	void put(String name, InputStream in);
}

@Component(designate = BlobStoreComponent.Config.class, immediate = true)
public class BlobStoreComponent implements Store {
	interface Config {
		String domain();

		String _secretKey();

		String _accessKey();

		Region region();
	};

	Config			config;
	AmazonS3Client	client;

	@Activate
	void activate(Map< ? , ? > map) {
		config = Configurable.createConfigurable(Config.class, map);
		AWSCredentials credentials = new BasicAWSCredentials(config._accessKey(), config._secretKey());
		client = new AmazonS3Client(credentials);
		client.createBucket(config.domain(), config.region());
	}

	@Override
	public InputStream get(String name) {
		S3Object obj = client.getObject(config.domain(), name);
		return obj.getObjectContent();
	}

	@Override
	public void put(String name, InputStream input) {
		client.putObject(config.domain(), name, input, null);
	}

}
