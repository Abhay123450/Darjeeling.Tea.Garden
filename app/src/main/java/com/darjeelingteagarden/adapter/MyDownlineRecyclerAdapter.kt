package com.darjeelingteagarden.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.darjeelingteagarden.R
import com.darjeelingteagarden.model.MyDownline
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class MyDownlineRecyclerAdapter(
    val context: Context,
    private val userList: MutableList<MyDownline>,
    private val isAdmin: Boolean,
    private val navController: NavController,
    private val releaseDue: (user: MyDownline) -> Unit
    ):RecyclerView.Adapter<MyDownlineRecyclerAdapter.MyDownlineViewHolder>() {

        class MyDownlineViewHolder(view: View): RecyclerView.ViewHolder(view){
            val cardParent: MaterialCardView = view.findViewById(R.id.cardParentUserList)
            val txtUserName: TextView = view.findViewById(R.id.txtUserName)
            val txtUserRole: TextView = view.findViewById(R.id.txtUserRole)
            val txtUserId: TextView = view.findViewById(R.id.txtUserId)
            val rlDueBalance: RelativeLayout = view.findViewById(R.id.rlDueBalance)
            val txtDueBalance: TextView = view.findViewById(R.id.txtUserDueBalance)
            val btnDueRelease: MaterialButton = view.findViewById(R.id.btnDueRelease)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyDownlineViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_view_user_details_my_downline, parent, false)
        return MyDownlineViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyDownlineViewHolder, position: Int) {
        val userInfo: MyDownline = userList[position]

        holder.txtUserName.text = userInfo.userName
        holder.txtUserRole.text = userInfo.userRole
        holder.txtUserId.text = userInfo.userId
        holder.txtDueBalance.text = userInfo.balanceDue.toString()

        holder.cardParent.setOnClickListener {
            navController.navigate(R.id.action_myDownlineListFragment_to_myDownlineUserDetailsFragment)
        }

        if (isAdmin){

            holder.rlDueBalance.visibility = View.VISIBLE

            if (userInfo.balanceDue > 0){
                holder.btnDueRelease.visibility = View.VISIBLE
            }
            else{
                holder.btnDueRelease.visibility = View.GONE
            }

            holder.btnDueRelease.setOnClickListener {
                releaseDue(userInfo)
            }

        }
        else{
            holder.rlDueBalance.visibility = View.GONE
        }

    }

    override fun getItemCount(): Int {
        return userList.size
    }
}