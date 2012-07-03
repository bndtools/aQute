package aQute.impl.email.ses;

import java.util.*;

import org.osgi.service.log.*;

import aQute.aws.*;
import aQute.aws.ses.*;
import aQute.aws.ses.SES.SESRequest;
import aQute.bnd.annotation.component.*;
import aQute.impl.email.ses.EmailImpl.Config;
import aQute.lib.converter.*;
import aQute.service.email.*;

@Component(configurationPolicy = ConfigurationPolicy.require, designateFactory = Config.class)
public class EmailImpl implements Email {
	AWS			aws;
	SES			ses;	;
	LogService	log;

	interface Config {
		String _awsSecret();

		String _awsId();

		String source();
	}

	Config	config;

	@Activate
	void activate(Map<String,Object> props) throws Exception {
		config = Converter.cnv(Config.class, props);
		aws = new AWS(config._awsId(), config._awsSecret());
		ses = aws.ses();
	}

	@Deactivate
	void deactivate() {
		ses.close();
	}

	@Override
	public EmailRequest subject(String subject, Object... args) throws Exception {
		final SESRequest rq = ses.subject(String.format(subject, args));

		return new EmailRequest() {

			@Override
			public EmailRequest html(String html) {
				rq.html(html);
				return this;
			}

			@Override
			public EmailRequest text(String format, Object... args) {
				rq.text(String.format(format, args));
				return this;
			}

			@Override
			public EmailRequest to(String... address) {
				for (String ad : address)
					rq.to(ad);
				return this;
			}

			@Override
			public EmailRequest cc(String... address) {
				for (String ad : address)
					rq.to(ad);
				return this;
			}

			@Override
			public EmailRequest returnPath(String address) {
				rq.returnPath(address);
				return this;
			}

			@Override
			public EmailRequest replyTo(String address) {
				rq.replyTo(address);
				return this;
			}

			@Override
			public EmailRequest bcc(String... address) {
				for (String ad : address)
					rq.to(ad);
				return this;
			}

			@Override
			public EmailRequest from(String address) throws Exception {
				rq.from(address);
				return this;
			}

			@Override
			public void send() throws Exception {
				rq.from(config.source() == null ? "ses@aQute.biz" : config.source());
				rq.send();
			}

		};
	}
}
