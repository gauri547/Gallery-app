package com.streamliners.galleryapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.palette.graphics.Palette;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.streamliners.galleryapp.databinding.ItemCardBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ItemHelper {

    private OnCompleteListener listener;
    private String rectangularImageUrl = "https://picsum.photos/%d/%d";
    private String squareImageUrl = "https://picsum.photos/%d";
    private Context context;
    private Set<Integer> colors;
    private Bitmap bitmap;
    private List<String> labels;
    private String url;

    /**
     * Fetch rectangular random image
     *
     * @param x        Height of image
     * @param y        Width of image
     * @param context  Activity state
     * @param listener Complete event handler
     */
    public void fetchData(int x, int y, Context context, OnCompleteListener listener) {
        this.context = context;
        this.listener = listener;
        Log.d("Abhi", "fetchImage: " + String.format(rectangularImageUrl, x, y));
        //fetch rectangular image
        fetchImage(
                String.format(rectangularImageUrl, x, y));


    }

    /**
     * Fetch square random image
     *
     * @param x        Height and Width of image
     * @param context  Activity state
     * @param listener Complete event handler
     */
    public void fetchData(int x, Context context, OnCompleteListener listener) {
        this.context = context;
        this.listener = listener;

        //fetch square image
        fetchImage(
                String.format(squareImageUrl, x));
    }

    /**
     * Fetch Random image
     *
     * @param url Random Image URl
     */
    private void fetchImage(String url) {

        //fetch Redirect Url
        new RedirectURLHelper(new RedirectURLHelper.OnCompleteListener() {
            @Override
            public void fetchRedirectUrl(String redirectUrl) {
                ItemHelper.this.url = redirectUrl;

                //Fetch image by using glide library
                Glide.with(context)
                        .asBitmap()
                        .load(redirectUrl)
                        .into(new CustomTarget<Bitmap>() {
                            //On image successfully fetch
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {

                                bitmap = resource;

                                //Get colors from image
                                extraPaletteFromBitmap();
                            }

                            @Override
                            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                                super.onLoadFailed(errorDrawable);

                                //call onComplete listener
                                listener.onError("Image load failed");
                            }

                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {

                            }

                        });
            }

            @Override
            public void OnFail() {
                listener.onError("Image load failed");
            }
        }).execute(url);

    }

    /**
     * @param url      Image Url
     * @param context  Activity state
     * @param listener Complete event handler
     */
    public void editImage(String url, Context context, OnCompleteListener listener) {
        this.context = context;
        this.url = url;
        this.listener = listener;
        Glide.with(context)
                .asBitmap()
                .onlyRetrieveFromCache(true)
                .load(url)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        bitmap = resource;
                        extraPaletteFromBitmap();
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                });
    }


    /**
     * @param path     Image path
     * @param context  Activity state
     * @param listener Complete event handler
     */
    public void fetchImageFromGallery(String path, Context context, OnCompleteListener listener) {
        this.context = context;
        this.url = path;
        this.listener = listener;

        Glide.with(context)
                .asBitmap()
                .load(url)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        bitmap = resource;
                        extraPaletteFromBitmap();
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                });
    }

    /**
     * Fetch colors from image by using Palette library
     */
    private void extraPaletteFromBitmap() {
        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
            public void onGenerated(Palette p) {

                //call getColorsFromPalette function
                colors = getColorsFromPalette(p);

                //Get label from image
                labelImage();
            }
        });
    }

    /**
     * Fetch labels from image by using mlkit image-labeling library
     */
    private void labelImage() {
        InputImage inputImage = InputImage.fromBitmap(bitmap, 0);
        ImageLabeler labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS);

        //Get labels
        labeler.process(inputImage)
                .addOnSuccessListener(new OnSuccessListener<List<ImageLabel>>() {
                    @Override
                    public void onSuccess(List<ImageLabel> labels) {
                        ItemHelper.this.labels = new ArrayList<>();

                        //store labels in List<String> labels
                        for (ImageLabel imageLabel : labels) {
                            ItemHelper.this.labels.add(imageLabel.getText());
                        }
                        //call onComplete listener
                        listener.onFetch(bitmap, colors, ItemHelper.this.labels, url);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //call onComplete listener
                        listener.onError(e.toString());
                    }
                });
    }

    /**
     * @param p Palette of image
     * @return set of colors
     */
    private Set<Integer> getColorsFromPalette(Palette p) {
        Set<Integer> colors = new HashSet<>();

        //Store colors in Set<Integer> colors
        //Vibrant colors
        colors.add(p.getVibrantColor(0));
        colors.add(p.getLightVibrantColor(0));
        colors.add(p.getDarkVibrantColor(0));
        //Muted colors
        colors.add(p.getMutedColor(0));
        colors.add(p.getLightMutedColor(0));
        colors.add(p.getDarkMutedColor(0));

        //Remove black color
        colors.remove(0);

        return colors;

    }

    /**
     * Interface call when a image is fetch or give some error.
     */
    interface OnCompleteListener {
        /**
         * Call when image all data fetch completely
         *
         * @param bitmap       Image
         * @param colorPalette Image colors
         * @param labels       Image labels
         * @param url          Image url
         */
        void onFetch(Bitmap bitmap, Set<Integer> colorPalette, List<String> labels, String url);

        /**
         * Call when error come
         *
         * @param exception Error
         */
        void onError(String exception);
    }

}
