package shopping_network;

import java.sql.Timestamp;

public class Purchase {
	
	/*
	 * Store details about a purchase.
	 *  - price
	 *  - timestamp
	 *  - sequence number (in case timestamps aren't enough differentiation).
	 *    Sequence number should be reset for each new second.
	 *  - originating user id
	 */
	
	private int userID; //The user who made the purchase.
	private double price; //The price of the purchase;
	private Timestamp timestamp; //The time of the purchase
	private int purchaseNum; //Supplementary variable to the timestamp.
	//purchaseNum increments on the purchases made within a given second.
	
	/**
	 * Generates a Purchase object with the given specifications.
	 * @param userID The ID of the user who made the purchase.
	 * @param price The price of the purchase.
	 * @param timestamp The time at which the purchase was made.
	 * @param purchaseNum Supplementary information to the timestamp.
	 */
	public Purchase(int userID, double price, Timestamp timestamp, int purchaseNum){
		this.userID = userID;
		this.price = price;
		this.timestamp = timestamp;
		this.purchaseNum = purchaseNum;
	}
	
	
	public int getUserID(){
		return userID;
	}
	public double getPrice(){
		return price;
	}
	public Timestamp getTimestamp(){
		return timestamp;
	}
	public int getPurchaseNum(){
		return purchaseNum;
	}
	
	/**
	 * Determines which purchase was made first. Returns true if p1 was made before p2.
	 * Also returns True If the purchases are made at the same time,
	 * though this should only occur when p1 == p2.
	 * @param p1 A purchase.
	 * @param p2 A purchase.
	 * @return True if p1 was made first. False otherwise.
	 */
	public static boolean comparePurchases(Purchase p1, Purchase p2){
		if(p1.getTimestamp().after(p2.getTimestamp())){
			return false;
		} else if(p1.getTimestamp().before(p2.getTimestamp())){
			return true;
		} else {
			if(p1.getPurchaseNum() > p2.getPurchaseNum()){
				return false;
			} else {
				return true;
			}
		}
	}
	
	

}
