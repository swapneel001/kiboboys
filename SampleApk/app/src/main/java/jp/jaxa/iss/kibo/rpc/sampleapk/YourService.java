package jp.jaxa.iss.kibo.rpc.sampleapk;

import android.graphics.Bitmap;
import android.util.Log;
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

        String qrcode = getQR();
        String[] splt = qrcode.split("[\"{}:,pxyz]+");
        int ap = Integer.parseInt(splt[1]);
        double ax = Double.parseDouble(splt[2]);
        double ay = Double.parseDouble(splt[3]);
        double az = Double.parseDouble(splt[4]);

        double x = 11.21;
        double y = -9.8;
        double z = 4.79;

        if (ap==1 || ap==2 || ap==8)
        {
            moveToWrapper(ax,ay,az, 0.0, 0.0, -0.707, 0.707);

        }


        if (ap==3){
            double k=(ay+9.8)/(ax-11.21);
            int collision=0;
            for (z=az-0.22;z<=az+0.005;z+=0.01){
                if ((ax+0.155<=x)  && (x<=ax+0.38) && (x== 11.21+k*(y+9.8))){
                    collision+=1;}}
            if (collision!=0){
                moveToWrapper(ax+0.155,y,z,0,0,-0.707,0.707);
                moveToWrapper (ax,ay,az,0,0,-0.707,0.707);}
            else
                moveToWrapper (ax,ay,az,0,0, -0.707,0.707);
        }

        if (ap==4){
            double k=(ay+9.8)/(ax-11.21);
            int collision=0;
            for (z=az-0.22;z<=az+0.005;z+=0.01){
                if ((ax+0.08<=x)  && (x<=ax+0.38) && (x== 11.21+k*(y+9.8))){
                    collision+=1;}}
            if (collision!=0){
                moveToWrapper(ax+0.155,y,z,0,0,-0.707,0.707);
                moveToWrapper (ax,ay,az,0,0,-0.707,0.707);}
            else
                moveToWrapper (ax,ay,az,0,0, -0.707,0.707);
        }

        if (ap==5){
            x=(4.1025+0.22*az-az*ax+1.0682*ax*ax-7.42*ax)/(1.0682*ax-az-6.96);
            z=az+1.0682*(x-ax);
            if ((x>=10.71) && (x<=11.55) && (z>=4.32) && (z<=5.57)){
                moveToWrapper (x,ay,z,0,0, -0.707,0.707);
                moveToWrapper (ax,ay,az,0,0, -0.707,0.707);
            }
            else {
                double x_KOZL = ax - 0.22;
                moveToWrapper(x_KOZL,-9.8,4.79, 0.0, 0.0, -0.707, 0.707);
                moveToWrapper(x_KOZL,-9.8,az, 0.0, 0.0, -0.707, 0.707);
                moveToWrapper(ax,-9.8,az, 0.0, 0.0, -0.707, 0.707);}
        }

        if (ap==6){
            x=(6.2125+12.78*ax-0.7272*ax*ax-az*ax-0.22*az)/(13.24-az-0.7272*ax);
            z=az-0.7272*(x-ax);
            if ((x>=10.71) && (x<=11.55) && (z>=4.32) && (z<=5.57)){
                moveToWrapper (x,ay,z,0,0, -0.707,0.707);
                moveToWrapper (ax,ay,az,0,0, -0.707,0.707);
            }
            else {
                double x_KOZL = ax - 0.22;
                moveToWrapper(x_KOZL,-9.8,4.79, 0.0, 0.0, -0.707, 0.707);
                moveToWrapper(x_KOZL,-9.8,az, 0.0, 0.0, -0.707, 0.707);
                moveToWrapper(ax,-9.8,az, 0.0, 0.0, -0.707, 0.707);}
        }

        if (ap==7)
        {
            x=(63.6425-0.218*az-1.0682*ax*ax-ax*az+11.7395*ax)/(16.9595-1.0682*ax-az);
            z=az-1.0682*(x-ax);
            if ((x>=10.71) && (x<=11.55) && (z>=4.32) && (z<=5.57)){
                moveToWrapper (x,ay,z,0,0, -0.707,0.707);
                moveToWrapper (ax,ay,az,0,0, -0.707,0.707);
            }
            else {
                double x_KOZR = ax;
                moveToWrapper(x_KOZR,-9.8,4.79, 0.0, 0.0, -0.707, 0.707);
                moveToWrapper(x_KOZR,-9.8,az, 0.0, 0.0, -0.707, 0.707);
                moveToWrapper(ax,-9.8,az, 0.0, 0.0, -0.707, 0.707);}
        }



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
        api.flashlightControlFront(0.2f);
        try {
            java.lang.Thread.sleep(2000);
        } catch(InterruptedException ex){
            java.lang.Thread.currentThread().interrupt();
        }
        Bitmap bitmap;
        bitmap = api.getBitmapNavCam();
        api.flashlightControlFront(0);
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
}

