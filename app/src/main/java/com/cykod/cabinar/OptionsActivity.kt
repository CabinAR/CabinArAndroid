package com.cykod.cabinar

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity;

import kotlinx.android.synthetic.main.activity_options.*

class OptionsActivity : AppCompatActivity() {

    companion object {
        fun newIntent(context: Context): Intent {
            val detailIntent = Intent(context, OptionsActivity::class.java)
            return detailIntent
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_options)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

}
