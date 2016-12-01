import java.io.{File, FileInputStream, InputStream}

import org.apache.jena.ontology._
import org.apache.jena.riot._

import scala.collection.JavaConversions._
import org.apache.jena.rdf.model.{ModelFactory, Statement}
import play.api.libs.json._

val owlfile = new File("D:\\Coba PLAY\\ontology-wisata-bandung\\conf\\wisata-2.owl")
val is = new FileInputStream(owlfile)

val model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_RDFS_INF);

model.read(is,null,"RDF/XML")

is.close()
val artefact = model.getOntClass( "http://www.semanticweb.org/paramita/ontologies/2016/10/untitled-ontology-28#Lokasi")

artefact.listInstances(false).toList
  .map( m =>
    m.listProperties().toList.filter(_.getObject.toString != "http://www.w3.org/2002/07/owl#NamedIndividual")
      .map((s) =>
        (s.getPredicate.getLocalName -> JsString(s.getObject.toString))
      )
  )