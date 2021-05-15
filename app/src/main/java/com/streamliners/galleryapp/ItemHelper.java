package com.streamliners.galleryapp;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ItemHelper {
    private Context context;
    private OnCompleteListener listener;

    private String rectangularImageUrl = "https://picsum.photos/%d/%d"
            , squareImageUrl = "https://picsum.photos/%d";

    private Bitmap bitmap;
    private Set<Integer> colors;



    /**
     * Fetch Data for Rectangular Image
     * @param x
     * @param y
     * @param context
     * @param listener
     */
    public void fetchData(int x, int y, Context context, OnCompleteListener listener) {
        this.context = context;
        this.listener = listener;
        fetchImage(
                String.format(rectangularImageUrl, x, y)
        );
    }


    /**
     * Fetch data for Square Image
     * @param x
     * @param context
     * @param listener
     */
    public void fetchData(int x, Context context, OnCompleteListener listener) {
        this.context = context;
        this.listener = listener;
        fetchImage(
                String.format(squareImageUrl, x)
        );
    }


    /**
     * Fetches image from URL
     * @param url
     */
    void fetchImage(String url) {

        Glide.with(context)
                .asBitmap()
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull @org.jetbrains.annotations.NotNull Bitmap resource, @Nullable @org.jetbrains.annotations.Nullable Transition<? super Bitmap> transition) {
                        bitmap = resource;
                        extractPaletteFromBitmap();
                    }

                    @Override
                    public void onLoadCleared(@Nullable @org.jetbrains.annotations.Nullable Drawable placeholder) {

                    }
                });
    }


    /**
     * Extract Color Palettes from Image(Bitmap)
     */
    private void extractPaletteFromBitmap() {
        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(Palette palette) {
                colors = getColorsFromPalette(palette);

                extractLabels();
            }
        });
    }


    /**
     * Fetches colors from Palette
     * @param palette
     * @return
     */
    private Set<Integer> getColorsFromPalette (Palette palette) {
        Set<Integer> colors = new HashSet<>();
        colors.add(palette.getVibrantColor(0));
        colors.add(palette.getLightVibrantColor(0));
        colors.add(palette.getDarkVibrantColor(0));

        colors.add(palette.getMutedColor(0));
        colors.add(palette.getLightMutedColor(0));
        colors.add(palette.getDarkMutedColor(0));

        colors.add(palette.getVibrantColor(0));
        colors.remove(0);

        return colors;
    }


    /**
     * Extracts labels from Image
     */
    private void extractLabels() {
        InputImage image = InputImage.fromBitmap(bitmap, 0);
        ImageLabeler labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS);
        labeler.process(image)
                .addOnSuccessListener(new OnSuccessListener<List<ImageLabel>>() {
                    @Override
                    public void onSuccess(@NonNull @NotNull List<ImageLabel> imageLabels) {
                        List<String> strings = new ArrayList<>();
                        for (ImageLabel label : imageLabels){
                            strings.add(label.getText());
                        }
                        listener.onFetchedData(bitmap, colors, strings);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull  Exception e) {
                        listener.onError(e.toString());
                    }
                });
    }

    /**
     * Callback when image data is fetched
     */
    interface OnCompleteListener{
        void onFetchedData(Bitmap image, Set<Integer> colors, List<String> labels);
        void onError(String error);
    }

}