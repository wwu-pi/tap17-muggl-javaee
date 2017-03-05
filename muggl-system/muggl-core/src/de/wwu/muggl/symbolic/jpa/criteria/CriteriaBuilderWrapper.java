package de.wwu.muggl.symbolic.jpa.criteria;

import javax.persistence.criteria.CriteriaBuilder;

import de.wwu.muggl.solvers.expressions.IntConstant;
import de.wwu.muggl.vm.classfile.structures.Field;
import de.wwu.muggl.vm.initialization.Arrayref;
import de.wwu.muggl.vm.initialization.Objectref;
import de.wwu.muggl.vm.var.ReferenceVariable;

public class CriteriaBuilderWrapper {

	protected CriteriaBuilder original;
	
	public CriteriaBuilderWrapper(CriteriaBuilder original) {
		this.original = original;
	}
	
	public CriteriaBuilder getOriginal() {
		return this.original;
	}

	public CriteriaQueryWrapper createQuery(Objectref clazzRef) {
		try {
			String clazzName = getClassNameFromObjectref(clazzRef);
			Class<?> resultClass = ClassLoader.getSystemClassLoader().loadClass(clazzName);
			return new CriteriaQueryWrapper(this.original.createQuery(resultClass));
		} catch(Exception e) {
			throw new RuntimeException("error while generating criteria query", e);
		}
	}
	
	public CriteriaPredicateWrapper equal(CriteriaPathWrapper path, Objectref value) {
		return new CriteriaPredicateWrapper(this.original.equal(path.getOriginal(), value));
	}
	
	private String getClassNameFromObjectref(Objectref classObjectref) {
		Field nameField = classObjectref.getInitializedClass().getClassFile().getFieldByName("name");
		return getStringFromObjectref((Objectref)classObjectref.getField(nameField));
	}
	
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
}
