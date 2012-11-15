package org.hudsonci.eclipse.maven.actions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.INewWizard;
import org.hudsonci.eclipse.core.HudsonPlugin;
import org.hudsonci.eclipse.core.ui.wizards.NewHudsonJobWizard;

public class OpenNewJobWizardAction extends AbstractOpenWizardAction {

	public static final String ID = "Open_New_Maven_Job_Action"; //NON-nls

	public OpenNewJobWizardAction() {
		setId(ID);
		setText("Create new Job in Hudson");
		ImageDescriptor imageDescriptor = HudsonPlugin.getImageDescriptor("/icons/new-package.gif");
		setImageDescriptor(imageDescriptor);
	}
	
	@Override
	protected INewWizard createWizard() throws CoreException {
		return new NewHudsonJobWizard();
	}

}
