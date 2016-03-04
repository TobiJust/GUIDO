package de.thwildau.guido.model;

import java.io.Serializable;

/**
 * Data model of a user. Serializable to pass it between Activities.
 * @author GUIDO
 * @version 2013-12-15
 * @see Serializable
 */
public class User implements Serializable {
	private static final long serialVersionUID = -4720446181992730595L;
	private String id, name, imageRef, email;
	private int created, participated;

	private double lat, lng;
	
	public User(String id, String name, String imageRef, String email, double lat, double lng) {
		this.id = id;
		this.name = name;
		this.imageRef = imageRef;
		this.email = email;
		this.lat = lat;
		this.lng = lng;
	}
	
	public User() {}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getImageRef() {
		return imageRef;
	}

	public void setImageRef(String imageRef) {
		this.imageRef = imageRef;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public double getLat() {
		return lat;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}

	public double getLng() {
		return lng;
	}

	public void setLng(double lng) {
		this.lng = lng;
	}
	
	public int getCreated() {
		return created;
	}

	public void setCreated(int created) {
		this.created = created;
	}

	public int getParticipated() {
		return participated;
	}

	public void setParticipated(int participated) {
		this.participated = participated;
	}
}
