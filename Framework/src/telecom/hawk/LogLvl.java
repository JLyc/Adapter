package telecom.hawk;

/**
 * Created by JLyc on 26. 3. 2015.
 */
public enum LogLvl {
    INFO("info"), WARN("wran"), CRTITICAL("critical");

    private String name;

    LogLvl(String name) {
        this.name = name;
    }

    public String getName(){
        return name;
    }
}
