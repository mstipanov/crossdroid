package hr.sting.crossdroid;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.newrelic.agent.android.NewRelic;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {
    public static final String TAG = "crossdroid";
    public static final String PREFS_NAME = "crossdroid";
    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ", Locale.getDefault());
    private SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        NewRelic.withApplicationToken(
                "AAd9b13936cdf05fef456070d47434c59f45db9f9f"
        ).start(this.getApplication());

        setContentView(R.layout.activity_main);
        settings = getSharedPreferences(PREFS_NAME, 0);

        if (null != getIntent() && null != getIntent().getData() && getIntent().getData().toString().startsWith("https://docs.google.com/forms")) {
            String spreadsheet = getIntent().getData().toString();
            editSpreadsheet(spreadsheet);
        }
    }

    private void editSpreadsheet(String spreadsheet) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Spreadsheet");

        final EditText input = new EditText(this);
        input.setText(spreadsheet);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String spreadsheet = input.getText().toString();
                saveSpreadsheet(spreadsheet);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void saveSpreadsheet(String spreadsheet) {
        SharedPreferences.Editor editor = settings.edit();
        if (!validateSpreadsheet(spreadsheet)) {
            Toast.makeText(MainActivity.this, "INVALID SPREADSHEET!", Toast.LENGTH_LONG).show();
            return;
        }

        editor.putString("spreadsheet", spreadsheet);
        editor.commit();
    }

    public void setCheckpoint(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Checkpoint");

        final EditText input = new EditText(this);
        input.setText(String.valueOf(settings.getInt("checkpoint", 1)));
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences.Editor editor = settings.edit();
                editor.putInt("checkpoint", Integer.parseInt(input.getText().toString()));

                // Commit the edits!
                editor.commit();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    public void scanToolbar(View view) {
        if (null == getSpreadsheet()) {
            Toast.makeText(MainActivity.this, "NO SPREADSHEET!", Toast.LENGTH_LONG).show();
            return;
        }

        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
//        integrator.setBeepEnabled(true);
        integrator.setBarcodeImageEnabled(true);
        integrator.setCaptureActivity(ToolbarCaptureActivity.class).initiateScan();
    }

    public void gallery(View view) {
        Intent intent = new Intent(this, GalleryActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Log.d(TAG, "Cancelled scan");
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                Log.d(TAG, "Scanned");
                if(null!=result.getBarcodeImagePath()){
                    File file = new File(result.getBarcodeImagePath());
                    if(file.exists()){
                        String jpegName = file.getName();
                        String metaName = jpegName.replace(".jpg", ".meta");
                        File metaFile = new File(file.getParentFile(), metaName);
                        try {
                            FileUtils.writeStringToFile(metaFile, result.getContents() + " - " + SIMPLE_DATE_FORMAT.format(new Date()));
                        } catch (IOException e) {
                            Log.e(MainActivity.TAG, "Error saving file: "+ file, e);
                        }
                    }
                }


                String spreadsheet = getSpreadsheet();
                if (null == spreadsheet) {
                    return;
                }

                final int number = Integer.parseInt(result.getContents());
                if (spreadsheet.endsWith("/viewform")) {
                    spreadsheet = spreadsheet.replace("/viewform", "/formResponse");
                }

                PostCheckpointTask task = new PostCheckpointTask(
                        spreadsheet,
                        settings.getInt("checkpoint", 1), number) {
                    @Override
                    protected void onPostExecute(Boolean aBoolean) {
                        super.onPostExecute(aBoolean);
                        if (Boolean.TRUE.equals(aBoolean)) {
                            Toast.makeText(MainActivity.this, "Checked: " + number, Toast.LENGTH_LONG).show();
                            scanToolbar(null);
                        } else {
                            Toast.makeText(MainActivity.this, "ERROR SENDING: " + number, Toast.LENGTH_LONG).show();
                        }
                    }
                };
                task.execute();
            }
        } else {
            // This is important, otherwise the result will not be passed to the fragment
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private String getSpreadsheet() {
        String spreadsheet = settings.getString("spreadsheet", null);
        if (null == spreadsheet) {
            Toast.makeText(MainActivity.this, "NO SPREADSHEET DEFINED!", Toast.LENGTH_LONG).show();
        }
        return spreadsheet;
    }

    public void setSpreadsheet(View view) {
        editSpreadsheet(settings.getString("spreadsheet", ""));
    }

    private boolean validateSpreadsheet(String spreadsheet) {
        //TODO do it smarter, check if it actually exists, ...
        return null != spreadsheet && spreadsheet.startsWith("https://docs.google.com/forms");
    }
}