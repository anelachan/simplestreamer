/* ImageResponse.java
Author: Anela Chan
Date: 16 May 2014
Description: Generates a JSON object for a server response with 
both fields "response": "image" and "data": [compressed image data]
*/

package simplestream;
import org.json.JSONObject;
import org.json.JSONException;

public class ImgResponse extends Msg{
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