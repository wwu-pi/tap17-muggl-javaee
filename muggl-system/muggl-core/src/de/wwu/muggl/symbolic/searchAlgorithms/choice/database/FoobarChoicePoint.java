package de.wwu.muggl.symbolic.searchAlgorithms.choice.database;

import de.wwu.muggl.configuration.MugglException;
import de.wwu.muggl.solvers.expressions.ConstraintExpression;
import de.wwu.muggl.symbolic.searchAlgorithms.choice.ChoicePoint;
import de.wwu.muggl.symbolic.searchAlgorithms.choice.exc.ExceptionThrowingChoicePoint;
import de.wwu.muggl.vm.Frame;
import de.wwu.muggl.vm.exceptions.VmRuntimeException;

public class FoobarChoicePoint extends ExceptionThrowingChoicePoint {

	public FoobarChoicePoint(Frame frame, int pc, int pcNext, ChoicePoint parent) {
		super(frame, pc, pcNext, parent);
	}

	@Override
	public boolean hasAnotherChoice() {
		return counter <= 2;
	}

	@Override
	public void changeToNextChoice() throws MugglException {
		counter++;
	}

	@Override
	public boolean changesTheConstraintSystem() {
		return false;
	}

	@Override
	public ConstraintExpression getConstraintExpression() {
		return null;
	}

	@Override
	public void setConstraintExpression(ConstraintExpression constraintExpression) {
		System.out.println("x");
	}

	@Override
	public boolean enforcesStateChanges() {
		return true;
	}
	
	private int counter = 0;

	@Override
	public void applyStateChanges() throws VmRuntimeException {
		if(counter == 0) {
			throw new VmRuntimeException(frame.getVm().generateExc("javax.persistence.EntityExistsException", "not good"));
		} else if(counter == 1) {
			throw new VmRuntimeException(frame.getVm().generateExc("java.lang.NullPointerException", "not good"));
		} else {
			// do nothing
		}
	}

	@Override
	public String getChoicePointType() {
		return null;
	}
}
