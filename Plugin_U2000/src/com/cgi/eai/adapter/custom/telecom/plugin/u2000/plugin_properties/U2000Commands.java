//package com.cgi.eai.adapter.custom.telecom.plugin.u2000.plugin_properties;
//
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
//
//import java.io.BufferedReader;
//import java.io.FileReader;
//import java.io.IOException;
//import java.nio.file.FileSystems;
//import java.nio.file.Path;
//import java.util.HashMap;
//import java.util.Map;
//
///**
// * Created by JLyc on 8. 4. 2015.
// */
//public class U2000Commands {
//    private static final Log LOG = LogFactory.getLog(U2000Commands.class);
//    private static Map<String, String> responseObject = new HashMap<>();
//
//    public static Map<String, String> loadU2000Commands() {
//        Path u2000Commands = FileSystems.getDefault().getPath(System.getProperty("user.dir"), "config", "U2000Commands.prop");
//        try {
//            BufferedReader readProperties = new BufferedReader(new FileReader(oidTranslationPropertiesFile.toFile()));
//            String _line;
//            while ((_line = readProperties.readLine()) != null) {
//                String[] _split = _line.split("=");
//                responseObject.put(_split[0], _split[1]);
//            }
//        } catch (IOException e) {
//            LOG.warn("Unable read property file. Using default hard coded", e);
//            getValues();
//        }
//        return responseObject;
//    }
//
//    private static void getValues() {
//
//    }
//}
