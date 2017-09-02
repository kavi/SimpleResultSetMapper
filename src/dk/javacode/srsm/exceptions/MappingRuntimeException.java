package dk.javacode.srsm.exceptions;

public class MappingRuntimeException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5891911820756789652L;

	public MappingRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public MappingRuntimeException(String message) {
		super(message);
	}
}
