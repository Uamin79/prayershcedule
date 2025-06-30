package com.example.prayerschedule;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout;
import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements LocationListener {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private TextView tvTime, tvDate, tvMosqueName, tvMosqueAddress, tvAnnouncement;
    private TextView tvImsak, tvShubuh, tvSyuruq, tvZhuhur, tvAshar, tvMaghrib, tvIsya;
    private ImageView ivSlideshow;
    private LinearLayout prayerTimesLayout;

    private LocationManager locationManager;
    private Handler imageHandler;
    private int[] imageResources = {
            R.drawable.image1,
            R.drawable.image2,
            R.drawable.image3
    };
    private int currentImageIndex = 0;

    private Runnable imageRunnable = new Runnable() {
        @Override
        public void run() {
            currentImageIndex = (currentImageIndex + 1) % imageResources.length;
            ivSlideshow.setImageResource(imageResources[currentImageIndex]);
            imageHandler.postDelayed(this, 10000); // Ganti gambar setiap 10 detik
        }
    };

    private PrayerDbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        dbHelper = new PrayerDbHelper(this);

        updateTimeAndDate();

        imageHandler = new Handler();
        imageHandler.post(imageRunnable);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        requestLocationPermission();
        
    }

    private void initializeViews() {
        tvTime = findViewById(R.id.tvTime);
        tvDate = findViewById(R.id.tvDate);
        tvMosqueName = findViewById(R.id.tvMosqueName);
        tvMosqueAddress = findViewById(R.id.tvMosqueAddress);
        tvAnnouncement = findViewById(R.id.tvAnnouncement);
        ivSlideshow = findViewById(R.id.ivSlideshow);
        prayerTimesLayout = findViewById(R.id.prayerTimesLayout);

        tvImsak = findViewById(R.id.tvImsak);
        tvShubuh = findViewById(R.id.tvShubuh);
        tvSyuruq = findViewById(R.id.tvSyuruq);
        tvZhuhur = findViewById(R.id.tvZhuhur);
        tvAshar = findViewById(R.id.tvAshar);
        tvMaghrib = findViewById(R.id.tvMaghrib);
        tvIsya = findViewById(R.id.tvIsya);
    }

    private void updateTimeAndDate() {
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                SimpleDateFormat timeSdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                SimpleDateFormat dateSdf = new SimpleDateFormat("EEEE, dd MMMM yyyy", new Locale("id"));
                Date now = new Date();
                tvTime.setText(timeSdf.format(now));
                tvDate.setText(dateSdf.format(now));
                highlightCurrentPrayer();
                handler.postDelayed(this, 1000); // update setiap detik
            }
        });
    }

    private void requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            startLocationUpdates();
        }
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    60000, // 1 menit
                    10, // 10 meter
                    this);
            updateLocation(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                Toast.makeText(this, "Izin lokasi ditolak", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        updateLocation(location);
    }

    private void updateLocation(Location location) {
        if (location != null) {
            double lat = location.getLatitude();
            double lon = location.getLongitude();



            tvMosqueAddress.setText(String.format(Locale.getDefault(), "Lokasi: %.4f, %.4f", lat, lon));

            Calendar cal = Calendar.getInstance();
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH) + 1;
            int day = cal.get(Calendar.DAY_OF_MONTH);

            int timeZone = Calendar.getInstance().getTimeZone().getRawOffset() / (1000 * 60 * 60);
            double[] prayerTimes = PrayerCalculator.calcPrayerTimes(year, month, day, lon, lat, timeZone, -18, -18);

            updatePrayerTimes(prayerTimes);
        } else {
        double lat = -6.986376;
        double lon =  110.396473;}


    }

    private void updatePrayerTimes(double[] prayerTimes) {
        tvImsak.setText(String.format("Imsak\n%s", formatTime(prayerTimes[0] - 0.1))); // Imsak 10 menit sebelum Subuh
        tvShubuh.setText(String.format("Shubuh\n%s", formatTime(prayerTimes[0])));
        tvSyuruq.setText(String.format("Syuruq\n%s", formatTime(prayerTimes[1])));
        tvZhuhur.setText(String.format("Zhuhur\n%s", formatTime(prayerTimes[2])));
        tvAshar.setText(String.format("Ashar\n%s", formatTime(prayerTimes[3])));
        tvMaghrib.setText(String.format("Maghrib\n%s", formatTime(prayerTimes[4])));
        tvIsya.setText(String.format("Isya\n%s", formatTime(prayerTimes[5])));
    }

    private void highlightCurrentPrayer() {
        Calendar now = Calendar.getInstance();
        double currentTime = now.get(Calendar.HOUR_OF_DAY) + now.get(Calendar.MINUTE) / 60.0;

        TextView[] prayerViews = {tvShubuh, tvSyuruq, tvZhuhur, tvAshar, tvMaghrib, tvIsya};
        for (TextView tv : prayerViews) {
            tv.setBackgroundColor(Color.TRANSPARENT);
        }

        if (currentTime < getTimeFromTextView(tvShubuh)) {
            tvIsya.setBackgroundColor(Color.parseColor("#FFD700"));
        } else if (currentTime < getTimeFromTextView(tvSyuruq)) {
            tvShubuh.setBackgroundColor(Color.parseColor("#FFD700"));
        } else if (currentTime < getTimeFromTextView(tvZhuhur)) {
            tvSyuruq.setBackgroundColor(Color.parseColor("#FFD700"));
        } else if (currentTime < getTimeFromTextView(tvAshar)) {
            tvZhuhur.setBackgroundColor(Color.parseColor("#FFD700"));
        } else if (currentTime < getTimeFromTextView(tvMaghrib)) {
            tvAshar.setBackgroundColor(Color.parseColor("#FFD700"));
        } else if (currentTime < getTimeFromTextView(tvIsya)) {
            tvMaghrib.setBackgroundColor(Color.parseColor("#FFD700"));
        } else {
            tvIsya.setBackgroundColor(Color.parseColor("#FFD700"));
        }
    }

    private double getTimeFromTextView(TextView tv) {
        String[] parts = tv.getText().toString().split("\n");
        String[] timeParts = parts[1].split(":");
        return Integer.parseInt(timeParts[0]) + Integer.parseInt(timeParts[1]) / 60.0;
    }

    private String formatTime(double time) {
        int hours = (int) time;
        int minutes = (int) ((time - hours) * 60);
        return String.format(Locale.getDefault(), "%02d:%02d", hours, minutes);
    }

    // Implementasi metode LocationListener yang tidak digunakan
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    @Override
    public void onProviderEnabled(String provider) {}

    @Override
    public void onProviderDisabled(String provider) {}

    @Override
    protected void onResume() {
        super.onResume();
        startLocationUpdates();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    private void stopLocationUpdates() {
        locationManager.removeUpdates(this);
    }
}