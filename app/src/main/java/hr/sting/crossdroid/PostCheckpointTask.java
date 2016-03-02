package hr.sting.crossdroid;

import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.InputStream;

import static hr.sting.crossdroid.MainActivity.TAG;

/**
 * @author mstipanov
 * @since 01.03.2016.
 */
public class PostCheckpointTask extends AsyncTask<Void, Void, Boolean> {
    private String url;
    private Pair<String, String> checkpoint;
    private Pair<String, String> id;

    public PostCheckpointTask(String url, Pair<String, String> checkpoint, Pair<String, String> id) {
        this.url = url;
        this.checkpoint = checkpoint;
        this.id = id;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        long t = System.currentTimeMillis();
        try {
            DefaultHttpClient httpclient = new DefaultHttpClient();
            HttpPost httpPostRequest = new HttpPost(url);

            StringEntity se;
            se = new StringEntity(checkpoint.first + "=" + checkpoint.second + "&" + id.first + "=" + id.second);

            // Set HTTP parameters
            httpPostRequest.setEntity(se);
            httpPostRequest.setHeader("Content-type", "application/x-www-form-urlencoded");

            HttpResponse response = httpclient.execute(httpPostRequest);
            Log.i(TAG, "HTTPResponse received in [" + (System.currentTimeMillis() - t) + "ms]");

            HttpEntity entity = response.getEntity();

            if (entity != null) {
                // Read the content stream
                InputStream instream = entity.getContent();
                String body = IOUtils.toString(instream, "UTF-8");
                if (null == body || !body.contains("Vaš je odgovor zabilježen.")) {
                    return Boolean.FALSE;
                }
                return Boolean.TRUE;
            }
            return Boolean.FALSE;
        } catch (Exception e) {
            Log.e(TAG, "HTTP error received in [" + (System.currentTimeMillis() - t) + "ms]");
            return Boolean.FALSE;
        }
    }
}
