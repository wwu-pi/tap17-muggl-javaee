//package de.wwu.muggl.vm.impl.jpa.db;
//
//import de.wwu.muggl.vm.classfile.structures.Field;
//import de.wwu.muggl.vm.initialization.Arrayref;
//import de.wwu.muggl.vm.initialization.InitializedClass;
//import de.wwu.muggl.vm.initialization.Objectref;
//
//public class DatabaseInspector {
//	
//	private static NewVirtualDatabase staticDB;
//	
//	public static void setLastDB(NewVirtualDatabase db) {
//		staticDB = db;
//	}
//	
//	public static void printLast() {
//		inspectDatabase(staticDB);
//	}
//
//	public static void inspectDatabase(NewVirtualDatabase db) {
//		System.out.println("Database: " + db);
//		for(String entity : db.getEntityMap().keySet()) {
//			System.out.println("Entity=" + entity);
//			for(EntityObjectEntry entry : db.getEntityMap().get(entity)) {
//				System.out.println(" " + entry.getName());
//				for(Field field : entry.getFields().keySet()) {
//					Object value = entry.getField(field);
//					System.out.println("   field=["+field.getName()+"]  =  value=["+value+"]");
//					if(value instanceof Objectref) {
//						Objectref objectRef = (Objectref)value;
//						InitializedClass initClass = objectRef.getInitializedClass();
//						if(initClass != null && initClass.getClassFile().getName().equals("java.util.ArrayList")) {
//							Field elementData = initClass.getClassFile().getFieldByName("elementData");
//							Object elementDataValue = objectRef.getField(elementData);
//							if(elementDataValue instanceof Arrayref) {
//								Arrayref arrayRef = (Arrayref)elementDataValue;
//								System.out.println("      Array with length=" + arrayRef.length);
//								for(int ia=0; ia<arrayRef.length; ia++) {
//									System.out.println("         " + (ia+1) +") " + arrayRef.getElement(ia));
//								}
//							}
//						}
//					}
//				}
//			}
//		}
//	}
//}
