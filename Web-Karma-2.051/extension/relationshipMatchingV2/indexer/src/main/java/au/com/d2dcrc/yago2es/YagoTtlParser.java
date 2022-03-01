package au.com.d2dcrc.yago2es;

import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.parser.NxParser;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

/**
 * Created by Zaiwen FENG on 26/06/2017.
 */
/**Aim to parse Yago fact file in the form of .ttl**/
public class YagoTtlParser {

    /**This inner class define the fields that are parsed from yagoSchema.ttl**/
    public class RelationValue {

        public String subject = "";
        public String predicate = "";
        public String obj = "";

        /**constructor*
         * @param subject subject of the triple
         * @param predicate predicate of the triple
         * @param obj object of the triple
         * */
        public RelationValue (String subject, String predicate, String obj) {

            this.subject = subject;
            this.predicate = predicate;
            this.obj = obj;
        }

        public String getSubject () {

            return subject;
        }

        public String getPredicate () {

            return predicate;
        }

        public String getObj () {

            return obj;
        }
    }

    /**Parse the YAGO facts in the form of .ttl *
     * @param filePath path of the Yago facts file
     * @return a list of Yago facts
     * */
    public List<YagoFact> parseYagoFacts (String filePath) {

        List<YagoFact> yagoFacts = new ArrayList<YagoFact>();

        long item = 0L;

        try {

            FileInputStream is = new FileInputStream(filePath);

            NxParser nxp = new NxParser();

            nxp.parse(is);

            while (nxp.hasNext()) {

                /**Get a triple**/
                Node[] nx = nxp.next();
                String subject = nx[0].toString();
                subject = subject.substring(1, subject.length() - 1);
                String predicate = nx[1].toString();
                predicate = predicate.substring(1, predicate.length() - 1);
                String object = nx[2].toString();
                object = object.substring(1, object.length() - 1);

                YagoFact yagoFact = new YagoFact(subject, predicate, object);
                yagoFacts.add(yagoFact);

                System.out.println("Now we have done " + item++ + " entries");
            }


        }catch (IOException e) {

            e.printStackTrace();
        }
        return yagoFacts;
    }

    /**Parse Yago Schemas, and extract the relationships and related information from yagoSchema.tll*
     * @param filePath path of the Yago schema file
     * @return a map describing the correspondence from relationships to their domain and range
     * */
    public Map<String, YagoRelationship> parseYagoSchema (String filePath) {

        List<RelationValue> relationValues = new ArrayList<>();
        Map<String, YagoRelationship> yagoRelationshipHashMap = new HashMap<>();
        List<String> redundantRelationsList = new ArrayList<String>();

        try {

            FileInputStream is = new FileInputStream(filePath);

            NxParser nxp = new NxParser();

            nxp.parse(is);

            while (nxp.hasNext()) {

                /**Get a triple**/
                Node[] nx = nxp.next();
                String subject = nx[0].toString();
                subject = subject.substring(1, subject.length()-1);

                String predicate = nx[1].toString();

                String obj = nx[2].toString();

                redundantRelationsList.add(subject);
                RelationValue relationValue = new RelationValue(subject, predicate, obj);
                relationValues.add(relationValue);
            }

            /**Begin to build the Yago Schema Map...**/

            /**First, remove all the duplicated relations**/
            Set<String> allRelations = new HashSet<String>(redundantRelationsList);

            Iterator<String> it = allRelations.iterator();
            while (it.hasNext()){

                String relation = it.next();

                /**Build a new Yago Relationship object**/
                YagoRelationship yagoRelationship = new YagoRelationship();

                /**Set the name to this relationship**/
                yagoRelationship.setRelationName(relation);

                for (RelationValue relationValue : relationValues) {

                    if (relationValue.getSubject().equals(relation)) {

                        if (relationValue.getPredicate().equals("<rdfs:domain>")) {

                            /**Set the domain to this relationship**/
                            yagoRelationship.setDomain(relationValue.getObj());

                        }else if (relationValue.getPredicate().equals("<rdfs:range>")) {

                            /**Set the range to this relationship**/
                            yagoRelationship.setRange(relationValue.getObj());

                        }else if (relationValue.getPredicate().equals("<rdf:type>")) {

                            /**Set the type to this relationship**/
                            yagoRelationship.setType(relationValue.getObj());

                        }
                    }
                }

                yagoRelationshipHashMap.put(relation, yagoRelationship);
            }
        }catch (IOException e) {

            e.printStackTrace();
        }
        return yagoRelationshipHashMap;
    }

    /**Parse Yago Types*
     * @param filePath the file directory of yagoSimpleTypes.ttl
     * @return a list of pairs of instances and corresponding semantic types
     * */
    public List<RelationValue> parseYagoSimpleTypes (String filePath) {

        List<RelationValue> yagoTypeList = new ArrayList<RelationValue>();

        long item = 0L;

        try {
            FileInputStream is = new FileInputStream(filePath);

            NxParser nxp = new NxParser();

            nxp.parse(is);

            while (nxp.hasNext()) {

                /**Get a triple**/
                Node[] nx = nxp.next();
                String subject = nx[0].toString();
                subject = subject.substring(1, subject.length()-1);

                String predicate = nx[1].toString();

                String obj = nx[2].toString();

                RelationValue relationValue = new RelationValue(subject, predicate, obj);
                yagoTypeList.add(relationValue);

                System.out.println("Now we have done " + item++ + " type entries");
            }

        }catch (IOException e) {

            e.printStackTrace();
        }

        return yagoTypeList;
    }

    /**Parse Yago Taxonomy*
     * @param filePath the file directory of yagoTaxonomy.ttl
     * @return a list of pairs of instances and corresponding semantic types
     * */
    public List<RelationValue> parseYagoTaxonomy (String filePath) {

        List<RelationValue> yagoTaxonomyList = new ArrayList<RelationValue>();

        long item = 0L;

        try {
            FileInputStream is = new FileInputStream(filePath);

            NxParser nxp = new NxParser();

            nxp.parse(is);

            while (nxp.hasNext()) {

                /**Get a triple**/
                Node[] nx = nxp.next();
                String subject = nx[0].toString();

                String predicate = nx[1].toString();

                String obj = nx[2].toString();

                RelationValue relationValue = new RelationValue(subject, predicate, obj);
                yagoTaxonomyList.add(relationValue);

                System.out.println("Now we have done " + item++ + " taxonomy entries");
            }

        }catch (IOException e) {

            e.printStackTrace();
        }

        return yagoTaxonomyList;
    }
}
