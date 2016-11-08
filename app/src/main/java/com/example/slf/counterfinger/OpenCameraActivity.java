package com.example.slf.counterfinger;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfInt4;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;


public class OpenCameraActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2, View.OnClickListener {

    private static final String TAG = "OpenCameraActivity";
    private Button button;

    static {
        OpenCVLoader.initDebug();
    }
    private int num=0;
    private int lastshownum=0;
    private Mat mRgba;
    private Mat mFlipRgba;
    private Mat mTransposeRgba;
    private Handler handler;

    private CameraBridgeViewBase mOpenCvCameraView;

    public OpenCameraActivity() {

        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_open_camera);
        button= (Button) findViewById(R.id.button);
        button.setOnClickListener(this);
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.fd_activity_surface_view);
        mOpenCvCameraView.enableView();//
        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCvCameraView.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_BACK);//前置摄像头 CAMERA_ID_BACK为后置摄像头
        handler=new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                Log.d("test.....cont.....",num+"");
//                Toast.makeText(OpenCameraActivity.this,lastshownum,Toast.LENGTH_SHORT).show();
                return false;
            }
        });

    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void onDestroy() {
        super.onDestroy();
        mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat();
        mFlipRgba = new Mat();
        mTransposeRgba = new Mat();

    }

    public void onCameraViewStopped() {
        mRgba.release();
    }

    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();//注意
        ArrayList<MatOfPoint> contours=new ArrayList<MatOfPoint>();
        Core.flip(mRgba, mFlipRgba, 1);
//        下面两条是后置摄像头要写的ΩΩ
        Core.flip(mFlipRgba, mFlipRgba, 1);
        Core.flip(mFlipRgba, mFlipRgba, 0);
        Core.transpose(mFlipRgba, mTransposeRgba);
        Imgproc.resize(mTransposeRgba,mFlipRgba,mFlipRgba.size() , 0.0D, 0.0D, 0);
        Imgproc.cvtColor(mFlipRgba, mRgba, Imgproc.COLOR_BGR2GRAY);
        Imgproc.threshold(mRgba, mRgba, 100, 255, Imgproc.THRESH_BINARY);
        Imgproc.cvtColor(mRgba, mFlipRgba, Imgproc.COLOR_GRAY2RGBA, 4);
        Mat hierarchy = new Mat();
        hierarchy.convertTo(hierarchy, CvType.CV_32SC1);
        //定义轮廓抽取模式
        int mode = Imgproc.RETR_EXTERNAL;
        //定义轮廓识别方法
        int method = Imgproc.CHAIN_APPROX_NONE;
        Log.d("test.....","here");
        Imgproc.findContours(mRgba, contours,hierarchy,mode,method);
        Log.d("test.....","here2");

        num=contours.size();
        double x=0;
        double y=0;
        for (int k=0; k < contours.size(); k++){
            Point[] ap=contours.get(k).toArray();
            x=x+ap[0].x;
            y=y+ap[0].y;
        }
        Point cenPoint=new Point(x/contours.size(),y/contours.size());
        ArrayList<MatOfInt> hull=new ArrayList<MatOfInt>();
        ArrayList<MatOfInt4> dis=new ArrayList<MatOfInt4>();
        int shownum=0;
        int max=0;
        lastshownum=0;
        for (int k=0; k < contours.size(); k++){
            MatOfInt matint=new MatOfInt();
            Imgproc.convexHull(contours.get(k), matint,true);
            hull.add(matint);

        }
        for (int k=0; k < contours.size(); k++){
            try {
                MatOfInt4 matint4=new MatOfInt4();
                Imgproc.convexityDefects(contours.get(k), hull.get(k),matint4);
                List<Integer> cdList = matint4.toList();
//                double[] a=matint4.get(0,0);

//                int a1=cdList.get(0);
//                int a2=cdList.get(1);
//                int a3=cdList.get(2);
//                int a4=cdList.get(3);
                shownum=0;
                for (int i=0;i<cdList.size();i++){
                    if (i%4==3&&cdList.get(i)>40500){
//                        Point[] a1=contours.get(cdList.get(i-3)).toArray();
//                        Double x1=a1[0].x;
//                        Double y1=a1[0].y;
//                        Point[] a2=contours.get(cdList.get(i-2)).toArray();
//                        Double x2=a2[0].x;
//                        Double y2=a2[0].y;
//                        Point[] a3=contours.get(cdList.get(i-1)).toArray();
//                        Double x3=a3[0].x;
//                        Double y3=a3[0].y;
//                        if ((x1-x3)*(x2-x3)+(y1-y3)*(y2-y3)>0){
//                            shownum++;
//                        }
//                        找到最大值
//                        if (cdList.get(i)>max){
//                            max=cdList.get(i);
//                        }

                        shownum++;
                    }
                }
//                for (int i=0;i<cdList.size();i++){
////                    防止max为0的时候
//                    if (max==0){
//                        break;
//                    }
//                    if (i%4==3&&max*1.0/cdList.get(i)<2){
//                        shownum++;
//                    }
//                }
//                //        手指比凹槽+1
//                shownum++;


                if (shownum>lastshownum){
                    lastshownum=shownum;
                }

            }catch (Exception e){

            }

        }
        num=dis.size()+1;
        handler.sendEmptyMessage(lastshownum);
        return mFlipRgba;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return true;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.button:
//                Toast.makeText(OpenCameraActivity.this,""+num,Toast.LENGTH_SHORT).show();
                if (lastshownum==0){
                    lastshownum++;
                }
                Toast.makeText(OpenCameraActivity.this,lastshownum+"",Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
