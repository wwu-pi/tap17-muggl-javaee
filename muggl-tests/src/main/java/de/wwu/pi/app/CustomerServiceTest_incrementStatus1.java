package de.wwu.pi.app;

import static org.junit.Assert.*;

import javax.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.wwu.javaee.test.runner.MugglExceptionMatcher;
/**
 * This class has been generated by Muggl for the automated testing of method
 * de.wwu.pi.app.ejb.CustomerService.incrementStatus(int s, java.util.Date d, long r).
 * Test cases have been computed using the symbolic execution of Muggl. Muggl
 * is a tool for the fully automated generation of test cases by analysing a
 * program's byte code. It aims at testing any possible flow through the program's
 * code rather than "guessing" required test cases, as a human would do.
 * Refer to http://www.wi.uni-muenster.de/pi/personal/majchrzak.php for more
 * information or contact the author at tim.majchrzak@wi.uni-muenster.de.
 * 
 * Executing the method main(null) will invoke JUnit (if it is in the class path).
 * The methods for setting up the test and for running the tests have also been
 * annotated.
 * 
 * Important settings for this run:
 * Search algorithm:            iterative deepening depth first
 * Time Limit:                  1 hours
 * Maximum loop cycles to take: 200
 * Maximum instructions before
 * finding a new solution:     infinite
 * Solver:                     de.wwu.muggl.solvers.jacop.JaCoPSolverManager
 * 
 * The total number of solutions found was 3. After deleting redundancy and
 * removing unnecessary solutions, 3 distinct test cases were found.
 * There was no further reduction of test cases.
 * 
 * This file has been generated on Wednesday, 01 March, 2017 10:56.
 * 
 * @author Muggl 1.00 Alpha (unreleased)
 */
@RunWith(Arquillian.class)
public class CustomerServiceTest_incrementStatus1 extends AbstractCustomerServiceTest {
	@Inject
	private de.wwu.pi.app.ejb.CustomerService testedClass;

	@Deployment
	public static Archive<?> createDeployment() {
		return ShrinkWrap.create(WebArchive.class, "test.war")
			.addClasses(requiredApplicationClasses)
			.addAsResource("META-INF/persistence.xml")
			.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
	}


	/**
	 * Set up the unit test by initializing the fields to the desired values.
	 */
	@Before public void setUp() {
	}
	/**
	 * Run the tests on de.wwu.pi.app.ejb.CustomerService.incrementStatus(int s, java.util.Date d, long r).
	 */
	@Test public void testIncrementStatus1() throws Exception { 
		// pre-execution required data 
		utx.begin();
		em.joinTransaction();
		utx.commit();
		em.clear();

		// generate method arguments
		int arg1 = 0;
		java.util.Date arg2 = new java.util.Date(0);
		long arg3 = 0L;
		/** SOLUTION 
		{SymbolicQueryResultList#1202392516#idx-0.elementData.length=0, SymbolicQueryResultList#1202392516.length=0, d.fastTime=0}
		  ***********************************/ 
		this.testedClass.incrementStatus(arg1, arg2, arg3); // In this solution, not all parameters are used (long r is unused)., long r is unused).
		// expect 0 new entries in database:
		// check post-execution database state 
	} 
 
	@Test public void testIncrementStatus2() throws Exception { 
		// pre-execution required data 
		utx.begin();
		em.joinTransaction();
		// generate object with id: de.wwu.pi.app.entities.CustomerOrder157
		de.wwu.pi.app.entities.CustomerOrder customerOrder1 = new de.wwu.pi.app.entities.CustomerOrder();// object id: de.wwu.pi.app.entities.CustomerOrder157
		java.util.Date date1 = new java.util.Date(0);// object id: java.util.Date165
		customerOrder1.setOrderDate(date1);
		de.wwu.pi.app.entities.Customer customer1 = new de.wwu.pi.app.entities.Customer();// object id: de.wwu.pi.app.entities.Customer159
		customer1.setName("#");
		customer1.setId("#");
		customer1.setStatus(0);
		customerOrder1.setCustomer(customer1);
		// generate object with id: de.wwu.pi.app.entities.Customer159
		// generate object with id: de.wwu.pi.app.entities.OrderItem153
		de.wwu.pi.app.entities.OrderItem orderItem1 = new de.wwu.pi.app.entities.OrderItem();// object id: de.wwu.pi.app.entities.OrderItem153
		orderItem1.setPrice(1);
		orderItem1.setName("#");
		orderItem1.setOrder(customerOrder1);
		em.persist(customerOrder1);
		em.persist(orderItem1);
		em.persist(customer1);
		utx.commit();
		em.clear();

		// generate method arguments
		int arg1 = -50;
		java.util.Date arg2 = new java.util.Date(0);
		long arg3 = -50L;
		/** SOLUTION 
		{DATA#SymbolicQuery.158.element#0.id.value.element.0=35, DATA#SymbolicQuery.158.element#0.id.value.length=1, DATA#SymbolicQuery.158.element#0.name.value.element.0=35, DATA#SymbolicQuery.158.element#0.name.value.length=1, DATA#SymbolicQuery.158.element#0.status=0, SymbolicQuery.152.element#0.name.value.length=1, SymbolicQuery.152.element#0.price=1, SymbolicQuery.156.element#0.orderDate.fastTime=0, SymbolicQuery.158.element#0.id.value.element.0=35, SymbolicQuery.158.element#0.id.value.length=1, SymbolicQuery.158.element#0.id.value.length=1, SymbolicQuery.158.element#0.name.value.element.0=35, SymbolicQuery.158.element#0.name.value.length=1, SymbolicQuery.158.element#0.name.value.length=1, SymbolicQuery.158.element#0.status=0, SymbolicQuery.158.element#0.status=0, SymbolicQueryResultList#1202392516#idx-0.elementData.length=1, SymbolicQueryResultList#1202392516.length=1, d.fastTime=0, r=-50, s=-50}
		  ***********************************/ 
		this.testedClass.incrementStatus(arg1, arg2, arg3);
		// expect 0 new entries in database:
		// check post-execution database state 
		de.wwu.pi.app.entities.Customer entity166_db = (de.wwu.pi.app.entities.Customer)em.find(de.wwu.pi.app.entities.Customer.class, customer1.getId());
		assertNotNull(entity166_db);
		assertEquals("#", entity166_db.getName());
		assertEquals("#", entity166_db.getId());
		assertEquals(0, entity166_db.getStatus());
	} 
 
	@Test public void testIncrementStatus3() throws Exception { 
		// pre-execution required data 
		utx.begin();
		em.joinTransaction();
		// generate object with id: de.wwu.pi.app.entities.CustomerOrder157
		de.wwu.pi.app.entities.CustomerOrder customerOrder1 = new de.wwu.pi.app.entities.CustomerOrder();// object id: de.wwu.pi.app.entities.CustomerOrder157
		java.util.Date date1 = new java.util.Date(0);// object id: java.util.Date165
		customerOrder1.setOrderDate(date1);
		de.wwu.pi.app.entities.Customer customer1 = new de.wwu.pi.app.entities.Customer();// object id: de.wwu.pi.app.entities.Customer159
		customer1.setName("#");
		customer1.setId("#");
		customer1.setStatus(0);
		customerOrder1.setCustomer(customer1);
		// generate object with id: de.wwu.pi.app.entities.Customer159
		// generate object with id: de.wwu.pi.app.entities.OrderItem153
		de.wwu.pi.app.entities.OrderItem orderItem1 = new de.wwu.pi.app.entities.OrderItem();// object id: de.wwu.pi.app.entities.OrderItem153
		orderItem1.setPrice(1);
		orderItem1.setName("#");
		orderItem1.setOrder(customerOrder1);
		em.persist(customerOrder1);
		em.persist(orderItem1);
		em.persist(customer1);
		utx.commit();
		em.clear();

		// generate method arguments
		int arg1 = 1;
		java.util.Date arg2 = new java.util.Date(0);
		long arg3 = -50L;
		/** SOLUTION 
		{DATA#SymbolicQuery.158.element#0.id.value.element.0=35, DATA#SymbolicQuery.158.element#0.id.value.length=1, DATA#SymbolicQuery.158.element#0.name.value.element.0=35, DATA#SymbolicQuery.158.element#0.name.value.length=1, DATA#SymbolicQuery.158.element#0.status=0, SymbolicQuery.152.element#0.name.value.length=1, SymbolicQuery.152.element#0.price=1, SymbolicQuery.156.element#0.orderDate.fastTime=0, SymbolicQuery.158.element#0.id.value.element.0=35, SymbolicQuery.158.element#0.id.value.length=1, SymbolicQuery.158.element#0.id.value.length=1, SymbolicQuery.158.element#0.name.value.element.0=35, SymbolicQuery.158.element#0.name.value.length=1, SymbolicQuery.158.element#0.name.value.length=1, SymbolicQuery.158.element#0.status=0, SymbolicQuery.158.element#0.status=0, SymbolicQueryResultList#1202392516#idx-0.elementData.length=1, SymbolicQueryResultList#1202392516.length=1, d.fastTime=0, r=-50, s=1}
		  ***********************************/ 
		this.testedClass.incrementStatus(arg1, arg2, arg3);
		// expect 0 new entries in database:
		// check post-execution database state 
		de.wwu.pi.app.entities.Customer entity166_db = (de.wwu.pi.app.entities.Customer)em.find(de.wwu.pi.app.entities.Customer.class, customer1.getId());
		assertNotNull(entity166_db);
		assertEquals("#", entity166_db.getName());
		assertEquals("#", entity166_db.getId());
		assertEquals(1, entity166_db.getStatus());
	} 
 
}