package simplestream;
import java.net.*;
import java.io.*;
import java.util.Scanner;
import org.json.JSONObject;
import org.json.JSONException;

public class Client extends Thread{

	private int sport; //sport is port for serving connection requests
	private String hostName;
	private int rport; 
	private int sleepTime;
	private Scanner keyboard;
	boolean appLive = false;

	private Socket s = null;
	private DataInputStream is = null;
	private DataOutputStream os = null;

	Client(int sp, String hn, int rp, int st, Scanner k, boolean a){
		sport = sp;
		hostName = hn;
		rport = rp;
		sleepTime = st;
		keyboard = k;
		appLive = a;
		this.start();
	}

	@Override
	public void run(){
		try{
			// connect to server
			s = new Socket(hostName, rport);
			System.out.println("Connection established.");
			is = new DataInputStream(s.getInputStream());
			os = new DataOutputStream(s.getOutputStream());	

			// read status message
			System.out.println("Received: " + is.readUTF());
			// TODO: add functionality to not try to request if clients = 3,
			// or not send ratelimit info if ratelimit not being implemented
			// though technically the server should just ignore us if we 
			// give a ratelimit and it's not being implemented

			// send startstream request, we always send parameters 
			// (as of now, see above)
			StartRequest startReq = new StartRequest(sleepTime,sport);
			String startMsg = startReq.toJSONString();
			os.writeUTF(startMsg);
			System.out.println("Sent: " + startMsg);

			// read startingstream ack
			String startAck = is.readUTF();
			System.out.println("Received: " + startAck);
			
			boolean serverOverloaded = processStartAck(startAck);
			if(serverOverloaded)
				System.out.println("Sorry server is overloaded."); 
				// TODO: implement handover functionality here
			else this.runNormal();
		} catch(UnknownHostException e){
			System.out.println("Socket: " + e.getMessage());
		} catch(IOException e){
			System.out.println("Connection: " + e.getMessage());
		}
	}

	private static boolean processStartAck(String msg){
		try{
			JSONObject obj = new JSONObject(msg);
			if(obj.get("response").equals("overloaded"))
				return true;
		} catch(JSONException e){
			e.printStackTrace();
			System.exit(-1);
		}
		return false;
	}

	private void runNormal(){
		// start receiving images
		GetImgThread getImgThread = new GetImgThread(s,is);
		// listen for stop command... fix this in the case they enter something else?
		if(keyboard.nextLine().isEmpty()){ 
			StopRequest stopReq = new StopRequest();
			String stopMsg = stopReq.toJSONString();
			try{
				os.writeUTF(stopMsg);	
			} catch(IOException e){
				System.out.println("Connection: " + e.getMessage());
			}		
		}
		/* when it receives stoppedstream ack, 
			GetImgThread will stop ITSELF (that thread has now taken over the input stream)
			AND close the connection. the only way to do this synchronously...
			should also add functionality so that it kills ALL other threads
			including the Server by flagging the appLive as false. 
			btw it is also possible to nest the thread inside Client...
			not sure which is better */
	}

}