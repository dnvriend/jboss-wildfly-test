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

package mdb

import java.util.logging.Logger
import javax.jms.{ MessageListener, Message, TextMessage }
import javax.ejb.{ MessageDriven, ActivationConfigProperty }

@MessageDriven(name = "HelloWorldQueueMDB", activationConfig = Array(
  new ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "queue/HELLOWORLDMDBQueue"),
  new ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
  new ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge")
))
class HelloWorldQueueMDB extends MessageListener {
  val logger = Logger.getLogger(this.getClass.getName)
  override def onMessage(message: Message): Unit = {
    message match {
      case msg: TextMessage ⇒
        logger.info("Received Message from queue: " + msg.getText)
      case msg ⇒
        logger.warning("Message of wrong type: " + msg.getClass.getName)
    }
  }
}
