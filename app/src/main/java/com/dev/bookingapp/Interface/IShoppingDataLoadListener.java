package com.dev.bookingapp.Interface;

import java.util.List;

import com.dev.bookingapp.Model.ShoppingItem;

public interface IShoppingDataLoadListener {
    void onShoppingDataLoadSuccess(List<ShoppingItem> shoppingItemList);
    void onShoppingDataLoadFailed(String message);


}
