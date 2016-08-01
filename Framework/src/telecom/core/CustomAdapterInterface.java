package telecom.core;

import org.w3c.dom.*;

import java.util.*;

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
    public Document request(Map<String, String> att) throws ResponseException;
}
