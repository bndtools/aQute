package aQute.aws.sdb;

import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;

import javax.crypto.*;
import javax.crypto.spec.*;
import javax.xml.parsers.*;
import javax.xml.xpath.*;

import org.w3c.dom.*;

import aQute.lib.base64.*;

import service.aws.sdb.*;

public class SDBImpl implements SDB {
	static DocumentBuilderFactory	dbf	= DocumentBuilderFactory.newInstance();
	static XPathFactory				xpf	= XPathFactory.newInstance();
	static Mac						mac;
	static Random					r	= new Random();
	static {
		dbf.setNamespaceAware(false);
		try {
			mac = Mac.getInstance("HmacSHA256");
		}
		catch (final Exception e) {
			e.printStackTrace();
		}
	}


	public static String httpDate() {
		final SimpleDateFormat df = new SimpleDateFormat(
				"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		df.setTimeZone(TimeZone.getTimeZone("UTC"));
		return df.format(new Date());
	}



	private final String		awsId;
	private final SecretKeySpec	secret;

	public SDBImpl(final String awsId, final String key) {
		this.awsId = awsId;
		this.secret = new SecretKeySpec(key.getBytes(), "HmacSHA256");
	}

	/**
	 * Create a new domain
	 */
	public void createDomain(String name) throws Exception {
		SortedMap<String, String> parameters = getStandardParameters(
				"CreateDomain", name);
		execute(parameters);
	}

	/**
	 * Delete attributes.
	 * 
	 * tuple map MUST contain an 'id' key, otherwise nothing will be deleted
	 */
	public void deleteAttributes(final String domain,
			final Map<String, String>... tuples) throws Exception {
		SortedMap<String, String> parameters = getStandardParameters(
				"BatchDeleteAttributes", domain);

		int itemIndex = 1;
		for (Map<String, String> tuple : tuples) {
			String id = tuple.get("id");
			if (id == null)
				throw new IllegalArgumentException(
						"A tuple must contain an id: " + tuple);

			String prefix = "Item." + itemIndex + ".";
			int attributeIndex = 1;
			for (final Map.Entry<String, String> me : tuple.entrySet()) {
				String prefix2 = prefix + "Attribute." + attributeIndex + ".";

				if ("id".equals(me.getKey())) {
					parameters.put(prefix + "ItemName", me.getValue());
				}
				else {
					parameters.put(prefix2 + "Name", me.getKey());
					parameters.put(prefix2 + "Value", me.getValue());
				}
				++attributeIndex;
			}
			itemIndex++;
		}
		execute(parameters);
	}

	public void deleteDomain(String domain) throws Exception {
		SortedMap<String, String> parameters = getStandardParameters(
				"DeleteDomain", domain);
		execute(parameters);
	}

	String encodeUrl(String value) {
		try {
			return URLEncoder.encode(value, "utf-8").replace("+", "%20")
					.replace("*", "%2A").replace("%7E", "~");
		}
		catch (final UnsupportedEncodingException ex) {
			throw new RuntimeException(ex);
		}
	}

	public Map<String, String> getAttributes(final String domain,
			final String item) throws Exception {
		SortedMap<String, String> parameters = getStandardParameters(
				"GetAttributes", domain);
		parameters.put("ItemName", item);
		InputStream in = execute(parameters);

		Map<String, String> tuple = new HashMap<String, String>();
		tuple.put("id", item);
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(in);
		XPath xpath = xpf.newXPath();
		NodeList nodes = (NodeList) xpath.evaluate(
				"/GetAttributesResponse/GetAttributesResult/Attribute", doc,
				XPathConstants.NODESET);
		for (int i = 0; i < nodes.getLength(); i++) {
			String key = xpath.evaluate("Name", nodes.item(i));
			String value = xpath.evaluate("Value", nodes.item(i));
			tuple.put(key, value);
		}
		return tuple;
	}

	byte[] hmacSha256(final String str) {
		try {
			mac.init(secret);
		}
		catch (final Exception e) {
			throw new RuntimeException(e);
		}
		return mac.doFinal(str.getBytes());
	}

	/**
	 * List domains.
	 * 
	 */
	public Collection<String> listDomains() throws Exception {
		SortedMap<String, String> parameters = getStandardParameters(
				"ListDomains", null);
		InputStream in = execute(parameters);

		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(in);
		XPath xpath = xpf.newXPath();
		NodeList nodes = (NodeList) xpath.evaluate(
				"/ListDomainsResponse/ListDomainsResult/DomainName", doc,
				XPathConstants.NODESET);
		List<String> result = new ArrayList<String>();
		for (int i = 0; i < nodes.getLength(); i++) {
			result.add(xpath.evaluate(".", nodes.item(i)));
		}
		return result;
	}

	// if tuple does not contain an 'id' key a unique id will be generated
	public void putAttributes(final String domain,
			Map<String, String>... tuples) throws Exception {
		SortedMap<String, String> parameters = getStandardParameters(
				"BatchPutAttributes", domain);

		int itemIndex = 1;
		for (Map<String, String> tuple : tuples) {
			String id = tuple.get("id");
			if (id == null)
				throw new IllegalArgumentException(
						"A tuple must contain an id: " + tuple);

			String prefix = "Item." + itemIndex + ".";
			int attributeIndex = 1;
			for (final Map.Entry<String, String> me : tuple.entrySet()) {
				String prefix2 = prefix + "Attribute." + attributeIndex + ".";

				if ("id".equals(me.getKey())) {
					parameters.put(prefix + "ItemName", me.getValue());
				}
				else {
					parameters.put(prefix2 + "Name", me.getKey());
					parameters.put(prefix2 + "Value", me.getValue());
					parameters.put(prefix2 + "Replace", "true");
				}
				++attributeIndex;
			}
			itemIndex++;
		}

		InputStream in = execute(parameters);
		in.close();
	}

	private SortedMap<String, String> getStandardParameters(String action,
			String domain) {
		SortedMap<String, String> map = new TreeMap<String, String>();
		map.put("Timestamp", httpDate());
		map.put("AWSAccessKeyId", awsId);
		map.put("Action", action);
		if (domain != null)
			map.put("DomainName", domain);
		map.put("SignatureMethod", "HmacSHA256");
		map.put("SignatureVersion", "2");
		map.put("Version", "2009-04-15");
		return map;
	}

	InputStream execute(SortedMap<String, String> map) throws Exception {
		StringBuilder sb = new StringBuilder();
		String del = "";
		for (Map.Entry<String, String> parameter : map.entrySet()) {
			sb.append(del) //
					.append(parameter.getKey()) //
					.append("=") //
					.append(encodeUrl(parameter.getValue()));
			del = "&";
		}

		final String toSign = "GET\n" + "sdb.amazonaws.com\n" + "/\n" + sb;
		final String endpoint = "https://sdb.amazonaws.com/?" + sb
				+ "&Signature=" + encodeUrl(Base64.encodeBase64(hmacSha256(toSign)));

		URL url = new URL(endpoint);
		System.out.println(url);
		final HttpURLConnection con = (HttpURLConnection) url.openConnection();
		if (con.getResponseCode() >= 300) {
			throw new Exception(con.getResponseMessage());
		}
		return con.getInputStream();
	}

	public Collection<Map<String, String>> select(String q) throws Exception {
		List<Map<String, String>> tuples = new ArrayList<Map<String, String>>();
		String token = null;
		while (true) {
			SortedMap<String, String> parameters = getStandardParameters(
					"Select", null);
			parameters.put("SelectExpression", q);
			if (token != null)
				parameters.put("NextToken", token);

			InputStream in = execute(parameters);

			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(in);
			XPath xpath = xpf.newXPath();
			NodeList items = (NodeList) xpath.evaluate(
					"/SelectResponse/SelectResult/Item", doc,
					XPathConstants.NODESET);
			for (int i = 0; i < items.getLength(); i++) {
				Map<String, String> tuple = new HashMap<String, String>();
				String id = xpath.evaluate("Name", items.item(i));

				NodeList attributes = (NodeList) xpath.evaluate("Attribute",
						doc, XPathConstants.NODESET);
				for (int a = 0; a < attributes.getLength(); a++) {
					Node attr = attributes.item(a);
					String key = xpath.evaluate("Name", attr);
					String value = xpath.evaluate("Value", attr);
					tuple.put(key, value);
				}
				tuple.put("id", id); // ensure it cannot be overwritten
				tuples.add(tuple);
			}
			
			token = xpath.evaluate("//NextToken", doc);
			if ( token == null || token.length()==0)
				return tuples;			
		}
	}

	@Override
	public void putAttributesConditional(String domain,
			Map<String, String> tuple, String expectedKey, String expectedValue)
			throws Exception {
		SortedMap<String, String> parameters = getStandardParameters(
				"PutAttributes", domain);

		String id = tuple.get("id");
		if (id == null)
			throw new IllegalArgumentException("A tuple must contain an id: "
					+ tuple);

		int attributeIndex = 1;
		for (final Map.Entry<String, String> me : tuple.entrySet()) {
			String prefix2 = "Attribute." + attributeIndex + ".";

			if ("id".equals(me.getKey())) {
				parameters.put("ItemName", me.getValue());
			}
			else {
				parameters.put(prefix2 + "Name", me.getKey());
				parameters.put(prefix2 + "Value", me.getValue());
				parameters.put(prefix2 + "Replace", "true");
			}
			++attributeIndex;
		}
		parameters.put("Expected.0.Name", expectedKey);
		if (expectedValue != null)
			parameters.put("Expected.0.Value", expectedValue);
		else
			parameters.put("Expected.0.Exists", "false");

		InputStream in = execute(parameters);
		in.close();
	}

}
