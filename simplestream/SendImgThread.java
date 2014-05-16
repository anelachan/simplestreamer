package simplestream;
import java.net.*;
import java.io.*;

public class SendImgThread extends Thread{
	private DataOutputStream os = null;
	private int sleepTime = 100;

	SendImgThread(DataOutputStream myOS, int st){
		os = myOS;
		sleepTime = st;
		this.start();
	}

	public void run(){
		while(true){
			try{
				ImgResponse imgResp = new ImgResponse("IMAGEDATAHERE");
				String msgSent = imgResp.toJSONString();
				os.writeUTF(msgSent);
				System.out.println("Sent: " + msgSent);
				Thread.sleep(sleepTime);
			} catch(InterruptedException e){
				return;
			} catch(IOException e){
				System.out.println("Connection: "+e.getMessage());
			}
			if(Thread.interrupted())
				return;
		}	
	}
}