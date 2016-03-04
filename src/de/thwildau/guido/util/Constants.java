package de.thwildau.guido.util;

/**
 * This class holds constants which are used in the application.
 * <br>URLs<br>Travel Types<br>Route categories<br>Response Codes for communication<br>
 * Billing Requests<br>Preferences keys<br>Notification IDs<br>Error Codes
 * @author GUIDO
 */
public class Constants {

	/*
	 * URLs
	 */
	public static final String URL_SECURE = "https://";
	public static final String URL_STANDARD = "http://";
	public static final String URL_BASE = "www.tm.th-wildau.de/~labbe/Guido/index.php/requests/";
	public static final String URL_CREATE_USER = "createUser";
	public static final String URL_GET_MESSAGES = "getMessages";
	public static final String URL_SEND_MESSAGE = "sendMessage";
	public static final String URL_CREATE_ROUTE = "createRoute";
	public static final String URL_GET_ROUTES = "listRoutes";
	public static final String URL_START_ROUTE = "startRoute";
	public static final String URL_JOIN_PUBLIC = "joinPublic";
	public static final String URL_JOIN_PRIVATE = "joinPrivate";
	public static final String URL_POSITION_ALL = "getPosAll";
	public static final String URL_POSITION_GUIDO = "getPosGuido";
	public static final String URL_SEND_POSITION = "sendPos";
	public static final String URL_END_ROUTE = "endRoute";
	public static final String URL_LEAVE_ROUTE = "leaveRoute";
	public static final String URL_ROUTE_DETAILS = "getRouteDetails";
	public static final String URL_LOGOUT = "logout";
	public static final String URL_LOGIN = "login";
	public static final String URL_LOGIN_REQUEST = "isLoggedIn";
	public static final String URL_DELETE_MESSAGE = "deleteMessage";
	public static final String URL_CONTACTS = "getContacts";
	public static final String URL_ADD_CONTACT = "addContact";
	public static final String URL_DELETE_CONTACT = "deleteContact";
	public static final String URL_USER_INFO = "getUserInfo";
	public static final String URL_CONTACT_INFO = "getContactInfo";
	public static final String URL_CHANGE_PROFILE_NAME = "changeProfileName";
	public static final String URL_FORCE_LOGIN = "forceLogin";
	
	/*
	 * Travel Types
	 */
	public static final int TRAVEL_CAR = 1;
	public static final int TRAVEL_PUBLIC = 2;
	public static final int TRAVEL_AFOOT = 3;
	public static final int TRAVEL_BIKE = 4;
	
	/*
	 * Route Category
	 */
	public static final String[] ROUTE_CATEGORIES = {"Nachtleben", "Tourist", "Sport", "Kultur"};
	
	/*
	 * Travel Types as Array
	 */
	public static final String[] ROUTE_TRAVELTYPES = {"Auto", "÷ffentliche Verkehrsmittel", "Zu Fuﬂ", "Fahrrad"};
	
	/*
	 * Response Codes
	 */
	public static final int RESP_ERROR = 0;
	public static final int RESP_REGISTRATION = 1;
	public static final int RESP_LOGIN = 2;
	public static final int RESP_GET_MESSAGE = 3;
	public static final int RESP_SEND_MESSAGE = 4;
	public static final int RESP_CREATE_ROUTE = 5;
	public static final int RESP_START_ROUTE = 6;
	public static final int RESP_JOIN_PUBLIC = 7;
	public static final int RESP_JOIN_PRIVATE = 8;
	public static final int RESP_GET_POS_ALL = 9;
	public static final int RESP_GET_POS_GUIDO = 10;
	public static final int RESP_SEND_POS = 11;
	public static final int RESP_END_ROUTE = 12;
	public static final int RESP_LEAVE_ROUTE = 13;
	public static final int RESP_GET_ROUTE_DETAILS = 14;
	public static final int RESP_LIST_ROUTES = 16;
	public static final int RESP_LOGOUT = 17;
	public static final int RESP_LOGGED_IN = 18;
	public static final int RESP_DELETE_MESSAGE = 19;
	public static final int RESP_GET_CONTACTS = 20;
	public static final int RESP_ADD_CONTACT = 21;
	public static final int RESP_DELETE_CONTACT = 22;
	public static final int RESP_GET_USER_INFO = 23;
	public static final int RESP_GET_CONTACT_INFO = 24;
	public static final int RESP_CHANGE_PROFILE_NAME = 25;
	
	/*
	 * Billing Requests
	 */
	public static final int REQ_PROD_DETAILS = 0;
	public static final int REQ_PROD_PURCHASE = 1;
	public static final int REQ_PROD_CONSUMED = 2;
	public static final int REQ_PROD_LIST = 3;
	
	/*
	 * Preferences Keys
	 */
	public static final String PREF_USER_ID = "user_id";
	public static final String PREF_ACTIVE_ROUTE = "active_route";
	
	/*
	 * Notification IDs
	 */
	public static final int NOT_MESSAGE_RECEIVED = 0;
	public static final int NOT_ROUTE_STARTED = 1;
	
	/*
	 * Months
	 */
	public static final String[] MONTHS = {"Jan", "Feb", "M‰r", "Apr", "Mai", "Jun", "Jul", "Aug", "Sep", "Okt", "Nov", "Dez"};
	
	/*
	 * Guido Sphere
	 */
	public static final double ACTIVE_ROUTE_MAX_DISTANCE = 25.0;
	
	/*
	 * Error Codes
	 */
	public static final int ERR_USER_EXISTS = 0;
	public static final int ERR_USER_PASS_WRONG = 1;
	public static final int ERR_USER_ALREADY_LOGGED_IN = 2;
	public static final int ERR_USER_ATUH = 3;
	public static final int ERR_USER_NOT_LOGGED_IN = 4;
	
	public static final int ERR_MSG_USER_NOT_EXISTING = 10;
	public static final int ERR_MSG_USER_NOT_OWNER = 11;
	
	public static final int ERR_ROUTE_NOT_GUIDE = 20;
	public static final int ERR_ROUTE_NOT_STARTED = 21;
	public static final int ERR_ROUTE_NOT_EXISTING = 22;
	public static final int ERR_ROUTE_NOT_PARTICIPATING = 23;
	public static final int ERR_ROUTE_NOT_PARTICIPATING_ANY = 24;
	
	public static final int ERR_ROUTE_NO_PERMISSION = 30;
	public static final int ERR_ROUTE_ALREADY_STARTED = 31;
	public static final int ERR_ROUTE_ALREADY_PARTICIPATING = 32;
	public static final int ERR_ROUTE_IS_FULL = 34;
	public static final int ERR_ROUTE_WRONG_PASS = 35;

}
