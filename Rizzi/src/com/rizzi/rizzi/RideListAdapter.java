package com.rizzi.rizzi;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

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
												R.id.post_list_profile_pic);
			viewHolder.tv_affinity_description =(TextView) rowview.findViewById(
											R.id.post_list_tv_affinity_description);	
			viewHolder.tv_post_description = (TextView) rowview.findViewById(
												R.id.post_list_tv_description);
			// set tag for future reuse of the view
			rowview.setTag(viewHolder);
		}else{
			viewHolder = (ViewHolder) rowview.getTag();
			//viewHolder.iv.setImageDrawable(mContext.getResources().getDrawable(R.drawable.someone));
		}
		
		
		//Map<String,String> enquiry = mEnquiries.get(position);
		Map<String,Object> result = getItem(position);
		
		//String title = enquiry.get(TAG_TITLE).toString();
	
		//int status = Integer.parseInt(enquiry.get(TAG_STATUS).toString());
		//int status_color = android.R.color.black;
		
		
		//viewHolder.enqueryID = Integer.parseInt(queryID);
		
		
		// image loading procedure:
		// 1. check if image available in memory / disk
		// 2. set image if not in memory then fetch from URL
		// Note: currently, use picasso instead 
		/*Picasso.with(mContext)
				.load(userAvatarURL)
				.placeholder(R.drawable.someone)
				.resize(70, 70)
				.into(viewHolder.iv);
		
		if (userAvatarURL ==null || userAvatarURL.endsWith("someone.png")){			
			// cancel request when download is not needed
			Picasso.with(mContext)
				.cancelRequest(viewHolder.iv);
		}*/

		return rowview;
	}

	

}
