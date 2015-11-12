package com.cgi.eai.adapter.custom.telecom.plugin.SNMP;


import com.cgi.eai.adapter.custom.telecom.plugin.SNMP.plugin_properties.WeatherProbesDefaultOID;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;

/**
 * Created by JLyc on 27. 3. 2015.
 */

// translate oid to stringl

public class WeatherProbes extends SNMPplugin {
    private static final Log LOG = LogFactory.getLog(WeatherProbes.class);

    private static Map<String, String> oidTranslator = WeatherProbesDefaultOID.loadOIDTranslator();

    public WeatherProbes() {
    }
}
