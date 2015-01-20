/* Server.java
* Author: Anela Chan
* Date: 29 May 2014
* Description: Listens for and accepts connections
*/

package simplestream;
import java.net.*;
import java.io.*;
import java.util.ArrayList;
import org.json.JSONObject;
import org.json.JSONException;

public class Server extends Thread{

	/* for serving connections */
	private ServerSocket listenSocket = null;
	private int sport = 6262; // LISTENING port
	private ArrayList<MsgPassingThread> clientThreads; // needed to close
	/* maintain for handover response */
	private String hostName = null;
	private int rport = 0;
	private JSONObject remoteServerData = null; // remote server data IF SS in remote mode 
	private ArrayList<JSONObject> connections; // SHARED, will hold IP and sport of each client


	// if SimpleStreamer in local mode
	Server(int sp){
		sport = sp;
	    connections = new ArrayList<JSONObject>(3);
	    clientThreads = new ArrayList<MsgPassingThread>(3);
	    this.setSocket();
	    this.start();
    }

    // if SimpleStreamer in remote mode
	Server(int sp, String hn, int rp){
		sport = sp;
		hostName = hn;
		rport = rp;
	    connections = new ArrayList<JSONObject>(3);
	    clientThreads = new ArrayList<MsgPassingThread>(3);
      this.setSocket();
      this.setServerData();
      this.start();
    }

	public void run(){
		while(true){
			try{
				Socket clientSocket = listenSocket.accept(); // can't interrupt

				// add to addresses ArrayList
				SocketAddress clientIP = clientSocket.getRemoteSocketAddress();
				String clientIPString = editIP(clientIP);
				// if on same machine, change IP from localhost
				if(clientIPString.equals("127.0.0.1"))
					clientIPString = InetAddress.getLocalHost().toString().split("/")[1];
                addToConnections(clientIPString);

				// spawn new client thread
				MsgPassingThread clientThread = new MsgPassingThread(
										clientSocket, connections, remoteServerData);
				clientThreads.add(clientThread);
			}
			catch (IOException e){
				break;
			} catch (JSONException e){
				System.out.println("JSONObject: " + e.getMessage());
			} 
		}
		return;
	}

	@Override
	public void interrupt(){
		try{
			listenSocket.close();
			System.out.println("Server listening socket closed.");
			// stop all the threads, close the sockets
			for(int i = 0; i < clientThreads.size(); i++){
				clientThreads.get(i).getSocket().close();
				clientThreads.get(i).interrupt();
			}
		} catch(IOException ignored){
		} finally{
			super.interrupt();
		}
	}

	private void setSocket(){
		try{
			listenSocket = new ServerSocket(sport);
			System.out.println("Server listening for connection...");
		} catch(IOException e){
			System.out.println("Listen socket: " + e.getMessage());
		}
	}

	private void setServerData(){
		try{
			remoteServerData = new JSONObject();
			remoteServerData.put("ip",hostName);
			remoteServerData.put("port",rport);
		} catch(JSONException e){
			e.printStackTrace();
			System.exit(-1);
		}
	}

	private static String editIP(SocketAddress clientIP){
		String version0 = clientIP.toString();
		String[] version1 = version0.split(":");
		String[] version2 = version1[0].split("/");
		return version2[1];
	}


	private void addToConnections(String clientIP) throws JSONException{
		JSONObject obj = new JSONObject();
		obj.put("ip",clientIP);
    System.out.println("clientIP added to connections in Server: " + clientIP);
		connections.add(obj);
	}

}