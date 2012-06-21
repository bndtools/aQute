package aQute.aws.s3;

public class S3Exception extends RuntimeException {
	private static final long	serialVersionUID	= 1L;
	final public int			responseCode;

	public S3Exception(String msg, int responseCode) {
		super(msg);
		this.responseCode = responseCode;
	}

}
