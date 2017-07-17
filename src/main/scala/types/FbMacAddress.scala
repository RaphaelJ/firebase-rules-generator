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

object FbMacAddress {
  /** Creates a `FbNode` that only accepts 48-bits MAC addresses
    * (e.g. F2:23:12:b3:E4:56).
    */
  def apply(): FbNode = {
    val hexByte = "[A-Fa-f0-9]{2}"
    FbString(regex=Some(s"/^(${hexByte}:){5}${hexByte}$$/"))
  }
}
