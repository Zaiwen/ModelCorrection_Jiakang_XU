package acrc.itms.unisa;

import org.apache.log4j.Logger;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.rep.alignment.LabeledLink;

import java.awt.*;
import java.util.*;
import java.util.List;

/**artificial knowledge graph generation...... *
 * @from 19 Sep 2018
 * @author Zaiwen FENG
 * */


public class Generator {

    static Logger logger = Logger.getLogger(Generator.class.getName());


    /**Generate a random knowledge graph based on the CDM*
     * @param n the # of the KG
     * @param commonDataModel cdm
     * @param nodeLabelMap a map recording the correspondence between string label (like: Person) and integer label (like: 9)
     *     @param edgeLabelMap a map recording the correspondence between string label (like: employs) and integer label (like: 9)
     * @From 13 Nov 2018
     * @revised 12 Feb 2019
     * @Return a synthetic knowledge graph
     * */
    public static DirectedWeightedMultigraph<Node, LabeledLink> KGExample2 (int n, CommonDataModel commonDataModel, Map<String, Integer> nodeLabelMap
    , Map<String,Integer> edgeLabelMap) throws Exception {

        VerticesPool vp = new VerticesPool();//create a vertices pool
        vp.init(100000,commonDataModel, nodeLabelMap);//init the pool
        System.out.println("vertices pool has been set up...");

        Map<String, Integer> proportion = new HashMap<>();//set the proportion of different entity types
        proportion.put(nodeLabelMap.get("Medicare").toString(),300);//Medicare account
        proportion.put(nodeLabelMap.get("Insurance_production").toString(),20);//Insurance Production
        proportion.put(nodeLabelMap.get("Car_leasing_company").toString(),2);//Car Leasing Company
        proportion.put(nodeLabelMap.get("Working_position").toString(),220);//Faculty Position
        proportion.put(nodeLabelMap.get("Vehicle").toString(),550);//Vehicle
        proportion.put(nodeLabelMap.get("Tax_office").toString(),2);//Tax Office
        proportion.put(nodeLabelMap.get("Bank_account").toString(),1000);//Bank Account
        proportion.put(nodeLabelMap.get("University").toString(),2);//University
        proportion.put(nodeLabelMap.get("Parking_lot").toString(),10);//Parking lot
        proportion.put(nodeLabelMap.get("Insurance_company").toString(),5);//insurance company
        proportion.put(nodeLabelMap.get("Gym").toString(),3);//gym
        proportion.put(nodeLabelMap.get("Hospital").toString(),1);//hospital
        proportion.put(nodeLabelMap.get("Person").toString(),400);//Person
        proportion.put(nodeLabelMap.get("Location").toString(),180);//Location

        /**Set up pattern factory to generate dependent structural patterns for each relationship type. Added on 12 Feb 2019**/
        PatternFactory patternFactory = new PatternFactory();

        RelationshipProp r1 = new RelationshipProp();//r1-person--lives in----location
        r1.setName(edgeLabelMap.get("lives_in").toString());

        RelationshipProp r2 = new RelationshipProp();//r2 university---employs----person
        r2.setName(edgeLabelMap.get("employs").toString());
        r2.setPercentageT(0.3);//constraint: only 30% persons are employed by the universities

        RelationshipProp r3 = new RelationshipProp();//r3 person---studies in---university
        r3.setName(edgeLabelMap.get("studied_in").toString());

        RelationshipProp r4 = new RelationshipProp();//r4 person---opens----bank account
        r4.setName(edgeLabelMap.get("opens").toString());
        r4.setWeightedOption(1);//incremental strategy

        RelationshipProp r5 = new RelationshipProp();//r5 person---links with----medicare
        r5.setName(edgeLabelMap.get("links_with").toString());

        RelationshipProp r6 = new RelationshipProp();//r6 university---provides---faculty_position
        r6.setName(edgeLabelMap.get("provides").toString());

        RelationshipProp r7 = new RelationshipProp();//r7 person---is in----faculty_position
        r7.setName(edgeLabelMap.get("is_in").toString());

        RelationshipProp r8 = new RelationshipProp();//r8 university----transfers to----bank account
        r8.setName(edgeLabelMap.get("transfers_to").toString());
        /**     * PATTERN DESCRIPTION:
         * UNIVERSITY EMPLOYS A PERSON, WHO OPENS SOME BANK ACCOUNTS,
         * UNIVERSITY WILL TRANSFER MONEY TO ONE OF THEM
         * PATTERN TYPE:
         * UNIQUE CYCLING DEPENDENCY**/
        Pattern C1 = patternFactory.generatePattern_C1(nodeLabelMap,edgeLabelMap);
        r8.addDependentPattern(C1);

        RelationshipProp r9 = new RelationshipProp();//r9 Person----pays tax to---Tax Office
        r9.setName(edgeLabelMap.get("pays_tax_for").toString());

        RelationshipProp r10 = new RelationshipProp();//r10 Tax Office---links account to---Bank Account
        r10.setName(edgeLabelMap.get("links_account_to").toString());

        RelationshipProp r11 = new RelationshipProp();//r11 Insurance Production---product of---Insurance Company
        r11.setName(edgeLabelMap.get("product_of").toString());

        RelationshipProp r12 = new RelationshipProp();//r12 Insurance Company---links to---Bank Account
        r12.setName(edgeLabelMap.get("links_to").toString());

        RelationshipProp r13 = new RelationshipProp();//r13 Person---owns---Vehicle
        r13.setName(edgeLabelMap.get("owns").toString());
        r13.setPercentageT(0.7);//constraint: persons only owns 70% cars

        RelationshipProp r14 = new RelationshipProp();//r14 Vehicles---owned by----Car leasing Company
        r14.setName(edgeLabelMap.get("owned_by").toString());

        RelationshipProp r15 = new RelationshipProp();//r15 car leasing company ---around---location
        r15.setName(edgeLabelMap.get("around").toString());

        RelationshipProp r16 = new RelationshipProp();//r16 person---rents---vehicles
        r16.setName(edgeLabelMap.get("rents").toString());

        RelationshipProp r17 = new RelationshipProp();//r17 Parking lot---close to---Location
        r17.setName(edgeLabelMap.get("closes_to").toString());
        r17.setWeightedOption(1);//incremental strategy

        RelationshipProp r18 = new RelationshipProp();//r18 Vehicle---garaged at----Parking Lot
        r18.setName(edgeLabelMap.get("garaged_at").toString());

        RelationshipProp r19 = new RelationshipProp();//r19 Person---registered in---Gym
        r19.setName(edgeLabelMap.get("registered_in").toString());

        RelationshipProp r20 = new RelationshipProp();//r20 Hospital---next to----Location
        r20.setName(edgeLabelMap.get("next_to").toString());

        RelationshipProp r21 = new RelationshipProp();// Person---see doctor in---Hospital
        r21.setName(edgeLabelMap.get("sees_doctor").toString());


        Map<String, RelationshipProp> relationshipPropMap = new HashMap<>();
        relationshipPropMap.put(edgeLabelMap.get("lives_in").toString(),r1);
        relationshipPropMap.put(edgeLabelMap.get("employs").toString(),r2);
        relationshipPropMap.put(edgeLabelMap.get("studied_in").toString(),r3);
        relationshipPropMap.put(edgeLabelMap.get("opens").toString(),r4);
        relationshipPropMap.put(edgeLabelMap.get("links_with").toString(),r5);
        relationshipPropMap.put(edgeLabelMap.get("provides").toString(),r6);
        relationshipPropMap.put(edgeLabelMap.get("is_in").toString(),r7);
        relationshipPropMap.put(edgeLabelMap.get("transfers_to").toString(),r8);
        relationshipPropMap.put(edgeLabelMap.get("pays_tax_for").toString(),r9);
        relationshipPropMap.put(edgeLabelMap.get("links_account_to").toString(),r10);
        relationshipPropMap.put(edgeLabelMap.get("product_of").toString(),r11);
        relationshipPropMap.put(edgeLabelMap.get("links_to").toString(),r12);
        relationshipPropMap.put(edgeLabelMap.get("owns").toString(),r13);
        relationshipPropMap.put(edgeLabelMap.get("owned_by").toString(),r14);
        relationshipPropMap.put(edgeLabelMap.get("around").toString(),r15);
        relationshipPropMap.put(edgeLabelMap.get("rents").toString(),r16);
        relationshipPropMap.put(edgeLabelMap.get("closes_to").toString(),r17);
        relationshipPropMap.put(edgeLabelMap.get("garaged_at").toString(),r18);
        relationshipPropMap.put(edgeLabelMap.get("registered_in").toString(),r19);
        relationshipPropMap.put(edgeLabelMap.get("next_to").toString(), r20);
        relationshipPropMap.put(edgeLabelMap.get("sees_doctor").toString(), r21);


        List<String> relationshipOrder = new ArrayList<>();/**Create the relationship order**/
        /****Step 1****/
        relationshipOrder.add(edgeLabelMap.get("links_to").toString());//R12
        relationshipOrder.add(edgeLabelMap.get("pays_tax_for").toString());//r9
        relationshipOrder.add(edgeLabelMap.get("lives_in").toString());//r1
        relationshipOrder.add(edgeLabelMap.get("employs").toString());//r2
        relationshipOrder.add(edgeLabelMap.get("studied_in").toString());//r3
        relationshipOrder.add(edgeLabelMap.get("opens").toString());//r4
        relationshipOrder.add(edgeLabelMap.get("owns").toString());//r13
        relationshipOrder.add(edgeLabelMap.get("owned_by").toString());//r14
        relationshipOrder.add(edgeLabelMap.get("links_with").toString());//r5
        relationshipOrder.add(edgeLabelMap.get("provides").toString());//r6
        relationshipOrder.add(edgeLabelMap.get("product_of").toString());//r11
        relationshipOrder.add(edgeLabelMap.get("around").toString());//r15
        relationshipOrder.add(edgeLabelMap.get("closes_to").toString());//r17
        relationshipOrder.add(edgeLabelMap.get("registered_in").toString());//r19
        relationshipOrder.add(edgeLabelMap.get("next_to").toString());//r20


        /****Step 2****/
        relationshipOrder.add(edgeLabelMap.get("transfers_to").toString());//r8
//        relationshipOrder.add(edgeLabelMap.get("is_in").toString());//r7
//        relationshipOrder.add(edgeLabelMap.get("links_account_to").toString());//r10
//        relationshipOrder.add(edgeLabelMap.get("rents").toString());//r16
//        relationshipOrder.add(edgeLabelMap.get("sees_doctor").toString());//r21


        /****Step 3*******/
//        relationshipOrder.add(edgeLabelMap.get("garaged_at").toString());

        /**Create KG**/
        KnowledgeGraph knowledgeGraph = commonDataModel.generateKG2(vp,n,proportion,relationshipOrder,relationshipPropMap, 1000);//generate an example KG
        DirectedWeightedMultigraph<Node, LabeledLink> kg = knowledgeGraph.getKg();
        DirectedWeightedMultigraph<Node, LabeledLink> newKG = new DirectedWeightedMultigraph<>(LabeledLink.class);
        Utilities.shuffleIds(kg, newKG);//shuffle ids of nodes of the knowledge graph to adapt gephi
        knowledgeGraph.setKg(newKG);
        System.out.println("KG is built...");
        String kgName = "kg20190211_N2000";
        knowledgeGraph.serializeKG(newKG,kgName.concat(".lg"));
        System.out.println("KG is serialized...");

        /**3.....Visualize the KG.....**/
        Map<String,Color> colorMap = commonDataModel.getColorMap();
        Map<String,Color> tempColorMap = new HashMap<>();
        Iterator it3 = colorMap.entrySet().iterator();
        while(it3.hasNext()){
            Map.Entry pair = (Map.Entry)it3.next();
            String oldLabel = (String)pair.getKey();
            Color color = (Color) pair.getValue();
            tempColorMap.put(commonDataModel.getNodeLabelMap().get(oldLabel).toString(),color);//update key of 'colorMap'
        }
        GephiUtilities.convertToGephi(newKG, Settings.GephiFileAddress, kgName.concat(".gexf"), tempColorMap);
        System.out.println("viz file is built...");
        logger.debug("This is the log4j logging");

        return newKG;
    }

    public static void main (String args[]) {
        try{

            CommonDataModel commonDataModel = new CommonDataModel(1,new DirectedWeightedMultigraph<>(LabeledLink.class), new HashMap<>(), new HashMap<>(), new HashMap<>());
            commonDataModel.generateCDMExample2();//generate an example cdm
            commonDataModel.shuffleLabels();//shuffle the edge label and node label of the CDM
            Map<String, Integer> nodeLabelMap = commonDataModel.getNodeLabelMap();
            Map<String, Integer> edgeLabelMap = commonDataModel.getEdgeLabelMap();
            Utilities.printMap(commonDataModel.getNodeLabelMap());//print all the node labels
            Utilities.printMap(commonDataModel.getEdgeLabelMap());//print out all the edge labels
            System.out.println("CDM is successfully built...");
            long start = System.currentTimeMillis();
            DirectedWeightedMultigraph<Node, LabeledLink> kg = KGExample2(2000, commonDataModel, nodeLabelMap, edgeLabelMap);
            System.out.println("done!");
            long elapsedTimeMills = System.currentTimeMillis() - start;
            float elapsedTimeSec = elapsedTimeMills/1000F;
            System.out.println("the time for generating is: " + elapsedTimeSec + " s!");
        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
