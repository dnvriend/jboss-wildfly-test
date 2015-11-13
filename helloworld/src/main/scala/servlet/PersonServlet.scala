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

package servlet

import javax.annotation.Resource
import javax.servlet.annotation._
import javax.servlet.http._
import javax.sql.DataSource

import repository.{ JdbcConnection, PersonRepository }

@WebServlet(value = Array("/PersonServlet"))
class PersonServlet extends HttpServlet {
  @Resource(lookup = "java:jboss/datasources/PostgresDS")
  var ds: DataSource = null

  implicit def conn: JdbcConnection = new JdbcConnection {
    var dataSource = ds
  }

  override def doGet(request: HttpServletRequest, response: HttpServletResponse) {
    response.setContentType("text/html")
    response.setCharacterEncoding("UTF-8")

    val out = response.getWriter
    out.println("<h3>PersonServlet</h3>")

    PersonRepository.persons().map { xs ⇒
      if (xs.isEmpty) {
        out.println("No persons in the database")
      } else {
        xs.foreach(person ⇒ out.println(person + "<br/>"))
      }
    }.recover {
      case t: Throwable ⇒
        t.printStackTrace()
        out.println("Something bad has happened: " + t.getMessage)
    }

    val firstNameOption = Option(request.getParameter("firstName"))
    val lastNameOption = Option(request.getParameter("lastName"))
    (firstNameOption, lastNameOption) match {
      case (Some(firstName), Some(lastName)) ⇒
        PersonRepository.savePerson(firstName, lastName)
          .map { _ ⇒
            out.println(s"Person saved")
          }
          .recover {
            case t: Throwable ⇒
              t.printStackTrace()
              out.println("Something bad has happened: " + t.getMessage)
          }
      case _ ⇒
        out.println(
          """
            |<p>
            |Enter a user to save: <br/>
            |<form action='PersonServlet' method='post'>
            |First name: <input type='text' length='20' name='firstName'/><br/>
            |Last name: <input type='text' length='20' name='lastName'/><br/>
            |<input type='submit'/>
            |</form>
            |</p>
          """.stripMargin)

        out.close()
    }
  }

  override def doPost(req: HttpServletRequest, resp: HttpServletResponse): Unit =
    doGet(req, resp)
}
