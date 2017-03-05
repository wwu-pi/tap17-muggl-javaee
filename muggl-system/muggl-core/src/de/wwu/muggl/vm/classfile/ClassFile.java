package de.wwu.muggl.vm.classfile;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Level;

import de.wwu.muggl.configuration.Globals;
import de.wwu.muggl.instructions.FieldResolutionError;
import de.wwu.muggl.instructions.MethodResolutionError;
import de.wwu.muggl.vm.VirtualMachine;
import de.wwu.muggl.vm.classfile.structures.Attribute;
import de.wwu.muggl.vm.classfile.structures.Constant;
import de.wwu.muggl.vm.classfile.structures.Field;
import de.wwu.muggl.vm.classfile.structures.Method;
import de.wwu.muggl.vm.classfile.structures.attributes.AttributeDeprecated;
import de.wwu.muggl.vm.classfile.structures.attributes.AttributeInnerClasses;
import de.wwu.muggl.vm.classfile.structures.attributes.AttributeRuntimeInvisibleAnnotations;
import de.wwu.muggl.vm.classfile.structures.attributes.AttributeRuntimeVisibleAnnotations;
import de.wwu.muggl.vm.classfile.structures.attributes.AttributeSourceFile;
import de.wwu.muggl.vm.classfile.structures.attributes.AttributeUnknownSkipped;
import de.wwu.muggl.vm.classfile.structures.attributes.elements.Annotation;
import de.wwu.muggl.vm.classfile.structures.attributes.elements.InnerClass;
import de.wwu.muggl.vm.classfile.structures.constants.ConstantClass;
import de.wwu.muggl.vm.classfile.structures.constants.ConstantDouble;
import de.wwu.muggl.vm.classfile.structures.constants.ConstantFieldref;
import de.wwu.muggl.vm.classfile.structures.constants.ConstantFloat;
import de.wwu.muggl.vm.classfile.structures.constants.ConstantInteger;
import de.wwu.muggl.vm.classfile.structures.constants.ConstantInterfaceMethodref;
import de.wwu.muggl.vm.classfile.structures.constants.ConstantInvokeDynamic;
import de.wwu.muggl.vm.classfile.structures.constants.ConstantLong;
import de.wwu.muggl.vm.classfile.structures.constants.ConstantMethodHandle;
import de.wwu.muggl.vm.classfile.structures.constants.ConstantMethodType;
import de.wwu.muggl.vm.classfile.structures.constants.ConstantMethodref;
import de.wwu.muggl.vm.classfile.structures.constants.ConstantNameAndType;
import de.wwu.muggl.vm.classfile.structures.constants.ConstantString;
import de.wwu.muggl.vm.classfile.structures.constants.ConstantUtf8;
import de.wwu.muggl.vm.initialization.InitializedClass;
import de.wwu.muggl.vm.initialization.Objectref;
import de.wwu.muggl.vm.initialization.PrimitiveWrappingImpossibleException;
import de.wwu.muggl.vm.loading.MugglClassLoader;

/**
 * A ClassFile is the representation of a java class in class-format. It offers the constants of the
 * class-format, as well as the possibility to load classes from the file system. when initialized,
 * it provides methods to access the complete data structure of a class file.
 * 
 * @author Tim Majchrzak
 * @version 1.0.0, 2011-01-15
 */
public class ClassFile {
	// Constants for the constant_pool.
	/**
	 * The CONSTANT_Class with a byte value of 7.
	 */
	public static final byte	CONSTANT_CLASS				= 7;
	/**
	 * The CONSTANT_Fieldref with a byte value of 9.
	 */
	public static final byte	CONSTANT_FIELDREF			= 9;
	/**
	 * The CONSTANT_Methodref with a byte value of 10.
	 */
	public static final byte	CONSTANT_METHODREF			= 10;
	/**
	 * The CONSTANT_InterfaceMethodref with a byte value of 11.
	 */
	public static final byte	CONSTANT_INTERFACEMETHODREF	= 11;
	/**
	 * The CONSTANT_String with a byte value of 9.
	 */
	public static final byte	CONSTANT_STRING				= 8;
	/**
	 * The CONSTANT_Integer with a byte value of 3.
	 */
	public static final byte	CONSTANT_INTEGER			= 3;
	/**
	 * The CONSTANT_Float with a byte value of 4.
	 */
	public static final byte	CONSTANT_FLOAT				= 4;
	/**
	 * The CONSTANT_Long with a byte value of 5.
	 */
	public static final byte	CONSTANT_LONG				= 5;
	/**
	 * The CONSTANT_Double with a byte value of 6.
	 */
	public static final byte	CONSTANT_DOUBLE				= 6;
	/**
	 * The CONSTANT_NameAndType with a byte value of 12.
	 */
	public static final byte	CONSTANT_NAMEANDTYPE		= 12;
	/**
	 * The CONSTANT_Utf8 with a byte value of 1.
	 */
	public static final byte	CONSTANT_UTF8				= 1;
	/**
	 * The CONSTANT_MethodHandle with a byte value of 15 (Java 7, JSR 292)
	 */
	public static final byte	CONSTANT_METHODHANDLE		= 15;
	/**
	 * The CONSTANT_MethodType with a byte value of 16 (Java 7, JSR 292)
	 */
	public static final byte	CONSTANT_METHODTYPE			= 16;
	/**
	 * The CONSTANT_InvokeDynamic with a byte value of 18 (Java 8)
	 */
	public static final byte	CONSTANT_INVOKEDYNAMIC		= 18;

	/*
	 * Access flag constants. Only subsets of them are used for classes, fields, methods etc., which
	 * also explains the double usage of 0x0020.
	 */
	/**
	 * The ACC_PUBLIC with a short value of 0x0001.
	 */
	public static final short	ACC_PUBLIC					= 0x0001;
	/**
	 * The ACC_PRIVATE with a short value of 0x0002.
	 */
	public static final short	ACC_PRIVATE					= 0x0002;
	/**
	 * The ACC_PROTECTED with a short value of 0x0004.
	 */
	public static final short	ACC_PROTECTED				= 0x0004;
	/**
	 * The ACC_STATIC with a short value of 0x0008.
	 */
	public static final short	ACC_STATIC					= 0x0008;
	/**
	 * The ACC_FINAL with a short value of 0x0010.
	 */
	public static final short	ACC_FINAL					= 0x0010;
	/**
	 * The ACC_SUPER with a short value of 0x0020.
	 */
	public static final short	ACC_SUPER					= 0x0020;
	/**
	 * The ACC_SYNCHRONIZED with a short value of 0x0020.
	 */
	public static final short	ACC_SYNCHRONIZED			= 0x0020;
	/**
	 * The ACC_VOLATILE with a short value of 0x0040.
	 */
	public static final short	ACC_VOLATILE				= 0x0040;
	/**
	 * The ACC_BRIDGE with a short value of 0x0040.
	 */
	public static final short	ACC_BRIDGE					= 0x0040;
	/**
	 * The ACC_TRANSIENT with a short value of 0x0080.
	 */
	public static final short	ACC_VARARGS					= 0x0080;
	/**
	 * The ACC_VARARGS with a short value of 0x0080.
	 */
	public static final short	ACC_TRANSIENT				= 0x0080;
	/**
	 * The ACC_NATIVE with a short value of 0x0100.
	 */
	public static final short	ACC_NATIVE					= 0x0100;
	/**
	 * The ACC_INTERFACE with a short value of 0x0200.
	 */
	public static final short	ACC_INTERFACE				= 0x0200;
	/**
	 * The ACC_ABSTRACT with a short value of 0x0400.
	 */
	public static final short	ACC_ABSTRACT				= 0x0400;
	/**
	 * The ACC_STRICT with a short value of 0x0800.
	 */
	public static final short	ACC_STRICT					= 0x0800;
	/**
	 * The ACC_SYNTHETIC with a short value of 0x1000.
	 */
	public static final short	ACC_SYNTHETIC				= 0x1000;
	/**
	 * The ACC_ANNOTATION.
	 */
	public static final short	ACC_ANNOTATION				= 0x2000;
	/**
	 * The ACC_ENUM with a short value of 0x4000.
	 */
	public static final short	ACC_ENUM					= 0x4000;

	// Constants for data types.
	/**
	 * The T_BOOLEAN with a byte value of 4.
	 */
	public static final byte	T_BOOLEAN					= 4;
	/**
	 * The T_BYTE with a byte value of 8.
	 */
	public static final byte	T_BYTE						= 8;
	/**
	 * The T_CHAR with a byte value of 5.
	 */
	public static final byte	T_CHAR						= 5;
	/**
	 * The T_DOUBLE with a byte value of 7.
	 */
	public static final byte	T_DOUBLE					= 7;
	/**
	 * The T_FLOAT with a byte value of 6.
	 */
	public static final byte	T_FLOAT						= 6;
	/**
	 * The T_INT with a byte value of 10.
	 */
	public static final byte	T_INT						= 10;
	/**
	 * The T_LONG with a byte value of 11.
	 */
	public static final byte	T_LONG						= 11;
	/**
	 * The T_SHORT with a byte value of 9.
	 */
	public static final byte	T_SHORT						= 9;

	// The magic number.
	/**
	 * The magic number of java class files.
	 */
	public static final int		CAFEBABE					= 0xCAFEBABE;

	// Fields for the elements of the class.
	private int					magic;
	private int					minorVersion;
	private int					majorVersion;
	private int					constantPoolCount;
	private Constant[]			constantPool;
	private int					accessFlags;
	private int					thisClass;
	private int					superClass;
	private int					interfacesCount;
	private int[]				interfaces;
	private int					fieldsCount;
	private Field[]				fields;
	private int					methodsCount;
	private Method[]			methods;
	private int					attributeCount;
	private Attribute[]			attributes;

	// Fields for the access flags.
	private boolean				accPublic;
	private boolean				accFinal;
	private boolean				accSuper;
	private boolean				accInterface;
	private boolean				accAbstract;
	private boolean				accSynthetic;
	private boolean				accAnnotation;
	private boolean				accEnum;

	// Field to hold information about the Class object;
	private int					byteLength;
	private byte[]				bytes;
	private Class<?>			instanceOfClass;
	private InitializedClass	initializedClass;
	private Objectref			primitiveWrapper;

	// Other fields.
	private MugglClassLoader	classLoader;
	private boolean				readingData					= false;
	private DataInputStream		dis;
	private String				name;
	private String				fullPath;
	private long				loadingNumber;
	
	private String              genericType;

	/**
	 * Constructor for reading a class from a File resource.
	 * 
	 * @param classLoader The MugglClassLoader which loads this ClassFile.
	 * @param file The file containing a class.
	 * @throws ClassFileException On fatal errors loading or parsing a class file.
	 * @throws FileNotFoundException When a required file was not found.
	 * @throws IOException On fatal problems reading or writing to the file system.
	 */
	public ClassFile(MugglClassLoader classLoader, File file) throws ClassFileException,
			FileNotFoundException, IOException {
		this(classLoader, new FileInputStream(file), new FileInputStream(file), file.length(), file
				.getPath());
	}

	/**
	 * Constructor for reading a class from an InputStream. The stream is closed after that.
	 * 
	 * @param classLoader The MugglClassLoader which loads this ClassFile.
	 * @param is The InputStream to read the class from.
	 * @param is2 A second InputStream to read the class from (must not be identical, but a second
	 *        instance!).
	 * @param byteLength The number of bytes this ClassFile consists of.
	 * @param fullPath The full path of the class, also showing whether it belongs to a jar-archive.
	 * @throws ClassFileException Thrown on fatal errors loading or parsing a class file.
	 * @throws FileNotFoundException Thrown when a required file was not found.
	 * @throws IOException Thrown on fatal problems reading or writing to the file system.
	 */
	public ClassFile(MugglClassLoader classLoader, InputStream is, InputStream is2,
			long byteLength, String fullPath) throws ClassFileException, FileNotFoundException,
			IOException {
		try {
			this.classLoader = classLoader;
			if (!fullPath.toLowerCase().endsWith(".class")) {
				fullPath += ".class";
			}
			if (Globals.getInst().logger.isInfoEnabled())
				Globals.getInst().logger.info("Parsing class " + fullPath);
			this.fullPath = fullPath;
			this.loadingNumber = classLoader.getNextLoadingNumber();
			this.readingData = true;
			this.dis = new DataInputStream(is);
			this.byteLength = (int) byteLength;
			this.bytes = new byte[this.byteLength];
			readClass();
			this.readingData = false;
			this.instanceOfClass = null;

			// Now read the whole stream into an array of bytes.
			this.dis.close();
			is.close();
			this.dis = new DataInputStream(is2);
			int a = 0;
			try {
				while (this.dis.available() != 0) {
					this.bytes[a] = this.dis.readByte();
					a++;
				}
			} catch (ArrayIndexOutOfBoundsException e) {
				throw new ClassFileException(
						"The specified length of the class file does not reflectr the actual length of it.");
			}
			if (a < this.byteLength)
				throw new ClassFileException(
						"The specified length of the class file does not reflectr the actual length of it.");
		} finally {
			// Close the streams.
			this.dis.close();
			is2.close();
		}
	}

	/**
	 * Static method to read the name of a class from a class file. It works much faster than using
	 * the constructor. Of course, it is much less flexible at the same time. However, no ressources
	 * will be accocated permanently, the class loader will not be touched and there will be no
	 * logging.<br />
	 * <br />
	 * WARNING: This methods does no checks whatsoever! It will not run the validation processes
	 * required to read a class file. Any checking that can be avoided when trying to read the name
	 * of the class is skipped. This makes this method fast; yet there is no guarantee a class file
	 * can be successfully parsed if its name was successfully read. It may just turn out to be
	 * corrupt.
	 * 
	 * @param file The file containing a class.
	 * @return The name of a class as specified by itself.
	 * @throws ClassFileException On fatal errors loading or parsing a class file.
	 * @throws FileNotFoundException Wwhen a required file was not found.
	 * @throws IOException On fatal problems reading from the file system.
	 */
	public static String readNameFromClassFile(File file) throws ClassFileException,
			FileNotFoundException, IOException {
		String fullPath = file.getPath();

		// File access.
		FileInputStream is = new FileInputStream(file);
		DataInputStream dis = new DataInputStream(is);
		try {
			// Start parsing the file.
			int magic = dis.readInt();
			if (magic != CAFEBABE) {
				throw new ClassFileException("Invalid class file.");
			}
			// Skipping data not needed here.
			dis.readUnsignedShort();
			dis.readUnsignedShort();
			// Minimal validation of the constant pool count.
			int constantPoolCount = dis.readUnsignedShort();
			if (constantPoolCount < 1) {
				throw new ClassFileException(
						"Encountered a corrupt class file: The constant_pool_count cannot be less than one.");
			}
			if (constantPoolCount > Limitations.MAX_CONSTANT_POOL_COUNT) {
				throw new ClassFileException(
						"Encountered a corrupt class file: The constant_pool_count must not have more than "
								+ Limitations.MAX_CONSTANT_POOL_COUNT + " entries.");
			}

			/*
			 * Read the constant pool. Just save the indices CONSTANT_Class_info structures point to
			 * and the Strings from CONSTANT_Utf8_info structures. Skip anything else.
			 */
			int[] index = new int[constantPoolCount];
			String[] names = new String[constantPoolCount];
			for (int a = 1; a < constantPoolCount; a++) {
				short tag = dis.readByte();
				switch (tag) {
					case CONSTANT_CLASS:
						index[a] = dis.readUnsignedShort();
						break;
					case CONSTANT_FIELDREF:
						readSomeBytes(dis, 4);
						break;
					case CONSTANT_METHODREF:
						readSomeBytes(dis, 4);
						break;
					case CONSTANT_INTERFACEMETHODREF:
						readSomeBytes(dis, 4);
						break;
					case CONSTANT_STRING:
						readSomeBytes(dis, 2);
						break;
					case CONSTANT_INTEGER:
						readSomeBytes(dis, 4);
						break;
					case CONSTANT_FLOAT:
						readSomeBytes(dis, 4);
						break;
					case CONSTANT_LONG:
						readSomeBytes(dis, 8);
						a++; // Count up as it takes two slots.
						break;
					case CONSTANT_DOUBLE:
						readSomeBytes(dis, 8);
						a++; // Count up as it takes two slots.
						break;
					case CONSTANT_NAMEANDTYPE:
						readSomeBytes(dis, 4);
						break;
					case CONSTANT_UTF8:
						int length = dis.readUnsignedShort();
						byte[] bytes = new byte[length];
						for (int b = 0; b < length; b++) {
							bytes[b] = dis.readByte();
						}
						names[a] = new String(bytes, "UTF8");
						break;
					default:
						throw new ClassFileException("Encountered an unknown Constant - halting.");
				}
			}

			// Skip another two bytes and get thisClass then.
			dis.readUnsignedShort();
			int thisClass = dis.readUnsignedShort();

			// It is now possible to get the name.
			String name = names[index[thisClass]];

			// Check if this name matches the file name.
			if (!fullPath.toLowerCase().endsWith(".class")) {
				fullPath += ".class";
			}
			String path = fullPath.replace("\\", "/");
			String className = path.substring(path.lastIndexOf("/") + 1);
			className = className.substring(0, className.indexOf("."));
			String nameToCheck = name.substring(name.lastIndexOf("/") + 1);
			nameToCheck = nameToCheck.substring(nameToCheck.lastIndexOf(".") + 1);

			/*
			 * Convert to lower case. On windows systems the file name might have characters
			 * capitalized that the class definition name does not capitalized and the other way
			 * around.
			 */
			if (!className.toLowerCase().equals(nameToCheck.toLowerCase()))
				throw new ClassFileException("The defined class " + nameToCheck
						+ " does not match the file name " + className + ".");

			// Successfully finished.
			return name;
		} catch (EOFException e) {
			throw new ClassFileException(
					"Unexpectedly uncountered the EOF - halting. The file is no valid class file.");
		} finally {
			// Closing open handlers.
			try {
				dis.close();
			} finally {
				is.close();
			}
		}
	}

	/**
	 * Auxiliary methods to read and thereby skip an arbitrary number of bytes from an input stream.
	 * 
	 * @param dis The {@link DataInputStream} to read the bytes from.
	 * @param numberOfBytes The number of bytes to read.
	 * @throws EOFException If the input stream has unexpectedly reached the end.
	 * @throws IOException On fatal problems reading from the file system
	 */
	private static void readSomeBytes(DataInputStream dis, int numberOfBytes) throws EOFException,
			IOException {
		for (int a = 0; a < numberOfBytes; a++) {
			dis.readByte();
		}
	}

	/**
	 * Main method for reading the class. It delegated the parsing to other classes holding
	 * representations of data structures from the class file.
	 * 
	 * @throws ClassFileException Thrown on fatal errors loading or parsing a class file.
	 * @throws IOException Thrown on fatal problems reading or writing to the file system.
	 */
	private void readClass() throws ClassFileException, IOException {
		try {
			// From now on read step by step, never setting the pointer in the DataInputStream back.
			this.magic = this.dis.readInt();
			if (Globals.getInst().logger.isDebugEnabled())
				Globals.getInst().logger.debug("Parsing: Read magic: 0x"
						+ Integer.toHexString(this.magic).toUpperCase());
			if (this.magic != CAFEBABE) {
				throw new ClassFileException("Invalid class file.");
			}
			this.minorVersion = this.dis.readUnsignedShort();
			if (Globals.getInst().logger.isDebugEnabled())
				Globals.getInst().logger.debug("Parsing: Read minor_version: " + this.minorVersion);
			this.majorVersion = this.dis.readUnsignedShort();
			if (Globals.getInst().logger.isDebugEnabled())
				Globals.getInst().logger.debug("Parsing: Read major_version: " + this.majorVersion);
			this.constantPoolCount = this.dis.readUnsignedShort();
			if (Globals.getInst().logger.isDebugEnabled())
				Globals.getInst().logger.debug("Parsing: Read constant_pool_count: "
						+ this.constantPoolCount);
			if (this.constantPoolCount < 1) {
				throw new ClassFileException(
						"Encountered a corrupt class file: The constant_pool_count cannot be less than one.");
			}
			if (this.constantPoolCount > Limitations.MAX_CONSTANT_POOL_COUNT) {
				throw new ClassFileException(
						"Encountered a corrupt class file: The constant_pool_count must not have more than "
								+ Limitations.MAX_CONSTANT_POOL_COUNT + " entries.");
			}

			// Fill the constant_pool with data. This is delegated to other classes.
			this.constantPool = new Constant[this.constantPoolCount];
			for (int a = 1; a < this.constantPoolCount; a++) {
				byte tag = this.dis.readByte();
				switch (tag) {
					case CONSTANT_CLASS:
						this.constantPool[a] = new ConstantClass(this);
						break;
					case CONSTANT_FIELDREF:
						this.constantPool[a] = new ConstantFieldref(this);
						break;
					case CONSTANT_METHODREF:
						this.constantPool[a] = new ConstantMethodref(this);
						break;
					case CONSTANT_INTERFACEMETHODREF:
						this.constantPool[a] = new ConstantInterfaceMethodref(this);
						break;
					case CONSTANT_STRING:
						this.constantPool[a] = new ConstantString(this);
						break;
					case CONSTANT_INTEGER:
						this.constantPool[a] = new ConstantInteger(this);
						break;
					case CONSTANT_FLOAT:
						this.constantPool[a] = new ConstantFloat(this);
						break;
					case CONSTANT_LONG:
						this.constantPool[a] = new ConstantLong(this);
						a++; // Count up as it takes two slots.
						break;
					case CONSTANT_DOUBLE:
						this.constantPool[a] = new ConstantDouble(this);
						a++; // Count up as it takes two slots.
						break;
					case CONSTANT_NAMEANDTYPE:
						this.constantPool[a] = new ConstantNameAndType(this);
						break;
					case CONSTANT_UTF8:
						this.constantPool[a] = new ConstantUtf8(this);
						break;
					case CONSTANT_METHODHANDLE:
						this.constantPool[a] = new ConstantMethodHandle(this);
						break;
					case CONSTANT_METHODTYPE:
						this.constantPool[a] = new ConstantMethodType(this);
						break;
					case CONSTANT_INVOKEDYNAMIC:
						this.constantPool[a] = new ConstantInvokeDynamic(this);
						break;
					default:
						throw new ClassFileException("Encountered an unknown Constant - halting.");
				}
			}
			if (Globals.getInst().logger.isDebugEnabled())
				Globals.getInst().logger.debug("Parsing: Read constant_pool entries.");

			// Verify that every constant_pool entry has the correct Constant_UTF8_info for its
			// name_index.
			for (int a = 1; a < this.constantPoolCount; a++) {
				byte tag = this.constantPool[a].getTag();
				switch (tag) {
					case CONSTANT_CLASS:
						if (this.constantPool[((ConstantClass) this.constantPool[a]).getNameIndex()]
								.getTag() != CONSTANT_UTF8)
							throw new ClassFileException(
									"Encountered a corrupt class file: name_index of a CONSTANT_Class_info should point to a CONSTANT_Utf8_info.");
						break;
					case CONSTANT_FIELDREF:
						if (this.constantPool[((ConstantFieldref) this.constantPool[a])
								.getClassIndex()].getTag() != CONSTANT_CLASS) {
							throw new ClassFileException(
									"Encountered a corrupt class file: class_index of a CONSTANT_Fieldref_info should point to a CONSTANT_Class_info.");
						}
						if (this.constantPool[((ConstantFieldref) this.constantPool[a])
								.getNameAndTypeIndex()].getTag() != CONSTANT_NAMEANDTYPE) {
							throw new ClassFileException(
									"Encountered a corrupt class file: name_and_type_index of a CONSTANT_Fieldref_info should point to a CONSTANT_NameAndType_info.");
						}
						break;
					case CONSTANT_METHODREF:
						if (this.constantPool[((ConstantMethodref) this.constantPool[a])
								.getClassIndex()].getTag() != CONSTANT_CLASS) {
							throw new ClassFileException(
									"Encountered a corrupt class file: class_index of a CONSTANT_Methodref_info should point to a CONSTANT_Class_info.");
						}
						if (this.constantPool[((ConstantMethodref) this.constantPool[a])
								.getNameAndTypeIndex()].getTag() != CONSTANT_NAMEANDTYPE) {
							throw new ClassFileException(
									"Encountered a corrupt class file: name_and_type_index of a CONSTANT_Methodref_info should point to a CONSTANT_NameAndType_info.");
						}
						break;
					case CONSTANT_INTERFACEMETHODREF:
						if (this.constantPool[((ConstantInterfaceMethodref) this.constantPool[a])
								.getClassIndex()].getTag() != CONSTANT_CLASS) {
							throw new ClassFileException(
									"Encountered a corrupt class file: class_index of a CONSTANT_InterfaceMethodref_info should point to a CONSTANT_Class_info.");
						}
						if (this.constantPool[((ConstantInterfaceMethodref) this.constantPool[a])
								.getNameAndTypeIndex()].getTag() != CONSTANT_NAMEANDTYPE) {
							throw new ClassFileException(
									"Encountered a corrupt class file: name_and_type_index of a CONSTANT_InterfaceMethodref_info should point to a CONSTANT_NameAndType_info.");
						}
						break;
					case CONSTANT_STRING:
						if (this.constantPool[((ConstantString) this.constantPool[a])
								.getStringIndex()].getTag() != CONSTANT_UTF8) {
							throw new ClassFileException(
									"Encountered a corrupt class file: string_index of a CONSTANT_String_info should point to a CONSTANT_Utf8_info.");
						}
						break;
					case CONSTANT_LONG:
						// Just count up as it takes two slots.
						a++;
						break;
					case CONSTANT_DOUBLE:
						// Just count up as it takes two slots.
						a++;
						break;
					case CONSTANT_NAMEANDTYPE:
						if (this.constantPool[((ConstantNameAndType) this.constantPool[a])
								.getNameIndex()].getTag() != CONSTANT_UTF8) {
							throw new ClassFileException(
									"Encountered a corrupt class file: name_index of a CONSTANT_NameAndType_info should point to a CONSTANT_Utf8_info.");
						}
						if (this.constantPool[((ConstantNameAndType) this.constantPool[a])
								.getDescriptorIndex()].getTag() != CONSTANT_UTF8) {
							throw new ClassFileException(
									"Encountered a corrupt class file: descriptor_index of a CONSTANT_NameAndType_info should point to a CONSTANT_Utf8_info.");
						}
						break;
					case CONSTANT_UTF8:
						break;
				}
			}

			if (Globals.getInst().logger.isDebugEnabled())
				Globals.getInst().logger.debug("Parsing: Verified constant_pool entries.");

			this.accessFlags = this.dis.readUnsignedShort();
			parseAccessFlags();
			if (Globals.getInst().logger.isDebugEnabled())
				Globals.getInst().logger.debug("Parsing: Read access_flags: " + this.accessFlags
						+ " (" + getPrefix() + ")");

			this.thisClass = this.dis.readUnsignedShort();
			if (Globals.getInst().logger.isDebugEnabled())
				Globals.getInst().logger.debug("Parsing: Read this_class: " + this.thisClass + " ("
						+ this.constantPool[this.thisClass].toString() + ")");
			checkIndexIntoTheConstantPool(this.thisClass, false);

			this.name = this.constantPool[this.thisClass].toString().replace("/", ".");

			// Check if this name matches the file name.
			String path = this.fullPath.replace("\\", "/");
			String className = path.substring(path.lastIndexOf("|") + 1); // strip away surrounding java archives (if any) 
			className = className.substring(className.lastIndexOf("/") + 1); // strip away parent directories (if any)
			className = className.substring(0, className.indexOf("."));
			String name = this.name.substring(this.name.lastIndexOf(".") + 1);
			/*
			 * Convert to lower case. On windows systems the file name might have characters
			 * capitalized that the class definition name does not capitalized and the other way
			 * around.
			 */
			if (!className.toLowerCase().equals(name.toLowerCase()))
				throw new ClassFileException("The defined class " + name
						+ " does not match the file name " + className + ".");

			// Continue reading the class file.
			this.superClass = this.dis.readUnsignedShort();
			if (Globals.getInst().logger.isDebugEnabled()) {
				checkIndexIntoTheConstantPool(this.superClass, true);
				String superClass = "no super class";
				if (this.superClass != 0)
					superClass = this.constantPool[this.superClass].toString();
				Globals.getInst().logger.debug("Parsing: Read super_class: " + this.superClass
						+ " (" + superClass + ")");
			}
			this.interfacesCount = this.dis.readUnsignedShort();
			if (Globals.getInst().logger.isDebugEnabled())
				Globals.getInst().logger.debug("Parsing: Read interfaces_count: "
						+ this.interfacesCount);
			if (this.interfacesCount < 0) {
				throw new ClassFileException(
						"Encountered a corrupt class file: interfaces_count must not be less than zero.");
			}
			if (this.interfacesCount > Limitations.MAX_INTERFACES_COUNT) {
				throw new ClassFileException(
						"Encountered a corrupt class file: interfaces_count must not be greater than "
								+ Limitations.MAX_INTERFACES_COUNT + ".");
			}
			this.interfaces = new int[this.interfacesCount];
			for (int a = 0; a < this.interfacesCount; a++) {
				this.interfaces[a] = this.dis.readUnsignedShort();
				checkIndexIntoTheConstantPool(this.interfaces[a], false);
				if (this.constantPool[this.interfaces[a]].getTag() != CONSTANT_CLASS) {
					throw new ClassFileException(
							"Encountered a corrupt class file: Interface number " + a
									+ " does not point to a CONSTANT_Class_info.");
				}
			}
			if (Globals.getInst().logger.isDebugEnabled())
				Globals.getInst().logger.debug("Parsing: Read the interfaces");
			this.fieldsCount = this.dis.readUnsignedShort();
			if (Globals.getInst().logger.isDebugEnabled())
				Globals.getInst().logger.debug("Parsing: Read fields_count: " + this.fieldsCount);
			// Read the Fields. This is delegated to other classes.
			this.fields = new Field[this.fieldsCount];
			if (this.fieldsCount < 0) {
				throw new ClassFileException(
						"Encountered a corrupt class file: fields_count must not be less than zero.");
			}
			if (this.fieldsCount > Limitations.MAX_FIELDS_COUNT) {
				throw new ClassFileException(
						"Encountered a corrupt class file: fields_count must not be greater than "
								+ Limitations.MAX_FIELDS_COUNT + ".");
			}
			for (int a = 0; a < this.fieldsCount; a++) {
				this.fields[a] = new Field(this);
			}
			if (Globals.getInst().logger.isDebugEnabled())
				Globals.getInst().logger.debug("Parsing: Read the fields");
			this.methodsCount = this.dis.readUnsignedShort();
			if (this.methodsCount < 0) {
				throw new ClassFileException(
						"Encountered a corrupt class file: methods_count must not be less than zero.");
			}
			if (this.methodsCount > Limitations.MAX_METHODS_COUNT) {
				throw new ClassFileException(
						"Encountered a corrupt class file: methods_count must not be greater than 65535"
								+ Limitations.MAX_METHODS_COUNT + ".");
			}
			if (Globals.getInst().logger.isDebugEnabled())
				Globals.getInst().logger.debug("Parsing: Read method_count: " + this.methodsCount);
			// Read the methods. This is delegated to other classes.
			this.methods = new Method[this.methodsCount];
			if (this.methodsCount < 0) {
				throw new ClassFileException(
						"Encountered a corrupt class file: methods_count is less than zero.");
			}
			for (int a = 0; a < this.methodsCount; a++) {
				this.methods[a] = new Method(this);
			}
			if (Globals.getInst().logger.isDebugEnabled())
				Globals.getInst().logger.debug("Parsing: Read the methods");
			this.attributeCount = this.dis.readUnsignedShort();
			if (Globals.getInst().logger.isDebugEnabled())
				Globals.getInst().logger.debug("Parsing:Read attributes_count: "
						+ this.attributeCount);
			// Read the methods. This is delegated to other classes.
			this.attributes = new Attribute[this.attributeCount];
			if (this.attributeCount < 0) {
				throw new ClassFileException(
						"Encountered a corrupt class file: attribute_count is less than zero.");
			}
			for (int a = 0; a < this.attributeCount; a++) {
				int attributeNameIndex = this.dis.readUnsignedShort();
				ConstantUtf8 constant = null;
				if (attributeNameIndex >= this.constantPoolCount) {
					throw new ClassFileException(
							"Encountered a corrupt class file: Access to the constant pool entry #"
									+ attributeNameIndex + " was requested. Yet, there are only "
									+ this.constantPoolCount + " entries in total.");
				}
				try {
					constant = (ConstantUtf8) this.constantPool[attributeNameIndex];
				} catch (ClassCastException e) {
					throw new ClassFileException("Parsing: Expected a ConstantUtf8 at "
							+ attributeNameIndex
							+ " in the constant_pool when reading the attributes, but got "
							+ this.constantPool[attributeNameIndex].getClass().getName() + ".");
				}
				String attributeName = constant.getStringValue();
				// Which Attribute is it?
				if (attributeName.equals("SourceFile")) {
					this.attributes[a] = new AttributeSourceFile(this, attributeNameIndex);
				} else if (attributeName.equals("Deprecated")) {
					this.attributes[a] = new AttributeDeprecated(this, attributeNameIndex);
				} else if (attributeName.equals("InnerClasses")) {
					this.attributes[a] = new AttributeInnerClasses(this, attributeNameIndex);
				} else if (attributeName.equals("RuntimeVisibleAnnotations")) {
					this.attributes[a] = new AttributeRuntimeVisibleAnnotations(this,
							attributeNameIndex);
				} else if (attributeName.equals("RuntimeInvisibleAnnotations")) {
					this.attributes[a] = new AttributeRuntimeInvisibleAnnotations(this,
							attributeNameIndex);
				} else {
					if (Globals.getInst().logger.isDebugEnabled())
						Globals.getInst().logger
								.debug("Parsing: Encountered an unknown attribute \""
										+ attributeName + "\"");
					this.attributes[a] = new AttributeUnknownSkipped(this, attributeNameIndex);
				}
				if (Globals.getInst().logger.isDebugEnabled())
					Globals.getInst().logger.debug("Parsing: Read the attributes");
			}
			if (Globals.getInst().logger.isDebugEnabled())
				Globals.getInst().logger.debug("Parsing completed successfully.");
		} catch (ClassFileException e) {
			if (Globals.getInst().logger.isEnabledFor(Level.WARN))
				Globals.getInst().logger.warn("Parsing class " + this.name
						+ " failed with a ClassFileException with reason " + e.getMessage());
			throw e;
		} catch (IOException e) {
			if (Globals.getInst().logger.isEnabledFor(Level.WARN))
				Globals.getInst().logger.warn("Parsing class " + this.name
						+ " failed with a IOException with reason " + e.getMessage());
			throw e;
		}
	}

	/**
	 * Check if the index into the constant pool is valid. It only is valid, if it is greater than
	 * zero and less than the number of entries.
	 * 
	 * @param index The index into the constant pool to check.
	 * @param allowZero Allow the lowerBound to be zero instead of one.
	 * @throws ClassFileException If the index is invalid.
	 */
	private void checkIndexIntoTheConstantPool(int index, boolean allowZero)
			throws ClassFileException {
		if (index >= this.constantPoolCount) {
			throw new ClassFileException(
					"Encountered a corrupt class file: An index into the constant pool at position "
							+ index + " was found. Yet, there are only " + this.constantPoolCount
							+ " entries in total.");
		}
		int upperBound = 1;
		if (allowZero) upperBound--;
		if (index < upperBound) {
			throw new ClassFileException(
					"Encountered a corrupt class file: An index into the constant pool was found thats value is less than one.");
		}
	}

	/**
	 * Write the class represented by this ClassFile to a DataOutputStream. Hence, this method can
	 * be used to write it to a file.
	 * 
	 * @param dos The DataOutputStream.
	 * @throws IOException Thrown on fatal errors writting to the output stream.
	 */
	public void writeClass(DataOutputStream dos) throws IOException {
		if (Globals.getInst().logger.isInfoEnabled())
			Globals.getInst().logger.info("Writing class " + this.name);
		// From now on write step by step, never setting the pointer in the DataInputStream back.
		dos.writeInt(this.magic);
		dos.writeShort(this.minorVersion);
		dos.writeShort(this.majorVersion);

		// Write the constant_pool.
		dos.writeShort(this.constantPoolCount);
		for (int a = 1; a < this.constantPoolCount; a++) {
			this.constantPool[a].writeToClassFile(dos);
			if (this.constantPool[a].getTag() == CONSTANT_DOUBLE
					|| this.constantPool[a].getTag() == CONSTANT_LONG) a++;
		}

		// Continue writing.
		dos.writeShort(this.accessFlags);
		dos.writeShort(this.thisClass);
		dos.writeShort(this.superClass);
		dos.writeShort(this.interfacesCount);
		// Write the interfaces
		for (int a = 0; a < this.interfacesCount; a++) {
			dos.writeShort(this.interfaces[a]);
		}

		// Write the fields.
		dos.writeShort(this.fieldsCount);
		for (int a = 0; a < this.fieldsCount; a++) {
			this.fields[a].writeToClassFile(dos);
		}

		// Write the methods.
		dos.writeShort(this.methodsCount);
		for (int a = 0; a < this.methodsCount; a++) {
			this.methods[a].writeToClassFile(dos);
		}

		// Write the attributes.
		dos.writeShort(this.attributeCount);
		for (int a = 0; a < this.attributeCount; a++) {
			this.attributes[a].writeToClassFile(dos);
		}
		if (Globals.getInst().logger.isInfoEnabled())
			Globals.getInst().logger.info("Writing finished successfully.");
	}

	/**
	 * Parse the access flags field so that the single access flags can be easily checked.
	 * 
	 * @throws ClassFileException If the access_flags have a value less than zero.
	 */
	protected void parseAccessFlags() throws ClassFileException {
		int flags = this.accessFlags;
		if (flags < 0)
			throw new ClassFileException(
					"Encountered a corrupt class file: access_flags is less than zero.");

		// Parse the flags.
		if (flags >= ClassFile.ACC_ENUM) {
			flags -= ClassFile.ACC_ENUM;
			this.accEnum = true;
		}
		if (flags >= ClassFile.ACC_ANNOTATION) {
			flags -= ClassFile.ACC_ANNOTATION;
			this.accAnnotation = true;
		}
		if (flags >= ClassFile.ACC_SYNTHETIC) {
			flags -= ClassFile.ACC_SYNTHETIC;
			this.accSynthetic = true;
		}
		if (flags >= 0x0800) {
			flags -= 0x0800;
			if (Globals.getInst().logger.isDebugEnabled())
				Globals.getInst().logger
						.debug("Encountered and ignored a flag with value 0x0800 which is unknown for a class.");
		}
		if (flags >= ClassFile.ACC_ABSTRACT) {
			flags -= ClassFile.ACC_ABSTRACT;
			this.accAbstract = true;
		}
		if (flags >= ClassFile.ACC_INTERFACE) {
			flags -= ClassFile.ACC_INTERFACE;
			this.accInterface = true;
		}
		if (flags >= 0x0100) {
			flags -= 0x0100;
			if (Globals.getInst().logger.isDebugEnabled())
				Globals.getInst().logger
						.debug("Encountered and ignored a flag with value 0x0100 which is unknown for a class.");
		}
		if (flags >= 0x0080) {
			flags -= 0x0080;
			if (Globals.getInst().logger.isDebugEnabled())
				Globals.getInst().logger
						.debug("Encountered and ignored a flag with value 0x0080 which is unknown for a class.");
		}
		if (flags >= 0x0040) {
			flags -= 0x0040;
			if (Globals.getInst().logger.isDebugEnabled())
				Globals.getInst().logger
						.debug("Encountered and ignored a flag with value 0x0040 which is unknown for a class.");
		}
		if (flags >= ClassFile.ACC_SUPER) {
			flags -= ClassFile.ACC_SUPER;
			this.accSuper = true;
		}
		if (flags >= ClassFile.ACC_FINAL) {
			flags -= ClassFile.ACC_FINAL;
			this.accFinal = true;
		}
		if (flags >= 0x0008) {
			flags -= 0x0008;
			if (Globals.getInst().logger.isDebugEnabled())
				Globals.getInst().logger
						.debug("Encountered and ignored a flag with value 0x0008 which is unknown for a class.");
		}
		if (flags >= 0x0004) {
			flags -= 0x0004;
			if (Globals.getInst().logger.isDebugEnabled())
				Globals.getInst().logger
						.debug("Encountered and ignored a flag with value 0x0004 which is unknown for a class.");
		}
		if (flags >= 0x0002) {
			flags -= 0x0002;
			if (Globals.getInst().logger.isDebugEnabled())
				Globals.getInst().logger
						.debug("Encountered and ignored a flag with value 0x0002 which is unknown for a class.");
		}
		if (flags >= ClassFile.ACC_PUBLIC) {
			flags -= ClassFile.ACC_PUBLIC;
			this.accPublic = true;
		}

		// Check the flags.
		if (this.accInterface) {
			if (!this.accAbstract)
				throw new ClassFileException("An interface must have its ACC_ABSTRACT flag set.");
			if (this.accFinal)
				throw new ClassFileException("An interface must not have the ACC_FINAL flag set.");
			if (this.accSuper)
				throw new ClassFileException("An interface must not have the ACC_SUPER flag set.");
			if (this.accSynthetic)
				throw new ClassFileException(
						"An interface must not have the ACC_SYNTHETIC flag set.");
			if (this.accEnum)
				throw new ClassFileException("An interface must not have the ACC_ENUM flag set.");
		} else {
			if (this.accAnnotation)
				throw new ClassFileException(
						"An annotation type must have the ACC_INTERFACE flag set.");
		}
		if (this.accAbstract && this.accFinal)
			throw new ClassFileException("An abstract class must not have its ACC_FINAL flag set.");
	}

	/**
	 * Getter for the constant_pool.
	 * 
	 * @return The constant_pool as a Constant[].
	 */
	public Constant[] getConstantPool() {
		return this.constantPool;
	}

	/**
	 * Getter for the Methods.
	 * 
	 * @return The methods as a Method[].
	 */
	public Method[] getMethods() {
		return this.methods;
	}

	/**
	 * Getter for the access_flags.
	 * 
	 * @return The access flags as an int.
	 */
	public int getAccessFlags() {
		return this.accessFlags;
	}

	/**
	 * Getter for The attribute_count.
	 * 
	 * @return The attribute_count as an int.
	 */
	public int getAttributeCount() {
		return this.attributeCount;
	}

	/**
	 * Getter for the attributes.
	 * 
	 * @return The attributes as an Attribute[].
	 */
	public Attribute[] getAttributes() {
		return this.attributes;
	}

	/**
	 * Getter for the constant_pool_count.
	 * 
	 * @return The constant_pool_count as an int.
	 */
	public int getConstantPoolCount() {
		return this.constantPoolCount;
	}

	/**
	 * Getter for the fields.
	 * 
	 * @return The fields as a Field[].
	 */
	public Field[] getFields() {
		return this.fields;
	}

	/**
	 * Getter for the fields_count.
	 * 
	 * @return The fields_count as an int.
	 */
	public int getFieldsCount() {
		return this.fieldsCount;
	}

	/**
	 * Getter for the interfaces.
	 * 
	 * @return The interfaces as an array of int.
	 */
	public int[] getInterfaces() {
		return this.interfaces;
	}

	/**
	 * Getter for the interfaces_count.
	 * 
	 * @return The interfaces_count as an int.
	 */
	public int getInterfacesCount() {
		return this.interfacesCount;
	}

	/**
	 * Getter for the magic value.
	 * 
	 * @return The magic value as an int (always 0xCAFEBABE).
	 */
	public int getMagic() {
		return this.magic;
	}

	/**
	 * Getter for the major_version.
	 * 
	 * @return The major_version as an int.
	 */
	public int getMajorVersion() {
		return this.majorVersion;
	}

	/**
	 * Getter for the methods_count.
	 * 
	 * @return The methods_count as an int.
	 */
	public int getMethodsCount() {
		return this.methodsCount;
	}

	/**
	 * Getter for the minor_version.
	 * 
	 * @return The minor_version as an int.
	 */
	public int getMinorVersion() {
		return this.minorVersion;
	}

	/**
	 * Getter for the super_class.
	 * 
	 * @return The super_class as an int.
	 */
	public int getSuperClass() {
		return this.superClass;
	}

	/**
	 * Get the super ClassFile of this ClassFile.
	 * 
	 * @return The super ClassFile of this ClassFile; or null, should there be no super class.
	 * @throws ClassFileException On fatal errors loading or parsing a class file.
	 */
	public ClassFile getSuperClassFile() throws ClassFileException {
		if (this.superClass == 0) return null;
		return this.classLoader.getClassAsClassFile(this.constantPool[this.superClass]
				.getStringValue());
	}

	/**
	 * Getter for this_class.
	 * 
	 * @return This_class as an int.
	 */
	public int getThisClass() {
		return this.thisClass;
	}

	/**
	 * Build and return a String representation of the access flags.
	 * 
	 * @return A String representation of the access flags.
	 */
	public String getPrefix() {
		String prefix = "";
		if (this.accPublic) {
			prefix = "public ";
		}
		if (this.accFinal) {
			prefix += "final ";
		}
		if (this.accSuper) {
			prefix += "super ";
		}
		if (this.accInterface) {
			prefix += "interface ";
		}

		return prefix;
	}

	/**
	 * Getter for the access flag "abstract".
	 * 
	 * @return true, if the class is abstract, false otherwise.
	 */
	public boolean isAccAbstract() {
		return this.accAbstract;
	}

	/**
	 * Getter for the access flag "final".
	 * 
	 * @return true, if the class is final, false otherwise.
	 */
	public boolean isAccFinal() {
		return this.accFinal;
	}

	/**
	 * Getter for the access flag "interface".
	 * 
	 * @return true, if the class is an interface, false otherwise.
	 */
	public boolean isAccInterface() {
		return this.accInterface;
	}

	/**
	 * Getter for the access flag "public".
	 * 
	 * @return true, if the class is public, false otherwise.
	 */
	public boolean isAccPublic() {
		return this.accPublic;
	}

	/**
	 * Getter for the access flag "super".
	 * 
	 * @return true, if the class has the "super" flag, false otherwise.
	 */
	public boolean isAccSuper() {
		return this.accSuper;
	}

	/**
	 * Getter for the access flag "synthetic".
	 * 
	 * @return true, if the class has the "synthetic" flag, false otherwise.
	 */
	public boolean isAccSynthetic() {
		return this.accSynthetic;
	}

	/**
	 * Getter for the access flag "annotation".
	 * 
	 * @return true, if the class has the "annotation" flag, false otherwise.
	 */
	public boolean isAccAnnotation() {
		return this.accAnnotation;
	}

	/**
	 * Getter for the access flag "enum".
	 * 
	 * @return true, if the class has the "enum" flag, false otherwise.
	 */
	public boolean isAccEnum() {
		return this.accEnum;
	}

	/**
	 * Getter for the full name of the class including the package. Example: "java.lang.Integer".
	 * 
	 * @return The name of the class.
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Getter for the full canonical name of the class including the package. Inner Classes will be
	 * separated by a "dot" and not by the dollar sign.
	 * 
	 * @return The name of the class.
	 */
	public String getCanonicalName() {
		// TODO local or anonymous classes should return null.
		// First check if this class is an inner class.
		for (int a = 0; a < this.attributes.length; a++) {
			if (this.attributes[a] instanceof AttributeInnerClasses) {
				InnerClass[] innerClasses = ((AttributeInnerClasses) this.attributes[a])
						.getClasses();
				for (int b = 0; b < innerClasses.length; b++) {
					// Is this class is the inner class of another class?
					if (this.constantPool[innerClasses[b].getInnerClassInfoIndex()]
							.getStringValue().replace("/", ".").equals(this.name)) {
						/*
						 * Return the name of the inner class attached to the outer class' canonical
						 * name.
						 */
						String outerName = this.constantPool[innerClasses[b]
								.getOuterClassInfoIndex()].getStringValue();
						try {
							ClassFile outerClassFile = this.classLoader
									.getClassAsClassFile(outerName);
							return outerClassFile.getCanonicalName()
									+ "."
									+ this.constantPool[innerClasses[b].getInnerNameIndex()]
											.getStringValue();
						} catch (ClassFileException e) {
							// Simply ignore it. This is really unlikely to happen.
						}
					}
				}
			}
		}

		// Just return the normal name.
		return this.name;
	}

	/**
	 * Getter for the package-exclusive name of the class. Example: "Integer".
	 * 
	 * @return The name of the class.
	 */
	public String getClassName() {
		return this.name.substring(this.name.lastIndexOf(".") + 1);
	}

	/**
	 * Find a field of this class by the given name and descriptor and return it.
	 * 
	 * @param name The Fields' name to search for.
	 * @param descriptor The Fields' descriptor to search for.
	 * @return The found Field representation as a Field object.
	 * @throws FieldResolutionError If the Field could not be found.
	 */
	public Field getFieldByNameAndDescriptor(String name, String descriptor) {
		for (int a = 0; a < this.fieldsCount; a++) {
			if (this.fields[a].getName().equals(name)
					&& this.fields[a].getDescriptor().equals(descriptor)) return this.fields[a];
		}
		throw new FieldResolutionError("Field " + name + " with descriptor " + descriptor
				+ " could not be resolved for class " + getName() + ".");
	}

	/**
	 * Find a field of this class by the given name and return it.
	 * 
	 * @param name The Fields' name to search for.
	 * @return The found Field representation as a Field object.
	 * @throws FieldResolutionError If the Field could not be found.
	 */
	public Field getFieldByName(String name) {
		return getFieldByName(name, false);
	}

	/**
	 * Find a field of this class by the given name and return it.
	 * 
	 * @param name The Fields' name to search for.
	 * @param searchSuperClasses If set to true, the super classes of this class will be searched
	 *        for the field.
	 * @return The found Field representation as a Field object.
	 * @throws FieldResolutionError If the Field could not be found.
	 */
	public Field getFieldByName(String name, boolean searchSuperClasses) {
		// Search for the field.
		for (int a = 0; a < this.fieldsCount; a++) {
			if (this.fields[a].getName().equals(name)) return this.fields[a];
		}

		// Did not found it. Search the super classes?
		if (searchSuperClasses) {
			try {
				ClassFile superClassFile = this.getSuperClassFile();
				while (superClassFile != null) {
					// Search for the field.
					for (int a = 0; a < superClassFile.fieldsCount; a++) {
						if (superClassFile.fields[a].getName().equals(name))
							return superClassFile.fields[a];
					}

					// Get the super class' super class.
					superClassFile = superClassFile.getSuperClassFile();
				}
			} catch (ClassFileException e) {
				// Just ignore it. It is very unlikely to happen.
			}
		}

		// Give up.
		throw new FieldResolutionError("Field " + name + " could not be resolved for class "
				+ getName() + ".");
	}

	/**
	 * Find a method of this class by the given name and descriptor and return it.
	 * 
	 * @param name The methods' name to search for.
	 * @param descriptor The methods' descriptor to search for.
	 * @return The found Method representation as a Method object.
	 * @throws MethodResolutionError If the method could not be found.
	 */
	public Method getMethodByNameAndDescriptor(String name, String descriptor) {
		for (int a = 0; a < this.methodsCount; a++) {
			if (this.methods[a].getName().equals(name)
					&& this.methods[a].getDescriptor().equals(descriptor)) return this.methods[a];
		}
		throw new MethodResolutionError("Method " + name + " could not be resolved for class "
				+ getName() + ".");
	}

	/**
	 * Find the <clinit>-Method, if there is any.
	 * 
	 * @throws MethodResolutionError If the method could not be found.
	 * @return The <clinit>-Method of this ClassFile.
	 */
	public Method getClinitMethod() {
		for (int a = 0; a < this.methodsCount; a++) {
			if (this.methods[a].getName().equals("<clinit>")) return this.methods[a];
		}
		throw new MethodResolutionError("Method clinit could not be resolved for class "
				+ getName() + ".");
	}
	
	public Method getPostConstructMethod() {
		for(Method method : this.methods) {
			for(Attribute attribute : method.getAttributes()) {
				if (attribute.getStructureName().equals("attribute_runtime_visible_annotation")) {
					AttributeRuntimeVisibleAnnotations attributeAnnotation = (AttributeRuntimeVisibleAnnotations) attribute;
					for(Annotation annotation : attributeAnnotation.getAnnotations()) {
						String annoName = constantPool[annotation.getTypeIndex()].getStringValue();
						if (annoName.equals("Ljavax/annotation/PostConstruct;")) {
							return method;
						}
					}					
				}
			}
		}
		return null;
	}

	/**
	 * Returns the DataInputStream if field readingData is true.
	 * 
	 * @return The DataInputStream or null, if it is already closed.
	 */
	public DataInputStream getDis() {
		if (this.readingData) return this.dis;
		return null;
	}

	/**
	 * Return a String representation of the package this class belongs to. Example: "java.lang".
	 * 
	 * @return A String representation of the package this class belongs to.
	 * @see #getTopLevelPackageName()
	 */
	public String getPackageName() {
		return this.name.substring(0, this.name.lastIndexOf("."));
	}

	/**
	 * Return a String representation of the top level package this class belongs to. Example:
	 * "java".
	 * 
	 * @return A String representation of the top level package this class belongs to.
	 * @see #getPackageName()
	 */
	public String getTopLevelPackageName() {
		return this.name.substring(0, this.name.indexOf("."));
	}

	/**
	 * Getter for the MugglClassLoader.
	 * 
	 * @return The MugglClassLoader that loaded this ClassFile.
	 */
	public MugglClassLoader getClassLoader() {
		return this.classLoader;
	}

	/**
	 * Get an instance of Class representing this ClassFile.
	 * 
	 * There is a caching mechanism for this method. When parsing a class into its representation by
	 * this file, only its bytes are read into an array but no instance of Class is generated. If
	 * this method is called for the first time, this is done. For the economic use of memory, this
	 * instance is cached then and hence stored to a field. At the same time, the array of bytes is
	 * discarded. It may not be used than anymore.
	 * 
	 * @return An instance of Class representing this ClassFile.
	 */
	public Class<?> getInstanceOfClass() {
		if (this.instanceOfClass == null) {
			this.instanceOfClass = this.classLoader.defineClassFromClassFile(getName(), this.bytes);
			this.bytes = null;
		}
		return this.instanceOfClass;
	}

	/**
	 * Generate an instance of Objectref for this ClassFile that will work as a wrapper for a
	 * primitive type. This will invoke the static initializers, if that has not been done, yet.
	 * 
	 * Please refer to the documentation of {@link InitializedClass#getANewPrimitiveWrapper()} for further
	 * details about the wrappers for primitive types.
	 * 
	 * @param vm The currently running virtual machine.
	 * @return A new instance of objectref for this ClassFile with the primitive flag set.
	 * @throws ExceptionInInitializerError If class initialization fails.
	 * @throws PrimitiveWrappingImpossibleException If the represented ClassFile cannot be used as a
	 *         primitive wrapper.
	 */
	public Objectref getAPrimitiveWrapperObjectref(VirtualMachine vm)
			throws PrimitiveWrappingImpossibleException {
		if (this.primitiveWrapper == null) {
			if (this.initializedClass == null) {
				new InitializedClass(this, vm);
			}
			this.primitiveWrapper = this.initializedClass.getANewPrimitiveWrapper();
		}
		return this.primitiveWrapper;
	}

	/**
	 * Invokes the static initializers for this ClassFile, if that has not yet been done, and return
	 * an the InitializedClass instance.
	 * 
	 * @param vm The currently running virtual machine.
	 * @return The instance of InitializedClass for this ClassFile.
	 * @throws ExceptionInInitializerError If class initialization fails.
	 */
	public InitializedClass getTheInitializedClass(VirtualMachine vm) {
		if (this.initializedClass == null) {
			new InitializedClass(this, vm);
		}
		return this.initializedClass;
	}

	/**
	 * Get the initialized class object for this class file.
	 *
	 * @return The InitializedClass for this class file.
	 */
	public InitializedClass getInitializedClass() {
		return this.initializedClass;
	}
	
	/**
	 * Receive an instance of the InitializedClass.
	 * 
	 * This method seems obsolete on the first look, but it is not. When calling
	 * getAnInitializedClass() for the first time, this will lead to an instance of InitializedClass
	 * being created for this ClassFile. In the constructor of it, the invocation of its
	 * <clinit>-Method might be invoked. If this static initializer contains instructions such as
	 * putstatic, getAnInitializedClass() of this ClassFile is invoked, to get the main
	 * InitializedClass instance which will hold all static fields. Due to the program flow, at this
	 * time the field initializedClass would be unset. So the constructor of InitializedClass will
	 * invoke this method first, ensuring that InitializedClass will point to the first instance of
	 * InitializedClass for this ClassFile. So putstatic of the <clinit>-Method will get the correct
	 * instance, and everything will run fine.
	 * 
	 * @param initializedClass The InitializedClass of this ClassFile.
	 */
	public void putInitializedClass(InitializedClass initializedClass) {
		if (initializedClass.getClassFile().equals(this)) this.initializedClass = initializedClass;
	}

	/**
	 * Unload the initialized class. Warning: Do not use this method while executing an application.
	 * It is only meant to be used to free ressources and reinitialize all classes when starting a
	 * new application.
	 */
	public void unloadInitializedClass() {
		this.initializedClass = null;
	}

	/**
	 * Getter for the full path of the class file this class has been loaded from. It will include
	 * jar-archives with a syntax like
	 * drive:/path/to/jar/file.jar|internal/path/to/class/file.class.
	 * 
	 * @return The full path of the class file this class has been loaded from.
	 */
	public String getFullPath() {
		return this.fullPath;
	}

	/**
	 * Setter for the full path of the class file this class has been loaded from. It will include
	 * jar-archives with a syntax like
	 * drive:/path/to/jar/file.jar|internal/path/to/class/file.class.
	 * 
	 * @param fullPath The full path of the class file this class has been loaded from.
	 */
	public void setFullPath(String fullPath) {
		this.fullPath = fullPath;
	}

	/**
	 * Getter for the loading number of this class. Loading numbers can be used to determine which
	 * of two ClassFile instances has been generated earlier.
	 * 
	 * @return The loading number of this class.
	 */
	public long getLoadingNumber() {
		return this.loadingNumber;
	}

	/**
	 * Return an appropriate String representation of this method by simply invoking getName().
	 * 
	 * @return An appropriate String representation of this method.
	 */
	@Override
	public String toString() {
		return getName();
	}

	/**
	 * Returns true if the specified object is this. Returns false in any other case. By contract,
	 * there should at at no time be two instances of ClassFile for the same class file.
	 * 
	 * @param obj The object to compare with this class file.
	 * @return true if <i>obj</i> is this; false otherwise.
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		return false;
	}

	/**
	 * Returns a hash code value for the ClassFile. It is computed from the hash code its name.
	 * 
	 * @return a hash code value for this object.
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return this.getName().hashCode();
	}

	/**
	 * Get the number of bytes this class file uses.
	 * 
	 * @return The number of bytes this class file uses.
	 */
	public int getByteLength() {
		return this.byteLength;
	}

}
