import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONObject;


public class ApiManager {

    public static final String ID = "314943051";

    public static String BASE_URL = "https://app.seker.live/fm1/send-message";

    public static PollManager pollManager = new PollManager();

    public static String ApiRequestGetMessage(String message, int minuets){
        try {
            OkHttpClient client = new OkHttpClient();

            HttpUrl.Builder builder = HttpUrl.parse(BASE_URL).newBuilder();

            builder.addQueryParameter("id", ID);

            builder.addQueryParameter("text", Constant.API_SCRIPT + message);

            HttpUrl url = builder.build();

            Request request = new Request.Builder()
                    .url(url)
                    .build();

            Response response = client.newCall(request).execute();

            if (!response.isSuccessful()) {
                System.err.println("שגיאה בבקשה: קוד " + response.code());
                return null;
            }

            JSONObject jsonObject = new JSONObject(response.body().string());
            System.out.println(jsonObject);

            pollManager.addPollWithQuestions(jsonObject.getString("extra"), minuets);

            return jsonObject.getString("extra");


        } catch (Exception e) {
            System.err.println("שגיאה כללית: " + e.getMessage());
            return null;
        }
    }


}
