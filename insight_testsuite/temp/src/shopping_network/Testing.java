package shopping_network;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is used solely for testing.
 * @author Kevin
 *
 */
public class Testing {
	public static void main(String[] args) throws IOException{
		String batchPath = "./sample_dataset/batch_log.json";
		String streamPath = "./sample_dataset/stream_log.json";
		
		String  smallBatchPath = "log_input/batch_log.json";
		String  smallStreamPath = "log_input/stream_log.json";
		
		String  exBatchPath = "sample_dataset_2/batch_log.json";
		String  exStreamPath = "sample_dataset_2/stream_log.json";
		
		String inDirectory1 = "./log_input";
		String inDirectory2 = "./sample_dataset";
		String inDirectory3 = "./sample_dataset_2";
		String outDirectory = "./log_output";
		
		//Detector.networkUpdates(inDirectory2, outDirectory);
		
		
		sortingTest(100);
	}
	
	
	/**
	 * Prints statistics about the file to the console.
	 * 
	 * Notable stats: About 80% of events are purchases. About 19% are befriends,
	 * and about 1% are unfriends.
	 * 
	 * Other observations: There are exactly 10000 users with IDs 0 through 9999.
	 * Based on the total number of edges, each user has about 10 friends.
	 * 
	 * @param filePath A String.
	 * @throws IOException
	 */
	public static void fileStats(String filePath) throws IOException{
		
		String pattern = Event.pattern;
		Pattern p = Pattern.compile(pattern);
		
		String line = null;
		
		BufferedReader br = new BufferedReader(new FileReader(filePath));
		
		int numTotal = 0;
		int numPurchases = 0;
		int numBefriends = 0;
		int numUnfriends = 0;
		int highestID = 0;
		
		long firstTime = System.currentTimeMillis();
		long lastTime = 0;
		
		
		while((line = br.readLine()) != null){
			Matcher m = p.matcher(line);
			if(m.matches()){
				numTotal++;
				switch(m.group(1)){
				case "purchase":
					numPurchases++;
					int id = Integer.parseInt(m.group(3));
					highestID = highestID < id ? id : highestID;
					break;
				case "befriend":
					numBefriends++;
					int id1 = Integer.parseInt(m.group(3));
					highestID = highestID < id1 ? id1 : highestID;
					int id2 = Integer.parseInt(m.group(4));
					highestID = highestID < id2 ? id2 : highestID;
					break;
				case "unfriend":
					numUnfriends++;
					int id1b = Integer.parseInt(m.group(3));
					highestID = highestID < id1b ? id1b : highestID;
					int id2b = Integer.parseInt(m.group(4));
					highestID = highestID < id2b ? id2b : highestID;
					break;
				default:
					break;
				}
				
				Timestamp t = Timestamp.valueOf(m.group(2));
				if(t.getTime() > lastTime){
					lastTime = t.getTime();
				}
				if(t.getTime() < firstTime){
					firstTime = t.getTime();
				}
				
				
				
				
				
			} else {
				System.out.println(line);
			}
			
		}
		br.close();
		System.out.println("Total number of events: "+numTotal);
		System.out.println("Highest ID: "+ highestID);
		System.out.println("Number of purchases: "+numPurchases);
		System.out.println("Number of befriends: "+numBefriends);
		System.out.println("Number of unfriends: "+numUnfriends);
		
		String start = new Timestamp(firstTime).toString();
		String end = new Timestamp(lastTime).toString();
		System.out.println("Time range: "+start+ " to "+ end);
		
	}
	
	/**
	 * Generates a sample network for further analysis.
	 * 
	 * Observations: All 10000 users are active.
	 * 
	 * @param filePath
	 * @throws IOException
	 */
	public static void sampleNetwork(String filePath) throws IOException{
		String pattern = Event.pattern;
		Pattern p = Pattern.compile(pattern);
		
		String line = null;
		
		boolean[] userExists = new boolean[10000];
		
		BufferedReader br = new BufferedReader(new FileReader(filePath));
		
		while((line = br.readLine()) != null){
			Matcher m = p.matcher(line);
			if(m.matches()){
				int id = Integer.parseInt(m.group(3));
				userExists[id] = true;
				if(!m.group(1).equals("purchase")){
					int id2 = Integer.parseInt(m.group(4));
					userExists[id2] = true;
				}
				
				
			} else {
				System.out.println(line);
			}
			
		}
		br.close();
		
		int nonExistingUsers = 0;
		for(int i = 0; i < userExists.length; i++){
			if(!userExists[i]){
				nonExistingUsers++;
			}
		}
		System.out.println("There are "+nonExistingUsers+" users without any activity.");
		
	}
	
	
	
	
	
	
	/**
	 * Regex checker for a few examples.
	 */
	public void checkRegex(){
		String ex1 = "{\"event_type\":\"purchase\", \"timestamp\":\"2017-06-14 18:46:50\", \"id\": \"581\", \"amount\": \"29.04\"}";
		String ex2 = "{\"event_type\":\"befriend\", \"timestamp\":\"2017-06-14 18:46:50\", \"id1\": \"3792\", \"id2\": \"3048\"}";
		String ex3 = "{\"event_type\":\"unfriend\", \"timestamp\":\"2017-06-14 18:46:50\", \"id1\": \"907\", \"id2\": \"4553\"}";
		
		String pattern = Event.pattern;
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(ex3);
		
		if(m.find()){
			System.out.println(m.group(0));
			System.out.println(m.group(1));
			System.out.println(m.group(2));
			System.out.println(m.group(3));
			System.out.println(m.group(4));
		} else {
			System.out.println("fail "+ex3);
			System.out.println(pattern);
		}
		
		
	}
	
	
	public static void checkTimestampRegex(Purchase p){
		String timestamp = p.getTimestamp().toString();
		
		
		String timestampRegex = "^[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}";
		Pattern pat = Pattern.compile(timestampRegex);
		Matcher m = pat.matcher(timestamp);
		
		System.out.print(timestamp+" ");
		if(m.find()){
			System.out.println(m.group(0));
			
		}
		
		
	}
	
	
	
	
	
	/**
	 * Builds a social network of a certain degree, then tests it for updates.
	 * That is, counts how many of the purchases
	 * @param degrees
	 * @param batchPath
	 * @param streamPath
	 * @throws IOException 
	 */
	public static void networkUpdates(String batchPath, String streamPath, int displayStats) throws IOException{
		//TODO
		//int i = 0;
		//for(int i = 1)
		
		Network n = new Network();
		
		String line = null;
		BufferedReader br = new BufferedReader(new FileReader(batchPath));
		
		line = br.readLine();
		if(line == null){
			br.close();
			return;
		}
		Event.setParameters(line);
		
		int counter = 0;
		
		if(displayStats > 0){
			displayNetworkStats(n);
		}
		while((line = br.readLine()) != null){
			Event.parseEvent(n, line, false);
			if(counter < displayStats){
				displayNetworkStats(n);
				counter++;
			}
		}
		br.close();
		
		if(displayStats > 0){
			System.out.println("Now running stream...");
		}
		counter = 0;
		line = null;
		BufferedReader br2 = new BufferedReader(new FileReader(streamPath));
		while((line = br2.readLine()) != null){
			Event.parseEvent(n, line, true);
			if(counter < displayStats){
				displayNetworkStats(n);
				counter++;
			}
		}
		br2.close();
		
		
		
	}
	
	
	public static void displayUserStats(User u){
		if(u.getPurchases().size() == 0 && u.getFriends().size() == 0){
			return;
		}
		
		System.out.print("ID: "+u.getID()+" Friends: ");
		ArrayList<User> friends = u.getFriends();
		for(User v : friends){
			System.out.print(v.getID()+", ");
		}
		//System.out.println();
		LinkedList<Purchase> purchases = u.getPurchases();
		System.out.print("Purchases: ");
		for(Purchase p : purchases){
			System.out.print(p.getPrice()+", ");
		}
		System.out.println();
	}
	
	public static void displayUserStats(User u, boolean snStats){
		if(u.getPurchases().size() == 0 && u.getFriends().size() == 0){
			return;
		}
		displayUserStats(u);
		if(snStats){
			System.out.print("    Social Network: ");
			ArrayList<User> sn = u.socialNetwork(Event.D);
			for(User v : sn){
				System.out.print(v.getID() + ", ");
			}
		}
	}
	
	
	public static void displayNetworkStats(Network n){
		
		int size = n.getNumUsers();
		for(int i = 0; i < size; i++){
			displayUserStats(n.getUser(i));
		}
		System.out.println();
		
	}
	
	
	/**
	 * Tests to ensure the double is being formatted correctly.
	 * It should be rounded down and contain exactly two decimal places.
	 * No scientific notation.
	 */
	public static void decimalFormatTests(){
		Double d = (double)0;//change the double here
		System.out.println(d.toString());
		DecimalFormat df = new DecimalFormat("0");
		df.setMaximumFractionDigits(2);
		df.setMinimumFractionDigits(2);
		df.setRoundingMode(RoundingMode.DOWN);
		System.out.println(df.format(d));
	}
	
	
	public static void sortingTest(int numElements){
		ArrayList<Purchase> testPur = new ArrayList<Purchase>();
		ArrayList<ListIterator<Purchase>> testAlip = new ArrayList<ListIterator<Purchase>>();
		
		Timestamp t1 = new Timestamp(0);
		Timestamp t2 = new Timestamp(numElements);
		
		//ArrayList<Integer> permutation = new ArrayList<Integer>();
		for(int i = 0; i < numElements; i++){
			Purchase p = new Purchase(0, 0, i%2 == 0 ? t1 : t2, i);
			testPur.add(p);
		}
		Collections.shuffle(testPur);
		for(int i = 0; i < numElements; i++){
			ArrayList<Purchase> temp = new ArrayList<Purchase>();
			Purchase p = testPur.get(i);
			temp.add(p);
			ListIterator<Purchase> lTemp = temp.listIterator(1);
			testAlip.add(lTemp);
			
			long l = p.getPurchaseNum() + p.getTimestamp().getTime();
			System.out.print(l + " ");
		}
		System.out.println("sorting...");
		User.quickSort(testPur, testAlip, 0, numElements-1);
		
		for(int i = 0; i < numElements; i++){
			Purchase p = testPur.get(i);
			long l = p.getPurchaseNum() + p.getTimestamp().getTime();
			System.out.print(l + " ");
		}
		System.out.println("now checking list iterators...");
		for(int i = 0; i < numElements; i++){
			Purchase p = testAlip.get(i).previous();
			long l = p.getPurchaseNum() + p.getTimestamp().getTime();
			System.out.print(l + " ");
		}
		
		
		
		
		
		
		
	}
	
	
	
	
	
	
}
