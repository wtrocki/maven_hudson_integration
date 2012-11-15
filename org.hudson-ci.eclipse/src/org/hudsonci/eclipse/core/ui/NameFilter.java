package org.hudsonci.eclipse.core.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.hudsonci.eclipse.core.HudsonPlugin;
import org.hudsonci.eclipse.core.job.Job;


public class NameFilter extends ViewerFilter implements PropertyChangeListener{

	private String filterStr = null;

	public boolean select(Viewer viewer, Object parentElement, Object element) {
		Job job = (Job) element;
		String name = job.getName();
		IPreferenceStore prefs = HudsonPlugin.getDefault().getPreferenceStore();           
		if(!prefs.getBoolean(HudsonPlugin.PREF_FILTER_NAME)) {                     
			return true;
		}
		if (filterStr == null || name.startsWith(filterStr)) {                   
			return true;
		}
		return false;
	}

	public void propertyChange(PropertyChangeEvent evt) {
		filterStr = (String)evt.getNewValue();                  
		((HudsonView)evt.getSource()).refreshTableViewer();
	}

}
