/*
 * Copyright 2017 Max Meldrum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.meldrum.next.api

import fs2.{Strategy, Task}
import org.http4s.{MediaType, Request, Uri}
import org.http4s.client.blaze.PooledHttp1Client
import org.http4s.headers.`Content-Type`
import org.http4s.headers.`Accept`
import org.http4s.dsl._
import org.http4s.circe._
import io.circe.syntax._
import org.http4s.Status.Successful
import se.meldrum.next.api.NextClient.NextRequest
import se.meldrum.next.models._
import se.meldrum.next.util.Login

class NextClient(config: NextConfig) extends Encoders {
  private[this] val client = PooledHttp1Client()
  private[this] val headers = List(`Content-Type`(MediaType.`application/json`), `Accept`(MediaType.`application/json`)
  , `Content-Type`(MediaType.`application/json`))
  implicit val strategy = Strategy.fromExecutionContext(scala.concurrent.ExecutionContext.Implicits.global)

  // To handle JSON decoding..
  import io.circe.generic.auto._

  def getStatus(): Task[NextStatus] = {
    // This is not working... look into it
    //val uri = new Uri().withPath(config.baseURL)
    val uri = Uri.uri("https://api.test.nordnet.se/next/2/")
    val req = Request(GET, uri)
      .putHeaders(headers :_*)

    client.expect(req)(jsonOf[NextStatus])
  }


  def login(): Option[NextRequest[ErrorResponse, LoginResponse]] = {
    val uri = Uri.uri("https://api.test.nordnet.se/next/2/login/")
    val authParam = Login.getAuthParam(config)
    authParam.map {p =>
      val e = LoginRequest(p, "NEXTAPI2")
      val post = Request(POST, uri)
        .withBody(e.asJson)
        .putHeaders(headers :_*)

      client.fetch(post) {
        case Successful(resp) => resp.as[LoginResponse].map(Right(_))
        case resp => resp.as[ErrorResponse].map(Left(_))
      }
    }
  }
}

object NextClient {
  type NextRequest[A,B]= Task[Either[A,B]]
}
