package cheqideh.exception.servicelayer;

public class ChequeExpiredException extends RuntimeException {
    public ChequeExpiredException(String message) {
        super(message);
    }
}
