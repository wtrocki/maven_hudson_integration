package org.hudsonci.eclipse.core.preference;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.hudsonci.eclipse.core.HudsonPlugin;


public class PreferenceInitializer extends AbstractPreferenceInitializer {

	public PreferenceInitializer() {
	}

	public void initializeDefaultPreferences() {
		IPreferenceStore node = HudsonPlugin.getDefault().getPreferenceStore();
		node.setDefault(HudsonPlugin.PREF_AUTO_UPDATE, "true");
		node.setDefault(HudsonPlugin.PREF_UPDATE_INTERVAL, "30");
		node.setDefault(HudsonPlugin.PREF_POPUP_ON_ERROR, "false");
		node.setDefault(HudsonPlugin.PREF_POPUP_ON_CONNECTION_ERROR, "false");
		node.setDefault(HudsonPlugin.PREF_BASE_URL, "http://localhost:8080");
		String[] filterprefs = new String[] { HudsonPlugin.PREF_FILTER_FAIL, HudsonPlugin.PREF_FILTER_FAIL_TEST, HudsonPlugin.PREF_FILTER_NO_BUILD, HudsonPlugin.PREF_FILTER_SUCCESS, HudsonPlugin.PREF_FILTER_DISABLED };
		for (int i = 0; i < filterprefs.length; i++) {
			node.setDefault(filterprefs[i], true);
		}
	}
}
