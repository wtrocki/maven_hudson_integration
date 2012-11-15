package org.hudsonci.eclipse.core.ui;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

public class HudsonBrowser extends EditorPart {

	private Browser browser;

	public void doSave(IProgressMonitor monitor) {

	}

	public void doSaveAs() {
	}

	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		setSite(site);
		setInput(input);
	}

	public boolean isDirty() {
		return false;
	}

	public boolean isSaveAsAllowed() {
		return false;
	}

	public void createPartControl(Composite parent) {
		
		browser = new Browser(parent, SWT.NONE);
		if (getEditorInput() != null) {
			openUrl(((IPathEditorInput)getEditorInput()).getPath().toString(), getEditorInput().getName());
		}
	}

	public void setFocus() {
		browser.setFocus();
	}
	
	protected void setInput(IEditorInput input) {
		super.setInput(input);

		if (browser != null) {
			browser.setUrl(fixBrokenUrlString(((IPathEditorInput)input).getPath().toString()));
		}
	}
	
	public void openUrl(String url, String name) {
		browser.setUrl(fixBrokenUrlString(url));
		setPartName("Hudson: " + name);
	}

	private String fixBrokenUrlString(String url) {
		if (url.indexOf("://") < 0) {
			return url.replaceFirst(":/", "://");
		}
		return url;
	}


}
