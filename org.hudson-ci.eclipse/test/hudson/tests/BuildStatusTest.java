package hudson.tests;

import static org.junit.Assert.*;

import org.hudsonci.eclipse.core.build.BuildStatus;
import org.junit.Test;

public class BuildStatusTest {

	@Test
	public void testGetKnownStatus() {
		BuildStatus success = BuildStatus.getStatus("blue");
		assertNotNull(success);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void unknownStatusShouldThrowException() throws Exception {
		BuildStatus.getStatus("illegal");
	}
	
	@Test
	public void normalAndRunningIsSame() throws Exception {
		assertEquals(BuildStatus.getStatus("blue"), BuildStatus.getStatus("blue_anime"));
	}

}
