package redis.clients.johm;

public class JOhmException extends RuntimeException {

    public JOhmException(Exception e) {
	super(e);
    }

    public JOhmException(String message) {
	super(message);
    }

    /**
     * 
     */
    private static final long serialVersionUID = 5824673432789607128L;

}
