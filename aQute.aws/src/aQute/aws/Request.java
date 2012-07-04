package aQute.aws;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.xml.parsers.*;
import javax.xml.xpath.*;

import org.w3c.dom.*;

import aQute.lib.hex.*;
import aQute.lib.io.*;
import aQute.libg.cryptography.*;

public abstract class Request {
	static XPathFactory				xpf			= XPathFactory.newInstance();

	final SortedMap<String,Object>	arguments	= new TreeMap<String,Object>();
	private String					endpoint;
	private String					verb		= "GET";
	private int						timeout;
	private Document				response;
	private HttpURLConnection		connection;
	private XPath					xpath;
	private int						attribute;

	Request() {

	}

	abstract public HttpURLConnection sign() throws Exception;

	public Request endpoint(String endpoint) {
		this.endpoint = endpoint;
		return this;
	}

	public String endpoint() {
		return endpoint;
	}

	public Request timeout(int timeout) {
		this.timeout = timeout;
		return this;
	}

	public int timeout() {
		return timeout;
	}

	public String verb() {
		return verb;
	}

	public Request verb(String verb) {
		this.verb = verb;
		return this;
	}

	public Request arg(String key, Object value) {
		if (value != null)
			arguments.put(key, value);
		return this;
	}

	public Request attr(String key, Object value) {
		if (value != null)
			arg("Attribute." + attribute + "." + key, value);
		return this;
	}

	public Request nextAttr() {
		attribute++;
		return this;
	}

	public Request arg(String key, Object value, boolean doit) {
		if (doit)
			arg(key, value);
		return this;
	}

	public int connect() throws Exception {
		if (connection == null) {
			connection = sign();
			if (timeout > 0)
				connection.setReadTimeout(timeout);
			connection.connect();
		}
		return connection.getResponseCode();
	}

	public Document getResponse() throws Exception {
		connect();
		if (response == null) {
			DocumentBuilder db = getDocumentBuilder();
			if (getResponseCode() < 300)
				response = db.parse(connection.getInputStream());
			else {
				InputStream in = connection.getErrorStream();
				if (in == null)
					in = new ByteArrayInputStream(
							"<?xml version='1.0'?><Error><Code>NO ERROR GIVEN</Code></Error>".getBytes());

				String collect = IO.collect(in);
				response = db.parse(new ByteArrayInputStream(collect.getBytes("UTF-8")));
			}
		}
		return response;
	}

	public String getResponseMessage() throws IOException {
		return connection.getResponseMessage();
	}

	public int getResponseCode() throws Exception {
		connect();
		return connection.getResponseCode();
	}

	public String string(String expression) throws Exception {
		check();
		return string(getResponse(), expression);
	}

	public String string(Node node, String expression) throws XPathExpressionException, Exception {
		return getXpath().evaluate(expression, node);
	}

	public Iterable<Node> nodes(String expression) throws Exception {
		check();
		return nodes(getResponse(), expression);
	}

	public Iterable<Node> nodes(Node node, String expression) throws XPathExpressionException, Exception {
		final NodeList list = (NodeList) getXpath().evaluate(expression, node, XPathConstants.NODESET);
		return new Iterable<Node>() {

			@Override
			public Iterator<Node> iterator() {
				return new Iterator<Node>() {
					int	n	= 0;

					@Override
					public boolean hasNext() {
						return n < list.getLength();
					}

					@Override
					public Node next() {
						return list.item(n++);
					}

					@Override
					public void remove() {
						throw new UnsupportedOperationException();
					}
				};
			}

		};
	}

	public Request check(String... ignores) throws Exception {
		if (getResponseCode() < 300)
			return this;

		String error = getError();
		for (String ignore : ignores) {
			if (error.contains(ignore))
				return this;
		}
		throw new AWSException(this);
	}

	public String getError() throws Exception {
		if (getResponseCode() < 300)
			return null;

		return string(getResponse(), ".");
	}

	XPath getXpath() {
		if (xpath == null) {
			synchronized (xpf) {
				xpath = xpf.newXPath();
			}
		}
		return xpath;
	}

	public boolean checkmd5(String md5OfBody, String body) throws Exception {
		MD5 given = new MD5(Hex.toByteArray(md5OfBody.toUpperCase()));
		Digester<MD5> calculated = MD5.getDigester();
		calculated.write(body.getBytes("UTF-8"));
		return calculated.digest().equals(given);
	}

	protected abstract DocumentBuilder getDocumentBuilder() throws Exception;
}
