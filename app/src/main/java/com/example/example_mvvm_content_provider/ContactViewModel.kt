package com.example.example_mvvm_content_provider

import android.annotation.SuppressLint
import android.app.Application
import android.content.ContentValues
import android.database.Cursor
import android.os.Build
import android.provider.ContactsContract
import android.util.Log
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
        _listContact.value = contactList
    }

    fun convertContact() {
        val contactList = _listContact.value
        contactList?.forEach { item ->
            when (item.number.length) {
                11 -> {
                    val number = item.number.substring(3)
                    updateNumberPhone("03$number", item.id)
                }

                12 -> {
                    val number = item.number.substring(4)
                    updateNumberPhone("03$number", item.id)
                }
                else -> {
                    item.number = item.number
                }
            }
        }
        _listContact.postValue(contactList!!)
    }

    fun updateContact(newNumberPhone: String, newName: String, pos: Int) {
        val contacts = _listContact.value
        contacts?.get(pos)?.let { updateName(newName, it.id) }
        contacts?.get(pos)?.let { updateNumberPhone(newNumberPhone, it.id) }
    }

    private fun updateName(newName: String, idContact: String) {
        val condition =
            "${ContactsContract.Data.CONTACT_ID} = ? AND ${ContactsContract.Data.MIMETYPE} = ?"
        val params =
            arrayOf(idContact, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
        val contentValue = ContentValues().apply {
            put(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, newName)
        }
        contentResolver.update(ContactsContract.Data.CONTENT_URI, contentValue, condition, params)
    }


    private fun updateNumberPhone(newNumberPhone: String, idContact: String) {
        val condition =
            "${ContactsContract.Data.CONTACT_ID} = ? AND ${ContactsContract.Data.MIMETYPE} = ?"
        val params = arrayOf(idContact, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
        val contentValue = ContentValues().apply {
            put(ContactsContract.CommonDataKinds.Phone.NUMBER, newNumberPhone)
        }
        contentResolver.update(ContactsContract.Data.CONTENT_URI, contentValue, condition, params)
    }

    fun deleteContact(listIdContact: MutableList<String>) {
        val cursor: Cursor? = contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            arrayOf(ContactsContract.Contacts._ID),
            "${ContactsContract.Contacts._ID} = ?",
            listIdContact.toTypedArray(),
            null
        )

        cursor?.use {
            if (cursor.moveToFirst()) {
                val idContact =
                    cursor.getLong(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
                Log.e("TAG", "Hello")
                /*val contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, idContact)
                val deleteCount = contentResolver.delete(contactUri, null, null)
                if (deleteCount > 0) {
                    Log.e("ContactDeleted", "Contact deleted: $idContact")
                } else {
                    Log.e("ContactDeleted", "Failed to delete contact: $idContact")
                }*/
            }
        }
    }


    fun addContact(name: String, numberPhone: String): Int {
        val rawContactUri =
            contentResolver.insert(
                ContactsContract.RawContacts.CONTENT_URI,
                ContentValues()
            ) // tạo uri rỗng để chứa contact mới
        val rawContactId =
            rawContactUri?.lastPathSegment?.toLong() ?: return 0 // lấy id của uri rỗng

        val nameValues = ContentValues().apply {
            put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
            put(
                ContactsContract.Data.MIMETYPE,
                ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
            )
            put(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, name)
        }

        contentResolver.insert(ContactsContract.Data.CONTENT_URI, nameValues)

        val numberPhoneValues = ContentValues().apply {
            put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
            put(
                ContactsContract.Data.MIMETYPE,
                ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
            )
            put(
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                numberPhone
            ) // phoneNumber là số điện thoại bạn muốn thêm
            put(
                ContactsContract.CommonDataKinds.Phone.TYPE,
                ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE
            ) // loại số điện thoại
        }

        contentResolver.insert(ContactsContract.Data.CONTENT_URI, numberPhoneValues)
        return 1
    }
}