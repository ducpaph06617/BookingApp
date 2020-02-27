package com.dev.bookingapp.Fragments;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.dev.bookingapp.Adapter.MyTimeSlotAdapter;
import com.dev.bookingapp.Common.Common;
import com.dev.bookingapp.Common.SpacesItemDecoration;
import com.dev.bookingapp.Interface.ITimeSlotLoadListener;
import com.dev.bookingapp.Model.EventBus.DisplayTimeSlotEvent;
import com.dev.bookingapp.Model.TimeSlot;
import com.dev.bookingapp.R;
import devs.mulham.horizontalcalendar.HorizontalCalendar;
import devs.mulham.horizontalcalendar.HorizontalCalendarView;
import devs.mulham.horizontalcalendar.utils.HorizontalCalendarListener;
import dmax.dialog.SpotsDialog;

public class BookingStep3Fragment extends Fragment implements ITimeSlotLoadListener {
    DocumentReference barberDoc;
    ITimeSlotLoadListener iTimeSlotLoadListener;

    AlertDialog dialog;
    Unbinder unbinder;
    // LocalBroadcastManager localBroadcastManager;
    @BindView(R.id.recycler_time_slot)
    RecyclerView recycler_time_slot;
    @BindView(R.id.calendarView)
    HorizontalCalendarView calendarView;
    SimpleDateFormat simpleDateFormat;
//    BroadcastReceiver displayTimeSlot = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            Calendar date = Calendar.getInstance();
//            date.add(Calendar.DATE, 0);
//            loadAvailableTimeSlotOfBarber(Common.currentBarber.getBarberId(),
//                    simpleDateFormat.format(date.getTime()));
//        }
//    };


    //========================================
    //EventBus Start


    @Override
    public void onStart() {
        EventBus.getDefault().register(this);
        super.onStart();

    }


    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    public void loadAllTimeSlotAvailable(DisplayTimeSlotEvent event)
    {
        if (event.isDisplay()){
            Calendar date = Calendar.getInstance();
            date.add(Calendar.DATE, 2);
            loadAvailableTimeSlotOfBarber(Common.currentBarber.getBarberId(),
                    simpleDateFormat.format(date.getTime()));
        }

    }
    private void loadAvailableTimeSlotOfBarber(String barberId,final String bookdate) {
        dialog.show();
        // /AllSalon/NewYork/Branch/trI33eemAuukur3PMxFD/Babers/63pPey00zHtKKZ4CvXT3
        barberDoc = FirebaseFirestore.getInstance()
                .collection("AllSalon")
                .document(Common.city)
                .collection("Branch")
                .document(Common.currentSalon.getSalonId())
                .collection("Barbers")
                .document(Common.currentBarber.getBarberId());

        barberDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot.exists()) {
                        CollectionReference date = FirebaseFirestore.getInstance()
                                .collection("AllSalon")
                                .document(Common.city)
                                .collection("Branch")
                                .document(Common.currentSalon.getSalonId())
                                .collection("Barbers")
                                .document(Common.currentBarber.getBarberId())
                                .collection(bookdate);

                        date.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    QuerySnapshot querySnapshot = task.getResult();
                                    if (querySnapshot.isEmpty())
                                        iTimeSlotLoadListener.onTimeSlotLoadEmpty();
                                    else {
                                        List<TimeSlot> timeSlots = new ArrayList<>();
                                        for (QueryDocumentSnapshot document : task.getResult())
                                            timeSlots.add(document.toObject(TimeSlot.class));
                                        iTimeSlotLoadListener.onTimeSlotLoadSuccess(timeSlots);
                                    }
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                iTimeSlotLoadListener.onTimeSlotLoadFailed(e
                                        .getMessage());
                            }
                        });
                    }
                }
            }
        });
    }

    static BookingStep3Fragment instance;

    public static BookingStep3Fragment getInstance() {
        if (instance == null)
            instance = new BookingStep3Fragment();
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        iTimeSlotLoadListener = this;
        //   localBroadcastManager = LocalBroadcastManager.getInstance (getContext());
        //  localBroadcastManager.registerReceiver(displayTimeSlot, new IntentFilter(Common.KEY_DISPLAY_TIME_SLOT));
        simpleDateFormat = new SimpleDateFormat("dd_MM_yyyy"); //15/10/2019
        dialog = new SpotsDialog.Builder().setContext(getContext()).setCancelable(false).build();
//        selected_date = Calendar.getInstance();
//        selected_date.add(Calendar.DATE, 0);

    }

//    @Override
//    public void onDestroy() {
//      //  localBroadcastManager.unregisterReceiver(displayTimeSlot);
//        super.onDestroy();
//    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_booking_step_three, container, false);
        unbinder = ButterKnife.bind(this, view);
        init(view);
        return view;
    }

    private void init(View view) {
        recycler_time_slot.setHasFixedSize(true);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 3);
        recycler_time_slot.setLayoutManager(gridLayoutManager);
        // gridLayoutManager.setOrientation(RecyclerView.HORIZONTAL);
        recycler_time_slot.addItemDecoration(new SpacesItemDecoration(8));


        Calendar startDate = Calendar.getInstance();
        startDate.add(Calendar.DATE, 0);
        Calendar endDate = Calendar.getInstance();
        endDate.add(Calendar.DATE, 1);
        HorizontalCalendar horizontalCalendar = new HorizontalCalendar.Builder(view, R.id.calendarView)
                .range(startDate, endDate)
                .datesNumberOnScreen(1)
                .mode(HorizontalCalendar.Mode.DAYS)
                .defaultSelectedDate(startDate)
                .build();
        horizontalCalendar.setCalendarListener(new HorizontalCalendarListener() {
            @Override
            public void onDateSelected(Calendar date, int position) {
                if (Common.bookingDate.getTimeInMillis() != date.getTimeInMillis()) {
                    Common.bookingDate = date;
                    loadAvailableTimeSlotOfBarber(Common.currentBarber.getBarberId()
                            , simpleDateFormat.format(date.getTime())
                    );
                }
            }
        });
    }

    @Override
    public void onTimeSlotLoadSuccess(List<TimeSlot> timeSlotList) {
        MyTimeSlotAdapter adapter = new MyTimeSlotAdapter(getContext(), timeSlotList);
        recycler_time_slot.setAdapter(adapter);
        dialog.dismiss();

    }

    @Override
    public void onTimeSlotLoadFailed(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        dialog.dismiss();
    }

    @Override
    public void onTimeSlotLoadEmpty() {
        MyTimeSlotAdapter adapter = new MyTimeSlotAdapter(getContext());
        recycler_time_slot.setAdapter(adapter);
        dialog.dismiss();

    }
}
