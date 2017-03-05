package de.wwu.muggl.vm.impl.jpa.dbgen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.wwu.muggl.db.VirtualObjectDatabase;
import de.wwu.muggl.db.entry.DatabaseObject;
import de.wwu.muggl.jpa.criteria.ComparisonOperator;
import de.wwu.muggl.jpa.criteria.MugglJPAEntityPath;
import de.wwu.muggl.jpa.criteria.MugglJPAQueryComparisonRestriction;
import de.wwu.muggl.jpa.criteria.MugglJPAQueryRestriction;
import de.wwu.muggl.jpa.criteria.MugglJoin;
import de.wwu.muggl.jpa.criteria.MugglPath;
import de.wwu.muggl.jpa.criteria.MugglRoot;
import de.wwu.muggl.solvers.Solution;
import de.wwu.muggl.solvers.expressions.IntConstant;
import de.wwu.muggl.solvers.expressions.NumericConstant;
import de.wwu.muggl.solvers.expressions.NumericVariable;
import de.wwu.muggl.solvers.expressions.Variable;
import de.wwu.muggl.solvers.expressions.objgen.ObjectReferenceEqual;
import de.wwu.muggl.solvers.expressions.objgen.meta.ObjectReferenceComparisonConstraint;
import de.wwu.muggl.symbolic.jpa.gen.EntityReferenceGenerator;
import de.wwu.muggl.vm.classfile.structures.Field;
import de.wwu.muggl.vm.impl.jpa.JPAVirtualMachine;
import de.wwu.muggl.vm.var.ReferenceQueryResultArrayListVariable;
import de.wwu.muggl.vm.var.ReferenceVariable;
import de.wwu.muggl.vm.var.SymbolicArrayref;

public class DBQueryResultDataGenerator {
	
	protected JPAVirtualMachine vm;
	protected EntityReferenceGenerator entityRefGenerator;

	public DBQueryResultDataGenerator(JPAVirtualMachine vm) {
		this.entityRefGenerator = new EntityReferenceGenerator(vm);
		this.vm = vm;
	}
	
	public void generateRequiredQueryData(ReferenceQueryResultArrayListVariable symQryResList, Solution solution) {
		Map<String, MinMax> amountObjectsToGenerate = new HashMap<>();
		
		NumericVariable symLength = symQryResList.getSymbolicLength();
		int length = ((NumericConstant)solution.getValue(symLength)).getIntValue();
		
		if(length <= 0) {
			return;
		}
		
		
		
		// TODO: so die element data aus dem query result lsit herausbekommen: 
		
		SymbolicArrayref symArryData = (SymbolicArrayref)symQryResList.getField(symQryResList.getInitializedClass().getClassFile().getFieldByName("elementData"));
		List<ReferenceVariable> qryListData = new ArrayList<>();
		if(symArryData != null) {
			int symArrLength = ((IntConstant)solution.getValue(symArryData.getSymbolicLength())).getIntValue();
			for(int i=0; i<symArrLength; i++) {
				Object eleData = symArryData.getElement(i);
				if(eleData != null) {
					if(eleData instanceof ReferenceVariable) {
						qryListData.add((ReferenceVariable) eleData);
					}
				}
			}
		}
		
		if(symQryResList.getCriteriaQuery().getSelectSet().toArray()[0] instanceof MugglRoot) {
			MugglRoot selectRoot = (MugglRoot)symQryResList.getCriteriaQuery().getSelectSet().toArray()[0];
			String entityName = selectRoot.getEntityClassName();
			System.out.println("von entityName=" +entityName + " müssen " + length + " da sein");
			amountObjectsToGenerate.put(entityName, new MinMax(length, length));
		}
		
		List<MugglJoin<?,?>> joinList = new ArrayList<>();
		for(MugglRoot<?> root : symQryResList.getCriteriaQuery().getRootSet()) {
			String rootEntityName = root.getEntityClassName();
			for(MugglJoin join : root.getJoinSet()) {
				handleJoin(join, amountObjectsToGenerate);
				joinList.add(join);
			}
		}
		
		for(String x : amountObjectsToGenerate.keySet()) {
			System.out.println("Entity="+x + "  -> "+ amountObjectsToGenerate.get(x));
		}
		
		generatedEntityObjectsStart(amountObjectsToGenerate, symQryResList.getQueryRestrictions(), qryListData, joinList);
	}
	
	
	
	private void generatedEntityObjectsStart(Map<String, MinMax> amountObjectsToGenerate, Set<MugglJPAQueryRestriction> restrictionSet, List<ReferenceVariable> queryData, List<MugglJoin<?,?>> joinList) {
		Map<String, Integer> entityObjectsToGenerate = new HashMap<>();
		for(String entityName : amountObjectsToGenerate.keySet()) {
			MinMax cardinality = amountObjectsToGenerate.get(entityName);
			entityObjectsToGenerate.put(entityName, cardinality.min);
		}
		
		Map<String, List<ReferenceVariable>> generatedObjects = generateObjects(entityObjectsToGenerate, joinList);
		if(queryData.size() > 0) {
			List<ReferenceVariable> generatedObjectList = generatedObjects.get(queryData.get(0).getInitializedClassName());
			for(int i=0; i<queryData.size(); i++) {
				generatedObjectList.set(i, queryData.get(i));
			}
		}
		applyQueryRestrictions(generatedObjects, restrictionSet);
		
		if(hasSolution(generatedObjects, restrictionSet)) {
			VirtualObjectDatabase vodb = this.vm.getVirtualObjectDatabase();
			for(String entityName : generatedObjects.keySet()) {
				for(ReferenceVariable data : generatedObjects.get(entityName)) {
					vodb.addPreExecutionRequiredData(this.vm.getSolverManager(), entityName, data);
				}
			}
		} else {
			tryNextSolution(generatedObjects, restrictionSet);
		}
	}

	private void applyQueryRestrictions(Map<String, List<ReferenceVariable>> generatedObjects, Set<MugglJPAQueryRestriction> restrictionSet) {
		for(String entityName : generatedObjects.keySet()) {
			for(ReferenceVariable entityObject : generatedObjects.get(entityName)) {
				for(MugglJPAQueryRestriction restriction : restrictionSet) {
					applyRestrictionToEntityObject(restriction, entityObject);
				}
			}
		}
	}

	private void applyRestrictionToEntityObject(MugglJPAQueryRestriction restriction, ReferenceVariable entityObject) {
		if(restriction instanceof MugglJPAQueryComparisonRestriction) {
			applyComparisonRestriction((MugglJPAQueryComparisonRestriction)restriction, entityObject);
		}
	}
	
	private void applyComparisonRestriction(MugglJPAQueryComparisonRestriction comparison, ReferenceVariable entityObject) {
		if(comparison.getComparisonOperator().equals(ComparisonOperator.EQ)) {
			// apply an equal restriction
			
			Object left = comparison.getLeft();
			Object right = comparison.getRight();
			
			if(left instanceof MugglJPAEntityPath && right instanceof Variable) {
				MugglJPAEntityPath path = (MugglJPAEntityPath)left;
				String entityName = path.getEntityClassName();
				String attributeName = path.getAttributeName();
				
				if(entityObject.getObjectType().equals(entityName)) {
					Object value = entityObject.valueMap().get(attributeName);
					Field attributeField = entityObject.getInitializedClass().getClassFile().getFieldByName(attributeName);
					entityObject.putField(attributeField, comparison.getRight());
//					if(value instanceof ReferenceVariable) {
//						ObjectReferenceEqual constraint = new ObjectReferenceEqual((ReferenceVariable)value, comparison.getRight());
//						vm.getSolverManager().addConstraint(constraint);
//					}
				}
				
			}
		}
	}

	private Map<String, List<ReferenceVariable>> generateObjects(Map<String, Integer> entityObjectsToGenerate, List<MugglJoin<?, ?>> joinList) {
		Map<String, List<ReferenceVariable>> resultMap = new HashMap<>();
		for(String entityName : entityObjectsToGenerate.keySet()) {
			List<ReferenceVariable> entityList = new ArrayList<>();
			for(int i=0; i<entityObjectsToGenerate.get(entityName); i++) {
				entityList.add(entityRefGenerator.generateNewEntityReference(entityName));
			}
			resultMap.put(entityName, entityList);
		}
		return resultMap;
	}
	
	

	private void tryNextSolution(Map<String, List<ReferenceVariable>> generatedObjects,	Set<MugglJPAQueryRestriction> restrictionSet) {
		
	}

	private boolean hasSolution(Map<String, List<ReferenceVariable>> generatedObjects, Set<MugglJPAQueryRestriction> restrictionSet) {
		return true;
	}



	protected void handleJoin(MugglJoin join, Map<String, MinMax> amountObjectsToGenerate) {
		String sourceEntityName = join.getSourceEntityName();
		String targetEntityName = join.getTargetEntityName();
		
		switch(join.getMugglJoinType()) {
			case MANY_TO_MANY : {
				if(amountObjectsToGenerate.get(sourceEntityName) == null) {
					amountObjectsToGenerate.put(sourceEntityName, new MinMax(1, Integer.MAX_VALUE));
				}
				if(amountObjectsToGenerate.get(targetEntityName) == null) {
					amountObjectsToGenerate.put(targetEntityName, new MinMax(1, Integer.MAX_VALUE));
				}
				break;
			}
			
			case MANY_TO_ONE : {
				if(amountObjectsToGenerate.get(sourceEntityName) == null) {
					amountObjectsToGenerate.put(sourceEntityName, new MinMax(1, Integer.MAX_VALUE));
				}
				if(amountObjectsToGenerate.get(targetEntityName) == null) {
					int min = amountObjectsToGenerate.get(sourceEntityName).min();
					int max = amountObjectsToGenerate.get(sourceEntityName).max();
					amountObjectsToGenerate.put(targetEntityName, new MinMax(min, max));
				}
				break;
			}
			
		}
		
		for(Object j : join.getJoinSet()) {
			if(j instanceof MugglJoin) {
				handleJoin((MugglJoin)j, amountObjectsToGenerate);
			}
		}
	}
	
	public static class MinMax {
		protected int min;
		protected int max;
		public MinMax(int min, int max) {
			this.min = min;
			this.max = max;
		}
		public int min() {
			return min;
		}
		public int max() {
			return max;
		}
		@Override
		public String toString() {
			return "min="+min+", max="+max;
		}
	}

}
