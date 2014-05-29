
/* MsgPassingThread.java
Author: Anela Chan and King Chan
Date: 17 May 2014
Description: Each client connection serviced by Server has 
own MsgPassingThread, where main message-passing of protocol occurs.

Upon receiving a startstream request, will instantiate a SendImgThread 
which actually sends the images while simultaneously polling for a stop
*/

package simplestream;
import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import org.json.JSONObject;
import org.json.JSONException;

public class MsgPassingThread extends Thread{

	/* for operating on socket */
	private Socket socket;
	private DataInputStream is = null;
	private DataOutputStream os = null;
	private ArrayList<Socket> allSockets = null; // shared with server

	/* for generating overloaded/handover response */
	private ArrayList<JSONObject> connections = null; // shared with server
	private JSONObject remoteServerData = null; // shared with server, built by server
	private JSONObject clientData = null; // for each client
	private int numClients;

	/* specifications sent by client */
	private int sleepTime = 100;
	private int sport = 6262;

	private boolean stopStream = false;
	private SendImgThread sendImgThread = null;

	MsgPassingThread(Socket s, ArrayList<JSONObject> addresses, JSONObject rsd){
		socket = s;
		connections = addresses; 
		numClients = addresses.size();
		remoteServerData = rsd;
		try{
			is = new DataInputStream(socket.getInputStream());
			os = new DataOutputStream(socket.getOutputStream());				
		} catch(IOException e1){
			System.out.println("Connection: "+e1.getMessage());
		}
		this.start();
	}

	public Socket getSocket(){
		return socket;
	}

	public void run(){

		while(!Thread.interrupted()){
			try{
				// send status msg to client
				StatusResponse status = new StatusResponse("local",Math.min(numClients,3));
				String statusMsg = status.toJSONString();
				os.writeUTF(statusMsg); 
				System.out.println("Sent: " + statusMsg);

				// receive and process startstream request
				String startMsg = is.readUTF();
				System.out.println("Received: " + startMsg);

				if(numClients <= 3)
					this.runNormal(startMsg);
				else{
					connections = new ArrayList<JSONObject>(connections.subList(0,3)); // slice to size 3
					OverloadedResponse overloadResp = new OverloadedResponse(connections,remoteServerData);
					String overloadMsg = overloadResp.toJSONString();
							
					os.writeUTF(overloadMsg);
					System.out.println("OverloadedResponse: " + overloadMsg);
				}
									
			} catch(IOException ignored){
			} catch (JSONException e){
				System.out.println("JSONObject: " + e.getMessage());
			} 
		}
		
		if(sendImgThread != null)
			sendImgThread.interrupt();
		return;
			
	}

	public void runNormal(String startMsg) throws IOException, JSONException{
		processStart(startMsg);
		// send response
		GenericResponse startResp = new GenericResponse("startingstream");
		String startingMsg = startResp.toJSONString();
		os.writeUTF(startingMsg);
		System.out.println("Sent: " + startingMsg);

		// start sending images, takes over os
		sendImgThread = new SendImgThread(os,sleepTime);

		// simultaneously poll for a stop request
		while(!stopStream){
			String msgReceived = is.readUTF();
			System.out.println("Received: " + msgReceived);
			processStop(msgReceived);
		}

		// when stopstream request received, interrupt, send ack, close.
		if(stopStream){

			sendImgThread.interrupt(); 

			GenericResponse stopResp = new GenericResponse("stoppedstream");
			String stopMsg = stopResp.toJSONString();

            byte[] data = stopMsg.getBytes("UTF-8");
            os.writeInt(data.length);
            os.write(data);

			System.out.println("Sent: " + stopMsg);

			// give a moment for client to receive stoppedstream
			try{
				Thread.sleep(500); 
			} catch(InterruptedException e){
				Thread.currentThread().interrupt();
				return;
			}

			// close the client's socket
			is.close();
			os.close();
			socket.close();
			System.out.println("Connection closed.");

			// remove client from list of connections
			connections.remove(clientData);
		}

	}

	private synchronized void processStart(String msg) throws JSONException{
		/* inspect client's specifications */
		JSONObject obj = new JSONObject(msg);
		if(!obj.get("request").equals("startstream"))
			assert(false); // client should not send anything besides startstream

		if(obj.has("ratelimit")){
			int st = (Integer)obj.get("ratelimit");
			if (st > 100) sleepTime = st; // ignore if <= 100
		}
		if(obj.has("sport")){
			sport = (Integer)obj.get("sport"); // otherwise defaults to 6262
        }

		/* store client port info */
		clientData = connections.get(connections.size() - 1);
		clientData.put("port",sport);

	}

	private void processStop(String msg) throws JSONException{

		JSONObject obj = new JSONObject(msg);
		if(!obj.get("request").equals("stopstream"))
			assert(false);
		else{
			stopStream = true;
		}

	}

}
