package com.example.googlemaps.BookMark;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.googlemaps.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPhotoResponse;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.List;

public class AdapterListImage extends RecyclerView.Adapter<AdapterListImage.MyViewHolder> {

    public static Context context;
    List<PhotoMetadata> metadataList;

    public AdapterListImage(Context context, List<PhotoMetadata> metadataList){
        this.metadataList = metadataList;
        this.context = context;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageView;

        public MyViewHolder(View v) {
            super(v);
            imageView = v.findViewById(R.id.image);

        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.list_imageview_bookmark,parent,false);
        MyViewHolder myViewHolder = new MyViewHolder(v);
        return myViewHolder;

    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {


        PhotoMetadata metadata = metadataList.get(position);

//        holder.imageView.setMinimumWidth(metadata.getWidth());
//        holder.imageView.setMinimumHeight(metadata.getHeight());
//        holder.imageView.setMaxWidth(metadata.getWidth());
//        holder.imageView.setMaxHeight(metadata.getHeight());

        PlacesClient placesClient = Places.createClient(context);

        final FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(metadata)
                .setMaxWidth(300)
                .setMaxHeight(200)
                .build();

        placesClient.fetchPhoto(photoRequest).addOnSuccessListener(new OnSuccessListener<FetchPhotoResponse>() {
            @Override
            public void onSuccess(FetchPhotoResponse fetchPhotoResponse) {
                Bitmap bitmap = fetchPhotoResponse.getBitmap();
                holder.imageView.setImageBitmap(bitmap);

            }
        });



    }

    @Override
    public int getItemCount() {
        if(metadataList == null){
            return 0;
        }
        return metadataList.size();
    }



}