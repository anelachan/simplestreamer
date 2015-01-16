/* SendImgThread.java
* Author: Anela Chan and King Chan
* Date: 29 May 2014
* Description: Instantiated by MsgPassingThread, sends out the image
* data while the MsgPassingThread concurrently polls for a stop request.
*/

package simplestream;
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
		while(!Thread.interrupted()){
			try{
                // Due to a 64k limit on DataOutputStream.writeUTF(), we have to
                // simulate our own writeUTF that has no limitations on String length.
                String msgSent = SimpleStreamer.img;
                byte[] data = msgSent.getBytes("UTF-8");
                os.writeInt(data.length);
                os.write(data);
				Thread.sleep(sleepTime);
			} catch(InterruptedException e){
				return;
			} catch(IOException ignored){
			}
		}
		return;
	}
}