package org.hudsonci.eclipse.core.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.eclipse.ui.part.Page;
import org.hudsonci.eclipse.core.ui.actions.OpenPreferencesAction;


public class ConfigErrorPage extends Page {

	private Form form;

	public void createControl(Composite parent) {
		FormToolkit toolkit = new FormToolkit(parent.getDisplay());
		form = toolkit.createForm(parent);
		form.setText("Not configured");

		TableWrapLayout layout = new TableWrapLayout();
		form.getBody().setLayout(layout);
		FormText text = toolkit.createFormText(form.getBody(), true);
		text.setText("The Hudson plugin has not yet been configured. Use the preferences page to configure.", false, false);

		Hyperlink link = toolkit.createHyperlink(form.getBody(), "Open preferences", SWT.WRAP);
		link.addHyperlinkListener(new IHyperlinkListener() {
			public void linkActivated(HyperlinkEvent e) {
				new OpenPreferencesAction(getSite().getShell()).run();
			}

			public void linkEntered(HyperlinkEvent e) {
			}

			public void linkExited(HyperlinkEvent e) {
			}
		});
		return;
	}

	public Control getControl() {
		return form;
	}

	public void setFocus() {
	}

}
