package simplestream;
import org.json.JSONObject;

public class Msg{
	JSONObject obj = null;

	Msg(){
		obj = new JSONObject();
	}

	public String toJSONString(){
		return obj.toString();
	}
}