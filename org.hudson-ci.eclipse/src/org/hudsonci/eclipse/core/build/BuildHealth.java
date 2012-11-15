package org.hudsonci.eclipse.core.build;

import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.hudsonci.eclipse.core.HudsonPlugin;

public class BuildHealth implements Comparable<BuildHealth> {

	private final int health;

	public BuildHealth(int health) {
		this.health = health - (health % 20);
	}
	
	public static BuildHealth getLowest(List<String> values) {
		BuildHealth last = null;
		for (String val : values) {
			BuildHealth h = new BuildHealth(Integer.parseInt(val));
			if (last == null) {
				last = h;
			} else if (last.compareTo(h) > 0) {
				last = h;
			}
		}
		return last;
	}
	
	public Image getImage() {
		String imagePath = "icons/health_" + (health == 100 ? 80 : health) + ".png";

		return HudsonPlugin.getImage(imagePath);
	}
	
	public int hashCode() {
		return 13 * health;
	}
	
	public boolean equals(Object obj) {
		BuildHealth other = (BuildHealth) obj;
		return health == other.health;
	}

	public int compareTo(BuildHealth o) {
		return health - o.health;
	}
	
	public String toString() {
		return "Health: " + health;
	}
	
	public int getHealth() {
		return health;
	}
}
