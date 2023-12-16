package com.example.expp

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!isInternetAvailable()) {
            // No internet, switch to the "No Internet" layout
            setContentView(R.layout.no_internet)

            val retryButton = findViewById<Button>(R.id.retryButton)
            retryButton.setOnClickListener {
                val progress = findViewById<ProgressBar>(R.id.progressBar11)
                val nointernet = findViewById<TextView>(R.id.nointernet)
                val internetstablish = findViewById<TextView>(R.id.stablishinternet)
                progress.visibility = View.VISIBLE
                retryButton.visibility = View.GONE
                nointernet.visibility = View.GONE
                internetstablish.visibility = View.VISIBLE
                Handler().postDelayed({ // Start the main activity or any other activity after the delay
                    // Finish the splash activity to prevent going back to it
                    progress.visibility = View.GONE
                    retryButton.visibility = View.VISIBLE
                    nointernet.visibility = View.VISIBLE
                    internetstablish.visibility = View.GONE
                }, 4000)
                // Retry button is clicked, check for internet again or take appropriate action
                if (isInternetAvailable()) {
                    // Internet is available now, switch back to the main layout
                    setContentView(R.layout.activity_main)


                }

            }

            }
        else
        {

        }
            Handler().postDelayed({ // Start the main activity or any other activity after the delay
                val authManager = AuthManager(this)

                if (authManager.isLoggedIn) {
                    startActivity(Intent(this, UserListActivity::class.java))

                } else {
                    val intent = Intent(this, loginpage::class.java)
                    startActivity(intent)
                    // Show login UI
                    // ...
                }

                // Finish the splash activity to prevent going back to it
                finish()
            }, 4000)
            supportActionBar?.hide()
        }


    private fun isInternetAvailable(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val networkCapabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            return networkCapabilities != null &&
                    (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
        } else {
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            return activeNetworkInfo != null && activeNetworkInfo.isConnected
        }

    }
}