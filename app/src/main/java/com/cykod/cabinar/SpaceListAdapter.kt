package com.cykod.cabinar

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.squareup.picasso.Picasso
import org.w3c.dom.Text


class SpaceListAdapter(
    private val context: Context,
    private val dataSource: ArrayList<CabinSpace>) : BaseAdapter() {

    private val inflater: LayoutInflater
            = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int {
        return dataSource.size
    }

    //2
    override fun getItem(position: Int): Any {
        return dataSource[position]
    }

    //3
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    //4
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // Get view for row item

        val view: View
        val holder: ViewHolder


        if(convertView == null) {
            view = inflater.inflate(R.layout.spacelist_row, parent, false)

            holder = ViewHolder()

            holder.titleTextView = view.findViewById(R.id.spaceTitleID) as TextView
            holder.subtitleTextView = view.findViewById(R.id.spaceSubtitleID) as TextView
            holder.iconImageView = view.findViewById(R.id.spaceIconID) as ImageView

            view.tag = holder
        } else {
            view = convertView
            holder = convertView.tag as ViewHolder
        }


        var space = getItem(position) as CabinSpace

        var titleTextView = holder.titleTextView
        var subtitleTextView = holder.subtitleTextView
        var iconImageView = holder.iconImageView

        titleTextView.text = space.name
        subtitleTextView.text = space.tagline

        Picasso.with(context).load(space.iconUrl).placeholder(R.mipmap.ic_launcher).into(iconImageView)

        return view
    }

    private class ViewHolder {
        lateinit var titleTextView: TextView
        lateinit var subtitleTextView: TextView
        lateinit var iconImageView: ImageView
    }

}