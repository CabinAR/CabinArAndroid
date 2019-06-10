package com.cykod.cabinar

import android.content.Context
import com.android.volley.Request

sealed class ApiRoute {

    val timeOut: Int
        get() {
            return 3000
        }

    val baseUrl: String
        get() {
            return "https://www.cabin-ar.com/api"
        }
    val url: String
        get() {
            return "$baseUrl/${when (this) {
                is GetSpaces -> "spaces?latitude=${this.latitude?.toString() ?: ""}&longitude=${this.longitude?.toString() ?: ""}&api_key=${this.apiToken?.toString() ?: ""}"
                is GetSpace -> "spaces/$spaceId?api_key=${this.apiToken?.toString() ?: ""}"
                is GetUser -> "logins?api_key=${this.apiToken}"
                else -> ""
            }}"
        }
    val httpMethod: Int
        get() {
            return when (this) {
                else -> Request.Method.GET
            }
        }

    val params: HashMap<String, String>
        get() {
            return when (this) {
                else -> hashMapOf()
            }
        }

    val headers: HashMap<String, String>
        get() {
            return when (this) {
                else -> hashMapOf()
            }
        }

    data class GetSpaces(var latitude: Double?, var longitude: Double?, var apiToken: String?, var ctx: Context) : ApiRoute()
    data class GetSpace(var spaceId: Int, var apiToken: String?, var ctx: Context) : ApiRoute()
    data class GetUser(var apiToken: String) : ApiRoute()
}