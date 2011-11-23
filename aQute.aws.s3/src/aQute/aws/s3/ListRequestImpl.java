package aQute.aws.s3;

import java.io.*;
import java.util.*;

import javax.xml.parsers.*;
import javax.xml.xpath.*;

import org.w3c.dom.*;

import service.aws.s3.*;
import service.aws.s3.Bucket.Content;
import service.aws.s3.Bucket.ListRequest;

public class ListRequestImpl extends CommonRequestImpl<ListRequest> implements
		ListRequest {
	final Bucket					bucket;
	final SortedMap<String, String>	args	= new TreeMap<String, String>();
	final DocumentBuilder			db;
	final XPath						xpath;

	ListRequestImpl(S3Impl parent, Bucket bucket)
			throws ParserConfigurationException {
		super(parent);
		this.bucket = bucket;
		db = S3Impl.dbf.newDocumentBuilder();
		xpath = S3Impl.xpf.newXPath();
	}

	@Override
	public ListRequestImpl delimeter(String delimeter) {
		args.put("delimeter", delimeter);;
		return this;
	}

	@Override
	public ListRequest marker(String marker) {
		args.put("marker", marker);;
		return this;
	}

	@Override
	public ListRequest maxKeys(int maxKeys) {
		args.put("max-keys", Integer.toString(maxKeys));
		return this;
	}

	@Override
	public ListRequest prefix(String prefix) {
		args.put("prefix", prefix);;
		return this;
	}

	/**
	 * <ListBucketResult xmlns="http://s3.amazonaws.com/doc/2006-03-01">
	 * <Name>quotes</Name> <Prefix>N</Prefix> <Marker>Ned</Marker>
	 * <MaxKeys>40</MaxKeys> <IsTruncated>false</IsTruncated> <Contents>
	 * <Key>Nelson</Key> <LastModified>2006-01-01T12:00:00.000Z</LastModified>
	 * <ETag>&quot;828ef3fdfa96f00ad9f27c383fc9ac7f&quot;</ETag> <Size>5</Size>
	 * <StorageClass>STANDARD</StorageClass> <Owner>
	 * <ID>bcaf161ca5fb16fd081034f</ID> <DisplayName>webfile</DisplayName>
	 * </Owner> </Contents> <Contents> <Key>Neo</Key>
	 * <LastModified>2006-01-01T12:00:00.000Z</LastModified>
	 * <ETag>&quot;828ef3fdfa96f00ad9f27c383fc9ac7f&quot;</ETag> <Size>4</Size>
	 * <StorageClass>STANDARD</StorageClass> <Owner>
	 * <ID>bcaf1ffd86a5fb16fd081034f</ID> <DisplayName>webfile</DisplayName>
	 * </Owner> </Contents> </ListBucketResult>
	 */

	@Override
	public Iterator<Content> iterator() {

		return new Iterator<Content>() {
			NodeList	contents;
			int			item;
			boolean		isTruncated	= true;
			Content 	current;
			
			@Override
			public boolean hasNext() {
				if (contents != null && item < contents.getLength())
					return true;
				try {
					while (isTruncated) {
						if ( current != null)
							args.put("marker", current.key);
						
						InputStream in = parent.construct(S3Impl.METHOD.GET,
								bucket.getName(), null, null, headers, args);
				
						//IO.copy(in, System.out);
						Document doc = db.parse(in);
						Node listBucketResult = (Node) xpath.evaluate("/ListBucketResult",
								doc, XPathConstants.NODE);

						String trunc = xpath.evaluate("IsTruncated",
								listBucketResult);
						isTruncated = Boolean.parseBoolean(trunc);
						contents = (NodeList) xpath.evaluate("Contents", listBucketResult,
								XPathConstants.NODESET);
						item = 0;
						System.out.println(contents.getLength());

						if (contents.getLength() > 0)
							return true;
					}
				}
				catch (Exception e) {
					throw new RuntimeException(e);
				}
				return false;
			}

			@Override
			public Content next() {
				try {
					final Node c = contents.item(item++);
					current = new Content();
					current.bucket = bucket;
					current.key = xpath.evaluate("Key", c);
					current.lastModified = parent.awsDate(xpath.evaluate(
							"LastModified", c));
					current.etag = xpath.evaluate("ETag", c);
					current.size = Long.parseLong(xpath.evaluate("Size", c));
					current.storageClass = Enum.valueOf(S3.StorageClass.class,
							xpath.evaluate("StorageClass", c));
					return current;
				}
				catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			@Override
			public void remove() {
				if ( current != null)
					try {
						bucket.delete(current.key);
					}
					catch (Exception e) {
						throw new RuntimeException(e);
					}
			}

		};
	}
}
