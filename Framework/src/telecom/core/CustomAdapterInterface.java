package telecom.core;

import org.w3c.dom.*;

import java.util.*;

public interface CustomAdapterInterface {

    /**
     * Create communication chanel and return answer from external system
     * @param att request Map object
     * @return response Map object
     * @throws Exception in case of fail message response
     */
    Document request(Map<String, String> att) throws ResponseException;
}
