package de.wwu.muggl.instructions.jpa;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DescSplitter {
	public static void main(String[] args) {
		
		
		splitMethodDesc("(Ljavax/persistence/criteria/Selection;)Ljavax/persistence/criteria/CriteriaQuery;");
		splitMethodDesc("([IFB[[[[[Ljava/lang/String;Ljava/lang/String;[I[S[BBLjava/lang/BLtring;)");
		splitMethodDesc("Ljava/lang/String;BBBBLjava/lang/String;");
		splitMethodDesc("ZBCSIFDJ[Z[B[C[S[I[F[D[JLZBCSIFDJ;LZBCSIFDJ;[LZBCSIFDJ;LZBCSIFDJ;[LZBCSIFDJ;");
	}
	
	public void foo1(boolean b) {
		
	}
	
	public void foo1(Boolean b) {
		
	}
	
	public static List<String> splitMethodDesc(String desc) {
		//\[*L[^;]+;|\[[ZBCSIFDJ]|[ZBCSIFDJ]
		int beginIndex = desc.indexOf('(');
		int endIndex = desc.lastIndexOf(')');
		if((beginIndex == -1 && endIndex != -1) || (beginIndex != -1 && endIndex == -1)) {
			System.err.println(beginIndex);
			System.err.println(endIndex);
			throw new RuntimeException();
		}
		String x0;
		if(beginIndex == -1 && endIndex == -1) {
			x0 = desc;
		}
		else {
			x0 = desc.substring(beginIndex + 1, endIndex);
		}
		Pattern pattern = Pattern.compile("\\[*L[^;]+;|\\[[ZBCSIFDJ]|[ZBCSIFDJ]");
        Matcher matcher = pattern.matcher(x0);

        ArrayList<String> listMatches = new ArrayList<String>();

        while(matcher.find())
        {
            listMatches.add(matcher.group());
        }
        
        return listMatches;
	}
}