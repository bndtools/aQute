package aqute.eclipse.coffee.editor;

import org.eclipse.core.runtime.*;
import org.eclipse.ui.*;
import org.eclipse.ui.editors.text.*;

public class CoffeeTextEditor extends TextEditor
{
    CoffeeConfiguration coffeeConfiguration = new CoffeeConfiguration(getSharedColors());

    public CoffeeTextEditor()
    {
        super();
        setSourceViewerConfiguration(coffeeConfiguration);
        setDocumentProvider(coffeeConfiguration.getDocumentProvider());

    }
    
    protected void doSetInput(IEditorInput input) throws CoreException {
    	super.doSetInput(input);
    }

}