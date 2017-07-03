package shopping_network;

import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A holder for static event methods and variables.
 * The primary method here is parseEvent, which takes each new line of the log fi
 * @author Kevin
 *
 */
public class Event {
	
	public static final String pattern = "^\\{\"event_type\":\"(purchase|befriend|unfriend)\", \"timestamp\":\"([0-9\\- :]+)\", \"id1?\": \"([0-9]+)\", \"(?:amount|id2)\": \"([0-9.]+)\"\\}$";
	static Pattern p = Pattern.compile(pattern);
	
	private static long currentTime = 0;
	private static int purchaseNum = 0;//Purchases are numbered each second to preserve order.
	
	/**
	 * Parses a string corresponding to a purchase, befriend, or unfriend event,
	 * then passes the parameters to the corresponding event.
	 * Some actions are reserved for when the events are streaming to speed up the
	 * building of the initial network.
	 * @param n a Network
	 * @param event An event
	 * @param streaming True if currently streaming
	 */
	public static String parseEvent(Network n, String event, boolean streaming){
		String out = null;
		Matcher m = p.matcher(event);
		if(m.matches()){
			
			Timestamp timestamp = Timestamp.valueOf(m.group(2));//Already in JDBC timestamp escape format! Handy!
			if(timestamp.getTime() > currentTime){
				currentTime = timestamp.getTime();
				purchaseNum = 0;
			}
			
			if(m.group(1).equals("purchase")){
				int id = Integer.parseInt(m.group(3));
				double amount = Double.parseDouble(m.group(4));
				out = purchase(n, id, amount, timestamp, purchaseNum, streaming);
				purchaseNum++;
			} else if(m.group(1).equals("befriend")){
				int id1 = Integer.parseInt(m.group(3));
				int id2 = Integer.parseInt(m.group(4));
				befriend(n, id1, id2, timestamp, streaming);
			} else if(m.group(1).equals("unfriend")){
				int id1 = Integer.parseInt(m.group(3));
				int id2 = Integer.parseInt(m.group(4));
				unfriend(n, id1, id2, timestamp, streaming);
			} else {
				//Exception for unhandled event. This will never occur.
				System.out.println("Unhandled event: \""+m.group(1)+"\" "+ m.group(0));
			}
			
			
			
		} else {
			if(!event.equals("")){
				throw new IllegalArgumentException("Cannot parse event: \""+event+"\"");
			}
		}
		return out;
	}

	
	private static String purchase(Network n, int id, double amount, Timestamp timestamp, int purchaseNum, boolean streaming){
		Purchase p = new Purchase(id, amount, timestamp, purchaseNum);
		User u = n.getUser(id);
		u.addPurchase(p, streaming);
		
		if(!streaming){
			return null;
		}
		if(u.checkForOutlier(p)){
			String flag = writePurchaseFlag(p, u.getMean(), u.getSD());
			return flag;
			//System.out.println(flag);
		}
		return null;
	}
	private static void befriend(Network n, int id1, int id2, Timestamp timestamp, boolean streaming){
		User u1 = n.getUser(id1);
		User u2 = n.getUser(id2);
		u1.addFriend(u2, streaming);
		u2.addFriend(u1, streaming);
	}
	private static void unfriend(Network n, int id1, int id2, Timestamp timestamp, boolean streaming){
		User u1 = n.getUser(id1);
		User u2 = n.getUser(id2);
		u1.removeFriend(u2, streaming);
		u2.removeFriend(u1, streaming);
	}
	
	
	
	
	public static int D = 2;//Number of degrees in a user's social network. Should be at least 1. (Default is 2.)
	public static int T = 50;//Number of consecutive purchases to use in a user's social network. Should be at least 2. (Default is 50.)
	private static final String parameterRegex = "^\\{\"D\":\"([0-9]+)\", \"T\":\"([0-9]+)\"\\}$";
	private static final Pattern p2 = Pattern.compile(parameterRegex);
	
	/**
	 * Sets the parameters D and T based on the input String.
	 * D is the number of degrees in a user's social network.
	 * T is the number of consecutive purchases to use when computing mean
	 * and standard deviation in a user's social network.
	 * @param parameters The input String.
	 */
	public static void setParameters(String parameters){
		Matcher m = p2.matcher(parameters);
		if(m.matches()){
			Event.D = Integer.parseInt(m.group(1));
			Event.T = Integer.parseInt(m.group(2));
		} else {
			throw new IllegalArgumentException("Cannot set parameters: \""+parameters+"\"");
		}
	}
	
	/**
	 * Writes a flag for a purchase. This is meant to be used in a .json file.
	 * The mean and standard deviation are truncated here; the more precise values
	 * were used in the computations.
	 * @param p A purchase
	 * @param mean A double corresponding to a mean
	 * @param sd A double corresponding to a standard deviation
	 * @return A String
	 */
	public static String writePurchaseFlag(Purchase p, double mean, double sd){
		String timestamp = p.getTimestamp().toString();
		timestamp = timestamp.substring(0, timestamp.length()-2);//remove the ".0"	
				
		String s = "{\"event_type\":\"purchase\", ";
		s += "\"timestamp\":\""+timestamp+"\", ";
		s += "\"id\": \"" + p.getUserID()+"\", ";
		s += "\"amount\": \"" + formatDouble(p.getPrice()) + "\", ";
		s += "\"mean\": \"" + formatDouble(mean) + "\", ";
		s += "\"sd\": \"" + formatDouble(sd) + "\"}";
				
		return s;
	}
	
	/**
	 * Truncates a double to two decimal places, and returns it as a String;
	 * e.g. 13.73849 is truncated to 13.73.
	 * The string will always have precision to exactly two decimal places;
	 * e.g. 15 is returned as 15.00 and 31.1 is returned as 31.10.
	 * 
	 * @param d A double
	 * @return A String
	 */
	protected static String formatDouble(double d){
		
		DecimalFormat df = new DecimalFormat("0");
		df.setMaximumFractionDigits(2);
		df.setMinimumFractionDigits(2);
		df.setRoundingMode(RoundingMode.DOWN);
		return df.format(d);
		//int temp = (int)(d*100);
		//String s = ""+((double)(temp)/100);
		//if(temp%10 == 0 && temp%100 != 0){
		//	s += "0";
		//} else if(temp%100 == 0 && !s.contains(".")){
		//	s+=".00";//probably redundant, from the looks of it
		//}
		//return s;
		//return (double)(temp)/100;
	}
	
	
	
	
	
	
	
	
	
	
	
	

}
