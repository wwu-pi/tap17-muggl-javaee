//package de.wwu.muggl.instructions.jpa;
//
//import de.wwu.muggl.instructions.InvalidInstructionInitialisationException;
//import de.wwu.muggl.instructions.bytecode.Ifnonnull;
//import de.wwu.muggl.instructions.general.If_icmp;
//import de.wwu.muggl.instructions.general.If_ref;
//import de.wwu.muggl.jpa.FindResult;
//import de.wwu.muggl.solvers.SolverManager;
//import de.wwu.muggl.solvers.expressions.And;
//import de.wwu.muggl.solvers.expressions.ConstraintExpression;
//import de.wwu.muggl.solvers.expressions.IntConstant;
//import de.wwu.muggl.solvers.expressions.LessOrEqual;
//import de.wwu.muggl.solvers.expressions.NumericEqual;
//import de.wwu.muggl.solvers.expressions.NumericNotEqual;
//import de.wwu.muggl.solvers.expressions.NumericVariable;
//import de.wwu.muggl.solvers.expressions.Term;
//import de.wwu.muggl.solvers.expressions.Variable;
//import de.wwu.muggl.solvers.solver.constraints.AndConstraint;
//import de.wwu.muggl.symbolic.generating.DatabaseGenerator;
//import de.wwu.muggl.symbolic.generating.Generator;
//import de.wwu.muggl.symbolic.generating.GeneratorProvider;
//import de.wwu.muggl.symbolic.generating.jpa.JPAAnalyzeException;
//import de.wwu.muggl.symbolic.generating.jpa.JPAEntityAnalyzer;
//import de.wwu.muggl.symbolic.jpa.JPAEntityConstraint;
//import de.wwu.muggl.symbolic.jpa.var.SymbolicFindQueryResult;
//import de.wwu.muggl.symbolic.jpa.var.SymbolicQueryResult;
//import de.wwu.muggl.vm.Frame;
//import de.wwu.muggl.vm.classfile.structures.Method;
//import de.wwu.muggl.vm.classfile.structures.attributes.AttributeCode;
//import de.wwu.muggl.vm.impl.jpa.JPAVirtualMachine;
//import de.wwu.muggl.vm.impl.symbolic.SymbolicExecutionException;
//import de.wwu.muggl.vm.impl.symbolic.SymbolicVirtualMachine;
//import de.wwu.muggl.vm.initialization.FieldObjectNullReference;
//
//public class JPAIfnonnull extends Ifnonnull {
//	
//	private JPAEntityAnalyzer analyzer;
//	private SolverManager solverManager;
//	
//	// TODO: das hier war nur zum testen..
////	public static NumericVariable userID;
//
//	public JPAIfnonnull(AttributeCode code, int lineNumber)	throws InvalidInstructionInitialisationException {
//		super(code, lineNumber);
//		analyzer = new JPAEntityAnalyzer();
//		
//		
//		// TODO: das hier war nur zum testen..
////		userID = new NumericVariable("UserId0", "int");
//	}
//
//	public void setSolverManager(SolverManager solverManager) {
//		this.solverManager = solverManager;
//	}
//	
//	@Override
//	public void executeSymbolically(Frame frame) {
//		if(frame.getOperandStack().peek() instanceof FindResult) {
//			// generate a choice point for either find result exists, or not exists in DB
//			JPAVirtualMachine jpaVM = (JPAVirtualMachine) frame.getVm();
//			FindResult findResult = (FindResult) frame.getOperandStack().pop();
//			jpaVM.generateNewFindResultChoicePoint(jpaVM, this, findResult, getJumpTarget());
//		} else {
//			super.executeSymbolically(frame);
//		}
//		
//		
////		Object value = frame.getOperandStack().pop();
////		if(value != null) {
////			// if the value is not null:
////			// 1) check if it is a null reference
////			if(value instanceof FieldObjectNullReference) {
////				// it is a null reference -> do not jump to target
////			}
////			// 2) check special JPA handling, e.g., if it is a query that might have returned a null-reference
////			if(value instanceof SymbolicFindQueryResult) {
////				SymbolicFindQueryResult queryResult = (SymbolicFindQueryResult)value;
////				try {
////					JPAEntityConstraint constraint = analyzer.generateInitialEntityConstraint(queryResult.getResultClass(), solverManager);
////					String field = constraint.getIdField();
////					Variable v = constraint.getVariable(field);
////					
////					ConstraintExpression mainExpression = null;
////					NumericVariable mainIdVar = (NumericVariable)v;
////					if(v instanceof NumericVariable) {
////						mainIdVar = (NumericVariable)v;
////						mainExpression = NumericEqual.newInstance(mainIdVar, (NumericVariable)queryResult.getKey());
////					}
////					
//////					if(constraint.getDependentEntities().size() > 0) {
//////						// generate also for dependent entities
//////						for(Class<?> dpEntityClass : constraint.getDependentEntities()) {
//////							JPAEntityConstraint dpConstraint = analyzer.generateInitialEntityConstraint(dpEntityClass);
//////							String dpIdField = dpConstraint.getIdField(); // TODO: not the ID field, but the field for the refernce...
//////							NumericVariable dpIdVar = (NumericVariable)constraint.getVariable(dpIdField);
//////							NumericEqual.newInstance(mainIdVar, dpIdVar);
//////						}
//////					}
////					
////					((SymbolicVirtualMachine) frame.getVm()).generateNewDBChoicePoint(this, mainExpression);
////				} catch (SymbolicExecutionException e) {
////					e.printStackTrace();
////				} catch (JPAAnalyzeException e) {
////					e.printStackTrace();
////				}
////			}
////		}
//	}
//
////	private ConstraintExpression getConstraintExpression(SymbolicQueryResult queryResult) {
////		if(queryResult instanceof SymbolicFindQueryResult) {
////			SymbolicFindQueryResult findQuery = (SymbolicFindQueryResult)queryResult;
//////			return NumericNotEqual.newInstance(userID, (NumericVariable)findQuery.getKey());
////			return NumericEqual.newInstance(userID, IntConstant.THREE);
////		}
////		return LessOrEqual.newInstance(new NumericVariable("fooA", "int"), new NumericVariable("fooB", "int"));
////	}
//}
