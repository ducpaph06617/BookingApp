package com.dev.bookingapp.Interface;

import java.util.List;

import com.dev.bookingapp.Model.Banner;

public interface ILookbookLoadListener {
    void onLookbookLoadSuccess(List<Banner> banners);
    void onLookbookLoadFailed(String message);
}
