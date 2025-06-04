package fcu.app.distributionapp;

import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import android.Manifest;
import android.location.Geocoder;
import android.location.Address;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.List;
import java.util.Locale;
import java.io.IOException;

import com.google.android.gms.maps.*;

import com.google.android.gms.maps.model.*;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

public class MapFragment extends Fragment {

    private MapView mapView;
    private GoogleMap googleMap;
    private EditText etsearch;
    private Button btnsearch;
    private PlacesClient placesClient;

    private static final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
        } else {
            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1001
            );
        }
    }

    public MapFragment() {}

    public static MapFragment newInstance() {
        return new MapFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        mapView = view.findViewById(R.id.mapView);
        etsearch = view.findViewById(R.id.et_mapSearch);
        btnsearch = view.findViewById(R.id.btn_mapSearch);

        super.onCreate(savedInstanceState);
        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), "AIzaSyDPAfL5noHis2xm0YegmJvzG4u8g-ykktM", Locale.TAIWAN);
            placesClient = Places.createClient(requireContext());
        }

        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY);
        }

        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(map -> {
            googleMap = map;

            // 啟用地圖 UI 功能
            googleMap.getUiSettings().setZoomControlsEnabled(true);
            googleMap.getUiSettings().setZoomGesturesEnabled(true);

            enableMyLocation(); // 啟用定位
        });

        btnsearch.setOnClickListener(v -> {
            String locationName = etsearch.getText().toString().trim();
            if (!locationName.isEmpty()) {
                searchLocation(locationName);
            } else {
                Toast.makeText(getContext(), "請輸入地點名稱", Toast.LENGTH_SHORT).show();
            }
        });
        return view;
    }

    private void searchLocation(String locationName) {
        AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();

        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                .setSessionToken(token)
                .setQuery(locationName)
                .build();

        placesClient.findAutocompletePredictions(request).addOnSuccessListener(response -> {
            if (!response.getAutocompletePredictions().isEmpty()) {
                String placeId = response.getAutocompletePredictions().get(0).getPlaceId();

                List<Place.Field> placeFields = List.of(
                        Place.Field.ID,
                        Place.Field.NAME,
                        Place.Field.ADDRESS,
                        Place.Field.LAT_LNG,
                        Place.Field.RATING
                );

                FetchPlaceRequest fetchPlaceRequest = FetchPlaceRequest.builder(placeId, placeFields).build();

                placesClient.fetchPlace(fetchPlaceRequest).addOnSuccessListener(fetchPlaceResponse -> {
                    Place place = fetchPlaceResponse.getPlace();

                    LatLng latLng = place.getLatLng();
                    if (latLng != null) {
                        googleMap.clear();
                        googleMap.addMarker(new MarkerOptions().position(latLng).title(place.getName()));
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                    }

                    showPlaceInfo(place);

                }).addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "無法取得地點詳細資訊", Toast.LENGTH_SHORT).show();
                });

            } else {
                Toast.makeText(getContext(), "找不到該地點", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "搜尋失敗：" + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onResume() { super.onResume(); mapView.onResume(); }

    @Override
    public void onPause() { super.onPause(); mapView.onPause(); }

    @Override
    public void onStart() { super.onStart(); mapView.onStart(); }

    @Override
    public void onStop() { super.onStop(); mapView.onStop(); }

    @Override
    public void onDestroy() { super.onDestroy(); mapView.onDestroy(); }

    @Override
    public void onLowMemory() { super.onLowMemory(); mapView.onLowMemory(); }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Bundle mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle);
        }
        mapView.onSaveInstanceState(mapViewBundle);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1001) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (googleMap != null) {
                    googleMap.setMyLocationEnabled(true);
                }
            } else {
                Toast.makeText(getContext(), "未授權定位權限，無法顯示目前位置", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void fetchPlaceDetailsByLatLng(LatLng latLng, String fallbackName) {
        List<Place.Field> placeFields = List.of(
                Place.Field.NAME,
                Place.Field.ADDRESS,
                Place.Field.RATING,
                Place.Field.TYPES
        );

        // 建立查詢請求（用緯經度會比較複雜，建議用名稱來查）
        com.google.android.libraries.places.api.net.FindCurrentPlaceRequest request =
                com.google.android.libraries.places.api.net.FindCurrentPlaceRequest.newInstance(placeFields);

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getContext(), "缺少定位權限，無法查詢地點資訊", Toast.LENGTH_SHORT).show();
            return;
        }

        placesClient.findCurrentPlace(request).addOnSuccessListener(response -> {
            if (!response.getPlaceLikelihoods().isEmpty()) {
                Place place = response.getPlaceLikelihoods().get(0).getPlace();
                showPlaceInfo(place);
            } else {
                showFallbackInfo(fallbackName, latLng);
            }
        }).addOnFailureListener(e -> {
            showFallbackInfo(fallbackName, latLng);
        });
    }

    private void showPlaceInfo(Place place) {
        String info = "名稱: " + place.getName() +
                "\n地址: " + place.getAddress() +
                (place.getRating() != null ? "\n評分: " + place.getRating() : "");

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("地點資訊")
                .setMessage(info)
                .setPositiveButton("關閉", null)
                .show();
    }

    private void showFallbackInfo(String name, LatLng latLng) {
        String info = "名稱: " + name +
                "\n座標: " + latLng.latitude + ", " + latLng.longitude;

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("基本地點資訊")
                .setMessage(info)
                .setPositiveButton("關閉", null)
                .show();
    }

}
