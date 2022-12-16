package com.emresaritas.basicmediaplayer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.window.OnBackInvokedCallback;

import com.google.android.material.button.MaterialButton;

public class FileExploreActivity extends Activity {

    private FilesAdapter filesAdapter;
    private RecyclerView recyclerView;
    private String currentPath;
    private TextView textPath;
    private MaterialButton button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_explore);

        Intent intent = getIntent();
        currentPath = intent.getStringExtra("path");

        textPath = findViewById(R.id.textPath);
        button = findViewById(R.id.backBtn);
        recyclerView = findViewById(R.id.recycle);
        
        textPath.setText(currentPath);
        filesAdapter = new FilesAdapter(this, this, currentPath);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 4, GridLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(filesAdapter);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FileExploreActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}