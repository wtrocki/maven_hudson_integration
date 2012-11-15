package org.hudsonci.eclipse.core.connection.digger;

import java.util.Collection;

final class HudsonDiggerThread extends Thread implements DiggerListener {
	private final DiggerDialog hudsonDialog;
	HudsonDigger digger;
	private boolean digging = false;

	HudsonDiggerThread(DiggerDialog hudsonDialog) {
		this.hudsonDialog = hudsonDialog;
		this.digger = new HudsonDigger();
		digger.addListener(this);
	}

	@Override
	public void run() {
		digging = true;
		try {
			digger.digForHudson();
		} catch (final Exception e) {
			hudsonDialog.getShell().getDisplay().asyncExec(new Runnable() {
				public void run() {
					hudsonDialog.digException(e);
				}

			});
		} finally {
			if (digging) {
				this.hudsonDialog.getShell().getDisplay().asyncExec(new Runnable() {
					public void run() {
						hudsonDialog.digFinished(digger.getServers());
					}
				});
			}
			digging = false;
		}
	}

	public boolean isDigging() {
		return digging;
	}

	public void stopDigging() {
		digger.stopDigging();
		digging = false;
		interrupt();
	}

	public void serversFound(Collection<HudsonServer> servers) {
		this.hudsonDialog.getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				hudsonDialog.digFinished(digger.getServers());
			}
		});
	}
}
