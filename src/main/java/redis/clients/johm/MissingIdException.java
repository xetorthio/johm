package redis.clients.johm;

public class MissingIdException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 431576167757996845L;


    public MissingIdException() {
    }

    public MissingIdException(String message) {
        super(message);
    }
}
