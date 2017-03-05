package de.wwu.muggl.symbolic.jpa;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.NamedQuery;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.StoredProcedureQuery;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.metamodel.Metamodel;

//import org.broadleafcommerce.common.config.domain.AbstractModuleConfiguration;
//import org.broadleafcommerce.common.config.domain.SystemPropertyImpl;
//import org.broadleafcommerce.common.currency.domain.BroadleafCurrencyImpl;
//import org.broadleafcommerce.common.email.domain.EmailTrackingClicksImpl;
//import org.broadleafcommerce.common.email.domain.EmailTrackingImpl;
//import org.broadleafcommerce.common.email.domain.EmailTrackingOpensImpl;
//import org.broadleafcommerce.common.enumeration.domain.DataDrivenEnumerationImpl;
//import org.broadleafcommerce.common.enumeration.domain.DataDrivenEnumerationValueImpl;
//import org.broadleafcommerce.common.i18n.domain.ISOCountryImpl;
//import org.broadleafcommerce.common.i18n.domain.TranslationImpl;
//import org.broadleafcommerce.common.locale.domain.LocaleImpl;
//import org.broadleafcommerce.common.media.domain.MediaImpl;
//import org.broadleafcommerce.common.sandbox.domain.SandBoxImpl;
//import org.broadleafcommerce.common.sandbox.domain.SandBoxManagementImpl;
//import org.broadleafcommerce.common.site.domain.CatalogImpl;
//import org.broadleafcommerce.common.site.domain.SiteCatalogXrefImpl;
//import org.broadleafcommerce.common.site.domain.SiteImpl;
//import org.broadleafcommerce.common.sitemap.domain.CustomUrlSiteMapGeneratorConfigurationImpl;
//import org.broadleafcommerce.common.sitemap.domain.SiteMapConfigurationImpl;
//import org.broadleafcommerce.common.sitemap.domain.SiteMapGeneratorConfigurationImpl;
//import org.broadleafcommerce.common.sitemap.domain.SiteMapUrlEntryImpl;

import de.wwu.muggl.db.constraint.EntityConstraintAnalyzer;
import de.wwu.muggl.db.entry.DatabaseObject;
import de.wwu.muggl.jpa.MugglTypedQuery;
import de.wwu.muggl.jpa.criteria.MugglCriteriaQuery;
import de.wwu.muggl.jpa.criteria.meta.MugglJPA;
import de.wwu.muggl.jpa.ql.analyzer.QLAnalyzer;
import de.wwu.muggl.jpa.ql.stmt.QLStatement;
import de.wwu.muggl.solvers.expressions.AllDifferent;
import de.wwu.muggl.solvers.expressions.AllDifferentString;
import de.wwu.muggl.solvers.expressions.BooleanConstant;
import de.wwu.muggl.solvers.expressions.ConstraintExpression;
import de.wwu.muggl.solvers.expressions.Expression;
import de.wwu.muggl.solvers.expressions.GreaterOrEqual;
import de.wwu.muggl.solvers.expressions.GreaterThan;
import de.wwu.muggl.solvers.expressions.IntConstant;
import de.wwu.muggl.solvers.expressions.NumericConstant;
import de.wwu.muggl.solvers.expressions.NumericVariable;
import de.wwu.muggl.solvers.expressions.Term;
import de.wwu.muggl.solvers.expressions.array.IArrayref;
import de.wwu.muggl.symbolic.jpa.criteria.CriteriaBuilderWrapper;
import de.wwu.muggl.symbolic.jpa.criteria.CriteriaQueryWrapper;
import de.wwu.muggl.symbolic.jpa.gen.EntityReferenceGenerator;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.structures.Attribute;
import de.wwu.muggl.vm.classfile.structures.Field;
import de.wwu.muggl.vm.classfile.structures.attributes.AttributeRuntimeVisibleAnnotations;
import de.wwu.muggl.vm.classfile.structures.attributes.elements.Annotation;
import de.wwu.muggl.vm.exceptions.VmRuntimeException;
import de.wwu.muggl.vm.impl.jpa.JPAVirtualMachine;
import de.wwu.muggl.vm.impl.symbolic.SymbolicVirtualMachine;
import de.wwu.muggl.vm.initialization.Arrayref;
import de.wwu.muggl.vm.initialization.ObjectNullRef;
import de.wwu.muggl.vm.initialization.Objectref;
import de.wwu.muggl.vm.var.EntityObjectref;
import de.wwu.muggl.vm.var.ReferenceVariable;
import de.wwu.muggl.vm.var.SymbolicArrayref;
import de.wwu.muggl.vm.var.gen.SymoblicEntityFieldGenerator;

public class MugglEntityManager implements EntityManager, MugglJPA {

	private static final EntityManagerFactory emf = Persistence.createEntityManagerFactory("NONJTAPersistenceUnit");
	
	private JPAVirtualMachine vm;
	private EntityManager original;
	
	private QLAnalyzer hqlAnalyzer;
	private EntityReferenceGenerator entityRefGenerator;
	
	private Set<Class<?>> managedEntityClasses;
	
	public MugglTypedQuery<?> createQuery(MugglCriteriaQuery<?> criteriaQuery) {
		return new MugglTypedQuery<>(criteriaQuery);
	}	
	
	public Object createQuery(CriteriaQueryWrapper criteriaQueryWrapper) {
		TypedQuery typedQuery = this.original.createQuery(criteriaQueryWrapper.getOriginal());
		QLStatement qlStmt = hqlAnalyzer.getQLSelectStatement(typedQuery);
		
		int i=0;
		for(Objectref parameter : criteriaQueryWrapper.getSymbolicParameter()) {
			qlStmt.setParameter("param"+i++, parameter);
		}
		
		for(Object o : criteriaQueryWrapper.getOriginal().getParameters()) {
			if(o instanceof ParameterExpression) {
				ParameterExpression<?> paraExpr = (ParameterExpression<?>)o;
				paraExpr.getAlias();
				paraExpr.getName();
				
			}
		}
		return qlStmt;
	}
	
	public MugglEntityManager(JPAVirtualMachine vm) {
		this.vm = vm;
		this.entityRefGenerator = new EntityReferenceGenerator(this.vm);
		if(original == null) {
			loadEntityManager();
		}
		
		Class<?>[] managedClasses = {
				
//				de.wwu.pi.app.entities.Customer.class,
//				de.wwu.pi.app.entities.CustomerOrder.class,
//				de.wwu.pi.app.entities.OrderItem.class,
				
//				javaeetutorial.roster.entity.Player.class,
//				javaeetutorial.roster.entity.Player_.class,
//				javaeetutorial.roster.entity.League.class,
//				javaeetutorial.roster.entity.League_.class,
//				javaeetutorial.roster.entity.WinterLeague.class,
//				javaeetutorial.roster.entity.SummerLeague.class,
//				javaeetutorial.roster.entity.Team.class,
//				javaeetutorial.roster.entity.Team_.class,
				
//				org.jboss.as.quickstarts.greeter.domain.User.class,				

//				javaeetutorial.dukesbookstore.entity.Book.class,
				
//				javaeetutorial.order.entity.CustomerOrder.class,
//				javaeetutorial.order.entity.LineItem.class,
//				javaeetutorial.order.entity.Part.class,
//				javaeetutorial.order.entity.Vendor.class,
//				javaeetutorial.order.entity.VendorPart.class,

//				javaeetutorial.order.entity.CustomerOrder.class,
//				javaeetutorial.order.entity.LineItem.class,
//				javaeetutorial.order.entity.LineItemKey.class,
//				javaeetutorial.order.entity.Part.class,
//				javaeetutorial.order.entity.PartKey.class,
//				javaeetutorial.order.entity.Vendor.class,
//				javaeetutorial.order.entity.VendorPart.class,

//				de.unimuenster.pi.library.jpa.Book.class,
//				de.unimuenster.pi.library.jpa.CD.class,
//				de.unimuenster.pi.library.jpa.Copy.class,
//				de.unimuenster.pi.library.jpa.Loan.class,
//				de.unimuenster.pi.library.jpa.Medium.class,
//				de.unimuenster.pi.library.jpa.User.class,

//				javaeetutorial.dukestutoring.entity.Student.class,
//				javaeetutorial.dukestutoring.entity.PersonDetails.class,
//				javaeetutorial.dukestutoring.entity.TutoringSession.class,
//				javaeetutorial.dukestutoring.entity.Guardian.class,
//				javaeetutorial.dukestutoring.entity.StatusEntry.class,
//				javaeetutorial.dukestutoring.entity.Administrator.class,
//				javaeetutorial.dukestutoring.entity.Address.class
			};
				
		this.managedEntityClasses = new HashSet<>();
		for(Class<?> c : managedClasses) {
			this.managedEntityClasses.add(c);
		}
		
		this.hqlAnalyzer = new QLAnalyzer(managedClasses);
		
		
		
		
		// test:
//		String leagueId = "#";
//		CriteriaBuilder cb = this.original.getCriteriaBuilder();
//		CriteriaQuery<Player> cq = cb.createQuery(Player.class);
//		Root<Player> player = cq.from(Player.class);
//        Join<Player, Team> team = player.join(Player_.team);
//        Join<Team, League> league = team.join(Team_.league);
//        cq.where(cb.equal(league.get(League_.id), leagueId));
//        cq.select(player).distinct(true);
//        TypedQuery<Player> q = this.original.createQuery(cq);
//        q.getResultList();
//        
//        this.hqlAnalyzer.getQLSelectStatement(q);
	}
	
	public Set<Class<?>> getManagedEntityClasses() {
		return this.managedEntityClasses;
	}
	
	protected void loadEntityManager() {
		this.original = emf.createEntityManager();
	}
	
	public CriteriaBuilderWrapper getCriteriaWrapper() {
		return new CriteriaBuilderWrapper(this.original.getCriteriaBuilder()); 
	}
	
	public Object createQuery(Objectref jpqlObjectref) {
		if(jpqlObjectref.getObjectType().equals(String.class.getName())) {
			String jpqlString = getStringFromObjectref(jpqlObjectref);
			return hqlAnalyzer.getQLSelectStatement(jpqlString, null);
		}
		return null;
	}
	
	public Object createQuery(Objectref jpqlObjectref, Objectref resultClassObjectref) {
		if(jpqlObjectref.getObjectType().equals(String.class.getName()) && resultClassObjectref.getObjectType().equals(Class.class.getName())) {
			String resultClassName = getClassNameFromObjectref(resultClassObjectref);
			String jpqlString = getStringFromObjectref(jpqlObjectref);
			return hqlAnalyzer.getQLSelectStatement(jpqlString, resultClassName);
		}
		return null;
	}
	
	public void remove(EntityObjectref e) {
		Set<DatabaseObject> set = this.vm.getVirtualObjectDatabase().getData(e.getObjectType());
		for(DatabaseObject db : set) {
			if(db.getObjectId().equals(e.getObjectId())) {
				set.remove(db);
			}
		}
	}
	
	public void remove(ObjectNullRef e) throws VmRuntimeException {
		throw new VmRuntimeException(this.vm.generateExc("java.lang.IllegalArgumentException"));
	}
	
	public Object createNamedQuery(Objectref objRef) {
		String namedQuery = getStringFromObjectref(objRef);
		for(Class<?> entityClass : managedEntityClasses) {
			if(entityClass.isAnnotationPresent(NamedQuery.class)) {
				if(entityClass.getAnnotation(NamedQuery.class).name().equals(namedQuery)) {
					String jpqlQueryString = entityClass.getAnnotation(NamedQuery.class).query();
					return hqlAnalyzer.getQLSelectStatement(jpqlQueryString, null);
				}
			}
		}
		return null;
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
	
	public Objectref merge(EntityObjectref objectRef) {
		return merge((Objectref)objectRef);
	}
	
	public Objectref merge(ReferenceVariable objectRef) {
		return merge((Objectref)objectRef);
	}
	
	public Objectref merge(Objectref objectRef) {
		SymoblicEntityFieldGenerator entityFieldGenerator = new SymoblicEntityFieldGenerator(this.vm);
		
		EntityObjectref entityObjectref = new EntityObjectref(entityFieldGenerator, objectRef.getName(), objectRef);
		
		// set symbolic id field name
		if(!isGeneratedIdAttribute(entityObjectref)) {
			String idFieldName = EntityConstraintAnalyzer.getIdFieldName(entityObjectref.getObjectType());
			Field idField = entityObjectref.getInitializedClass().getClassFile().getFieldByName(idFieldName, true);
			Object idValue = entityObjectref.getField(idField);
			if(idValue instanceof NumericVariable) {
				this.vm.getSolverManager().addConstraint(GreaterOrEqual.newInstance((NumericVariable)idValue, NumericConstant.getZero(Expression.INT)));
			}
		}
		
		String entityName = entityObjectref.getObjectType();
		this.vm.getVirtualObjectDatabase().addEntityData(this.vm.getSolverManager(), entityName, entityObjectref);
		
		return entityObjectref; //  new EntityObjectref(entityFieldGenerator, objectRef.getName(), objectRef);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	private boolean isGeneratedIdAttribute(Objectref objectToCheck) {
		ClassFile classFile = objectToCheck.getInitializedClass().getClassFile();
		
		// check field annotations
		String idFieldName = EntityConstraintAnalyzer.getIdFieldName(classFile.getName());
		Field idField = classFile.getFieldByName(idFieldName, true);
		for(Attribute attribute : idField.getAttributes()) {
			AttributeRuntimeVisibleAnnotations attributeAnnotation = (AttributeRuntimeVisibleAnnotations) attribute;
			for(Annotation anno : attributeAnnotation.getAnnotations()) {
				if (anno.getClassFile().getConstantPool()[anno.getTypeIndex()].getStringValue().equals("Ljavax/persistence/GeneratedValue;")) {
					return true;
				}
			}
		}
		
		// check method annotations
		de.wwu.muggl.vm.classfile.structures.Method idMethod = null;
		String idMethodName = "get"+idField.getName();
		for(de.wwu.muggl.vm.classfile.structures.Method m : classFile.getMethods()) {
			if(m.getName().toUpperCase().equals(idMethodName.toUpperCase())) {
				idMethod = m;
				break;
			}
		}
		if(idMethod != null) {
			for(Attribute attribute : idMethod.getAttributes()) {
				if(attribute instanceof AttributeRuntimeVisibleAnnotations) {
					AttributeRuntimeVisibleAnnotations attributeAnnotation = (AttributeRuntimeVisibleAnnotations) attribute;
					for(Annotation anno : attributeAnnotation.getAnnotations()) {
						if (anno.getClassFile().getConstantPool()[anno.getTypeIndex()].getStringValue().equals("Ljavax/persistence/GeneratedValue;")) {
							return true;
						}
					}
				}
			}
		}
		
		return false;
	}
	
	
	

	
	@Deprecated // choice point is created in invokeinterface
	public void persist(Objectref dbObj) {
		// add object to the virtual database
		this.vm.getVirtualObjectDatabase().addEntityData(this.vm.getSolverManager(), dbObj.getInitializedClassName(), dbObj);
		
		// set unique value constraint for the ID field
		Set<DatabaseObject> entityData = this.vm.getVirtualObjectDatabase().getData(dbObj.getInitializedClassName());
		String idField = EntityConstraintAnalyzer.getIdFieldName(dbObj.getInitializedClassName());
		
		Set<Expression> uniqueIdValues = new HashSet<>();
		List<IArrayref> differentStrings = new ArrayList<>();
		
		for(DatabaseObject o : entityData) {
			Object idValue = o.valueMap().get(idField);
			
			if(idValue == null) {
				throw new RuntimeException("Could not add a null id field as DIFFERENT ALL");
			}
			
			// Numeric Variable as ID
			if(idValue instanceof NumericVariable) {
				uniqueIdValues.add((NumericVariable)idValue);
			}
			
			// String as ID
			else if(idValue instanceof Objectref
				&& ((Objectref)idValue).getInitializedClassName().equals("java.lang.String")) {
				Objectref refVar = (Objectref)idValue;
				Field valueField = refVar.getInitializedClass().getClassFile().getFieldByName("value");
				Object arrayRefObj = refVar.getField(valueField);
				if(arrayRefObj instanceof SymbolicArrayref) {
					SymbolicArrayref symArray = (SymbolicArrayref)arrayRefObj;
					differentStrings.add(symArray);
				} else {
					Arrayref arrayRef = (Arrayref)arrayRefObj;
					differentStrings.add(arrayRef);
				}
			}
			
			else {
				// implement other types
				throw new RuntimeException("Type " + idValue.getClass().getName() + " not handled yet as UNIQUE ID FIELD");
			}
		}
		
		if(differentStrings.size() > 1) {
			ConstraintExpression ce = new AllDifferentString(differentStrings.toArray(new IArrayref[differentStrings.size()]));
			((SymbolicVirtualMachine)vm).getSolverManager().addConstraint(ce);
		}
		
		// if there are at least two variables, set uniqueness constraint
		if(uniqueIdValues.size() > 1) {
			for(Expression expression : uniqueIdValues) {
				if(expression instanceof Term) {
					ConstraintExpression ce = GreaterThan.newInstance((Term)expression, NumericConstant.getZero(Expression.INT));
					if(ce instanceof BooleanConstant && ((BooleanConstant)ce).getValue()) {
						continue;
					}
					((SymbolicVirtualMachine)vm).getSolverManager().addConstraint(ce);
				}
			}
			((SymbolicVirtualMachine)vm).getSolverManager().addConstraint(
					new AllDifferent(uniqueIdValues));
		}
	}

	@Override
	public void persist(Object object) {
		if(object instanceof Objectref) {
			persist((Objectref)object);
		} else {
			throw new RuntimeException("Could not handle persist operation of: " + object);
		}
		
//		if(object instanceof Objectref) {
//			vm.getVirtualDatabase().addData((Objectref)object);
//		} else {
//			this.original.persist(object);
//		}
	}
	
	@Override
	public void remove(Object object) {
		if(object instanceof Objectref) {
			vm.getVirtualDatabase().removeData((Objectref)object);
		} else {
			this.original.remove(object);
		}
	}
	
	public void remove(ReferenceVariable object) {
		String entityName = object.getObjectType();
		Set<DatabaseObject> data = this.vm.getVirtualObjectDatabase().getData(entityName);
		System.out.println("obj ID = " + object.getObjectId());
		for(DatabaseObject dbObj : data) {
			if(object == dbObj || object.getObjectId().equals(dbObj.getObjectId())) {
				data.remove(dbObj);
			}
		}
		
		this.vm.getVirtualObjectDatabase().getPreExecutionRequiredData();
		this.vm.getVirtualObjectDatabase().getData();
		
		System.out.println("removeo its ..");
	}
	
	
	
	
	public ReferenceVariable find(Objectref classReference, ReferenceVariable entityIdReference) {
		String entityName = entityRefGenerator.getClassNameOfClassObjectRef(classReference);
		Set<DatabaseObject> entityDataSet = this.vm.getVirtualObjectDatabase().getData().get(entityName);
		if(entityDataSet == null) {
			return null; // no entity available in database
		}
		
		String idFieldName = EntityConstraintAnalyzer.getIdFieldName(entityName);
		
		for(DatabaseObject dbObj : entityDataSet) {
			Object idFieldValue = dbObj.valueMap().get(idFieldName);
			if(idFieldValue != null && idFieldValue.equals(entityIdReference)) {
				return (ReferenceVariable)dbObj;
			}
		}
		
		return null;
		
		
//		this.vm.generateEntityManagerFindChoicePoint(instruction, classReference.getInitializedClassName(), entityIdReference);
		
//		return null;
		
		
		
		
//		ReferenceVariable entityReference = entityRefGenerator.generateNewEntityReference(classReference);
//		String idFieldName = EntityConstraintAnalyzer.getIdFieldName(entityReference.getInitializedClassName());
//		Field idField = entityReference.getInitializedClass().getClassFile().getFieldByName(idFieldName);
//		entityReference.putField(idField, entityIdReference);
//		this.vm.getVirtualObjectDatabase().addPreExecutionRequiredData(entityReference.getInitializedClassName(), entityReference);
//		this.vm.getVirtualObjectDatabase().addEntityData(entityReference.getInitializedClassName(), entityReference);
//		return entityReference;
	}
	
	
	
	
	@Override
	public <T> T find(Class<T> arg0, Object arg1) {
		return this.original.find(arg0, arg1);
	}
	
	
	
	@Override
	public CriteriaBuilder getCriteriaBuilder() {
		throw new RuntimeException("use wrapper");
//		return new CriteriaBuilderWrapper(this.original.getCriteriaBuilder());	
		
		
//		this.original.getMetamodel().getEntities();
//		
//		CriteriaQuery cq = this.original.getCriteriaBuilder().createQuery();
//		Root<Customer> c = cq.from(Customer.class);
//		cq.select(c);
//		Query q = this.original.createQuery(cq);
//		
//		String ql = ((org.hibernate.query.criteria.internal.compile.CriteriaQueryTypeQueryAdapter)q).getQueryString();
//		hqlAnalyzer.getQLSelectStatement(ql, null);
//		
//		this.original.getCriteriaBuilder().createQuery(Customer.class).from(Customer.class);
		
		
		
		
//		return new MugglCriteriaBuilder();
//		return new MugglCriteriaBuilder(this.original.getCriteriaBuilder());
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	// simply passing to original...
	
	@Override
	public void clear() {
		this.original.clear();
	}

	@Override
	public void close() {
		this.original.close();
	}

	@Override
	public boolean contains(Object arg0) {
		return this.original.contains(arg0);
	}

	@Override
	public <T> EntityGraph<T> createEntityGraph(Class<T> arg0) {
		return this.original.createEntityGraph(arg0);
	}

	@Override
	public EntityGraph<?> createEntityGraph(String arg0) {
		return this.original.createEntityGraph(arg0);
	}

	@Override
	public Query createNamedQuery(String arg0) {
		return this.original.createNamedQuery(arg0);
	}

	@Override
	public <T> TypedQuery<T> createNamedQuery(String arg0, Class<T> arg1) {
		return this.original.createNamedQuery(arg0, arg1);
	}

	@Override
	public StoredProcedureQuery createNamedStoredProcedureQuery(String arg0) {
		return this.original.createNamedStoredProcedureQuery(arg0);
	}

	@Override
	public Query createNativeQuery(String arg0) {
		return this.original.createNativeQuery(arg0);
		
	}

	@Override
	public Query createNativeQuery(String arg0, Class arg1) {
		return this.original.createNativeQuery(arg0, arg1);
		
	}

	@Override
	public Query createNativeQuery(String arg0, String arg1) {
		return this.original.createNativeQuery(arg0, arg1);
		
	}

	@Override
	public Query createQuery(String arg0) {
		return this.original.createQuery(arg0);
		
	}

	@Override
	public <T> TypedQuery<T> createQuery(CriteriaQuery<T> arg0) {
		return this.original.createQuery(arg0);
		
	}

	@Override
	public Query createQuery(CriteriaUpdate arg0) {
		return this.original.createQuery(arg0);
		
	}

	@Override
	public Query createQuery(CriteriaDelete arg0) {
		return this.original.createQuery(arg0);
		
	}

	@Override
	public <T> TypedQuery<T> createQuery(String arg0, Class<T> arg1) {
		return this.original.createQuery(arg0, arg1);
		
	}

	@Override
	public StoredProcedureQuery createStoredProcedureQuery(String arg0) {
		return this.original.createStoredProcedureQuery(arg0);
		
	}

	@Override
	public StoredProcedureQuery createStoredProcedureQuery(String arg0, Class... arg1) {
		return this.original.createStoredProcedureQuery(arg0, arg1);
		
	}

	@Override
	public StoredProcedureQuery createStoredProcedureQuery(String arg0,	String... arg1) {
		return this.original.createStoredProcedureQuery(arg0, arg1);
		
	}

	@Override
	public void detach(Object arg0) {
		this.original.detach(arg0);
		
	}

	@Override
	public <T> T find(Class<T> arg0, Object arg1, Map<String, Object> arg2) {
		return this.original.find(arg0, arg1, arg2);
		
	}

	@Override
	public <T> T find(Class<T> arg0, Object arg1, LockModeType arg2) {
		return this.original.find(arg0, arg1, arg2);
		
	}

	@Override
	public <T> T find(Class<T> arg0, Object arg1, LockModeType arg2, Map<String, Object> arg3) {
		return this.original.find(arg0, arg1, arg2, arg3);
		
	}

	@Override
	public void flush() {
		this.original.flush();
		
	}



	@Override
	public Object getDelegate() {
		return this.original.getDelegate();
		
	}

	@Override
	public EntityGraph<?> getEntityGraph(String arg0) {
		return this.original.getEntityGraph(arg0);
		
	}

	@Override
	public <T> List<EntityGraph<? super T>> getEntityGraphs(Class<T> arg0) {
		return this.original.getEntityGraphs(arg0);
		
	}

	@Override
	public EntityManagerFactory getEntityManagerFactory() {
		return this.original.getEntityManagerFactory();
		
	}

	@Override
	public FlushModeType getFlushMode() {
		return this.original.getFlushMode();
		
	}

	@Override
	public LockModeType getLockMode(Object arg0) {
		return this.original.getLockMode(arg0);
		
	}

	@Override
	public Metamodel getMetamodel() {
		return this.original.getMetamodel();
		
	}

	@Override
	public Map<String, Object> getProperties() {
		return this.original.getProperties();
		
	}

	@Override
	public <T> T getReference(Class<T> arg0, Object arg1) {
		return this.original.getReference(arg0, arg1);
		
	}

	@Override
	public EntityTransaction getTransaction() {
		return this.original.getTransaction();
		
	}

	@Override
	public boolean isJoinedToTransaction() {
		return this.original.isJoinedToTransaction();
	}

	@Override
	public boolean isOpen() {
		return  this.original.isOpen();
	}

	@Override
	public void joinTransaction() {
		this.original.joinTransaction();
	}

	@Override
	public void lock(Object arg0, LockModeType arg1) {
		this.original.lock(arg0, arg1);
		
	}

	@Override
	public void lock(Object arg0, LockModeType arg1, Map<String, Object> arg2) {
		this.original.lock(arg0, arg1, arg2);
		
	}

	@Override
	public <T> T merge(T arg0) {
		return this.original.merge(arg0);
		
	}

	@Override
	public void refresh(Object arg0) {
		this.original.refresh(arg0);
		
	}

	@Override
	public void refresh(Object arg0, Map<String, Object> arg1) {
		this.original.refresh(arg0, arg1);
		
	}

	@Override
	public void refresh(Object arg0, LockModeType arg1) {
		this.original.refresh(arg0, arg1);
		
	}

	@Override
	public void refresh(Object arg0, LockModeType arg1,	Map<String, Object> arg2) {
		this.original.refresh(arg0, arg1, arg2);
		
	}


	@Override
	public void setFlushMode(FlushModeType arg0) {
		this.original.setFlushMode(arg0);
		
	}

	@Override
	public void setProperty(String arg0, Object arg1) {
		this.original.setProperty(arg0, arg1);
		
	}

	@Override
	public <T> T unwrap(Class<T> arg0) {
		return this.original.unwrap(arg0);
		
	}

}
