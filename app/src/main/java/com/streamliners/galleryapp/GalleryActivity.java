package com.streamliners.galleryapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class GalleryActivity extends AppCompatActivity {
    ActivityGalleryBinding b;
    SharedPreferences preferences;
    List<Item> items = new ArrayList<>();
    private boolean isDialogBoxShowed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityGalleryBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());


        preferences = getPreferences(MODE_PRIVATE);
        inflateDataFromSharedPreferences();
    }


    /**
     * Gives Add Image Option in menu
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gallery, menu);
        return true;
    }


    /**
     * Shows add image dialog on clicking icon in menu
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.addImage){
            showAddImageDialog();
            return true;
        }
        return false;
    }


    /**
     * Shows Image Dialog Box
     */
    private void showAddImageDialog() {
        if (this.getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            isDialogBoxShowed = true;
            // To set the screen orientation in portrait mode only
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        new AddImageDialog()
                .show(this, new AddImageDialog.onCompleteListener() {
                    @Override
                    public void onImageAdded(Item item) {
                        items.add(item);
                        inflateViewforItem(item);
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
     * Adds Image Card to Linear Layout
     * @param item
     */
    private void inflateViewforItem(Item item) {

        //Inflate Layout
        ItemCardBinding binding = ItemCardBinding.inflate(getLayoutInflater());
        //Bind Data
        binding.imageView.setImageBitmap(item.image);
        binding.title.setText(item.label);
        binding.title.setBackgroundColor(item.color);

        //Add it to the list
        b.list.addView(binding.getRoot());
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
    }

    private String stringFromBitmap(Bitmap bitmap){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos); //bm is the bitmap object
        byte[] b = baos.toByteArray();

        String encodedImage = Base64.encodeToString(b, Base64.DEFAULT);
        return encodedImage;
    }

    private Bitmap bitmapFromString(String str){
        byte [] encodeByte=Base64.decode(str,Base64.DEFAULT);

        InputStream inputStream  = new ByteArrayInputStream(encodeByte);
        Bitmap bitmap  = BitmapFactory.decodeStream(inputStream);
        return bitmap;
    }

    @Override
    protected void onPause() {
        super.onPause();

        SharedPreferences.Editor myEdit = preferences.edit();

        int numOfImg = items.size();
        myEdit.putInt(Constants.NUMOFIMG, numOfImg).apply();

        int counter = 0;
        for (Item item : items){
            myEdit.putInt(Constants.COLOR + counter, item.color)
                    .putString(Constants.LABEL + counter, item.label)
                    .putString(Constants.IMAGE + counter, stringFromBitmap(item.image))
                    .apply();
            counter++;
        }
        myEdit.commit();
    }

    private void inflateDataFromSharedPreferences(){
        int itemCount = preferences.getInt(Constants.NUMOFIMG,0);

        // Inflate all items from shared preferences
        for (int i = 0; i < itemCount; i++){
            Item item = new Item(bitmapFromString(preferences.getString(Constants.IMAGE + i,""))
                    ,preferences.getInt(Constants.COLOR + i,0)
                    ,preferences.getString(Constants.LABEL + i,""));

            items.add(item);
            inflateViewforItem(item);
        }
    }
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }
}











