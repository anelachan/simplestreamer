package simplestream;
import org.json.JSONObject;
import org.json.JSONException;

public class StartRequest extends Msg{

	private int sleepTime = 100;
	private int sport = 6262;

	StartRequest(){
		createJSON();
	}

	
	StartRequest(int st){
		if (st > 100)
			sleepTime = st;
		createJSON();	
	}

	StartRequest(int st, int sp){
		if (st > 100)
			sleepTime = st;
		sport = sp;
		createJSON();
	}

	@SuppressWarnings("unchecked")
	private void createJSON(){
		try{
			obj.put("ratelimit",sleepTime);
			obj.put("sport",sport);
			obj.put("request","startstream");
		} catch(JSONException e){
			e.printStackTrace();
			System.exit(-1);
		}
		
	}
	
}