package org.hudsonci.eclipse.core.preference;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringButtonFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.hudsonci.eclipse.core.HudsonPlugin;
import org.hudsonci.eclipse.core.connection.HudsonOperation;
import org.hudsonci.eclipse.core.connection.digger.DiggerDialog;


public class HudsonPreferencesPage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	public static final String ID="org.hudsonci.eclipse.hudson.preference";
	
	private IntegerFieldEditor interval;

	private StringFieldEditor password;

	private StringFieldEditor username;

	private BooleanFieldEditor authEnabled;

	public HudsonPreferencesPage() {
		super(FieldEditorPreferencePage.GRID);
	}

	protected void createFieldEditors() {
		addField(new HudsonUrlField(getFieldEditorParent()));

		addPasswordFields();
		
		final BooleanFieldEditor enabled = new BooleanFieldEditor(HudsonPlugin.PREF_AUTO_UPDATE, "Update periodically?", getFieldEditorParent()) {
			protected Button getChangeControl(Composite parent) {
				final Button c = super.getChangeControl(parent);

				c.addSelectionListener(new SelectionListener() {
					public void widgetDefaultSelected(SelectionEvent arg0) {
					}

					public void widgetSelected(SelectionEvent e) {
						interval.setEnabled(c.getSelection(), getFieldEditorParent());
					}

				});
				return c;
			}
		};
		interval = new IntegerFieldEditor(HudsonPlugin.PREF_UPDATE_INTERVAL, "Update interval (seconds)", getFieldEditorParent());

		addField(enabled);
		addField(interval);

		addField(new BooleanFieldEditor(HudsonPlugin.PREF_POPUP_ON_ERROR, "Popup window when state changes to error?", getFieldEditorParent()));
		addField(new BooleanFieldEditor(HudsonPlugin.PREF_POPUP_ON_CONNECTION_ERROR, "Popup error when connection to Hudson fails?", getFieldEditorParent()));

		interval.setEnabled(getPreferenceStore().getBoolean(HudsonPlugin.PREF_AUTO_UPDATE), getFieldEditorParent());
	}

	private void addPasswordFields() {
		
		//adds fields for authentication
		authEnabled = new BooleanFieldEditor(HudsonPlugin.PREF_USE_AUTH, "Use authentication", getFieldEditorParent()) {
			protected Button getChangeControl(Composite parent) {
				final Button c = super.getChangeControl(parent);

				c.addSelectionListener(new SelectionListener() {
					public void widgetDefaultSelected(SelectionEvent arg0) {
					}

					public void widgetSelected(SelectionEvent e) {
						username.setEnabled(c.getSelection(), getFieldEditorParent());
						password.setEnabled(c.getSelection(), getFieldEditorParent());
					}

				});
				return c;
			}
		};
		addField(authEnabled);
		username = new StringFieldEditor(HudsonPlugin.PREF_LOGIN, "Login", getFieldEditorParent());
		password = new StringFieldEditor(HudsonPlugin.PREF_PASSWORD, "Password", getFieldEditorParent()) {
			@Override
			protected void doFillIntoGrid(Composite parent, int numColumns) {
				super.doFillIntoGrid(parent, numColumns);
				
				getTextControl().setEchoChar('*');
			}
		};
		addField(username);
		addField(password);
		username.setEnabled(getPreferenceStore().getBoolean(HudsonPlugin.PREF_USE_AUTH), getFieldEditorParent());
		password.setEnabled(getPreferenceStore().getBoolean(HudsonPlugin.PREF_USE_AUTH), getFieldEditorParent());
	}

	public void init(IWorkbench workbench) {
	}

	protected IPreferenceStore doGetPreferenceStore() {
		return HudsonPlugin.getDefault().getPreferenceStore();
	}

	public void dispose() {
		super.dispose();
	}

	private class HudsonUrlField extends StringButtonFieldEditor {
		public HudsonUrlField(Composite parent) {
			init(HudsonPlugin.PREF_BASE_URL, "Hudson base url");

			setChangeButtonText("Check url");
			setValidateStrategy(StringButtonFieldEditor.VALIDATE_ON_FOCUS_LOST);
			setEmptyStringAllowed(true);
			setErrorMessage("Invalid url");

			createControl(parent);

			Button button = getChangeControl(parent);
			button.addFocusListener(new FocusListener() {
				public void focusGained(FocusEvent e) {
				}

				public void focusLost(FocusEvent e) {
					if (isValid()) {
						getPage().setMessage(null);
					}
				}

			});
		}

		protected boolean checkState() {
			try {
				check();
				clearErrorMessage();
				return true;
			} catch (Exception e) {
				showErrorMessage();
				return false;
			}
		}

		private void check() throws Exception {
			if (getStringValue() != null && !"".equals(getStringValue().trim())) {
				new HudsonOperation().checkValidUrl(getStringValue(), authEnabled.getBooleanValue(), username.getStringValue(), password.getStringValue());
			}
		}

		protected String changePressed() {
			try {
				check();
				getPage().setMessage("Valid url", FieldEditorPreferencePage.INFORMATION);
				setValid(true);
			} catch (Exception e) {
				showErrorMessage(e.getMessage());
				setValid(false);
			}
			return null;
		}
		
		@Override
		protected void doFillIntoGrid(Composite parent, int numColumns) {
			super.doFillIntoGrid(parent, numColumns);
			
			Button discover = new Button(parent, SWT.NONE);
			discover.setText("Discover");
			discover.setToolTipText("Discover running Hudson instances on the local network");
			
			GridData gd = new GridData();
			gd.horizontalSpan = 3;
			gd.horizontalAlignment = SWT.RIGHT;
			discover.setLayoutData(gd);
			discover.addSelectionListener(new SelectionListener() {
				public void widgetDefaultSelected(SelectionEvent arg0) {}

				public void widgetSelected(SelectionEvent arg0) {
					DiggerDialog dialog = new DiggerDialog(getShell());
					if (Window.OK == dialog.open() && dialog.getSelection() != null) {
						setStringValue(dialog.getSelection());
					}
				}
			});
			
		}
	}
}
