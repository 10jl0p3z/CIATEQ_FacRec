package com.example.reconocimientofacial;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.view.View.OnClickListener;
import android.content.Intent;
import android.graphics.Bitmap;


public class MainActivity extends AppCompatActivity implements OnClickListener {

    Button btn;
    ImageView img;
    Intent i;
    Bitmap bmp;
    final static int cons= 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    public void init()
    {
        btn = (Button) findViewById(R.id.btn_TakePhoto);
        btn.setOnClickListener(this);
        img = (ImageView) findViewById(R.id.img_CurrentPhoto);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
        {
            btn.setEnabled(false);
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 0) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                btn.setEnabled(true);
            }
        }
    }

    @Override
    public void onClick(View v)
    {
        int id;
        id = v.getId();
        switch (id)
        {
            case R.id.btn_TakePhoto:
                i = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(i,cons);

                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK)
        {
            Bundle ext= data.getExtras();
            bmp = (Bitmap) ext.get("data");
            img.setImageBitmap(bmp);
        }
    }
}
