package de.wwu.muggl.symbolic.jpa.criteria;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaQuery;

import de.wwu.muggl.solvers.expressions.IntConstant;
import de.wwu.muggl.vm.classfile.structures.Field;
import de.wwu.muggl.vm.initialization.Arrayref;
import de.wwu.muggl.vm.initialization.Objectref;

public class CriteriaQueryWrapper {

	protected CriteriaQuery original;
	
	protected List<Objectref> symbolicParameter;
	
	public CriteriaQueryWrapper(CriteriaQuery original) {
		this.original = original;
		this.symbolicParameter = new ArrayList<>();
	}
	
	public CriteriaQuery getOriginal() {
		return this.original;
	}

	public CriteriaRootWrapper from(Objectref clazzRef) {
		try {
			String clazzName = getClassNameFromObjectref(clazzRef);
			Class<?> resultClass = ClassLoader.getSystemClassLoader().loadClass(clazzName);
			return new CriteriaRootWrapper(this.original.from(resultClass));
		} catch(Exception e) {
			throw new RuntimeException("error while generating criteria query", e);
		}
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

	public void addSymbolicParameter(List<Objectref> symbolicParameter) {
		this.symbolicParameter.addAll(symbolicParameter);
	}
	
	public List<Objectref> getSymbolicParameter() {
		return this.symbolicParameter;
	}
}
