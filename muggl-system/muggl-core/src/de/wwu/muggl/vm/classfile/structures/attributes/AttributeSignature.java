package de.wwu.muggl.vm.classfile.structures.attributes;

import java.io.IOException;

import de.wwu.muggl.configuration.Globals;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.ClassFileException;
import de.wwu.muggl.vm.classfile.structures.Attribute;

public class AttributeSignature extends Attribute {

	private byte[] bytes;
	
	public AttributeSignature(ClassFile classFile, int attributeNameIndex) throws ClassFileException, IOException {
		super(classFile, attributeNameIndex);
		this.bytes = new byte[this.attributeLength];
		for (int a = 0; a < this.attributeLength; a++) {
			this.bytes[a] = classFile.getDis().readByte();
		}
		if (Globals.getInst().logger.isTraceEnabled()) Globals.getInst().logger.trace("Parsing: Read the byte of an unknown attribute");
	}

	public byte[] getBytes() {
		return this.bytes;
	}

	@Override
	public String getStructureName() {
		return "(Signature attribute)";
	}

	public byte getSignatureIndex() {
		return this.bytes[1];
	}

}
