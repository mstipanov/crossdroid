package hr.sting.crossdroid;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.CompoundBarcodeView;

import java.util.List;

import static hr.sting.crossdroid.MainActivity.PREFS_NAME;

/**
 * This sample performs continuous scanning, displaying the barcode and source image whenever
 * a barcode is scanned.
 */
public class ContinuousCaptureActivity extends Activity {
    private static final String TAG = ContinuousCaptureActivity.class.getSimpleName();

    private CompoundBarcodeView barcodeView;
    private SharedPreferences settings;
    private BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if (result.getText() != null) {
                barcodeView.setStatusText(result.getText());
            }
            //Added preview of scanned barcode
            ImageView imageView = (ImageView) findViewById(R.id.barcodePreview);
            imageView.setImageBitmap(result.getBitmapWithResultPoints(Color.YELLOW));

            Log.d(TAG, "Scanned");
            final int number = Integer.parseInt(result.getText());
            PostCheckpointTask task = new PostCheckpointTask(
                    "https://docs.google.com/forms/d/1YwpjBAbYKX6bfHcLwnpKeVMj1xnmj5ISiYwRWPIki6E/formResponse",
                    settings.getInt("checkpoint", 1), number) {
                @Override
                protected void onPostExecute(Boolean aBoolean) {
                    super.onPostExecute(aBoolean);
                    if (Boolean.TRUE.equals(aBoolean)) {
                        Toast.makeText(ContinuousCaptureActivity.this, "Checked: " + number, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(ContinuousCaptureActivity.this, "ERROR SENDING: " + number, Toast.LENGTH_LONG).show();
                    }
                    barcodeView.setStatusText("Scan next entry!");
                }
            };
            task.execute();
        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.continuous_scan);
        settings = getSharedPreferences(PREFS_NAME, 0);

        barcodeView = (CompoundBarcodeView) findViewById(R.id.barcode_scanner);
        barcodeView.decodeContinuous(callback);
    }

    @Override
    protected void onResume() {
        super.onResume();

        barcodeView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();

        barcodeView.pause();
    }

    public void pause(View view) {
        barcodeView.pause();
    }

    public void resume(View view) {
        barcodeView.resume();
    }

    public void triggerScan(View view) {
        barcodeView.decodeSingle(callback);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return barcodeView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
    }
}