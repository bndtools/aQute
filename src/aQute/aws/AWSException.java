package aQute.aws;

public class AWSException extends RuntimeException {
	private static final long	serialVersionUID	= 1L;
	final private Request		request;

	public AWSException(Request request) throws Exception {
		super(request.getResponseCode() + ": " + request.getResponseMessage() + " " + request.getError());
		this.request = request;
	}

	public AWSException(Request request, Throwable cause) throws Exception {
		super(request.getResponseCode() + ": " + request.getResponseMessage() + " " + request.getError(), cause);
		this.request = request;
	}

	public AWSException(String message, Throwable cause) throws Exception {
		super(message, cause);
		this.request = null;
	}

	public AWSException(String message) throws Exception {
		super(message);
		this.request = null;
	}

	public Request getRequest() {
		return request;
	}
}
