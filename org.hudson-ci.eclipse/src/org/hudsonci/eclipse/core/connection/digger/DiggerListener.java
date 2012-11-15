package org.hudsonci.eclipse.core.connection.digger;

import java.util.Collection;

public interface DiggerListener {

	public void serversFound(Collection<HudsonServer> servers);
}
