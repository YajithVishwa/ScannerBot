package com.yajith.scanner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.widget.Toast;

import com.github.barteksc.pdfviewer.PDFView;

import java.io.File;

public class PDFViewer extends AppCompatActivity {
    private PDFView pdfView;
    private String file;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_p_d_f_viewer);
        pdfView=findViewById(R.id.pdf);
        getSupportActionBar().setTitle("PDF Viewer");
        file= (String) getIntent().getExtras().get("name");
        File f=new File(getApplicationContext().getExternalFilesDir("Scanner"),file+".pdf");
        Toast.makeText(PDFViewer.this,file,Toast.LENGTH_LONG);
        pdfView.fromFile(f).load();


    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }
}