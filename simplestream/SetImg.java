package simplestream;

import com.github.sarxos.webcam.ds.buildin.natives.Device;
import com.github.sarxos.webcam.ds.buildin.natives.DeviceList;
import com.github.sarxos.webcam.ds.buildin.natives.OpenIMAJGrabber;
import org.apache.commons.codec.binary.Base64;
import org.bridj.Pointer;

import java.io.IOException;

/**
 * Created by kingchan on 25/05/2014.
 */
public class SetImg {

    public static String img = null;

    // static constructor. Used here to support REMOTE_MODE, by creating the static String img
    static {
    }

    public void generateImgResponse() {

        OpenIMAJGrabber grabber = new OpenIMAJGrabber();
        int bytesAvail = 0;

        Device device = null;
        Pointer<DeviceList> devices = grabber.getVideoDevices();
        for (Device d : devices.get().asArrayList()) {
            device = d;
            break;
        }

        boolean started = grabber.startSession(320, 240, 30, Pointer.pointerTo(device));
        if (!started) {
            throw new RuntimeException("Not able to start native grabber!");
        }

        do {
            ImgResponse imgResponseMsg = new ImgResponse(grabNextFrame(grabber));
            // JSON encoded image is stored in a Class Variable, so other threads can access it.
            img = imgResponseMsg.toJSONString();

            // This is used to test for a new line <enter> keypress on the Server, when in Local Mode. May not be needed?
            try {
                bytesAvail = System.in.available();
            } catch (IOException e) {
                System.out.println("SimpleStreamer: " + e.getMessage());
            }
        } while (bytesAvail == 0);
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

