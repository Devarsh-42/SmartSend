package com.example.realtimenotes

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.realtimenotes.databinding.NotesItemBinding

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

        holder.adapterBinding.LinearLayoutID.setOnClickListener {
            val intent = Intent(context, UpdateUserActivity::class.java)
            intent.putExtra("id",userList[position].userID)
            intent.putExtra("name",userList[position].userName)
            intent.putExtra("age",userList[position].userAge)
            intent.putExtra("email",userList[position].userEmail)
            context.startActivity(intent)//Send data to update User Activity
        }
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    inner class UsersViewHolder(val adapterBinding: NotesItemBinding) :
        RecyclerView.ViewHolder(adapterBinding.root)
}
