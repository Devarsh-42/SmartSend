package com.example.realtimenotes

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.realtimenotes.databinding.ActivityMainBinding
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class MainActivity : AppCompatActivity() {
    lateinit var mainBinding : ActivityMainBinding

    val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    val myReference: DatabaseReference = database.reference.child("MyUsers")

    val userList = ArrayList<Users>()
    lateinit var userAdapter: UserAdapter
    lateinit var toolbar: MaterialToolbar

    val imageList = ArrayList<String>()

    val firebaseStorage : FirebaseStorage = FirebaseStorage.getInstance()
    val storageReference : StorageReference = firebaseStorage.reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        val view = mainBinding.root
        setContentView(view)

        toolbar = findViewById(R.id.toolbarID)

        mainBinding.AddFAB.setOnClickListener {
            val intent = Intent(this@MainActivity, AddUserActivity::class.java)
            startActivity(intent)
        }

        toolbar.setOnMenuItemClickListener {item->
            when(item.itemId){
                R.id.DeleteAll -> showAlertDialogBox()

                R.id.SignOutID -> {
                    FirebaseAuth.getInstance().signOut()
                    val intent = Intent(this@MainActivity,LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
            return@setOnMenuItemClickListener true
        }

        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT){
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                TODO("Not yet implemented")
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                viewHolder.adapterPosition
                //We will do the Deletion Process using IDs of the User
                val id = userAdapter.getUserId(viewHolder.adapterPosition)

                val name = userAdapter.getUserName(viewHolder.adapterPosition)

                myReference.child(id).removeValue() // Selected user will be deleted

                //Deleting the Images in Cloud Storage
                val imageName = userAdapter.getImageName(viewHolder.adapterPosition)
                val imageReference = storageReference.child("images").child(imageName)

                imageReference.delete()

                Toast.makeText(this@MainActivity,
                    "The User: $name was Deleted from Database",Toast.LENGTH_SHORT).show()
            }

        }).attachToRecyclerView(mainBinding.recyclerViewID)//Attaches the ItemTouchHelper Method to Recycler View

        retrieveData()
    }

    fun showAlertDialogBox() {
        val alertDialog = AlertDialog.Builder(this@MainActivity)
        alertDialog.setMessage("By Clicking on Yes All the Notes Will be Deleted")
        alertDialog.setTitle("Warning!!")
        alertDialog.setIcon(R.drawable.round_warning_24)
        alertDialog.setNegativeButton("Cancel") { dialogInterface, i ->
            dialogInterface.cancel()
        }
        alertDialog.setPositiveButton("Yes"){dialogInterface,_->
            myReference.addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (eachUser in snapshot.children) {
                        val user = eachUser.getValue(Users::class.java)
                        if (user != null) {
                            imageList.add(user.imageName)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
            myReference.removeValue().addOnCompleteListener { task->
                userAdapter.notifyDataSetChanged()
                Toast.makeText(this@MainActivity,"All Users Were Deleted",Toast.LENGTH_SHORT).show()
            } // Deletes the MyUser From the Database
        }.show()
    }

    fun retrieveData() {
        myReference.addValueEventListener(object : ValueEventListener { //Should Delete The DataList before entering the new data
            override fun onDataChange(snapshot: DataSnapshot) {

                userList.clear()

                for (eachUser in snapshot.children) {
                    val user = eachUser.getValue(Users::class.java)
                    if (user != null) {
                        println("UserID: ${user.userID}")
                        println("UserName: ${user.userName}")
                        println("UserEmail: ${user.userEmail}")
                        println("UserAge: ${user.userAge}")
                        println("**************************")
                        userList.add(user)
                    }
                }

                // Move the adapter initialization and RecyclerView setup outside the loop
                userAdapter = UserAdapter(this@MainActivity, userList)
                mainBinding.recyclerViewID.layoutManager = LinearLayoutManager(this@MainActivity)
                mainBinding.recyclerViewID.adapter = userAdapter
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle cancellation if needed
            }
        })
    }
}
