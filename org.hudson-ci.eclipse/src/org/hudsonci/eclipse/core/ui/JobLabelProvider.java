package org.hudsonci.eclipse.core.ui;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.hudsonci.eclipse.core.job.Job;


public class JobLabelProvider extends LabelProvider implements ITableLabelProvider {

	public Image getColumnImage(Object element, int columnIndex) {
		Job j = (Job) element;
		if (columnIndex == 3 && j.getStatus() != null) {
			return j.getStatus().getImage();
		}
		if (columnIndex == 4 && j.getHealth() != null) {
			return j.getHealth().getImage();
		}
		return null;
	}

	public String getColumnText(Object element, int columnIndex) {
		Job j = (Job) element;
		switch (columnIndex) {
			case 0:
				return j.getName();
			case 1:
				if (j.getLastBuild() == null) {
					return "No build";
				}
				return "#" + j.getLastBuild().getNumber();
			case 2:
				if (j.getLastBuild() == null) {
					return "No build";
				}
				return j.getLastBuild().getTimestamp().toLocaleString();
		}
		return null;
	}
}
