package de.thwildau.guido.util;

/**
 * This class represents a Respone object received from or sent to the server.
 * @author Guido
 */
public class Response {
	
	/**
	 * The responses id
	 */
	private int id;
	/**
	 * The responses content
	 */
	private Object object;
	
	/**
	 * Empty standard constructor.
	 */
	public Response() {}
	
	/**
	 * Constructor assigning the given parameters.
	 * @param id The responses id.
	 * @param object The responses content.
	 */
	public Response(int id, Object object) {
		this.id = id;
		this.object = object;
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @return the object
	 */
	public Object getObject() {
		return object;
	}

	/**
	 * @param object the object to set
	 */
	public void setObject(Object object) {
		this.object = object;
	}
	
	
}
