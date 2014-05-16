package simplestream;
import java.net.*;
import java.io.*;
import org.json.JSONObject;
import org.json.JSONException;

public class GetImgThread extends Thread{
	// this may also hook up to local Viewer and decompress and play?
	private DataInputStream is;
	private Socket socket;

	GetImgThread(Socket s, DataInputStream myIS){
		is = myIS;
		socket = s;
		this.start();
	}

	public void run(){
		while(true){
			try{
				String msgReceived = is.readUTF();
				System.out.println("Received: " + msgReceived);
				processMsg(msgReceived);
			} catch(IOException e){
				return; 
			} catch(InterruptedException e){
				return;
			}
		if(Thread.interrupted())
			return;
		} 
	}

	private void processMsg(String msg) throws InterruptedException{
		try{
			JSONObject obj = new JSONObject(msg);
			if(obj.get("response").equals("stoppedstream")){ 
			// for some reason, this msg is never printed and I don't know why...
				socket.close();
				System.out.println("Closed connection.");
				throw new InterruptedException();
			}	
		} catch(IOException e){
			System.out.println("Connection: " + e.getMessage());
		} catch(JSONException e){
			e.printStackTrace();
			System.exit(-1);
		}
	}
}

