package com.example.googlemaps;


import static android.app.PendingIntent.getActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import com.example.googlemaps.Adapter.AutoCompleteAdapter;
import com.example.googlemaps.BookMark.BookMarkActivity;
import com.example.googlemaps.BookMark.WorkWithSQLiteBookMark;
import com.example.googlemaps.Fragments.UserListFragment;

import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.googlemaps.Adapter.AutoCompleteAdapter;
import com.example.googlemaps.Adapter.HistoryAdapter;
import com.example.googlemaps.Direction.DIrectionListener;
import com.example.googlemaps.Direction.DirectionFinder;
import com.example.googlemaps.Direction.Route;
import com.example.googlemaps.InfoPlace.FragmentInfo;
import com.example.googlemaps.InfoPlace.MainCallbacks;
import com.example.googlemaps.InfoPlace.infoPlace;

import com.example.googlemaps.PlaceLabeling.LabelingActivity;
import com.example.googlemaps.PlaceLabeling.ListLabelAdapterForMap;
import com.example.googlemaps.PlaceLabeling.ListLabelAdapter;
import com.example.googlemaps.PlaceLabeling.ListLabelAdapterForMap;
import com.example.googlemaps.PlaceLabeling.WorkWithSQLiteLabel;
import com.example.googlemaps.Walking.WalkingActivity;
import com.example.googlemaps.databinding.ActivityMapsBinding;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.auth.FirebaseAuth;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;



public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, DIrectionListener, MainCallbacks {

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int REQUEST_CODE_LOCATION = 1234;

    private List<Intent> intentList;

    private FusedLocationProviderClient fusedLocationProviderClient;

    //var
    private boolean markerSearchCheckDest = false;
    private Marker markerSearchDest;

    private boolean isMarkerSearchCheckOrigin = false;

    private Marker markerSearchOrigin;

    //private Polyline poly = null;
    private List<Polyline> polyPaths = new ArrayList<>();

    private static boolean checkPermissionLocation = false;

    private GoogleMap mMap;
    private ActivityMapsBinding binding;

    public static LatLng myLocation = null;
    public static LatLng originLocation = null;
    public static LatLng destLocation = null;

    //widgets
    public static AutoCompleteTextView textSearch;
    public static AutoCompleteTextView textSearchOrigin;
    public static AutoCompleteTextView textSearchDest;

    private static ImageView imgSearch;
    private static ImageView imgBackSearch;

    private static ImageView imgSearchOrigin;
    private static ImageView imgBackSearchOrigin;

    private static ImageView imgSearchDest;
    private static ImageView imgBackSearchDest;

    private static ImageView clearText;
    private static ImageView clearText1;
    private static ImageView clearText2;
    public static ListView listViewSearch;
    public static RecyclerView listViewLabel;
    public static LinearLayout history;
    public static ImageView visibility;
    private static ImageView visibility_off;


    public static TextView timeCar;
    public static TextView timeMoto;
    public static TextView timeBus;
    public static TextView timeWalk;
    private static ImageView imgGps;

    private ImageView imgBookMark;


    public static SQLiteDatabase db;

    FragmentTransaction ft;
    FragmentInfo fi;
    public static FrameLayout frameLayout;

    public static LinearLayout linearLayout1;
    public static LinearLayout linearLayout2;
    public static LinearLayout yourLocation;

    public View viewModeTraffic;

    public String trafficMode;
    com.example.googlemaps.InfoPlace.infoPlace infoPlace;


    //places
    PlacesClient placesClient;


    //token




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(),getString(R.string.API_KEY));
        }

        setTitle("Map view");

        // tạo hoặc mở database
        db = this.openOrCreateDatabase("myDB",MODE_PRIVATE,null);

        //db.execSQL(" DROP TABLE IF EXISTS LichSu;");

        //Tạo bảng lịch sử nếu chưa tồn tại
        if(!checkIfTableExists(db, "LichSu")){
            db.execSQL("create table LichSu ("
                                    +"id integer PRIMARY KEY autoincrement,"
                                    +"name text,"
                                    +"address text);"
            );
        }

        // tạo bảng Nhãn nếu chưa tồn tại
        WorkWithSQLiteLabel workWithSQLiteLabel = new WorkWithSQLiteLabel(db, "Nhan");
        workWithSQLiteLabel.createTblLabel();

        // tạo bảng Lưu nếu chưa tồn tại
        WorkWithSQLiteBookMark workWithSQLiteBookMark = new WorkWithSQLiteBookMark(db,"Luu");
        workWithSQLiteBookMark.createTable();


        placesClient = Places.createClient(this);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        imgSearch = findViewById(R.id.iconSearch);
        imgBackSearch = findViewById(R.id.iconBackSearch);
        imgBackSearch.setVisibility(View.GONE);

        imgSearchOrigin = findViewById(R.id.iconSearchOrigin);
        imgBackSearchOrigin = findViewById(R.id.iconBackSearchOrigin);
        imgBackSearchOrigin.setVisibility(View.GONE);

        imgSearchDest = findViewById(R.id.iconSearchDest);
        imgBackSearchDest = findViewById(R.id.iconBackSearchDest);
        imgBackSearchDest.setVisibility(View.GONE);

        // autoComplete code
        textSearch = findViewById(R.id.textSearch);
        textSearchOrigin = findViewById(R.id.textSearchOrigin);
        textSearchDest = findViewById(R.id.textSearchDest);


        clearText = findViewById(R.id.clearText);
        clearText.setVisibility(View.GONE);


        clearText1 = findViewById(R.id.clearText1);
        clearText1.setVisibility(View.GONE);


        clearText2 = findViewById(R.id.clearText2);
        clearText2.setVisibility(View.GONE);

        listViewSearch = findViewById(R.id.listViewSearch);
        listViewSearch.setVisibility(View.GONE);

        listViewLabel = findViewById(R.id.listViewLabel);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        listViewLabel.setLayoutManager(linearLayoutManager);
        listViewLabel.setVisibility(View.INVISIBLE);

        history = findViewById(R.id.history);
        history.setVisibility(View.GONE);


        linearLayout1 = findViewById(R.id.relative1);
        linearLayout2 = findViewById(R.id.relative2);
        linearLayout2.setVisibility(View.GONE);

        timeCar = findViewById(R.id.timeCar);
        timeMoto = findViewById(R.id.timeMoto);
        timeBus = findViewById(R.id.timeBus);
        timeWalk = findViewById(R.id.timeWalk);

        yourLocation = findViewById(R.id.yourLocation);
        yourLocation.setVisibility(View.GONE);
        // thiết lập adapter

        // check khi ô search thay đổi text
        textSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence c, int i, int i1, int i2) {
                // khi người dùng nhập hoặc thay đổi từ khóa, autoComplete sẽ hiện ra
                if(c.length() > 0){
                    autoCompleteSearch(textSearch,c.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        textSearch.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                InfoSearching infoSearching = (InfoSearching)adapterView.getItemAtPosition(i);
                textSearch.setText(infoSearching.getAddress());

                //tìm kiếm dựa trên thông tin của ô search
                geoLocate(textSearch,"dest");
                invisibleKeyBoard(textSearch);
            }
        });

        textSearch.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {

                showHistory(textSearch,hasFocus);
                convertSearchAndBackSearch(textSearch,hasFocus);


            }


        });

        textSearchOrigin.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence c, int i, int i1, int i2) {
                // khi người dùng nhập hoặc thay đổi từ khóa, autoComplete sẽ hiện ra
                if(c.length() > 0){
                    autoCompleteSearch(textSearchOrigin,c.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        textSearchOrigin.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                InfoSearching infoSearching = (InfoSearching)adapterView.getItemAtPosition(i);
                textSearchOrigin.setText(infoSearching.getAddress());
                if(isMarkerSearchCheckOrigin){
                    markerSearchOrigin.setVisible(true);
                }

                //tìm kiếm dựa trên thông tin của ô search
                geoLocate(textSearchOrigin, "origin");
                onClickTrafficMode(viewModeTraffic);
                invisibleKeyBoard(textSearchOrigin);
            }
        });

        textSearchOrigin.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {

                showHistory(textSearchOrigin,hasFocus);
                convertSearchAndBackSearch(textSearchOrigin,hasFocus);
                showYourLocation(textSearchOrigin,hasFocus);

            }
        });

        textSearchDest.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence c, int i, int i1, int i2) {
                // khi người dùng nhập hoặc thay đổi từ khóa, autoComplete sẽ hiện ra
                if(c.length() > 0){
                    autoCompleteSearch(textSearchDest,c.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        textSearchDest.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                InfoSearching infoSearching = (InfoSearching)adapterView.getItemAtPosition(i);
                textSearchDest.setText(infoSearching.getAddress());
                //tìm kiếm dựa trên thông tin của ô search
                geoLocate(textSearchDest, "dest");
                onClickTrafficMode(viewModeTraffic);
                invisibleKeyBoard(textSearchDest);
            }
        });

        textSearchDest.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {

                showHistory(textSearchDest,hasFocus);
                convertSearchAndBackSearch(textSearchDest,hasFocus);
                showYourLocation(textSearchDest,hasFocus);

            }
        });

        // Thiết lập
        imgGps = findViewById(R.id.gps);
        imgBookMark = findViewById(R.id.favourite);

        getPermissionFromUser();


        // tạo fragment info khi người dùng click vào địa điểm, thông tin sẽ hiện ra
        ft = getSupportFragmentManager().beginTransaction();
        fi = new FragmentInfo();
        ft.replace(R.id.fragmentInfo,fi);ft.commit();
//
        frameLayout = findViewById(R.id.fragmentInfo);
        frameLayout.setVisibility(View.GONE);



    }




    @Override
    protected void onPostResume() {
        super.onPostResume();
        // cập nhật dữ liệu cho label
        List<InfoSearching> listLabel = new ArrayList<>();
        if(checkIfTableExists(db,"Nhan")){
            Cursor cursor = db.rawQuery("SELECT * FROM Nhan", null);
            if(cursor.moveToFirst()){
                do{
                    @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex("name"));
                    @SuppressLint("Range") String address = cursor.getString(cursor.getColumnIndex("address"));
                    InfoSearching is = new InfoSearching(name,address,0);
                    listLabel.add(is);

                }while (cursor.moveToNext());
            }
            listLabel.add(new InfoSearching("Khác","Xem thêm",0));
        }
        ListLabelAdapterForMap lla = new ListLabelAdapterForMap(this,listLabel);
        listViewLabel.setAdapter(lla);
    }

    public void convertSearchAndBackSearch(AutoCompleteTextView view, boolean isFocus){
        if(view.getId() == textSearch.getId()){
            if(isFocus){
                imgSearch.setVisibility(View.GONE);
                imgBackSearch.setVisibility(View.VISIBLE);
            }else{
                imgSearch.setVisibility(View.VISIBLE);
                imgBackSearch.setVisibility(View.GONE);
            }
        }else if(view.getId() == textSearchOrigin.getId()){
            if(isFocus){
                imgSearchOrigin.setVisibility(View.GONE);
                imgBackSearchOrigin.setVisibility(View.VISIBLE);
            }else{
                imgSearchOrigin.setVisibility(View.VISIBLE);
                imgBackSearchOrigin.setVisibility(View.GONE);
            }
        }else{
            if(isFocus){
                imgSearchDest.setVisibility(View.GONE);
                imgBackSearchDest.setVisibility(View.VISIBLE);
            }else{
                imgSearchDest.setVisibility(View.VISIBLE);
                imgBackSearchDest.setVisibility(View.GONE);
            }
        }

    }

    public void showHistory(AutoCompleteTextView textView,boolean hasFocus){
        if(hasFocus){

            // phần lấy dữ liệu của nhãn từ sqlite, set adapter cho nó
            List<InfoSearching> listLabel = new ArrayList<>();
            if(checkIfTableExists(db,"Nhan")){
                Cursor cursor = db.rawQuery("SELECT * FROM Nhan", null);
                if(cursor.moveToFirst()){
                    do{
                        @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex("name"));
                        @SuppressLint("Range") String address = cursor.getString(cursor.getColumnIndex("address"));
                        InfoSearching is = new InfoSearching(name,address,0);
                        listLabel.add(is);

                    }while (cursor.moveToNext());
                }
            }
            ListLabelAdapterForMap lla = new ListLabelAdapterForMap(this,listLabel);
            listViewLabel.setAdapter(lla);
            listViewLabel.setVisibility(View.VISIBLE);


            // phần lấy dữ liệu của Lịch sử từ sqlite, set adapter cho nó
            List<InfoSearching> list = new ArrayList<>();
            int i = 0;
            if(checkIfTableExists(db,"LichSu")){
                Cursor cursor = db.rawQuery("SELECT * FROM LichSu", null);
                if(cursor.moveToFirst()){
                    do{
                        @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex("name"));
                        @SuppressLint("Range") String address = cursor.getString(cursor.getColumnIndex("address"));
                        InfoSearching is = new InfoSearching(name,address,0);
                        list.add(0,is);

                        i++;
                    }while (cursor.moveToNext());
                }
                listLabel.add(new InfoSearching("Khác","Xem thêm",0));
            }


            HistoryAdapter aa = new HistoryAdapter(getApplicationContext(),list);
            listViewSearch.setAdapter(aa);
            listViewSearch.setVisibility(View.VISIBLE);

            history.setVisibility(View.VISIBLE);

            // phần ẩn và hiện micro
            if(textView.getId() == textSearch.getId()){

                clearText.setVisibility(View.VISIBLE);
            }else if(textView.getId() == textSearchOrigin.getId()){

                clearText1.setVisibility(View.VISIBLE);
            }else{

                clearText2.setVisibility(View.VISIBLE);
            }



            listViewSearch.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    InfoSearching infoSearching = (InfoSearching)adapterView.getItemAtPosition(i);
                    textView.setText(infoSearching.getAddress());

                    //tìm kiếm dựa trên thông tin của ô search
                    if(textView.getId() == textSearchOrigin.getId()){
                        if(isMarkerSearchCheckOrigin){
                            markerSearchOrigin.setVisible(true);
                        }

                        //tìm kiếm dựa trên thông tin của ô search
                        geoLocate(textSearchOrigin, "origin");
                        onClickTrafficMode(viewModeTraffic);
                        invisibleKeyBoard(textSearchOrigin);
                    }else if(textView.getId() == textSearchDest.getId()){
                        geoLocate(textView,"dest");
                        invisibleKeyBoard(textView);
                        sendRequestFindPath(originLocation,destLocation,fi.drivingMode);
                    }
                    else{
                        geoLocate(textView,"dest");
                        invisibleKeyBoard(textView);
                    }

                }
            });

        }else{
            listViewLabel.setVisibility(View.INVISIBLE);
            listViewSearch.setVisibility(View.GONE);
            history.setVisibility(View.GONE);

            if(textView.getId() == textSearch.getId()){

                clearText.setVisibility(View.GONE);
            }else if(textView.getId() == textSearchOrigin.getId()){

                clearText1.setVisibility(View.GONE);
            }else{

                clearText2.setVisibility(View.GONE);
            }
        }
    }

    public void showYourLocation(AutoCompleteTextView textView,boolean hasFocus){
        if(hasFocus) {
            yourLocation.setVisibility(View.VISIBLE);
            yourLocation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    handleYourLocation(view);
                }
            });

        }else{
            yourLocation.setVisibility(View.GONE);
        }
    }
    private void deleteRowHistory(int id){
        db.delete("LichSu","id=?",new String[]{String.valueOf(id)});
    }

    private void updateHistory(){
        db.execSQL("UPDATE LichSu SET id = id - 1 WHERE id > 1");
    }

    private void addRowHistory(InfoSearching is){
        String query = "insert into LichSu(name, address) values (?,?)";
        db.execSQL(query, new Object[]{is.getName(), is.getAddress()});

        if(countRowHistory() > 10){
            deleteRowHistory(1);
            updateHistory();
        }
    }

    private int countRowHistory(){

        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM LichSu", null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();

        return count;

    }

    private void signOut(){
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#000000")));
        getMenuInflater().inflate(R.menu.menu_main, menu);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:{
                UserListFragment fragment =
                        (UserListFragment) getSupportFragmentManager().findFragmentByTag("User List");
                if(fragment != null){
                    if(fragment.isVisible()){
                        getSupportFragmentManager().popBackStack();
                        return true;
                    }
                }
                finish();
                return true;
            }
            case R.id.action_sign_out:{
                signOut();
                return true;
            }
            default:{
                return super.onOptionsItemSelected(item);
            }
        }

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        getDeviceLocation();

//        LatLng HoChiMinhLocation = new LatLng(10.823099, 106.629662);
//        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(HoChiMinhLocation,15f),10,null);

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {
                String TAG = "onMakerClick";


                String name = marker.getTitle();
                Log.e(TAG, "onMarkerClick: " + name );

                // lấy place id thông qua autoComplete
                FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                        .setQuery(name) // Tìm kiếm địa điểm theo tên
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
                            Log.e(TAG, "Place found: " + place.getName());
                            Log.e(TAG, "Address: " + response.getAutocompletePredictions().get(0).getFullText(null).toString());
                            Log.e(TAG, "LatLng: " + place.getLatLng());
                            Log.e(TAG, "Rating: " + place.getRating());

                            // gửi infoPlace qua cho fragment khi click vào marker
                            infoPlace = new infoPlace(place.getName(),response.getAutocompletePredictions().get(0).getFullText(null).toString(),
                                    String.valueOf(place.getRating()),place.getLatLng());
                            fi.onMsgFromMainToFrag(infoPlace);
                            frameLayout.setVisibility(View.VISIBLE);

//                            List<PhotoMetadata> photoMetadataList = place.getPhotoMetadatas();
//                            if (photoMetadataList != null && photoMetadataList.size() > 0) {
//                                PhotoMetadata photoMetadata = photoMetadataList.get(0);
//                                String photoUrl = photoMetadata.getAttributions();
//
//                                Log.e(TAG, "Photo URL: " + photoUrl);
//                            }


                        }).addOnFailureListener((exception) -> {
                            if (exception instanceof ApiException) {
                                ApiException apiException = (ApiException) exception;
                                int statusCode = apiException.getStatusCode();
                                Log.e(TAG, "Place not found: " + exception.getMessage());
                                Log.e(TAG, "Status code: " + statusCode);
                            }
                        });

                    }
                }).addOnFailureListener((exception) -> {
                    Log.e("Error", "Error getting autocomplete predictions", exception);
                });

                return true;
            }
        });

        if (checkPermissionLocation) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            // hiển thị vị trí hiện tại
            mMap.setMyLocationEnabled(true);

            // tắt nút quay về vị trí hiện tại
            mMap.getUiSettings().setMyLocationButtonEnabled(false);

            Search(textSearch,"dest");
            Search(textSearchOrigin,"origin");
            Search(textSearchDest,"dest");
        }

////////////////////////////////////////////////////////////////////////////////
        // ATTENTION: This was auto-generated to handle app links.
        // khi người dùng click vào đường link chia sẻ, mở ứng dụng vào thẳng activity map
        // sau đó activity sẽ tìm xem có uri được gửi hay không, nếu có thì thực hiện hiển thị
        // địa chỉ được chia sẻ

        Uri uri = getIntent().getData();
        if(uri != null){
            //Toast.makeText(this, "TestLink", Toast.LENGTH_SHORT).show();
            String query = uri.getQuery();
            String param[] = query.split("=");

            try {
                String decodeAddress = URLDecoder.decode(param[1],"UTF-8");
                decodeAddress.replace("%2B","+");
                textSearch.setText(decodeAddress);
                geoLocate(textSearch,"dest");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }

        }


        ////////////////////////////////////////////////////
        // Xem địa chỉ từ bookmark
        // BookMark gửi intent chứa địa chỉ qua map activity, nếu tìm thấy trong
        // intent có key = "fromBookMark" thì hiển thị địa điểm đó ra
        Bundle bundle = getIntent().getExtras();

        if(bundle != null && bundle.containsKey("fromBookMark")){
            textSearch.setText(bundle.getString("fromBookMark"));
            geoLocate(textSearch,"dest");

            String name = textSearch.getText().toString();
            // lấy place id thông qua autoComplete
            FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                    .setQuery(name) // Tìm kiếm địa điểm theo tên
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
                        infoPlace = new infoPlace(place.getName(),response.getAutocompletePredictions().get(0).getFullText(null).toString(),
                                String.valueOf(place.getRating()),place.getLatLng());
                        fi.onMsgFromMainToFrag(infoPlace);
                        frameLayout.setVisibility(View.VISIBLE);

//                            List<PhotoMetadata> photoMetadataList = place.getPhotoMetadatas();
//                            if (photoMetadataList != null && photoMetadataList.size() > 0) {
//                                PhotoMetadata photoMetadata = photoMetadataList.get(0);
//                                String photoUrl = photoMetadata.getAttributions();
//
//                                Log.e(TAG, "Photo URL: " + photoUrl);
//                            }


                    }).addOnFailureListener((exception) -> {
                        if (exception instanceof ApiException) {
                            ApiException apiException = (ApiException) exception;
                            int statusCode = apiException.getStatusCode();

                        }
                    });

                }
            }).addOnFailureListener((exception) -> {
                Log.e("Error", "Error getting autocomplete predictions", exception);
            });
        }

    }



    public void initMap() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    // Hàm kiểm tra xem một bảng có tồn tại trong cơ sở dữ liệu hay không
    public static boolean checkIfTableExists(SQLiteDatabase database, String tableName) {
        Cursor cursor = null;
        try {
            cursor = database.rawQuery("SELECT * FROM " + tableName + " LIMIT 1", null);
        } catch (Exception e) {
            // Bảng không tồn tại
        }
        if (cursor != null) {
            cursor.close();
            return true;
        } else {
            return false;
        }
    }



    public synchronized void getDeviceLocation() {
        // để lấy vị trí hiện tại
        FusedLocationProviderClient fusedLocationProviderClient;
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        Log.e("getDeviceLocation", "getMyLocation");

        try {


            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            do {

                Task getLocation = fusedLocationProviderClient.getLastLocation();
                getLocation.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {

                            boolean checkFirstView = false ;
                            if(myLocation == null){
                                checkFirstView = true;
                            }
                            Location location = (Location) getLocation.getResult();
                            if (location == null) {
                                myLocation = null;
                                Log.e("loop", "getDeviceLocation: null");
                                return;

                            }

                            // Add a marker in myLocation and move the camera
                            myLocation = new LatLng(location.getLatitude(), location.getLongitude());

                            CameraPosition cameraPosition = new CameraPosition.Builder()
                                    .target(myLocation)
                                    .zoom(17f)
                                    .build();
                            CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
                            if(checkFirstView == true){
                                mMap.animateCamera(cameraUpdate, 10, null);
                            }else{
                                mMap.animateCamera(cameraUpdate, 2000, null);
                            }

                            Log.e("loop", "getDeviceLocation: not null " + String.valueOf(myLocation.latitude));

                        }
                    }
                });

            } while (myLocation.equals(null));


        } catch (Exception e) {
            Log.e("GetDeviceLocation", e.getMessage());
        }

    }

    public void Search(AutoCompleteTextView textView,String typeLocation){
        textView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                // kiểm tra nếu người dùng bấm nút search ở bàn phím
                // Hoặc bấm nút done
                // Hoặc bấm nút enter
                // Hoặc chạm
                if(i == EditorInfo.IME_ACTION_SEARCH
                        || i == EditorInfo.IME_ACTION_DONE
                        || keyEvent.getAction() == KeyEvent.ACTION_DOWN
                        || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER){

                    //gọi hàm geoLocate để lấy giá trị address sau khi search
                    geoLocate((AutoCompleteTextView) textView, typeLocation);

                    // sau khi bấm vào tim kiếm thì sẽ ẩn bàn phím
                    invisibleKeyBoard(textView);

                    // tìm đường đi địa điểm
                    onClickTrafficMode(viewModeTraffic);

                    return true;


                }
                return false;
            }
        });
    }

    public void autoCompleteSearch(AutoCompleteTextView textView,String query){
        AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();

        String TAG = "AutoComplete";
        // Create a RectangularBounds object.
        RectangularBounds bounds = RectangularBounds.newInstance(
                new LatLng(-33.880490, 151.184363),
                new LatLng(-33.858754, 151.229596));
        // Use the builder to create a FindAutocompletePredictionsRequest.
        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                // Call either setLocationBias() OR setLocationRestriction().
//                .setLocationBias(bounds)
//                //.setLocationRestriction(bounds)
                .setOrigin(myLocation)
                .setCountries("VN")
//                .setTypesFilter(Arrays.asList(TypeFilter.ADDRESS.toString()))
                .setSessionToken(token)
                .setQuery(query)
                .build();

        placesClient.findAutocompletePredictions(request).addOnSuccessListener((response) -> {

            List<InfoSearching> infoSearchings = new ArrayList<>();
            List<AutocompletePrediction> autocompletePredictions = response.getAutocompletePredictions();

            // nếu có vị trí hiện tại thì trả về khoảng cách từ địa điểm đến vị trí hiện tại
            if(myLocation != null){
                for (int i = 0; i < autocompletePredictions.size(); i++) {
                    if(autocompletePredictions.get(i).getDistanceMeters() != null){
                        InfoSearching infoSearching = new InfoSearching(autocompletePredictions.get(i).getPrimaryText(null).toString(),
                                autocompletePredictions.get(i).getFullText(null).toString(),autocompletePredictions.get(i).getDistanceMeters());
                        infoSearchings.add(infoSearching);
                    }else{
                        InfoSearching infoSearching = new InfoSearching(autocompletePredictions.get(i).getPrimaryText(null).toString(),
                                autocompletePredictions.get(i).getFullText(null).toString(),-1);
                        infoSearchings.add(infoSearching);
                    }


                }
                AutoCompleteAdapter aa = new AutoCompleteAdapter(MapsActivity.this,infoSearchings);
                textView.setAdapter(aa);
                aa.notifyDataSetChanged();

            }else{
                for (int i = 0; i < autocompletePredictions.size(); i++) {
                    InfoSearching infoSearching = new InfoSearching(autocompletePredictions.get(i).getPrimaryText(null).toString(),
                            autocompletePredictions.get(i).getFullText(null).toString(),-1);
                    infoSearchings.add(infoSearching);

                }
                AutoCompleteAdapter aa = new AutoCompleteAdapter(MapsActivity.this,infoSearchings);
                textView.setAdapter(aa);
                aa.notifyDataSetChanged();

            }


        }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException) {
                ApiException apiException = (ApiException) exception;
                Log.e(TAG, "Place not found: " + apiException.getStatusCode());
            }
        });
    }

    public void callGeoLocateForOnclick(View view){
        invisibleKeyBoard(view);
        geoLocate(textSearch, "dest");
    }

    public void callBackSearch(View view){
        if(textSearch.isFocused()){
            textSearch.clearFocus();
            invisibleKeyBoard(view);
        }else if(textSearchOrigin.isFocused()){
            textSearchOrigin.clearFocus();
            invisibleKeyBoard(view);
        }else{
            textSearchDest.clearFocus();
            invisibleKeyBoard(view);
        }
    }

    public void geoLocate(AutoCompleteTextView textView, String typeLocation){


        String searchString = textView.getText().toString();

        textSearch.clearFocus();
        textSearchOrigin.clearFocus();
        textSearchDest.clearFocus();

        if(!searchString.equals("Vị trí của bạn")){
            Geocoder geocoder = new Geocoder(MapsActivity.this);
            List<Address> list = new ArrayList<>();
            try{
                list = geocoder.getFromLocationName(searchString, 1);
            }catch (Exception e){
                Log.e("geoLocate", "geoLocate: IOException: " + e.getMessage() );
            }

            if(list.size() > 0){
                Address address = list.get(0);

                LatLng destination = new LatLng(address.getLatitude(), address.getLongitude());

                // lấy thông tin tìm kiếm lưu vào infoSearching
                FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                        .setQuery(searchString) // Tìm kiếm địa điểm theo tên
                        .build();

                // Thực hiện truy vấn
                Task<FindAutocompletePredictionsResponse> task = placesClient.findAutocompletePredictions(request);
                // Xử lý kết quả trả về
                task.addOnSuccessListener((response) -> {
                    if (!response.getAutocompletePredictions().isEmpty()) {

                        List<AutocompletePrediction> autocompletePredictions = response.getAutocompletePredictions();
                        AutocompletePrediction target = autocompletePredictions.get(0);
                        InfoSearching is = new InfoSearching(target.getPrimaryText(null).toString(), target.getFullText(null).toString(), 0);

                        String[] whereArgs = new String[]{is.getName()};
                        db.delete("LichSu","name=?",whereArgs);


                        addRowHistory(is);
//                        db.execSQL(query, new Object[]{is.getName(), is.getAddress()});
                    }
                }).addOnFailureListener((exception) -> {
                    Log.e("Error", "Error getting autocomplete predictions", exception);
                });



                if(typeLocation.equals("origin")){
                    originLocation = destination;

                    // kiểu tra xem markersearch tồn tại hay chưa
                    // nếu chưa tổn tại marker thì thêm marker mới vào

                    if(!isMarkerSearchCheckOrigin){
                        markerSearchOrigin = mMap.addMarker(new MarkerOptions().position(destination).title(searchString));
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(destination,15f),2000,null);
                        isMarkerSearchCheckOrigin = true;

                    }else{
                        markerSearchOrigin.setPosition(destination);
                        markerSearchOrigin.setTitle(searchString);
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(destination,15f),2000,null);
                    }

                    sendRequestFindPath(originLocation,destLocation,trafficMode);
                }else{


                    destLocation = destination;

                    // kiểu tra xem markersearch tồn tại hay chưa
                    // nếu chưa tổn tại marker thì thêm marker mới vào
                    if(!markerSearchCheckDest){
                        markerSearchDest = mMap.addMarker(new MarkerOptions().position(destination).title(searchString));
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(destination,15f),2000,null);
                        markerSearchCheckDest = true;

                    }else{
                        markerSearchDest.setPosition(destination);
                        markerSearchDest.setTitle(searchString);
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(destination,15f),2000,null);
                    }

                    if(textView.getId() == textSearchDest.getId()){
                        sendRequestFindPath(originLocation,destLocation,trafficMode);
                    }


                }

                Log.d("geoLocate", "geoLocate: found a location: " + address.toString());
                //Toast.makeText(this, address.toString(), Toast.LENGTH_SHORT).show();



                if(polyPaths != null){
                    for(Polyline polyline : polyPaths){
                        polyline.remove();
                    }
                }


                //sendRequestFindPath(destination);

            }
        }else{
            if(typeLocation.equals("origin")){
                originLocation = myLocation;

                if(textSearchOrigin.getText().toString().equals("")){
                    originLocation = myLocation;
                    if(isMarkerSearchCheckOrigin){
                        markerSearchOrigin.remove();
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(originLocation,15f),2000,null);
                    }
                }else{
                    if(!isMarkerSearchCheckOrigin){
                        markerSearchOrigin = mMap.addMarker(new MarkerOptions().position(originLocation).title(searchString));
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(originLocation,15f),2000,null);
                        isMarkerSearchCheckOrigin = true;

                    }else{
                        markerSearchOrigin.setPosition(originLocation);
                        markerSearchOrigin.setTitle(searchString);
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(originLocation,15f),2000,null);
                    }
                }

                sendRequestFindPath(originLocation,destLocation,trafficMode);

                // kiểu tra xem markersearch tồn tại hay chưa
                // nếu chưa tổn tại marker thì thêm marker mới vào

            }else{

                destLocation = myLocation;

                if(textSearchDest.getText().toString().equals("")){
                    destLocation = myLocation;
                    if(markerSearchCheckDest){
                        markerSearchDest.remove();
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(destLocation,15f),2000,null);
                    }
                }else{
                    if(!markerSearchCheckDest){
                        markerSearchDest = mMap.addMarker(new MarkerOptions().position(destLocation).title(searchString));
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(destLocation,15f),2000,null);
                        markerSearchCheckDest = true;

                    }else{
                        markerSearchDest.setPosition(destLocation);
                        markerSearchDest.setTitle(searchString);
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(destLocation,15f),2000,null);
                    }
                }

                if(textView.getId() == textSearchDest.getId()){
                    sendRequestFindPath(originLocation,destLocation,trafficMode);
                }


            }
        }

        frameLayout.setVisibility(View.GONE);

    }



    public void getPermissionFromUser(){
        String permission[] = {
                FINE_LOCATION,
                COARSE_LOCATION
        };

        // check xem người dùng đã cấp phép vị trí GPS chưa( vị trí chính xác )
        if(ContextCompat.checkSelfPermission(this,FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){

            // check xem người dùng đã cấp phép vị trí Wifi, mạng... chưa( vị trí xấp xỉ )
            if(ContextCompat.checkSelfPermission(this, COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                checkPermissionLocation = true;
                initMap();
            }else{
                // nếu chưa cấp phép 1 trong 2, sẽ yêu cầu người dùng cấp phép, hàm requestPermissions sẽ gọi hàm
                // onRequestPermissionResult thực thi
                ActivityCompat.requestPermissions(this,permission,REQUEST_CODE_LOCATION);
            }

        }
        else{
            ActivityCompat.requestPermissions(this,permission,REQUEST_CODE_LOCATION);
        }
    }

    @Override
    // hàm xử lý các cấp phép của người dùng
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        checkPermissionLocation = false;
        switch (requestCode){
            case REQUEST_CODE_LOCATION:{

                // kiểu tra xem grantResult có trả về gì không
                if(grantResults.length > 0){
                    // nếu có thì check từng phần tử trả về, có một quyền chưa được cấp phép thì return false

                    for(int i = 0; i < grantResults.length; i++){
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            checkPermissionLocation = false;
                            return;
                        }
                    }

                    checkPermissionLocation = true;
                    initMap();
                }
            }
        }
    }

    // Hàm để xét sự kiện gps, khi nhấn vào nút gps, cam sẽ trở về vị trí người dùng
    public void handleGps(View view){
        LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            // Hiển thị thông báo yêu cầu bật vị trí
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Vị trí của bạn chưa được bật");
            builder.setMessage("Để tiếp tục, vui lòng bật vị trí để có thể sử dụng chức năng định vị vị trí này của Google Maps");
            builder.setPositiveButton("Đồng ý", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Mở cài đặt để người dùng bật vị trí
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
            });
            builder.setNegativeButton("Không, cảm ơn", null);
            builder.show();
        }
        else{
            getDeviceLocation();
        }


    }

    public void handleYourLocation(View view){
        // click vào vị trí của bạn thì sẽ lấy vị trí hiện tại

        FusedLocationProviderClient fusedLocationProviderClient;
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        Log.e("getDeviceLocation", "getMyLocation");

        try {


            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            do {

                Task getLocation = fusedLocationProviderClient.getLastLocation();
                getLocation.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {


                            Location location = (Location) getLocation.getResult();
                            if (location == null) {
                                myLocation = null;
                                Log.e("loop", "getDeviceLocation: null");
                                return;

                            }

                            // Add a marker in myLocation and move the camera
                            myLocation = new LatLng(location.getLatitude(), location.getLongitude());

                            if(textSearchOrigin.isFocused()){
                                textSearchOrigin.setText("Vị trí của bạn");
                                geoLocate(textSearchOrigin,"origin");
                            }else{
                                textSearchDest.setText("Vị trí của bạn");
                                geoLocate(textSearchDest,"dest");
                            }



                        }
                    }
                });

            } while (myLocation.equals(null));


        } catch (Exception e) {
            Log.e("GetDeviceLocation", e.getMessage());
        }
    }

    public void handleBookMark(View view){
        Intent intent = new Intent(this, BookMarkActivity.class);
        startActivity(intent);
    }

    public void handleWalkingMode(View view){

        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_dialog);

        Window window = dialog.getWindow();
        if(window == null)
            return;

        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        Button cancelButton = window.findViewById(R.id.cancelBtn);
        Button acceptButton = window.findViewById(R.id.acceptBtn);

        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), WalkingActivity.class);
                startActivity(intent);
                dialog.dismiss();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();

    }


    public void invisibleKeyBoard(View view){
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    // ham gui yeu cau tim duong di
    public synchronized void sendRequestFindPath(LatLng origin,LatLng destination, String trafficMode){
        String TAG = "sendRequestFindPath";

        if(origin == null){
            Toast.makeText(this, "Please enter origin address!", Toast.LENGTH_SHORT).show();
            return;
        }

        if(destination == null){
            Toast.makeText(this, "Please enter destination address!", Toast.LENGTH_SHORT).show();
            return;
        }

        try{
            DirectionFinder finder = new DirectionFinder(getApplicationContext(), this,origin, destination,true);
            finder.execute(trafficMode);
        }catch (Exception e){
            Log.e(TAG, "sendRequestFindPath: " + e.getMessage() );
        }


    }
    @Override
    public void onStartFindDirection() {
        if(polyPaths != null){
            for(Polyline polyline : polyPaths){
                polyline.remove();
            }
        }
    }

    @Override
    public void onSuccessFindDirection(List<Route> routeList) {

        for(int i = 1; i < routeList.size(); i++){
            Route route = routeList.get(i);

            //List<LatLng> points = new ArrayList<>();
            PolylineOptions polylineOptions;


            polylineOptions = new PolylineOptions().
                    geodesic(true).
                    color(Color.argb(180,183, 187, 204)).
                    width(15);


            for(int j = 0; j < route.polyline.size(); j++){
                polylineOptions.add(route.polyline.get(j));
                //points.add(route.polyline.get(j));

            }

            polyPaths.add(mMap.addPolyline(polylineOptions));

            //fi.onMsgFromMainToFrag(route.distance, route.duration);


        }

        if(routeList.size() > 0){
            Route route = routeList.get(0);

            PolylineOptions polylineOptions = new PolylineOptions().
                    geodesic(true).
                    color(Color.rgb(123, 133, 237)).
                    width(15);

            for(int i = 0; i < route.polyline.size(); i++){
                polylineOptions.add(route.polyline.get(i));
            }

            polyPaths.add(mMap.addPolyline(polylineOptions));

            fi.onMsgFromMainToFrag(route.distance, route.duration);
        }



        invisibleKeyBoard(textSearch);

    }

    @Override
    public void onMsgFromFragToMain(String eventBtn, com.example.googlemaps.InfoPlace.infoPlace infoPlace) {
        if(eventBtn.equals("close")){
            frameLayout.setVisibility(View.GONE);
        }

    }

    public void closeClick(View view){
        frameLayout.setVisibility(View.GONE);
    }

    public void backDirection(View view){
        fi.onMsgFromMainToFrag("backDirection");

        if(markerSearchOrigin != null){
            markerSearchOrigin.setVisible(false);
        }


        if(polyPaths != null){
            for(Polyline polyline : polyPaths){
                polyline.remove();
            }
        }
    }

    @SuppressLint("ResourceAsColor")
    public void onClickTrafficMode(View view){

        viewModeTraffic = view;
        fi.onMsgFromMainToFrag(view);
        timeCar.setText("--");
        timeWalk.setText("--");
        timeBus.setText("--");
        timeMoto.setText("--");
        findViewById(R.id.car).setBackgroundColor(Color.WHITE);
        findViewById(R.id.walk).setBackgroundColor(Color.WHITE);
        findViewById(R.id.bus).setBackgroundColor(Color.WHITE);
        findViewById(R.id.moto).setBackgroundColor(Color.WHITE);
        
        if(viewModeTraffic != null) {
            
            if(view.getId() == findViewById(R.id.car).getId()){
                sendRequestFindPath(originLocation,destLocation,fi.drivingMode);

                findViewById(R.id.car).setBackgroundResource(R.drawable.custom_traffic_mode);
               //findViewById(R.id.imageViewCar).setBackgroundColor(R.color.blue1);

            }

            if(view.getId() == findViewById(R.id.moto).getId()){
                sendRequestFindPath(originLocation,destLocation,fi.motoMode);

                findViewById(R.id.moto).setBackgroundResource(R.drawable.custom_traffic_mode);
                //findViewById(R.id.imageViewMoto).setBackgroundColor(R.color.blue1);

            }

            if(view.getId() == findViewById(R.id.bus).getId()){
                sendRequestFindPath(originLocation,destLocation,fi.transitMode);

                findViewById(R.id.bus).setBackgroundResource(R.drawable.custom_traffic_mode);
                //findViewById(R.id.imageViewBus).setBackgroundColor(R.color.blue1);

            }

            if(view.getId() == findViewById(R.id.walk).getId()){
                sendRequestFindPath(originLocation,destLocation,fi.walkMode);

                findViewById(R.id.walk).setBackgroundResource(R.drawable.custom_traffic_mode);
                //findViewById(R.id.imageViewWalk).setBackgroundColor(R.color.blue1);

            }
    
       }
    }


    public void clearText(View view){

        if(textSearch.isFocused()){
            textSearch.setText("");
        }
        else if(textSearchOrigin.isFocused()){
            textSearchOrigin.setText("");
        }else{
            textSearchDest.setText("");
        }
    }



}
