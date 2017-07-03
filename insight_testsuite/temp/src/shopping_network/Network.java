package shopping_network;

import java.util.ArrayList;

/**
 * A container for all users. The network is expanded to accommodate new users.
 * This network relies on the range of user IDs being based on the total number
 * of users, so there should be no user with ID 923465 in a network of 10 users.
 * 
 * @author Kevin
 *
 */
public class Network {
	
	ArrayList<User> users = new ArrayList<User>();
	
	public Network(){
		
	}
	
	/**
	 * Resizes the network to accommodate higher user IDs.
	 * @param newUserID An integer
	 */
	public void expandNetwork(int newUserID){
		for(int i = users.size(); i <= newUserID; i++){
			User u = new User(i);
			users.add(u);
		}
	}
	
	/**
	 * Finds the user with the given ID.
	 * @param userID An integer
	 * @return A User
	 */
	public User getUser(int userID){
		if(userID >= users.size()){
			expandNetwork(userID);
		}
		return users.get(userID);
	}
	
	/**
	 * Gives the number of users in the network.
	 * @return An integer
	 */
	public int getNumUsers(){
		return users.size();
	}
	
	
	

}
