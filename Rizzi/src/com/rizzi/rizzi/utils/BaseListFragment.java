package com.rizzi.rizzi.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Activity;
import android.content.Context;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.rizzi.rizzi.R;


public abstract class BaseListFragment extends ListFragment{
	
	//public static final String PREFS_NAME = "PetInAmerica_ListArticles";
	//public static final String KEY_ARTICLE_READ = "Article_Read";

	private static final String TAG = "BaseListFragment";
	private static final String KEY_ERR = "error";
	
	/*
	 * the key at the first level of the JSON response object
	 * Note: don't make it static, because each instance of this fragment class
	 *       may have different key_list value. 
	 */
	private String KEY_LIST;
	
	/*
	 * mPage will be appended to the URL, for some API calls, it indicates 
	 * the page of the list, for other API calls, it could mean userid.  
	 */
	private int mPage = 1;
	//private boolean mflag_addData = false;		// false-don't add data
	private boolean mIsUserSpecific = false;	// flag for user specific data
	private boolean mHasFooter = true;			// flag to indicate if footer is needed
	private ArrayAdapter<Map<String, Object>> mBaseAdapter;
	private Context mContext;
	private String mUrl;
	private View mfooterview = null; 
	
	
	/**
	 * Set the custom list adapter to use when the Http request is completed,
	 * simply call function setCustomAdapter() passing in an instance of the 
	 * custom listadapter
	 * 
	 * @param resultArray  - the resulting data 
	 */
	protected abstract void onHttpDoneSetAdapter(List<Map<String, Object>> resultArray);
	
	/**
	 * when the http request is completed, if the listadapter is not null, 
	 * data will be added to the list adapter. please specify how the data can be added
	 * to the current adapter, use getCustomAdatper() to obtain an instance of the the 
	 * current custom adapter  
	 * 
	 * @param resultArray  - the resulting data 
	 */
	protected abstract void onHttpDoneAddData(List<Map<String, Object>> resultArray);
	
	protected abstract void onItemClickAction(View v, int position, long id);
	
	public void setPage(int page){
		mPage = page;
	}
	
	public int getPage(){
		return mPage;
	}
		
	public void setParameters(String url,  String jsonListKey, boolean hasfooter) {
		mUrl = url;
		KEY_LIST = jsonListKey;
		mHasFooter = hasfooter;
	}
	public void setUserDataFlag(boolean isUserSpecific){
		mIsUserSpecific = isUserSpecific;
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mContext = activity.getApplicationContext();
		
	}
	
	public void loadListInBackground(){
		// fetch list data from the network
		new HttpPostTask().execute(mUrl + Integer.toString(mPage));
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		if (getListAdapter() == null){
			// first time the view is created
			loadListInBackground();
		}
		
		// set up footer 
		if (mHasFooter){
			LayoutInflater inflater = (LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			mfooterview = (View) inflater.inflate(R.layout.list_footer, null);
			getListView().addFooterView(mfooterview);
			getListView().setFooterDividersEnabled(true);
			// set the footer as invisible only make it visible when needed
			mfooterview.setVisibility(View.GONE);
			mfooterview.setClickable(false);
		}
		
		// disable scroll bar
		getListView().setVerticalScrollBarEnabled(false);		
		
		// monitor scroll activity, add more articles when scroll close to bottom
		getListView().setOnScrollListener(new OnScrollListener() {
			/*
			 * this listener is used to continuously load more article when scroll down to bottom
			 */
		@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
			}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				if(firstVisibleItem + visibleItemCount == totalItemCount - 1 && totalItemCount != 0)
				{
					
					// when the visible item reaches the last item, 
					if (getListAdapter() != null){
						mPage += 1;
						loadListInBackground();
						if (mfooterview != null){
							mfooterview.setVisibility(View.VISIBLE);
						}
					}
				}
			}
		});
		
	}
	

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		if (position == getListView().getCount() - 1){
			// if footer is clicked, this assumes footer exists
			return;
		}
		onItemClickAction(v, position, id);
	}
	
	/**
	 * This function displays no content available when the 
	 * list is empty
	 * Overwriting this function to customize actions to be taken 
	 * when the list is empty 
	 */
	protected void handleEmptyList(){
		if (mfooterview!=null){
			TextView tvFooter = (TextView) mfooterview
					.findViewById(R.id.list_footer_tv_loading);
			tvFooter.setText("No content available");
			
			ProgressBar pbFooter = (ProgressBar) mfooterview
							.findViewById(R.id.list_footer_pb_loading);
			pbFooter.setVisibility(View.INVISIBLE);
		}
		
	}
	
	/**
	 * This function displays "the end of the list" when the no more
	 * list items is available 
	 * Overwriting this function to customize actions to be taken 
	 * when the list is complete
	 */
	protected void handleEndofList(){
		if (mfooterview!=null){
			TextView tvFooter = (TextView) mfooterview
					.findViewById(R.id.list_footer_tv_loading);
			tvFooter.setText("End of list");
			
			ProgressBar pbFooter = (ProgressBar) mfooterview
							.findViewById(R.id.list_footer_pb_loading);
			pbFooter.setVisibility(View.INVISIBLE);
		}
	}
	
	@SuppressWarnings("unchecked")
	protected ArrayList<Map<String, Object>> ParseHttpResponseJson(
			String JSONResponse) throws JSONException{
		// -- Parse Json object, 
		JSONObject responseObject = null;
		JSONArray responseArray = null;
		List<Map<String, Object>> arrayList = new ArrayList<Map<String, Object>>();
		responseObject = (JSONObject) new JSONTokener(
				JSONResponse).nextValue();
		if (responseObject !=null){
			/*int errorCode = responseObject.getInt(KEY_ERR);
			if (errorCode != Constants.NO_ERROR){
				return arrayList;
			}*/
			String listObject = responseObject.getString(KEY_LIST);
			if (listObject.equalsIgnoreCase("null")){
				return (ArrayList<Map<String, Object>>) arrayList;
			}
			responseArray = responseObject.getJSONArray(KEY_LIST);
			if (responseArray != null){
				arrayList = JsonHelper.toList(responseArray);
				return (ArrayList<Map<String, Object>>) arrayList;
			}
		}
		return null;
	}
	
	/**
	 * add necessary parameters to the http post, for example adding userinfo 
	 * and token to the post to facilitate URL authentication   
	 * @param post	
	 * @return the post with appended parameters or itself
	 */
	protected HttpPost addParameterstoUrlPost(HttpPost post){
		return post;
	}

	protected void setCustomAdapter(ArrayAdapter<Map<String, Object>> customAdapter){
		mBaseAdapter = customAdapter;
		setListAdapter(mBaseAdapter);
	}
	protected ArrayAdapter<Map<String, Object>> getCustomAdapter(){
		return mBaseAdapter;
	}

	private class HttpPostTask extends AsyncTask<String, Void, List<Map<String, Object>>> {

		AndroidHttpClient mClient = AndroidHttpClient.newInstance("");
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			
		}

		@Override
		protected List<Map<String, Object>> doInBackground(String... params) {
			String url = params[0];
			HttpPost post = new HttpPost(url);
			
			post = addParameterstoUrlPost(post);
			
			HttpResponse response = null;		
			String JSONResponse = null;
			try {
				response = mClient.execute(post);
				JSONResponse = new BasicResponseHandler()
				.handleResponse(response);
				
				return ParseHttpResponseJson(JSONResponse);
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}catch (JSONException e) {
				e.printStackTrace();
				Log.e(TAG, "JSONException");
				Log.e(TAG, "Currently Loading URL:" + url);
				// TODO Log.d(TAG, "Please handle exception here");
			}finally{
				if (null != mClient){
					mClient.close();
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(List<Map<String, Object>> resultArray) {
			if (isAdded() && resultArray != null){		
			// always test isAdded for a fragment, this help make sure
			// the getActivity doesn't return null pointer
				if (resultArray.size() > 0 ){
					 if (getListAdapter() == null){
						 onHttpDoneSetAdapter(resultArray);
					 }else{
						 onHttpDoneAddData(resultArray);
					 }
				}else{
					// no more list items to be displayed
					// handle it
					if (getListView() == null && getListView().getCount() < 1){
						handleEmptyList();;
					}else{
						handleEndofList();
					}
				}
				
			}
			else{
				Log.d(TAG, "Need to handle null return result cases");
			}
		}
	}

}
