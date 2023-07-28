package com.darjeelingteagarden.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.darjeelingteagarden.model.Cart
import com.darjeelingteagarden.model.Product

class AppDataViewModel(application: Application) : AndroidViewModel(application) {

    var authToken = ""

    var userName = ""

    var storeItemList = mutableListOf<Product>()

    var cartItemList = mutableListOf<Cart>()

}