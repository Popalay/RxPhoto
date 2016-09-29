package com.github.oliveiradev.lib;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.IOException;
import java.io.InputStream;

import rx.Observable;
import rx.functions.Func1;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

public final class RxPhoto {

    private static Subject<Uri, Uri> subject = new SerializedSubject(PublishSubject.create());
    private static Context mContext; //use only Application Context

    public static Observable<Uri> request(Context context, TypeRequest typeRequest) {
        mContext = context;
        startOverlapActivity(typeRequest);
        subject = Factory.create();
        return subject;
    }

    public static Observable<Bitmap> getBitmap(Observable<Uri> observable) {
        return observable.map(new Func1<Uri, Bitmap>() {
            @Override
            public Bitmap call(Uri uri) {
                try {
                    return getBitmapFromStream(uri);
                } catch (IOException e) {
                    e.printStackTrace();
                    Observable.error(e);
                    return null;
                }
            }
        });
    }

    protected static void onActivityResult(Uri uri) {
        if (uri != null) {
            subject.onNext(uri);
            subject.onCompleted();
        }
    }

    private static void startOverlapActivity(TypeRequest typeRequest) {
        Intent intent = new Intent(mContext, OverlapActivity.class);
        intent.putExtra("enum", typeRequest);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }

    private static Bitmap getBitmapFromStream(Uri url) throws IOException {
        InputStream stream = mContext.getContentResolver().openInputStream(url);
        Bitmap bitmap = BitmapFactory.decodeStream(stream);
        if (stream != null)
            stream.close();
        return bitmap;
    }

    private static class Factory {

        public static Subject create() {
            return new SerializedSubject(PublishSubject.create());
        }
    }
}