package services

import javax.inject.{Inject, Singleton}

import play.api.Environment

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

}
