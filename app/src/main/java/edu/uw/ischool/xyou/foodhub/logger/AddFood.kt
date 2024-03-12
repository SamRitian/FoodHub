package edu.uw.ischool.xyou.foodhub.logger

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import edu.uw.ischool.xyou.foodhub.R
import edu.uw.ischool.xyou.foodhub.data.FoodItem
import edu.uw.ischool.xyou.foodhub.utils.JsonParser
import edu.uw.ischool.xyou.foodhub.utils.VolleyService
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class AddFood: Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    lateinit var searchBar : EditText
    private val BASEURL = "https://foodhub-backend.azurewebsites.net/api/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_food, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchBar = view.findViewById<EditText>(R.id.search_bar)
        val searchBtn = view.findViewById<Button>(R.id.search_btn)


        searchBtn.setOnClickListener{
            Log.i("TEST", "button clicked")
            lifecycleScope.launch {
                try {
                    val searchRes = findFood(view, searchBar.text.toString())
//                    Log.i("TEST", searchRes.toString())
                } catch (e: Exception) {
                    Log.e("ERROR", "Failed to fetch data", e)
                }
            }
        }
    }

    private suspend fun findFood(view : View, input: String): List<FoodItem> {
        val url = "${BASEURL}logger/search/${input}"
        val completableDeferred = CompletableDeferred<List<FoodItem>>()

        val request = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                val data = JsonParser().parseSearchFood(response.toString())
                Log.i("DATA", "res: $data")
                completableDeferred.complete(data)
                showRes(view, data)
            },
            { error ->
                Log.e("ERROR", "Error: $error")
                completableDeferred.completeExceptionally(error)
            }
        )
        VolleyService.getInstance(requireActivity()).add(request)

        return completableDeferred.await()
    }

    private fun showRes(view: View, itemsList: List<FoodItem>) {
        val itemsView = view.findViewById<ListView>(R.id.results)

        val adapter = CustomListAdapter(requireContext(), requireActivity(), lifecycleScope, itemsList, true)
        itemsView.adapter = adapter
    }
}