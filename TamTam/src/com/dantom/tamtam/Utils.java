package com.dantom.tamtam;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;

public class Utils {
	
	public static void PlaySound(int sound, Context context) {
		MediaPlayer mp = MediaPlayer.create(context, sound);
        mp.setOnCompletionListener(new OnCompletionListener() {
			
			public void onCompletion(MediaPlayer mp) {
				mp.release();
				
			}
		});   
        mp.start();
	}
}
