package com.example.musicapp;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.splashscreen.SplashScreen;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;

public class FileListActivity extends AppCompatActivity {

    final String permission = Manifest.permission.READ_EXTERNAL_STORAGE;
    ActivityResultLauncher<String> storagePermissionLauncher;
    RecyclerView recyclerView;
    TextView noFileText;


    File[] filesAndFolders;
    private File root;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_list);
        recyclerView = findViewById(R.id.recycler_view);
        noFileText = findViewById(R.id.nofiles_Textview);

        userReponses();
    }

    private void userReponses() {
        if ((ContextCompat.checkSelfPermission(this, permission)) == PackageManager.PERMISSION_GRANTED) {
            fetchFiles();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if ((shouldShowRequestPermissionRationale(permission))) {
                new AlertDialog.Builder(this)
                        .setTitle("Yêu cầu cấp quyền bộ nhớ").setMessage("Cấp quyền để tiến hành đọc bộ nhớ thiết bị!").setPositiveButton("Chấp nhận", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                storagePermissionLauncher.launch(permission);
                            }
                        }).setNegativeButton("Từ chối", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(getApplicationContext(), "Bạn từ chối cấp quyền!!!", Toast.LENGTH_SHORT).show();
                            }
                        }).show();
            }
        } else {
            Toast.makeText(this, "Bạn từ chối sử dụng ứng dụng!!!", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchFiles() {
        String path = null;
        Intent intent = getIntent();
        if (intent != null) {
            path = intent.getStringExtra("path");
        }
        if (path == null) {
            path = Environment.getExternalStorageDirectory().getPath();
        }

        File root = new File(path);
        filesAndFolders = root.listFiles();

        if (filesAndFolders == null) {
            noFileText.setVisibility(View.VISIBLE);
            return;
        }

        noFileText.setVisibility(View.INVISIBLE);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        FileAdapter fileAdapter = new FileAdapter(this, filesAndFolders, new FileAdapter.ItemClickListener() {
            @Override
            public void onItemClick(String path) {
                new AlertDialog.Builder(FileListActivity.this)
                        .setTitle("Lựa chọn").setMessage("Bạn muốn mở thư mục hay phát toàn bộ nhạc trong thư mục đã chọn?")
                        .setPositiveButton("Mở thư mục", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(FileListActivity.this, FileListActivity.class);
                                intent.putExtra("path", path);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        }).setNegativeButton("Phát nhạc", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(FileListActivity.this, MainActivity.class);
                                intent.putExtra("path", path);
                                startActivity(intent);
                            }
                        }).show();
            }
        });
        recyclerView.setAdapter(fileAdapter);
    }

    private void userChoise(String path) {
        new AlertDialog.Builder(this)
                .setTitle("Lựa chọn").setMessage("Bạn muốn mở thư mục hay phát toàn bộ nhạc trong thư mục đã chọn?")
                .setPositiveButton("Mở thư mục", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).setNegativeButton("Phát nhạc", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();
    }
}
