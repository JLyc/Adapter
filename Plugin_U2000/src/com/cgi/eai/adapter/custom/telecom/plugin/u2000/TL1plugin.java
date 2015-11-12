package com.cgi.eai.adapter.custom.telecom.plugin.u2000;

import com.tibco.security.*;
import org.apache.commons.net.telnet.*;
import telecom.core.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.*;

//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;


/**
 * Created by JLyc on 10. 4. 2015.
 */
public abstract class TL1plugin implements CustomAdapterInterface {
//    private static final Log LOG = LogFactory.getLog(TL1plugin.class);

    protected static Map<java.lang.String, String> CommandTranslator;
    protected Map<String, String> responseObject = new HashMap<>();
    protected Map<String, String> att;

    private TelnetClient telnet;
    private Socket socket;
    private InputStream in= null;
    private PrintStream out=null;
    private long timeout = 1000l;

    @Override
    public Map<String, String> request(Map<String, String> att) throws Exception {
        this.att = att;

        telnet = new TelnetClient();
        telnet.connect(att.get("ip-address"), Integer.parseInt(att.get("port")));
        in = telnet.getInputStream();
        out = new PrintStream(telnet.getOutputStream());
        try {

            write("LOGIN:::CTAG::UN=" + getUname()+ ",PWD=" + getUpwd() + ";");
            TimeUnit.MILLISECONDS.sleep(500);
            String output = readUntil();
            if(!output.matches(".+ENDESC=Succeeded.+"))
            {
                System.out.println(output);
            }
            //status
            write("LST-ONTRUNINFO::ALIAS=48575443EBFAA923:1::;");
//            write("LST-ONTRUNINFO::ALIAS=32303131E487BA41:1::;");
            //config
//            write("LST-ONTDETAIL::ALIAS=32303131E487BA41:1::;");
//            write("LST-ONTDDMDETAIL::ALIAS=32303131E487BA41:1::;");
//            write("LST-ONT::DID=7340036,FN=0,SN=18,PN=15,ONTID=3:1::;");
            //upstream
//            write("LST-ONTPORT::ALIAS=32303131E487BA41:2::;");
//            write("LST-ONTPORT::ALIAS=32303131E487BA41,ONTPORTTYPE=ETH,ONTPORTID=3:1::;");
//            write("LST-ONTPORTDETAIL::ALIAS=32303131E487BA41,ONTPORTTYPE=IPHOST,ONTPORTID=1:1::;");
//            write("LST-ONTDBAPROF::DID=7340036,FN=0,SN=18,PN=15,ONTID=3:1::;");
//            write("LST-ONTPORT::ALIAS=32303131E487BA41:2::;");
            TimeUnit.MILLISECONDS.sleep(1000);
//            System.out.println("ErrorHunter");
            output = readUntil();
            if(output.matches(".+ENDESC=Succeeded.+"))
            {
                System.out.println("output");
                printOutput(output);
            }else{
                System.out.println(output);
            }

            write("LOGOUT:::CTAG::;");
        } catch (Exception e) {

        }
        return null;
    }

    private Map<String, String> readTL1SucceedResponse(String succeedMsg){
        Map<String, String> parcialOutput = new HashMap<>();
        String[] helpString = null;
        String[] name = null;
        String[] values = null;
        Pattern pattern = Pattern.compile("-{9,}(.*)-{3}");
        Matcher matcher = pattern.matcher(succeedMsg);
        if(matcher.find()){
            helpString = matcher.group(1).trim().split("\\s\\s");
            name = helpString[0].split("\\t");
            values = helpString[1].split("\\t");
        }
        for(int i = 0 ; i<name.length; i++){
            parcialOutput.put(name[i], values[i]);
        }
        return parcialOutput;
    }
//  LST-ONTRUNINFO::ALIAS=48575443EBFAA923:1::;
    private void getDeviceData(Map<String,String> input) throws IOException {
        if(att.containsKey("allias")){
            for(Map.Entry inputEntry : input) {
                String command = att.get("command");
                command.replace("%" + inputEntry.getKey() + "%", (CharSequence) inputEntry.getValue());
//            write();
            }
        }
        write("LST-ONTRUNINFO::ALIAS=48575443EBFAA923:1::;");

    }


    private Map<String, String> outputMap = new HashMap<>();

    private void printOutput(String output) {
        String[] helpString = null;
        String[] name = null;
        String[] values = null;
        Pattern pattern = Pattern.compile("-{9,}(.*)-{3}");
        Matcher matcher = pattern.matcher(output);
        if(matcher.find()){
            helpString = matcher.group(1).trim().split("\\s\\s");
            name = helpString[0].split("\\t");
            values = helpString[1].split("\\t");
        }
        for(int i = 0 ; i<name.length; i++){
            String duplicite = outputMap.put(name[i], values[i]);
            if(duplicite!=null){
                String uniquebName = name[i];
                while(outputMap.put(uniquebName,values[i])!=null){
                    uniquebName += ".1";
                }
            }
            System.out.println(name[i]+" : "+ values[i]);
        }
    }

    private void printtable(){
        for(Map.Entry entry : outputMap.entrySet()){
            System.out.println(entry.getKey()+" : "+entry.getValue());
        }
    }


    private String getUpwd(){
        try {
            return String.valueOf(ObfuscationEngine.decrypt(att.get("user-pwd")));
        } catch (AXSecurityException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getUname(){
        try {
            return String.valueOf(ObfuscationEngine.decrypt(att.get("user-name")));
        } catch (AXSecurityException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getCommand() {
        //TODO
        return "LST-ONTRUNINFO::ALIAS=48575442327A5329:1::;";
    }

    public String readUntil() throws IOException {
        long lastTime = System.currentTimeMillis();
        StringBuilder sb = new StringBuilder();
        while (true) {
            int c = -1;
            byte[] text;
            if (in.available() > 0) {
                c = in.read(text = new byte[in.available()]);
                sb.append(new String(text));
            }
            long now = System.currentTimeMillis();
            if (c != -1) {
                lastTime = now;
            }
            if (now - lastTime > timeout) {
                break;
            }
            try {
                Thread.sleep(50);
            } catch (Exception e) {
            }
        }
        return sb.toString().replace("\n", " ").replace("\r", " ");
    }

    //this method writes to server, but waits no prompt.
    public void write(String value) {
        try {
            out.println(value);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //closes a this client. you may want to send command "exit" beforehand
    public void disconnect() {
        try {
            if (socket != null)
                socket.close();
            if (telnet != null)
                telnet.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}