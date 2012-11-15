package org.hudsonci.eclipse.core.ui.wizards;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import org.hudsonci.eclipse.core.HudsonPlugin;
import org.hudsonci.eclipse.core.operation.CreateJobOperation;
import org.maven.ide.components.pom.CiManagement;
import org.maven.ide.components.pom.Model;
import org.maven.ide.eclipse.MavenPlugin;
import org.maven.ide.eclipse.embedder.MavenModelManager;
import org.maven.ide.eclipse.embedder.ProjectUpdater;
import org.maven.ide.eclipse.project.IMavenProjectFacade;
import org.maven.ide.eclipse.project.MavenProjectManager;

/**
 * This is a sample new wizard. Its role is to create a new file resource in the
 * provided container. If the container resource (a folder or a project) is
 * selected in the workspace when the wizard is opened, it will accept it as the
 * target container. The wizard creates one file with the extension "mpe". If a
 * sample multi-page editor (also available as a template) is registered for the
 * same extension, it will be able to open it.
 */

public class NewHudsonJobWizard extends Wizard implements INewWizard {

	private NewJobPage page;
	private ISelection selection;
	private final String icon = "/icons/wizban/package.gif";
	private String HUDSON_SYSTEM_NAME = "hudson";;

	/**
	 * Constructor for NewHudsonJob.
	 */
	public NewHudsonJobWizard() {
		super();
		setNeedsProgressMonitor(true);
		setDefaultPageImageDescriptor(HudsonPlugin.getImageDescriptor(icon));
	}

	/**
	 * Adding the page to the wizard.
	 */
	public void addPages() {
		page = new NewJobPage(selection);
		addPage(page);
	}

	/**
	 * This method is called when 'Finish' button is pressed in the wizard. We
	 * will create an operation and run it using wizard as execution context.
	 */
	public boolean performFinish() {
		final String fileName = page.getJobName();
		final IProject project = page.getProject();

		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor)
					throws InvocationTargetException {
				try {
					doFinish(project, fileName, monitor);
				} catch (CoreException e) {
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}
		};
		try {
			getContainer().run(true, false, op);
		} catch (InterruptedException e) {
			return false;
		} catch (InvocationTargetException e) {
			Throwable realException = e.getTargetException();
			MessageDialog.openError(getShell(), "Error",
					realException.getMessage());
			return false;
		}
		return true;
	}

	/**
	 * The worker method.
	 */
	private void doFinish(IProject project, String jobName,
			IProgressMonitor monitor) throws CoreException {
		if (project != null) {
			monitor.setTaskName("Creating job in hudson");
			CreateJobOperation cj = new CreateJobOperation();
			try {
				cj.createNewJob(jobName);
				monitor.worked(3);
			} catch (final Exception e) {
				HudsonPlugin.logMessage("", e);
				Display.getCurrent().asyncExec(new Runnable() {
					public void run() {
						MessageDialog.open(MessageDialog.ERROR, getShell(),
								"Error","Unable to create job in hudson. Check hudson location in preferences", SWT.NONE);
					}
				});
				return;
			}
			monitor.worked(1);
			
			if (project.hasNature("org.maven.ide.eclipse.maven2Nature")) {
				monitor.setTaskName("Performing changes in Maven pom file.");
				MavenProjectManager projectManager = MavenPlugin.getDefault()
						.getMavenProjectManager();
				IMavenProjectFacade facade = projectManager
						.getProject(project);
				try {
					monitor.worked(1);
					final String baseUrl = HudsonPlugin.getDefault()
							.getPreferenceStore()
							.getString(HudsonPlugin.PREF_BASE_URL);
					final URI ciURI=new URI("http://" + baseUrl + "/job/" + jobName
							+ "/build");
					monitor.worked(1);
					final MavenModelManager mavenModelManager = MavenPlugin.getDefault().getMavenModelManager();
					mavenModelManager.updateProject(facade.getPom(), new ProjectUpdater() {
						@Override
						public void update(Model model) {
						CiManagement ciManagement = model.getCiManagement();
						if(ciManagement==null)
							ciManagement=new CiManagmentAdapter();
						 ciManagement.setSystem(HUDSON_SYSTEM_NAME);
						 ciManagement.setUrl(ciURI.toString());
						 model.setCiManagement(ciManagement);
						}
					});
				} catch (Exception e) {
					HudsonPlugin.logMessage(e.getLocalizedMessage(), e);
				}
			}
		}
	}

	/**
	 * We will accept the selection in the workbench to see if we can initialize
	 * from it.
	 * 
	 * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
	}
}