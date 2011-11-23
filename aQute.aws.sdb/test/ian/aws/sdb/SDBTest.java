package ian.aws.sdb;

import java.util.*;

import aQute.aws.sdb.*;

import junit.framework.*;

/**
 * Exception in thread "main" java.io.IOException: Server returned HTTP response
 * code: 403 for URL:
 * https://sdb.amazonaws.com/?AWSAccessKeyId=AKIAI62VQLAKOGGY5AUA
 * &Action=BatchPutAttributes
 * &DomainName=test&Item.1.Attribute.1.Name=a&Item.1.Attribute
 * .1.Replace=true&Item
 * .1.Attribute.1.Value=av&Item.1.Attribute.2.Name=b&Item.1.Attribute
 * .2.Replace=true
 * &Item.1.Attribute.2.Value=bv&Item.1.ItemName=item1&SignatureMethod
 * =HmacSHA256&
 * SignatureVersion=2&Timestamp=2011-11-22T10%3A27%3A23.053Z&Version=
 * 2009-04-15&Signature=%2F1FvvXO%2FLZDF3bGRuYYUFAmtSh5tSt5xJAqXKERdC%2Bw%3D
 * https://sdb.amazonaws.com/?AWSAccessKeyId=AKIAI62VQLAKOGGY5AUA&Action=
 * BatchPutAttributes
 * &DomainName=test&Item.1.Attribute.1.Name=a&Item.1.Attribute.1
 * .Replace=true&Item
 * .1.Attribute.1.Value=av&Item.1.Attribute.2.Name=b&Item.1.Attribute
 * .2.Replace=true
 * &Item.1.Attribute.2.Value=bv&Item.1.ItemName=item1&SignatureMethod
 * =HmacSHA256&
 * SignatureVersion=2&Timestamp=2011-11-22T10%3A28%3A14.119Z&Version=
 * 2&Signature=px2EsgEnfsQ%2BF8q9wRIcb31MiVxg7peVILl83nu%2BTAY%3D
 */

public class SDBTest extends TestCase {
	public void testSimple() throws Exception {
		SDBImpl sdb = new SDBImpl("AKIAI62VQLAKOGGY5AUA",
				"Tm/mWXn3ydMEDmgoSmB51dWxrnepI34p49lziWsk");
		sdb.createDomain("test");
		System.out.println(sdb.listDomains());
		TreeMap<String, String> map = new TreeMap<String, String>();
		map.put("id", "item1");
		map.put("a", "av");
		map.put("b", "bv");
		sdb.putAttributes("test", map);
		sdb.putAttributesConditional("test", map, "a", "av");
		try {
			sdb.putAttributesConditional("test", map, "a", "0");
			fail("Expected exception");
		}
		catch (Exception e) {

		}
		System.out.println(sdb.getAttributes("test", "item1"));
		System.out.println(sdb.select("select * from test"));
		map = new TreeMap<String, String>();
		map.put("id", "item1");
		sdb.deleteAttributes("test", map);
		System.out.println(sdb.getAttributes("test", "item1"));
		sdb.deleteDomain("test");
		System.out.println(sdb.listDomains());
	}
}
