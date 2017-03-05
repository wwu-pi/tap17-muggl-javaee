package de.wwu.muggl.vm.execution.nativeWrapping;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import de.wwu.muggl.configuration.Globals;

/**
 * This class is a replacement wrapper for java.io.PrintStream. It should be used when
 * a PrintStream is needed that will write text to a console. While in the implementation
 * of java.ioPrintStream a native method is used, this wrapper just writes the text to
 * a log file entry. This wrapper is especially meant to be used to replace the streams
 * System.err and System.out, since they are widely used. Instead of just being ignored,
 * text sent to on of these streams will be visible in the log file of this application,
 * or even in the step by step window if this execution mode is chosen.<br />
 * <br />
 * Warning: Not all methods of java.io.PrintStream are overridden. Invocation of inherited
 * methods might lead to unexpected results. The means of this wrapper are mainly to have
 * print() and println() invocations written to the log file of this application.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-10
 */
public class PrintStreamWrapper extends PrintStream {

	/**
	 * Field to store information what PrintStream this class is mapping.
	 *
	 * This private field is not used directly. When a class that uses a PrintStream is initialized
	 * and this Wrapper is used as the PrintStream, this field is written directly by the virtual
	 * machine. It is then read when this wrapper is actually used and supplied as an argument to
	 * the static method {@link #writeToLogfileImplementation(String, String)}.
	 */
	@SuppressWarnings("unused")
	private String wrapperFor;

	/**
	 * Constructor that initializes the super class.
	 * @param arg0 The output stream to which values and objects would be printed.
	 */
	public PrintStreamWrapper(OutputStream arg0) {
		super(arg0);
	}

	/**
	 * Constructor that initializes the super class.
	 * @param arg0 The output stream to which values and objects would be printed.
	 * @param arg1 Does not have any functionality here.
	 */
	public PrintStreamWrapper(OutputStream arg0, boolean arg1) {
		super(arg0, arg1);
	}

	/**
	 * Constructor that initializes the super class.
	 * @param arg0 The output stream to which values and objects would be printed.
	 * @param arg1 Does not have any functionality here.
	 * @param arg2 Does not have any functionality here.
	 * @throws UnsupportedEncodingException Probably thrown by the super constructor.
	 */
	public PrintStreamWrapper(OutputStream arg0, boolean arg1, String arg2)
			throws UnsupportedEncodingException {
		super(arg0, arg1, arg2);
	}

	/**
	 * Constructor that initializes the super class.
	 * @param arg0 Does not have any functionality here.
	 * @throws FileNotFoundException Probably thrown by the super constructor.
	 */
	public PrintStreamWrapper(String arg0) throws FileNotFoundException {
		super(arg0);
	}

	/**
	 * Constructor that initializes the super class.
	 * @param arg0 Does not have any functionality here.
	 * @param arg1 Does not have any functionality here.
	 * @throws FileNotFoundException Probably thrown by the super constructor.
	 * @throws UnsupportedEncodingException Probably thrown by the super constructor.
	 */
	public PrintStreamWrapper(String arg0, String arg1)
			throws FileNotFoundException, UnsupportedEncodingException {
		super(arg0, arg1);
	}

	/**
	 * Constructor that initializes the super class.
	 * @param arg0 Does not have any functionality here.
	 * @throws FileNotFoundException Probably thrown by the super constructor.
	 */
	public PrintStreamWrapper(File arg0) throws FileNotFoundException {
		super(arg0);
	}

	/**
	 *
	 * @param arg0 Does not have any functionality here.
	 * @param arg1 Does not have any functionality here.
	 * @throws FileNotFoundException Probably thrown by the super constructor.
	 * @throws UnsupportedEncodingException Probably thrown by the super constructor.
	 */
	public PrintStreamWrapper(File arg0, String arg1)
			throws FileNotFoundException, UnsupportedEncodingException {
		super(arg0, arg1);
	}

	/**
	 * Print the value of a boolean.
	 * @param b A boolean to be printed.
	 */
	@Override
	public void print(boolean b) {
		writeToLogfile(String.valueOf(b));
	}

	/**
	 * Print the value of a char.
	 * @param c A char to be printed.
	 */
	@Override
	public void print(char c) {
		writeToLogfile(String.valueOf(c));
	}

	/**
	 * Print the value of an array of chars.
	 * @param s An array of char to be printed.
	 */
	@Override
	public void print(char[] s) {
		writeToLogfile(String.valueOf(s));
	}

	/**
	 * Print the value of a double.
	 * @param d A double to be printed.
	 */
	@Override
	public void print(double d) {
		writeToLogfile(String.valueOf(d));
	}

	/**
	 * Print the value of a float.
	 * @param f A float to be printed.
	 */
	@Override
	public void print(float f) {
		writeToLogfile(String.valueOf(f));
	}

	/**
	 * Print the value of an int.
	 * @param i An int to be printed.
	 */
	@Override
	public void print(int i) {
		writeToLogfile(String.valueOf(i));
	}

	/**
	 * Print the value of a long.
	 * @param l A long to be printed.
	 */
	@Override
	public void print(long l) {
		writeToLogfile(String.valueOf(l));
	}

	/**
	 * Print the value of an object, using String.valueOf(obj).
	 * @param obj An Object to be printed.
	 */
	@Override
	public void print(Object obj) {
		writeToLogfile(String.valueOf(obj));
	}

	/**
	 * Print the value of a String.
	 * @param s A String to be printed.
	 */
	@Override
	public void print(String s) {
		writeToLogfile(s);
	}

	/**
	 * Print a line break.
	 */
	@Override
	public void println() {
		writeToLogfile("\n");
	}

	/**
	 * Print the value of a boolean followed by a line break.
	 * @param x A boolean to be printed, followed by a line break.
	 */
	@Override
	public void println(boolean x) {
		writeToLogfile(String.valueOf(x) + "\n");
	}

	/**
	 * Print the value of a char followed by a line break.
	 * @param x A char to be printed, followed by a line break.
	 */
	@Override
	public void println(char x) {
		writeToLogfile(String.valueOf(x) + "\n");
	}

	/**
	 * Print the value of an array of chars followed by a line break.
	 * @param x An array of char to be printed, followed by a line break.
	 */
	@Override
	public void println(char[] x) {
		writeToLogfile(String.valueOf(x) + "\n");
	}

	/**
	 * Print the value of a double followed by a line break.
	 * @param x A double to be printed, followed by a line break.
	 */
	@Override
	public void println(double x) {
		writeToLogfile(String.valueOf(x) + "\n");
	}

	/**
	 * Print the value of a float followed by a line break.
	 * @param x A float to be printed, followed by a line break.
	 */
	@Override
	public void println(float x) {
		writeToLogfile(String.valueOf(x) + "\n");
	}

	/**
	 * Print the value of an integer followed by a line break.
	 * @param x An int to be printed, followed by a line break.
	 */
	@Override
	public void println(int x) {
		writeToLogfile(String.valueOf(x) + "\n");
	}

	/**
	 * Print the value of a long followed by a line break.
	 * @param x A long to be printed, followed by a line break.
	 */
	@Override
	public void println(long x) {
		writeToLogfile(String.valueOf(x) + "\n");
	}

	/**
	 * Print the value of a object, using String.valueOf(obj), followed by a line break.
	 * @param x An Object to be printed, followed by a line break.
	 */
	@Override
	public void println(Object x) {
		writeToLogfile(x.toString() + "\n");
	}

	/**
	 * Print the value of a String followed by a line break.
	 * @param x A String to be printed, followed by a line break.
	 */
	@Override
	public void println(String x) {
		writeToLogfile(x + "\n");
	}

	/**
	 * Print the value of a sequence of bytes. The bytes to print are determined by the
	 * supplied offset off and the length <code>len</code>.
	 * @param buf A byte array.
     * @param off The offset from which to start reading bytes.
     * @param len The number of bytes to write.
	 */
	@Override
	public void write(byte[] buf, int off, int len) {
		if (off >= 0 && off + len < buf.length) {
			String value = "";
			for (int a = off; a < off + len; a++) {
				value += String.valueOf(buf[a]);
			}
			writeToLogfile(value);
		}
	}

	/**
	 *  Print the value of a byte.
	 *  @param b An int to write.
	 */
	@Override
	public void write(int b) {
		writeToLogfile(String.valueOf((byte) b));
	}

	/**
	 * Write to the log file.
	 *
	 * @param s A string to write.
	 */
	private static native void writeToLogfile(String s);

	/**
	 * Write the supplied String to the log file. If the logging level is set to less detailed
	 * than info, this method will do nothing.
	 *
	 * @param s The String to write to the log file.
	 * @param wrapperFor The class this is a wrapper for.
	 */
	public static void writeToLogfileImplementation(String s, String wrapperFor) {
		Globals constants = Globals.getInst();
		if (constants.execLogger.isInfoEnabled()) {
			if (s != null) {
				s = s.replace("\r", "\\r");
				s = s.replace("\n", "\\n");
				s = s.replace("\r\n", "\\r\\n");
				s = s.replace("\t", "\\t");
			}
			constants.setLoggingMessagesEscaping(false);
			constants.execLogger.info("The executed application wrote to " + wrapperFor + ":<br />\n<b>" + s + "</b>");
			constants.setLoggingMessagesEscaping(true);
		}
	}
}
