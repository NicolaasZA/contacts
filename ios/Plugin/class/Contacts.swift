//
//  Contacts.swift
//  Plugin
//
//  Created by Jonathan Gerber on 16.02.20.
//  Copyright © 2020 Byrds & Bytes GmbH. All rights reserved.
//

import Foundation
import Contacts

class Contacts {
    class func getContactFromCNContact() throws -> [CNContact] {

        let contactStore = CNContactStore()
        let keysToFetch = [
            CNContactFormatter.descriptorForRequiredKeys(for: .fullName),
            CNContactPhoneNumbersKey,
            CNContactGivenNameKey,
            CNContactMiddleNameKey,
            CNContactFamilyNameKey,
            CNContactEmailAddressesKey,
            CNContactOrganizationNameKey
            ] as [Any]

        //Get all the containers
        var allContainers: [CNContainer] = []
        allContainers = try contactStore.containers(matching: nil)


        var results: [CNContact] = []

        // Iterate all containers and append their contacts to our results array
        for container in allContainers {

            let fetchPredicate = CNContact.predicateForContactsInContainer(withIdentifier: container.identifier)

            let containerResults = try contactStore.unifiedContacts(matching: fetchPredicate, keysToFetch: keysToFetch as! [CNKeyDescriptor])
            results.append(contentsOf: containerResults)
        }

        return results
    }
    
    class func updateContact(_ id: String, contact: CNMutableContact) -> Bool {
        print(contact)
        
        let store = CNContactStore()
        let request = CNSaveRequest()
        request.update(contact)
        
        do {
            try store.execute(request)
            return true
        } catch _{
            return false
        }
    }
    
    class func addContact(_ contact: CNMutableContact) -> Bool {
        print(contact)
        
        let store = CNContactStore()
        let request = CNSaveRequest()
        request.add(contact, toContainerWithIdentifier: nil)
        
        do {
            try store.execute(request)
            return true
        } catch _{
            return false
        }
    }
    
    class func createContactObject(_ givenName: String, _ familyName: String, _ orgName: String, _ phoneNumbers: [String], _ emailAddrs: [String]) -> CNMutableContact {
        let obj = CNMutableContact()
        obj.givenName = givenName
        obj.familyName = familyName
        obj.organizationName = orgName
        
        return copyAddrAndNum(obj, phoneNumbers, emailAddrs)
    }
    
    class func copyAddrAndNum(_ contact: CNMutableContact, _ phoneNumbers: [String], _ emailAddrs: [String]) -> CNMutableContact {
        // Copy phone numbers
        if (phoneNumbers != nil && phoneNumbers.isEmpty == false) {
            contact.phoneNumbers = []
            for number in phoneNumbers {
                let phoneNumObj = CNPhoneNumber(stringValue: number)
                contact.phoneNumbers.append(CNLabeledValue(label: CNLabelHome, value: phoneNumObj))
            }
        }
        
        // Copy email addresses
        if (emailAddrs != nil && emailAddrs.isEmpty == false) {
            contact.emailAddresses = []
            for email in emailAddrs {
                contact.emailAddresses.append(CNLabeledValue(label: CNLabelHome, value: email as NSString))
            }
        }
        
        // Done
        return contact
    }
}


