<p align="center"><br><img src="https://user-images.githubusercontent.com/236501/85893648-1c92e880-b7a8-11ea-926d-95355b8175c7.png" width="128" height="128" /></p>

<h3 align="center">Contacts</h3>
<p align="center"><strong><code>@capacitor-community/contacts</code></strong></p>
<p align="center">
  Capacitor community plugin for fetching contacts.
</p>

<p align="center">
  <img src="https://img.shields.io/maintenance/yes/2020?style=flat-square" />
  <a href="https://github.com/capacitor-community/contacts/actions?query=workflow%3A%22Test+and+Build+Plugin%22"><img src="https://img.shields.io/github/workflow/status/capacitor-community/contacts/Test%20and%20Build%20Plugin?style=flat-square" /></a>
  <a href="https://www.npmjs.com/package/@capacitor-community/contacts"><img src="https://img.shields.io/npm/l/@capacitor-community/contacts?style=flat-square" /></a>
<br>
  <a href="https://www.npmjs.com/package/@capacitor-community/contacts"><img src="https://img.shields.io/npm/dw/@capacitor-community/contacts?style=flat-square" /></a>
  <a href="https://www.npmjs.com/package/@capacitor-community/contacts"><img src="https://img.shields.io/npm/v/@capacitor-community/contacts?style=flat-square" /></a>
  <!-- ALL-CONTRIBUTORS-BADGE:START - Do not remove or modify this section -->
<a href="#contributors-"><img src="https://img.shields.io/badge/all_contributors-4-orange.svg?style=flat-square" /></a>
<!-- ALL-CONTRIBUTORS-BADGE:END -->


## Maintainers

| Maintainer                           | GitHub                                                                                       | Social                       | Sponsoring Company |
| ------------------------------------ | -------------------------------------------------------------------------------------------- | ---------------------------- | ------------------ |
| Jonathan Gerber / Byrds & Bytes GmbH | [idrimi](https://github.com/idrimi) / [Byrds & Bytes GmbH](https://github.com/byrdsandbytes) | [byrds.ch](https://byrds.ch) | Byrds & Bytes GmbH |

Maintenance Status: Actively Maintained

## NOTE: THIS IS A HEAVILY MODIFIED FORK to allow for the viewing/adding/updating of contacts. 

---
## Prerequisites
- Ionic Angular Project with Capacitor already intergrated.
- NodeJS with Node Package Manager installed on the system.

---
## Installation

Install:

```
npm i --save git+https://github.com/NicolaasZA/contacts.git
```

Sync:

```
npx cap sync
```

### iOS

For iOS you need to set a usage description in your info.plist file. (Privacy Setting)
Open xCode search for your info.plist file and press the tiny "+". Add the following entry:
>Privacy - Contacts Usage Description

Give it a value like:
>We need access to your contacts in order to do something.

### Android Notes

For Android you have to add the permisions in your AndroidManifest.xml. Add the following permissions before the closing of the "manifest" tag.

```xml
<uses-permission android:name="android.permission.READ_CONTACTS" />
<uses-permission android:name="android.permission.WRITE_CONTACTS" />
```

Next import the capContacts class to your MainActivity

```java
// Initializes the Bridge
    this.init(savedInstanceState, new ArrayList<Class<? extends Plugin>>() {{
      // Additional plugins you've installed go here
      // Ex: add(TotallyAwesomePlugin.class);
      add(Contacts.class);
    }});
```

Make sure to import it properly as well.

```java
import ch.byrds.capacitor.contacts.Contacts;
```

**NOTE**: On Android you have to ask for permission first, before you can fetch the contacts. Use the `getPermissions()` method before you try to fetch contacts using `getContacts()`.

---
## Usage / Examples

Import the Plugin in your TS file:
```typescript
import { Plugins } from "@capacitor/core";
const  { Contacts } = Plugins;
```

You have the following functions available:
```typescript
export interface ContactsPlugin {
  getPermissions(): Promise<PermissionStatus>;
  getContacts(): Promise<{ contacts: Contact[] }>;
  addContact(contact: any): Promise<{ action: string, success: boolean }>;
  viewContact(contactId: any): Promise<{ action: string, success: boolean }>;
}
```

### getContacts - _(iOS, Android supported)_

Get all contacts:
```typescript
Contacts.getContacts().then(result => {
    console.log(result);
    for (const contact of result.contacts) {
        console.log(contact);
    }
});
```

### addContact - _(iOS supported)_

Add a new contact with given data, or modify existing (matched with contactId)
```typescript
// contact.contactId is already set on a contact received from getContacts().
// Set as empty to create a new contact.
const contact = {
  contactId: '', 
  firstName: 'John',
  familyName?: 'Doe',
  phoneNumbers: [ '1231231234' ],
  emails: [ 'example@domain.com' ],
  organizationName: 'Some Company'
};

Contacts.addContact(contact).then(result => {
    if (result.success) {
      if (result.action == 'added') {
        // New contact was made
      } else {
        // Existing contact (with same contactId) was updated
      }
    } else {
      // Adding failed
    }
});
```

### viewContact - _(not yet supported, but soon)_
Launches a native view of the contact.
```typescript
Contacts.viewContact(contactId).then(result => {
    if (result.success) {
      // No exceptions during native call
    } else {
      // Some error during viewing call
    }
});
```

---
## Interfaces

```typescript
export interface PermissionStatus {
  granted: boolean;
}

export interface Contact { 
  contactId: string;
  firstName?: string;
  familyName?: string;
  phoneNumbers: string[];
  emails: string[];
  organizationName?: string;
}
```

## Built With

- Swift 5
- Java
- Angular
- Capacitor

## Authors

- Jonathan Gerber ([idrimi](https://github.com/idrimi))

## License

MIT

## Contributors

<!-- ALL-CONTRIBUTORS-LIST:START - Do not remove or modify this section -->
<!-- prettier-ignore-start -->
<!-- markdownlint-disable -->
<table>
  <tr>
    <td align="center"><a href="https://github.com/Idrimi"><img src="https://avatars0.githubusercontent.com/u/24573405?v=4?s=100" width="100px;" alt=""/><br /><sub><b>idrimi</b></sub></a><br /><a href="https://github.com/idrimi (Jonathan Gerber)/contacts/commits?author=Idrimi" title="Code">💻</a></td>
    <td align="center"><a href="https://github.com/tafelnl"><img src="https://avatars2.githubusercontent.com/u/35837839?v=4?s=100" width="100px;" alt=""/><br /><sub><b>Tafel</b></sub></a><br /><a href="https://github.com/idrimi (Jonathan Gerber)/contacts/commits?author=tafelnl" title="Code">💻</a></td>
    <td align="center"><a href="http://ionicframework.com/"><img src="https://avatars3.githubusercontent.com/u/11214?v=4?s=100" width="100px;" alt=""/><br /><sub><b>Max Lynch</b></sub></a><br /><a href="https://github.com/idrimi (Jonathan Gerber)/contacts/commits?author=mlynch" title="Documentation">📖</a> <a href="#eventOrganizing-mlynch" title="Event Organizing">📋</a></td>
    <td align="center"><a href="https://github.com/david-garzon-adl"><img src="https://avatars0.githubusercontent.com/u/45822796?v=4?s=100" width="100px;" alt=""/><br /><sub><b>David Javier Garzon Carrillo</b></sub></a><br /><a href="https://github.com/idrimi (Jonathan Gerber)/contacts/commits?author=david-garzon-adl" title="Code">💻</a></td>
    <td align="center"><a href="https://github.com/NicolaasZA"><img src="https://avatars0.githubusercontent.com/NicolaasZA?v=4?s=100" width="100px;" alt=""/><br /><sub><b>NicolaasZA (FORK)</b></sub></a><br /><a href="https://github.com/NicolaasZA/contacts/commits?author=NicolaasZA" title="Code">💻</a></td>
  </tr>
</table>

<!-- markdownlint-enable -->
<!-- prettier-ignore-end -->

<!-- ALL-CONTRIBUTORS-LIST:END -->
<!-- prettier-ignore -->
<!-- ALL-CONTRIBUTORS-LIST:END -->
