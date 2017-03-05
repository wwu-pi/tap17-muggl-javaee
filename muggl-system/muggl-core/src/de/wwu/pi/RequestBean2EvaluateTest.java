//package de.wwu.pi;
//
//import java.lang.reflect.Field;
//import java.lang.reflect.Modifier;
//
//public class RequestBean2EvaluateTest {
//
//	public RequestBean2EvaluateTest() {
//		testedClass = new de.wwu.pi.RequestBean2Evaluate();
//	}
//	
//	private de.wwu.pi.RequestBean2Evaluate testedClass;
//	private de.wwu.pi.entity.User user1;
//	private de.wwu.pi.entity.User user2;
//	
//	public void setUp() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
//		Field modifiersField = Field.class.getDeclaredField("modifiers");
//		modifiersField.setAccessible(true);
//		
//		this.user1 = new de.wwu.pi.entity.User();
//		java.lang.String user1Field_name = new java.lang.String();
//		char[] user1_name_value = new char[0];
//		java.lang.reflect.Field user1Field_nameField_value = java.lang.String.class.getDeclaredField("value");
//		user1Field_nameField_value.setAccessible(true);
//		modifiersField.setInt(user1Field_nameField_value, user1Field_nameField_value.getModifiers() & ~Modifier.FINAL);
//		user1Field_nameField_value.set(user1Field_name, user1_name_value);
//		java.lang.reflect.Field user1Field_nameField = de.wwu.pi.entity.User.class.getDeclaredField("name");
//		user1Field_nameField.setAccessible(true);
//		user1Field_nameField.set(this.user1, user1Field_name);
//		
//		this.user2 = new de.wwu.pi.entity.User();
//		java.lang.String user2Field_name = new java.lang.String();
//		char[] user2_name_value = new char[3];
//		user2_name_value[0] = 'f';
//		user2_name_value[1] = 'o';
//		user2_name_value[2] = 'o';
//		java.lang.reflect.Field user2Field_nameField_value = java.lang.String.class.getDeclaredField("value");
//		user2Field_nameField_value.setAccessible(true);
//		modifiersField.setInt(user2Field_nameField_value, user2Field_nameField_value.getModifiers() & ~Modifier.FINAL);
//		user2Field_nameField_value.set(user2Field_name, user2_name_value);
//		
//		java.lang.reflect.Field user2Field_nameField = de.wwu.pi.entity.User.class.getDeclaredField("name");
//		user2Field_nameField.setAccessible(true);
//		user2Field_nameField.set(this.user2, user2Field_name);
//	}
//	
//	public static void main(String[] args) throws Exception {
//		RequestBean2EvaluateTest m = new RequestBean2EvaluateTest();
//		m.setUp();
//		boolean b1 = m.test01();
//		boolean b2 = m.test02();
//		System.out.println("b1=" + b1);
//		System.out.println("b2=" + b2);
//	}
//	
//	public boolean test01() {
//		return this.testedClass.doUserTest(this.user1);
//	}
//	
//	
//	public boolean test02() {
//		return this.testedClass.doUserTest(this.user2);
//	}
//
//}
