package com.dev.bookingapp.Interface;

import java.util.List;

import com.dev.bookingapp.Model.Banner;

public interface IBannerLoadListener {

    void onBannerLoadSuccess(List<Banner> banners);
    void onBannerLoadFailed(String message);
}
