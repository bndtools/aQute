package test;

import java.util.*;

import aQute.lib.configurable.*;

import junit.framework.*;

public class BasicTest extends TestCase {

	interface Config {
		boolean aBoolean();

		byte aByte();

		char aChar();

		short aShort();

		int anInt();

		long aLong();

		float aFloat();

		double aDouble();

		String aString();

		Collection<String> aStringCollection();

		String[] aStringArray();

		int[] anIntArray();

		Collection<Integer> anIntegerCollection();
	}

	public void testBoolean() {
		Config c = with(true);
		assertEquals( true, c.aBoolean());
		assertTrue( c.aByte() != 0);
		assertTrue( c.aShort() != 0);
		assertTrue( c.anInt() != 0);
		assertTrue( c.aLong() != 0);
		assertTrue( c.aFloat() != 0);
		assertTrue( c.aDouble() != 0);
		assertEquals( "true", c.aString());
		assertEquals( Arrays.asList(new String[] {"true"}), Arrays.asList(c.aStringArray()));
		assertEquals( 1, c.anIntArray().length);
		assertTrue( c.anIntArray()[0] != 0);
		assertEquals( Arrays.asList(new String[] {"true"}), c.aStringCollection());
	}
	
	public void testByte() {
		Config c = with(55);
		assertEquals( true, c.aBoolean());
		assertEquals(55,  c.aByte());
		assertEquals( 55, c.aShort() );
		assertEquals( 55, c.anInt() );
		assertEquals( 55, c.aLong() );
		assertEquals( (float)55.0, c.aFloat());
		assertEquals( (double)55.0, c.aDouble());
		assertEquals( "55", c.aString());
		assertEquals( Arrays.asList(new String[] {"55"}), Arrays.asList(c.aStringArray()));
		assertEquals( 1, c.anIntArray().length);
		assertEquals( 55, c.anIntArray()[0]);
		assertEquals( Arrays.asList(new String[] {"55"}), c.aStringCollection());
	}
	
	public void testShort() {
		Config c = with(-33);
		assertEquals( true, c.aBoolean());
		assertEquals(-33,  c.aByte());
		assertEquals( -33, c.aShort() );
		assertEquals( -33, c.anInt() );
		assertEquals( -33, c.aLong() );
		assertEquals( (float)-33.0, c.aFloat());
		assertEquals( (double)-33.0, c.aDouble());
		assertEquals( "-33", c.aString());
		assertEquals( Arrays.asList(new String[] {"-33"}), Arrays.asList(c.aStringArray()));
		assertEquals( 1, c.anIntArray().length);
		assertEquals( -33, c.anIntArray()[0]);
		assertEquals( Arrays.asList(new String[] {"-33"}), c.aStringCollection());
	}
	
	public void testInt() {
		Config c = with(0);
		assertEquals( false, c.aBoolean());
		assertEquals(0,  c.aByte());
		assertEquals( 0, c.aShort() );
		assertEquals( 0, c.anInt() );
		assertEquals( 0, c.aLong() );
		assertEquals( (float)0.0, c.aFloat());
		assertEquals( (double)0.0, c.aDouble());
		assertEquals( "0", c.aString());
		assertEquals( Arrays.asList(new String[] {"0"}), Arrays.asList(c.aStringArray()));
		assertEquals( 1, c.anIntArray().length);
		assertEquals( 0, c.anIntArray()[0]);
		assertEquals( Arrays.asList(new String[] {"0"}), c.aStringCollection());
	}
	
	Config with(Object o) {
		Map<String,Object> p = new HashMap<String,Object>();
		p.put("aBoolean", o);
		p.put("aByte", o);
		p.put("aChar", o);
		p.put("aShort", o);
		p.put("anInt", o);
		p.put("aLong", o);
		p.put("aFloat", o);
		p.put("aDouble", o);
		p.put("aString", o);
		p.put("aStringCollection", o);
		p.put("aStringArray", o);
		p.put("anIntArray", o);
		p.put("anIntegerCollection", o);
		
		return Configurable.createConfigurable(Config.class, p);
	}
}
