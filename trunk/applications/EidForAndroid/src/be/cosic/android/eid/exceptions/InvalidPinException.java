
package be.cosic.android.eid.exceptions;

public class InvalidPinException extends PinException {
	private static final long serialVersionUID = 1L;
	public InvalidPinException() {
		super();
	}
	public InvalidPinException(String msg) {
		super(msg);
	}
}
