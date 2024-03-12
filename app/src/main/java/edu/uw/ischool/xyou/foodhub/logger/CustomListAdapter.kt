package edu.uw.ischool.xyou.foodhub.logger

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.lifecycleScope
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.google.gson.Gson
import edu.uw.ischool.xyou.foodhub.MainActivity
import edu.uw.ischool.xyou.foodhub.R
import edu.uw.ischool.xyou.foodhub.data.FoodItem
import edu.uw.ischool.xyou.foodhub.data.Logger
import edu.uw.ischool.xyou.foodhub.utils.JsonParser
import edu.uw.ischool.xyou.foodhub.utils.VolleyService
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import org.json.JSONObject

class CustomListAdapter(
    context: Context,
    val activity: Activity,
    val lifecycleScope: LifecycleCoroutineScope,
    val itemList: List<FoodItem>,
    val isAddFood: Boolean
) : ArrayAdapter<FoodItem>(context, R.layout.list_item_layout, itemList) {
    private val BASE_URL = "https://foodhub-backend.azurewebsites.net/api/"
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var itemView = convertView
        if (itemView == null) {
            itemView = LayoutInflater.from(context).inflate(R.layout.list_item_layout, parent, false)
        }

        val currentItem = itemList[position]
        val title = itemView?.findViewById<TextView>(R.id.food_name)
        title?.text = currentItem.name

        val cal = itemView?.findViewById<TextView>(R.id.food_cal)
        cal?.text = "Calories: ${currentItem.calories}"

        val serving = itemView?.findViewById<TextView>(R.id.food_serving)
        serving?.text = "Serving: ${currentItem.serving}"

        val protein = itemView?.findViewById<TextView>(R.id.food_protein)
        protein?.text = "Protein: ${currentItem.protein}"

        val carbs = itemView?.findViewById<TextView>(R.id.food_carbs)
        carbs?.text = "Carbs: ${currentItem.carbs}"

        val fat = itemView?.findViewById<TextView>(R.id.food_fat)
        fat?.text = "Fat: ${currentItem.fat}"

        val btn = itemView?.findViewById<android.widget.Button>(R.id.action_btn)

        if(isAddFood) {
            btn?.setBackgroundResource(R.drawable.add_btn)
            btn?.setOnClickListener{
                lifecycleScope.launch {
                    try {
                        addFood("allison", "breakfast", currentItem)
                    } catch (e: Exception) {
                        Log.e("ERROR", "Failed to fetch data", e)
                    }
                }
            }
        } else {
            btn?.setBackgroundResource(R.drawable.delete_btn)
            btn?.setOnClickListener {
                //delete the food from db
                lifecycleScope.launch {
                    try {
                        deleteFood("allison", "lunch", currentItem.foodId)
                    } catch (e: Exception) {
                        Log.e("ERROR", "Failed to fetch data", e)
                    }
                }
            }
        }
        return itemView!!
    }

    private suspend fun deleteFood(username: String, meal: String, foodItem: String): Boolean {
        val url = "${BASE_URL}logger/delete"
        val completableDeferred = CompletableDeferred<Boolean>()

        Log.i("DATA", "given id: ${foodItem}")

        val params = JSONObject().apply {
            // Add your body parameters here
            put("username", username)
            put("foodId", foodItem)
            put("meal", meal)
        }

        val request = JsonObjectRequest(
            Request.Method.POST, url, params,
            { response ->
                Log.i("DATA", response.toString())
                completableDeferred.complete(true)
            },
            { error ->
                Log.e("ERROR", "Error: $error")
                completableDeferred.completeExceptionally(error)
                completableDeferred.complete(false)
            }
        )
        VolleyService.getInstance(activity).add(request)

        return completableDeferred.await()
    }

    private suspend fun addFood(username: String, meal: String, foodItem: FoodItem): String {
        val url = "${BASE_URL}logger"

        Log.i("SLEEP", "foodItem: ${foodItem}")
        val completableDeferred = CompletableDeferred<String>()

        val gson = Gson()
        val foodItemJSON = gson.toJson(foodItem)

        val params = JSONObject().apply {
            put("username", username)
            put("foodItem", foodItemJSON)
            put("meal", meal)
        }

        val request = JsonObjectRequest(
            Request.Method.POST, url, params,
            { response ->
                Log.i("DATA", response.toString())
                completableDeferred.complete("Successfully added the food item")
            },
            { error ->
                Log.e("ERROR", "Error: $error")
                completableDeferred.completeExceptionally(error)
                completableDeferred.complete("Failed to add food")
            }
        )
        VolleyService.getInstance(activity).add(request)

        return completableDeferred.await()
    }
}