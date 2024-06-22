package com.darjeelingteagarden.fragment

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.darjeelingteagarden.R
import com.darjeelingteagarden.activity.MyDownlineActivity
import com.darjeelingteagarden.adapter.MyDownlineRecyclerAdapter
import com.darjeelingteagarden.databinding.FragmentMyDownlineListBinding
import com.darjeelingteagarden.model.MyDownline
import com.darjeelingteagarden.repository.AppDataSingleton
import com.darjeelingteagarden.repository.NotificationDataSingleton
import com.darjeelingteagarden.util.ConnectionManager

class MyDownlineListFragment : Fragment() {

    private lateinit var binding: FragmentMyDownlineListBinding
    private lateinit var mContext: Context

    private lateinit var recyclerViewMyDownline: RecyclerView
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private lateinit var myDownlineRecyclerAdapter: MyDownlineRecyclerAdapter

    private var myDownlineList = mutableListOf<MyDownline>()

    private var currentPage = 1
    private var role = "All"
    private var sort = 1 //1 > orderDate, 2> -orderDate, 3> amountPayable, 4> -amountPayable
    private val limit = 10
    private var totalUsers = 0

    private lateinit var queue: RequestQueue

    private var loadingMyDownline = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        binding = FragmentMyDownlineListBinding.inflate(inflater, container, false)

        queue = Volley.newRequestQueue(mContext)

        recyclerViewMyDownline = binding.recyclerViewMyDownline
        layoutManager = LinearLayoutManager(mContext)

        if (NotificationDataSingleton.notificationToOpen){
            if (NotificationDataSingleton.activityToOpen == "users"){
                findNavController().navigate(R.id.action_myDownlineListFragment_to_myDownlineUserDetailsFragment)
            }
        }

        if (AppDataSingleton.showUserDetails){
            findNavController().navigate(R.id.action_myDownlineListFragment_to_myDownlineUserDetailsFragment)
        }

//        if (AppDataSingleton.myDownlineGoBack){
//            AppDataSingleton.myDownlineGoBack = false
//            findNavController().popBackStack()
//        }

        getMyDownline(role, currentPage, sort)

        binding.autoCompleteTextUserRole.setOnItemClickListener { adapterView, view, position, id ->
            role = adapterView.getItemAtPosition(position).toString()
            currentPage = 1
            getMyDownline(role, currentPage, sort)
        }

        binding.autoCompleteTextViewSort.setOnItemClickListener { adapterView, view, i, l ->
            sort = i + 1
            currentPage = 1
            getMyDownline(role, currentPage, sort)
        }

        binding.btnLoadMoreMyDownline.setOnClickListener {
            getMyDownline(role, ++currentPage, sort)
            binding.progressBarMyDownline.visibility = View.VISIBLE
            it.visibility = View.GONE
        }

        binding.fabCallNow.setOnClickListener {
            AppDataSingleton.callNow(mContext)
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        initializeFilterDropdown()
        initializeSortDropdown()
        if (myDownlineList.isEmpty()){
            currentPage = 1
            getMyDownline(role, currentPage, sort)
        }
//        if (AppDataSingleton.myDownlineGoBack){
//            AppDataSingleton.myDownlineGoBack = false
//            (activity as MyDownlineActivity).goBack()
//        }
    }

    private fun initializeFilterDropdown(){

        val userRoles = mutableListOf("All", "Super Stockist", "Distributor", "Dealer", "Wholesaler", "Retailer")

        val arrayAdapter = ArrayAdapter(mContext, R.layout.dropdown_item, userRoles)
        binding.autoCompleteTextUserRole.setAdapter(arrayAdapter)
    }

    private fun initializeSortDropdown(){

        val sortOptions = mutableListOf("Newest", "Oldest", "Credit Due")

        val arrayAdapter = ArrayAdapter(mContext, R.layout.dropdown_item, sortOptions)
        binding.autoCompleteTextViewSort.setAdapter(arrayAdapter)
    }

    private fun populateRecyclerView(myDownline: MutableList<MyDownline>){

        var isAdmin = false
        if (AppDataSingleton.getUserInfo.role.equals("admin", ignoreCase = true)){
            isAdmin = true
        }

        myDownlineRecyclerAdapter = MyDownlineRecyclerAdapter(mContext, myDownlineList, isAdmin, findNavController()){
            releaseCredit(it._id)
        }
        recyclerViewMyDownline.adapter = myDownlineRecyclerAdapter
        recyclerViewMyDownline.layoutManager = layoutManager
        binding.progressBarMyDownline.visibility = View.GONE

        var text = ""

        if (totalUsers > currentPage * limit){
            text = "Showing 1 - ${currentPage * limit} of $totalUsers users"
            binding.btnLoadMoreMyDownline.visibility = View.VISIBLE
        } else {
            text = "Showing 1 - $totalUsers of $totalUsers users"
            binding.btnLoadMoreMyDownline.visibility = View.GONE
        }

        binding.txtPage.text = text
    }

    private fun populateAdditionalData(){
        myDownlineRecyclerAdapter.notifyItemRangeInserted((currentPage-1)*limit+1, limit)
        binding.progressBarMyDownline.visibility = View.GONE

        var text = ""

        if (totalUsers > currentPage * limit){
            text = "Showing 1 - ${currentPage * limit} of $totalUsers users"
            binding.btnLoadMoreMyDownline.visibility = View.VISIBLE
        } else {
            text = "Showing 1 - $totalUsers of $totalUsers users"
            binding.btnLoadMoreMyDownline.visibility = View.GONE
        }

        binding.txtPage.text = text
    }

    private fun getMyDownline(role: String, page: Int, sort: Int){

        if (!ConnectionManager().isOnline(mContext)){
            AppDataSingleton.noInternet(mContext)
            return
        }

        if (loadingMyDownline){
            return
        }

        loadingMyDownline = true

        binding.btnLoadMoreMyDownline.visibility = View.GONE
        binding.progressBarMyDownline.visibility = View.GONE

        val url = getString(R.string.homeUrl) + "api/v1/user/myDownline?role=$role&page=$page&sort=$sort"

        val jsonObjectRequest = object: JsonObjectRequest(
            Method.GET,
            url,
            null,
            Response.Listener {
                try {

                    loadingMyDownline = false

                    val success = it.getBoolean("success")

                    if (success){

                        myDownlineList.clear()

                        totalUsers = it.getInt("itemsCount")

                        if (totalUsers == 0){
                            Toast.makeText(mContext,
                            "No Users", Toast.LENGTH_LONG).show()
                            myDownlineRecyclerAdapter.notifyDataSetChanged()
                            binding.progressBarMyDownline.visibility = View.GONE
                            return@Listener
                        }

                        val data = it.getJSONArray("data")

                        for (i in 0 until data.length()){

                            val user = data.getJSONObject(i)

                            val myDownline = MyDownline(
                                user.getString("_id"),
                                user.getString("userId"),
                                user.getString("name"),
                                user.getString("role"),
                                user.getDouble("credit")
                            )

                            myDownlineList.add(myDownline)

                        }

                        if (currentPage == 1){
                            populateRecyclerView(myDownlineList)
                        }
                        else{
                            populateAdditionalData()
                        }

                        if (totalUsers > currentPage * limit){
                            binding.btnLoadMoreMyDownline.visibility = View.VISIBLE
                            binding.progressBarMyDownline.visibility = View.GONE
                        }
                        else{
                            binding.btnLoadMoreMyDownline.visibility = View.GONE
                            binding.progressBarMyDownline.visibility = View.GONE
                        }

                    }

                }
                catch (e: Exception){
                    loadingMyDownline = false
                    Toast.makeText(mContext, "An error occurred: $e", Toast.LENGTH_LONG).show()
                }

            },
            Response.ErrorListener {
                loadingMyDownline = false
                Toast.makeText(mContext, "No users found!", Toast.LENGTH_LONG).show()
            }
        ){
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/json"
                headers["auth-token"] = AppDataSingleton.getAuthToken
                return headers
            }
        }

        queue.add(jsonObjectRequest)
    }

    private fun releaseCredit(userId: String){

        val url = "${getString(R.string.homeUrl)}api/v1/admin/user/releaseCredit/$userId"

        val jsonObjectRequest = object: JsonObjectRequest(
            Method.GET,
            url,
            null,
            Response.Listener {
                try {

                    val success = it.getBoolean("success")

                    if (success){



                    }

                }
                catch (e: Exception){

                }

            },
            Response.ErrorListener {

            }
        ){
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/json"
                headers["auth-token"] = AppDataSingleton.getAuthToken
                return headers
            }
        }

        queue.add(jsonObjectRequest)

    }

}