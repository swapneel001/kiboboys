package jp.jaxa.iss.kibo.rpc.sampleapk;

import android.graphics.Bitmap;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;

import com.google.zxing.FormatException;

import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.common.HybridBinarizer;



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

        api.flashlightControlFront(0.025f);
        //Read QR code from point-A
        Bitmap image = api.getBitmapNavCam();
        api.flashlightControlFront(0);
        String content = readQr(image);

        api.sendDiscoveredQR(content);
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
                               double qua_w){

        final int LOOP_MAX = 3;
        final Point point = new Point(pos_x, pos_y, pos_z);
        final Quaternion quaternion = new Quaternion((float)qua_x, (float)qua_y,
                                                     (float)qua_z, (float)qua_w);

        Result result = api.moveTo(point, quaternion, true);

        int loopCounter = 0;
        while(!result.hasSucceeded() || loopCounter < LOOP_MAX){
            result = api.moveTo(point, quaternion, true);
            ++loopCounter;
        }
    }

    private String readQr(Bitmap image){
            String qrcontent = "error";
            int width = image.getWidth();
            int height = image.getHeight();
            int[] pixels = new int[width*height];
            image.getPixels(pixels,0,width,0,0,width,height);

            RGBLuminanceSource source = new RGBLuminanceSource(width,height,pixels);

            BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));
            Reader reader = new MultiFormatReader();
            try {
                com.google.zxing.Result result = reader.decode(binaryBitmap);
                qrcontent = result.getText();
                Log.d("readQR",result);
            } catch(NotFoundException e) {
                e.printStackTrace();
            } catch (ChecksumException e) {
                e.printStackTrace();
            } catch (FormatException e) {
                e.printStackTrace();
            }
            return qrcontent;
        }

}

