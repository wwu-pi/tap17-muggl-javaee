package de.wwu.muggl.solvers.conf;

// TODOME: rewrite this to properties file
// use default properties file + some user specified properties file

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

import de.wwu.muggl.configuration.Globals;


/**
 * @author Christoph
 */
@SuppressWarnings("all")
public class ConfigReader {

    private static final String configurationFileName = "/conf/solver-config.xml";

    private static ConfigReader instance;

    public static ConfigReader getInstance(){
	if (instance == null)
	    instance = new ConfigReader();
	return instance;
    }
    
    protected Document configuration;

    private ConfigReader(){
	DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
	domFactory.setIgnoringComments(true);
	domFactory.setValidating(true);
	try{
	    // URI baseURI = getBaseURI();

	    String fileName = Globals.getInst().BASE_DIRECTORY + configurationFileName;
	    DocumentBuilder docBuilder = domFactory.newDocumentBuilder();
	    docBuilder.setErrorHandler(new ConfigErrorHandler());
	    configuration = docBuilder.parse(fileName);
	} catch (Exception e){
	    // e.printStackTrace();
	    throw new InternalError("Configuration file can not be read: " + e.getClass().getName()
		    + "(" + e.getMessage() + ")");
	}
    }

    public URI getBaseURI(){
	try{
	    File file = new File(getClass().getClassLoader().getResource(getClass().getName().replaceAll("\\.", "/") + ".class").toURI());
	    return file.getParentFile().getParentFile().getParentFile().toURI();
	} catch (URISyntaxException e){
	    throw new InternalError(e.toString());
	}
    }

    public Document getConfigDocument(){
	return configuration;
    }

    public Node getNode(String xPathExpression) throws XPathExpressionException{
	return getNode(xPathExpression, configuration);
    }

    public Node getNode(String xPathExpression, Object item) throws XPathExpressionException{
	XPathFactory xPathFactory = XPathFactory.newInstance();
	XPath xPath = xPathFactory.newXPath();
	return (Node)xPath.evaluate(xPathExpression, item, XPathConstants.NODE);
    }

    public NodeList getNodeList(String xPathExpression) throws XPathExpressionException{
	XPathFactory xPathFactory = XPathFactory.newInstance();
	XPath xPath = xPathFactory.newXPath();
	return (NodeList)xPath.evaluate(xPathExpression, configuration, XPathConstants.NODESET);
    }

    public String getTextContent(String xPathExpression) throws XPathExpressionException{
	return getTextContent(xPathExpression, configuration);
    }

    public String getTextContent(String xPathExpression, Object item) throws XPathExpressionException{
	Node node = getNode(xPathExpression, item);
	return node.getTextContent();
    }

    public String[] getTextContents(String xPathExpression) throws XPathExpressionException{
	NodeList nodeList = getNodeList(xPathExpression);
	String[] result = new String[nodeList.getLength()];
	for (int i = 0; i < nodeList.getLength(); i++)
	    result[i] = nodeList.item(i).getTextContent();
	return result;
    }

    class ConfigErrorHandler implements ErrorHandler{

	public void error(SAXParseException e){
	    if (Globals.getInst().solverLogger.isDebugEnabled())
		Globals.getInst().solverLogger.debug("Invalid configuration file");
	    throw new InternalError(e.toString());
	}

	public void fatalError(SAXParseException e){
	    if (Globals.getInst().solverLogger.isDebugEnabled())
		Globals.getInst().solverLogger.debug("Invalid configuration file");
	    throw new InternalError(e.toString());
	}

	public void warning(SAXParseException e){
	    if (Globals.getInst().solverLogger.isDebugEnabled())
		Globals.getInst().solverLogger.debug("SAX warning: " + e.toString());
	}
    }
}
