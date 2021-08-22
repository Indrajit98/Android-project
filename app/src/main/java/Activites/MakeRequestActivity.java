package Activites;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.PermissionChecker;
import androidx.preference.PreferenceManager;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.androidnetworking.interfaces.UploadProgressListener;
import com.bumptech.glide.Glide;
import com.indrajit.Donor_Hub.R;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import Utils.Endpoints;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

public class MakeRequestActivity extends AppCompatActivity {

    EditText messageText;
    TextView chooseImageText;
    ImageView postImage;
    Button submit_button;
    Uri imageUri;
    String encodedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_request);
        AndroidNetworking.initialize(getApplicationContext());
        messageText = findViewById(R.id.message);
        chooseImageText = findViewById(R.id.choose_text);
        postImage = findViewById(R.id.post_image);
        submit_button =findViewById(R.id.submit_button);
        submit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isValid()){
                    //code to upload this post.
                   // uploadRequest(messageText.getText().toString());
                    uploadImage(messageText.getText().toString());
                }

            }
        });
        chooseImageText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // code to pick image
                permission();
            }
        });

    }

    private void uploadImage(final String toString) {
        StringRequest request = new StringRequest(Request.Method.POST, Endpoints.image_upload, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Toast.makeText(MakeRequestActivity.this, response, Toast.LENGTH_SHORT).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MakeRequestActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("number",  PreferenceManager.getDefaultSharedPreferences(getApplicationContext())//oise ni?
                        .getString("number", "12345"));
                params.put("image", encodedImage);
                params.put("message", toString);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(MakeRequestActivity.this);
        requestQueue.add(request);
    }

    private void pickImage(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent,101);
    }

    private void permission(){
        if (PermissionChecker.checkSelfPermission(getApplicationContext(),READ_EXTERNAL_STORAGE)
                != PermissionChecker.PERMISSION_GRANTED){
            //asking for permission
            requestPermissions(new String[]{READ_EXTERNAL_STORAGE},401);
        }else{
            //permission is already there
            pickImage();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 401) {
            if (grantResults[0] == PermissionChecker.PERMISSION_GRANTED){
                //permission was granted
                pickImage();
            }else {
                //permission not granted
                showMessage("Permission Declined");
            }
        }
    }

    private void uploadRequest(String message) {
        //code to upload the message
        String path = "";
        try {
            path = getPath(imageUri);
        } catch (URISyntaxException e) {
            showMessage("wrong uri");
        }
        String number = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .getString("number", "12345");

        AndroidNetworking.upload(Endpoints.upload_request)
                .addMultipartFile("file", new File(path))
                .addQueryParameter("message",message)
                .addQueryParameter("number",number)
                .setPriority(Priority.HIGH)
                .build()
                .setUploadProgressListener(new UploadProgressListener() {
                    @Override
                    public void onProgress(long bytesUploaded, long totalBytes) {
                        // do anything with progress
                        int progress =(int) (100 * bytesUploaded / totalBytes);
                        chooseImageText.setText(String.valueOf(progress+"%"));
                        chooseImageText.setOnClickListener(null);
                    }
                })
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            if (response.getBoolean("success")){
                                showMessage("Succesfull");
                                MakeRequestActivity.this.finish();
                            }
                            else {
                                showMessage(response.getString("message"));
                            }
                            Toast.makeText(MakeRequestActivity.this, ""+response.getString("success"), Toast.LENGTH_SHORT).show();
                    }catch (Exception e){
                            Toast.makeText(MakeRequestActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                }

                    @Override
                    public void onError(ANError anError) {
                        Toast.makeText(MakeRequestActivity.this, ""+anError.getErrorDetail(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void imageStore(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] imageBytes = stream.toByteArray();
        encodedImage = android.util.Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101 && resultCode == RESULT_OK){
           if (data != null){
               imageUri = data.getData();
               InputStream inputStream = null;
               try {
                   inputStream = getContentResolver().openInputStream(imageUri);
               } catch (FileNotFoundException e) {
                   e.printStackTrace();
               }
               Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
               imageStore(bitmap);
               Glide.with(getApplicationContext()).load(imageUri).into(postImage);
           }
        }
    }

    private boolean isValid(){
        if (messageText.getText().toString().isEmpty()){
            showMessage("Message shouldn't be empty");
            return false;
        }else if (imageUri == null){
            showMessage("Pick Image");
            return false;
        }
        return true;
    }
    public void showMessage(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @SuppressLint("NewApi")
    private String getPath(Uri uri) throws URISyntaxException {
        final boolean needToCheckUri = Build.VERSION.SDK_INT >= 19;
        String selection = null;
        String[] selectionArgs = null;
        // Uri is different in versions after KITKAT (Android 4.4), we need to
        // deal with different Uris.
        if (needToCheckUri && DocumentsContract.isDocumentUri(getApplicationContext(), uri)) {
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                return Environment.getExternalStorageDirectory() + "/" + split[1];
            } else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                uri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
            } else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("image".equals(type)) {
                    uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                selection = "_id=?";
                selectionArgs = new String[]{
                        split[1]
                };
            }
        }
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = {
                    MediaStore.Images.Media.DATA
            };
            Cursor cursor = null;
            try {
                cursor = getContentResolver()
                        .query(uri, projection, selection, selectionArgs, null);
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }


    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }


    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }


    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }



}