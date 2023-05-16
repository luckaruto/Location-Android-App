package com.example.googlemaps.GuideDirection;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Image;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.googlemaps.Direction.DIrectionListener;
import com.example.googlemaps.Direction.DirectionFinder;
import com.example.googlemaps.Direction.Route;
import com.example.googlemaps.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class GuideByText extends AppCompatActivity implements OnMapReadyCallback, DIrectionListener {

    GoogleMap map;
    private LatLng myLocation = null;
    private boolean checkFirstLocation = false ;
    private LatLng destination;
    private String addressDest;
    private String trafficMode;

    private TextView guide1;
    private TextView guide2;
    private TextView timeDuration;
    private TextView distance;
    private TextView estimatedTime;

    private static String TAG = "GuideByText";

    private String prevRoad = "";
    private String curRoad = "";

    // chuyển văn bản sang giọng nói
    private static TextToSpeech textToSpeech;


    // biến này sẽ kiểm tra xem hướng đi tiếp theo có gần không, nếu trong phạm vi 100m mà phải chuyển
    // hướng thì sẽ chỉ đường và thêm đoạn text chỉ hướng đi tiếp theo
    private boolean check100m = true;

    // nếu còn 20m nữa chuyển hướng
    private boolean check20m = true;

    Translator translator;
    private boolean checkTranslate = false;

    // create request gps
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationCallback locationCallback;

    private List<Polyline> polyPaths = new ArrayList<>();

    // convert maneuver to Vietnamese
    private Maneuver maneuver ;

    private boolean checkManeuver = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide_by_text);

        initMap();

        getSupportActionBar().hide();

        getDeviceLocation();

        // khởi tạo chuyển văn bản thành giọng nói
        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if(i != TextToSpeech.ERROR){
                    Locale locale = new Locale("vi","VN");
                    //Locale locale =  Locale.ENGLISH;
                    textToSpeech.setLanguage(locale);
                }
            }
        });

        guide1 = findViewById(R.id.guide1);
        guide2 = findViewById(R.id.guide2);

        timeDuration = findViewById(R.id.timeDuration);
        distance = findViewById(R.id.distance);
        estimatedTime = findViewById(R.id.time);

        maneuver = new Maneuver();

    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        // hiển thị vị trí hiện tại
        map.setMyLocationEnabled(true);

        // tắt nút quay về vị trí hiện tại
        map.getUiSettings().setMyLocationButtonEnabled(false);


        Bundle data = getIntent().getExtras();
        myLocation = new LatLng(data.getDouble("latitudeOrigin"), data.getDouble("longitudeOrigin"));
        destination = new LatLng(data.getDouble("latitudeDest"), data.getDouble("longitudeDest"));
        addressDest = data.getString("address");
        trafficMode = data.getString("trafficMode");

        //Log.e(TAG, "onMapReady: " + String.valueOf(destination.latitude) + " " + String.valueOf(destination.longitude));

        map.addMarker(new MarkerOptions().position(destination).title(addressDest));

        //Log.e(TAG, "onLocationChanged: " + String.valueOf(myLocation.latitude) + " " + String.valueOf(myLocation.longitude) +
        //String.valueOf(destination.latitude) + " " + String.valueOf(destination.longitude));

        sendRequestFindPath(myLocation, destination, trafficMode);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .setInterval(2000)
                .setFastestInterval(1000)
                .setSmallestDisplacement(1);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                myLocation = new LatLng(locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude());
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(myLocation)
                        .zoom(17f)
                        .build();
                CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
                map.animateCamera(cameraUpdate,500,null);
                sendRequestFindPath(myLocation, destination, trafficMode);
                //callSpeech("Oke Oke");
            }
        };

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());


    }

    public void initMap() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

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

                            Location location = (Location) getLocation.getResult();
                            if (location == null) {
                                myLocation = null;
                                Log.e("loop", "getDeviceLocation: null");
                                return;

                            }

                            // Add a marker in myLocation and move the camera
                            myLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            float tilt = 35; // độ nghiêng mới là 45 độ
                            CameraPosition cameraPosition = new CameraPosition.Builder()
                                    .target(myLocation)
                                    .zoom(17f)
                                    .tilt(tilt)
                                    .build();
                            CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);

                            if(checkFirstLocation == false){
                                map.animateCamera(cameraUpdate, 10, null);
                            } else{
                                map.animateCamera(cameraUpdate, 2000, null);
                            }

                            checkFirstLocation = true;

                            Log.e("loop", "getDeviceLocation: not null " + String.valueOf(myLocation.latitude));

                        }
                    }
                });

            } while (myLocation.equals(null));


        } catch (Exception e) {
            Log.e("GetDeviceLocation", e.getMessage());
        }

    }


    public void handleGps(View view) {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
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
        } else {
            getDeviceLocation();
        }


    }

    public synchronized void sendRequestFindPath(LatLng origin, LatLng destination, String trafficMode) {
        String TAG = "sendRequestFindPath";

        if (origin == null) {
            Toast.makeText(this, "Please enter origin address!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (destination == null) {
            Toast.makeText(this, "Please enter destination address!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            DirectionFinder finder = new DirectionFinder(getApplicationContext(), this, origin, destination, false);
            finder.execute(trafficMode);
        } catch (Exception e) {
            Log.e(TAG, "sendRequestFindPath: " + e.getMessage());
        }


    }

    @Override
    public void onStartFindDirection() {
        if (polyPaths != null) {
            for (Polyline polyline : polyPaths) {
                polyline.remove();
            }
        }
    }

    @Override
    public void onSuccessFindDirection(List<Route> routeList) {
        //Log.e(TAG, "onSuccessFindDirection: " + routeList.get(0).firstStep);
        //Toast.makeText(GuideByText.this, routeList.get(0).firstStep, Toast.LENGTH_SHORT).show();
        for (Route route : routeList) {

            List<LatLng> points = new ArrayList<>();

            PolylineOptions polylineOptions = new PolylineOptions().
                    geodesic(true).
                    color(Color.rgb(21, 189, 242)).
                    width(15);

            for (int i = 0; i < route.polyline.size(); i++) {
                polylineOptions.add(route.polyline.get(i));
                points.add(route.polyline.get(i));

            }

            polyPaths.add(map.addPolyline(polylineOptions));

        }

        // cập nhật giao diện
        timeDuration.setText(routeList.get(0).duration.text);
        distance.setText(routeList.get(0).distance.text);

        EstimateTime estimateTime = new EstimateTime();
        estimatedTime.setText(estimateTime.estimate(routeList.get(0).duration.value));

        // kiểm tra xem có qua đường mới chưa
        // nếu rồi thì hiển thị giọng nói đầu đoạn đường
        String guideText1 = String.valueOf(Html.fromHtml(routeList.get(0).firstStep,Html.FROM_HTML_MODE_COMPACT));
        guideText1.replace("Đ.","đường ");

        // xóa bớt chỉ dẫn
        int dotIndex = guideText1.indexOf("\n");
        if(dotIndex != -1){
            guideText1 = guideText1.substring(0, dotIndex);
        }

        curRoad = guideText1;

        // chỉ đường cơ bản
        guide1.setText(guideText1);

        ImageView imageGuide1 = findViewById(R.id.icManeuverGuide1);
        imageGuide1.setImageResource(maneuver.findImageManeuver("straight"));


        if(!curRoad.equals(prevRoad)){

            check100m = true;
            check20m = true;

            // kiểm tra xem đoạn đường này có khoản cách < 200m không, nếu bé hơn 100m thì chỉ đường đi tiếp theo, không thì
            // không cần chỉ đường đi tiếp theo
            if(routeList.get(0).distanceFirstStep < 200){

                // tìm hành động cần làm sau khi đi hết đường
                String nextManeuver = maneuver.convertToVietnamese(routeList.get(0).maneuver);
                String guideText2 = "";
                // nếu có hành động tiếp theo thì chỉ, không thì thôi
                if(nextManeuver != null){
                    guideText2 = "Sau đó, "+nextManeuver;
                    guide2.setText(guideText2);
                    ImageView imgGuide2 = findViewById(R.id.icManeuverGuide2);
                    imgGuide2.setImageResource(maneuver.findImageManeuver(routeList.get(0).maneuver));
                    findViewById(R.id.linear2).setVisibility(View.VISIBLE);
                }else{
                    findViewById(R.id.linear2).setVisibility(View.GONE);
                }

                new SpeechTask().execute(guideText1 + "." + guideText2);
            }else{
                // không thì chỉ đường thôi
                new SpeechTask().execute(guideText1);
                findViewById(R.id.linear2).setVisibility(View.GONE);
            }

            prevRoad = curRoad;
            //Log.e(TAG, "onSuccessFindDirection: firstStep");
        }
        // nếu chưa thì
        else{
            // kiểm tra xem đã đến gần đến đoạn đường tiếp theo chưa, nếu < 100m nữa đến đoạn đường tiếp theo thì
            if(routeList.get(0).distanceFirstStep < 100 && check100m){
                check100m = false;
                String nextManeuver = maneuver.convertToVietnamese(routeList.get(0).maneuver);
                String guideText2 = routeList.get(0).distanceFirstStep + " mét nữa "+nextManeuver;
                ImageView imgGuide2 = findViewById(R.id.icManeuverGuide2);
                imgGuide2.setImageResource(maneuver.findImageManeuver(routeList.get(0).maneuver));
                findViewById(R.id.linear2).setVisibility(View.VISIBLE);
                new SpeechTask().execute( guideText2);
            }
            // kiểm tra xem đã đến gần đến đoạn đường tiếp theo chưa, nếu < 20m nữa đến đoạn đường tiếp theo thì
            if (routeList.get(0).distanceFirstStep < 20 && check20m) {
                check20m = false;
                String nextManeuver = maneuver.convertToVietnamese(routeList.get(0).maneuver);
                String guideText2 = "Chuẩn bị "+nextManeuver;
                ImageView imgGuide2 = findViewById(R.id.icManeuverGuide2);
                imgGuide2.setImageResource(maneuver.findImageManeuver(routeList.get(0).maneuver));
                findViewById(R.id.linear2).setVisibility(View.VISIBLE);
                new SpeechTask().execute( guideText2);
            }

        }

    }

    public static void callSpeech(String text){
        textToSpeech.speak(text,TextToSpeech.QUEUE_ADD,null);
        Log.e(TAG, "callSpeech: "+text );
    }


    @Override
    public void onBackPressed() {

        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        finish();
    }

    public void close(View view){
        onBackPressed();
    }
}
