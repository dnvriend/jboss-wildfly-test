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
import javax.jms._
import javax.servlet.annotation._
import javax.servlet.http._

/**
 * Please note that ActiveMQ only supports JMS 1.1 API, so this servlet only uses
 * the JMS 1.1 API and not the newer 2.0 API
 *
 * see: https://developer.jboss.org/wiki/HowToUseOutOfProcessActiveMQWithWildFly
 */
@WebServlet(value = Array("/HelloWorldMDBServlet"))
class HelloWorldMDBServlet extends HttpServlet {

  final val MsgCount: Int = 5

  @Resource(lookup = "java:/ActiveMQConnectionFactory")
  var cf: ConnectionFactory = null

  @Resource(mappedName = "java:/queue/HELLOWORLDMDBQueue")
  var queue: Queue = null

  @Resource(mappedName = "java:/topic/HELLOWORLDMDBTopic")
  var topic: Topic = null

  override def doGet(request: HttpServletRequest, response: HttpServletResponse) {
    response.setContentType("text/html")
    response.setCharacterEncoding("UTF-8")

    val out = response.getWriter

    val useTopic: Boolean = request.getParameterMap.keySet().contains("topic")
    val destination: Destination = if (useTopic) topic else queue

    val connection: Connection = cf.createConnection()
    val session: Session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)
    val producer: MessageProducer = session.createProducer(destination)

    out.println("<h3>HelloWorldMDBServlet</h3>")
    out.write("<p>Sending messages to <em>" + destination + "</em></p>")
    out.write("<h2>Following messages will be send to the destination:</h2>")

    (1 to MsgCount).foreach { i ⇒
      val text = s"This is message $i"
      val message: TextMessage = session.createTextMessage(text)
      producer.send(message)
      out.println(text + "<br/>")
    }

    out.write("<p><i>Go to your WildFly Server console or Server log to see the result of messages processing</i></p>");
    out.close()
  }

  override def doPost(req: HttpServletRequest, resp: HttpServletResponse): Unit =
    doGet(req, resp)
}
