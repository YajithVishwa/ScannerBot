 package com.yajith.scanner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.codec.BmpImage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

 public class MainActivity extends AppCompatActivity {
    private LinearLayout linearLayout;
    private ImageButton button;
    private Button scan,save;
    private ArrayList<Bitmap> arrayList;
    private SpeechRecognizer speechRecognizer;
    private String name="yajith";
    private Context context;
    private TextToSpeech textToSpeech;
    /*
     <ImageButton
        android:layout_width="100dp"
        android:layout_height="100dp"
        android:text="Take photo"
        android:background="@drawable/round"
        android:src="@drawable/ic_channel_foreground"
        android:layout_weight="0"
        android:id="@+id/add"
        android:layout_gravity="bottom|center"/>

     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        arrayList=new ArrayList<>();
        context=this;
      //  button=findViewById(R.id.add);
        save=findViewById(R.id.save);
        scan=findViewById(R.id.scan);
        linearLayout=findViewById(R.id.photo);
        textToSpeech=new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if(i!=TextToSpeech.ERROR)
                {
                    textToSpeech.setLanguage(Locale.UK);
                }
            }
        });
        speechRecognizer=SpeechRecognizer.createSpeechRecognizer(this);
/*        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Need to speak");
                try {
                    startActivityForResult(intent,500);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
               // capture();
            }
        });*/
        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                capture();
            }
        });
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                save(arrayList);
            }
        });
    }
    private void capture()
    {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)== PackageManager.PERMISSION_GRANTED)
        {
            Intent capture=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(capture,100);
        }
        else
        {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA},200);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 200)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, 100);
            }
            else
            {

            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==100&&resultCode== Activity.RESULT_OK)
        {
            Bitmap bitmap= (Bitmap) data.getExtras().get("data");
            addimage(linearLayout,bitmap);
            arrayList.add(bitmap);
        }
        if(requestCode==500)
        {
            ArrayList dat=data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if(dat.get(0).toString().contains("capture"))
            {
                capture();
            }
            if(dat.get(0).toString().contains("enter the name"))
            {
                String[] n=dat.get(0).toString().split("enter the name ");
                name=n[1].trim().toLowerCase();
                Toast.makeText(this, n[1], Toast.LENGTH_SHORT).show();
            }
            if(dat.get(0).toString().contains("save"))
            {
                if(name.equals(""))
                {
                    textToSpeech.speak("Say Enter the name",TextToSpeech.QUEUE_FLUSH,null);
                    Toast.makeText(this, "Say Enter the name", Toast.LENGTH_SHORT).show();
                }
                if(arrayList.size()<0)
                {
                    textToSpeech.speak("No Images Captured",TextToSpeech.QUEUE_FLUSH,null);
                    Toast.makeText(this, "No Images Captured", Toast.LENGTH_SHORT).show();
                }

            }

        }
    }
    private void filestore(ArrayList<Bitmap> bitmaps)
    {
        int i=0;
        File file=new File(context.getExternalFilesDir("Scanner"),"Temp");
        if(!file.exists())
        {
            file.mkdir();
        }
        for(Bitmap bitmap:bitmaps)
        {
           ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
           bitmap.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream);
           File file1=new File(context.getExternalFilesDir("Scanner/Temp"),i+".jpeg");
            try {
                FileOutputStream fileOutputStream=new FileOutputStream(file1);
                fileOutputStream.write(byteArrayOutputStream.toByteArray());
                fileOutputStream.flush();
                fileOutputStream.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            i++;
        }
    }
    private void save(ArrayList<Bitmap> arrayList) {
        filestore(arrayList);
        File dirc=new File(String.valueOf(context.getExternalFilesDir("Scanner/Temp")));
        if(dirc.exists())
        {

        }
        else
        {
            Toast.makeText(this, "Error Occurred", Toast.LENGTH_SHORT).show();
        }
    }

     private File getOutputFile() {
         File root = new File(this.getExternalFilesDir(null), "Scanner Folder");

         boolean isFolderCreated = true;
         if (!root.exists()) {
             isFolderCreated = root.mkdir();
         }
         if (isFolderCreated) {
             String timeStamp = new SimpleDateFormat("yyyy_MM_dd_HH_mm", Locale.US).format(new Date());
             return new File(root, name + ".pdf");
         } else {
             Toast.makeText(this, "Folder is not created", Toast.LENGTH_SHORT).show();
             return null;
         }
     }
    private void addimage(LinearLayout linearLayout, Bitmap bitmap)
    {
        ImageView imageView=new ImageView(this);
        imageView.setImageBitmap(bitmap);
        linearLayout.addView(imageView);
    }

    @Override
    protected void onDestroy() {
        if(speechRecognizer!=null)
        {
            speechRecognizer.destroy();
        }
        if(textToSpeech!=null)
        {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }
}