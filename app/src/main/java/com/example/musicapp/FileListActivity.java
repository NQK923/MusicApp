package com.example.musicapp;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.splashscreen.SplashScreen;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;

public class FileListActivity extends AppCompatActivity {

    final String permissionWrite = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    ActivityResultLauncher<String> storagePermissionLauncher;
    RecyclerView recyclerView;
    TextView noFileText;
    File[] filesAndFolders;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_list);
        recyclerView = findViewById(R.id.recycler_view);
        noFileText = findViewById(R.id.nofiles_Textview);
        storagePermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
            if (granted) {
                fetchFiles();
            } else {
                userReponses();
            }
        });
        storagePermissionLauncher.launch(permissionWrite);

    }

    private void userReponses() {
        if ((ContextCompat.checkSelfPermission(this, permissionWrite)) == PackageManager.PERMISSION_GRANTED) {
            fetchFiles();
        } else {
            if ((shouldShowRequestPermissionRationale(permissionWrite))) {
                new AlertDialog.Builder(this)
                        .setTitle("Yêu cầu cấp quyền bộ nhớ").setMessage("Cấp quyền để tiến hành đọc bộ nhớ thiết bị!").setPositiveButton("Chấp nhận", (dialog, which) -> storagePermissionLauncher.launch(permissionWrite)).setNegativeButton("Từ chối", (dialog, which) -> Toast.makeText(getApplicationContext(), "Bạn từ chối cấp quyền!!!", Toast.LENGTH_SHORT).show()).show();
            }
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
        assert filesAndFolders != null;
        Log.i("FileSize", filesAndFolders.length + " fetchFiles: ");
        if (filesAndFolders.length == 0) {
            noFileText.setVisibility(View.VISIBLE);
            return;
        }
        noFileText.setVisibility(View.INVISIBLE);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        FileAdapter fileAdapter = new FileAdapter(this, filesAndFolders, path1 -> new AlertDialog.Builder(FileListActivity.this)
                .setTitle("Lựa chọn").setMessage("Bạn muốn mở thư mục hay phát toàn bộ nhạc trong thư mục đã chọn?")
                .setPositiveButton("Mở thư mục", (dialog, which) -> {
                    Intent intent1 = new Intent(FileListActivity.this, FileListActivity.class);
                    intent1.putExtra("path", path1);
                    intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent1);
                }).setNegativeButton("Phát nhạc", (dialog, which) -> {
                    Intent intent1 = new Intent(FileListActivity.this, MainActivity.class);
                    intent1.putExtra("path", path1);
                    startActivity(intent1);
                }).show());
        recyclerView.setAdapter(fileAdapter);
    }
}
