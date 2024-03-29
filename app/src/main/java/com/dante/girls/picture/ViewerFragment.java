package com.dante.girls.picture;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.view.View;

import com.dante.girls.R;
import com.dante.girls.base.BaseFragment;
import com.dante.girls.base.Constants;
import com.dante.girls.lib.TouchImageView;
import com.dante.girls.model.Image;
import com.dante.girls.utils.BitmapUtil;
import com.dante.girls.utils.BlurBuilder;
import com.dante.girls.utils.SPUtil;
import com.dante.girls.utils.Share;
import com.dante.girls.utils.UI;
import com.tbruyelle.rxpermissions.RxPermissions;

import java.io.File;
import java.util.concurrent.ExecutionException;

import butterknife.BindView;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.schedulers.Schedulers;


/**
 * Photo view fragment.
 */
public class ViewerFragment extends BaseFragment implements View.OnLongClickListener, View.OnClickListener {

    private static final String TAG = "test";
    @BindView(R.id.headImage)
    TouchImageView imageView;
    private String url;
    private ViewerActivity context;
    private Bitmap bitmap;

    public static ViewerFragment newInstance(String url) {
        ViewerFragment fragment = new ViewerFragment();
        Bundle args = new Bundle();
        args.putString(Constants.URL, url);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int initLayoutId() {
        return R.layout.fragment_viewer;
    }

    @Override
    protected void initViews() {
        context = (ViewerActivity) getActivity();
        url = getArguments().getString(Constants.URL);
        ViewCompat.setTransitionName(imageView, url);
        load();
    }

    private void showHint() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.hint).
                setMessage(R.string.browse_picture_hint).
                setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).
                create().show();
    }

    @Override
    protected void initData() {
        imageView.setOnClickListener(this);
        imageView.setOnLongClickListener(this);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public boolean onLongClick(View v) {
        blur(bitmap);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final String[] items = {getString(R.string.share_to), getString(R.string.save_img)};
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    share(bitmap);
                } else if (which == 1) {
                    context.hideSystemUi();
                    save(bitmap);
                }
            }
        }).setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                imageView.setImageBitmap(bitmap);
                context.hideSystemUi();
            }
        }).show();
        return true;
    }

    private void blur(Bitmap bitmap) {
        Observable.just(bitmap)
                .map(new Func1<Bitmap, Bitmap>() {
                    @Override
                    public Bitmap call(Bitmap bitmap) {
                        return BlurBuilder.blur(bitmap);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Bitmap>() {
                    @Override
                    public void call(Bitmap bitmap) {
                        imageView.setImageBitmap(bitmap);
                    }
                });
    }

    private void save(final Bitmap bitmap) {
        RxPermissions permissions = new RxPermissions(context);
        permissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .map(new Func1<Boolean, File>() {
                    @Override
                    public File call(Boolean granted) {
                        return BitmapUtil.writeToFile(bitmap);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<File>() {
                    @Override
                    public void call(File file) {
                        if (file != null && file.exists()) {
                            UI.showSnack(rootView, getString(R.string.save_img_success)
                                    + file.getPath());

                        } else {
                            UI.showSnack(rootView, R.string.save_img_failed);
                        }
                    }
                });
    }

    private void share(final Bitmap bitmap) {
        RxPermissions permissions = new RxPermissions(context);
        permissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .map(new Func1<Boolean, Uri>() {
                    @Override
                    public Uri call(Boolean granted) {
                        if (granted) {
                            return BitmapUtil.bitmapToUri(bitmap);
                        }
                        return null;

                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Uri>() {
                    @Override
                    public void call(Uri uri) {
                        Share.shareImage(context, uri);
                    }
                });

    }

    @Override
    public void onClick(View v) {
        if (!SPUtil.getBoolean(Constants.HAS_HINT)) {
            showHint();
            SPUtil.save(Constants.HAS_HINT, true);
        } else {
            context.supportFinishAfterTransition();
        }
    }

    private void load() {
        Observable.defer(new Func0<Observable<Bitmap>>() {
            @Override
            public Observable<Bitmap> call() {
                try {
                    return Observable.just(Image.getBitmap(context, url));
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
                return null;
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Bitmap>() {
                    @Override
                    public void call(Bitmap b) {
                        bitmap = b;
                        imageView.setImageBitmap(b);
                        context.supportStartPostponedEnterTransition();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        throwable.printStackTrace();
                    }
                });
    }

    public View getSharedElement() {
        return imageView;
    }

}
