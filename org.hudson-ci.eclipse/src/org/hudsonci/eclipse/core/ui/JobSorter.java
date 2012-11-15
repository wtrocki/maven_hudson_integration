package org.hudsonci.eclipse.core.ui;


import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.hudsonci.eclipse.core.job.Job;



public class JobSorter extends ViewerSorter implements Listener {

	private String current = "Project";
	private int direction = SWT.UP;
	private final TableViewer viewer;

	public JobSorter(TableViewer viewer) {
		this.viewer = viewer;
	}

	public int compare(Viewer viewer, Object e1, Object e2) {
		Job j1 = (Job) e1;
		Job j2 = (Job) e2;

		int res = 0;
		if ("Project".equals(current)) {
			res = j1.getName().compareTo(j2.getName());
		} else if ("Build".equals(current)) {
			if (j1.getLastBuild() == null) {
				res = -1;
			} else if (j2.getLastBuild() == null) {
				res = 1;
			} else {
				res = Long.valueOf(j1.getLastBuild().getNumber()).compareTo(Long.valueOf(j2.getLastBuild().getNumber()));
			}
		} else if ("Status".equals(current)) {
			res = getComparator().compare(j1.getStatus(), j2.getStatus());
		} else if ("Health".equals(current)) {
			res = getComparator().compare(j1.getHealth(), j2.getHealth());
		} else if ("Date and Time".equals(current)) {
			if (j1.getLastBuild() == null) {
				res = -1;
			} else if (j2.getLastBuild() == null) {
				res = 1;
			} else {
				res = j1.getLastBuild().getTimestamp().compareTo(j2.getLastBuild().getTimestamp());
			}
		}

		if (direction == SWT.DOWN) {
			res = -res;
		}
		return res;
	}

	public void handleEvent(Event event) {
		TableColumn col = (TableColumn) event.widget;
		Table table = col.getParent();
		
		if (col.getText().equals(current)) {
			direction = direction == SWT.UP ? SWT.DOWN : SWT.UP;
		} else {
			direction = SWT.UP;
			table.setSortColumn(col);
		}
		table.setSortDirection(direction);
		
		
		current = col.getText();
		viewer.refresh();
	}
}
