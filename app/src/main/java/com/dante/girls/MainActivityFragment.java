package com.dante.girls;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.SharedElementCallback;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.View;

import com.dante.girls.base.BaseFragment;
import com.dante.girls.model.MessageEvent;
import com.dante.girls.picture.PictureFragment;
import com.dante.girls.picture.RecyclerFragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import butterknife.BindView;

import static com.bumptech.glide.gifdecoder.GifHeaderParser.TAG;
import static com.dante.girls.net.API.TYPE_DB_BREAST;
import static com.dante.girls.net.API.TYPE_DB_BUTT;
import static com.dante.girls.net.API.TYPE_DB_LEG;
import static com.dante.girls.net.API.TYPE_DB_RANK;
import static com.dante.girls.net.API.TYPE_DB_SILK;
import static com.dante.girls.net.API.TYPE_GANK;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends BaseFragment {
    private static final int SMOOTH_SCROLL_POSITION = 50;
    @BindView(R.id.pager)
    ViewPager pager;
    @BindView(R.id.tabs)
    TabLayout tabs;
    private List<RecyclerFragment> fragments = new ArrayList<>();
    private TabPagerAdapter adapter;
    private Bundle reenterState;

    @Override
    protected int initLayoutId() {
        return R.layout.fragment_tab;
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void initViews() {
        adapter = new TabPagerAdapter(getChildFragmentManager());
        initFragments();
        pager.setAdapter(adapter);
        tabs.setTabMode(TabLayout.MODE_SCROLLABLE);
        tabs.setupWithViewPager(pager);
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                pager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                scrollToTop(fragments.get(tab.getPosition()).getRecyclerView());
            }
        });
    }

    @Override
    protected void initData() {
        setExitSharedElementCallback(new SharedElementCallback() {
            @Override
            public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                if (reenterState != null) {
                    int i = reenterState.getInt("index", 0);
                    Log.d(TAG, "reenter from " + i);
                    sharedElements.clear();
                    sharedElements.put(adapter.getCurrent().getData(i).url, adapter.getCurrent().getRecyclerView().getLayoutManager().findViewByPosition(i));
                    reenterState = null;
                }
            }
        });
    }

    private void initFragments() {
        List<String> titles = Arrays.asList(getString(R.string.gank),
                getString(R.string.db_rank),
                getString(R.string.db_leg),
                getString(R.string.db_silk),
                getString(R.string.db_breast),
                getString(R.string.db_butt));
        fragments.add(PictureFragment.newInstance(TYPE_GANK));
        fragments.add(PictureFragment.newInstance(TYPE_DB_RANK));
        fragments.add(PictureFragment.newInstance(TYPE_DB_LEG));
        fragments.add(PictureFragment.newInstance(TYPE_DB_SILK));
        fragments.add(PictureFragment.newInstance(TYPE_DB_BREAST));
        fragments.add(PictureFragment.newInstance(TYPE_DB_BUTT));
        if (fragments.size() != titles.size()) {
            throw new IllegalArgumentException("You need add all fragments in " + getClass().getSimpleName());
        }
        adapter.setFragments(fragments, titles);
    }

    private void scrollToTop(RecyclerView list) {
        if (null != list) {
            int lastPosition;

            StaggeredGridLayoutManager manager = (StaggeredGridLayoutManager) list.getLayoutManager();
            lastPosition = manager.findLastVisibleItemPositions(
                    new int[manager.getSpanCount()])[1];


            if (lastPosition < SMOOTH_SCROLL_POSITION) {
                list.smoothScrollToPosition(0);
            } else {
                list.scrollToPosition(0);
            }
        }
    }

    @Subscribe
    public void onReenter(MessageEvent data) {
        Log.i(TAG, "onReenter: ");
//        getActivity().supportPostponeEnterTransition();
//        final int index = data.index;
//        final RecyclerView recyclerView = adapter.getCurrent().getRecyclerView();
//        recyclerView.smoothScrollToPosition(index);
//        recyclerView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
//            @Override
//            public boolean onPreDraw() {
//                recyclerView.getViewTreeObserver().removeOnPreDrawListener(this);
//                getActivity().supportStartPostponedEnterTransition();
//                return true;
//            }
//        });
    }

    public class TabPagerAdapter extends FragmentPagerAdapter {
        private List<RecyclerFragment> fragments;
        private List<String> titles;

        TabPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        void setFragments(List<RecyclerFragment> fragments, List<String> titles) {
            this.fragments = fragments;
            this.titles = titles;
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }

        PictureFragment getCurrent() {
            return (PictureFragment) adapter.instantiateItem(pager, pager.getCurrentItem());
        }
    }
}
