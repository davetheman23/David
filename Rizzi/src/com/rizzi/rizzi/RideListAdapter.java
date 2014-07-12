package com.rizzi.rizzi;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.rizzi.rizzi.parseclasses.ParseUserHelper;
import com.rizzi.rizzi.parseclasses.TripPosts;
import com.squareup.picasso.Picasso;

public class RideListAdapter extends ArrayAdapter<Map<String,Object>> {
	
	private int mRowLayout;
	
	private Context mContext;
	
	class ViewHolder{
		ImageView iv_profile_pic;
		TextView tv_affinity_description;
		TextView tv_post_description;
	}

	public RideListAdapter(Context context, int resource, 
							List<Map<String,Object>> objects) {
		super(context, resource, objects);
		mRowLayout = resource;
		
		mContext = context;
	}
	
	/*
	 *  each row in the list will call getView, this implementation deterimes
	 *  the behavior and layout of each row of the list
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		
		// reuse views - for faster loading, avoid inflation everytime
		ViewHolder viewHolder = null;
		View rowview = convertView;
		if (rowview == null){
			LayoutInflater inflater = (LayoutInflater) mContext.
						getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			// if no rowview before, new viewholder is created
			viewHolder = new ViewHolder();
			
			// inflate the layout view, and get individual views
			rowview = inflater.inflate(mRowLayout, parent, false);
			viewHolder.iv_profile_pic = (ImageView) rowview.findViewById(
												R.id.list_post_item_iv_profile_pic);
			viewHolder.tv_affinity_description =(TextView) rowview.findViewById(
										R.id.list_post_item_tv_affinity_description);	
			viewHolder.tv_post_description = (TextView) rowview.findViewById(
												R.id.list_post_item_tv_description);
			// set tag for future reuse of the view
			rowview.setTag(viewHolder);
		}else{
			viewHolder = (ViewHolder) rowview.getTag();
		}
		

		Map<String,Object> result = getItem(position);
		
		String postDescription = result.get(TripPosts.KEY_DESCRIPTION).toString();
		String facebookId = result.get(ParseUserHelper.KEY_FB_ID).toString();
		SimpleDateFormat dateFormat = new SimpleDateFormat("MMM-dd hh:mm:ss");
		Date departTime = (Date)result.get(TripPosts.KEY_DEPART_AT);
		viewHolder.tv_post_description.setText("plan to leave at " + 
											dateFormat.format(departTime));
		
		//String title = enquiry.get(TAG_TITLE).toString();
	
		//int status = Integer.parseInt(enquiry.get(TAG_STATUS).toString());
		//int status_color = android.R.color.black;
		
		
		//viewHolder.enqueryID = Integer.parseInt(queryID);
		
		
		// image loading procedure:
		// 1. check if image available in memory / disk
		// 2. set image if not in memory then fetch from URL
		// Note: currently, use picasso instead 
		String urlGetFBPic = ParseUserHelper.URL_FB_GRAPH
				+ facebookId + "/picture?type=square&height=80&width=80";
		Picasso.with(mContext)
				.load(urlGetFBPic)
				.placeholder(R.drawable.someone)
				.resize(80, 80)
				.into(viewHolder.iv_profile_pic);

		return rowview;
	}

	

}
