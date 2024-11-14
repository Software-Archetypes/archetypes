# Party archetype pattern

## Problem statement

### Level 1
A large portion of software created worldwide is intended for various types of end users: individual users, employees, other applications, and systems. Therefore, there’s a good chance you've faced the challenge of building a user management system at some point. Perhaps you had to prepare a module that enables user registration, updating their information (such as first name, last name, address), authentication (including setting passwords or one-time access codes), and authorization (including defining roles).

### Level 2
If the software is used to automate processes that generate revenue, the user often represents our client. Clients can be both individuals and companies. Sometimes, the client is a sole trader, combining characteristics of both an individual and a business. In some services, user registration is synonymous with client registration, though this doesn’t have to be the rule. The following scenarios, among others, are possible:
- A user registers in the system but, since they haven’t purchased a subscription (and are not technically a client), has limited access to services — in this situation, the user entity is created before (if it ever happens) an associated client entity is created.
- A client appears in the system, with whom we sign a service agreement. Only after the contract is signed might it be necessary to create one or multiple user accounts—in this situation, the user entity is created afterward.

### Level 3
The problem can become even more complex when:
- a person or company is neither a user nor a client, but, for example, a lead with whom we are conducting negotiations
- a company consists of multiple departments, and we want to enable different departments to perform different operations within the system (for instance, we may have a company hierarchy in which each subsidiary can make purchases, but billing is handled solely with the parent company as the payer)
- a company that is our client can also simultaneously be our supplier/partner (e.g. a fiber optic manufacturing company orders subscription contracts with internet and phone services for its employees while also providing services as a cable supplier)
- a company with which we have signed a contract will integrate with our system via an API (i.e. there will be no user logging into our system through the UI)

### What's next?
The above demonstrates that managing users, clients, partners, and various types of entities can be a very complex issue, where entities can assume multiple roles and have different relationships with each other.

## Possible solutions

### Let's start with a `User`
The evolutionary emergence of new business cases often results in an evolutionary solution design. We often see a solution that starts with a `User` class that manages basic user data as a person, as well as authentication and role management:


```java
class User {
    private final UserId id;
    private final FirstName firstName;
    private final LastName lastName;
    private final EmailAddress emailAddress;
    private final PasswordHash passwordHash;
    private Addresses addresses;
    private Roles roles;
    
    User(UserId id, FirstName firstName, LastName lastName, EmailAddress emailAddress, Password password) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.emailAddress = emailAddress;
        this.passwordHash = password.hash();
        this.roles = Roles.emptySet();
        this.addresses = Addresses.emptySet();
    }
    
    boolean authenticate(Password password) {
        return password.hash().matches(passwordHash);
    }
    
    void assign(Role role) {
        roles.add(role);
    }
    
    void assignOrUpdate(Address address) {
        addresses.add(address);
    }
    
    void remove(AddressId addressId) {
        addresses.remove(addressId);
    }
    
    //getters if needed
    //...
}
```

### When a `User` can be a `Customer`
When a user can be a customer, a common solution is to add a boolean flag:

```java
boolean customer;
```

In cases where clients are individuals (individual customers), this might be sufficient. However, if the customers are companies, issues arise, such as the fact that a company doesn’t have a first and last name but rather a business name and tax identification number, and often has various types of addresses (e.g. contact, billing). Attempting to meet these requirements within the `User` class would result in adding another set of data fields and address policies that would only be used in a subset of cases:

```java
class User {
    private final UserId id;
    private final FirstName firstName;
    private final LastName lastName;
    private final CompanyName companyName;
    private final TaxNumber taxNumber;
    private final EmailAddress emailAddress;
    private final PasswordHash passwordHash;
    private Addresses addresses;
    private Set<Role> roles;

    //user-specific API
    User(UserId id, FirstName firstName, LastName lastName, EmailAddress emailAddress, Password password) {
        this.id = id;
        //user data
        this.firstName = firstName;
        this.lastName = lastName;
        this.emailAddress = emailAddress;
        this.passwordHash = password.hash();
        this.roles = Roles.emptySet();
        this.addresses = Addresses.emptySet();
        //customer data
        this.companyName = null;
        this.taxNumber = null;
    }

    boolean authenticate(Password password) {
        password.hash().matches(passwordHash);
    }

    void assign(Role role) {
        roles.add(role);
    }
    
    void assignOrUpdate(Address address) {
        addresses.add(address, AddressType.RESIDENTIAL);
    }

    boolean isUser() {
        firstName != null || lastName != null;
    }

    //customer-specific API
    User(UserId id, CompanyName companyName, TaxNumber taxNumber, EmailAddress emailAddress) {
        this.id = id;
        //user data
        this.firstName = null;
        this.lastName = null;
        this.emailAddress = emailAddress;
        this.passwordHash = PasswordHash.empty();
        this.roles = Roles.emptySet();
        this.addresses = Addresses.emptySet();
        //customer data
        this.companyName = companyName;
        this.taxNumber = taxNumber;
    }
    
    void assignOrUpdate(Address address, AddressType addressType) {
        addresses.add(address, addressType);
    }

    boolean isCompany() {
        companyName != null || taxNumber != null;
    }
    
    //common API
    void remove(AddressId addressId) {
        addresses.remove(addressId);
    }

    //getters if needed
    //...
}
```

It’s evident that this model diverges from reality, where the creation of a user and a customer are often separated in time. Such a solution typically has a negative impact on the production chain, as independent streams of changes converge in a single class, increasing its complexity and reducing reusability and testability. On the client-side code, this model requires continuous checks to determine whether the object represents a user, a customer, or possibly both.

Attempting to handle additional cases (such as introducing the concept of a lead) within this model will only worsen the situation. Once again, the user and customer models often bloat due to the need to add various flags and marker fields, such as `Instant agreementSignatureDate`, `Instant validTo`, `boolean active`, etc. A critical requirement that challenges this model is the need to establish a one-to-many relationship between the customer and user.

### What about separate models?
A solution that addresses the mentioned problems, that we often observe in such situations is the creation of use case-specific classes, namely `User`, `Customer`, and `Lead`. We now reach a point where each specific case requires the creation of a new specialized model. On one hand, this is beneficial because the model better reflects the forces at play in the business problem. However, when a company or individual appears in multiple forms, the solution becomes complicated again.

For example, a sole proprietorship may start as a lead, then become a client, and we set up a user account for it; afterward, the same business entity becomes our partner. As a result, we would need to duplicate the same data in the `User`, `Customer`, `Lead`, and `Partner` classes, or create classes as Cartesian products of the functionalities and data structures of each type of entity, such as `CustomerPartner`. In this case, solving one problem generates another.

As a result, such solutions are semantically incorrect, meaning that the model does not reflect the actual essence of the business problem, thus complicating programming on both the model and client code sides. The model is characterized by high complexity and poor functional scalability. Typically, in such situations, we observe the use of separate models by different teams, which effectively hinders data consistency, and very similar work must be done in many places by many people.

## Solution

The essence of the problem lies in the role-centric model. Both the attempt to enrich the `User` type with data and functions specific to a given role, as well as the creation of types corresponding to roles, such as `Lead`, `Customer` or `Partner`, cause many problems. What if the center of the model is not a role, but an entity such as a person or an organization?

Then we get a structure similar to this one:

![Party](diagrams/party-basic-model.puml)

Now we can enrich it with all kinds of data necessary in our business, such as address or authentication data as well as behavior - like authentication strategies:

![Party](diagrams/party-basic-model.puml)

In this way, we can model any entity of any type, dynamically change its roles, and supplement it with policies and behaviors specific to the use case.

This is the archetypal **Party** model.

The *Party* archetype is a reusable business model used to represent different types of entities (individuals, companies, institutions) that play various roles, such as customer, supplier, user, partner, etc. *Party* allows for simplifying data management and relationships between these entities, avoiding issues of data duplication, logic redundancy within the model, providing high level of flexibility and reusability.

## Features of the Party Archetype

1. **Universal Representation of Entities** - the Party archetype acts as a general, overarching representation of any type of entity in the system, regardless of whether it’s an individual, a company, or another type of organization. In this way, a single Party model can handle data related to both individuals (e.g., first name, last name, residential address) and companies (business name, tax identification number, headquarters addresses).

2. *Multiple Roles* - Each Party instance can take on various roles, such as Client, Supplier, Partner, or User. These roles can be assigned dynamically, allowing a single Party instance to fulfill different functions depending on the business context. Party thus eliminates the need for separate classes for different entity types and allows more flexible role management.

3. **Unified Data Model and Database** - Using Party facilitates storing information in a single, central data structure, helping to maintain consistency and avoid redundancy issues. This way, various applications and systems can refer to the same Party instance, simplifying synchronization and data maintenance.

4. **Avoiding Data Duplication** - Party reduces the need for duplicating data, such as addresses, phone numbers, or other shared attributes. Instead of recreating these fields in various classes, Party stores common data that is accessible regardless of the roles played by a particular instance.

5. **Scalability and Flexibility** - The Party model is easy to expand, as new roles and relationships can be added without needing to modify the core structure. Party is well-suited to scale with growing organizational needs, as it allows adding new functionalities and behaviors with minimal disruption to the existing model.

## How it works?

### Party

TBD

### PartyId

`PartyId` might look trivial, but it is critical to understand the decision behind using an identifier that is unique throughout all supported party types.
Companies tend to use semantic keys as identifiers. For example:
- tax number or personal identification number (in Poland we call it PESEL) - although it is a unique identifier, it limits the flexibility of the model, because  a company has no personal identification number and person is not obliged to know and use its tax number. It means you can use the identifier only in certain subset of your business
- phone number - the problem with phone numbers is that their pool is limited, and numbers are reused. When your contract with telecommunication service provider expires, your number can be sold to some other person or a company. It means you cannot rely on phone numbers long term. 
- email address - not every business requires email to be verified, especially when you call the sales department to get a new insurance offer. The offer might be generated for your name, but sent to your wife's email address.

To avoid confusion, and ensure flexibility and functional scalability of the model, we should use a unique identifier. In terms of implementation  `PartyId` is a record.

`PartyId` is an identifier of every party regardless its type. There is no such thing as `OrganizationId` or `PersonId`, because most things related to the `Party` archetype are type-agnostic (relationships, roles). Also, it is often the case that a party is sole trader, meaning it is both person and organization.

#### How do I generate `PartyId`?

`PartyId` like most identifiers can be generated either by the app or by the database engine. We recommend choosing the former as you have more control over it. The control include the possibility to unit-test it, or change its semantics.

### RegisteredIdentifier

Beyond the need for unique identification of parties, which we addressed with `PartyId`, there is often a requirement to identify parties using semantic values such as tax numbers, personal identification numbers, passport numbers, or identity card numbers. As noted previously in the section on `PartyId`, these identifiers may not be unique and may not apply to all types of parties. However, addressing this need allows us to enrich the `Party` model with additional types of identifiers and to implement policies or rules that govern their use. Each party can have zero or more associated registered identifiers.

### Roles

A *role archetype* is intended to represent both the general role a given party might have, regardless of the specific use case, and the role it may play in its relationships with other parties. Here, we focus on the former.

A *role* is a simple value object containing the name (or type) of the role. Roles can be added or removed in an idempotent manner. A specific role may be represented either by an instance of the `Role` type with a specific name (e.g., `"Customer"`) or through inheritance by creating subtypes. Each party can have zero or more associated roles.

Defining roles for a given party can be further refined with policies or rules that restrict the assignment of certain roles (e.g., roles specific to organizations cannot be assigned to individuals).

### Addresses

TBD

### Authentication

TBD

### Further improvements

Everything you've seen so far is only a glimpse of what the Party archetype can offer. Soon, we'll explore more about inter-party relationships, capabilities, and assets.