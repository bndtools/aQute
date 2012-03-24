package aQute.aws;


public class Protocol {
	final int signature;
	final AWS aws;
	final String endpoint;
	final String version;

	Protocol(AWS aws, String endpoint, String version, int signature) {
		this.aws=aws;
		this.endpoint = endpoint;
		this.version = version;
		this.signature = signature;
	}
	
	public Request action(String action) throws Exception {
		return aws.request(this, action);
	}
}
