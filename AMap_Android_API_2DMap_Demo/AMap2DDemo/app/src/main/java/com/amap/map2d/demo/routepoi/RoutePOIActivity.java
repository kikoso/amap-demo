package com.amap.map2d.demo.routepoi;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.AMap.InfoWindowAdapter;
import com.amap.api.maps2d.AMap.OnInfoWindowClickListener;
import com.amap.api.maps2d.AMap.OnMapClickListener;
import com.amap.api.maps2d.AMap.OnMarkerClickListener;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.LatLngBounds;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RideRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.RouteSearch.DriveRouteQuery;
import com.amap.api.services.route.RouteSearch.OnRouteSearchListener;
import com.amap.api.services.route.WalkRouteResult;
import com.amap.api.services.routepoisearch.RoutePOIItem;
import com.amap.api.services.routepoisearch.RoutePOISearch;
import com.amap.api.services.routepoisearch.RoutePOISearch.OnRoutePOISearchListener;
import com.amap.api.services.routepoisearch.RoutePOISearch.RoutePOISearchType;
import com.amap.api.services.routepoisearch.RoutePOISearchQuery;
import com.amap.api.services.routepoisearch.RoutePOISearchResult;
import com.amap.map2d.demo.R;
import com.amap.map2d.demo.route.DrivingRouteOverLay;
import com.amap.map2d.demo.util.AMapUtil;
import com.amap.map2d.demo.util.ToastUtil;


public class RoutePOIActivity extends Activity implements OnMapClickListener, 
OnMarkerClickListener, OnInfoWindowClickListener, InfoWindowAdapter, OnRouteSearchListener, OnRoutePOISearchListener {
	private AMap aMap;
	private MapView mapView;
	private Context mContext;
	private RouteSearch mRouteSearch;
	private DriveRouteResult mDriveRouteResult;
	private LatLonPoint mStartPoint = new LatLonPoint(39.942295, 116.335891);//?????????116.335891,39.942295
	private LatLonPoint mEndPoint = new LatLonPoint(39.995576, 116.481288);//?????????116.481288,39.995576
	private final int ROUTE_TYPE_DRIVE = 2;
	private int mode = RouteSearch.DrivingDefault;
	
	private ProgressDialog progDialog = null;// ??????????????????
	private myRoutePoiOverlay overlay;
	private TextView gasbtn,ATMbtn,Maibtn,Toibtn;
	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.routepoi_activity);
		
		mContext = this.getApplicationContext();
		mapView = (MapView) findViewById(R.id.route_map);
		mapView.onCreate(bundle);// ?????????????????????
		init();
		setfromandtoMarker();
		searchRouteResult(ROUTE_TYPE_DRIVE, mode);
	}

	private void setfromandtoMarker() {
		aMap.addMarker(new MarkerOptions()
		.position(AMapUtil.convertToLatLng(mStartPoint))
		.icon(BitmapDescriptorFactory.fromResource(R.drawable.start)));
		aMap.addMarker(new MarkerOptions()
		.position(AMapUtil.convertToLatLng(mEndPoint))
		.icon(BitmapDescriptorFactory.fromResource(R.drawable.end)));		
	}

	/**
	 * ?????????AMap??????
	 */
	private void init() {
		if (aMap == null) {
			aMap = mapView.getMap();	
		}
		registerListener();
		mRouteSearch = new RouteSearch(this);
		mRouteSearch.setRouteSearchListener(this);
		gasbtn = (TextView)findViewById(R.id.gasbtn);
		ATMbtn= (TextView)findViewById(R.id.ATMbtn);
		Maibtn= (TextView)findViewById(R.id.Maibtn);
		Toibtn= (TextView)findViewById(R.id.Toibtn);
	}

	/**
	 * ????????????
	 */
	private void registerListener() {
		aMap.setOnMapClickListener(RoutePOIActivity.this);
		aMap.setOnMarkerClickListener(RoutePOIActivity.this);
		aMap.setOnInfoWindowClickListener(RoutePOIActivity.this);
		aMap.setInfoWindowAdapter(RoutePOIActivity.this);
		
	}

	@Override
	public View getInfoContents(Marker arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public View getInfoWindow(Marker arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onInfoWindowClick(Marker arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onMarkerClick(Marker marker) {

		return false;
	}

	@Override
	public void onMapClick(LatLng arg0) {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * ??????????????????????????????
	 */
	public void searchRouteResult(int routeType, int mode) {
		if (mStartPoint == null) {
			ToastUtil.show(mContext, "???????????????");
			return;
		}
		if (mEndPoint == null) {
			ToastUtil.show(mContext, "???????????????");
		}
		showProgressDialog();
		final RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(
				mStartPoint, mEndPoint);
		if (routeType == ROUTE_TYPE_DRIVE) {// ??????????????????
			DriveRouteQuery query = new DriveRouteQuery(fromAndTo, mode, null,
					null, "");// ????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
			mRouteSearch.calculateDriveRouteAsyn(query);// ????????????????????????????????????
		}
	}

	@Override
	public void onBusRouteSearched(BusRouteResult result, int errorCode) {
		
	}

	@Override
	public void onDriveRouteSearched(DriveRouteResult result, int errorCode) {
		dissmissProgressDialog();
		aMap.clear();// ?????????????????????????????????
		if (errorCode == AMapException.CODE_AMAP_SUCCESS) {
			if (result != null && result.getPaths() != null) {
				if (result.getPaths().size() > 0) {
					mDriveRouteResult = result;
					final DrivePath drivePath = mDriveRouteResult.getPaths()
							.get(0);
					DrivingRouteOverLay drivingRouteOverlay = new DrivingRouteOverLay(
							mContext, aMap, drivePath,
							mDriveRouteResult.getStartPos(),
							mDriveRouteResult.getTargetPos(), null);
					drivingRouteOverlay.setNodeIconVisibility(false);//????????????marker????????????
					drivingRouteOverlay.setIsColorfulline(false);
					drivingRouteOverlay.setRouteWidth(10);
					drivingRouteOverlay.removeFromMap();
					drivingRouteOverlay.addToMap();
					drivingRouteOverlay.zoomToSpan();

				} else if (result != null && result.getPaths() == null) {
					ToastUtil.show(mContext, R.string.no_result);
				}

			} else {
				ToastUtil.show(mContext, R.string.no_result);
			}
		} else {
			ToastUtil.showerror(this.getApplicationContext(), errorCode);
		}
	}

	@Override
	public void onWalkRouteSearched(WalkRouteResult result, int errorCode) {
		
	}
	

	/**
	 * ???????????????
	 */
	private void showProgressDialog() {
		if (progDialog == null)
			progDialog = new ProgressDialog(this);
		    progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		    progDialog.setIndeterminate(false);
		    progDialog.setCancelable(true);
		    progDialog.setMessage("????????????");
		    progDialog.show();
	    }

	/**
	 * ???????????????
	 */
	private void dissmissProgressDialog() {
		if (progDialog != null) {
			progDialog.dismiss();
		}
	}

	/**
	 * ??????????????????
	 */
	@Override
	protected void onResume() {
		super.onResume();
		mapView.onResume();
	}

	/**
	 * ??????????????????
	 */
	@Override
	protected void onPause() {
		super.onPause();
		mapView.onPause();
	}

	/**
	 * ??????????????????
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mapView.onSaveInstanceState(outState);
	}

	/**
	 * ??????????????????
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mapView.onDestroy();
	}

	@Override
	public void onRideRouteSearched(RideRouteResult arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	public void ongasClick(View view) {
		searchRoutePOI(RoutePOISearchType.TypeGasStation);
		gasbtn.setTextColor(Color.BLUE);
		ATMbtn.setTextColor(Color.GRAY);
		Maibtn.setTextColor(Color.GRAY);
		Toibtn.setTextColor(Color.GRAY);
	}

	public void onATMClick(View view) {
		searchRoutePOI(RoutePOISearchType.TypeATM);
		gasbtn.setTextColor(Color.GRAY);
		ATMbtn.setTextColor(Color.BLUE);
		Maibtn.setTextColor(Color.GRAY);
		Toibtn.setTextColor(Color.GRAY);
	}

	public void onMaiClick(View view) {
		searchRoutePOI(RoutePOISearchType.TypeMaintenanceStation);
		gasbtn.setTextColor(Color.GRAY);
		ATMbtn.setTextColor(Color.GRAY);
		Maibtn.setTextColor(Color.BLUE);
		Toibtn.setTextColor(Color.GRAY);
	}
	
	public void onToiClick(View view) {
		searchRoutePOI(RoutePOISearchType.TypeToilet);
		gasbtn.setTextColor(Color.GRAY);
		ATMbtn.setTextColor(Color.GRAY);
		Maibtn.setTextColor(Color.GRAY);
		Toibtn.setTextColor(Color.BLUE);
	}
	
	private void searchRoutePOI(RoutePOISearchType type) {
		if (overlay != null) {
			overlay.removeFromMap();
		}
		RoutePOISearchQuery query = new RoutePOISearchQuery(mStartPoint ,mEndPoint, mode, type, 250);
		final RoutePOISearch search = new RoutePOISearch(this, query);
		search.setPoiSearchListener(this);
		search.searchRoutePOIAsyn();
		
	}

	@Override
	public void onRoutePoiSearched(RoutePOISearchResult result, int errorCode) {
		if (errorCode == AMapException.CODE_AMAP_SUCCESS) {
			if(result != null){
				List<RoutePOIItem> items = result.getRoutePois();
				if (items != null && items.size() > 0) {
					if (overlay != null) {
						overlay.removeFromMap();
					}
					overlay = new myRoutePoiOverlay(aMap, items);
					overlay.addToMap();
				} else {
					ToastUtil.show(RoutePOIActivity.this,R.string.no_result);
				}
			}
		}else{
			ToastUtil.show(RoutePOIActivity.this, "????????????"+errorCode);
		}
		
	}
	
	
	/**
	 * ?????????PoiOverlay
	 *
	 */
	
	private class myRoutePoiOverlay {
		private AMap mamap;
		private List<RoutePOIItem> mPois;
	    private ArrayList<Marker> mPoiMarks = new ArrayList<Marker>();
		public myRoutePoiOverlay(AMap amap ,List<RoutePOIItem> pois) {
			mamap = amap;
	        mPois = pois;
		}

	    /**
	     * ??????Marker???????????????
	     * @since V2.1.0
	     */
	    public void addToMap() {
	        for (int i = 0; i < mPois.size(); i++) {
	            Marker marker = mamap.addMarker(getMarkerOptions(i));
	            RoutePOIItem item = mPois.get(i);
				marker.setObject(item);
	            mPoiMarks.add(marker);
	        }
	    }

	    /**
	     * ??????PoiOverlay????????????Marker???
	     *
	     * @since V2.1.0
	     */
	    public void removeFromMap() {
	        for (Marker mark : mPoiMarks) {
	            mark.remove();
	        }
	    }

	    /**
	     * ?????????????????????????????????
	     * @since V2.1.0
	     */
	    public void zoomToSpan() {
	        if (mPois != null && mPois.size() > 0) {
	            if (mamap == null)
	                return;
	            LatLngBounds bounds = getLatLngBounds();
	            mamap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
	        }
	    }

	    private LatLngBounds getLatLngBounds() {
	        LatLngBounds.Builder b = LatLngBounds.builder();
	        for (int i = 0; i < mPois.size(); i++) {
	            b.include(new LatLng(mPois.get(i).getPoint().getLatitude(),
	                    mPois.get(i).getPoint().getLongitude()));
	        }
	        return b.build();
	    }

	    private MarkerOptions getMarkerOptions(int index) {
	        return new MarkerOptions()
	                .position(
	                        new LatLng(mPois.get(index).getPoint()
	                                .getLatitude(), mPois.get(index)
	                                .getPoint().getLongitude()))
	                .title(getTitle(index)).snippet(getSnippet(index));
	    }

	    protected String getTitle(int index) {
	        return mPois.get(index).getTitle();
	    }

	    protected String getSnippet(int index) {
	        return mPois.get(index).getDistance() + "???  " + mPois.get(index).getDuration() + "???";
	    }

	    /**
	     * ???marker?????????poi???list????????????
	     *
	     * @param marker ????????????????????????
	     * @return ?????????marker?????????poi???list????????????
	     * @since V2.1.0
	     */
	    public int getPoiIndex(Marker marker) {
	        for (int i = 0; i < mPoiMarks.size(); i++) {
	            if (mPoiMarks.get(i).equals(marker)) {
	                return i;
	            }
	        }
	        return -1;
	    }

	    /**
	     * ?????????index???poi????????????
	     * @param index ?????????poi???
	     * @return poi????????????poi???????????????????????????????????????????????????com.amap.api.services.core???????????? <strong><a href="../../../../../../Search/com/amap/api/services/core/PoiItem.html" title="com.amap.api.services.core?????????">PoiItem</a></strong>???
	     * @since V2.1.0
	     */
	    public RoutePOIItem getPoiItem(int index) {
	        if (index < 0 || index >= mPois.size()) {
	            return null;
	        }
	        return mPois.get(index);
	    }
	}
}

