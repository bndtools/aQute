package aQute.vaadin.trials;

import java.io.*;
import java.net.*;
import java.util.*;

import com.vaadin.*;
import com.vaadin.data.*;
import com.vaadin.data.Property.*;
import com.vaadin.data.util.*;
import com.vaadin.terminal.*;
import com.vaadin.ui.*;

@aQute.bnd.annotation.component.Component(factory = "com.vaadin.Application/trials")
public class Trials extends Application {
	private static final long serialVersionUID = 1L;

	Label hello = new Label("Hello World");
	final Button button = new Button("Fail Me");

	public class BundleDesc {
		String bsn;
		String version;
		int size;
		Date date = new Date();
		List<String> list = new ArrayList<String>();

		public List<String> getList() {
			return list;
		}

		public void setList(List<String> list) {
			this.list = list;
		}

		public Date getDate() {
			return date;
		}

		public void setDate(Date date) {
			this.date = date;
		}

		public String getBsn() {
			return bsn;
		}

		public void setBsn(String bsn) {
			this.bsn = bsn;
		}

		public String getVersion() {
			return version;
		}

		public void setVersion(String version) {
			this.version = version;
		}

		public int getSize() {
			return size;
		}

		public void setSize(int size) {
			this.size = size;
		}

	}

	@Override
	public void init() {
		final Window mainWindow = new Window();
		setMainWindow(mainWindow);

		final Form form = new Form();
		form.setCaption("Contact Information");
		form
				.setDescription("Please specify name of the person and the city where the person lives in.");

		BundleDesc bd = new BundleDesc();
		bd.bsn = "biz.aQute.bnd";
		bd.version = "1.0.1";
		bd.size = 10000;
		BeanItem<BundleDesc> bi = new BeanItem<BundleDesc>(bd);

		form.setItemDataSource(bi);
		form.setWidth("400px");

		mainWindow.addComponent(form);

		mainWindow.addComponent(hello);
		mainWindow.addComponent(button);
		button.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;

			public void buttonClick(Button.ClickEvent event) {
				mainWindow.showNotification("This is the caption",
						"This is the description",
						Window.Notification.TYPE_ERROR_MESSAGE);

			}
		});
		hello.setComponentError(new UserError("Hello????"));
		TextField tf = new TextField("Name");
		tf.setWidth(400, Sizeable.UNITS_PIXELS);
		mainWindow.addComponent(tf);

		ExternalResource flashResource;
		try {
			flashResource = new ExternalResource(new URL(
					"http://www.netsketch.com/ccc2.swf"));
			Embedded embedded = new Embedded("Embedded Caption", flashResource);
			embedded.setType(Embedded.TYPE_OBJECT);
			embedded.setMimeType("application/x-shockwave-flash");
			embedded.setWidth(400, Sizeable.UNITS_PIXELS);
			embedded.setHeight(400, Sizeable.UNITS_PIXELS);
			mainWindow.addComponent(embedded);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		tf.addListener(new ValueChangeListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				System.out.println("Changed value "
						+ event.getProperty().getValue());
			}
		});
		tf.setColumns(100);
		tf.setRows(10);

		InlineDateField date = new InlineDateField("Datum");
		date.setLocale(new Locale("de", "DE"));
		date.setResolution(DateField.RESOLUTION_SEC);
		mainWindow.addComponent(date);
		date.addListener(ValueChangeEvent.class, this, "dateChanged");

		Link link = new Link("link to a resource", new ExternalResource(
				"http://www.vaadin.com/"), "_", 500, 500, 10);
		mainWindow.addComponent(link);

		RichTextArea rta = new RichTextArea();
		rta.setCaption("My Text");
		rta.setValue("<h1>Hello World</h1><p>And this is text</p>");
		mainWindow.addComponent(rta);

		Select tcs = new Select("TwinColSelect");
		mainWindow.addComponent(tcs);
		tcs.addItem("one");
		tcs.addItem("two");
		tcs.addItem("three");

		OptionGroup og = new OptionGroup();
		og.addItem("one");
		og.addItem("two");
		og.addItem("three");
		og.setMultiSelect(false);

		OptionGroup og1 = new OptionGroup();
		Item item = og1.addItem(String.class);
		System.out.println(item);
		og1.addItem(Class.class);
		og1.addItem(Serializable.class);
		og1.setMultiSelect(true);
		og1.addListener(ValueChangeEvent.class, this, "selectClass");

		mainWindow.addComponent(og);
		mainWindow.addComponent(og1);

	}

	public void dateChanged(ValueChangeEvent o) {
		System.out.println(o);
	}

	public void selectClass(ValueChangeEvent o) {
		System.out.println(o);
	}
}
