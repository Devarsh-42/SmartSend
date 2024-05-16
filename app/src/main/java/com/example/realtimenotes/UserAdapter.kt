package com.example.realtimenotes

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.realtimenotes.databinding.NotesItemBinding
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso

class UserAdapter(val context: Context, private val userList: ArrayList<Users>) :
    RecyclerView.Adapter<UserAdapter.UsersViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsersViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = NotesItemBinding.inflate(inflater, parent, false)
        return UsersViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UsersViewHolder, position: Int) {
        holder.adapterBinding.UserNameID.text = userList[position].userName
        holder.adapterBinding.UserEmailID.text = userList[position].userEmail
        holder.adapterBinding.UserAge.text = userList[position].userAge.toString()

        val imageUrl = userList[position].url
        val imageName = userList[position].imageName

        // Check if imageUrl is not empty or null before loading with Picasso
        if (imageUrl.isNotEmpty()) {
            Picasso.get().load(imageUrl).into(holder.adapterBinding.imageView, object : Callback {
                override fun onSuccess() {
                    holder.adapterBinding.progressBar2.visibility = View.INVISIBLE
                }

                override fun onError(e: Exception?) {
                    Toast.makeText(context, e?.localizedMessage, Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            // Handle the case where imageUrl is empty or null (e.g., set a placeholder image)
            // You can customize this part based on your app's requirements.
            holder.adapterBinding.progressBar2.visibility = View.INVISIBLE
        }

        holder.adapterBinding.LinearLayoutID.setOnClickListener {
            val intent = Intent(context, UpdateUserActivity::class.java)
            intent.putExtra("id", userList[position].userID)
            intent.putExtra("name", userList[position].userName)
            intent.putExtra("age", userList[position].userAge)
            intent.putExtra("email", userList[position].userEmail)
            intent.putExtra("imageUrl",imageUrl)
            intent.putExtra("imageName",imageName)
            context.startActivity(intent)
        }
    }


    override fun getItemCount(): Int {
        return userList.size
    }

    fun getUserId(position: Int): String {
        //Detects/Returns the pos. of the Item to be deleted from the UserList
        return userList[position].userID
    }
    fun getUserName(position: Int): String {
        //Returns the username for the toast Message
        return userList[position].userID
    }
    fun getImageName(position: Int) : String{
        return userList[position].imageName
    }

    inner class UsersViewHolder(val adapterBinding: NotesItemBinding) :
        RecyclerView.ViewHolder(adapterBinding.root)
}
