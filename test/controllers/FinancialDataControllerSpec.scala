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

package controllers

import assets.Messages.{EstimatedTaxLiability => messages}
import assets.TestConstants.Estimates._
import assets.TestConstants.CalcBreakdown._
import assets.TestConstants._
import assets.TestConstants.BusinessDetails._
import assets.TestConstants.PropertyDetails._
import config.FrontendAppConfig
import mocks.services.MockFinancialDataService
import controllers.predicates.{AuthenticationPredicate, IncomeSourceDetailsPredicate}
import mocks.controllers.predicates.{MockAsyncActionPredicate, MockAuthenticationPredicate, MockIncomeSourceDetailsPredicate}
import play.api.http.Status
import play.api.i18n.MessagesApi
import play.api.test.Helpers.{contentType, _}
import utils.TestSupport


class FinancialDataControllerSpec extends TestSupport
  with MockFinancialDataService with MockAsyncActionPredicate with MockIncomeSourceDetailsPredicate with MockAuthenticationPredicate {

  // Last Calculation Service mocks
  def mockFinancialDataSuccess(): Unit = setupMockGetFinancialData(testNino, testYear)(Some(calculationDisplaySuccessModel(calculationDataSuccessModel)))
  def mockFinancialDataNoBreakdown(): Unit = setupMockGetFinancialData(testNino, testYear)(Some(calculationDisplaySuccessModel(calculationDataSuccessModel)))
  def mockFinancialDataError(): Unit = setupMockGetFinancialData(testNino, testYear)(None)

  class setupTestController(authentication: AuthenticationPredicate, incomeSources: IncomeSourceDetailsPredicate)
    extends FinancialDataController()(
      fakeApplication.injector.instanceOf[FrontendAppConfig],
      fakeApplication.injector.instanceOf[MessagesApi],
      new asyncActionBuilder(authentication, incomeSources),
      mockFinancialDataService
    )

  "The FinancialDataController.getFinancialData(year) action" when {

    "Called with an Authenticated HMRC-MTD-IT User" which {

      "that successfully retrieves Business only income from the Income Sources predicate" +
        "and financial data from the FinancialData Service" should {

        object TestFinancialDataController extends setupTestController(MockAuthenticated, BusinessIncome)

        lazy val result = TestFinancialDataController.getFinancialData(testYear)(fakeRequestWithActiveSession)
        lazy val document = result.toHtmlDocument

        "return Status OK (200)" in {
          mockFinancialDataSuccess()
          status(result) shouldBe Status.OK
        }

        "return HTML" in {
          mockFinancialDataSuccess()
          contentType(result) shouldBe Some("text/html")
          charset(result) shouldBe Some("utf-8")
        }

        "render the EstimatedTaxLiability page" in {
          mockFinancialDataSuccess()
          document.title() shouldBe messages.title
        }
      }

      "receives Property only income from the Income Sources predicate" +
        "and financial data from the FinancialData Service" should {

        object TestFinancialDataController extends setupTestController(MockAuthenticated, PropertyIncome)

        lazy val result = TestFinancialDataController.getFinancialData(testYear)(fakeRequestWithActiveSession)
        lazy val document = result.toHtmlDocument

        "return Status OK (200)" in {
          mockFinancialDataSuccess()
          status(result) shouldBe Status.OK
        }

        "return HTML" in {
          mockFinancialDataSuccess()
          contentType(result) shouldBe Some("text/html")
          charset(result) shouldBe Some("utf-8")
        }

        "render the EstimatedTaxLiability page" in {
          mockFinancialDataSuccess()
          document.title() shouldBe messages.title
        }
      }

      "receives Business Income source from the Income Sources predicate and an error from the FinancialData Service" should {

        object TestFinancialDataController extends setupTestController(MockAuthenticated, BusinessIncome)

        lazy val result = TestFinancialDataController.getFinancialData(testYear)(fakeRequestWithActiveSession)

        "return Internal Server Error (500)" in {
          mockFinancialDataError()
          status(result) shouldBe Status.INTERNAL_SERVER_ERROR
        }

        "return HTML" in {
          contentType(result) shouldBe Some("text/html")
          charset(result) shouldBe Some("utf-8")
        }
      }

      "receives No Income sources from the Income Sources predicate and an error from the FinancialData Service" should {

        object TestFinancialDataController extends setupTestController(MockAuthenticated, NoIncome)

        lazy val result = TestFinancialDataController.getFinancialData(testYear)(fakeRequestWithActiveSession)

        "return Internal Server Error (500)" in {
          mockFinancialDataError()
          status(result) shouldBe Status.INTERNAL_SERVER_ERROR
        }

        "return HTML" in {
          contentType(result) shouldBe Some("text/html")
          charset(result) shouldBe Some("utf-8")
        }
      }

    }

    "Called with an Unauthenticated User" should {

      object TestFinancialDataController extends setupTestController(MockUnauthorised, PropertyIncome)

      "return redirect SEE_OTHER (303)" in {
        val result = TestFinancialDataController.getFinancialData(testYear)(fakeRequestNoSession)
        status(result) shouldBe Status.SEE_OTHER
      }
    }
  }


  "The FinancialDataController.redirectToEarliestEstimatedTaxLiability() action" when {

    "Called with an Authenticated HMRC-MTD-IT User" which {

      "successfully retrieves Business only income from the Income Sources predicate" should {

        object TestFinancialDataController extends setupTestController(MockAuthenticated, BusinessIncome)

        lazy val result = TestFinancialDataController.redirectToEarliestEstimatedTaxLiability(fakeRequestWithActiveSession)

        "return Status SEE_OTHER (303) (redirect)" in {
          status(result) shouldBe Status.SEE_OTHER
        }

        s"redirect to ${
          controllers.routes.FinancialDataController
            .getFinancialData(businessIncomeModel.accountingPeriod.determineTaxYear)
        }" in {
          redirectLocation(result) shouldBe Some(controllers.routes.FinancialDataController
            .getFinancialData(businessIncomeModel.accountingPeriod.determineTaxYear).url)
        }
      }

      "successfully retrieves Property only income from the Income Sources predicate" should {

        object TestFinancialDataController extends setupTestController(MockAuthenticated, PropertyIncome)

        lazy val result = TestFinancialDataController.redirectToEarliestEstimatedTaxLiability(fakeRequestWithActiveSession)

        "return Status SEE_OTHER (303) (redirect)" in {
          status(result) shouldBe Status.SEE_OTHER
        }

        s"redirect to ${
          controllers.routes.FinancialDataController
            .getFinancialData(propertySuccessModel.accountingPeriod.determineTaxYear)
        }" in {
          redirectLocation(result) shouldBe Some(controllers.routes.FinancialDataController
            .getFinancialData(propertySuccessModel.accountingPeriod.determineTaxYear).url)
        }
      }

      "successfully retrieves both Property and Business income from the Income Sources predicate" when {

        "the Business Accounting Period is aligned to the Tax Year and the Property Income" should {

          object TestFinancialDataController extends setupTestController(MockAuthenticated, BothIncomeAlignedTaxYear)

          lazy val result = TestFinancialDataController.redirectToEarliestEstimatedTaxLiability(fakeRequestWithActiveSession)

          "return Status SEE_OTHER (303) (redirect)" in {
            status(result) shouldBe Status.SEE_OTHER
          }

          s"redirect to ${
            controllers.routes.FinancialDataController
              .getFinancialData(propertySuccessModel.accountingPeriod.determineTaxYear)
          }" in {
            redirectLocation(result) shouldBe Some(controllers.routes.FinancialDataController
              .getFinancialData(propertySuccessModel.accountingPeriod.determineTaxYear).url)
          }

        }

        "the Property Income Accounting Period is prior to the Business Income Accounting Period" should {

          object TestFinancialDataController extends setupTestController(MockAuthenticated, BothIncome)

          lazy val result = TestFinancialDataController.redirectToEarliestEstimatedTaxLiability(fakeRequestWithActiveSession)

          "return Status SEE_OTHER (303) (redirect)" in {
            status(result) shouldBe Status.SEE_OTHER
          }

          s"redirect to ${
            controllers.routes.FinancialDataController
              .getFinancialData(propertySuccessModel.accountingPeriod.determineTaxYear)
          }" in {
            redirectLocation(result) shouldBe Some(controllers.routes.FinancialDataController
              .getFinancialData(propertySuccessModel.accountingPeriod.determineTaxYear).url)
          }

        }
      }

      "retrieves no Income Sources via the Income Sources predicate" should {

        object TestFinancialDataController extends setupTestController(MockAuthenticated, NoIncome)

        lazy val result = TestFinancialDataController.redirectToEarliestEstimatedTaxLiability(fakeRequestWithActiveSession)

        "return Status ISE (500)" in {
          status(result) shouldBe Status.INTERNAL_SERVER_ERROR
        }
      }
    }

    "Called with an Unauthenticated User" should {

      object TestFinancialDataController extends setupTestController(MockUnauthorised, PropertyIncome)

      "return redirect SEE_OTHER (303)" in {
        val result = TestFinancialDataController.getFinancialData(testYear)(fakeRequestNoSession)
        status(result) shouldBe Status.SEE_OTHER
      }
    }
  }

}