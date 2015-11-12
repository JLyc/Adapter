package telecom.statistic;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by JLyc on 31. 3. 2015.
 */
public class AdapterStatistic {
    private static final Map<String, Long> statistic = new HashMap<>();

    static {
        addStatistic("MsgCount");
        addStatistic("ProcessingSumTime");
    }

    public static void addStatistic(String statisticName){
        statistic.put(statisticName, (long) 0);
    }

    public static void startTime(String statisticName) {
        statistic.put(statisticName, System.currentTimeMillis());
    }

    public static String elapsedTime(String statisticName) {
        long millis = System.currentTimeMillis() - statistic.get(statisticName);

        return longToTime(millis);
    }

    public static synchronized void increaseValue(String statisticName, long value){
        statistic.put(statisticName, statistic.get(statisticName) + value);
    }

    public static void setValue(String statisticName, long value){
        statistic.put(statisticName, value);
    }

    public static long getValue(String statisticName) {
        return statistic.get(statisticName);
    }

    public static String longToTime(long millis){
        long second = (millis / 1000) % 60;
        long minute = (millis / (1000 * 60)) % 60;
        long hour = (millis / (1000 * 60 * 60)) % 24;
        millis = millis % 1000;

        return String.format("%02d:%02d:%02d:%d", hour, minute, second, millis);
    }
}
