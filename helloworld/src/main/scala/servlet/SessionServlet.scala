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

import java.text.SimpleDateFormat
import java.util.Date
import javax.servlet.annotation._
import javax.servlet.http._

@WebServlet(value = Array("/SessionServlet"))
class SessionServlet extends HttpServlet {

  def formatTime(timestamp: Long): String = {
    val sdf = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSS")
    sdf.format(new Date(timestamp))
  }

  override def doGet(request: HttpServletRequest, response: HttpServletResponse) {
    response.setContentType("text/html")
    response.setCharacterEncoding("UTF-8")

    val out = response.getWriter
    out.println("<h3>Session Test Example</h3>")

    val session = request.getSession(true)
    out.println(
      s"""
        |Session Id: ${session.getId} <br/>
        |Created: ${formatTime(session.getCreationTime)} <br/>
        |Last Accessed: ${formatTime(session.getLastAccessedTime)} <br/>
      """.stripMargin)

    Option(request.getParameter("dataname")).foreach { dataName ⇒
      Option(request.getParameter("datavalue")).foreach { dataValue ⇒
        session.setAttribute(dataName, dataValue);
      }
    }

    import scala.collection.JavaConversions._
    val xs = session.getAttributeNames
    val sessionDataString = xs.map(name ⇒ s"$name = ${session.getAttribute(name)}").mkString("<br/>")
    out.println(
      s"""
        |<p>
        |The following data is in your session: <br/><br/>
        |$sessionDataString
        |</p>
        |
        |<p>
        |POST based form <br/>
        |<form action='${response.encodeURL("SessionServlet")}' method='post'>
        | Name of session attribute: <input type='text' size='20' name='dataname'/><br/>
        | Value of session attribute: <input type='text' size='20' name='datavalue'/><br/>
        | <input type='submit'/>
        |</form>
        |</p>
        |
        |<p>
        |GET based form <br/>
        |<form action='${response.encodeURL("SessionServlet")}' method='get'>
        | Name of session attribute: <input type='text' size='20' name='dataname'/><br/>
        | Value of session attribute: <input type='text' size='20' name='datavalue'/><br/>
        | <input type='submit'/>
        |</form>
        |</p>
        |
        |<p><a href='${response.encodeURL("SessionServlet?dataname=foo&datavalue=bar")}'>URL encoded</a>
      """.stripMargin)

    out.close()
  }

  override def doPost(req: HttpServletRequest, resp: HttpServletResponse): Unit =
    doGet(req, resp)
}
