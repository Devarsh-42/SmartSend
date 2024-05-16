package com.example.realtimenotes

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.realtimenotes.databinding.ActivityAddUserBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import java.util.UUID

class AddUserActivity : AppCompatActivity() {
    lateinit var addUserBinding: ActivityAddUserBinding
    var database: FirebaseDatabase = FirebaseDatabase.getInstance()
    var myReference: DatabaseReference = database.reference.child("MyUsers")

    lateinit var activityResultLauncher: ActivityResultLauncher<Intent>

    var ImageUri : Uri? = null
    val firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()
    val storageReference: StorageReference = firebaseStorage.reference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_user)
        addUserBinding = ActivityAddUserBinding.inflate(layoutInflater)
        val view = addUserBinding.root
        setContentView(view)

        supportActionBar?.title = "Add User"

        //Resister Activity Result Launcher object in OnCreate
        registerActivityForResult()

        addUserBinding.AddBTN.setOnClickListener {
            uploadPhoto()
        }
        addUserBinding.UserProfileImage.setOnClickListener {
            chooseImage()
        }
    }
    fun registerActivityForResult() {
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val resultCode = result.resultCode
            val imageData = result.data

            if(resultCode == RESULT_OK && imageData != null){
                ImageUri = imageData.data
                //Picasso library -  used to show any image to show in Imageview using its Uri/url
                ImageUri?.let {
                    Picasso.get().load(it).into(addUserBinding.UserProfileImage)
                }
            }
            // Add your code to handle the result here
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            activityResultLauncher.launch(intent)
        }else{
            //Notify user if they don't allow you won't be able to upload the Image
        }
    }

    fun chooseImage() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES)

        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if (ContextCompat.checkSelfPermission(this, permissions[0]) != PackageManager.PERMISSION_GRANTED) {
            // Request the permission here, e.g., using ActivityCompat.requestPermissions
            ActivityCompat.requestPermissions(this, permissions,1)
        }else{
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            activityResultLauncher.launch(intent)

        }
    }
    fun uploadPhoto() {
        addUserBinding.AddBTN.isClickable = false
        addUserBinding.progressBar.visibility = View.VISIBLE

        val userId = myReference.push().key.toString() // Generate a unique key for the user
        val imageName = UUID.randomUUID().toString()

        val imageReference = storageReference.child("images").child(userId).child(imageName)
        ImageUri?.let { uri ->
            imageReference.putFile(uri).addOnSuccessListener { _ ->
                // Image upload successful, now get the download URL
                imageReference.downloadUrl.addOnSuccessListener { url ->
                    val imageURL = url.toString()
                    addUserToDatabase(userId, imageURL,imageName)
                }.addOnFailureListener { e ->
                    // Handle failure to get image download URL
                    Toast.makeText(this, "Failed to get image download URL: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    restoreUIState()
                }
            }.addOnFailureListener { e ->
                // Handle failure to upload image
                Toast.makeText(this, "Failed to upload image: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                restoreUIState()
            }
        } ?: run {
            // Handle case where ImageUri is null
            Toast.makeText(this, "ImageUri is null", Toast.LENGTH_SHORT).show()
            restoreUIState()
        }
    }

    fun addUserToDatabase(userId: String, imageUrl: String,imageName: String) {
        val name: String = addUserBinding.NameEDTV.text.toString()
        val email: String = addUserBinding.EmailEDTV.text.toString()
        val age: Int = addUserBinding.AgeEDTV.text.toString().toInt()

        val user = Users(userId, name, email, age, imageUrl,imageName)
        myReference.child(userId).setValue(user).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(applicationContext, "User Added to Database", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(applicationContext, "Failed to add user to database: ${task.exception}", Toast.LENGTH_SHORT).show()
            }
            restoreUIState()
        }
    }
    fun restoreUIState() {
        addUserBinding.AddBTN.isClickable = true
        addUserBinding.progressBar.visibility = View.INVISIBLE
    }
}