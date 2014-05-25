package simplestream;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONException;
import org.json.simple.JSONObject;



import javax.swing.*;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by kingchan on 22/05/2014.
 */
public class LocalViewer extends Thread {

    Viewer myViewer = null;

    LocalViewer(Viewer viewer) {
        myViewer = viewer;
        this.start();
    }

    public void run() {
        JFrame frame = null;
        String imgData = null;
        String img = null;
        //ArrayBlockingQueue<String> queue = null;
        //ArrayBlockingQueue<String> imgQueue = new ArrayBlockingQueue<String>(QUEUE_MAX_LENGTH);

        try {
            frame = new JFrame("Simple Stream Viewer");
            frame.setVisible(true);
            frame.setSize(320, 240);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(myViewer);
        } catch (Exception e) {
            System.out.println("Viewer: " + e.getMessage());
        }

        while(true) {
            if((img = SetImg.img) != null) {
                /*
                try {
                    queue.put(img);
                } catch (InterruptedException e) {
                    System.out.println("FramedViewer: " + e.getMessage());
                }
                */

                try {
                    org.json.JSONObject obj = new org.json.JSONObject(img);
                    imgData = obj.get("data").toString();
                } catch(JSONException e){
                    e.printStackTrace();
                    System.exit(-1);
                }

                byte[] nobase64_image = Base64.decodeBase64(Base64.decodeBase64(imgData));
                /* Decompress the image */
                byte[] decompressed_image = Compressor.decompress(nobase64_image);
                /* Give the raw image bytes to the viewer. */
                myViewer.ViewerInput(decompressed_image);
                frame.repaint();
            }
            else {
                System.out.println("Waiting for frames...");
            }
        }
    }

    private static boolean isNullOrBlank(String s)
    {
        return (s==null || s.trim().equals(""));
    }

}
