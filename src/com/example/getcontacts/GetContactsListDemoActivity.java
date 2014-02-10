
package com.example.getcontacts;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.GroupMembership;
import android.provider.ContactsContract.Contacts;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.javacodegeeks.android.androidphonecontactsexample.R;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class GetContactsListDemoActivity extends Activity {
    public static final String TAG = "contacts";
    public ListView lv_contact;
    private MyAdapter contactAdapter;

    private ArrayList<Long> contactList = null;

    Uri CONTENT_URI = ContactsContract.Contacts.CONTENT_URI;
    String _ID = ContactsContract.Contacts._ID;
    String DISPLAY_NAME = ContactsContract.Contacts.DISPLAY_NAME;
    String FAVORIATE = ContactsContract.Contacts.STARRED;
    String GROUP_ID = GroupMembership.GROUP_ROW_ID;
    String HAS_PHONE_NUMBER = ContactsContract.Contacts.HAS_PHONE_NUMBER;

    Uri PhoneCONTENT_URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
    String Phone_CONTACT_ID = ContactsContract.CommonDataKinds.Phone.CONTACT_ID;
    String NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;
    String NUMBER_CATEGORY = ContactsContract.CommonDataKinds.Phone.TYPE;

    Uri EmailCONTENT_URI = ContactsContract.CommonDataKinds.Email.CONTENT_URI;
    String EmailCONTACT_ID = ContactsContract.CommonDataKinds.Email.CONTACT_ID;
    String DATA = ContactsContract.CommonDataKinds.Email.DATA;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        lv_contact = (ListView) findViewById(R.id.lv_contact);

        setContactList();
        contactAdapter = new MyAdapter(this);
        lv_contact.setAdapter(contactAdapter);
    }

    private void setContactList() {
        if (contactList == null) {
            contactList = new ArrayList<Long>();
        } else {
            contactList.clear();
        }
        ContentResolver contentResolver = getContentResolver();
        Cursor cursor = contentResolver.query(CONTENT_URI, null, null, null, null);

        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                Long contact_id = cursor.getLong(cursor.getColumnIndex(_ID));
                contactList.add(contact_id);
            }
        }
        cursor.close();
    }

    private StringBuffer getPhoneInfo(Long contact_id) {
        StringBuffer phoneNumber = new StringBuffer();
        ContentResolver contentResolver = getContentResolver();
        // Query and loop for every phone number of the contact
        Cursor phoneCursor = contentResolver.query(PhoneCONTENT_URI, null, Phone_CONTACT_ID + " = ?", new String[] {
                contact_id.toString()
        }, null);
        while (phoneCursor.moveToNext()) {
            String number = phoneCursor.getString(phoneCursor.getColumnIndex(NUMBER));
            int phoneNumberType = phoneCursor.getInt(phoneCursor.getColumnIndex(NUMBER_CATEGORY));
            if (number != null) {
                phoneNumber.append("\nPhone number: " + number);
                phoneNumber.append("\nPhone type: " + getPhoneType(phoneNumberType));
            }

        }

        phoneCursor.close();
        return phoneNumber;
    }

    private String getUserName(Long contact_id) {
        String userName = null;
        ContentResolver contentResolver = getContentResolver();
        // Query and loop for every phone number of the contact
        Cursor cursor = contentResolver.query(CONTENT_URI, null, _ID + " = ?", new String[] {
                contact_id.toString()
        }, null);

        cursor.moveToFirst();
        userName = cursor.getString(cursor.getColumnIndex(DISPLAY_NAME));

        cursor.close();
        return userName;
    }

    private StringBuffer getEmail(Long contact_id) {

        StringBuffer emailBuffer = new StringBuffer();

        ContentResolver contentResolver = getContentResolver();
        // Query and loop for every email of the contact
        Cursor emailCursor = contentResolver.query(EmailCONTENT_URI, null, EmailCONTACT_ID + " = ?", new String[] {
                contact_id.toString()
        }, null);

        while (emailCursor.moveToNext()) {

            String email = emailCursor.getString(emailCursor.getColumnIndex(DATA));

            emailBuffer.append("\nEmail:" + email);
        }

        emailCursor.close();

        return emailBuffer;
    }

    private void getFavoriate(Long contact_id, TextView v) {
        ContentResolver contentResolver = getContentResolver();
        // Query and loop for every phone number of the contact
        Cursor cursor = contentResolver.query(CONTENT_URI, null, _ID + " = ?", new String[] {
                contact_id.toString()
        }, null);

        cursor.moveToFirst();
        String favoriate = cursor.getString(cursor.getColumnIndex(FAVORIATE));
        //  String group=cursor.getString(cursor.getColumnIndex(GROUP_ID));
        for (int i = 0; i < cursor.getColumnCount(); i++) {
            Log.w(TAG, "cursor name:" + cursor.getColumnName(i));
        }

        if (favoriate.equals("1")) {
            v.setTextColor(Color.RED);
        } else {
            v.setTextColor(Color.BLACK);
        }

        cursor.close();
    }

    private String getPhoneType(int type) {
        String phoneNumberType = null;
        Log.w(TAG, "type:" + type);
        switch (type) {
            case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
                phoneNumberType = "Home";
                break;
            case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
                phoneNumberType = "Mobile";
                break;
            case ContactsContract.CommonDataKinds.Phone.TYPE_COMPANY_MAIN:
                phoneNumberType = "Company";
                break;
            case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
                phoneNumberType = "Work";
                break;
            default:
                break;
        }

        return phoneNumberType;
    }

    public InputStream openPhoto(long contactId) {
        Uri contactUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, contactId);
        Uri photoUri = Uri.withAppendedPath(contactUri, Contacts.Photo.CONTENT_DIRECTORY);
        Cursor cursor = getContentResolver().query(photoUri,
                new String[] {
                    Contacts.Photo.PHOTO
                }, null, null, null);
        if (cursor == null) {
            return null;
        }
        try {
            if (cursor.moveToFirst()) {
                byte[] data = cursor.getBlob(0);
                if (data != null) {
                    return new ByteArrayInputStream(data);
                }
            }
        } finally {
            cursor.close();
        }
        return null;
    }

    public InputStream openDisplayPhoto(long contactId) {
        Uri contactUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, contactId);
        Uri displayPhotoUri = Uri.withAppendedPath(contactUri, Contacts.Photo.DISPLAY_PHOTO);
        try {
            AssetFileDescriptor fd =
                    getContentResolver().openAssetFileDescriptor(displayPhotoUri, "r");
            return fd.createInputStream();
        } catch (IOException e) {
            return null;
        }
    }

    public class MyAdapter extends BaseAdapter {

        private LayoutInflater myInflater;

        public MyAdapter(Context ctxt) {
            myInflater = LayoutInflater.from(ctxt);
        }

        @Override
        public int getCount()
        {
            return contactList.size();
        }

        @Override
        public Object getItem(int position)
        {
            return contactList.get(position);
        }

        @Override
        public long getItemId(int position)
        {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            ViewHolder holder;

            if (convertView == null) {
                convertView = myInflater.inflate(R.layout.list_item, null);

                holder = new ViewHolder();
                holder.userPic = (ImageView) convertView.findViewById(R.id.iv_user_pic);
                holder.userName = (TextView) convertView.findViewById(R.id.tv_user_name);
                holder.userNumber = (TextView) convertView.findViewById(R.id.tv_user_phone_number);
                holder.userEmail = (TextView) convertView.findViewById(R.id.tv_user_email);
                convertView.setTag(holder);

                convertView.setTag(holder);
            }
            else {
                holder = (ViewHolder) convertView.getTag();
            }
            Long contactId = contactList.get(position);
            getFavoriate(contactId, holder.userName);
            holder.userName.setText(getUserName(contactId));
            holder.userNumber.setText(getPhoneInfo(contactId));
            holder.userPic.setImageBitmap(BitmapFactory.decodeStream(openPhoto(contactId)));
            holder.userEmail.setText(getEmail(contactId));

            return convertView;
        }
        class ViewHolder {
            ImageView userPic;
            TextView userName;
            TextView userNumber;
            TextView userEmail;
        }
    }

}
