package de.thwildau.guido.model;

import java.io.Serializable;

/**
 * Data model of a point of interest. Serializable to pass it with a {@link Route} between Activities.
 * @author GUIDO
 * @version 2013-12-14
 * @see Serializable
 */
public class POI implements Serializable {
	private static final long serialVersionUID = -7472689548718389870L;
	private String id, name, description;
	private double lat, lng;
	
	public POI(String id, String name, String description, double lat, double lng ) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.lat = lat;
		this.lng = lng;
	}
	
	public POI() {}

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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
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
}
