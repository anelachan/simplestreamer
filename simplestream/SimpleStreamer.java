package simplestream;
import java.net.*;
import java.io.*;
import java.util.Scanner;
import org.json.JSONObject;
import org.json.JSONException;

public class SimpleStreamer{

	private int sport = 6262; //sport is port for serving connection requests
	private String hostName;
	private int rport = 6262;
	private int sleepTime = 100;
	boolean appLive = false;


	private Scanner keyboard = null;
	private Server server = null;
	private Client client = null;

	// private Viewer viewer
	// private Camera viewer
	// private ArrayBlockingQueue<JSONObject> imgQueue 
			// only used if server actually has a conn? 

	SimpleStreamer(String[] args){
		sport = Integer.parseInt(args[0]);
		hostName = args[1];
		rport = Integer.parseInt(args[2]);
		sleepTime = Integer.parseInt(args[3]);
		appLive = true;

		keyboard = new Scanner(System.in);
		client = new Client(sport, hostName, rport, sleepTime, keyboard, appLive);
		// having 6 parameters is less than ideal but other option is nesting

	}

	public static void main(String[] args){

		SimpleStreamer app = new SimpleStreamer(args);

	}
}