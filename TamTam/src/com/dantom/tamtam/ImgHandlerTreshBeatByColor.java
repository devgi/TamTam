package com.dantom.tamtam;

import java.util.ArrayList;
import java.util.List;

import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import android.content.Context;
import android.util.Log;

/**
 * This class is using threshold to detect hands,  and find the moment of impact(with the virtual drum) 
 * by calculating the size of each hand.
 */
public class ImgHandlerTreshBeatByColor implements CvCameraViewListener2 {
	
	// The tag of the logging of this class
	final static String TAG = "tam";
	
	//The minimal diff in hand size, before updating the hand size.
	private static final double CONTOUR_MEAN_DIFF_TRESHOLD = 15;
	
	// The frame took from the camera
	private Mat frameRGBA;
	
	// The sounds the drums will make
	private int[] drums = {R.raw.bongo_1,R.raw.bongo_2};
	
	// The context of the main activity, used to play the drum sounds.
	private Context c;

	// This array uses to save the hands sizes between to different frames.
	private double[] distances = {0, 0};
	
	// This array is used to save the current direction of each hand, true means down.
	private boolean[] directions = {false, false};
	
	// This is the array of colors for each hand contour.
	private Scalar[] colors = {new Scalar(0,0,255,0), new Scalar(255,0,0,0)};

	/** The image resizer. */
	private ImageResizer imageResizer;
	
	/**
	 * Constructor.
	 * 
	 * @param c The context of the main activity, used to play the drum sounds.
	 */
	public ImgHandlerTreshBeatByColor(Context c){
		this.c = c;
	}
	
	public void onCameraViewStarted(int width, int height) {
		this.imageResizer = new ImageResizer(10, new Size(width, height));
    }
	
    public void onCameraViewStopped() {
    	// not in use
    }

    /**
     * This method is called when there is a new frame.
     * 
     * @see org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2#onCameraFrame(org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame)
     */
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
    	frameRGBA = inputFrame.rgba();
    	
    	manipulation();
    	
    	return frameRGBA;
    }
    
    
    private void manipulation(){
    	Log.d(TAG, "start maninpulation");
    	
    	Mat smallFrame = new Mat();
    	imageResizer.shrinkImage(frameRGBA, smallFrame);
    	Core.flip(smallFrame, smallFrame, 1);
    	
    	Mat result = handSegmentatoin(smallFrame);
    	Mat t = result.clone();
    	
		MatOfPoint maxCnt[] = find2LargestContours(result);
		
		MatOfPoint[] contours = new MatOfPoint[2];
		
		updateHands(smallFrame, maxCnt, contours);
		
		
		Mat white = new Mat(smallFrame.size(), smallFrame.type(), new Scalar(255, 255, 255));
		drawContours(white, contours);
		
        imageResizer.rescaleImage(white, frameRGBA);
    }

	private Mat handSegmentatoin(Mat smallFrame) {
		Mat result = new Mat();
		Imgproc.cvtColor(smallFrame, result , Imgproc.COLOR_RGBA2GRAY);
    	
    	Log.d(TAG, "blur");
    	Imgproc.blur(result, result, new Size(5, 5));
    	
    	Log.d(TAG, "tresh");
    	Imgproc.threshold(result, result, 0, 255, Imgproc.THRESH_BINARY_INV + Imgproc.THRESH_OTSU);
		return result;
	}
	
	private Mat handSegmentatoin3(Mat smallFrame) {
		Mat tmp = new Mat();
		Imgproc.cvtColor(smallFrame, tmp , Imgproc.COLOR_RGBA2RGB);
		
		Mat res = new Mat();
		Imgproc.cvtColor(tmp, res , Imgproc.COLOR_RGB2HSV);
    	
    	Log.d(TAG, "blur");
    	Imgproc.blur(res, res, new Size(5, 5));
    	
    	Mat bw = new Mat();
    	Core.inRange(res, new Scalar(18 , 50, 80), new Scalar(255, 255, 255), bw);
    	
    	//Imgproc.morphologyEx(bw, bw, Imgproc.MO, Imgproc.getStructuringElement(Imgproc.CV_SHAPE_RECT, new Size(5, 5)));
    	Mat kernel = Imgproc.getStructuringElement(Imgproc.CV_SHAPE_ELLIPSE, new Size(5, 5));
    	Imgproc.morphologyEx(bw, bw, Imgproc.MORPH_CLOSE, kernel);
    	
    	
    	
//    	Log.d(TAG, "tresh");
//    	Imgproc.threshold(result, result, 0, 255, Imgproc.THRESH_BINARY_INV + Imgproc.THRESH_OTSU);
		return bw;
	}

	private void updateHands(Mat smallFrame, MatOfPoint[] maxCnt,
			MatOfPoint[] contours) {
		
		if (maxCnt[0]  != null) {
			Mat mask = Mat.zeros(smallFrame.size(), CvType.CV_8UC(1));
			List<MatOfPoint> contorsToDraw = new ArrayList<MatOfPoint>();
			contorsToDraw.add(maxCnt[0]);
			Imgproc.drawContours(mask, contorsToDraw, 0, new Scalar(255), Core.FILLED);
			
			Scalar mean = Core.mean(smallFrame, mask);
			
			Moments p = Imgproc.moments(maxCnt[0]);
			int x = (int) (p.get_m10() / p.get_m00());
	        int w = smallFrame.width();
	        
	        if (x < w/2) {
	        	contours[0] = maxCnt[0];
	        	updateHand(mean,0);
	        } else {
	        	contours[1] = maxCnt[0];
	        	updateHand(mean,1);
	        }
		}
		if (maxCnt[1]  != null) {
			Mat mask = Mat.zeros(smallFrame.size(), CvType.CV_8UC(1));
			List<MatOfPoint> contorsToDraw = new ArrayList<MatOfPoint>();
			contorsToDraw.add(maxCnt[0]);
			Imgproc.drawContours(mask, contorsToDraw, 0, new Scalar(255), Core.FILLED);
			
			Scalar mean = Core.mean(smallFrame, mask);
			
			Moments p = Imgproc.moments(maxCnt[1]);
			int x = (int) (p.get_m10() / p.get_m00());
	        int w = smallFrame.width();
	        
	        if (x < w/2) {
        		contours[0] = maxCnt[1];
        		updateHand(mean, 0);
	        } else {
	        	contours[1] = maxCnt[1];
	        	updateHand(mean, 1);
	        }
		}
	}

	private void drawContours(Mat frame, MatOfPoint[] contours) {
		if (contours[0] != null) {
			drawContour(contours[0],frame, 0);
		} else {
			distances[0] = 0;
		}
		if (contours[1] != null) {
			drawContour(contours[1],frame, 1);
		} else {
			distances[1] = 0;
		}
	}

	private void drawContour(MatOfPoint contour, Mat smallFrame, int handIndex) {
		Moments p = Imgproc.moments(contour);
		int x = (int) (p.get_m10() / p.get_m00());
		int y = (int) (p.get_m01() / p.get_m00());
		
		Core.circle(smallFrame, new Point(x, y), 4, colors[handIndex]);
		MatOfInt hull = new MatOfInt();
		Imgproc.convexHull(contour, hull);
		List<MatOfPoint> contorsToDraw = new ArrayList<MatOfPoint>();
		contorsToDraw.add(contour);
		Imgproc.drawContours(smallFrame, contorsToDraw, 0, colors[handIndex], 1);
	}

	private double distance(Scalar point) {
		double xd = Math.pow(255 - point.val[0], 2); 
		double yd = Math.pow(255 - point.val[1], 2);
		double zd = Math.pow(255 - point.val[2], 2);
		
		return Math.sqrt(xd + yd + zd);		
	}
	
	private void updateHand(Scalar mean, int i) {
		double d = distance(mean);
		if (i == 1) {
			Log.d(TAG, "h1 " + d );
		} else {
			Log.d(TAG, "h0 " + d );
		}
		
    	if (Math.abs(distances[i] - d) > CONTOUR_MEAN_DIFF_TRESHOLD) {
    		if (distances[i] < d) {
    			directions[i] = true;
    		} else if (directions[i]) {
    			Utils.PlaySound(drums[i], c);
    			directions[i] = false;
    		}
    		distances[i] = d;
    	}
	}
	
	private MatOfPoint[] find2LargestContours(Mat frame) {
		List<MatOfPoint> allContours = new ArrayList<MatOfPoint>();
		Mat tempMat = new Mat();
		Imgproc.findContours(frame, allContours  , tempMat  , Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
		Log.i(TAG, "counters " + allContours.size());
		
		double[] maxArea =  new double[2];
		double area = 0;
		MatOfPoint[] maxCnt = new MatOfPoint[2];
		
		//Finding the 2 largest contours
		for (MatOfPoint cnt : allContours) {
			area = Imgproc.contourArea(cnt);
			if (area > maxArea[1] && area < 5000 && area > 170) {
				if (area > maxArea[0]){
					maxArea[1] = maxArea[0];
					maxCnt[1] = maxCnt[0];
					maxArea[0] = area;
					maxCnt[0] = cnt;
				} else {
					maxArea[1] = area;
					maxCnt[1] = cnt;
				}
			}
		}
		return maxCnt;
	}

}
