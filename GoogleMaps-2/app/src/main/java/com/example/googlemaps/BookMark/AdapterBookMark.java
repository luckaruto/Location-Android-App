package com.example.googlemaps.BookMark;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.googlemaps.InfoPlace.FragmentInfo;
import com.example.googlemaps.InfoPlace.infoPlace;
import com.example.googlemaps.InfoSearching;
import com.example.googlemaps.MapsActivity;
import com.example.googlemaps.PlaceLabeling.ReloadActivity;
import com.example.googlemaps.R;

import java.io.UnsupportedEncodingException;
import java.util.List;

public class AdapterBookMark extends ArrayAdapter<infoPlace> {

    Context context;
    List<infoPlace> data;
    ReloadActivity reloadActivity;

    public AdapterBookMark(@NonNull Context context, List<infoPlace> list, ReloadActivity reloadActivity) {
        super(context,0, list);
        this.context = context;
        this.data = list;
        this.reloadActivity = reloadActivity;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if(convertView == null){
            convertView = LayoutInflater.from(this.context).inflate(R.layout.list_items_bookmark,parent,false);
        }

        infoPlace ip = data.get(position);

        TextView name = convertView.findViewById(R.id.name);
        name.setText(ip.name);

        TextView address = convertView.findViewById(R.id.address);
        address.setText(ip.address);

        TextView rating = convertView.findViewById(R.id.rating);
        rating.setText("Rating: "+ip.rating);

        if(ip.rating == "null" ){
            rating.setVisibility(View.GONE);
            RatingBar ratingBar = convertView.findViewById(R.id.starRating);
            ratingBar.setVisibility(View.GONE);
        }else{
            RatingBar ratingBar = convertView.findViewById(R.id.starRating);
            ratingBar.setRating(Float.parseFloat(ip.rating));
        }

        RecyclerView listViewImage = convertView.findViewById(R.id.listViewImage);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        listViewImage.setLayoutManager(linearLayoutManager);
        AdapterListImage al = new AdapterListImage(context, ip.metadataList);
        listViewImage.setAdapter(al);


        ImageView delete = convertView.findViewById(R.id.delete);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Xác nhận xóa!");
                builder.setMessage("Bạn có chắc chắn muốn xóa địa điểm này?");
                builder.setPositiveButton("Có", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        SQLiteDatabase db = context.openOrCreateDatabase("myDB", Context.MODE_PRIVATE,null);
                        WorkWithSQLiteBookMark workWithSQLiteBookMark = new WorkWithSQLiteBookMark(db,"Luu");
                        workWithSQLiteBookMark.deleteRow(ip.name, ip.address);

                        reloadActivity.reloadActivity();
                    }
                });

                builder.setNegativeButton("Không",null);
                builder.show();
            }
        });

        AppCompatButton btnDirection = convertView.findViewById(R.id.direction);
        btnDirection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, MapsActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("fromBookMark", ip.address);
                intent.putExtras(bundle);
                context.startActivity(intent);
            }
        });

        AppCompatButton btnShare = convertView.findViewById(R.id.share);
        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    FragmentInfo.shareLocation(context,ip.address);
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            }
        });





        return convertView;
    }


}
