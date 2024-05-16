package com.example.realtimenotes

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.realtimenotes.databinding.ActivityPhoneNumberBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class PhoneNumberActivity : AppCompatActivity() {
    lateinit var phoneNumberBinding: ActivityPhoneNumberBinding
    var auth: FirebaseAuth = FirebaseAuth.getInstance()

    lateinit var mCallbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks

    var verificationCode = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        phoneNumberBinding = ActivityPhoneNumberBinding.inflate(layoutInflater)
        val view = phoneNumberBinding.root
        setContentView(view)

        phoneNumberBinding.SendOtpBTN.setOnClickListener {
            val userNumber = phoneNumberBinding.PhoneNumber.text.toString()
            val option = PhoneAuthOptions.newBuilder(auth).setPhoneNumber(userNumber)
                .setTimeout(60L,TimeUnit.SECONDS) //Code can be resend till 60 sec
                .setActivity(this@PhoneNumberActivity) //In which activity the OPT will be verified
                .setCallbacks(mCallbacks)
                .build()

            PhoneAuthProvider.verifyPhoneNumber(option)

        }

        phoneNumberBinding.VerifySignInBTN.setOnClickListener {
            signInwithOTP()
        }
        mCallbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks(){
            override fun onVerificationCompleted(p0: PhoneAuthCredential) {

            }
            override fun onVerificationFailed(p0: FirebaseException) {

            }

            override fun onCodeSent(p0: String, p1: PhoneAuthProvider.ForceResendingToken) {
                super.onCodeSent(p0, p1)

                verificationCode = p0 //p0 is the coming verification code
            }
        }
    }
    fun signInwithOTP() {
        val userEnteredCode = phoneNumberBinding.PhoneNumber.text.toString()

        val credential = PhoneAuthProvider.getCredential(verificationCode,userEnteredCode)

        signInWithPhoneAuthCredential(credential)

    }
    fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential){

        auth.signInWithCredential(credential).addOnCompleteListener {task->
            if(task.isSuccessful){

                val intent = Intent(this@PhoneNumberActivity,MainActivity::class.java)
                startActivity(intent)
                finish()
            }
            else{
                Toast.makeText(this@PhoneNumberActivity,"The Code You entered is Incorrect",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }
}