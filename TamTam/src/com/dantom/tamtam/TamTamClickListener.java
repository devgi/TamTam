package com.dantom.tamtam;

import java.util.Random;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.view.View;
import android.view.View.OnClickListener;

public class TamTamClickListener implements OnClickListener {
	
	private int sound;
	private Random r = new Random(System.currentTimeMillis());
	private Context context;
	
	public TamTamClickListener (int sound, Context context){
		this.sound = sound;
		this.context = context;
	}
	public void onClick(View v) {
		MediaPlayer mp = MediaPlayer.create(context, sound);
        mp.setOnCompletionListener(new OnCompletionListener() {
			
			public void onCompletion(MediaPlayer mp) {
				mp.release();
				
			}
		});   
        mp.start();
	}

}