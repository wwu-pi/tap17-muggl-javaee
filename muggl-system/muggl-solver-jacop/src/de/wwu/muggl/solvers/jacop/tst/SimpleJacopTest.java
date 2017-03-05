package de.wwu.muggl.solvers.jacop.tst;

import org.jacop.constraints.Alldifferent;
import org.jacop.constraints.Constraint;
import org.jacop.constraints.XeqY;
import org.jacop.constraints.XgteqC;
import org.jacop.constraints.XneqY;
import org.jacop.core.Domain;
import org.jacop.core.IntDomain;
import org.jacop.core.IntVar;
import org.jacop.floats.core.FloatDomain;
import org.jacop.search.DepthFirstSearch;
import org.jacop.search.IndomainMin;
import org.jacop.search.Search;
import org.jacop.search.SelectChoicePoint;
import org.jacop.search.SimpleSelect;
import org.jacop.search.SmallestDomain;

import de.wwu.muggl.solvers.jacop.JacopMugglStore;

public class SimpleJacopTest {
	
	protected JacopMugglStore jacopStore;

	public static void main(String[] args) {
		new SimpleJacopTest().f();
	}
	
	protected void f() {
		init();
		jacopStore.setLevel(jacopStore.level + 1);
		
		IntVar leagueIdValueLength = new IntVar(jacopStore, "leagueId.value.length", -50, 50);
		Constraint c1 = new XgteqC(leagueIdValueLength, 0);
		addConstraint(c1);
		
		IntVar resultlistLength = new IntVar(jacopStore, "resultListLength", -50, 50);
		Constraint c2 = new XgteqC(resultlistLength, 0);
		addConstraint(c2);
		
		Constraint c3 = new XgteqC(resultlistLength, 1);
		addConstraint(c3);
		
		IntVar resultlistElementDataLength = new IntVar(jacopStore, "resultlistElementDataLength", -50, 50);
		Constraint c4 = new XgteqC(resultlistElementDataLength, 0);
		addConstraint(c4);
		
		Constraint c5 = new XeqY(resultlistLength, resultlistElementDataLength);
		addConstraint(c5);
		
		
		IntVar league = new IntVar(jacopStore, "league", -50, 50);
		IntVar lid1 = new IntVar(jacopStore, "lid1", -50, 50);
		IntVar lid2 = new IntVar(jacopStore, "lid2", -50, 50);
		
		addConstraint(new XeqY(lid1, league));
		addConstraint(new XeqY(lid2, league));
		addConstraint(new XneqY(lid1, lid2));
		
		IntVar[] vars = {lid1, lid2, league, leagueIdValueLength, resultlistLength, resultlistElementDataLength};
//		solveIt(vars);
		
		boolean b = jacopStore.consistency();
		System.out.println("b= "+b);
		
		
	}
	
	protected void solveIt(IntVar[] vars) {
		// solve it		
		SelectChoicePoint<IntVar> select = new SimpleSelect<IntVar>(vars,
				new SmallestDomain<IntVar>(), new IndomainMin<IntVar>());
		Search<IntVar> labelInt = new DepthFirstSearch<IntVar>();
		labelInt.setPrintInfo(true);
		boolean solutionFound = labelInt.labeling(jacopStore, select) && labelInt.assignSolution();
		System.out.println("*** solution found = "+ solutionFound);
		Domain[] solution = labelInt.getSolution();
		for (int i = 0; i < solution.length; i++) {
			
			
			if (solution[i] instanceof IntDomain) {
				int minValue = ((IntDomain) solution[i]).min();
				System.out.println(solution[i]  +"    = " + minValue);
			} else {
				double minValue = ((FloatDomain) solution[i]).min();
				System.out.println(solution[i]  +"    = " + minValue);
			}
		}
	}
	
	protected void addConstraint(Constraint c) {
		System.out.println("add constraint: " + c + " to level: " + jacopStore.level);
		jacopStore.impose(c);
		jacopStore.setLevel(jacopStore.level + 1);
	}
	
	protected void test() {
		init();
		IntVar leagueId1 = new IntVar(jacopStore, "lid1", 1, 127);
		IntVar leagueId2 = new IntVar(jacopStore, "lid2", 1, 127);
		
		Constraint c1 = new XeqY(leagueId1, leagueId2);
		jacopStore.impose(c1);
		
		IntVar[] leagues = {leagueId1, leagueId2};
		Constraint c2 = new Alldifferent(leagues);
		jacopStore.impose(c2);
		
		// solve it		
		IntVar[] vars = {leagueId1, leagueId2};
		SelectChoicePoint<IntVar> select = new SimpleSelect<IntVar>(vars,
				new SmallestDomain<IntVar>(), new IndomainMin<IntVar>());
		Search<IntVar> labelInt = new DepthFirstSearch<IntVar>();
		labelInt.setPrintInfo(true);
		boolean solutionFound = labelInt.labeling(jacopStore, select) && labelInt.assignSolution();
		System.out.println("*** solution found = "+ solutionFound);
		Domain[] solution = labelInt.getSolution();
		for (int i = 0; i < solution.length; i++) {
			
			
			if (solution[i] instanceof IntDomain) {
				int minValue = ((IntDomain) solution[i]).min();
				System.out.println(solution[i]  +"    = " + minValue);
			} else {
				double minValue = ((FloatDomain) solution[i]).min();
				System.out.println(solution[i]  +"    = " + minValue);
			}
		}
	}

	private void init() {
		this.jacopStore = new JacopMugglStore();
	}
}
