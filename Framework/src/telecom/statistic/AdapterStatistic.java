package telecom.statistic;

import org.apache.commons.logging.*;
import telecom.core.*;

import java.util.*;
import java.util.concurrent.*;

/**
 * Created by JLyc on 31. 3. 2015.
 */
public class AdapterStatistic {
    private static final Log LOG = LogFactory.getLog(AdapterStatistic.class);
    private static final Map<String, String> statistic = new HashMap<>();
    private static final long startTime = System.currentTimeMillis();

    static {

        addStatistic("UpTime");
        addStatistic("ProcessingSumTime");
        addStatistic("NumberOfThreads");
        addStatistic("ActiveMsg");
        addStatistic("MsgInQueue");
        addStatistic("MsgCompletzed");
        addStatistic("TimePerMsg");
        addStatistic("MsgPeek");
        addStatistic("MsgCount");
        statistic.put("ProcessingSumTime", longToTime(0));
    }

    private static void addStatistic(String statisticName){
        statistic.put(statisticName, "0");
    }
/*
    private static void startTime(String statisticName) {
        statistic.put(statisticName, System.currentTimeMillis());
    }

    private static String elapsedTime(String statisticName) {
        long millis = System.currentTimeMillis() - statistic.get(statisticName);

        return longToTime(millis);
    }
*/
    public static synchronized void increaseValue(String statisticName, long value) {
        long getValue = Long.valueOf(statistic.get(statisticName));
        statistic.put(statisticName, String.valueOf(getValue+value));
    }

    public static synchronized void increaseTime(String statisticName, long value) {
        long getValue = timeToLong(statistic.get(statisticName));
        Long value2 = getValue + value;
        statistic.put(statisticName, longToTime(value2) );
    }

    public static void setValue(String statisticName, long value) {
        statistic.put(statisticName, String.valueOf(value));
    }

    public static String getValue(String statisticName) {
        return statistic.get(statisticName);
    }

    public static String longToTime(long millis){
        long second = (millis / 1000) % 60;
        long minute = (millis / (1000 * 60)) % 60;
        long hour = (millis / (1000 * 60 * 60)) % 24;
        millis = millis % 1000;

        return String.format("%02d:%02d:%02d:%d", hour, minute, second, millis);
    }

    public static long timeToLong(String time){
        String[] parsedTime = time.split(":");
        long hour = Long.valueOf(parsedTime[0]) * 60 * 60 * 1000;
        long minute = Long.valueOf(parsedTime[1]) * 60 * 1000;
        long second = Long.valueOf(parsedTime[2]) * 1000;
        long millis = Long.valueOf(parsedTime[3]);
        return hour+minute+second+millis;
    }

    private static void updateStatistic() {
        long currentTime = System.currentTimeMillis();
        ThreadPoolExecutor tpExecutor = CommunicationClient.getExecutor();
        statistic.put("UpTime", longToTime(currentTime - startTime));
        statistic.put("NumberOfThreads", String.valueOf(tpExecutor.getPoolSize()));
        statistic.put("ActiveMsg", String.valueOf(tpExecutor.getActiveCount()));
        statistic.put("MsgInQueue", String.valueOf(tpExecutor.getQueue().size()));
        statistic.put("MsgCompleted", String.valueOf(tpExecutor.getCompletedTaskCount()));
        if(Long.valueOf(statistic.get("MsgCount"))!=0)
            statistic.put("TimePerMsg", longToTime(timeToLong(statistic.get("ProcessingSumTime")) / Long.valueOf(statistic.get("MsgCount"))));
        else
            statistic.put("TimePerMsg", "0");
        statistic.put("MsgPeek", String.valueOf(tpExecutor.getLargestPoolSize()));
    }

    public static Map<String, String> getStatistic() {
        updateStatistic();
        return statistic;
    }
}
