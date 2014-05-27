package simplestream;
import java.net.*;
import java.io.*;
import java.util.*;
import java.util.ArrayList;
import java.util.Scanner;
import org.json.JSONObject;
import org.json.JSONException;
import org.json.JSONArray;

public class Client extends Thread{

    /* for making connection to remote server */
	private String hostName;
	private int rport; 
    private Socket s = null;
    private DataInputStream is = null;
    private DataOutputStream os = null;

    /* for providing info to remote server in startstream request */
    private int sport;
	private int sleepTime;

    /* passed from SimpleStreamer to kill application */
    // CHANGE?
	private Scanner keyboard; 
	boolean appLive = false;

    /* information from server */
    String startResp = null;
    JSONObject startRespJSON = null;
    boolean rateLimited = false;
    boolean serverOverloaded = false;

	Client(int sp, String hn, int rp, int st, Scanner k, boolean a){
		
		hostName = hn;
		rport = rp;

        sport = sp;
		sleepTime = st;

		keyboard = k;
		appLive = a;
		this.start();
	}

	@Override
	public void run(){

        try{
            this.connectStart();
            if(serverOverloaded) 
                this.runHandover();
            else 
                this.runNormal();   
        } catch(UnknownHostException e){
			System.out.println("Socket: " + e.getMessage());
		} catch(IOException e){
			System.out.println("Connection: " + e.getMessage());
		}
	}

    private void connectStart() throws IOException, UnknownHostException{
        // connect to server
        this.makeConnection();
        // receive and process status response
        this.processStatusResp(is.readUTF()); 
        // make appropriate startstream request
        this.makeStartRequest();
        // receive and process startingstream acknowledgement/overloaded response
        startResp = is.readUTF();
        processStartResp(startResp);   
    }

    private void processStartResp(String msg){
        try{
            startRespJSON = new JSONObject(msg); // build JSONObject of resp
            if(startRespJSON.get("response").equals("overloaded"))
                serverOverloaded = true;
        } catch(JSONException e){
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private void runHandover() throws UnknownHostException, IOException{
        // Close current datastreams and socket
        System.out.println("Sorry server is overloaded.");
        is.close();
        os.close();
        s.close();

        // first reset host, rport to remote's remote server's
        // TODO: do not do this if the field doesn't exist!
        JSONArray clientJSONArray = null;
        try {
            JSONObject serverJSON = new JSONObject(startRespJSON.get("server"));
            hostName = (String)serverJSON.get("ip");
            rport = (Integer)serverJSON.get("port");
            clientJSONArray = new JSONArray(startRespJSON.get("clients"));
        }
        catch (JSONException e) {
            System.out.println("JSONObject: " + e.getMessage());
        }

        //System.out.println("Server - IP: " + serverIP + " port: " + serverPort);

        while(serverOverloaded) {

            // First try the server in the overloaded msg response
            System.out.println("Server - IP: " + hostName + " port: " + Integer.toString(rport));
            this.connectStart();
            
            System.out.println("serverOverloaded:" + serverOverloaded);
            if (!serverOverloaded)
                break;


            // Next try each of the 3 clients in the overloaded msg response
            for (int i = 0; i < clientJSONArray.length(); i++) {
                try {
                    JSONObject clientJSON = new JSONObject(clientJSONArray.get(i));
                    hostName = (String)clientJSON.get("ip");
                    rport = (Integer) clientJSON.get("port");
                } catch (JSONException e) {
                    System.out.println("JSONObject: " + e.getMessage());
                }
                    System.out.println("Client " + i + " - IP: " + hostName + " port: " + Integer.toString(rport));
                    this.connectStart();
                    System.out.println("serverOverloaded:" + serverOverloaded);
                    if (!serverOverloaded) {
                        break;                        }
                    }
                }
            System.out.println("Handover Success: Found another server to receive the desired stream from.");
            this.runNormal();

    }

    private void makeConnection() throws UnknownHostException, IOException{
        s = new Socket(hostName, rport);
        System.out.println("Connection established.");
        is = new DataInputStream(s.getInputStream());
        os = new DataOutputStream(s.getOutputStream());
    }

    private void processStatusResp(String msg) throws UnknownHostException, IOException{
        try{
            JSONObject obj = new JSONObject(msg);
            if(obj.get("ratelimiting").equals("yes"))
                rateLimited = true;
        } catch(JSONException e){
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private void makeStartRequest() throws UnknownHostException, IOException{
        StartRequest startReq;
        if(rateLimited) startReq = new StartRequest(sleepTime, sport);
        else startReq = new StartRequest(sport);
        os.writeUTF(startReq.toJSONString());  
    }

	private void runNormal(){
		// start receiving images
		GetImgThread getImgThread = new GetImgThread(s,is);
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