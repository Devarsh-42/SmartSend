package com.example.realtimenotes

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.realtimenotes.databinding.ActivityForgetBinding
import com.google.firebase.auth.FirebaseAuth

class ForgetActivity : AppCompatActivity() {
    lateinit var forgetBinding: ActivityForgetBinding
    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        forgetBinding = ActivityForgetBinding.inflate(layoutInflater)
        val view = forgetBinding.root
        setContentView(view)

        forgetBinding.ResetBTN.setOnClickListener {
            val forgetEmail = forgetBinding.ForgetEmailID.text.toString()
            auth.sendPasswordResetEmail(forgetEmail).addOnCompleteListener{task->
                if(task.isSuccessful){
                    Toast.makeText(applicationContext,"Password Reset Link Sent to $forgetEmail",
                        Toast.LENGTH_SHORT).show()
                    finish()
                }

            }
        }
    }
}