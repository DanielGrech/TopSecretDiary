package com.DGSD.SecretDiary;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.AbstractCollection;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class Utils {
    public static final String TAG = Utils.class.getSimpleName();

    public static class EXTRA {
        public static final String INTERNAL = "_internal";

        public static final String UPDATE = "_update";

        public static final String ENTRY_ID = "_entry_id";
    }

    public static boolean isTablet(Context context) {
        // Can use static final constants like HONEYCOMB, declared in later versions
        // of the OS since they are inlined at compile time. This is guaranteed behavior.
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) && (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                == Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    public static int getPasswordRating(String password) {
        if (password == null || password.length() < 5) {
            return Password.WEAK;
        }

        int passwordStrength = 0;

        // minimal pw length of 6
        if (password.length() > 5) {
            passwordStrength++;
        }

        // lower and upper case
        if (password.toLowerCase()!= password) {
            passwordStrength += 2;
        }

        // good pw length of 8+
        if (password.length() > 7) {
            passwordStrength += 2;
        }

        int numDigits = getNumberDigits(password);

        // contains digits and non-digits
        if (numDigits > 0 && numDigits != password.length()) {
            passwordStrength += 3;
        }

        if(passwordStrength >= 6 ) {
            return Password.STRONG;
        } else if(passwordStrength >= 4) {
            return Password.OK;
        } else {
            return Password.WEAK;
        }
    }

    public static int getNumberDigits(String inString){
        if (isEmpty(inString)) {
            return 0;
        }

        int numDigits= 0;

        for (int i = 0, size = inString.length(); i < size; i++) {
            if (Character.isDigit(inString.charAt(i))) {
                numDigits++;
            }
        }
        return numDigits;
    }

    public static boolean isEmpty(String inString) {
        return inString == null || inString.length() == 0;
    }

    public static File getTempFile(Context context) throws FileNotFoundException{
        //it will return /data/data/com.DGSD.SecretDiary/image.tmp
        final File path = new File( context.getFilesDir() + "/images/");

        if(!path.exists() && !path.mkdirs()) {
            throw new FileNotFoundException("Cant create file");
        }

        File retval = new File(path, "image.tmp");
        if(!retval.exists()) {
            try {
                Log.d(TAG,"Creating new temp file");
                retval.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Log.d(TAG, "Temp file already exists");
        }
        return retval;
    }

    public static String getPath(Activity activity, Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = activity.managedQuery(uri, projection, null, null, null);
        int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    public static String join(AbstractCollection<String> s, String delimiter) {
        if (s.isEmpty()) {
            return "";
        }

        Iterator<String> iter = s.iterator();
        StringBuffer buffer = new StringBuffer(iter.next());

        while (iter.hasNext()) {
            buffer.append(delimiter).append(iter.next());
        }

        return buffer.toString();
    }

    public static List<String> unjoin(String string, String joiner) {
        if(string == null || string.length() == 0) {
            return null;
        }

        System.err.println("UNJOINING STRING: " + string);
        return Arrays.asList(string.split(joiner));
    }

    public static Bitmap decodeFile(File f){
        try {
            //Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f),null,o);

            //The new size we want to scale to
            final int REQUIRED_SIZE=70;

            //Find the correct scale value. It should be the power of 2.
            int width_tmp=o.outWidth, height_tmp=o.outHeight;
            int scale=1;
            while(true){
                if(width_tmp/2<REQUIRED_SIZE || height_tmp/2<REQUIRED_SIZE)
                    break;
                width_tmp/=2;
                height_tmp/=2;
                scale*=2;
            }

            //Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize=scale;
            return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
        } catch (FileNotFoundException e) {}
        return null;
    }

    public static Location getLocation(Context c, int minDistance, long minTime) {
        Location bestResult = null;
        float bestAccuracy = Float.MAX_VALUE;
        long bestTime = Long.MIN_VALUE;

        LocationManager lm = (LocationManager) c.getSystemService(Context.LOCATION_SERVICE);

        List<String> matchingProviders = lm.getAllProviders();
        for (String provider: matchingProviders) {
            Location location = lm.getLastKnownLocation(provider);
            if (location != null) {
                float accuracy = location.getAccuracy();
                long time = location.getTime();

                if ((time > minTime && accuracy < bestAccuracy)) {
                    bestResult = location;
                    bestAccuracy = accuracy;
                    bestTime = time;
                }
                else if (time < minTime && bestAccuracy == Float.MAX_VALUE && time > bestTime) {
                    bestResult = location;
                    bestTime = time;
                }
            }
        }

        return bestResult;
    }

    public static void hideKeyboard(View v) {
        InputMethodManager imm =
                (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }


    public static class Password {
        public static final int WEAK = 0;
        public static final int OK = 1;
        public static final int STRONG = 2;
    }

}
