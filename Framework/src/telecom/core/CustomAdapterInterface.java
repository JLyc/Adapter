package telecom.core;

import java.lang.Exception;import java.lang.String;import java.util.Map;

/**
 * Created by JLyc on 27. 3. 2015.
 */
public interface CustomAdapterInterface {

    /**
     * Create communication chanel and return answer from external system
     * @param att request Map object
     * @return response Map object
     * @throws Exception in case of fail message response
     */
    public Map<String, String> request(Map<String, String> att) throws Exception;
}
