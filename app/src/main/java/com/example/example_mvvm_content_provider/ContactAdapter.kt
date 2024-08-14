package com.example.example_mvvm_content_provider

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.example_mvvm_content_provider.databinding.LayoutItemContactBinding

class ContactAdapter(private var listContact: MutableList<Contact>, private val onClick: OnClick) :
    RecyclerView.Adapter<ContactAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: LayoutItemContactBinding) :
        RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
        val binding = LayoutItemContactBinding.inflate(view, parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = listContact.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.binding.tvName.text = listContact[position].name
        holder.binding.tvNumber.text = listContact[position].number

        holder.itemView.setOnClickListener {
            holder.binding.checkSelected.isChecked = !holder.binding.checkSelected.isChecked
            onClick.onClick(position)
        }
        holder.binding.btnUpdate.setOnClickListener {
            onClick.onClickUpdate(position)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateListContact(list : List<Contact>){
        listContact.clear()
        listContact.addAll(list)
        notifyDataSetChanged()
    }

}