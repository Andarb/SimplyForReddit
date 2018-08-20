package com.github.andarb.simplyreddit.adapters;

import android.content.Context;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TextView;

import com.github.andarb.simplyreddit.MainActivity;
import com.github.andarb.simplyreddit.R;
import com.github.andarb.simplyreddit.ViewPagerFragment;

/* ViewPager adapter for different post categories (see MainActivity.PAGES[]) */
public class PostPagerAdapter extends FragmentPagerAdapter {

    private static final int[] ACTIVE_ICONS = {R.drawable.hot_icon_black_18,
            R.drawable.top_icon_black_18,
            R.drawable.new_icon_black_18};

    private static final int[] INACTIVE_ICONS = {R.drawable.hot_icon_white_18,
            R.drawable.top_icon_white_18,
            R.drawable.new_icon_white_18};

    public PostPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    // Number of pages
    @Override
    public int getCount() {
        return MainActivity.PAGES.length;
    }

    // Create a page to display
    @Override
    public Fragment getItem(int position) {
        return ViewPagerFragment.newInstance(MainActivity.PAGES[position]);
    }

    // Create custom view tabs with text, and icons located on the left
    public void setupCustomTabs(Context context, TabLayout tabLayout) {
        for (int i = 0; i < getCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);

            View tabRootView = LayoutInflater.from(context).inflate(R.layout.custom_tab, null);
            tabRootView.setLayoutParams(new TableLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            tabRootView.setPadding(0, 0, 0, 0);

            TextView tabTV = tabRootView.findViewById(R.id.tab_tv);
            tabTV.setText(MainActivity.PAGES[i]);
            tabTV.setCompoundDrawablesWithIntrinsicBounds(INACTIVE_ICONS[i], 0, 0, 0);

            tab.setCustomView(tabRootView);
        }
    }

    // Highlight currently selected tab
    public void highlightTab(Context context, TabLayout.Tab tab) {
        View rootView = tab.getCustomView();
        TextView tabTV = rootView.findViewById(R.id.tab_tv);

        rootView.setBackgroundColor(context.getResources().getColor(R.color.colorActiveTabBg));
        tabTV.setTextColor(context.getResources().getColor(R.color.colorTextPrimary));
        tabTV.setCompoundDrawablesWithIntrinsicBounds(ACTIVE_ICONS[tab.getPosition()],
                0, 0, 0);
    }

    // Unhighlight when a selected tab is changed
    public void unHighlightTab(Context context, TabLayout.Tab tab) {
        View rootView = tab.getCustomView();
        TextView tabTV = rootView.findViewById(R.id.tab_tv);

        rootView.setBackgroundColor(context.getResources().getColor(R.color.colorPrimary));
        tabTV.setTextColor(context.getResources().getColor(R.color.colorTextInactiveTab));
        tabTV.setCompoundDrawablesWithIntrinsicBounds(INACTIVE_ICONS[tab.getPosition()],
                0, 0, 0);
    }
}