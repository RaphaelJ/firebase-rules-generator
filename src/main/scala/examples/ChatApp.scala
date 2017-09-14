// Firebase Rules Generator
// Bloom Technologies Inc. Copyright 2017
//
// Authors: Raphael Javaux <raphael@bloomlife.com>
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.

// Generates the Firebase rules for a very simple chat application. Here is an
// example of data that will be accepted as valid by the schema defined here
// bellow.
//
// {
//     "users": {
//         "userid1": {
//             "created_at": "2017-03-16T19:20:30.45Z",
//             "full_name": "Raphael Javaux",
//             "email": "raphael@bloomlife.com",
//             "birth_date": "1991-03-06",
//             "website": "https://bloomlife.com"
//         },
//         "userid2": {
//             "created_at": "2017-01-14T09:09:33.15Z",
//             "full_name": "Dave Null",
//             "email": "dev@null.com",
//             "sex": "male"
//         }
//     },
//     "rooms": {
//         "general": {
//             "is_public": true,
//             "members": { "userid1": true }
//         },
//         "cats": {
//             "is_public": false,
//             "members": { "userid1": true, "userid2": true }
//         }
//     },
//     "messages": {
//         "general": {
//             "0": {
//                 "created_at": "2017-03-16T19:20:30.45Z",
//                 "sender": "userid1",
//                 "text": "Hello!"
//             }
//         }
//     }
// }

package com.bloomlife.fbrules.examples

import play.api.libs.json._

// Imports the required classes and definitions
import com.bloomlife.fbrules._
import com.bloomlife.fbrules.ruleexpr._
import com.bloomlife.fbrules.ruleexpr.Implicits._
import com.bloomlife.fbrules.types._
import com.bloomlife.fbrules.types.Implicits._

object ChatApp {
  def main(args: Array[String]) = {
    //
    // Object definitions
    //

    val user = FbObject(
        // Required fields
        "created_at"        := FbDateTime(),                    // An ISO 8601 date + time, without timezome
        "email"             := FbEmail(),                       // A valid email address
        "full_name"         := FbString(maxLength=Some(256)),   // A string that is 256 characters or less

        // Optional fields
        "birth_date"        ?= FbOr(FbDate(), FbDateTime()),    // An ISO 8601 date, with or without time
        "sex"               ?= FbEnum("male", "female"),        // Should be either "male" or "female"
        "website"           ?= FbURL()                          // A valid URL that starts with 'http' or 'https'
      )

    val room = FbObject(
        "is_public"         := FbBoolean(),

        // The collections of the members that are in this room. Notice that the
        // collection is a required field, as we would like the collection to
        // contain at least one user (empty nodes in Firebase are implicitely
        // removed).
        "members"           := FbCollection(userId =>
          FbBoolean().
            // Only allows users to add themselves to the collection.
            writeIf(userId === Auth.uid)

            // Ensures that the associated user object exists.
            validateIf((Root / "users" / userId).exists)
        )
      )

    val message = FbObject(
        "created_at"        := FbDateTime(),                    // An ISO 8601 date + time, without timezome

        "sender"            :=
          FbString().
            // Checks that sender is the current user
            validateIf(NewData.asString === Auth.uid).

            // Checks that the sender exists in `/users/{sender}`
            validateIf({
              val senderUserId = NewData.asString
              (Root / "users" / senderUserId).exists
            }),

        "text"              :=
          // Requires the text to be at least one character long, and does not
          // allow the field to contain the word `shit` or `nazi`.
          FbString(minLength=Some(1)).
            validateIf({
              val textValue = NewData.asString
              !(textValue.contains("shit") || textValue.contains("nazi"))
            })
      )

    //
    // Root node definition
    //

    val root = FbObject(
        // Notice that all the collections are optional, as empty collections
        // will be automatically removed by Firebase.

        "users"     ?= FbCollection(userId =>
          user.
            // Only allows users to modify their own account.
            writeIf(userId === Auth.uid)
        ),

        "rooms"     ?= FbCollection(roomName =>
          room.
            // Only logged-in users can create rooms.
            writeIf(Auth.isLoggedIn).

            // Only public rooms are visible to non-member users.
            readIf(
                 (NewData / "is_public").asBoolean
              || (NewData / "members" / Auth.uid).exists
            )
        ),

        "messages"  ?= FbCollection(roomName =>
          FbCollection(msgId => message).
            // Only allows messages in a room the user is a member of.
            accessIf(
              (Root / "rooms" / roomName / "members" / Auth.uid).exists
            )
        )
      )

    // Generates and prints the JSON object containing the rules.
    val rules = Rules.generate(root)
    println(Json.prettyPrint(rules))
  }
}
