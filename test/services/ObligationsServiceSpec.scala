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


import java.time.LocalDate
import java.time.format.DateTimeFormatter

import assets.TestConstants
import models._
import play.api.i18n.Messages
import play.api.libs.json.{JsResultException, Json}
import play.mvc.Http.Status
import uk.gov.hmrc.play.http.{HeaderCarrier, InternalServerException}
import utils.TestSupport



import assets.TestConstants.BusinessDetails._
import assets.TestConstants.Obligations._
import assets.TestConstants._
import mocks.connectors.{MockBusinessDetailsConnector, MockBusinessObligationDataConnector, MockPropertyObligationDataConnector}
import utils.TestSupport


class ObligationsServiceSpec extends TestSupport with MockBusinessObligationDataConnector with MockBusinessDetailsConnector with MockPropertyObligationDataConnector {

  object TestObligationsService extends ObligationsService(mockObligationDataConnector, mockBusinessDetailsConnector, mockPropertyObligationDataConnector)

  "The ObligationsService.getObligations method" when {

    "a successful single business" which {

      "has a valid list of obligations returned from the connector" should {

        "return a valid list of obligations" in {
          setupMockBusinesslistResult(testNino)(businessesSuccessModel)
          setupMockObligation(testNino, testSelfEmploymentId)(obligationsDataSuccessModel)
          await(TestObligationsService.getBusinessObligations(testNino)) shouldBe obligationsDataSuccessModel
        }
      }

      "does not have a valid list of obligations returned from the connector" should {

        "return a valid list of obligations" in {
          setupMockBusinesslistResult(testNino)(businessesSuccessModel)
          setupMockObligation(testNino, testSelfEmploymentId)(obligationsDataErrorModel)
          await(TestObligationsService.getBusinessObligations(testNino)) shouldBe obligationsDataErrorModel
        }
      }
    }


    "no business list is found" should {

      "return an obligations error model" in {
        setupMockBusinesslistResult(testNino)(businessListErrorModel)
        await(TestObligationsService.getBusinessObligations(testNino)) shouldBe obligationsDataErrorModel
      }
    }
  }

  "The ObligationsService.getPropertyObligations method" when {

    "a single list of obligations is returned from the connector" should {

      "return a valid list of obligations" in {

        setupMockPropertyObligation(testNino)(TestConstants.Obligations.obligationsDataSuccessModel)

        val successfulObligationsResponse =
          ObligationsModel(
            List(
              ObligationModel(
                start = "2017-04-01",
                end = "2017-6-30",
                due = "2017-7-31",
                met = true
              ),
              ObligationModel(
                start = "2017-7-1",
                end = "2017-9-30",
                due = "2017-10-30",
                met = false
              ),
              ObligationModel(
                start = "2017-7-1",
                end = "2017-9-30",
                due = "2017-10-31",
                met = false
              )
            )
          )
        await(TestObligationsService.getPropertyObligations(testNino)) shouldBe successfulObligationsResponse
      }
    }
  }
}
