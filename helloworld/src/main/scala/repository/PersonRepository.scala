/*
 * Copyright 2015 Dennis Vriend
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package repository

import java.sql.ResultSet

import scala.util.Try

object PersonRepository {
  import JdbcConnection._

  // case classes and (un)marshaller(s) could be put in separate objects/traits
  case class Person(id: String, firstName: String, lastName: String, created: Option[String] = None)

  implicit def rowToPerson(row: ResultSet): Person =
    Person(row.uuidStr("ID"), row.str("FIRST_NAME"), row.str("LAST_NAME"), row.dateTimeStrOpt("CREATED"))

  implicit def rowToId(row: ResultSet): String = row.uuidStr("ID")

  def savePerson(firstName: String, lastName: String)(implicit conn: JdbcConnection): Try[Int] =
    conn.executeUpdate(q"INSERT INTO PERSONS (FIRST_NAME, LAST_NAME) VALUES ($firstName, $lastName)")

  def persons(limit: Int = Int.MaxValue, offset: Int = 0)(implicit conn: JdbcConnection): Try[Seq[Person]] =
    conn.mapQuery(q"SELECT * FROM PERSONS ORDER BY CREATED DESC LIMIT $limit OFFSET $offset")

  def person(id: String)(implicit conn: JdbcConnection): Try[Option[Person]] =
    conn.mapSingle(q"SELECT * FROM PERSONS WHERE ID = $id")

  def clear()(implicit conn: JdbcConnection): Try[Int] =
    conn.executeUpdate("TRUNCATE PERSONS")
}
