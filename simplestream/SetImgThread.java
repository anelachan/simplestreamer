package simplestream;
import java.net.*;
import java.io.*;

import com.github.sarxos.webcam.ds.buildin.natives.Device;
import com.github.sarxos.webcam.ds.buildin.natives.DeviceList;
import com.github.sarxos.webcam.ds.buildin.natives.OpenIMAJGrabber;
import org.apache.commons.codec.binary.Base64;
import org.bridj.Pointer;
import org.json.JSONObject;
import org.json.JSONException;
import sun.java2d.pipe.SpanShapeRenderer;

public class SetImgThread extends Thread {

    SetImgThread() {
        this.start();
    }

    public void run() {
        SetImg setimg = new SetImg();
        if (SimpleStreamer.mode == SimpleStreamer.LOCAL_MODE)
            setimg.generateImgResponse();
    }
}