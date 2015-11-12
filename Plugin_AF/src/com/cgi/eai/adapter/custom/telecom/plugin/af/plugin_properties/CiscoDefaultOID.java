package com.cgi.eai.adapter.custom.telecom.plugin.af.plugin_properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by JLyc on 8. 4. 2015.
 */
public class CiscoDefaultOID {
    private static final Log LOG = LogFactory.getLog(CiscoDefaultOID.class);
    private static Map<String, String> responseObject = new HashMap<>();

    public static Map<String, String> loadOIDTranslator() {
        Path oidTranslationPropertiesFile = FileSystems.getDefault().getPath(System.getProperty("user.dir"), "plugin", "CiscoOIDTranslations.prop");
        try {
            BufferedReader readProperties = new BufferedReader(new FileReader(oidTranslationPropertiesFile.toFile()));
            String _line;
            while ((_line = readProperties.readLine()) != null) {
                String[] _split = _line.split("=");
                responseObject.put(_split[0], _split[1]);
            }
        } catch (IOException e) {
            LOG.warn("Unable read property file. Using default hard coded", e);
            getValues();
        }
        return responseObject;
    }

    private static void getValues() {
        responseObject.put("1.3.6.1.4.1.9.9.46.1.3.1.1.1","vtpVlanIndex");
        responseObject.put("1.3.6.1.4.1.9.9.46.1.3.1.1.2","vtpVlanState");
        responseObject.put("1.3.6.1.4.1.9.9.46.1.3.1.1.3","vtpVlanType");
        responseObject.put("1.3.6.1.4.1.9.9.46.1.3.1.1.4","vtpVlanName");
        responseObject.put("1.3.6.1.4.1.9.9.46.1.3.1.1.5","vtpVlanMtu");
        responseObject.put("1.3.6.1.4.1.9.9.46.1.3.1.1.6","vtpVlanDot10Said");
        responseObject.put("1.3.6.1.4.1.9.9.46.1.3.1.1.7","vtpVlanRingNumber");
        responseObject.put("1.3.6.1.4.1.9.9.46.1.3.1.1.8","vtpVlanBridgeNumber");
        responseObject.put("1.3.6.1.4.1.9.9.46.1.3.1.1.9","vtpVlanStpType");
        responseObject.put("1.3.6.1.4.1.9.9.46.1.3.1.1.10","vtpVlanParentVlan");
        responseObject.put("1.3.6.1.4.1.9.9.46.1.3.1.1.11","vtpVlanTranslationalVlan1");
        responseObject.put("1.3.6.1.4.1.9.9.46.1.3.1.1.12","vtpVlanTranslationalVlan2");
        responseObject.put("1.3.6.1.4.1.9.9.46.1.3.1.1.13","vtpVlanBridgeType");
        responseObject.put("1.3.6.1.4.1.9.9.46.1.3.1.1.14","vtpVlanAreHopCount");
        responseObject.put("1.3.6.1.4.1.9.9.46.1.3.1.1.15","vtpVlanSteHopCount");
        responseObject.put("1.3.6.1.4.1.9.9.46.1.3.1.1.16","vtpVlanIsCRFBackup");
        responseObject.put("1.3.6.1.4.1.9.9.46.1.3.1.1.17","vtpVlanTypeExt");
        responseObject.put("1.3.6.1.4.1.9.9.46.1.3.1.1.18","vtpVlanIfIndex");
    }
}
