package com.dantom.tamtam;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;

public class MainActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button b1 = (Button) this.findViewById(R.id.button1);
        b1.setOnClickListener(new TamTamClickListener(R.raw.bongo_1, MainActivity.this));
        Button b2 = (Button) this.findViewById(R.id.button2);
        b2.setOnClickListener(new TamTamClickListener(R.raw.bongo_2, MainActivity.this));
        Button b3 = (Button) this.findViewById(R.id.button3);
        b3.setOnClickListener(new TamTamClickListener(R.raw.bongo_3, MainActivity.this));
        Button b4 = (Button) this.findViewById(R.id.button4);
        b4.setOnClickListener(new TamTamClickListener(R.raw.bongo_4, MainActivity.this));
    }

}
