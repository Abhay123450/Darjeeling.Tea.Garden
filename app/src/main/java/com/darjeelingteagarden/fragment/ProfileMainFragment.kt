package com.darjeelingteagarden.fragment

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.darjeelingteagarden.R
import com.darjeelingteagarden.adapter.ProfileRecyclerAdapter
import com.darjeelingteagarden.adapter.StoreRecyclerAdapter
import com.darjeelingteagarden.repository.UserActionList

class ProfileMainFragment : Fragment() {

    lateinit var recyclerViewProfile: RecyclerView
    lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var recyclerAdapter: ProfileRecyclerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile_main, container, false)

        recyclerViewProfile = view.findViewById(R.id.recyclerViewProfile)
        layoutManager = LinearLayoutManager(activity)

        recyclerAdapter = ProfileRecyclerAdapter(activity as Context, UserActionList().getAllUserActions())

        recyclerViewProfile.adapter = recyclerAdapter
        recyclerViewProfile.layoutManager = layoutManager
        recyclerViewProfile.addItemDecoration(
            DividerItemDecoration(
                recyclerViewProfile.context,
                (layoutManager as LinearLayoutManager).orientation
            )
        )

        return view
    }

}