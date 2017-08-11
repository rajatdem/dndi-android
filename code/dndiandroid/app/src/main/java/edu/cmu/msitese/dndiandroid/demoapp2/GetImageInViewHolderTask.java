package edu.cmu.msitese.dndiandroid.demoapp2;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Yu-Lun Tsai on 31/07/2017.
 */

public class GetImageInViewHolderTask extends AsyncTask<ImageViewHolder, Void, ImageViewHolder> {

    private static final String TAG = "YELP_DEMO";

    private Context mContext;

    public GetImageInViewHolderTask(Context context){
        mContext = context;
    }

    @Override
    protected ImageViewHolder doInBackground(ImageViewHolder... params) {

        ImageViewHolder viewHolder = params[0];
        try {
            URL url = new URL(viewHolder.imageUrl);
            viewHolder.bitmap = BitmapFactory.decodeStream(url.openStream());
        } catch(MalformedURLException e1){
            Log.e(TAG, "Malformed url exception: " + e1.getMessage());
            viewHolder.bitmap = null;
        } catch (IOException e2) {
            Log.e(TAG, "IO exception: " + e2.getMessage());
            viewHolder.bitmap = null;
        }
        return viewHolder;
    }

    @Override
    protected void onPostExecute(ImageViewHolder result) {
        super.onPostExecute(result);

        Drawable imageDrawable = result.imageView.getDrawable();
        result.imageView.setImageDrawable(new BitmapDrawable(mContext.getResources(), result.bitmap));

        if (imageDrawable != null){
            BitmapDrawable bitmapDrawable = ((BitmapDrawable) imageDrawable);
            if (!bitmapDrawable.getBitmap().isRecycled()) {
                bitmapDrawable.getBitmap().recycle();
            }
        }
    }
}
