package org.hudsonci.eclipse.core.ui;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.Preferences.PropertyChangeEvent;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ControlContribution;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.SubStatusLineManager;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.hudsonci.eclipse.core.HudsonPlugin;
import org.hudsonci.eclipse.core.connection.HudsonOperation;
import org.hudsonci.eclipse.core.connection.ParametersRequiredException;
import org.hudsonci.eclipse.core.job.Job;
import org.hudsonci.eclipse.core.ui.actions.BuildStatusAction;
import org.hudsonci.eclipse.core.ui.actions.FilterAction;
import org.hudsonci.eclipse.core.ui.actions.FilterJobAction;
import org.hudsonci.eclipse.core.ui.actions.OpenPreferencesAction;
import org.hudsonci.eclipse.core.ui.actions.SelectViewAction;
import org.hudsonci.eclipse.core.ui.actions.StatusFilter;


public class HudsonView extends ViewPart implements PropertyChangeListener {
	private TableViewer viewer;

	private Action scheduleAction;

	private Action refreshAction;

	private Action openBrowserAction;

	private Action viewConsoleAction;

	private String baseUrl;

	private Action securityTokenAction;

	private NameFilter nameFilter;

	private Text nameText;

	private PropertyChangeSupport nameChanges = new PropertyChangeSupport(this);

	private JobContentProvider jobContentProvider;

	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		configurePreferences();
		nameChanges.addPropertyChangeListener(this);
	}

	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout(SWT.VERTICAL));

		viewer = new TableViewer(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);

		JobSorter sorter = new JobSorter(viewer);
		BuildStatusAction buildStatusAction = new BuildStatusAction();

		Table t = viewer.getTable();
		t.setLayout(new GridLayout());
		t.setLayoutData(new GridData(GridData.FILL_BOTH));
		TableColumn col = new TableColumn(t, SWT.NONE);
		col.setText("Project");
		col.addListener(SWT.Selection, sorter);

		col = new TableColumn(t, SWT.LEFT);
		col.setText("Build");
		col.addListener(SWT.Selection, sorter);

		col = new TableColumn(t, SWT.LEFT);
		col.setText("Date and Time");
		col.addListener(SWT.Selection, sorter);

		col = new TableColumn(t, SWT.LEFT);
		col.setText("Status");
		col.setWidth(60);
		col.addListener(SWT.Selection, sorter);
		
		col = new TableColumn(t, SWT.LEFT);
		col.setText("Health");
		col.setWidth(20);
		col.addListener(SWT.Selection, sorter);
		
		t.setHeaderVisible(true);

		viewer.setColumnProperties(new String[] { "Project", "Status", "" });
		jobContentProvider = new JobContentProvider(viewer, buildStatusAction);

		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();

		// Set the content provider after creating the filters since 
		// a refresh on the viewer is triggered for each filter added.
		viewer.setContentProvider(jobContentProvider);
		viewer.setLabelProvider(new JobLabelProvider());

		viewer.setSorter(sorter);
		viewer.setInput(getViewSite());

		SubStatusLineManager slm = (SubStatusLineManager) getViewSite().getActionBars().getStatusLineManager();
		IContributionManager slmParent = slm.getParent();
		if (slmParent.find(BuildStatusAction.ID) == null) {
			slmParent.add(buildStatusAction);
		}

		for (int i = 0; i < 3; i++) {
			t.getColumn(i).pack();
		}
		
		refreshAction.run();
	}

	private void configurePreferences() {
		final Preferences prefs = HudsonPlugin.getDefault().getPluginPreferences();
		prefs.addPropertyChangeListener(new Preferences.IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				baseUrl = prefs.getString(HudsonPlugin.PREF_BASE_URL);
				refreshAction.run();
			}
		});
		baseUrl = prefs.getString(HudsonPlugin.PREF_BASE_URL);
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				HudsonView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalToolBar(bars.getToolBarManager());
		fillLocalPullDown(bars.getMenuManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(refreshAction);
		manager.add(new Separator());
		manager.add(new OpenPreferencesAction(getSite().getShell()));

		MenuManager filtermenu = new MenuManager("Filters");

		StatusFilter filter = new StatusFilter();
		nameFilter = new NameFilter();
		nameChanges.addPropertyChangeListener(nameFilter);
		filtermenu.add(new FilterAction(viewer, "Successful builds", "Show successful builds", HudsonPlugin.PREF_FILTER_SUCCESS, filter));
		filtermenu.add(new FilterAction(viewer, "Failed builds", "Show failed builds", HudsonPlugin.PREF_FILTER_FAIL, filter));
		filtermenu.add(new FilterAction(viewer, "Test failures", "Show builds with test failures", HudsonPlugin.PREF_FILTER_FAIL_TEST, filter));
		filtermenu.add(new FilterAction(viewer, "Unbuilt projects", "Show projects which have not been built yet", HudsonPlugin.PREF_FILTER_NO_BUILD, filter));
		FilterAction nameFilterAction = new FilterAction(viewer, "Name Filter", "Show Projects by Name", HudsonPlugin.PREF_FILTER_NAME, nameFilter);
		nameFilterAction.addPropertyChangeListener(new IPropertyChangeListener() {
			public void propertyChange(
					org.eclipse.jface.util.PropertyChangeEvent event) {
				if(nameText!=null && nameText.isDisposed()==false)
					nameText.setEnabled(((Boolean)event.getNewValue()).booleanValue());
			}
		});
		filtermenu.add(nameFilterAction);		

		manager.add(filtermenu);
		
		MenuManager viewmenu = new MenuManager("Views");
		updateViewMenu(viewmenu);
		manager.add(viewmenu);
	}

	private void updateViewMenu(MenuManager viewmenu) {
		HudsonOperation client = new HudsonOperation();
		
		try {
			JobView[] views = client.getViews();
			for (JobView view : views) {
				viewmenu.add(new SelectViewAction(viewer, view, jobContentProvider));
			}
		} catch (IOException e) {
			// unable to get views. Don't do anything
		}
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(openBrowserAction);
		manager.add(scheduleAction);
		manager.add(viewConsoleAction);
		manager.add(securityTokenAction);
		manager.add(new Separator());
		manager.add(refreshAction);

		makeFilterAction(manager);

		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void makeFilterAction(IMenuManager manager) {
		IStructuredSelection sel = (IStructuredSelection) viewer.getSelection();
		if (sel.size() == 1) {
			Job j = (Job) sel.getFirstElement();
			manager.add(new FilterJobAction("Ignore failed builds", "Do not report build errors for this project", j.getName()));
		}
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		ControlContribution nameControlContribution = new ControlContribution("contribution_id") {
			protected Control createControl(Composite parent) {			 
				nameText = new Text(parent, SWT.BORDER);

				nameText.addModifyListener(new ModifyListener() {
					public void modifyText(org.eclipse.swt.events.ModifyEvent e) {					
						nameChanges.fireIndexedPropertyChange("text", 0, null,
								nameText.getText());
					}
				});
				return nameText;
			}

			protected int computeWidth(Control control) {
				return 200;
			}
		};

		manager.add(nameControlContribution);
		manager.add(scheduleAction);
		manager.add(refreshAction);
	}

	private void makeActions() {
		scheduleAction = new Action() {
			public void run() {
				IStructuredSelection sel = (IStructuredSelection) viewer.getSelection();
				final Job j = (Job) sel.getFirstElement();

				org.eclipse.core.runtime.jobs.Job sj = new org.eclipse.core.runtime.jobs.Job("Scheduling Hudson build") {
					protected IStatus run(IProgressMonitor monitor) {
						monitor.beginTask("Scheduling job " + j.getName(), 1);
						try {
							final HudsonOperation hudsonClient = new HudsonOperation();
							try {
								hudsonClient.scheduleJob(j.getName());
							} catch (IOException e1) {
								return new Status(Status.ERROR, HudsonPlugin.PLUGIN_ID, 0, "Unable to schedule job", e1);
							} catch (ParametersRequiredException e) {
								Display.getDefault().syncExec(new Runnable() {
									public void run() {
										ParameterizedBuildDialog d = new ParameterizedBuildDialog(hudsonClient, j, getSite().getShell(), HudsonPlugin.getDefault().getPluginPreferences());
										d.open();
										HudsonPlugin.getDefault().savePluginPreferences();
									}
								});
							}

							try {
								Thread.sleep(2000);
							} catch (InterruptedException e) {
								Thread.currentThread().interrupt();
							}
							refreshAction.run();
						} finally {
							monitor.done();
						}
						return Status.OK_STATUS;
					}

				};
				sj.schedule();
			}
		};
		scheduleAction.setText("Schedule new build");
		scheduleAction.setToolTipText("Schedule new build for project");
		scheduleAction.setImageDescriptor(HudsonPlugin.getImageDescriptor("icons/schedule.png"));
		scheduleAction.setEnabled(false);

		openBrowserAction = new Action() {
			public void run() {
				IStructuredSelection sel = (IStructuredSelection) viewer.getSelection();
				openBrowser(sel);
			}
		};
		openBrowserAction.setText("Open in browser");
		openBrowserAction.setToolTipText("Open job status in browser");
		openBrowserAction.setEnabled(false);

		viewConsoleAction = new Action() {
			public void run() {
				IStructuredSelection sel = (IStructuredSelection) viewer.getSelection();
				Job j = (Job) sel.getFirstElement();

				String url = baseUrl + "/job/" + j.getName() + "/lastBuild/consoleText";
				openBrowser(url, "Console output");
			}
		};
		viewConsoleAction.setText("View console output");
		viewConsoleAction.setToolTipText("Open the console output for the latest build");
		viewConsoleAction.setEnabled(false);

		securityTokenAction = new Action() {
			public void run() {
				IStructuredSelection sel = (IStructuredSelection) viewer.getSelection();
				Job j = (Job) sel.getFirstElement();

				showSecurityTokenDialog(j);
			}
		};
		securityTokenAction.setText("Set security token...");
		securityTokenAction.setToolTipText("Configure the security token used to schedule builds");
		securityTokenAction.setEnabled(false);

		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection sel = (IStructuredSelection) event.getSelection();
				scheduleAction.setEnabled(sel.size() == 1);
				openBrowserAction.setEnabled(sel.size() == 1);
				securityTokenAction.setEnabled(sel.size() == 1);

				Job j = (Job) sel.getFirstElement();
				viewConsoleAction.setEnabled(sel.size() == 1 && j.getLastBuild() != null);
			}
		});

		refreshAction = new Action() {
			public void run() {
				org.eclipse.core.runtime.jobs.Job refresh = new org.eclipse.core.runtime.jobs.Job("Refreshing Hudson status") {
					protected IStatus run(IProgressMonitor monitor) {
						monitor.beginTask("Refreshing Hudson status", 1);
						try {
							jobContentProvider.refresh();
							
							Display.getDefault().asyncExec(new Runnable() {
								public void run() {
									viewer.refresh();
								}
							});
						} finally {
							monitor.done();
						}
						return Status.OK_STATUS;
					}
				};
				refresh.schedule();
			}
		};
		refreshAction.setText("Refresh status");
		refreshAction.setToolTipText("Refresh status for all projects");
		refreshAction.setImageDescriptor(HudsonPlugin.getImageDescriptor("icons/refresh.png"));
	}

	private void showSecurityTokenDialog(Job j) {
		Preferences prefs = HudsonPlugin.getDefault().getPluginPreferences();
		String sc = prefs.getString(HudsonPlugin.PREF_SECURITY_TOKEN + "_" + j.getName());
		InputDialog dialog = new InputDialog(getSite().getShell(), "Enter security token for " + j.getName(), "Enter the security token for job " + j.getName(), sc, null);
		if (dialog.open() == InputDialog.OK) {
			prefs.setValue(HudsonPlugin.PREF_SECURITY_TOKEN + "_" + j.getName(), dialog.getValue());
		}
	}

	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {

				final IStructuredSelection sel = (IStructuredSelection) event.getSelection();
				org.eclipse.core.runtime.jobs.Job open = new org.eclipse.core.runtime.jobs.Job("Opening Hudson browser") {
					protected IStatus run(IProgressMonitor monitor) {
						Display.getDefault().syncExec(new Runnable() {
							public void run() {
								openBrowser(sel);
							}
						});
						return Status.OK_STATUS;
					}
				};
				open.setPriority(org.eclipse.core.runtime.jobs.Job.INTERACTIVE);
				open.schedule();
			}
		});
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		if (viewer != null) {
			viewer.getControl().setFocus();
		}
	}

	private void openBrowser(IStructuredSelection selection) {
		final Job j = (Job) selection.getFirstElement();
		final String url = baseUrl + "/job/" + j.getName();
		final String name = j.getName();
		openBrowser(url, name);
	}

	private void openBrowser(final String url, final String name) {
		try {
			IEditorReference[] refs = getSite().getPage().getEditorReferences();

			IEditorPart editor = null;
			for (int i = 0; i < refs.length; i++) {
				if (refs[i].getId().equals(HudsonPlugin.PLUGIN_ID + ".browser")) {
					editor = refs[i].getEditor(true);
					break;
				}
			}
			if (editor != null) {
				((HudsonBrowser) editor).openUrl(url, name);
			}

			if (editor == null) {
				getSite().getPage().openEditor(new IPathEditorInput() {
					public IPath getPath() {
						return new Path(url);
					}

					public boolean exists() {
						return false;
					}

					public ImageDescriptor getImageDescriptor() {
						return null;
					}

					public String getName() {
						return name;
					}

					public IPersistableElement getPersistable() {
						return null;
					}

					public String getToolTipText() {
						return "Hudson browser";
					}

					public Object getAdapter(Class adapter) {
						return null;
					}
				}, HudsonPlugin.PLUGIN_ID + ".browser");
			}
		} catch (PartInitException e) {
			showError("Unable to launch browser", e);
		}
	}

	private void showError(String msg, Exception e) {
		ErrorDialog.openError(getSite().getShell(), "Hudson Error", null, new Status(Status.ERROR, HudsonPlugin.PLUGIN_ID, Status.OK, msg, e));
	}
	
	public void propertyChange(java.beans.PropertyChangeEvent arg0) {
		viewer.removeFilter(nameFilter);
		viewer.addFilter(nameFilter);		 
	}

	public void refreshTableViewer() {
		refreshAction.run();
	}

	// This will remove the hudson status linecontribution when the view closes.
	@Override
	public void dispose() {
		SubStatusLineManager slm = (SubStatusLineManager) getViewSite().getActionBars().getStatusLineManager();
		IContributionManager parent = slm.getParent();
		parent.remove(BuildStatusAction.ID);
		parent.update(false);
		super.dispose();
	}

}