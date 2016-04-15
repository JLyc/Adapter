package telecom.monitoring;

/**
 * Created by JLyc on 26. 3. 2015.
 */
public enum LogLvl {
    INFO("info"), WARN("warn"), DEBUG("debug"), ERROR("error"), FATAL("fatal"), OFF("off");

    private String name;

    LogLvl(String name) {
        this.name = name;
    }

    public String getName(){
        return name;
    }
}
