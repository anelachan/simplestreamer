package simplestream;
import java.net.*;
import java.io.*;
import java.util.ArrayList;
import org.json.JSONObject;
import org.json.JSONException;

public class Server extends Thread{

	private ServerSocket listenSocket = null;
	private int sport = 6262;

	boolean appLive = false; // SHARED
	ArrayList<JSONObject> connections; // SHARED
	JSONObject serverData = null; // SHARED

	Server(int sp, boolean aL){
		appLive = aL;
		sport = sp;
		clientsRemaining = 3;
		connections = new ArrayList<JSONObject>(3);
		this.setSocket();
		this.setServerData();
		this.start();
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
			InetAddress serverIP = listenSocket.getInetAddress();
			String serverIPString = serverIP.getHostAddress();
			serverData = new JSONObject();
			serverData.put("ip",serverIPString);
			serverData.put("port",sport);
		} catch(JSONException e){
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public void run(){
		while(appLive){
			try{
				Socket clientSocket = listenSocket.accept();

				// add to addresses ArrayList
				SocketAddress clientIP = clientSocket.getRemoteSocketAddress();
				String clientIPString = editIP(clientIP);
				addToConnections(clientIPString);

				System.out.println("Received connection, IP: " + clientIPString);
				// TODO: also spawn a new queue for each client somehow?
				MsgPassingThread clientThread = new MsgPassingThread(
										clientSocket, connections, serverData);
			} catch (IOException e2){
				System.out.println("Listen socket: " + e2.getMessage());
			}
		}
	}

	private static String editIP(SocketAddress clientIP){
		String version0 = clientIP.toString();
		String[] version1 = version0.split(":");
		String[] version2 = version1[0].split("/");
		return version2[1];
	}

	private void addToConnections(String clientIP){
		try{
			JSONObject obj = new JSONObject();
			obj.put("ip",clientIP);
			connections.add(obj);
		} catch (JSONException e){
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public static void main(String[] args){
		Server myServer = new Server(6262,true);
	}
}