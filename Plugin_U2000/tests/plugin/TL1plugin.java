//package plugin;
//
//import com.cgi.eai.adapter.custom.telecom.plugin.u2000.SmartXPath;
//import com.tibco.security.AXSecurityException;
//import com.tibco.security.ObfuscationEngine;
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
//import org.apache.commons.net.telnet.TelnetClient;
//import org.w3c.dom.Document;
//import org.w3c.dom.Node;
//import org.w3c.dom.NodeList;
//import org.xml.sax.SAXException;
//import telecom.core.CustomAdapterInterface;
//import telecom.core.ResponseException;
//
//import javax.xml.parsers.DocumentBuilder;
//import javax.xml.parsers.DocumentBuilderFactory;
//import javax.xml.parsers.ParserConfigurationException;
//import java.io.BufferedInputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.PrintStream;
//import java.nio.file.FileSystems;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.util.*;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//
///**
// * Created by andrej.socha@cgi.com on 10. 4. 2015.
// */
//public abstract class TL1plugin implements CustomAdapterInterface {
//    private static final Log LOG = LogFactory.getLog(TL1plugin.class);
//
//    protected int resultCode = 0;
//    protected String descriptionText = "success";
//
//    protected Map<String, String> responseObject = new TreeMap<>();
//    protected Map<String, String> att;
//    protected Map<String, String> enrich = new HashMap<>();
//
//    protected int groupIndex = 0;
//
//    private TelnetClient telnet;
//    private InputStream in = null;
//    private PrintStream out = null;
//    BufferedInputStream bin = null;
//    private long timeout = 1000;
//
////    private final Path COMMAND_LOCATION = FileSystems.getDefault().getPath("config" + FileSystems.getDefault().getSeparator() + "Commands.xml");
//    private final Path COMMAND_LOCATION = FileSystems.getDefault().getPath("D:\\Work_Workspacae\\ServiceAssurance\\TelecomCustomAdapter\\Plugin_U2000\\config\\Commands.xml");
//    Document doc = loadConfigFile(COMMAND_LOCATION);
//
//    SmartXPath xpath = new SmartXPath();
//
//    private Document loadConfigFile(Path path) {
//        try {
//            System.out.println(Files.exists(COMMAND_LOCATION));
//            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
//            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
//            Document doc = dBuilder.parse(path.toFile());
//            doc.getDocumentElement().normalize();
//            return doc;
//        } catch (SAXException | ParserConfigurationException | IOException e) {
//            LOG.error(e.toString());
//            return null;
//        }
//    }
//
//    @Override
//    public Document request(final Map<String, String> attribute) throws ResponseException {
//        try {
//            this.att = new HashMap<>(attribute);
//
//            telnet = new TelnetClient();
//            telnet.connect(att.get("IP-ADDRESS"), Integer.parseInt(att.get("PORT")));
//            in = telnet.getInputStream();
//            bin = new BufferedInputStream(telnet.getInputStream());
//            out = new PrintStream(telnet.getOutputStream());
//
//            LOG.debug("Logging in...");
//            tl1Login();
//
//            if (!att.containsKey("DID")) {
//                LOG.debug("Getting device identifications ");
//                getIdentification();
//            }
//
//            for (String commad : getOperation()) {
//                LOG.debug("Calling command in form:" + commad);
//                callCommand(commad);
//            }
//            if (!enrich.isEmpty()) {
//                LOG.debug("Calling enriching commands...");
//                enriching();
//            }
//
//        } catch (Exception e) {
//            LOG.error(e.toString());
//            resultCode = 1;
//            descriptionText = e.getMessage();
//        } finally {
//            LOG.debug("Logging out...");
//            tl1Logout();
//            disconnect();
//            LOG.debug(responseObject);
//            return createResponseObject(responseObject);
//        }
//    }
//
//    protected abstract Document createResponseObject(Map<String, String> responseObject) throws ResponseException;
//
//    private void enriching() throws IOException {
//        //enrich queue list of values
//        for (Map.Entry<String, String> entry : enrich.entrySet()) {
//            String parseEnrichingProperties = entry.getValue();
//            String parseEnrichingCommand = entry.getKey();
//
//            Map<String, String> preMergeOutput = new HashMap<>();
//            for (Map.Entry<String, String> searchEntry : responseObject.entrySet()) {
//                if (searchEntry.getKey().matches(parseEnrichingProperties.split("=")[0] + "#\\d+") && searchEntry.getValue().equalsIgnoreCase(parseEnrichingProperties.split("=")[1])) {
//                    String identificationIndex = searchEntry.getKey().substring(parseEnrichingProperties.split("=")[0].length());
//                    LOG.debug("Enriching data with command: " + parseEnrichingCommand);
//                    write(constructDynamicCommandFromBaseForGroups(parseEnrichingCommand, responseObject, identificationIndex));
//                    for (Map.Entry<String, String> fillingOutput : readTL1SucceedResponse(readUntil()).entrySet()) {
//                        preMergeOutput.put(fillingOutput.getKey().replaceFirst("#\\d+", "") + identificationIndex, fillingOutput.getValue());
//                    }
//                }
//                parseEnrichingCommand = entry.getKey();
//            }
//            responseObject.putAll(preMergeOutput);
//        }
//    }
//
//    private String constructDynamicCommandFromBaseForGroups(String command, Map<String, String> base, String group) {
//        Pattern pattern = Pattern.compile("(%\\w+%)");
//        Matcher matcher = pattern.matcher(command);
//        int index = 0;
//        while (matcher.find()) {
//            String forReplace = matcher.group(index);
//            command = command.replace(forReplace, base.get(forReplace.replaceAll("%", "") + group));
//        }
//        LOG.debug("Command: "+command);
//        return command;
//    }
//
//    private void callCommand(String command) throws IOException {
//        String cmd = constructDynamicCommandFromBaseForGroups(command, att, "");
//        write(cmd);
//        String succes = readUntil();
//        responseObject.putAll(readTL1SucceedResponse(succes));
//    }
//
//    private List<String> getOperation() {
//        List<String> list = new ArrayList<>();
//
//        NodeList children = xpath.from(doc).forExpression("/operations").asNodeList().item(0).getChildNodes();
//
//        for (int index = 0; index < children.getLength(); index++) {
//
//            if (children.item(index).getNodeType() == Node.ELEMENT_NODE && att.get("OPERATION").equalsIgnoreCase(children.item(index).getNodeName())) {
//                NodeList opeartionCommands = children.item(index).getChildNodes();
//                for (int command = 0; command < opeartionCommands.getLength(); command++) {
//                    if (opeartionCommands.item(command).getNodeType() == Node.ELEMENT_NODE) {
//                        if (opeartionCommands.item(command).hasAttributes()) {
//                            enrich.put(opeartionCommands.item(command).getTextContent(), opeartionCommands.item(command).getAttributes().item(0).getNodeValue());
//                        } else {
//                            list.add(opeartionCommands.item(command).getTextContent());
//                        }
//                    }
//                }
//            }
//        }
//        return list;
//    }
//
//    private void getIdentification() throws IOException {
//        String command = xpath.from(doc).forExpression("/operations/getAlias/command").asString();
//        write(constructDynamicCommandFromBaseForGroups(command, att, ""));
//        Map<String, String> device = readTL1SucceedResponse(readUntil());
//        att.put("DID", device.get("DID#0"));
//        att.put("FN", device.get("FN#0"));
//        att.put("SN", device.get("SN#0"));
//        att.put("PN", device.get("PN#0"));
//        att.put("ONTID", device.get("ONTID#0"));
////      LOG.debug("Reset groupIndex=0. No real group operation was called");
////      this operation shouldn't increase groupIndex because its just for getting required data from alias. -> andrej.socha@cgi.com
//        groupIndex=0;
//    }
//
//    private Map<String, String> readTL1SucceedResponse(String succeedMsg) {
//        Map<String, String> partialOutput = new HashMap<>();
//        String[] helpString = null;
//        String[] name = null;
//        String[] values = null;
//        Pattern pattern = Pattern.compile("-{9,}(.*)-{5}");
//        Matcher matcher = pattern.matcher(succeedMsg);
//        if (matcher.find()) {
//            helpString = matcher.group(1).trim().split("\\s\\s");
//            if (helpString.length == 3) {
//                name = helpString[0].split("\\t");
//                values = helpString[1].split("\\t");
//                for (int i = 0; i < name.length; i++) {
////                    LOG.debug(groupIndex);
//                    partialOutput.put(name[i]+"#"+groupIndex, values[i]);
////                    partialOutput.put(name[i], values[i]);
//                }
//                groupIndex++;
//            } else {
//                String[][] multiVlaues = new String[helpString.length - 2][];
//                name = helpString[0].split("\\t");
//                for (int index = 0; index < multiVlaues.length; index++) {
//                    multiVlaues[index] = helpString[index + 1].split("\\t");
//                }
//                for (int i = 0; i <= multiVlaues.length - 1; i++) {
//                    for (int j = 0; j < name.length; j++) {
//                        partialOutput.put(name[j] + "#"+groupIndex, multiVlaues[i][j]);
//                    }
//                    groupIndex++;
//                }
//            }
//        }
//        LOG.debug("Operation output: " + partialOutput);
//        return partialOutput;
//    }
//
//    private void tl1Login() throws InterruptedException, IOException {
//        write("LOGIN:::CTAG::UN=" + getUname() + ",PWD=" + getUpwd() + ";");
//        readUntil();
//    }
//
//    private String getUpwd() {
//        try {
//            return String.valueOf(ObfuscationEngine.decrypt(att.get("USER-PWD")));
//        } catch (AXSecurityException e) {
//            LOG.error(e.toString());
//            return att.get("USER-PWD");
//        }
//    }
//
//    private String getUname() {
//        try {
//            return String.valueOf(ObfuscationEngine.decrypt(att.get("USER-NAME")));
//        } catch (AXSecurityException e) {
//            LOG.error(e.toString());
//            return att.get("USER-NAME");
//        }
//    }
//
//    private void tl1Logout() {
//        write("LOGOUT:::CTAG::;");
//    }
//
//    public String readUntil() throws IOException {
//        StringBuilder sb = new StringBuilder();
//        while (sb.toString().equals("") || !sb.toString().endsWith(";")) {
//            while (bin.available() > 0) {
//                char c = (char) bin.read();
//                sb.append(c);
//            }
//        }
//        String out = sb.toString().replace("\n", " ").replace("\r", " ");
//        if (out.matches(".+ENDESC=Succeeded.+") || out.equals("")) {
//            return out;
//        } else {
////            Called command failed to response with success
//            throw new IOException("Called command failed: " + out);
//        }
//    }
//
//    @Deprecated
//    public String readUntilOld() throws IOException {
//        long lastTime = System.currentTimeMillis();
//        StringBuilder sb = new StringBuilder();
//        while (true) {
//            int c = -1;
//            byte[] text;
//            if (in.available() > 0) {
//                c = in.read(text = new byte[in.available()]);
//                sb.append(new String(text));
//            }
//            long now = System.currentTimeMillis();
//            if (c != -1) {
//                lastTime = now;
//            }
//            if (now - lastTime > timeout) {
//                break;
//            }
//            try {
//                Thread.sleep(50);
//            } catch (Exception e) {
//            }
//        }
//        String out = sb.toString().replace("\n", " ").replace("\r", " ");
//        if (out.matches(".+ENDESC=Succeeded.+") || out.equals("")) {
//            return out;
//        } else {
//            throw new IOException("Called command failed: " + out);
//        }
//    }
//
//    //this method writes to server, but waits no prompt.
//    public void write(String value) {
//        try {
//            out.println(value);
//            out.flush();
//        } catch (Exception e) {
//            LOG.error(e.toString());
//        }
//    }
//
//    //closes a this client. you may want to send command "exit" before
//    public void disconnect() {
//        try {
//            if (telnet != null) telnet.disconnect();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//
//}