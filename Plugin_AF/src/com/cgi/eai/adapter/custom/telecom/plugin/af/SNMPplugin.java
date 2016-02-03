package com.cgi.eai.adapter.custom.telecom.plugin.af;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.DefaultPDUFactory;
import org.snmp4j.util.TableEvent;
import org.snmp4j.util.TableUtils;
import org.w3c.dom.*;
import telecom.core.CustomAdapterInterface;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by JLyc on 9. 4. 2015.
 */
public abstract class SNMPplugin implements CustomAdapterInterface {
    private static final Log LOG = LogFactory.getLog(SNMPplugin.class);


    protected static Map<String, String> oidTranslator;
    protected Map<String, String> responseObject = new HashMap<>();
    protected Map<String, String> att;


    /**
     * Create communication chanel and return answer from external system
     * @param att request Map object
     * @return response Map object
     * @throws Exception in case of fail message response
     */
    public Document request(Map<String, String> att) throws Exception {
        this.att = att;

        CommunityTarget comtarget = getCommunityTarget();
        TransportMapping<?> transport = new DefaultUdpTransportMapping();
        transport.listen();
        Snmp snmp = new Snmp(transport);

        TableUtils utils = new TableUtils(snmp, new DefaultPDUFactory(PDU.GETBULK));
        utils.setMaxNumRowsPerPDU(20);

        System.out.println(getOID());

        OID[] columnOids = new OID[] {
                new OID(getOID()),
        };

        LOG.debug("Sending walk Request to Agent...");
        List<TableEvent> list = utils.getTable(comtarget, columnOids, new OID("1"), new OID("10"));

        for (TableEvent e : list) {
            fillObject(e.getColumns()[0]);
        }
        snmp.close();

        return getResponseObject();
    }

    /**
     * Translation oid indemnification to human defined names from translation config
     *
     * @param variableBinding values from device
     */
    private void fillObject(VariableBinding variableBinding) {
            String keyValue = (oidTranslator.get(variableBinding.getOid().toDottedString()));
            if (keyValue == null) {
                keyValue = "unknown_element" + (responseObject.size()+1);
            }
            responseObject.put(keyValue, variableBinding.toValueString());
    }

    /**
     * @return oid-value translate MIB name to OID value
     */
    protected String getOID() {
        String oid = ".0";
        if(att.get("oid-value").matches("(\\.?(\\d{1,})*)+")){
            return att.get("oid-value");
        }
        for (Map.Entry<String, String> entry : oidTranslator.entrySet()) {
            oid = entry.getValue().equalsIgnoreCase(att.get("oid-value")) ? entry.getKey() : ".0";
        }
        LOG.debug("OID request translated to: "+oid);
        return oid;
    }

    /**
     * Create end point by provided information
     *
     * @return CommunityTarget end point
     */
    protected CommunityTarget getCommunityTarget() {
        CommunityTarget comtarget = new CommunityTarget();
        comtarget.setCommunity(new OctetString(att.get("community")));
        comtarget.setVersion(Integer.valueOf(att.get("snmp-version")));
        comtarget.setAddress(new UdpAddress(att.get("ip-address") + "/" + att.get("port")));
        comtarget.setRetries(Integer.valueOf(att.get("retries-count")));
        comtarget.setTimeout(Integer.valueOf(att.get("timeout")));
        LOG.debug("Community target created.");
        return comtarget;
    }

    protected Map<String, String> getResponseObject(){
        return responseObject;
    }
}
