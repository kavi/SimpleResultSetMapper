package dk.javacode.srsm.exceptions;

public class MappingException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1628392154683456511L;

	public MappingException(String message, Throwable cause) {
		super(message, cause);
	}

	public MappingException(String message) {
		super(message);
	}
}
