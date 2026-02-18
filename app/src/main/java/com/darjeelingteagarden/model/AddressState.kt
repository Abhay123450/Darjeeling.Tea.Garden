package com.darjeelingteagarden.model

sealed class AddressState {
    object Home: AddressState()
    object AddAddress: AddressState()
    object AddressSaved: AddressState()
    object AddressNotSaved: AddressState()
    object EditAddress: AddressState()
    object NoAddress: AddressState()
    object Loading: AddressState()
}