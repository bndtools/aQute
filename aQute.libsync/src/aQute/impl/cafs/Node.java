package aQute.impl.cafs;

import java.io.*;
import java.util.*;

import aQute.lib.json.*;
import aQute.libg.cryptography.*;
import aQute.service.cafs.data.*;

class Node {
	static final JSONCodec	codec	= new JSONCodec();
	String					name;
	int						size	= -1;
	long					time;
	long					pos;
	final Map<String,Node>	entries	= new HashMap<String,Node>();
	SHA1					digest;
	byte[]					catalogData;
	private long			flags;
	String					comment;

	Node getNode(String path) {
		// System.out.println("Get node " + path);
		int n = path.indexOf('/');
		if (n >= 0) {
			String name = path.substring(0, n + 1); // Include /
			String remainder = path.substring(n + 1);
			// System.out.println("Descent " + path + " local=" + name +
			// " remainder=" + remainder);
			Node node = getLocalNode(name);
			if (remainder.isEmpty())
				return node; // directory node

			return node.getNode(remainder);
		}
		return getLocalNode(path);
	}

	private Node getLocalNode(String name) {
		Node node = entries.get(name);
		if (node == null) {
			// System.out.println("Creating new node " + name);

			node = new Node();
			node.name = name;
			entries.put(name, node);
		}
		return node;
	}

	/**
	 * Fixup all the SHAs for each of the directories and collect the shas.
	 * 
	 * @param dir
	 * @return
	 * @throws Exception
	 */
	void fixupCatalogs(Map<SHA1,Node> shas) throws Exception {
		// System.out.println("Fixing up " + name);
		if (isDirectory()) {
			for (Node node : entries.values()) {
				if (node.size < 0)
					node.fixupCatalogs(shas); // catalog
				else
					shas.put(node.digest, node); // normal data
			}

			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			Digester<SHA1> digester = SHA1.getDigester(bout);
			codec.enc().to(digester).put(getCatalogData()).flush();

			this.digest = digester.digest();
			shas.put(digest, this);
			catalogData = bout.toByteArray();
		}
	}

	private boolean isDirectory() {
		return name == null || name.endsWith("/");
	}

	CatalogData getCatalogData() {
		CatalogData data = new CatalogData();
		data.entries = new ArrayList<EntryData>();
		for (Map.Entry<String,Node> e : entries.entrySet()) {
			EntryData entryData = new EntryData();
			entryData.name = e.getKey();
			entryData.size = e.getValue().size;
			entryData.time = e.getValue().time;
			entryData.digest = e.getValue().digest.digest();
			entryData.flags = e.getValue().flags;
			data.entries.add(entryData);
		}
		return data;
	}

	public void tree(int n) {
		for (int i = 0; i < n; i++)
			System.out.print(" ");
		System.out.println(name + " " + digest);
		if (entries != null)
			for (Node node : entries.values()) {
				node.tree(n + 1);
			}
	}
}
