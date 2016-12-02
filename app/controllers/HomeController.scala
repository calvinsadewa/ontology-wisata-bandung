package controllers

import javax.inject._

import org.semanticweb.owlapi.model.IRI
import play.api._
import play.api.mvc._
import play.api.libs.json._
import services.OntologyProvider

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.JavaConversions._

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject() (ontologyProvider: OntologyProvider) extends Controller {

  val baseURI = "http://www.semanticweb.org/paramita/ontologies/2016/10/untitled-ontology-28#"
  /**
   * Create an Action to render an HTML page with a welcome message.
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def getIndividual(individualName: String) = Action.async{
    val individual = ontologyProvider.getIndividual(IRI.create(baseURI + individualName))
    val result = ontologyProvider.individualToJson(individual)

    Future(Ok(result))
  }

  def DLQuery (query:String)  = Action.async{
    val individuals = ontologyProvider.DLInference( java.net.URLDecoder.decode(query, "UTF-8") );
    val results = individuals
      .map( m => ontologyProvider.individualToJson(m) )

    Future(Ok(Json.toJson(results)))
  }

}