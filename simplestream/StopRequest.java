package simplestream;
import org.json.JSONObject;
import org.json.JSONException;

public class StopRequest extends Msg{

	StopRequest(){
		try{
			obj.put("request","stopstream");
		} catch(JSONException e) {
			e.printStackTrace();
			System.exit(-1);
		}	
	}
	
}