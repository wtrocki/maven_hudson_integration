package org.hudsonci.eclipse.core.ui.actions;


import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.hudsonci.eclipse.core.HudsonPlugin;



public class OpenPreferencesAction extends Action {
	
	private final Shell shell;

	public OpenPreferencesAction(Shell shell) {
		this.shell = shell;
		
		setText("Preferences...");
		setToolTipText("Open preferences for the Hudson plugin");
	}

	public void run() {
		PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(shell, HudsonPlugin.PLUGIN_ID + ".preference", null, null);
		dialog.open();
	}
}
