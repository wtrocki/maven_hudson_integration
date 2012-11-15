package org.hudsonci.eclipse.core.ui.wizards;

import org.eclipse.emf.common.util.EList;
import org.maven.ide.components.pom.Notifier;
import org.maven.ide.components.pom.impl.CiManagementImpl;

public class CiManagmentAdapter extends CiManagementImpl {
	public CiManagmentAdapter() {
		super();
	}

	@Override
	public EList<Notifier> getNotifiers() {
		// TODO Advanced notifiers for CI system.
		return super.getNotifiers();
	}
}
