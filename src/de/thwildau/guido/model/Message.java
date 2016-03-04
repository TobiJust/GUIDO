package de.thwildau.guido.model;

import java.io.Serializable;

/**
 * Data model of a message.
 * @author GUIDO
 * @version 2013-12-13
 */
public class Message implements Serializable{
	private static final long serialVersionUID = 7317139681040344526L;
	
	private String id;

	private String fromEmail;

	private String toEmail;

	private String message;

	private String subject;
	
	public Message(String id, String fromEmail, String toEmail, String message, String subject) {
		this.id = id;
		this.fromEmail = fromEmail;
		this.toEmail = toEmail;
		this.message = message;
		this.subject = subject;
	}
	
	public Message() {}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getFromEmail() {
		return fromEmail;
	}

	public void setFromEmail(String fromEmail) {
		this.fromEmail = fromEmail;
	}

	public String getToEmail() {
		return toEmail;
	}

	public void setToEmail(String toEmail) {
		this.toEmail = toEmail;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}
}
