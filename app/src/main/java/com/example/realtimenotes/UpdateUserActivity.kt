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
import com.example.realtimenotes.databinding.ActivityUpdateUserBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso

class UpdateUserActivity : AppCompatActivity() {
    lateinit var updateUserBinding: ActivityUpdateUserBinding

    val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    val myRefrence: DatabaseReference = database.reference.child("MyUsers")

    lateinit var activityResultLauncher: ActivityResultLauncher<Intent>

    var ImageUri : Uri? = null
    val firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()
    val storageReference: StorageReference = firebaseStorage.reference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_user)
        updateUserBinding = ActivityUpdateUserBinding.inflate(layoutInflater)
        val view = updateUserBinding.root
        getAndSetData()
        updateUserBinding.UpdateBTN.setOnClickListener {
            uploadPhoto()
        }
        setContentView(view)
        registerActivityForResult()

        updateUserBinding.UserUpdatedProfileImage.setOnClickListener {
            chooseImage()
        }
    }
    fun uploadPhoto() {
        updateUserBinding.UpdateBTN.isClickable = false
        updateUserBinding.progressBar.visibility = View.VISIBLE

        val userID = intent.getStringExtra("id").toString() // Use the existing user ID obtained from the intent
        val imageName = intent.getStringExtra("imageName").toString()

        val imageReference = storageReference.child("images").child(userID).child(imageName)

        ImageUri?.let { uri ->
            imageReference.putFile(uri).addOnSuccessListener { _ ->
                // Image upload successful, now get the download URL
                Toast.makeText(this, "Image Updated", Toast.LENGTH_SHORT).show()
                imageReference.downloadUrl.addOnSuccessListener { url ->
                    val imageURL = url.toString()
                    UpdateData(imageURL,imageName)
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


    private fun restoreUIState() {

        updateUserBinding.UpdateBTN.isClickable = true
        updateUserBinding.progressBar.visibility = View.INVISIBLE

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

    fun getAndSetData(){
        val subject = intent.getStringExtra("Subject")
        val EmailText = intent.getStringExtra("EmailText")
        val email = intent.getStringExtra("email")
        val imageUrl = intent.getStringExtra("imageUrl").toString()

        updateUserBinding.UpdateEmailEDTV.setText(email)
        updateUserBinding.UpdateSubjectEDT.setText(subject)
        updateUserBinding.UpdateEmailTextEDT.setText(EmailText)

        Picasso.get().load(imageUrl).into(updateUserBinding.UserUpdatedProfileImage)
    }

    fun registerActivityForResult() {
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val resultCode = result.resultCode
            val imageData = result.data

            if(resultCode == RESULT_OK && imageData != null){
                ImageUri = imageData.data
                //Picasso library -  used to show any image to show in Imageview using its Uri/url
                ImageUri?.let {
                    Picasso.get().load(it).into(updateUserBinding.UserUpdatedProfileImage)
                }
            }
            // Add your code to handle the result here
        }
    }
    fun UpdateData(imageUrl : String,imageName : String) {
        val updateSubject = updateUserBinding.UpdateSubjectEDT.text.toString()
        val updateEmail = updateUserBinding.UpdateEmailEDTV.text.toString()
        val updateEmailText = updateUserBinding.UpdateEmailTextEDT.text.toString()
        val userID = intent.getStringExtra("id").toString()

        val userMap = mutableMapOf<String, Any>()
        userMap["userName"] = updateSubject
        userMap["userEmail"] = updateEmail
        userMap["userAge"] = updateEmailText
        userMap["url"] = imageUrl
        userMap["imageName"] = imageName


        myRefrence.child(userID).updateChildren(userMap).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(applicationContext, "The $updateSubject has been Updated", Toast.LENGTH_SHORT).show()
                restoreUIState()
                finish()
            } else {
                Toast.makeText(applicationContext, "Failed to update user data", Toast.LENGTH_SHORT).show()
                restoreUIState()
            }
        }
    }

}