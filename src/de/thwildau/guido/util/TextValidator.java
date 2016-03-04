package de.thwildau.guido.util;

/**
 * Utility class to validate entered text.
 * @author Guido
 */
public class TextValidator {

	/**
	 * Returns true if given String is not empty.
	 * @param text
	 * @return
	 */
	public static boolean isNotEmpty(String text) {
		return (!(text == null || text.isEmpty() || text.length() == 0 || text.equals("")));
	}
	
	/**
	 * Returns true if the given Strings syntax is a valid email address.
	 * @param email
	 * @return
	 */
	public static boolean isValidEmail(String email) {
		return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
	}
	
	/**
	 * Returns true if the given String contains only numbers.
	 * @param text
	 * @return
	 */
	public static boolean isNumber(String text) {
		try {
			Integer.parseInt(text);
		} catch(NumberFormatException e) {
			return false;
		}
		return true;
	}
}
