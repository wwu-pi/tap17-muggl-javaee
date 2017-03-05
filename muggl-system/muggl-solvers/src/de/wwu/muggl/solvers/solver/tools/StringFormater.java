package de.wwu.muggl.solvers.solver.tools;

/**
 * @author Christoph Lembeck
 */
@SuppressWarnings("all")
public class StringFormater{

    public static String nanosToMillis(long nanos, int decimals){
	String result = Long.toString(nanos);
	if (decimals > 6)
	    decimals = 6;
	while (result.length() < 7)
	    result = "0" + result;
	if (decimals > 0){
	    result = result.substring(0, result.length() - 6) + "." + result.substring(result.length() - 6, result.length() - (6 - decimals));
	} else {
	    result = result.substring(0, result.length() - 6);
	}
	return result;
    }

    /**
     * Replaces the reserved characters &amp;, &lt;, &gt;, &quot;, and &apos; by
     * their XML encodings and returns the valid XML representation of the passed
     * String argument.
     * @param msg the String that should be encoded to be a valid XML String.
     * @return the valid XML representation of the string.
     */
    @Deprecated
    public static String xmlEncode(String msg){
	return msg.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("'", "&apos;").replaceAll("\"", "&quot;");
    }

}
