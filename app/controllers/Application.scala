package controllers

import play.api.Environment
import play.api.mvc._
import scala.concurrent.ExecutionContext.Implicits._

import javax.inject._
import play.api.Play


import scala.concurrent._
import scala.concurrent.duration._
import database._
import models._
import play.api.data.Form
import play.api.data.Forms.mapping
import play.api.data.Forms.text
import play.api.libs.concurrent.Execution.Implicits.defaultContext


import org.bson.types.ObjectId

import org.joda.time.DateTime
import play.api.mvc.Action
import play.api.mvc.Controller

import play.api.data.Forms._
import play.api.data._
import play.api.data.format.Formats._

import play.api.i18n.I18nSupport
import play.api.i18n.MessagesApi

import com.mongodb.casbah.Imports._






class Application @Inject() (val messagesApi :MessagesApi)extends Controller with I18nSupport {

  def index = Action { implicit rs =>
    // RegisterJodaTimeConversionHelpers()
    val resultingJobs: List[Job] = Jobs.all
    Ok(views.html.index(resultingJobs))
  }


  private def createDB() = {
    // val tables = Await.result(db.run(MTable.getTables), Duration.Inf).toList
    // if (!tables.isEmpty) {
    //   val reset = DBIO.seq(Jobs.jobTable.schema.drop /*, Tables.data.schema.drop */)
    //   Await.result(db.run(reset), Duration.Inf)
    // }
    // val setup = DBIO.seq(Jobs.jobTable.schema.create /*, Tables.data.schema.create */)
    // Await.result(db.run(setup), Duration.Inf)

  }

  def details(id: String) = Action {
    val jobDetails: Option[Job] = Jobs.byID(id)
    // val relatedJob:  Option[List[Job]] = Jobs.relatedJob(jobDetails.get.jobType, id)
    val relatedJob:  Option[List[Job]] = Jobs.relatedJob(jobDetails.get.jobType, id)
    Ok(views.html.details(jobDetails.get, relatedJob.getOrElse(List[Job]())))
  }

  // def apply = Action {
  //   Ok(views.html.details("Okay"))
  // }

  def add = Action {
    // val regions : List[Region] = Regions.all
    Ok(views.html.add(jobForm))
  }

  def createJob = Action{ implicit request =>
    val form = jobForm.bindFromRequest()
    var jobDetails: Option[Job] = None
    var relatedJob:  Option[List[Job]] = None
    form.fold(
      formWithErrors => {
        println("-------------------------- JOOOOOOOOOOOOOOOOOOOB" + formWithErrors)
        BadRequest(views.html.add(formWithErrors))
      },
      job => {
        println("-------------------------- JOOOOOOOOOOOOOOOOOOOB")
          val jobId = Jobs.insert(job.name, job.description, job.startDate, job.endDate, job.jobType, job.region) //, job.jobType, job.region, job.hourlyPay, job.workingTime, job.email, filename)
          jobDetails = Jobs.byID(jobId)
          relatedJob= Jobs.relatedJob(jobDetails.get.jobType, jobId)
          Ok(views.html.details(jobDetails.get, relatedJob.getOrElse(List[Job]())))
      }
    )
  }

  def jobForm:Form[Job] = Form(
    mapping(
      "name" -> nonEmptyText,
     "description" ->text,
     "startDate" -> jodaDate("yyyy-MM-dd"),
     "endDate" -> jodaDate("yyyy-MM-dd"),
     "jobType" -> text,
     "region" ->text,
    //  "hourlyPay" -> of(doubleFormat),
    //  "workingTime" -> number (min = 0, max = 100),
    //  "email" -> email,
    //  "img"-> optional(text),
     "id" -> optional(text)
   )(Job.apply)(Job.unapply)
  )


}
