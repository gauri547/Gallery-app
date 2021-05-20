package com.streamliners.galleryapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.streamliners.galleryapp.databinding.ActivityGalleryBinding;
import com.streamliners.galleryapp.databinding.ItemCardBinding;
import com.streamliners.galleryapp.models.Item;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class GalleryActivity extends AppCompatActivity {
    private static final int REQUEST_READ_EXTERNAL_STORAGE = 0;
    ActivityGalleryBinding b;
    List<Item> itemList;
    int selectedPosition;
    List<Item> removeItem;
    private boolean isEdited;
    private boolean isAdd;
    int noOfImages = 0;
    private boolean isUpdate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityGalleryBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());


        //Load data from sharedPreferences
        loadSharedPreferenceData();
    }

    /**
     * Load data from sharedPreferences
     * Fetch Images from caches
     */
    private void loadSharedPreferenceData() {
        String items = getPreferences(MODE_PRIVATE).getString("ITEMS", null);
        if (items == null || items.equals("[]")) {
            return;
        }
        b.heading.setVisibility(View.GONE);
        Gson gson = new Gson();
        Type type = new TypeToken<List<Item>>() {
        }.getType();

        itemList = gson.fromJson(items, type);

        //Fetch data from caches
        for (Item item : itemList) {
            ItemCardBinding binding = ItemCardBinding.inflate(getLayoutInflater());

            Glide.with(this)
                    .asBitmap()
                    .load(item.url)
                    .into(binding.fetchImage);

            binding.Title.setBackgroundColor(item.color);
            binding.Title.setText(item.label);

            b.linearLayout.addView(binding.getRoot());
            setupContextMenu(binding,b.linearLayout.getChildCount()-1);
        }
        noOfImages = itemList.size();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gallery, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.downloadImage) {
            showAddImageDialog();
            return true;
        }
        else if(item.getItemId()==R.id.addImage){
            addImgFromFile();
        }
        return false;
    }

    private void addImgFromFile() {
        if(!checkPermission()){
            return;
        }
        CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).start(this);

    }
    /**
     * Check Storage permission
     * @return Permission Granted or not
     */
    private boolean checkPermission() {
        if(ActivityCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},REQUEST_READ_EXTERNAL_STORAGE);

            return false;
        }
        return true;
    }
    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item){
        AdapterView.AdapterContextMenuInfo info=(AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch(item.getItemId()){
            case R.id.edit:
                editImage();
                return true;
            case R.id.share:
                shareImage();
                return true;
        }
        return super.onContextItemSelected(item);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode==REQUEST_READ_EXTERNAL_STORAGE) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).start(this);
            }
        }
    }


    /**
     * Share Image
     */
    private void shareImage() {

        //get Image from ImageView
        ImageView imageView = b.linearLayout.getChildAt(selectedPosition).findViewById(R.id.fetchImage);
        Bitmap bm = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        Uri uri = getImageToShare(bm);
        Intent intent = new Intent(Intent.ACTION_SEND);
        // putting uri of image to be shared
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        // setting type to image
        intent.setType("image/jpeg");
        startActivity(Intent.createChooser(intent, "Share Via"));
    }

    private void setupContextMenu(ItemCardBinding binding,int position){
        binding.cardView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                v.getId();
                getMenuInflater().inflate(R.menu.context_menu,menu);
                selectedPosition=position;
            }
        });

    }
    private void editImage(){
        new AddImageDialog().editFetchImage(this, itemList.get(selectedPosition), new AddImageDialog.OnCompleteListener() {
            @Override
            public void onImageAdd(Item item) {
                TextView tv=b.linearLayout.getChildAt(selectedPosition).findViewById(R.id.Title);
                tv.setText(item.label);
                tv.setBackgroundColor(item.color);
                itemList.set(selectedPosition,new Item(item.color,item.label,item.url));
                isEdited=true;
            }

            @Override
            public void onError(String error) {

            }
        });
    }
    /**
     * To show the dialog to add image
     */
    private void showAddImageDialog() {

        new AddImageDialog()
                .showDialog(this, new AddImageDialog.OnCompleteListener() {
                    @Override
                    public void onImageAdd(Item item) {
                        inflateViewForItem(item);
                    }

                    @Override
                    public void onError(String error) {
                        new MaterialAlertDialogBuilder(GalleryActivity.this)
                                .setTitle("Error")
                                .setMessage(error)
                                .show();

                    }
                });
    }
    /**
     * Retrieving the url to share
     */
    private Uri getImageToShare(Bitmap  bitmap) {
        File gallery = new File(getCacheDir(), "images");
        Uri uri = null;
        try {
            gallery.mkdirs();
            File file = new File(gallery, "shared_image.png");
            FileOutputStream outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
            outputStream.flush();
            outputStream.close();
            uri = FileProvider.getUriForFile(this, "com.anni.shareimage.fileprovider", file);
        } catch (Exception e) {

        }
        return uri;
    }

    /**
     * To inflate the view for the incoming item
     */
    private void inflateViewForItem(Item item) {

        if (noOfImages == 0) {
            b.heading.setVisibility(View.GONE);
        }
        //Inflate layout
        ItemCardBinding binding = ItemCardBinding.inflate(getLayoutInflater());

        //Bind data
        binding.fetchImage.setImageBitmap(item.image);
        binding.Title.setBackgroundColor(item.color);
        binding.Title.setText(item.label);
        b.linearLayout.addView(binding.getRoot());

        //Add Item
        Item newItem = new Item(item.color, item.label, item.url);
        if (itemList == null) {
            itemList = new ArrayList<>();
        }
        itemList.add(newItem);
        isAdd = true;
        setupContextMenu(binding,b.linearLayout.getChildCount()-1);
        noOfImages++;
    }
    @Override
    protected void onPause() {
        super.onPause();

        //Remove Item and save
        if (removeItem != null) {
            itemList.removeAll(removeItem);

            Gson gson = new Gson();
            String json = gson.toJson(itemList);

            getPreferences(MODE_PRIVATE).edit().putString("ITEMS", json).apply();

            isUpdate=true;
            isAdd = false;
            isEdited = false;
        }

        //save in SharedPreference
        if (isEdited || isAdd) {
            Gson gson = new Gson();
            String json = gson.toJson(itemList);
            getPreferences(MODE_PRIVATE).edit().putString("ITEMS", json).apply();
            isAdd = false;
            isEdited = false;
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE&&resultCode == RESULT_OK) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            Uri resultUri = result.getUri();

            new AddImageDialog().fetchImgFromAlbum(this, resultUri.getPath(), new AddImageDialog.OnCompleteListener() {
                @Override
                public void onImageAdd(Item item) {
                    inflateViewForItem(item);
                }

                @Override
                public void onError(String error) {

                }
            });

        }
    }
}

