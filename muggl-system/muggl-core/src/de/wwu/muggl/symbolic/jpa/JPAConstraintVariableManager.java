package de.wwu.muggl.symbolic.jpa;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

// soll jetzt ueber den virutal database laufen
@Deprecated
public class JPAConstraintVariableManager {

	private static JPAConstraintVariableManager instance;
	
	private Map<String, JPAEntityConstraint> entityConstraints;
	
	private JPAConstraintVariableManager() {
		entityConstraints = new HashMap<String, JPAEntityConstraint>();
	}
	
	public void addEntityConstraint(String entityName, JPAEntityConstraint constraint) {
		this.entityConstraints.put(entityName, constraint);
	}
	
	public JPAEntityConstraint getEntityConstraint(String entityName) {
		return this.entityConstraints.get(entityName);
	}
	
	public Set<String> getEntitiesWithConstraints() {
		return this.entityConstraints.keySet();
	}
	
	// TODO: die constraints der solution hinzufügen
	@Deprecated
	public static synchronized JPAConstraintVariableManager getInstance() {
		if(instance == null) {
			instance = new JPAConstraintVariableManager();
		}
		return instance;
	}
}
