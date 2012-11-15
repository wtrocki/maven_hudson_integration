package org.hudsonci.eclipse.core.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Shell;

public class SecurityTokenAction extends Action {

	@SuppressWarnings("unused")
	private final Shell shell;

	public SecurityTokenAction(Shell shell) {
		this.shell = shell;
		setText("Set security token...");
		setToolTipText("Configure the security token used to schedule builds");
	}

	public void run() {

	}
}
