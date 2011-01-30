package redis.clients.johm;

public class JOhmException extends RuntimeException {
    private static final long serialVersionUID = 6827914667569538366L;
    private final JOhmExceptionMeta meta;

    public JOhmException(Exception exception, JOhmExceptionMeta meta) {
        super(exception);
        this.meta = meta;
    }

    public JOhmException(String message, JOhmExceptionMeta meta) {
        super(message);
        this.meta = meta;
    }

    public JOhmExceptionMeta getMeta() {
        return meta;
    }

    public String getMessage() {
        String message = super.getMessage();
        if (message == null || message.trim().length() == 0) {
            message = meta.getMessage();
        }

        return message;
    }
}
