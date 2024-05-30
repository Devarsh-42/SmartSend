package com.example.realtimenotes

import android.Manifest
import android.app.TimePickerDialog
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
import com.google.ai.client.generativeai.GenerativeModel
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import kotlinx.coroutines.runBlocking
import java.util.Calendar
import java.util.UUID

class AddUserActivity : AppCompatActivity() {
    lateinit var addUserBinding: ActivityAddUserBinding
    lateinit var timePickerDialog: TimePickerDialog

    private lateinit var mailScheduler: MailScheduler
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private var ImageUri: Uri? = null
    private val firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()
    private val storageReference: StorageReference = firebaseStorage.reference
    private var database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private var myReference: DatabaseReference = database.reference.child("MyUsers")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addUserBinding = ActivityAddUserBinding.inflate(layoutInflater)
        setContentView(addUserBinding.root)

        supportActionBar?.title = "Add User"
        mailScheduler = MailScheduler(this)

        addUserBinding.PromptBTN.setOnClickListener {
            val subject = addUserBinding.SubjectEDT.text.toString()
            addUserBinding.progressBar.visibility = View.VISIBLE
            val generativeModel = GenerativeModel(
                // The Gemini 1.5 models are versatile and work with both text-only and multimodal prompts
                modelName = "gemini-1.5-flash",
                // Access your API key as a Build Configuration variable (see "Set up your API key" above)
                apiKey = "AIzaSyBYXNb6VF2psWaUS4armxkf0sJLDPSv0x4"
            )
            val user_prompt = addUserBinding.EmailPromptEDT.text.toString()
            val prompt = "Write an professional email on topic: $user_prompt & the Subject : $subject" +
                    "Do not mention the subject of email only generate email text, the email should be not be too short &" +
                    "Use Corporate language and email text Should be in proper format"
            runBlocking {
                val response = generativeModel.generateContent(prompt)
                addUserBinding.EmailTextEDT.setText(response.text)
                addUserBinding.progressBar.visibility = View.INVISIBLE
                val prompt2 = "Generate A Summary of this email for a Sticky Note"
                val response2 = generativeModel.generateContent(prompt2)
            }
        }
        registerActivityForResult()
        setupUI()
    }

    private fun setupUI() {
        addUserBinding.AddBTN.setOnClickListener {
            uploadPhoto()
        }
        addUserBinding.UserProfileImage.setOnClickListener {
            chooseImage()
        }
        addUserBinding.AddTimeBTN.setOnClickListener {
            val calendar = Calendar.getInstance()
            val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
            val currentMinute = calendar.get(Calendar.MINUTE)

            timePickerDialog = TimePickerDialog(
                this,
                TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                    // Handle the selected time here
                    val selectedTime = "$hourOfDay:$minute"
                    addUserBinding.AddTimeBTN.text = selectedTime
                },
                currentHour,
                currentMinute,
                false
            )
            timePickerDialog.show()
        }
    }

    private fun registerActivityForResult() {
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val resultCode = result.resultCode
            val imageData = result.data
            if (resultCode == RESULT_OK && imageData != null) {
                ImageUri = imageData.data
                ImageUri?.let {
                    Picasso.get().load(it).into(addUserBinding.UserProfileImage)
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            val intent = Intent().apply {
                type = "image/*"
                action = Intent.ACTION_GET_CONTENT
            }
            activityResultLauncher.launch(intent)
        }
    }

    private fun chooseImage() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if (ContextCompat.checkSelfPermission(this, permissions[0]) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, permissions, 1)
        } else {
            val intent = Intent().apply {
                type = "image/*"
                action = Intent.ACTION_GET_CONTENT
            }
            activityResultLauncher.launch(intent)
        }
    }

    private fun uploadPhoto() {
        addUserBinding.AddBTN.isClickable = false
        addUserBinding.progressBar.visibility = View.VISIBLE

        val userId = myReference.push().key.toString()
        val imageName = UUID.randomUUID().toString()
        val imageReference = storageReference.child("images").child(userId).child(imageName)

        ImageUri?.let { uri ->
            imageReference.putFile(uri).addOnSuccessListener {
                imageReference.downloadUrl.addOnSuccessListener { url ->
                    val imageURL = url.toString()
                    addUserToDatabase(userId, imageURL, imageName)
                }.addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to get image download URL: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    restoreUIState()
                }
            }.addOnFailureListener { e ->
                Toast.makeText(this, "Failed to upload image: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                restoreUIState()
            }
        } ?: run {
            Toast.makeText(this, "ImageUri is null", Toast.LENGTH_SHORT).show()
            restoreUIState()
        }
    }

    private fun addUserToDatabase(userId: String, imageUrl: String, imageName: String) {
        val subject = addUserBinding.SubjectEDT.text.toString()
        val email = addUserBinding.EmailEDTV.text.toString()
        val emailText = addUserBinding.EmailTextEDT.text.toString()
        val emailTime = addUserBinding.AddTimeBTN.text.toString()

        val user = Users(userId, subject, email, emailText, imageUrl, emailTime, imageName)
        myReference.child(userId).setValue(user).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(applicationContext, "User Added to Database", Toast.LENGTH_SHORT).show()
                scheduleEmail(to = email, subject = subject,body = emailText,time = emailTime)
                finish()
            } else {
                Toast.makeText(applicationContext, "Failed to add user to database: ${task.exception}", Toast.LENGTH_SHORT).show()
            }
            restoreUIState()
        }
    }

    private fun scheduleEmail(to: String, subject: String, body: String, time: String) {
        // Parse time string "HH:mm" to calculate delay
        val currentTime = Calendar.getInstance()
        val targetTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, time.split(":")[0].toInt())
            set(Calendar.MINUTE, time.split(":")[1].toInt())
        }

        val delay = targetTime.timeInMillis - currentTime.timeInMillis
        if (delay > 0) {
            mailScheduler.scheduleMailSending(to, subject, body, delay)
        } else {
            Toast.makeText(this, "Scheduled time is in the past!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initTimepicker() {
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)

        timePickerDialog = TimePickerDialog(
            this,
            TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                val selectedTime = "$hourOfDay:$minute"
                addUserBinding.AddTimeBTN.text = selectedTime
            },
            currentHour,
            currentMinute,
            false
        )
    }

    private fun restoreUIState() {
        addUserBinding.AddBTN.isClickable = true
        addUserBinding.progressBar.visibility = View.INVISIBLE
    }
}
