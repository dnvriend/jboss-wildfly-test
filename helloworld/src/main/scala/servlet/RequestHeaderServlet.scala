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

@WebServlet(value = Array("/RequestHeaderServlet"))
class RequestHeaderServlet extends HttpServlet {
  override def doGet(request: HttpServletRequest, response: HttpServletResponse) {
    response.setContentType("text/html")
    response.setCharacterEncoding("UTF-8")

    val out = response.getWriter
    out.println("<h3>Request Header Example</h3>")

    import scala.collection.JavaConversions._
    val xs = request.getHeaderNames
    xs.foreach { headerName ⇒
      val headerValue = request.getHeader(headerName)
      out.println(s"$headerName: $headerValue <br/>")
    }
    out.close()
  }
  override def doPost(req: HttpServletRequest, resp: HttpServletResponse): Unit =
    doGet(req, resp)
}
