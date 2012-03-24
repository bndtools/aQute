package aQute.aws.credentials;
import java.io.*;
import java.util.*;

public class UserCredentials {
	File		home	= new File((String) System.getProperties().get("user.home"));
	Properties	properties;

	public String getAWSAccessKeyId() {
		return getProperties().getProperty("awsid");
	}

	public String getAWSSecretKey() {
		return getProperties().getProperty("awssecret");
	}

	private synchronized Properties getProperties()  {
		if (properties != null)
			return properties;

		try {
			properties = new Properties();
			File aws = new File(home, ".aws");
			File pf = new File(aws, "properties");
			if (!pf.isFile())
				throw new FileNotFoundException();

			InputStream in = new FileInputStream(pf);
			try {
				properties.load(in);
			} finally {
				in.close();
			}
			return properties;
		} catch (Exception e) {
			return new Properties();
		}
	}
}
