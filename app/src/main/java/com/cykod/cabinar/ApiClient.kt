package com.cykod.cabinar

import android.content.Context
import com.android.volley.*
import com.android.volley.toolbox.BasicNetwork
import com.android.volley.toolbox.DiskBasedCache
import com.android.volley.toolbox.HurlStack
import com.android.volley.toolbox.StringRequest
import com.google.gson.GsonBuilder

class ApiClient(private val ctx: Context) {

    /***
     * PERFORM REQUEST
     */
    private fun performRequest(route: ApiRoute, completion: (success: Boolean, response: String) -> Unit) {
        var url  = route.url
        var httpMethod= route.httpMethod

        val request: StringRequest = object : StringRequest(httpMethod, url, { response ->
            this.handle(response, completion)
        }, {
            it.printStackTrace()
            if (it.networkResponse != null && it.networkResponse.data != null)
                this.handle(String(it.networkResponse.data), completion)
            else
                this.handle(getStringError(it), completion)
        }) {
            override fun getParams(): MutableMap<String, String> {
                return route.params
            }

            override fun getHeaders(): MutableMap<String, String> {
                return route.headers
            }
        }
        request.retryPolicy = DefaultRetryPolicy(route.timeOut, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        getRequestQueue().add(request)
    }

    /**
     * This method will make the creation of the answer as ApiResponse
     **/
    private fun handle(response: String, completion: (success: Boolean, response: String) -> Unit) {
        completion.invoke(true, response)
    }

    /**
     * This method will return the error as String
     **/
    private fun getStringError(volleyError: VolleyError): String {
        return when (volleyError) {
            is TimeoutError -> "The conection timed out."
            is NoConnectionError -> "The connection couldn´t be established."
            is AuthFailureError -> "There was an authentication failure in your request."
            is ServerError -> "Error while prosessing the server response."
            is NetworkError -> "Network error, please verify your conection."
            is ParseError -> "Error while prosessing the server response."
            else -> "Internet error"
        }
    }
    /**
     * We create and return a new instance for the queue of Volley requests.
     **/
    private fun getRequestQueue(): RequestQueue {
        val maxCacheSize = 20 * 1024 * 1024
        val cache = DiskBasedCache(ctx.cacheDir, maxCacheSize)
        val netWork = BasicNetwork(HurlStack())
        val mRequestQueue = RequestQueue(cache, netWork)
        mRequestQueue.start()
        System.setProperty("http.keepAlive", "false")
        return mRequestQueue
    }

    fun getSpaces(latitude: Double?, longitude: Double?,completion: (ArrayList<CabinSpace>?, message:String) -> Unit) {
        val route = ApiRoute.GetSpaces(latitude, longitude, ctx)
        this.performRequest(route) { success, response ->
            if(success) {
                val gsonBuilder = GsonBuilder().serializeNulls()
                val gson = gsonBuilder.create()

                var spaceList = gson.fromJson(response, Array<CabinSpace>::class.java).toList()
                var spaces: ArrayList<CabinSpace> = ArrayList(spaceList)
                completion.invoke(spaces, "")
            } else {
                completion.invoke(null, response)
            }
        }

    }

    fun getSpace(spaceId: Int, completion: (CabinSpace?, message:String) -> Unit) {
        val route = ApiRoute.GetSpace(spaceId, ctx)
        this.performRequest(route) { success, response ->
            if(success) {
                val gsonBuilder = GsonBuilder().serializeNulls()
                val gson = gsonBuilder.create()

                var space = gson.fromJson(response, CabinSpace::class.java)
                completion.invoke(space, "")
            } else {
                completion.invoke(null, response)
            }
        }

    }


}