package aqute.eclipse.coffee;

import java.io.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.swt.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.editors.text.*;
import org.eclipse.ui.ide.*;
import org.eclipse.ui.part.*;

import aqute.eclipse.coffee.editor.*;

/**
 * An example showing how to create a multi-page editor. This example has 3
 * pages:
 * <ul>
 * <li>page 0 contains a nested text editor.
 * <li>page 2 Shows the page in Javascript
 * </ul>
 */
public class CoffeeEditor extends MultiPageEditorPart implements IResourceChangeListener {

	/** The text editor used in page 0. */
	private TextEditor	editor;
	/** The text widget used in page 2. */
	private StyledText	text;
	private IFile		file;

	/**
	 * Creates a multi-page editor example.
	 * 
	 * @throws IOException
	 */
	public CoffeeEditor() throws IOException {
		super();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
	}

	/**
	 * Creates page 0 of the multi-page editor, which contains a text editor for
	 * Coffee Script.
	 */
	void createCoffeeEditor() {
		try {
			editor = new CoffeeTextEditor();
			int index = addPage(editor, getEditorInput());
			setPageText(index, "Coffee");
			setPartName(editor.getTitle());
		}
		catch (PartInitException e) {
			ErrorDialog.openError(getSite().getShell(), "Error creating nested text editor", null, e.getStatus());
		}
	}

	/**
	 * Creates page 2 of the multi-page editor, which shows the sorted text.
	 */
	void createJavascriptViewer() {
		Composite composite = new Composite(getContainer(), SWT.NONE);
		FillLayout layout = new FillLayout();
		composite.setLayout(layout);
		text = new StyledText(composite, SWT.H_SCROLL | SWT.V_SCROLL);
		text.setEditable(false);

		int index = addPage(composite);
		setPageText(index, "JavaScript");
	}

	/**
	 * Creates the pages of the multi-page editor.
	 */
	protected void createPages() {
		createCoffeeEditor();
		createJavascriptViewer();
	}

	/**
	 * The <code>MultiPageEditorPart</code> implementation of this
	 * <code>IWorkbenchPart</code> method disposes all nested editors.
	 * Subclasses may extend.
	 */
	public void dispose() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
		super.dispose();
	}

	/**
	 * Saves the multi-page editor's document.
	 */
	public void doSave(IProgressMonitor monitor) {
		editor.doSave(monitor);
		saveJS();
	}

	// Originally saved it as a JS file but this was not that useful,
	// should make it an action
	private void saveJS() {
		// try {
		// IPath path = file.getLocation();
		//
		// IPath jspath = path.makeAbsolute().removeFileExtension()
		// .addFileExtension("js");
		// File jsfile = jspath.toFile();
		// File dir = jsfile.getParentFile();
		// dir = new File(dir, "js");
		// jsfile = new File(dir, jsfile.getName());
		//
		// if (viewJavaScript()) {
		// dir.mkdirs();
		// IO.store(text.getText(), jsfile);
		// } else
		// jsfile.delete();
		// file.getParent().refreshLocal(2, null);
		// } catch (Exception e) {
		// throw new RuntimeException(e);
		// }
	}

	/**
	 * Saves the multi-page editor's document as another file. Also updates the
	 * text for page 0's tab, and updates this multi-page editor's input to
	 * correspond to the nested editor's.
	 */
	public void doSaveAs() {
		editor.doSaveAs();
		saveJS();
	}

	/*
	 * (non-Javadoc) Method declared on IEditorPart
	 */
	public void gotoMarker(IMarker marker) {
		setActivePage(0);
		IDE.gotoMarker(getEditor(0), marker);
	}

	/**
	 * The <code>MultiPageEditorExample</code> implementation of this method
	 * checks that the input is an instance of <code>IFileEditorInput</code>.
	 */
	public void init(IEditorSite site, IEditorInput editorInput) throws PartInitException {
		if (!(editorInput instanceof IFileEditorInput))
			throw new PartInitException("Invalid Input: Must be IFileEditorInput");
		super.init(site, editorInput);
		file = ((IFileEditorInput) editorInput).getFile();

		addPropertyListener(new IPropertyListener() {

			@Override
			public void propertyChanged(Object arg0, int arg1) {
				// System.out.println("Changed " + arg0 + " " + arg1);
			}
		});
	}

	/*
	 * (non-Javadoc) Method declared on IEditorPart.
	 */
	public boolean isSaveAsAllowed() {
		return true;
	}

	/**
	 * Calculates the contents of page 2 when the it is activated.
	 */
	protected void pageChange(int newPageIndex) {
		super.pageChange(newPageIndex);
		if (newPageIndex == 1) {
			viewJavaScript();
		}
	}

	/**
	 * Closes all project files on project close.
	 */
	public void resourceChanged(final IResourceChangeEvent event) {
		if (event.getType() == IResourceChangeEvent.PRE_CLOSE) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					IWorkbenchPage[] pages = getSite().getWorkbenchWindow().getPages();
					for (int i = 0; i < pages.length; i++) {
						if (((FileEditorInput) editor.getEditorInput()).getFile().getProject()
								.equals(event.getResource())) {
							IEditorPart editorPart = pages[i].findEditor(editor.getEditorInput());
							pages[i].closeEditor(editorPart, true);
						}
					}
				}
			});
		}
	}

	/**
	 * Sorts the words in page 0, and shows them in page 2.
	 * 
	 * @throws IOException
	 */
	boolean viewJavaScript() {
		try {
			file.deleteMarkers(null, true, 0);
			String input = editor.getDocumentProvider().getDocument(editor.getEditorInput()).get();

			text.setText(NativeCoffee.compile(input));
			return true;
		}
		catch (RuntimeException re) {
			try {
				IMarker marker = file.createMarker(IMarker.PROBLEM);
				marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
				Err e = new Err(re.getMessage());
				marker.setAttribute(IMarker.LINE_NUMBER, e.line);
				marker.setAttribute(IMarker.MESSAGE, e.message);
				text.setText(re.getMessage());
			}
			catch (CoreException e) {
				new RuntimeException(e);
			}
		}
		catch (Exception e) {
			new RuntimeException(e);
		}
		return false;
	}

	// static String compile(final String command[], final String input)
	// throws Exception {
	// Process process = Runtime.getRuntime().exec(command);
	// InputStream in = process.getInputStream();
	// final InputStreamReader isr = new InputStreamReader(in);
	//
	// if (input != null) {
	// OutputStream out = process.getOutputStream();
	// final OutputStreamWriter osw = new OutputStreamWriter(out);
	// new Thread("compile") {
	// public void run() {
	// try {
	// osw.write("\n".toCharArray());
	// StringReader sr = new StringReader(input);
	// char[] buffer = new char[1000];
	// int size = sr.read(buffer);
	// while (size >= 0) {
	// osw.write(buffer, 0, size);
	// size = sr.read(buffer);
	// }
	// osw.write("\n".toCharArray());
	// } catch (Exception e) {
	// e.printStackTrace();
	// } finally {
	// try {
	// osw.close();
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// }
	// }
	// }.start();
	// }
	// StringWriter sw = new StringWriter();
	// char[] buffer = new char[1000];
	// int size = isr.read(buffer);
	// while (size >= 0) {
	// sw.write(buffer, 0, size);
	// size = isr.read(buffer);
	// }
	// String text = sw.toString();
	// if (!text.isEmpty())
	// return text;
	//
	// final InputStreamReader isr2 = new InputStreamReader(
	// process.getErrorStream());
	// size = isr2.read(buffer);
	// while (size >= 0) {
	// sw.write(buffer, 0, size);
	// size = isr2.read(buffer);
	// }
	// process.destroy();
	// throw new RuntimeException(sw.toString());
	// }
}
