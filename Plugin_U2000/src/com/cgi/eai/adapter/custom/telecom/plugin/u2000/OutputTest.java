package com.cgi.eai.adapter.custom.telecom.plugin.u2000;

/**
 * Created 27. 5. 2015.
 *
 * @author JLyc
 */
public class OutputTest {

    static String testOutput =
            "   7340036 2015-05-27 09:01:51\n" +
                    "M  1 COMPLD\n" +
                    "   EN=0   ENDESC=Succeeded.\n" +
                    "   blktag=1\n" +
                    "   blkcount=7\n" +
                    "   blktotal=7\n" +
                    "\n" +
                    "Gpon ont port information of the device\n" +
                    "-----------------------------------------------------------------\n" +
                    "DID     FN      SN      PN      ONTID   ONTPORTTYPE     ONTPORTID       ONTPSTAT\n" +
                    "7340036 0       18      15      3       POTS    1       --\n" +
                    "7340036 0       18      15      3       POTS    2       --\n" +
                    "7340036 0       18      15      3       IPHOST  1       --\n" +
                    "7340036 0       18      15      3       ETH     1       Activating\n" +
                    "7340036 0       18      15      3       ETH     2       Activating\n" +
                    "7340036 0       18      15      3       ETH     3       Activating\n" +
                    "7340036 0       18      15      3       ETH     4       Activating\n" +
                    "-----------------------------------------------------------------\n";

    public static void main(String[] args) {
        testOutput.contains("ENDESC=Succeeded");

//        testOutput.replaceAll("adf");



    }
}
