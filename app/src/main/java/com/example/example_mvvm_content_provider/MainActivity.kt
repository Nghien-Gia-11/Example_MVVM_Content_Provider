package com.example.example_mvvm_content_provider

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.example_mvvm_content_provider.databinding.ActivityMainBinding
import com.example.example_mvvm_content_provider.databinding.LayoutDialogAddContactBinding


class MainActivity : AppCompatActivity(), OnClick {

    private lateinit var binding: ActivityMainBinding
    private lateinit var bindingDialog: LayoutDialogAddContactBinding
    private val viewModel: ContactViewModel by viewModels()

    private lateinit var diaLog: AlertDialog
    private var listContact = mutableListOf<Contact>()
    private lateinit var adapterContact: ContactAdapter
    private var listIdContact = mutableListOf<String>()

    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { isGranted ->
            if (isGranted.all { it.value }) {
                Toast.makeText(this, "Quyền đã được cấp !", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Quyền chưa được cấp !", Toast.LENGTH_LONG).show()
            }
        }


    @SuppressLint("Recycle")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        checkPermission()

        setAdapterContact()

        viewModel.listContact.observe(this) {
            listContact = it
            adapterContact.updateListContact(it)
        }

        binding.btnShowContact.setOnClickListener {
            viewModel.getContact()
        }

        binding.btnConvert.setOnClickListener {
            viewModel.convertContact()
        }

        binding.btnAddContact.setOnClickListener {
            createDiaLogAddContact(true, -1) // true : thêm contact
        }

        binding.btnDelete.setOnClickListener {
            Log.e("TAG", "delete")
            viewModel.deleteContact(listIdContact)
        }

    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createDiaLogAddContact(isAddContact: Boolean, pos: Int) {
        val build = AlertDialog.Builder(this)
        bindingDialog = LayoutDialogAddContactBinding.inflate(LayoutInflater.from(this))
        build.setView(bindingDialog.root)
        bindingDialog.btnCancel.setOnClickListener {
            diaLog.dismiss()
        }
        if (isAddContact) {
            bindingDialog.btnAdd.text = "Add Contact"
            addContact()
        } else {
            bindingDialog.btnAdd.text = "Update"
            updateContact(pos)
        }
        diaLog = build.create()
        diaLog.show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateContact(pos: Int) {
        bindingDialog.edtName.setText(listContact[pos].name)
        bindingDialog.edtNumberPhone.setText(listContact[pos].number)

        bindingDialog.btnAdd.setOnClickListener {
            val newName = bindingDialog.edtName.text.toString()
            val newNumberPhone = bindingDialog.edtNumberPhone.text.toString()
            viewModel.updateContact(newNumberPhone, newName, pos)
            diaLog.dismiss()
            Toast.makeText(this, "Finish", Toast.LENGTH_LONG).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun addContact() {
        var isCheck = true

        bindingDialog.btnAdd.setOnClickListener {
            val name = bindingDialog.edtName.text.toString()
            val numberPhone = bindingDialog.edtNumberPhone.text.toString()
            if (numberPhone.length < 10 || numberPhone.length > 12) {
                isCheck = false
            }
            if (isCheck) {
                if (viewModel.addContact(name, numberPhone) == 1) {
                    Toast.makeText(this, "Thêm thành công", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Thêm thất bại", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Số điện thoại không đúng", Toast.LENGTH_SHORT).show()
                bindingDialog.edtNumberPhone.setText("")
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun checkPermission() {
        val permissions =
            arrayOf(Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS)
        if (permissions.any {
                ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
            }) {
            requestPermission.launch(permissions)
        }
    }

    private fun setAdapterContact() {
        adapterContact = ContactAdapter(listContact, this)
        binding.rvContact.apply {
            adapter = adapterContact
            layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)
            addItemDecoration(
                DividerItemDecoration(
                    this@MainActivity,
                    DividerItemDecoration.VERTICAL
                )
            )
        }
    }

    override fun onClick(pos: Int) {
        listIdContact.add("$pos")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onClickUpdate(pos: Int) {
        createDiaLogAddContact(false, pos) // false : update contact
    }
}