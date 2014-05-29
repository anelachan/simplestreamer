/* GetImgThread.java
Author: Anela Chan and King Chan
Date: 29 May 2014
Description: Instantiated by the Client, the GetImgThread retrieves 
image data from a remote server and sets the local SimpleStreamer 
viewer's image via SimpleStreamer.img. 

GetImgThread also waits for a "stoppedstream" response from the remote 
server. When it receives it will close the socket and stop.
*/

package simplestream;
import java.net.*;
import java.io.*;
import org.json.JSONObject;
import org.json.JSONException;

public class GetImgThread extends Thread{

	private DataInputStream is;
	private DataOutputStream os;
	private Socket socket;

	GetImgThread(Socket s, DataInputStream myIS, DataOutputStream myOS){
		is = myIS;
		os = myOS;
		socket = s;
		this.start();
	}

	public void run(){
		while(true){
			try{
                // Due to a 64k limit on DataOutputStream.writeUTF(), we have to
                // simulate our own writeUTF that has no limitations on String length.
                // Here, we have created our own 'version' of DataInputStream.readUTF()
                int length = is.readInt();
                byte[] data = new byte[length];
                is.readFully(data);
                String msgReceived = new String(data,"UTF-8");
                processMsg(msgReceived); // will stop and CLOSE SOCKET if receive "stoppedstream"
				
			} catch(IOException e){
				System.out.println("Connection " + e.getMessage());
				break; // if remote server terminates break loop and end
			} catch(InterruptedException e){
				return;
			}
			if(Thread.interrupted())
				return;
		}
		try{
			is.close();
			os.close();
			socket.close();
			System.out.println("Remote server terminated. Closing socket.");
			System.exit(0);
			return;
		} catch (IOException e2){
			System.out.println("Error closing socket.");
		}
	}

	private void processMsg(String msg) throws IOException, InterruptedException{
		try{
			JSONObject obj = new JSONObject(msg);
			if(obj.get("response").equals("image")){
				SimpleStreamer.img = msg; // set img
			} else if(obj.get("response").equals("stoppedstream")){
				System.out.println("Received: " + msg);
				is.close();
				os.close();
                socket.close();
				System.out.println("Closed connection.");
				throw new InterruptedException();
            } else
            	assert false;
		} catch(JSONException e){
			e.printStackTrace();
			System.exit(-1);
		}
	}
}

