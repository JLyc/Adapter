package com.cgi.eai.adapter.custom.telecom.plugin.SNMP.plugin_properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.String;import java.lang.System;import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by JLyc on 8. 4. 2015.
 */
public class WeatherProbesDefaultOID {
    private static final Log LOG = LogFactory.getLog(WeatherProbesDefaultOID.class);
    private static Map<String, String> responseObject = new HashMap<>();

    public static Map<String, String> loadOIDTranslator() {
        Path oidTranslationPropertiesFile = FileSystems.getDefault().getPath(System.getProperty("user.dir"), "plugin", "WeatherProbesOIDTranslations.prop");
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
        responseObject.put("1.3.6.1.4.1.27070.3.4.1.2.1.1.1", "s2RxIndex");
        responseObject.put("1.3.6.1.4.1.27070.3.4.1.2.1.2.1", "s2RxNwTxIndex");
        responseObject.put("1.3.6.1.4.1.27070.3.4.1.2.1.3.1", "s2RxPhysicalPort");
        responseObject.put("1.3.6.1.4.1.27070.3.4.1.2.1.4.1", "s2RxFrequency");
        responseObject.put("1.3.6.1.4.1.27070.3.4.1.2.1.5.1", "s2RxLnbVoltage");
        responseObject.put("1.3.6.1.4.1.27070.3.4.1.2.1.6.1", "s2Rx22kHzTone");
        responseObject.put("1.3.6.1.4.1.27070.3.4.1.2.1.7.1", "s2RxStatus");
        responseObject.put("1.3.6.1.4.1.27070.3.4.1.2.1.8.1", "s2RxRfLeve");
        responseObject.put("1.3.6.1.4.1.27070.3.4.1.2.1.9.1", "s2RxSNR");
        responseObject.put("1.3.6.1.4.1.27070.3.4.1.2.1.10.1", "s2RxBER");
        responseObject.put("1.3.6.1.4.1.27070.3.4.1.2.1.11.1", "s2RxMER");
        responseObject.put("1.3.6.1.4.1.27070.3.4.1.2.1.12.1", "s2RxDvbStandard");
        responseObject.put("1.3.6.1.4.1.27070.3.4.1.2.1.13.1", "s2RxModulation");
        responseObject.put("1.3.6.1.4.1.27070.3.4.1.2.1.14.1", "s2RxPunctureRate");
        responseObject.put("1.3.6.1.4.1.27070.3.4.1.2.1.15.1", "s2RxFecLength");
        responseObject.put("1.3.6.1.4.1.27070.3.4.1.2.1.16.1", "s2RxSymbolrate");
        responseObject.put("1.3.6.1.4.1.27070.3.4.1.2.1.17.1", "s2RxBitrate");
    }
}
