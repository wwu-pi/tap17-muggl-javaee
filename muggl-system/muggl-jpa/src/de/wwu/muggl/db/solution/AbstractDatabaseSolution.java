package de.wwu.muggl.db.solution;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.wwu.muggl.db.VirtualDatabase;
import de.wwu.muggl.db.obj.DBObject;
import de.wwu.muggl.solvers.Solution;

public abstract class AbstractDatabaseSolution {

	protected SolutionValueManager solutionValueManager;
	protected Map<String, List<DBObject>> solution;

	public AbstractDatabaseSolution(VirtualDatabase database, Solution solution) {
		this.solution = new HashMap<String, List<DBObject>>();
		this.solutionValueManager = new SolutionValueManager();
		fillSolution(database, solution);
	}

	protected abstract void fillSolution(VirtualDatabase database, Solution solution);
	
	public Map<String, List<DBObject>> getSolution() {
		return this.solution;
	}
	
}
