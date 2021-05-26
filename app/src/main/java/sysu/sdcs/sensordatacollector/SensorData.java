package sysu.sdcs.sensordatacollector;

import android.hardware.SensorManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by justk on 2018/6/13.
 */

public class SensorData {
    public static List<String> timestamps = new ArrayList<String>();
    public static List<String[]> magneticSensorData = new ArrayList<String[]>();
    public static List<String[]> accelerometerSensorData = new ArrayList<String[]>();
    public static List<String[]> orientationSensorData = new ArrayList<String[]>();
    public static List<String[]> stepSensorData = new ArrayList<String[]>();
    public static List<String[]> gyroscopeSensorData = new ArrayList<String[]>();



    public static void clear(){
        timestamps.clear();
        magneticSensorData.clear();
        accelerometerSensorData.clear();
        orientationSensorData.clear();
        stepSensorData.clear();
        gyroscopeSensorData.clear();

    }


    public static void addSensorData(String[] mData, String[] aData, String[] oData,
                                     String gData[], String[] sData, String captime){

        final float[] accelerometerReading = new float[3];
        final float[] magnetometerReading = new float[3];
        for(int i = 0;i < 3; ++i){
            accelerometerReading[i] = Float.parseFloat(aData[i]);
            magnetometerReading[i] = Float.parseFloat(mData[i]);
        }

        final float[] rotationMatrix = new float[9];
        SensorManager.getRotationMatrix(rotationMatrix, null,
                accelerometerReading, magnetometerReading);
        // "mRotationMatrix" now has up-to-date information.
        float[] mOrientationAngles = new float[3];
        SensorManager.getOrientation(rotationMatrix, mOrientationAngles);

        oData[0] = String.valueOf(Math.toDegrees(mOrientationAngles[0]));
        oData[1] = String.valueOf(Math.toDegrees(mOrientationAngles[1]));
        oData[2] = String.valueOf(Math.toDegrees(mOrientationAngles[2]));

        magneticSensorData.add(mData);
        accelerometerSensorData.add(aData);
        stepSensorData.add(sData);
        gyroscopeSensorData.add(gData);
        timestamps.add(captime);
        orientationSensorData.add(oData);
    }

    public static String getFileHead(){
        return  "frame,mag_x,mag_y,mag_z,acc_x,acc_y,acc_z,gyro_x,gyro_y,gyro_z," +
                "orien_x,orien_y,orien_z,step_detect,step_count,timestamp\n";
    }

    public static String getAllDataStr(){
        String data = "";
        for(int i = 0 ; i < magneticSensorData.size() ; i++){
            String[] mag = magneticSensorData.get(i);
            String[] gyro = gyroscopeSensorData.get(i);
            String[] orien = orientationSensorData.get(i);
            String[] step = stepSensorData.get(i);
            String[] acc = accelerometerSensorData.get(i);
            String one_detail = "" + (i+1) + ","
                    + mag[0] + "," + mag[1] + "," + mag[2] + ","
                    + acc[0] + "," + acc[1] + "," + acc[2] + ","
                    + gyro[0] + "," + gyro[1] + "," + gyro[2] + ","
                    + orien[0] + "," + orien[1] + "," + orien[2] + ","
                    + null2zero(step[0]) + "," + step[1] + "," + timestamps.get(i) + "\n" ;
            data = data + one_detail;
        }
//        clear();
        return data;
    }

    public static String getLastDataStr() {
        String data = "";
        int i = magneticSensorData.size()-1;
        String[] mag = magneticSensorData.get(i);
        String[] gyro = gyroscopeSensorData.get(i);
        String[] orien = orientationSensorData.get(i);
        String[] step = stepSensorData.get(i);
        String[] acc = accelerometerSensorData.get(i);
        String one_detail = "" + (i+1) + ","
                + mag[0] + "," + mag[1] + "," + mag[2] + ","
                + acc[0] + "," + acc[1] + "," + acc[2] + ","
                + gyro[0] + "," + gyro[1] + "," + gyro[2] + ","
                + orien[0] + "," + orien[1] + "," + orien[2] + ","
                + null2zero(step[0]) + "," + step[1] + "," + timestamps.get(i) + "\n" ;
        data = data + one_detail;
//        clear();
        return data;
    }

    public static String null2zero(String item){
        if(item == null || item.equals(""))
            return "0";
        return item;
    }

}
