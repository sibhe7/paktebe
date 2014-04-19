package com.maps.paktebe;

import java.util.List;

import org.json.JSONObject;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.GoogleMap.OnMyLocationChangeListener;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

@SuppressLint("NewApi")
public class DirectionActivity extends Activity implements OnMyLocationChangeListener {
	
	private final String URL = "http://maps.googleapis.com/maps/api/directions/json?";
	private LatLng start ;
	private LatLng end;
	private String nama;
	private GoogleMap map;
	private JSONParser json;
	private ProgressDialog pDialog;
	private List<LatLng> listDirections;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_direction);
		json = new JSONParser();
		setupMapIfNeeded();
		
		Bundle b = getIntent().getExtras();
		if(b != null){
			start = new LatLng(b.getDouble(PetaLokasi.KEY_LAT_ASAL), b.getDouble(PetaLokasi.KEY_LNG_ASAL));
			end = new LatLng(b.getDouble(PetaLokasi.KEY_LAT_TUJUAN), b.getDouble(PetaLokasi.KEY_LNG_TUJUAN));
			nama = b.getString(PetaLokasi.KEY_NAMA);			
		}
		new AsyncTaskDirection().execute();
	}
	
	private void setupMapIfNeeded() {
		if (map == null){
			map = ((MapFragment)getFragmentManager().findFragmentById(R.id.mapsdirections)).getMap();
			
			if (map!= null){
				setupMap();
			}
		}		
	}

	private void setupMap() {
		map.setMyLocationEnabled(true);
		map.setOnMyLocationChangeListener(this);
		moveToMyLocation();				
	}

	private void moveToMyLocation() {
		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Criteria criteria = new Criteria();
		
		Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
		if (location != null){
			map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 13));			
		}		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		int resCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());
		if (resCode != ConnectionResult.SUCCESS){
			GooglePlayServicesUtil.getErrorDialog(resCode, this, 1);
		}
	}
	
	
	private class AsyncTaskDirection extends AsyncTask<Void, Void, Void>{

		@Override
		protected Void doInBackground(Void... params) {
			String uri = URL
					+ "origin=" + start.latitude + "," + start.longitude
					+ "&destination=" + end.latitude + "," + end.longitude
					+ "&sensor=true&units=metric";
			JSONObject jObject = json.getJSONFromURL(uri);
			listDirections = json.getDirection(jObject);
			
			return null;
		}
		
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(DirectionActivity.this);
			pDialog.setMessage("Loading ....");
			pDialog.setCancelable(true);
			pDialog.show();
		}
		
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			pDialog.dismiss();
			gambarDirection();			
		}


		private void gambarDirection() {
			PolylineOptions line = new PolylineOptions().width(3).color(Color.BLUE);
			for (int i = 0; i< listDirections.size();i++){
				line.add(listDirections.get(i));
			}
			map.addPolyline(line);
			
			map.addMarker(new MarkerOptions()
					.position(end)
					.title(nama)
					.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));			
		}		
	}

	@Override
	public void onMyLocationChange(Location location) {
		Toast.makeText(this, "Lokasi berubah ke " + location.getLatitude() + "," + location.getLongitude(),
				Toast.LENGTH_SHORT).show();		
	}
}
