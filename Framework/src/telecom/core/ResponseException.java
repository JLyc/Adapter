package telecom.core;

/**
 * Created by sochaa on 28. 4. 2016.
 */

/**
 * Use for plugin response in case some error occurred in plugin.
 *
 * Set error message as response xml message to send error back to requestor
 */
public class ResponseException extends Throwable {

    public ResponseException(String message) {
        super(message);
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }
}
