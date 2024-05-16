package com.example.realtimenotes

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.realtimenotes.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth

class SignUpActivity : AppCompatActivity() {
    lateinit var signUpBinding: ActivitySignUpBinding

    val auth: FirebaseAuth = FirebaseAuth.getInstance() //auth object of Firebase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        signUpBinding = ActivitySignUpBinding.inflate(layoutInflater)
        val view = signUpBinding.root
        setContentView(view)

        signUpBinding.SignUpBTN.setOnClickListener {
            val signUpEmail = signUpBinding.SignUpEmailID.text.toString()
            val signUpPassword = signUpBinding.SignUpPasswordID.text.toString()

            signUpWithFirebase(signUpEmail,signUpPassword)
        }
    }
    fun signUpWithFirebase(userEmail: String, userPassword: String){
        auth.createUserWithEmailAndPassword(userEmail,userPassword).addOnCompleteListener {task->

            if(task.isSuccessful){
                Toast.makeText(this@SignUpActivity,"Your Account Has been created",Toast.LENGTH_SHORT).show()
                finish()
            }else{
                Toast.makeText(this@SignUpActivity,task.exception?.toString(),Toast.LENGTH_SHORT).show()

            }

        }
    }
}