 package com.yajith.scanner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
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
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
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
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

 public class MainActivity extends AppCompatActivity {
    private LinearLayout linearLayout;
    private ImageButton button;
    private ArrayList<Bitmap> arrayList;
    private SpeechRecognizer speechRecognizer;
    private String name="";
    private Context context;
    private TextToSpeech textToSpeech;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        arrayList=new ArrayList<>();
        context=this;
        button=findViewById(R.id.add);
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
        button.setOnClickListener(new View.OnClickListener() {
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
                Toast.makeText(context, "Error Occurred", Toast.LENGTH_SHORT).show();
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
        if(requestCode==500) {
            try {
                ArrayList dat = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                if (dat.get(0).toString().contains("capture")) {
                    capture();
                }
                if (dat.get(0).toString().contains("open")) {
                    open();
                }
                if (dat.get(0).toString().contains("delete")) {
                    delete();
                }
                if (dat.get(0).toString().contains("name")) {
                    String[] n = dat.get(0).toString().split("name");
                    if (n.length > 1) {
                        name = n[1].trim().toLowerCase();
                        textToSpeech.speak("Done Added name " + name, TextToSpeech.QUEUE_FLUSH, null);
                    } else {
                        textToSpeech.speak("say name name of file", TextToSpeech.QUEUE_FLUSH, null);
                    }
                }
                if (dat.get(0).toString().contains("save")) {
                    if (name.equals("")) {
                        textToSpeech.speak("Say name name of file", TextToSpeech.QUEUE_FLUSH, null);
                        return;
                    }
                    if (arrayList.size() < 0) {
                        textToSpeech.speak("No Images Captured. say capture to capture images", TextToSpeech.QUEUE_FLUSH, null);
                        return;
                    }
                    save(arrayList);
                }
            } catch (NullPointerException e)
            {
                e.printStackTrace();
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
            File[] allfile=dirc.listFiles();
            if(allfile.length<0)
            {
                Toast.makeText(this, "No File Found", Toast.LENGTH_SHORT).show();
            }
            else
            {
                Document document=new Document();
                try {
                    if(name.equals(""))
                    {
                        textToSpeech.speak("Say name name of file",TextToSpeech.QUEUE_FLUSH,null);return;
                    }
                    File file=new File(context.getExternalFilesDir("Scanner"),name+".pdf");
                    if(!file.exists())
                    {
                        file.createNewFile();
                    }
                    PdfWriter.getInstance(document,new FileOutputStream(file));
                    document.open();
                    for(int i=0;i<allfile.length;i++) {
                        String path=context.getExternalFilesDir("Scanner/Temp").toString()+"/"+i+".jpeg";
                        Image image = Image.getInstance(path);
                        float scaler = ((document.getPageSize().getWidth() - document.leftMargin()-document.rightMargin() - 0) / image.getWidth()) * 100;
                        image.scalePercent(scaler);
                        image.setAlignment(Image.ALIGN_CENTER | Image.ALIGN_TOP);
                        document.add(image);
                        document.newPage();
                    }
                    document.close();
                } catch (DocumentException e) {
                    e.printStackTrace();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                openpdf();
            }
        }
        else
        {
            textToSpeech.speak("Error",TextToSpeech.QUEUE_FLUSH,null);
        }

    }
    private void open()
    {
        if(name.equals(""))
        {
            textToSpeech.speak("No File Saved",TextToSpeech.QUEUE_FLUSH,null);
            return;
        }
        File file=new File(context.getExternalFilesDir("Scanner"),name+".pdf");
        Intent intent=new Intent(MainActivity.this,PDFViewer.class);
        intent.putExtra("name",name);
        startActivity(intent);
    }
    private void openpdf()
    {
        File file=new File(context.getExternalFilesDir("Scanner"),name+".pdf");
        Intent intent=new Intent(MainActivity.this,PDFViewer.class);
        intent.putExtra("name",name);
        linearLayout.removeAllViewsInLayout();
        startActivity(intent);
        /*Uri uri=Uri.parse("content://"+file.getAbsolutePath());
        Intent intent=new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setDataAndType(uri,"application/pdf");
        try {
            startActivity(intent);
        }
        catch (ActivityNotFoundException r)
        {
            r.printStackTrace();
        }*/
    }
    private void delete()
    {
        File file=new File(context.getExternalFilesDir("Scanner"),"Temp");
        if(file.exists())
        {
            file.delete();
        }
    }
    private void addimage(LinearLayout linearLayout, Bitmap bitmap)
    {
        ImageView imageView=new ImageView(this);
        imageView.setImageBitmap(bitmap);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 400);
        layoutParams.gravity= Gravity.CENTER;
        imageView.setLayoutParams(layoutParams);
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

     @Override
     public void onBackPressed() {

     }
 }