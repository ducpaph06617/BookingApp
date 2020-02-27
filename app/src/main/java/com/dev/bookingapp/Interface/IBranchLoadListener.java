package com.dev.bookingapp.Interface;

import java.util.List;

import com.dev.bookingapp.Model.Salon;

public interface IBranchLoadListener {

    void onBranchLoadSuccess(List<Salon> salonList);
    void onBranchLoadFailed(String message);
}
