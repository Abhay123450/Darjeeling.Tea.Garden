package com.darjeelingteagarden.fragment

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.darjeelingteagarden.R
import com.darjeelingteagarden.adapter.AddressAdapter
import com.darjeelingteagarden.databinding.BottomSheetAddressBinding
import com.darjeelingteagarden.model.Address
import com.darjeelingteagarden.model.AddressState
import com.darjeelingteagarden.util.markRequired
import com.darjeelingteagarden.viewModel.AddressViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AddressBottomSheet(val isDuringCheckoutFlow: Boolean = true) : BottomSheetDialogFragment() {

    private val viewModel: AddressViewModel by activityViewModels()

    private var _binding: BottomSheetAddressBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(requireContext(), theme).apply {
            setOnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                    if (_binding != null && binding.viewFlipper.displayedChild == 1) {
                        // Handle internal navigation
                        binding.viewFlipper.setInAnimation(context, R.anim.from_left)
                        binding.viewFlipper.setOutAnimation(context, R.anim.to_right)
                        binding.viewFlipper.displayedChild = 0
                        updateTitle()
                        true // ⬅ consume back press
                    } else {
                        dismiss()
                        true
                    }
                } else {
                    false
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val bottomSheet =
            (dialog as BottomSheetDialog).findViewById<View>(
                com.google.android.material.R.id.design_bottom_sheet
            )

        bottomSheet?.let {
            val behavior = BottomSheetBehavior.from(it)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.skipCollapsed = true
            behavior.isDraggable = false
            it.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetAddressBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.textInputLayoutName.markRequired("Name")
        binding.textInputLayoutPhoneNumber.markRequired("Phone Number")
        binding.textInputLayoutAddressLine1.markRequired("Address Line 1")
        binding.textInputLayoutPincode.markRequired("Pincode")
        binding.textInputLayoutCity.markRequired("City")
        binding.textInputLayoutState.markRequired("State")

        val rv = binding.rvAddresses

        binding.btnAddNewAddress.setOnClickListener {
            navigateToAddAddress()
            parentFragmentManager.setFragmentResult("address_add", bundleOf())
        }

        binding.textInputEditTextPincode.doOnTextChanged { text, _, _, _ ->
            val pincode = text.toString()
            if (pincode.length == 6 && pincode.toInt() in 100000..999999) {
                viewModel.fetchCityAndStateFromPincode(requireContext(), pincode){ city, state ->
                    binding.textInputEditTextCity.setText(city)
                    binding.textInputEditTextState.setText(state)
                }
            }
        }

        binding.btnSaveNewAddress.setOnClickListener {

            val name = binding.textInputEditTextName.text.toString().trim()
            val phoneNumber = binding.textInputEditTextPhoneNumber.text.toString().trim()
            val alternatePhoneNumber =
                binding.textInputEditTextAlternatePhoneNumber.text.toString().trim()
            val addressLine1 = binding.textInputEditTextAddressLine1.text.toString().trim()
            val addressLine2 = binding.textInputEditTextAddressLine2.text.toString().trim()
            val landmark = binding.textInputEditTextLandmark.text.toString().trim()
            val pincode = binding.textInputEditTextPincode.text.toString().trim()
            val city = binding.textInputEditTextCity.text.toString().trim()
            val state = binding.textInputEditTextState.text.toString().trim()

            if (!isSaveAddressDataValid(
                    name,
                    phoneNumber,
                    addressLine1,
                    pincode,
                    city,
                    state
                )
            ) {
                return@setOnClickListener
            }

            val newAddress = Address(
                "1",
                name,
                phoneNumber,
                alternatePhoneNumber,
                addressLine1,
                addressLine2,
                landmark,
                pincode,
                state,
                city,
                "India",
                isDefault = false,
                isSelected = false
            )

            if (isDuringCheckoutFlow) {
                returnAddress(newAddress)
                viewModel.addAddress(activity as Context, address = newAddress)
                dismiss()
            } else {
                viewModel.addAddress(requireContext(), address = newAddress, callback = {
                    navigateBackToSelectAddress()
                    rv.adapter?.notifyItemInserted(-1)
                })
            }

        }

        binding.btnBack.setOnClickListener {
            if (binding.viewFlipper.displayedChild == 1) {
                navigateBackToSelectAddress()
            } else {
                dismiss()
            }
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAddressRecyclerAdapter()
        observeViewModel()
        updateTitle()

        viewModel.fetchMyAddresses(requireContext())

    }

    private fun isSaveAddressDataValid(
        name: String,
        phoneNumber: String,
        addressLine1: String,
        pincode: String,
        city: String,
        state: String
    ): Boolean {
        var isDataValid = true

        if (name.isEmpty() || name.length < 3) {
            binding.textInputLayoutName.error = "Name is required"
            isDataValid = false
        }
        if (phoneNumber.isEmpty() || phoneNumber.length != 10) {
            binding.textInputLayoutPhoneNumber.error = "Phone number is required"
            isDataValid = false
        }
        if (addressLine1.isEmpty()) {
            binding.textInputLayoutAddressLine1.error = "Address line 1 is required"
            isDataValid = false
        }
        if (pincode.isEmpty() || pincode.length != 6) {
            binding.textInputLayoutPincode.error = "Pincode is required"
            isDataValid = false
        }
        if (city.isEmpty()) {
            binding.textInputLayoutCity.error = "City is required"
            isDataValid = false
        }
        if (state.isEmpty()) {
            binding.textInputLayoutState.error = "State is required"
            isDataValid = false
        }

        return isDataValid
    }

    private fun observeViewModel(){
        viewModel.addressState.observe(viewLifecycleOwner){ state ->
            when (state){
                is AddressState.Home -> {
                    binding.rvAddresses.adapter?.notifyDataSetChanged()
                    animateLeftToRight()
                    binding.viewFlipper.displayedChild = 0
                    updateTitle()
                }
                is AddressState.AddAddress -> {
                    val displayedChild = binding.viewFlipper.displayedChild
                    if (displayedChild == 0) {
                        animateRightToLeft()
                        binding.viewFlipper.displayedChild = 1
                        updateTitle()
                    }
                    else if (displayedChild == 2){
                        animateRightToLeft()
                        binding.viewFlipper.displayedChild = 1
                        updateTitle()
                    }
                }
                is AddressState.AddressSaved -> {
                    Toast.makeText(context, "Address saved successfully", Toast.LENGTH_SHORT).show()
                    binding.rvAddresses.adapter?.notifyDataSetChanged()
                    animateLeftToRight()
                    binding.viewFlipper.displayedChild = 0
                    updateTitle()
                }
                is AddressState.AddressNotSaved -> {
                    Toast.makeText(context, "Failed to add address", Toast.LENGTH_SHORT).show()
                    animateLeftToRight()
                    binding.viewFlipper.displayedChild = 0
                    updateTitle()
                }
                is AddressState.EditAddress -> {

                }
                is AddressState.NoAddress -> {
                    binding.viewFlipper.displayedChild
                    if (isDuringCheckoutFlow){
                            animateRightToLeft()
                            binding.viewFlipper.displayedChild = 1
                            updateTitle()
                    }
                    else{
                        animateLeftToRight()
                        binding.viewFlipper.displayedChild = 0
                        updateTitle()
                    }
                }
                is AddressState.Loading -> {
                    binding.viewFlipper.displayedChild = 2
                }
            }
        }
    }

    private fun setupAddressRecyclerAdapter(){
        val adapter = AddressAdapter(
            viewModel.getAddresses(),
            onSelect = { address ->
                if (isDuringCheckoutFlow) {
                    returnAddress(address)
                    dismiss()
                }
            },
            onEdit = {},
            onDelete = {}
        )
        binding.rvAddresses.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAddresses.adapter = adapter
    }

    private fun animateLeftToRight(){
        binding.viewFlipper.setInAnimation(context, R.anim.from_left)
        binding.viewFlipper.setOutAnimation(context, R.anim.to_right)
    }

    private fun animateRightToLeft(){
        binding.viewFlipper.setInAnimation(context, R.anim.from_right)
        binding.viewFlipper.setOutAnimation(context, R.anim.to_left)
    }

    private fun navigateToAddAddress(){
        animateRightToLeft()
        binding.viewFlipper.displayedChild = 1
        updateTitle()
    }

    private fun navigateBackToSelectAddress(){
        animateLeftToRight()
        binding.viewFlipper.displayedChild = 0
        updateTitle()
    }

    private fun updateTitle(){
        if (binding.viewFlipper.displayedChild == 0) {
            if (isDuringCheckoutFlow) {
                binding.txtTitle.text = requireContext().getString(R.string.select_delivery_address)
            } else {
                binding.txtTitle.text = requireContext().getString(R.string.my_addresses)
            }
        }
        else if (binding.viewFlipper.displayedChild == 1){
            binding.txtTitle.text = requireContext().getString(R.string.add_new_address)
        }
    }

    private fun returnAddress(address: Address) {
        parentFragmentManager.setFragmentResult(
            "address_result",
            bundleOf("address" to address)
        )
    }

}
