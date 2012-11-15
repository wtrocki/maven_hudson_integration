package hudson.tests;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.hudsonci.eclipse.core.build.BuildHealth;
import org.junit.Test;

public class BuildHealthTest {

	@Test
	public void testGetLowest() {
		List<String> values = Arrays.asList("80", "60", "100");
		
		BuildHealth health = BuildHealth.getLowest(values);
		assertNotNull(health);
		assertEquals(60, health.getHealth());
		
		values = Arrays.asList("100");
		health = BuildHealth.getLowest(values);
		assertNotNull(health);
		assertEquals(100, health.getHealth());
	}

}
