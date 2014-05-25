package simplestream;
import org.json.JSONObject;
import org.json.JSONException;

public class GenericResponse extends Msg{

	private String response = null;
	
	@SuppressWarnings("unchecked")
	GenericResponse(String r){
		response = r;
		try{
			obj.put("response",response); 
		} catch (JSONException e){
			e.printStackTrace();
			System.exit(-1);
		}
	}
}