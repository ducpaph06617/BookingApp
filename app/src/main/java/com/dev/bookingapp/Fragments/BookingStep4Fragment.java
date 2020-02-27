package com.dev.bookingapp.Fragments;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import com.dev.bookingapp.Common.Common;
import com.dev.bookingapp.Database.CartDataSource;
import com.dev.bookingapp.Database.CartDatabase;
import com.dev.bookingapp.Database.CartItem;
import com.dev.bookingapp.Database.LocalCartDataSource;
import com.dev.bookingapp.Model.BookingInformation;
import com.dev.bookingapp.Model.EventBus.ConfirmBookingEvent;
import com.dev.bookingapp.Model.FCMResponse;
import com.dev.bookingapp.Model.FCMSendData;
import com.dev.bookingapp.Model.MyNotification;
import com.dev.bookingapp.Model.MyToken;
import com.dev.bookingapp.R;
import com.dev.bookingapp.Retrofit.IFCMApi;
import com.dev.bookingapp.Retrofit.RetrofitClient;
import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class BookingStep4Fragment extends Fragment {

    CartDataSource cartDataSource;

    CompositeDisposable compositeDisposable = new CompositeDisposable();

    SimpleDateFormat simpleDateFormat;
//    LocalBroadcastManager localBroadcastManager;

    Unbinder unbinder;

    IFCMApi ifcmApi;

    AlertDialog dialog;

    @BindView(R.id.txt_booking_barber_text)
    TextView txt_booking_barber_text;

    @BindView(R.id.txt_booking_time_text)
    TextView txt_booking_time_text;

    @BindView(R.id.txt_salon_name)
    TextView txt_salon_name;


    @BindView(R.id.txt_salon_address)
    TextView txt_salon_address;

    @BindView(R.id.txt_salon_open_hours)
    TextView txt_salon_open_hours;

    @BindView(R.id.txt_salon_phone)
    TextView txt_salon_phone;

    @BindView(R.id.txt_salon_website)
    TextView txt_salon_website;

    @OnClick(R.id.btn_confirm)
    void confirmBooking(){

        dialog.show();

//        DatabaseUtils.getAllCart(CartDatabase.getInstance(getContext()), this);
        compositeDisposable.add(cartDataSource.getAllItemFromCart(Common.currentUser.getPhoneNumber())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<CartItem>>() {
                               @Override
                               public void accept(List<CartItem> cartItems) throws Exception {
                                   //Here, We get all Cart Item
                                   //Process TimeStamp
                                   //We will use TimeStamp to filter all booking with date is greater today
                                   //for only display all future Booking
                                   String startTime = Common.convertTimeSlotToString(Common.currentTimeSlot);
                                   String[] convertTime = startTime.split("-"); //Split ex: 09:00-09:30
                                   //Get Start Time
                                   String[] startTimeConvert = convertTime[0].split(":");
                                   int startHourInt = Integer.parseInt(startTimeConvert[0].trim());
                                   int startMinInt = Integer.parseInt(startTimeConvert[1].trim()); //We get 00

                                   Calendar bookingDateWithhourHouse = Calendar.getInstance();
                                   bookingDateWithhourHouse.setTimeInMillis(Common.bookingDate.getTimeInMillis());
                                   bookingDateWithhourHouse.set(Calendar.HOUR_OF_DAY, startHourInt);
                                   bookingDateWithhourHouse.set(Calendar.MINUTE, startMinInt);

                                   //Create Time Stamp object and apply to BookingInformation
                                   Timestamp timestamp = new Timestamp(bookingDateWithhourHouse.getTime());



                                   //Create Booking Information
                                   final BookingInformation bookingInformation  = new BookingInformation();

                                   bookingInformation.setCityBook(Common.city);
                                   bookingInformation.setTimestamp(timestamp);
                                   bookingInformation.setDone(false); //Always False, because we will use this field to filter for display on user
                                   bookingInformation.setBarberId(Common.currentBarber.getBarberId());
                                   bookingInformation.setBarberName(Common.currentBarber.getName());
                                   bookingInformation.setCustomerName(Common.currentUser.getName());
                                   bookingInformation.setCustomerPhone(Common.currentUser.getPhoneNumber());
                                   bookingInformation.setSalonId(Common.currentSalon.getSalonId());
                                   bookingInformation.setSalonAddress(Common.currentSalon.getAddress());
                                   bookingInformation.setSalonName(Common.currentSalon.getName());
                                   bookingInformation.setTime(new StringBuilder(Common.convertTimeSlotToString(Common.currentTimeSlot))
                                           .append(" at ")
                                           .append(simpleDateFormat.format(bookingDateWithhourHouse.getTime())).toString());

                                   bookingInformation.setSlot(Long.valueOf(Common.currentTimeSlot));
                                   bookingInformation.setCartItemList(cartItems); //ADD Cart Item List to Booking Information

                                   //Submit to Barber Doc
                                   DocumentReference bookingDate = FirebaseFirestore.getInstance()
                                           .collection("AllSalon")
                                           .document(Common.city)
                                           .collection("Branch")
                                           .document(Common.currentSalon.getSalonId())
                                           .collection("Barbers")
                                           .document(Common.currentBarber.getBarberId())
                                           .collection(Common.simpleFormatDate.format(Common.bookingDate.getTime()))
                                           .document(String.valueOf(Common.currentTimeSlot));

                                   //Write Data
                                   bookingDate.set(bookingInformation)
                                           .addOnSuccessListener(new OnSuccessListener<Void>() {
                                               @Override
                                               public void onSuccess(Void aVoid) {

                                                   //After, Add Success Booking, Just Clear Cart
//                        DatabaseUtils.clearCart(CartDatabase.getInstance(getContext()));
                                                   cartDataSource.clearCart(Common.currentUser.getPhoneNumber())
                                                           .subscribeOn(Schedulers.io())
                                                           .observeOn(AndroidSchedulers.mainThread())
                                                           .subscribe(new SingleObserver<Integer>() {
                                                               @Override
                                                               public void onSubscribe(Disposable d) {

                                                               }

                                                               @Override
                                                               public void onSuccess(Integer integer) {
                                                                   //Here We can check If already exist booking, We will prevent new booking
                                                                   addToUserBooking(bookingInformation);
                                                               }

                                                               @Override
                                                               public void onError(Throwable e) {
                                                                   Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                                               }
                                                           });




                                               }
                                           }).addOnFailureListener(new OnFailureListener() {
                                       @Override
                                       public void onFailure(@NonNull Exception e) {
                                           Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                       }
                                   });
                               }
                           }, throwable -> Toast.makeText(getContext(), ""+throwable.getMessage(), Toast.LENGTH_SHORT).show()
                ));

    }

    private void addToUserBooking(BookingInformation bookingInformation) {

        //First Create New Collection
        CollectionReference userBooking = FirebaseFirestore.getInstance()
                .collection("User")
                .document(Common.currentUser.getPhoneNumber())
                .collection("Booking");


        //Check if Document exist in this collection
        //Get Current Date
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);

        Timestamp toDayTimeStamp = new Timestamp(calendar.getTime());

        userBooking.whereGreaterThanOrEqualTo("timestamp", toDayTimeStamp)
                .whereEqualTo("done", false)
                .limit(1) //Only take 1
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.getResult().isEmpty())
                        {
                            //Set Data
                            userBooking.document()
                                    .set(bookingInformation)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            MyNotification myNotification = new MyNotification();
                                            myNotification.setUid(UUID.randomUUID().toString());
                                            myNotification.setTitle("New Booking");
                                            myNotification.setContent("You have new appointment for Customer with " + Common.currentUser.getName());
                                            myNotification.setRead(false); //We will filter only notification with read = "false"
                                            myNotification.setServerTimestamp(FieldValue.serverTimestamp());

                                            //Submit Notification to "Notification" collection of barber
                                            FirebaseFirestore.getInstance()
                                                .collection("AllSalon")
                                                .document(Common.city)
                                                .collection("Branch")
                                                .document(Common.currentSalon.getSalonId())
                                                .collection("Barbers")
                                                .document(Common.currentBarber.getBarberId())
                                                .collection("Notifications")
                                                .document(myNotification.getUid()) //Unique Key
                                                .set(myNotification)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                          //First, Get Token base on Barber Id
                                                            FirebaseFirestore.getInstance()
                                                                    .collection("Tokens")
                                                                    .whereEqualTo("userPhone", Common.currentBarber.getUsername())
                                                                    .limit(1)
                                                                    .get()
                                                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                            if(task.isSuccessful() && task.getResult().size() > 0)
                                                                            {
                                                                                MyToken myToken = new MyToken();
                                                                                for(DocumentSnapshot tokenSnapshot : task.getResult())
                                                                                    myToken = tokenSnapshot.toObject(MyToken.class);

                                                                                //Create Data to Send
                                                                                FCMSendData sendRequest = new FCMSendData();
                                                                                Map<String, String> dataSend = new HashMap<>();
                                                                                dataSend.put(Common.TITLE_KEY, "New Booking");
                                                                                dataSend.put(Common.CONTENT_KEY, "You have new booking from User " + Common.currentUser.getName());

                                                                                sendRequest.setTo(myToken.getToken());
                                                                                sendRequest.setData(dataSend);

                                                                               compositeDisposable.add( ifcmApi.sendNotification(sendRequest)
                                                                                       .subscribeOn(Schedulers.io())
                                                                                       .observeOn(AndroidSchedulers.mainThread())
                                                                                       .subscribe(new Consumer<FCMResponse>() {
                                                                                           @Override
                                                                                           public void accept(FCMResponse fcmResponse) throws Exception {
                                                                                               if(dialog.isShowing())
                                                                                                   dialog.dismiss();

                                                                                               addToCalendar(Common.bookingDate, Common.convertTimeSlotToString(Common.currentTimeSlot));

                                                                                               resetStaticData();
                                                                                               getActivity().finish(); //Close Activity
                                                                                               Toast.makeText(getContext(), "Success!", Toast.LENGTH_SHORT).show();
                                                                                           }
                                                                                       }, new Consumer<Throwable>() {
                                                                                           @Override
                                                                                           public void accept(Throwable throwable) throws Exception {
                                                                                               Log.d("NOTIFICATION_ERROR", throwable.getMessage());

                                                                                               addToCalendar(Common.bookingDate, Common.convertTimeSlotToString(Common.currentTimeSlot));

                                                                                               resetStaticData();
                                                                                               getActivity().finish(); //Close Activity
                                                                                               Toast.makeText(getContext(), "Success!", Toast.LENGTH_SHORT).show();

                                                                                           }
                                                                                       }));

                                                                            }
                                                                        }
                                                                    });
                                                        }
                                                    });
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            if(dialog.isShowing())
                                                dialog.dismiss();
                                            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else{
                            if(dialog.isShowing())
                                dialog.dismiss();

                            resetStaticData();
                            getActivity().finish(); //Close Activity
                            Toast.makeText(getContext(), "Success!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    private void addToCalendar(Calendar bookingDate, String startDate) {
        String startTime = Common.convertTimeSlotToString(Common.currentTimeSlot);
        String[] convertTime = startTime.split("-"); //Split ex: 09:00-10:30
        //Get Start Time
        String[] startTimeConvert = convertTime[0].split(":");
        int startHourInt = Integer.parseInt(startTimeConvert[0].trim()); //we get 09
        int startMinInt = Integer.parseInt(startTimeConvert[1].trim()); //We get 00

        String[] endTimeConvert = convertTime[1].split(":");
        int endHourInt = Integer.parseInt(endTimeConvert[0].trim()); //We get 10
        int endMinInt = Integer.parseInt(endTimeConvert[1].trim()); //We get 00

        Calendar startEvent = Calendar.getInstance();
        startEvent.setTimeInMillis(bookingDate.getTimeInMillis());
        startEvent.set(Calendar.HOUR_OF_DAY, startHourInt); //Set Event Start Time
        startEvent.set(Calendar.MINUTE, startMinInt); //Set Event Start minute

        Calendar endEvent = Calendar.getInstance();
        endEvent.setTimeInMillis(bookingDate.getTimeInMillis());
        endEvent.set(Calendar.HOUR_OF_DAY, endHourInt); //Set Event Start Time
        endEvent.set(Calendar.MINUTE, endMinInt); //Set Event Start minute

        //AFter we have start Event and End event convert it to format string
        SimpleDateFormat calendarDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        String startEventTime = calendarDateFormat.format(startEvent.getTime());
        String endEventTime = calendarDateFormat.format(endEvent.getTime());

        addToDeviceCalendar(startEventTime, endEventTime, "Haircut Booking",
                new StringBuilder("Haircut from ")
                    .append(startTime)
                    .append(" with ")
                    .append(Common.currentBarber.getName())
                    .append(" at ")
                    .append(Common.currentSalon.getName()).toString(),
                        new StringBuilder("Address: ").append(Common.currentSalon.getAddress()).toString());


    }

    private void addToDeviceCalendar(String startEventTime, String endEventTime, String title , String description , String location) {
        SimpleDateFormat calendarDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        try{
            Date start = calendarDateFormat.parse(startEventTime);
            Date end = calendarDateFormat.parse(endEventTime);

            ContentValues event = new ContentValues();

            //Put
            event.put(CalendarContract.Events.CALENDAR_ID, getCalendar(getContext()));
            event.put(CalendarContract.Events.TITLE, title);
            event.put(CalendarContract.Events.DESCRIPTION, description);
            event.put(CalendarContract.Events.EVENT_LOCATION, location);

            //Time
            event.put(CalendarContract.Events.DTSTART, start.getTime());
            event.put(CalendarContract.Events.DTEND, end.getTime());
            event.put(CalendarContract.Events.ALL_DAY, 0);
            event.put(CalendarContract.Events.HAS_ALARM, 1);

            String timeZone = TimeZone.getDefault().getID();
            event.put(CalendarContract.Events.EVENT_TIMEZONE, timeZone);


            Uri calendars;
            if(Build.VERSION.SDK_INT >= 8 )
                calendars = Uri.parse("content://com.android.calendar/events");
            else
                calendars = Uri.parse("content://calendar/events");


            Uri uri_save = getActivity().getContentResolver().insert(calendars, event);
            //Save to cache
            Paper.init(getActivity());
            Paper.book().write(Common.EVENT_URI_CACHE, uri_save.toString());



        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private String getCalendar(Context context) {
        //Get Default Calendar ID of Calendar of Gmail
        String gmailIdCalendar = "";
        String projection[]={"_id", "calendar_displayName"};
        Uri calendars = Uri.parse("content://com.android.calendar/calendars");

        ContentResolver contentResolver = context.getContentResolver();

        //Select all Calendars
        Cursor managedCursor = contentResolver.query(calendars, projection, null, null, null);
        if(managedCursor.moveToFirst()){
            String calName;
            int nameCol = managedCursor.getColumnIndex(projection[1]);
            int idCol = managedCursor.getColumnIndex(projection[0]);
            do {
                calName = managedCursor.getString(nameCol);
                if (calName.contains("@gmail.com")) {
                    gmailIdCalendar = managedCursor.getString(idCol);
                    break; //Exit as soon as have ID
                }
            }while(managedCursor.moveToNext());
            managedCursor.close();
        }
        return gmailIdCalendar;
    }

    private void resetStaticData() {
        Common.step = 0;
        Common.currentTimeSlot = -1;
        Common.currentSalon = null;
        Common.currentBarber = null;
        Common.bookingDate.add(Calendar.DATE, 0);

    }

//    BroadcastReceiver confirmBookingReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            setData();
//        }
//    };

    //================================================================
    //EVENT BUS START

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void setDataBooking(ConfirmBookingEvent event)
    {
       if(event.isConfirm())
       {
           setData();
       }
    }

    //================================================================

    private void setData() {
        txt_booking_barber_text.setText(Common.currentBarber.getName());
        txt_booking_time_text.setText(new StringBuilder(Common.convertTimeSlotToString(Common.currentTimeSlot))
                .append(" at ")
                .append(simpleDateFormat.format(Common.bookingDate.getTime())));
        txt_salon_phone.setText(Common.currentSalon.getPhone());

        txt_salon_address.setText(Common.currentSalon.getAddress());
        txt_salon_website.setText(Common.currentSalon.getWebsite());

        txt_salon_name.setText(Common.currentSalon.getName());
        txt_salon_open_hours.setText(Common.currentSalon.getOpenHours());

    }



    static BookingStep4Fragment instance;

    public static BookingStep4Fragment getInstance() {
        if(instance == null)
            instance = new BookingStep4Fragment();
        return instance;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ifcmApi = RetrofitClient.getInstance().create(IFCMApi.class);

        //Apply format for Date Display in confirm screen.
        simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");

//        localBroadcastManager = LocalBroadcastManager.getInstance(getContext());
//        localBroadcastManager.registerReceiver(confirmBookingReceiver, new IntentFilter(Common.KEY_CONFIRM_BOOKING));

        dialog = new SpotsDialog.Builder().setContext(getContext()).setCancelable(false)
            .build();
    }

    @Override
    public void onDestroy() {
//        localBroadcastManager.unregisterReceiver(confirmBookingReceiver);
        compositeDisposable.clear();
        super.onDestroy();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View itemView =  inflater.inflate(R.layout.fragment_booking_step_four,container, false);

        unbinder = ButterKnife.bind(this, itemView);
        //Remember Init CartDatasource here, If you don't want to get NULL Reference Here
        //Because GetContext return Null
        cartDataSource = new LocalCartDataSource(CartDatabase.getInstance(getContext()).cartDAO());

        return itemView;

    }


}
