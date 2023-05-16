package com.example.googlemaps.BookMark;

import static com.example.googlemaps.MapsActivity.frameLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.googlemaps.InfoPlace.infoPlace;
import com.example.googlemaps.InfoSearching;
import com.example.googlemaps.PlaceLabeling.ReloadActivity;
import com.example.googlemaps.R;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPhotoResponse;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BookMarkActivity extends AppCompatActivity implements ReloadActivity {

    SQLiteDatabase db;
    List<InfoSearching> list;
    List<infoPlace> data;

    ListView listViewBookMark;

    ImageView image;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_mark);

        getSupportActionBar().hide();

        listViewBookMark = findViewById(R.id.listViewBookMark);

        list = new ArrayList<>();
        data = new ArrayList<>();

        db = this.openOrCreateDatabase("myDB",MODE_PRIVATE, null);
        Cursor cursor = db.rawQuery("select * from Luu",null);
        if(cursor.moveToFirst()){
            do{
                @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex("name"));
                @SuppressLint("Range") String address = cursor.getString(cursor.getColumnIndex("address"));

                InfoSearching is = new InfoSearching(name,address,0);
                list.add(is);


            }while (cursor.moveToNext());
        }

        PlacesClient placesClient = Places.createClient(this);


        for(int i = 0; i < list.size(); i++){
            // lấy place id thông qua autoComplete
            FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                    .setQuery(list.get(i).getAddress()) // Tìm kiếm địa điểm theo tên
                    .build();
            // Thực hiện truy vấn
            Task<FindAutocompletePredictionsResponse> task = placesClient.findAutocompletePredictions(request);

            // Xử lý kết quả trả về
            task.addOnSuccessListener((response) -> {
                if (!response.getAutocompletePredictions().isEmpty()) {
                    // Lấy Place ID của địa điểm đầu tiên
                    String placeId = response.getAutocompletePredictions().get(0).getPlaceId();
                    Log.e("Place ID", placeId);

                    List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.RATING, Place.Field.PHOTO_METADATAS);
                    FetchPlaceRequest request2 = FetchPlaceRequest.builder(placeId, placeFields).build();

                    // Gửi yêu cầu truy vấn địa điểm và xử lý kết quả trả về
                    placesClient.fetchPlace(request2).addOnSuccessListener((response2) -> {
                        Place place = response2.getPlace();


                        // gửi infoPlace qua cho fragment khi click vào marker
                        infoPlace infoPlace = new infoPlace(place.getName(),response.getAutocompletePredictions().get(0).getFullText(null).toString(),
                                String.valueOf(place.getRating()),place.getLatLng());

                        infoPlace.metadataList = place.getPhotoMetadatas();
                        data.add(infoPlace);




                        // kiểm tra nếu danh sách ban đầu = danh sách đã lưu vào
                        if(list.size() == data.size()){

                            AdapterBookMark ab = new AdapterBookMark(this,data,this::reloadActivity);
                            listViewBookMark.setAdapter(ab);

                        }

                    }).addOnFailureListener((exception) -> {
                        if (exception instanceof ApiException) {
                            ApiException apiException = (ApiException) exception;
                            int statusCode = apiException.getStatusCode();
                            Log.e("TAG", "Place not found: " + exception.getMessage());
                            Log.e("TAG", "Status code: " + statusCode);
                        }
                    });

                }
            }).addOnFailureListener((exception) -> {
                Log.e("Error", "Error getting autocomplete predictions", exception);
            });


        }




//        Toast.makeText(this, data.get(0).address, Toast.LENGTH_SHORT).show();
    }

    public void backActivity(View view){
        onBackPressed();
    }


    @Override
    public void reloadActivity() {
        recreate();
    }
}