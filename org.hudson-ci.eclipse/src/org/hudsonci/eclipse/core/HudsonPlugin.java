package org.hudsonci.eclipse.core;

import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

/**
 * The activator class controls the plug-in life cycle
 */
public class HudsonPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.hudson-ci.eclipse.main";

	public static final String JOB_FAMILY_UPDATE = PLUGIN_ID + ".update";

	public static final String PREF_BASE_URL = "base_url";

	public static final String PREF_AUTO_UPDATE = "auto_update";

	public static final String PREF_UPDATE_INTERVAL = "update_interval";

	public static final String PREF_POPUP_ON_ERROR = "popup_error";
	
	public static final String PREF_POPUP_ON_CONNECTION_ERROR = "popup_connection_error";

	public static final String PREF_FILTER_SUCCESS = "filter_success";

	public static final String PREF_FILTER_FAIL = "filter_fail";

	public static final String PREF_FILTER_FAIL_TEST = "filter_fail_test";

	public static final String PREF_FILTER_NO_BUILD = "filter_no_build";

	public static final String PREF_FILTER_IGNORE_PROJECT = "filter_ignore_build";

	public static final String PREF_FILTER_DISABLED = "filter_disabled";

	public static final String PREF_SECURITY_TOKEN = "security_token";
	
	public static final String PREF_FILTER_NAME = "filter_by_name";
	
	public static final String PREF_SELECTED_VIEW = "selected_view";

	public static final String PREF_USE_AUTH = "use_auth";

	public static final String PREF_LOGIN = "login";

	public static final String PREF_PASSWORD = "password";
	
	public static final String PREF_PARAMETERS = "parameter_";

	// The shared instance
	private static HudsonPlugin plugin;

	private ImageRegistry registry;

	private ServiceTracker tracker;

	/**
	 * The constructor
	 */
	public HudsonPlugin() {
		plugin = this;
		this.registry = new ImageRegistry();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);

		tracker = new ServiceTracker(getBundle().getBundleContext(), IProxyService.class.getName(), null);
		tracker.open();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		tracker.close();
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static HudsonPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in relative path
	 * 
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	/**
	 * Returns an image for the image file at the given plug-in relative path
	 * 
	 * @param path
	 *            the path
	 * @return the image
	 */
	public static Image getImage(String path) {
		Image img = plugin.registry.get(path);
		if (img == null) {
			final ImageDescriptor desc = getImageDescriptor(path);
			if (desc != null) {
				img = desc.createImage(true);
				plugin.registry.put(path, img);
			}
		}
		return img;
	}

	public IProxyService getProxyService() {
		return (IProxyService) tracker.getService();
	}
	
	/**
	 * 
	 * @return main workbench shell. (Dialogs will be placed in center of workbench)
	 */
	public static Shell getRefShell() {
		return plugin.getWorkbench().getActiveWorkbenchWindow().getShell();
	}

	/**
	 * Logs error
	 *
	 */
	public static void logMessage(String message,Exception e) {
		plugin.getLog().log(new Status(Status.ERROR, PLUGIN_ID, message,e));

	}
	
}
