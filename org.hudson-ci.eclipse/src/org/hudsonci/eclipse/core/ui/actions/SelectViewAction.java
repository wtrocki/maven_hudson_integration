package org.hudsonci.eclipse.core.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.TableViewer;
import org.hudsonci.eclipse.core.HudsonPlugin;
import org.hudsonci.eclipse.core.ui.JobContentProvider;
import org.hudsonci.eclipse.core.ui.JobView;


public class SelectViewAction extends Action{

	private final IPreferenceStore store;
	private final JobView view;
	private final JobContentProvider jobContentProvider;
	private final TableViewer viewer;

	public SelectViewAction(TableViewer viewer, JobView view, JobContentProvider jobContentProvider) {
		super(view.getName(), AS_RADIO_BUTTON);
		this.viewer = viewer;
		this.view = view;
		this.jobContentProvider = jobContentProvider;
		setToolTipText(view.getName());
		
		store = HudsonPlugin.getDefault().getPreferenceStore();
		
		if (view.getName().equals(store.getString(HudsonPlugin.PREF_SELECTED_VIEW))) {
			setChecked(true);
			run();
		}
	}
	
	public void run() {
		store.setValue(HudsonPlugin.PREF_SELECTED_VIEW, view.getName());

		jobContentProvider.setView(view.getUrl());
		viewer.refresh();
	}
}
