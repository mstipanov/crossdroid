package hr.sting.crossdroid;

import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static hr.sting.crossdroid.MainActivity.TAG;

/**
 * @author mstipanov
 * @since 01.03.2016.
 */
public class GetFormTask extends AsyncTask<Void, Void, Pair<String, String>> {
    private static final Pattern CHECKPOINT_PATTERN = Pattern.compile("aria-label=\"Checkpoint\".+?input type=\"hidden\" name=\"(entry\\..+?)\"");
    private static final Pattern BROJ_PATTERN = Pattern.compile("aria-label=\"Broj\".+?input type=\"hidden\" name=\"(entry\\..+?)\"");
    private String url;

    public GetFormTask(String url) {
        this.url = url;
    }

    @Override
    protected Pair<String, String> doInBackground(Void... params) {
        long t = System.currentTimeMillis();
        try {
            DefaultHttpClient httpclient = new DefaultHttpClient();
            HttpGet httpPostRequest = new HttpGet(url);
            HttpResponse response = httpclient.execute(httpPostRequest);
            Log.i(TAG, "HTTPResponse received in [" + (System.currentTimeMillis() - t) + "ms]");

            HttpEntity entity = response.getEntity();

            if (entity != null) {
                // Read the content stream
                InputStream instream = entity.getContent();
                String body = IOUtils.toString(instream, "UTF-8");

                String checkpointKey = null;
                Matcher matcher = CHECKPOINT_PATTERN.matcher(body);
                if (matcher.find()) {
                    checkpointKey = matcher.group(1);
                }
                if (null == checkpointKey) {
                    throw new IllegalArgumentException("checkpointKey is mandatory");
                }

                String brojKey = null;
                matcher = BROJ_PATTERN.matcher(body);
                if (matcher.find()) {
                    brojKey = matcher.group(1);
                }
                if (null == brojKey) {
                    throw new IllegalArgumentException("brojKey is mandatory");
                }

                return new Pair<>(checkpointKey, brojKey);
            }
            return null;
        } catch (Exception e) {
            Log.e(TAG, "HTTP error received in [" + (System.currentTimeMillis() - t) + "ms]");
            return null;
        }
    }
}
