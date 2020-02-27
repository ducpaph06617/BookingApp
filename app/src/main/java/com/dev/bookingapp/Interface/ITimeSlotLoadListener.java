package com.dev.bookingapp.Interface;

import java.util.List;

import com.dev.bookingapp.Model.TimeSlot;

public interface ITimeSlotLoadListener {

    void onTimeSlotLoadSuccess(List<TimeSlot> timeSlotList);
    void onTimeSlotLoadFailed(String message);
    void onTimeSlotLoadEmpty();
}
