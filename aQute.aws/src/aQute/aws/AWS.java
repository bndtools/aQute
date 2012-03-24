package aQute.aws;

import java.io.*;
import java.net.*;
import java.security.*;
import java.text.*;
import java.util.*;

import javax.crypto.*;
import javax.crypto.spec.*;
import javax.xml.parsers.*;

import aQute.aws.s3.*;
import aQute.aws.ses.*;
import aQute.aws.sqs.*;
import aQute.lib.base64.*;
/**
 * https://email.us-east-1.amazonaws.com/
 * ?Action=SendEmail
&Destination.ToAddresses.member.1=ses%40aQute.biz
&Destination.ToAddresses.member.1=allan%40example.com
&Message.Body.Html.Data=Hello%20peter
&Message.Body.Text.Data=Hello.%20I%20hope%20you%20are%20having%20a%20good%20day.
&Message.Subject.Data=Hello%20Peter
&Message.Subject.Data=This%20is%20the%20subject%20line.
&Source=ses%40aQute.biz
&Source=user%40example.com

https://email.us-east-1.amazonaws.com/
?Action=SendEmail
&Source=user%40example.com
&Destination.ToAddresses.member.1=allan%40example.com
&Message.Subject.Data=This%20is%20the%20subject%20line.
&Message.Body.Text.Data=Hello.%20I%20hope%20you%20are%20having%20a%20good%20day.
&AWSAccessKeyId=AKIAIOSFODNN7EXAMPLE
&Signature=RhU864jFu893mg7g9N9j9nr6h7EXAMPLE
&Algorithm=HMACSHA256                    

 */
public class AWS {
	final private String						accessKey;
	final private String						secretKey;
	final private Mac							mac;
	final private SecretKeySpec					secret;
	final private static SimpleDateFormat		awsDateFormat	= new SimpleDateFormat(
																		"yyyy-MM-dd'T'HH:mm:ss'Z'");
	static SimpleDateFormat						httpDateFormat	= new SimpleDateFormat(
																		"EEE, dd MMM yyyy HH:mm:ss Z");
	final private static DocumentBuilderFactory	dbf				= DocumentBuilderFactory
																		.newInstance();
	static {
		dbf.setNamespaceAware(false);
	}

	public AWS(String accessKey, String secretKey)
			throws NoSuchAlgorithmException {
		this.accessKey = accessKey;
		this.mac = Mac.getInstance("HmacSHA256");
		this.secret = new SecretKeySpec(secretKey.getBytes(), "HmacSHA256");
		this.secretKey = secretKey;
	}

	public Request request(final Protocol protocol, final String action)
			throws ParseException {
		return new Request() {

			{
				arg("Action", action);
				endpoint(protocol.endpoint);
			}

			public HttpURLConnection sign() throws Exception {

				// TODO since 8 bit chars are not supported, we can optimize

				synchronized (secret) {

					switch (protocol.signature) {
						case 2 :
							return signVersion2(protocol);
						case 3 :
							return signVersion3();
						default :
							throw new IllegalArgumentException(
									"Invalid signature version");
					}
				}
			}

			private HttpURLConnection signVersion3()
					throws MalformedURLException, IOException {
				// Now this is simple ...
				StringBuilder query = buildRequest();
				URL url = new URL(endpoint() + "?" + query);
				HttpURLConnection conn = (HttpURLConnection) url
						.openConnection();
				String date = httpDateFormat.format(new Date());
				conn.setRequestProperty("Date", date);
				byte[] signature = hmacSha256(date);

				conn.setRequestProperty(
						"X-Amzn-Authorization",
						"AWS3-HTTPS AWSAccessKeyId=" + accessKey
								+ ", Algorithm=HmacSHA256, Signature="
								+ Base64.encodeBase64(signature));
				System.out.println(url.toExternalForm());
				System.out.println(conn.getRequestProperty("Date"));
				System.out.println(conn.getRequestProperty("X-Amzn-Authorization"));
				
				return conn;
			}

			private HttpURLConnection signVersion2(final Protocol protocol) throws MalformedURLException,
					IOException {
				arg("AWSAccessKeyId", accessKey);
				arg("Timestamp", awsDateFormat.format(new Date()));
				arg("SignatureMethod", "HmacSHA256");
				arg("SignatureVersion", "2");
				arg("Version", protocol.version);

				StringBuilder query = buildRequest();
				
				URL endpoint = new URL(endpoint());
				String host = endpoint.getHost();
				String path = endpoint.getPath();
				if (path == null || path.isEmpty())
					path = "/";

				final String toSign = verb().toUpperCase() + "\n" + host + "\n"
						+ path + "\n" + query;

				final String ep = endpoint + "?" + query + "&Signature="
						+ encodeUrl(Base64.encodeBase64(hmacSha256(toSign)));
				URL url = new URL(ep);
				return (HttpURLConnection) url.openConnection();
			}

			private StringBuilder buildRequest() {
				StringBuilder sb = new StringBuilder();
				String del = "";

				for (Map.Entry<String, Object> parameter : arguments.entrySet()) {
					Object value = parameter.getValue();
					if (value != null) {
						sb.append(del) //
								.append(parameter.getKey()) //
								.append("=")
								//
								.append(encodeUrl(parameter.getValue()
										.toString()));
					}
					del = "&";
				}
				return sb;
			}

			protected DocumentBuilder getDocumentBuilder()
					throws ParserConfigurationException {
				return dbf.newDocumentBuilder();
			}

		};
	}

	private String encodeUrl(String value) {
		try {
			// TODO Not very efficient now
			return URLEncoder.encode(value, "utf-8").replace("+", "%20")
					.replace("*", "%2A").replace("%7E", "~");
		}
		catch (final UnsupportedEncodingException ex) {
			throw new RuntimeException(ex);
		}
	}

	private byte[] hmacSha256(final String str) {
		try {
			mac.init(secret);
		}
		catch (final Exception e) {
			throw new RuntimeException(e);
		}
		return mac.doFinal(str.getBytes());
	}

	public S3 s3() {
		return new S3(accessKey, secretKey);
	}

	public SES ses() {
		return new SES(new Protocol(this,"https://email.us-east-1.amazonaws.com/",null,3));
	}

	public SQS sqs(String region) {
		return new SQS(new Protocol(this,region,SQS.version,2));
	}

}
