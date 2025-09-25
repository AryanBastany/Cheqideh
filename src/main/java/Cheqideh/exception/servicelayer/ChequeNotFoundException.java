package Cheqideh.exception.servicelayer;

public class ChequeNotFoundException extends RuntimeException {
    public ChequeNotFoundException(String message) {
        super(message);
    }
}
