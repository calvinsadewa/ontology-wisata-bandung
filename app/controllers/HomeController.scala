package controllers

import javax.inject._

import org.apache.jena.rdf.model.Statement
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

  def getInstances(ontologyClass: String) = Action.async{
    val model = ontologyProvider.model
    val artefact = model.getOntClass( baseURI + ontologyClass );
    val results = artefact.listInstances().toList.filterNot(_.getURI == null)
      .map( m => ontologyProvider.instanceToJson(m) )

    Future(Ok(Json.toJson(results)))
  }

  def getInstance(instanceName: String) = Action.async{
    val model = ontologyProvider.model
    val artefact = model.getOntResource( baseURI + instanceName )
    val result = ontologyProvider.instanceToJson(artefact)

    Future(Ok(result))
  }

}