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

  async addContact(): Promise<string> {
    throw new Error('addContacts not available');
  }
}

const Contacts = new ContactsPluginWeb();

export { Contacts };

registerWebPlugin(Contacts);
