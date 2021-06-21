package jp.jaxa.iss.kibo.rpc.sampleapk;

import java.util.Arrays;
import android.graphics.Bitmap;
import android.util.Log;
import java.util.Vector;
import static android.graphics.Bitmap.createBitmap;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

import org.opencv.calib3d.Calib3d;
import org.opencv.aruco.Aruco;
import org.opencv.aruco.DetectorParameters;
import org.opencv.aruco.Dictionary;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import static org.opencv.android.Utils.bitmapToMat;
import static org.opencv.android.Utils.matToBitmap;
import static org.opencv.core.CvType.CV_32FC1;
import static org.opencv.core.CvType.CV_64F;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import jp.jaxa.iss.kibo.rpc.api.KiboRpcService;

import gov.nasa.arc.astrobee.Result;
import gov.nasa.arc.astrobee.types.Point;
import gov.nasa.arc.astrobee.types.Quaternion;

/**
 * Class meant to handle commands from the Ground Data System and execute them in Astrobee
 */

public class YourService extends KiboRpcService {
    @Override
    protected void runPlan1() {
        // astrobee is undocked and the mission starts
        api.startMission();
        // move to Point A
        moveToWrapper(11.21, -9.8, 4.79, 0, 0, -0.707, 0.707);
        api.flashlightControlFront(0.05f);
        try {
            java.lang.Thread.sleep(1000);
        } catch(InterruptedException ex){
            java.lang.Thread.currentThread().interrupt();
        }
        String qrcode = getQR();
        String[] splt = qrcode.split("[\"{}:,pxyz]+");
        int ap = Integer.parseInt(splt[1]);
        double ax = Double.parseDouble(splt[2]);
        double ay = Double.parseDouble(splt[3]);
        double az = Double.parseDouble(splt[4]);

        //Log.d("Pattern", int.tostring(ap));
        Log.d("x_Adash", "Value: " + Double.toString(ax));
        Log.d("y_Adash", "Value: " + Double.toString(ay));
        Log.d("z_Adash", "Value: " + Double.toString(az));

        movetoAdash(ap,ax,ay,az);

        arEvent(ay);




    }
    @Override
    protected void runPlan2(){
        // write here your plan 2
    }

    @Override
    protected void runPlan3(){
        // write here your plan 3
    }

    // You can add your method
    private void moveToWrapper(double pos_x, double pos_y, double pos_z,
                               double qua_x, double qua_y, double qua_z,
                               double qua_w) {
        final int LOOP_MAX = 3;
        final Point point = new Point(pos_x, pos_y, pos_z);
        final Quaternion quaternion = new Quaternion((float) qua_x, (float) qua_y,
                (float) qua_z, (float) qua_w);

        Result result = api.moveTo(point, quaternion, true);

        int loopCounter = 0;
        while (!result.hasSucceeded() || loopCounter < LOOP_MAX) {
            result = api.moveTo(point, quaternion, true);
            ++loopCounter;
        }
    }

    private String getQR(){

        Bitmap bitmap;
        bitmap = api.getBitmapNavCam();


        String QRCodeString = readQR(bitmap);
        api.sendDiscoveredQR(QRCodeString);

        return QRCodeString;
    }

    private String readQR(Bitmap bitmap){
        String result = "error";
//        double n = 0.5;
//        double m = 0.5;
//        int width = bitmap.getWidth();
//        int height = bitmap.getHeight();
//        int newWidth = (int) (width * n);
//        int newHeight = (int) (height * n);
//        int startX = width/4;
//        int startY =height/4;
//        Bitmap bitmapTriming = createBitmap(bitmap, startX, startY, newWidth, newHeight);
//        Bitmap bitmapResize = Bitmap.createScaledBitmap(bitmapTriming, (int) (newWidth * m), (int) (newHeight * m), true);
        Bitmap bitmapResize = Bitmap.createBitmap(bitmap,0,0,960,960);
        int width = bitmapResize.getWidth();
        int height = bitmapResize.getHeight();
        int[] pixels = new int[width * height];
        bitmapResize.getPixels(pixels, 0, width, 0, 0, width, height);
        try {
            LuminanceSource source = new RGBLuminanceSource(width, height, pixels);
            BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));
            Reader reader = new QRCodeReader();
            com.google.zxing.Result decodeResult = reader.decode(binaryBitmap);
            result = decodeResult.getText();
            Log.d("QR content",result);
        } catch (Exception e) {
        }
        return result;
    }

    private void arEvent(double ay){
        double[] forward = readAR(ay);
        double[] globalup = {0,1,0};

        double[] right = crossProduct(forward,globalup);
        double[] up = crossProduct(right,forward);

        Quaternion finalquaternion = finalquaternionRotation(forward,up);
        Point goal = new Point(0,0,0);
        api.relativeMoveTo(goal,finalquaternion,true);
        api.laserControl(true);
        try {
            java.lang.Thread.sleep(400);
        } catch(InterruptedException ex){
            java.lang.Thread.currentThread().interrupt();
        }

        api.takeSnapshot();

    }

    private static double[] crossProduct(double[] a, double[] b) {
        double[] output = {0,0,0};
        output[0] = a[1] * b[2] - a[2] * b[1];
        output[1] = a[2] * b[0] - a[0] * b[2];
        output[2] = a[0] * b[1] - a[1] * b[0];
        return output;
    }

    private Quaternion finalquaternionRotation(double[] forward, double[] up){
        double[] vector = forward;
        double[] vector2 = crossProduct(up,forward);
        double[] vector3 = crossProduct(vector,vector2);

        float m00 = (float)vector2[0];
        float m01 = (float)vector2[1];
        float m02 = (float)vector2[2];
        float m10 = (float)vector3[0];
        float m11 = (float)vector3[1];
        float m12 = (float)vector3[2];
        float m20 = (float)vector[0];
        float m21 = (float)vector[1];
        float m22 = (float)vector[2];
        float num8 = (m00 + m11) + m22;

        if (num8 > 0f){
            float num = (float)Math.sqrt(num8 + 1f);
            float nw = num * 0.5f;
            num = 0.5f / num;
            float nx = (m12 - m21) * num;
            float ny = (m20 - m02) * num;
            float nz = (m01 - m10) * num;
            Quaternion quaternion = new Quaternion(nx,ny,nz,nw);
            return quaternion;
        }
        if ((m00 >= m11) && (m00 >= m22)){
            float num7 = (float)Math.sqrt(((1f + m00) - m11) - m22);
            float num4 = 0.5f / num7;
            float nx = 0.5f * num7;
            float ny = (m01 + m10) * num4;
            float nz = (m02 + m20) * num4;
            float nw = (m12 - m21) * num4;
            Quaternion quaternion = new Quaternion(nx,ny,nz,nw);
            return quaternion;
        }
        if (m11 > m22){
            float num6 = (float)Math.sqrt(((1f + m11) - m00) - m22);
            float num3 = 0.5f / num6;
            float nx = (m10+ m01) * num3;
            float ny = 0.5f * num6;
            float nz = (m21 + m12) * num3;
            float nw = (m20 - m02) * num3;
            Quaternion quaternion = new Quaternion(nx,ny,nz,nw);
            return quaternion;
        }
        float num5 = (float)Math.sqrt(((1f + m22) - m00) - m11);
        float num2 = 0.5f / num5;
        float nx = (m20 + m02) * num2;
        float ny = (m21 + m12) * num2;
        float nz = 0.5f * num5;
        float nw = (m01 - m10) * num2;
        Quaternion quaternion = new Quaternion(nx,ny,nz,nw);
        return quaternion;

    }

    private double[] readAR(double ay){
        Dictionary dictionary = Aruco.getPredefinedDictionary(Aruco.DICT_5X5_250);

        Bitmap bitmap = api.getBitmapNavCam();
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        bitmap = Bitmap.createScaledBitmap(bitmap,(int)(width),(int)(height),true);
        Mat inputImage = new Mat();
        bitmapToMat(bitmap,inputImage,false);
        List<Mat> corners = new ArrayList<>();
        Mat markerIds = new Mat();
        DetectorParameters parameters = DetectorParameters.create();
        Imgproc.cvtColor(inputImage, inputImage, Imgproc.COLOR_BGR2GRAY);
        Aruco.detectMarkers(inputImage, dictionary, corners, markerIds, parameters);
        if(!corners.isEmpty()) {
            Log.d("AR[status]:", " Detected");
            Log.d("AR[status]", corners.toString());
            Log.d("AR[status]", corners.size() + " ");
            Log.d("AR[status]", markerIds.dump());
        }else{
            Log.d("AR[status]:", "Detected");
        }

        double ar_width = corners.get(0).get(0,2)[0]-corners.get(0).get(0,0)[0];
        double ar_height = corners.get(0).get(0,2)[1] - corners.get(0).get(0,0)[1];
        double AR_center_x = corners.get(0).get(0,0)[0] + (ar_width / 2);
        double AR_center_z = corners.get(0).get(0,0)[1] + (ar_height / 2);
        Log.d("AR width",Double.toString(ar_width));
        Log.d("AR height",Double.toString(ar_height));
        Log.d("AR center x ",Double.toString(AR_center_x));
        Log.d("AR center z",Double.toString(AR_center_z));
        double[] forwardvector = forwardVector(AR_center_x,ay,AR_center_z);
        return forwardvector;
    }

    private void movetoAdash(int ap,double ax, double ay, double az){
        if (ap==1 || ap==2 || ap==8)
        {
            moveToWrapper(ax,ay,az, 0.0, 0.0, -0.707, 0.707);

        }

        if (ap==3 || ap==4)
        {
            moveToWrapper(ax,-9.8,4.79, 0.0, 0.0, -0.707, 0.707);
            moveToWrapper(ax,-9.8,az, 0.0, 0.0, -0.707, 0.707);
        }

        if (ap==5 || ap==6)
        {
            double x_KOZL = ax - 0.22;
            moveToWrapper(x_KOZL,-9.8,4.79, 0.0, 0.0, -0.707, 0.707);
            moveToWrapper(x_KOZL,-9.8,az, 0.0, 0.0, -0.707, 0.707);
            moveToWrapper(ax,-9.8,az, 0.0, 0.0, -0.707, 0.707);



        }
        if (ap==7)
        {
            double x_KOZR = ax;
            moveToWrapper(x_KOZR,-9.8,4.79, 0.0, 0.0, -0.707, 0.707);
            moveToWrapper(x_KOZR,-9.8,az, 0.0, 0.0, -0.707, 0.707);
            moveToWrapper(ax,-9.8,az, 0.0, 0.0, -0.707, 0.707);
        }
    }
    public Bitmap undistortImg(Bitmap input_bitmap)
    {
        Mat input_mat = new Mat();
        bitmapToMat(input_bitmap,input_mat);
        double camera_matrix[] = {692.827528, 0.000000, 571.399891, 0.000000, 691.919547, 504.956891, 0.000000, 0.000000, 1.000000};
        double distortion_coefficients[] = {-0.312191, 0.073843, -0.000918, 0.001890, 0.000000};
        Mat camera_matrix_mat = new Mat(3,3,CV_64F);
        Mat distortion_coefficients_mat = new Mat(1,5,CV_64F);
        camera_matrix_mat.put(0,0,camera_matrix[0]);
        camera_matrix_mat.put(0,1,camera_matrix[1]);
        camera_matrix_mat.put(0,2,camera_matrix[2]);
        camera_matrix_mat.put(1,0,camera_matrix[3]);
        camera_matrix_mat.put(1,1,camera_matrix[4]);
        camera_matrix_mat.put(1,2,camera_matrix[5]);
        camera_matrix_mat.put(2,0,camera_matrix[6]);
        camera_matrix_mat.put(2,1,camera_matrix[7]);
        camera_matrix_mat.put(2,2,camera_matrix[8]);
        distortion_coefficients_mat.put(0,0,distortion_coefficients[0]);
        distortion_coefficients_mat.put(0,1,distortion_coefficients[1]);
        distortion_coefficients_mat.put(0,2,distortion_coefficients[2]);
        distortion_coefficients_mat.put(0,3,distortion_coefficients[3]);
        distortion_coefficients_mat.put(0,4,distortion_coefficients[4]);
        Mat undistorted_mat = new Mat();
        Size original_size = new Size(input_mat.width(),input_mat.height());
        Mat new_cammat = Calib3d.getOptimalNewCameraMatrix(camera_matrix_mat,distortion_coefficients_mat,original_size,1);
        Mat eye = Mat.eye(3,3,CV_32FC1);
        Mat output_map1 = new Mat();
        Mat output_map2 = new Mat();
        Imgproc.initUndistortRectifyMap(camera_matrix_mat,distortion_coefficients_mat,eye,new_cammat,original_size,CV_32FC1,output_map1,output_map2);
        Imgproc.remap(input_mat,undistorted_mat,output_map1,output_map2,Imgproc.INTER_AREA);
        Bitmap output_bitmap = createBitmap(undistorted_mat.width(),undistorted_mat.height(), Bitmap.Config.ARGB_8888);
        matToBitmap(undistorted_mat,output_bitmap);
        return output_bitmap;
    }
    private Mat getCamIntrinsics(){
        //        cam_matrix arr to mat
        Mat cam_Matrix = new Mat();

        double [][] Nav_Intrinsics = api.getNavCamIntrinsics();
        for (int i = 0; i <= 8; ++i)
        {
            int row , col ;

            if(i < 3){
                row = 0;col = i;
            } else if(i<6){
                row = 1;col = i-3;
            } else{
                row = 2;col = i-6;
            }

            cam_Matrix.put(row, col, Nav_Intrinsics[0][i]);
        }
        if(!cam_Matrix.empty()) {
            Log.d("Get Cam_Matrix[status]:", "Acquired");
        }else{
            Log.d("Get Cam_Matrix[status]:", "Not Acquired");
        }
        return cam_Matrix;
    }
    private Mat getDist_coeff(){
        //         dat coefficient arr to mat
        double [][] Nav_Intrinsics = api.getNavCamIntrinsics();
        Mat dist_Coeff = new Mat();
        for(int i = 0; i<=4 ;i++)
        {
            dist_Coeff.put(0,i,Nav_Intrinsics[1][i]);
        }
        Log.d("Get Dist_coeff[status]:","Acquired");
        return dist_Coeff;

    }


    public static double[] forwardVector(double ar_centre_x,double ar_centre_y, double ar_centre_z) {
        double[] la = { 0.1302 ,0.0572 ,-0.1111 };
        double[] ac = { -0.1177 , 0.0422,0.0826 };
        double[] ct = { ar_centre_x ,ar_centre_y ,ar_centre_z };

        double[] result = add(la,ac);
        double[] finalresult = add(result,ct);
        System.out.println("vector sum: " + Arrays.toString(result));
        return finalresult;
        }
    public static double[] add(double[] first, double[] second)
    {
        int length = first.length <second.length ? first.length : second.length;
        double[] result = new double[length];
        for (int i = 0; i < length; i++)
        {
            result[i] = first[i] + second[i];
        }
        return result;
    }



}

