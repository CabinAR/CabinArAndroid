package com.cykod.cabinar

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity;

import kotlinx.android.synthetic.main.activity_options.*
import kotlinx.android.synthetic.main.content_options.*

class OptionsActivity : AppCompatActivity() {


    private lateinit var apiClient: ApiClient

    private var user:CabinUser? = null

    fun getApiToken():String? {
        var sharedPref = this.getSharedPreferences("cabinar",Context.MODE_PRIVATE)
        return sharedPref.getString("api_token",null)
    }


    fun setApiToken(apiToken: String?) {
        var sharedPref = this.getSharedPreferences("cabinar",Context.MODE_PRIVATE)
        with (sharedPref.edit()) {
            if(apiToken == null) {
                remove("api_token")
            } else {
                putString("api_token", apiToken!!)
            }
            apiClient.apiToken = apiToken
            commit()
        }
    }

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

        login_button.setOnClickListener {
            apiClient.apiToken = cabin_key.text.toString()
            getLogin()
        }

        logout_button.setOnClickListener {
            setApiToken(null)
            user = null
            getLogin()
        }

        apiClient = ApiClient(applicationContext, getApiToken())


        logout_block.visibility = View.INVISIBLE
        getLogin()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }


    fun getLogin() {
        if(apiClient.apiToken != null) {
            apiClient.getUser { loggedInUser, message ->
                if(loggedInUser != null) {
                    setApiToken(apiClient.apiToken)
                    user = loggedInUser
                    showLogout()
                } else {
                    setApiToken(null)
                    user = null
                    showLogin(message)
                }
            }

        } else {
            showLogin()
        }

    }

    fun showLogin(errorMessage: String? = null) {
        logout_block.visibility = View.GONE
        login_block.visibility = View.VISIBLE
        if(errorMessage != null) {
            error_message.text = errorMessage
            error_message.visibility = View.VISIBLE
        } else {
            error_message.visibility = View.GONE
        }
    }

    fun showLogout() {
        if(user != null) {
            user_email.text = user!!.email
            logout_block.visibility = View.VISIBLE
            login_block.visibility = View.GONE
        }


    }



}
