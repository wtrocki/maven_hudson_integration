package org.hudsonci.eclipse.core.connection.digger;

import java.util.Collection;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.hudsonci.eclipse.core.HudsonPlugin;


public class DiggerDialog extends Dialog {

	private static final int DIG_ID = IDialogConstants.CLIENT_ID + 1;
	private List list;
	private Button digButton;
	private String selection = null;
	private ProgressBar progressBar;
	private HudsonDiggerThread digThread;
	private Button okButton;

	public DiggerDialog(Shell parentShell) {
		super(parentShell);
		setBlockOnOpen(true);
		digThread = new HudsonDiggerThread(this);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Hudson Digger");
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		Label header = new Label(composite, SWT.NONE);
		header.setText("Discover running Hudson instances");
		header.setFont(new Font(getShell().getDisplay(), new FontData(header.getFont().getFontData()[0].getName(), 10, SWT.BOLD)));
		
		Label discovered = new Label(composite, SWT.NONE);
		discovered.setText("Discovered nodes:");
		
		list = new List(composite, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		GridData gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;
		gd.widthHint = 250;
		gd.heightHint = 100;
		list.setLayoutData(gd);
		list.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				list = null;
			}
		});
		list.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				refreshSelection();
			}

			public void widgetSelected(SelectionEvent e) {
				refreshSelection();

			}

			private void refreshSelection() {
				String[] selections = list.getSelection();
				if (0 != selections.length) {
					selection = selections[0];
				}
				okButton.setEnabled(selection != null);
			}
		});

		progressBar = new ProgressBar(composite, SWT.HORIZONTAL | SWT.INDETERMINATE);
		progressBar.setVisible(false);
		gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = false;
		gd.widthHint = 250;
		gd.horizontalAlignment = SWT.CENTER;
		gd.verticalAlignment = SWT.CENTER;
		progressBar.setLayoutData(gd);
		
		return composite;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		digButton = createButton(parent, DiggerDialog.DIG_ID, "Dig", false);
		
		okButton = getButton(IDialogConstants.OK_ID);
		okButton.setEnabled(false);
		
	}
	
	@Override
	protected void buttonPressed(int buttonId) {
		if (DiggerDialog.DIG_ID == buttonId) {
			digPressed();
		} else {
			super.buttonPressed(buttonId);
		}
	}

	@Override
	protected void cancelPressed() {
		if (digThread.isDigging()) {
			digThread.stopDigging();
		}
		super.cancelPressed();
	}
	
	@Override
	protected void okPressed() {
		if (digThread.isDigging()) {
			digThread.stopDigging();
		}
		super.okPressed();
	}
	
	private void digPressed() {
		digButton.setEnabled(false);
		getButton(IDialogConstants.OK_ID).setEnabled(false);
		progressBar.setVisible(true);
		digThread.start();
	}
	
	void updateList(Collection<HudsonServer> servers) {
		this.list.removeAll();
		for (HudsonServer server : servers) {
			String url = server.getUrl();
			if (!url.isEmpty() && null != url) {
				this.list.add(url);
			}
		}
	}

	void digFinished(Set<HudsonServer> servers) {
		updateList(servers);
		digButton.setEnabled(true);
		getButton(IDialogConstants.OK_ID).setEnabled(true);
		progressBar.setVisible(false);
	}

	void digException(Exception e) {
		HudsonPlugin myPlugin = HudsonPlugin.getDefault();
		myPlugin.getLog().log(
				new Status(IStatus.ERROR, myPlugin.getBundle()
						.getSymbolicName(), "Error digging for Hudson servers",
						e));
	}

	public String getSelection() {
		return selection;
	}

	@Override
	public boolean close() {
		if (digThread.isDigging()) {
			return false;
		}
		return super.close();
	}
}
