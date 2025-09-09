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

package models

import models.DashboardStatus.{CannotStartYet, Completed, NotStarted}

enum DashboardStatus {
  case CannotStartYet, NotStarted, Completed
}

enum DashboardStage(
    val companyDetailsStatus: DashboardStatus = NotStarted,
    val contactsInfoStatus: DashboardStatus = CannotStartYet,
    val showSubmit: Boolean = false
) {
  case CompanyDetails extends DashboardStage()
  case ContactsInfo
      extends DashboardStage(
        companyDetailsStatus = Completed,
        contactsInfoStatus = NotStarted
      )
  case Submission
      extends DashboardStage(
        companyDetailsStatus = Completed,
        contactsInfoStatus = Completed,
        showSubmit = true
      )
}
