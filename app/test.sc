import java.io.{File, FileInputStream, InputStream}

import org.apache.jena.ontology._
import org.apache.jena.riot._

import scala.collection.JavaConversions._
import org.apache.jena.rdf.model.{ModelFactory, Statement}
import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntaxEditorParser
import org.semanticweb.HermiT.Reasoner
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.expression.{OWLEntityChecker, ShortFormEntityChecker}
import org.semanticweb.owlapi.io.StringDocumentSource
import play.api.libs.json._
import org.semanticweb.owlapi.manchestersyntax.parser.ManchesterOWLSyntaxParserImpl
import org.semanticweb.owlapi.model._
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory
import org.semanticweb.owlapi.util.{BidirectionalShortFormProvider, BidirectionalShortFormProviderAdapter, ShortFormProvider, SimpleShortFormProvider}

val owlfile = new File("D:\\Coba PLAY\\ontology-wisata-bandung\\conf\\wisata.owl")
val is = new FileInputStream(owlfile)

val manager = OWLManager.createOWLOntologyManager();
val ontology = manager
  .loadOntologyFromOntologyDocument(owlfile);

println(ontology.getOntologyID)

val reasoner = new Reasoner.ReasonerFactory().createReasoner(ontology);

val shortFormProvider = new SimpleShortFormProvider();

class DLQueryParser(val rootOntology: OWLOntology,
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

val parser = new DLQueryParser(ontology,shortFormProvider)


val classExpressionString = "Pariwisata"

val classExpression = parser
  .parseClassExpression(classExpressionString);
val individuals = reasoner.getInstances(classExpression, false);
individuals.getFlattened();
val individual = individuals.getFlattened().toList(0)
ontology.axioms(individual).iterator().toList
  .groupBy(axiom => axiom.getAxiomType).toList
  .filter(_ match {
    case (AxiomType.OBJECT_PROPERTY_ASSERTION, _) => true
    case (AxiomType.DATA_PROPERTY_ASSERTION, _) => true
    case _ => false
  })
  .map ( _ match {
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
                .map(a => shortFormProvider.getShortForm(a)))
        })
      (AxiomType.OBJECT_PROPERTY_ASSERTION,propertyAndSubjects)
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
      (AxiomType.DATA_PROPERTY_ASSERTION,propertyAndSubjects)
    }
  })
