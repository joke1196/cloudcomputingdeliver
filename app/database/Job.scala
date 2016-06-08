package database

import scala.concurrent._
import scala.concurrent.duration._
import scala.collection.JavaConversions._
import javax.inject.Inject
import play.api.Play
import javax.inject._
import play.api.libs.concurrent.Execution.Implicits._

import com.mongodb.casbah.Imports._
import com.mongodb.casbah.MongoConnection
import com.mongodb.casbah.MongoURI

import org.joda.time.DateTime


import org.bson.types.ObjectId


case class Job( name: String, description:String, startDate: DateTime, endDate:DateTime, jobType:String, region:String, id: Option[String] = None) //,jobType:Int, region:Int, hourlyPay:Double, workingTime:Int, email:String, img:Option[String] = None, id: Option[Int] = None)

object JobMongoProperties {
  val ID = "_id"
  val NAME = "name"
  val DESCRIPTION = "description"
  val STARTDATE = "startDate"
  val ENDDATE = "endDate"
  val JOBTYPE = "jobType"
  val REGION = "region"
}




object MongoFactory {
    private val URL = "mongodb://admin:MxkhQI8eIAQd@575856d689f5cf71c700006d-kendreey.rhcloud.com:49476/"
    private val DATABASE = "cloudplay"
    private val COLLECTION = "jobs"
    // val connection = MongoConnection(SERVER)
    // val collection = connection(DATABASE)(COLLECTION)

    lazy val uri = MongoClientURI( URL + DATABASE)
    lazy val mongo = MongoClient(uri)
    lazy val db = mongo(uri.database.get)
    lazy val collection = db(COLLECTION)

}


object Jobs {


    com.mongodb.casbah.commons.conversions.scala.RegisterConversionHelpers()
    com.mongodb.casbah.commons.conversions.scala.RegisterJodaTimeConversionHelpers()


// name: String,description:String, startDate: Date, endDate: Date, jobType:Int, region:Int, hourlyPay:Double, workingTime:Int, email:String, img:Option[String]

  def insert(name: String, description:String, startDate: DateTime, endDate:DateTime, jobType:String, region:String):String = {
    val mongoObj = toMongoObject(Job(name,description ,startDate ,endDate, jobType, region))
    MongoFactory.collection.save(mongoObj)
    mongoObj.get("_id").toString
    // Await.result(db.run(jobTable += new Job(name, description, startDate, endDate,jobType, region, hourlyPay, workingTime, email, img )), Duration.Inf)
  }

  def all(): List[Job]  ={
  //  (for (j <- Await.result(db.run(jobTable.result), Duration.Inf)) yield j).toList
    // Jobs.find(MongoDBObject("_id" -> MongoDBObject("$exists" -> true))).toList
    val results = MongoFactory.collection.find()
    (for( j <- results) yield fromMongoObject(j)).toList
  // yield new Job(j.name, j.startDate,j.endDate,j.jobType, j.region,j.hourlyPay,j.workingTime,j.email,j.img)).toList

 }
  // def insert(job: Job): Future[Unit] = dbConfig.db.run(jobs += job).map { _ => () }

  def byID(id: String): Option[Job] = {
    // val jobs = for (j <- Await.result(db.run(jobTable.filter(_.id === id).result), Duration.Inf)) yield j
    // if (jobs.isEmpty) None
    // else Some(jobs.head)
    val oid:ObjectId = new ObjectId(id.asInstanceOf[String])
    val mongoObj = MongoDBObject("_id" -> oid)
    val res = MongoFactory.collection.findOne(mongoObj)
    res match {
      case Some(res) => Some(fromMongoObject(res))
      case None => None
    }

  }

  def getTypes(): List[String] = {
    val res = all
    res.map(x=>x.jobType).distinct
  }

  def relatedJob(jobType: String, id:String): Option[List[Job]] = {
      // val job = for (j <- Await.result(db.run(jobTable.filter(_.jobType === typeId).filter(_.id =!= id).result), Duration.Inf)) yield j
      // if(job.isEmpty) None
      // else Some(job.take(5).toList)
      val oid : ObjectId = new ObjectId(id.asInstanceOf[String])
      val mongoObj = MongoDBObject("jobType" -> jobType) ++ ("_id" $ne oid)
      val res = MongoFactory.collection.find(mongoObj)
      Some((for(j <- res) yield fromMongoObject(j)).take(5).toList)
  }

  def toMongoObject(job: Job): DBObject = {
   MongoDBObject(
     JobMongoProperties.NAME -> job.name,
     JobMongoProperties.DESCRIPTION -> job.description,
     JobMongoProperties.STARTDATE -> job.startDate,
     JobMongoProperties.ENDDATE -> job.endDate,
     JobMongoProperties.JOBTYPE -> job.jobType,
     JobMongoProperties.REGION -> job.region
   )
 }

 def fromMongoObject(db: DBObject): Job = {
   new Job(
     id = Some(db.get(JobMongoProperties.ID).toString),
     name = db.getAsOrElse[String](JobMongoProperties.NAME, ""),
     description = db.getAsOrElse[String](JobMongoProperties.DESCRIPTION, ""),
     startDate = db.getAsOrElse[DateTime](JobMongoProperties.STARTDATE, new DateTime()), // slightly different get
     endDate = db.getAsOrElse[DateTime](JobMongoProperties.ENDDATE, new DateTime()),
     jobType = db.getAsOrElse[String](JobMongoProperties.JOBTYPE, ""),
     region = db.getAsOrElse[String](JobMongoProperties.REGION, "")

   )
 }


}
