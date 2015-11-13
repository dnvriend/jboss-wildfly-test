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

import java.sql._
import java.text.SimpleDateFormat
import java.util.UUID
import javax.sql.DataSource
import scala.util.{ Failure, Success, Try }
import scala.concurrent._

object JdbcConnection {
  type Seq[+A] = scala.collection.immutable.Seq[A]
  val Seq = scala.collection.immutable.Seq
  type List[+A] = scala.collection.immutable.List[A]
  val List = scala.collection.immutable.List
  type Vector[+A] = scala.collection.immutable.Vector[A]
  val Vector = scala.collection.immutable.Vector

  implicit class RowData(rs: ResultSet) {
    def apply(columnNumber: Int): Any = rs.getObject(columnNumber)
    def apply(columnName: String): Any = rs.getObject(columnName)
    def toIterator[E](rowMapper: ResultSet ⇒ E): Iterator[E] = new Iterator[E] {
      override def hasNext: Boolean = rs.next()
      override def next(): E = rowMapper(rs)
    }.toIterator
  }

  implicit class DateFormatter(date: java.sql.Date) {
    def print: String = new SimpleDateFormat("yyyy-MM-dd").format(date)
  }

  implicit class TimeFormatter(time: java.sql.Time) {
    def print: String = new SimpleDateFormat("HH:mm:ss.SSS").format(time)
  }

  implicit class TimestampFormatter(timestamp: java.sql.Timestamp) {
    def print: String = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").format(timestamp)
  }

  implicit class NullOption(v: Try[String]) {
    def toOpt: Option[String] = v.toOption.find(_ != "null")
  }

  implicit class PimpedRowData(row: ResultSet) {
    def str(name: String): String = Option(row(name).asInstanceOf[String]).map(_.trim).orNull
    def str(index: Int): String = Option(row(index).asInstanceOf[String]).map(_.trim).orNull
    def strOpt(name: String): Option[String] = Option(str(name))
    def strOpt(index: Int): Option[String] = Option(str(index))
    def int(name: String): Int = row(name).asInstanceOf[Int]
    def int(index: Int): Int = row(index).asInstanceOf[Int]
    def intOpt(name: String): Option[Int] = Try(int(name)).toOption
    def intOpt(index: Int): Option[Int] = Try(int(index)).toOption
    def long(name: String): Long = row(name).asInstanceOf[Long]
    def long(index: Int): Long = row(index).asInstanceOf[Long]
    def longOpt(name: String): Option[Long] = Try(long(name)).toOption
    def longOpt(index: Int): Option[Long] = Try(long(index)).toOption
    def float(name: String): Float = row(name).asInstanceOf[Float]
    def float(index: Int): Float = row(index).asInstanceOf[Float]
    def floatOpt(name: String): Option[Float] = Try(float(name)).toOption
    def floatOpt(index: Int): Option[Float] = Try(float(index)).toOption
    def double(name: String): Double = row(name).asInstanceOf[Double]
    def double(index: Int): Double = row(index).asInstanceOf[Double]
    def doubleOpt(index: Int): Option[Double] = Try(double(index)).toOption
    def doubleOpt(name: String): Option[Double] = Try(double(name)).toOption
    def bi(name: String): BigInt = row(name).asInstanceOf[BigInt]
    def bi(index: Int): BigInt = row(index).asInstanceOf[BigInt]
    def biOpt(name: String): Option[BigInt] = Try(bi(name)).toOption
    def biOpt(index: Int): Option[BigInt] = Try(bi(index)).toOption
    def bd(name: String): BigDecimal = BigDecimal(row(name).asInstanceOf[java.math.BigDecimal])
    def bd(index: Int): BigDecimal = BigDecimal(row(index).asInstanceOf[java.math.BigDecimal])
    def bdOpt(name: String): Option[BigDecimal] = Option(bd(name))
    def bdOpt(index: Int): Option[BigDecimal] = Option(bd(index))
    def date(name: String): Date = row(name).asInstanceOf[Date]
    def date(index: Int): Date = row(index).asInstanceOf[Date]
    def dateOpt(name: String): Option[Date] = Option(date(name))
    def dateOpt(index: Int): Option[Date] = Option(date(index))
    def dateStr(name: String): String = date(name).print
    def dateStr(index: Int): String = date(index).print
    def dateStrOpt(name: String): Option[String] = Option(date(name)).map(_.print)
    def dateStrOpt(index: Int): Option[String] = Option(date(index)).map(_.print)
    def dateTime(name: String): Timestamp = row(name).asInstanceOf[Timestamp]
    def dateTime(index: Int): Timestamp = row(index).asInstanceOf[Timestamp]
    def dateTimeStr(name: String): String = dateTime(name).print
    def dateTimeStr(index: Int): String = dateTime(index).print
    def dateTimeStrOpt(name: String): Option[String] = Option(dateTime(name)).map(_.print)
    def dateTimeStrOpt(index: Int): Option[String] = Option(dateTime(index)).map(_.print)
    def bool(name: String): Boolean = row(name).asInstanceOf[Boolean]
    def bool(index: Int): Boolean = row(index).asInstanceOf[Boolean]
    def boolOpt(name: String): Option[Boolean] = Try(bool(name)).toOption
    def boolOpt(index: Int): Option[Boolean] = Try(bool(index)).toOption
    def uuid(name: String): UUID = row(name).asInstanceOf[UUID]
    def uuid(index: Int): UUID = row(index).asInstanceOf[UUID]
    def uuidOpt(name: String): Option[UUID] = Try(uuid(name)).toOption
    def uuidOpt(index: Int): Option[UUID] = Try(uuid(index)).toOption
    def uuidStr(name: String): String = uuid(name).toString
    def uuidStr(index: Int): String = uuid(index).toString
  }

  //
  // SqlInterpolation
  //

  case class SqlAndArgs(sql: String, args: Seq[Any]) {
    def +(that: SqlAndArgs): SqlAndArgs = {
      SqlAndArgs(sql + " " + that.sql, args ++ that.args)
    }

    def stripMargin: SqlAndArgs = SqlAndArgs(sql.stripMargin, args)
  }

  // '$arg does string interpolation rather than argument
  implicit class SqlInterpolationHelper(val sc: StringContext) {
    def q(args: Any*): SqlAndArgs = {

      var actualArgs: List[Any] = List()
      val parts = sc.parts.iterator.toList
      val inserts = args.iterator.toList

      val pi = parts.zip(inserts)
      val sql = pi.foldLeft("")((a: String, b: (String, Any)) ⇒ {
        if (b._1.endsWith("'")) {
          a + b._1.dropRight(1) + b._2
        } else {
          if (b._2.isInstanceOf[List[Any]]) {
            val list = b._2.asInstanceOf[List[Any]]
            actualArgs = list.reverse ++ actualArgs
            a + b._1 + ("?," * list.length).dropRight(1)
          } else {
            actualArgs = b._2 :: actualArgs
            a + b._1 + "?"
          }
        }
      })
      val extra = if (pi.length < parts.length) parts.reverse.head.toString else ""

      SqlAndArgs(sql + extra, actualArgs.reverse)
    }
  }
}

trait JdbcConnection {
  import JdbcConnection._
  def dataSource: DataSource

  def withConnection[R](f: Connection ⇒ R): Try[R] = blocking {
    import resource._
    managed[Connection](dataSource.getConnection)
      .map { (conn: Connection) ⇒ f(conn) }
      .either match {
        case Left(cause)       ⇒ Failure(cause.head)
        case Right(connection) ⇒ Success(connection)
      }
  }

  def withStatement[R](f: Statement ⇒ R): Try[R] =
    withConnection[R] { (conn: Connection) ⇒ f(conn.createStatement) }

  def withPreparedStatement[R](query: String)(f: PreparedStatement ⇒ R): Try[R] =
    withConnection[R] { (conn: Connection) ⇒ f(conn.prepareStatement(query)) }

  def withPreparedResultSet[R](query: String, values: Seq[Any])(implicit f: ResultSet ⇒ R): Try[R] =
    withPreparedStatement[R](query) { (preparedStatement: PreparedStatement) ⇒
      f(withValueInsertion(values, preparedStatement).executeQuery())
    }

  def withResultSet[R](query: String)(f: ResultSet ⇒ R): Try[R] =
    withStatement[R] { (statement: Statement) ⇒ f(statement.executeQuery(query)) }

  def queryForList[E](query: String)(implicit rowMapper: ResultSet ⇒ E): Try[Seq[E]] =
    withResultSet(query)(_.toIterator(rowMapper).toList)

  /**
   * Returns a Vector of elements
   * @param interpolation
   * @tparam A
   * @return
   */
  def queryForList[A](interpolation: SqlAndArgs)(implicit rowMapper: ResultSet ⇒ A): Try[Seq[A]] =
    queryForList(interpolation.sql, interpolation.args)

  def queryForList[E](query: String, values: Seq[Any])(implicit rowMapper: ResultSet ⇒ E): Try[Seq[E]] =
    withPreparedResultSet(query, values)(_.toIterator(rowMapper).toList)

  def queryForObject[E](query: String)(implicit rowMapper: ResultSet ⇒ E): Try[E] =
    withResultSet(query) { rs ⇒
      rs.next()
      rowMapper(rs)
    }

  /**
   * Returns an Option of an element
   * @param interpolation
   * @param rowMapper
   * @tparam A
   * @return
   */
  def queryForObject[A](interpolation: SqlAndArgs)(implicit rowMapper: ResultSet ⇒ A): Try[A] =
    queryForObject(interpolation.sql, interpolation.args)

  def queryForObject[E](query: String, values: Seq[Any])(implicit rowMapper: ResultSet ⇒ E): Try[E] =
    withPreparedResultSet(query, values) { rs ⇒
      rs.next()
      rowMapper(rs)
    }

  def mapSingle[A](interpolation: SqlAndArgs)(implicit rowMapper: ResultSet ⇒ A): Try[Option[A]] =
    queryForList(
      if (interpolation.sql.contains("LIMIT 1"))
        interpolation else interpolation.copy(sql = interpolation.sql + " LIMIT 1")
    )(rowMapper).map(_.headOption)

  def mapQuery[A](interpolation: SqlAndArgs)(implicit rowMapper: ResultSet ⇒ A): Try[Seq[A]] =
    queryForList(interpolation)

  def executeQueryId[A](interpolation: SqlAndArgs)(implicit rowMapper: ResultSet ⇒ A): Try[A] =
    executeQuery(interpolation.sql, interpolation.args).map { rs ⇒
      rs.next()
      rowMapper(rs)
    }

  private def withValueInsertion(values: Seq[Any], preparedStatement: PreparedStatement): PreparedStatement = {
    values.zipWithIndex.map(t ⇒ (t._1, t._2 + 1)).foreach {
      case (int: Int, index)             ⇒ preparedStatement.setInt(index, int)
      case (long: Long, index)           ⇒ preparedStatement.setLong(index, long)
      case (double: Double, index)       ⇒ preparedStatement.setDouble(index, double)
      case (boolean: Boolean, index)     ⇒ preparedStatement.setBoolean(index, boolean)
      case (float: Float, index)         ⇒ preparedStatement.setFloat(index, float)
      case (byte: Byte, index)           ⇒ preparedStatement.setByte(index, byte)
      case (short: Short, index)         ⇒ preparedStatement.setShort(index, short)
      case (timestamp: Timestamp, index) ⇒ preparedStatement.setTimestamp(index, timestamp)
      case (date: Date, index)           ⇒ preparedStatement.setDate(index, date)
      case (date: java.util.Date, index) ⇒ preparedStatement.setDate(index, new Date(date.getTime))
      case (any: Any, index)             ⇒ preparedStatement.setString(index, any.toString)
      case (null, index)                 ⇒ preparedStatement.setNull(index, Types.NULL)
    }
    preparedStatement
  }

  /**
   * Executes the SQL query in this PreparedStatement object and returns the ResultSet object generated by the query.
   * This is used generally for reading the content of the database. The output will be in the form of ResultSet.
   * Generally SELECT statement is used.
   * @param interpolation
   * @return
   */
  def executeQuery(interpolation: SqlAndArgs): Try[ResultSet] =
    executeQuery(interpolation.sql, interpolation.args)

  /**
   * Executes the SQL query in this PreparedStatement object and returns the ResultSet object generated by the query.
   * This is used generally for reading the content of the database. The output will be in the form of ResultSet.
   * Generally SELECT statement is used.
   * @param query
   * @return
   */
  def executeQuery(query: String): Try[ResultSet] =
    withStatement[ResultSet] { (statement: Statement) ⇒
      statement.executeQuery(query)
    }

  /**
   * Executes the SQL query in this PreparedStatement object and returns the ResultSet object generated by the query.
   * This is used generally for reading the content of the database. The output will be in the form of ResultSet.
   * Generally SELECT statement is used.
   * @param query
   * @param values
   * @return
   */
  def executeQuery(query: String, values: Seq[Any]): Try[ResultSet] =
    withPreparedStatement[ResultSet](query) { preparedStatement ⇒
      withValueInsertion(values, preparedStatement).executeQuery()
    }

  /**
   * Executes the SQL statement in this PreparedStatement object, which must be an SQL INSERT, UPDATE or DELETE statement;
   * or an SQL statement that returns nothing, such as a DDL statement. This is generally used for altering the databases.
   * Generally DROP TABLE or DATABASE, INSERT into TABLE, UPDATE TABLE, DELETE from TABLE statements will be used in this.
   * The output will be in the form of int. This int value denotes the number of rows affected by the query.
   * @param query
   * @return
   */
  def executeUpdate(query: String): Try[Int] =
    withStatement[Int] { (statement: Statement) ⇒
      statement.executeUpdate(query)
    }

  /**
   * Executes the SQL statement in this PreparedStatement object, which must be an SQL INSERT, UPDATE or DELETE statement;
   * or an SQL statement that returns nothing, such as a DDL statement. This is generally used for altering the databases.
   * Generally DROP TABLE or DATABASE, INSERT into TABLE, UPDATE TABLE, DELETE from TABLE statements will be used in this.
   * The output will be in the form of int. This int value denotes the number of rows affected by the query.
   * @param interpolation
   * @return
   */
  def executeUpdate(interpolation: SqlAndArgs): Try[Int] =
    executeUpdate(interpolation.sql, interpolation.args)

  /**
   * Executes the SQL statement in this PreparedStatement object, which must be an SQL INSERT, UPDATE or DELETE statement;
   * or an SQL statement that returns nothing, such as a DDL statement. This is generally used for altering the databases.
   * Generally DROP TABLE or DATABASE, INSERT into TABLE, UPDATE TABLE, DELETE from TABLE statements will be used in this.
   * The output will be in the form of int. This int value denotes the number of rows affected by the query.
   * @param query
   * @param values
   * @return
   */
  def executeUpdate(query: String, values: Seq[Any]): Try[Int] =
    withPreparedStatement[Int](query) { (preparedStatement: PreparedStatement) ⇒
      withValueInsertion(values, preparedStatement).executeUpdate()
    }

  /**
   * Executes the SQL statement in this PreparedStatement object, which may be any kind of SQL statement,
   * This will return a boolean. TRUE indicates the result is a ResultSet and FALSE indicates
   * it has the int value which denotes number of rows affected by the query. It can be used for executing stored procedures.
   * @param interpolation
   * @return
   */
  def execute(interpolation: SqlAndArgs): Try[Boolean] =
    execute(interpolation.sql, interpolation.args)

  /**
   * Executes the SQL statement in this PreparedStatement object, which may be any kind of SQL statement,
   * This will return a boolean. TRUE indicates the result is a ResultSet and FALSE indicates
   * it has the int value which denotes number of rows affected by the query. It can be used for executing stored procedures.
   * @param query
   * @param values
   * @return
   */
  def execute(query: String, values: Seq[Any]): Try[Boolean] =
    withPreparedStatement[Boolean](query) { preparedStatement ⇒
      withValueInsertion(values, preparedStatement).execute()
    }

  /**
   * Executes the SQL statement in this PreparedStatement object, which may be any kind of SQL statement,
   * This will return a boolean. TRUE indicates the result is a ResultSet and FALSE indicates
   * it has the int value which denotes number of rows affected by the query. It can be used for executing stored procedures.
   * @param query
   * @return
   */
  def execute(query: String): Try[Boolean] =
    withStatement[Boolean] { statement ⇒
      statement.execute(query)
    }
}
