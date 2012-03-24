package test;

import static org.junit.Assert.*;

import org.junit.*;

import aQute.aws.*;
import aQute.aws.credentials.*;
import aQute.aws.ses.*;

public class SESTest {
	UserCredentials	uc	= new UserCredentials();

	@Test
	public void test() throws Exception {
		AWS aws = new AWS(uc.getAWSAccessKeyId(), uc.getAWSSecretKey());
		SES ses = aws.ses();
		String from = ses.subject("Hello Peter").to("ses@aqute.biz").text("Hello peter").from("ses@aqute.biz");
		System.out.println(from);
		assertNotNull(from);
	}

}
