package de.wwu.muggl.jpa.criteria;

import de.wwu.muggl.jpa.criteria.meta.MugglJPA;

import java.util.List;

import javax.persistence.criteria.Selection;

public class MugglSelection<X> extends MugglTupleElement<X> implements Selection<X>, MugglJPA {

	@Override
	public Selection<X> alias(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isCompoundSelection() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<Selection<?>> getCompoundSelectionItems() {
		// TODO Auto-generated method stub
		return null;
	}


}
