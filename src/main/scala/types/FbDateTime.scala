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

package com.bloomlife.fbrules.types

object FbDateTime {
  /** Creates a `FbNode` that only accepts ISO 8601-formated dates with time
   *  (e.g. 1997-07-16T19:20:30.45+01:00).
   *
   *  @param hasTimeOffset if true, times can have a time offset other than Z
   *                       (UTC).
   *
   *  See [[https://www.w3.org/TR/NOTE-datetime]] for reference.
   */
  def apply(hasTimeOffset: Boolean = false): FbNode = {
    val date = "\\d{4}-[01]\\d-[0-3]\\d"
    val hourMin = "[0-2]\\d:[0-5]\\d"
    val secMs = ":[0-5]\\d(.\\d+)?"

    val timeOffet =
      if (hasTimeOffset) s"(Z|([+-]${hourMin}))"
      else "Z"

    val regex = s"/^${date}T${hourMin}(${secMs})?${timeOffet}$$/"

    FbString(regex=Some(regex))
  }
}
