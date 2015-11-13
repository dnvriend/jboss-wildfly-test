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

@WebServlet(value = Array("/RequestInfoServlet"))
class RequestInfoServlet extends HttpServlet {
  override def doGet(request: HttpServletRequest, response: HttpServletResponse) {
    response.setContentType("text/html")
    response.setCharacterEncoding("UTF-8")

    val out = response.getWriter
    out.println(s"""
       |<h3>Request Information Example</h3>
       |Method: ${request.getMethod} <br/>
       |Protocol: ${request.getProtocol} <br/>
       |<br/>
       |
       |Request URI: ${request.getRequestURI} <br/>
       |PathInfo: ${request.getPathInfo} <br/>
       |Path: ${request.getContextPath} <br/>
       |Base Path: ${request.getScheme}://${request.getServerName}:${request.getServerPort}${request.getContextPath}/ <br/>
       |Servlet Path: ${request.getServletPath} <br/>
       |<br/>
       |
       |Remote Address: ${request.getRemoteAddr} <br/>
       |Remote Host: ${request.getRemoteHost} <br/>
       |Remote User: ${request.getRemoteUser} <br/>
       |Remote Port: ${request.getRemotePort} <br/>
       |<br/>
       |
       |Local Address: ${request.getLocalAddr} <br/>
       |Local Name: ${request.getLocalName} <br/>
       |Local Port: ${request.getLocalPort} <br/>
       |<br/>
      """.stripMargin)
  }

  override def doPost(req: HttpServletRequest, resp: HttpServletResponse): Unit =
    doGet(req, resp)
}
