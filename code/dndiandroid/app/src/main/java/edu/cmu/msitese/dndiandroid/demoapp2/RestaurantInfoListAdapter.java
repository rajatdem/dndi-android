package edu.cmu.msitese.dndiandroid.demoapp2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import edu.cmu.msitese.dndiandroid.R;

/**
 * Created by Yu-Lun Tsai on 31/07/2017.
 */

public class RestaurantInfoListAdapter extends ArrayAdapter<RestaurantInfoCell> {

    private Context mContext;

    public RestaurantInfoListAdapter(Context context, List<RestaurantInfoCell> restaurants) {
        super(context, 0, restaurants);
        mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // Get the data item for this position
        RestaurantInfoCell cell = getItem(position);
        ImageViewHolder viewHolder;

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(
                    getContext()).inflate(R.layout.listview_cell, parent, false);
            viewHolder = new ImageViewHolder();
            viewHolder.imageView = (ImageView) convertView.findViewById(R.id.restaurantImage);
            convertView.setTag(viewHolder);
        }

        viewHolder = (ImageViewHolder)convertView.getTag();
        if(viewHolder.bitmap == null){
            // get corresponding profile URL asynchronously
            viewHolder.imageUrl = cell.getImageUrl();
            new GetImageInViewHolderTask(mContext).execute(viewHolder);
        }
        else{
            viewHolder.imageView.setImageBitmap(viewHolder.bitmap);
        }

        // Lookup view for data population
        TextView nameLabel = (TextView) convertView.findViewById(R.id.nameTextLabel);
        TextView descLabel = (TextView) convertView.findViewById(R.id.addrTextLabel);

        // Populate the data into the template view using the data object
        nameLabel.setText(cell.getName());
        descLabel.setText(cell.getDescription());

        // Return the completed view to render on screen
        return convertView;
    }
}