/* WebCam.java
* Author: King Chan
* Date: 28 May 2014
* Description: Runs thread to capture images from local WebCam.
*/


package simplestream;

import com.github.sarxos.webcam.ds.buildin.natives.Device;
import com.github.sarxos.webcam.ds.buildin.natives.DeviceList;
import com.github.sarxos.webcam.ds.buildin.natives.OpenIMAJGrabber;
import org.apache.commons.codec.binary.Base64;
import org.bridj.Pointer;

public class WebCam extends Thread {

	private OpenIMAJGrabber grabber = null;
	private Device device = null;
	private Pointer<DeviceList> devices;

	WebCam(){
		grabber = new OpenIMAJGrabber();
        devices = grabber.getVideoDevices();
        for (Device d : devices.get().asArrayList()) {
            device = d;
            break;
        }
        this.start();
	}

	@Override
	public void run(){
		
		boolean started = grabber.startSession(320, 240, 30, Pointer.pointerTo(device));
        if (!started) 
            throw new RuntimeException("Not able to start native grabber!");

        while(!Thread.interrupted())
        {
            ImgResponse imgResponseMsg = new ImgResponse(grabNextFrame(grabber));
            SimpleStreamer.img = imgResponseMsg.toJSONString();
        } 
        
        grabber.stopSession();
	}

    private String grabNextFrame(OpenIMAJGrabber grabber) {

        /* Get a frame from the webcam. */
        grabber.nextFrame();
		/* Get the raw bytes of the frame. */
        byte[] raw_image = grabber.getImage().getBytes(320 * 240 * 3);
	    /* Apply a crude kind of image compression. */
        byte[] compressed_image = Compressor.compress(raw_image);
        /* Prepare the date to be sent in a text friendly format. */
        byte[] base64_image = Base64.encodeBase64(compressed_image);
        return new String(Base64.encodeBase64(base64_image));
    }
}