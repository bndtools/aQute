package aQute.service.cafs.data;

public class EntryData {
	public String	name;
	public byte[]	digest;
	public long		flags;
	public long		time;
	public int		size;
	public String	comment;

	public boolean isCatalog() {
		return size < 0;
	}
}
