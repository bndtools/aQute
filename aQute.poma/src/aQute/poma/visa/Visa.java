package aQute.poma.visa;

import java.math.*;

import aQute.bnd.annotation.component.*;
import aQute.poma.domain.*;
import aQute.poma.service.gateway.*;

/**
 * Implements a payment gateway for VISA credit cards.
 */
@Component
public class Visa implements PaymentGateway {

	@Override
	public String getName() {
		return "VISA";
	}

	@Override
	public com.vaadin.ui.Component getForm() {
		return new VisaForm();
	}

	@Override
	public Payment getTransfer(com.vaadin.ui.Component form) {
		final VisaForm vf = (VisaForm) form;
		return new Payment() {

			@Override
			public void transfer(BigDecimal amount) {
				System.out.println("Charging VISA card " + vf.getCCNumber() + " for "+ amount + " from " + vf.getName());
			}
			
		};
	}
	
	

}
