import { WebPlugin, registerWebPlugin } from "@capacitor/core";
import { ContactsPlugin, PermissionStatus, Contact } from "./definitions";

export class ContactsPluginWeb extends WebPlugin implements ContactsPlugin {
  constructor() {
    super({
      name: "CapContacts",
      platforms: ["web"],
    });
  }

  async getPermissions(): Promise<PermissionStatus> {
    throw new Error("getPermission not available");
  }

  async getContacts(): Promise<{ contacts: Contact[] }> {
    throw new Error("getContacts not available");
  }

  async addContact(contact: any): Promise<{ action: string, success: boolean }> {
    throw new Error('addContacts not available');
  }

  async viewContact(contactId: any): Promise<{ action: string, success: boolean }> {
    throw new Error('addContacts not available');
  }
}

const Contacts = new ContactsPluginWeb();

export { Contacts };

registerWebPlugin(Contacts);
