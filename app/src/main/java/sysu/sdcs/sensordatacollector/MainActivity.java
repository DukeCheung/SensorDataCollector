package sysu.sdcs.sensordatacollector;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorManager;
//import android.support.annotation.NonNull;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
//import android.support.v7.app.AppCompatActivity;

import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback{

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
        init();
        btn_control.setOnClickListener(btn_listener);
        camera_control.setOnClickListener(camera_start_listener);
    }

    public void init(){
        Log.d("init", "mediaRecorder");
        btn_control = findViewById(R.id.btn_control);
        camera_control = findViewById(R.id.camera_start);
        edt_path = findViewById(R.id.edt_pathID);
        tv_record = findViewById(R.id.record);
        sv_view = (SurfaceView) findViewById(R.id.sv_view);
        mSurfaceHolder = sv_view.getHolder();
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mSurfaceHolder.addCallback(this);
        
        sensorListener = new SensorListener();
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        orientationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        stepDetectSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);

//        permissionCheck();
    }

    public void permissionCheck(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED){
            //申请WRITE_EXTERNAL_STORAGE权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQ_CODE_PERMISSION_EXTERNAL_STORAGE);
        }
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS)
                != PackageManager.PERMISSION_GRANTED){
            //申请BODY_SENSOR权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BODY_SENSORS},
                    REQ_CODE_PERMISSION_SENSOR);
        }
    }

    private View.OnClickListener camera_start_listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(!isRecording)
                start();
            else{
                stop();
            }
        }
    };

    protected void start() {
        handler.postDelayed(runnable,1000);
        try {
            File file = new File("/storage/emulated/0/SensorData/" + edt_path.getText().toString() +".mp4");
            if (file.exists()) {
                // 如果文件存在，删除它，演示代码保证设备上只有一个录音文件
                file.delete();
            }
            Log.d("media", "mediaRecorder");
            if(mediaRecorder==null)
                mediaRecorder = new MediaRecorder();
            camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
            if (camera != null) {
                camera.lock();
                camera.cancelAutoFocus();
                Camera.Parameters parameters = camera.getParameters();
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
                parameters.set("cam_mode", 1 ); //not sure why this arcane setting is required. found this in another post on Stackoverlflow
                camera.setParameters(parameters);
                camera.stopPreview();
                camera.setDisplayOrientation(90);
                camera.unlock();
            }
            mediaRecorder.setCamera(camera);
            // 设置音频录入源
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            // 设置视频图像的录入源

            mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            // 设置录入媒体的输出格式
            //mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            // 设置音频的编码格式
           // mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
            // 设置视频的编码格式
            //mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);
            // 设置视频的采样率，每秒4帧
            //mediaRecorder.setVideoFrameRate(4);
            mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_1080P));
            // 设置录制视频文件的输出路径
            mediaRecorder.setOutputFile(file.getAbsolutePath());
            // 设置捕获视频图像的预览界面

            mediaRecorder.setPreviewDisplay(sv_view.getHolder().getSurface());

            mediaRecorder.setOnErrorListener(new MediaRecorder.OnErrorListener() {

                @Override
                public void onError(MediaRecorder mr, int what, int extra) {
                    // 发生错误，停止录制
                    Log.d("error", "onError: ");
                    if(mediaRecorder==null){
                        mediaRecorder.release();
                        mediaRecorder = null;
                    }

                    isRecording=false;

                    if(camera!=null){

                        camera.lock();
                        camera.release();
                        camera.unlock();
                        camera = null;
                    }
                    Toast.makeText(MainActivity.this, "录制出错", Toast.LENGTH_SHORT).show();
                }
            });

            // 准备、开始
            mediaRecorder.prepare();
            mediaRecorder.start();
            isRecording = true;
            Log.d("camera", "start! ");
            btn_control.callOnClick();
            camera_control.setText("Stop");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    protected void stop() {
        if (isRecording) {
            // 如果正在录制，停止并释放资源

            handler.removeCallbacks(runnable);
            if (mediaRecorder != null) {
                mediaRecorder.release();
                mediaRecorder = null;
            }
            if (camera != null) {
                camera.stopPreview();
                camera.release();
                camera = null;
            }

            isRecording=false;
            btn_control.callOnClick();
            camera_control.setText("Record");
            Log.d("camera", "end! ");
        }
    }

    private View.OnClickListener btn_listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
                if (edt_path.getText().toString().equals("") ||
                        edt_path.getText().toString() == null) {
                    Toast.makeText(MainActivity.this, "path ID 不能为空", Toast.LENGTH_SHORT).show();
                } else if (btn_control.getText().toString().equals("Sensor")) {
                    if (!sensorManager.registerListener(sensorListener, accelerometerSensor, SensorManager.SENSOR_DELAY_FASTEST))
                        Toast.makeText(MainActivity.this, "加速度传感器不可用", Toast.LENGTH_SHORT).show();

                    if (!sensorManager.registerListener(sensorListener, magneticSensor, SensorManager.SENSOR_DELAY_FASTEST))
                        Toast.makeText(MainActivity.this, "磁场传感器不可用", Toast.LENGTH_SHORT).show();

                    if (!sensorManager.registerListener(sensorListener, orientationSensor, SensorManager.SENSOR_DELAY_FASTEST))
                        Toast.makeText(MainActivity.this, "方向传感器不可用", Toast.LENGTH_SHORT).show();

                    if (!sensorManager.registerListener(sensorListener, stepCounterSensor, SensorManager.SENSOR_DELAY_FASTEST))
                        Toast.makeText(MainActivity.this, "记步传感器不可用", Toast.LENGTH_SHORT).show();

                    if (!sensorManager.registerListener(sensorListener, stepDetectSensor, SensorManager.SENSOR_DELAY_FASTEST))
                        Toast.makeText(MainActivity.this, "记步传感器不可用", Toast.LENGTH_SHORT).show();

                    if (!sensorManager.registerListener(sensorListener, gyroscopeSensor, SensorManager.SENSOR_DELAY_FASTEST))
                        Toast.makeText(MainActivity.this, "陀螺仪不可用", Toast.LENGTH_SHORT).show();
                    // 重力加速度传感器
                    if (!sensorManager.registerListener(sensorListener, gravitySensor, SensorManager.SENSOR_DELAY_FASTEST))
                        Toast.makeText(MainActivity.this, "重力传感器不可用", Toast.LENGTH_SHORT).show();

                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
                    }

                    btn_control.setText("stop");
                    file_name = edt_path.getText().toString() + ".csv";
                    FileUtil.saveSensorData(file_name, SensorData.getFileHead());
                    ScheduledExecutorService service = Executors.newScheduledThreadPool(5);
                    future = service.scheduleAtFixedRate(new DataSaveTask(file_name), 5, 5, TimeUnit.SECONDS);
                } else {
                    future.cancel(true);
                    sensorManager.unregisterListener(sensorListener);
                    if (FileUtil.saveSensorData(file_name, SensorData.getAllDataStr())) {
                        cap_records += file_name + "\n";
                        tv_record.setText(cap_records);
                        Toast.makeText(MainActivity.this, "传感器数据保存成功", Toast.LENGTH_SHORT).show();
                    } else
                        Toast.makeText(MainActivity.this, "传感器数据保存失败", Toast.LENGTH_SHORT).show();
                    SensorData.clear();
                    btn_control.setText("Sensor");
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

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d("create", "surfaceCreated");
        mSurfaceHolder = holder;

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mSurfaceHolder = holder;
        Log.d("changed", "surfaceChanged");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d("destroyed", "surfaceDestroyed");

    }
}