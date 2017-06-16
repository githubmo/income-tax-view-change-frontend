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

package services

import javax.inject.{Inject, Singleton}

import connectors.{BusinessDetailsConnector, ObligationDataConnector}
import models._
import play.api.Logger
import play.api.libs.json.JsResultException
import play.twirl.api.Html
import uk.gov.hmrc.play.http.{HeaderCarrier, InternalServerException}
import utils.ImplicitLongDate._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class ObligationsService @Inject()(val obligationDataConnector: ObligationDataConnector,
                                  val businessDetailsConnector: BusinessDetailsConnector
                                  ) {

  def getObligations(nino: String)(implicit hc: HeaderCarrier): Future[ObligationsResponseModel] = {

    Logger.debug(s"[ObligationsService][getObligations] - Requesting Obligation details from connectors for user with NINO: $nino")
    businessDetailsConnector.getBusinessList(nino).flatMap {
      case success: BusinessListModel =>
        // Only one business is returned for MVP hence .head to obtain ID.
        Logger.debug(s"[ObligationsService][getObligations] - Retrieved Obligation: \n\n$success")
        obligationDataConnector.getObligationData(nino, success.business.head.id)
      case error: BusinessListError =>
        Logger.debug(s"[ObligationService][getObligations] - Error Response Status: ${error.code}, Message: ${error.message}")
        Future.successful(ObligationsErrorModel(error.code, error.message))
    }
  }
}