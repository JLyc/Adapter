package com.cgi.eai.adapter.custom.telecom.plugin.u2000.plugin_properties;

import com.cgi.eai.adapter.custom.telecom.plugin.u2000.U2000;
import org.apache.commons.net.telnet.TelnetClient;
import org.w3c.dom.Document;
import telecom.core.ResponseException;
import telecom.core.wrapers.CommunicationMessageInterface;
import telecom.core.wrapers.jms.JmsMessageInterfaceWraper;
import telecom.core.wrapers.rv.RVMessageInterfaceWraper;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

/**
 * Created by sochaa on 20. 6. 2016.
 */
public class Runner {
    private static TelnetClient telnet;
    private static InputStream in;
    private static BufferedInputStream bin;
    private static PrintStream out;

    public static void main(String[] args) throws IOException {

//        fungame();
//        if(true){
//            return;
//        }
        Map<String, String> att = new HashMap<>();
        att.put("SYSTEM", "U2000");
        att.put("IP-ADDRESS", "localhost");
        att.put("PORT", "1111");
        att.put("USER-NAME", "#!5AsQrh90x7GOkwyaAJrgor/RSFdAgfpghLnq3DQeewY=");
        att.put("USER-PWD", "#!DGtwPAzMD9C3k/O9WXE6vVDetHQXpjjbgNdzcZvriT0=");
        att.put("RETRIES-COUNT", "2");
        att.put("TIMEOUT", "100");
//        att.put("ALIAS","48575443EBFAA923");
        att.put("ALIAS","ST_Business");
        att.put("OPERATION", "getUpstream");
        att.put("OPERATION", "getStatus");
        att.put("OPERATION", "getConfig");
        U2000 mvf =  new U2000();
        try {
            Document out = mvf.request(att);
            createResponseFromDoc(out);
        } catch (ResponseException e) {
            e.printStackTrace();
        }

//        telnet = new TelnetClient();
//        telnet.connect("localhost",1112);
//        in = telnet.getInputStream();
//        bin = new BufferedInputStream(telnet.getInputStream());
//        out = new PrintStream(telnet.getOutputStream());
//        write("LOGIN:::CTAG::UN=satl1user,PWD=U2000u2000@;");
//        System.out.println(readUntil());
//        write("LST-ONTRUNINFO::ALIAS=4857544321F41857:1::;");
////        write("LST-ONTRUNINFO::ALIAS=48575443EBFAA923:1::;");
//        System.out.println(readUntil());
//
//        tl1Logout();
//        disconnect();
    }

    private static CommunicationMessageInterface createResponseFromDoc(Document doc) {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);

            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            transformer.transform(source, result);
            System.out.println(writer.toString());
            return null;
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void fungame() {
        Map<String, String> responseObject = new HashMap<>();
        responseObject.put("FN0", "0");
        responseObject.put("SN0", "0");

        responseObject.put("FN1", "0");
        responseObject.put("SN1", "0");

        responseObject.put("FN4", "0");
        responseObject.put("SN4", "0");

//        responseObject.put("FN1", "0");
//        responseObject.put("FN2", "0");
        responseObject.put("FN3", "0");
//        responseObject.put("FN4", "0");
        responseObject.put("FN5", "0");
//        responseObject.put("FN6", "0");
//        responseObject.put("FN7", "0");
        responseObject.put("FN8", "0");
//        responseObject.put("FN9", "0");
        responseObject.put("FN10", "0");
        responseObject.put("FN11", "0");
//        int counter=1;
        int maxCount=0;
        TreeSet<String> treeSet = new TreeSet<>();

        Map<String, Integer> helpone = new HashMap<>();
        for (String key : responseObject.keySet()) {
            String makeKey = key.replaceFirst("\\d+", "");
            Integer count = helpone.containsKey(makeKey)? helpone.get(makeKey) : 0;
            helpone.put(makeKey, ++count);
        }

        for(Map.Entry<String, Integer> entry : helpone.entrySet()){
            if(maxCount<entry.getValue()){
                maxCount = entry.getValue();
            }
        }

//        for (String key : responseObject.keySet()) {
//            String subkey = key.replace("\\d", "");
//            boolean add = treeSet.add(subkey);
//
//            if (!treeSet.add()) {
//                if (counter++ > maxCount) {
//                    maxCount = counter;
//                }
//            } else {
//                counter = 1;
//            }
//        }
        System.out.println(maxCount);
    }

    private static void tl1Logout() {
        write("LOGOUT:::CTAG::;");
    }
    public static String readUntil() throws IOException {
        StringBuilder sb = new StringBuilder();
        while (sb.toString().equals("") || !sb.toString().endsWith(";")) {
            while (bin.available() > 0) {
                char c = (char) bin.read();
                sb.append(c);
            }
        }
        String out = sb.toString().replace("\n", " ").replace("\r", " ");
        if (out.matches(".+ENDESC=Succeeded.+") || out.equals("")) {
            return out;
        } else {
//            Called command failed to response with success
            throw new IOException("Called command failed: " + out);
        }
    }

    public static void write(String value) {
        try {
            out.println(value);
            out.flush();
        } catch (Exception e) {
        }
    }

    //closes a this client. you may want to send command "exit" before
    public static void disconnect() {
        try {
            if (telnet != null) telnet.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
