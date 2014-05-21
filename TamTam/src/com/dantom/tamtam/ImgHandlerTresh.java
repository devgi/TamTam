package com.dantom.tamtam;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.BackgroundSubtractor;
import org.opencv.video.BackgroundSubtractorMOG2;

import android.graphics.Point;
import android.util.Log;

public class ImgHandlerTresh implements CvCameraViewListener2 {

	final static String TAG = "tam";
	private Mat frameRGBA;
//	private Mat smallFrame;
//	private Mat result;
	private BackgroundSubtractor bgfg;
//	private ArrayList<MatOfPoint> contours;
//	private Mat h;
//	private MatOfInt hull;
//	private Mat bgmask;

	
	public void onCameraViewStarted(int width, int height) {
//		
//		result = new Mat();
//		smallFrame = new Mat();
		bgfg = new BackgroundSubtractorMOG2(0, 160, false);
//		bgmask = new Mat();
//		contours = new ArrayList<MatOfPoint>();
//		h = new Mat();
//		hull = new MatOfInt();
    }
	
    public void onCameraViewStopped() {
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
    	frameRGBA=inputFrame.rgba();
        // return inputFrame.rgba();
    	manipulation();
    	return frameRGBA;
    }
    
   
    
    private void manipulation(){
    	Log.d(TAG, "start maninpulation");
    	
    	Size origFrameSize = frameRGBA.size();
    	int shrinkFactor = 10; 
    	Size newFrameSize = new Size(frameRGBA.width() / shrinkFactor, frameRGBA.height() / shrinkFactor);
    	Mat smallFrame = new Mat();
		Imgproc.resize(frameRGBA, smallFrame , newFrameSize, 0, 0, Imgproc.INTER_NEAREST);
    	
    	
    	
    	Mat result = new Mat();
		Imgproc.cvtColor(smallFrame, result , Imgproc.COLOR_RGBA2GRAY);
    	
    	Log.d(TAG, "blur");
    	Imgproc.blur(result, result, new Size(5, 5));
    	
    	
    	Log.d(TAG, "tresh");
    	Imgproc.threshold(result, result, 0, 255, Imgproc.THRESH_BINARY_INV + Imgproc.THRESH_OTSU);
    	
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Mat h = new Mat();
		Imgproc.findContours(result, contours  , h  , Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
		Log.e(TAG, "counters " + contours.size());
		
		
		double maxArea = 0;
		double area = 0;
		MatOfPoint maxCnt = null;
		for (MatOfPoint cnt : contours) {
			area = Imgproc.contourArea(cnt);
			if (area > maxArea) {
				maxArea = area;
				maxCnt = cnt;
			}
		}
		
		if (maxCnt != null){
			Log.e(TAG, "max area " + maxArea );
			MatOfInt hull = new MatOfInt();
			Imgproc.convexHull(maxCnt, hull );
			List<MatOfPoint> contorsToDraw = new ArrayList<MatOfPoint>();
			contorsToDraw.add(maxCnt);
			Imgproc.drawContours(smallFrame, contorsToDraw, 0, new Scalar(0, 255, 0), 1);
		}
		
		
    	
        Imgproc.resize(smallFrame, frameRGBA, origFrameSize);
		
    }

}
