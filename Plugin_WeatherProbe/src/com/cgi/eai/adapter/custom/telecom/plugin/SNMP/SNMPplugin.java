package com.cgi.eai.adapter.custom.telecom.plugin.SNMP;

import org.apache.commons.logging.*;
import org.snmp4j.*;
import org.snmp4j.event.*;
import org.snmp4j.smi.*;
import org.snmp4j.transport.*;
import org.snmp4j.util.*;
import org.w3c.dom.*;
import telecom.core.*;

import java.io.*;
import java.util.*;

/**
 * Created by JLyc on 9. 4. 2015.
 */
public abstract class SNMPplugin implements CustomAdapterInterface {
    private static final Log LOG = LogFactory.getLog(SNMPplugin.class);

    protected Map<String, String> oidTranslator = loadOidTranslator();
    protected Map<String, String> responseObject = new HashMap<>();
    protected Map<String, String> att;

    protected int resultCode = 0;
    protected String descriptionText = "success";

    /**
     * Implementation of algorithm translating OID dotted string to MIB names
     *
     * @return Map of OID corresponding to MIB names
     */
    public abstract Map<String, String> loadOidTranslator();

    /**
     * Create communication chanel and return answer from external system
     *
     * @param att request Map object
     * @return response Map object
     * @throws Exception in case of fail message response
     */
    public Document request(Map<String, String> att) {
        try {
            this.att = new HashMap<>(att);
            String requestType = att.get("REQUEST-TYPE");
            CommunityTarget comtarget = getCommunityTarget();
            TransportMapping<?> transport = new DefaultUdpTransportMapping();
            transport.listen();
            Snmp snmp = new Snmp(transport);
            snmp.listen();

            String oid = getOID();
            OID[] columnOids = new OID[]{new OID(oid)};
            switch (requestType) {
                case "GET":
                    LOG.debug("Sending GET Request to Agent...");
                    Vector<? extends VariableBinding> singleResponse = snmpGet(comtarget, snmp, columnOids[0].toDottedString());
                    if (singleResponse != null) {
                        LOG.debug("Processing received data...\n"+singleResponse);
                        for (VariableBinding e : singleResponse) {
                            fillObject(e);
                        }
                    }else{
                        resultCode=1;
                        descriptionText = "No data found by " + requestType;
                    }
                    break;
                case "GETWALK":
                    LOG.debug("Sending GETWALK Request to Agent...");
                    TableUtils utils = new TableUtils(snmp, new DefaultPDUFactory(PDU.GETBULK));
                    utils.setMaxNumRowsPerPDU(100);
                    List<TableEvent> list = utils.getTable(comtarget, columnOids, new OID("1"), new OID(att.get("OID-MAX")));
                    if(list.get(0).getIndex()!=null){
                        for (TableEvent e : list) {
                            LOG.debug("Processing received data...\n" + list);
                            fillObject(e.getColumns()[0]);
                        }
                    }else{
                        resultCode=1;
                        descriptionText = "No data found by " + requestType;
                    }
                    break;
                case "WALK":
                        LOG.debug("Sending walk Request by config to Agent...");
                        manualTableWalk(comtarget, snmp, oid);
                    if (responseObject.size()==0){
                        resultCode=1;
                        descriptionText = "No data found by " + requestType;
                    }
                        break;
                default:
                    resultCode=2;
                    descriptionText = "No valid: " + requestType;
            }

            snmp.close();
        } catch (IOException e) {
            LOG.error(e);
            resultCode = 1;
            descriptionText = e.getMessage();
            errorResponse(e);
        }
        return createResponseObject(responseObject);
    }

    private Vector<? extends VariableBinding> snmpGet(CommunityTarget comtarget, Snmp snmp, String oid) throws IOException {
        PDU pdu = new PDU();
        pdu.add(new VariableBinding(new OID(oid)));
        pdu.setType(PDU.GET);
        String requestID = oid.replaceAll("\\.", "");
        pdu.setRequestID(new Integer32(Integer.valueOf(requestID.substring(requestID.length() - 6))));

        ResponseEvent response = snmp.get(pdu, comtarget);
        PDU responsePDU = response.getResponse();
        if (responsePDU == null || responsePDU.getVariableBindings().get(0).toValueString().equals("noSuchObject")) {
            return null;
        } else {
            return responsePDU.getVariableBindings();
        }
    }

    private void manualTableWalk(CommunityTarget comtarget, Snmp snmp, String oid) throws IOException {
        List<String> oidTableList = new ArrayList<>();
        for (Map.Entry<String, String> entry : oidTranslator.entrySet()) {
            if (entry.getKey().matches(oid + ".{0," + Integer.valueOf(att.get("OIDDEPTH")) * 2 + "}")) {
                oidTableList.add(entry.getKey());
            }
        }
        for (String oidEntry : oidTableList) {
            Vector<? extends VariableBinding> singleResponse = snmpGet(comtarget, snmp, oidEntry);
            if (singleResponse == null) {
                continue;
            }
            for (VariableBinding e : singleResponse) {
                fillObject(e);
            }
        }
    }

    /**
     * Construct error message for response
     *
     * @param e Exception cause of error
     * @return Exception message as Map<>
     */
    private void errorResponse(Exception e) {
        resultCode = 0;
        descriptionText = e.getMessage();
    }

    /**
     * Translation oid indemnification to human defined names from translation config
     *
     * @param variableBinding values from device
     */
    private void fillObject(VariableBinding variableBinding) {
        String keyValue = (oidTranslator.get(variableBinding.getOid().toDottedString()));
        if (keyValue == null) {
            LOG.debug("OID not in translation table: " + variableBinding.getOid().toDottedString());
            keyValue = "unknown_element_uid" + (responseObject.size() + 1);
        }
        responseObject.put(keyValue, variableBinding.toValueString());
        LOG.debug("OID translation to: " + variableBinding.toValueString());
    }

    /**
     * @return oid-value translate MIB name to OID value
     */
    protected String getOID() {
        String oid = ".0";
        if (att.get("OID-NAME").matches("(\\.?(\\d{1,})*)+")) {
            return att.get("OID-NAME");
        }
        if (oidTranslator.containsValue(att.get("OID-NAME"))) {
            for (Map.Entry<String, String> entry : oidTranslator.entrySet()) {
                if (entry.getValue().equalsIgnoreCase(att.get("OID-NAME"))) {
                    oid = entry.getKey();
                    break;
                }
            }
        } else {
            LOG.error("Unable translate OID-NAME:" + att.get("OID-NAME") + ", to dotted string");
            throw new NoSuchElementException("Unable translate OID-NAME:" + att.get("OID-NAME") + ", to dotted string");
        }

        return oid;
    }

    /**
     * Create end point by provided information
     *
     * @return CommunityTarget end point
     */
    protected CommunityTarget getCommunityTarget() {
        CommunityTarget comtarget = new CommunityTarget();
        comtarget.setCommunity(new OctetString(att.get("COMMUNITY")));
        comtarget.setVersion(Integer.valueOf(att.get("SNMP-VERSION")));
        comtarget.setAddress(new UdpAddress(att.get("IP-ADDRESS") + "/" + att.get("PORT")));
        comtarget.setRetries(Integer.valueOf(att.get("RETRIES-COUNT")));
        comtarget.setTimeout(Integer.valueOf(att.get("TIMEOUT")));
        LOG.debug("Community target created.");
        return comtarget;
    }

    protected abstract Document createResponseObject(Map<String, String> responseObject);
}
