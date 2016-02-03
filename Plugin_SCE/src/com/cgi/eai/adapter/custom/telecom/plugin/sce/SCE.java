package com.cgi.eai.adapter.custom.telecom.plugin.sce;

import org.w3c.dom.*;
import telecom.core.CustomAdapterInterface;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by JLyc on 8. 4. 2015.
 */
public class SCE implements CustomAdapterInterface {

    @Override
    public Document request(Map<String, String> att) {
        Map<String, String> dvalue = new HashMap<>();
//        SMNonBlockingApi smnbapi = new SMNonBlockingApi();
//        try {
//            smnbapi.connect(att.get("10.159.61.42"),14374);
//            smnbapi.connect(att.get("ip-address"),att.get("port"));
//
//        }
//        finally {
//            smnbapi.disconnect();
//        }
        return dvalue;
    }
}
