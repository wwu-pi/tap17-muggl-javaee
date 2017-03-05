package de.wwu.muggl.db.solution;

import java.util.HashSet;
import java.util.Set;

import de.wwu.muggl.db.VirtualDatabase;
import de.wwu.muggl.db.entry.DatabaseObject;
import de.wwu.muggl.db.entry.EntityEntry;
import de.wwu.muggl.db.obj.DBObject;
import de.wwu.muggl.db.obj.DBObjectGenerator;
import de.wwu.muggl.solvers.Solution;

public class PostExecutionDatabaseSolution {

	protected Set<DBObject> solution;

	public PostExecutionDatabaseSolution(VirtualDatabase database, Solution solution) {
		this.solution = new HashSet<DBObject>();
//		fillSolution(database, solution);
	}

	protected void fillSolution(VirtualDatabase database, Solution solution) {
		// 
		Set<String> objectIdSet = new HashSet<>();
		for(EntityEntry data : database.getRequiredData().values()) {
			objectIdSet.add(data.getObjectId());
		}
		for(DatabaseObject data : database.getData()) {
			objectIdSet.add(data.getObjectId());
		}
		
		DBObjectGenerator gen = new DBObjectGenerator(objectIdSet);
		for(DatabaseObject objectRef : database.getData()) {
			DBObject dbObject = gen.generateObjectByEntityEntry(objectRef, solution);
			this.solution.add(dbObject);
		}
	}
	
	public Set<DBObject> getSolution() {
		return this.solution;
	}
}
