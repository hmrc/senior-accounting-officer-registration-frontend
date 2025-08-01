/*
 * Copyright 2025 HM Revenue & Customs
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

package support

import play.api.http.SecretConfiguration
import play.api.libs.crypto.DefaultCookieSigner
import uk.gov.hmrc.crypto.{PlainText, SymmetricCryptoFactory}

import java.net.URLEncoder

object SessionCookieBaker {
  private val applicationSecret = "yNhI04vHs9<_HWbC`]20u`37=NGLGYY5:0Tg5?y`W<NoJnXWqmjcgZBec@rOxb^G"
  private val cookieKey         = "gvBoGdgzqG1AarzF1LY0zQ=="
  private val cookieSigner      = new DefaultCookieSigner(SecretConfiguration(cookieKey))

  private def cookieValue(sessionData: Map[String, String], lang: Option[String]) = {
    def encode(data: Map[String, String]): PlainText = {
      val encoded = data
        .map((k, v) => s"${URLEncoder.encode(k, "UTF-8")}=${URLEncoder.encode(v, "UTF-8")}")
        .mkString("&")
      val key = applicationSecret.getBytes
      PlainText(s"${cookieSigner.sign(encoded, key)}-$encoded")
    }

    val encodedCookie = encode(sessionData)
    val encrypted     = SymmetricCryptoFactory.aesGcmCrypto(cookieKey).encrypt(encodedCookie).value
    val langValue     = lang.fold("")(l => s"PLAY_LANG=$l;")

    s"""mdtp="$encrypted"; Path=/; HTTPOnly"; Path=/; HTTPOnly; $langValue"""
  }

  def bakeSessionCookie(additionalData: Map[String, String] = Map.empty, lang: Option[String] = None): String =
    cookieValue(additionalData, lang)
}
