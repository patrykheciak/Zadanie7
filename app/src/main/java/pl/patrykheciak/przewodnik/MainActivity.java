package pl.patrykheciak.przewodnik;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnPolylineClickListener {

    private GoogleMap mMap;
    private Marker recentlyFocusedMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setMapTypeBasedOnPreference();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.show_wroclaw:
                animateToWroclaw();
                return true;
            case R.id.settings:
                startActivity(new Intent(this, MyPreferenceActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // ----- Map callbacks -----------
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        addMarkers();
        addOthers();
        mMap.setInfoWindowAdapter(new MyInfoWindowAdapter());
        mMap.setOnMarkerClickListener(this);
        mMap.setOnPolylineClickListener(this);

        setMapTypeBasedOnPreference();
        showWroclaw();
//        mMap.getUiSettings().setMapToolbarEnabled(true);
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        recentlyFocusedMarker = marker;

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                LatLng position = marker.getPosition();
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 17));
            }
        }, 100);
        return false;
    }

    @Override
    public void onPolylineClick(Polyline polyline) {

        mMap.animateCamera(
                CameraUpdateFactory
                        .newLatLngZoom(new LatLng(51.103727, 17.033645), 15));
    }

    // ===============================

    private void setMapTypeBasedOnPreference() {
        if (mMap != null) {
            SharedPreferences spref = PreferenceManager.getDefaultSharedPreferences(this);
            String mapType = spref.getString("mapTypePref", "Teren");
            if (mapType.equals("Teren"))
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
            else if (mapType.equals("Satelita"))
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            else if (mapType.equals("Normalna"))
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            else if (mapType.equals("Hybryda"))
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);


            boolean useDarkStyle = spref.getBoolean("darkStylePref", false);
            if (useDarkStyle) {
                try {
                    mMap.setMapStyle(
                            MapStyleOptions.loadRawResourceStyle(
                                    this, R.raw.map_style));
                } catch (Resources.NotFoundException e) {
                    e.printStackTrace();
                }
            } else {
                mMap.setMapStyle(null);
            }
        }
    }

    private void showWroclaw() {
        LatLng rynek = new LatLng(51.109602, 17.032046);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(rynek, 12));
    }

    private void animateToWroclaw() {
        if (recentlyFocusedMarker != null) {
            if (recentlyFocusedMarker.isInfoWindowShown())
                recentlyFocusedMarker.hideInfoWindow();
        }

        LatLng rynek = new LatLng(51.109602, 17.032046);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(rynek, 12));
    }

    private void addMarkers() {
        LatLng rynek = new LatLng(51.109602, 17.032046);
        mMap.addMarker(new MarkerOptions().position(rynek).title("Rynek").snippet("Jeden z największych rynków staromiejskich Europy"));

        LatLng panorama = new LatLng(51.110133, 17.044394);
        mMap.addMarker(new MarkerOptions().position(panorama).title("Panorama Racławicka"));

        LatLng zoo = new LatLng(51.105607, 17.076229);
        mMap.addMarker(new MarkerOptions().position(zoo).title("ZOO Wrocław"));

        LatLng skytower = new LatLng(51.094517, 17.019569);
        mMap.addMarker(new MarkerOptions().position(skytower).title("Sky Tower").snippet("Taras widokowy na wysokości ponad 200m"));

        LatLng hala = new LatLng(51.106900, 17.077311);
        mMap.addMarker(new MarkerOptions().position(hala).title("Hala Stulecia").snippet("Hala widowiskowo-sportowa w stylu ekspresjonistycznym"));
    }

    private void addOthers() {
        mMap.addPolygon(
                new PolygonOptions()
                        .add(new LatLng(51.107943, 17.070291))
                        .add(new LatLng(51.104838, 17.068489))
                        .add(new LatLng(51.100452, 17.079465))
                        .add(new LatLng(51.101200, 17.080205))
                        .add(new LatLng(51.102204, 17.077963))
                        .add(new LatLng(51.104535, 17.080377))
                        .fillColor(Color.argb(50, 100, 255, 0))
                        .strokeColor(Color.argb(100, 50, 200, 0))
        );

        mMap.addPolyline(
                new PolylineOptions()
                        .add(new LatLng(51.099196, 17.036640))
                        .add(new LatLng(51.099845, 17.035910))
                        .add(new LatLng(51.101260, 17.029295))
                        .add(new LatLng(51.109076, 17.032976))
                        .add(new LatLng(51.109536, 17.031600))
                        .clickable(true)
                        .color(Color.argb(200, 50, 200, 0))

        );
    }

    class MyInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        private final View myContentsView;

        MyInfoWindowAdapter() {
            myContentsView = getLayoutInflater().inflate(R.layout.custom_info_contents, null);
        }

        @Override
        public View getInfoContents(final Marker marker) {

            TextView tvTitle = ((TextView) myContentsView.findViewById(R.id.title));
            tvTitle.setText(marker.getTitle());
            TextView tvSnippet = ((TextView) myContentsView.findViewById(R.id.snippet));
            tvSnippet.setText(marker.getSnippet());
            ImageView imageView = myContentsView.findViewById(R.id.imageE);

            imageView.setImageDrawable(null);
            String title = marker.getTitle();
            Picasso picasso = Picasso.get();
            RequestCreator load = null;

            if (title.equals("Rynek")) {
                load = picasso.load("https://www.wroclaw.pl/files/cmsdocuments/302368/630x350/rynek.jpg");
            } else if (title.equals("Panorama Racławicka")) {
                load = picasso.load("http://dzieje.pl/sites/default/files/styles/open_article_750x0_/public/201612/panorama_raclawicka_0.jpg");
            } else if (title.equals("ZOO Wrocław")) {
                load = picasso.load("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcT16br0ITZtiqW8wL_l2DIZLDpt8mM0T1Hfq11prr7qgwerdkqb");
            } else if (title.equals("Sky Tower")) {
                load = picasso.load("https://cdn.galleries.smcloud.net/t/galleries/gf-ZdeS-MdPo-BVWc_sky-tower-we-wroclawiu-najwyzszy-budynek-w-polsce-664x442.jpg");
            } else if (title.equals("Hala Stulecia")) {
                load = picasso.load("https://az851360.vo.msecnd.net/media/4082/ten450.jpg");
            }

            load.into(imageView, new Callback() {
                @Override
                public void onSuccess() {
                    if (marker.isInfoWindowShown()) {
                        marker.hideInfoWindow();
                        marker.showInfoWindow();
                    }
                }

                @Override
                public void onError(Exception e) {

                }
            });
            return myContentsView;
        }

        @Override
        public View getInfoWindow(Marker marker) {
            // TODO Auto-generated method stub
            return null;
        }

    }

}