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

/**
 * <p>Definition of Edge for dependency graphs</p>
 *
 * <p>Edge is then feeded into graphs of the JUNG library</p>
 *
 * <p>The Edge definition can be modified based on one's own needs</p>
 *
 * @author Implemented by Haibin Liu and Tested by Philippe Thomas
 *
 */
public class Edge {
    /** governor token */
    private Vertex gov;

    /** Edge label */
    private String label;

    /** dependent token */
    private Vertex dep;

    /**
     * Construtor to initialize the class field
     * @param gov governor node of the edge
     * @param dep  dependent node of the edge
     * @param label label of the edge
     */
    public Edge (Vertex gov, String label, Vertex dep) {
        this.gov = gov;
        this.label = label;
        this.dep = dep;
    }

    /**
     * retrieve dependent node of the edge
     * @return dependent node
     */
    public Vertex getDependent() {
        return dep;
    }

    /**
     * retrieve governor node of the edge
     * @return governor node
     */
    public Vertex getGovernor() {
        return gov;
    }

    /**
     * retrieve edge label
     * @return edge label
     */
    public String getLabel() {
        return label;
    }


    /**
     * print edge content
     */
    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append(label);
        sb.append("(");
        sb.append(gov.toString());
        sb.append(", ");
        sb.append(dep.toString());
        sb.append(")");

        return sb.toString();
    }
}

