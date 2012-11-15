package org.hudsonci.eclipse.core.ui.actions;


import org.eclipse.jface.action.Action;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.texteditor.StatusLineContributionItem;
import org.hudsonci.eclipse.core.HudsonPlugin;
import org.hudsonci.eclipse.core.job.Job;



public class BuildStatusAction extends StatusLineContributionItem {
	public static final String ID = HudsonPlugin.PLUGIN_ID + ".statusline";
	
	public BuildStatusAction() {
		super(ID);

		setImage(HudsonPlugin.getImage("icons/hudson.png"));
		setToolTipText("Hudson status");
		
		setActionHandler(new Action() {
			public void run() {
				try {
					HudsonPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(HudsonPlugin.PLUGIN_ID + ".views.HudsonView");
				} catch (PartInitException e) {
					e.printStackTrace();
				}
			}
		});
	}

	public void setOk() {
		setImage(HudsonPlugin.getImage("icons/blue.png"));
		setToolTipText("All builds ok");
	}
	
	public void setUnknown() {
		setImage(HudsonPlugin.getImage("icons/yellow.png"));
		setToolTipText("Unknown status");
	}
	
	public void setError(Job job) {
		setImage(HudsonPlugin.getImage("icons/red.png"));
		setToolTipText("Build error in " + job.getName());
	}
}
