package simplestream;
import java.net.*;
import java.io.*;

public class SendImgThread extends Thread{
	private DataOutputStream os = null;
	private int sleepTime = 100;
    private SimpleStreamer mySimpleStreamer = null;

	SendImgThread(DataOutputStream myOS, int st){
		os = myOS;
		sleepTime = st;
		this.start();
	}

	public void run(){
		while(true){
			try{
                // Due to a 64k limit on DataOutputStream.writeUTF(), we have to
                // simulate our own writeUTF that has no limitations on String length.
                String msgSent = SetImg.img;
                byte[] data = msgSent.getBytes("UTF-8");
                os.writeInt(data.length);
                os.write(data);

                // System.out.println("Sent: " + msgSent);
				Thread.sleep(sleepTime);
			} catch(InterruptedException e){
				return;
			} catch(IOException e){
				System.out.println("SendImgThread: "+e.getMessage());
			}
			if(Thread.interrupted())
				return;
		}	
	}
}