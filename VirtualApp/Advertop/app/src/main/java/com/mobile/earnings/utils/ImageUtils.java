package com.mobile.earnings.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;




public class ImageUtils{

	private static final int IMAGE_WIDTH = 500, IMAGE_HEIGHT = 500;

	@SuppressLint("NewApi")
	public static String getRealPathFromURI_API19(Context context, Uri uri){
		if(Uri.EMPTY.equals(uri)){
			return null;
		}
		String filePath = "";
		try{
			String wholeID = DocumentsContract.getDocumentId(uri);

			String id = wholeID.split(":")[1];

			String[] column = {MediaStore.Images.Media.DATA};

			// where id is equal to
			String sel = MediaStore.Images.Media._ID + "=?";

			Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, column, sel, new String[]{id}, null);

			int columnIndex = cursor.getColumnIndex(column[0]);

			if(cursor.moveToFirst()) {
				filePath = cursor.getString(columnIndex);
			}
			if(cursor != null) {
				cursor.close();
			}
		} catch(IllegalArgumentException e){
			String[] proj = {MediaStore.Images.Media.DATA};
			Cursor cursor = context.getContentResolver().query(uri, proj, null, null, null);
			if(cursor != null) {
				int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
				cursor.moveToFirst();
				filePath = cursor.getString(column_index);
				cursor.close();
			}
		}
		return filePath;
	}


	@SuppressLint("NewApi")
	public static String getRealPathFromURI_API11to18(Context context, Uri contentUri){
		if(Uri.EMPTY.equals(contentUri)){
			return null;
		}
		String[] proj = {MediaStore.Images.Media.DATA};
		String result = null;

		CursorLoader cursorLoader = new CursorLoader(context, contentUri, proj, null, null, null);
		Cursor cursor = cursorLoader.loadInBackground();

		if(cursor != null) {
			int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			result = cursor.getString(column_index);
		}
		return result;
	}

	@Nullable
	public static Bitmap saveSelectedImage(Context context, Intent data, String imageName){
		File photoDirectory = new File(Environment.getExternalStorageDirectory(), "/LiberPhotos/");
		Bitmap image = null;
		if(data != null) {
			try{
				Uri imagePath = data.getData();
				image = ImageUtils.decodeSampledBitmapFromUri(context, imagePath, IMAGE_WIDTH, IMAGE_HEIGHT);
				ByteArrayOutputStream bytes = new ByteArrayOutputStream();
				if(image != null) {
					image.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
					File destination = new File(photoDirectory + imageName);
				}
			} catch(IOException e){
				e.printStackTrace();
			}
		}
		return image;
	}

	private static Bitmap decodeSampledBitmapFromUri(Context context, Uri imageUri, int reqWidth, int reqHeight) throws IOException{
		// Get input stream of the image
		final BitmapFactory.Options options = new BitmapFactory.Options();
		// First decode with inJustDecodeBounds=true to check dimensions
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeStream(context.getContentResolver().openInputStream(imageUri), null, options);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeStream(context.getContentResolver().openInputStream(imageUri), null, options);
	}

	private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight){
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if(height > reqHeight || width > reqWidth) {

			final int halfHeight = height / 2;
			final int halfWidth = width / 2;

			// Calculate the largest inSampleSize value that is a power of 2 and keeps both
			// height and width larger than the requested height and width.
			while((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth){
				inSampleSize *= 2;
			}
		}

		return inSampleSize;
	}
}
