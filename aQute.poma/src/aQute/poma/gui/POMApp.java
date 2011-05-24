package aQute.poma.gui;

import java.util.concurrent.atomic.*;

import aQute.bnd.annotation.component.*;
import aQute.poma.domain.*;
import aQute.poma.service.db.*;
import aQute.poma.service.payments.*;

import com.vaadin.*;
import com.vaadin.ui.*;

/**
 * This the main web application for POMA. It demonstrates how µServices
 * significantly simplify systems.
 * 
 * This is a dummy program that shows my ignorance of payment systems but
 * hopefully shows the power of OSGi µServices to simplify the software
 * architecture. The application uses a dummy customer where the customer has a
 * set of bills to pay. The user can select a bill and a payment method (visa,
 * wire, etc) The pay button then sends the payment to a PaymentManager that
 * knows how to audit and execute the payment.
 * 
 * 
 * Registers a POMA component factory that is used by Vaadin to create web
 * context path at /poma at the local host (normally port 8080):
 * http://localhost:8080/poma.
 */

@aQute.bnd.annotation.component.Component(factory = "com.vaadin.Application/poma")
public class POMApp extends Application {
	private static final long serialVersionUID = 1;
	Customer customer;
	PaymentView paymentView = new PaymentView(this);
	AtomicReference<PaymentManager> paymentManager = new AtomicReference<PaymentManager>();

	/*
	 * Initialize the main window and add the payment view to it. (non-Javadoc)
	 * 
	 * @see com.vaadin.Application#init()
	 */
	@Override
	public void init() {
		Window window = new Window();
		setMainWindow(window);
		window.addComponent(new Label("POMA v1.0"));
		window.addComponent(paymentView);

	}

	/**
	 * Called from the GUI when the needs to be paid.
	 * 
	 * @param bill
	 *            The bill to be paid
	 * @param payment
	 *            The payment method
	 */
	public void pay(Bill bill, Payment payment) {
		paymentManager.get().pay(bill, payment);
		paymentView.setBills( customer.getBills());
	}
	
	/**
	 * Our database that gives us our domain objects. We use it to get a dummy
	 * customers and set the payment view to its bills.
	 * 
	 * @param db
	 */
	@Reference
	public void setDB(DB db) {
		this.customer = db.getCustomer("1234");
		paymentView.setBills(customer.getBills());
	}

	/**
	 * A Payment Gateway represents a way of paying. As we want to support
	 * multiple different ways of paying we accept any number of payment
	 * gateways. In general, a Payment Gateway represents VISA, Amex, Wire
	 * transfer, Check, etc.
	 * 
	 * @param gateway
	 *            The payment gateway
	 */
	@Reference(type = '*')
	public void addPaymentGateway(PaymentGateway gateway) {
		paymentView.addPaymentMethod(gateway);
	}

	/**
	 * Payment gateways can be added and removed dynamically.
	 * 
	 * @param gateway
	 */
	public void removePaymentGateway(PaymentGateway pf) {
		paymentView.removePaymentMethod(pf);
	}

	/**
	 * Set the payment manager. This guy knows how to handle the different
	 * actions associated with a payment.
	 * 
	 * @param manager
	 */
	@Reference(type = '?')
	public void setPaymentStrategy(PaymentManager manager) {
		this.paymentManager.set(manager);
		paymentView.checkPayEnabled();
	}
	public void unsetPaymentStrategy(PaymentManager manager) {
		paymentManager.compareAndSet(manager, null);
		paymentView.checkPayEnabled();
	}
}
