package com.example.realtimenotes

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.realtimenotes.databinding.ActivityUpdateUserBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class UpdateUserActivity : AppCompatActivity() {
    lateinit var updateUserBinding: ActivityUpdateUserBinding

    val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    val myRefrence: DatabaseReference = database.reference.child("MyUsers")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_user)
        updateUserBinding = ActivityUpdateUserBinding.inflate(layoutInflater)
        val view = updateUserBinding.root
        getAndSetData()
        updateUserBinding.UpdateBTN.setOnClickListener {
            UpdateData()
        }
        setContentView(view)
    }

    fun getAndSetData(){
        val name = intent.getStringExtra("name")
        val age = intent.getIntExtra("age",1)
        val email = intent.getStringExtra("email")

        updateUserBinding.NameUpdateEDTV.setText(name)
        updateUserBinding.AgeUpdateEDTV.setText(age.toString())
        updateUserBinding.EmailUpdateEDTV.setText(email)
        
    }
    fun UpdateData(){
        val updateName = updateUserBinding.NameUpdateEDTV.text.toString()
        val updateEmail = updateUserBinding.EmailUpdateEDTV.text.toString()
        val updateAge = updateUserBinding.AgeUpdateEDTV.text.toString().toInt()
        val userID = intent.getStringExtra("id").toString()

        val userMap = mutableMapOf<String,Any>()//Data is stored & can br Transferred in firebase using Map of key and Values
        userMap["userID"] = userID
        userMap["userName"] = updateName
        userMap["userEmail"] = updateEmail
        userMap["userAge"] = updateAge

        myRefrence.child(userID).updateChildren(userMap).addOnCompleteListener {task->
            if(task.isSuccessful){
                Toast.makeText(applicationContext,"The $updateName has benn Updated",Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}