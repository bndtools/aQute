package aqute.eclipse.coffee;

import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.text.*;
import org.eclipse.swt.*;
import org.eclipse.ui.*;
import org.eclipse.ui.actions.*;
import org.eclipse.ui.ide.*;
import org.eclipse.ui.part.*;
import org.eclipse.ui.texteditor.*;

/**
 * Manages the installation/deinstallation of global actions for multi-page
 * editors. Responsible for the redirection of global actions to the active
 * editor. Multi-page contributor replaces the contributors for the individual
 * editors in the multi-page editor.
 */
public class CoffeeContributor extends MultiPageEditorActionBarContributor {
	private IEditorPart	activeEditorPart;
	private Action		printIt;
	private ITextEditor	editor;

	/**
	 * Creates a multi-page contributor.
	 */
	public CoffeeContributor() {
		super();
		createActions();
	}

	/**
	 * Returns the action registed with the given text editor.
	 * 
	 * @return IAction or null if editor is null.
	 */
	protected IAction getAction(ITextEditor editor, String actionID) {
		this.editor = editor;
		return (editor == null ? null : editor.getAction(actionID));
	}

	/*
	 * (non-JavaDoc) Method declared in
	 * AbstractMultiPageEditorActionBarContributor.
	 */

	public void setActivePage(IEditorPart part) {
		if (activeEditorPart == part)
			return;

		activeEditorPart = part;

		IActionBars actionBars = getActionBars();
		if (actionBars != null) {

			ITextEditor editor = (part instanceof ITextEditor) ? (ITextEditor) part : null;

			actionBars.setGlobalActionHandler(ActionFactory.DELETE.getId(),
					getAction(editor, ITextEditorActionConstants.DELETE));
			actionBars.setGlobalActionHandler(ActionFactory.UNDO.getId(),
					getAction(editor, ITextEditorActionConstants.UNDO));
			actionBars.setGlobalActionHandler(ActionFactory.REDO.getId(),
					getAction(editor, ITextEditorActionConstants.REDO));
			actionBars.setGlobalActionHandler(ActionFactory.CUT.getId(),
					getAction(editor, ITextEditorActionConstants.CUT));
			actionBars.setGlobalActionHandler(ActionFactory.COPY.getId(),
					getAction(editor, ITextEditorActionConstants.COPY));
			actionBars.setGlobalActionHandler(ActionFactory.PASTE.getId(),
					getAction(editor, ITextEditorActionConstants.PASTE));
			actionBars.setGlobalActionHandler(ActionFactory.SELECT_ALL.getId(),
					getAction(editor, ITextEditorActionConstants.SELECT_ALL));
			actionBars.setGlobalActionHandler(ActionFactory.FIND.getId(),
					getAction(editor, ITextEditorActionConstants.FIND));
			actionBars.setGlobalActionHandler(IDEActionFactory.BOOKMARK.getId(),
					getAction(editor, IDEActionFactory.BOOKMARK.getId()));
			actionBars.updateActionBars();
		}
	}

	private void createActions() {
		printIt = new Action() {
			public void run() {
				IDocumentProvider dp = editor.getDocumentProvider();
				IDocument doc = dp.getDocument(editor.getEditorInput());

				ITextSelection selection = (ITextSelection) editor.getSelectionProvider().getSelection();

				if (!selection.isEmpty()) {
					String result = "";
					String source = selection.getText();
					try {
						result = NativeCoffee.eval(source);
					}
					catch (Exception e) {
						Err err = new Err(e.getMessage());
						result = "#" + err.line + ": " + err.message;
					}
					try {
						int n = selection.getEndLine();
						int count = doc.getNumberOfLines();
						if (count > n + 1)
							n++;

						int offset = doc.getLineOffset(n);
						doc.replace(offset, 0, result + "\n");
						editor.getSelectionProvider().setSelection(new TextSelection(offset, result.length()));
					}
					catch (Exception ee) {
						throw new RuntimeException(ee);
					}
				} else
					MessageDialog.openWarning(null, "CoffeeScript", "Nothing selected");
			}
		};
		printIt.setAccelerator(SWT.MOD1 + '7');

		printIt.setText("&Print It");
		printIt.setToolTipText("Execute the selection and print the result");
		printIt.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
				.getImageDescriptor(IDE.SharedImages.IMG_OBJS_TASK_TSK));

	}

	public void contributeToMenu(IMenuManager manager) {
		IMenuManager menu = new MenuManager("Coffee");
		manager.prependToGroup(IWorkbenchActionConstants.MB_ADDITIONS, menu);
		menu.add(printIt);
	}

	public void contributeToToolBar(IToolBarManager manager) {
		manager.add(new Separator());
		manager.add(printIt);
	}
}
