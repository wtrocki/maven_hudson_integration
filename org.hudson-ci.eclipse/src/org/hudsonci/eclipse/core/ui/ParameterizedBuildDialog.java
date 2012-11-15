package org.hudsonci.eclipse.core.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.hudsonci.eclipse.core.HudsonPlugin;
import org.hudsonci.eclipse.core.build.BuildParameter;
import org.hudsonci.eclipse.core.connection.HudsonOperation;
import org.hudsonci.eclipse.core.connection.ParametersRequiredException;
import org.hudsonci.eclipse.core.job.Job;


public class ParameterizedBuildDialog extends Dialog {

	private final HudsonOperation client;
	private final Job job;
	
	private List<BuildParameter> parameters;
	private boolean defaultParams;
	private final Preferences preferences;

	public ParameterizedBuildDialog(HudsonOperation client, Job job, Shell parentShell, Preferences preferences) {
		super(parentShell);
		this.client = client;
		this.job = job;
		this.preferences = preferences;
		this.defaultParams = true;

		parameters = job.getDefaultParameters();
		if ((parameters == null) || (parameters.size() == 0)) {
			defaultParams = false;
		}
		if (job.getLastBuild() != null) {
			parameters = job.getLastBuild().getParameters();
		}
		if ((parameters == null) || (parameters.size() == 0)) {
			parameters = BuildParameter.deserialize(preferences.getString(HudsonPlugin.PREF_PARAMETERS + job.getName()));
			if (parameters == null) {
				parameters = new ArrayList<BuildParameter>();
				parameters.add(new BuildParameter("param1", "value1"));
			}
		}
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite createDialogArea = new Composite(parent, SWT.NULL);
		createDialogArea.setLayout(new GridLayout());
		
		Label label = new Label(createDialogArea, SWT.CENTER);
		label.setText("Build requires parameters, please configure");
		
		final TableViewer table = new TableViewer(createDialogArea, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		Table t = table.getTable();
		GridData gridData = new GridData();
		gridData.heightHint = 150;
		gridData.widthHint = 300;
		t.setLayoutData(gridData);
		
		TableColumn col = new TableColumn(t, SWT.LEFT);
		col.setText("BuildParameter");

		col = new TableColumn(t, SWT.LEFT);
		col.setText("Value");

		t.setHeaderVisible(true);
		table.setColumnProperties(new String[] { "parameter", "value"});

		if (defaultParams) {
			Button def = new Button(createDialogArea, SWT.NONE);
			def.setText("Load default parameters");
			def.addSelectionListener(new SelectionListener() {
				public void widgetDefaultSelected(SelectionEvent arg0) {
				}
				public void widgetSelected(SelectionEvent arg0) {
					parameters = job.getDefaultParameters();
					table.refresh();
				}
			});
			Button last = new Button(createDialogArea, SWT.NONE);
			last.setText("Load last build parameters");
			last.addSelectionListener(new SelectionListener() {
				public void widgetDefaultSelected(SelectionEvent arg0) {
				}
				public void widgetSelected(SelectionEvent arg0) {
					parameters = job.getLastBuild().getParameters();
					table.refresh();
				}
			});
		}

		Button add = new Button(createDialogArea, SWT.NONE);
		add.setText("Add parameter");
		add.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
			public void widgetSelected(SelectionEvent arg0) {
				parameters.add(new BuildParameter("param" + (parameters.size() + 1), "value" + (parameters.size() + 1)));
				table.refresh();
			}
		});
		
		table.setContentProvider(new ContentProvider());
		table.setLabelProvider(new ParameterLabelProvider());
		table.setCellEditors(new CellEditor[] { new TextCellEditor(t), new TextCellEditor(t)});
		table.setCellModifier(new ParameterModifier(table));
		
		table.refresh();
		table.setInput(getShell());

		for (int i = 0; i < 2; i++) {
			t.getColumn(i).pack();
		}
		return createDialogArea;
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Configure Build Parameters");
	}
	
	@Override
	protected void okPressed() {
		super.okPressed();
		
		try {
			preferences.setValue(HudsonPlugin.PREF_PARAMETERS + job.getName(), BuildParameter.serialize(parameters));
			client.scheduleJob(job.getName(), parameters);
		} catch (IOException e) {
			ErrorDialog.openError(getShell(), "Unable to schedule build", "Unable to schedule build", new Status(Status.ERROR, HudsonPlugin.PLUGIN_ID, 0, "Unable to schedule job", e));
		} catch (ParametersRequiredException e) {
			ParameterizedBuildDialog d = new ParameterizedBuildDialog(client, job, getShell(), preferences);
			d.open();
		}
		
	}

	private class ContentProvider implements IStructuredContentProvider {

		public Object[] getElements(Object arg0) {
			return parameters.toArray(new BuildParameter[parameters.size()]);
		}

		public void dispose() {}
		public void inputChanged(Viewer arg0, Object arg1, Object arg2) {}
	}
	
	
	private static class ParameterLabelProvider extends LabelProvider implements ITableLabelProvider {

		public Image getColumnImage(Object arg0, int arg1) {
			return null;
		}

		public String getColumnText(Object arg0, int arg1) {
			BuildParameter p = (BuildParameter) arg0;
			switch (arg1) {
				case 0:
					return p.getName();
				case 1:
					return p.getValue();
			}
			return null;
		}
	}
	
	private static class ParameterModifier implements ICellModifier {
		private final TableViewer table;

		public ParameterModifier(TableViewer table) {
			this.table = table;
		}

		public boolean canModify(Object arg0, String arg1) {
			return true;
		}

		public Object getValue(Object arg0, String arg1) {
			BuildParameter p = (BuildParameter) arg0;
			if ("parameter".equals(arg1)) {
				return p.getName();
			} else if ("value".equals(arg1)) {
				return p.getValue();
			}
			return null;
		}

		public void modify(Object arg0, String arg1, Object arg2) {
			if (arg0 instanceof Item) {
				arg0 = ((Item)arg0).getData();
			}
			BuildParameter p = (BuildParameter) arg0;
			if ("parameter".equals(arg1)) {
				p.setName((String) arg2);
			} else if ("value".equals(arg1)) {
				p.setValue((String) arg2);
			}
			
			table.refresh();
		}
		
	}
}
