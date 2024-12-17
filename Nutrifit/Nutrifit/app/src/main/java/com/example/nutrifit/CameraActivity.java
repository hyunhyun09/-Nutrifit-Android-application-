package com.example.nutrifit;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AppCompatActivity;
import android.graphics.ImageDecoder;

import com.doinglab.foodlens.sdk.FoodLens;
import com.doinglab.foodlens.sdk.NetworkService;
import com.doinglab.foodlens.sdk.errors.BaseError;
import com.doinglab.foodlens.sdk.network.model.RecognitionResult;
import com.doinglab.foodlens.sdk.RecognizeResultHandler;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CameraActivity extends AppCompatActivity {

    private Button sendButton;
    private ImageView imageView;

    private ActivityResultLauncher<String> permissionLauncher;
    private ActivityResultLauncher<Uri> cameraLauncher;
    private ActivityResultLauncher<String> galleryLauncher;

    private byte[] imageByteData;
    private Uri photoUri;
    private static final String TAG_E = "ERROR";
    private static final String TAG_S = "SUCCESS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        imageView = findViewById(R.id.imageView);
        Button cameraButton = findViewById(R.id.cameraButton);
        Button galleryButton = findViewById(R.id.galleryButton);
        sendButton = findViewById(R.id.sendButton);

        sendButton.setEnabled(false);

        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                result -> {
                    if (result) {
                        openCamera();
                    } else {
                        Toast.makeText(this, "카메라 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // 카메라 런처 초기화
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                result -> {
                    if (result) {
                        imageView.setImageURI(photoUri);
                        imageByteData = convertUriToByteArray(photoUri);
                        sendButton.setEnabled(true);
                    } else {
                        Log.e(TAG_E, "카메라 종료.");
                    }
                }
        );

        galleryButton.setOnClickListener(v -> galleryLauncher.launch("image/*"));

        sendButton.setOnClickListener(v -> {
            if (imageByteData != null) {
                sendImageToFoodLens(imageByteData);
            } else {
                Toast.makeText(this, "사진이 선택되지 않았습니다.", Toast.LENGTH_SHORT).show();
            }
        });

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                this::handleImageSelection
        );

        cameraButton.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                permissionLauncher.launch(Manifest.permission.CAMERA);
            }
        });
    }

    private void openCamera() {
        File photoFile = null;
        try {
            photoFile = createImageFile();
            Log.i(TAG_S, "photoFile opened.");
        } catch (IOException e) {
            Toast.makeText(this, "촬영 실패.", Toast.LENGTH_SHORT).show();
            return;
        }

        photoUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", photoFile);
        cameraLauncher.launch(photoUri);
    }

    private File createImageFile() throws IOException {
        @SuppressLint("SimpleDateFormat") String imageFileName = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File storageDir = getExternalFilesDir("Pictures");
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        Log.i(TAG_S, "image file 생성.");
        return image;
    }

    private void handleImageSelection(Uri uri) {
        if (uri != null) {
            imageView.setImageURI(uri);
            imageByteData = convertUriToByteArray(uri);
        }

        if (imageByteData != null) {
            Log.i(TAG_S, "이미지 선택 완료.");
            sendButton.setEnabled(true);
        } else {
            sendButton.setEnabled(false);
        }
    }

    private byte[] convertBitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    @SuppressLint("NewApi")
    private byte[] convertUriToByteArray(Uri uri) {
        try {
            Bitmap bitmap;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(getContentResolver(), uri));
            } else {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
            }
            return convertBitmapToByteArray(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void sendImageToFoodLens(byte[] imageData) {
        NetworkService networkService = FoodLens.createNetworkService(getApplicationContext());

        networkService.predictMultipleFood(imageData, new RecognizeResultHandler() {
            @Override
            public void onSuccess(RecognitionResult result) {
                String resultJson = result.toJSONString();
                Intent intent = new Intent(CameraActivity.this, CameraResultActivity.class);
                intent.putExtra("result_json", resultJson);
                startActivity(intent);
            }

            @Override
            public void onError(BaseError error) {
                Log.e(TAG_E, "FoodLens API 호출 실패: " + error.getMessage());
            }
        });
    }
}