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
import javax.inject.Inject
import javax.jms.Destination
import javax.jms.JMSContext
import javax.jms.JMSDestinationDefinition
import javax.jms.JMSDestinationDefinitions
import javax.jms.Queue
import javax.jms.Topic
import javax.servlet.annotation._
import javax.servlet.http._

@JMSDestinationDefinitions(
  value = Array(
    new JMSDestinationDefinition(
      name = "java:/queue/HELLOWORLDMDBQueue",
      interfaceName = "javax.jms.Queue",
      destinationName = "HelloWorldMDBQueue"
    ),
    new JMSDestinationDefinition(
      name = "java:/topic/HELLOWORLDMDBTopic",
      interfaceName = "javax.jms.Topic",
      destinationName = "HelloWorldMDBTopic"
    )
  ))
@WebServlet(value = Array("/HelloWorldMDBServlet"))
class HelloWorldMDBServlet extends HttpServlet {

  final val MsgCount: Int = 5

  @Inject
  var context: JMSContext = null

  @Resource(lookup = "java:/queue/HELLOWORLDMDBQueue")
  var queue: Queue = null

  @Resource(lookup = "java:/topic/HELLOWORLDMDBTopic")
  var topic: Topic = null

  override def doGet(request: HttpServletRequest, response: HttpServletResponse) {
    response.setContentType("text/html")
    response.setCharacterEncoding("UTF-8")

    val out = response.getWriter

    val useTopic: Boolean = request.getParameterMap.keySet().contains("topic")
    val destination: Destination = if (useTopic) topic else queue

    out.println("<h3>HelloWorldMDBServlet</h3>")
    out.write("<p>Sending messages to <em>" + destination + "</em></p>")
    out.write("<h2>Following messages will be send to the destination:</h2>")

    (1 to MsgCount).foreach { i ⇒
      val msg = s"This is message $i"
      context.createProducer().send(destination, msg)
      out.println(msg + "<br/>")
    }

    out.write("<p><i>Go to your WildFly Server console or Server log to see the result of messages processing</i></p>");
    out.close()
  }

  override def doPost(req: HttpServletRequest, resp: HttpServletResponse): Unit =
    doGet(req, resp)
}
