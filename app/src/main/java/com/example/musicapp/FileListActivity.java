package com.example.musicapp;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;

public class FileListActivity extends AppCompatActivity {

    final String permission = Manifest.permission.READ_EXTERNAL_STORAGE;
    ActivityResultLauncher<String> storagePermissionLauncher;
    RecyclerView recyclerView = findViewById(R.id.recyclerview);
    TextView noFileText = findViewById(R.id.nofiles_Textview);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_list);
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
        String path = Environment.getExternalStorageDirectory().getPath();

        File root = new File(path);
        File[] filesAndFolders = root.listFiles();
        if (filesAndFolders == null) {
            noFileText.setVisibility(View.VISIBLE);
            return;
        }

        noFileText.setVisibility(View.INVISIBLE);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new FileAdapter(getApplicationContext(), filesAndFolders));
    }
}
