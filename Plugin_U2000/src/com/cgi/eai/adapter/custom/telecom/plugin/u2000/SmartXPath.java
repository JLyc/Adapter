package com.cgi.eai.adapter.custom.telecom.plugin.u2000;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.namespace.QName;
import javax.xml.xpath.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sochaa on 2. 1. 2015.
 */
public class SmartXPath {
    private static XPathFactory xPathFactory = XPathFactory.newInstance();
    private static XPath xPath = xPathFactory.newXPath();

    private Document document;
    private String expression;
    private Object result;

    public SmartXPath from(Document document) {
        this.document = document;
        return this;
    }

    public SmartXPath forExpression(String expression) {
        this.expression = expression;
        return this;
    }

    private synchronized void result(QName type) {
        synchronized (xPath) {
            try {
                result = xPath.compile(expression).evaluate(document, type);
                testResult(type);
            } catch (XPathExpressionException e) {
                e.printStackTrace();
            } finally {
                reuseInstance();
            }
        }
    }

    private void testResult(QName type) throws XPathExpressionException {
        if (result == null) {
            throw new XPathExpressionException("XPhat is null");
        }
        if (type.equals(XPathConstants.NODESET) && ((NodeList) result).getLength() == 0) {
            throw new XPathExpressionException("XPhat NodeSet is empty");
        }
        if (type.equals(XPathConstants.NODE) && ((Node) result).getTextContent().equals("")) {
            throw new XPathExpressionException("XPhat Node is empty");
        }
    }

    public NodeList asNodeList() {
        result(XPathConstants.NODESET);
        return (NodeList) result;
    }

    public List<String> asList() {
        List<String> list = new ArrayList<>();
        result(XPathConstants.NODESET);
        NodeList result = (NodeList) this.result;
        NodeList children = result.item(0).getChildNodes();
        for (int index = 0; index < children.getLength(); index++) {
            if(children.item(index).getNodeType()==Node.ELEMENT_NODE)
            list.add(children.item(index).getTextContent());
        }
        return list;
    }

    public String asString() {
        result(XPathConstants.STRING);
        return (String) result;
    }

    public boolean asBoolean() {
        result(XPathConstants.BOOLEAN);
        return (boolean) result;

    }

    public Node asNode() {
        result(XPathConstants.NODE);
        return (Node) result;
    }

    public Number asNumber() {
        result(XPathConstants.NUMBER);
        return (Number) result;
    }

    private static void reuseInstance() {
        xPath.reset();
    }
}
