package org.hudsonci.eclipse.maven.actions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.hudsonci.eclipse.core.HudsonPlugin;

/**
 * <p>
 * Abstract base classed used for the open wizard actions.
 * </p>
 * 
 * <p>
 * Note: This class is for internal use only. Clients should not use this class.
 * </p>
 */
public abstract class AbstractOpenWizardAction extends Action   {

	private Shell fShell;
	private IStructuredSelection fSelection;

	/**
	 * Creates the action.
	 */
	protected AbstractOpenWizardAction() {
		fShell = null;
		fSelection = null;
	}

	public void run() {
		Shell shell = getShell();
		try {
			INewWizard wizard = createWizard();
			wizard.init(PlatformUI.getWorkbench(), getSelection());

			WizardDialog dialog = new WizardDialog(shell, wizard);
			PixelConverter converter = new PixelConverter(
					JFaceResources.getDialogFont());
			dialog.setMinimumPageSize(
					converter.convertWidthInCharsToPixels(70),
					converter.convertHeightInCharsToPixels(20));
			dialog.create();
			dialog.open();
		} catch (CoreException e) {
			HudsonPlugin.logMessage("Invalid source", e);
		}
	}

	/**
	 * Creates and configures the wizard. This method should only be called
	 * once.
	 * 
	 * @return returns the created wizard.
	 * @throws CoreException
	 *             exception is thrown when the creation was not successful.
	 */
	abstract protected INewWizard createWizard() throws CoreException;

	/**
	 * Returns the configured selection. If no selection has been configured
	 * using {@link #setSelection(IStructuredSelection)}, the currently selected
	 * element of the active workbench is returned.
	 * 
	 * @return the configured selection
	 */
	protected IStructuredSelection getSelection() {
		if (fSelection == null) {
			return evaluateCurrentSelection();
		}
		return fSelection;
	}

	private IStructuredSelection evaluateCurrentSelection() {
		IWorkbenchWindow window = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		if (window != null) {
			ISelection selection = window.getSelectionService().getSelection();
			if (selection instanceof IStructuredSelection) {
				return (IStructuredSelection) selection;
			}
		}
		return StructuredSelection.EMPTY;
	}

	/**
	 * Configures the selection to be used as initial selection of the wizard.
	 * 
	 * @param selection
	 *            the selection to be set or <code>null</code> to use the
	 *            selection of the active workbench window
	 */
	public void setSelection(IStructuredSelection selection) {
		fSelection = selection;
	}

	/**
	 * Returns the configured shell. If no shell has been configured using
	 * {@link #setShell(Shell)}, the shell of the currently active workbench is
	 * returned.
	 * 
	 * @return the configured shell
	 */
	protected Shell getShell() {
		if (fShell == null) {
			return HudsonPlugin.getRefShell();
		}
		return fShell;
	}

	/**
	 * Configures the shell to be used as parent shell by the wizard.
	 * 
	 * @param shell
	 *            the shell to be set or <code>null</code> to use the shell of
	 *            the active workbench window
	 */
	public void setShell(Shell shell) {
		fShell = shell;
	}

}
