/*
 * Copyright 2017 HM Revenue & Customs
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

package views.helpers

import play.api.i18n.{Messages, MessagesApi}
import utils.ImplicitDateFormatter._

object TaxYearRenderHelper {

  def renderTaxYear(taxYear: Int)(implicit messages: Messages): String = {
    messages("estimated_tax_liability.tax-year", s"${taxYear - 1}", s"$taxYear")
  }

  def renderTitle(taxYear: Int)(implicit messages: Messages): String = {
    messages("estimated_tax_liability.title", s"${taxYear - 1}", s"$taxYear")
  }

  def renderPaymentDueDate(taxYear: Int): String = {
    s"${taxYear + 1}-01-31".toLocalDate.toLongDate
  }
}
