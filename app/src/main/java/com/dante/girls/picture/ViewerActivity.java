package com.dante.girls.picture;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.SharedElementCallback;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import com.dante.girls.R;
import com.dante.girls.base.BaseActivity;
import com.dante.girls.base.Constants;
import com.dante.girls.model.DB;
import com.dante.girls.model.Image;
import com.dante.girls.utils.SPUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import io.realm.RealmChangeListener;

@TargetApi(Build.VERSION_CODES.KITKAT)
public class ViewerActivity extends BaseActivity implements RealmChangeListener {
    private static final String TAG = "DetailActivity";
    private static final int SYSTEM_UI_SHOW = View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
    private static final int SYSTEM_UI_HIDE = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_FULLSCREEN;
    @BindView(R.id.pager)
    ViewPager pager;
    @BindView(R.id.container)
    FrameLayout container;
    private DetailPagerAdapter adapter;
    private int currentPosition;
    private int type;
    private List<Image> images;
    private boolean isSystemUiShown = true;
    private int position;

    @Override
    protected void onPause() {
        super.onPause();
        SPUtil.save(type + Constants.POSITION, currentPosition);
    }

    @Override
    protected int initLayoutId() {
        return R.layout.activity_detail;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void initViews() {
        super.initViews();
        supportPostponeEnterTransition();
        position = getIntent().getIntExtra(Constants.POSITION, 0);
        currentPosition = position;
        List<Fragment> fragments = new ArrayList<>();

        type = getIntent().getIntExtra(Constants.TYPE, 0);
        images = DB.getImages(realm, type);

        for (int i = 0; i < images.size(); i++) {
            fragments.add(ViewerFragment.newInstance(images.get(i).url));
        }
        adapter = new DetailPagerAdapter(getSupportFragmentManager(), fragments);
        pager.setAdapter(adapter);
        pager.setOffscreenPageLimit(2);
        pager.setCurrentItem(position);
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                currentPosition = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (isSystemUiShown) hideSystemUi();
            }
        });

        // 避免图片在进行 Shared Element Transition 时盖过 Toolbar
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setSharedElementsUseOverlay(false);
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        supportFinishAfterTransition();
    }

    @Override
    public void supportFinishAfterTransition() {
//        EventBus.getDefault().post(new MessageEvent(currentPosition));
        if (isPositionChanged()) {
            setShareElement();
            finish();
        } else {
            super.supportFinishAfterTransition();
        }
    }

    private void setShareElement() {
        setEnterSharedElementCallback(new SharedElementCallback() {
            @Override
            public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                super.onMapSharedElements(names, sharedElements);
                names.clear();
                Log.i(TAG, "onMapSharedElements: clear");
//                names.add(currentUrl());
                sharedElements.clear();
//                sharedElements.put(currentUrl(), adapter.getCurrent().getSharedElement());
            }
        });
    }

    private boolean isPositionChanged() {
        return position != currentPosition;
    }

    public String currentUrl() {
        return images.get(currentPosition).url;
    }

    public void toggleSystemUI() {
        if (isSystemUiShown) {
            hideSystemUi();
        } else {
            showSystemUi();
        }
    }

    public void showSystemUi() {
        pager.setSystemUiVisibility(SYSTEM_UI_SHOW);
        isSystemUiShown = true;
    }

    public void hideSystemUi() {
        pager.setSystemUiVisibility(SYSTEM_UI_HIDE);
        isSystemUiShown = false;
    }

    @Override
    public void onChange(Object element) {
        adapter.notifyDataSetChanged();
    }


    private class DetailPagerAdapter extends FragmentStatePagerAdapter {
        private List<Fragment> fragments;

        DetailPagerAdapter(FragmentManager fm, List<Fragment> fragments) {
            super(fm);
            this.fragments = fragments;
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        ViewerFragment getCurrent() {
            return (ViewerFragment) adapter.instantiateItem(pager, pager.getCurrentItem());
        }
    }
}
