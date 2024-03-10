package edu.uw.ischool.xyou.foodhub.logger

import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.marginTop
import androidx.core.view.setMargins
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import edu.uw.ischool.xyou.foodhub.R
import edu.uw.ischool.xyou.foodhub.data.Logger
import edu.uw.ischool.xyou.foodhub.utils.JsonParser
import edu.uw.ischool.xyou.foodhub.utils.VolleyService
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [LoggerFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LoggerFragment : Fragment() {
    private val BASE_URL = "https://foodhub-backend.azurewebsites.net/api"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_logger, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpCards(view)
    }

    private fun setUpCards(view: View) {
        var logInfo = JSONObject()
        lifecycleScope.launch {
            try {
                logInfo = fetchLoggerData()
            } catch (e: Exception) {
                Log.e("ERROR", "Failed to fetch data", e)
            }
        }

        // there has to be a better way to do this
        val meal = arrayOf("breakfast", "lunch", "dinner", "snack")
        val calories = arrayOf(R.id.breakfast_cal, R.id.lunch_cal, R.id.snack_cal, R.id.dinner_cal)
        val btns = arrayOf(R.id.breakfast_btn, R.id.lunch_btn, R.id.snack_btn, R.id.dinner_btn)
        val cards = arrayOf(R.id.breakfast_card, R.id.lunch_card, R.id.snack_card, R.id.dinner_card)

        for (i in calories.indices) {
            val mealCal = view.findViewById<TextView>(calories[i])

            mealCal.text = logInfo.get("calPerMeal").toString()
        }

        for (btn in btns.indices) {
            val addBtn = view.findViewById<android.widget.Button>(btns[btn])
            addBtn.setOnClickListener{
                activity?.supportFragmentManager?.beginTransaction()?.replace(R.id.container, AddFood())?.commit()
            }
        }

        for (i in cards.indices) {
            val card = view.findViewById<LinearLayout>(cards[i])
            card.setOnClickListener {
                activity?.supportFragmentManager?.beginTransaction()?.replace(R.id.container, ViewLog())?.commit()
            }
        }

    }

    private suspend fun fetchLoggerData(): JSONObject {
        val url = "${BASE_URL}logger?username=johndoe"
        val completableDeferred = CompletableDeferred<JSONObject>()

        val request = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                Log.i("DATA", response.toString())
                completableDeferred.complete(response)
            },
            { error ->
                Log.e("ERROR", "Error: $error")
                completableDeferred.completeExceptionally(error)
            }
        )
        VolleyService.getInstance(requireActivity()).add(request)

        return completableDeferred.await()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment LoggerFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            LoggerFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}