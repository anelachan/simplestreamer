package simplestream;
import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import org.json.JSONObject;
import org.json.JSONException;

public class MsgPassingThread extends Thread{

	private Socket socket; // each connected client has own
	private DataInputStream is = null;
	private DataOutputStream os = null;
	// ArrayBlockingQueue<JSONObject> imgQ = null; ...each connected client should get its own?
	ArrayList<JSONObject> connections = null; // shared with server
	JSONObject serverData = null; // shared with server, built by server
	JSONObject clientData = null; // for each client


	private int sleepTime = 100;
	private int numClients;
	private int sport = 6262; // client's serving port
	private boolean stopStream = false;
	
	MsgPassingThread(Socket s, ArrayList<JSONObject> addresses, JSONObject sd){
		socket = s;
		connections = addresses; 
		numClients = addresses.size();
		serverData = sd;
		try{
			is = new DataInputStream(socket.getInputStream());
			os = new DataOutputStream(socket.getOutputStream());				
		} catch(IOException e1){
			System.out.println("Connection: "+e1.getMessage());
		}
		this.start();
	}

	public void run(){

		try{
			// send status msg to client
			StatusResponse status = new StatusResponse("local",numClients-1);
			String statusMsg = status.toJSONString();
			os.writeUTF(statusMsg); 
			System.out.println("Sent: " + statusMsg);

			// receive and process startstream request
			String startMsg = is.readUTF();
			System.out.println("Received: " + startMsg);
			
			if(numClients <= 3) // includes current client!
				this.runNormal(startMsg);
			else{
				connections = new ArrayList<JSONObject>(connections.subList(0,2)); // slice to size 3
				OverloadedResponse overloadResp = new OverloadedResponse(connections,serverData);
				String overloadMsg = overloadResp.toJSONString();
				
				os.writeUTF(overloadMsg);
				System.out.println("Sent: " + overloadMsg);
			}
		} catch(IOException e){
			System.out.println("Connection: " + e.getMessage());
		}

	}

	public void runNormal(String startMsg){
		try{
			processStart(startMsg);
			// send response
			GenericResponse startResp = new GenericResponse("startingstream");
			String startingMsg = startResp.toJSONString();
			os.writeUTF(startingMsg);
			System.out.println("Sent: " + startingMsg);

			// start sending images
			SendImgThread sendImgThread = new SendImgThread(os,sleepTime);

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
				System.out.println("Sent: " + stopMsg);
				socket.close();
				System.out.println("Connection closed.");
				// remove client from list of connections
				connections.remove(clientData);

			}

		} catch(IOException e2){
			System.out.println("Connection: "+e2.getMessage());
		}
	}

	private synchronized void processStart(String msg){
		try{
			/* inspect client's specifications */
			JSONObject obj = new JSONObject(msg);
			if(!obj.get("request").equals("startstream"))
				assert(false); // client should not send anything besides startstream

			if(obj.has("ratelimit")){
				int st = (int)obj.get("ratelimit");
				if (st > 100) sleepTime = st; // ignore if <= 100
			}
			if(obj.has("sport")){
				sport = (int)obj.get("sport"); // otherwise defaults to 6262
			}

			/* store client port info */
			clientData = connections.get(connections.size() - 1);
			clientData.put("port",sport);
		} catch (JSONException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	private void processStop(String msg){
		try{
			JSONObject obj = new JSONObject(msg);
			if(!obj.get("request").equals("stopstream"))
				assert(false);
			else stopStream = true;

		} catch(JSONException e){
			e.printStackTrace();
			System.exit(-1);
		}
	}

	private void printConnectionList(){
		ArrayList<String> jsonStrings = new ArrayList<String>(3);
		for (JSONObject el: connections)
			jsonStrings.add(el.toString());
		String connectionList = Arrays.toString(jsonStrings.toArray());
	}
}
