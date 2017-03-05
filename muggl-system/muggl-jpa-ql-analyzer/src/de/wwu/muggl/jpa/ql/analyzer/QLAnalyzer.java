package de.wwu.muggl.jpa.ql.analyzer;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.TypedQuery;

import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.hibernate.query.criteria.internal.compile.CriteriaQueryTypeQueryAdapter;
import org.hibernate.sqm.parser.common.ParsingContext;
import org.hibernate.sqm.parser.hql.internal.HqlParseTreeBuilder;
import org.hibernate.sqm.parser.hql.internal.SemanticQueryBuilder;
import org.hibernate.sqm.parser.hql.internal.antlr.HqlParser;
import org.hibernate.sqm.query.SqmSelectStatement;
import org.hibernate.sqm.query.SqmStatement;
import org.hibernate.sqm.query.from.SqmFromClause;
import org.hibernate.sqm.query.from.SqmFromElementSpace;
import org.hibernate.sqm.query.predicate.SqmWhereClause;
import org.hibernate.sqm.test.ConsumerContextImpl;
import org.hibernate.sqm.test.domain.OrmHelper;

import de.wwu.muggl.jpa.ql.stmt.QLStatement;

public class QLAnalyzer {

	protected ParseTreeWalker walker;
	protected final ConsumerContextImpl consumerContext;

	public QLAnalyzer(Class<?>... entityClasses) {
		this.walker = new ParseTreeWalker();
		this.consumerContext = new ConsumerContextImpl(OrmHelper.buildDomainMetamodel(entityClasses));
	}
	
	public QLStatement getQLSelectStatement(String qlString, String resultClassName) {
		SqmStatement sqmStmt = getSqmStatement(qlString);
		if(sqmStmt instanceof SqmSelectStatement) {
			return new QLStatement((SqmSelectStatement)sqmStmt, resultClassName);
		}
		return null;
	}
	
	public QLStatement getQLSelectStatement(TypedQuery<?> typedQuery) {
//		SqmStatement sqmStmt = getSqmStatement(typedQuery);
		CriteriaQueryTypeQueryAdapter<?> c = (CriteriaQueryTypeQueryAdapter<?>)typedQuery;
		String qlString = c.getQueryString();
		SqmStatement sqmStmt = getSqmStatement(qlString);
		if(sqmStmt instanceof SqmSelectStatement) {
			return new QLStatement((SqmSelectStatement)sqmStmt, null);
		}
		return null;
	}
	
	
	
	
	
	public void analyze(String qlString) {
		SqmStatement sqmStmt = getSqmStatement(qlString);
		if(sqmStmt instanceof SqmSelectStatement) {
			analyzeSelectStatement((SqmSelectStatement)sqmStmt);
		}
	}
	
	protected void analyzeSelectStatement(SqmSelectStatement selectStmt) {
		Set<String> requiredEntityNames = analyzeFromClause(selectStmt.getQuerySpec().getFromClause());
		System.out.println(requiredEntityNames);
		analyzeWhereClause(selectStmt.getQuerySpec().getWhereClause());
	}
	

	private void analyzeWhereClause(SqmWhereClause whereClause) {
		System.out.println(whereClause);
		whereClause.getPredicate();
	}

	private Set<String> analyzeFromClause(SqmFromClause fromClause) {
		Set<String> entityNames = new HashSet<String>();
		for(SqmFromElementSpace fromElement : fromClause.getFromElementSpaces()) {
			entityNames.add(fromElement.getRoot().getEntityName());
		}
		return entityNames;
	}

	protected SqmStatement getSqmStatement(String query) {
		final HqlParser parser = HqlParseTreeBuilder.INSTANCE.parseHql(query);
		final ParsingContext parsingContext = new ParsingContext(consumerContext);
		return SemanticQueryBuilder.buildSemanticModel(parser.statement(), parsingContext);
	}

}
