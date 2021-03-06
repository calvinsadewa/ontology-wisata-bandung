package services

import javax.inject.{Inject, Singleton}

import java.io.File
import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntaxEditorParser
import org.semanticweb.HermiT.Reasoner
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.expression.ShortFormEntityChecker
import org.semanticweb.owlapi.model._
import org.semanticweb.owlapi.util.{BidirectionalShortFormProvider, BidirectionalShortFormProviderAdapter, ShortFormProvider, SimpleShortFormProvider}
import org.semanticweb.owlapi.search._
import play.api.Environment
import play.api.libs.json._

import scala.collection.JavaConversions._

/**
  * Created by calvin-pc on 12/1/2016.
  */
@Singleton
class OntologyProvider @Inject() (env:Environment) {
  private val manager = OWLManager.createOWLOntologyManager();
  private val ontology = manager
    .loadOntologyFromOntologyDocument(new File(env.classLoader.getResource("wisata.owl").toURI));
  private val factory = manager.getOWLDataFactory
  private val reasoner = new Reasoner.ReasonerFactory().createReasoner(ontology);
  private val shortFormProvider = new SimpleShortFormProvider();
  private val parser = new DLQueryParser(ontology,shortFormProvider)
  private val class_to_inverse_object_properties = ontology.axioms(AxiomType.OBJECT_PROPERTY_RANGE).iterator.toSeq
    .map(a => (a.getProperty.getNamedProperty,a.getRange))
    .map{
      case (property,ranges) =>
        (property,reasoner.getSubClasses(ranges).flatten ++ ranges.getClassesInSignature)
    }
    .flatMap{
      case (property,classes) => classes.map((_,property))
    }
    .groupBy{
      case (cls,_) => cls
    }
    .map{
      case (cls,properties) => (cls,properties.map{case (_,p) => p })
    }.withDefaultValue(Seq())

  def getIndividual(iri: IRI): OWLNamedIndividual ={
    factory.getOWLNamedIndividual(iri)
  }

  def DLInference (query: String): List[OWLNamedIndividual] ={
    val classExpression = parser
      .parseClassExpression(query);
    val individuals = reasoner.getInstances(classExpression, false);
    individuals.getFlattened.toList
  }

  def individualToJson(individual: OWLNamedIndividual): JsObject = {
    val classes = EntitySearcher.getTypes(individual,ontology).iterator.toSeq.map(b => b.asOWLClass())
    val inverse_properties = classes.flatMap(class_to_inverse_object_properties(_)).toSet
    val inverse_property_to_individuals = inverse_properties
      .map(ip => (ip,DLInference("%s some {%s}".format(ip.getIRI.getShortForm,individual.getIRI.getShortForm))))
    val js = ontology.axioms(individual).iterator().toList
      .groupBy(axiom => axiom.getAxiomType).toList
      .filter(_ match {
        case (AxiomType.OBJECT_PROPERTY_ASSERTION, _) => true
        case (AxiomType.DATA_PROPERTY_ASSERTION, _) => true
        case (AxiomType.CLASS_ASSERTION, _) => true
        case _ => false
      })
      .foldRight(Json.obj("uri" -> individual.getIRI.toString,"local_name" -> individual.getIRI.getShortForm))((gr,js) => {
          gr match {
            case (AxiomType.OBJECT_PROPERTY_ASSERTION,axioms) => {
              val propertyAndSubjects = axioms
                .map(_.asInstanceOf[OWLObjectPropertyAssertionAxiom])
                .groupBy(
                  axiom => shortFormProvider.getShortForm(axiom.getProperty.getNamedProperty)
                ).map( _ match {
                case (property_name, axioms) =>
                  (property_name,
                    axioms
                      .map(_.getObject.asOWLNamedIndividual())
                      .map(a => Json.obj("name" -> shortFormProvider.getShortForm(a), "iri" -> a.getIRI.toString)))
              })
              val isi:JsValue = Json.toJson(propertyAndSubjects)
              js + ("object_property" -> isi)
            }
            case (AxiomType.DATA_PROPERTY_ASSERTION,axioms) => {
              val propertyAndSubjects = axioms
                .map(_.asInstanceOf[OWLDataPropertyAssertionAxiom])
                .groupBy(
                  axiom => shortFormProvider.getShortForm(axiom.getProperty.asOWLDataProperty)
                ).map( _ match {
                case (property_name, axioms) =>
                  (property_name,
                    axioms
                      .map(_.getObject.getLiteral)
                  )
              })
              val isi:JsValue = Json.toJson(propertyAndSubjects)
              js + ("data_property" -> isi)
            }
            case (AxiomType.CLASS_ASSERTION,axioms) => {
              val propertyAndSubjects = axioms
                .map(_.asInstanceOf[OWLClassAssertionAxiom].getClassExpression.asOWLClass)
                .map(a => Json.obj("name" -> shortFormProvider.getShortForm(a), "iri" -> a.getIRI.toString))
              val isi:JsValue = Json.toJson(propertyAndSubjects)
              js + ("class_property" -> isi)
            }
          }
        })
    val js2:JsObject = js + ("inverse_object" -> Json.toJson( inverse_property_to_individuals.map{
      case (ip, individuals) =>
        (ip.getIRI.getShortForm,
          individuals
            .map(a => Json.obj("name" -> shortFormProvider.getShortForm(a), "iri" -> a.getIRI.toString)))
    }.toMap))
    js2
  }

  private class DLQueryParser(val rootOntology: OWLOntology,
                              val bidiShortFormProvider : BidirectionalShortFormProvider) {

    def this(rootOntology : OWLOntology,shortFormProvider : ShortFormProvider) {
      this(rootOntology, {
        val manager = rootOntology.getOWLOntologyManager()
        val importsClosure = rootOntology.getImportsClosure()
        val bidiShortFormProvider = new BidirectionalShortFormProviderAdapter(
          manager, importsClosure, shortFormProvider)
        bidiShortFormProvider
      })
    }

    def parseClassExpression(classExpressionString: String ): OWLClassExpression = {
      val dataFactory = rootOntology.getOWLOntologyManager()
        .getOWLDataFactory();
      // Set up the real parser
      val parser = new ManchesterOWLSyntaxEditorParser(
        dataFactory, classExpressionString);
      parser.setDefaultOntology(rootOntology);
      // Specify an entity checker that wil be used to check a class
      // expression contains the correct names.
      val entityChecker = new ShortFormEntityChecker(
        bidiShortFormProvider);
      parser.setOWLEntityChecker(entityChecker);
      // Do the actual parsing
      return parser.parseClassExpression();
    }
  }
}
