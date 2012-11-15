package org.hudsonci.eclipse.maven.actions;

import org.eclipse.jface.action.IMenuManager;
import org.maven.ide.eclipse.actions.AbstractMavenMenuCreator;
import org.maven.ide.eclipse.actions.SelectionUtil;

public class NewJobMavenMenuCreator extends AbstractMavenMenuCreator {

	public NewJobMavenMenuCreator() {
		super();
	}

	@Override
	public void createMenu(IMenuManager mgr) {
		int selectionType = SelectionUtil.getSelectionType(selection);
		if(selectionType == SelectionUtil.UNSUPPORTED) {
			return;
		}
		
		if(selectionType == SelectionUtil.PROJECT_WITH_NATURE) {
			OpenNewJobWizardAction openNewJobWizardAction = new OpenNewJobWizardAction();
			mgr.appendToGroup(OPEN, openNewJobWizardAction);
		}
	}
}
