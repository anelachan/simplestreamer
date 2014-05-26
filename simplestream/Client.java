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

        String startAck = null;

		try{
            startAck = processUntilStartAck(sport, hostName, rport, sleepTime, keyboard, appLive);

            boolean serverOverloaded = processStartAck(startAck);

            if(serverOverloaded) {
                System.out.println("Sorry server is overloaded.");
                // Close current datastreams and socket
                is.close();
                os.close();
                s.close();

                // TODO: implement handover functionality here
                String clients = processHandoverList("clients", startAck);
                String server = processHandoverList("server", startAck);

                JSONObject serverData = null;
                JSONArray connections = null;
                List list = new ArrayList();

                int serverPort = 0;
                String serverIP = null;

                try {
                    serverData = new JSONObject(server);
                    connections = new JSONArray(clients);
                    list = toList(connections);
                    serverIP = serverData.get("ip").toString();
                    serverPort = (Integer)serverData.get("port");
                }
                catch (JSONException e) {
                    System.out.println("JSONObject: " + e.getMessage());
                }

                //System.out.println("Server - IP: " + serverIP + " port: " + serverPort);

                JSONObject clientData = null;
                int clientPort = 0;
                String clientIP = null;

                while(serverOverloaded) {

                    // First try the server in the overloaded msg response
                    System.out.println("Server - IP: " + serverIP + " port: " + serverPort);
                    startAck = processUntilStartAck(sport, serverIP, serverPort, sleepTime, keyboard, appLive);
                    serverOverloaded = processStartAck(startAck);
                    System.out.println("serverOverloaded:" + serverOverloaded);
                    if (!serverOverloaded) {
                        break;
                    }

                    // Next try each of the 3 clients in the overloaded msg response
                    for (int i = 0; i < list.size(); i++) {
                        try {
                            clientData = new JSONObject(list.get(i).toString());
                            clientIP = clientData.get("ip").toString();
                            clientPort = (Integer) clientData.get("port");
                        } catch (JSONException e) {
                            System.out.println("JSONObject: " + e.getMessage());
                        }
                        System.out.println("Client " + i + " - IP: " + clientIP + " port: " + clientPort);
                        startAck = processUntilStartAck(sport, clientIP, clientPort, sleepTime, keyboard, appLive);
                        serverOverloaded = processStartAck(startAck);
                        System.out.println("serverOverloaded:" + serverOverloaded);
                        if (!serverOverloaded) {
                            break;
                        }
                    }
                }
                System.out.println("Handover Success: Found another server to receive the desired stream from.");
                this.runNormal();
            }
            else this.runNormal();
		} catch(UnknownHostException e){
			System.out.println("Socket: " + e.getMessage());
		} catch(IOException e){
			System.out.println("Connection: " + e.getMessage());
		}
	}

    public static List toList(JSONArray array) throws JSONException {
        List list = new ArrayList();
        System.out.println("JSONArray Length: " + array.length());
        for (int i = 0; i < array.length(); i++) {
            list.add(fromJson(array.get(i)));
        }
        return list;
    }

    private static Object fromJson(Object json) throws JSONException {
        if (json == JSONObject.NULL) {
            return null;
        } else if (json instanceof JSONArray) {
            return toList((JSONArray) json);
        } else {
            return json;
        }
    }

    private static String processHandoverList(String key, String msg){
        JSONObject obj = null;
        String objString = null;

        try{
            obj = new JSONObject(msg);
            objString = obj.get(key).toString();
            return objString;
        } catch(JSONException e){
            e.printStackTrace();
            System.exit(-1);
        }
        return objString;
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

    private static boolean processStatusResp(String msg){
        try{
            JSONObject obj = new JSONObject(msg);
            if(obj.get("ratelimiting").equals("yes"))
                return true;
        } catch(JSONException e){
            e.printStackTrace();
            System.exit(-1);
        }
        return false;
    }

    private String processUntilStartAck(int sp, String hn, int rp, int st, Scanner k, boolean a) {

        String startAck = null;

        try{
            // connect to server
            s = new Socket(hn, rp);
            System.out.println("Connection established.");
            is = new DataInputStream(s.getInputStream());
            os = new DataOutputStream(s.getOutputStream());

            // TODO: add functionality to not try to request if clients = 3,
            // or not send ratelimit info if ratelimit not being implemented
            // though technically the server should just ignore us if we
            // give a ratelimit and it's not being implemented - completed

            // send startstream request, we always send parameters
            // (as of now, see above)
            // kc: FIXED. startstream request customised based on whether
            // ratelimiting is in effect.

            // read status message
            String statusResp = is.readUTF();
            // System.out.println("Received: " + statusResp);

            // determine if server is ratelimiting
            boolean rateLimited = processStatusResp(statusResp);

            StartRequest startReq;
            if(rateLimited)
                startReq = new StartRequest(st, sp);
            else
                startReq = new StartRequest(sp);

            String startMsg = startReq.toJSONString();
            os.writeUTF(startMsg);
            // System.out.println("Sent: " + startMsg);

            // read startingstream ack
            startAck = is.readUTF();

            //startAck = "{\"response\":\"overloaded\",\"clients\":[{\"ip\":\"localhost\",\"port\":6262},{\"ip\":\"localhost\",\"port\":6262},{\"ip\":\"localhost\",\"port\":6262}],\"server\":{\"ip\":\"124.135.134.134\",\"port\":4532}}";
            System.out.println("Received: " + startAck);

            return startAck;
        } catch(UnknownHostException e){
            System.out.println("Socket: " + e.getMessage());
        } catch(IOException e){
            System.out.println("Connection: " + e.getMessage());
        }

        return startAck;
    }


	private void runNormal(){
		// start receiving images
		GetImgThread getImgThread = new GetImgThread(s,is);
		// listen for stop command... fix this in the case they enter something else?
        // kc: no need i think - the only case we have to handle is for a newline.
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