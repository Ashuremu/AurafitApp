package com.aurafit.AuraFitApp.ui.community;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ViewPagerAdapter extends FragmentStateAdapter {

    // Fragment references to enable refresh calls
    private ProfileFragment profileFragment;
    private DiscoverFragment discoverFragment;
    private LikesFragment likesFragment;

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                profileFragment = new ProfileFragment();
                return profileFragment;
            case 1:
                discoverFragment = new DiscoverFragment();
                return discoverFragment;
            case 2:
                likesFragment = new LikesFragment();
                return likesFragment;
            default:
                profileFragment = new ProfileFragment();
                return profileFragment;
        }
    }

    @Override
    public int getItemCount() {
        return 3; // Profile, Discover, Likes
    }

    public Fragment getFragment(int position) {
        switch (position) {
            case 0:
                return profileFragment;
            case 1:
                return discoverFragment;
            case 2:
                return likesFragment;
            default:
                return profileFragment;
        }
    }
}
