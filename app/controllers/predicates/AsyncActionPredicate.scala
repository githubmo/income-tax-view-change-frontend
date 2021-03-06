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

package controllers.predicates

import javax.inject.{Inject, Singleton}

import auth.MtdItUser
import controllers.BaseController
import models.IncomeSourcesModel
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, Request, Result}
import play.twirl.api.Html

import scala.concurrent.Future

@Singleton
class AsyncActionPredicate @Inject()(implicit val messagesApi: MessagesApi,
                                     val sessionTimeoutPredicate: SessionTimeoutPredicate,
                                     val authenticationPredicate: AuthenticationPredicate,
                                     val incomeSourceDetailsPredicate: IncomeSourceDetailsPredicate
                                    ) extends BaseController {

  def async(action: Request[AnyContent] => MtdItUser => IncomeSourcesModel => Future[Result]): Action[AnyContent] =
    Action.async { implicit request =>
      sessionTimeoutPredicate.checkSessionTimeout {
        authenticationPredicate.authorisedUser { implicit user =>
            incomeSourceDetailsPredicate.retrieveIncomeSources { implicit sources =>
              action(request)(user)(sources)
            }
          }
        }
      }

}