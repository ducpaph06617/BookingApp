package com.dev.bookingapp.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import com.dev.bookingapp.Common.Common;
import com.dev.bookingapp.Database.CartDataSource;
import com.dev.bookingapp.Database.CartDatabase;
import com.dev.bookingapp.Database.CartItem;
import com.dev.bookingapp.Database.LocalCartDataSource;
import com.dev.bookingapp.Interface.IRecyclerItemSelectedListener;
import com.dev.bookingapp.Model.ShoppingItem;
import com.dev.bookingapp.R;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class MyShoppingItemAdapter extends RecyclerView.Adapter<MyShoppingItemAdapter.MyViewHolder> {

    Context context;
    List<ShoppingItem> shoppingItemList;
    CartDataSource cartDataSource;
    CompositeDisposable compositeDisposable;

    public void onDestroy()
    {
        compositeDisposable.clear();
    }

    public MyShoppingItemAdapter(Context context, List<ShoppingItem> shoppingItemList) {
        this.context = context;
        this.shoppingItemList = shoppingItemList;
        cartDataSource = new LocalCartDataSource(CartDatabase.getInstance(context).cartDAO());
        compositeDisposable = new CompositeDisposable();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.layout_shopping_item, viewGroup, false );
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
        Picasso.get().load(shoppingItemList.get(i).getImage()).into(myViewHolder.img_shopping_item);
        myViewHolder.txt_shopping_item_name.setText(Common.formatShoppingItemName(shoppingItemList.get(i).getName()));
        myViewHolder.txt_shopping_item_price.setText(new StringBuilder("$").append(shoppingItemList.get(i).getPrice()).toString());

        //Add to Cart
        myViewHolder.setiRecyclerItemSelectedListener(new IRecyclerItemSelectedListener() {
            @Override
            public void onItemSelectedListener(View view, int pos) {
                //Create Cart Item
                CartItem cartItem = new CartItem();
                cartItem.setProductId(shoppingItemList.get(pos).getId());
                cartItem.setProductName(shoppingItemList.get(pos).getName());
                cartItem.setProductImage(shoppingItemList.get(pos).getImage());
                cartItem.setProductQuantity(1);
                cartItem.setProductPrice(shoppingItemList.get(pos).getPrice());
                cartItem.setUserPhone(Common.currentUser.getPhoneNumber());


                //Insert To Database
//                DatabaseUtils.insertToCart(cartDatabase, cartItem);
                compositeDisposable.add(cartDataSource.insert(cartItem)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        ()-> Toast.makeText(context, "Added to Cart", Toast.LENGTH_SHORT).show(),
                        throwable -> Toast.makeText(context, ""+throwable.getMessage(), Toast.LENGTH_SHORT).show()
                ));

            }
        });
    }

    @Override
    public int getItemCount() {
        return shoppingItemList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView txt_shopping_item_name, txt_shopping_item_price, txt_add_to_cart;
        ImageView img_shopping_item;

        IRecyclerItemSelectedListener iRecyclerItemSelectedListener;

        public void setiRecyclerItemSelectedListener(IRecyclerItemSelectedListener iRecyclerItemSelectedListener) {
            this.iRecyclerItemSelectedListener = iRecyclerItemSelectedListener;
        }

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            img_shopping_item = (ImageView)itemView.findViewById(R.id.img_shopping_item);
            txt_shopping_item_name = (TextView)itemView.findViewById(R.id.txt_name_shopping_item);
            txt_shopping_item_price = (TextView)itemView.findViewById(R.id.txt_price_shopping_item);
            txt_add_to_cart = (TextView)itemView.findViewById(R.id.txt_add_to_cart);

            txt_add_to_cart.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            iRecyclerItemSelectedListener.onItemSelectedListener(view, getAdapterPosition());
        }
    }
}
