package sysu.sdcs.sensordatacollector;

import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ScheduledFuture;

//import android.support.annotation.NonNull;
//import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity{
    private static Socket socket;
    private static final int REQ_CODE_PERMISSION_EXTERNAL_STORAGE = 0x1111;
    private static final int REQ_CODE_PERMISSION_SENSOR = 0x2222;

    private SensorManager sensorManager;
    private SensorListener sensorListener;
    private Sensor accelerometerSensor;
    private Sensor gyroscopeSensor;
    private Sensor gravitySensor;
    private Sensor magneticSensor;
    private Sensor orientationSensor;
    private Sensor stepCounterSensor;
    private Sensor stepDetectSensor;

    private Button btn_control;
    private Button camera_control;
    private EditText edt_path;
    private TextView tv_state;
    private TextView tv_record;

    private ScheduledFuture future;
    private String file_name = "";
    private String cap_records = "";

    private ImageView mPicture;
    private boolean isRecording;
    private MediaRecorder mediaRecorder;
    private SurfaceView sv_view;
    private Camera camera;
    private SurfaceHolder mSurfaceHolder;
    private CameraPreview mPreview;
    private Button captureButton;

    private android.os.Handler handler = new android.os.Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            handler.postDelayed(this,1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("onCreate", "mediaRecorder");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//保持屏幕唤醒
        // btn_control.setOnClickListener(btn_listener);
        try {
            camera = Camera.open();
        } catch (Exception e) {

        }
        camera.getParameters().setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        mPreview = new CameraPreview(this, camera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);
        captureButton = (Button) findViewById(R.id.button_capture);
        captureButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // get an image from the camera
                        camera.takePicture(null, null, Picture);
                    }
                }
        );
    }
    private static File getOutputMediaFile(){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File("/storage/emulated/0/SensorDataCollector/Pictures/");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                "IMG_"+ timeStamp + ".jpg");

        return mediaFile;
    }
    private Camera.PictureCallback Picture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            File pictureFile = getOutputMediaFile();
            if (pictureFile == null){
                Log.d("TAG", "Error creating media file, check storage permissions");
                return;
            }

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
            } catch (FileNotFoundException e) {
                Log.d("TAG", "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d("TAG", "Error accessing file: " + e.getMessage());
            }
        }
    };

    //权限申请
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQ_CODE_PERMISSION_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // User agree the permission
                } else {
                    // User disagree the permission
                    Toast.makeText(MainActivity.this, "请打开存储权限", Toast.LENGTH_LONG).show();
                }
            }
            case REQ_CODE_PERMISSION_SENSOR: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // User agree the permission
                }
                else {
                    // User disagree the permission
                    Toast.makeText(this, "请打开传感器权限", Toast.LENGTH_LONG).show();
                }
            }
            break;
        }
    }
}