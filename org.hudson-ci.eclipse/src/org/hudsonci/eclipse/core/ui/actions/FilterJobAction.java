package org.hudsonci.eclipse.core.ui.actions;

import org.eclipse.jface.preference.BooleanPropertyAction;
import org.eclipse.swt.widgets.Event;
import org.hudsonci.eclipse.core.HudsonPlugin;


public class FilterJobAction extends BooleanPropertyAction {

	public FilterJobAction(String title, String tooltip, String job) throws IllegalArgumentException {
		super(title, HudsonPlugin.getDefault().getPreferenceStore(), HudsonPlugin.PREF_FILTER_IGNORE_PROJECT + "_" + job);

		setToolTipText(tooltip);
	}

	public void runWithEvent(Event event) {
		super.runWithEvent(event);

	}

}
