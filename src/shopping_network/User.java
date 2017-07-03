package shopping_network;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 * This object stores information about a user. Particularly, it stores their ID,
 * a list of friends, a list of their recent purchases, and a list of recent purchases
 * of users within their social network.
 * @author Kevin
 *
 */
public class User {
	
	private int id;
	private ArrayList<User> friends = new ArrayList<User>();
	
	/**
	 * Constructs a user with the given ID.
	 * @param id A non-negative integer.
	 */
	public User(int id){
		this.id = id;
	}
	
	/**
	 * Gets the ID of the user.
	 * @return A non-negative integer.
	 */
	public int getID(){
		return id;
	}
	
	/**
	 * Adds a friend to the user.
	 * This method first checks if the users are already friends.
	 * In the streaming stage, it also flags sufficiently close users
	 * that there is a change in their social network.
	 * @param u Another user.
	 */
	public void addFriend(User u, boolean streaming){
		for(User v : friends){
			if(u == v){
				return;
			}
		}
		if(streaming){
			ArrayList<User> sn = socialNetwork(Event.D - 1);
			for(User w : sn){
				w.flagSN();//flags sufficiently close users that there is a change in their social network
			}
		}
		friends.add(u);
	}
	
	/**
	 * Removes a friend from the user.
	 * If the users are not currently friends, this method will do nothing.
	 * This method also flags sufficiently close users that there is a change in their social network.
	 * @param u Another user.
	 */
	public void removeFriend(User u, boolean streaming){
		for(int i = 0; i < friends.size(); i++){
			if(friends.get(i) == u){
				friends.remove(i);
				
				if(streaming){
					ArrayList<User> sn = socialNetwork(Event.D - 1);
					for(User w : sn){
						w.flagSN();
					}
				}
				return;
			}
		}
	}
	/**
	 * Gets a list of the user's friends.
	 * @return An ArrayList of Users.
	 */
	protected ArrayList<User> getFriends(){
		return friends;
	}
	

	private int crawlDegree = -1;//used in a s.n. crawl, then reset back to -1
	/**
	 * Gets the crawlDegree variable, which is used in a social network crawl.
	 * So, in a degree 3 search, the originating user is assigned
	 * crawl degree 3, their friends are assigned 2, friends of friends are assigned 1,
	 * and friends of friends of friends are assigned 0.
	 * @return An integer.
	 */
	private int getCrawlDegree(){
		return crawlDegree;
	}
	/**
	 * Sets the crawlDegree variable, for use in a social network crawl. 
	 * @param degree
	 */
	private void setCrawlDegree(int degree){
		crawlDegree = degree;
	}
	
	/**
	 * Generates a user's social network for a given degree.
	 * The crawl iterates on a list of users, each with an assigned degree.
	 * It begins with the list consisting of the original user; the input is their
	 * assigned degree. At each iteration, friends of the current user are added to
	 * the back of the list with degree one less than the current user. Friends already
	 * on the list will have already been assigned a degree and will be ignored. The
	 * iterations continue until they reach the end of the list or they arrive at a user
	 * with degree 0 (all later users on the list will also have degree 0).
	 * 
	 * This social network includes the original user as the first element. To use the
	 * social network without the original user, remove index 0 or otherwise ignore it.
	 * 
	 * @param degree The maximum number of degrees of separation to be used.
	 * @return An ArrayList of Users.
	 */
	public ArrayList<User> socialNetwork(int degree){
		crawlDegree = degree;
		ArrayList<User> sn = new ArrayList<User>();
		sn.add(this);
		int i = 0;
		while(i < sn.size()){
			if(sn.get(i).getCrawlDegree() == 0){
				break;
			}
			for(User u : sn.get(i).friends){
				if(u.getCrawlDegree() == -1){
					u.setCrawlDegree(sn.get(i).getCrawlDegree() - 1);
					sn.add(u);
				}
				
			}
			i++;
		}
		for(User u : sn){
			u.setCrawlDegree(-1);
		}
		return sn;
	}
	
	private LinkedList<Purchase> purchases = new LinkedList<Purchase>();
	
	/**
	 * Adds a user's purchase to their personal history.
	 * Also adds the purchase to the network histories of the
	 * users in the current user's social network.
	 * @param p A new purchase.
	 */
	public void addPurchase(Purchase p, boolean streaming){
		purchases.add(p);
		while(purchases.size() > Event.T){
			purchases.removeFirst();
		}
		if(streaming){
			ArrayList<User> sn = socialNetwork(Event.D);
			for(int i = 1; i < sn.size(); i++){
				sn.get(i).addSNPurchase(p);
			}
		}
		
	}
	
	/**
	 * Gets the purchase history for the user.
	 * @return A list of purchases.
	 */
	protected LinkedList<Purchase> getPurchases(){
		return purchases;
	}
	
	
	
	
	private LinkedList<Purchase> snPurchases = new LinkedList<Purchase>();//social network purchases
	private boolean snChange = true;//flags a change that may impact the user's social network
	//if true, the user should rebuild their friends' purchase histories when needed
	
	/*
	 * Idea: Rather than rebuilding the entire purchase history, only rebuild the affected portion.
	 * ... This would be a headache to implement and probably wouldn't save much time.
	 */
	private double snPurchaseSum = 0;//used for mean and standard deviation
	private double snPurchaseSquareSum = 0;//used for standard deviation
	
	public void flagSN(){
		snChange = true;
	}
	public boolean snIsFlagged(){
		return snChange;
	}
	
	/**
	 * Adds a purchase to the social network purchase history of the user,
	 * and updates analytics variables appropriately.
	 * @param p A new purchase
	 */
	protected void addSNPurchase(Purchase p){
		if(snChange){
			return;//pointless to add to a list that will be rebuilt from scratch
		}
		snPurchases.add(p);
		snPurchaseSum += p.getPrice();
		snPurchaseSquareSum += p.getPrice()*p.getPrice();
		while(snPurchases.size() > Event.T){
			double temp = snPurchases.getFirst().getPrice();
			snPurchaseSum -= temp;
			snPurchaseSquareSum -= temp*temp;
			snPurchases.removeFirst();
		}
	}
	
	/**
	 * Builds (or rebuilds) the social network purchase history of the user.
	 * First, it crawls and generates the social network. Then it finds the most recent
	 * purchases and builds them
	 */
	protected void buildSNPurchaseHistory(){
		ArrayList<User> sn = socialNetwork(Event.D);
		//We create iterators for the individual purchase histories in the social network
		ArrayList<ListIterator<Purchase>> alip = new ArrayList<ListIterator<Purchase>>();
		ArrayList<Purchase> ap = new ArrayList<Purchase>();//keeps track of current item in each iterator
		for(int i = 1; i < sn.size(); i++){//we have i=1 because the current user is at index 0
			LinkedList<Purchase> temp = sn.get(i).getPurchases();
			ListIterator<Purchase> temp2 = temp.listIterator(temp.size());
			if(temp2.hasPrevious()){
				ap.add(temp2.previous());
				alip.add(temp2);
			}
		}
		
		//we next sort ap by time, earliest first. Sorting alip in parallel.
		
		quickSort(ap, alip, 0, ap.size()-1);
		
		//Now, the last element in ap is the most recent purchase. It is added to the linkedlist
		//and its respective iterator is triggered. The purchase is replaced in ap (if an older
		//one exists for the respective user), and the new purchase is moved to its proper
		//location in ap. Actions done on ap are done on alip as well.
		
		
		LinkedList<Purchase> purchases = new LinkedList<Purchase>();
		
		while(ap.size() > 0 && purchases.size() < Event.T){
			purchases.addFirst(ap.get(ap.size()-1));//adds latest purchase to history
			ListIterator<Purchase> temp = alip.get(alip.size()-1);
			if(temp.hasPrevious()){
				ap.set(ap.size()-1, temp.previous());
				//move to correct location in ap, alip...
				int i = ap.size()-1;
				while(i > 0){
					if(Purchase.comparePurchases(ap.get(i), ap.get(i-1))){
						Purchase t1 = ap.get(i);//swap
						ListIterator<Purchase> t2 = alip.get(i);
						ap.set(i, ap.get(i-1));
						alip.set(i, alip.get(i-1));
						ap.set(i-1, t1);
						alip.set(i-1, t2);
						i--;
					} else {
						break;
					}
				}
			} else {
				alip.remove(alip.size()-1);
				ap.remove(ap.size()-1);	
			}
			
		}
		
		snPurchases = purchases;//TODO: check this bahemoth
		snChange = false;
		snPurchaseSum = 0;
		snPurchaseSquareSum = 0;
		for(Purchase p : snPurchases){
			snPurchaseSum += p.getPrice();
			snPurchaseSquareSum += p.getPrice()*p.getPrice();
		}
		
	}
	
	
	/**
	 * Instance of quicksort. Sorts elements earlier to later.
	 * @param ap An ArrayList of Purchases
	 * @param alip An ArrayList of iterators, parallel to ap.
	 * @param lo The lower bound for the segment to be sorted.
	 * @param hi The upper bound for the segment to be sorted.
	 */
	protected static void quickSort(ArrayList<Purchase> ap, ArrayList<ListIterator<Purchase>> alip, int lo, int hi){
		if(lo < hi){
			int p = partition(ap, alip, lo, hi);
			quickSort(ap, alip, lo, p-1);
			quickSort(ap, alip, p+1, hi);
		}
	}
	
	private static int partition(ArrayList<Purchase> ap, ArrayList<ListIterator<Purchase>> alip, int lo, int hi){
		Purchase pivot = ap.get(hi);
		int i = lo - 1;
		for(int j = lo; j <= hi; j++){
			if(Purchase.comparePurchases(ap.get(j),pivot)){
				i++;
				if(i != j){
					Purchase temp = ap.get(i);//swap objects at indices i and j
					ListIterator<Purchase> temp2 = alip.get(i);
					ap.set(i, ap.get(j));
					alip.set(i, alip.get(j));
					ap.set(j, temp);
					alip.set(j, temp2);
				}
			}
		}
		return i;
	}
	
	
	
	double mean = 0;//mean and standard deviation are updated when a purchase is checked
	double sd = 0;
		
	/**
	 * Determines if the purchase is an outlier. It is an outlier if it is greater than 3
	 * standard deviations above the mean of the social network's purchase history.
	 * If the social network's purchase history is strictly smaller than 2, then there is
	 * not enough information and the purchase is not flagged as an outlier (returns False).
	 * 
	 * The mean and standard deviation are computed from snPurchaseSum and snPurchaseSquareSum,
	 * two variables that count the sum of prices of purchases in the purchase history,
	 * and the sum of the squares of the prices.
	 * 
	 * The method builds the social network purchase history if needed before running the statistics.
	 * 
	 * @param p A purchase
	 * @return True if the purchase is an outlier.
	 */
	public boolean checkForOutlier(Purchase p){
		if(snChange){
			buildSNPurchaseHistory();
		}
		
		int histSize = snPurchases.size();
		if(histSize < 2){
			return false;//not enough purchases in history to determine outlier
		}
		
		mean = snPurchaseSum/histSize;
		sd = Math.sqrt((snPurchaseSquareSum/histSize) - (mean*mean));
		
		return p.getPrice() > mean + 3*sd;
		
	}
	
	/**
	 * Returns the mean of the most recent purchases in the user's social network.
	 * The mean is only updated after checkForOutlier is called; this method is
	 * meant to be used sparingly.
	 * @return A double
	 */
	protected double getMean(){
		return mean;
	}
	/**
	 * Returns the standard deviation of the most recent purchases in the user's
	 * social network. The standard deviation, like the mean, is only updated after
	 * checkForOutlier is called; this method is meant to be used sparingly.
	 * @return A double
	 */
	protected double getSD(){
		return sd;
	}
	
}




/*
 * Design consideration: Should all purchases in a user's social network
 * be stored for that user? Old enough purchases would be ignored, but they
 * could become relevant again after defriending.
 * 
 * 
 * (1) Store only the user's personal purchases. This would be most space-efficient, 
 *     and befriending/unfriending would be trivial, but it would mean crawling
 *     their social network whenever a purchase is made. (Maybe not a bad thing?)
 *     The latest purchases of all users in the s.n would have to be compared and
 *     calculated for every new purchase, but friends' purchase logs would not need
 *     to be updated.
 *     
 * (2) Store all purchases of the user's social network for that user. This would
 *     simplify calculation of mean and standard deviation, but a new purchase would
 *     still have to be added to the logs of all other users in the user's social network.
 *     There's no way around accessing the social network with a purchase, but all we would
 *     do to each other user is add the purchase to their sn-purchase logs. Additionally,
 *     befriending and unfriending would be a headache; these events could substantially
 *     change a social network and a lot of cross-checking would be needed.
 *     A solution to the befriend/unfriend problem would be to simply flag affected users
 *     and use (1) to re-compile their sn-purchase history when they make a purchase.
 *     It would be wasteful to update social networks and logs for a user multiple times
 *     before a purchase is made.
 *     
 * (3) Store only the latest T purchases of the user's social network. This is similar to
 *     (2) with a potential for substantially more space efficiency. We could use the same
 *     befriend/unfriend solution as in (1).
 * 
 * Conclusion: It seems (1) is fundamental. We could use (3) to potentially increase
 *     time-efficiency at the cost of space (and not much space at that, if we just have a
 *     list of pointers to the purchases). We should check how often a user's social
 *     network is changed (after initially being built) to see if (3) would be worth the
 *     extra effort or if it should be discarded. That is, we should see how often a user
 *     is flagged for having their sh-purchase history updated when they make a purchase,
 *     versus how often they can simply refer to the pre-made sh-purchase history.
 *     
 * Idea: Use (3)*. For a new purchase, crawl a user's social network and add it to the logs
 *     of respective users. If the original user's log is flagged, update it in the same crawl.
 *     (The purchase should also be in the user's personal history, which doesn't need to be larger
 *     than size T.)
 *     Next, check the user's log to calculate mean and standard deviation. Maybe the log can be 
 *     a linked list of size T, and mean and standard deviation can be updated by simply checking
 *     the values added and removed. Math it out to check this... (Checks out! We just need to 
 *     store and update a sum and a sum of squares).
 *     * When first building the network, we don't need to flag purchases, so we don't need to
 *     waste time crawling social networks.
 *     ** We could optionally build the sn-purchase histories of all users between the batch and
 *     stream steps. This would add time overall, but it would save time streaming.
 *     Long term it would be negligible either way. 
 *     
 * Data: In log_stream, there are 779 purchases. 752 of these are the first purchases
 *       of a user within log_stream, so it is expected that the social network purchase
 *       history is built. Of the remaining 25, only four need to be rebuilt. These are
 *       pretty small numbers, but it sounds promising that we made the correct decision.
 * 
 * User:
 *  - Store personal purchase history (up to size T; should allow for smaller sizes).
 *  - Store list (pointers) for friends.
 *  - Store social network purchase history (up to size T; should allow for smaller sizes).
 *    - Also store a sum and a sum-of-squares for this element. Update it whenever
 *      a purchase is added. Use these to compute mean and standard deviation.
 *    - Also have a flag for compiling or re-compiling this list when needed.
 *    * Alternatively don't store this list (i.e. it's always flagged). This saves space,
 *      but it's yet to be determined if it's better.
 *     
 */




