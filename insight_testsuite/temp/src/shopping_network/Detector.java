package shopping_network;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * The main class for the algorithm.
 * This class deals with the reading and writing of files.
 * @author Kevin
 *
 */
public class Detector {
	
	/**
	 * Runs the anomalous file detector for log files in the given input directory.
	 * Prints the files to a file in the given output directory.
	 * The first argument given should be the input directory, and the second should be
	 * the output directory.
	 * In the input directory, there should be files "batch_log.json" and "stream_log.json".
	 * A file "flagged_purchases.json" will be made or rewritten in the output directory.
	 * @param args An array of strings
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException{
		if(args.length != 2){
			throw new IllegalArgumentException("Input not valid. Please give exactly two parameters."
					+ " These parameters should be the input directory followed by the output directory.");
		}
		//File testDir1 = new File(args[0]);
		//File testDir2 = new File(args[1]);
		//if(testDir1.isDirectory()){
		//	throw new IllegalArgumentException("Input directory not found or not a valid directory.");
		//}
		//testDir2.mkdir();//This should throw an exception if the directory is not valid
		
		networkUpdates(args[0], args[1]);
		//TODO: make sure input is valid here. Check that directories and files exist
		//and return appropriate exceptions if they don't.
	}
	
	
	
	
	public static void networkUpdates(String inDirectory, String outDirectory) throws IOException{
		String batchPath = inDirectory + File.separator + "batch_log.json";
		String streamPath = inDirectory + File.separator + "stream_log.json";
		networkUpdates(batchPath, streamPath, outDirectory);
	}
	
	
	/**
	 * Reads batch and stream logs, and determines purchases in the stream file that are anomalous.
	 * Logs the anomalous purchases in a new file "flagged_purchases.json" in the output directory.
	 * 
	 * @param batchPath The file path for the batch log
	 * @param streamPath The file path for the stream log
	 * @param outDirectory The output directory
	 * @throws IOException
	 */
	public static void networkUpdates(String batchPath, String streamPath, String outDirectory) throws IOException{
		Network n = new Network();
		
		String line = null;
		BufferedReader br = new BufferedReader(new FileReader(batchPath));
		
		line = br.readLine();
		if(line == null){
			br.close();
			return;
		}
		Event.setParameters(line);
		
		while((line = br.readLine()) != null){
			Event.parseEvent(n, line, false);
		}
		br.close();
		
		//File outFile = new File(outDirectory+System.lineSeparator()+"flagged_purchases.json");
		//outFile.createNewFile();//doesn't create new one if it already exists
		String outFile = outDirectory+File.separator+"flagged_purchases.json";
		BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
		
		
		line = null;
		BufferedReader br2 = new BufferedReader(new FileReader(streamPath));
		while((line = br2.readLine()) != null){
			String s = Event.parseEvent(n, line, true);
			if(s != null){
				bw.write(s);
				bw.newLine();
			}
		}
		//bw.newLine();
		br2.close();
		bw.close();
	}

}
