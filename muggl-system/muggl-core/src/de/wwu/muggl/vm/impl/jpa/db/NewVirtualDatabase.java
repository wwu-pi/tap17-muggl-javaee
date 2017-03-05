//package de.wwu.muggl.vm.impl.jpa.db;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//
//import de.wwu.muggl.instructions.bytecode.Arraylength;
//import de.wwu.muggl.solvers.expressions.ConstraintExpression;
//import de.wwu.muggl.solvers.expressions.Expression;
//import de.wwu.muggl.symbolic.generating.jpa.JPAEntityAnalyzer;
//import de.wwu.muggl.symbolic.jpa.var.entity.EntityObjectReference;
//import de.wwu.muggl.vm.VirtualMachine;
//import de.wwu.muggl.vm.classfile.ClassFile;
//import de.wwu.muggl.vm.classfile.structures.Field;
//import de.wwu.muggl.vm.impl.jpa.db.constraints.JPAStaticEntityConstraint;
//import de.wwu.muggl.vm.impl.jpa.db.constraints.JPAStaticEntityConstraintGenerator;
//import de.wwu.muggl.vm.impl.symbolic.SymbolicVirtualMachine;
//import de.wwu.muggl.vm.initialization.Objectref;
//
//public class NewVirtualDatabase {
//	
//	// neue map mit nur entities...
//	private Map<String, List<EntityObjectEntry>> entityMap;
//	
//	// static entity constraints
////	private Map<String, JPAStaticEntityConstraint> constraintMap;
//	
//	public void addEntityObject(String entityName, EntityObjectEntry entityObject) {
//		List<EntityObjectEntry> entities = entityMap.get(entityName);
//		if(entities == null) {
//			entities = new ArrayList<EntityObjectEntry>();
////			JPAStaticEntityConstraint constraint = JPAStaticEntityConstraintGenerator.generateEntityConstraints(entityName);
////			this.constraintMap.put(entityName, constraint);
//		}
//		entities.add(entityObject);
//		entityMap.put(entityName, entities);
//	}
//	
//	public Map<String, List<EntityObjectEntry>> getEntityMap() {
//		return entityMap;
//	}
//	
////	public Map<String, JPAStaticEntityConstraint> getConstraintMap() {
////		return constraintMap;
////	}
//	
//
//	protected Map<String, DynamicExpressionMatrix> entityExpressionMatrixMap;
//	
//	public NewVirtualDatabase() {
//		this.entityExpressionMatrixMap = new HashMap<>();
//		this.entityMap = new HashMap<>();
////		this.constraintMap = new HashMap<>();
//	}
//	
//	public NewVirtualDatabase getClone() {
//		// TODO auch entityMap clone
//		System.out.println("*** TODO: auch die entity map clonen..");
//		
//		NewVirtualDatabase clone = new NewVirtualDatabase();
//		clone.entityMap = cloneEntityMap();
//		
//		for(String entity : this.entityExpressionMatrixMap.keySet()) {
//			DynamicExpressionMatrix cloneMatrix = entityExpressionMatrixMap.get(entity).getClone();
//			clone.entityExpressionMatrixMap.put(entity, cloneMatrix);
//		}
//		return clone;
//	}
//	
//	private Map<String, List<EntityObjectEntry>> cloneEntityMap() {
//		Map<String, List<EntityObjectEntry>> entityMapNew = new HashMap<>();
//		for(String entity : this.entityMap.keySet()) {
//			List<EntityObjectEntry> entityObjectList = new ArrayList<>();
//			for(EntityObjectEntry entityEntry : this.entityMap.get(entity)) {
////				ClassFile entityClassFile = vm.getClassLoader().getClassAsClassFile(entityEntry.getName());
////				Objectref objectRef = vm.getAnObjectref(entityClassFile);
////				
////				for(Field field : entityEntry.getFields().keySet()) {
////					objectRef.putField(field, entityEntry.getField(field));
////				}
////				
//				
//				EntityObjectEntry newObjectRef = entityEntry; // TODO: clone the entity
//				entityObjectList.add(newObjectRef);
//			}
//			entityMapNew.put(entity, entityObjectList);
//		}
//		return entityMapNew;
//	}
//	
//	public List<ConstraintExpression> generateDBConstraints() {
//		// TODO implement me
//		// TODO: dazu die StaticEntityConstraints benutzen, z.B. um Unique-Values zu finden und dazu constraints zu bauen.
//		return new ArrayList<>();
//	}
//	
//	public Map<String, DynamicExpressionMatrix> getEntityExpressionMatrixMap() {
//		return this.entityExpressionMatrixMap;
//	}
//	
//	public DynamicExpressionMatrix getExpressionMatrix(String entityName) {
//		return this.entityExpressionMatrixMap.get(entityName);
//	}
//	
//	public DynamicExpressionMatrix getExpressionMatrix(String entityName, boolean createIfNotExist) {
//		DynamicExpressionMatrix matrix = this.entityExpressionMatrixMap.get(entityName);
//		if(matrix == null && createIfNotExist) {
//			String[] columnNames = getColumnNames(entityName);
//			matrix = new DynamicExpressionMatrix(5, columnNames.length, columnNames);
//			this.entityExpressionMatrixMap.put(entityName, matrix);
//		}
//		return matrix;
//	} 
//	
//	private String[] getColumnNames(String entityName) {
//		try {
//			List<String> columnNames = new ArrayList<>();
//			Class<?> entityClazz = ClassLoader.getSystemClassLoader().loadClass(entityName);
//			for(java.lang.reflect.Field f : entityClazz.getDeclaredFields()) {
//				if(!java.lang.reflect.Modifier.isStatic(f.getModifiers())) {
//					columnNames.add(f.getName());
//				}
//			}
//			
//			return columnNames.toArray(new String[columnNames.size()]);
//		} catch (ClassNotFoundException e) {
//			e.printStackTrace();
//		}
//		return null;
//	}
//	
////	public EntityObjectEntry find(String entityName, Object id) {
////		JPAStaticEntityConstraint constraint = constraintMap.get(entityName);
////		String idField = constraint.getIdField();
////		for(EntityObjectEntry entry : this.entityMap.get(entityName)) {
////			// TODO: make this do better perform..
////			for(Field field : entry.getFields().keySet()) {
////				if(field.getName().equals(idField)) {
////					if(entry.getField(field).equals(id)) {
////						return entry;
////					}
////				}
////			}
////		}
////		return null;
////	}
//	
//	public void persist(Objectref objectRef) {
//		String className = objectRef.getName();
//		
//		addEntityObject(className, new EntityObjectEntry(objectRef));
//		
//		Set<Field> fields = objectRef.getFields().keySet();
//		
//		DynamicExpressionMatrix matrix = entityExpressionMatrixMap.get(className);
//		
//		if(matrix == null) {
//			String[] colNames = new String[fields.size()];
//			int i=0;
//			for(Field field : fields) {
//				colNames[i++] = field.getName();
//			}
//			matrix = new DynamicExpressionMatrix(5, fields.size(), colNames);
//		}
//		
//		matrix.newEntryStart();
//		int pos = matrix.getCurrentPosition();
//		
//		for(Field field : fields) {
//			Object objectValue = objectRef.getFields().get(field);
//			Expression expressionValue = getExpressionObjectValue(objectValue);
//			if(expressionValue == null) {
//				throw new RuntimeException("Could not handle value: " + objectValue+ ". It is not a expression! Please modify VirtualDatabase#getExpressionObjectValue method to return a expression for this kind of value...");
//			}
//			
//			int columnPos = matrix.getColumnPosition(field.getName());
//			
//			matrix.set(pos, columnPos, expressionValue);
//		}
//		
//		entityExpressionMatrixMap.put(className, matrix);
//	}
//	
//	protected Expression getExpressionObjectValue(Object value) {
//		if(value instanceof Expression) {
//			return (Expression)value;
//		}
//		return null;
//	}
//
//	public void mustExist(EntityObjectReference eor) throws Exception {
//		if(!doesExistInDB(eor)) {
//			DynamicExpressionMatrix matrix = entityExpressionMatrixMap.get(eor.getEntityClassName());
//			if(matrix == null) {
//				Class<?> entityClazz = ClassLoader.getSystemClassLoader().loadClass(eor.getEntityClassName());
//				java.lang.reflect.Field[] fields = entityClazz.getDeclaredFields();
//				List<String> columnNamesList = new ArrayList<>();
//				for(int i=0; i<fields.length; i++) {
//					if(!java.lang.reflect.Modifier.isStatic(fields[i].getModifiers())) {
//						columnNamesList.add(fields[i].getName());
//					}
//				}
//				String[] columnNames = columnNamesList.toArray(new String[columnNamesList.size()]);
//				matrix = new DynamicExpressionMatrix(5, columnNames.length, columnNames);
//				matrix.newEntryStart();
//				int x = matrix.getCurrentPosition();
//				for(int y=0; y<columnNames.length; y++) {
//					matrix.set(x, y, eor.getFieldValue(columnNames[y]));
//				}
//				matrix.setRequiredData(x); // mark the new record as (pre-execution) 'required'
//				matrix.setEntityReference(x, eor); // set a link to the actual entity reference object (from the VM stack)
//			}
//			entityExpressionMatrixMap.put(eor.getEntityClassName(), matrix);
//		}
//	}
//	
//	private boolean doesExistInDB(EntityObjectReference eor) {
//		return false; // TODO: richtig implementieren....
//	}
//
//	
//	public void remove(Objectref object) {
//		// TODO: remove it..
//		System.out.println("*** REMOVE OBJECT: " + object);
//	}
//
//
//}
