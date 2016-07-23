package net.mobfish.sellfisch.kupershot.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Bajic Dusko (www.bajicdusko.com) on 23-Jul-16.
 */
public class ImageUtility {

    public static String IMAGE_DIRECTORY = "/mobfish/kupershot/";
    public static String IMAGE_NAME = "kupershot_image.jpg";
    private static String IMAGE_COMPRESSED_NAME = "kupershot_image_compressed.jpg";

    public static String compressImage() {
        try {
            FileInputStream finOne = new FileInputStream(new File(Environment.getExternalStorageDirectory() + IMAGE_DIRECTORY + IMAGE_NAME));
            FileInputStream finTwo = new FileInputStream(new File(Environment.getExternalStorageDirectory() + IMAGE_DIRECTORY + IMAGE_NAME));

            //get image orientation
            ExifInterface ei = new ExifInterface(new File(Environment.getExternalStorageDirectory() + IMAGE_DIRECTORY + IMAGE_NAME).getAbsolutePath());
            int originalOrientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            Bitmap sampledBitmap = decodeSampledBitmap(finOne, finTwo);
            finOne.close();
            finTwo.close();

            File compessedFile = new File(Environment.getExternalStorageDirectory() + IMAGE_DIRECTORY + IMAGE_COMPRESSED_NAME);
            FileOutputStream compressedOutputStream = new FileOutputStream(compessedFile);
            sampledBitmap.compress(Bitmap.CompressFormat.JPEG, 50, compressedOutputStream);
            compressedOutputStream.flush();
            compressedOutputStream.close();


            //set original image orientation
            ExifInterface ei2 = new ExifInterface(compessedFile.getAbsolutePath());
            ei2.setAttribute(ExifInterface.TAG_ORIENTATION, String.valueOf(originalOrientation));
            ei2.saveAttributes();

            return compessedFile.getAbsolutePath();
        } catch (Exception ex) {
            return null;
        }
    }

    public static Bitmap decodeSampledBitmap(InputStream streamOne, InputStream streamTwo) throws IOException {

        int dimm = 2000;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        // First decode with inJustDecodeBounds=true to check dimensions
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(streamOne, null, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, dimm, dimm);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        Bitmap b = BitmapFactory.decodeStream(streamTwo, null, options);
        return b;
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

}
