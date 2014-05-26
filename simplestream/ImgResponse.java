package simplestream;
import org.json.JSONObject;
import org.json.JSONException;

public class ImgResponse extends Msg{
	// this will need to hook up to something to actually
	// receive image data, Base64 --> String

	private String imgData;
	
	ImgResponse(String i){ 
		imgData = i;
		try{
			obj.put("response", "image");
			obj.put("data", imgData);
		} catch(JSONException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

}