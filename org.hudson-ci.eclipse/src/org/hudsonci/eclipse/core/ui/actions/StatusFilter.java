package org.hudsonci.eclipse.core.ui.actions;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.hudsonci.eclipse.core.HudsonPlugin;
import org.hudsonci.eclipse.core.job.Job;


public class StatusFilter extends ViewerFilter {

	public boolean select(Viewer viewer, Object parentElement, Object element) {
		Job j = (Job) element;
		IPreferenceStore prefs = HudsonPlugin.getDefault().getPreferenceStore();

		switch (j.getStatus().getStatus()) {
			case SUCCESS:
				return prefs.getBoolean(HudsonPlugin.PREF_FILTER_SUCCESS);
			case FAIL:
				return prefs.getBoolean(HudsonPlugin.PREF_FILTER_FAIL);
			case TEST_FAIL:
				return prefs.getBoolean(HudsonPlugin.PREF_FILTER_FAIL_TEST);
			case NO_BUILD:
				return prefs.getBoolean(HudsonPlugin.PREF_FILTER_NO_BUILD);
			case DISABLED:
				return prefs.getBoolean(HudsonPlugin.PREF_FILTER_DISABLED);
		}

		return true;
	}

}
