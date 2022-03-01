/*
 Copyright (c) 2012, Regents of the University of Colorado
 All rights reserved.

 Redistribution and use in source and binary forms, with or without modification,
 are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, this
    list of conditions and the following disclaimer.

 * Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 * Neither the name of the University of Colorado nor the names of its
    contributors may be used to endorse or promote products derived from this
    software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package ESM;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>Definition of Vertex for dependency graphs</p>
 *
 * <p>Vertex is then feeded into graphs of the JUNG library</p>
 *
 * <p>The Vertex definition can be modified based on one's own needs</p>
 *
 * @author Implemented by Haibin Liu and Tested by Philippe Thomas
 *
 */
public class Vertex {
    /** original entire token including position number and POS tag */
    private String token;

    /** sentence token only */
    private String word;

    /** lemma of word (i.e. label. 2019.02.25)*/
    private String lemma;

    /** POS tag */
    private String tag;

    /** token position (i.e. id. 2019.02.25)*/
    private int pos;

    /** annotation of the node */
    private String annotation;

    /** for quick POS comparison */
    private String generalizedPOS;

    /** for quick node comparison */
    private String compareForm;

    /**
     * Construtor to initialize the class field
     * @param token token of the vertex
     */
    public Vertex (String token) {
        this.token = token;
        Matcher m = Pattern.compile("^(.+)-(\\d+)\\x27*\\/(.+)$").matcher(token);
        if(!m.find())
            throw new RuntimeException("The node: "
                    + token + " is not valid. Please check.");
        word = m.group(1);
        pos = Integer.parseInt( m.group(2) );
        tag = m.group(3);
        annotation = "Component";
    }

    /**
     * default Construtor to initialize the class fields to empty
     */
    public Vertex () {
        annotation = "";
        compareForm = "";
        generalizedPOS = "";
        lemma = "";
        pos = 0;
        tag = "";
        token = "";
        word = "";
    }

    /**Constructor with node Id as a parameter*
     * @param id of the node
     * @from 18 Jan 2019
     * */
    public Vertex (int id, String t) {
        annotation = "Component";
        //compareForm = "";
        //generalizedPOS = "";
        //lemma = "";
        pos = id;
        tag = t;
        word = "BIO_Entity";
        token = word.concat("-").concat(String.valueOf(id).concat("/").concat(tag));

    }

    /**
     * use one node's information to update another node
     */
    protected void update (Vertex target) {
        annotation = target.annotation;
        compareForm = target.compareForm;
        generalizedPOS = target.generalizedPOS;
        lemma = target.lemma;
        pos = target.pos;
        tag = target.tag;
        token = target.token;
        word = target.word;
    }

    /**
     * retrieve original token of the node
     * @return original token
     */
    public String getToken() {
        return token;
    }

    /**
     * retrieve the word of the node
     * @return word
     */
    public String getWord() {
        return word;
    }

    /**
     * retrieve the POS tag of the node
     * @return POS tag
     */
    public String getTag() {
        return tag;
    }

    /**
     * retrieve the pos of the node
     * @return pos
     * @from 18 Jan 2019
     * @comment pos is the id of the node
     * **/
    public int getPos() { return pos; }

    /**
     * set lemma for the node
     * @param lemma lemma of the node
     */
    public void setLemma(String lemma) {
        this.lemma = lemma;
    }

    /**
     * retrieve the lemma of the node
     * @return lemma
     * @from 27 Jan 2019
     * **/
    public String getLemma() {return lemma;}

    /**
     * retrieve the comparison form of the node
     * @return comparison form
     */
    public String getCompareForm() {
        return compareForm;
    }

    /**
     * set the comparison form of the node
     * @param compareForm compare Form the node
     */
    public void setCompareForm(String compareForm) {
        this.compareForm = compareForm;
    }

    /**
     * set the generalized POS tag of the node
     * @param generalizedPOS generalized POS tag of the node
     */
    public void setGeneralizedPOS(String generalizedPOS) {
        this.generalizedPOS = generalizedPOS;
    }


    /**
     * print node content
     */
    @Override
    public String toString() {
        return word +"-" +pos +"/" +tag;
    }
}
