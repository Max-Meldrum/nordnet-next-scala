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

package se.meldrum.next.util

import java.net.URLEncoder
import java.security.{KeyFactory, PublicKey}
import java.security.spec.X509EncodedKeySpec
import java.util.Base64
import javax.crypto.Cipher


object Login {

  // https://gist.github.com/urcadox/6173812
  private def decodePublicKey(encodedKey: Array[Byte]): Option[PublicKey] = {
    scala.util.control.Exception.allCatch.opt {
      val spec = new X509EncodedKeySpec(encodedKey)
      val factory = KeyFactory.getInstance("RSA")
      factory.generatePublic(spec)
    }
  }

  // https://gist.github.com/urcadox/6173812
  private def decodePublicKey(encodedKey: String): Option[PublicKey] =
    decodePublicKey(Base64.getDecoder.decode(encodedKey))


  def getAuthParam(config: Config): Option[String] = {
    val l = Base64.getEncoder
      .encodeToString(s"${config.username}:${config.password}:${System.currentTimeMillis().toString}".getBytes)

    val rsa = config.pemfile
    .getLines()
    .filter(!_.startsWith("----")) // Ignore ---- BEGIN KEY / END KEY
    .mkString

    decodePublicKey(rsa).map { pub =>
      val cipher = Cipher.getInstance("RSA")
      cipher.init(Cipher.ENCRYPT_MODE, pub)
      val encryptedBytes = cipher.doFinal(l.getBytes("UTF-8"))
      val authParam = Base64.getEncoder.encodeToString(encryptedBytes)
      URLEncoder.encode(authParam, "UTF-8")
    }
  }
}
