package com.cgi.eai.adapter.custom.telecom.plugin.sce;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;

/**
 *
 * Samotna implementacia servisu pre volania subscriber managera
 *
 * @author bkontur
 */
public class SubscriberManagerServiceImpl implements SubscriberManagerService {

    //instancia loggera
    private static final Logger logger = Logger.getLogger(SubscriberManagerServiceImpl.class);
    //konfiguracia pripojenia na SSH
    private String host;
    private Integer port;
    private String userName;
    private String password;
    //samotne skripty ktore mama tiez v konfiguraku
    private String commandSetPolicy;
    private String commandRemovePolicy;
    private String commandSetAdditivePolicy;
    private String commandAddQuota;
    private String commandShowQuota;
    private Boolean doNotExecuteCommands;

    public SubscriberManagerServiceImpl() {
        super();
    }

    public ModificationResultEnum setPolicy(ServiceTypeEnum serviceType, String subscriberLogin, String ipAddress, String policyId) {
        logger.debug("setPolicy(subscriberLogin=" + subscriberLogin + ", ipAddress=" + ipAddress + ", policyId=" + policyId + ")");

        String command = null;

        switch (serviceType) {
            case PARENTAL_CONTROL:
            case BOSS_CONTROL:
                command = MessageFormat.format(getCommandSetPolicy(), subscriberLogin, ipAddress, policyId);
                break;
            case PACKET_PRIORITIZATION:
                command = MessageFormat.format(getCommandSetAdditivePolicy(), subscriberLogin, ipAddress, policyId);
                break;
            default:
                throw new IllegalArgumentException("unsupported serviceType=" + serviceType);
        }

        return executeCommand(command).getCommandResult();
    }

    public ModificationResultEnum removePolicy(String subscriberLogin) {
        logger.debug("removePolicy(subscriberLogin=" + subscriberLogin + ")");

        String command = MessageFormat.format(getCommandRemovePolicy(), subscriberLogin);

        return executeCommand(command).getCommandResult();
    }

    public ModificationResultEnum addQuota(String subscriberLogin, Long uploadQuota, Long downloadQuota) {
        logger.debug("addQuota(subscriberLogin=" + subscriberLogin + ", uploadQuota=" + uploadQuota + ", downloadQuota=" + downloadQuota + ")");

        String command = getCommandAddQuota();
        command = command.replace("{0}", subscriberLogin);
        command = command.replace("{1}", uploadQuota.toString());
        command = command.replace("{2}", downloadQuota.toString());
        //MessageFormat.format(getCommandAddQuota(), subscriberLogin, uploadQuota, downloadQuota); toto mi rozmrdava tie longy:
        //16:06:34,224 DEBUG [sk.tempest.st.ditra.wp5.pccb.core.service.sm.impl.SubscriberManagerServiceImpl] - addQuota(subscriberLogin=luki@dslmini, uploadQuota=1048576, downloadQuota=1048576)|#]
        //[#|2011-11-25T16:06:34.224+0100|INFO|sun-appserver-ee8.1_02|javax.enterprise.system.stream.out|_ThreadID=13;|2011-11-25 16:06:34,224 DEBUG [sk.tempest.st.ditra.wp5.pccb.core.service.sm.impl.SubscriberManagerServiceImpl] - executeCommand(command=/opt/pcube/sm/server/bin/p3qm --add-quota -s luki@dslmini -b 1=1,048,576,2=1,048,576)|#]

        return executeCommand(command).getCommandResult();
    }

    /**
     * Vyparsuje zostavajucu kvotu z niecoho takehoto:
     *
     * pcube@sm1::~ p3qm --show-quota -s luki@comfortmini
     *
     * Package ID = 58
     * Last SCE that Reported quota = 192.168.217.17
     *
     * Aggregation Period:
     * Last Replenish Time = Mon Dec 05 09:13:37 CET 2011
     * Aggregation Period End = Sun Jan 01 07:24:01 CET 2012
     *
     * Quota Buckets:
     * Bucket 1 - Quota size = 2200000, Remaining Quota = -10149, Last quota reported by SCE = -1
     * Bucket 2 - Quota size = 2200000, Remaining Quota = -11143, Last quota reported by SCE = -33
     *
     * Penalty start = TIME_NOT_SET
     * Next penalty monitor = TIME_NOT_SET
     * Command terminated successfully
     */
    private static long parseRemainingQuota(String commandResult) {
        int start = commandResult.indexOf("Remaining Quota = ");
        if (start < 0) {
            throw new IllegalArgumentException("neparsovatelne:'" + commandResult + "' (nenasiel som retazec 'Remaining Quota = '");
        }
        start += 18; //pridam 18 znakov - tolko ma "Remaining Quota = "
        start = commandResult.indexOf("Remaining Quota = ", start); //hladam druhy vyskyt, prvy je upload, mna zaujima download
        if (start < 0) {
            throw new IllegalArgumentException("neparsovatelne:'" + commandResult + "' (nenasiel som druhy vyskyt retazca 'Remaining Quota = '");
        }
        start += 18;
        int end = commandResult.indexOf(",", start);
        if (end < 0) {
            throw new IllegalArgumentException("neparsovatelne:'" + commandResult + "' (nenasiel som ciarku za hodnotou kvoty)");
        }
        String downloadQuota = commandResult.substring(start, end);
        logger.debug("vyparsovana kvota='" + downloadQuota + "'");

        return Long.parseLong(downloadQuota);
    }

    /**
     * Zistenie zostavajucej kvoty.
     */
    public long getRemainingQuota(String subscriberLogin) {
        logger.debug("getRemainingQuota(subscriberLogin=" + subscriberLogin + ")");

        String command = MessageFormat.format(getCommandShowQuota(), subscriberLogin);

        CommandResult cr = executeCommand(command);
        if (cr.getCommandResult().equals(ModificationResultEnum.NOT_OK)) {
            logger.error("nepodarilo sa zistit kvotu pre subscriberLogin=" + subscriberLogin + " s vysledkom=" + cr.getCommandResultString() + ", vraciam 0");

            return 0L;
        }

        return parseRemainingQuota(cr.getCommandResultString());
    }

    /**
     * @return the doNotExecuteCommands
     */
    public Boolean getDoNotExecuteCommands() {
        return doNotExecuteCommands;
    }

    /**
     * @param doNotExecuteCommands the doNotExecuteCommands to set
     */
    public void setDoNotExecuteCommands(Boolean doNotExecuteCommands) {
        this.doNotExecuteCommands = doNotExecuteCommands;
    }

    private static class CommandResult {
        private ModificationResultEnum commandResult;
        private String commandResultString;

        public CommandResult(ModificationResultEnum commandResult, String commandResultString) {
            super();
            this.commandResult = commandResult;
            this.commandResultString = commandResultString;
        }

        //<editor-fold defaultstate="collapsed" desc="standardne gettery / settery">
        public ModificationResultEnum getCommandResult() {
            return commandResult;
        }

        public void setCommandResult(ModificationResultEnum commandResult) {
            this.commandResult = commandResult;
        }

        public String getCommandResultString() {
            return commandResultString;
        }

        public void setCommandResultString(String commandResultString) {
            this.commandResultString = commandResultString;
        }
        //</editor-fold>
    }

    protected CommandResult executeCommand(String command) {
        logger.debug("executeCommand(command=" + command + ")");
        if (getDoNotExecuteCommands()) {
            logger.warn("nastavenie doNotExecuteCommands je nastavene na hodnotu true, ziadne prikazy neposielam do SM a priamo vraciam vysledok OK");
            return new CommandResult(ModificationResultEnum.OK, "nastavenie doNotExecuteCommands je nastavene na hodnotu true, ziadne prikazy neposielam do SM a priamo vraciam vysledok OK");
        }
        Connection conn = null;
        Session sess = null;
        try {
            if (getPort() != null) {
                conn = new Connection(getHost(), getPort());
            } else {
                conn = new Connection(getHost());
            }

            conn.connect();

            /* Authenticate.
             * If you get an IOException saying something like
             * "Authentication method password not supported by the server at this stage."
             * then please check the FAQ.
             */
            boolean isAuthenticated = conn.authenticateWithPassword(getUserName(), getPassword());

            if (isAuthenticated == false) {
                throw new IOException("Authentication failed.");
            }

            sess = conn.openSession();

            sess.execCommand(command);

            BufferedReader br = null;
            String lineOut = null;
            InputStream stdout = new StreamGobbler(sess.getStdout());
            br = new BufferedReader(new InputStreamReader(stdout));
            StringBuffer output = new StringBuffer("");
            while (true) {
                lineOut = br.readLine();
                if (lineOut == null) {
                    break;
                }
                output.append(lineOut);
                //logger.info("lineOut='" + lineOut + "'");                    
            }
            logger.debug("output=" + output.toString());

            InputStream sterr = new StreamGobbler(sess.getStderr());
            br = new BufferedReader(new InputStreamReader(sterr));
            logger.debug("ExitCode: " + sess.getExitStatus());
            boolean chyba = false;
            String lineErr = null;
            while (true) {
                lineErr = br.readLine();
                if (lineErr == null) {
                    break;
                }
                if (StringUtils.isNotEmpty(lineErr)) {
                    chyba = true;
                }
                logger.info("lineErr='" + lineErr + "'");
            }

            if (!chyba) {

                return new CommandResult(ModificationResultEnum.OK, output.toString());

            } else {
                logger.warn("chyba vo vykonavani prikazu='" + command + "' (" + lineErr + ")");

                return new CommandResult(ModificationResultEnum.NOT_OK, output.toString());

            }
        } catch (Exception e) {
            logger.error("!!! nepodarilo sa vykonat prikaz na subscriber managerovi !!!, e=" + e.getLocalizedMessage(), e);
            throw new RuntimeException("!!! nepodarilo sa vykonat prikaz na subscriber managerovi !!!, e=" + e.getLocalizedMessage(), e);
        } finally {
            if (sess != null) {
                try {
                    sess.close();
                } catch (Exception ee) {
                    logger.warn("nepodarilos sa uzavriet session", ee);
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception eee) {
                    logger.warn("nepodarilo sa uzavriet connection", eee);
                }
            }
        }
    }

    //<editor-fold defaultstate="collapsed" desc="standardne gettery / settery">
    public String getCommandRemovePolicy() {
        return commandRemovePolicy;
    }

    public void setCommandRemovePolicy(String commandRemovePolicy) {
        this.commandRemovePolicy = commandRemovePolicy;
    }

    public String getCommandSetPolicy() {
        return commandSetPolicy;
    }

    public void setCommandSetPolicy(String commandSetPolicy) {
        this.commandSetPolicy = commandSetPolicy;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getCommandSetAdditivePolicy() {
        return commandSetAdditivePolicy;
    }

    public void setCommandSetAdditivePolicy(String commandSetAdditivePolicy) {
        this.commandSetAdditivePolicy = commandSetAdditivePolicy;
    }

    public String getCommandAddQuota() {
        return commandAddQuota;
    }

    public void setCommandAddQuota(String commandAddQuota) {
        this.commandAddQuota = commandAddQuota;
    }

    public String getCommandShowQuota() {
        return commandShowQuota;
    }

    public void setCommandShowQuota(String commandShowQuota) {
        this.commandShowQuota = commandShowQuota;
    }
    //</editor-fold>
}
