package com.example.realtimenotes

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.realtimenotes.databinding.ActivityAddUserBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class AddUserActivity : AppCompatActivity() {
    lateinit var addUserBinding: ActivityAddUserBinding
    var database: FirebaseDatabase = FirebaseDatabase.getInstance()
    var myReference: DatabaseReference = database.reference.child("MyUsers")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_user)
        addUserBinding = ActivityAddUserBinding.inflate(layoutInflater)
        val view = addUserBinding.root
        setContentView(view)

        supportActionBar?.title = "Add User"

        addUserBinding.AddBTN.setOnClickListener {
            addUserToDatabase()
        }
    }
    fun addUserToDatabase(){
        val name: String = addUserBinding.NameEDTV.text.toString()
        val email: String = addUserBinding.EmailEDTV.text.toString()
        val age: Int = addUserBinding.AgeEDTV.text.toString().toInt()

        val id: String = myReference.push().key.toString() //Generate a unique key in The DB by Firebase

        val user = Users(id, name, email, age)
        myReference.child(id).setValue(user).addOnCompleteListener {task->

            if(task.isSuccessful){
                Toast.makeText(applicationContext, "User Added to Database", Toast.LENGTH_SHORT).show()
                finish()
            }

            else{
                Toast.makeText(applicationContext, task.exception.toString(), Toast.LENGTH_SHORT).show()
            }
        } // register each user under its unique Key

    }
}