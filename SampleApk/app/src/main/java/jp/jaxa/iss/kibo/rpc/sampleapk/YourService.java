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

        String[] stringArray1 = getQR();
        String pattern = stringArray1[0];
        double pattern1 = Double.parseDouble(pattern);
        double x_Adash = Double.parseDouble(stringArray1[1]);
        double y_Adash = Double.parseDouble(stringArray1[2]);
        double z_Adash = Double.parseDouble(stringArray1[3]);

        if (pattern1==1.0 || pattern1==2.0 || pattern1==8.0)
        {
            moveToWrapper(x_Adash,y_Adash,z_Adash, 0.0, 0.0, -0.707, 0.707);

        }


        if (pattern1==3.0 || pattern1==4.0)
        {
            moveToWrapper(x_Adash,-9.8,4.79, 0.0, 0.0, -0.707, 0.707);
            moveToWrapper(x_Adash,-9.8,z_Adash, 0.0, 0.0, -0.707, 0.707);
        }

        if (pattern1==5.0 || pattern1==6.0)
        {
            double x_KOZL = x_Adash - 0.22;
            moveToWrapper(x_KOZL,-9.8,4.79, 0.0, 0.0, -0.707, 0.707);
            moveToWrapper(x_KOZL,-9.8,z_Adash, 0.0, 0.0, -0.707, 0.707);
            moveToWrapper(x_Adash,-9.8,z_Adash, 0.0, 0.0, -0.707, 0.707);



        }
        if (pattern1==7.0)
        {
            double x_KOZR = x_Adash;
            moveToWrapper(x_KOZR,-9.8,4.79, 0.0, 0.0, -0.707, 0.707);
            moveToWrapper(x_KOZR,-9.8,z_Adash, 0.0, 0.0, -0.707, 0.707);
            moveToWrapper(x_Adash,-9.8,z_Adash, 0.0, 0.0, -0.707, 0.707);
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

    private String[] getQR(){
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

        String[] stringArray = new String[4];
        int element = 0;
        for (int i = 1; i < QRCodeString.length();++i)
        {
            if (QRCodeString.charAt(i)==':')
            {
                int condition= 0;
                int initial= i+1;

                for (int j=i;condition<1;++j)
                {
                    if (QRCodeString.charAt(j)==',')
                    {
                        stringArray[element]= QRCodeString.substring(initial,j-1);
                        condition=1;
                        element++;

                    }
                }

            }
        }

        return stringArray;

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

