package de.thwildau.guido.util;

import java.util.Comparator;

import de.thwildau.guido.model.Message;

/**
 * Sorts the messages by date using a Comparator.
 * @author Guido
 * @see Comparator
 */
public class MessageSortDate implements Comparator<Message> {

	/**
	 * Compare two messages
	 */
	@Override
	public int compare(Message lhs, Message rhs) {
		if(Integer.parseInt(lhs.getId())<Integer.parseInt(lhs.getId())){
			return 1;
		}
		else{
			return -1;
		}
	}

}
