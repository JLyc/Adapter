package framework;

import org.junit.Test;
import telecom.core.AdapterCore;

/**
 * Created by JLyc on 13. 4. 2015.
 */
public class AdapterCoreTest {
    private String[] questionMark = {"?"};
    private String[] msgType = {"-msgType","jms"};

    @Test
    public void adapterCoreTestUseageMsg(){
        AdapterCore.main(questionMark);
    }

    @Test
    public void adapterCoreTestCommClient(){
        AdapterCore.main(msgType);
    }
}
