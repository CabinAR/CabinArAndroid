package com.cykod.cabinar

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName


interface JSONConvertable {
    fun toJSON(): String = Gson().toJson(this)
}

inline fun <reified T: JSONConvertable> String.toObject(): T = Gson().fromJson(this, T::class.java)


data class CabinSpace(
    @SerializedName("id")
    var id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("tagline")
    val tagline: String?,
    @SerializedName("icon_url")
    val iconUrl: String?,
    @SerializedName("pieces")
    val pieces: Array<CabinPiece>) : JSONConvertable


data class CabinPiece(
    @SerializedName("id")
    var id: Int,
    @SerializedName("marker_url")
    var markerUrl: String,
    @SerializedName("marker_meter_width")
    var markerMeterWidth: Float?,
    @SerializedName("marker_meter_height")
    var markerMeterHeight: Float?,
    @SerializedName("scene")
    var scene: String,
    @SerializedName("assets")
    var assets: String?
) {

}