package de.wwu.muggl.solvers.conf;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.NodeList;

import de.wwu.muggl.solvers.solver.SolverInfo;
import de.wwu.muggl.solvers.solver.listener.SolverManagerListener;

/**
 * Solver specific configuration read from the configuration file using ConfigReader.
 * 
 * @author Christoph Lembeck
 */
public class SolverManagerConfig{

    private static SolverManagerConfig instance;

    public static SolverManagerConfig getInstance(){
	if (instance == null)
	    instance = new SolverManagerConfig();
	return instance;
    }

    protected SolverInfo[] solverInfos;
    protected String[] listenerNames;
    
    protected boolean precalculateSolutions;
    
    private SolverManagerConfig(){
	ConfigReader configReader = ConfigReader.getInstance();
	try{	    
	    precalculateSolutions = configReader.getTextContent("//TesttoolConfiguration/SolverSystem/General/SolutionPreCalculation/@enabled").equalsIgnoreCase("yes");
	    listenerNames = configReader.getTextContents("//TesttoolConfiguration/SolverSystem/General/SolverManagerListener[attribute::enabled='yes']/@class");
	    
	    
	    TesttoolConfig.getLogger().trace("SolverManagerConfig: Listeners read from config file " + Arrays.toString(listenerNames));
	    
	    
	    NodeList solverNodes = configReader.getNodeList("//TesttoolConfiguration/SolverSystem/SolverList/Solver");	    
	    solverInfos = new SolverInfo[solverNodes.getLength()];
	    for (int i = 0; i < solverNodes.getLength(); i++){
		String className = configReader.getTextContent("@class", solverNodes.item(i));
		boolean enabled = configReader.getTextContent("@enabled", solverNodes.item(i)).equalsIgnoreCase("yes");
		int priority = Integer.parseInt(configReader.getTextContent("@priority", solverNodes.item(i)));
		SolverInfo solverInfo = new SolverInfo(className, priority, enabled);		
		solverInfos[i] =  solverInfo;
	    }
	    Arrays.sort(solverInfos);
	    
	    
	    TesttoolConfig.getLogger().debug("SolverManagerConfig: Solvers read from config file " + Arrays.toString(solverInfos) );
	    
	} catch (XPathExpressionException xpee){
	    xpee.printStackTrace();
	    throw new InternalError(xpee.toString());
	}
    }

    public String[] getListenerNames(){
	return listenerNames;
    }
    
    public Set<SolverManagerListener> getListeners() {
	HashSet<SolverManagerListener> listeners = new HashSet<SolverManagerListener>();
	
	try{ 
	    for (String listenerName : getListenerNames()){
		SolverManagerListener listener = (SolverManagerListener) Class.forName(listenerName).newInstance();
		listeners.add(listener);
	    }
	} catch (ClassNotFoundException cnfe){
	    throw new InternalError(cnfe.toString());
	} catch (InstantiationException ie){
	    throw new InternalError(ie.toString());
	} catch (IllegalAccessException iae){
	    throw new InternalError(iae.toString());
	}
	
	return listeners;
    }

    public String[] getSolverClassNames(){
	String[] result = new String[solverInfos.length];
	for (int i = 0; i < result.length; i++)
	    result[i] = solverInfos[i].getSolverClassName();
	return result;
    }

    /**
     * Returns the solver information for the available solvers. If onlyEnabled is true
     * it returns only the solvers which are enabled (generally in the config file).
     * @param onlyEnabled controls whether only enabled solvers are returned or not.
     * @return Array of SolverInfo.
     * @see setSolverEnabled
     */
    public SolverInfo[] getSolverInfos(boolean onlyEnabled){
	if (onlyEnabled){
	    int count = 0;
	    for (SolverInfo info: solverInfos)
		if (info.isEnabled())
		    count++;
	    SolverInfo[] result = new SolverInfo[count];
	    count = 0;
	    for (SolverInfo info: solverInfos)
		if (info.isEnabled()){
		    result[count++] = (SolverInfo)info.clone();
		}
	    return result;
	} else
	    return solverInfos.clone();
    }

    public void setSolverEnabled(String solverClassName, boolean enabled){
	for (SolverInfo info: solverInfos)
	    if (info.getSolverClassName().equals(solverClassName)){
		info.setEnabled(enabled);
		return;
	    }
    }

    public boolean getPrecalculateSolutions() { return precalculateSolutions; }
}
