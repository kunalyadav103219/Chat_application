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
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class loginpage : AppCompatActivity() {
    private lateinit var username: EditText
    private lateinit var pas: EditText
    private lateinit var loginbtn: Button
    private lateinit var register: TextView
    private lateinit var forget: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var progressbar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loginpage)
        auth = FirebaseAuth.getInstance()
        username = findViewById(R.id.lname)
        pas = findViewById(R.id.lpass)
        loginbtn = findViewById(R.id.loginBtn)
        register = findViewById(R.id.rbtn)
        forget = findViewById(R.id.fbtn)
        database = FirebaseDatabase.getInstance().reference
        progressbar = findViewById(R.id.progressBar)
        if (!isInternetAvailable()) {
            // No internet, switch to the "No Internet" layout
            setContentView(R.layout.no_internet)

            val retryButton = findViewById<Button>(R.id.retryButton)
            retryButton.setOnClickListener {
                val progress=findViewById<ProgressBar>(R.id.progressBar11)
                val nointernet=findViewById<TextView>(R.id.nointernet)
                val internetstablish=findViewById<TextView>(R.id.stablishinternet)
                progress.visibility=View.VISIBLE
                retryButton.visibility=View.GONE
                nointernet.visibility=View.GONE
                internetstablish.visibility=View.VISIBLE
                Handler().postDelayed({ // Start the main activity or any other activity after the delay
                    // Finish the splash activity to prevent going back to it
                    progress.visibility=View.GONE
                    retryButton.visibility=View.VISIBLE
                    nointernet.visibility=View.VISIBLE
                    internetstablish.visibility=View.GONE
                }, 4000)
                // Retry button is clicked, check for internet again or take appropriate action
                if (isInternetAvailable()) {
                    // Internet is available now, switch back to the main layout
                    setContentView(R.layout.activity_loginpage)
                }
            }
        } else {
            // Internet is available, proceed with app functionality
        }
    
        register.setOnClickListener {
            val intent = Intent(this, signup::class.java)
            startActivity(intent)
            finish()
        }
        forget.setOnClickListener {
            showResetPasswordDialog()
        }
        loginbtn.setOnClickListener {

            if (validation()) {
                progressbar.visibility = View.VISIBLE
                auth.signInWithEmailAndPassword(username.text.toString(), pas.text.toString())
                    .addOnCompleteListener(this)
                    { task ->

                        if (task.isSuccessful) {
                            val em = auth.currentUser?.isEmailVerified
                            if (em == true) {

                                Toast.makeText(this, "Login successful", Toast.LENGTH_LONG).show()
                                val authManager = AuthManager(this)
                                authManager.setLoggedIn(true)
                                val intent = Intent(this, UserListActivity::class.java)
                                startActivity(intent)
                                progressbar.visibility = View.GONE
                                finish()
                            } else {
                                Toast.makeText(this, "Verify your email", Toast.LENGTH_LONG).show()
                            }
                        } else {
                            Toast.makeText(this, "Register your self", Toast.LENGTH_LONG).show()
                            progressbar.visibility = View.GONE
                        }
                    }


            }
        }
    }

    private fun isInternetAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            return networkCapabilities != null &&
                    (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
        } else {
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            return activeNetworkInfo != null && activeNetworkInfo.isConnected
        }
    }

    private fun showResetPasswordDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialogbox, null)
        val emailEditText = dialogView.findViewById<EditText>(R.id.gmailforget)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Reset Password")
            .setView(dialogView)
            .setPositiveButton("Reset") { _, _ ->
                val email = emailEditText.text.toString().trim()

                if (email.isEmpty()) {
                    // Handle empty email
                } else {
                    auth.sendPasswordResetEmail(email)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(this,"password forget link send successful on your gmail",Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(this,"First you should register yourself",Toast.LENGTH_LONG).show()
                                // Handle failed email sending
                            }
                        }
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        dialog.show()
    }

    private fun validation():Boolean {
        val e=username.text.toString()
        if (username.length()==0)
        {
            username.error="username is required"
            username.requestFocus()
            return false
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(e).matches()) {
            username.error = "Invalid username"
            username.requestFocus()
            return false
        }
        if (pas.length()==0)
        {
            pas.error="password is required"
            pas.requestFocus()
            return false
        }
        if (pas.length()<6)
        {
            pas.error="password should 6 digit"
            pas.requestFocus()
            return false
        }
        return true
    }
}