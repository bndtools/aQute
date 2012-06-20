package aQute.poma.service.gateway;

import com.vaadin.ui.*;

/**
 * A Payment Gateway abstracts the payment method. It can stand for a Wire
 * transfer or a Credit card company, or Pigeons.
 */
public interface PaymentGateway {
	/**
	 * The name of the gateway. Used to allow the user to select the payment
	 * method.
	 * 
	 * @return
	 */
	String getName();

	/**
	 * A form that contains the user editable elements.
	 * 
	 * @return
	 */
	Component getForm();

	/**
	 * Get a payment object by looking at the form.
	 * 
	 * @param form
	 * @return
	 */
	Payment getTransfer(Component form);
}
