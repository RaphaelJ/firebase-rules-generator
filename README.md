# firebase-rules-generator

Small utility that you can use to generate advanced Firebase rules.

It takes as input a declarative definition of your Firebase schema, and generates the Firebase rules that will enforce it.

Schemas are defined as plain-Scala scripts.

## Requirements

[Download and install](http://www.scala-sbt.org/download.html) the *sbt* open-source build tool.

## Example

Let's suppose that you want to enforce a schema for a basic chat app. The complete working example is located at `src/main/scala/examples/ChatApp.scala`.

You can generate the rules by running the script by running the following command:

```
 sbt "run-main com.bloomlife.fbrules.examples.ChatApp"
```

Our chat example app will be composed of three main object types: *users*, *rooms* and *messages*:

```
{
    "users": {
        "userid1": {
            "id": "userid1",
            "created_at": "2017-03-16T19:20:30.45Z",
            "full_name": "Raphael Javaux",
            "email": "raphael@bloomlife.com",
            "birth_date": "1991-03-06",
            "website": "https://bloomlife.com"
        },
        "userid2": {
            "id": "userid2",
            "created_at": "2017-01-14T09:09:33.15Z",
            "full_name": "Dave Null",
            "email": "dev@null.com",
            "sex": "male"
        }
    },
    "rooms": {
        "general": {
            "name": "general",
            "is_public": true,
            "members": { "raphaelj": true }
        },
        "cats": {
            "name": "cats",
            "is_public": false,
            "members": { "raphaelj": true, "devnull": true }
        }
    },
    "messages": {
        "general": {
            "0": {
                "created_at": "2017-03-16T19:20:30.45Z",
                "sender": "raphaelj",
                "text": "Hello!"
            }
        }
    }         
}
```

### Defining objects

Let's start by defining the schema for users. Object schemas are defined using `FbObject()`:

```
val user = FbObject(
    // Required fields
    "created_at"        := FbDateTime(),                    // An ISO 8601 date + time, without timezome
    "email"             := FbEmail(),                       // A valid email address
    "full_name"         := FbString(maxLength=Some(256)),   // A string that is 256 characters or less

    // Optional fields
    "birth_date"        ?= FbDate(),                        // An ISO 8601 date, without time
    "sex"               ?= FbEnum("male", "female"),        // Should be either "male" or "female"
    "website"           ?= FbURL()                          // A valid URL that starts with 'http' or 'https'
)
```

`FbObject`s accept a list of fields as input. Fields can be either required (`:=`) or optional (`?=`). The following fields are available (some fields have parameters):

| Field type    | Description                                | Examples        |
|---------------|--------------------------------------------|-----------------|
| `FbBoolean()` | Either the value `true` or `false`         | `true`, `false` |
| `FbDate()`    | [ISO 8601](https://en.wikipedia.org/wiki/ISO_8601) formated date | `"2017-12-25"`    |
| `FbDateTime(hasTimeOffset=false)` | [ISO 8601](https://en.wikipedia.org/wiki/ISO_8601) formated date with time. If `hasTimeOffset` is set to `true` (default is `false`), accepts a time offset different than UTC. | `"2017-12-25T01:12:32.35Z"`, `"2017-12-25T01:13:32.35+01:00"` (if `hasTimeOffset` is set to `true`). |
| `FbEmail()`   | Valid email addresses                      | `"raphael@bloomlife.com"` |
| `FbEnum(option1, option2, ...)` | Only accepts the listed values | `male` or `female` if defined as `FbEnum("male", "female")` |
| `FbHexColor()` | Hexadecimal RGB color                     | `"#ABB987"`, `"#ABC"` |
| `FbNode()`     | Any value                                 | `12`, `"hello world"`, `false` |
| `FbNumber(min=None, max=None)` | Any integral or floating point numbers. If `min` is not `None`, should be `>=` than this value. If `max` is not `None`, should be `<=` than this value. | `0`, `0.3145`, `1` if defined as `FbEnum(min=Some(0), max=Some(1))` |
| `FbString(minLength = None, maxLength = None, regex = None)` | A character string that is at least `minLength` characters long (if defined), at most `maxLength` characters long (if defined) and that matches the given [Javascript regular expression](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Guide/Regular_Expressions) (if defined). | Any string that ends with `ed` and that is at least 3 characters long if defined as `FbString(minLength=Some(3), regex=Some("/ed$/")`. |
| `FbURL()`     | URL that begin with `http://` or `https://`. | `"http://bloomlife.com"` |

### Object collections

Sometimes, one will want to have a collections of objects of the same schema, indentified by an unique ID. Use `FbCollection()` to define new collections. The following example define the `users`, `rooms` and `messages` collections of our example:

```
val users = FbCollection(userId => user)
```

*Note: empty collections in Firebase are automatically removed by the database. If you want to allow an object to contain an empty collection, define the collection as an optional field (i.e. using `?=`).*

### Add constraints to nodes

You can add access and validation constraints to any schema node (objects, fields or collections) using the `readIf()`, `writeIf()`, `accessIf()` and `validateIf()` methods. These matches [the `.read`, `.write` and `.validate` constraints of Firebase rules](https://firebase.google.com/docs/reference/security/database/) and have access to the [Firebase Rules variables](https://firebase.google.com/docs/reference/security/database/#variables).

In the following example, we are going to redefine the `users` collection we defined earlier with new constraints:

```
val users = FbCollection(
        userId =>
            user.
                // Checks that the username in the user object matches the username in the collection.
                validateIf((NewDate / "userId").asString === userId).

                // Only allows users to modify their own account.
                writeIf(username === Auth.uid)
    )
```

| Method         | Description                                                |
|----------------|------------------------------------------------------------|
| `readIf()`     | Allows the object to be read if the condition is evaluated to `true` |
| `writeIf()`    | Allows the object to be created/modified if the condition is evaluated to `true` |
| `accessIf()`   | Combines `readIf()` and `writeIf()` |
| `validateIf()` | Allows the object to be created/modified if the condition is evaluated to `true` |
