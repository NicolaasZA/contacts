//
//  Plugin.swift
//
//
//  Created by Jonathan Gerber on 15.02.20.
//  Copyright © 2020 Byrds & Bytes GmbH. All rights reserved.

import Foundation
import Capacitor
import Contacts

@objc(ContactsPlugin)
public class ContactsPlugin: CAPPlugin {

    @objc func getPermissions(_ call: CAPPluginCall) {
        print("checkPermission was triggered in Swift")
        Permissions.contactPermission { granted in
            switch granted {
            case true:
                call.success([
                    "granted": true
                ])
            default:
                call.success([
                    "granted": false
                ])
                }
            }
    }

    @objc func getContacts(_ call: CAPPluginCall) {
        var contactsArray : [PluginResultData] = [];
        Permissions.contactPermission { granted in
            if granted {
                do {
                    let contacts = try Contacts.getContactFromCNContact()

                    for contact in contacts {
                        var phoneNumbers: [String] = []
                        var emails: [String] = []
                        for number in contact.phoneNumbers {
                            let numberToAppend = number.value.stringValue
                            phoneNumbers.append(numberToAppend)
                            print(phoneNumbers)
                        }
                        for email in contact.emailAddresses {
                            let emailToAppend = email.value as String
                            emails.append(emailToAppend)
                        }
                        let contactResult: PluginResultData = [
                            "contactId": contact.identifier,
                            "firstName": contact.givenName,
                            "lastName": contact.familyName,
                            "organizationName": contact.organizationName,
                            "phoneNumbers": phoneNumbers,
                            "emails": emails
                        ]
                        contactsArray.append(contactResult)
                    }
                    call.success([
                        "contacts": contactsArray
                    ])
                } catch let error as NSError {
                    call.error("Generic Error", error)
                }
            } else {
                call.error("User denied access to contacts")
            }
        }
    }

    @objc func addContact(_ call: CAPPluginCall) {
        Permissions.contactPermission { granted in
            if granted {
                do {
                    // Get data from JS
                    let contactId = call.getString("contactId")
                    let firstName = call.getString("firstName")
                    let familyName = call.getString("familyName")
                    let organizationName = call.getString("organizationName")
                    let phoneNumbers = call.getArray("phoneNumbers", String.self)
                    let emailAddrs = call.getArray("emails", String.self)
                    
                    if (contactId != nil) {
                        
                        // Look for matching contact. If found, set isExisting to true.
                        var isExisting = false
                        
                        let contacts = try Contacts.getContactFromCNContact()
                        for contact in contacts {
                            
                            if (contact.identifier == contactId && !isExisting) {
                                // Found a matching contact, so update
                                isExisting = true
                                
                                var modifiedContact = contact.mutableCopy() as! CNMutableContact
                                if (firstName != nil) {modifiedContact.givenName = firstName!}
                                if (familyName != nil) {modifiedContact.familyName = familyName!}
                                if (organizationName != nil) {modifiedContact.organizationName = organizationName!}
                                
                                modifiedContact = Contacts.copyAddrAndNum(modifiedContact, phoneNumbers!, emailAddrs!)
                                
                                let result = Contacts.updateContact(contact.identifier, contact: modifiedContact)
                                call.success([
                                    "action": "modified",
                                    "success": result
                                ])
                                return;
                            }
                        }
                        
                        if (!isExisting) {
                            // No match was found, this is a new contact.
                            let newContact = Contacts.createContactObject(firstName!, familyName!, organizationName!, phoneNumbers!, emailAddrs!)
                            let result = Contacts.addContact(newContact)
                            call.success([
                                "action": "added",
                                "success": result
                            ])
                        }
                        
                    } else {
                        call.reject("No contact object supplied")
                    }
                } catch let error as NSError {
                    call.error("Generic Error", error)
                }
            } else {
                call.error("User denied access to contacts")
            }
        }
    }
    
    @objc func viewContact(_ call: CAPPluginCall) {
        call.error("Not implemented")
    }

}

