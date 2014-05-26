package simplestream;
import java.net.*;
import java.io.*;
import org.json.JSONObject;
import org.json.JSONException;

public class GetImgThread extends Thread{
	// this may also hook up to local Viewer and decompress and play?
    // kc: local Viewer has already been instantiated, and waits for frames
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
                // Due to a 64k limit on DataOutputStream.writeUTF(), we have to
                // simulate our own writeUTF that has no limitations on String length.
                // Here, we have created our own 'version' of DataInputStream.readUTF()
                int length = is.readInt();
                byte[] data = new byte[length];
                is.readFully(data);
                String msgReceived = new String(data,"UTF-8");

				//System.out.println("Received: " + msgReceived);
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
            // kc: FIXED. msg not received because stoppedstream msg was not
            // being placed on outputstream (in MsgPassingThread)
				socket.close();
				System.out.println("Closed connection.");
				throw new InterruptedException();
			} else {
                SetImg.img = msg;
            }

		} catch(IOException e){
			System.out.println("Connection: " + e.getMessage());
		} catch(JSONException e){
			e.printStackTrace();
			System.exit(-1);
		}
	}
}

