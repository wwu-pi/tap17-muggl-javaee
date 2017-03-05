package de.wwu.muggl.vm.classfile.support;

import de.wwu.muggl.configuration.Globals;
import de.wwu.muggl.configuration.Options;
import de.wwu.muggl.instructions.InvalidInstructionInitialisationException;
import de.wwu.muggl.instructions.bytecode.*;
import de.wwu.muggl.instructions.interfaces.Instruction;
import de.wwu.muggl.instructions.jpa.JPACheckcast;
import de.wwu.muggl.instructions.jpa.JPAInvokeInterface;
import de.wwu.muggl.instructions.jpa.JPAInvokevirtual;
import de.wwu.muggl.instructions.replaced.replacer.InvokestaticReplacer;
import de.wwu.muggl.instructions.replaced.replacer.Ldc2_wReplacer;
import de.wwu.muggl.instructions.replaced.replacer.LdcReplacer;
import de.wwu.muggl.instructions.replaced.replacer.Ldc_wReplacer;
import de.wwu.muggl.vm.classfile.structures.attributes.AttributeCode;

/**
 * The ByteCodeParser offers static methods for the parsing on unsigned byes (represented by
 * shorts here). It supplies back an Instruction holding initialized object of the instruction
 * read from the bytecode.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-10
 */
public class BytecodeParser {

	/**
	 * Protected default constructor.
	 */
	protected BytecodeParser() {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Parses an unsigned byte to an Instruction without a look at the code and while
	 * ignoring the current line. This might lead to problems with a couple of the
	 * instructions. Especially loading of further bytes is not possible (many
	 * instructions have one or more additional bytes).
	 *
	 * @param byteCodeInstruction The unsigned byte as a short.
	 * @return A properly initialized Instruction.
	 * @throws InvalidInstructionInitialisationException Any fatal problems with the parsing and the initialization will lead to this exception.
	 */
	public static Instruction parse(short byteCodeInstruction) throws InvalidInstructionInitialisationException {
		return parse(byteCodeInstruction, null, 0);
	}

	/**
	 * Parses an unsigned byte to an Instruction, supplying further needed data to the
	 * classes for the instructions.
	 *
	 * @param byteCodeInstruction The unsigned byte as a short.
	 * @param code The AttributeCode this instruction is taken from.
	 * @param currentLine The "line" in the method this instruction is at.
	 * @return A properly initialized Instruction.
	 * @throws InvalidInstructionInitialisationException Any fatal problems with the parsing and the initialization will lead to this exception.
	 */
	public static Instruction parse(short byteCodeInstruction, AttributeCode code, int currentLine) throws InvalidInstructionInitialisationException {
		boolean optimize = Options.getInst().dynReplaceInstWithOptimizedOnes;

		try {
			switch (byteCodeInstruction) {
				case 0x00: return new Nop();
				case 0x01: return new AConst_Null();
				case 0x02: return new IConst(-1);
				case 0x03: return new IConst(0);
				case 0x04: return new IConst(1);
				case 0x05: return new IConst(2);
				case 0x06: return new IConst(3);
				case 0x07: return new IConst(4);
				case 0x08: return new IConst(5);
				case 0x09: return new LConst(0L);
				case 0x0a: return new LConst(1L);
				case 0x0b: return new FConst(0F);
				case 0x0c: return new FConst(1F);
				case 0x0d: return new FConst(2F);
				case 0x0e: return new DConst(0D);
				case 0x0f: return new DConst(1D);
				case 0x10: return new BiPush(code);
				case 0x11: return new SiPush(code);
				case 0x12: return optimize ? new LdcReplacer(code) : new Ldc(code);
				case 0x13: return optimize ? new Ldc_wReplacer(code) : new Ldc_w(code);
				case 0x14: return optimize ? new Ldc2_wReplacer(code) : new Ldc2_w(code);
				case 0x15: return new ILoad(code, 255);
				case 0x16: return new LLoad(code, 255);
				case 0x17: return new FLoad(code, 255);
				case 0x18: return new DLoad(code, 255);
				case 0x19: return new ALoad(code, 255);
				case 0x1a: return new ILoad(code, 0);
				case 0x1b: return new ILoad(code, 1);
				case 0x1c: return new ILoad(code, 2);
				case 0x1d: return new ILoad(code, 3);
				case 0x1e: return new LLoad(code, 0);
				case 0x1f: return new LLoad(code, 1);
				case 0x20: return new LLoad(code, 2);
				case 0x21: return new LLoad(code, 3);
				case 0x22: return new FLoad(code, 0);
				case 0x23: return new FLoad(code, 1);
				case 0x24: return new FLoad(code, 2);
				case 0x25: return new FLoad(code, 3);
				case 0x26: return new DLoad(code, 0);
				case 0x27: return new DLoad(code, 1);
				case 0x28: return new DLoad(code, 2);
				case 0x29: return new DLoad(code, 3);
				case 0x2a: return new ALoad(code, 0);
				case 0x2b: return new ALoad(code, 1);
				case 0x2c: return new ALoad(code, 2);
				case 0x2d: return new ALoad(code, 3);
				case 0x2e: return new IAload();
				case 0x2f: return new LAload();
				case 0x30: return new FAload();
				case 0x31: return new DAload();
				case 0x32: return new AAload();
				case 0x33: return new BAload();
				case 0x34: return new CAload();
				case 0x35: return new SAload();
				case 0x36: return new IStore(code, 255);
				case 0x37: return new LStore(code, 255);
				case 0x38: return new FStore(code, 255);
				case 0x39: return new DStore(code, 255);
				case 0x3a: return new AStore(code, 255);
				case 0x3b: return new IStore(code, 0);
				case 0x3c: return new IStore(code, 1);
				case 0x3d: return new IStore(code, 2);
				case 0x3e: return new IStore(code, 3);
				case 0x3f: return new LStore(code, 0);
				case 0x40: return new LStore(code, 1);
				case 0x41: return new LStore(code, 2);
				case 0x42: return new LStore(code, 3);
				case 0x43: return new FStore(code, 0);
				case 0x44: return new FStore(code, 1);
				case 0x45: return new FStore(code, 2);
				case 0x46: return new FStore(code, 3);
				case 0x47: return new DStore(code, 0);
				case 0x48: return new DStore(code, 1);
				case 0x49: return new DStore(code, 2);
				case 0x4a: return new DStore(code, 3);
				case 0x4b: return new AStore(code, 0);
				case 0x4c: return new AStore(code, 1);
				case 0x4d: return new AStore(code, 2);
				case 0x4e: return new AStore(code, 3);
				case 0x4f: return new IAstore();
				case 0x50: return new LAstore();
				case 0x51: return new FAstore();
				case 0x52: return new DAstore();
				case 0x53: return new AAstore();
				case 0x54: return new BAstore();
				case 0x55: return new CAstore();
				case 0x56: return new SAstore();
				case 0x57: return new Pop();
				case 0x58: return new Pop2();
				case 0x59: return new Dup();
				case 0x5a: return new Dup_x1();
				case 0x5b: return new Dup_x2();
				case 0x5c: return new Dup2();
				case 0x5d: return new Dup2_x1();
				case 0x5e: return new Dup2_x2();
				case 0x5f: return new Swap();
				case 0x60: return new IAdd();
				case 0x61: return new LAdd();
				case 0x62: return new FAdd();
				case 0x63: return new DAdd();
				case 0x64: return new ISub();
				case 0x65: return new LSub();
				case 0x66: return new FSub();
				case 0x67: return new DSub();
				case 0x68: return new IMul();
				case 0x69: return new LMul();
				case 0x6a: return new FMul();
				case 0x6b: return new DMul();
				case 0x6c: return new IDiv();
				case 0x6d: return new LDiv();
				case 0x6e: return new FDiv();
				case 0x6f: return new DDiv();
				case 0x70: return new IRem();
				case 0x71: return new LRem();
				case 0x72: return new FRem();
				case 0x73: return new DRem();
				case 0x74: return new INeg();
				case 0x75: return new LNeg();
				case 0x76: return new FNeg();
				case 0x77: return new DNeg();
				case 0x78: return new Ishl();
				case 0x79: return new Lshl();
				case 0x7a: return new Ishr();
				case 0x7b: return new Lshr();
				case 0x7c: return new Iushr();
				case 0x7d: return new Lushr();
				case 0x7e: return new IAnd();
				case 0x7f: return new LAnd();
				case 0x80: return new IOr();
				case 0x81: return new LOr();
				case 0x82: return new IXor();
				case 0x83: return new LXor();
				case 0x84: return new Iinc(code);
				case 0x85: return new I2l();
				case 0x86: return new I2f();
				case 0x87: return new I2d();
				case 0x88: return new L2i();
				case 0x89: return new L2f();
				case 0x8a: return new L2d();
				case 0x8b: return new F2i();
				case 0x8c: return new F2l();
				case 0x8d: return new F2d();
				case 0x8e: return new D2i();
				case 0x8f: return new D2l();
				case 0x90: return new D2f();
				case 0x91: return new I2b();
				case 0x92: return new I2c();
				case 0x93: return new I2s();
				case 0x94: return new LCmp();
				case 0x95: return new FCmpl();
				case 0x96: return new FCmpg();
				case 0x97: return new DCmpl();
				case 0x98: return new DCmpg();
				case 0x99: return new Ifeq(code, currentLine);
				case 0x9a: return new Ifne(code, currentLine);
				case 0x9b: return new Iflt(code, currentLine);
				case 0x9c: return new Ifge(code, currentLine);
				case 0x9d: return new Ifgt(code, currentLine);
				case 0x9e: return new Ifle(code, currentLine);
				case 0x9f: return new If_icmpeq(code, currentLine);
				case 0xa0: return new If_icmpne(code, currentLine);
				case 0xa1: return new If_icmplt(code, currentLine);
				case 0xa2: return new If_icmpge(code, currentLine);
				case 0xa3: return new If_icmpgt(code, currentLine);
				case 0xa4: return new If_icmple(code, currentLine);
				case 0xa5: return new If_acmpeq(code, currentLine);
				case 0xa6: return new If_acmpne(code, currentLine);
				case 0xa7: return new Goto(code);
				case 0xa8: return new Jsr(code);
				case 0xa9: return new Ret(code);
				case 0xaa: return new Tableswitch(code, currentLine);
				case 0xab: return new Lookupswitch(code, currentLine);
				case 0xac: return new IReturn();
				case 0xad: return new LReturn();
				case 0xae: return new FReturn();
				case 0xaf: return new DReturn();
				case 0xb0: return new AReturn();
				case 0xb1: return new Return();
				case 0xb2: return new Getstatic(code);
				case 0xb3: return new Putstatic(code);
				case 0xb4: return new Getfield(code);
				case 0xb5: return new Putfield(code);
				case 0xb6: return Options.getInst().jpaMode ? new JPAInvokevirtual(code) : new Invokevirtual(code);
				case 0xb7: return new Invokespecial(code);
				case 0xb8: return optimize ? new InvokestaticReplacer(code) : new Invokestatic(code);
				case 0xb9: return Options.getInst().jpaMode ? new JPAInvokeInterface(code) : new Invokeinterface(code);
				case 0xba: return new Xxxunusedxxx();
				case 0xbb: return new New(code);
				case 0xbc: return new Newarray(code);
				case 0xbd: return new Anewarray(code);
				case 0xbe: return new Arraylength();
				case 0xbf: return new Athrow();
				case 0xc0: return Options.getInst().jpaMode ? new JPACheckcast(code) : new Checkcast(code);
				case 0xc1: return new Instanceof(code);
				case 0xc2: return new Monitorenter();
				case 0xc3: return new Monitorexit();
				case 0xc4: return new Wide(code);
				case 0xc5: return new Multianewarray(code);
				case 0xc6: return new Ifnull(code, currentLine);
				case 0xc7: return new Ifnonnull(code, currentLine);
				case 0xc8: return new Goto_w(code);
				case 0xca:
					if (Globals.getInst().logger.isDebugEnabled())
						Globals.getInst().logger.debug("Parsing opcodes: Encountered unexpected opcode 202 (0Xca). This breakpoint opcode is not meant to be found in valid class files. It will be loaded gracefully, though.");
					return new Breakpoint();
				case 0xfe:
					if (Globals.getInst().logger.isDebugEnabled())
						Globals.getInst().logger.debug("Parsing opcodes: Encountered unexpected opcode 254 (0Xfe). This impdep1 opcode is not meant to be found in valid class files. It will be loaded gracefully, though.");
					return new Impdep1();
				case 0xff:
					if (Globals.getInst().logger.isDebugEnabled())
						Globals.getInst().logger.debug("Parsing opcodes: Encountered unexpected opcode 255 (0Xff). This impdep2 opcode is not meant to be found in valid class files. It will be loaded gracefully, though.");
					return new Impdep2();
				default:
					throw new InvalidInstructionInitialisationException("Encountered an unknown opcode.");
			}
		} catch (InvalidInstructionInitialisationException e) {
			throw new InvalidInstructionInitialisationException("Parsing of the bytecode failed: " + e.getMessage());
		}
	}

}
