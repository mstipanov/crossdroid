package hr.sting.crossdroid;

import android.os.AsyncTask;
import android.util.Log;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import static hr.sting.crossdroid.MainActivity.TAG;

/**
 * @author mstipanov
 * @since 01.03.2016.
 */
public class PostCheckpointTask extends AsyncTask<Void, Void, Boolean> {
    private String url;
    private int checkpoint;
    private int id;

    public PostCheckpointTask(String url, int checkpoint, int id) {
        this.url = url;
        this.checkpoint = checkpoint;
        this.id = id;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            DefaultHttpClient httpclient = new DefaultHttpClient();
            HttpPost httpPostRequest = new HttpPost(url);

            StringEntity se;
            se = new StringEntity("entry.1248733596=" + checkpoint + "&entry.56246414=" + id);

            // Set HTTP parameters
            httpPostRequest.setEntity(se);
            httpPostRequest.setHeader("Content-type", "application/x-www-form-urlencoded");

            long t = System.currentTimeMillis();
            HttpResponse response = (HttpResponse) httpclient.execute(httpPostRequest);
            Log.i(TAG, "HTTPResponse received in [" + (System.currentTimeMillis() - t) + "ms]");

            HttpEntity entity = response.getEntity();

            if (entity != null) {
                // Read the content stream
//                InputStream instream = entity.getContent();

                // convert content stream to a String
//                String resultString= convertStreamToString(instream);
//                instream.close();
//                resultString = resultString.substring(1,resultString.length()-1); // remove wrapping "[" and "]"

                // Raw DEBUG output of our received JSON object:
//                Log.i(TAG,"<JSONObject>\n"+jsonObjRecv.toString()+"\n</JSONObject>");
                //TODO verify content contains "Vaš je odgovor zabilježen."
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return Boolean.TRUE;
    }
}
