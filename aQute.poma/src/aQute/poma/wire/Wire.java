package aQute.poma.wire;

import java.math.*;

import aQute.bnd.annotation.component.*;
import aQute.poma.service.gateway.*;

/**
 * Implements a silly Wire transfer gateway. THis is just and example to show
 * how you can use µservices to provide pluggable payment gateways.
 *
 */
@Component
public class Wire implements PaymentGateway {

	@Override
	public String getName() {
		return "Wire transfer";
	}

	@Override
	public com.vaadin.ui.Component getForm() {
		return new WireForm();
	}

	@Override
	public Payment getTransfer(com.vaadin.ui.Component form) {
		final WireForm pf = (WireForm) form;
		return new Payment() {

			@Override
			public void transfer(BigDecimal amount) {
				System.out.println("Wire transfer from IBAN " + pf.getIBAN() + " amount " + amount + " for customer " + pf.getName());
			}
			
		};
	}

}
