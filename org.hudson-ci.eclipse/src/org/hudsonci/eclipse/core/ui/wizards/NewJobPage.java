package org.hudsonci.eclipse.core.ui.wizards;

import java.util.prefs.Preferences;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.hudsonci.eclipse.core.preference.HudsonPreferencesPage;

/**
 * The "New" wizard page allows setting the container for the new file as well
 * as the file name. The page will only accept file name without the extension
 * OR with the extension that matches the expected one (mpe).
 */
public class NewJobPage extends WizardPage {

	private Text containerText;
	private Text nameText;
	private IProject project;
	private ISelection selection;

	/**
	 * Constructor for SampleNewWizardPage.
	 * 
	 * @param pageName
	 */
	public NewJobPage(ISelection selection) {
		super("wizardPage");
		setTitle("New Job");
		setDescription("This wizard creates a new Hudson Job.");
		this.selection = selection;
	}

	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 3;
		layout.verticalSpacing = 9;
		Label label = new Label(container, SWT.NULL);
		label.setText("&Project name:");

		containerText = new Text(container, SWT.BORDER | SWT.SINGLE);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		containerText.setLayoutData(gd);
		containerText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				String text = containerText.getText();
				project = ResourcesPlugin.getWorkspace().getRoot()
						.getProject(text);
				if (project == null || !project.isAccessible()) {
					updateStatus("Invalid project");
					return;
				}
			}
		});

		Button button = new Button(container, SWT.PUSH);
		button.setText("Browse...");
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleBrowse();
			}
		});
		label = new Label(container, SWT.NULL);
		label.setText("&Job Name:");

		nameText = new Text(container, SWT.BORDER | SWT.SINGLE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		nameText.setLayoutData(gd);
		nameText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				validateName();
			}
		});
		new Label(container, SWT.NONE);
		
		Link link=new Link(container, SWT.NONE);
		link.setText("All Hudson configurations can be configured on <a>Hudson CI</a> preference page");
		link.addListener(SWT.Selection, new Listener() {
		      public void handleEvent(Event event) {
		    	 final Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		       final PreferenceDialog preference = PreferencesUtil.createPreferenceDialogOn(shell, HudsonPreferencesPage.ID, null, null);
		       preference.open();
		      }});
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan=3;
		link.setLayoutData(gd);
		
		initialize();
		validateName();
		setControl(container);
	}

	/**
	 * Tests if the current workbench selection is a suitable container to use.
	 */
	private void initialize() {
		if (selection != null && selection.isEmpty() == false
				&& selection instanceof IStructuredSelection) {
			IStructuredSelection ssel = (IStructuredSelection) selection;
			if (ssel.size() > 1)
				return;
			Object obj = ssel.getFirstElement();
			if (obj instanceof IResource) {
				if (obj instanceof IProject)
					project = (IProject) obj;
				else
					project = ((IResource) obj).getProject();
				containerText.setText(project.getFullPath().toString());
			}
		}
		containerText.setEditable(false);
		nameText.setText("MyJob");
	}

	/**
	 * Uses the standard container selection dialog to choose the new value for
	 * the container field.
	 */

	private void handleBrowse() {
		ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(
				getShell(), new WorkbenchLabelProvider(),
				new BaseWorkbenchContentProvider());
		dialog.setTitle("Select target Project");
		dialog.setMessage("Select the target project:");
		dialog.setInput(ResourcesPlugin.getWorkspace().getRoot());
		dialog.setAllowMultiple(false);
		dialog.addFilter(new ViewerFilter() {
			@Override
			public boolean select(Viewer viewer, Object parentElement,
					Object element) {
				if (element instanceof IProject) {
					return true;
				}
				return false;
			}
		});

		if (dialog.open() == ContainerSelectionDialog.OK) {
			Object result = dialog.getFirstResult();
			if (result instanceof IProject) {
				IProject project = (IProject) result;
				containerText.setText(project.getName());
				this.project = project;
			}
		}
	}

	/**
	 * Ensures that both text fields are set.
	 */

	private void validateName() {
		String fileName = getJobName();

		if (fileName.length() == 0) {
			updateStatus("Hudson Job name must be specified");
			return;
		}
		updateStatus(null);
	}

	private void updateStatus(String message) {
		setErrorMessage(message);
		setPageComplete(message == null);
	}

	public String getContainerName() {
		return containerText.getText();
	}

	public String getJobName() {
		return nameText.getText();
	}

	public IProject getProject() {
		return project;
	}
}