package de.thwildau.guido.util;

/**
 * This class represents an occurred error.
 * @author Guido
 */
public class GuidoError {
	
	/**
	 * An error message
	 */
	private String message;
	
	/**
	 * An error code.
	 */
	private int errorCode;
	
	/**
	 * Constructor assigning the given message and error code.
	 * @param msg The error message.
	 * @param i The error code.
	 */
	public GuidoError(String msg, int i){
		setMessage(msg);
		setErrorCode(i);
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}
}
