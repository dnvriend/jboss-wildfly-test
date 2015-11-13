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

import javax.servlet.annotation._
import javax.servlet.http._

@WebServlet(value = Array("/CookieServlet"))
class CookieServlet extends HttpServlet {
  override def doGet(request: HttpServletRequest, response: HttpServletResponse) {
    response.setContentType("text/html")
    response.setCharacterEncoding("UTF-8")

    val out = response.getWriter
    out.println("<h3>Cookie Test Example</h3>")

    val cookiesArrayOption = Option(request.getCookies)
    if (cookiesArrayOption.isEmpty) {
      out.println("Your browser isn't sending any cookies")
    } else {
      cookiesArrayOption.foreach { cookiesArray ⇒
        out.println("Your browser is sending the following cookies to the server: <br/>")
        import scala.collection.JavaConversions._
        cookiesArray.zipWithIndex.foreach {
          case (cookie, index) ⇒ out.println(s"Cookie[$index] Name: ${cookie.getName}, Value: ${cookie.getValue} <br/>")
        }
      }
    }

    val cookieNameOption = Option(request.getParameter("cookiename"))
    val cookieValueOption = Option(request.getParameter("cookievalue"))
    (cookieNameOption, cookieValueOption) match {
      case (Some(cookieName), Some(cookieValue)) ⇒
        val cookie = new Cookie(cookieName, cookieValue)
        response.addCookie(cookie)
        out.println(
          s"""
            |<p>
            |You just sent the following cookie to your browser: <br/>
            |Please refresh the browser so that the browser will send the newly added cookie to the server. <br/>
            |Name: $cookieName <br/>
            |Value: $cookieValue <br/>
            |</p>
          """.stripMargin)
      case _ ⇒
        out.println(
          """
          |<p>
          |Create a cookie to send to your browser: <br/>
          |<form action='CookieServlet' method='post'>
          |Name: <input type='text' length='20' name='cookiename'/><br/>
          |Value: <input type='text' length='20' name='cookievalue'/><br/>
          |<input type='submit'/>
          |</form>
          |</p>
        """.stripMargin)
    }
    out.close()
  }

  override def doPost(req: HttpServletRequest, resp: HttpServletResponse): Unit =
    doGet(req, resp)
}
