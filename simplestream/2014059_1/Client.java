/* Client.java
Authors: Anela Chan and King Chan
Date: 29 May 2014
Description: Run if SimpleStreamer is in remote mode, the Client connects
to a remote SimpleStreamer server process and passes messages according to
the protocol. Once startingstream begins, the Client begins a GetImgThread 
which takes over the input stream and is responsible for closing the socket 
in the case of a request to end the stream. 
Handover functionality is also implemented in the Client, as the Client will 
read an overloaded response and attempt to make a new connection.
*/


package simplestream;
import java.net.*;
import java.io.*;
import java.util.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;

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

    /* receive from server */
    private String startResp = null;
    private JSONObject startRespJSON = null;
    private boolean rateLimited = false;
    private boolean serverOverloaded = false;
    private ArrayList<JSONObject> handoverArrayList = null;

    private volatile boolean stop = false;

	Client(String hn, int rp, int sp, int st){
		
		hostName = hn;
		rport = rp;

        sport = sp;
		sleepTime = st;

		this.start();
	}

	@Override
	public void run() {
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
		} catch (JSONException e){
            System.out.println("JSONObject: " + e.getMessage());
        }
	}

    private void connectStart() throws IOException, UnknownHostException, JSONException{
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

    private void makeConnection() throws UnknownHostException, IOException{
        s = new Socket(hostName, rport);
        System.out.println("Connection established.");
        is = new DataInputStream(s.getInputStream());
        os = new DataOutputStream(s.getOutputStream());
    }

    private void processStatusResp(String msg) throws JSONException{
        JSONObject obj = new JSONObject(msg);
        if(obj.get("ratelimiting").equals("yes"))
            rateLimited = true;

    }

    private void processStartResp(String msg) throws JSONException{
        startRespJSON = new JSONObject(msg); // build JSONObject of resp
        if(startRespJSON.get("response").equals("overloaded"))
            serverOverloaded = true;
        else
            serverOverloaded = false;
    }

    private void makeStartRequest() throws UnknownHostException, IOException{
        StartRequest startReq;
        if(rateLimited) startReq = new StartRequest(sleepTime, sport);
        else startReq = new StartRequest(sport);
        os.writeUTF(startReq.toJSONString());  
    }

    private void runNormal() throws IOException{
        // start receiving images
        GetImgThread getImgThread = new GetImgThread(s,is,os);

        // entering new line on client side will trigger stop
        while(true){
            if (stop) {
                break;
            }
        }

        // send out stop request
        StopRequest stopReq = new StopRequest();
        String stopMsg = stopReq.toJSONString();
        os.writeUTF(stopMsg);  
        System.out.println("Sent: " + stopMsg);
        
        /* handling final stoppedstream ack happens in GetImgThread
        as GetImgThread has taken over the input stream 
        GetImgThread will close the streams and socket. */
    }

    public void stopClient(){
        stop = true;
    }

    private void fillHandoverArrayList(JSONObject startRespJSON) throws JSONException{

        if (handoverArrayList == null) {
            handoverArrayList = new ArrayList<JSONObject>();
        }

        if (startRespJSON.has("server")) {
            JSONObject serverJSON = new JSONObject(startRespJSON.get("server").toString());
            handoverArrayList.add(serverJSON);
        }

        JSONArray clientJSONArray = new JSONArray(startRespJSON.get("clients").toString());

        for (int i = 0; i < clientJSONArray.length(); i++) {
            JSONObject clientJSON = (JSONObject) clientJSONArray.get(i);
            handoverArrayList.add(clientJSON);
        }

        System.out.println("handoverArrayList:" + handoverArrayList);
    }

    private void runHandover() throws UnknownHostException, IOException, JSONException {
        // Close current datastreams and socket
        System.out.println("Sorry server is overloaded.");
        is.close();
        os.close();
        s.close();

        while(serverOverloaded) {

            this.fillHandoverArrayList(startRespJSON);
            System.out.println("serverOverloaded:" + serverOverloaded);

            JSONObject handoverJSON = null;
            try {
                handoverJSON = handoverArrayList.remove(0);
            } catch (NullPointerException e) {
                System.out.println("No alternate servers available");
                System.exit(0);
                // close client
            }
            hostName = (String) handoverJSON.get("ip");
            rport = (Integer) handoverJSON.get("port");
            System.out.println("Server - IP: " + hostName + " port: " + Integer.toString(rport));
            this.connectStart();
            if (!serverOverloaded)
                break;
        }
        System.out.println("Handover Success: Found another server to receive the desired stream from.");
        this.runNormal();
    }

}