package com.example.realtimenotes

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.realtimenotes.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    lateinit var loginBinding: ActivityLoginBinding
    var auth: FirebaseAuth = FirebaseAuth.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loginBinding = ActivityLoginBinding.inflate(layoutInflater)
        val view = loginBinding.root
        setContentView(view)

        loginBinding.LoginBTN.setOnClickListener {
            val loginEmail = loginBinding.LoginEmailID.text.toString()
            val loginPassword = loginBinding.LoginPasswordID.text.toString()
            loginInWithFirebase(loginEmail,loginPassword)

        }
        loginBinding.LoginSignUpBTN.setOnClickListener {

            val intent = Intent(this@LoginActivity,SignUpActivity::class.java)
            startActivity(intent)

        }
        loginBinding.ForgotPassTV.setOnClickListener {

            val intent = Intent(this@LoginActivity,ForgetActivity::class.java)
            startActivity(intent)

        }
        loginBinding.PhoneNumberSignInTV.setOnClickListener {

            val intent = Intent(this@LoginActivity,PhoneNumberActivity::class.java)
            startActivity(intent)
            finish()

        }
    }
    override fun onStart() {
        super.onStart()
        val user = auth.currentUser // gets user info using auth object until the user logs out

        if(user != null){ //if user has already logged in then user variable will have a value
            //Action to be taken if user hase already logged in once
            val intent = Intent(applicationContext,MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    fun loginInWithFirebase(userEmail: String,userPassword: String) {
        auth.signInWithEmailAndPassword(userEmail, userPassword)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Toast.makeText(this@LoginActivity,"Login Successful", Toast.LENGTH_SHORT).show()
                    val intent = Intent(applicationContext,MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {

                    // If sign in fails, display a message to the user.
                    Toast.makeText(this@LoginActivity,task.exception?.toString(), Toast.LENGTH_SHORT).show()

                }
            }
    }
}