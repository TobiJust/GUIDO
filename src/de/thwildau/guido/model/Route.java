package de.thwildau.guido.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

/**
 * Data model of a route. Serializable to pass it between Activities.
 * @author GUIDO
 * @version 2013-12-14
 * @see Serializable
 */
public class Route implements Serializable {
	private static final long serialVersionUID = 2720892855448834737L;
	private int maxPart, currPart;
	private String id, name, description, category;
	private boolean password;
	private Date date;
	private int travelType;
	private ArrayList<POI> pois;
	private ArrayList<User> participators;
	private User creator;
	
	public Route(String id, String name, String description, int maxPart, int currPart, String category, boolean password, Date date, int travelType, ArrayList<POI> pois, User creator, ArrayList<User> participators) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.maxPart = maxPart;
		this.currPart = currPart;
		this.category = category;
		this.password = password;
		this.date = date;
		this.travelType = travelType;
		this.pois = pois;
		this.creator = creator;
	}
	
	public ArrayList<User> getParticipators() {
		return participators;
	}

	public void setParticipators(ArrayList<User> participators) {
		this.participators = participators;
	}

	public Route() {}

	public POI getStart() {
		return pois.get(0);
	}
	
	public POI getEnd() {
		return pois.get(pois.size()-1);
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getMaxPart() {
		return maxPart;
	}

	public void setMaxPart(int maxPart) {
		this.maxPart = maxPart;
	}
	
	public int getCurrPart() {
		return currPart;
	}

	public void setCurrPart(int currPart) {
		this.currPart = currPart;
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

	public boolean getPassword() {
		return password;
	}

	public void setPassword(boolean password) {
		this.password = password;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public int getTravelType() {
		return travelType;
	}

	public void setTravelType(int travelType) {
		this.travelType = travelType;
	}

	public ArrayList<POI> getPois() {
		return pois;
	}

	public void setPois(ArrayList<POI> pois) {
		this.pois = pois;
	}
	
	public User getCreator() {
		return creator;
	}
	
	public void setCreator(User creator) {
		this.creator = creator;
	}
}
