package com.example.example_mvvm_content_provider

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.example_mvvm_content_provider.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity(), OnClick {

    private lateinit var binding: ActivityMainBinding

    private val viewModel: ContactViewModel by viewModels()


    private var listContact = mutableListOf<Contact>()
    private lateinit var adapterContact: ContactAdapter

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
            adapterContact.updateListContact(listContact)
        }

        binding.btnConvert.setOnClickListener {
            viewModel.convertContact()
        }

        binding.btnAddContact.setOnClickListener {
            addContact("abc", "01679812367")
        }

    }

    private fun addContact(name : String, number : String) {
        val contentResolver = contentResolver
        val rawContactUri = contentResolver.insert(ContactsContract.RawContacts.CONTENT_URI, ContentValues())
        val rawContactId = rawContactUri?.lastPathSegment?.toLong() ?: return

        val nameValues = ContentValues().apply {
            put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
            put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
            put(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, name)
        }

        contentResolver.insert(ContactsContract.Data.CONTENT_URI, nameValues)

        /*val numberPhoneValues = ContentValues().apply {
            put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
            put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
            put(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, name)
        }
        contentResolver.insert(ContactsContract.Data.CONTENT_URI, numberPhoneValues)*/

        val numberPhoneValues = ContentValues().apply {
            put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
            put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
            put(ContactsContract.CommonDataKinds.Phone.NUMBER, number) // phoneNumber là số điện thoại bạn muốn thêm
            put(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE) // loại số điện thoại
        }
        contentResolver.insert(ContactsContract.Data.CONTENT_URI, numberPhoneValues)


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
        Toast.makeText(this, listContact[pos].name, Toast.LENGTH_SHORT).show()
    }
}