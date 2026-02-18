package com.darjeelingteagarden.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.darjeelingteagarden.R
import com.darjeelingteagarden.model.Address

class AddressAdapter(
    private val addressList: List<Address>,
    private val onSelect: (Address) -> Unit,
    private val onEdit: (Address) -> Unit,
    private val onDelete: (Address) -> Unit
) : RecyclerView.Adapter<AddressAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val container: View = view.findViewById(R.id.container)
        val name: TextView = view.findViewById(R.id.txtName)
        val phone: TextView = view.findViewById(R.id.txtPhoneNumber)
        val alternatePhone: TextView = view.findViewById(R.id.txtAlternatePhoneNumber)
        val addressLine1: TextView = view.findViewById(R.id.txtAddressLine1)
        val addressLine2: TextView = view.findViewById(R.id.txtAddressLine2)
        val landmark: TextView = view.findViewById(R.id.txtLandmark)
        val city: TextView = view.findViewById(R.id.txtCity)
        val state: TextView = view.findViewById(R.id.txtState)
        val country: TextView = view.findViewById(R.id.txtCountry)
        val pincode: TextView = view.findViewById(R.id.txtPincode)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_view_address_single_row, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val address = addressList[position]

        holder.name.text = address.name.trim()
        holder.phone.text = address.phoneNumber.trim()
        holder.alternatePhone.text = address.alternatePhoneNumber.trim()
        holder.addressLine1.text = address.addressLine1.trim()
        holder.addressLine2.text = address.addressLine2.trim()
        holder.landmark.text = address.landmark.trim()
        holder.city.text = address.city.trim()
        holder.state.text = address.state.trim()
        holder.country.text = address.country.trim()
        holder.pincode.text = address.postalCode.trim()

        if (address.alternatePhoneNumber.isBlank()){
            holder.alternatePhone.visibility = View.GONE
        }
        else{
            holder.alternatePhone.visibility = View.VISIBLE
        }

        if (address.addressLine2.isBlank()){
            holder.addressLine2.visibility = View.GONE
        }
        else{
            holder.addressLine2.visibility = View.VISIBLE
        }

        if (address.landmark.isBlank()){
            holder.landmark.visibility = View.GONE
        }
        else{
            holder.landmark.visibility = View.VISIBLE
        }

        holder.container.setOnClickListener { onSelect(address) }
//        holder.edit.setOnClickListener { onEdit(address) }
//        holder.delete.setOnClickListener { onDelete(address) }
    }

    override fun getItemCount(): Int {
        return addressList.size
    }
}
