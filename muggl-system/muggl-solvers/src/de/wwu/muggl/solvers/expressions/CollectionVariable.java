//package de.wwu.muggl.solvers.expressions;
//
//public abstract class CollectionVariable extends Term implements Variable {
//
//    /**
//     * Stores the name of the variable.
//     */
//    protected String name;
//    
//    /**
//     * Stores the collection type.
//     */
//    protected String collectionType;
//    
//    /**
//     * Length of this collection.
//     */
//	protected NumericVariable length;
//
//    public CollectionVariable(String name, String collectionType) {
//		this.name = name;
//		this.collectionType = collectionType;
//		this.length = new NumericVariable(name + ".length", Expression.INT);
//	}
//    
//    @Override
//    public boolean isConstant(){
//    	return false;
//    }
//    
//    public String getCollectionType() {
//    	return this.collectionType;
//    }
//}
