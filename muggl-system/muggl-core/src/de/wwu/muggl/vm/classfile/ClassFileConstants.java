package de.wwu.muggl.vm.classfile;

public final class ClassFileConstants {

	// Constants for the constant_pool.
	// openjdk equivalence roughly in openjdk/jdk/src/share/javavm/export/classfile_constants.h
	// and openjdk/hotspot/src/share/vm/prims/jvm.h

	// FIXME mxs: would make sense to move the ClassFile Constants here to have a shorter file over there.

	public static enum ReferenceKind {
		REF_getField(1), REF_getStatic(2), REF_putField(3), REF_putStatic(4), REF_invokeVirtual(5), REF_invokeStatic(
				6), REF_invokeSpecial(7), REF_newInvokeSpecial(8), REF_invokeInterface(9);

		private final int val;

		ReferenceKind(int idx) {
			this.val = idx;
		}
		
		public int val() {
			return val;
		}

		public static ReferenceKind valueOf(int value) {
			for (ReferenceKind e : ReferenceKind.values()) {
				if (e.val == value) {
					return e;
				}
			}
			return null;// not found
		}

		public String toString() {
			return super.toString().replaceAll("REF_", "").toLowerCase();
		}
	}
}