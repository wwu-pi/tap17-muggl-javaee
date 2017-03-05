package de.wwu.javaee.test.runner;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

public class MugglExceptionMatcher extends TypeSafeMatcher<Throwable> {

	protected Class<?> expectedException;
	
	public MugglExceptionMatcher(Class<?> expectedException) {
		this.expectedException = expectedException;
	}
	
	@Override
	public void describeTo(Description description) {
		
	}

	@Override
	protected boolean matchesSafely(Throwable item) {
		Throwable t = item.getCause();
		while(t != null) {
			if(t.getClass().getName().equals(expectedException.getName())) {
				return true;
			}
			t = t.getCause();
		}
		return false;
	}
	
	

}
