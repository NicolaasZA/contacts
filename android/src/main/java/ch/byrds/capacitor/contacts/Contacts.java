package ch.byrds.capacitor.contacts;

import android.Manifest;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Event;
import android.provider.ContactsContract.CommonDataKinds.Organization;
import android.provider.ContactsContract.CommonDataKinds.Phone;

import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.NativePlugin;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@NativePlugin(
        permissionRequestCode = Contacts.REQUEST_CODE,
        permissions = {
                Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS,
        }
)
public class Contacts extends Plugin {
    public static final int REQUEST_CODE = 0x1651; // Unique request code
    private static final String CONTACT_ID = "contactId";
    private static final String EMAILS = "emails";
    private static final String PHONE_NUMBERS = "phoneNumbers";
    private static final String DISPLAY_NAME = "displayName";
    private static final String ORGANIZATION_NAME = "organizationName";
    private static final String ORGANIZATION_ROLE = "organizationRole";
    private static final String BIRTHDAY = "birthday";

    @PluginMethod
    public void getPermissions(PluginCall call) {
        if (!hasRequiredPermissions()) {
            requestPermissions(call);
        } else {
            JSObject result = new JSObject();
            result.put("granted", true);
            call.success(result);
        }
    }

    @Override
    protected void handleRequestPermissionsResult(
            int requestCode,
            String[] permissions,
            int[] grantResults
    ) {
        super.handleRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
        );

        PluginCall savedCall = getSavedCall();
        JSObject result = new JSObject();

        if (!hasRequiredPermissions()) {
            result.put("granted", false);
            savedCall.success(result);
        } else {
            result.put("granted", true);
            savedCall.success(result);
        }
    }

    @PluginMethod
    public void getContacts(PluginCall call) {
        JSArray jsContacts = getContactsList();

        JSObject result = new JSObject();
        result.put("contacts", jsContacts);

        call.success(result);
    }

    @PluginMethod
    public void getGroups(PluginCall call) {
        JSObject result = new JSObject();
        JSArray jsGroups = new JSArray();
        Cursor dataCursor = getContext()
                .getContentResolver()
                .query(ContactsContract.Groups.CONTENT_URI, null, null, null, null);

        while (dataCursor.moveToNext()) {
            JSObject jsGroup = new JSObject();
            String groupId = dataCursor.getString(
                    dataCursor.getColumnIndex(ContactsContract.Groups._ID)
            );
            jsGroup.put("groupId", groupId);
            jsGroup.put(
                    "accountType",
                    dataCursor.getString(
                            dataCursor.getColumnIndex(ContactsContract.Groups.ACCOUNT_TYPE)
                    )
            );
            jsGroup.put(
                    "accountName",
                    dataCursor.getString(
                            dataCursor.getColumnIndex(ContactsContract.Groups.ACCOUNT_NAME)
                    )
            );
            jsGroup.put(
                    "title",
                    dataCursor.getString(
                            dataCursor.getColumnIndex(ContactsContract.Groups.TITLE)
                    )
            );
            jsGroups.put(jsGroup);
        }
        dataCursor.close();

        result.put("groups", jsGroups);
        call.success(result);
    }

    @PluginMethod
    public void getContactGroups(PluginCall call) {
        Cursor dataCursor = getContext()
                .getContentResolver()
                .query(
                        ContactsContract.Data.CONTENT_URI,
                        new String[]{
                                ContactsContract.Data.CONTACT_ID,
                                ContactsContract.Data.DATA1,
                        },
                        ContactsContract.Data.MIMETYPE + "=?",
                        new String[]{
                                ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE,
                        },
                        null
                );

        Map<String, Set<String>> contact2GroupMap = new HashMap<>();
        while (dataCursor.moveToNext()) {
            String contact_id = dataCursor.getString(0);
            String group_id = dataCursor.getString(1);

            Set<String> groups = new HashSet<>();
            if (contact2GroupMap.containsKey(contact_id)) {
                groups = contact2GroupMap.get(contact_id);
            }
            groups.add(group_id);
            contact2GroupMap.put(contact_id, groups);
        }
        dataCursor.close();

        JSObject result = new JSObject();
        for (Map.Entry<String, Set<String>> entry : contact2GroupMap.entrySet()) {
            JSArray jsGroups = new JSArray();
            Set<String> groups = entry.getValue();
            for (String group : groups) {
                jsGroups.put(group);
            }
            result.put(entry.getKey(), jsGroups);
        }

        call.success(result);
    }

    @PluginMethod
    public void deleteContact(PluginCall call) {
        Uri uri = Uri.withAppendedPath(
                ContactsContract.Contacts.CONTENT_LOOKUP_URI,
                call.getString(CONTACT_ID)
        );
        getContext().getContentResolver().delete(uri, null, null);

        JSObject result = new JSObject();
        call.success(result);
    }

    @PluginMethod
    public void addContact(PluginCall call) {
        // Get input fields
        String contactId = call.getString(CONTACT_ID);
        String firstName = call.getString("firstName");
        String familyName = call.getString("familyName");
        String organizationName = call.getString(ORGANIZATION_NAME);
        JSArray phoneNumbers = call.getArray(PHONE_NUMBERS);
        JSArray emails = call.getArray(EMAILS);
        try {
            boolean isUpdate = false;
            boolean success = false;

            JSArray contacts = getContactsList();

            JSObject newContact = new JSObject();
            newContact.put(CONTACT_ID, contactId);
            newContact.put(DISPLAY_NAME, firstName + " " + familyName);
            newContact.put(ORGANIZATION_NAME, organizationName);
            newContact.put(PHONE_NUMBERS, phoneNumbers);
            newContact.put(EMAILS, emails);

            if (!contactId.isEmpty()) {
                // look for existing if contact ID is provided
                for (int index = 0; index < contacts.length(); index++) {
                    JSObject contact = (JSObject) contacts.get(index);

                    // IF existing, update
                    if (contact.getString(CONTACT_ID).equals(contactId)) {
                        isUpdate = true;
                        success = updateExistingContact(newContact, contactId);
                    }
                }
            }

            // No match was found, or no contact ID was specified. Create a new contact.
            if (!isUpdate) {
                success = createNewContact(newContact);
            }

            JSObject result = new JSObject();
            result.put("action", isUpdate ? "updated" : "added");
            result.put("success", success);
            call.success(result);
        } catch (Exception e) {
            call.error(e.getMessage());
        }
    }

    private boolean createNewContact(JSObject newContact) {
        try {
            String DisplayName = newContact.getString(DISPLAY_NAME);
            String MobileNumber = null;
            String emailID = null;
            String company = newContact.getString(ORGANIZATION_NAME);

            JSArray phones = (JSArray) newContact.get(PHONE_NUMBERS);
            if (phones.length() > 0) {
                MobileNumber = phones.getString(0);
            }

            JSArray emails = (JSArray) newContact.get(EMAILS);
            if (emails.length() > 0) {
                emailID = emails.getString(0);
            }

            ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

            ops.add(ContentProviderOperation.newInsert(
                    ContactsContract.RawContacts.CONTENT_URI)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                    .build());

            //------------------------------------------------------ Names
            if (DisplayName != null) {
                ops.add(ContentProviderOperation.newInsert(
                        ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(ContactsContract.Data.MIMETYPE,
                                ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                        .withValue(
                                ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
                                DisplayName).build());
            }

            //------------------------------------------------------ Mobile Number
            if (MobileNumber != null) {
                ops.add(ContentProviderOperation.
                        newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(ContactsContract.Data.MIMETYPE,
                                Phone.CONTENT_ITEM_TYPE)
                        .withValue(Phone.NUMBER, MobileNumber)
                        .withValue(Phone.TYPE,
                                Phone.TYPE_WORK)
                        .build());
            }

            //------------------------------------------------------ Email
            if (emailID != null) {
                ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(ContactsContract.Data.MIMETYPE,
                                ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Email.DATA, emailID)
                        .withValue(ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_WORK)
                        .build());
            }

            //------------------------------------------------------ Organization
            if (!company.equals("")) {
                ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(ContactsContract.Data.MIMETYPE,
                                ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Organization.COMPANY, company)
                        .withValue(ContactsContract.CommonDataKinds.Organization.TYPE, ContactsContract.CommonDataKinds.Organization.TYPE_WORK)
                        .build());
            }

            // Asking the Contact provider to create a new contact
            getContext().getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);

//      ContentResolver contentResolver = getContext().getContentResolver();
//
//      ContentValues cv = new ContentValues();
//      cv.put(ContactsContract.CommonDataKinds.Organization.TITLE, newContact.getString(ORGANIZATION_NAME));
//      cv.put(Phone.DISPLAY_NAME, newContact.getString(DISPLAY_NAME));
//      cv.put(ContactsContract.RawContacts.ACCOUNT_TYPE, null);
//      cv.put(ContactsContract.RawContacts.ACCOUNT_NAME, null);
//
//      // TODO MULTIPLE CELL PHONE
//      JSArray phones = (JSArray) newContact.get(PHONE_NUMBERS);
//      if (phones.length() > 0) {
//        String phone = phones.getString(0);
//        cv.put(Phone.NUMBER, phone);
//        cv.put(Phone.TYPE, Phone.TYPE_MOBILE);
//      }
//
//      // TODO MULTIPLE EMAILS
//      JSArray emails = (JSArray) newContact.get(EMAILS);
//      if (emails.length() > 0) {
//        String email = emails.getString(0);
//        cv.put(ContactsContract.CommonDataKinds.Email.DATA, email);
//      }
//
//      contentResolver.insert(ContactsContract.RawContacts.CONTENT_URI, cv);

            return true;
        } catch (Exception exc) {
            exc.printStackTrace();
            return false;
        }
    }

    private boolean updateExistingContact(JSObject newContact, String existingContactID) throws JSONException, OperationApplicationException, RemoteException {
        String displayName = newContact.getString(DISPLAY_NAME);
        String workPhone = null;
        String workEmail = null;

        JSArray phones = (JSArray) newContact.get(PHONE_NUMBERS);
        if (phones.length() > 0) {
            workPhone = phones.getString(0);
        }

        JSArray emails = (JSArray) newContact.get(EMAILS);
        if (emails.length() > 0) {
            workEmail = emails.getString(0);
        }

        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

        // UPDATE PHONE NUMBER
        if (workPhone != null) {
            String where = ContactsContract.Contacts._ID + "=? AND "
                    + ContactsContract.Data.MIMETYPE + "=? AND "
                    + String.valueOf(Phone.TYPE) + "=?";

            String[] params = new String[]{
                    existingContactID,
                    Phone.CONTENT_ITEM_TYPE,
                    String.valueOf(Phone.TYPE_WORK)
            };

            ops.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                    .withSelection(where, params)
                    .withValue(Phone.NUMBER, workPhone)
                    .build()
            );
        }

        // UPDATE EMAIL
        if (workEmail != null) {
            String where = ContactsContract.Contacts._ID + "=? AND "
                    + ContactsContract.Data.MIMETYPE + "=? AND "
                    + String.valueOf(ContactsContract.CommonDataKinds.Email.TYPE) + "=?";

            String[] params = new String[]{
                    existingContactID,
                    Email.CONTENT_ITEM_TYPE,
                    String.valueOf(Email.TYPE_WORK)
            };

            ops.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                    .withSelection(where, params)
                    .withValue(Email.DATA, workEmail)
                    .build()
            );
        }

        // Update name
        if (displayName != null) {
            // TODO Update display name
        }

        getContext().getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
        return true;
    }

    private JSArray getContactsList() {
        JSArray jsContacts = new JSArray();
        ContentResolver contentResolver = getContext().getContentResolver();

        String[] projection = new String[]{
                ContactsContract.Data.MIMETYPE,
                Event.TYPE,
                Organization.TITLE,
                ContactsContract.Contacts._ID,
                ContactsContract.Data.CONTACT_ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Contactables.DATA,
        };
        String selection = ContactsContract.Data.MIMETYPE + " in (?, ?, ?, ?)";
        String[] selectionArgs = new String[]{
                Email.CONTENT_ITEM_TYPE,
                Phone.CONTENT_ITEM_TYPE,
                Event.CONTENT_ITEM_TYPE,
                Organization.CONTENT_ITEM_TYPE,
        };

        Cursor contactsCursor = contentResolver.query(
                ContactsContract.Data.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null
        );

        if (contactsCursor != null && contactsCursor.getCount() > 0) {
            HashMap<Object, JSObject> contactsById = new HashMap<>();

            while (contactsCursor.moveToNext()) {
                String _id = contactsCursor.getString(
                        contactsCursor.getColumnIndex(ContactsContract.Contacts._ID)
                );
                String contactId = contactsCursor.getString(
                        contactsCursor.getColumnIndex(ContactsContract.Data.CONTACT_ID)
                );

                JSObject jsContact = new JSObject();

                if (!contactsById.containsKey(contactId)) {
                    // this contact does not yet exist in HashMap,
                    // so put it to the HashMap

                    jsContact.put(CONTACT_ID, contactId);
                    String displayName = contactsCursor.getString(
                            contactsCursor.getColumnIndex(
                                    ContactsContract.Contacts.DISPLAY_NAME
                            )
                    );
                    jsContact.put(DISPLAY_NAME, displayName);
                    JSArray jsPhoneNumbers = new JSArray();
                    jsContact.put(PHONE_NUMBERS, jsPhoneNumbers);
                    JSArray jsEmailAddresses = new JSArray();
                    jsContact.put(EMAILS, jsEmailAddresses);

                    jsContacts.put(jsContact);
                } else {
                    // this contact already exists,
                    // retrieve it
                    jsContact = contactsById.get(contactId);
                }

                if (jsContact != null) {
                    String mimeType = contactsCursor.getString(
                            contactsCursor.getColumnIndex(ContactsContract.Data.MIMETYPE)
                    );
                    String data = contactsCursor.getString(
                            contactsCursor.getColumnIndex(
                                    ContactsContract.CommonDataKinds.Contactables.DATA
                            )
                    );

                    // email
                    if (mimeType.equals(Email.CONTENT_ITEM_TYPE)) {
                        try {
                            // add this email to the list
                            JSArray emailAddresses = (JSArray) jsContact.get(EMAILS);
                            emailAddresses.put(data);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    // phone
                    else if (mimeType.equals(Phone.CONTENT_ITEM_TYPE)) {
                        try {
                            // add this phone to the list
                            JSArray jsPhoneNumbers = (JSArray) jsContact.get(PHONE_NUMBERS);
                            jsPhoneNumbers.put(data);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    // birthday
                    else if (mimeType.equals(Event.CONTENT_ITEM_TYPE)) {
                        int eventType = contactsCursor.getInt(
                                contactsCursor.getColumnIndex(Event.TYPE)
                        );
                        if (eventType == Event.TYPE_BIRTHDAY) {
                            jsContact.put(BIRTHDAY, data);
                        }
                    }
                    // organization
                    else if (mimeType.equals(Organization.CONTENT_ITEM_TYPE)) {
                        jsContact.put(ORGANIZATION_NAME, data);
                        String organizationRole = contactsCursor.getString(
                                contactsCursor.getColumnIndex(Organization.TITLE)
                        );
                        if (organizationRole != null) {
                            jsContact.put(ORGANIZATION_ROLE, organizationRole);
                        }
                    }

                    contactsById.put(contactId, jsContact);
                }
            }
        }
        if (contactsCursor != null) {
            contactsCursor.close();
        }

        return jsContacts;
    }

}
