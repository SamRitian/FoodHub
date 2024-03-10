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
import edu.uw.ischool.xyou.foodhub.data.Food
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
    private val BASEURL = "https://foodhub-backend.azurewebsites.net/api"

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
        showRes(view)
        super.onViewCreated(view, savedInstanceState)

        searchBar = view.findViewById<EditText>(R.id.search_bar)
        val searchBtn = view.findViewById<Button>(R.id.search_btn)

        searchBar.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                searchBtn.isEnabled
            }
        })

        searchBtn.setOnClickListener{
            lifecycleScope.launch {
                try {
                    val searchRes = findFood(searchBar.text.toString())
                    Log.i("PLS", searchRes.toString())
                } catch (e: Exception) {
                    Log.e("ERROR", "Failed to fetch data", e)
                }
            }
        }
    }

    private suspend fun findFood(input: String): List<Food> {
        val url = "${BASEURL}logger?username=janedoe"
        val completableDeferred = CompletableDeferred<List<Food>>()

        val request = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                val logged = JsonParser().parseFood(response.toString())
                Log.i("DATA", logged.toString())
                completableDeferred.complete(logged)
            },
            { error ->
                Log.e("ERROR", "Error: $error")
                completableDeferred.completeExceptionally(error)
            }
        )
        VolleyService.getInstance(requireActivity()).add(request)

        return completableDeferred.await()
    }

    private fun showRes(view: View) {
        val itemsView = view.findViewById<ListView>(R.id.results)

        val testList = listOf<ArrayList<String>>(arrayListOf("Toasted white bread", "Per 100g - Calories: 293kcal | Fat: 4.00g | Carbs: 54.40g | Protein: 9.00g"),
            arrayListOf("Toasted whole bread", "Per 100g - Calories: 282kcal | Fat: 4.42g | Carbs: 51.32g | Protein: 9.95g"),
            arrayListOf("Honey wheat toast", "Per 1 serving - Calories: 120kcal | Fat: 0.50g | Carbs: 25.00g | Protein: 5.00g"))

        val adapter = CustomListAdapter(requireContext(), testList, true)
        itemsView.adapter = adapter
    }
}