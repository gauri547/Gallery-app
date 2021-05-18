package com.streamliners.galleryapp;

import androidx.annotation.NonNull;

import androidx.appcompat.app.AppCompatActivity;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.streamliners.galleryapp.databinding.ActivityGalleryBinding;
import com.streamliners.galleryapp.databinding.ItemCardBinding;
import com.streamliners.galleryapp.models.Item;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;


public class GalleryActivity extends AppCompatActivity {
    ActivityGalleryBinding b;
    List<Item> itemList;
    int selectedPosition;
    List<Item> removeItem;
    private boolean isEdited;
    private boolean isAdd;
    int noOfImages = 0;
    // Shared preferences
    SharedPreferences preferences;

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
        Log.d("Now", "loadSharedPreferenceData: " + items);
        Gson gson = new Gson();
        Type type = new TypeToken<List<Item>>() {
        }.getType();

        itemList = gson.fromJson(items, type);

        //Fetch data from caches
        for (Item item : itemList) {
            ItemCardBinding binding = ItemCardBinding.inflate(getLayoutInflater());

            Glide.with(this)
                    .asBitmap()
                    .onlyRetrieveFromCache(true)
                    .load(item.url)
                    .into(binding.fetchImage);

            binding.Title.setBackgroundColor(item.color);
            binding.Title.setText(item.label);

            Log.d("Now", "onResourceReady: " + item.label);

            b.linearLayout.addView(binding.getRoot());



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
        if (item.getItemId() == R.id.addImage) {
            showAddImageDialog();
            return true;
        }
        return false;
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
     * To inflate the view for the incoming item
     *
     * @param item {@link Item}
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

            finish();
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


}






