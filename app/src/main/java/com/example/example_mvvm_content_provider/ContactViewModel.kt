package com.example.example_mvvm_content_provider

import android.annotation.SuppressLint
import android.app.Application
import android.database.Cursor
import android.os.Build
import android.provider.ContactsContract
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

@RequiresApi(Build.VERSION_CODES.O)
class ContactViewModel(application: Application) : AndroidViewModel(application) {

    private var _listContact = MutableLiveData<MutableList<Contact>>()
    val listContact: LiveData<MutableList<Contact>> get() = _listContact

    private val contentResolver = getApplication<Application>().contentResolver

    init {
        getContact()
    }


    @SuppressLint("Recycle")
    @RequiresApi(Build.VERSION_CODES.O)
    fun getContact() {
        val contactList = mutableListOf<Contact>()
        val cursor: Cursor? = contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            null,
            null,
            null,
            null
        )

        cursor?.use {
            if (it.count > 0) {
                while (it.moveToNext()) {
                    val id = it.getString(it.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
                    val name =
                        it.getString(it.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME))

                    val phonesCursor: Cursor? = contentResolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                        arrayOf(id),
                        null
                    )
                    phonesCursor?.use { phoneCursor ->
                        while (phoneCursor.moveToNext()) {
                            val number = phoneCursor.getString(
                                phoneCursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
                            )
                            contactList.add(Contact(id, name, number))
                        }
                    }
                }
            }
        }
        _listContact.postValue(contactList)
    }

    fun convertContact() {
        val contactList = _listContact.value
        contactList?.forEach { item ->
            when (item.number.length) {
                11 -> {
                    val number = item.number.substring(3)
                    item.number = "03$number"
                }

                12 -> {
                    val number = item.number.substring(4)
                    item.number = "03$number"
                }
                else -> {
                    item.number = item.number
                }
            }
        }
        _listContact.postValue(contactList!!)
    }


}