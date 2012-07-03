package aQute.service.email;

public interface Email {
	EmailRequest subject(String subject, Object... args) throws Exception;

	interface EmailRequest {
		EmailRequest html(String html);

		EmailRequest text(String format, Object... args);

		EmailRequest to(String... address);

		EmailRequest cc(String... address);

		EmailRequest returnPath(String address);

		EmailRequest replyTo(String address);

		EmailRequest bcc(String... address);

		EmailRequest from(String address) throws Exception;

		void send() throws Exception;
	}

}
