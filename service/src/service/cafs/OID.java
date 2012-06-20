package service.cafs;

import java.io.*;
import java.security.*;
import java.util.*;
import java.util.regex.*;

public class OID implements Comparable<OID> {
	final byte[]	digest;

	public static StoreOutputStream calculate(final OutputStream... out) throws Exception {
		final MessageDigest instance = MessageDigest.getInstance("SHA1");
		return new StoreOutputStream() {
			OID	oid;

			@Override
			public void write(int b) throws IOException {
				instance.update((byte) b);
				for (OutputStream o : out) {
					o.write(b);
				}
			}

			@Override
			public void write(byte[] b, int offset, int length) throws IOException {
				instance.update(b, offset, length);
				for (OutputStream o : out) {
					o.write(b, offset, length);
				}
			}

			@Override
			public synchronized OID getOid() throws Exception {
				if (oid == null) {
					return new OID(instance.digest());
				} else
					return oid;
			}
		};
	}

	public OID(byte[] digest) {
		if (digest == null || digest.length != 20)
			throw new IllegalArgumentException("Invalid digest, must be byte[] of 20 bytes for a SHA-1, got "
					+ Arrays.toString(digest));
		this.digest = digest;
	}

	public OID(InputStream in, OutputStream... out) throws NoSuchAlgorithmException, IOException {

		MessageDigest instance = MessageDigest.getInstance("SHA1");

		byte buffer[] = new byte[8000];
		int size = in.read(buffer);
		while (size > 0) {
			instance.update(buffer, 0, size);
			for (OutputStream o : out)
				o.write(buffer, 0, size);

			size = in.read(buffer);
		}
		in.close();

		this.digest = instance.digest();
	}

	static Pattern	DIGEST	= Pattern.compile("[0-9A-Fa-f]{40}?");

	public OID(CharSequence digest) {
		if (!DIGEST.matcher(digest).matches())
			throw new IllegalArgumentException("Invalid length for digest, got " + digest + " pattern: "
					+ DIGEST.toString());
		this.digest = new byte[20];
		for (int i = 0; i < 20; i++) {
			this.digest[i] = (byte) (nibble(digest.charAt(i * 2)) << 4 + nibble(digest.charAt(i * 2 + 1)));
		}
	}

	private int nibble(char c) {
		if (c >= 'a')
			return c - 'a' + 10;

		if (c >= 'A')
			return c - 'A' + 10;

		return c - '0';
	}

	static char[]	NIBBLES	= new char[] {
			'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
							};

	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < digest.length; i++) {
			sb.append(NIBBLES[0xF & (digest[i] >> 4)]);
			sb.append(NIBBLES[0xF & digest[i]]);
		}
		return sb.toString();
	}

	public byte[] getDigest() {
		return digest.clone();
	}

	public int hashCode() {
		return digest[0] + digest[1] << 8 + digest[2] << 16 + digest[3] << 24;
	}

	public boolean equals(Object other) {
		if (other instanceof OID) {
			return Arrays.equals(digest, ((OID) other).digest);
		}
		return false;
	}

	@Override
	public int compareTo(OID o) {
		for (int i = 0; i < digest.length; i++) {
			if (digest[i] > o.digest[i])
				return 1;
			else if (digest[i] < o.digest[i])
				return -1;
		}
		return 0;
	}
}
