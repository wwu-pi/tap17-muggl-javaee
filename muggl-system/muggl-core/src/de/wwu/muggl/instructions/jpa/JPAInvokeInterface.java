package de.wwu.muggl.instructions.jpa;

import java.lang.reflect.InvocationTargetException;
import java.util.Enumeration;
import java.util.List;
import java.util.Stack;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.CollectionAttribute;

import de.wwu.muggl.instructions.InvalidInstructionInitialisationException;
import de.wwu.muggl.instructions.bytecode.Invokeinterface;
import de.wwu.muggl.jpa.MugglTypedQuery;
import de.wwu.muggl.jpa.criteria.meta.MugglJPA;
import de.wwu.muggl.jpa.criteria.metamodel.SymbolicEntityAttribute;
import de.wwu.muggl.jpa.ql.stmt.QLStatement;
import de.wwu.muggl.solvers.SolverManager;
import de.wwu.muggl.solvers.expressions.DoubleConstant;
import de.wwu.muggl.solvers.expressions.FloatConstant;
import de.wwu.muggl.solvers.expressions.IntConstant;
import de.wwu.muggl.solvers.expressions.LongConstant;
import de.wwu.muggl.symbolic.generating.jpa.JPAEntityAnalyzer;
import de.wwu.muggl.symbolic.jpa.MugglEntityManager;
import de.wwu.muggl.symbolic.jpa.MugglUserTransaction;
import de.wwu.muggl.symbolic.jpa.criteria.CriteriaBuilderWrapper;
import de.wwu.muggl.symbolic.jpa.criteria.CriteriaJoinWrapper;
import de.wwu.muggl.symbolic.jpa.criteria.CriteriaPathWrapper;
import de.wwu.muggl.symbolic.jpa.criteria.CriteriaPredicateWrapper;
import de.wwu.muggl.symbolic.jpa.criteria.CriteriaQueryWrapper;
import de.wwu.muggl.symbolic.jpa.criteria.CriteriaRootWrapper;
import de.wwu.muggl.symbolic.jpa.gen.EntityReferenceGenerator;
import de.wwu.muggl.symbolic.objgen.SymbolicObjectGenerationException;
import de.wwu.muggl.symbolic.objgen.SymbolicObjectGenerator;
import de.wwu.muggl.vm.Frame;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.ClassFileException;
import de.wwu.muggl.vm.classfile.structures.Constant;
import de.wwu.muggl.vm.classfile.structures.Field;
import de.wwu.muggl.vm.classfile.structures.Method;
import de.wwu.muggl.vm.classfile.structures.attributes.AttributeCode;
import de.wwu.muggl.vm.exceptions.NoExceptionHandlerFoundException;
import de.wwu.muggl.vm.exceptions.VmRuntimeException;
import de.wwu.muggl.vm.execution.ExecutionException;
import de.wwu.muggl.vm.execution.ResolutionAlgorithms;
import de.wwu.muggl.vm.impl.jpa.JPAVirtualMachine;
import de.wwu.muggl.vm.impl.symbolic.SymbolicExecutionException;
import de.wwu.muggl.vm.impl.symbolic.SymbolicVirtualMachine;
import de.wwu.muggl.vm.impl.symbolic.exceptions.SymbolicExceptionHandler;
import de.wwu.muggl.vm.initialization.Arrayref;
import de.wwu.muggl.vm.initialization.Objectref;
import de.wwu.muggl.vm.var.EntityObjectref;
import de.wwu.muggl.vm.var.ReferenceArrayListVariable;
import de.wwu.muggl.vm.var.ReferenceCollectionVariable;
import de.wwu.muggl.vm.var.ReferenceQueryResultArrayListVariable;
import de.wwu.muggl.vm.var.ReferenceVariable;
import de.wwu.muggl.vm.var.sym.SymbolicQueryResultList;

public class JPAInvokeInterface extends Invokeinterface {

	private JPAEntityAnalyzer analyzer;
	private SolverManager solverManager;
	private EntityReferenceGenerator entityRefGenerator;
	
	public JPAInvokeInterface(AttributeCode code) throws InvalidInstructionInitialisationException {
		super(code);
		analyzer = new JPAEntityAnalyzer();
		entityRefGenerator = new EntityReferenceGenerator(null);
	}
	
	public void setSolverManager(SolverManager solverManager) {
		this.solverManager = solverManager;
	}

	
	@Override
	public void executeSymbolically(Frame frame) throws NoExceptionHandlerFoundException, SymbolicExecutionException {
		try {
			Stack<Object> stack = frame.getOperandStack();
			
			int index = this.otherBytes[0] << ONE_BYTE | this.otherBytes[1];
			Constant constant = frame.getConstantPool()[index];
			String[] nameAndType = getNameAndType(constant);
			
			List<String> para = DescSplitter.splitMethodDesc(nameAndType[1]);
			int k = stack.size() - para.size() - 1;

			ClassFile methodClassFile = getMethodClassFile(constant, frame.getVm().getClassLoader());
			if(methodClassFile.getName().startsWith("javax.persistence.criteria")) {
				System.out.println("create criteria query wrappers");
				
				if(nameAndType[0].equals("createQuery") && nameAndType[1].equals("(Ljava/lang/Class;)Ljavax/persistence/criteria/CriteriaQuery;")) {	
					Objectref clazzRef = (Objectref)stack.pop();
					CriteriaBuilderWrapper builder = (CriteriaBuilderWrapper)stack.pop();
					CriteriaQuery criteriaQuery = builder.createQuery(clazzRef).getOriginal();
					stack.push(new CriteriaQueryWrapper(criteriaQuery));
					return;
				}
				
				else if(nameAndType[0].equals("from") && nameAndType[1].equals("(Ljava/lang/Class;)Ljavax/persistence/criteria/Root;")) {
					Objectref clazzRef = (Objectref)stack.pop();
					CriteriaQueryWrapper wrapper = (CriteriaQueryWrapper)stack.pop();
					Root root = wrapper.from(clazzRef).getOriginal();
					stack.push(new CriteriaRootWrapper(root));
					return;
				}
				
				else if(nameAndType[0].equals("get") && nameAndType[1].equals("(Ljavax/persistence/metamodel/SingularAttribute;)Ljavax/persistence/criteria/Path;")) {
					SymbolicEntityAttribute singularAttributeRef = (SymbolicEntityAttribute)stack.pop();
					Object o = stack.pop();
					if(o instanceof CriteriaJoinWrapper) {
						CriteriaJoinWrapper join = (CriteriaJoinWrapper)o;
						Path path = join.getOriginal().get(singularAttributeRef.getFieldName());
						stack.push(new CriteriaPathWrapper(path));
						return;
					} else if(o instanceof CriteriaRootWrapper) {
						CriteriaRootWrapper root = (CriteriaRootWrapper)o;
						Path path = root.get(singularAttributeRef).getOriginal();
						stack.push(new CriteriaPathWrapper(path));
						return;
					} else if(o instanceof CriteriaPathWrapper) {
						CriteriaPathWrapper path = (CriteriaPathWrapper)o;
						Path p = path.getOriginal().get(singularAttributeRef.getFieldName());
						stack.push(new CriteriaPathWrapper(p));
						return;
					} else {
						System.out.println("-");
					}
				}
				
				else if(nameAndType[0].equals("equal") && nameAndType[1].equals("(Ljavax/persistence/criteria/Expression;Ljava/lang/Object;)Ljavax/persistence/criteria/Predicate;")) {
					Object objectValue = stack.pop();
					CriteriaPathWrapper pathWrapper = (CriteriaPathWrapper)stack.pop();
					CriteriaBuilderWrapper builder = (CriteriaBuilderWrapper)stack.pop();
//					Predicate predicate = builder.getOriginal().equal(pathWrapper.getOriginal(), getDummyObject(pathWrapper.getOriginal().getJavaType(), objectValue));
					Object dummyVal = 1L;
					if(objectValue instanceof Objectref) {
						Objectref obRef = (Objectref)objectValue;
						if(obRef.getObjectType().equals("java.lang.String")) {
							dummyVal = "";
						}
					}
					Predicate predicate = builder.getOriginal().equal(pathWrapper.getOriginal(), dummyVal);
					CriteriaPredicateWrapper predicateWrapper = new CriteriaPredicateWrapper(predicate);
//					predicateWrapper.addSymbolicParameter(objectValue);
					stack.push(predicateWrapper);
					return;
				}
				
				else if(nameAndType[0].equals("isTrue") && nameAndType[1].equals("(Ljavax/persistence/criteria/Expression;)Ljavax/persistence/criteria/Predicate;")) {
					CriteriaPathWrapper pathWrapper = (CriteriaPathWrapper)stack.pop();
					CriteriaBuilderWrapper builder = (CriteriaBuilderWrapper)stack.pop();
					Predicate predicate = builder.getOriginal().isTrue(pathWrapper.getOriginal());
					CriteriaPredicateWrapper predicateWrapper = new CriteriaPredicateWrapper(predicate);
					stack.push(predicateWrapper);
					return;
				}
				else if(nameAndType[0].equals("isFalse") && nameAndType[1].equals("(Ljavax/persistence/criteria/Expression;)Ljavax/persistence/criteria/Predicate;")) {
					CriteriaPathWrapper pathWrapper = (CriteriaPathWrapper)stack.pop();
					CriteriaBuilderWrapper builder = (CriteriaBuilderWrapper)stack.pop();
					Predicate predicate = builder.getOriginal().isFalse(pathWrapper.getOriginal());
					CriteriaPredicateWrapper predicateWrapper = new CriteriaPredicateWrapper(predicate);
					stack.push(predicateWrapper);
					return;
				}
				
				else if(nameAndType[0].equals("greaterThan")) {
					CriteriaPathWrapper path1 = (CriteriaPathWrapper)stack.pop();
					CriteriaPathWrapper path2 = (CriteriaPathWrapper)stack.pop();
					CriteriaBuilderWrapper builder = (CriteriaBuilderWrapper)stack.pop();
					Predicate predicate = builder.getOriginal().greaterThan(path1.getOriginal(), path2.getOriginal());
					CriteriaPredicateWrapper predicateWrapper = new CriteriaPredicateWrapper(predicate);
					stack.push(predicateWrapper);
					return;
				}
				
				else if(nameAndType[0].equals("isNull")) {
					CriteriaPathWrapper p = (CriteriaPathWrapper)stack.pop();
					CriteriaBuilderWrapper builder = (CriteriaBuilderWrapper)stack.pop();
					Predicate predicate = builder.getOriginal().isNull(p.getOriginal());
					CriteriaPredicateWrapper predicateWrapper = new CriteriaPredicateWrapper(predicate);
					stack.push(predicateWrapper);
					return;
				}
				
				else if(nameAndType[0].equals("between")) {
					System.out.println("here we go");
					Object value2 = stack.pop();
					Object value1 = stack.pop();
					CriteriaPathWrapper path = (CriteriaPathWrapper)stack.pop();
					CriteriaBuilderWrapper builder = (CriteriaBuilderWrapper)stack.pop();
					Expression<Double> expr = (Expression)path.getOriginal();
					
					Double dv1 = new Double(0);
					Double dv2 = new Double(1);
					Predicate predicate = builder.getOriginal().between(expr,dv1,dv2);
					
					CriteriaPredicateWrapper predicateWrapper = new CriteriaPredicateWrapper(predicate);
//					predicateWrapper.addSymbolicParameter(value1);
//					predicateWrapper.addSymbolicParameter(value2);
					stack.push(predicateWrapper);
					return;
				}
				
				else if(nameAndType[0].equals("join")) {
					Object o = stack.pop();
					Object j = stack.pop();
					if(o instanceof SymbolicEntityAttribute && j instanceof CriteriaRootWrapper) {
						String fieldName = ((SymbolicEntityAttribute)o).getFieldName();
						Join join = ((CriteriaRootWrapper)j).getOriginal().join(fieldName);
						stack.push(new CriteriaJoinWrapper(join));
						return;
					} else if(o instanceof SymbolicEntityAttribute && j instanceof CriteriaJoinWrapper) {
						String fieldName = ((SymbolicEntityAttribute)o).getFieldName();
						Join join = ((CriteriaJoinWrapper)j).getOriginal().join(fieldName);
						stack.push(new CriteriaJoinWrapper(join));
						return;
					}
				}
				
				else if(nameAndType[0].equals("where") && nameAndType[1].equals("(Ljavax/persistence/criteria/Expression;)Ljavax/persistence/criteria/CriteriaQuery;")) {
					CriteriaPredicateWrapper predicate = (CriteriaPredicateWrapper)stack.pop();
					CriteriaQueryWrapper query = (CriteriaQueryWrapper)stack.pop();
					query.getOriginal().where(predicate.getOriginal());
					query.addSymbolicParameter(predicate.getSymbolicParameter());
					stack.push(query);
					return;
				}

				else if(nameAndType[0].equals("where") && nameAndType[1].equals("([Ljavax/persistence/criteria/Predicate;)Ljavax/persistence/criteria/CriteriaQuery;")) {
					Arrayref symArray = (Arrayref)stack.pop();
					CriteriaQueryWrapper query = (CriteriaQueryWrapper)stack.pop();
					
					Predicate[] predicateArray = new Predicate[symArray.length];
					for(int i=0; i<symArray.length; i++) {
						CriteriaPredicateWrapper predicate = (CriteriaPredicateWrapper)symArray.getElement(i);
						predicateArray[i] = predicate.getOriginal();
						query.addSymbolicParameter(predicate.getSymbolicParameter());
					}
					
					query.getOriginal().where(predicateArray);
					stack.push(query);
					return;
				}
				
				
				
				else if(nameAndType[0].equals("select") && nameAndType[1].equals("(Ljavax/persistence/criteria/Selection;)Ljavax/persistence/criteria/CriteriaQuery;")) {
					Object ob = stack.pop();
					CriteriaQueryWrapper query = (CriteriaQueryWrapper)stack.pop();
					
					if(ob instanceof CriteriaRootWrapper) {
						CriteriaRootWrapper root = (CriteriaRootWrapper)ob;
						query.getOriginal().select(root.getOriginal());
					} else if(ob instanceof CriteriaPathWrapper) {
						CriteriaPathWrapper path = (CriteriaPathWrapper)ob;
						query.getOriginal().select(path.getOriginal());
					}
					
					stack.push(query);
					return;
				}
				
				else if(nameAndType[0].equals("distinct") && nameAndType[1].equals("(Z)Ljavax/persistence/criteria/CriteriaQuery;")) {
					IntConstant distinctConstant = (IntConstant)stack.pop();
					CriteriaQueryWrapper query = (CriteriaQueryWrapper)stack.pop();
					if(distinctConstant.getIntValue() == 1) {
						query.getOriginal().distinct(true);
					}
					stack.push(query);
					return;
				}
				
				else {
					Object[] parameterObjects = new Object[para.size()];
					Class<?>[] paraClasses = new Class[para.size()];
					
					for(int i=(para.size()-1);i>=0; i--) {
						parameterObjects[i] = stack.pop();
						paraClasses[i] = parameterObjects[i].getClass();
					}
					
					Object referenceObject = stack.pop(); // pop the reference
					
					java.lang.reflect.Method realMethod = referenceObject.getClass().getMethod(nameAndType[0], paraClasses);
					Object result = realMethod.invoke(referenceObject, parameterObjects);
					
					if(!realMethod.getReturnType().getName().equals("void")) {
						stack.push(result);
					}
					return;
				}
			}
			
			
			if(k >= 0 && stack.get(k) instanceof CriteriaBuilderWrapper) {
				if(nameAndType[0].equals("createQuery") && nameAndType[1].equals("(Ljava/lang/Class;)Ljavax/persistence/criteria/CriteriaQuery;")) {	
					Objectref clazzRef = (Objectref)stack.pop();
					CriteriaBuilderWrapper builder = (CriteriaBuilderWrapper)stack.pop();
					stack.push(builder.createQuery(clazzRef));
					return;
				}
				
				if(nameAndType[0].equals("equal") && nameAndType[1].equals("(Ljavax/persistence/criteria/Expression;Ljava/lang/Object;)Ljavax/persistence/criteria/Predicate;")) {	
					Objectref value = (Objectref)stack.pop();
					CriteriaPathWrapper path = (CriteriaPathWrapper)stack.pop();
					CriteriaBuilderWrapper builder = (CriteriaBuilderWrapper)stack.pop();
					stack.push(builder.equal(path, value));
					return;
				}
				
				throw new RuntimeException("not supported yet: " + nameAndType[0] + " " + nameAndType[1]);
			}
			
			if(k >= 0 && stack.get(k) instanceof CriteriaQueryWrapper) {
				if(nameAndType[0].equals("from") && nameAndType[1].equals("(Ljava/lang/Class;)Ljavax/persistence/criteria/Root;")) {
					Objectref clazzRef = (Objectref)stack.pop();
					CriteriaQueryWrapper wrapper = (CriteriaQueryWrapper)stack.pop();
					stack.push(wrapper.from(clazzRef));
					return;
				}
				
				if(nameAndType[0].equals("where") && nameAndType[1].equals("(Ljavax/persistence/criteria/Expression;)Ljavax/persistence/criteria/CriteriaQuery;")) {
					CriteriaPredicateWrapper where = (CriteriaPredicateWrapper)stack.pop();
					CriteriaQueryWrapper wrapper = (CriteriaQueryWrapper)stack.pop();
					stack.push(wrapper.getOriginal().where(where.getOriginal()));
					return;
				}
				
				if(nameAndType[0].equals("select") && nameAndType[1].equals("(Ljavax/persistence/criteria/Selection;)Ljavax/persistence/criteria/CriteriaQuery;")) {
					
				}
				
				throw new RuntimeException("not supported yet: " + nameAndType[0] + " " + nameAndType[1]);
			}
			
			if(k >= 0 && stack.get(k) instanceof CriteriaRootWrapper) {
				if(nameAndType[0].equals("get") 
					&& nameAndType[1].equals("(Ljavax/persistence/metamodel/SingularAttribute;)Ljavax/persistence/criteria/Path;")) {
					SymbolicEntityAttribute singularAttributeRef = (SymbolicEntityAttribute)stack.pop();
					CriteriaRootWrapper wrapper = (CriteriaRootWrapper)stack.pop();
					stack.push(wrapper.get(singularAttributeRef));
					return;
				}
				throw new RuntimeException("not supported yet: " + nameAndType[0] + " " + nameAndType[1]);
			}
			
			
			if(k >= 0 && stack.get(k) instanceof QLStatement) {
				Object[] parameterObjects = new Object[para.size()];
				Class<?>[] paraClasses = new Class[para.size()];
				
				for(int i=(para.size()-1);i>=0; i--) {
					parameterObjects[i] = stack.pop();
					paraClasses[i] = parameterObjects[i].getClass();
				}
				Object referenceObject = stack.pop(); // pop the reference
				
				if(nameAndType[0].equals("setParameter") && 
					(nameAndType[1].equals("(Ljava/lang/String;Ljava/lang/Object;)Ljavax/persistence/TypedQuery;")
					|| nameAndType[1].equals("(Ljava/lang/String;Ljava/lang/Object;)Ljavax/persistence/Query;"))) {
					QLStatement<?> qlStmt = (QLStatement<?>)referenceObject;
					String name = getStringFromObjectref((Objectref)parameterObjects[0]);
					qlStmt.setParameter(name, parameterObjects[1]);
					stack.push(qlStmt);
				} else if(nameAndType[0].equals("getResultList")
					&& nameAndType[1].equals("()Ljava/util/List;")) {
					
					QLStatement<?> qlStatement = (QLStatement<?>)referenceObject;
					String queryResultName = "SymbolicQueryResultList#"+qlStatement.hashCode();
					SymbolicQueryResultList queryResultList = new SymbolicQueryResultList(queryResultName, (JPAVirtualMachine)frame.getVm(), qlStatement);
					
					queryResultList.checkForExistingData();
					
					stack.push(queryResultList);
					// no choice point here	((JPAVirtualMachine)frame.getVm()).generateGetQueryResultListChoicePoint(this, (QLStatement<?>)referenceObject);
				} else if(nameAndType[0].equals("getSingleResult")
						&& nameAndType[1].equals("()Ljava/lang/Object;")) {
					
					((JPAVirtualMachine)frame.getVm()).generateGetSingleResultChoicePoint(this, (QLStatement<?>)referenceObject);
				}
				
				
			} else if(k >= 0 && stack.get(k) instanceof MugglUserTransaction) {
				return;				
				
			} else if(k >= 0 && stack.get(k) instanceof MugglEntityManager) {
				
				Object[] parameterObjects = new Object[para.size()];
				Class<?>[] paraClasses = new Class[para.size()];
				
				for(int i=(para.size()-1);i>=0; i--) {
					parameterObjects[i] = stack.pop();
					paraClasses[i] = parameterObjects[i].getClass();
				}
				Object referenceObject = stack.pop(); // pop the reference
				
				
				if(nameAndType[0].equals("find")) {
					((JPAVirtualMachine)frame.getVm()).generateEntityManagerFindChoicePoint(
							this, 
							entityRefGenerator.getClassNameOfClassObjectRef((Objectref) parameterObjects[0]), 
							parameterObjects[1]);
				} else if(nameAndType[0].equals("getCriteriaBuilder")) {
					stack.push(((JPAVirtualMachine)frame.getVm()).getMugglEntityManager().getCriteriaWrapper());
				} else if(nameAndType[0].equals("persist")) {
//					throw new VmRuntimeException(frame.getVm().generateExc("javax.persistence.EntityExistsException", "not good"));
					((JPAVirtualMachine)frame.getVm()).generateEntityManagerPersistChoicePoint(this, (Objectref)parameterObjects[0]);
				} else {
					java.lang.reflect.Method realMethod = MugglEntityManager.class.getMethod(nameAndType[0], paraClasses);

					try {
						Object result = realMethod.invoke(referenceObject, parameterObjects);
						if(!realMethod.getReturnType().getName().equals("void")) {
							stack.push(result);
						}
					} catch(InvocationTargetException e) {
						e.printStackTrace();
						if(e.getTargetException() instanceof VmRuntimeException) {
							VmRuntimeException vmEx = (VmRuntimeException)e.getTargetException();
							throw new VmRuntimeException(vmEx.getWrappedException());
						}
					}
				}
			} else if(k >= 0 && (stack.get(k) instanceof MugglTypedQuery<?>)) {
				Object[] parameterObjects = new Object[para.size()];
				Class<?>[] paraClasses = new Class[para.size()];
				
				for(int i=(para.size()-1);i>=0; i--) {
					parameterObjects[i] = stack.pop();
					paraClasses[i] = parameterObjects[i].getClass();
				}
				
				MugglTypedQuery<?> typedQuery = (MugglTypedQuery<?>)stack.pop();
				
				if(nameAndType[0].equals("getResultList")) {
					String resultListName = "resultlist#"+typedQuery.hashCode();
					ReferenceQueryResultArrayListVariable resultList = new ReferenceQueryResultArrayListVariable(resultListName, ((JPAVirtualMachine)frame.getVm()), typedQuery.getCriteriaQuery());
					stack.push(resultList);
					throw new RuntimeException("Not handled here");
//					((JPAVirtualMachine)frame.getVm()).getVirtualObjectDatabase().addQueryResultList(resultList);
					// TODO also add this reference to the virtual database as 'required data list' oder so...
				}
				
			} else if(k >= 0 && (stack.get(k) instanceof MugglJPA)) {
				Object[] parameterObjects = new Object[para.size()];
				Class<?>[] paraClasses = new Class[para.size()];
				
				for(int i=(para.size()-1);i>=0; i--) {
					parameterObjects[i] = stack.pop();
					paraClasses[i] = parameterObjects[i].getClass();
				}
				
				Object referenceObject = stack.pop(); // pop the reference
				
				java.lang.reflect.Method realMethod = referenceObject.getClass().getMethod(nameAndType[0], paraClasses);
				Object result = realMethod.invoke(referenceObject, parameterObjects);
				
				if(!realMethod.getReturnType().getName().equals("void")) {
					stack.push(result);
				}
	
			} else {
				super.executeSymbolically(frame);
			}
		} catch (VmRuntimeException e) {
			SymbolicExceptionHandler handler = new SymbolicExceptionHandler(frame, e);
			try {
				handler.handleException();
			} catch (ExecutionException e2) {
				executionFailedSymbolically(e2);
			}
		} catch (SymbolicExecutionException e) {
			executionFailedSymbolically(e);
		} catch (Exception e) {
			e.printStackTrace();
			throw new SymbolicExecutionException("Error while invoking interface method: " + this, e);
			
		}
	}
		
	private Object getDummyObject(Class javaType, Objectref objectValue) {
		if(javaType.getName().equals("long") || javaType.getName().equals("java.lang.Long")) {
			return new Long(0);
		}
		
		if(javaType.getName().equals("int") || javaType.getName().equals("java.lang.Integer")) {
			return new Integer(0);
		}
		
		return objectValue;
	}

	private String getClassNameOfClassObjectRef(Objectref classObjectRef) {
		for(Field f : classObjectRef.getFields().keySet()) {
			if(f.getName().equals("name")) {
				return getStringValueOfObjectRef((Objectref)classObjectRef.getFields().get(f));
			}
		}
		return null;
	}
	
	private Object[] getConcreteObjects(Object[] parameterObjects, Class<?>[] types) throws Exception {
		Object[] concretObjects = new Object[parameterObjects.length];
		for(int i=0; i<parameterObjects.length; i++) {
			if(parameterObjects[i] instanceof Objectref) {
				Objectref objRef = (Objectref)parameterObjects[i];
				String name = objRef.getInitializedClass().getClassFile().getName();
				System.out.println("class file name = " + name);
				if(name.equals(String.class.getName())) {
					String stringValue = getStringValueOfObjectRef((Objectref)parameterObjects[i]);
					concretObjects[i] = stringValue;
				} else
				if(name.equals(Integer.class.getName())) {
					for(Field f : objRef.getFields().keySet()) {
						if(f.getName().equals("value")) {
							Object obj = objRef.getFields().get(f);
							if(obj instanceof IntConstant) {
								IntConstant ic = (IntConstant)obj;
								concretObjects[i] = new Integer(ic.getIntValue());
							}
						}
					}
				} else
				if(types[i] == Class.class && name.equals(Class.class.getName())) {
					for(Field f : objRef.getFields().keySet()) {
						if(f.getName().equals("name")) {
							String className = getStringValueOfObjectRef((Objectref)objRef.getFields().get(f));
							try {
								Class<?> clazz = ClassLoader.getSystemClassLoader().loadClass(className);
								concretObjects[i] = clazz;
								break;
							} catch (ClassNotFoundException e) {
								throw new RuntimeException("Could not load class: " + className + "!", e);
							}
						}
					}
				} else {
					// it is an object reference, but cannot get any concret value...
					concretObjects[i] = parameterObjects[i];
				}
			} else
			if(types[i] == String.class && parameterObjects[i] instanceof Objectref) {
				String stringValue = getStringValueOfObjectRef((Objectref)parameterObjects[i]);
				concretObjects[i] = stringValue;
			} else if(types[i].isAssignableFrom(Integer.class) && parameterObjects[i] instanceof Objectref) {
				Objectref objRef = (Objectref)parameterObjects[i];
				for(Field f : objRef.getFields().keySet()) {
					if(f.getName().equals("value")) {
						Object obj = objRef.getFields().get(f);
						if(obj instanceof IntConstant) {
							IntConstant ic = (IntConstant)obj;
							concretObjects[i] = new Integer(ic.getIntValue());
						}
					}
				}
			} else if(types[i] == Class.class && parameterObjects[i] instanceof Objectref) {
				Objectref objRef = (Objectref)parameterObjects[i];
				for(Field f : objRef.getFields().keySet()) {
					if(f.getName().equals("name")) {
						String className = getStringValueOfObjectRef((Objectref)objRef.getFields().get(f));
						try {
							Class<?> clazz = ClassLoader.getSystemClassLoader().loadClass(className);
							concretObjects[i] = clazz;
							break;
						} catch (ClassNotFoundException e) {
							throw new RuntimeException("Could not load class: " + className + "!", e);
						}
					}
				}
				objRef.getInitializedClass().getClassFile();
//			} else if(types[i] != Object.class && types[i].isAssignableFrom(parameterObjects[i].getClass())) {
//				concretObjects[i] = parameterObjects[i];
			} else if(types[i] == Object.class && parameterObjects[i] instanceof Objectref) {
				concretObjects[i] = parameterObjects[i];
			} else if(parameterObjects[i] instanceof IntConstant) {			
				concretObjects[i] = ((IntConstant)parameterObjects[i]).getIntValue();
			} else if(parameterObjects[i] instanceof DoubleConstant) {			
				concretObjects[i] = ((DoubleConstant)parameterObjects[i]).getDoubleValue();
			} else if(parameterObjects[i] instanceof FloatConstant) {			
				concretObjects[i] = ((FloatConstant)parameterObjects[i]).getFloatValue();
			} else if(parameterObjects[i] instanceof LongConstant) {			
				concretObjects[i] = ((LongConstant)parameterObjects[i]).getLongValue();
			} else if(types[i].isAssignableFrom(parameterObjects[i].getClass())) {
				concretObjects[i] = parameterObjects[i];
			} else {
				throw new RuntimeException("Could not find concrete type...");
			}
		}
		return concretObjects;
	}
 
	protected Class<?>[] getTypes(List<String> descriptions) {
		Class<?>[] types = new Class<?>[descriptions.size()];
		for(int i=0; i<descriptions.size(); i++) {
			String desc = descriptions.get(i);
			if(desc.equals("B")) {
				types[i] = byte.class;
			} else if(desc.equals("C")) {
				types[i] = char.class;
			} else if(desc.equals("D")) {
				types[i] = double.class;
			} else if(desc.equals("F")) {
				types[i] = float.class;
			} else if(desc.equals("I")) {
				types[i] = int.class;
			} else if(desc.equals("J")) {
				types[i] = long.class;
			} else if(desc.equals("S")) {
				types[i] = short.class;
			} else if(desc.equals("Z")) {
				types[i] = boolean.class;
			} else if(desc.startsWith("L")) {
				String className = desc.substring(1, desc.length()-1);
				className = className.replaceAll("/*/", "\\.");
				try {
					types[i] = ClassLoader.getSystemClassLoader().loadClass(className);
				} catch (ClassNotFoundException e) {
					throw new RuntimeException("Could not load class: " + className + "!", e);
				}
			}
		}
		return types;
	}

	
	protected Object[] getParameters(Stack<Object> stack, int methodArgCount) {
		Object[] parameters = new Object[methodArgCount + this.hasObjectrefParameter];
		for (int a = parameters.length - 1; a >= this.hasObjectrefParameter; a--) {
			parameters[a] = stack.pop();
		}
		return parameters;
	}
	
	
	public int getMethodParametersCount(Frame frame) throws ClassFileException, ExecutionException, VmRuntimeException  {
		int index = this.otherBytes[0] << ONE_BYTE | this.otherBytes[1];
		Constant constant = frame.getConstantPool()[index];

		// Get the name and the descriptor.
		String[] nameAndType = getNameAndType(constant);
		ClassFile methodClassFile = getMethodClassFile(constant, frame.getVm().getClassLoader());
		
		// Try to resolve method from this class.
		ResolutionAlgorithms resoluton = new ResolutionAlgorithms(frame.getVm().getClassLoader());
		Method method;
		try {
			method = resoluton.resolveMethod(methodClassFile, nameAndType);
		} catch (ClassFileException e) {
			throw new VmRuntimeException(frame.getVm().generateExc("java.lang.NoClassDefFoundError", e.getMessage()));
		} catch (NoSuchMethodError e) {
			throw new VmRuntimeException(frame.getVm().generateExc("java.lang.NoSuchMethodError", e.getMessage()));
		}
		int parameterCount = method.getNumberOfArguments();
		if (frame.getOperandStack().size() < parameterCount)
			throw new ExecutionException("Error while executing instruction " + getName()
					+ ": There are less elements on the stack than parameters needed.");
		return method.getNumberOfArguments();
	}
	
	
/*
//	@Override
	public void executeSymbolicallyOLD(Frame frame) {
		
		try {		
			Stack<Object> stack = frame.getOperandStack();
			
			int index = this.otherBytes[0] << ONE_BYTE | this.otherBytes[1];
			Constant constant = frame.getConstantPool()[index];
	
			// Get the name and the descriptor.
			String[] nameAndType = getNameAndType(constant);
			ClassFile methodClassFile = getMethodClassFile(constant, frame.getVm().getClassLoader());
			
			// Try to resolve method from this class.
			ResolutionAlgorithms resoluton = new ResolutionAlgorithms(frame.getVm().getClassLoader());
			Method method;
			try {
				method = resoluton.resolveMethod(methodClassFile, nameAndType);
			} catch (ClassFileException e) {
				throw new VmRuntimeException(frame.getVm().generateExc("java.lang.NoClassDefFoundError", e.getMessage()));
			} catch (NoSuchMethodError e) {
				throw new VmRuntimeException(frame.getVm().generateExc("java.lang.NoSuchMethodError", e.getMessage()));
			}
			
			// Prepare the parameter's array.
			int parameterCount = method.getNumberOfArguments();
			if (stack.size() < parameterCount)
				throw new ExecutionException("Error while executing instruction " + getName()
						+ ": There are less elements on the stack than parameters needed.");
			// If it is not invokestatic the object reference is on the stack below the arguments.
			Object[] parameters = new Object[parameterCount + this.hasObjectrefParameter];
	
			// Get nargs arguments.
			for (int a = parameters.length - 1; a >= this.hasObjectrefParameter; a--) {
				parameters[a] = stack.pop();
			}
		
//			if(stack != null && stack.size() > 0 && stack.peek() instanceof SymbolicCriteriaBuilder) {
//				SymbolicCriteriaBuilder criteriaBuilder = (SymbolicCriteriaBuilder) stack.pop();
//
//				if(method.getName().equals("createQuery")) {
//					stack.push(criteriaBuilder.createQuery((Objectref)parameters[1]));
//					return;
//				}
//			}			
			
//			if(stack != null && stack.size() > 0 && stack.peek() instanceof SymbolicAbstractQuery) {
//				SymbolicAbstractQuery query = (SymbolicAbstractQuery) stack.pop();
//				
//				if(method.getName().equals("from")) {
//					stack.push(query.from((Objectref)parameters[1]));
//					return;
//				}
//				
//				if(method.getName().equals("select")) {
//					stack.push(query.select((SymbolicSelection)parameters[1]));
//					return;
//				}
//			}
			
			
			
			if(stack != null && stack.size() > 0 && stack.peek() instanceof SymbolicEntityManager) {
				SymbolicEntityManager entityManager = (SymbolicEntityManager) stack.pop();
				
				if(method.getName().equals("getCriteriaBuilder")) {
					stack.push(new SymbolicCriteriaBuilder());
					return;
				}
				
				if(method.getName().equals("persist")) {
					((JPAVirtualMachine) frame.getVm()).getVirtualDatabase().persist((Objectref)parameters[1]);
//					entityManager.persist((Objectref)parameters[1]);
					return;
				}
				
				if(method.getName().equals("find")) {
					Objectref clazzRef = (Objectref)parameters[1];
					Field field = clazzRef.getFields().keys().nextElement();
					Object o = clazzRef.getFields().get(field);
					
					String entityClassName = getStringValueOfObjectRef((Objectref)o);
					
					Class<?> entityClass = null;
					try {
						// TODO: load it with the classpath entries from the muggl-class-loader
						entityClass = ClassLoader.getSystemClassLoader().loadClass(entityClassName);
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
						throw new ExecutionException("Could not load class: " +entityClass);
					}
					
					JPAEntityConstraint constraint = analyzer.generateInitialEntityConstraint(entityClass, solverManager);
					
					((JPAVirtualMachine) frame.getVm()).getVirtualDatabase().addVariables(constraint);
					
					ConstraintExpression mainExpression = null;
					Object keyObject = parameters[2];
					String idFieldName = constraint.getIdField();
					Variable v = constraint.getVariable(idFieldName);
					NumericVariable mainIdVar = (NumericVariable)v;
					if(v instanceof NumericVariable) {
						mainIdVar = (NumericVariable)v;
						if(keyObject instanceof NumericVariable) {
							mainExpression = NumericEqual.newInstance(mainIdVar, (NumericVariable)keyObject);
						} else {
							throw new RuntimeException("What now?");
						}
					}
					
					stack.push(entityManager.find(entityClass, parameters[2], false));
					
					((JPAVirtualMachine) frame.getVm()).generateNewJPAEntityManagerFindChoicePoint(this, mainExpression);
					
					return;
					
					
					
					
					
					/*
					JPAEntityConstraint constraint = analyzer.generateInitialEntityConstraint(entityClass, solverManager);
					String idFieldName = constraint.getIdField();
					Variable v = constraint.getVariable(idFieldName);
															
					Object keyObject = parameters[2];
					ConstraintExpression mainExpression = null;
					NumericVariable mainIdVar = (NumericVariable)v;
					if(v instanceof NumericVariable) {
						mainIdVar = (NumericVariable)v;
						if(keyObject instanceof NumericVariable) {
							mainExpression = NumericEqual.newInstance(mainIdVar, (NumericVariable)keyObject);
						} else {
							throw new RuntimeException("What now?");
						}
					}
					
					if(constraint.getDependentEntities().size() > 0) {
						for(Class<?> dpEntityClass : constraint.getDependentEntities()) {
							JPAEntityConstraint dpConstraint = analyzer.generateInitialEntityConstraint(dpEntityClass, solverManager);
							String dpIdField = dpConstraint.getIdField(); // TODO: not the ID field, but the field for the refernce...
							NumericVariable dpIdVar = (NumericVariable)constraint.getVariable(dpIdField);
							solverManager.addConstraint(NumericEqual.newInstance(mainIdVar, dpIdVar));
						}
					}
					
					stack.push(entityManager.find(entityClass, parameters[2], false));
					
					
					((SymbolicVirtualMachine) frame.getVm()).generateNewJPAEntityManagerFindChoicePoint(this, mainExpression);
//					((SymbolicVirtualMachine) frame.getVm()).generateNewJPAEntityManagerFindChoicePoint(this, entityClassName, keyValue);
					return;
					'/
				}
				
				return;
			}
			
			
			
			// if not JPA special, then execute 'normally'
			super.executeSymbolically(frame);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
*/










//	public void executeSymbolicallyOLD(Frame frame) {
//		try {		
//			Stack<Object> stack = frame.getOperandStack();
//			
//			int index = this.otherBytes[0] << ONE_BYTE | this.otherBytes[1];
//			Constant constant = frame.getConstantPool()[index];
//	
//			// Get the name and the descriptor.
//			String[] nameAndType = getNameAndType(constant);
//			ClassFile methodClassFile = getMethodClassFile(constant, frame.getVm().getClassLoader());	
//			
//			// Try to resolve method from this class.
//			ResolutionAlgorithms resoluton = new ResolutionAlgorithms(frame.getVm().getClassLoader());
//			Method method;
//			try {
//				method = resoluton.resolveMethod(methodClassFile, nameAndType);
//			} catch (ClassFileException e) {
//				throw new VmRuntimeException(frame.getVm().generateExc("java.lang.NoClassDefFoundError", e.getMessage()));
//			} catch (NoSuchMethodError e) {
//				throw new VmRuntimeException(frame.getVm().generateExc("java.lang.NoSuchMethodError", e.getMessage()));
//			}
//			
//			// Prepare the parameter's array.
//			int parameterCount = method.getNumberOfArguments();
//			if (stack.size() < parameterCount)
//				throw new ExecutionException("Error while executing instruction " + getName()
//						+ ": There are less elements on the stack than parameters needed.");
//			// If it is not invokestatic the object reference is on the stack below the arguments.
//			Object[] parameters = new Object[parameterCount + this.hasObjectrefParameter];
//	
//			// Get nargs arguments.
//			for (int a = parameters.length - 1; a >= this.hasObjectrefParameter; a--) {
//				parameters[a] = stack.pop();
//			}
//		
//			if(stack != null && stack.size() > 0 && stack.peek() instanceof SymbolicEntityManager) {
//				SymbolicEntityManager entityManager = (SymbolicEntityManager) stack.pop();
//				
//				if(method.getName().equals("persist")) {				
//					entityManager.persist((Objectref)parameters[1]);
//					return;
//				}
//				
//				System.out.println("yep, it is a symbolic entity manager...");
//				Objectref clazzRef = (Objectref)parameters[1];
//				Field field = clazzRef.getFields().keys().nextElement();
//				Object o = clazzRef.getFields().get(field);
//				System.out.println("o=" +o);
//				
//				Objectref oRef = (Objectref)o;
//				Enumeration<Field> fields = oRef.getFields().keys();
//				String entityClassName = "";
//				while(fields.hasMoreElements()){
//					Field f = fields.nextElement();
//					if(f.getName().equals("value")) {
//						Object THE_CLASS = oRef.getField(f);
//						Arrayref classNameRef = (Arrayref)THE_CLASS;
//						for(int i=0; i<classNameRef.length; i++) {
//							IntConstant ic = (IntConstant)classNameRef.getElement(i);
//							byte asciiNumber = (byte)ic.getValue();
//							// TODO: concatenate via string builder...
//							entityClassName += Character.toString((char)asciiNumber);
//						}
//					}
//				}
//				
//				
//				// check what to do...
//				if(methodClassFile.getName().equals("javax.persistence.EntityManager")
//					&& nameAndType.length == 2 
//					&& nameAndType[0].equals("find") 
//					&& nameAndType[1].equals("(Ljava/lang/Class;Ljava/lang/Object;)Ljava/lang/Object;")) {
//					
//					Class<?> entityClass = null;
//					try {
//						// TODO: load it with the classpath entries from the muggl-class-loader
//						entityClass = ClassLoader.getSystemClassLoader().loadClass(entityClassName);
//					} catch (ClassNotFoundException e) {
//						e.printStackTrace();
//						throw new ExecutionException("Could not load class: " +entityClass);
//					}
//					
//					stack.push(entityManager.find(entityClass, parameters[2]));
//					return;
//				}
//				
//				// if we are here -> error... (we 
//				throw new ExecutionException("Symbolic Entity Manager does not know what to do here...");
//			}
//		
//			// if not JPA special, then execute 'normally'
//			super.executeSymbolically(frame);
//		} catch (Exception e) {
//
//		}
//	}

	private String getStringFromObjectref(Objectref stringObjectref) {
		Field valueField = stringObjectref.getInitializedClass().getClassFile().getFieldByName("value");
		Arrayref arrayref = (Arrayref)stringObjectref.getField(valueField);
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<arrayref.length; i++) {
			char c = (char)((IntConstant)arrayref.getElement(i)).getIntValue();
			sb.append(c);
		}
		return sb.toString();
	}
	
	@Deprecated
	protected String getStringValueOfObjectRef(Objectref oRef) {
		Enumeration<Field> fields = oRef.getFields().keys();
		String entityClassName = "";
		while(fields.hasMoreElements()){
			Field f = fields.nextElement();
			if(f.getName().equals("value")) {
				Object THE_CLASS = oRef.getField(f);
				Arrayref classNameRef = (Arrayref)THE_CLASS;
				for(int i=0; i<classNameRef.length; i++) {
					IntConstant ic = (IntConstant)classNameRef.getElement(i);
					byte asciiNumber = (byte)ic.getValue();
					// TODO: concatenate via string builder...
					entityClassName += Character.toString((char)asciiNumber);
				}
			}
		}
		return entityClassName;
	}
}
