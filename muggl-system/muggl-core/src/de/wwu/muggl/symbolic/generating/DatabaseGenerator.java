package de.wwu.muggl.symbolic.generating;

public class DatabaseGenerator implements Generator {

	@Override
	public String getName() {
		return "Database Generator";
	}

	@Override
	public String getDescription() {
		return "Generates the required database state.";
	}

	@Override
	public boolean allowsChoicePoint() {
		return true;
	}

	@Override
	public boolean hasAnotherObject() {
		// TODO is it true?
		return true;
	}

	@Override
	public Object provideObject() {
		// TODO return a reference object that is poped onto the stack, e.g., query resutl object etc...
		// return a object that might be poped onto the stack
		return null;
	}

	@Override
	public boolean objectNeedsConversion() {
		// MugglToJavaConversion conversion = new MugglToJavaConversion(frame.getVm());
		return true;
	}

	@Override
	public void reset() {
		// TODO: reset to the starting point
		// that means reset the database state.
		System.out.println("Database-Generator: reset to the starting point");
	}

}
