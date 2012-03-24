package aQute.aws.ses;

import aQute.aws.*;

public class SES {
	Protocol	aws;

	public SES(Protocol protocol) {
		this.aws = protocol;
	}

	public SESRequest subject(String subject) throws Exception {
		Request request = aws.action("SendEmail");
		if ( subject != null)
			request.arg("Message.Subject.Data", subject);
		return new SESRequest(request);
	}

	public static class SESRequest {
		final Request	request;
		int				to	= 1;
		int				cc	= 1;
		int				bcc	= 1;
		int				reply	= 1;

		SESRequest(Request request) {
			this.request = request;
		}

		public SESRequest html(String html) {
			request.arg("Message.Body.Html.Data", html);
			return this;

		}

		public SESRequest text(String text) {
			request.arg("Message.Body.Text.Data", text);
			return this;

		}

		public SESRequest to(String address) {
			request.arg("Destination.ToAddresses.member."+to++, address);
			return this;

		}

		public SESRequest cc(String address) {
			request.arg("Destination.CcAddresses.member."+cc++, address);
			return this;

		}

		public SESRequest returnPath(String address) {
			request.arg("ReturnPath", address);
			return this;

		}

		public SESRequest replyTo(String address) {
			request.arg("ReplyToAddresses.member."+reply++, address);
			return this;
		}

		public SESRequest bcc(String address) {
			request.arg("Destination.CcAddresses.member."+bcc++, address);
			return this;
		}

		public String from(String address) throws Exception {
			request.arg("Source", address);
			return request.string("SendEmailResponse/SendEmailResult/MessageId");
		}
	}
}
