package services

import javax.inject.{Inject, Singleton}

import org.apache.jena.rdf.model.Statement
import play.api.Environment
import play.api.libs.json._
import scala.collection.JavaConversions._

/**
  * Created by calvin-pc on 12/1/2016.
  */
@Singleton
class OntologyProvider @Inject() (env:Environment) {
  import java.io.{File, FileInputStream}

  import org.apache.jena.ontology._
  import org.apache.jena.rdf.model.ModelFactory

  private val is = env.classLoader.getResourceAsStream("wisata-2.owl")

  val model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_RULE_INF);

  model.read(is,null,"RDF/XML")

  is.close()

  def instanceToJson(ontResource: OntResource):JsObject = {
    ontResource.listProperties().toList
      .groupBy(s => s.getPredicate.getLocalName)
      .foldLeft(Json.obj("uri" -> ontResource.getURI,"local_name" -> ontResource.getLocalName))((js,ls) =>
        {
          val (key, list_value) = ls
          js + (key -> JsArray(list_value.map( b => b.getObject.toString).map(JsString(_))))
        })
  }

}
