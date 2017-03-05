package de.wwu.muggl.solvers.jacop.tst;

import org.jacop.constraints.Constraint;
import org.jacop.constraints.Sum;
import org.jacop.constraints.SumInt;
import org.jacop.constraints.XeqC;
import org.jacop.constraints.XneqC;
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

public class JacopTestMain {

	protected JacopMugglStore jacopStore;
	private java.lang.String x0;
	
	public void foobar() {
		this.x0= new java.lang.String();
		try {
			// set field 'values' in object x0
			Object x0FieldValues_value = "";
			java.lang.reflect.Field x0FieldValues = java.lang.String.class.getField("values");
			x0FieldValues.setAccessible(true);
			x0FieldValues.set(this.x0, x0FieldValues_value);
			java.lang.Character[] valuesadlskjfas = new java.lang.Character[2];
		} catch(NoSuchFieldException nsfe) {
			// do something
		} catch (IllegalArgumentException e) {
			// do something
		} catch (IllegalAccessException e) {
			// do something
		}
		
	}
	
	public static void main(String[] args) {		
		
		JacopTestMain jacop = new JacopTestMain();
		jacop.init();
		
		IntVar xLength = jacop.getNewIntVar("xlength");
		IntVar xMin1 = jacop.getNewIntVar("min1");
		IntVar xMin2 = jacop.getNewIntVar("min2");
		IntVar xMin3 = jacop.getNewIntVar("min3");
		
		XeqC xLengthEq3 = new XeqC(xLength, 3);
		XeqC xMin1C = new XeqC(xMin1, -1);
		XeqC xMin2C = new XeqC(xMin2, -1);
		XeqC xMin3C = new XeqC(xMin3, -1);
		
		jacop.jacopStore.impose(xLengthEq3);
		jacop.jacopStore.impose(xMin1C);
		jacop.jacopStore.impose(xMin2C);
		jacop.jacopStore.impose(xMin3C);
		
		System.out.println("\n\n\n*** xlength, xmin1, xmin2, xmin3 added... *** \n");
		System.err.println("Store before consistency:");
		System.err.println(jacop.jacopStore);
		boolean consistent = jacop.jacopStore.consistency();
		System.out.println("+++ consistent 1: " + consistent);
		System.err.println("Store after consistency:");
		System.err.println(jacop.jacopStore);
		
		System.out.println("\n\n\n*** sum xlength-1-1-1 added... *** \n");
		IntVar[] sumList = {xLength, xMin1, xMin2, xMin3};
		IntVar sumValue = jacop.getNewIntVar("sumvalue");
		SumInt sumC = new SumInt(jacop.jacopStore, sumList, "==", sumValue);
		
		jacop.jacopStore.impose(sumC);
		
		System.out.println("------------------------");
		
		System.err.println("Store before sum added consistency:");
		System.err.println(jacop.jacopStore);
//		consistent = jacop.jacopStore.consistency();
		System.out.println("+++ consistent 2: " + consistent);
		System.err.println("Store after sum added and consistency:");
		System.err.println(jacop.jacopStore);
		
		System.out.println("------------------------");
		
		System.out.println("\n\n\n*** sum value equals (not) zero added... *** \n");
		XneqC sumZero = new XneqC(sumValue, 0);
		jacop.jacopStore.impose(sumZero);
		
		System.err.println("Store before sum (not) equals zero consistency:");
		System.err.println(jacop.jacopStore);
		consistent = jacop.jacopStore.consistency();
		System.out.println("+++ consistent 3: " + consistent);
		System.err.println("Store after sum added and consistency:");
		System.err.println(jacop.jacopStore);
		
//		
//		// x.value.length != 3
//		IntVar xValueLength = jacop.getNewIntVar(""); // x.value.length
//		XneqC xValueLengthNotThree = new XneqC(xValueLength, 3); // x.value.length != 3
//		jacop.addConstraint(xValueLengthNotThree);
		
		
		
		
		// add variables
		IntVar[] vars = {xLength, xMin1, xMin2, xMin3};
		
		
		
		
		
		
		
		
		// solve it		
		SelectChoicePoint<IntVar> select = new SimpleSelect<IntVar>(vars,
				new SmallestDomain<IntVar>(), new IndomainMin<IntVar>());
		Search<IntVar> labelInt = new DepthFirstSearch<IntVar>();
		labelInt.setPrintInfo(true);
		boolean solutionFound = labelInt.labeling(jacop.jacopStore, select) && labelInt.assignSolution();
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
		jacopStore = new JacopMugglStore();
	}
	
	public void addConstraint(Constraint constraint) {
		jacopStore.setLevel(jacopStore.level + 1);
		jacopStore.impose(constraint);		
	}
	
	private IntVar getNewIntVar(String name) {
		return new IntVar(jacopStore, name, -50, 50);
	}
}
