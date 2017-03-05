package de.wwu.muggl.vm.impl.jpa.db;

import java.util.HashMap;
import java.util.Map;

import de.wwu.muggl.solvers.expressions.Expression;
import de.wwu.muggl.symbolic.jpa.var.entity.EntityObjectReference;

// ACCESSIBLE COLUMN AND ROW WISE -> good
public class DynamicExpressionMatrix {

	private Expression[][] matrix;
	private int currentPosition;
	private String[] columnNames;
	private boolean[] isRequiredData;
	private Map<Integer, EntityObjectReference> entityReferenceMap;
	
	private int n, m;
	
	DynamicExpressionMatrix(int n, int m, String[] columnNames) {
		if(n < 0) n = 0;
		if(m < 0) m = 0;
		if(m != columnNames.length) throw new RuntimeException("For each column a description must be given");
		this.matrix = new Expression[n][m];
		this.columnNames = columnNames;
		this.currentPosition = -1;
		this.isRequiredData = new boolean[n];
		this.entityReferenceMap = new HashMap<Integer, EntityObjectReference>();
		this.n = n;
		this.m = m;
	}
	
	DynamicExpressionMatrix getClone() {
		DynamicExpressionMatrix clone = new DynamicExpressionMatrix(n,m,columnNames);
		for(int x=0; x<this.currentPosition; x++) {
			for(int y=0; y<columnNames.length; y++) {
				clone.set(x, y, matrix[x][y]);
			}
		}
		return clone;
	}
	
	public void newEntryStart() {
		currentPosition++;
	}
	
	public int getCurrentPosition() {
		return currentPosition;
	}
	
	public String[] getColumnNames() {
		return columnNames;
	}
	
	public void set(int x, int y, Expression value) {
		if (x >= matrix.length) {
			Expression[][] tmp = matrix;
			matrix = new Expression[x + 1][];
			System.arraycopy(tmp, 0, matrix, 0, tmp.length);
			for (int i = x; i < x + 1; i++) {
				matrix[i] = new Expression[y];
			}
			
			boolean[] tmp2 = isRequiredData;
			isRequiredData = new boolean[x + 1];
			System.arraycopy(tmp2, 0, isRequiredData, 0, tmp2.length);
		}

		if (y >= matrix[x].length) {
			Expression[] tmp = matrix[x];
			matrix[x] = new Expression[y + 1];
			System.arraycopy(tmp, 0, matrix[x], 0, tmp.length);
		}

		matrix[x][y] = value;
	}
	
	public void setRequiredData(int x) {
		this.isRequiredData[x] = true;
	}
	
	public boolean isRequiredData(int x) {
		return this.isRequiredData[x];
	}
	
	public int getColumnPosition(String columnName) {
		for(int i=0; i<columnNames.length; i++) {
			if(columnNames[i].equals(columnName)) {
				return i;
			}
		}
		return -1;
	}
	
	public Expression get(int x, String columnName) {
		int colPos = getColumnPosition(columnName);
		return get(x, colPos);
	}

    public Expression get(int x, int y) {
        return x >= matrix.length || y >= matrix[x].length ? null : matrix[x][y];
    }

	public void setEntityReference(int x, EntityObjectReference eor) {
		entityReferenceMap.put(x, eor);
	}
    
	public EntityObjectReference getEntityReference(int x) {
		return entityReferenceMap.get(x);
	}
    
}
